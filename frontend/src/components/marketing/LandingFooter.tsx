import Link from "next/link";
import { Mail, ShieldCheck } from "lucide-react";

import { CONTATO, MARCA, whatsappLink } from "@/core/consts/landing";

export function LandingFooter() {
  return (
    <footer className="border-t border-border/60 bg-card">
      <div className="mx-auto flex max-w-6xl flex-col gap-6 px-4 py-10 md:flex-row md:items-center md:justify-between md:px-6">
        <div className="flex items-center gap-2">
          <span className="grid h-9 w-9 place-items-center rounded-full bg-primary text-primary-foreground">
            <ShieldCheck className="size-5" />
          </span>
          <div>
            <p className="font-display text-lg text-primary">{MARCA.nome}</p>
            <p className="text-xs text-muted-foreground">{MARCA.categoria}</p>
          </div>
        </div>

        <div className="flex flex-col gap-2 text-sm text-text-list">
          <a
            href={`mailto:${CONTATO.email}`}
            className="inline-flex items-center gap-2 hover:text-primary"
          >
            <Mail className="size-4" /> {CONTATO.email}
          </a>
          <a
            href={whatsappLink("Olá! Tenho interesse no sistema Arctech.")}
            target="_blank"
            rel="noopener noreferrer"
            className="hover:text-primary"
          >
            Falar no WhatsApp
          </a>
          <Link href="/login" className="hover:text-primary">
            Acessar o painel
          </Link>
        </div>
      </div>
      <div className="border-t border-border/60 py-4">
        <p className="text-center text-xs text-muted-foreground">
          © {MARCA.nome} · Todos os direitos reservados.
        </p>
      </div>
    </footer>
  );
}
