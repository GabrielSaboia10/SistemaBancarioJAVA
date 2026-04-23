import { Router } from 'express'
import bcrypt from 'bcryptjs'
import prisma from '../lib/prisma'
import { authorize } from '../middleware/auth'
import { normalizeCpf, stripCpf } from '../lib/cpf'

const router = Router()

router.get('/', async (req, res, next) => {
  try {
    if (req.user!.role === 'CLIENTE') {
      const pessoa = req.user!.pessoaId
        ? await prisma.pessoa.findUnique({ where: { id: req.user!.pessoaId } })
        : null
      res.json(pessoa ? [pessoa] : [])
      return
    }
    const pessoas = await prisma.pessoa.findMany({ orderBy: { nome: 'asc' } })
    res.json(pessoas)
  } catch (e) { next(e) }
})

router.get('/:id', async (req, res, next) => {
  try {
    const id = Number(req.params.id)
    if (req.user!.role === 'CLIENTE' && req.user!.pessoaId !== id) {
      res.status(403).json({ erro: 'Acesso negado' })
      return
    }
    const pessoa = await prisma.pessoa.findUniqueOrThrow({ where: { id } })
    res.json(pessoa)
  } catch (e) { next(e) }
})

router.post('/', authorize('ADMIN'), async (req, res, next) => {
  try {
    const { cpf, nome, idade } = req.body
    if (!cpf) throw new Error('CPF é obrigatório')
    const cpfFormatado = normalizeCpf(cpf)
    if (!nome || nome.length > 40) throw new Error('Nome é obrigatório e deve ter até 40 caracteres')
    if (idade < 0 || idade > 150) throw new Error('Idade inválida')

    const pessoa = await prisma.pessoa.create({ data: { cpf: cpfFormatado, nome, idade } })

    const senhaInicial = stripCpf(cpfFormatado).slice(-6)
    const hash = await bcrypt.hash(senhaInicial, 10)
    await prisma.usuario.create({
      data: { cpf: cpfFormatado, senha: hash, role: 'CLIENTE', pessoaId: pessoa.id },
    })

    res.status(201).json(pessoa)
  } catch (e) { next(e) }
})

router.put('/:id', authorize('ADMIN'), async (req, res, next) => {
  try {
    const { nome, idade } = req.body
    const pessoa = await prisma.pessoa.update({
      where: { id: Number(req.params.id) },
      data: { nome, idade },
    })
    res.json(pessoa)
  } catch (e) { next(e) }
})

router.delete('/:id', authorize('ADMIN'), async (req, res, next) => {
  try {
    const id = Number(req.params.id)
    await prisma.usuario.deleteMany({ where: { pessoaId: id } })
    await prisma.pessoa.delete({ where: { id } })
    res.status(204).send()
  } catch (e) { next(e) }
})

export default router
