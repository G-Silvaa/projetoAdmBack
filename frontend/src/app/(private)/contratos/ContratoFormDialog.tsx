"use client";

import { useEffect, useState } from "react";
import { useForm, Controller } from "react-hook-form";
import { Loader2, Search } from "lucide-react";
import { toast } from "sonner";

import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
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
import { MaskedInput } from "@/components/shared/MaskedInput";

import { clientesService } from "@/core/services/clientes.service";
import { contratosService } from "@/core/services/contratos.service";
import { beneficiosOptions, beneficioToCodigo } from "@/core/consts/beneficios";
import { formatCPF, formatCurrencyBR, unformatCPF } from "@/core/helpers/format";
import type { ICliente } from "@/core/types/domain";

type Mode = "create" | "edit";

interface ContratoFormDialogProps {
  open: boolean;
  mode: Mode;
  contratoRow?: ContratoRow;
  onOpenChange: (open: boolean) => void;
  onSaved?: () => void;
}

export interface ContratoRow {
  Id: number;
  Nome?: string;
  CPF?: string;
  Benefício?: string;
  Número?: string;
  Início?: string;
  Conclusão?: string;
  Valor?: string;
  __valor?: number;
}

function brToIso(value?: string | null): string {
  if (!value) return "";
  if (/^\d{4}-\d{2}-\d{2}/.test(value)) return value.split("T")[0];
  const m = /^(\d{2})\/(\d{2})\/(\d{4})$/.exec(value);
  if (m) return `${m[3]}-${m[2]}-${m[1]}`;
  return value;
}

function moneyToNumber(formatted: string): number {
  const digits = formatted.replace(/\D/g, "");
  if (!digits) return 0;
  return Number(digits) / 100;
}

interface FormValues {
  cpf: string;
  clienteId: number | null;
  beneficio: string;
  valor: string;
  indicacao: string;
  inicio: string;
  conclusao: string;
}

const defaults: FormValues = {
  cpf: "",
  clienteId: null,
  beneficio: "",
  valor: "",
  indicacao: "",
  inicio: "",
  conclusao: "",
};

export function ContratoFormDialog({
  open,
  mode,
  contratoRow,
  onOpenChange,
  onSaved,
}: ContratoFormDialogProps) {
  const isCreate = mode === "create";
  const [submitting, setSubmitting] = useState(false);
  const [searching, setSearching] = useState(false);
  const [clienteEncontrado, setClienteEncontrado] = useState<ICliente | null>(null);
  const [clienteErro, setClienteErro] = useState<string | null>(null);

  const { register, handleSubmit, control, watch, setValue, reset } = useForm<FormValues>({
    defaultValues: defaults,
  });

  const cpfValue = watch("cpf");

  // Reset ao abrir/fechar
  useEffect(() => {
    if (!open) {
      reset(defaults);
      setClienteEncontrado(null);
      setClienteErro(null);
      return;
    }
    if (!isCreate && contratoRow) {
      reset({
        ...defaults,
        clienteId: null,
        cpf: contratoRow.CPF ?? "",
        beneficio: contratoRow.Benefício ?? "",
        valor: contratoRow.__valor ? formatCurrencyBR(contratoRow.__valor) : "",
        indicacao: "",
        inicio: brToIso(contratoRow.Início),
        conclusao: brToIso(contratoRow.Conclusão),
      });
    } else if (isCreate) {
      reset(defaults);
    }
  }, [open, isCreate, contratoRow, reset]);

  // CPF lookup no modo create
  useEffect(() => {
    if (!isCreate || !open) return;
    const digits = unformatCPF(cpfValue);
    if (digits.length !== 11) {
      setClienteEncontrado(null);
      setClienteErro(null);
      setValue("clienteId", null);
      return;
    }
    let cancelled = false;
    setSearching(true);
    setClienteErro(null);
    const timer = setTimeout(async () => {
      try {
        const result = await clientesService.filtrar({ cpf: digits });
        const cliente = result.content?.[0];
        if (cancelled) return;
        if (cliente) {
          setClienteEncontrado(cliente);
          setValue("clienteId", cliente.id);
        } else {
          setClienteEncontrado(null);
          setValue("clienteId", null);
          setClienteErro(
            "CPF não encontrado. Cadastre o cliente primeiro em /clientes → Novo cliente.",
          );
        }
      } catch {
        if (!cancelled) {
          setClienteErro("Erro ao buscar cliente.");
        }
      } finally {
        if (!cancelled) setSearching(false);
      }
    }, 350);
    return () => {
      cancelled = true;
      clearTimeout(timer);
    };
  }, [cpfValue, isCreate, open, setValue]);

  const onSubmit = handleSubmit(async (values) => {
    setSubmitting(true);
    try {
      if (isCreate) {
        if (!values.clienteId) {
          toast.error("Selecione um cliente válido (CPF não encontrado).");
          setSubmitting(false);
          return;
        }
        const payload = {
          cliente: { id: values.clienteId },
          beneficio: beneficioToCodigo(values.beneficio),
          valor: moneyToNumber(values.valor),
          indicacao: values.indicacao || null,
        };
        await clientesService.associarBeneficio(payload);
        toast.success("Contrato criado.");
      } else if (contratoRow) {
        const payload: Record<string, unknown> = {
          valor: moneyToNumber(values.valor),
          indicacao: values.indicacao || null,
          inicio: values.inicio || null,
          conclusao: values.conclusao || null,
        };
        await contratosService.atualizar(contratoRow.Id, payload);
        toast.success("Contrato atualizado.");
      }
      onSaved?.();
      onOpenChange(false);
    } catch (err) {
      const data = (err as { response?: { data?: { detail?: string; title?: string; message?: string } } })?.response?.data;
      toast.error(data?.detail ?? data?.message ?? data?.title ?? "Erro ao salvar contrato.");
    } finally {
      setSubmitting(false);
    }
  });

  return (
    <Dialog open={open} onOpenChange={(o) => !submitting && onOpenChange(o)}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>
            {isCreate ? "Novo contrato" : "Editar contrato"}
          </DialogTitle>
          <DialogDescription>
            {isCreate
              ? "Associe um novo benefício a um cliente já cadastrado. O processo é criado automaticamente."
              : contratoRow?.Nome
                ? `${contratoRow.Nome} · ${contratoRow.Benefício ?? ""}`
                : "Atualize valor, datas e indicação."}
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={onSubmit} className="space-y-4">
          {isCreate ? (
            <>
              <div className="space-y-1.5">
                <Label className="text-xs font-medium text-text-list">CPF do cliente</Label>
                <Controller
                  control={control}
                  name="cpf"
                  render={({ field }) => (
                    <MaskedInput
                      mask="cpf"
                      value={field.value}
                      onChange={field.onChange}
                      placeholder="000.000.000-00"
                    />
                  )}
                />
                {searching && (
                  <p className="text-xs text-muted-foreground flex items-center gap-1">
                    <Loader2 className="size-3 animate-spin" /> Buscando cliente...
                  </p>
                )}
                {clienteEncontrado && (
                  <div className="rounded-md border border-success/40 bg-success/5 px-3 py-2 text-xs">
                    <p className="font-medium text-success">Cliente encontrado</p>
                    <p className="text-text-list">
                      {clienteEncontrado.contato?.nome} · {formatCPF(clienteEncontrado.cpf)}
                    </p>
                  </div>
                )}
                {clienteErro && (
                  <div className="rounded-md border border-destructive/40 bg-destructive/5 px-3 py-2 text-xs text-destructive flex items-start gap-1.5">
                    <Search className="size-3 mt-0.5 shrink-0" />
                    {clienteErro}
                  </div>
                )}
              </div>
            </>
          ) : (
            <div className="rounded-md border border-border bg-background/40 px-3 py-2 text-xs">
              <p className="font-medium text-text">{contratoRow?.Nome}</p>
              <p className="text-muted-foreground">
                CPF {contratoRow?.CPF} · contrato {contratoRow?.Número}
              </p>
            </div>
          )}

          <div className="grid gap-3 sm:grid-cols-2">
            <div className="space-y-1.5">
              <Label className="text-xs font-medium text-text-list">Benefício</Label>
              <Controller
                control={control}
                name="beneficio"
                render={({ field }) => (
                  <Select
                    value={field.value}
                    onValueChange={field.onChange}
                    disabled={!isCreate}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Selecione" />
                    </SelectTrigger>
                    <SelectContent>
                      {beneficiosOptions.map((b) => (
                        <SelectItem key={b.value} value={b.value}>
                          {b.label}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                )}
              />
              {!isCreate && (
                <p className="text-xs text-muted-foreground">
                  Benefício é definido no momento da criação.
                </p>
              )}
            </div>
            <div className="space-y-1.5">
              <Label className="text-xs font-medium text-text-list">Valor</Label>
              <Controller
                control={control}
                name="valor"
                render={({ field }) => (
                  <MaskedInput
                    mask="money"
                    value={field.value}
                    onChange={field.onChange}
                    placeholder="R$ 0,00"
                  />
                )}
              />
            </div>
          </div>

          {!isCreate && (
            <div className="grid gap-3 sm:grid-cols-2">
              <div className="space-y-1.5">
                <Label className="text-xs font-medium text-text-list">Início</Label>
                <Input type="date" {...register("inicio")} />
              </div>
              <div className="space-y-1.5">
                <Label className="text-xs font-medium text-text-list">Conclusão</Label>
                <Input type="date" {...register("conclusao")} />
                <p className="text-xs text-muted-foreground">
                  Só preencher após todos os processos estarem aprovados/reprovados.
                </p>
              </div>
            </div>
          )}

          <div className="space-y-1.5">
            <Label className="text-xs font-medium text-text-list">Indicação</Label>
            <Input
              {...register("indicacao")}
              placeholder="Como o cliente chegou (opcional)"
            />
          </div>
        </form>

        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)} disabled={submitting}>
            Cancelar
          </Button>
          <Button
            onClick={onSubmit}
            disabled={submitting || (isCreate && !clienteEncontrado)}
          >
            {submitting && <Loader2 className="size-4 animate-spin" />}
            {isCreate ? "Criar contrato" : "Salvar alterações"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
