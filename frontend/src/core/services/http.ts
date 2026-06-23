import axios, { AxiosInstance, InternalAxiosRequestConfig } from "axios";
import { site } from "@/core/config/site";
import { ACCESS_TOKEN_KEY, AUTH_USER_KEY } from "@/core/auth/storage-keys";

export { ACCESS_TOKEN_KEY, AUTH_USER_KEY };

const PUBLIC_ENDPOINTS = ["/auth/login", "/auth/register"];

function isBrowser() {
  return typeof window !== "undefined";
}

export function getStoredToken(): string | null {
  if (!isBrowser()) return null;
  return window.localStorage.getItem(ACCESS_TOKEN_KEY);
}

export function setStoredToken(token: string) {
  if (!isBrowser()) return;
  window.localStorage.setItem(ACCESS_TOKEN_KEY, token);
  document.cookie = `${ACCESS_TOKEN_KEY}=${token}; path=/; max-age=${60 * 60 * 24 * 7}; SameSite=Lax`;
}

export function clearStoredAuth() {
  if (!isBrowser()) return;
  window.localStorage.removeItem(ACCESS_TOKEN_KEY);
  window.localStorage.removeItem(AUTH_USER_KEY);
  document.cookie = `${ACCESS_TOKEN_KEY}=; path=/; max-age=0; SameSite=Lax`;
}

function shouldSkipAuth(url?: string) {
  if (!url) return false;
  return PUBLIC_ENDPOINTS.some((endpoint) => url.includes(endpoint));
}

export const api: AxiosInstance = axios.create({
  baseURL: site.apiUrl,
  headers: {
    "Content-Type": "application/json",
    "ngrok-skip-browser-warning": "1",
  },
});

api.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = getStoredToken();
  if (token && !shouldSkipAuth(config.url)) {
    config.headers = config.headers ?? {};
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error?.response?.status;
    const url: string | undefined = error?.config?.url;
    if ((status === 401 || status === 403) && !shouldSkipAuth(url) && isBrowser()) {
      clearStoredAuth();
      if (!window.location.pathname.startsWith("/login")) {
        const redirect = encodeURIComponent(window.location.pathname);
        window.location.href = `/login?redirectTo=${redirect}`;
      }
    }
    // 402 = assinatura inativa/vencida: leva para a tela de assinatura (gate).
    if (status === 402 && isBrowser() && !window.location.pathname.startsWith("/assinatura")) {
      window.location.href = "/assinatura";
    }
    return Promise.reject(error);
  },
);
