"use client";

import { useState } from "react";
import { MessageCircle } from "lucide-react";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { whatsappLink } from "@/core/consts/landing";

export function ContactForm() {
  const [nome, setNome] = useState("");
  const [escritorio, setEscritorio] = useState("");
  const [mensagem, setMensagem] = useState("");

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    const texto = [
      "Olá! Tenho interesse no sistema Arctech.",
      nome && `Nome: ${nome}`,
      escritorio && `Escritório: ${escritorio}`,
      mensagem && `Mensagem: ${mensagem}`,
    ]
      .filter(Boolean)
      .join("\n");

    window.open(whatsappLink(texto), "_blank", "noopener,noreferrer");
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div className="grid gap-4 sm:grid-cols-2">
        <div className="space-y-2">
          <Label htmlFor="c-nome">Seu nome</Label>
          <Input
            id="c-nome"
            value={nome}
            onChange={(e) => setNome(e.target.value)}
            placeholder="Como podemos te chamar?"
          />
        </div>
        <div className="space-y-2">
          <Label htmlFor="c-escritorio">Escritório (opcional)</Label>
          <Input
            id="c-escritorio"
            value={escritorio}
            onChange={(e) => setEscritorio(e.target.value)}
            placeholder="Nome do seu escritório"
          />
        </div>
      </div>
      <div className="space-y-2">
        <Label htmlFor="c-mensagem">Como podemos ajudar?</Label>
        <textarea
          id="c-mensagem"
          value={mensagem}
          onChange={(e) => setMensagem(e.target.value)}
          rows={4}
          placeholder="Conte rapidamente o que você precisa..."
          className="flex w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
        />
      </div>
      <Button type="submit" size="lg" className="w-full sm:w-auto">
        <MessageCircle className="size-4" /> Enviar pelo WhatsApp
      </Button>
    </form>
  );
}
