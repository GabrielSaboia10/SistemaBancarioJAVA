import client from './client'
import type { ContaBancaria, CreateContaDTO, Stats } from '../types'

export const contasApi = {
  getAll: () => client.get<ContaBancaria[]>('/contas').then((r) => r.data),
  getById: (id: number) => client.get<ContaBancaria>(`/contas/${id}`).then((r) => r.data),
  getStats: () => client.get<Stats>('/contas/stats').then((r) => r.data),
  create: (data: CreateContaDTO) => client.post<ContaBancaria>('/contas', data).then((r) => r.data),
  update: (id: number, data: { limiteChequeEspecial: number; agenciaId: number }) =>
    client.put<ContaBancaria>(`/contas/${id}`, data).then((r) => r.data),
  remove: (id: number) => client.delete(`/contas/${id}`),
  depositar: (id: number, valor: number) =>
    client.post<ContaBancaria>(`/contas/${id}/depositar`, { valor }).then((r) => r.data),
  sacar: (id: number, valor: number) =>
    client.post<ContaBancaria>(`/contas/${id}/sacar`, { valor }).then((r) => r.data),
  transferir: (id: number, destinoId: number, valor: number) =>
    client.post<ContaBancaria>(`/contas/${id}/transferir`, { destinoId, valor }).then((r) => r.data),
  transferirPorNumConta: (id: number, destinoNumConta: number, valor: number) =>
    client.post<ContaBancaria>(`/contas/${id}/transferir`, { destinoNumConta, valor }).then((r) => r.data),
}
