import { API_URL } from '@/config';
import { armazenamento } from './armazenamento';
import type { TokenResponse, Usuario } from './tipos';

/** Erro devolvido pela API no formato padrão (ApiError.java). */
export class ErroApi extends Error {
  constructor(
    readonly status: number,
    mensagem: string,
    readonly codigo?: string,
    readonly campos?: Record<string, string>,
  ) {
    super(mensagem);
  }
}

type Ouvinte = (usuario: Usuario | null) => void;

/**
 * Sessão do app: o access token (15 min) fica só em memória; o refresh token fica no
 * SecureStore. Quando a API responde 401, renova o token e repete a requisição uma vez.
 */
class ClienteApi {
  private accessToken: string | null = null;
  private renovacao: Promise<string> | null = null;
  private ouvintes = new Set<Ouvinte>();

  /** Avisado quando a sessão muda (login, renovação, logout, sessão expirada). */
  aoMudarSessao(ouvinte: Ouvinte): () => void {
    this.ouvintes.add(ouvinte);
    return () => this.ouvintes.delete(ouvinte);
  }

  async login(cpf: string, senha: string): Promise<Usuario> {
    const res = await this.enviar<TokenResponse>('POST', '/api/auth/login', { cpf, senha }, false);
    await this.salvarSessao(res);
    return res.usuario;
  }

  /** Ao abrir o app: se houver refresh token salvo, recupera a sessão. */
  async restaurar(): Promise<Usuario | null> {
    if (!(await armazenamento.ler())) return null;
    try {
      await this.renovar();
      return await this.requisitar<Usuario>('GET', '/api/auth/me');
    } catch {
      return null;
    }
  }

  async logout(): Promise<void> {
    const refreshToken = await armazenamento.ler();
    if (refreshToken) {
      this.enviar('POST', '/api/auth/logout', { refreshToken }, false).catch(() => {});
    }
    await this.encerrar();
  }

  /** Troca a senha; a API encerra todas as sessões, então o usuário entra de novo. */
  async trocarSenha(senhaAtual: string, novaSenha: string): Promise<void> {
    await this.requisitar('PUT', '/api/auth/senha', { senhaAtual, novaSenha });
    await this.encerrar();
  }

  /** Requisição autenticada. Em caso de 401, renova a sessão e tenta mais uma vez. */
  async requisitar<T>(metodo: string, caminho: string, corpo?: unknown): Promise<T> {
    try {
      return await this.enviar<T>(metodo, caminho, corpo, true);
    } catch (erro) {
      if (!(erro instanceof ErroApi) || erro.status !== 401) throw erro;
      try {
        await this.renovar();
      } catch {
        await this.encerrar();
        throw erro;
      }
      return this.enviar<T>(metodo, caminho, corpo, true);
    }
  }

  /** Baixa um texto (ex.: o extrato em CSV) com o token. */
  async baixarTexto(caminho: string): Promise<string> {
    const resposta = await fetch(API_URL + caminho, { headers: this.cabecalhos(true) });
    if (resposta.status === 401) {
      await this.renovar();
      return this.baixarTexto(caminho);
    }
    if (!resposta.ok) throw await this.erroDe(resposta);
    return resposta.text();
  }

  /**
   * O refresh token só vale uma vez: se várias requisições derem 401 juntas,
   * todas esperam a mesma renovação.
   */
  private renovar(): Promise<string> {
    this.renovacao ??= (async () => {
      try {
        const refreshToken = await armazenamento.ler();
        if (!refreshToken) throw new ErroApi(401, 'Sessão expirada');
        const res = await this.enviar<TokenResponse>('POST', '/api/auth/refresh', { refreshToken }, false);
        await this.salvarSessao(res);
        return res.token;
      } finally {
        this.renovacao = null;
      }
    })();
    return this.renovacao;
  }

  private async enviar<T>(metodo: string, caminho: string, corpo: unknown, autenticado: boolean): Promise<T> {
    let resposta: Response;
    try {
      resposta = await fetch(API_URL + caminho, {
        method: metodo,
        headers: this.cabecalhos(autenticado, corpo !== undefined),
        body: corpo !== undefined ? JSON.stringify(corpo) : undefined,
      });
    } catch {
      throw new ErroApi(0, `Sem conexão com o servidor (${API_URL}). Confira se a API está rodando e se o celular está na mesma rede.`);
    }
    if (!resposta.ok) throw await this.erroDe(resposta);
    if (resposta.status === 204) return undefined as T;
    return (await resposta.json()) as T;
  }

  private cabecalhos(autenticado: boolean, json = false): Record<string, string> {
    const h: Record<string, string> = {};
    if (json) h['Content-Type'] = 'application/json';
    if (autenticado && this.accessToken) h.Authorization = `Bearer ${this.accessToken}`;
    return h;
  }

  private async erroDe(resposta: Response): Promise<ErroApi> {
    try {
      const corpo = await resposta.json();
      const campos = corpo.campos as Record<string, string> | undefined;
      const mensagem = campos ? Object.values(campos).join(' · ') : corpo.mensagem;
      return new ErroApi(resposta.status, mensagem ?? 'Algo deu errado.', corpo.codigo, campos);
    } catch {
      return new ErroApi(resposta.status, 'Algo deu errado. Tente novamente.');
    }
  }

  private async salvarSessao(res: TokenResponse): Promise<void> {
    this.accessToken = res.token;
    await armazenamento.salvar(res.refreshToken);
    this.ouvintes.forEach((o) => o(res.usuario));
  }

  private async encerrar(): Promise<void> {
    this.accessToken = null;
    await armazenamento.apagar();
    this.ouvintes.forEach((o) => o(null));
  }
}

export const api = new ClienteApi();

export function mensagemDeErro(erro: unknown): string {
  return erro instanceof ErroApi ? erro.message : 'Algo deu errado. Tente novamente.';
}
