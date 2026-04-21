# Image Analyzer
Responsável por analisar as imagens e transformar em texto estruturado.

## Executando
Configure as variáveis de ambiente:

| Variável | Descrição | Padrão |
|---|---|---|
| `ANTHROPIC_API_KEY` | Chave de API da Anthropic (obrigatória) | — |
| `ANTHROPIC_MODEL` | Modelo Claude a ser usado | `claude-haiku-4-5-20251001` |
| `DB_USERNAME` | Usuário do banco PostgreSQL | `pg` |
| `DB_PASSWORD` | Senha do banco PostgreSQL | `pgpw` |
| `JOB_CRON` | Expressão cron do job de análise (formato Spring: segundos minutos horas dia-do-mês mês dia-da-semana) | `0 0 12 * * *` |

> Exemplo para Linux:
> ```bash
> export ANTHROPIC_API_KEY="sua_chave"
> ```

Execute o comando:
```bash
./mvnw spring-boot:run
```

A análise será executada de acordo com a configuração da env `JOB_CRON`.
