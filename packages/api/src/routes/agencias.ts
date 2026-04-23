import { Router } from 'express'
import prisma from '../lib/prisma'

const router = Router()

router.get('/', async (_req, res, next) => {
  try {
    const agencias = await prisma.agenciaBancaria.findMany({ orderBy: { numero: 'asc' } })
    res.json(agencias)
  } catch (e) { next(e) }
})

router.get('/:id', async (req, res, next) => {
  try {
    const agencia = await prisma.agenciaBancaria.findUniqueOrThrow({ where: { id: Number(req.params.id) } })
    res.json(agencia)
  } catch (e) { next(e) }
})

router.post('/', async (req, res, next) => {
  try {
    const { numero, endereco, cidade } = req.body
    if (!numero || numero < 1 || numero > 10000) throw new Error('Número da agência inválido (1 a 10000)')
    if (!endereco || endereco.length > 100) throw new Error('Endereço obrigatório, até 100 caracteres')
    if (!cidade || cidade.length > 25) throw new Error('Cidade obrigatória, até 25 caracteres')
    const agencia = await prisma.agenciaBancaria.create({ data: { numero, endereco, cidade } })
    res.status(201).json(agencia)
  } catch (e) { next(e) }
})

router.put('/:id', async (req, res, next) => {
  try {
    const { endereco, cidade } = req.body
    const agencia = await prisma.agenciaBancaria.update({
      where: { id: Number(req.params.id) },
      data: { endereco, cidade },
    })
    res.json(agencia)
  } catch (e) { next(e) }
})

router.delete('/:id', async (req, res, next) => {
  try {
    await prisma.agenciaBancaria.delete({ where: { id: Number(req.params.id) } })
    res.status(204).send()
  } catch (e) { next(e) }
})

export default router
