import { z } from 'zod'

export const loginSchema = z.object({
  cpf: z.string().min(11).max(14),
  senha: z.string().min(1),
})

export const trocarSenhaSchema = z.object({
  senhaAtual: z.string().min(1),
  novaSenha: z.string().min(6, 'Nova senha deve ter no mínimo 6 caracteres'),
})

export const refreshSchema = z.object({
  refreshToken: z.string().min(1),
})

export const logoutSchema = z.object({
  refreshToken: z.string().min(1),
})
