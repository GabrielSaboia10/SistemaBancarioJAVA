const required = ['DATABASE_URL', 'JWT_SECRET'] as const

for (const key of required) {
  if (!process.env[key]) {
    console.error(`[env] variável de ambiente obrigatória ausente: ${key}`)
    process.exit(1)
  }
}

export const env = {
  DATABASE_URL: process.env.DATABASE_URL as string,
  JWT_SECRET: process.env.JWT_SECRET as string,
  PORT: process.env.PORT ?? '3001',
}
