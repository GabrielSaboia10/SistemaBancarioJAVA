import client from './client'
import { AuthUser } from '../types'

interface LoginResponse {
  token: string
  role: string
  nome: string
  pessoaId: number | null
}

export const authApi = {
  login: (cpf: string, senha: string) =>
    client.post<LoginResponse>('/auth/login', { cpf, senha }).then(r => r.data as AuthUser),
  me: () => client.get('/auth/me').then(r => r.data),
}
