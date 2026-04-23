import { Router } from 'express'
import prisma from '../lib/prisma'

const router = Router()

router.get('/', async (_req, res, next) => {
  try {
    const pessoas = await prisma.pessoa.findMany({ orderBy: { nome: 'asc' } })
    res.json(pessoas)
  } catch (e) { next(e) }
})

router.get('/:id', async (req, res, next) => {
  try {
    const pessoa = await prisma.pessoa.findUniqueOrThrow({ where: { id: Number(req.params.id) } })
    res.json(pessoa)
  } catch (e) { next(e) }
})

router.post('/', async (req, res, next) => {
  try {
    const { cpf, nome, idade } = req.body
    if (!cpf || cpf.length !== 14) throw new Error('CPF deve ter 14 caracteres (ex: 123.456.789-09)')
    if (!nome || nome.length > 40) throw new Error('Nome é obrigatório e deve ter até 40 caracteres')
    if (idade < 0 || idade > 150) throw new Error('Idade inválida')
    const pessoa = await prisma.pessoa.create({ data: { cpf, nome, idade } })
    res.status(201).json(pessoa)
  } catch (e) { next(e) }
})

router.put('/:id', async (req, res, next) => {
  try {
    const { nome, idade } = req.body
    const pessoa = await prisma.pessoa.update({
      where: { id: Number(req.params.id) },
      data: { nome, idade },
    })
    res.json(pessoa)
  } catch (e) { next(e) }
})

router.delete('/:id', async (req, res, next) => {
  try {
    await prisma.pessoa.delete({ where: { id: Number(req.params.id) } })
    res.status(204).send()
  } catch (e) { next(e) }
})

export default router
