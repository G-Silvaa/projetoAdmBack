"use client";

import { useCallback, useEffect, useState } from "react";
import { CalendarClock, CheckCircle2, Loader2, MessageCircle, QrCode, TriangleAlert } from "lucide-react";

import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { assinaturaService } from "@/core/services/assinatura.service";
import { whatsappLink } from "@/core/consts/landing";
import type { Assinatura } from "@/core/types/auth";
import { PagamentoPixDialog } from "./PagamentoPixDialog";

function formatPreco(preco: number) {
  return preco.toLocaleString("pt-BR", {
    style: "currency",
    currency: "BRL",
    minimumFractionDigits: 0,
  });
}

function formatData(value: string | null) {
  if (!value) return "—";
  return new Date(value).toLocaleDateString("pt-BR");
}

export default function AssinaturaPage() {
  const [assinatura, setAssinatura] = useState<Assinatura | null>(null);
  const [loading, setLoading] = useState(true);
  const [pixOpen, setPixOpen] = useState(false);

  const carregar = useCallback(() => {
    return assinaturaService
      .minha()
      .then(setAssinatura)
      .catch(() => setAssinatura(null));
  }, []);

  useEffect(() => {
    carregar().finally(() => setLoading(false));
  }, [carregar]);

  if (loading) {
    return (
      <div className="grid place-items-center py-20">
        <Loader2 className="size-6 animate-spin text-primary" />
      </div>
    );
  }

  if (!assinatura) {
    return (
      <div className="page-shell">
        <p className="text-text-list">Não foi possível carregar sua assinatura.</p>
      </div>
    );
  }

  const bloqueada = !assinatura.permiteAcesso;
  const emTrial = assinatura.status === "TRIAL";
  const ctaMensagem = `Olá! Quero ativar minha assinatura do Arctech (plano ${assinatura.planoNome}).`;

  return (
    <div className="page-shell max-w-3xl">
      <div>
        <p className="eyebrow">Minha assinatura</p>
        <h1 className="mt-1 font-display text-2xl text-primary">Plano e cobrança</h1>
      </div>

      {bloqueada && (
        <div className="flex items-start gap-3 rounded-lg border border-destructive bg-destructive/10 p-4">
          <TriangleAlert className="mt-0.5 size-5 shrink-0 text-destructive" />
          <div>
            <p className="font-medium text-text">Seu acesso está bloqueado</p>
            <p className="text-sm text-text-list">
              Sua assinatura está {assinatura.statusLabel.toLowerCase()}. Regularize para voltar a
              usar o sistema.
            </p>
          </div>
        </div>
      )}

      <div className="surface-card p-6">
        <div className="flex items-start justify-between">
          <div>
            <p className="text-sm text-muted-foreground">Plano atual</p>
            <p className="font-display text-2xl text-text">{assinatura.planoNome}</p>
          </div>
          <Badge className={bloqueada ? "bg-destructive" : emTrial ? "bg-warning" : "bg-success"}>
            {assinatura.statusLabel}
          </Badge>
        </div>

        <div className="mt-4 flex items-end gap-1">
          <span className="font-display text-3xl text-primary">
            {formatPreco(assinatura.preco)}
          </span>
          <span className="mb-1 text-sm text-muted-foreground">/mês</span>
        </div>

        <div className="mt-5 grid gap-3 sm:grid-cols-2">
          {emTrial && assinatura.diasRestantes != null && (
            <div className="flex items-center gap-2 rounded-md bg-accent px-3 py-2 text-sm">
              <CalendarClock className="size-4 text-secondary" />
              Teste grátis: <strong>{assinatura.diasRestantes} dia(s)</strong> restante(s)
            </div>
          )}
          <div className="flex items-center gap-2 rounded-md bg-accent px-3 py-2 text-sm">
            <CheckCircle2 className="size-4 text-success" />
            {emTrial ? "Teste até" : "Válida até"} {formatData(assinatura.vencimento)}
          </div>
        </div>

        <div className="mt-4 text-sm text-text-list">
          Limites do plano:{" "}
          {assinatura.maxUsuarios == null ? "usuários ilimitados" : `${assinatura.maxUsuarios} usuário(s)`}
          {" · "}
          {assinatura.maxClientes == null ? "clientes ilimitados" : `${assinatura.maxClientes} clientes`}
        </div>

        <div className="mt-6 border-t border-border pt-5">
          <div className="flex flex-col gap-3 sm:flex-row">
            <Button size="lg" onClick={() => setPixOpen(true)}>
              <QrCode className="size-4" />
              Pagar com PIX
            </Button>
            <Button asChild size="lg" variant="outline">
              <a
                href={whatsappLink(ctaMensagem)}
                target="_blank"
                rel="noopener noreferrer"
              >
                <MessageCircle className="size-4" />
                Falar no WhatsApp
              </a>
            </Button>
          </div>
          <p className="mt-2 text-xs text-muted-foreground">
            {emTrial ? "Ative sua assinatura" : "Regularize seu pagamento"} na hora com PIX — confirmação
            automática.
          </p>
        </div>
      </div>

      <PagamentoPixDialog open={pixOpen} onOpenChange={setPixOpen} onPago={carregar} />
    </div>
  );
}
