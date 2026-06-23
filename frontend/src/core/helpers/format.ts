export function formatCPF(cpf?: string | null): string {
  if (!cpf) return "";
  const digits = cpf.replace(/\D/g, "");
  if (digits.length !== 11) return cpf;
  return digits.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, "$1.$2.$3-$4");
}

export function unformatCPF(cpf?: string | null): string {
  return (cpf ?? "").replace(/\D/g, "");
}

export function formatDateBR(date?: string | null): string {
  if (!date) return "";
  const iso = date.split("T")[0];
  const parts = iso.split("-");
  if (parts.length !== 3) return date;
  const [ano, mes, dia] = parts;
  return `${dia}/${mes}/${ano}`;
}

export function formatDateTimeBR(value?: string | null): string {
  if (!value) return "";
  const [date, time] = value.split("T");
  const hh = time ? time.substring(0, 5) : "";
  return `${formatDateBR(date)}${hh ? ` ${hh}` : ""}`.trim();
}

export function formatCurrencyBR(value?: number | null): string {
  return new Intl.NumberFormat("pt-BR", {
    style: "currency",
    currency: "BRL",
    maximumFractionDigits: 2,
  }).format(Number(value ?? 0));
}

export function todayLabel(): string {
  return new Intl.DateTimeFormat("pt-BR", {
    day: "2-digit",
    month: "long",
    year: "numeric",
  }).format(new Date());
}

export function getInitials(name?: string | null): string {
  const trimmed = name?.trim();
  if (!trimmed) return "U";
  return trimmed
    .split(" ")
    .slice(0, 2)
    .map((part) => part.charAt(0).toUpperCase())
    .join("");
}
