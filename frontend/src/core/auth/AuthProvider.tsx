"use client";

import { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import { authService } from "@/core/services/auth.service";
import {
  AUTH_USER_KEY,
  clearStoredAuth,
  getStoredToken,
} from "@/core/services/http";
import {
  CAPABILITY_ACCESS,
  NIVEL_LABELS,
  ROUTE_ACCESS,
  type AuthUser,
  type Capability,
  type NivelUsuario,
} from "@/core/types/auth";

interface AuthContextValue {
  user: AuthUser | null;
  isLoading: boolean;
  isAuthenticated: boolean;
  logout: () => void;
  refresh: () => Promise<void>;
  hasCapability: (capability: Capability) => boolean;
  canAccessRoute: (route: string) => boolean;
  getNivelLabel: (nivel?: NivelUsuario) => string;
}

const AuthContext = createContext<AuthContextValue | null>(null);

function readStoredUser(): AuthUser | null {
  if (typeof window === "undefined") return null;
  const raw = window.localStorage.getItem(AUTH_USER_KEY);
  if (!raw) return null;
  try {
    return JSON.parse(raw) as AuthUser;
  } catch {
    return null;
  }
}

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const router = useRouter();
  const [user, setUser] = useState<AuthUser | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  const refresh = useCallback(async () => {
    if (!getStoredToken()) {
      setUser(null);
      setIsLoading(false);
      return;
    }
    try {
      const me = await authService.me();
      setUser(me);
    } catch {
      clearStoredAuth();
      setUser(null);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    setUser(readStoredUser());
    refresh();
  }, [refresh]);

  const logout = useCallback(() => {
    clearStoredAuth();
    setUser(null);
    router.replace("/");
  }, [router]);

  const hasCapability = useCallback(
    (capability: Capability) => {
      const nivel = user?.nivel;
      if (!nivel) return false;
      return (CAPABILITY_ACCESS[capability] as readonly NivelUsuario[]).includes(nivel);
    },
    [user],
  );

  const canAccessRoute = useCallback(
    (route: string) => {
      const nivel = user?.nivel;
      if (!nivel) return false;
      const normalized = route.startsWith("/") ? route : `/${route}`;
      return ROUTE_ACCESS[nivel]?.includes(normalized) ?? false;
    },
    [user],
  );

  const getNivelLabel = useCallback(
    (nivel?: NivelUsuario) => (nivel ? (NIVEL_LABELS[nivel] ?? nivel) : ""),
    [],
  );

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      isLoading,
      isAuthenticated: Boolean(user),
      logout,
      refresh,
      hasCapability,
      canAccessRoute,
      getNivelLabel,
    }),
    [user, isLoading, logout, refresh, hasCapability, canAccessRoute, getNivelLabel],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
}
