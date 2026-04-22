# Sistema Bancário Java

Sistema de gerenciamento bancário desenvolvido em Java com interface gráfica Swing, seguindo o padrão arquitetural **MVC** (Model-View-Controller) com camada **DAO** para persistência de dados.

## Funcionalidades

- **Cadastro de Pessoas** — Incluir, alterar, excluir e consultar clientes
- **Cadastro de Agências Bancárias** — Gerenciar agências com número, endereço e cidade
- **Cadastro de Contas Bancárias** — Criar contas vinculadas a correntistas e agências
- **Operações Bancárias** — Depósito, saque e transferência entre contas
- **Persistência de dados** — Dados salvos em arquivo local entre sessões

## Tecnologias

- **Java 11+**
- **Java Swing** com Look & Feel Nimbus
- **Padrão MVC** + **DAO**
- **Serialização Java** para persistência

## Como executar

1. Clone o repositório
2. Importe o projeto no Eclipse como *Existing Java Project*
3. Execute a classe `controller.CtrlPrograma`

## Estrutura do projeto

```
src/
├── controller/      # Controladores (MVC)
├── model/           # Entidades e regras de negócio
│   └── dao/         # Acesso e persistência de dados
└── viewer/          # Interface gráfica (Swing)
```

## Regras de negócio

| Entidade | Restrições |
|---|---|
| Pessoa | CPF com 14 caracteres, nome até 40 caracteres, idade 0-150 |
| Conta | Número entre 1000-99999, limite até R$ 30.000 |
| Agência | Número até 10000, endereço até 100 caracteres |
| Saque | Saldo não pode ficar abaixo do limite negativo |
| Transferência | Valida saldo da conta origem antes de transferir |
