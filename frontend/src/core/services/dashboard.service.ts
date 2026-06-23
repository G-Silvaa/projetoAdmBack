import { api } from "./http";
import type { DashboardOverview, PagedResponse } from "@/core/types/domain";
import { formatCPF, formatDateBR } from "@/core/helpers/format";

const STATUS_LABEL: Record<string, string> = {
  AGUARDANDO: "Aguardando",
  PENDENTE: "Pendente",
  ANALISE: "Análise",
  CUMPRIMENTO_EXIGENCIA: "Cumprimento com Exigência",
  ANALISE_ADMINISTRATIVA: "Análise Administrativa",
  APROVADO: "Aprovado",
  REPROVADO: "Reprovado",
};

function formatRow(item: Record<string, unknown>) {
  return {
    Nome: item.nome,
    CPF: formatCPF(item.cpf as string),
    Telefone: item.telefone,
    "Número do Protocolo": item.numeroProtocolo,
    Status: STATUS_LABEL[item.status as string] ?? item.status,
    Beneficio: item.beneficio,
    "Data de Concessão": formatDateBR(item.dataConcessao as string),
    Cessação: item.cessacao ? formatDateBR(item.cessacao as string) : "",
  };
}

export const dashboardService = {
  async getOverview(): Promise<DashboardOverview> {
    const { data } = await api.get<DashboardOverview>("dashboard/overview");
    return data;
  },
  async getCessacao(): Promise<PagedResponse<Record<string, unknown>>> {
    const { data } = await api.get<PagedResponse<Record<string, unknown>>>("processo/cessacao");
    data.content = (data.content ?? []).map(formatRow);
    return data;
  },
  async getConcedidos(): Promise<PagedResponse<Record<string, unknown>>> {
    const { data } = await api.get<PagedResponse<Record<string, unknown>>>("processo/concedido");
    data.content = (data.content ?? []).map(formatRow);
    return data;
  },
};
