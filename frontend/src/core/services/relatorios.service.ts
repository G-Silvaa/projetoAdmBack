import { api } from "./http";
import type {
  IRelatorioIntervalo,
  RelatorioIntervaloTipo,
  RelatorioMensalTipo,
  RelatorioMes,
} from "@/core/types/relatorio";
import type { PagedResponse } from "@/core/types/domain";

export const relatoriosService = {
  async recentes(size = 12): Promise<RelatorioMes[]> {
    const { data } = await api.get<PagedResponse<RelatorioMes>>("domain/relatorio/query/recentes", {
      params: { size },
    });
    return data.content ?? [];
  },

  async baixarMensal(id: number, tipo: RelatorioMensalTipo) {
    return api.patch(`domain/relatorio/${id}/${tipo}`, {}, { responseType: "blob" });
  },

  async baixarIntervalo(tipo: RelatorioIntervaloTipo, payload: IRelatorioIntervalo) {
    return api.post(`domain/service/relatorio-service/${tipo}`, payload, {
      responseType: "blob",
    });
  },
};
