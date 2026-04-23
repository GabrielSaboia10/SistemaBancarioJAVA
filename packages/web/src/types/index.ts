export interface Pessoa {
  id: number
  cpf: string
  nome: string
  idade: number
  createdAt: string
}

export interface AgenciaBancaria {
  id: number
  numero: number
  endereco: string
  cidade: string
  createdAt: string
}

export interface ContaBancaria {
  id: number
  numContaCorrente: number
  limiteChequeEspecial: number
  saldo: number
  pessoaId: number
  agenciaId: number
  correntista: Pessoa
  agencia: AgenciaBancaria
  createdAt: string
}

export interface Stats {
  totalContas: number
  totalPessoas: number
  totalAgencias: number
  saldoTotal: number
}

export type CreatePessoaDTO = Omit<Pessoa, 'id' | 'createdAt'>
export type UpdatePessoaDTO = Pick<Pessoa, 'nome' | 'idade'>

export type CreateAgenciaDTO = Omit<AgenciaBancaria, 'id' | 'createdAt'>
export type UpdateAgenciaDTO = Pick<AgenciaBancaria, 'endereco' | 'cidade'>

export interface CreateContaDTO {
  numContaCorrente: number
  limiteChequeEspecial: number
  saldo: number
  pessoaId: number
  agenciaId: number
}
