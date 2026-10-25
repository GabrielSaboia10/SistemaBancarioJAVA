# App mobile (React Native + Expo)

App de celular do Sistema Bancário. Usa a mesma API Spring Boot do site ([`../backend`](../backend)).

Feito com **Expo 57**, **Expo Router** (navegação por abas) e TypeScript. Cliente e administrador usam o mesmo app: as telas mudam conforme o perfil de quem entra.

| Perfil | Abas | Outras telas |
|---|---|---|
| Cliente | Minhas contas, Operações, Perfil | Conta (extrato com filtro de período e exportação) |
| Admin | Painel, Clientes, Contas, Operações, Perfil | Conta (também editar e encerrar), Agências, Auditoria |

No primeiro acesso (ou depois que o admin gera uma nova senha), o app só mostra a tela de troca de senha.

## Como rodar no celular

1. Suba a API: `cd ../backend && ./mvnw spring-boot:run`
2. Instale o app **Expo Go** no celular (Play Store ou App Store).
3. Rode o app:
   ```bash
   npm install
   npm start
   ```
4. Leia o QR code que aparece no terminal com o Expo Go (Android) ou com a câmera (iPhone).

O celular precisa estar **na mesma rede Wi-Fi do PC**. O app descobre sozinho o IP do PC e chama a API em `http://IP-DO-PC:8080`. Se o Windows perguntar, libere o Java no firewall para a rede privada.

Para apontar para outra API, crie um arquivo `.env` com `EXPO_PUBLIC_API_URL=https://endereco-da-api`.

## Outros comandos

| Comando | O que faz |
|---|---|
| `npm run web` | Abre o app no navegador (útil para desenvolver). A API precisa liberar a origem: `CORS_ORIGINS=http://localhost:8081` |
| `npm test` | Testes (Jest + Testing Library) |
| `npm run typecheck` | Checagem de tipos |

## Organização

```
src/
  app/          telas (cada arquivo é uma rota do Expo Router)
    (abas)/     telas com barra de abas
    conta/[id]  detalhe da conta e extrato
  api/          cliente HTTP (token, renovação automática), endpoints e tipos
  auth/         contexto da sessão (quem está logado)
  components/   componentes visuais, busca com sugestões, diálogos
  hooks/        lista paginada, atraso para busca
  utils/        formatação (R$, CPF, datas) e compartilhamento de arquivos
```

## Segurança

- O refresh token fica no **SecureStore** (Keychain no iPhone, Keystore no Android), criptografado. O access token fica só em memória.
- Quando o token expira, o app renova sozinho e repete a requisição. Várias requisições ao mesmo tempo esperam uma única renovação.
- As permissões de verdade são checadas na API; o app só esconde o que o perfil não pode usar.
