import { describe, it, expect, vi, beforeEach } from 'vitest'
import * as contasService from '../contas.service'

vi.mock('../../../shared/lib/prisma')
import prisma from '../../../shared/lib/prisma'

const mockConta = {
  id: 1,
  numContaCorrente: 10001,
  saldo: 500.0,
  limiteChequeEspecial: 200.0,
  pessoaId: 1,
  agenciaId: 1,
  correntista: { id: 1, nome: 'João' },
  agencia: { id: 1, numero: 1001, cidade: 'SP' },
}

beforeEach(() => {
  vi.clearAllMocks()
})

// ──────────────────────────────────────────────
// depositar
// ──────────────────────────────────────────────
describe('depositar', () => {
  it('incrementa o saldo quando valor é positivo', async () => {
    const atualizada = { ...mockConta, saldo: 600 }
    vi.mocked(prisma.contaBancaria.update).mockResolvedValue(atualizada as any)

    const result = await contasService.depositar(1, 100)

    expect(prisma.contaBancaria.update).toHaveBeenCalledWith({
      where: { id: 1 },
      data: { saldo: { increment: 100 } },
      include: expect.any(Object),
    })
    expect(result.saldo).toBe(600)
  })

  it('rejeita valor zero', async () => {
    await expect(contasService.depositar(1, 0)).rejects.toThrow('Valor do depósito deve ser positivo')
  })

  it('rejeita valor negativo', async () => {
    await expect(contasService.depositar(1, -50)).rejects.toThrow('Valor do depósito deve ser positivo')
  })
})

// ──────────────────────────────────────────────
// sacar
// ──────────────────────────────────────────────
describe('sacar', () => {
  it('decrementa o saldo quando há saldo suficiente', async () => {
    vi.mocked(prisma.contaBancaria.findUniqueOrThrow).mockResolvedValue(mockConta as any)
    const atualizada = { ...mockConta, saldo: 300 }
    vi.mocked(prisma.contaBancaria.update).mockResolvedValue(atualizada as any)

    const result = await contasService.sacar(1, 200)
    expect(result.saldo).toBe(300)
  })

  it('permite saque que usa o limite do cheque especial', async () => {
    // saldo 500, limite 200 → pode sacar até 700
    vi.mocked(prisma.contaBancaria.findUniqueOrThrow).mockResolvedValue(mockConta as any)
    const atualizada = { ...mockConta, saldo: -100 }
    vi.mocked(prisma.contaBancaria.update).mockResolvedValue(atualizada as any)

    const result = await contasService.sacar(1, 600)
    expect(result.saldo).toBe(-100)
  })

  it('rejeita saque que excede saldo + limite', async () => {
    // saldo 500, limite 200 → máximo 700. Tentar sacar 701
    vi.mocked(prisma.contaBancaria.findUniqueOrThrow).mockResolvedValue(mockConta as any)

    await expect(contasService.sacar(1, 701)).rejects.toThrow('Saldo insuficiente')
  })

  it('rejeita valor zero ou negativo', async () => {
    await expect(contasService.sacar(1, 0)).rejects.toThrow('Valor do saque deve ser positivo')
    await expect(contasService.sacar(1, -10)).rejects.toThrow('Valor do saque deve ser positivo')
  })
})

// ──────────────────────────────────────────────
// transferir
// ──────────────────────────────────────────────
describe('transferir', () => {
  it('transfere entre duas contas com saldo suficiente', async () => {
    vi.mocked(prisma.contaBancaria.findUniqueOrThrow).mockResolvedValue(mockConta as any)
    const origemAtualizada = { ...mockConta, saldo: 400 }
    vi.mocked(prisma.contaBancaria.update)
      .mockResolvedValueOnce(origemAtualizada as any)
      .mockResolvedValueOnce({ ...mockConta, id: 2, saldo: 1100 } as any)

    const result = await contasService.transferir(1, 2, null, 100)
    expect(result.saldo).toBe(400)
  })

  it('rejeita quando conta destino não existe (por numConta)', async () => {
    vi.mocked(prisma.contaBancaria.findUniqueOrThrow).mockResolvedValue(mockConta as any)
    vi.mocked(prisma.contaBancaria.findUnique).mockResolvedValue(null)

    await expect(contasService.transferir(1, null, 99999, 100)).rejects.toThrow('Conta de destino não encontrada')
  })

  it('rejeita quando nenhum destino é informado', async () => {
    await expect(contasService.transferir(1, null, null, 100)).rejects.toThrow('Informe destinoId ou destinoNumConta')
  })

  it('rejeita transferência por saldo insuficiente', async () => {
    // saldo 500, limite 200 → máximo 700. Tentar transferir 800
    vi.mocked(prisma.contaBancaria.findUniqueOrThrow).mockResolvedValue(mockConta as any)

    await expect(contasService.transferir(1, 2, null, 800)).rejects.toThrow('Saldo insuficiente')
  })

  it('rejeita valor zero ou negativo', async () => {
    await expect(contasService.transferir(1, 2, null, 0)).rejects.toThrow('Valor deve ser positivo')
  })
})

// ──────────────────────────────────────────────
// criar
// ──────────────────────────────────────────────
describe('criar', () => {
  it('cria conta com dados válidos', async () => {
    const nova = { ...mockConta, numContaCorrente: 5000 }
    vi.mocked(prisma.contaBancaria.create).mockResolvedValue(nova as any)

    const result = await contasService.criar(5000, 0, 0, 1, 1)
    expect(result.numContaCorrente).toBe(5000)
  })

  it('rejeita número de conta abaixo do mínimo', async () => {
    await expect(contasService.criar(999, 0, 0, 1, 1)).rejects.toThrow('Número de conta deve ser entre 1000 e 99999')
  })

  it('rejeita número de conta acima do máximo', async () => {
    await expect(contasService.criar(100000, 0, 0, 1, 1)).rejects.toThrow('Número de conta deve ser entre 1000 e 99999')
  })

  it('rejeita limite acima de R$ 30.000', async () => {
    await expect(contasService.criar(5000, 30001, 0, 1, 1)).rejects.toThrow('Limite deve ser entre R$ 0 e R$ 30.000')
  })

  it('rejeita limite negativo', async () => {
    await expect(contasService.criar(5000, -1, 0, 1, 1)).rejects.toThrow('Limite deve ser entre R$ 0 e R$ 30.000')
  })
})

// ──────────────────────────────────────────────
// verificarOwnership
// ──────────────────────────────────────────────
describe('verificarOwnership', () => {
  it('retorna true quando pessoa é dona da conta', async () => {
    vi.mocked(prisma.contaBancaria.findUnique).mockResolvedValue(mockConta as any)
    expect(await contasService.verificarOwnership(1, 1)).toBe(true)
  })

  it('retorna false quando pessoa não é dona da conta', async () => {
    vi.mocked(prisma.contaBancaria.findUnique).mockResolvedValue(mockConta as any)
    expect(await contasService.verificarOwnership(1, 99)).toBe(false)
  })

  it('retorna false quando conta não existe', async () => {
    vi.mocked(prisma.contaBancaria.findUnique).mockResolvedValue(null)
    expect(await contasService.verificarOwnership(999, 1)).toBe(false)
  })
})
