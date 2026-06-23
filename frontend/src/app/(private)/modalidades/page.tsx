"use client";

import { useMemo, useState } from "react";
import { BookText, CheckCircle2, Search, XCircle } from "lucide-react";

import { PageHeader } from "@/components/layout/PageHeader";
import { SectionCard } from "@/components/shared/SectionCard";
import { StatCard } from "@/components/layout/StatCard";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Badge } from "@/components/ui/badge";
import {
  Accordion,
  AccordionContent,
  AccordionItem,
  AccordionTrigger,
} from "@/components/ui/accordion";
import {
  modalidadesReference,
  processRulesReference,
  type ModalidadeReference,
} from "@/core/consts/modalidades";

function Booleanish({ ativo }: { ativo: boolean }) {
  return (
    <Badge
      variant={ativo ? "default" : "outline"}
      className={ativo ? "bg-success/15 text-success border-success/30" : "text-muted-foreground"}
    >
      {ativo ? <CheckCircle2 className="size-3" /> : <XCircle className="size-3" />}
      {ativo ? "Sim" : "Não"}
    </Badge>
  );
}

export default function ModalidadesPage() {
  const [search, setSearch] = useState("");

  const filtered = useMemo<ModalidadeReference[]>(() => {
    const normalized = search.trim().toLowerCase();
    if (!normalized) return modalidadesReference;
    return modalidadesReference.filter(
      (item) =>
        item.code.includes(normalized) ||
        item.label.toLowerCase().includes(normalized) ||
        item.category.toLowerCase().includes(normalized) ||
        item.notes.some((n) => n.toLowerCase().includes(normalized)),
    );
  }, [search]);

  const stats = useMemo(() => {
    return {
      total: modalidadesReference.length,
      pericia: modalidadesReference.filter((m) => m.periciaMedica).length,
      avaliacao: modalidadesReference.filter((m) => m.avaliacaoSocial).length,
      cessacao: modalidadesReference.filter((m) => m.cessacaoQuandoAprovado).length,
    };
  }, []);

  return (
    <div className="page-shell">
      <PageHeader
        eyebrow="Catálogo"
        title="Modalidades de benefício"
        description="Códigos previdenciários e assistenciais aceitos no fluxo, com regras de perícia, avaliação social e cessação."
      />

      <div className="stats-grid">
        <StatCard label="Modalidades catalogadas" value={stats.total} />
        <StatCard label="Com perícia médica" value={stats.pericia} />
        <StatCard label="Com avaliação social" value={stats.avaliacao} />
        <StatCard label="Exigem cessação ao aprovar" value={stats.cessacao} highlight />
      </div>

      <SectionCard
        title="Regras do processo"
        description="Validações que se aplicam a qualquer modalidade durante o ciclo de vida do processo."
      >
        <div className="grid gap-3 md:grid-cols-2">
          {processRulesReference.map((rule) => (
            <div key={rule.title} className="rounded-2xl border border-border bg-background/60 p-4">
              <p className="text-sm font-semibold text-primary">{rule.title}</p>
              <p className="mt-1 text-xs text-muted-foreground">{rule.detail}</p>
              <div className="mt-3 flex flex-wrap gap-1">
                {rule.fields.map((field) => (
                  <Badge key={field} variant="outline" className="text-[0.7rem]">
                    {field}
                  </Badge>
                ))}
              </div>
            </div>
          ))}
        </div>
      </SectionCard>

      <SectionCard
        title="Catálogo de modalidades"
        description="Cada item lista o código INSS, categoria e exigências operacionais."
      >
        <div className="mb-4 max-w-sm space-y-1.5">
          <Label htmlFor="mod-search" className="text-xs font-medium text-text-list">
            Buscar modalidade
          </Label>
          <div className="relative">
            <Search className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              id="mod-search"
              placeholder="Código, nome ou regra"
              className="pl-9"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </div>
        </div>

        {filtered.length === 0 ? (
          <div className="flex flex-col items-center gap-2 py-10 text-center text-sm text-muted-foreground">
            <BookText className="size-8" />
            Nenhuma modalidade encontrada.
          </div>
        ) : (
          <Accordion type="multiple" className="space-y-2">
            {filtered.map((item) => (
              <AccordionItem
                key={item.key}
                value={item.key}
                className="rounded-2xl border border-border bg-background/60 px-4"
              >
                <AccordionTrigger className="hover:no-underline">
                  <div className="flex w-full flex-wrap items-center justify-between gap-3 pr-3">
                    <div className="flex items-center gap-3">
                      <Badge variant="outline" className="rounded-full font-mono">
                        {item.code}
                      </Badge>
                      <div className="text-left">
                        <p className="font-semibold text-primary">{item.label}</p>
                        <p className="text-xs text-muted-foreground">{item.category}</p>
                      </div>
                    </div>
                    <div className="hidden gap-2 md:flex">
                      <Booleanish ativo={item.periciaMedica} />
                      <Booleanish ativo={item.avaliacaoSocial} />
                    </div>
                  </div>
                </AccordionTrigger>
                <AccordionContent className="space-y-3 pt-1">
                  <p className="text-sm text-muted-foreground">{item.summary}</p>
                  <div className="grid gap-2 md:grid-cols-2">
                    <p className="text-xs">
                      <span className="font-semibold text-primary">Perícia médica:</span>{" "}
                      {item.periciaMedica ? "Sim" : "Não"}
                    </p>
                    <p className="text-xs">
                      <span className="font-semibold text-primary">Avaliação social:</span>{" "}
                      {item.avaliacaoSocial ? "Sim" : "Não"}
                    </p>
                    <p className="text-xs">
                      <span className="font-semibold text-primary">Cessação ao aprovar:</span>{" "}
                      {item.cessacaoQuandoAprovado ? "Sim" : "Não"}
                    </p>
                    <p className="text-xs">
                      <span className="font-semibold text-primary">Valor concedido:</span>{" "}
                      {item.valorConcedidoQuandoAprovado ? "Sim" : "Não"}
                    </p>
                  </div>
                  <ul className="list-inside list-disc space-y-1 text-xs text-muted-foreground">
                    {item.notes.map((note) => (
                      <li key={note}>{note}</li>
                    ))}
                  </ul>
                </AccordionContent>
              </AccordionItem>
            ))}
          </Accordion>
        )}
      </SectionCard>
    </div>
  );
}
