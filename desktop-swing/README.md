# Versão desktop (Java Swing)

Primeira versão do sistema, antes da API REST e do frontend Angular. É um app de janelas em Java Swing, no padrão **MVC + DAO**, que grava os dados em arquivo local (serialização Java).

Existem duas versões aqui, mantidas como histórico:

| Pasta | Versão | Diferenças |
|---|---|---|
| [`src/`](src/) | **Atual** | Login com CPF e senha, perfis ADMIN/CLIENTE, "Minhas contas", troca de senha e configuração do primeiro administrador |
| [`versao-inicial/src/`](versao-inicial/src/) | Inicial | Sem login; tem a tela de operações bancárias (depósito, saque e transferência) |

## Como executar

1. Importe a pasta no Eclipse como *Existing Java Project* (ou abra no IntelliJ e marque `src` como pasta de código).
2. Execute a classe `controller.CtrlPrograma`.

Requer Java 11 ou superior. Os dados ficam em `usuarios.dat` e arquivos semelhantes, criados na pasta em que o programa roda (eles não vão para o git).

> A versão principal do sistema hoje é a web: API em [`../backend`](../backend) e interface em [`../frontend`](../frontend).
