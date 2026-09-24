import { describe, it, expect, vi, beforeEach } from 'vitest'
import request from 'supertest'
import bcrypt from 'bcryptjs'

vi.mock('../../../shared/lib/prisma')
vi.mock('../../../shared/lib/logger', () => ({ logger: { info: vi.fn(), error: vi.fn(), warn: vi.fn() } }))
vi.mock('pino-http', () => ({ default: () => (_req: any, _res: any, next: any) => next() }))

import prisma from '../../../shared/lib/prisma'
import app from '../../../app'

const mockUsuario = {
  id: 1,
  cpf: '12345678901',
  senha: bcrypt.hashSync('123456', 10),
  role: 'CLIENTE' as const,
  pessoaId: 1,
  ativo: true,
  pessoa: { nome: 'João Silva' },
}

const mockRefreshToken = {
  id: 1,
  token: 'valid-refresh-token-abc123',
  usuarioId: 1,
  expiresAt: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000),
  usuario: mockUsuario,
}

beforeEach(() => { vi.clearAllMocks() })

describe('POST /api/auth/login', () => {
  it('retorna token com credenciais válidas', async () => {
    vi.mocked(prisma.usuario.findUnique).mockResolvedValue(mockUsuario as any)
    vi.mocked(prisma.refreshToken.create).mockResolvedValue(mockRefreshToken as any)

    const res = await request(app)
      .post('/api/auth/login')
      .send({ cpf: '123.456.789-01', senha: '123456' })

    expect(res.status).toBe(200)
    expect(res.body).toHaveProperty('token')
    expect(res.body).toHaveProperty('refreshToken')
    expect(res.body.role).toBe('CLIENTE')
  })

  it('retorna 401 com senha errada', async () => {
    vi.mocked(prisma.usuario.findUnique).mockResolvedValue(mockUsuario as any)

    const res = await request(app)
      .post('/api/auth/login')
      .send({ cpf: '123.456.789-01', senha: 'errada' })

    expect(res.status).toBe(401)
    expect(res.body).toHaveProperty('erro')
  })

  it('retorna 401 com usuário inexistente', async () => {
    vi.mocked(prisma.usuario.findUnique).mockResolvedValue(null)

    const res = await request(app)
      .post('/api/auth/login')
      .send({ cpf: '000.000.000-00', senha: '123456' })

    expect(res.status).toBe(401)
  })

  it('retorna 400 com body inválido', async () => {
    const res = await request(app)
      .post('/api/auth/login')
      .send({ cpf: '123' })

    expect(res.status).toBe(400)
    expect(res.body).toHaveProperty('detalhes')
  })
})

describe('POST /api/auth/refresh', () => {
  it('retorna novo par de tokens com refresh token válido', async () => {
    vi.mocked(prisma.refreshToken.findUnique).mockResolvedValue(mockRefreshToken as any)
    vi.mocked(prisma.refreshToken.delete).mockResolvedValue(mockRefreshToken as any)
    vi.mocked(prisma.refreshToken.create).mockResolvedValue({ ...mockRefreshToken, token: 'novo-token' } as any)

    const res = await request(app)
      .post('/api/auth/refresh')
      .send({ refreshToken: 'valid-refresh-token-abc123' })

    expect(res.status).toBe(200)
    expect(res.body).toHaveProperty('token')
    expect(res.body).toHaveProperty('refreshToken')
  })

  it('retorna 401 com refresh token inexistente', async () => {
    vi.mocked(prisma.refreshToken.findUnique).mockResolvedValue(null)

    const res = await request(app)
      .post('/api/auth/refresh')
      .send({ refreshToken: 'invalido' })

    expect(res.status).toBe(401)
  })

  it('retorna 401 com refresh token expirado', async () => {
    vi.mocked(prisma.refreshToken.findUnique).mockResolvedValue({
      ...mockRefreshToken,
      expiresAt: new Date(Date.now() - 1000),
    } as any)
    vi.mocked(prisma.refreshToken.delete).mockResolvedValue(mockRefreshToken as any)

    const res = await request(app)
      .post('/api/auth/refresh')
      .send({ refreshToken: 'expirado' })

    expect(res.status).toBe(401)
  })
})

describe('POST /api/auth/logout', () => {
  it('retorna 204 e invalida o refresh token', async () => {
    vi.mocked(prisma.refreshToken.deleteMany).mockResolvedValue({ count: 1 })

    const res = await request(app)
      .post('/api/auth/logout')
      .send({ refreshToken: 'qualquer-token' })

    expect(res.status).toBe(204)
    expect(prisma.refreshToken.deleteMany).toHaveBeenCalledWith({ where: { token: 'qualquer-token' } })
  })
})

describe('GET /api/auth/me', () => {
  it('retorna 401 sem token', async () => {
    const res = await request(app).get('/api/auth/me')
    expect(res.status).toBe(401)
  })
})

describe('GET /api/health', () => {
  it('retorna status ok', async () => {
    const res = await request(app).get('/api/health')
    expect(res.status).toBe(200)
    expect(res.body.status).toBe('ok')
  })
})
