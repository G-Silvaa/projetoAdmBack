import { api, AUTH_USER_KEY, setStoredToken } from "./http";
import type {
  AuthResponse,
  AuthUser,
  LoginPayload,
  RegisterPayload,
} from "@/core/types/auth";

function persistSession(response: AuthResponse) {
  setStoredToken(response.accessToken);
  if (typeof window !== "undefined") {
    window.localStorage.setItem(AUTH_USER_KEY, JSON.stringify(response.user));
  }
}

export const authService = {
  async login(payload: LoginPayload): Promise<AuthResponse> {
    const { data } = await api.post<AuthResponse>("auth/login", payload);
    persistSession(data);
    return data;
  },

  async register(payload: RegisterPayload): Promise<AuthResponse> {
    const { data } = await api.post<AuthResponse>("auth/register", payload);
    persistSession(data);
    return data;
  },

  async me(): Promise<AuthUser> {
    const { data } = await api.get<AuthUser>("auth/me");
    if (typeof window !== "undefined") {
      window.localStorage.setItem(AUTH_USER_KEY, JSON.stringify(data));
    }
    return data;
  },
};
