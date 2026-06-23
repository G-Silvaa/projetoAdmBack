"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import {
  ArrowRight,
  Banknote,
  CheckCircle2,
  ClipboardList,
  FileText,
  HourglassIcon,
  RefreshCw,
  Users,
} from "lucide-react";

import { PageHeader } from "@/components/layout/PageHeader";
import { SectionCard } from "@/components/shared/SectionCard";
import { StatCard } from "@/components/layout/StatCard";
import { DataTable } from "@/components/shared/DataTable";
import { useAuth } from "@/core/auth/AuthProvider";
import { dashboardService } from "@/core/services/dashboard.service";
import { formatCurrencyBR } from "@/core/helpers/format";
import type { DashboardOverview } from "@/core/types/domain";

const STATUS_LABELS: Record<string, string> = {
  AGUARDANDO: "Aguardando",
  PENDENTE: "Pendente",
  ANALISE: "Em análise",
  CUMPRIMENTO_EXIGENCIA: "Exigência",
  ANALISE_ADMINISTRATIVA: "Análise administrativa",
  APROVADO: "Concedido",
  REPROVADO: "Indeferido",
};

const QUICK_LINKS = [
  { label: "Clientes", route: "/clientes", icon: Users },
  { label: "Processos", route: "/processos", icon: ClipboardList },
  { label: "Contratos", route: "/contratos", icon: FileText },
  { label: "Finanças", route: "/financas", icon: Banknote },
];

export default function HomePage() {
  const { canAccessRoute, user, getNivelLabel } = useAuth();
  const [overview, setOverview] = useState<DashboardOverview>();
  const [cessacao, setCessacao] = useState<Record<string, unknown>[]>([]);
  const [concedidos, setConcedidos] = useState<Record<string, unknown>[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;
    Promise.allSettled([
      dashboardService.getOverview(),
      dashboardService.getCessacao(),
      dashboardService.getConcedidos(),
    ])
      .then(([ov, ces, con]) => {
        if (cancelled) return;
        if (ov.status === "fulfilled") setOverview(ov.value);
        if (ces.status === "fulfilled") setCessacao(ces.value.content ?? []);
        if (con.status === "fulfilled") setConcedidos(con.value.content ?? []);
      })
      .finally(() => !cancelled && setIsLoading(false));
    return () => {
      cancelled = true;
    };
  }, []);

  const visibleQuickLinks = QUICK_LINKS.filter((item) => canAccessRoute(item.route));
  const maxStatus = Math.max(...(overview?.statusProcessos?.map((s) => s.total) ?? [1]), 1);

  const priorityItems = overview
    ? [
        {
          label: "Casos em cessação",
          description: "Clientes que pedem contato e revisão imediata.",
          value: cessacao.length,
          route: "/processos",
          icon: HourglassIcon,
        },
        {
          label: "Concedidos recentes",
          description: "Benefícios próximos da janela de 60 dias.",
          value: concedidos.length,
          route: "/processos",
          icon: CheckCircle2,
        },
        {
          label: "Financeiros em aberto",
          description: "Cobranças aguardando baixa ou acompanhamento.",
          value: overview.financeirosEmAberto,
          route: "/financas",
          icon: Banknote,
        },
        {
          label: "Contratos encerrados",
          description: "Vínculos que podem exigir renovação.",
          value: overview.contratosEncerrados,
          route: "/contratos",
          icon: RefreshCw,
        },
      ].filter((item) => canAccessRoute(item.route))
    : [];

  return (
    <div className="page-shell">
      <PageHeader
        eyebrow={user ? `${getNivelLabel(user.nivel)} · ${user.nome.split(" ")[0]}` : "Arctech"}
        title="Painel previdenciário"
        description="Visão consolidada da operação: clientes, processos, contratos e fluxo financeiro num mesmo lugar."
        actions={visibleQuickLinks.map((item) => {
          const Icon = item.icon;
          return (
            <Link
              key={item.route}
              href={item.route}
              className="inline-flex items-center gap-2 rounded-md bg-primary px-3 py-1.5 text-sm font-medium text-primary-foreground hover:bg-primary-hover transition-colors"
            >
              <Icon className="size-4" />
              {item.label}
            </Link>
          );
        })}
      />

      {overview && (
        <div className="stats-grid">
          <StatCard label="Clientes cadastrados" value={overview.totalClientes} meta="Base ativa de pessoas atendidas" />
          <StatCard label="Contratos ativos" value={overview.contratosAtivos} meta={`${overview.contratosEncerrados} encerrados`} />
          <StatCard label="Processos em andamento" value={overview.processosEmAndamento} meta={`${overview.processosConcedidos} concedidos`} />
          <StatCard
            label="Valor da carteira"
            value={formatCurrencyBR(overview.valorCarteira)}
            meta={`${overview.financeirosEmAberto} cobranças em aberto`}
            highlight
          />
        </div>
      )}

      {priorityItems.length > 0 && (
        <SectionCard title="Prioridades de hoje" description="Pontos de atenção da operação para acompanhar de perto.">
          <div className="grid gap-3 md:grid-cols-2">
            {priorityItems.map((item) => {
              const Icon = item.icon;
              return (
                <Link
                  key={item.label}
                  href={item.route}
                  className="group flex items-start gap-3 rounded-2xl border border-border bg-background/60 p-4 transition-colors hover:bg-secondary/10"
                >
                  <div className="grid h-10 w-10 place-items-center rounded-full bg-primary/10 text-primary">
                    <Icon className="size-4" />
                  </div>
                  <div className="flex-1">
                    <p className="text-sm font-semibold text-primary">{item.label}</p>
                    <p className="text-xs text-muted-foreground">{item.description}</p>
                  </div>
                  <div className="text-right">
                    <p className="text-2xl font-bold text-primary leading-none">{item.value}</p>
                    <ArrowRight className="ml-auto mt-2 size-4 text-muted-foreground transition-transform group-hover:translate-x-1" />
                  </div>
                </Link>
              );
            })}
          </div>
        </SectionCard>
      )}

      {overview?.statusProcessos && overview.statusProcessos.length > 0 && (
        <SectionCard
          title="Distribuição de status"
          description="Como os processos estão divididos por fase atual."
        >
          <div className="space-y-3">
            {overview.statusProcessos.map((status) => (
              <div key={status.status}>
                <div className="mb-1 flex items-center justify-between text-sm">
                  <span className="font-medium text-primary">
                    {STATUS_LABELS[status.status] ?? status.status}
                  </span>
                  <span className="text-muted-foreground">{status.total}</span>
                </div>
                <div className="h-2 overflow-hidden rounded-full bg-primary/10">
                  <div
                    className="h-full rounded-full bg-secondary"
                    style={{ width: `${(status.total / maxStatus) * 100}%` }}
                  />
                </div>
              </div>
            ))}
          </div>
        </SectionCard>
      )}

      {cessacao.length > 0 && (
        <SectionCard
          title="Casos próximos da cessação"
          description="Clientes com benefícios prestes a cessar; priorizar contato."
        >
          <DataTable
            data={cessacao as Array<Record<string, unknown>>}
            isLoading={isLoading}
            showSearch={false}
            pageSize={5}
            emptyMessage="Nenhum caso em cessação."
          />
        </SectionCard>
      )}

      {concedidos.length > 0 && (
        <SectionCard
          title="Benefícios concedidos recentemente"
          description="Janela de acompanhamento pós-concessão."
        >
          <DataTable
            data={concedidos as Array<Record<string, unknown>>}
            isLoading={isLoading}
            showSearch={false}
            pageSize={5}
            emptyMessage="Nenhuma concessão recente."
          />
        </SectionCard>
      )}
    </div>
  );
}
