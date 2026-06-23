export interface ModalidadeReference {
  code: string;
  key: string;
  label: string;
  category: string;
  periciaMedica: boolean;
  avaliacaoSocial: boolean;
  cessacaoQuandoAprovado: boolean;
  valorConcedidoQuandoAprovado: boolean;
  summary: string;
  notes: string[];
}

export interface ProcessRuleReference {
  title: string;
  detail: string;
  fields: string[];
}

export const modalidadesReference: ModalidadeReference[] = [
  {
    code: "87",
    key: "BPC_LOAS__DEFICIENTE",
    label: "BPC/LOAS ao Deficiente",
    category: "Assistencial",
    periciaMedica: true,
    avaliacaoSocial: true,
    cessacaoQuandoAprovado: false,
    valorConcedidoQuandoAprovado: false,
    summary: "Modalidade que permite perícia médica e avaliação social no processo.",
    notes: [
      "Use quando o atendimento estiver vinculado ao amparo assistencial da pessoa com deficiência.",
      "Se houver avaliação social, ela precisa ter data e endereço preenchidos.",
    ],
  },
  {
    code: "88",
    key: "BPC_LOAS__IDOSO",
    label: "BPC/LOAS ao Idoso",
    category: "Assistencial",
    periciaMedica: false,
    avaliacaoSocial: false,
    cessacaoQuandoAprovado: false,
    valorConcedidoQuandoAprovado: false,
    summary: "Modalidade assistencial sem uso de perícia médica ou avaliação social no processo atual.",
    notes: ["Quando houver concessão, a carta usa a regra do BPC ao idoso."],
  },
  {
    code: "41",
    key: "APOSENTADORIA_IDADE",
    label: "Aposentadoria por Idade",
    category: "Previdenciário",
    periciaMedica: false,
    avaliacaoSocial: false,
    cessacaoQuandoAprovado: false,
    valorConcedidoQuandoAprovado: false,
    summary: "Fluxo previdenciário padrão, sem perícia médica e sem cessação obrigatória.",
    notes: ["Ao aprovar, informe a data de concessão do processo."],
  },
  {
    code: "42",
    key: "APOSENTADORIA_TEMPO_CONTRIBUICAO",
    label: "Aposentadoria por Tempo de Contribuição",
    category: "Previdenciário",
    periciaMedica: false,
    avaliacaoSocial: false,
    cessacaoQuandoAprovado: false,
    valorConcedidoQuandoAprovado: false,
    summary: "Modalidade previdenciária sem agendas presenciais obrigatórias no processo.",
    notes: ["Ao aprovar, informe a data de concessão do processo."],
  },
  {
    code: "32",
    key: "APOSENTADORIA_INVALIDEZ",
    label: "Aposentadoria por Invalidez",
    category: "Previdenciário",
    periciaMedica: false,
    avaliacaoSocial: false,
    cessacaoQuandoAprovado: false,
    valorConcedidoQuandoAprovado: false,
    summary: "No sistema atual, não aceita perícia médica nem avaliação social no processo.",
    notes: ["Ao aprovar, informe a data de concessão do processo."],
  },
  {
    code: "21",
    key: "PENSAO_MORTE",
    label: "Pensão por Morte",
    category: "Previdenciário",
    periciaMedica: false,
    avaliacaoSocial: false,
    cessacaoQuandoAprovado: false,
    valorConcedidoQuandoAprovado: false,
    summary: "Fluxo sem perícia médica e sem avaliação social no processo.",
    notes: ["Ao aprovar, informe a data de concessão do processo."],
  },
  {
    code: "25",
    key: "AUXILIO_RECLUSAO",
    label: "Auxílio Reclusão",
    category: "Previdenciário",
    periciaMedica: false,
    avaliacaoSocial: false,
    cessacaoQuandoAprovado: false,
    valorConcedidoQuandoAprovado: false,
    summary: "Modalidade sem uso de perícia médica e avaliação social no processo.",
    notes: ["Ao aprovar, informe a data de concessão do processo."],
  },
  {
    code: "31",
    key: "AUXILIO_INCAPACIDADE_TEMPORARIA",
    label: "Auxílio por Incapacidade Temporária",
    category: "Previdenciário",
    periciaMedica: true,
    avaliacaoSocial: false,
    cessacaoQuandoAprovado: true,
    valorConcedidoQuandoAprovado: true,
    summary: "Modalidade que aceita perícia médica e exige cessação quando o status ficar aprovado.",
    notes: [
      "Se o processo for aprovado, a data de cessação passa a ser obrigatória.",
      "Valor concedido e cessação só fazem sentido depois da concessão.",
    ],
  },
  {
    code: "36",
    key: "AUXILIO_ACIDENTE",
    label: "Auxílio Acidente",
    category: "Previdenciário",
    periciaMedica: false,
    avaliacaoSocial: false,
    cessacaoQuandoAprovado: false,
    valorConcedidoQuandoAprovado: false,
    summary: "Fluxo sem perícia médica e sem avaliação social no processo atual.",
    notes: ["Ao aprovar, informe a data de concessão do processo."],
  },
  {
    code: "80",
    key: "SALARIO_MATERNIDADE",
    label: "Salário Maternidade",
    category: "Previdenciário",
    periciaMedica: false,
    avaliacaoSocial: false,
    cessacaoQuandoAprovado: false,
    valorConcedidoQuandoAprovado: false,
    summary: "Modalidade sem perícia médica e sem avaliação social dentro do fluxo atual.",
    notes: ["Ao aprovar, informe a data de concessão do processo."],
  },
];

export const processRulesReference: ProcessRuleReference[] = [
  {
    title: "Protocolo obrigatório",
    detail:
      "Número do protocolo e entrada do protocolo são obrigatórios para análise, exigência, análise administrativa, aprovado e reprovado.",
    fields: ["Número do protocolo", "Entrada do protocolo", "Status"],
  },
  {
    title: "Documentos pendentes",
    detail:
      "Quando o status for Pendente ou Cumprimento de exigência, o campo de documentos pendentes precisa ser informado.",
    fields: ["Status", "Documentos pendentes"],
  },
  {
    title: "Concessão",
    detail: "Quando o status for Aprovado, a data de concessão precisa estar preenchida.",
    fields: ["Status", "Data de concessão"],
  },
  {
    title: "Agendamentos",
    detail: "Perícia médica e avaliação social precisam sempre de data e endereço juntos.",
    fields: ["Perícia médica", "Endereço perícia médica", "Avaliação social", "Endereço avaliação social"],
  },
];
