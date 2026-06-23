"use client";

import { forwardRef } from "react";
import { Input } from "@/components/ui/input";

function maskCPF(value: string) {
  const digits = value.replace(/\D/g, "").slice(0, 11);
  return digits
    .replace(/(\d{3})(\d)/, "$1.$2")
    .replace(/(\d{3})\.(\d{3})(\d)/, "$1.$2.$3")
    .replace(/\.(\d{3})(\d)/, ".$1-$2");
}

function maskMoney(value: string) {
  const digits = value.replace(/\D/g, "");
  if (!digits) return "";
  const number = Number(digits) / 100;
  return number.toLocaleString("pt-BR", { style: "currency", currency: "BRL" });
}

function maskTelefone(value: string) {
  const digits = value.replace(/\D/g, "").slice(0, 11);
  if (digits.length <= 10) {
    return digits.replace(/(\d{2})(\d{4})(\d{0,4})/, "($1) $2-$3").trim();
  }
  return digits.replace(/(\d{2})(\d{5})(\d{0,4})/, "($1) $2-$3").trim();
}

function maskCep(value: string) {
  const digits = value.replace(/\D/g, "").slice(0, 8);
  if (digits.length <= 5) return digits;
  return `${digits.slice(0, 5)}-${digits.slice(5)}`;
}

function maskRg(value: string) {
  return value.replace(/[^\dXx]/g, "").slice(0, 12).toUpperCase();
}

type MaskType = "cpf" | "money" | "telefone" | "cep" | "rg";

interface MaskedInputProps extends Omit<React.ComponentProps<"input">, "onChange" | "value"> {
  value?: string;
  onChange?: (value: string) => void;
  mask: MaskType;
}

export const MaskedInput = forwardRef<HTMLInputElement, MaskedInputProps>(
  ({ mask, value = "", onChange, ...props }, ref) => {
    const apply = (raw: string) => {
      if (mask === "cpf") return maskCPF(raw);
      if (mask === "money") return maskMoney(raw);
      if (mask === "telefone") return maskTelefone(raw);
      if (mask === "cep") return maskCep(raw);
      if (mask === "rg") return maskRg(raw);
      return raw;
    };

    return (
      <Input
        ref={ref}
        value={apply(value)}
        onChange={(e) => onChange?.(apply(e.target.value))}
        {...props}
      />
    );
  },
);
MaskedInput.displayName = "MaskedInput";
