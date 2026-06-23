"use client";

import { useEffect, useMemo, useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Pencil, Search, ShieldOff, ShieldCheck, Loader2 } from "lucide-react";
import { toast } from "sonner";

import { PageHeader } from "@/components/layout/PageHeader";
import { SectionCard } from "@/components/shared/SectionCard";
import { StatCard } from "@/components/layout/StatCard";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Badge } from "@/components/ui/badge";

import { useAuth } from "@/core/auth/AuthProvider";
import { usuariosService } from "@/core/services/usuarios.service";
import type {
  AuthUser,
  NivelUsuario,
  NivelUsuarioOption,
  UsuarioCreatePayload,
  UsuarioUpdatePayload,
} from "@/core/types/auth";
import { NIVEL_LABELS } from "@/core/types/auth";

const userSchema = z.object({
  nome: z.string().min(1, "Informe o nome").max(120),
  email: z.string().email("E-mail inválido"),
  nivel: z.enum(["ADMINISTRADOR", "GESTOR", "OPERADOR", "FINANCEIRO", "CONSULTA"]),
  senha: z.string().optional(),
  ativo: z.boolean(),
});

type UserFormValues = z.infer<typeof userSchema>;

const defaultValues: UserFormValues = {
  nome: "",
  email: "",
  nivel: "OPERADOR",
  senha: "",
  ativo: true,
};

export default function UsuariosPage() {
  const { user: currentUser, refresh } = useAuth();
  const [usuarios, setUsuarios] = useState<AuthUser[]>([]);
  const [niveis, setNiveis] = useState<NivelUsuarioOption[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [editingUserId, setEditingUserId] = useState<number | null>(null);
  const [search, setSearch] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const {
    register,
    handleSubmit,
    reset,
    setValue,
    watch,
    formState: { errors },
  } = useForm<UserFormValues>({
    resolver: zodResolver(userSchema),
    defaultValues,
  });

  const watchNivel = watch("nivel");
  const watchAtivo = watch("ativo");

  const load = async () => {
    setIsLoading(true);
    try {
      const [list, opts] = await Promise.all([
        usuariosService.listar(),
        usuariosService.listarNiveis().catch(() => []),
      ]);
      setUsuarios(list);
      setNiveis(
        opts.length > 0
          ? opts
          : (Object.entries(NIVEL_LABELS).map(([value, label]) => ({
              value: value as NivelUsuario,
              label,
            })) as NivelUsuarioOption[]),
      );
    } catch {
      toast.error("Não foi possível carregar os usuários.");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const filtered = useMemo(() => {
    const term = search.trim().toLowerCase();
    if (!term) return usuarios;
    return usuarios.filter(
      (u) =>
        u.nome.toLowerCase().includes(term) ||
        u.email.toLowerCase().includes(term) ||
        (NIVEL_LABELS[u.nivel] ?? "").toLowerCase().includes(term),
    );
  }, [usuarios, search]);

  const stats = useMemo(() => {
    return {
      total: usuarios.length,
      administradores: usuarios.filter((u) => u.nivel === "ADMINISTRADOR").length,
      ativos: usuarios.filter((u) => u.ativo).length,
      inativos: usuarios.filter((u) => !u.ativo).length,
    };
  }, [usuarios]);

  const resetForm = () => {
    setEditingUserId(null);
    reset(defaultValues);
  };

  const submit = handleSubmit(async (values) => {
    if (!editingUserId && (!values.senha || values.senha.length < 6)) {
      toast.error("A senha precisa ter ao menos 6 caracteres.");
      return;
    }
    setSubmitting(true);
    try {
      if (editingUserId) {
        const payload: UsuarioUpdatePayload = {
          nome: values.nome.trim(),
          email: values.email.trim(),
          nivel: values.nivel,
          ativo: values.ativo,
        };
        if (values.senha?.trim()) payload.senha = values.senha.trim();
        await usuariosService.atualizar(editingUserId, payload);
        toast.success("Usuário atualizado.");
        if (editingUserId === currentUser?.id) await refresh();
      } else {
        const payload: UsuarioCreatePayload = {
          nome: values.nome.trim(),
          email: values.email.trim(),
          nivel: values.nivel,
          senha: values.senha!.trim(),
        };
        await usuariosService.criar(payload);
        toast.success("Usuário criado.");
      }
      resetForm();
      load();
    } catch (err) {
      const msg =
        (err as { response?: { data?: { message?: string } } })?.response?.data?.message ??
        "Não foi possível salvar o usuário.";
      toast.error(msg);
    } finally {
      setSubmitting(false);
    }
  });

  const editar = (usuario: AuthUser) => {
    setEditingUserId(usuario.id);
    reset({
      nome: usuario.nome,
      email: usuario.email,
      nivel: usuario.nivel,
      senha: "",
      ativo: usuario.ativo,
    });
  };

  const toggleStatus = async (usuario: AuthUser) => {
    if (usuario.id === currentUser?.id) {
      toast.error("Você não pode desativar sua própria conta por aqui.");
      return;
    }
    try {
      await usuariosService.atualizar(usuario.id, { ativo: !usuario.ativo });
      toast.success(usuario.ativo ? "Usuário desativado." : "Usuário reativado.");
      load();
    } catch {
      toast.error("Não foi possível atualizar o status.");
    }
  };

  return (
    <div className="page-shell">
      <PageHeader
        eyebrow="Administração"
        title="Usuários"
        description="Gestão de acessos e níveis. Apenas administradores podem criar, editar ou alternar o status de outros usuários."
      />

      <div className="stats-grid">
        <StatCard label="Usuários cadastrados" value={stats.total} />
        <StatCard label="Administradores" value={stats.administradores} />
        <StatCard label="Ativos" value={stats.ativos} highlight />
        <StatCard label="Inativos" value={stats.inativos} />
      </div>

      <div className="grid gap-6 lg:grid-cols-[1.1fr_1fr]">
        <SectionCard
          title={editingUserId ? "Editar usuário" : "Novo usuário"}
          description={
            editingUserId
              ? "Atualize os dados e o nível de acesso. Deixe a senha em branco para mantê-la."
              : "Cadastre um novo usuário com nível de acesso."
          }
          actions={
            editingUserId ? (
              <Button variant="outline" onClick={resetForm}>
                Cancelar
              </Button>
            ) : undefined
          }
        >
          <form onSubmit={submit} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="u-nome">Nome completo</Label>
              <Input id="u-nome" {...register("nome")} />
              {errors.nome && <p className="text-xs text-destructive">{errors.nome.message}</p>}
            </div>
            <div className="space-y-2">
              <Label htmlFor="u-email">E-mail</Label>
              <Input id="u-email" type="email" {...register("email")} />
              {errors.email && <p className="text-xs text-destructive">{errors.email.message}</p>}
            </div>
            <div className="grid gap-3 sm:grid-cols-2">
              <div className="space-y-2">
                <Label>Nível</Label>
                <Select
                  value={watchNivel}
                  onValueChange={(v) => setValue("nivel", v as NivelUsuario, { shouldDirty: true })}
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {niveis.map((n) => (
                      <SelectItem key={n.value} value={n.value}>
                        {n.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className="space-y-2">
                <Label htmlFor="u-senha">
                  Senha {editingUserId ? <span className="text-muted-foreground">(opcional)</span> : null}
                </Label>
                <Input id="u-senha" type="password" autoComplete="new-password" {...register("senha")} />
              </div>
            </div>
            {editingUserId && (
              <label className="flex items-center gap-2 text-sm text-primary">
                <input
                  type="checkbox"
                  className="h-4 w-4 rounded border-border accent-primary"
                  checked={watchAtivo}
                  onChange={(e) => setValue("ativo", e.target.checked, { shouldDirty: true })}
                />
                Conta ativa
              </label>
            )}
            <Button type="submit" disabled={submitting} className="w-full">
              {submitting ? (
                <Loader2 className="size-4 animate-spin" />
              ) : editingUserId ? (
                "Salvar alterações"
              ) : (
                "Cadastrar usuário"
              )}
            </Button>
          </form>
        </SectionCard>

        <SectionCard title="Equipe">
          <div className="mb-3 max-w-sm space-y-1.5">
            <Label htmlFor="usr-search" className="text-xs font-medium text-text-list">
              Buscar usuário
            </Label>
            <div className="relative">
              <Search className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
              <Input
                id="usr-search"
                placeholder="Nome, e-mail ou nível"
                className="pl-9"
                value={search}
                onChange={(e) => setSearch(e.target.value)}
              />
            </div>
          </div>
          {isLoading ? (
            <div className="grid place-items-center py-10">
              <Loader2 className="size-5 animate-spin text-primary" />
            </div>
          ) : filtered.length === 0 ? (
            <p className="py-8 text-center text-sm text-muted-foreground">
              Nenhum usuário encontrado.
            </p>
          ) : (
            <ul className="space-y-2">
              {filtered.map((u) => (
                <li
                  key={u.id}
                  className="flex flex-col gap-2 rounded-2xl border border-border bg-background/60 p-3 sm:flex-row sm:items-center sm:justify-between"
                >
                  <div>
                    <p className="flex items-center gap-2 text-sm font-semibold text-primary">
                      {u.nome}
                      <Badge
                        variant="outline"
                        className={
                          u.ativo
                            ? "border-success/40 text-success"
                            : "border-destructive/40 text-destructive"
                        }
                      >
                        {u.ativo ? "Ativo" : "Inativo"}
                      </Badge>
                    </p>
                    <p className="text-xs text-muted-foreground">{u.email}</p>
                    <p className="text-[0.7rem] uppercase tracking-wider text-secondary">
                      {NIVEL_LABELS[u.nivel] ?? u.nivel}
                    </p>
                  </div>
                  <div className="flex gap-2">
                    <Button size="sm" variant="outline" onClick={() => editar(u)}>
                      <Pencil className="size-4" />
                      Editar
                    </Button>
                    <Button
                      size="sm"
                      variant={u.ativo ? "outline" : "default"}
                      onClick={() => toggleStatus(u)}
                      disabled={u.id === currentUser?.id}
                    >
                      {u.ativo ? (
                        <>
                          <ShieldOff className="size-4" /> Desativar
                        </>
                      ) : (
                        <>
                          <ShieldCheck className="size-4" /> Ativar
                        </>
                      )}
                    </Button>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </SectionCard>
      </div>
    </div>
  );
}
