/**
 * Conteúdo da landing page (página de vendas pública do Arctech).
 *
 * >>> EDITE AQUI <<<
 * Tudo que aparece na página de vendas (preços, planos, números, depoimentos,
 * contato, textos) está centralizado neste arquivo. Você não precisa mexer no
 * código das telas — só trocar os valores abaixo.
 */

/* ------------------------------------------------------------------ */
/* 1. CONTATO — coloque seus dados reais aqui                          */
/* ------------------------------------------------------------------ */

export const CONTATO = {
  // Número de WhatsApp no formato internacional, só dígitos (DDI + DDD + número).
  // Ex.: Brasil, DDD 11, número 91234-5678  ->  "5511912345678"
  whatsapp: "5585989990129",
  // E-mail comercial que recebe os contatos.
  email: "geniltonsilva4002@gmail.com",
  // Texto curto exibido perto do telefone (opcional).
  horarioAtendimento: "Atendimento de seg. a sex., das 9h às 18h",
};

/** Monta o link wa.me já com uma mensagem pronta. */
export function whatsappLink(mensagem?: string) {
  const base = `https://wa.me/${CONTATO.whatsapp}`;
  return mensagem ? `${base}?text=${encodeURIComponent(mensagem)}` : base;
}

/* ------------------------------------------------------------------ */
/* 2. MARCA / TEXTOS DO TOPO                                           */
/* ------------------------------------------------------------------ */

export const MARCA = {
  nome: "Arctech",
  // Aparece pequeno, em maiúsculas, acima do título.
  categoria: "Software de gestão previdenciária",
  heroTitulo:
    "Toda a rotina do seu escritório previdenciário, do primeiro atendimento ao recebimento dos honorários",
  heroSubtitulo:
    "Centralize clientes, processos, prazos, contratos e financeiro em um só lugar. Pare de controlar tudo em planilha e acompanhe cada benefício em tempo real.",
  // Frase de baixo atrito embaixo dos botões do topo.
  trial: "Teste grátis por 7 dias · não pedimos cartão de crédito",
};

/* ------------------------------------------------------------------ */
/* 3. NÚMEROS / PROVAS SOCIAIS                                         */
/*    Vazio de propósito: não usamos números inventados.              */
/*    Quando tiver dados REAIS, adicione itens aqui e a barra de       */
/*    números volta a aparecer sozinha. Ex.:                          */
/*      { valor: "+50", rotulo: "escritórios usando" },               */
/* ------------------------------------------------------------------ */

export type Metrica = { valor: string; rotulo: string };

export const METRICAS: Metrica[] = [];

/* ------------------------------------------------------------------ */
/* 4. DIFERENCIAIS / FUNCIONALIDADES                                  */
/* ------------------------------------------------------------------ */

export type Feature = {
  titulo: string;
  descricao: string;
  /** Nome do ícone do lucide-react (https://lucide.dev/icons). */
  icone: string;
};

export const FEATURES: Feature[] = [
  {
    titulo: "Clientes e segurados",
    descricao:
      "Cadastro com dados, documentos e histórico de cada segurado reunidos em um único lugar.",
    icone: "Users",
  },
  {
    titulo: "Processos e prazos",
    descricao:
      "Andamentos, audiências, perícias e prazos sob controle — para nunca mais perder uma data.",
    icone: "Gavel",
  },
  {
    titulo: "Contratos e honorários",
    descricao:
      "Gere contratos vinculados ao processo e acompanhe honorários contratados e recebidos.",
    icone: "FileSignature",
  },
  {
    titulo: "Financeiro do escritório",
    descricao:
      "Receitas, despesas e fluxo de caixa com uma visão clara do que entra e do que sai.",
    icone: "Wallet",
  },
  {
    titulo: "Relatórios em tempo real",
    descricao:
      "Indicadores e relatórios que mostram a saúde do escritório a qualquer momento.",
    icone: "BarChart3",
  },
  {
    titulo: "Controle de acessos",
    descricao:
      "Cada colaborador enxerga apenas o que precisa, com permissões por nível de usuário.",
    icone: "ShieldCheck",
  },
];

/* ------------------------------------------------------------------ */
/* 5. COMO FUNCIONA (3 passos)                                         */
/* ------------------------------------------------------------------ */

export type Passo = { titulo: string; descricao: string; icone: string };

export const COMO_FUNCIONA: Passo[] = [
  {
    titulo: "Crie sua conta",
    descricao:
      "Cadastre o escritório e sua equipe em minutos e defina o nível de acesso de cada colaborador.",
    icone: "UserPlus",
  },
  {
    titulo: "Centralize sua operação",
    descricao:
      "Cadastre clientes e vincule processos, prazos, contratos e documentos a cada segurado.",
    icone: "FolderKanban",
  },
  {
    titulo: "Acompanhe os resultados",
    descricao:
      "Veja prazos, andamentos e financeiro em painéis claros e tome decisões com dados na mão.",
    icone: "LineChart",
  },
];

/* ------------------------------------------------------------------ */
/* 6. DEPOIMENTOS                                                      */
/*    Vazio de propósito: ainda não há clientes, então não exibimos    */
/*    depoimentos (nada fictício). Quando tiver depoimentos REAIS,     */
/*    adicione itens aqui e a seção volta a aparecer sozinha. Ex.:     */
/*      { texto: "...", nome: "João Silva", escritorio: "Silva Adv · SP" }, */
/* ------------------------------------------------------------------ */

export type Depoimento = { texto: string; nome: string; escritorio: string };

export const DEPOIMENTOS: Depoimento[] = [];

/* ------------------------------------------------------------------ */
/* 7. PLANOS / PREÇOS  — ajuste os valores conforme sua estratégia     */
/* ------------------------------------------------------------------ */

export type Plano = {
  id: string;
  nome: string;
  /** Preço mensal em reais (apenas o número). Use null para "sob consulta". */
  preco: number | null;
  periodo: string;
  resumo: string;
  /** Marca o plano em destaque ("mais popular"). */
  destaque?: boolean;
  recursos: string[];
  /** Texto do botão de ação. */
  cta: string;
  /**
   * (Opcional) Promoção: quando preenchido, o `preco` aparece riscado e este
   * valor é exibido como preço promocional. Use junto com `promoTexto`.
   */
  precoPromocional?: number;
  /** (Opcional) Texto da promo, ex.: "nos 3 primeiros meses". */
  promoTexto?: string;
};

export const PLANOS: Plano[] = [
  {
    id: "normal",
    nome: "Normal",
    preco: 80,
    periodo: "/mês",
    resumo: "Para o advogado autônomo que está começando a organizar a operação.",
    cta: "Começar agora",
    recursos: [
      "Até 50 clientes ativos",
      "Gestão de processos e contratos",
      "Controle financeiro básico",
      "1 usuário",
      "Suporte por e-mail",
    ],
  },
  {
    id: "profissional",
    nome: "Profissional",
    preco: 110,
    precoPromocional: 90,
    promoTexto: "nos 3 primeiros meses",
    periodo: "/mês",
    resumo: "Para escritórios em crescimento que precisam de relatórios e equipe.",
    destaque: true,
    cta: "Aproveitar promoção",
    recursos: [
      "Clientes ilimitados",
      "Relatórios e indicadores avançados",
      "Controle financeiro completo",
      "Até 5 usuários",
      "Suporte prioritário por WhatsApp",
    ],
  },
  {
    id: "escritorio",
    nome: "Escritório",
    preco: 397,
    periodo: "/mês",
    resumo: "Para bancas e escritórios com vários colaboradores e alto volume.",
    cta: "Falar com vendas",
    recursos: [
      "Tudo do Profissional",
      "Usuários ilimitados",
      "Onboarding e treinamento da equipe",
      "Personalizações sob demanda",
      "Gerente de conta dedicado",
    ],
  },
];

/* ------------------------------------------------------------------ */
/* 8. PERGUNTAS FREQUENTES                                             */
/* ------------------------------------------------------------------ */

export type FaqItem = { pergunta: string; resposta: string };

export const FAQ: FaqItem[] = [
  {
    pergunta: "Preciso instalar alguma coisa?",
    resposta:
      "Não. O Arctech funciona 100% online, pelo navegador. Basta acessar com seu login, de qualquer computador.",
  },
  {
    pergunta: "Meus dados ficam seguros?",
    resposta:
      "Sim. Os acessos são controlados por nível de usuário e cada escritório só enxerga os próprios dados.",
  },
  {
    pergunta: "Posso testar antes de assinar?",
    resposta:
      "Pode. Você tem 7 dias de teste grátis, sem precisar cadastrar cartão de crédito.",
  },
  {
    pergunta: "Posso trocar de plano depois?",
    resposta:
      "A qualquer momento. Você faz upgrade ou downgrade conforme o crescimento do escritório.",
  },
  {
    pergunta: "Como funciona a compra do sistema?",
    resposta:
      "Você assina um dos planos mensais ou fala diretamente comigo pelo WhatsApp para uma proposta sob medida.",
  },
];
