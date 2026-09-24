import prisma from '../../shared/lib/prisma'
import { PaginationParams, paginate } from '../../shared/lib/pagination'

const include = { correntista: true, agencia: true }

export async function listarTodas(pagination?: PaginationParams) {
  if (!pagination) return prisma.contaBancaria.findMany({ include, orderBy: { numContaCorrente: 'asc' } })
  const [data, total] = await Promise.all([
    prisma.contaBancaria.findMany({ include, orderBy: { numContaCorrente: 'asc' }, skip: pagination.skip, take: pagination.limit }),
    prisma.contaBancaria.count(),
  ])
  return paginate(data, total, pagination)
}

export async function listarPorPessoa(pessoaId: number, pagination?: PaginationParams) {
  if (!pagination) return prisma.contaBancaria.findMany({ where: { pessoaId }, include, orderBy: { numContaCorrente: 'asc' } })
  const [data, total] = await Promise.all([
    prisma.contaBancaria.findMany({ where: { pessoaId }, include, orderBy: { numContaCorrente: 'asc' }, skip: pagination.skip, take: pagination.limit }),
    prisma.contaBancaria.count({ where: { pessoaId } }),
  ])
  return paginate(data, total, pagination)
}

export async function listarPorId(id: number) {
  return prisma.contaBancaria.findUniqueOrThrow({ where: { id }, include })
}

export async function statsTodas() {
  const [totalContas, totalPessoas, totalAgencias, saldoAgg] = await Promise.all([
    prisma.contaBancaria.count(),
    prisma.pessoa.count(),
    prisma.agenciaBancaria.count(),
    prisma.contaBancaria.aggregate({ _sum: { saldo: true } }),
  ])
  return { totalContas, totalPessoas, totalAgencias, saldoTotal: saldoAgg._sum.saldo ?? 0 }
}

export async function statsPorPessoa(pessoaId: number) {
  const [totalContas, saldoAgg] = await Promise.all([
    prisma.contaBancaria.count({ where: { pessoaId } }),
    prisma.contaBancaria.aggregate({ where: { pessoaId }, _sum: { saldo: true } }),
  ])
  return { totalContas, totalPessoas: 1, totalAgencias: 1, saldoTotal: saldoAgg._sum.saldo ?? 0 }
}

export async function criar(numContaCorrente: number, limiteChequeEspecial: number, saldo: number, pessoaId: number, agenciaId: number) {
  if (numContaCorrente < 1000 || numContaCorrente > 99999) throw new Error('Número de conta deve ser entre 1000 e 99999')
  if (limiteChequeEspecial < 0 || limiteChequeEspecial > 30000) throw new Error('Limite deve ser entre R$ 0 e R$ 30.000')
  return prisma.contaBancaria.create({
    data: { numContaCorrente, limiteChequeEspecial, saldo: saldo ?? 0, pessoaId, agenciaId },
    include,
  })
}

export async function atualizar(id: number, limiteChequeEspecial: number, agenciaId: number) {
  return prisma.contaBancaria.update({ where: { id }, data: { limiteChequeEspecial, agenciaId }, include })
}

export async function remover(id: number) {
  await prisma.contaBancaria.delete({ where: { id } })
}

export async function verificarOwnership(contaId: number, pessoaId: number): Promise<boolean> {
  const conta = await prisma.contaBancaria.findUnique({ where: { id: contaId } })
  return conta?.pessoaId === pessoaId
}

export async function depositar(id: number, valor: number) {
  if (!valor || valor <= 0) throw new Error('Valor do depósito deve ser positivo')
  return prisma.contaBancaria.update({ where: { id }, data: { saldo: { increment: valor } }, include })
}

export async function sacar(id: number, valor: number) {
  if (!valor || valor <= 0) throw new Error('Valor do saque deve ser positivo')
  const conta = await prisma.contaBancaria.findUniqueOrThrow({ where: { id } })
  if (conta.saldo - valor < -conta.limiteChequeEspecial)
    throw new Error(`Saldo insuficiente. Disponível: R$ ${(conta.saldo + conta.limiteChequeEspecial).toFixed(2)}`)
  return prisma.contaBancaria.update({ where: { id: conta.id }, data: { saldo: { decrement: valor } }, include })
}

export async function transferir(id: number, destinoId: number | null, destinoNumConta: number | null, valor: number) {
  if (!valor || valor <= 0) throw new Error('Valor deve ser positivo')
  let resolvedDestinoId = destinoId
  if (!resolvedDestinoId && destinoNumConta) {
    const dest = await prisma.contaBancaria.findUnique({ where: { numContaCorrente: destinoNumConta } })
    if (!dest) throw new Error('Conta de destino não encontrada')
    resolvedDestinoId = dest.id
  }
  if (!resolvedDestinoId) throw new Error('Informe destinoId ou destinoNumConta')
  const origem = await prisma.contaBancaria.findUniqueOrThrow({ where: { id } })
  if (origem.saldo - valor < -origem.limiteChequeEspecial)
    throw new Error(`Saldo insuficiente. Disponível: R$ ${(origem.saldo + origem.limiteChequeEspecial).toFixed(2)}`)
  const [atualizada] = await prisma.$transaction([
    prisma.contaBancaria.update({ where: { id: origem.id }, data: { saldo: { decrement: valor } }, include }),
    prisma.contaBancaria.update({ where: { id: resolvedDestinoId }, data: { saldo: { increment: valor } } }),
  ])
  return atualizada
}
