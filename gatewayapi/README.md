# Gateway API
Responsável por disponibilizar uma página web com botão de upload de imagens.

## Executando
Configure as variáveis de ambiente:

| Variável          | Descrição                                              | Padrão               |
| :---------------- | :----------------------------------------------------- | :------------------- |
| `DB_USERNAME`     | Nome de usuário do banco de dados PostgreSQL.          | `pg`                 |
| `DB_PASSWORD`     | Senha do banco de dados PostgreSQL.                    | `pgpw`               |
| `IMAGES_PATH`     | Diretório local onde as imagens serão armazenadas.     | `/tmp/sakura/images` |
| `UPLOAD_USERNAME` | Nome de usuário para autenticação na página de upload. | `admin`              |
| `UPLOAD_PASSWORD` | Senha para autenticação na página de upload.           | `changeme`           |
| `MAX_FILE_SIZE`   | Tamanho máximo permitido por arquivo enviado.          | `1GB`                |
| `MAX_REQUEST_SIZE`| Tamanho máximo permitido por requisição de upload.     | `1GB`                |


> Exemplo para Linux:
> ```bash
> export UPLOAD_USERNAME="seu_usuario"
> ```

Execute o comando:
```bash
./mvnw spring-boot:run
```

Acesse a página de upload em `http://localhost:8080/`.
