import { z } from 'zod'

export const criarContaSchema = z.object({
  numContaCorrente: z.number().int().positive(),
  limiteChequeEspecial: z.number().min(0),
  saldo: z.number().min(0),
  pessoaId: z.number().int().positive(),
  agenciaId: z.number().int().positive(),
})

export const atualizarContaSchema = z.object({
  limiteChequeEspecial: z.number().min(0).optional(),
  agenciaId: z.number().int().positive().optional(),
})

export const operacaoSchema = z.object({
  valor: z.number().positive(),
})

export const transferirSchema = z.object({
  valor: z.number().positive(),
  destinoId: z.number().int().positive().optional(),
  destinoNumConta: z.number().int().positive().optional(),
}).refine(d => d.destinoId != null || d.destinoNumConta != null, {
  message: 'Informe destinoId ou destinoNumConta',
})
