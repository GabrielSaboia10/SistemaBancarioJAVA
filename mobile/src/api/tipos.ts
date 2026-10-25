// Espelho dos DTOs da API (backend/src/main/java/com/banco/dto)

export type Role = 'ADMIN' | 'CLIENTE';

export interface Usuario {
  id: number;
  cpf: string;
  nome: string;
  role: Role;
  pessoaId: number | null;
  trocarSenha: boolean;
}

export interface TokenResponse {
  token: string;
  refreshToken: string;
  expiraEm: number;
  usuario: Usuario;
}

export interface Page<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface Pessoa {
  id: number;
  cpf: string;
  nome: string;
  idade: number;
  criadoEm: string;
  ativo: boolean | null;
  senhaTemporaria: boolean | null;
}

export interface Agencia {
  id: number;
  numero: number;
  endereco: string;
  cidade: string;
}

export interface Conta {
  id: number;
  numero: number;
  saldo: number;
  limiteChequeEspecial: number;
  saldoDisponivel: number;
  correntista: { id: number; nome: string; cpf: string };
  agencia: { id: number; numero: number; cidade: string };
}

export type TipoMovimentacao =
  | 'DEPOSITO'
  | 'SAQUE'
  | 'TRANSFERENCIA_ENVIADA'
  | 'TRANSFERENCIA_RECEBIDA';

export interface Movimentacao {
  id: number;
  tipo: TipoMovimentacao;
  valor: number;
  saldoApos: number;
  numeroContaContraparte: number | null;
  dataHora: string;
}

export interface Stats {
  totalClientes: number | null;
  totalAgencias: number | null;
  totalContas: number;
  saldoTotal: number;
}

export type EntidadeAuditada = 'PESSOA' | 'AGENCIA' | 'CONTA';

export interface RegistroAuditoria {
  id: number;
  usuarioCpf: string | null;
  acao: string;
  entidade: EntidadeAuditada;
  entidadeId: number | null;
  descricao: string;
  dataHora: string;
}
