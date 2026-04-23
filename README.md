# Sakura
## Descrição
Gerador de lista de compras com base em compras do passado.

## Implantação
> **Requisitos:** Docker Compose, Linux

Copie o arquivo `example.env` e renomeie a cópia para `.env`.

Configure as variáveis de ambiente no arquivo `.env`.

### Banco de dados
Inicie container do Postgres:
```bash
docker compose up postgres -d
```

### Migrações
Execute as migrações:
```bash
docker compose run --rm databasemigration ./mvnw liquibase:update
```

### Api e Jobs
Inicie os containeres:
```bash
docker compose up -d gatewayapi imageanalyzer dataanalyzer notifier
```
