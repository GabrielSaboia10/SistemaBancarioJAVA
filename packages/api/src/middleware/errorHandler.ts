import { Request, Response, NextFunction } from 'express'

export function errorHandler(err: Error, _req: Request, res: Response, _next: NextFunction) {
  console.error(err.message)
  const status = err.message.includes('não encontrad') ? 404 : 400
  res.status(status).json({ erro: err.message })
}
