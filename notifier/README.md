# Notifier
Responsável por verificar os itens com previsão de compra para o dia atual e enviar um e-mail com a lista de compras para o destinatário configurado.

## Executando
Configure as variáveis de ambiente:

| Variável | Descrição | Padrão |
|---|---|---|
| `DB_USERNAME` | Usuário do banco PostgreSQL | `pg` |
| `DB_PASSWORD` | Senha do banco PostgreSQL | `pgpw` |
| `MAIL_HOST` | Host do servidor SMTP | `smtp.gmail.com` |
| `MAIL_PORT` | Porta do servidor SMTP | `587` |
| `MAIL_USERNAME` | Usuário de autenticação SMTP (obrigatório) | — |
| `MAIL_PASSWORD` | Senha de autenticação SMTP (obrigatória) | — |
| `RECIPIENT_EMAIL` | E-mail do destinatário da lista de compras (obrigatório) | — |
| `JOB_CRON` | Expressão cron do job de notificação (formato Spring: segundos minutos horas dia-do-mês mês dia-da-semana) | `0 0 6 * * *` |

> Exemplo para Linux:
> ```bash
> export MAIL_USERNAME="seu_email@gmail.com"
> ```

Execute o comando:
```bash
./mvnw spring-boot:run
```

A notificação será executada de acordo com a configuração da env `JOB_CRON`.
