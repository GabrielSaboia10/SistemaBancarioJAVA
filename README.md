# Sistema Bancário

[![CI](https://github.com/GabrielSaboia10/SistemaBancarioJAVA/actions/workflows/ci.yml/badge.svg)](https://github.com/GabrielSaboia10/SistemaBancarioJAVA/actions/workflows/ci.yml)

Sistema bancário com **API REST em Java (Spring Boot)**, **site em Angular** e **app de celular em React Native (Expo)**. Os dois usam a mesma API.

Começou como um projeto desktop em Java Swing (ainda disponível em [`desktop-swing/`](desktop-swing/)) e evoluiu para uma aplicação web completa.

## Funcionalidades

**Clientes**
- Login com CPF e senha; no primeiro acesso, troca obrigatória da senha temporária
- Minhas contas, depósito, saque (com cheque especial) e transferência
- Extrato com filtro por período e exportação para Excel (CSV)
- Troca de senha no Perfil

**Administração**
- Painel com indicadores (clientes, agências, contas, saldo em custódia)
- Cadastro de clientes, agências e contas, com busca
- Bloqueio e liberação de acesso (vale na hora, sem esperar o token expirar)
- Geração de nova senha temporária para o cliente
- Operações em qualquer conta, com busca por número ou nome do titular
- Auditoria: registro de quem fez cada ação administrativa e quando

**Celular**
- App React Native (Expo) com as funções de cliente e de administrador
- O site também tem layout para celular e pode ser instalado na tela inicial (PWA)

## Tecnologias

| Camada | Stack |
|---|---|
| Backend | Java 21, Spring Boot 4, Spring Security (OAuth2 Resource Server / JWT), Spring Data JPA, Flyway, Bean Validation, springdoc (Swagger) |
| Banco | PostgreSQL (produção) · H2 (desenvolvimento e testes) |
| Cache | Redis (rate limit compartilhado entre instâncias) |
| Frontend | Angular 22 (standalone, signals, zoneless), Reactive Forms, PWA |
| Mobile | React Native 0.86, Expo 57, Expo Router, SecureStore |
| Testes | JUnit 5 + MockMvc (backend) · Vitest (frontend) · Jest + Testing Library (mobile) |
| Infra | Docker Compose + nginx · GitHub Actions |

## Segurança

- **Deny by default:** toda rota não listada em [`SecurityConfig`](backend/src/main/java/com/banco/config/SecurityConfig.java) é negada.
- **Verificação de dono:** um cliente que tenta ver ou movimentar a conta de outra pessoa recebe **403**.
- **Rate limit** (respostas **429** com `Retry-After`), em memória ou no Redis:

  | Regra | Limite |
  |---|---|
  | Login/refresh por IP | 10 a cada 15 min |
  | Senhas erradas por CPF (bloqueia o CPF) | 5 a cada 15 min |
  | Depósito/saque/transferência por usuário | 30 por minuto |
  | Demais rotas por IP | 300 a cada 15 min |

- **Senhas** com BCrypt. Senha temporária gerada aleatoriamente e troca obrigatória no primeiro acesso.
- **Refresh tokens** guardados só como hash SHA-256, rotacionados a cada uso e invalidados na troca de senha, no bloqueio e na redefinição de senha.
- **Bloqueio imediato:** a cada requisição a API confere se o usuário continua ativo.
- **Dinheiro em `BigDecimal`** (`NUMERIC(15,2)` no banco), sem erro de arredondamento.
- **Lock pessimista** no saque e na transferência: operações simultâneas na mesma conta não passam do limite. Há um teste com 20 saques em paralelo para garantir isso.
- **Login não revela** se um CPF está cadastrado. Swagger desligado em produção. Headers de segurança (CSP, X-Frame-Options) no nginx.

### Permissões por endpoint

| Endpoint | Acesso |
|---|---|
| `POST /api/auth/login`, `/refresh`, `/logout` · `GET /actuator/health` | Público |
| `GET /api/auth/me` · `PUT /api/auth/senha` | Logado (liberado mesmo com senha temporária) |
| `GET /api/contas`, `/stats`, `/{id}`, `/{id}/extrato`, `/{id}/extrato.csv` | Logado (cliente: só as próprias) |
| `POST /api/contas/{id}/depositar`, `/sacar`, `/transferir` | Logado (cliente: só as próprias) |
| `/api/pessoas/**` · `/api/agencias/**` · `GET /api/auditoria` · `POST/PUT/DELETE /api/contas` | ADMIN |

## Como rodar (desenvolvimento)

Pré-requisitos: **Java 21** e **Node 24**. Não precisa instalar Maven, banco de dados nem Redis.

```bash
# 1. API (http://localhost:8080) com banco H2 em arquivo e dados de exemplo
cd backend
./mvnw spring-boot:run

# 2. Frontend (http://localhost:4200), em outro terminal
cd frontend
npm install
npm start
```

Usuários de exemplo, criados só no perfil `dev`:

| Perfil | CPF | Senha |
|---|---|---|
| Admin | 000.000.000-00 | admin123 |
| Cliente | 529.982.247-25 | cliente123 |
| Cliente | 111.444.777-35 | cliente123 |

- **Documentação da API (Swagger):** http://localhost:8080/swagger-ui.html
- **Testar no celular:** rode `npm run start:celular` e acesse `http://IP-DO-SEU-PC:4200` pelo celular, na mesma rede Wi-Fi.
- **Rate limit no Redis:** rode a API com `RATE_LIMIT_ARMAZENAMENTO=redis` (e `REDIS_HOST`/`REDIS_PORT`, padrão `localhost:6379`).

## Como rodar com Docker

```bash
cp .env.example .env    # e troque as senhas e o JWT_SECRET
docker compose up --build
```

Acesse http://localhost:8080. O nginx serve o Angular e encaminha `/api` para o Spring, que usa PostgreSQL e Redis.

## App de celular

Instale o **Expo Go** no celular, suba a API e rode `cd mobile && npm install && npm start`. Leia o QR code com o Expo Go. O celular precisa estar na mesma rede Wi-Fi do PC. Detalhes em [`mobile/README.md`](mobile/README.md).

## Testes

```bash
cd backend && ./mvnw test     # 32 testes: regras da conta, CPF, permissões, senha temporária, bloqueio,
                              # auditoria, busca, extrato/CSV, rate limit (memória e Redis), concorrência
cd frontend && npm test       # 26 testes: login, guards, refresh de token, busca, operações, formatadores
cd mobile && npm test         # 16 testes: renovação de token, erros da API, login, formatadores
```

O teste do Redis é pulado automaticamente se não houver um Redis em `localhost:6379`. No CI (GitHub Actions) ele roda com um Redis de verdade.

## Estrutura

```
backend/                 API REST (Spring Boot)
  src/main/java/com/banco/
    config/              segurança, JWT, CORS, Swagger, criação do admin e dados de exemplo
    controller/          endpoints REST
    dto/                 contratos de entrada e saída (records)
    exception/           erros padronizados (ApiError)
    model/               entidades JPA com as regras da conta (saque, depósito, limite)
    repository/          Spring Data JPA (com lock pessimista)
    security/            rate limit (memória/Redis), token, bloqueio e senha temporária, handlers 401/403
    service/             regras de negócio e auditoria
  src/main/resources/db/migration/   schema do banco (Flyway)
frontend/                site em Angular + PWA
  src/app/core/          autenticação, interceptor, guards, chamadas à API
  src/app/layout/        menu lateral (desktop) e barra inferior (celular)
  src/app/pages/         telas
  src/app/shared/        componentes reutilizáveis (busca, modal, paginação, ícones)
mobile/                  app React Native (Expo)
  src/app/               telas (rotas do Expo Router)
  src/api/               cliente HTTP, endpoints e tipos
desktop-swing/           versões desktop originais (Java Swing, MVC + DAO)
.github/workflows/       CI: testes do backend, do frontend e do mobile, build das imagens Docker
```

## Regras de negócio

| Entidade | Regra |
|---|---|
| Pessoa | CPF válido (dígitos verificadores), nome até 40 caracteres, idade 0–150. Não pode ser excluída se tiver contas. |
| Conta | Número entre 1000 e 99999, limite de cheque especial até R$ 30.000. Só pode ser encerrada com saldo zero. |
| Agência | Número entre 1 e 9999. Não pode ser excluída se tiver contas. |
| Saque | O saldo não pode ficar abaixo de −limite. |
| Transferência | Valida o saldo da origem. Não permite transferir para a mesma conta. Registra no extrato das duas contas. |
| Acesso | Usuário bloqueado não entra e perde a sessão na hora. Senha temporária só permite trocar a senha. |
