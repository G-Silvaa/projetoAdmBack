export type MesValue =
  | "JANEIRO"
  | "FEVEREIRO"
  | "MARCO"
  | "ABRIL"
  | "MAIO"
  | "JUNHO"
  | "JULHO"
  | "AGOSTO"
  | "SETEMBRO"
  | "OUTUBRO"
  | "NOVEMBRO"
  | "DEZEMBRO";

export const MESES: { value: MesValue; label: string; ordinal: number }[] = [
  { value: "JANEIRO", label: "Janeiro", ordinal: 1 },
  { value: "FEVEREIRO", label: "Fevereiro", ordinal: 2 },
  { value: "MARCO", label: "Março", ordinal: 3 },
  { value: "ABRIL", label: "Abril", ordinal: 4 },
  { value: "MAIO", label: "Maio", ordinal: 5 },
  { value: "JUNHO", label: "Junho", ordinal: 6 },
  { value: "JULHO", label: "Julho", ordinal: 7 },
  { value: "AGOSTO", label: "Agosto", ordinal: 8 },
  { value: "SETEMBRO", label: "Setembro", ordinal: 9 },
  { value: "OUTUBRO", label: "Outubro", ordinal: 10 },
  { value: "NOVEMBRO", label: "Novembro", ordinal: 11 },
  { value: "DEZEMBRO", label: "Dezembro", ordinal: 12 },
];

export function mesLabel(mes?: MesValue | string): string {
  return MESES.find((m) => m.value === mes)?.label ?? (mes as string) ?? "";
}

export interface RelatorioMes {
  id: number;
  ano: number;
  mes: MesValue;
  totalContratos: number;
  totalBeneficiosConcedidos: number;
  totalBeneficiosAguardando: number;
  dadoEntrada: number;
}

export type RelatorioMensalTipo =
  | "relacao-contratos-do-mes"
  | "concessoes-do-mes"
  | "pericia-avaliacao-social-do-mes";

export type RelatorioIntervaloTipo =
  | "relacao-contratos"
  | "concessoes"
  | "pericia-avaliacao-social";

export interface IRelatorioIntervalo {
  domain: null;
  args: {
    intervalo: {
      inicio: { ano: number; mes: MesValue };
      termino: { ano: number; mes: MesValue };
    };
  };
}
