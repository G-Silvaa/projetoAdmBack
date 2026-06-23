export interface BeneficioOption {
  value: string; // enum NAME (usado em filtros JPQL)
  codigo: string; // código numérico (usado em POST/PATCH onde o backend @JsonCreator espera)
  label: string;
}

export const beneficiosOptions: BeneficioOption[] = [
  { value: "BPC_LOAS__DEFICIENTE", codigo: "87", label: "BPC/LOAS ao Deficiente" },
  { value: "BPC_LOAS__IDOSO", codigo: "88", label: "BPC/LOAS ao Idoso" },
  { value: "APOSENTADORIA_IDADE", codigo: "41", label: "Aposentadoria por Idade" },
  { value: "APOSENTADORIA_TEMPO_CONTRIBUICAO", codigo: "42", label: "Aposentadoria por Tempo de Contribuição" },
  { value: "APOSENTADORIA_INVALIDEZ", codigo: "32", label: "Aposentadoria por Invalidez" },
  { value: "PENSAO_MORTE", codigo: "21", label: "Pensão por Morte" },
  { value: "AUXILIO_RECLUSAO", codigo: "25", label: "Auxílio Reclusão" },
  { value: "AUXILIO_INCAPACIDADE_TEMPORARIA", codigo: "31", label: "Auxílio por Incapacidade Temporária" },
  { value: "AUXILIO_ACIDENTE", codigo: "36", label: "Auxílio Acidente" },
  { value: "SALARIO_MATERNIDADE", codigo: "80", label: "Salário Maternidade" },
];

export function beneficioToCodigo(value: string): string {
  return beneficiosOptions.find((b) => b.value === value)?.codigo ?? value;
}

export const statusProcessoOptions = [
  { value: "AGUARDANDO", label: "Aguardando" },
  { value: "PENDENTE", label: "Pendente" },
  { value: "ANALISE", label: "Análise" },
  { value: "CUMPRIMENTO_EXIGENCIA", label: "Cumprimento de exigência" },
  { value: "ANALISE_ADMINISTRATIVA", label: "Análise administrativa" },
  { value: "APROVADO", label: "Aprovado" },
  { value: "REPROVADO", label: "Reprovado" },
];
