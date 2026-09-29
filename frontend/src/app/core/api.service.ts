import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { environment } from '../../environments/environment';
import {
  Agencia,
  Conta,
  EntidadeAuditada,
  Movimentacao,
  Page,
  Pessoa,
  PessoaCriada,
  RegistroAuditoria,
  SenhaRedefinida,
  Stats,
} from './models';

/** Endereço da API: vazio na web (mesmo domínio), completo no app de celular. */
export const API = `${environment.apiUrl}/api`;

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);

  private params(page: number, size: number, extra: Record<string, string | null | undefined> = {}) {
    let p = new HttpParams().set('page', page).set('size', size);
    for (const [k, v] of Object.entries(extra)) if (v) p = p.set(k, v);
    return p;
  }

  // Pessoas (ADMIN)
  listarPessoas(page = 0, busca = '', size = 10) {
    return this.http.get<Page<Pessoa>>(`${API}/pessoas`, { params: this.params(page, size, { busca }) });
  }
  criarPessoa(dados: { cpf: string; nome: string; idade: number }) {
    return this.http.post<PessoaCriada>(`${API}/pessoas`, dados);
  }
  atualizarPessoa(id: number, dados: { nome: string; idade: number }) {
    return this.http.put<Pessoa>(`${API}/pessoas/${id}`, dados);
  }
  removerPessoa(id: number) {
    return this.http.delete<void>(`${API}/pessoas/${id}`);
  }
  alterarBloqueio(id: number, ativo: boolean) {
    return this.http.put<Pessoa>(`${API}/pessoas/${id}/bloqueio`, { ativo });
  }
  redefinirSenha(id: number) {
    return this.http.post<SenhaRedefinida>(`${API}/pessoas/${id}/redefinir-senha`, null);
  }

  // Agências (ADMIN)
  listarAgencias(page = 0, size = 10, busca = '') {
    return this.http.get<Page<Agencia>>(`${API}/agencias`, { params: this.params(page, size, { busca }) });
  }
  salvarAgencia(dados: Omit<Agencia, 'id'>, id?: number) {
    return id
      ? this.http.put<Agencia>(`${API}/agencias/${id}`, dados)
      : this.http.post<Agencia>(`${API}/agencias`, dados);
  }
  removerAgencia(id: number) {
    return this.http.delete<void>(`${API}/agencias/${id}`);
  }

  // Contas
  listarContas(page = 0, size = 10, busca = '') {
    return this.http.get<Page<Conta>>(`${API}/contas`, { params: this.params(page, size, { busca }) });
  }
  stats() {
    return this.http.get<Stats>(`${API}/contas/stats`);
  }
  buscarConta(id: number) {
    return this.http.get<Conta>(`${API}/contas/${id}`);
  }
  /** de/ate no formato yyyy-MM-dd (o valor de um input type="date") */
  extrato(id: number, page = 0, size = 15, de?: string, ate?: string) {
    return this.http.get<Page<Movimentacao>>(`${API}/contas/${id}/extrato`, {
      params: this.params(page, size, { de, ate }),
    });
  }
  /** Baixa o CSV como arquivo (Blob): precisa passar pelo HttpClient para levar o token. */
  extratoCsv(id: number, de?: string, ate?: string) {
    let params = new HttpParams();
    if (de) params = params.set('de', de);
    if (ate) params = params.set('ate', ate);
    return this.http.get(`${API}/contas/${id}/extrato.csv`, { params, responseType: 'blob' });
  }
  criarConta(dados: {
    numero: number;
    limiteChequeEspecial: number;
    saldoInicial: number;
    pessoaId: number;
    agenciaId: number;
  }) {
    return this.http.post<Conta>(`${API}/contas`, dados);
  }
  atualizarConta(id: number, dados: { limiteChequeEspecial: number; agenciaId: number }) {
    return this.http.put<Conta>(`${API}/contas/${id}`, dados);
  }
  removerConta(id: number) {
    return this.http.delete<void>(`${API}/contas/${id}`);
  }

  // Operações
  depositar(id: number, valor: number) {
    return this.http.post<Conta>(`${API}/contas/${id}/depositar`, { valor });
  }
  sacar(id: number, valor: number) {
    return this.http.post<Conta>(`${API}/contas/${id}/sacar`, { valor });
  }
  transferir(id: number, numeroContaDestino: number, valor: number) {
    return this.http.post<Conta>(`${API}/contas/${id}/transferir`, { numeroContaDestino, valor });
  }

  // Auditoria (ADMIN)
  auditoria(page = 0, entidade: EntidadeAuditada | '' = '', size = 20) {
    return this.http.get<Page<RegistroAuditoria>>(`${API}/auditoria`, {
      params: this.params(page, size, { entidade }),
    });
  }
}
