# LIV API

Backend Spring Boot da LIV Assessoria Previdenciária.

## Requisitos

- Java 21+
- Docker e Docker Compose

## Rodando com Docker

Suba aplicação e banco:

```bash
docker compose up --build
```

URLs:

- API: `http://localhost:8080/liv-api`
- Swagger UI: `http://localhost:8080/liv-api/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/liv-api/api-docs`
- PostgreSQL exposto no host: `localhost:5433`

Parar containers:

```bash
docker compose down
```

Parar e remover volume do banco:

```bash
docker compose down -v
```

## Rodando localmente

Suba só o PostgreSQL:

```bash
docker compose up -d db
```

Depois inicie a API:

```bash
DB_PORT=5433 ./mvnw spring-boot:run
```

## Banco de dados

- Banco padrão: `liv`
- Usuário padrão: `postgres`
- Senha padrão: `postgres`
- Schema usado pela aplicação: `liv`
- Porta do PostgreSQL no host ao usar Docker Compose: `5433`

As migrations em `src/main/resources/db/migration` são executadas automaticamente pelo Flyway na subida da aplicação.
