"use client";

import { useEffect, useMemo, useState } from "react";
import { Download, Pencil, Plus, RefreshCw, Trash2 } from "lucide-react";
import { toast } from "sonner";

import { PageHeader } from "@/components/layout/PageHeader";
import { SectionCard } from "@/components/shared/SectionCard";
import { StatCard } from "@/components/layout/StatCard";
import { DataTable } from "@/components/shared/DataTable";
import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { MaskedInput } from "@/components/shared/MaskedInput";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";

import { useAuth } from "@/core/auth/AuthProvider";
import { contratosService } from "@/core/services/contratos.service";
import { triggerBlobDownload } from "@/core/services/financas.service";
import { beneficiosOptions } from "@/core/consts/beneficios";
import {
  formatCPF,
  formatCurrencyBR,
  formatDateBR,
  unformatCPF,
} from "@/core/helpers/format";
import type { IContrato } from "@/core/types/domain";
import { ContratoFormDialog, type ContratoRow as DialogContratoRow } from "./ContratoFormDialog";

interface ContratoRow extends Record<string, unknown> {
  id: number;
  Id: number;
  __ativo: boolean;
  __valor: number;
  Nome: string;
  CPF: string;
  Benefício: string;
  Número: string;
  Início: string;
  Conclusão: string;
  Valor: string;
}

function toRow(item: IContrato): ContratoRow {
  return {
    id: item.id,
    Id: item.id,
    __ativo: !item.conclusao,
    __valor: Number(item.valor ?? 0),
    Nome: item.cliente?.contato?.nome ?? "",
    CPF: formatCPF(item.cliente?.cpf ?? ""),
    Benefício: item.beneficio,
    Número: item.numero,
    Início: formatDateBR(item.inicio),
    Conclusão: item.conclusao ? formatDateBR(item.conclusao) : "",
    Valor: formatCurrencyBR(item.valor ?? 0),
  };
}

export default function ContratosPage() {
  const { hasCapability } = useAuth();
  const canManage = hasCapability("manageContratos");
  const canRenew = hasCapability("renewContratos");
  const canDownload = hasCapability("downloadContratos");

  const [rows, setRows] = useState<ContratoRow[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [filtros, setFiltros] = useState({ nome: "", cpf: "", beneficio: "" });
  const [confirmDelete, setConfirmDelete] = useState<number | null>(null);
  const [confirmRenew, setConfirmRenew] = useState<number | null>(null);
  const [confirmLoading, setConfirmLoading] = useState(false);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [dialogMode, setDialogMode] = useState<"create" | "edit">("create");
  const [editingRow, setEditingRow] = useState<DialogContratoRow | undefined>();

  const load = async () => {
    setIsLoading(true);
    try {
      const list = await contratosService.listar();
      setRows(list.map(toRow));
    } catch {
      toast.error("Não foi possível carregar os contratos.");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const stats = useMemo(() => {
    const ativos = rows.filter((r) => r.__ativo).length;
    const encerrados = rows.length - ativos;
    const total = rows.reduce((acc, r) => acc + r.__valor, 0);
    return { total: rows.length, ativos, encerrados, valorTotal: formatCurrencyBR(total) };
  }, [rows]);

  const aplicarFiltros = async () => {
    if (!filtros.nome && !filtros.cpf && !filtros.beneficio) return load();
    setIsLoading(true);
    try {
      const list = await contratosService.filtrar({
        nome: filtros.nome || undefined,
        cpf: unformatCPF(filtros.cpf) || undefined,
        beneficio: filtros.beneficio || undefined,
      });
      setRows(list.map(toRow));
    } catch {
      toast.error("Não foi possível aplicar os filtros.");
    } finally {
      setIsLoading(false);
    }
  };

  const limparFiltros = () => {
    setFiltros({ nome: "", cpf: "", beneficio: "" });
    load();
  };

  const remover = async () => {
    if (!confirmDelete) return;
    setConfirmLoading(true);
    try {
      await contratosService.remover(confirmDelete);
      toast.success("Contrato removido.");
      setConfirmDelete(null);
      load();
    } catch {
      toast.error("Erro ao remover o contrato.");
    } finally {
      setConfirmLoading(false);
    }
  };

  const renovar = async () => {
    if (!confirmRenew) return;
    setConfirmLoading(true);
    try {
      await contratosService.renovar(confirmRenew);
      toast.success("Contrato renovado com sucesso.");
      setConfirmRenew(null);
      load();
    } catch (err) {
      const msg =
        (err as { response?: { data?: { detail?: string; title?: string } } })?.response?.data
          ?.detail ?? "Erro ao tentar renovar o contrato.";
      toast.error(msg);
    } finally {
      setConfirmLoading(false);
    }
  };

  const baixarPdf = async (id: number) => {
    try {
      const response = await contratosService.gerarPdf(id);
      triggerBlobDownload(response as { data: Blob; headers: Record<string, string> });
    } catch {
      toast.error("Erro ao baixar contrato.");
    }
  };

  return (
    <div className="page-shell">
      <PageHeader
        eyebrow="Carteira"
        title="Contratos"
        description={
          canManage
            ? "Edição, exclusão, renovação e emissão do contrato direto da tabela."
            : canDownload
              ? "Consulta da carteira contratual com emissão de documentos disponíveis."
              : "Consulta da carteira contratual em modo leitura."
        }
        actions={
          canManage && (
            <Button
              onClick={() => {
                setDialogMode("create");
                setEditingRow(undefined);
                setDialogOpen(true);
              }}
            >
              <Plus className="size-4" />
              Novo contrato
            </Button>
          )
        }
      />

      <div className="stats-grid">
        <StatCard label="Total de contratos" value={stats.total} />
        <StatCard label="Ativos" value={stats.ativos} />
        <StatCard label="Encerrados" value={stats.encerrados} />
        <StatCard label="Valor consolidado" value={stats.valorTotal} highlight />
      </div>

      <SectionCard title="Filtros">
        <div className="filter-grid">
          <div className="space-y-1.5">
            <Label htmlFor="cf-nome">Nome</Label>
            <Input
              id="cf-nome"
              placeholder="Nome do cliente"
              value={filtros.nome}
              onChange={(e) => setFiltros((f) => ({ ...f, nome: e.target.value }))}
            />
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="cf-cpf">CPF</Label>
            <MaskedInput
              id="cf-cpf"
              mask="cpf"
              placeholder="000.000.000-00"
              value={filtros.cpf}
              onChange={(v) => setFiltros((f) => ({ ...f, cpf: v }))}
            />
          </div>
          <div className="space-y-1.5">
            <Label>Benefício</Label>
            <Select
              value={filtros.beneficio}
              onValueChange={(v) => setFiltros((f) => ({ ...f, beneficio: v }))}
            >
              <SelectTrigger>
                <SelectValue placeholder="Todos" />
              </SelectTrigger>
              <SelectContent>
                {beneficiosOptions.map((b) => (
                  <SelectItem key={b.value} value={b.value}>
                    {b.label}
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

      <SectionCard title="Tabela de contratos">
        <DataTable
          data={rows}
          excludeColumns={["id"]}
          isLoading={isLoading}
          rowActions={(row) => {
            const id = Number(row.id);
            return (
              <>
                {canManage && (
                  <Button
                    size="sm"
                    variant="ghost"
                    onClick={() => {
                      setDialogMode("edit");
                      setEditingRow(row as unknown as DialogContratoRow);
                      setDialogOpen(true);
                    }}
                  >
                    <Pencil className="size-4" />
                  </Button>
                )}
                {canDownload && (
                  <Button size="sm" variant="ghost" onClick={() => baixarPdf(id)}>
                    <Download className="size-4" />
                  </Button>
                )}
                {canRenew && (
                  <Button size="sm" variant="ghost" onClick={() => setConfirmRenew(id)}>
                    <RefreshCw className="size-4" />
                  </Button>
                )}
                {canManage && (
                  <Button
                    size="sm"
                    variant="ghost"
                    className="text-destructive"
                    onClick={() => setConfirmDelete(id)}
                  >
                    <Trash2 className="size-4" />
                  </Button>
                )}
              </>
            );
          }}
        />
      </SectionCard>

      <ConfirmDialog
        open={confirmDelete !== null}
        onOpenChange={(o) => !o && setConfirmDelete(null)}
        title="Excluir contrato?"
        description="Essa ação é irreversível."
        confirmLabel="Excluir"
        destructive
        loading={confirmLoading}
        onConfirm={remover}
      />
      <ConfirmDialog
        open={confirmRenew !== null}
        onOpenChange={(o) => !o && setConfirmRenew(null)}
        title="Renovar contrato?"
        description="Confirma a renovação deste vínculo?"
        confirmLabel="Renovar"
        loading={confirmLoading}
        onConfirm={renovar}
      />

      <ContratoFormDialog
        open={dialogOpen}
        mode={dialogMode}
        contratoRow={editingRow}
        onOpenChange={setDialogOpen}
        onSaved={load}
      />
    </div>
  );
}
