import { api } from './cliente';
import type {
  Agencia,
  Conta,
  EntidadeAuditada,
  Movimentacao,
  Page,
  Pessoa,
  RegistroAuditoria,
  Stats,
} from './tipos';

function consulta(params: Record<string, string | number | undefined | null>): string {
  const partes = Object.entries(params)
    .filter(([, v]) => v !== undefined && v !== null && v !== '')
    .map(([k, v]) => `${k}=${encodeURIComponent(String(v))}`);
  return partes.length ? `?${partes.join('&')}` : '';
}

const get = <T>(caminho: string) => api.requisitar<T>('GET', caminho);
const post = <T>(caminho: string, corpo?: unknown) => api.requisitar<T>('POST', caminho, corpo);
const put = <T>(caminho: string, corpo: unknown) => api.requisitar<T>('PUT', caminho, corpo);
const del = (caminho: string) => api.requisitar<void>('DELETE', caminho);

// Contas
export const contas = {
  listar: (page = 0, busca = '', size = 20) => get<Page<Conta>>(`/api/contas${consulta({ page, size, busca })}`),
  stats: () => get<Stats>('/api/contas/stats'),
  buscar: (id: number) => get<Conta>(`/api/contas/${id}`),
  extrato: (id: number, page = 0, de?: string) =>
    get<Page<Movimentacao>>(`/api/contas/${id}/extrato${consulta({ page, size: 20, de })}`),
  extratoCsv: (id: number, de?: string) => api.baixarTexto(`/api/contas/${id}/extrato.csv${consulta({ de })}`),
  criar: (dados: { numero: number; limiteChequeEspecial: number; saldoInicial: number; pessoaId: number; agenciaId: number }) =>
    post<Conta>('/api/contas', dados),
  atualizar: (id: number, dados: { limiteChequeEspecial: number; agenciaId: number }) => put<Conta>(`/api/contas/${id}`, dados),
  encerrar: (id: number) => del(`/api/contas/${id}`),
  depositar: (id: number, valor: number) => post<Conta>(`/api/contas/${id}/depositar`, { valor }),
  sacar: (id: number, valor: number) => post<Conta>(`/api/contas/${id}/sacar`, { valor }),
  transferir: (id: number, numeroContaDestino: number, valor: number) =>
    post<Conta>(`/api/contas/${id}/transferir`, { numeroContaDestino, valor }),
};

// Clientes (ADMIN)
export const pessoas = {
  listar: (page = 0, busca = '') => get<Page<Pessoa>>(`/api/pessoas${consulta({ page, size: 20, busca })}`),
  criar: (dados: { cpf: string; nome: string; idade: number }) =>
    post<{ pessoa: Pessoa; senhaTemporaria: string }>('/api/pessoas', dados),
  atualizar: (id: number, dados: { nome: string; idade: number }) => put<Pessoa>(`/api/pessoas/${id}`, dados),
  excluir: (id: number) => del(`/api/pessoas/${id}`),
  alterarBloqueio: (id: number, ativo: boolean) => put<Pessoa>(`/api/pessoas/${id}/bloqueio`, { ativo }),
  redefinirSenha: (id: number) => post<{ cpf: string; senhaTemporaria: string }>(`/api/pessoas/${id}/redefinir-senha`),
};

// Agências (ADMIN)
export const agencias = {
  listar: (page = 0, busca = '') => get<Page<Agencia>>(`/api/agencias${consulta({ page, size: 20, busca })}`),
  salvar: (dados: Omit<Agencia, 'id'>, id?: number) =>
    id ? put<Agencia>(`/api/agencias/${id}`, dados) : post<Agencia>('/api/agencias', dados),
  excluir: (id: number) => del(`/api/agencias/${id}`),
};

// Auditoria (ADMIN)
export const auditoria = {
  listar: (page = 0, entidade: EntidadeAuditada | '' = '') =>
    get<Page<RegistroAuditoria>>(`/api/auditoria${consulta({ page, size: 20, entidade })}`),
};
