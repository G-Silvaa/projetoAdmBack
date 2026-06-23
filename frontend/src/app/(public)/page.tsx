import type { Metadata } from "next";
import Link from "next/link";
import {
  ArrowRight,
  BarChart3,
  Check,
  CheckCircle2,
  FileSignature,
  FolderKanban,
  Gavel,
  LineChart,
  Mail,
  MessageCircle,
  Quote,
  ShieldCheck,
  Sparkles,
  UserPlus,
  Users,
  Wallet,
  type LucideIcon,
} from "lucide-react";

import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import {
  Accordion,
  AccordionContent,
  AccordionItem,
  AccordionTrigger,
} from "@/components/ui/accordion";
import { ContactForm } from "@/components/marketing/ContactForm";
import {
  COMO_FUNCIONA,
  CONTATO,
  DEPOIMENTOS,
  FAQ,
  FEATURES,
  MARCA,
  METRICAS,
  PLANOS,
  whatsappLink,
} from "@/core/consts/landing";

export const metadata: Metadata = {
  title: "Arctech — Software de gestão para escritórios previdenciários",
  description: MARCA.heroSubtitulo,
};

const ICONS: Record<string, LucideIcon> = {
  Users,
  Gavel,
  FileSignature,
  Wallet,
  BarChart3,
  ShieldCheck,
  UserPlus,
  FolderKanban,
  LineChart,
};

function formatPreco(preco: number | null) {
  if (preco === null) return "Sob consulta";
  return preco.toLocaleString("pt-BR", {
    style: "currency",
    currency: "BRL",
    minimumFractionDigits: 0,
  });
}

/** Prévia do produto (mock) exibida no topo — dá "cara de software" à página. */
function ProductPreview() {
  return (
    <div className="surface-card overflow-hidden shadow-xl">
      {/* barra do "navegador" */}
      <div className="flex items-center gap-1.5 border-b border-border bg-accent/60 px-4 py-3">
        <span className="size-2.5 rounded-full bg-destructive/60" />
        <span className="size-2.5 rounded-full bg-warning/60" />
        <span className="size-2.5 rounded-full bg-success/60" />
        <span className="ml-3 rounded bg-card px-2 py-0.5 text-[11px] text-muted-foreground">
          app.arctech.com.br
        </span>
      </div>

      <div className="p-4">
        <p className="eyebrow">Painel · visão geral</p>
        {/* mini stat cards */}
        <div className="mt-3 grid grid-cols-3 gap-2">
          {[
            { l: "Processos ativos", v: "184" },
            { l: "Prazos da semana", v: "12" },
            { l: "Honorários a receber", v: "R$ 96k" },
          ].map((s) => (
            <div key={s.l} className="rounded-md border border-border bg-card p-2.5">
              <p className="text-[10px] text-muted-foreground">{s.l}</p>
              <p className="mt-0.5 text-lg font-semibold tracking-tight text-text">
                {s.v}
              </p>
            </div>
          ))}
        </div>

        {/* mini "tabela" de processos */}
        <div className="mt-3 rounded-md border border-border">
          <div className="flex items-center justify-between border-b border-border px-3 py-2">
            <span className="text-xs font-medium text-text">
              Próximos prazos
            </span>
            <span className="soft-badge">Esta semana</span>
          </div>
          <ul className="divide-y divide-border">
            {[
              { n: "Aposentadoria por idade", t: "Perícia", d: "hoje" },
              { n: "Auxílio-doença", t: "Contestação", d: "2 dias" },
              { n: "BPC/LOAS", t: "Audiência", d: "4 dias" },
            ].map((r) => (
              <li
                key={r.n}
                className="flex items-center justify-between px-3 py-2 text-xs"
              >
                <span className="text-text">{r.n}</span>
                <span className="flex items-center gap-2">
                  <span className="text-muted-foreground">{r.t}</span>
                  <span className="rounded bg-secondary-soft px-1.5 py-0.5 font-medium text-secondary">
                    {r.d}
                  </span>
                </span>
              </li>
            ))}
          </ul>
        </div>
      </div>
    </div>
  );
}

export default function LandingPage() {
  return (
    <>
      {/* ---------------- HERO ---------------- */}
      <section className="relative overflow-hidden border-b border-border/60">
        <div className="absolute inset-0 -z-10 bg-gradient-to-b from-secondary-soft to-background" />
        <div className="mx-auto grid max-w-6xl items-center gap-12 px-4 py-16 md:px-6 md:py-24 lg:grid-cols-2">
          {/* coluna texto */}
          <div>
            <p className="eyebrow">{MARCA.categoria}</p>
            <h1 className="mt-3 font-display text-4xl leading-[1.1] text-primary md:text-5xl">
              {MARCA.heroTitulo}
            </h1>
            <p className="mt-5 max-w-xl text-base text-text-list md:text-lg">
              {MARCA.heroSubtitulo}
            </p>

            <div className="mt-7 flex flex-col gap-3 sm:flex-row">
              <Button asChild size="lg">
                <a href="#planos">
                  Ver planos <ArrowRight className="size-4" />
                </a>
              </Button>
              <Button asChild size="lg" variant="outline">
                <a
                  href={whatsappLink("Olá! Quero conhecer o sistema Arctech.")}
                  target="_blank"
                  rel="noopener noreferrer"
                >
                  <MessageCircle className="size-4" /> Falar com um especialista
                </a>
              </Button>
            </div>

            <p className="mt-4 flex items-center gap-2 text-sm text-text-list">
              <CheckCircle2 className="size-4 text-success" /> {MARCA.trial}
            </p>
          </div>

          {/* coluna prévia do produto */}
          <div className="lg:pl-4">
            <ProductPreview />
          </div>
        </div>
      </section>

      {/* ---------------- NÚMEROS ---------------- */}
      {METRICAS.length > 0 && (
        <section className="border-b border-border/60 bg-primary text-primary-foreground">
          <div className="mx-auto grid max-w-6xl grid-cols-2 gap-6 px-4 py-10 md:grid-cols-4 md:px-6">
            {METRICAS.map((m) => (
              <div key={m.rotulo} className="text-center">
                <p className="font-display text-3xl md:text-4xl">{m.valor}</p>
                <p className="mt-1 text-sm text-primary-foreground/80">{m.rotulo}</p>
              </div>
            ))}
          </div>
        </section>
      )}

      {/* ---------------- RECURSOS ---------------- */}
      <section id="recursos" className="mx-auto max-w-6xl px-4 py-16 md:px-6 md:py-24">
        <div className="mx-auto max-w-2xl text-center">
          <p className="eyebrow">Recursos</p>
          <h2 className="mt-2 font-display text-3xl text-primary md:text-4xl">
            Tudo que o escritório previdenciário precisa, em um só sistema
          </h2>
          <p className="mt-3 text-text-list">
            Troque planilhas soltas e grupos de WhatsApp por uma plataforma feita
            para a rotina de quem trabalha com INSS.
          </p>
        </div>

        <div className="mt-12 grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
          {FEATURES.map((f) => {
            const Icon = ICONS[f.icone] ?? Sparkles;
            return (
              <div key={f.titulo} className="surface-card p-6">
                <span className="grid h-11 w-11 place-items-center rounded-lg bg-secondary-soft text-secondary">
                  <Icon className="size-5" />
                </span>
                <h3 className="mt-4 font-display text-lg text-text">{f.titulo}</h3>
                <p className="mt-1.5 text-sm text-text-list">{f.descricao}</p>
              </div>
            );
          })}
        </div>
      </section>

      {/* ---------------- COMO FUNCIONA ---------------- */}
      <section id="como-funciona" className="bg-card/50 py-16 md:py-24">
        <div className="mx-auto max-w-6xl px-4 md:px-6">
          <div className="mx-auto max-w-2xl text-center">
            <p className="eyebrow">Como funciona</p>
            <h2 className="mt-2 font-display text-3xl text-primary md:text-4xl">
              Comece a usar em 3 passos
            </h2>
          </div>

          <div className="mt-12 grid gap-6 md:grid-cols-3">
            {COMO_FUNCIONA.map((passo, i) => {
              const Icon = ICONS[passo.icone] ?? Sparkles;
              return (
                <div key={passo.titulo} className="relative surface-card p-7">
                  <span className="absolute right-5 top-5 font-display text-4xl text-accent">
                    {String(i + 1).padStart(2, "0")}
                  </span>
                  <span className="grid h-11 w-11 place-items-center rounded-lg bg-primary text-primary-foreground">
                    <Icon className="size-5" />
                  </span>
                  <h3 className="mt-4 font-display text-lg text-text">
                    {passo.titulo}
                  </h3>
                  <p className="mt-1.5 text-sm text-text-list">{passo.descricao}</p>
                </div>
              );
            })}
          </div>
        </div>
      </section>

      {/* ---------------- DEPOIMENTOS ---------------- */}
      {DEPOIMENTOS.length > 0 && (
        <section className="mx-auto max-w-6xl px-4 py-16 md:px-6 md:py-24">
          <div className="mx-auto max-w-2xl text-center">
            <p className="eyebrow">Quem usa, recomenda</p>
            <h2 className="mt-2 font-display text-3xl text-primary md:text-4xl">
              Escritórios que organizaram a rotina com o Arctech
            </h2>
          </div>

          <div className="mt-12 grid gap-6 md:grid-cols-3">
            {DEPOIMENTOS.map((d, i) => (
              <figure key={i} className="flex flex-col surface-card p-6">
                <Quote className="size-7 text-secondary/40" />
                <blockquote className="mt-3 flex-1 text-sm leading-relaxed text-text-list">
                  “{d.texto}”
                </blockquote>
                <figcaption className="mt-5 border-t border-border pt-4">
                  <p className="font-medium text-text">{d.nome}</p>
                  <p className="text-xs text-muted-foreground">{d.escritorio}</p>
                </figcaption>
              </figure>
            ))}
          </div>
        </section>
      )}

      {/* ---------------- PLANOS ---------------- */}
      <section id="planos" className="bg-card/50 py-16 md:py-24">
        <div className="mx-auto max-w-6xl px-4 md:px-6">
          <div className="mx-auto max-w-2xl text-center">
            <p className="eyebrow">Planos</p>
            <h2 className="mt-2 font-display text-3xl text-primary md:text-4xl">
              Escolha o plano ideal para o seu momento
            </h2>
            <p className="mt-3 text-text-list">
              Sem fidelidade, cancele quando quiser. Comece com 7 dias de teste
              grátis e sem cartão de crédito.
            </p>
          </div>

          <div className="mt-12 grid items-stretch gap-6 lg:grid-cols-3">
            {PLANOS.map((plano) => (
              <div
                key={plano.id}
                className={`relative flex flex-col surface-card p-7 ${
                  plano.destaque
                    ? "border-secondary shadow-lg ring-1 ring-secondary"
                    : ""
                }`}
              >
                {plano.destaque && (
                  <Badge className="absolute -top-3 left-1/2 -translate-x-1/2 bg-secondary">
                    Mais escolhido
                  </Badge>
                )}
                <h3 className="font-display text-xl text-primary">{plano.nome}</h3>
                <p className="mt-1 min-h-[2.5rem] text-sm text-text-list">
                  {plano.resumo}
                </p>
                <div className="mt-5">
                  {plano.precoPromocional != null && plano.preco !== null ? (
                    <>
                      <div className="flex items-center gap-2">
                        <span className="text-base text-muted-foreground line-through">
                          {formatPreco(plano.preco)}
                        </span>
                        <Badge className="bg-success text-white">Promo</Badge>
                      </div>
                      <div className="mt-1 flex items-end gap-1">
                        <span className="font-display text-4xl text-text">
                          {formatPreco(plano.precoPromocional)}
                        </span>
                        <span className="mb-1 text-sm text-muted-foreground">
                          {plano.periodo}
                        </span>
                      </div>
                      {plano.promoTexto && (
                        <p className="mt-1 text-xs font-medium text-success">
                          {plano.promoTexto} · depois {formatPreco(plano.preco)}
                          {plano.periodo}
                        </p>
                      )}
                    </>
                  ) : (
                    <div className="flex items-end gap-1">
                      <span className="font-display text-4xl text-text">
                        {formatPreco(plano.preco)}
                      </span>
                      {plano.preco !== null && (
                        <span className="mb-1 text-sm text-muted-foreground">
                          {plano.periodo}
                        </span>
                      )}
                    </div>
                  )}
                </div>

                <ul className="mt-6 space-y-3">
                  {plano.recursos.map((r) => (
                    <li key={r} className="flex items-start gap-2 text-sm text-text-list">
                      <Check className="mt-0.5 size-4 shrink-0 text-success" />
                      {r}
                    </li>
                  ))}
                </ul>

                <div className="mt-7 pt-2">
                  <Button
                    asChild
                    className="w-full"
                    variant={plano.destaque ? "default" : "outline"}
                  >
                    {plano.id === "escritorio" ? (
                      <a
                        href={whatsappLink(
                          `Olá! Tenho interesse no plano ${plano.nome} do sistema Arctech.`,
                        )}
                        target="_blank"
                        rel="noopener noreferrer"
                      >
                        {plano.cta}
                      </a>
                    ) : (
                      <Link href={`/cadastro?plano=${plano.id}`}>{plano.cta}</Link>
                    )}
                  </Button>
                </div>
              </div>
            ))}
          </div>

          <p className="mt-8 text-center text-sm text-muted-foreground">
            Já é assinante?{" "}
            <Link href="/login" className="font-medium text-secondary hover:underline">
              Acesse o painel
            </Link>
          </p>
        </div>
      </section>

      {/* ---------------- FAQ ---------------- */}
      <section id="faq" className="mx-auto max-w-3xl px-4 py-16 md:px-6 md:py-24">
        <div className="text-center">
          <p className="eyebrow">Dúvidas frequentes</p>
          <h2 className="mt-2 font-display text-3xl text-primary md:text-4xl">
            Perguntas comuns
          </h2>
        </div>
        <Accordion type="single" collapsible className="mt-8">
          {FAQ.map((item, i) => (
            <AccordionItem key={i} value={`item-${i}`}>
              <AccordionTrigger className="text-left text-base">
                {item.pergunta}
              </AccordionTrigger>
              <AccordionContent className="text-text-list">
                {item.resposta}
              </AccordionContent>
            </AccordionItem>
          ))}
        </Accordion>
      </section>

      {/* ---------------- CONTATO / COMPRAR ---------------- */}
      <section id="contato" className="bg-card/50 py-16 md:py-24">
        <div className="mx-auto grid max-w-6xl gap-12 px-4 md:px-6 lg:grid-cols-2">
          <div>
            <p className="eyebrow">Fale comigo</p>
            <h2 className="mt-2 font-display text-3xl text-primary md:text-4xl">
              Quer comprar o sistema ou tirar dúvidas?
            </h2>
            <p className="mt-3 text-text-list">
              Me chame e eu te apresento o Arctech na prática, monto uma proposta
              sob medida para o seu escritório e te ajudo na contratação.
            </p>

            <div className="mt-8 space-y-4">
              <a
                href={whatsappLink("Olá! Quero comprar o sistema Arctech.")}
                target="_blank"
                rel="noopener noreferrer"
                className="flex items-center gap-3 surface-card p-4 transition-colors hover:border-secondary"
              >
                <span className="grid h-11 w-11 place-items-center rounded-lg bg-success/10 text-success">
                  <MessageCircle className="size-5" />
                </span>
                <span>
                  <span className="block font-medium text-text">WhatsApp</span>
                  <span className="block text-sm text-text-list">
                    Resposta rápida — chame agora
                  </span>
                </span>
              </a>

              <a
                href={`mailto:${CONTATO.email}`}
                className="flex items-center gap-3 surface-card p-4 transition-colors hover:border-secondary"
              >
                <span className="grid h-11 w-11 place-items-center rounded-lg bg-secondary-soft text-secondary">
                  <Mail className="size-5" />
                </span>
                <span>
                  <span className="block font-medium text-text">E-mail</span>
                  <span className="block text-sm text-text-list">{CONTATO.email}</span>
                </span>
              </a>
            </div>

            <p className="mt-6 text-sm text-muted-foreground">
              {CONTATO.horarioAtendimento}
            </p>
          </div>

          <div className="surface-card p-6 md:p-8">
            <h3 className="font-display text-lg text-text">Deixe seu contato</h3>
            <p className="mb-5 mt-1 text-sm text-text-list">
              Preencha e continue a conversa direto no WhatsApp.
            </p>
            <ContactForm />
          </div>
        </div>
      </section>
    </>
  );
}
