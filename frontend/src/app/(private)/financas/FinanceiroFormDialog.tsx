"use client";

import { useEffect, useState } from "react";
import { useForm, Controller } from "react-hook-form";
import { Loader2 } from "lucide-react";
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
import { MaskedInput } from "@/components/shared/MaskedInput";

import { financasService } from "@/core/services/financas.service";
import { formatCurrencyBR } from "@/core/helpers/format";

export interface FinanceiroRow {
  Id: number;
  Nome?: string;
  CPF?: string;
  Benefício?: string;
  __valorTotal?: number;
  __proximaParcela?: number;
  "Parcelas restantes"?: number | string;
  "Próxima parcela"?: string;
  "Valor total à pagar"?: string;
}

function moneyToNumber(formatted: string): number {
  const digits = formatted.replace(/\D/g, "");
  if (!digits) return 0;
  return Number(digits) / 100;
}

interface FormValues {
  parcelasRestantes: string;
  valorProximaParcela: string;
  valorTotalPagar: string;
}

const defaults: FormValues = {
  parcelasRestantes: "",
  valorProximaParcela: "",
  valorTotalPagar: "",
};

interface FinanceiroFormDialogProps {
  open: boolean;
  row?: FinanceiroRow;
  onOpenChange: (open: boolean) => void;
  onSaved?: () => void;
}

export function FinanceiroFormDialog({
  open,
  row,
  onOpenChange,
  onSaved,
}: FinanceiroFormDialogProps) {
  const [submitting, setSubmitting] = useState(false);
  const { register, handleSubmit, control, reset } = useForm<FormValues>({
    defaultValues: defaults,
  });

  useEffect(() => {
    if (!open) {
      reset(defaults);
      return;
    }
    if (row) {
      reset({
        parcelasRestantes: String(row["Parcelas restantes"] ?? ""),
        valorProximaParcela: row.__proximaParcela
          ? formatCurrencyBR(row.__proximaParcela)
          : "",
        valorTotalPagar: row.__valorTotal ? formatCurrencyBR(row.__valorTotal) : "",
      });
    }
  }, [open, row, reset]);

  const onSubmit = handleSubmit(async (values) => {
    if (!row) return;
    setSubmitting(true);
    try {
      const parcelas = Number(values.parcelasRestantes);
      if (!Number.isFinite(parcelas) || parcelas < 0) {
        toast.error("Parcelas restantes precisa ser um número não negativo.");
        setSubmitting(false);
        return;
      }
      const payload: Record<string, unknown> = {
        parcelasRestantes: parcelas,
        valorProximaParcela: moneyToNumber(values.valorProximaParcela),
        valorTotalPagar: moneyToNumber(values.valorTotalPagar),
      };
      await financasService.atualizar(row.Id, payload);
      toast.success("Financeiro atualizado.");
      onSaved?.();
      onOpenChange(false);
    } catch (err) {
      const data = (err as { response?: { data?: { detail?: string; title?: string; message?: string } } })?.response?.data;
      toast.error(data?.detail ?? data?.message ?? data?.title ?? "Erro ao atualizar financeiro.");
    } finally {
      setSubmitting(false);
    }
  });

  return (
    <Dialog open={open} onOpenChange={(o) => !submitting && onOpenChange(o)}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Editar financeiro</DialogTitle>
          <DialogDescription>
            {row?.Nome ? (
              <span>
                {row.Nome}
                {row.CPF && <> · {row.CPF}</>}
                {row.Benefício && <> · {row.Benefício}</>}
              </span>
            ) : (
              "Ajustar parcelas e valores deste financeiro."
            )}
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={onSubmit} className="space-y-4">
          <div className="space-y-1.5">
            <Label className="text-xs font-medium text-text-list">Parcelas restantes</Label>
            <Input
              type="number"
              min={0}
              step={1}
              {...register("parcelasRestantes")}
            />
            <p className="text-xs text-muted-foreground">
              Se zerar e houver parcelas pagas, o backend marca como quitado automaticamente.
            </p>
          </div>

          <div className="grid gap-3 sm:grid-cols-2">
            <div className="space-y-1.5">
              <Label className="text-xs font-medium text-text-list">Valor da próxima parcela</Label>
              <Controller
                control={control}
                name="valorProximaParcela"
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
            <div className="space-y-1.5">
              <Label className="text-xs font-medium text-text-list">Valor total a pagar</Label>
              <Controller
                control={control}
                name="valorTotalPagar"
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
        </form>

        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)} disabled={submitting}>
            Cancelar
          </Button>
          <Button onClick={onSubmit} disabled={submitting || !row}>
            {submitting && <Loader2 className="size-4 animate-spin" />}
            Salvar alterações
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
