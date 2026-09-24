import jwt from 'jsonwebtoken'
import { Role } from '@prisma/client'

export interface JwtPayload {
  sub: number
  cpf: string
  role: Role
  pessoaId: number | null
}

import { env } from './env'

const SECRET = env.JWT_SECRET
const EXPIRES = '8h'

export function signToken(payload: JwtPayload): string {
  return jwt.sign(payload, SECRET, { expiresIn: EXPIRES })
}

export function verifyToken(token: string): JwtPayload {
  return jwt.verify(token, SECRET) as unknown as JwtPayload
}
