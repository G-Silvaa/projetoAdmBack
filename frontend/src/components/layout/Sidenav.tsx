"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import {
  LayoutGrid,
  Users,
  ClipboardList,
  FileText,
  BookText,
  Banknote,
  BarChart3,
  ShieldCheck,
  CreditCard,
  LogOut,
  PanelLeft,
  X,
} from "lucide-react";
import { useAuth } from "@/core/auth/AuthProvider";
import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { getInitials } from "@/core/helpers/format";

interface NavigationItem {
  label: string;
  route: string;
  icon: typeof LayoutGrid;
}

const NAV_ITEMS: NavigationItem[] = [
  { label: "Painel", route: "/home", icon: LayoutGrid },
  { label: "Clientes", route: "/clientes", icon: Users },
  { label: "Processos", route: "/processos", icon: ClipboardList },
  { label: "Contratos", route: "/contratos", icon: FileText },
  { label: "Modalidades", route: "/modalidades", icon: BookText },
  { label: "Finanças", route: "/financas", icon: Banknote },
  { label: "Relatórios", route: "/relatorios", icon: BarChart3 },
  { label: "Usuários", route: "/usuarios", icon: ShieldCheck },
  { label: "Assinatura", route: "/assinatura", icon: CreditCard },
];

export function Sidenav() {
  const pathname = usePathname();
  const router = useRouter();
  const { user, canAccessRoute, getNivelLabel, logout, isLoading } = useAuth();
  const [mobileOpen, setMobileOpen] = useState(false);

  useEffect(() => {
    if (!isLoading && user && pathname && pathname !== "/home" && !canAccessRoute(pathname)) {
      router.replace("/home");
    }
  }, [pathname, canAccessRoute, isLoading, user, router]);

  const items = NAV_ITEMS.filter((item) => canAccessRoute(item.route));
  const isExactRoute = (route: string) => route === "/home";

  return (
    <>
      <button
        type="button"
        onClick={() => setMobileOpen(true)}
        className="fixed top-3 left-3 z-40 inline-flex items-center justify-center rounded-md border border-border bg-card p-2 text-text shadow-sm lg:hidden"
        aria-label="Abrir menu"
      >
        <PanelLeft className="size-4" />
      </button>

      {mobileOpen && (
        <div
          className="fixed inset-0 z-40 bg-black/30 backdrop-blur-sm lg:hidden"
          onClick={() => setMobileOpen(false)}
        />
      )}

      <aside
        className={cn(
          "fixed inset-y-0 left-0 z-50 flex w-64 flex-col gap-4 border-r border-primary-strong bg-primary text-white px-3 py-5 transition-transform lg:translate-x-0",
          mobileOpen ? "translate-x-0" : "-translate-x-full lg:translate-x-0",
        )}
      >
        <div className="flex items-center justify-between px-2">
          <div className="flex items-center gap-2">
            <div className="grid h-8 w-8 place-items-center rounded-md bg-white/15 text-white text-sm font-semibold">
              A
            </div>
            <div>
              <p className="text-sm font-semibold text-white leading-none">Arctech</p>
              <p className="text-[0.7rem] text-white/60">Painel</p>
            </div>
          </div>
          <button
            type="button"
            onClick={() => setMobileOpen(false)}
            className="rounded-md p-1 text-white/70 hover:text-white lg:hidden"
            aria-label="Fechar menu"
          >
            <X className="size-4" />
          </button>
        </div>

        <nav className="flex-1 space-y-0.5 overflow-y-auto px-1">
          {items.map((item) => {
            const Icon = item.icon;
            const isActive = isExactRoute(item.route)
              ? pathname === item.route
              : pathname?.startsWith(item.route);
            return (
              <Link
                key={item.route}
                href={item.route}
                onClick={() => setMobileOpen(false)}
                className={cn(
                  "flex items-center gap-2.5 rounded-md px-2.5 py-2 text-sm transition-colors",
                  isActive
                    ? "bg-white/15 text-white font-medium"
                    : "text-white/70 hover:bg-white/10 hover:text-white",
                )}
              >
                <Icon className="size-4 shrink-0" />
                <span>{item.label}</span>
              </Link>
            );
          })}
        </nav>

        {user && (
          <div className="border-t border-white/15 pt-3 px-1">
            <div className="flex items-center gap-2 rounded-md px-2 py-1.5">
              <div className="grid h-8 w-8 place-items-center rounded-full bg-white/15 text-white text-xs font-semibold">
                {getInitials(user.nome)}
              </div>
              <div className="min-w-0 flex-1">
                <p className="truncate text-xs font-medium text-white">{user.nome}</p>
                <p className="text-[0.7rem] text-white/60">
                  {getNivelLabel(user.nivel)}
                </p>
              </div>
              <button
                type="button"
                onClick={() => {
                  setMobileOpen(false);
                  logout();
                }}
                className="rounded-md p-1.5 text-white/70 hover:text-white hover:bg-white/10"
                aria-label="Sair"
                title="Sair"
              >
                <LogOut className="size-4" />
              </button>
            </div>
          </div>
        )}
      </aside>
    </>
  );
}
