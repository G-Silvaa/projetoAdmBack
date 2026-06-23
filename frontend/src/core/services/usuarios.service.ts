import { api } from "./http";
import type {
  AuthUser,
  NivelUsuarioOption,
  UsuarioCreatePayload,
  UsuarioUpdatePayload,
} from "@/core/types/auth";

export const usuariosService = {
  async listar(): Promise<AuthUser[]> {
    const { data } = await api.get<AuthUser[]>("usuarios");
    return data;
  },
  async listarNiveis(): Promise<NivelUsuarioOption[]> {
    const { data } = await api.get<NivelUsuarioOption[]>("usuarios/niveis");
    return data;
  },
  async criar(payload: UsuarioCreatePayload): Promise<AuthUser> {
    const { data } = await api.post<AuthUser>("usuarios", payload);
    return data;
  },
  async atualizar(id: number, payload: UsuarioUpdatePayload): Promise<AuthUser> {
    const { data } = await api.patch<AuthUser>(`usuarios/${id}`, payload);
    return data;
  },
};
