import { Request, Response, NextFunction } from 'express'
import { verifyToken } from '../lib/jwt'
import { Role } from '@prisma/client'

export function authenticate(req: Request, res: Response, next: NextFunction) {
  const header = req.headers.authorization
  if (!header?.startsWith('Bearer ')) {
    res.status(401).json({ erro: 'Não autenticado' })
    return
  }
  try {
    req.user = verifyToken(header.slice(7))
    next()
  } catch {
    res.status(401).json({ erro: 'Token inválido ou expirado' })
  }
}

export function authorize(...roles: Role[]) {
  return (req: Request, res: Response, next: NextFunction) => {
    if (!req.user || !roles.includes(req.user.role)) {
      res.status(403).json({ erro: 'Acesso negado' })
      return
    }
    next()
  }
}
