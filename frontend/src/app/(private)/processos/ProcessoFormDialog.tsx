"use client";

import { useEffect, useMemo, useState } from "react";
import { useForm, Controller } from "react-hook-form";
import { Loader2, Info } from "lucide-react";
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

import { processosService } from "@/core/services/processos.service";
import { statusProcessoOptions } from "@/core/consts/beneficios";
import { formatCPF, formatCurrencyBR } from "@/core/helpers/format";

const MOD_PERICIA = new Set(["87", "31"]);
const MOD_AVALIACAO_SOCIAL = new Set(["87"]);
const MOD_AUXILIO_INCAPACIDADE = "31";

const STATUS_EXIGE_PROTOCOLO = new Set([
  "ANALISE",
  "CUMPRIMENTO_EXIGENCIA",
  "ANALISE_ADMINISTRATIVA",
  "APROVADO",
  "REPROVADO",
]);
const STATUS_EXIGE_DOCS_PENDENTES = new Set(["PENDENTE", "CUMPRIMENTO_EXIGENCIA"]);

interface ProcessoFormValues {
  status: string;
  numeroProtocolo: string;
  entradaDoProtocolo: string;
  documentosPendentes: string;
  periciaMedica: string;
  enderecoPericiaMedica: string;
  avaliacaoSocial: string;
  enderecoAvaliacaoSocial: string;
  dataConcessao: string;
  cessacao: string;
  valorConcedido: string;
}

const defaults: ProcessoFormValues = {
  status: "AGUARDANDO",
  numeroProtocolo: "",
  entradaDoProtocolo: "",
  documentosPendentes: "",
  periciaMedica: "",
  enderecoPericiaMedica: "",
  avaliacaoSocial: "",
  enderecoAvaliacaoSocial: "",
  dataConcessao: "",
  cessacao: "",
  valorConcedido: "",
};

interface ProcessoFormDialogProps {
  open: boolean;
  processoId?: number;
  onOpenChange: (open: boolean) => void;
  onSaved?: () => void;
}

function brToIso(value?: string | null): string {
  if (!value) return "";
  return value.split("T")[0];
}

function isoFromInput(value?: string): string | null {
  if (!value) return null;
  return value;
}

function dateTimeIso(value?: string): string | null {
  if (!value) return null;
  // input type=date returns YYYY-MM-DD, backend expects ISO 8601 with time for timestamp columns
  return value.includes("T") ? value : `${value}T00:00:00`;
}

function moneyToNumber(formatted: string): number | null {
  const digits = formatted.replace(/\D/g, "");
  if (!digits) return null;
  return Number(digits) / 100;
}

function numberToMoney(value?: number | null): string {
  if (value == null) return "";
  return formatCurrencyBR(value);
}

export function ProcessoFormDialog({
  open,
  processoId,
  onOpenChange,
  onSaved,
}: ProcessoFormDialogProps) {
  const [submitting, setSubmitting] = useState(false);
  const [loading, setLoading] = useState(false);
  const [processo, setProcesso] = useState<Record<string, unknown> | null>(null);

  const { register, handleSubmit, control, watch, reset, setValue } =
    useForm<ProcessoFormValues>({
      defaultValues: defaults,
    });

  const status = watch("status");
  const periciaData = watch("periciaMedica");
  const avaliacaoData = watch("avaliacaoSocial");

  // Carrega processo ao abrir
  useEffect(() => {
    if (!open || !processoId) return;
    setLoading(true);
    processosService
      .porId(processoId)
      .then((p) => {
        if (!p) return;
        setProcesso(p);
        reset({
          status: (p.status as string) ?? "AGUARDANDO",
          numeroProtocolo: (p.numeroProtocolo as string) ?? "",
          entradaDoProtocolo: brToIso(p.entradaDoProtocolo as string),
          documentosPendentes: (p.documentosPendentes as string) ?? "",
          periciaMedica: brToIso(p.periciaMedica as string),
          enderecoPericiaMedica: (p.enderecoPericiaMedica as string) ?? "",
          avaliacaoSocial: brToIso(p.avaliacaoSocial as string),
          enderecoAvaliacaoSocial: (p.enderecoAvaliacaoSocial as string) ?? "",
          dataConcessao: brToIso(p.dataConcessao as string),
          cessacao: brToIso(p.cessacao as string),
          valorConcedido: numberToMoney(p.valorConcedido as number | null),
        });
      })
      .catch(() => toast.error("Erro ao carregar processo."))
      .finally(() => setLoading(false));
  }, [open, processoId, reset]);

  useEffect(() => {
    if (!open) {
      reset(defaults);
      setProcesso(null);
    }
  }, [open, reset]);

  const cliente = (processo?.contrato as { cliente?: { contato?: { nome?: string }; cpf?: string } })?.cliente;
  const contrato = processo?.contrato as { beneficio?: string; numero?: string } | undefined;
  const modalidade = contrato?.beneficio;
  const isAuxilio31 = modalidade === MOD_AUXILIO_INCAPACIDADE;

  const permissoes = useMemo(() => {
    return {
      periciaPermitida: modalidade ? MOD_PERICIA.has(modalidade) : true,
      avaliacaoPermitida: modalidade ? MOD_AVALIACAO_SOCIAL.has(modalidade) : true,
      cessacaoVisivel: isAuxilio31,
      valorConcedidoVisivel: isAuxilio31,
      protocoloObrigatorio: STATUS_EXIGE_PROTOCOLO.has(status),
      documentosObrigatorios: STATUS_EXIGE_DOCS_PENDENTES.has(status),
      concessaoObrigatoria: status === "APROVADO",
      cessacaoObrigatoria: isAuxilio31 && status === "APROVADO",
    };
  }, [modalidade, status, isAuxilio31]);

  const onSubmit = handleSubmit(async (values) => {
    if (!processoId) return;
    setSubmitting(true);
    try {
      const payload: Record<string, unknown> = {
        status: values.status || null,
        numeroProtocolo: values.numeroProtocolo || null,
        entradaDoProtocolo: isoFromInput(values.entradaDoProtocolo),
        documentosPendentes: values.documentosPendentes || null,
        periciaMedica: dateTimeIso(values.periciaMedica),
        enderecoPericiaMedica: values.enderecoPericiaMedica || null,
        avaliacaoSocial: dateTimeIso(values.avaliacaoSocial),
        enderecoAvaliacaoSocial: values.enderecoAvaliacaoSocial || null,
        dataConcessao: isoFromInput(values.dataConcessao),
        cessacao: isoFromInput(values.cessacao),
        valorConcedido: moneyToNumber(values.valorConcedido),
      };
      await processosService.atualizar(processoId, payload);
      toast.success("Processo atualizado.");
      onSaved?.();
      onOpenChange(false);
    } catch (err) {
      const data = (err as { response?: { data?: { detail?: string; title?: string; message?: string } } })?.response?.data;
      toast.error(data?.detail ?? data?.message ?? data?.title ?? "Erro ao atualizar processo.");
    } finally {
      setSubmitting(false);
    }
  });

  return (
    <Dialog open={open} onOpenChange={(o) => !submitting && onOpenChange(o)}>
      <DialogContent className="max-w-3xl">
        <DialogHeader>
          <DialogTitle>Editar processo</DialogTitle>
          <DialogDescription>
            {cliente?.contato?.nome ? (
              <span>
                {cliente.contato.nome}
                {cliente.cpf && <> · {formatCPF(cliente.cpf)}</>}
                {contrato?.numero && <> · contrato {contrato.numero}</>}
                {modalidade && <> · modalidade <b>{modalidade}</b></>}
              </span>
            ) : (
              "Atualize status, agendamentos e concessão."
            )}
          </DialogDescription>
        </DialogHeader>

        {loading ? (
          <div className="grid place-items-center py-10">
            <Loader2 className="size-5 animate-spin text-primary" />
          </div>
        ) : (
          <form onSubmit={onSubmit} className="space-y-6 max-h-[70vh] overflow-y-auto pr-1">
            {/* Status + protocolo */}
            <section className="space-y-3">
              <div className="flex items-center justify-between">
                <h3 className="text-sm font-semibold text-text">Status e protocolo</h3>
                {permissoes.protocoloObrigatorio && (
                  <span className="text-xs text-warning flex items-center gap-1">
                    <Info className="size-3" /> Protocolo obrigatório neste status
                  </span>
                )}
              </div>
              <div className="grid gap-3 sm:grid-cols-2">
                <Field label="Status">
                  <Controller
                    control={control}
                    name="status"
                    render={({ field }) => (
                      <Select value={field.value} onValueChange={field.onChange}>
                        <SelectTrigger>
                          <SelectValue />
                        </SelectTrigger>
                        <SelectContent>
                          {statusProcessoOptions.map((o) => (
                            <SelectItem key={o.value} value={o.value}>
                              {o.label}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                    )}
                  />
                </Field>
                <Field label={`Número do protocolo${permissoes.protocoloObrigatorio ? " *" : ""}`}>
                  <Input {...register("numeroProtocolo")} placeholder="Apenas dígitos" />
                </Field>
              </div>
              <div className="grid gap-3 sm:grid-cols-2">
                <Field label={`Entrada do protocolo${permissoes.protocoloObrigatorio ? " *" : ""}`}>
                  <Input type="date" {...register("entradaDoProtocolo")} />
                </Field>
                <Field
                  label={`Documentos pendentes${permissoes.documentosObrigatorios ? " *" : ""}`}
                  hint={permissoes.documentosObrigatorios ? "Obrigatório neste status" : undefined}
                >
                  <Input
                    {...register("documentosPendentes")}
                    placeholder="ex.: RG, comprovante..."
                  />
                </Field>
              </div>
            </section>

            {/* Perícia médica */}
            <section className="space-y-3">
              <div className="flex items-center justify-between">
                <h3 className="text-sm font-semibold text-text">Perícia médica</h3>
                {!permissoes.periciaPermitida && (
                  <span className="text-xs text-muted-foreground flex items-center gap-1">
                    <Info className="size-3" /> Não permitido para esta modalidade
                  </span>
                )}
                {permissoes.periciaPermitida && status === "ANALISE_ADMINISTRATIVA" && (
                  <span className="text-xs text-warning flex items-center gap-1">
                    <Info className="size-3" /> Perícia obrigatória pra entrar em análise administrativa
                  </span>
                )}
              </div>
              <div className="grid gap-3 sm:grid-cols-2">
                <Field label="Data e hora">
                  <Input
                    type="datetime-local"
                    {...register("periciaMedica")}
                    disabled={!permissoes.periciaPermitida}
                  />
                </Field>
                <Field
                  label="Endereço"
                  hint={periciaData ? "Obrigatório quando há data" : undefined}
                >
                  <Input
                    {...register("enderecoPericiaMedica")}
                    disabled={!permissoes.periciaPermitida}
                  />
                </Field>
              </div>
            </section>

            {/* Avaliação social */}
            <section className="space-y-3">
              <div className="flex items-center justify-between">
                <h3 className="text-sm font-semibold text-text">Avaliação social</h3>
                {!permissoes.avaliacaoPermitida && (
                  <span className="text-xs text-muted-foreground flex items-center gap-1">
                    <Info className="size-3" /> Não permitido para esta modalidade
                  </span>
                )}
              </div>
              <div className="grid gap-3 sm:grid-cols-2">
                <Field label="Data e hora">
                  <Input
                    type="datetime-local"
                    {...register("avaliacaoSocial")}
                    disabled={!permissoes.avaliacaoPermitida}
                  />
                </Field>
                <Field
                  label="Endereço"
                  hint={avaliacaoData ? "Obrigatório quando há data" : undefined}
                >
                  <Input
                    {...register("enderecoAvaliacaoSocial")}
                    disabled={!permissoes.avaliacaoPermitida}
                  />
                </Field>
              </div>
            </section>

            {/* Concessão */}
            <section className="space-y-3">
              <div className="flex items-center justify-between">
                <h3 className="text-sm font-semibold text-text">Concessão</h3>
                {permissoes.concessaoObrigatoria && (
                  <span className="text-xs text-warning flex items-center gap-1">
                    <Info className="size-3" /> Data de concessão obrigatória pra aprovar
                  </span>
                )}
              </div>
              <div className="grid gap-3 sm:grid-cols-3">
                <Field label={`Data de concessão${permissoes.concessaoObrigatoria ? " *" : ""}`}>
                  <Input
                    type="date"
                    {...register("dataConcessao")}
                    disabled={status !== "APROVADO"}
                  />
                </Field>
                {permissoes.cessacaoVisivel && (
                  <Field
                    label={`Cessação${permissoes.cessacaoObrigatoria ? " *" : ""}`}
                    hint="Apenas modalidade 31"
                  >
                    <Input
                      type="date"
                      {...register("cessacao")}
                      disabled={status !== "APROVADO"}
                    />
                  </Field>
                )}
                {permissoes.valorConcedidoVisivel && (
                  <Field label="Valor concedido" hint="Apenas modalidade 31, após aprovação">
                    <Controller
                      control={control}
                      name="valorConcedido"
                      render={({ field }) => (
                        <MaskedInput
                          mask="money"
                          value={field.value}
                          onChange={field.onChange}
                          placeholder="R$ 0,00"
                          disabled={status !== "APROVADO"}
                        />
                      )}
                    />
                  </Field>
                )}
              </div>
            </section>
          </form>
        )}

        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)} disabled={submitting}>
            Cancelar
          </Button>
          <Button onClick={onSubmit} disabled={submitting || loading || !processoId}>
            {submitting && <Loader2 className="size-4 animate-spin" />}
            Salvar alterações
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

function Field({
  label,
  hint,
  children,
}: {
  label: string;
  hint?: string;
  children: React.ReactNode;
}) {
  return (
    <div className="space-y-1.5">
      <Label className="text-xs font-medium text-text-list">{label}</Label>
      {children}
      {hint && <p className="text-xs text-muted-foreground">{hint}</p>}
    </div>
  );
}
