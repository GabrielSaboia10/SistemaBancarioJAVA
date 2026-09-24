import { extendZodWithOpenApi, OpenAPIRegistry, OpenApiGeneratorV3 } from '@asteasolutions/zod-to-openapi'
import { z } from 'zod'

extendZodWithOpenApi(z)

const registry = new OpenAPIRegistry()

const bearerAuth = registry.registerComponent('securitySchemes', 'bearerAuth', {
  type: 'http',
  scheme: 'bearer',
  bearerFormat: 'JWT',
})

const security = [{ [bearerAuth.name]: [] }]

// --- Schemas reutilizáveis ---

const PessoaSchema = registry.register('Pessoa', z.object({
  id: z.number(),
  cpf: z.string(),
  nome: z.string(),
  idade: z.number(),
  createdAt: z.string(),
  updatedAt: z.string(),
}).openapi('Pessoa'))

const AgenciaSchema = registry.register('AgenciaBancaria', z.object({
  id: z.number(),
  numero: z.number(),
  endereco: z.string(),
  cidade: z.string(),
}).openapi('AgenciaBancaria'))

const ContaSchema = registry.register('ContaBancaria', z.object({
  id: z.number(),
  numContaCorrente: z.number(),
  saldo: z.number(),
  limiteChequeEspecial: z.number(),
  pessoaId: z.number(),
  agenciaId: z.number(),
}).openapi('ContaBancaria'))

const PaginatedPessoas = registry.register('PaginatedPessoas', z.object({
  data: z.array(PessoaSchema),
  total: z.number(),
  page: z.number(),
  limit: z.number(),
  totalPages: z.number(),
}).openapi('PaginatedPessoas'))

const ErroSchema = z.object({ erro: z.string() }).openapi('Erro')
const paginationParams = [
  { in: 'query' as const, name: 'page', schema: { type: 'integer' as const, default: 1 }, required: false },
  { in: 'query' as const, name: 'limit', schema: { type: 'integer' as const, default: 20, maximum: 100 }, required: false },
]

// --- Auth ---

registry.registerPath({
  method: 'post', path: '/api/auth/login', tags: ['Auth'],
  summary: 'Login',
  request: { body: { content: { 'application/json': { schema: z.object({ cpf: z.string(), senha: z.string() }) } } } },
  responses: {
    200: { description: 'Token JWT + refresh token', content: { 'application/json': { schema: z.object({ token: z.string(), refreshToken: z.string(), role: z.string(), nome: z.string() }) } } },
    401: { description: 'Credenciais inválidas', content: { 'application/json': { schema: ErroSchema } } },
  },
})

registry.registerPath({
  method: 'post', path: '/api/auth/refresh', tags: ['Auth'],
  summary: 'Renovar access token via refresh token',
  request: { body: { content: { 'application/json': { schema: z.object({ refreshToken: z.string() }) } } } },
  responses: {
    200: { description: 'Novo par de tokens', content: { 'application/json': { schema: z.object({ token: z.string(), refreshToken: z.string() }) } } },
    401: { description: 'Refresh token inválido ou expirado', content: { 'application/json': { schema: ErroSchema } } },
  },
})

registry.registerPath({
  method: 'post', path: '/api/auth/logout', tags: ['Auth'],
  summary: 'Invalidar refresh token',
  request: { body: { content: { 'application/json': { schema: z.object({ refreshToken: z.string() }) } } } },
  responses: { 204: { description: 'Logout realizado' } },
})

registry.registerPath({
  method: 'get', path: '/api/auth/me', tags: ['Auth'], security,
  summary: 'Dados do usuário autenticado',
  responses: { 200: { description: 'Payload do token', content: { 'application/json': { schema: z.object({ sub: z.number(), cpf: z.string(), role: z.string() }) } } } },
})

registry.registerPath({
  method: 'put', path: '/api/auth/senha', tags: ['Auth'], security,
  summary: 'Trocar senha',
  request: { body: { content: { 'application/json': { schema: z.object({ senhaAtual: z.string(), novaSenha: z.string().min(6) }) } } } },
  responses: {
    200: { description: 'Senha alterada', content: { 'application/json': { schema: z.object({ mensagem: z.string() }) } } },
    401: { description: 'Senha atual incorreta', content: { 'application/json': { schema: ErroSchema } } },
  },
})

// --- Pessoas ---

registry.registerPath({
  method: 'get', path: '/api/pessoas', tags: ['Pessoas'], security,
  summary: 'Listar pessoas (paginado para ADMIN)',
  parameters: paginationParams,
  responses: { 200: { description: 'Lista paginada', content: { 'application/json': { schema: PaginatedPessoas } } } },
})

registry.registerPath({
  method: 'post', path: '/api/pessoas', tags: ['Pessoas'], security,
  summary: 'Criar pessoa (ADMIN)',
  request: { body: { content: { 'application/json': { schema: z.object({ cpf: z.string(), nome: z.string(), idade: z.number() }) } } } },
  responses: { 201: { description: 'Pessoa criada', content: { 'application/json': { schema: PessoaSchema } } } },
})

registry.registerPath({
  method: 'get', path: '/api/pessoas/{id}', tags: ['Pessoas'], security,
  summary: 'Buscar pessoa por ID',
  request: { params: z.object({ id: z.string() }) },
  responses: { 200: { description: 'Pessoa', content: { 'application/json': { schema: PessoaSchema } } } },
})

registry.registerPath({
  method: 'put', path: '/api/pessoas/{id}', tags: ['Pessoas'], security,
  summary: 'Atualizar pessoa (ADMIN)',
  request: { params: z.object({ id: z.string() }), body: { content: { 'application/json': { schema: z.object({ nome: z.string().optional(), idade: z.number().optional() }) } } } },
  responses: { 200: { description: 'Pessoa atualizada', content: { 'application/json': { schema: PessoaSchema } } } },
})

registry.registerPath({
  method: 'delete', path: '/api/pessoas/{id}', tags: ['Pessoas'], security,
  summary: 'Remover pessoa (ADMIN)',
  request: { params: z.object({ id: z.string() }) },
  responses: { 204: { description: 'Removida' } },
})

// --- Agências ---

registry.registerPath({
  method: 'get', path: '/api/agencias', tags: ['Agências'], security,
  summary: 'Listar agências (paginado para ADMIN)',
  parameters: paginationParams,
  responses: { 200: { description: 'Lista paginada', content: { 'application/json': { schema: z.array(AgenciaSchema) } } } },
})

registry.registerPath({
  method: 'post', path: '/api/agencias', tags: ['Agências'], security,
  summary: 'Criar agência (ADMIN)',
  request: { body: { content: { 'application/json': { schema: z.object({ numero: z.number(), endereco: z.string(), cidade: z.string() }) } } } },
  responses: { 201: { description: 'Agência criada', content: { 'application/json': { schema: AgenciaSchema } } } },
})

// --- Contas ---

registry.registerPath({
  method: 'get', path: '/api/contas', tags: ['Contas'], security,
  summary: 'Listar contas (paginado)',
  parameters: paginationParams,
  responses: { 200: { description: 'Lista paginada', content: { 'application/json': { schema: z.array(ContaSchema) } } } },
})

registry.registerPath({
  method: 'post', path: '/api/contas/{id}/depositar', tags: ['Contas'], security,
  summary: 'Depositar',
  request: { params: z.object({ id: z.string() }), body: { content: { 'application/json': { schema: z.object({ valor: z.number().positive() }) } } } },
  responses: { 200: { description: 'Conta atualizada', content: { 'application/json': { schema: ContaSchema } } } },
})

registry.registerPath({
  method: 'post', path: '/api/contas/{id}/sacar', tags: ['Contas'], security,
  summary: 'Sacar',
  request: { params: z.object({ id: z.string() }), body: { content: { 'application/json': { schema: z.object({ valor: z.number().positive() }) } } } },
  responses: { 200: { description: 'Conta atualizada', content: { 'application/json': { schema: ContaSchema } } } },
})

registry.registerPath({
  method: 'post', path: '/api/contas/{id}/transferir', tags: ['Contas'], security,
  summary: 'Transferir',
  request: { params: z.object({ id: z.string() }), body: { content: { 'application/json': { schema: z.object({ valor: z.number().positive(), destinoId: z.number().optional(), destinoNumConta: z.number().optional() }) } } } },
  responses: { 200: { description: 'Conta origem atualizada', content: { 'application/json': { schema: ContaSchema } } } },
})

const generator = new OpenApiGeneratorV3(registry.definitions)

export const openApiDocument = generator.generateDocument({
  openapi: '3.0.0',
  info: { title: 'Sistema Bancário API', version: '1.0.0', description: 'API REST do sistema bancário — documentação interativa' },
  servers: [{ url: 'http://localhost:3001', description: 'Desenvolvimento' }],
})
