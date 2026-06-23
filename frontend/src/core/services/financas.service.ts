import { api } from "./http";
import type { IFinanceiro, PagedResponse } from "@/core/types/domain";

export interface FinanceiroFiltros {
  nome?: string;
  cpf?: string;
  beneficio?: string;
  situacaoParcela?: string;
  situacaoPagamento?: string;
}

const FIELDS = "*,contrato.cliente.contato.nome,contrato.cliente.cpf,contrato.beneficio";

function buildFilter(filtros: FinanceiroFiltros): string {
  const parts: string[] = [];
  if (filtros.nome) parts.push(`contrato.cliente.contato.nome ilike '${filtros.nome}'`);
  if (filtros.cpf) parts.push(`contrato.cliente.cpf ilike '${filtros.cpf}'`);
  if (filtros.beneficio) parts.push(`contrato.beneficio eq '${filtros.beneficio}'`);
  if (filtros.situacaoParcela) parts.push(`situacaoParcela eq '${filtros.situacaoParcela}'`);
  if (filtros.situacaoPagamento !== undefined && filtros.situacaoPagamento !== "")
    parts.push(`situacaoPagamento eq ${filtros.situacaoPagamento}`);
  return parts.join(" and ");
}

export const financasService = {
  async listar(): Promise<IFinanceiro[]> {
    const { data } = await api.get<PagedResponse<IFinanceiro>>("domain/financeiro", {
      params: { fields: FIELDS },
    });
    return data.content ?? [];
  },

  async filtrar(filtros: FinanceiroFiltros): Promise<IFinanceiro[]> {
    const filter = buildFilter(filtros);
    const { data } = await api.get<PagedResponse<IFinanceiro>>("domain/financeiro", {
      params: { fields: FIELDS, filter },
    });
    return data.content ?? [];
  },

  async atualizar(id: number, payload: Partial<IFinanceiro>): Promise<IFinanceiro> {
    const { data } = await api.patch<IFinanceiro>(`domain/financeiro/${id}`, payload);
    return data;
  },

  async gerarBoleto(id: number) {
    return api.patch(`domain/financeiro/${id}/boleto`, {}, { responseType: "blob" });
  },

  async baixarComprovante(id: number) {
    return api.patch(`domain/financeiro/${id}/comprovante`, {}, { responseType: "blob" });
  },
};

export function triggerBlobDownload(response: { data: Blob; headers: Record<string, string> }, fallback = "download.pdf") {
  const contentDisposition = response.headers["content-disposition"] ?? "";
  const match = contentDisposition.match(/filename=([^;]+)/);
  const filename = match ? match[1].replace(/['"]/g, "").trim() : fallback;
  const blob = new Blob([response.data], { type: "application/pdf" });
  const url = window.URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  window.URL.revokeObjectURL(url);
}
