import bcrypt from 'bcryptjs'
import crypto from 'crypto'
import prisma from '../../shared/lib/prisma'
import { signToken, verifyToken } from '../../shared/lib/jwt'
import { normalizeCpf } from '../../shared/lib/cpf'
import { AppError, UnauthorizedError } from '../../shared/errors/AppError'
import { logger } from '../../shared/lib/logger'

const REFRESH_EXPIRES_DAYS = 7

export async function login(cpf: string, senha: string) {
  if (!cpf || !senha) throw new Error('CPF e senha são obrigatórios')
  const cpfFormatado = normalizeCpf(cpf)
  const usuario = await prisma.usuario.findUnique({
    where: { cpf: cpfFormatado },
    include: { pessoa: true },
  })
  if (!usuario || !usuario.ativo) throw new UnauthorizedError('CPF ou senha inválidos')
  const senhaCorreta = await bcrypt.compare(String(senha), usuario.senha)
  if (!senhaCorreta) throw new UnauthorizedError('CPF ou senha inválidos')

  const token = signToken({ sub: usuario.id, cpf: usuario.cpf, role: usuario.role, pessoaId: usuario.pessoaId })
  const refreshToken = await criarRefreshToken(usuario.id)
  const nome = usuario.role === 'ADMIN' ? 'Administrador' : (usuario.pessoa?.nome ?? 'Cliente')

  logger.info({ usuarioId: usuario.id, role: usuario.role }, 'login bem-sucedido')
  return { token, refreshToken, role: usuario.role, nome, pessoaId: usuario.pessoaId }
}

export async function refresh(refreshTokenStr: string) {
  const stored = await prisma.refreshToken.findUnique({ where: { token: refreshTokenStr }, include: { usuario: true } })
  if (!stored || stored.expiresAt < new Date()) {
    if (stored) await prisma.refreshToken.delete({ where: { id: stored.id } })
    throw new UnauthorizedError('Refresh token inválido ou expirado')
  }

  const { usuario } = stored
  if (!usuario.ativo) throw new UnauthorizedError('Usuário inativo')

  await prisma.refreshToken.delete({ where: { id: stored.id } })
  const novoRefreshToken = await criarRefreshToken(usuario.id)
  const token = signToken({ sub: usuario.id, cpf: usuario.cpf, role: usuario.role, pessoaId: usuario.pessoaId })

  logger.info({ usuarioId: usuario.id }, 'refresh token rotacionado')
  return { token, refreshToken: novoRefreshToken }
}

export async function logout(refreshTokenStr: string) {
  await prisma.refreshToken.deleteMany({ where: { token: refreshTokenStr } })
}

export async function trocarSenha(usuarioId: number, senhaAtual: string, novaSenha: string) {
  const usuario = await prisma.usuario.findUniqueOrThrow({ where: { id: usuarioId } })
  const correta = await bcrypt.compare(senhaAtual, usuario.senha)
  if (!correta) throw new AppError('Senha atual incorreta', 401)
  const hash = await bcrypt.hash(novaSenha, 10)
  await prisma.usuario.update({ where: { id: usuarioId }, data: { senha: hash } })
  await prisma.refreshToken.deleteMany({ where: { usuarioId } })
  logger.info({ usuarioId }, 'senha alterada — refresh tokens invalidados')
}

async function criarRefreshToken(usuarioId: number): Promise<string> {
  const token = crypto.randomBytes(40).toString('hex')
  const expiresAt = new Date(Date.now() + REFRESH_EXPIRES_DAYS * 24 * 60 * 60 * 1000)
  await prisma.refreshToken.create({ data: { token, usuarioId, expiresAt } })
  return token
}

export async function limparRefreshTokensExpirados() {
  const { count } = await prisma.refreshToken.deleteMany({ where: { expiresAt: { lt: new Date() } } })
  if (count > 0) logger.info({ count }, 'refresh tokens expirados removidos')
}
