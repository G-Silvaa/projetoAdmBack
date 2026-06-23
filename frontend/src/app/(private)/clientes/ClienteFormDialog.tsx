"use client";

import { useEffect, useState } from "react";
import { useForm, Controller } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
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
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { MaskedInput } from "@/components/shared/MaskedInput";

import { clientesService } from "@/core/services/clientes.service";
import { fetchCep } from "@/core/helpers/cep";
import { unformatCPF } from "@/core/helpers/format";
import { beneficiosOptions } from "@/core/consts/beneficios";
import type { ICliente } from "@/core/types/domain";

const isoFromBR = (br?: string) => {
  if (!br) return undefined;
  const m = /^(\d{2})\/(\d{2})\/(\d{4})$/.exec(br);
  if (m) return `${m[3]}-${m[2]}-${m[1]}`;
  return br;
};

const brFromIso = (iso?: string | null) => {
  if (!iso) return "";
  return iso.split("T")[0];
};

function moneyToNumber(formatted: string): number {
  const digits = formatted.replace(/\D/g, "");
  if (!digits) return 0;
  return Number(digits) / 100;
}

const onlyDigits = (v?: string | null) => (v ?? "").replace(/\D/g, "");

const contatoSchema = z.object({
  nome: z.string().min(1, "Informe o nome"),
  email: z.string().email("E-mail inválido").or(z.literal("")),
  telefone: z
    .string()
    .min(1, "Informe o telefone")
    .refine((v) => onlyDigits(v).length >= 10, "Telefone incompleto"),
});

const enderecoSchema = z.object({
  cep: z
    .string()
    .min(1, "Informe o CEP")
    .refine((v) => onlyDigits(v).length === 8, "CEP precisa ter 8 dígitos"),
  logradouro: z.string().min(1, "Informe o logradouro"),
  complemento: z.string().optional(),
  bairro: z.string().min(1, "Informe o bairro"),
  cidade: z.string().min(1, "Informe a cidade"),
});

const representanteSchema = z.object({
  nome: z.string().min(1),
  email: z.string().email().or(z.literal("")),
  telefone: z.string().min(1),
  parentesco: z.string().min(1, "Informe o grau de parentesco"),
  cpf: z.string().refine((v) => onlyDigits(v).length === 11, "CPF inválido"),
  rg: z.string().optional(),
  nascimento: z.string().min(1, "Informe a data de nascimento"),
});

const baseClienteSchema = z.object({
  contato: contatoSchema,
  cpf: z.string().refine((v) => onlyDigits(v).length === 11, "CPF inválido"),
  rg: z.string().optional(),
  nascimento: z.string().min(1, "Informe a data de nascimento"),
  endereco: enderecoSchema,
  temRepresentante: z.boolean(),
  representante: representanteSchema.optional(),
});

const createClienteSchema = baseClienteSchema.extend({
  beneficio: z.string().min(1, "Selecione o benefício"),
  valor: z.string().min(1, "Informe o valor"),
});

const editClienteSchema = baseClienteSchema;

type CreateValues = z.infer<typeof createClienteSchema>;
type EditValues = z.infer<typeof editClienteSchema>;

const defaultCreate: CreateValues = {
  contato: { nome: "", email: "", telefone: "" },
  cpf: "",
  rg: "",
  nascimento: "",
  endereco: { cep: "", logradouro: "", complemento: "", bairro: "", cidade: "" },
  temRepresentante: false,
  representante: undefined,
  beneficio: "",
  valor: "",
};

type Mode = "create" | "edit";

interface ClienteFormDialogProps {
  open: boolean;
  mode: Mode;
  clienteId?: number;
  onOpenChange: (open: boolean) => void;
  onSaved?: () => void;
}

export function ClienteFormDialog({
  open,
  mode,
  clienteId,
  onOpenChange,
  onSaved,
}: ClienteFormDialogProps) {
  const isCreate = mode === "create";
  const [submitting, setSubmitting] = useState(false);
  const [loadingExisting, setLoadingExisting] = useState(false);

  const {
    register,
    handleSubmit,
    control,
    watch,
    setValue,
    reset,
    formState: { errors },
  } = useForm<CreateValues>({
    resolver: zodResolver(isCreate ? createClienteSchema : (editClienteSchema as unknown as typeof createClienteSchema)),
    defaultValues: defaultCreate,
  });

  const temRepresentante = watch("temRepresentante");
  const cepValue = watch("endereco.cep");
  const cpfValue = watch("cpf");

  // Auto-load existing cliente (edit mode)
  useEffect(() => {
    if (!open || !clienteId || isCreate) return;
    setLoadingExisting(true);
    clientesService
      .porId(clienteId)
      .then((cliente) => {
        if (!cliente) return;
        fillFromCliente(cliente);
      })
      .catch(() => toast.error("Erro ao carregar cliente."))
      .finally(() => setLoadingExisting(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [open, clienteId, isCreate]);

  // Reset on open/close
  useEffect(() => {
    if (!open) {
      reset(defaultCreate);
    }
  }, [open, reset]);

  // CPF lookup in create mode → preenche se já existir
  useEffect(() => {
    if (!isCreate || !open) return;
    const digits = onlyDigits(cpfValue);
    if (digits.length !== 11) return;
    let cancelled = false;
    const timer = setTimeout(async () => {
      try {
        const result = await clientesService.filtrar({ cpf: digits });
        const existing = result.content?.[0];
        if (existing && !cancelled) {
          toast.info("Cliente já cadastrado — campos preenchidos. Edite e salve para atualizar.");
          fillFromCliente(existing);
        }
      } catch {
        /* silent */
      }
    }, 350);
    return () => {
      cancelled = true;
      clearTimeout(timer);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [cpfValue, isCreate, open]);

  // CEP lookup → preenche endereço
  useEffect(() => {
    if (!open) return;
    const digits = onlyDigits(cepValue);
    if (digits.length !== 8) return;
    let cancelled = false;
    const timer = setTimeout(async () => {
      const data = await fetchCep(digits);
      if (data && !cancelled) {
        if (data.logradouro) setValue("endereco.logradouro", data.logradouro);
        if (data.complemento) setValue("endereco.complemento", data.complemento);
        if (data.bairro) setValue("endereco.bairro", data.bairro);
        if (data.localidade) setValue("endereco.cidade", data.localidade);
      }
    }, 350);
    return () => {
      cancelled = true;
      clearTimeout(timer);
    };
  }, [cepValue, open, setValue]);

  function fillFromCliente(cliente: ICliente) {
    const rep = cliente.representante;
    reset({
      ...defaultCreate,
      contato: {
        nome: cliente.contato?.nome ?? "",
        email: cliente.contato?.email ?? "",
        telefone: cliente.contato?.telefone ?? "",
      },
      cpf: cliente.cpf ?? "",
      rg: cliente.rg ?? "",
      nascimento: brFromIso(cliente.nascimento),
      endereco: {
        cep: (cliente as unknown as { endereco?: Record<string, string> }).endereco?.cep ?? "",
        logradouro:
          (cliente as unknown as { endereco?: Record<string, string> }).endereco?.logradouro ?? "",
        complemento:
          (cliente as unknown as { endereco?: Record<string, string> }).endereco?.complemento ??
          "",
        bairro: (cliente as unknown as { endereco?: Record<string, string> }).endereco?.bairro ?? "",
        cidade: (cliente as unknown as { endereco?: Record<string, string> }).endereco?.cidade ?? "",
      },
      temRepresentante: !!rep?.id,
      representante: rep?.id
        ? {
            nome: rep.nome ?? rep.contato?.nome ?? "",
            email: rep.email ?? rep.contato?.email ?? "",
            telefone: rep.telefone ?? rep.contato?.telefone ?? "",
            parentesco: rep.parentesco ?? "",
            cpf: rep.cpf ?? "",
            rg: rep.rg ?? "",
            nascimento: brFromIso(rep.nascimento),
          }
        : undefined,
    });
  }

  const onSubmit = handleSubmit(async (values) => {
    setSubmitting(true);
    try {
      const payload = {
        contato: {
          nome: values.contato.nome,
          email: values.contato.email || null,
          telefone: onlyDigits(values.contato.telefone),
        },
        cpf: onlyDigits(values.cpf),
        rg: values.rg ? values.rg : null,
        nascimento: isoFromBR(values.nascimento) ?? values.nascimento,
        endereco: {
          cep: onlyDigits(values.endereco.cep),
          logradouro: values.endereco.logradouro,
          complemento: values.endereco.complemento || null,
          bairro: values.endereco.bairro,
          cidade: values.endereco.cidade,
        },
        representante:
          values.temRepresentante && values.representante
            ? {
                contato: {
                  nome: values.representante.nome,
                  email: values.representante.email || null,
                  telefone: onlyDigits(values.representante.telefone),
                },
                parentesco: values.representante.parentesco,
                cpf: onlyDigits(values.representante.cpf),
                rg: values.representante.rg ? values.representante.rg : null,
                nascimento: isoFromBR(values.representante.nascimento) ?? values.representante.nascimento,
              }
            : null,
      };

      if (isCreate) {
        const cliente = await clientesService.criar(payload);
        // contrato inicial
        await clientesService.associarBeneficio({
          cliente: { id: (cliente as ICliente).id ?? (cliente as { id: number }).id },
          beneficio: values.beneficio,
          valor: moneyToNumber(values.valor),
        });
        toast.success("Cliente e contrato inicial criados.");
      } else if (clienteId) {
        await clientesService.atualizar(clienteId, payload);
        toast.success("Cliente atualizado.");
      }

      onSaved?.();
      onOpenChange(false);
    } catch (err) {
      const msg =
        (err as { response?: { data?: { message?: string } } })?.response?.data?.message ??
        "Não foi possível salvar o cliente.";
      toast.error(msg);
    } finally {
      setSubmitting(false);
    }
  });

  return (
    <Dialog open={open} onOpenChange={(o) => !submitting && onOpenChange(o)}>
      <DialogContent className="max-w-3xl">
        <DialogHeader>
          <DialogTitle>
            {isCreate ? "Cadastrar cliente" : "Editar cliente"}
          </DialogTitle>
          <DialogDescription>
            {isCreate
              ? "Dados pessoais, endereço, representante (opcional) e contrato inicial."
              : "Atualize dados do cadastro. O contrato é gerenciado separadamente."}
          </DialogDescription>
        </DialogHeader>

        {loadingExisting ? (
          <div className="grid place-items-center py-10">
            <Loader2 className="size-5 animate-spin text-primary" />
          </div>
        ) : (
          <form onSubmit={onSubmit} className="space-y-6 max-h-[70vh] overflow-y-auto pr-1">
            {/* Dados pessoais */}
            <section className="space-y-3">
              <h3 className="text-sm font-semibold text-text">Dados pessoais</h3>
              <div className="grid gap-3 sm:grid-cols-2">
                <Field label="CPF" error={errors.cpf?.message}>
                  <Controller
                    control={control}
                    name="cpf"
                    render={({ field }) => (
                      <MaskedInput
                        mask="cpf"
                        value={field.value}
                        onChange={field.onChange}
                        disabled={!isCreate}
                        placeholder="000.000.000-00"
                      />
                    )}
                  />
                </Field>
                <Field label="Nome completo" error={errors.contato?.nome?.message}>
                  <Input {...register("contato.nome")} placeholder="Nome do cliente" />
                </Field>
              </div>
              <div className="grid gap-3 sm:grid-cols-3">
                <Field label="RG" error={errors.rg?.message}>
                  <Controller
                    control={control}
                    name="rg"
                    render={({ field }) => (
                      <MaskedInput
                        mask="rg"
                        value={field.value ?? ""}
                        onChange={field.onChange}
                      />
                    )}
                  />
                </Field>
                <Field label="Nascimento" error={errors.nascimento?.message}>
                  <Input type="date" {...register("nascimento")} />
                </Field>
                <Field label="Telefone" error={errors.contato?.telefone?.message}>
                  <Controller
                    control={control}
                    name="contato.telefone"
                    render={({ field }) => (
                      <MaskedInput
                        mask="telefone"
                        value={field.value}
                        onChange={field.onChange}
                        placeholder="(00) 00000-0000"
                      />
                    )}
                  />
                </Field>
              </div>
              <Field label="E-mail" error={errors.contato?.email?.message}>
                <Input type="email" {...register("contato.email")} placeholder="opcional" />
              </Field>
            </section>

            {/* Endereço */}
            <section className="space-y-3">
              <h3 className="text-sm font-semibold text-text">Endereço</h3>
              <div className="grid gap-3 sm:grid-cols-[1fr_2fr]">
                <Field label="CEP" error={errors.endereco?.cep?.message}>
                  <Controller
                    control={control}
                    name="endereco.cep"
                    render={({ field }) => (
                      <MaskedInput
                        mask="cep"
                        value={field.value}
                        onChange={field.onChange}
                        placeholder="00000-000"
                      />
                    )}
                  />
                </Field>
                <Field label="Logradouro" error={errors.endereco?.logradouro?.message}>
                  <Input {...register("endereco.logradouro")} />
                </Field>
              </div>
              <div className="grid gap-3 sm:grid-cols-3">
                <Field label="Complemento" error={errors.endereco?.complemento?.message}>
                  <Input {...register("endereco.complemento")} placeholder="opcional" />
                </Field>
                <Field label="Bairro" error={errors.endereco?.bairro?.message}>
                  <Input {...register("endereco.bairro")} />
                </Field>
                <Field label="Cidade" error={errors.endereco?.cidade?.message}>
                  <Input {...register("endereco.cidade")} />
                </Field>
              </div>
            </section>

            {/* Representante */}
            <section className="space-y-3">
              <div className="flex items-center justify-between">
                <h3 className="text-sm font-semibold text-text">Representante legal</h3>
                <label className="flex items-center gap-2 text-sm text-text-list">
                  <input
                    type="checkbox"
                    className="h-4 w-4 rounded border-border accent-primary"
                    checked={temRepresentante}
                    onChange={(e) => setValue("temRepresentante", e.target.checked)}
                  />
                  Tem representante
                </label>
              </div>
              {temRepresentante && (
                <div className="space-y-3 rounded-md border border-border p-3 bg-background/40">
                  <div className="grid gap-3 sm:grid-cols-2">
                    <Field
                      label="CPF do representante"
                      error={errors.representante?.cpf?.message}
                    >
                      <Controller
                        control={control}
                        name="representante.cpf"
                        render={({ field }) => (
                          <MaskedInput
                            mask="cpf"
                            value={field.value ?? ""}
                            onChange={field.onChange}
                          />
                        )}
                      />
                    </Field>
                    <Field label="Nome" error={errors.representante?.nome?.message}>
                      <Input {...register("representante.nome")} />
                    </Field>
                  </div>
                  <div className="grid gap-3 sm:grid-cols-3">
                    <Field label="Parentesco" error={errors.representante?.parentesco?.message}>
                      <Input {...register("representante.parentesco")} placeholder="Mãe, filho..." />
                    </Field>
                    <Field label="RG">
                      <Controller
                        control={control}
                        name="representante.rg"
                        render={({ field }) => (
                          <MaskedInput
                            mask="rg"
                            value={field.value ?? ""}
                            onChange={field.onChange}
                          />
                        )}
                      />
                    </Field>
                    <Field
                      label="Nascimento"
                      error={errors.representante?.nascimento?.message}
                    >
                      <Input type="date" {...register("representante.nascimento")} />
                    </Field>
                  </div>
                  <div className="grid gap-3 sm:grid-cols-2">
                    <Field label="Telefone" error={errors.representante?.telefone?.message}>
                      <Controller
                        control={control}
                        name="representante.telefone"
                        render={({ field }) => (
                          <MaskedInput
                            mask="telefone"
                            value={field.value ?? ""}
                            onChange={field.onChange}
                          />
                        )}
                      />
                    </Field>
                    <Field label="E-mail" error={errors.representante?.email?.message}>
                      <Input type="email" {...register("representante.email")} placeholder="opcional" />
                    </Field>
                  </div>
                </div>
              )}
            </section>

            {/* Contrato inicial — só no create */}
            {isCreate && (
              <section className="space-y-3">
                <h3 className="text-sm font-semibold text-text">Contrato inicial</h3>
                <p className="text-xs text-muted-foreground">
                  Ao salvar, é criado também um contrato com o benefício escolhido e o processo
                  aguardando análise.
                </p>
                <div className="grid gap-3 sm:grid-cols-2">
                  <Field label="Benefício" error={(errors as { beneficio?: { message?: string } }).beneficio?.message}>
                    <Controller
                      control={control}
                      name="beneficio"
                      render={({ field }) => (
                        <Select value={field.value} onValueChange={field.onChange}>
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
                  </Field>
                  <Field label="Valor do contrato" error={(errors as { valor?: { message?: string } }).valor?.message}>
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
                  </Field>
                </div>
              </section>
            )}
          </form>
        )}

        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)} disabled={submitting}>
            Cancelar
          </Button>
          <Button onClick={onSubmit} disabled={submitting || loadingExisting}>
            {submitting && <Loader2 className="size-4 animate-spin" />}
            {isCreate ? "Cadastrar cliente" : "Salvar alterações"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

function Field({
  label,
  error,
  children,
}: {
  label: string;
  error?: string;
  children: React.ReactNode;
}) {
  return (
    <div className="space-y-1.5">
      <Label className="text-xs font-medium text-text-list">{label}</Label>
      {children}
      {error && <p className="text-xs text-destructive">{error}</p>}
    </div>
  );
}
