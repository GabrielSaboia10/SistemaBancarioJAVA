import { Router } from 'express'
import prisma from '../lib/prisma'
import { authorize } from '../middleware/auth'

const router = Router()

const include = { correntista: true, agencia: true }

router.get('/', async (req, res, next) => {
  try {
    const where = req.user!.role === 'CLIENTE' ? { pessoaId: req.user!.pessoaId! } : {}
    const contas = await prisma.contaBancaria.findMany({ where, include, orderBy: { numContaCorrente: 'asc' } })
    res.json(contas)
  } catch (e) { next(e) }
})

router.get('/stats', async (req, res, next) => {
  try {
    if (req.user!.role === 'CLIENTE') {
      const pessoaId = req.user!.pessoaId!
      const [totalContas, saldoAgg] = await Promise.all([
        prisma.contaBancaria.count({ where: { pessoaId } }),
        prisma.contaBancaria.aggregate({ where: { pessoaId }, _sum: { saldo: true } }),
      ])
      res.json({ totalContas, totalPessoas: 1, totalAgencias: 1, saldoTotal: saldoAgg._sum.saldo ?? 0 })
      return
    }
    const [totalContas, totalPessoas, totalAgencias, saldoAgg] = await Promise.all([
      prisma.contaBancaria.count(),
      prisma.pessoa.count(),
      prisma.agenciaBancaria.count(),
      prisma.contaBancaria.aggregate({ _sum: { saldo: true } }),
    ])
    res.json({ totalContas, totalPessoas, totalAgencias, saldoTotal: saldoAgg._sum.saldo ?? 0 })
  } catch (e) { next(e) }
})

router.get('/:id', async (req, res, next) => {
  try {
    const id = Number(req.params.id)
    const conta = await prisma.contaBancaria.findUniqueOrThrow({ where: { id }, include })
    if (req.user!.role === 'CLIENTE' && conta.pessoaId !== req.user!.pessoaId) {
      res.status(403).json({ erro: 'Acesso negado' }); return
    }
    res.json(conta)
  } catch (e) { next(e) }
})

router.post('/', authorize('ADMIN'), async (req, res, next) => {
  try {
    const { numContaCorrente, limiteChequeEspecial, saldo, pessoaId, agenciaId } = req.body
    if (numContaCorrente < 1000 || numContaCorrente > 99999) throw new Error('Número de conta deve ser entre 1000 e 99999')
    if (limiteChequeEspecial < 0 || limiteChequeEspecial > 30000) throw new Error('Limite deve ser entre R$ 0 e R$ 30.000')
    const conta = await prisma.contaBancaria.create({
      data: { numContaCorrente, limiteChequeEspecial, saldo: saldo ?? 0, pessoaId, agenciaId },
      include,
    })
    res.status(201).json(conta)
  } catch (e) { next(e) }
})

router.put('/:id', authorize('ADMIN'), async (req, res, next) => {
  try {
    const { limiteChequeEspecial, agenciaId } = req.body
    const conta = await prisma.contaBancaria.update({
      where: { id: Number(req.params.id) },
      data: { limiteChequeEspecial, agenciaId },
      include,
    })
    res.json(conta)
  } catch (e) { next(e) }
})

router.delete('/:id', authorize('ADMIN'), async (req, res, next) => {
  try {
    await prisma.contaBancaria.delete({ where: { id: Number(req.params.id) } })
    res.status(204).send()
  } catch (e) { next(e) }
})

async function verificarOwnership(contaId: number, pessoaId: number, res: any): Promise<boolean> {
  const conta = await prisma.contaBancaria.findUnique({ where: { id: contaId } })
  if (!conta || conta.pessoaId !== pessoaId) {
    res.status(403).json({ erro: 'Acesso negado' })
    return false
  }
  return true
}

router.post('/:id/depositar', async (req, res, next) => {
  try {
    const id = Number(req.params.id)
    if (req.user!.role === 'CLIENTE' && !(await verificarOwnership(id, req.user!.pessoaId!, res))) return
    const { valor } = req.body
    if (!valor || valor <= 0) throw new Error('Valor do depósito deve ser positivo')
    const conta = await prisma.contaBancaria.update({
      where: { id },
      data: { saldo: { increment: valor } },
      include,
    })
    res.json(conta)
  } catch (e) { next(e) }
})

router.post('/:id/sacar', async (req, res, next) => {
  try {
    const id = Number(req.params.id)
    if (req.user!.role === 'CLIENTE' && !(await verificarOwnership(id, req.user!.pessoaId!, res))) return
    const { valor } = req.body
    if (!valor || valor <= 0) throw new Error('Valor do saque deve ser positivo')
    const conta = await prisma.contaBancaria.findUniqueOrThrow({ where: { id } })
    if (conta.saldo - valor < -conta.limiteChequeEspecial)
      throw new Error(`Saldo insuficiente. Disponível: R$ ${(conta.saldo + conta.limiteChequeEspecial).toFixed(2)}`)
    const atualizada = await prisma.contaBancaria.update({
      where: { id: conta.id },
      data: { saldo: { decrement: valor } },
      include,
    })
    res.json(atualizada)
  } catch (e) { next(e) }
})

router.post('/:id/transferir', async (req, res, next) => {
  try {
    const id = Number(req.params.id)
    if (req.user!.role === 'CLIENTE' && !(await verificarOwnership(id, req.user!.pessoaId!, res))) return
    const { destinoId, destinoNumConta, valor } = req.body
    if (!valor || valor <= 0) throw new Error('Valor deve ser positivo')

    let resolvedDestinoId = destinoId ? Number(destinoId) : null
    if (!resolvedDestinoId && destinoNumConta) {
      const dest = await prisma.contaBancaria.findUnique({ where: { numContaCorrente: Number(destinoNumConta) } })
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
    res.json(atualizada)
  } catch (e) { next(e) }
})

export default router
