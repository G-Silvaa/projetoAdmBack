"use client";

import { useEffect, useMemo, useState } from "react";
import { FileDown, Pencil, RefreshCw } from "lucide-react";
import { toast } from "sonner";

import { PageHeader } from "@/components/layout/PageHeader";
import { SectionCard } from "@/components/shared/SectionCard";
import { StatCard } from "@/components/layout/StatCard";
import { DataTable } from "@/components/shared/DataTable";
import { MaskedInput } from "@/components/shared/MaskedInput";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";

import { useAuth } from "@/core/auth/AuthProvider";
import { processosService, type ProcessoRow } from "@/core/services/processos.service";
import { triggerBlobDownload } from "@/core/services/financas.service";
import { statusProcessoOptions } from "@/core/consts/beneficios";
import { unformatCPF } from "@/core/helpers/format";
import { ProcessoFormDialog } from "./ProcessoFormDialog";

export default function ProcessosPage() {
  const { hasCapability } = useAuth();
  const canManage = hasCapability("manageProcessos");
  const canIssueLetters = hasCapability("issueProcessLetters");

  const [data, setData] = useState<ProcessoRow[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [filtros, setFiltros] = useState({ Nome: "", CPF: "", Status: "" });
  const [editingId, setEditingId] = useState<number | undefined>();
  const [dialogOpen, setDialogOpen] = useState(false);

  const load = async () => {
    setIsLoading(true);
    try {
      setData(await processosService.listarTodos());
    } catch {
      toast.error("Não foi possível carregar os processos.");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const stats = useMemo(() => {
    const aprovados = data.filter((d) => d.Status === "Aprovado").length;
    const ativos = data.filter((d) => !["Aprovado", "Reprovado"].includes(d.Status)).length;
    const cessacoes = data.filter((d) => !!d["Cessação"]).length;
    return { total: data.length, aprovados, ativos, cessacoes };
  }, [data]);

  const aplicarFiltros = async () => {
    if (!filtros.Nome && !filtros.CPF && !filtros.Status) {
      load();
      return;
    }
    setIsLoading(true);
    try {
      const rows = await processosService.filtrar({
        Nome: filtros.Nome || undefined,
        CPF: unformatCPF(filtros.CPF) || undefined,
        Status: filtros.Status || undefined,
      });
      setData(rows);
    } catch {
      toast.error("Não foi possível aplicar os filtros.");
    } finally {
      setIsLoading(false);
    }
  };

  const limparFiltros = () => {
    setFiltros({ Nome: "", CPF: "", Status: "" });
    load();
  };

  const gerarCarta = async (
    id: number,
    tipo: "pericia-medica" | "avaliacao-social" | "concessao",
  ) => {
    try {
      const response = await processosService.gerarCarta(id, tipo);
      triggerBlobDownload(response as { data: Blob; headers: Record<string, string> });
    } catch {
      toast.error("Erro ao gerar a carta.");
    }
  };

  return (
    <div className="page-shell">
      <PageHeader
        eyebrow="Operação"
        title="Processos"
        description={
          canManage && canIssueLetters
            ? "Edição rápida e geração de cartas por processo."
            : canManage
              ? "Edição rápida dos processos em andamento."
              : "Consulta dos processos em modo leitura, sem ações operacionais."
        }
      />

      <div className="stats-grid">
        <StatCard label="Total de processos" value={stats.total} />
        <StatCard label="Em acompanhamento" value={stats.ativos} meta="Excluindo aprovados e reprovados" />
        <StatCard label="Aprovados" value={stats.aprovados} />
        <StatCard label="Com data de cessação" value={stats.cessacoes} highlight />
      </div>

      <SectionCard title="Filtros" description="Busque por nome, CPF ou status do processo.">
        <div className="filter-grid">
          <div className="space-y-1.5">
            <Label htmlFor="f-nome">Nome</Label>
            <Input
              id="f-nome"
              placeholder="Nome do cliente"
              value={filtros.Nome}
              onChange={(e) => setFiltros((f) => ({ ...f, Nome: e.target.value }))}
            />
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="f-cpf">CPF</Label>
            <MaskedInput
              id="f-cpf"
              mask="cpf"
              placeholder="000.000.000-00"
              value={filtros.CPF}
              onChange={(v) => setFiltros((f) => ({ ...f, CPF: v }))}
            />
          </div>
          <div className="space-y-1.5">
            <Label>Status</Label>
            <Select
              value={filtros.Status}
              onValueChange={(v) => setFiltros((f) => ({ ...f, Status: v }))}
            >
              <SelectTrigger>
                <SelectValue placeholder="Todos" />
              </SelectTrigger>
              <SelectContent>
                {statusProcessoOptions.map((o) => (
                  <SelectItem key={o.value} value={o.value}>
                    {o.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
        </div>
        <div className="mt-4 flex justify-end gap-2">
          <Button variant="outline" onClick={limparFiltros}>
            Limpar
          </Button>
          <Button onClick={aplicarFiltros}>Aplicar filtros</Button>
        </div>
      </SectionCard>

      <SectionCard
        title="Processos da operação"
        description={
          canManage
            ? "Editar dados, atualizar status e emitir cartas direto da tabela."
            : "Consulta dos processos em modo leitura."
        }
      >
        <DataTable
          data={data as unknown as Array<Record<string, unknown>>}
          excludeColumns={["id"]}
          isLoading={isLoading}
          searchPlaceholder="Filtro rápido (nome, CPF...)"
          rowActions={(row) => {
            const id = Number(row.id);
            return (
              <>
                {canManage && (
                  <Button
                    size="sm"
                    variant="ghost"
                    onClick={() => {
                      setEditingId(id);
                      setDialogOpen(true);
                    }}
                  >
                    <Pencil className="size-4" />
                  </Button>
                )}
                {canIssueLetters && (
                  <DropdownMenu>
                    <DropdownMenuTrigger asChild>
                      <Button size="sm" variant="ghost">
                        <FileDown className="size-4" />
                      </Button>
                    </DropdownMenuTrigger>
                    <DropdownMenuContent align="end">
                      <DropdownMenuItem onClick={() => gerarCarta(id, "pericia-medica")}>
                        Carta de perícia médica
                      </DropdownMenuItem>
                      <DropdownMenuItem onClick={() => gerarCarta(id, "avaliacao-social")}>
                        Carta de avaliação social
                      </DropdownMenuItem>
                      <DropdownMenuItem onClick={() => gerarCarta(id, "concessao")}>
                        Carta de concessão
                      </DropdownMenuItem>
                    </DropdownMenuContent>
                  </DropdownMenu>
                )}
              </>
            );
          }}
        />
        <div className="mt-3 flex justify-end">
          <Button variant="outline" size="sm" onClick={load}>
            <RefreshCw className="size-4" />
            Atualizar lista
          </Button>
        </div>
      </SectionCard>

      <ProcessoFormDialog
        open={dialogOpen}
        processoId={editingId}
        onOpenChange={setDialogOpen}
        onSaved={load}
      />
    </div>
  );
}
