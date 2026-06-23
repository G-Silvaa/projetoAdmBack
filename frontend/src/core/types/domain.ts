export interface IContato {
  nome: string;
  email: string | null;
  telefone: string | null;
}

export interface IRepresentante {
  id?: number;
  contato?: IContato;
  parentesco?: string;
  cpf?: string;
  rg?: string | null;
  nascimento?: string;
  // Compat / fallback campos achados em respostas legadas
  nome?: string;
  email?: string;
  telefone?: string;
}

export interface IEndereco {
  cep: string;
  logradouro: string;
  complemento?: string | null;
  bairro: string;
  cidade: string;
}

export interface ICliente {
  id: number;
  contato: IContato;
  cpf: string;
  rg: string;
  nascimento: string;
  endereco?: IEndereco;
  representante?: IRepresentante | null;
}

export type StatusProcesso =
  | "AGUARDANDO"
  | "PENDENTE"
  | "ANALISE"
  | "CUMPRIMENTO_EXIGENCIA"
  | "ANALISE_ADMINISTRATIVA"
  | "APROVADO"
  | "REPROVADO";

export const STATUS_PROCESSO_LABELS: Record<StatusProcesso, string> = {
  AGUARDANDO: "Aguardando",
  PENDENTE: "Pendente",
  ANALISE: "Análise",
  CUMPRIMENTO_EXIGENCIA: "Cumprimento com exigência",
  ANALISE_ADMINISTRATIVA: "Análise administrativa",
  APROVADO: "Aprovado",
  REPROVADO: "Reprovado",
};

export interface IProcesso {
  id: number;
  numeroProtocolo?: string;
  entradaDoProtocolo?: string;
  status: StatusProcesso;
  beneficio?: string;
  cessacao?: string;
  dataConcessao?: string;
  periciaMedica?: string;
  avaliacaoSocial?: string;
  documentosPendentes?: string;
  contrato: {
    id: number;
    beneficio: string;
    cliente: ICliente;
  };
}

export interface IContrato {
  id: number;
  beneficio: string;
  numero: string;
  inicio: string;
  conclusao?: string | null;
  valor?: number;
  cliente: ICliente;
}

export interface IFinanceiro {
  id: number;
  valorTotalPagar?: number;
  montantePago?: number;
  valorProximaParcela?: number;
  parcelasRestantes?: number;
  situacaoParcela?: string;
  situacaoPagamento?: boolean;
  dataPagamentoParcela?: string | null;
  contrato: IContrato;
}

export interface PagedResponse<T> {
  content: T[];
  totalElements?: number;
  totalPages?: number;
  number?: number;
  size?: number;
}

export interface DashboardStatusSummary {
  status: string;
  total: number;
}

export interface DashboardOverview {
  totalClientes: number;
  contratosAtivos: number;
  contratosEncerrados: number;
  processosEmAndamento: number;
  processosPendentes: number;
  processosConcedidos: number;
  financeirosEmAberto: number;
  financeirosQuitados: number;
  valorCarteira: number;
  statusProcessos: DashboardStatusSummary[];
}
