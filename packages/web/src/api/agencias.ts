import client from './client'
import type { AgenciaBancaria, CreateAgenciaDTO, UpdateAgenciaDTO } from '../types'

export const agenciasApi = {
  getAll: () => client.get<AgenciaBancaria[]>('/agencias').then((r) => r.data),
  getById: (id: number) => client.get<AgenciaBancaria>(`/agencias/${id}`).then((r) => r.data),
  create: (data: CreateAgenciaDTO) => client.post<AgenciaBancaria>('/agencias', data).then((r) => r.data),
  update: (id: number, data: UpdateAgenciaDTO) => client.put<AgenciaBancaria>(`/agencias/${id}`, data).then((r) => r.data),
  remove: (id: number) => client.delete(`/agencias/${id}`),
}
