import { api } from "./http";
import type { PagedResponse } from "@/core/types/domain";
import { formatCPF, formatDateBR, formatDateTimeBR } from "@/core/helpers/format";

const STATUS_LABEL: Record<string, string> = {
  AGUARDANDO: "Aguardando",
  PENDENTE: "Pendente",
  ANALISE: "Análise",
  CUMPRIMENTO_EXIGENCIA: "Cumprimento com Exigência",
  ANALISE_ADMINISTRATIVA: "Análise Administrativa",
  APROVADO: "Aprovado",
  REPROVADO: "Reprovado",
};

export type ProcessoRow = {
  id: number;
  Nome: string;
  CPF: string;
  "Número do protocolo": string;
  Cessação: string;
  Status: string;
  "Perícia médica": string;
  "Avaliação social": string;
  "Entrada do protocolo": string;
};

function formatRow(p: Record<string, unknown>): ProcessoRow {
  const contrato = (p as { contrato?: { cliente?: { contato?: { nome?: string }; cpf?: string } } }).contrato;
  return {
    id: Number(p.id),
    Nome: contrato?.cliente?.contato?.nome ?? "",
    CPF: formatCPF(contrato?.cliente?.cpf ?? ""),
    "Número do protocolo": (p.numeroProtocolo as string) || "Não informado",
    Cessação: formatDateBR(p.cessacao as string),
    Status: STATUS_LABEL[p.status as string] ?? (p.status as string),
    "Perícia médica": formatDateTimeBR(p.periciaMedica as string),
    "Avaliação social": formatDateTimeBR(p.avaliacaoSocial as string),
    "Entrada do protocolo": formatDateBR(p.entradaDoProtocolo as string),
  };
}

export interface ProcessoFiltros {
  Nome?: string;
  CPF?: string;
  numeroProtocolo?: string;
  Status?: string;
}

function buildFilter(filtros: ProcessoFiltros): string {
  const parts: string[] = [];
  if (filtros.Nome) parts.push(`contrato.cliente.contato.nome ilike '${filtros.Nome}%'`);
  if (filtros.CPF) parts.push(`contrato.cliente.cpf ilike '${filtros.CPF}%'`);
  if (filtros.numeroProtocolo) parts.push(`numeroProtocolo ilike '${filtros.numeroProtocolo}%'`);
  if (filtros.Status) parts.push(`status eq '${filtros.Status}'`);
  return parts.join(" and ");
}

export const processosService = {
  async listar(page = 0, size = 50): Promise<PagedResponse<Record<string, unknown>>> {
    const { data } = await api.get<PagedResponse<Record<string, unknown>>>("domain/processo", {
      params: { page, size, fields: "*,contrato.cliente" },
    });
    return data;
  },

  async listarTodos(): Promise<ProcessoRow[]> {
    const first = await this.listar(0, 50);
    const totalPages = first.totalPages ?? 1;
    const lists =
      totalPages <= 1
        ? [first]
        : [
            first,
            ...(await Promise.all(
              Array.from({ length: totalPages - 1 }, (_, i) => this.listar(i + 1, 50)),
            )),
          ];
    return lists.flatMap((p) => p.content ?? []).map(formatRow);
  },

  async filtrar(filtros: ProcessoFiltros): Promise<ProcessoRow[]> {
    const filter = buildFilter(filtros);
    const { data } = await api.get<PagedResponse<Record<string, unknown>>>("domain/processo", {
      params: { fields: "*,contrato.cliente", filter },
    });
    return (data.content ?? []).map(formatRow);
  },

  async porId(id: number): Promise<Record<string, unknown> | undefined> {
    const { data } = await api.get<PagedResponse<Record<string, unknown>>>("domain/processo", {
      params: { fields: "*,contrato.cliente", filter: `id eq ${id}` },
    });
    return data.content?.[0];
  },

  async criar(payload: unknown): Promise<unknown> {
    const { data } = await api.post("domain/processo/add", payload);
    return data;
  },

  async atualizar(id: number, payload: unknown): Promise<unknown> {
    const { data } = await api.patch(`domain/processo/${id}`, payload);
    return data;
  },

  async remover(id: number): Promise<void> {
    await api.delete(`domain/processo/${id}`);
  },

  async gerarCarta(id: number, tipo: "pericia-medica" | "avaliacao-social" | "concessao") {
    const path =
      tipo === "pericia-medica"
        ? `domain/processo/${id}/carta-de-pericia-medica`
        : tipo === "avaliacao-social"
          ? `domain/processo/${id}/carta-de-avaliacao-social`
          : `domain/processo/${id}/carta-de-concessao`;
    return api.patch(path, {}, { responseType: "blob" });
  },
};
