"use client";

import { Suspense, useEffect, useState } from "react";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Loader2, ShieldCheck } from "lucide-react";
import { toast } from "sonner";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { authService } from "@/core/services/auth.service";
import { assinaturaService } from "@/core/services/assinatura.service";
import type { PlanoApi } from "@/core/types/auth";

const schema = z
  .object({
    nome: z.string().min(1, "Informe seu nome").max(120),
    email: z.string().email("Informe um e-mail válido"),
    senha: z.string().min(6, "Mínimo de 6 caracteres"),
    confirmacaoSenha: z.string().min(6),
  })
  .refine((data) => data.senha === data.confirmacaoSenha, {
    message: "As senhas não conferem",
    path: ["confirmacaoSenha"],
  });

function formatPreco(preco: number) {
  return preco.toLocaleString("pt-BR", {
    style: "currency",
    currency: "BRL",
    minimumFractionDigits: 0,
  });
}

function CadastroForm() {
  const router = useRouter();
  const search = useSearchParams();
  const planoCodigo = search.get("plano") ?? undefined;
  const [submitting, setSubmitting] = useState(false);
  const [plano, setPlano] = useState<PlanoApi | null>(null);

  useEffect(() => {
    if (!planoCodigo) return;
    assinaturaService
      .planos()
      .then((planos) => setPlano(planos.find((p) => p.codigo === planoCodigo) ?? null))
      .catch(() => setPlano(null));
  }, [planoCodigo]);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<z.infer<typeof schema>>({
    resolver: zodResolver(schema),
    defaultValues: { nome: "", email: "", senha: "", confirmacaoSenha: "" },
  });

  const onSubmit = handleSubmit(async ({ nome, email, senha }) => {
    setSubmitting(true);
    try {
      await authService.register({ nome, email, senha, planoCodigo });
      toast.success("Conta criada! Seu teste grátis de 7 dias começou.");
      router.replace("/home");
      router.refresh();
    } catch (err) {
      const message =
        (err as { response?: { data?: { message?: string } } })?.response?.data?.message ??
        "Não foi possível concluir o cadastro.";
      toast.error(message);
    } finally {
      setSubmitting(false);
    }
  });

  return (
    <div className="w-full max-w-md surface-card p-6 md:p-8">
      <div className="mb-6 flex items-center gap-3">
        <div className="grid h-10 w-10 place-items-center rounded-full bg-primary text-primary-foreground">
          <ShieldCheck className="size-5" />
        </div>
        <div>
          <p className="eyebrow">Arctech · Criar conta</p>
          <h1 className="font-display text-2xl text-primary leading-none">
            Comece seu teste grátis
          </h1>
        </div>
      </div>

      {plano && (
        <div className="mb-5 flex items-center justify-between rounded-md border border-secondary bg-secondary-soft px-4 py-3">
          <div>
            <p className="text-xs text-muted-foreground">Plano selecionado</p>
            <p className="font-medium text-text">{plano.nome}</p>
          </div>
          <p className="font-display text-lg text-primary">
            {formatPreco(plano.preco)}
            <span className="text-xs text-muted-foreground">/mês</span>
          </p>
        </div>
      )}

      <p className="mb-5 text-sm text-text-list">
        7 dias grátis · sem cartão de crédito. Você só paga se decidir continuar.
      </p>

      <form onSubmit={onSubmit} className="space-y-4">
        <div className="space-y-2">
          <Label htmlFor="nome">Nome completo</Label>
          <Input id="nome" {...register("nome")} />
          {errors.nome && <p className="text-xs text-destructive">{errors.nome.message}</p>}
        </div>
        <div className="space-y-2">
          <Label htmlFor="email">E-mail</Label>
          <Input id="email" type="email" autoComplete="email" {...register("email")} />
          {errors.email && <p className="text-xs text-destructive">{errors.email.message}</p>}
        </div>
        <div className="grid gap-3 sm:grid-cols-2">
          <div className="space-y-2">
            <Label htmlFor="senha">Senha</Label>
            <Input id="senha" type="password" autoComplete="new-password" {...register("senha")} />
            {errors.senha && <p className="text-xs text-destructive">{errors.senha.message}</p>}
          </div>
          <div className="space-y-2">
            <Label htmlFor="confirma">Confirmar senha</Label>
            <Input
              id="confirma"
              type="password"
              autoComplete="new-password"
              {...register("confirmacaoSenha")}
            />
            {errors.confirmacaoSenha && (
              <p className="text-xs text-destructive">{errors.confirmacaoSenha.message}</p>
            )}
          </div>
        </div>
        <Button type="submit" className="w-full" disabled={submitting}>
          {submitting ? <Loader2 className="size-4 animate-spin" /> : "Criar conta e começar"}
        </Button>
      </form>

      <p className="mt-6 text-center text-sm text-muted-foreground">
        Já tem conta?{" "}
        <Link href="/login" className="font-medium text-secondary hover:underline">
          Entrar
        </Link>
      </p>
    </div>
  );
}

export default function CadastroPage() {
  return (
    <Suspense fallback={null}>
      <CadastroForm />
    </Suspense>
  );
}
