import { z } from 'zod'

export const criarAgenciaSchema = z.object({
  numero: z.number().int().min(1).max(10000),
  endereco: z.string().min(1).max(100),
  cidade: z.string().min(1).max(25),
})

export const atualizarAgenciaSchema = z.object({
  endereco: z.string().min(1).max(100).optional(),
  cidade: z.string().min(1).max(25).optional(),
})
