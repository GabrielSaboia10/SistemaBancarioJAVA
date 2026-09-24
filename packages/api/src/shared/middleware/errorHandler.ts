import { Request, Response, NextFunction } from 'express'
import { Prisma } from '@prisma/client'
import { AppError } from '../errors/AppError'

export function errorHandler(err: unknown, _req: Request, res: Response, _next: NextFunction) {
  if (err instanceof AppError) {
    return res.status(err.statusCode).json({ erro: err.message })
  }

  if (err instanceof Prisma.PrismaClientKnownRequestError) {
    if (err.code === 'P2025') {
      return res.status(404).json({ erro: 'Recurso não encontrado' })
    }
    if (err.code === 'P2002') {
      return res.status(409).json({ erro: 'Registro duplicado — valor já existe' })
    }
  }

  console.error(err)
  return res.status(500).json({ erro: 'Erro interno do servidor' })
}
