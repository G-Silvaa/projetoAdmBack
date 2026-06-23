"use client";

import { Suspense, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Loader2, ShieldCheck } from "lucide-react";
import { toast } from "sonner";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { authService } from "@/core/services/auth.service";

const loginSchema = z.object({
  email: z.string().email("Informe um e-mail válido"),
  senha: z.string().min(6, "Mínimo de 6 caracteres"),
});

const registerSchema = z
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

function LoginFormFields() {
  const router = useRouter();
  const search = useSearchParams();
  const [submitting, setSubmitting] = useState(false);
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<z.infer<typeof loginSchema>>({
    resolver: zodResolver(loginSchema),
    defaultValues: { email: "", senha: "" },
  });

  const onSubmit = handleSubmit(async (values) => {
    setSubmitting(true);
    try {
      await authService.login(values);
      const redirectTo = search.get("redirectTo") || "/home";
      router.replace(redirectTo);
      router.refresh();
    } catch (err) {
      const message =
        (err as { response?: { data?: { message?: string } } })?.response?.data?.message ??
        "Não foi possível entrar com essas credenciais.";
      toast.error(message);
    } finally {
      setSubmitting(false);
    }
  });

  return (
    <form onSubmit={onSubmit} className="space-y-4">
      <div className="space-y-2">
        <Label htmlFor="email">E-mail</Label>
        <Input id="email" type="email" autoComplete="email" {...register("email")} />
        {errors.email && <p className="text-xs text-destructive">{errors.email.message}</p>}
      </div>
      <div className="space-y-2">
        <Label htmlFor="senha">Senha</Label>
        <Input id="senha" type="password" autoComplete="current-password" {...register("senha")} />
        {errors.senha && <p className="text-xs text-destructive">{errors.senha.message}</p>}
      </div>
      <Button type="submit" className="w-full" disabled={submitting}>
        {submitting ? <Loader2 className="size-4 animate-spin" /> : "Entrar"}
      </Button>
    </form>
  );
}

function RegisterFormFields() {
  const router = useRouter();
  const [submitting, setSubmitting] = useState(false);
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<z.infer<typeof registerSchema>>({
    resolver: zodResolver(registerSchema),
    defaultValues: { nome: "", email: "", senha: "", confirmacaoSenha: "" },
  });

  const onSubmit = handleSubmit(async ({ nome, email, senha }) => {
    setSubmitting(true);
    try {
      await authService.register({ nome, email, senha });
      toast.success("Conta criada com sucesso.");
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
    <form onSubmit={onSubmit} className="space-y-4">
      <div className="space-y-2">
        <Label htmlFor="nome">Nome completo</Label>
        <Input id="nome" {...register("nome")} />
        {errors.nome && <p className="text-xs text-destructive">{errors.nome.message}</p>}
      </div>
      <div className="space-y-2">
        <Label htmlFor="r-email">E-mail</Label>
        <Input id="r-email" type="email" autoComplete="email" {...register("email")} />
        {errors.email && <p className="text-xs text-destructive">{errors.email.message}</p>}
      </div>
      <div className="grid gap-3 sm:grid-cols-2">
        <div className="space-y-2">
          <Label htmlFor="r-senha">Senha</Label>
          <Input id="r-senha" type="password" autoComplete="new-password" {...register("senha")} />
          {errors.senha && <p className="text-xs text-destructive">{errors.senha.message}</p>}
        </div>
        <div className="space-y-2">
          <Label htmlFor="r-confirma">Confirmar senha</Label>
          <Input
            id="r-confirma"
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
        {submitting ? <Loader2 className="size-4 animate-spin" /> : "Criar conta"}
      </Button>
    </form>
  );
}

export default function LoginPage() {
  return (
    <div className="w-full max-w-md surface-card p-6 md:p-8">
      <div className="mb-6 flex items-center gap-3">
        <div className="grid h-10 w-10 place-items-center rounded-full bg-primary text-primary-foreground">
          <ShieldCheck className="size-5" />
        </div>
        <div>
          <p className="eyebrow">Arctech · Plataforma</p>
          <h1 className="font-display text-3xl text-primary leading-none">
            Entrar no painel
          </h1>
        </div>
      </div>
      <Tabs defaultValue="login" className="w-full">
        <TabsList className="grid w-full grid-cols-2 mb-6">
          <TabsTrigger value="login">Acessar</TabsTrigger>
          <TabsTrigger value="register">Criar conta</TabsTrigger>
        </TabsList>
        <TabsContent value="login">
          <Suspense fallback={null}>
            <LoginFormFields />
          </Suspense>
        </TabsContent>
        <TabsContent value="register">
          <RegisterFormFields />
        </TabsContent>
      </Tabs>
      <p className="mt-6 text-center text-xs text-muted-foreground">
        Assistência previdenciária · acessos controlados por nível de usuário
      </p>
    </div>
  );
}
