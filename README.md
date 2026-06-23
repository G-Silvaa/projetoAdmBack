# Arctech — Assessoria Previdenciária (monorepo)

Stack completa em um repositório só: **API** (Spring Boot) + **front** (Next.js) + **PostgreSQL**, orquestrados por Docker Compose. Sobe tudo com um comando e o banco já vem populado com dados de demonstração.

```
.
├── backend/    # API Spring Boot (Java 21) — contexto /liv-api
├── frontend/   # App Next.js 14 (App Router)
└── docker-compose.yml
```

## Subir tudo (deploy local)

Requisito: Docker + Docker Compose.

```bash
docker compose up --build
```

| Serviço | URL |
| ------- | --- |
| Front | http://localhost:3000 |
| API | http://localhost:8080/liv-api |
| Swagger | http://localhost:8080/liv-api/swagger-ui.html |
| PostgreSQL | localhost:5433 |

Parar: `docker compose down` · Resetar o banco (re-seed): `docker compose down -v`

As migrations do Flyway rodam automaticamente na subida da API, incluindo o **seed de demonstração** (clientes, contratos, processos e financeiro).

## Usuários de demonstração

Senha de todos: `arctech123`

| Nível | E-mail |
| ----- | ------ |
| ADMINISTRADOR | admin@arctech.com.br |
| GESTOR | gestor@arctech.com.br |
| OPERADOR | operador@arctech.com.br |
| FINANCEIRO | financeiro@arctech.com.br |
| CONSULTA | consulta@arctech.com.br |

> Contas de demonstração com senha conhecida — troque antes de ir para produção.

## Configuração

Copie `.env.example` para `.env` e ajuste se necessário (porta do banco, URL pública da API no deploy, chave do AbacatePay).

- **Pagamento PIX (AbacatePay):** sem `ABACATEPAY_API_KEY` roda em **modo mock/sandbox** (QR gerado localmente, confirmação por simulação). Informe uma chave dev (`abc_dev_...`) para usar o sandbox real.
- **Deploy real:** ajuste `NEXT_PUBLIC_API_URL` para o domínio público da API (o front chama a API pelo browser).

## Desenvolvimento (sem Docker)

```bash
# banco
docker compose up -d db
# API
cd backend && DB_PORT=5433 ./mvnw spring-boot:run
# front
cd frontend && npm install && npm run dev
```
