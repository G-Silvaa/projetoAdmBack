export type NivelUsuario =
  | "ADMINISTRADOR"
  | "GESTOR"
  | "OPERADOR"
  | "FINANCEIRO"
  | "CONSULTA";

export interface AuthUser {
  id: number;
  nome: string;
  email: string;
  nivel: NivelUsuario;
  ativo: boolean;
  criadoEm: string;
  atualizadoEm: string;
  ultimoAcesso: string | null;
}

export interface AuthResponse {
  accessToken: string;
  user: AuthUser;
}

export interface LoginPayload {
  email: string;
  senha: string;
}

export interface RegisterPayload {
  nome: string;
  email: string;
  senha: string;
  /** Código do plano escolhido na landing (normal, profissional, escritorio). */
  planoCodigo?: string;
}

export interface Assinatura {
  planoCodigo: string;
  planoNome: string;
  preco: number;
  status: string;
  statusLabel: string;
  trialAte: string | null;
  vencimento: string | null;
  permiteAcesso: boolean;
  diasRestantes: number | null;
  maxUsuarios: number | null;
  maxClientes: number | null;
}

export interface PlanoApi {
  codigo: string;
  nome: string;
  preco: number;
  maxUsuarios: number | null;
  maxClientes: number | null;
}

export interface Cobranca {
  id: number;
  planoNome: string;
  valor: number;
  status: string;
  statusLabel: string;
  brCode: string | null;
  brCodeBase64: string | null;
  expiraEm: string | null;
  pago: boolean;
}

export interface NivelUsuarioOption {
  value: NivelUsuario;
  label: string;
}

export interface UsuarioCreatePayload {
  nome: string;
  email: string;
  senha: string;
  nivel: NivelUsuario;
}

export interface UsuarioUpdatePayload {
  nome?: string;
  email?: string;
  senha?: string;
  nivel?: NivelUsuario;
  ativo?: boolean;
}

export const NIVEL_LABELS: Record<NivelUsuario, string> = {
  ADMINISTRADOR: "Administrador",
  GESTOR: "Gestor",
  OPERADOR: "Operador",
  FINANCEIRO: "Financeiro",
  CONSULTA: "Consulta",
};

export const ROUTE_ACCESS: Record<NivelUsuario, string[]> = {
  ADMINISTRADOR: [
    "/home",
    "/clientes",
    "/processos",
    "/contratos",
    "/financas",
    "/modalidades",
    "/relatorios",
    "/usuarios",
    "/assinatura",
  ],
  GESTOR: [
    "/home",
    "/clientes",
    "/processos",
    "/contratos",
    "/financas",
    "/modalidades",
    "/relatorios",
    "/assinatura",
  ],
  OPERADOR: ["/home", "/clientes", "/processos", "/contratos", "/modalidades", "/relatorios", "/assinatura"],
  FINANCEIRO: ["/home", "/contratos", "/financas", "/modalidades", "/relatorios", "/assinatura"],
  CONSULTA: [
    "/home",
    "/clientes",
    "/processos",
    "/contratos",
    "/financas",
    "/modalidades",
    "/relatorios",
    "/assinatura",
  ],
};

export const CAPABILITY_ACCESS = {
  manageUsers: ["ADMINISTRADOR"],
  manageClientes: ["ADMINISTRADOR", "GESTOR", "OPERADOR"],
  manageProcessos: ["ADMINISTRADOR", "GESTOR", "OPERADOR"],
  issueProcessLetters: ["ADMINISTRADOR", "GESTOR", "OPERADOR"],
  manageContratos: ["ADMINISTRADOR", "GESTOR", "OPERADOR"],
  renewContratos: ["ADMINISTRADOR", "GESTOR", "OPERADOR"],
  downloadContratos: ["ADMINISTRADOR", "GESTOR", "OPERADOR", "FINANCEIRO", "CONSULTA"],
  manageFinancas: ["ADMINISTRADOR", "GESTOR", "FINANCEIRO"],
  operateFinanceiroDocuments: ["ADMINISTRADOR", "GESTOR", "FINANCEIRO"],
} as const satisfies Record<string, readonly NivelUsuario[]>;

export type Capability = keyof typeof CAPABILITY_ACCESS;
