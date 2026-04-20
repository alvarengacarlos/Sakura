# Gateway API
Responsável por administrar webhooks do Whatsapp. 

## Executando
Configure as variáveis de ambiente:

| Variável                | Descrição                                                                               | Padrão               |
| :---------------------- | :-------------------------------------------------------------------------------------- | :------------------- |
| `WHATSAPP_ACCESS_TOKEN` | **Obrigatório.** Seu token de acesso de Usuário do Sistema da API do WhatsApp Business. | -                    |
| `WHATSAPP_VERIFY_TOKEN` | **Obrigatório.** Token de verificação para o webhook do WhatsApp.                       | -                    |
| `GRAPH_API_BASE_URL`    | URL base para download de imagens da API do Graph.                                      | `https://graph.facebook.com/` |
| `DB_USERNAME`           | Nome de usuário do banco de dados PostgreSQL.                                           | `pg`                 |
| `DB_PASSWORD`           | Senha do banco de dados PostgreSQL.                                                     | `pgpw`               |
| `IMAGES_PATH`           | Diretório local onde as imagens serão armazenadas.                                      | `/tmp/sakura/images` |


> Exemplo para Linux:
> ```bash
> export WHATSAPP_ACCESS_TOKEN="seu_token_aqui"
> ```

Execute o comando:
```bash
./mvnw spring-boot:run
```
