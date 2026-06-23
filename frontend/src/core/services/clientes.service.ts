import { api } from "./http";
import type { ICliente, PagedResponse } from "@/core/types/domain";
import { formatCPF } from "@/core/helpers/format";

function format(clientes: ICliente[]): ICliente[] {
  return clientes.map((cliente) => ({
    ...cliente,
    cpf: cliente.cpf ? formatCPF(cliente.cpf) : cliente.cpf,
  }));
}

export interface ClienteFiltros {
  nome?: string;
  email?: string;
  rg?: string;
  cpf?: string;
}

function buildFilter(filtros: ClienteFiltros): string {
  const parts: string[] = [];
  if (filtros.nome) parts.push(`contato.nome ilike '${filtros.nome}'`);
  if (filtros.email) parts.push(`contato.email ilike '${filtros.email}'`);
  if (filtros.rg) parts.push(`rg ilike '${filtros.rg}'`);
  if (filtros.cpf) parts.push(`cpf ilike '${filtros.cpf}'`);
  return parts.join(" and ");
}

export const clientesService = {
  async listar(page = 0, size = 50): Promise<PagedResponse<ICliente>> {
    const { data } = await api.get<PagedResponse<ICliente>>("domain/cliente", {
      params: { page, size, fields: "*,representante" },
    });
    data.content = format(data.content ?? []);
    return data;
  },

  async listarTodos(): Promise<ICliente[]> {
    const first = await this.listar(0, 50);
    const totalPages = first.totalPages ?? 1;
    if (totalPages <= 1) return first.content;
    const rest = await Promise.all(
      Array.from({ length: totalPages - 1 }, (_, i) => this.listar(i + 1, 50)),
    );
    return [...first.content, ...rest.flatMap((p) => p.content)];
  },

  async filtrar(filtros: ClienteFiltros): Promise<PagedResponse<ICliente>> {
    const filter = buildFilter(filtros);
    const { data } = await api.get<PagedResponse<ICliente>>("domain/cliente", {
      params: { fields: "*,representante", filter },
    });
    data.content = format(data.content ?? []);
    return data;
  },

  async porId(id: number): Promise<ICliente | undefined> {
    const { data } = await api.get<PagedResponse<ICliente>>("domain/cliente", {
      params: { fields: "*,representante", filter: `id eq ${id}` },
    });
    return data.content?.[0];
  },

  async criar(payload: Record<string, unknown>): Promise<ICliente> {
    const { data } = await api.post<ICliente>("domain/cliente/add", payload);
    return data;
  },

  async atualizar(id: number, payload: Record<string, unknown>): Promise<ICliente> {
    const { data } = await api.patch<ICliente>(`domain/cliente/${id}`, payload);
    return data;
  },

  async remover(id: number): Promise<void> {
    await api.delete(`domain/cliente/${id}`);
  },

  async associarBeneficio(payload: unknown): Promise<unknown> {
    const { data } = await api.post("domain/contrato/add", payload);
    return data;
  },
};
