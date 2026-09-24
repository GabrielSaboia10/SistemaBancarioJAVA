// ─── Tipos de domínio ───────────────────────────────────────────────────────

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

export type Role = 'ADMIN' | 'CLIENTE'

export interface AuthUser {
  token: string
  refreshToken: string
  role: Role
  nome: string
  pessoaId: number | null
}

export interface Stats {
  totalContas: number
  totalPessoas: number
  totalAgencias: number
  saldoTotal: number
}

// ─── DTOs ────────────────────────────────────────────────────────────────────

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

// ─── Utilitários de CPF ──────────────────────────────────────────────────────

export function stripCpf(cpf: string): string {
  return cpf.replace(/\D/g, '')
}

export function formatCpf(value: string): string {
  const digits = stripCpf(value).slice(0, 11)
  if (digits.length <= 3) return digits
  if (digits.length <= 6) return `${digits.slice(0, 3)}.${digits.slice(3)}`
  if (digits.length <= 9) return `${digits.slice(0, 3)}.${digits.slice(3, 6)}.${digits.slice(6)}`
  return `${digits.slice(0, 3)}.${digits.slice(3, 6)}.${digits.slice(6, 9)}-${digits.slice(9, 11)}`
}

export function normalizeCpf(input: string): string {
  const digits = stripCpf(input)
  if (digits.length !== 11) throw new Error('CPF inválido — informe 11 dígitos ou no formato 000.000.000-00')
  return formatCpf(digits)
}

export function isValidCpf(value: string): boolean {
  const d = stripCpf(value)
  if (d.length !== 11 || /^(\d)\1+$/.test(d)) return false
  let sum = 0
  for (let i = 0; i < 9; i++) sum += Number(d[i]) * (10 - i)
  let r = (sum * 10) % 11
  if (r === 10 || r === 11) r = 0
  if (r !== Number(d[9])) return false
  sum = 0
  for (let i = 0; i < 10; i++) sum += Number(d[i]) * (11 - i)
  r = (sum * 10) % 11
  if (r === 10 || r === 11) r = 0
  return r === Number(d[10])
}
