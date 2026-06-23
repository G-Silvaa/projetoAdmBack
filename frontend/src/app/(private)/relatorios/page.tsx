"use client";

import { useEffect, useMemo, useState } from "react";
import {
  CalendarRange,
  CheckCircle2,
  Clock,
  Download,
  FileBarChart,
  Loader2,
  Stethoscope,
} from "lucide-react";
import { toast } from "sonner";

import { PageHeader } from "@/components/layout/PageHeader";
import { SectionCard } from "@/components/shared/SectionCard";
import { StatCard } from "@/components/layout/StatCard";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";

import { relatoriosService } from "@/core/services/relatorios.service";
import { triggerBlobDownload } from "@/core/services/financas.service";
import {
  MESES,
  mesLabel,
  type MesValue,
  type RelatorioIntervaloTipo,
  type RelatorioMensalTipo,
  type RelatorioMes,
} from "@/core/types/relatorio";

const RELATORIOS_MENSAIS: {
  tipo: RelatorioMensalTipo;
  label: string;
  description: string;
  icon: typeof FileBarChart;
}[] = [
  {
    tipo: "relacao-contratos-do-mes",
    label: "Relação de contratos",
    description: "Lista de todos os contratos fechados no mês.",
    icon: FileBarChart,
  },
  {
    tipo: "concessoes-do-mes",
    label: "Concessões",
    description: "Benefícios concedidos no mês (status APROVADO).",
    icon: CheckCircle2,
  },
  {
    tipo: "pericia-avaliacao-social-do-mes",
    label: "Perícia e avaliação social",
    description: "Processos com perícia médica ou avaliação social agendadas no mês.",
    icon: Stethoscope,
  },
];

const RELATORIOS_INTERVALO: {
  tipo: RelatorioIntervaloTipo;
  label: string;
  description: string;
  icon: typeof FileBarChart;
}[] = [
  {
    tipo: "relacao-contratos",
    label: "Relação de contratos",
    description: "Contratos fechados no período selecionado.",
    icon: FileBarChart,
  },
  {
    tipo: "concessoes",
    label: "Concessões",
    description: "Benefícios concedidos no período.",
    icon: CheckCircle2,
  },
  {
    tipo: "pericia-avaliacao-social",
    label: "Perícia e avaliação social",
    description: "Agendamentos no período.",
    icon: Stethoscope,
  },
];

function generateYears(): number[] {
  const now = new Date().getFullYear();
  const out: number[] = [];
  for (let y = now; y >= 2000; y--) out.push(y);
  return out;
}

export default function RelatoriosPage() {
  const [meses, setMeses] = useState<RelatorioMes[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [busyMonthId, setBusyMonthId] = useState<string | null>(null);
  const [intervaloDialog, setIntervaloDialog] = useState<RelatorioIntervaloTipo | null>(null);
  const [intervaloBusy, setIntervaloBusy] = useState(false);
  const currentYear = new Date().getFullYear();
  const [intervalo, setIntervalo] = useState<{
    inicioAno: number;
    inicioMes: MesValue;
    terminoAno: number;
    terminoMes: MesValue;
  }>({
    inicioAno: currentYear,
    inicioMes: "JANEIRO",
    terminoAno: currentYear,
    terminoMes: "DEZEMBRO",
  });
  const years = useMemo(generateYears, []);

  useEffect(() => {
    let cancelled = false;
    relatoriosService
      .recentes(12)
      .then((data) => !cancelled && setMeses(data))
      .catch(() => !cancelled && toast.error("Não foi possível carregar os relatórios recentes."))
      .finally(() => !cancelled && setIsLoading(false));
    return () => {
      cancelled = true;
    };
  }, []);

  const stats = useMemo(() => {
    if (!meses.length) {
      return { contratos: 0, concedidos: 0, aguardando: 0, entrada: 0 };
    }
    return meses.reduce(
      (acc, m) => ({
        contratos: acc.contratos + (m.totalContratos ?? 0),
        concedidos: acc.concedidos + (m.totalBeneficiosConcedidos ?? 0),
        aguardando: acc.aguardando + (m.totalBeneficiosAguardando ?? 0),
        entrada: acc.entrada + (m.dadoEntrada ?? 0),
      }),
      { contratos: 0, concedidos: 0, aguardando: 0, entrada: 0 },
    );
  }, [meses]);

  const baixarMensal = async (mes: RelatorioMes, tipo: RelatorioMensalTipo) => {
    const busyKey = `${mes.id}-${tipo}`;
    setBusyMonthId(busyKey);
    try {
      const response = await relatoriosService.baixarMensal(mes.id, tipo);
      triggerBlobDownload(
        response as { data: Blob; headers: Record<string, string> },
        `${tipo}-${mesLabel(mes.mes).toLowerCase()}-${mes.ano}.pdf`,
      );
    } catch {
      toast.error("Erro ao gerar o relatório mensal.");
    } finally {
      setBusyMonthId(null);
    }
  };

  const baixarIntervalo = async () => {
    if (!intervaloDialog) return;
    setIntervaloBusy(true);
    try {
      const response = await relatoriosService.baixarIntervalo(intervaloDialog, {
        domain: null,
        args: {
          intervalo: {
            inicio: { ano: intervalo.inicioAno, mes: intervalo.inicioMes },
            termino: { ano: intervalo.terminoAno, mes: intervalo.terminoMes },
          },
        },
      });
      triggerBlobDownload(
        response as { data: Blob; headers: Record<string, string> },
        `${intervaloDialog}-${mesLabel(intervalo.inicioMes).toLowerCase()}-${intervalo.inicioAno}-ate-${mesLabel(intervalo.terminoMes).toLowerCase()}-${intervalo.terminoAno}.pdf`,
      );
      setIntervaloDialog(null);
    } catch {
      toast.error("Erro ao gerar o relatório do intervalo.");
    } finally {
      setIntervaloBusy(false);
    }
  };

  return (
    <div className="page-shell">
      <PageHeader
        eyebrow="Inteligência operacional"
        title="Relatórios"
        description="PDFs operacionais e de prestação de contas. Acumulado dos últimos 12 meses e geração por período."
      />

      <div className="stats-grid">
        <StatCard
          label="Contratos no período"
          value={stats.contratos}
          meta="Soma dos últimos meses listados"
        />
        <StatCard label="Concedidos" value={stats.concedidos} highlight />
        <StatCard label="Aguardando" value={stats.aguardando} />
        <StatCard label="Dado entrada" value={stats.entrada} />
      </div>

      <SectionCard
        title="Relatórios por intervalo"
        description="Gere um PDF consolidado entre dois meses; útil para fechamentos trimestrais ou semestrais."
      >
        <div className="grid gap-3 md:grid-cols-3">
          {RELATORIOS_INTERVALO.map((r) => {
            const Icon = r.icon;
            return (
              <button
                key={r.tipo}
                type="button"
                onClick={() => setIntervaloDialog(r.tipo)}
                className="group flex flex-col items-start gap-2 rounded-2xl border border-border bg-background/60 p-4 text-left transition-colors hover:bg-secondary/10"
              >
                <div className="flex items-center gap-2">
                  <div className="grid h-9 w-9 place-items-center rounded-full bg-primary/10 text-primary">
                    <Icon className="size-4" />
                  </div>
                  <span className="font-semibold text-primary">{r.label}</span>
                </div>
                <p className="text-xs text-muted-foreground">{r.description}</p>
                <span className="mt-auto inline-flex items-center gap-2 text-xs font-semibold text-secondary group-hover:underline">
                  <CalendarRange className="size-3.5" />
                  Selecionar intervalo
                </span>
              </button>
            );
          })}
        </div>
      </SectionCard>

      <SectionCard
        title="Relatórios mensais"
        description="Cada mês listado já tem totais consolidados do backend; baixe o PDF para a fase desejada."
      >
        {isLoading ? (
          <div className="grid place-items-center py-10">
            <Loader2 className="size-5 animate-spin text-primary" />
          </div>
        ) : meses.length === 0 ? (
          <div className="flex flex-col items-center gap-2 py-8 text-center text-sm text-muted-foreground">
            <Clock className="size-8" />
            Nenhum mês consolidado ainda.
          </div>
        ) : (
          <ul className="space-y-3">
            {meses.map((mes) => (
              <li
                key={mes.id}
                className="rounded-2xl border border-border bg-background/60 p-4"
              >
                <div className="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
                  <div>
                    <p className="font-display text-2xl text-primary leading-none">
                      {mesLabel(mes.mes)} <span className="text-secondary">/ {mes.ano}</span>
                    </p>
                    <p className="mt-1 flex flex-wrap gap-x-4 gap-y-1 text-xs text-muted-foreground">
                      <span>Contratos: <b className="text-primary">{mes.totalContratos ?? 0}</b></span>
                      <span>Concedidos: <b className="text-primary">{mes.totalBeneficiosConcedidos ?? 0}</b></span>
                      <span>Aguardando: <b className="text-primary">{mes.totalBeneficiosAguardando ?? 0}</b></span>
                      <span>Entrada: <b className="text-primary">{mes.dadoEntrada ?? 0}</b></span>
                    </p>
                  </div>
                  <div className="flex flex-wrap gap-2">
                    {RELATORIOS_MENSAIS.map((r) => {
                      const Icon = r.icon;
                      const busy = busyMonthId === `${mes.id}-${r.tipo}`;
                      return (
                        <Button
                          key={r.tipo}
                          size="sm"
                          variant="outline"
                          disabled={busy}
                          onClick={() => baixarMensal(mes, r.tipo)}
                          title={r.description}
                        >
                          {busy ? (
                            <Loader2 className="size-4 animate-spin" />
                          ) : (
                            <Icon className="size-4" />
                          )}
                          {r.label}
                          <Download className="size-3 opacity-70" />
                        </Button>
                      );
                    })}
                  </div>
                </div>
              </li>
            ))}
          </ul>
        )}
      </SectionCard>

      <Dialog
        open={intervaloDialog !== null}
        onOpenChange={(o) => !o && !intervaloBusy && setIntervaloDialog(null)}
      >
        <DialogContent>
          <DialogHeader>
            <DialogTitle className="font-display text-2xl text-primary">
              Selecione o intervalo
            </DialogTitle>
            <DialogDescription>
              {intervaloDialog &&
                RELATORIOS_INTERVALO.find((r) => r.tipo === intervaloDialog)?.description}
            </DialogDescription>
          </DialogHeader>

          <div className="space-y-4">
            <div className="grid gap-3 sm:grid-cols-2">
              <div className="space-y-1.5">
                <Label>Ano início</Label>
                <Select
                  value={String(intervalo.inicioAno)}
                  onValueChange={(v) =>
                    setIntervalo((s) => ({ ...s, inicioAno: Number(v) }))
                  }
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {years.map((y) => (
                      <SelectItem key={y} value={String(y)}>
                        {y}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className="space-y-1.5">
                <Label>Mês início</Label>
                <Select
                  value={intervalo.inicioMes}
                  onValueChange={(v) =>
                    setIntervalo((s) => ({ ...s, inicioMes: v as MesValue }))
                  }
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {MESES.map((m) => (
                      <SelectItem key={m.value} value={m.value}>
                        {m.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>

            <div className="grid gap-3 sm:grid-cols-2">
              <div className="space-y-1.5">
                <Label>Ano término</Label>
                <Select
                  value={String(intervalo.terminoAno)}
                  onValueChange={(v) =>
                    setIntervalo((s) => ({ ...s, terminoAno: Number(v) }))
                  }
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {years.map((y) => (
                      <SelectItem key={y} value={String(y)}>
                        {y}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className="space-y-1.5">
                <Label>Mês término</Label>
                <Select
                  value={intervalo.terminoMes}
                  onValueChange={(v) =>
                    setIntervalo((s) => ({ ...s, terminoMes: v as MesValue }))
                  }
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {MESES.map((m) => (
                      <SelectItem key={m.value} value={m.value}>
                        {m.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>
          </div>

          <DialogFooter>
            <Button
              variant="outline"
              onClick={() => setIntervaloDialog(null)}
              disabled={intervaloBusy}
            >
              Cancelar
            </Button>
            <Button onClick={baixarIntervalo} disabled={intervaloBusy}>
              {intervaloBusy ? <Loader2 className="size-4 animate-spin" /> : <Download className="size-4" />}
              Baixar PDF
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
