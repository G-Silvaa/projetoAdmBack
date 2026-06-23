import { api } from "./http";
import type { Assinatura, Cobranca, PlanoApi } from "@/core/types/auth";

export const assinaturaService = {
  /** Assinatura da empresa logada (plano, status, trial, vencimento). */
  async minha(): Promise<Assinatura> {
    const { data } = await api.get<Assinatura>("assinatura");
    return data;
  },

  /** Lista pública de planos (endpoint aberto). */
  async planos(): Promise<PlanoApi[]> {
    const { data } = await api.get<PlanoApi[]>("planos");
    return data;
  },

  /** Gera uma cobrança PIX para o plano atual da empresa. */
  async criarPagamento(): Promise<Cobranca> {
    const { data } = await api.post<Cobranca>("assinatura/pagamento");
    return data;
  },

  /** Consulta o status de uma cobrança (polling). */
  async consultarPagamento(id: number): Promise<Cobranca> {
    const { data } = await api.get<Cobranca>(`assinatura/pagamento/${id}`);
    return data;
  },

  /** Simula a confirmação do pagamento (sandbox). */
  async simularPagamento(id: number): Promise<Cobranca> {
    const { data } = await api.post<Cobranca>(`assinatura/pagamento/${id}/simular`);
    return data;
  },
};
