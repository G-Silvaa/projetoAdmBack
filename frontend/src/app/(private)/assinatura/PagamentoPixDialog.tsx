"use client";

import { useEffect, useRef, useState } from "react";
import { Check, CheckCircle2, Copy, Loader2, QrCode } from "lucide-react";
import { toast } from "sonner";

import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { assinaturaService } from "@/core/services/assinatura.service";
import { formatCurrencyBR } from "@/core/helpers/format";
import type { Cobranca } from "@/core/types/auth";

const POLL_INTERVAL_MS = 4000;

interface PagamentoPixDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  /** Chamado quando o pagamento é confirmado (para recarregar a assinatura). */
  onPago: () => void;
}

export function PagamentoPixDialog({ open, onOpenChange, onPago }: PagamentoPixDialogProps) {
  const [cobranca, setCobranca] = useState<Cobranca | null>(null);
  const [loading, setLoading] = useState(false);
  const [erro, setErro] = useState(false);
  const [simulando, setSimulando] = useState(false);
  const [copiado, setCopiado] = useState(false);

  // Evita disparar a confirmação duas vezes (polling + simulação) e mantém as
  // callbacks do pai sempre atualizadas sem virar dependência dos effects.
  const confirmadoRef = useRef(false);
  const onPagoRef = useRef(onPago);
  const onOpenChangeRef = useRef(onOpenChange);
  useEffect(() => {
    onPagoRef.current = onPago;
    onOpenChangeRef.current = onOpenChange;
  });

  function confirmarPagamento() {
    if (confirmadoRef.current) return;
    confirmadoRef.current = true;
    toast.success("Pagamento confirmado! Sua assinatura está ativa.");
    onPagoRef.current();
    onOpenChangeRef.current(false);
  }

  // Cria a cobrança ao abrir; limpa o estado ao fechar.
  useEffect(() => {
    if (!open) {
      setCobranca(null);
      setErro(false);
      setSimulando(false);
      setCopiado(false);
      confirmadoRef.current = false;
      return;
    }

    let ativo = true;
    setLoading(true);
    setErro(false);
    assinaturaService
      .criarPagamento()
      .then((c) => {
        if (ativo) setCobranca(c);
      })
      .catch(() => {
        if (ativo) {
          setErro(true);
          toast.error("Não foi possível gerar a cobrança PIX.");
        }
      })
      .finally(() => {
        if (ativo) setLoading(false);
      });

    return () => {
      ativo = false;
    };
  }, [open]);

  // Polling do status enquanto a cobrança está pendente.
  useEffect(() => {
    if (!open || !cobranca || cobranca.pago) return;

    const id = cobranca.id;
    const timer = setInterval(() => {
      assinaturaService
        .consultarPagamento(id)
        .then((c) => {
          setCobranca(c);
          if (c.pago) confirmarPagamento();
        })
        .catch(() => {
          /* mantém o polling silencioso em caso de falha pontual */
        });
    }, POLL_INTERVAL_MS);

    return () => clearInterval(timer);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [open, cobranca?.id, cobranca?.pago]);

  async function copiarCodigo() {
    if (!cobranca?.brCode) return;
    try {
      await navigator.clipboard.writeText(cobranca.brCode);
      setCopiado(true);
      setTimeout(() => setCopiado(false), 2000);
    } catch {
      toast.error("Não foi possível copiar o código.");
    }
  }

  async function simularPagamento() {
    if (!cobranca) return;
    setSimulando(true);
    try {
      const c = await assinaturaService.simularPagamento(cobranca.id);
      setCobranca(c);
      if (c.pago) confirmarPagamento();
    } catch {
      toast.error("Não foi possível simular o pagamento.");
    } finally {
      setSimulando(false);
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <QrCode className="size-5 text-primary" />
            Pagamento via PIX
          </DialogTitle>
          <DialogDescription>
            Escaneie o QR code ou use o PIX copia e cola para pagar
            {cobranca ? ` ${formatCurrencyBR(cobranca.valor)}` : ""}.
          </DialogDescription>
        </DialogHeader>

        {loading && (
          <div className="grid place-items-center py-12">
            <Loader2 className="size-6 animate-spin text-primary" />
            <p className="mt-3 text-sm text-muted-foreground">Gerando cobrança…</p>
          </div>
        )}

        {!loading && erro && (
          <div className="py-8 text-center text-sm text-text-list">
            Falha ao gerar a cobrança. Feche e tente novamente.
          </div>
        )}

        {!loading && cobranca && !erro && (
          <div className="flex w-full min-w-0 flex-col items-center gap-4">
            {cobranca.brCodeBase64 && (
              <div className="rounded-lg border border-border bg-white p-3">
                {/* eslint-disable-next-line @next/next/no-img-element */}
                <img src={cobranca.brCodeBase64} alt="QR code PIX" className="size-56" />
              </div>
            )}

            {cobranca.brCode && (
              <div className="w-full min-w-0">
                <p className="mb-1 text-xs text-muted-foreground">PIX copia e cola</p>
                <div className="flex w-full min-w-0 items-center gap-2">
                  <code className="min-w-0 flex-1 truncate rounded-md bg-accent px-3 py-2 text-xs text-text">
                    {cobranca.brCode}
                  </code>
                  <Button variant="outline" size="icon" onClick={copiarCodigo} title="Copiar código">
                    {copiado ? <Check className="size-4 text-success" /> : <Copy className="size-4" />}
                  </Button>
                </div>
              </div>
            )}

            <div className="flex w-full items-center justify-center gap-2 rounded-md bg-accent px-3 py-2 text-sm text-text-list">
              <Loader2 className="size-4 animate-spin text-secondary" />
              Aguardando confirmação do pagamento…
            </div>

            {/* Sandbox: confirma o pagamento sem um app bancário real. */}
            <Button onClick={simularPagamento} disabled={simulando} className="w-full" size="lg">
              {simulando ? (
                <Loader2 className="size-4 animate-spin" />
              ) : (
                <CheckCircle2 className="size-4" />
              )}
              Simular pagamento (sandbox)
            </Button>
            <p className="text-center text-xs text-muted-foreground">
              Ambiente de testes — nenhum valor é cobrado de verdade.
            </p>
          </div>
        )}
      </DialogContent>
    </Dialog>
  );
}
