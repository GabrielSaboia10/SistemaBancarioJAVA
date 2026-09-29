# Frontend (Angular)

Interface web do Sistema Bancário. Consome a API Spring Boot de [`../backend`](../backend).

Angular 22 com componentes standalone, signals e sem zone.js. Funciona como site e como app instalável (PWA). O app nativo de celular fica em [`../mobile`](../mobile) (React Native).

## Comandos

| Comando | O que faz |
|---|---|
| `npm start` | Servidor de desenvolvimento em http://localhost:4200 (encaminha `/api` para `localhost:8080`) |
| `npm run start:celular` | Mesmo servidor, acessível por outros aparelhos da rede (`http://IP-DO-PC:4200`) |
| `npm test` | Testes (Vitest) |
| `npm run build` | Build de produção em `dist/frontend/browser` |

## Organização

```
src/app/
  core/       autenticação (sessão, refresh de token), interceptor HTTP, guards de rota, ApiService, modelos
  layout/     casca da área logada: menu lateral (desktop) e barra inferior (celular)
  pages/      uma pasta por tela
  shared/     componentes reutilizáveis: busca com sugestões, modal, paginação, ícones, formatadores
src/environments/
  environment.ts   endereço da API (vazio = mesmo domínio, caminhos relativos /api/...)
```

## Sessão

- O access token (15 min) fica só em memória; o refresh token fica no `localStorage` para a sessão sobreviver a um F5.
- Quando a API responde 401, o interceptor renova o token e repete a requisição. Entre abas, a renovação é serializada com a Web Locks API (o refresh token só vale uma vez).
- Usuário com senha temporária só acessa a tela de troca de senha, tanto pelos guards quanto pelo interceptor (403 `TROCA_DE_SENHA_OBRIGATORIA`).
