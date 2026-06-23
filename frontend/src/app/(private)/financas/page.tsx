"use client";

import { useEffect, useMemo, useState } from "react";
import { Download, FileText, Pencil, Receipt } from "lucide-react";
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
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";

import { useAuth } from "@/core/auth/AuthProvider";
import { financasService, triggerBlobDownload } from "@/core/services/financas.service";
import { beneficiosOptions } from "@/core/consts/beneficios";
import {
  formatCPF,
  formatCurrencyBR,
  formatDateBR,
  unformatCPF,
} from "@/core/helpers/format";
import type { IFinanceiro } from "@/core/types/domain";
import { FinanceiroFormDialog, type FinanceiroRow as DialogFinanceiroRow } from "./FinanceiroFormDialog";

interface FinanceiroRow extends Record<string, unknown> {
  id: number;
  Id: number;
  __quitado: boolean;
  __valorTotal: number;
  __proximaParcela: number;
  Nome: string;
  CPF: string;
  Benefício: string;
  "Valor total à pagar": string;
  "Montante pago": string;
  "Próxima parcela": string;
  "Parcelas restantes": number | string;
  "Situação parcela": string;
  "Situação pagamento": string;
  "Data pagamento da parcela": string;
}

function toRow(item: IFinanceiro): FinanceiroRow {
  return {
    id: item.id,
    Id: item.id,
    __quitado: !!item.situacaoPagamento,
    __valorTotal: Number(item.valorTotalPagar ?? 0),
    __proximaParcela: Number(item.valorProximaParcela ?? 0),
    Nome: item.contrato?.cliente?.contato?.nome ?? "",
    CPF: formatCPF(item.contrato?.cliente?.cpf ?? ""),
    Benefício: item.contrato?.beneficio ?? "",
    "Valor total à pagar": formatCurrencyBR(item.valorTotalPagar ?? 0),
    "Montante pago": formatCurrencyBR(item.montantePago ?? 0),
    "Próxima parcela": formatCurrencyBR(item.valorProximaParcela ?? 0),
    "Parcelas restantes": item.parcelasRestantes ?? "—",
    "Situação parcela": item.situacaoParcela ?? "—",
    "Situação pagamento": item.situacaoPagamento ? "Pago" : "Aguardando Pagamento",
    "Data pagamento da parcela": item.dataPagamentoParcela
      ? formatDateBR(item.dataPagamentoParcela)
      : "",
  };
}

const SITUACAO_PARCELA = [
  { value: "EM_DIA", label: "Em dia" },
  { value: "ATRASADA", label: "Atrasada" },
  { value: "VENCIDA", label: "Vencida" },
];

const SITUACAO_PAGAMENTO = [
  { value: "true", label: "Pago" },
  { value: "false", label: "Aguardando pagamento" },
];

export default function FinancasPage() {
  const { hasCapability } = useAuth();
  const canManage = hasCapability("manageFinancas");
  const canOperate = hasCapability("operateFinanceiroDocuments");

  const [rows, setRows] = useState<FinanceiroRow[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [filtros, setFiltros] = useState({
    nome: "",
    cpf: "",
    beneficio: "",
    situacaoParcela: "",
    situacaoPagamento: "",
  });
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editingRow, setEditingRow] = useState<DialogFinanceiroRow | undefined>();

  const load = async () => {
    setIsLoading(true);
    try {
      const list = await financasService.listar();
      setRows(list.map(toRow));
    } catch {
      toast.error("Erro ao carregar dados financeiros.");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const stats = useMemo(() => {
    const pagos = rows.filter((r) => r.__quitado).length;
    const pendentes = rows.length - pagos;
    const proxima = rows.reduce((acc, r) => acc + r.__proximaParcela, 0);
    return {
      total: rows.length,
      pagos,
      pendentes,
      proxima: formatCurrencyBR(proxima),
    };
  }, [rows]);

  const aplicarFiltros = async () => {
    const isEmpty =
      !filtros.nome &&
      !filtros.cpf &&
      !filtros.beneficio &&
      !filtros.situacaoParcela &&
      filtros.situacaoPagamento === "";
    if (isEmpty) return load();

    setIsLoading(true);
    try {
      const list = await financasService.filtrar({
        nome: filtros.nome || undefined,
        cpf: unformatCPF(filtros.cpf) || undefined,
        beneficio: filtros.beneficio || undefined,
        situacaoParcela: filtros.situacaoParcela || undefined,
        situacaoPagamento: filtros.situacaoPagamento,
      });
      setRows(list.map(toRow));
    } catch {
      toast.error("Não foi possível aplicar os filtros financeiros.");
    } finally {
      setIsLoading(false);
    }
  };

  const limparFiltros = () => {
    setFiltros({
      nome: "",
      cpf: "",
      beneficio: "",
      situacaoParcela: "",
      situacaoPagamento: "",
    });
    load();
  };

  const gerarBoleto = async (id: number) => {
    try {
      const response = await financasService.gerarBoleto(id);
      triggerBlobDownload(response as { data: Blob; headers: Record<string, string> }, "boleto.pdf");
    } catch {
      toast.error("Erro ao gerar o boleto.");
    }
  };

  const baixarComprovante = async (id: number) => {
    try {
      const response = await financasService.baixarComprovante(id);
      triggerBlobDownload(
        response as { data: Blob; headers: Record<string, string> },
        "comprovante.pdf",
      );
    } catch {
      toast.error("Erro ao baixar comprovante.");
    }
  };

  return (
    <div className="page-shell">
      <PageHeader
        eyebrow="Carteira financeira"
        title="Finanças"
        description={
          canManage
            ? "Edição rápida e emissão de documentos financeiros diretamente na tabela."
            : canOperate
              ? "Emissão de boletos e comprovantes com visão financeira consolidada."
              : "Consulta da carteira financeira em modo leitura."
        }
      />

      <div className="stats-grid">
        <StatCard label="Registros financeiros" value={stats.total} />
        <StatCard label="Pagos" value={stats.pagos} highlight />
        <StatCard label="Pendentes" value={stats.pendentes} />
        <StatCard label="Próxima receita" value={stats.proxima} />
      </div>

      <SectionCard title="Filtros">
        <div className="filter-grid">
          <div className="space-y-1.5">
            <Label htmlFor="ff-nome">Nome</Label>
            <Input
              id="ff-nome"
              placeholder="Nome do cliente"
              value={filtros.nome}
              onChange={(e) => setFiltros((f) => ({ ...f, nome: e.target.value }))}
            />
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="ff-cpf">CPF</Label>
            <MaskedInput
              id="ff-cpf"
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
          <div className="space-y-1.5">
            <Label>Situação parcela</Label>
            <Select
              value={filtros.situacaoParcela}
              onValueChange={(v) => setFiltros((f) => ({ ...f, situacaoParcela: v }))}
            >
              <SelectTrigger>
                <SelectValue placeholder="Todas" />
              </SelectTrigger>
              <SelectContent>
                {SITUACAO_PARCELA.map((b) => (
                  <SelectItem key={b.value} value={b.value}>
                    {b.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <div className="space-y-1.5">
            <Label>Situação pagamento</Label>
            <Select
              value={filtros.situacaoPagamento}
              onValueChange={(v) => setFiltros((f) => ({ ...f, situacaoPagamento: v }))}
            >
              <SelectTrigger>
                <SelectValue placeholder="Todas" />
              </SelectTrigger>
              <SelectContent>
                {SITUACAO_PAGAMENTO.map((b) => (
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

      <SectionCard title="Movimentação financeira">
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
                      setEditingRow(row as unknown as DialogFinanceiroRow);
                      setDialogOpen(true);
                    }}
                  >
                    <Pencil className="size-4" />
                  </Button>
                )}
                {canOperate && (
                  <>
                    <Button size="sm" variant="ghost" onClick={() => gerarBoleto(id)}>
                      <Receipt className="size-4" />
                    </Button>
                    <Button size="sm" variant="ghost" onClick={() => baixarComprovante(id)}>
                      <Download className="size-4" />
                    </Button>
                  </>
                )}
                {!canManage && !canOperate && (
                  <Button size="sm" variant="ghost" disabled>
                    <FileText className="size-4" />
                  </Button>
                )}
              </>
            );
          }}
        />
      </SectionCard>

      <FinanceiroFormDialog
        open={dialogOpen}
        row={editingRow}
        onOpenChange={setDialogOpen}
        onSaved={load}
      />
    </div>
  );
}
