"use client";

import { useEffect, useMemo, useState } from "react";
import { Pencil, Plus, Trash2, Users } from "lucide-react";
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

import { useAuth } from "@/core/auth/AuthProvider";
import { clientesService } from "@/core/services/clientes.service";
import { unformatCPF } from "@/core/helpers/format";
import type { ICliente, IRepresentante } from "@/core/types/domain";
import { ClienteFormDialog } from "./ClienteFormDialog";

interface ClienteRow extends Record<string, unknown> {
  id: number;
  Nome: string;
  CPF: string;
  RG: string;
  "E-mail": string;
  "Tem representante?": string;
}

function temRepresentante(rep?: IRepresentante | null): string {
  return rep && rep.id ? "Sim" : "Não";
}

function toRow(cliente: ICliente): ClienteRow {
  return {
    id: cliente.id,
    Nome: cliente.contato.nome,
    CPF: cliente.cpf,
    RG: cliente.rg,
    "E-mail": cliente.contato.email ?? "",
    "Tem representante?": temRepresentante(cliente.representante),
  };
}

export default function ClientesPage() {
  const { hasCapability } = useAuth();
  const canManage = hasCapability("manageClientes");

  const [rows, setRows] = useState<ClienteRow[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [filtros, setFiltros] = useState({ nome: "", email: "", rg: "", cpf: "" });
  const [confirmDelete, setConfirmDelete] = useState<number | null>(null);
  const [confirmLoading, setConfirmLoading] = useState(false);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editingId, setEditingId] = useState<number | undefined>(undefined);

  const load = async () => {
    setIsLoading(true);
    try {
      const list = await clientesService.listarTodos();
      setRows(list.map(toRow));
    } catch {
      toast.error("Não foi possível carregar os clientes.");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const stats = useMemo(() => {
    const comRep = rows.filter((r) => r["Tem representante?"] === "Sim").length;
    const semRep = rows.length - comRep;
    const emails = rows.filter((r) => !!r["E-mail"]).length;
    return { total: rows.length, comRep, semRep, emails };
  }, [rows]);

  const aplicarFiltros = async () => {
    if (!filtros.nome && !filtros.email && !filtros.rg && !filtros.cpf) return load();
    setIsLoading(true);
    try {
      const result = await clientesService.filtrar({
        ...filtros,
        cpf: unformatCPF(filtros.cpf) || undefined,
        nome: filtros.nome || undefined,
        email: filtros.email || undefined,
        rg: filtros.rg || undefined,
      });
      setRows((result.content ?? []).map(toRow));
    } catch {
      toast.error("Erro ao aplicar os filtros.");
    } finally {
      setIsLoading(false);
    }
  };

  const limparFiltros = () => {
    setFiltros({ nome: "", email: "", rg: "", cpf: "" });
    load();
  };

  const remover = async () => {
    if (!confirmDelete) return;
    setConfirmLoading(true);
    try {
      await clientesService.remover(confirmDelete);
      toast.success("Cliente removido.");
      setConfirmDelete(null);
      load();
    } catch {
      toast.error("Erro ao remover cliente.");
    } finally {
      setConfirmLoading(false);
    }
  };

  return (
    <div className="page-shell">
      <PageHeader
        eyebrow="Base cadastral"
        title="Clientes"
        description={
          canManage
            ? "Listagem principal para edição e exclusão dos registros."
            : "Listagem em modo consulta para visualizar cadastros e representantes."
        }
        actions={
          canManage && (
            <Button
              onClick={() => {
                setEditingId(undefined);
                setDialogOpen(true);
              }}
            >
              <Plus className="size-4" />
              Novo cliente
            </Button>
          )
        }
      />

      <div className="stats-grid">
        <StatCard label="Total de clientes" value={stats.total} />
        <StatCard label="Com representante" value={stats.comRep} />
        <StatCard label="Sem representante" value={stats.semRep} />
        <StatCard label="E-mails cadastrados" value={stats.emails} highlight />
      </div>

      <SectionCard title="Filtros">
        <div className="filter-grid">
          <div className="space-y-1.5">
            <Label htmlFor="cl-nome">Nome</Label>
            <Input
              id="cl-nome"
              placeholder="Nome do cliente"
              value={filtros.nome}
              onChange={(e) => setFiltros((f) => ({ ...f, nome: e.target.value }))}
            />
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="cl-email">E-mail</Label>
            <Input
              id="cl-email"
              placeholder="exemplo@dominio.com"
              value={filtros.email}
              onChange={(e) => setFiltros((f) => ({ ...f, email: e.target.value }))}
            />
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="cl-rg">RG</Label>
            <Input
              id="cl-rg"
              placeholder="Apenas dígitos"
              value={filtros.rg}
              onChange={(e) => setFiltros((f) => ({ ...f, rg: e.target.value }))}
            />
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="cl-cpf">CPF</Label>
            <MaskedInput
              id="cl-cpf"
              mask="cpf"
              placeholder="000.000.000-00"
              value={filtros.cpf}
              onChange={(v) => setFiltros((f) => ({ ...f, cpf: v }))}
            />
          </div>
        </div>
        <div className="mt-4 flex justify-end gap-2">
          <Button variant="outline" onClick={limparFiltros}>
            Limpar
          </Button>
          <Button onClick={aplicarFiltros}>Aplicar filtros</Button>
        </div>
      </SectionCard>

      <SectionCard title="Cadastros">
        {rows.length === 0 && !isLoading ? (
          <div className="flex flex-col items-center gap-3 py-12 text-center">
            <Users className="size-10 text-muted-foreground" />
            <p className="font-display text-xl text-primary">Nenhum cliente encontrado</p>
            <p className="max-w-sm text-sm text-muted-foreground">
              Ajuste os filtros ou cadastre um novo cliente para começar.
            </p>
          </div>
        ) : (
          <DataTable
            data={rows}
            excludeColumns={["id"]}
            isLoading={isLoading}
            searchPlaceholder="Busca rápida"
            rowActions={(row) => {
              const id = Number(row.id);
              return (
                <>
                  {canManage && (
                    <>
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
                      <Button
                        size="sm"
                        variant="ghost"
                        className="text-destructive"
                        onClick={() => setConfirmDelete(id)}
                      >
                        <Trash2 className="size-4" />
                      </Button>
                    </>
                  )}
                </>
              );
            }}
          />
        )}
      </SectionCard>

      <ConfirmDialog
        open={confirmDelete !== null}
        onOpenChange={(o) => !o && setConfirmDelete(null)}
        title="Excluir cliente?"
        description="Essa ação não pode ser desfeita."
        confirmLabel="Sim, excluir"
        destructive
        loading={confirmLoading}
        onConfirm={remover}
      />

      <ClienteFormDialog
        open={dialogOpen}
        mode={editingId ? "edit" : "create"}
        clienteId={editingId}
        onOpenChange={setDialogOpen}
        onSaved={load}
      />
    </div>
  );
}
