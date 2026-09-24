import { describe, it, expect, vi, beforeEach } from 'vitest'
import * as authService from '../auth.service'

vi.mock('../../../shared/lib/prisma')
vi.mock('../../../shared/lib/logger', () => ({ logger: { info: vi.fn(), error: vi.fn(), warn: vi.fn() } }))
vi.mock('bcryptjs', () => ({
  default: {
    compare: vi.fn(),
    hash: vi.fn().mockResolvedValue('hash-novo'),
  },
}))
vi.mock('../../../shared/lib/jwt', () => ({
  signToken: vi.fn().mockReturnValue('token-mock'),
}))

import prisma from '../../../shared/lib/prisma'
import bcrypt from 'bcryptjs'

const mockRefreshTokenRow = {
  id: 1, token: 'rt', usuarioId: 1, expiresAt: new Date(Date.now() + 86400000), createdAt: new Date(),
}

const mockUsuarioCliente = {
  id: 1,
  cpf: '123.456.789-09',
  senha: 'hash',
  role: 'CLIENTE' as const,
  ativo: true,
  pessoaId: 1,
  pessoa: { id: 1, nome: 'João Silva' },
}

const mockUsuarioAdmin = {
  id: 2,
  cpf: '000.000.000-00',
  senha: 'hash-admin',
  role: 'ADMIN' as const,
  ativo: true,
  pessoaId: null,
  pessoa: null,
}

beforeEach(() => {
  vi.clearAllMocks()
  vi.mocked(prisma.refreshToken.create).mockResolvedValue(mockRefreshTokenRow as any)
})

describe('login', () => {
  it('retorna token e dados do usuário cliente com credenciais válidas', async () => {
    vi.mocked(prisma.usuario.findUnique).mockResolvedValue(mockUsuarioCliente as any)
    vi.mocked(bcrypt.compare).mockResolvedValue(true as any)

    const result = await authService.login('123.456.789-09', 'senha123')

    expect(result.token).toBe('token-mock')
    expect(result.role).toBe('CLIENTE')
    expect(result.nome).toBe('João Silva')
    expect(result.pessoaId).toBe(1)
  })

  it('retorna "Administrador" como nome para usuário ADMIN', async () => {
    vi.mocked(prisma.usuario.findUnique).mockResolvedValue(mockUsuarioAdmin as any)
    vi.mocked(bcrypt.compare).mockResolvedValue(true as any)

    const result = await authService.login('000.000.000-00', 'admin123')
    expect(result.nome).toBe('Administrador')
    expect(result.pessoaId).toBeNull()
  })

  it('rejeita quando CPF e senha não são informados', async () => {
    await expect(authService.login('', '')).rejects.toThrow('CPF e senha são obrigatórios')
  })

  it('rejeita quando CPF tem formato inválido', async () => {
    await expect(authService.login('123', 'senha')).rejects.toThrow('CPF inválido')
  })

  it('rejeita quando usuário não existe', async () => {
    vi.mocked(prisma.usuario.findUnique).mockResolvedValue(null)

    await expect(authService.login('123.456.789-09', 'qualquer')).rejects.toThrow('CPF ou senha inválidos')
  })

  it('rejeita quando usuário está inativo', async () => {
    vi.mocked(prisma.usuario.findUnique).mockResolvedValue({ ...mockUsuarioCliente, ativo: false } as any)

    await expect(authService.login('123.456.789-09', 'senha123')).rejects.toThrow('CPF ou senha inválidos')
  })

  it('rejeita quando senha está errada', async () => {
    vi.mocked(prisma.usuario.findUnique).mockResolvedValue(mockUsuarioCliente as any)
    vi.mocked(bcrypt.compare).mockResolvedValue(false as any)

    await expect(authService.login('123.456.789-09', 'senha-errada')).rejects.toThrow('CPF ou senha inválidos')
  })
})
