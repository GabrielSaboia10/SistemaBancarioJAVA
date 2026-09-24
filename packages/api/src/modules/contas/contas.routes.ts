import { Router } from 'express'
import { authorize } from '../../shared/middleware/auth'
import { validate } from '../../shared/middleware/validate'
import { criarContaSchema, atualizarContaSchema, operacaoSchema, transferirSchema } from './contas.schemas'
import { parsePagination } from '../../shared/lib/pagination'
import * as contasService from './contas.service'

const router = Router()

router.get('/', async (req, res, next) => {
  try {
    const pagination = parsePagination(req.query)
    const contas = req.user!.role === 'CLIENTE'
      ? await contasService.listarPorPessoa(req.user!.pessoaId!, pagination)
      : await contasService.listarTodas(pagination)
    res.json(contas)
  } catch (e) { next(e) }
})

router.get('/stats', async (req, res, next) => {
  try {
    const stats = req.user!.role === 'CLIENTE'
      ? await contasService.statsPorPessoa(req.user!.pessoaId!)
      : await contasService.statsTodas()
    res.json(stats)
  } catch (e) { next(e) }
})

router.get('/:id', async (req, res, next) => {
  try {
    const id = Number(req.params.id)
    const conta = await contasService.listarPorId(id)
    if (req.user!.role === 'CLIENTE' && conta.pessoaId !== req.user!.pessoaId) {
      res.status(403).json({ erro: 'Acesso negado' }); return
    }
    res.json(conta)
  } catch (e) { next(e) }
})

router.post('/', authorize('ADMIN'), validate(criarContaSchema), async (req, res, next) => {
  try {
    const { numContaCorrente, limiteChequeEspecial, saldo, pessoaId, agenciaId } = req.body
    res.status(201).json(await contasService.criar(numContaCorrente, limiteChequeEspecial, saldo, pessoaId, agenciaId))
  } catch (e) { next(e) }
})

router.put('/:id', authorize('ADMIN'), validate(atualizarContaSchema), async (req, res, next) => {
  try {
    const { limiteChequeEspecial, agenciaId } = req.body
    res.json(await contasService.atualizar(Number(req.params.id), limiteChequeEspecial, agenciaId))
  } catch (e) { next(e) }
})

router.delete('/:id', authorize('ADMIN'), async (req, res, next) => {
  try {
    await contasService.remover(Number(req.params.id))
    res.status(204).send()
  } catch (e) { next(e) }
})

router.post('/:id/depositar', validate(operacaoSchema), async (req, res, next) => {
  try {
    const id = Number(req.params.id)
    if (req.user!.role === 'CLIENTE' && !(await contasService.verificarOwnership(id, req.user!.pessoaId!))) {
      res.status(403).json({ erro: 'Acesso negado' }); return
    }
    res.json(await contasService.depositar(id, req.body.valor))
  } catch (e) { next(e) }
})

router.post('/:id/sacar', validate(operacaoSchema), async (req, res, next) => {
  try {
    const id = Number(req.params.id)
    if (req.user!.role === 'CLIENTE' && !(await contasService.verificarOwnership(id, req.user!.pessoaId!))) {
      res.status(403).json({ erro: 'Acesso negado' }); return
    }
    res.json(await contasService.sacar(id, req.body.valor))
  } catch (e) { next(e) }
})

router.post('/:id/transferir', validate(transferirSchema), async (req, res, next) => {
  try {
    const id = Number(req.params.id)
    if (req.user!.role === 'CLIENTE' && !(await contasService.verificarOwnership(id, req.user!.pessoaId!))) {
      res.status(403).json({ erro: 'Acesso negado' }); return
    }
    const { destinoId, destinoNumConta, valor } = req.body
    res.json(await contasService.transferir(
      id,
      destinoId ? Number(destinoId) : null,
      destinoNumConta ? Number(destinoNumConta) : null,
      valor,
    ))
  } catch (e) { next(e) }
})

export default router
