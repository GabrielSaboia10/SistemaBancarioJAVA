import client from './client'
import type { Pessoa, CreatePessoaDTO, UpdatePessoaDTO } from '../types'

export const pessoasApi = {
  getAll: () => client.get<Pessoa[]>('/pessoas').then((r) => r.data),
  getById: (id: number) => client.get<Pessoa>(`/pessoas/${id}`).then((r) => r.data),
  create: (data: CreatePessoaDTO) => client.post<Pessoa>('/pessoas', data).then((r) => r.data),
  update: (id: number, data: UpdatePessoaDTO) => client.put<Pessoa>(`/pessoas/${id}`, data).then((r) => r.data),
  remove: (id: number) => client.delete(`/pessoas/${id}`),
}
