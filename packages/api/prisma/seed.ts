import { PrismaClient } from '@prisma/client'
import bcrypt from 'bcryptjs'
import { normalizeCpf } from '../src/lib/cpf'

const prisma = new PrismaClient()

async function main() {
  const cpf = normalizeCpf(process.env.ADMIN_CPF ?? '000.000.000-00')
  const senha = process.env.ADMIN_SENHA ?? 'admin123'
  const hash = await bcrypt.hash(senha, 10)

  await prisma.usuario.upsert({
    where: { cpf },
    update: {},
    create: { cpf, senha: hash, role: 'ADMIN', pessoaId: null },
  })

  console.log(`Admin criado: CPF=${cpf}`)
}

main()
  .catch(console.error)
  .finally(() => prisma.$disconnect())
