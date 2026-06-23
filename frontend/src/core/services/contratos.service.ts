import { api } from "./http";
import type { IContrato, PagedResponse } from "@/core/types/domain";

export interface ContratoFiltros {
  nome?: string;
  cpf?: string;
  beneficio?: string;
}

function buildFilter(filtros: ContratoFiltros): string {
  const parts: string[] = [];
  if (filtros.nome) parts.push(`cliente.contato.nome ilike '${filtros.nome}'`);
  if (filtros.cpf) parts.push(`cliente.cpf ilike '${filtros.cpf}'`);
  if (filtros.beneficio) parts.push(`beneficio eq '${filtros.beneficio}'`);
  return parts.join(" and ");
}

export const contratosService = {
  async listar(): Promise<IContrato[]> {
    const { data } = await api.get<PagedResponse<IContrato>>("domain/contrato", {
      params: { fields: "*,cliente" },
    });
    return data.content ?? [];
  },

  async filtrar(filtros: ContratoFiltros): Promise<IContrato[]> {
    const filter = buildFilter(filtros);
    const { data } = await api.get<PagedResponse<IContrato>>("domain/contrato", {
      params: { fields: "*,cliente", filter },
    });
    return data.content ?? [];
  },

  async atualizar(id: number, payload: Partial<IContrato>): Promise<IContrato> {
    const { data } = await api.patch<IContrato>(`domain/contrato/${id}`, payload);
    return data;
  },

  async remover(id: number): Promise<void> {
    await api.delete(`domain/contrato/${id}`);
  },

  async renovar(id: number): Promise<void> {
    await api.patch(`domain/contrato/${id}/renovar`, {});
  },

  async gerarPdf(id: number) {
    return api.patch(`domain/contrato/${id}/gerar-contrato`, {}, { responseType: "blob" });
  },
};
