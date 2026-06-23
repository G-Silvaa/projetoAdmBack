# Arctech ADM Front

Frontend administrativo do Arctech (assistência previdenciária), construído com Next.js 14 (App Router), TypeScript, Tailwind e shadcn/ui.

Migrado do Angular 18 (`freela-dev-angular-bkp/`) preservando paridade de features e adotando a estrutura/design do `edital-360-front`.

## Requisitos

- Node.js 20+
- npm 10+

## Instalação

```bash
npm install
```

## Variáveis de ambiente

```bash
cp .env.example .env
```

- `NEXT_PUBLIC_API_URL`: URL da API (default: `https://liv-api-demo.onrender.com/liv-api/`)
- `API_URL`: mesma URL para uso server-side / middleware

## Desenvolvimento

```bash
npm run dev
```

Aplicação em `http://localhost:3000`.

## Build de produção

```bash
npm run build
npm start
```

## Estrutura

- `src/app` — App Router (route groups `(auth)` e `(private)`)
- `src/components/ui` — primitivos shadcn/ui
- `src/components/layout` — sidebar e header da área privada
- `src/core/services` — clientes axios para a Arctech API
- `src/core/types` — tipos de domínio (auth, cliente, processo, contrato, financeiro)
- `src/core/consts` — constantes (modalidades, benefícios, status)
- `src/hooks` — hooks compartilhados
- `src/lib` — utilitários (`cn` etc.)

## Domínio

5 níveis de acesso (`ADMINISTRADOR`, `GESTOR`, `OPERADOR`, `FINANCEIRO`, `CONSULTA`).
Módulos: Painel, Clientes, Processos, Contratos, Modalidades, Finanças, Usuários.
