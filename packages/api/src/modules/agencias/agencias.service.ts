import prisma from '../../shared/lib/prisma'
import { PaginationParams, paginate } from '../../shared/lib/pagination'

export async function listarTodas(pagination?: PaginationParams) {
  if (!pagination) return prisma.agenciaBancaria.findMany({ orderBy: { numero: 'asc' } })
  const [data, total] = await Promise.all([
    prisma.agenciaBancaria.findMany({ orderBy: { numero: 'asc' }, skip: pagination.skip, take: pagination.limit }),
    prisma.agenciaBancaria.count(),
  ])
  return paginate(data, total, pagination)
}

export async function listarPorId(id: number) {
  return prisma.agenciaBancaria.findUniqueOrThrow({ where: { id } })
}

export async function listarPorPessoa(pessoaId: number) {
  const contas = await prisma.contaBancaria.findMany({
    where: { pessoaId },
    select: { agenciaId: true },
  })
  const ids = contas.map(c => c.agenciaId)
  return prisma.agenciaBancaria.findMany({ where: { id: { in: ids } } })
}

export async function agenciaPertenceAPessoa(agenciaId: number, pessoaId: number) {
  return prisma.contaBancaria.findFirst({ where: { pessoaId, agenciaId } })
}

export async function criar(numero: number, endereco: string, cidade: string) {
  if (!numero || numero < 1 || numero > 10000) throw new Error('Número da agência inválido (1 a 10000)')
  if (!endereco || endereco.length > 100) throw new Error('Endereço obrigatório, até 100 caracteres')
  if (!cidade || cidade.length > 25) throw new Error('Cidade obrigatória, até 25 caracteres')
  return prisma.agenciaBancaria.create({ data: { numero, endereco, cidade } })
}

export async function atualizar(id: number, endereco: string, cidade: string) {
  return prisma.agenciaBancaria.update({ where: { id }, data: { endereco, cidade } })
}

export async function remover(id: number) {
  await prisma.agenciaBancaria.delete({ where: { id } })
}
