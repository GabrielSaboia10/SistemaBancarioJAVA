import { Router } from 'express'
import { authorize } from '../../shared/middleware/auth'
import { validate } from '../../shared/middleware/validate'
import { criarAgenciaSchema, atualizarAgenciaSchema } from './agencias.schemas'
import { parsePagination } from '../../shared/lib/pagination'
import * as agenciasService from './agencias.service'

const router = Router()

router.get('/', async (req, res, next) => {
  try {
    if (req.user!.role === 'CLIENTE') {
      res.json(await agenciasService.listarPorPessoa(req.user!.pessoaId!))
      return
    }
    res.json(await agenciasService.listarTodas(parsePagination(req.query)))
  } catch (e) { next(e) }
})

router.get('/:id', async (req, res, next) => {
  try {
    const id = Number(req.params.id)
    if (req.user!.role === 'CLIENTE') {
      const conta = await agenciasService.agenciaPertenceAPessoa(id, req.user!.pessoaId!)
      if (!conta) { res.status(403).json({ erro: 'Acesso negado' }); return }
    }
    res.json(await agenciasService.listarPorId(id))
  } catch (e) { next(e) }
})

router.post('/', authorize('ADMIN'), validate(criarAgenciaSchema), async (req, res, next) => {
  try {
    const { numero, endereco, cidade } = req.body
    res.status(201).json(await agenciasService.criar(numero, endereco, cidade))
  } catch (e) { next(e) }
})

router.put('/:id', authorize('ADMIN'), validate(atualizarAgenciaSchema), async (req, res, next) => {
  try {
    const { endereco, cidade } = req.body
    res.json(await agenciasService.atualizar(Number(req.params.id), endereco, cidade))
  } catch (e) { next(e) }
})

router.delete('/:id', authorize('ADMIN'), async (req, res, next) => {
  try {
    await agenciasService.remover(Number(req.params.id))
    res.status(204).send()
  } catch (e) { next(e) }
})

export default router
