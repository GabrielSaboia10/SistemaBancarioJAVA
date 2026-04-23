import { Router } from 'express'
import bcrypt from 'bcryptjs'
import prisma from '../lib/prisma'
import { signToken } from '../lib/jwt'
import { normalizeCpf } from '../lib/cpf'
import { authenticate } from '../middleware/auth'

const router = Router()

router.post('/login', async (req, res, next) => {
  try {
    const { cpf, senha } = req.body
    if (!cpf || !senha) throw new Error('CPF e senha são obrigatórios')

    const cpfFormatado = normalizeCpf(cpf)

    const usuario = await prisma.usuario.findUnique({
      where: { cpf: cpfFormatado },
      include: { pessoa: true },
    })

    if (!usuario || !usuario.ativo) throw new Error('CPF ou senha inválidos')

    const senhaCorreta = await bcrypt.compare(String(senha), usuario.senha)
    if (!senhaCorreta) throw new Error('CPF ou senha inválidos')

    const token = signToken({
      sub: usuario.id,
      cpf: usuario.cpf,
      role: usuario.role,
      pessoaId: usuario.pessoaId,
    })

    const nome = usuario.role === 'ADMIN' ? 'Administrador' : (usuario.pessoa?.nome ?? 'Cliente')

    res.json({ token, role: usuario.role, nome, pessoaId: usuario.pessoaId })
  } catch (e) { next(e) }
})

router.get('/me', authenticate, (req, res) => {
  res.json(req.user)
})

export default router
