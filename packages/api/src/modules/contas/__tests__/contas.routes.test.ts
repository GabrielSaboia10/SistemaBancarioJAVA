import { describe, it, expect, vi, beforeEach } from 'vitest'
import request from 'supertest'
import { signToken } from '../../../shared/lib/jwt'

vi.mock('../../../shared/lib/prisma')
vi.mock('../../../shared/lib/logger', () => ({ logger: { info: vi.fn(), error: vi.fn(), warn: vi.fn() } }))
vi.mock('pino-http', () => ({ default: () => (_req: any, _res: any, next: any) => next() }))

import prisma from '../../../shared/lib/prisma'
import app from '../../../app'

const adminToken = `Bearer ${signToken({ sub: 1, cpf: '00000000000', role: 'ADMIN', pessoaId: null })}`
const clienteToken = `Bearer ${signToken({ sub: 2, cpf: '12345678901', role: 'CLIENTE', pessoaId: 1 })}`

const mockConta = {
  id: 1, numContaCorrente: 10001, saldo: 1000, limiteChequeEspecial: 500,
  pessoaId: 1, agenciaId: 1,
  correntista: { id: 1, nome: 'João' },
  agencia: { id: 1, numero: 1, cidade: 'SP' },
}

beforeEach(() => { vi.clearAllMocks() })

describe('GET /api/contas', () => {
  it('retorna 401 sem token', async () => {
    const res = await request(app).get('/api/contas')
    expect(res.status).toBe(401)
  })

  it('admin recebe lista paginada', async () => {
    vi.mocked(prisma.contaBancaria.findMany).mockResolvedValue([mockConta] as any)
    vi.mocked(prisma.contaBancaria.count).mockResolvedValue(1)

    const res = await request(app).get('/api/contas').set('Authorization', adminToken)
    expect(res.status).toBe(200)
    expect(res.body).toHaveProperty('data')
    expect(res.body.total).toBe(1)
  })

  it('cliente recebe apenas suas contas', async () => {
    vi.mocked(prisma.contaBancaria.findMany).mockResolvedValue([mockConta] as any)
    vi.mocked(prisma.contaBancaria.count).mockResolvedValue(1)

    const res = await request(app).get('/api/contas').set('Authorization', clienteToken)
    expect(res.status).toBe(200)
    expect(res.body.data[0].pessoaId).toBe(1)
  })
})

describe('POST /api/contas/:id/depositar', () => {
  it('retorna 400 com valor inválido', async () => {
    const res = await request(app)
      .post('/api/contas/1/depositar')
      .set('Authorization', adminToken)
      .send({ valor: -100 })
    expect(res.status).toBe(400)
  })

  it('admin deposita com sucesso', async () => {
    vi.mocked(prisma.contaBancaria.update).mockResolvedValue({ ...mockConta, saldo: 1100 } as any)

    const res = await request(app)
      .post('/api/contas/1/depositar')
      .set('Authorization', adminToken)
      .send({ valor: 100 })
    expect(res.status).toBe(200)
    expect(res.body.saldo).toBe(1100)
  })

  it('cliente é bloqueado se não é dono da conta', async () => {
    vi.mocked(prisma.contaBancaria.findUnique).mockResolvedValue({ ...mockConta, pessoaId: 99 } as any)

    const res = await request(app)
      .post('/api/contas/1/depositar')
      .set('Authorization', clienteToken)
      .send({ valor: 100 })
    expect(res.status).toBe(403)
  })
})

describe('POST /api/contas/:id/sacar', () => {
  it('retorna 400 com valor zero', async () => {
    const res = await request(app)
      .post('/api/contas/1/sacar')
      .set('Authorization', adminToken)
      .send({ valor: 0 })
    expect(res.status).toBe(400)
  })

  it('saque com saldo suficiente retorna conta atualizada', async () => {
    vi.mocked(prisma.contaBancaria.findUniqueOrThrow).mockResolvedValue(mockConta as any)
    vi.mocked(prisma.contaBancaria.update).mockResolvedValue({ ...mockConta, saldo: 900 } as any)

    const res = await request(app)
      .post('/api/contas/1/sacar')
      .set('Authorization', adminToken)
      .send({ valor: 100 })
    expect(res.status).toBe(200)
    expect(res.body.saldo).toBe(900)
  })
})

describe('POST /api/contas/:id/transferir', () => {
  it('retorna 400 sem destino', async () => {
    const res = await request(app)
      .post('/api/contas/1/transferir')
      .set('Authorization', adminToken)
      .send({ valor: 100 })
    expect(res.status).toBe(400)
  })

  it('transferência válida retorna conta origem', async () => {
    vi.mocked(prisma.contaBancaria.findUniqueOrThrow).mockResolvedValue(mockConta as any)
    vi.mocked(prisma.contaBancaria.update).mockResolvedValue({ ...mockConta, saldo: 800 } as any)
    vi.mocked(prisma.$transaction).mockResolvedValue([{ ...mockConta, saldo: 800 }] as any)

    const res = await request(app)
      .post('/api/contas/1/transferir')
      .set('Authorization', adminToken)
      .send({ valor: 200, destinoId: 2 })
    expect(res.status).toBe(200)
  })
})
