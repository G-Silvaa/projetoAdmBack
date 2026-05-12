package com.liv.domain;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.util.ddd.annotation.Create;
import javax.util.ddd.annotation.Delete;
import javax.util.ddd.annotation.Update;
import javax.util.ddd.domain.DomainException;
import javax.util.ddd.domain.IRepository;

import org.greport.Borders;
import org.greport.LayoutGrid;
import org.greport.Report;
import org.greport.pdf.ReportExporterPDF;

import com.liv.infra.util.ModalidadeConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
@Entity
@Table(name = "contratos", uniqueConstraints = {
		@UniqueConstraint(name = "contratos_uk_numero", columnNames = "numero")
})
public class Contrato {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@NotNull(message = "Indique o cliente")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "cliente_id", foreignKey = @ForeignKey(name = "contratos_fk_cliente"), nullable = false)
	private Cliente cliente;
	
	// Número de contrato, data do dia que está fechando contrato + 5 últimos número do CPF
	// Ex: 18112437340 -> fechado dia 18 do mês 11 de 2024 com 373-40 de dígitos finais do CPF
	@Column(name = "numero", length = 15, nullable = false)
	private String numero;
	
	@Column(name = "indicacao", length = 255)
	private String indicacao;
	
	@NotNull(message = "Informe o valor deste contrato")
	@Column(name = "valor")
	private BigDecimal valor;
	
	@NotNull(message = "Informe o benefício")
	@Enumerated(EnumType.STRING)
    @Convert(converter = ModalidadeConverter.class)
	@Column(name = "beneficio", nullable = false)
	private Modalidade beneficio;
	
	// @TODO: Atualização de datas só podem ser realizadas uma vez
	// Após isso, somente mediante permissão do admin
	// fechamento contrato
	@Temporal(TemporalType.DATE)
	@Column(name = "inicio", columnDefinition = "date", nullable = false)
	private Date inicio;
	
	// @TODO: só pode alterar uma vez, e, se errou, só com a senha do admin
	// conclusão do contrato
	@Temporal(TemporalType.DATE)
	@Column(name = "conclusao", columnDefinition = "date")
	private Date conclusao;
	
	// POST /liv-api/domain/contrato/add
	@Create
	public void add() {
		// @TODO: Verificar lógica de negócios
		// Atualmente ele só deixa gerar 
		List<Contrato> contratos = repository.getAll("SELECT c FROM Contrato c WHERE c.cliente.id = ?1", cliente.getId());
		
		for(Contrato contrato : contratos) {
			if(contrato.getBeneficio().equals(Modalidade.fromBeneficio(beneficio.getModalidade())) && !contrato.getBeneficio().equals(Modalidade.AUXILIO_INCAPACIDADE_TEMPORARIA)) {
				List<Processo> processos = repository.getAll("SELECT p FROM Processo p WHERE p.contrato.id = ?1", this.getId());
				for(Processo processo : processos) {
					if(processo.getStatus().equals(Status.APROVADO)) {
						throw new DomainException("Cliente já teve benefício concedido para esta modalidade");
					}
				}
			}
			
			if(contrato.getConclusao() == null && contrato.getBeneficio().equals(Modalidade.fromBeneficio(beneficio.getModalidade())) && contrato.getBeneficio().equals(Modalidade.AUXILIO_INCAPACIDADE_TEMPORARIA)) {
				throw new DomainException("Cliente ainda possui contrato vigente para esta modalidade. Renove-o, ao invés de gerar novo contrato.");
			}
			
			if(contrato.getConclusao() == null) {
				throw new DomainException("Cliente já possui um contrato vigente");
			}
		}
		
		this.setInicio(new Date());
		this.setNumero(gerarNumeroContrato(repository.get(Cliente.class, cliente.getId()).get().getCpf(), inicio));
		repository.add(this);
		
		Processo processo = new Processo();
		processo.setContrato(this);
		repository.add(processo);
	}
	
	// PATCH /liv-api/domain/contrato/{id}/renovar
	public void renovar() {
		List<Processo> processos = repository.getAll("SELECT p FROM Processo p WHERE p.contrato.id = ?1", this.getId());
		for(Processo processo : processos) {
			if(!processo.getContrato().getBeneficio().equals(Modalidade.AUXILIO_INCAPACIDADE_TEMPORARIA)) {
				throw new DomainException("Esta modalidade não permite renovação de contrato");
			}
			
			if(!processo.getStatus().equals(Status.APROVADO) || processo.getCessacao() == null) {
				throw new DomainException("Renovação não disponível no momento");
			}
		}
		
		this.setConclusao(null);
		repository.add(this);
		
		Processo processo = new Processo();
		processo.setContrato(this);
		repository.add(processo);
	}
	
	// PATCH /liv-api/domain/contrato/{id}/gerar-contrato
	public Object[] gerarContrato() {
		Report report = new Report();
		report.setPageHeight(842f);
		report.setPageWidth(595f);

		// Header em todas as páginas
		report.addHeaderGrid().setBorder(Borders.None)
				.addRow()
				.addValue("LIV ASSESSORIA PREVIDENCIÁRIA").setFontBold().setFontSize(9).sethAlignLeft()
				.addValue("Contrato Nº " + numero).setFontSize(9).sethAlignRight();

		// Footer com dados da empresa + nº do contrato
		report.addFooterGrid().setBorder(Borders.None)
				.addRow()
				.addValue("LIV Assessoria Previdenciária LTDA · CNPJ 48.994.154/0001-72").setFontSize(7).sethAlignLeft()
				.addValue("Av. 4 de julho, 387 · Jereissati II · Maracanaú/CE").setFontSize(7).sethAlignRight();

		// Título principal
		report.addGrid().setBorder(Borders.None)
				.addRow()
				.addValue("CONTRATO DE PRESTAÇÃO DE SERVIÇOS DE ASSESSORIA PREVIDENCIÁRIA")
				.setFontBold().setFontSize(14).sethAlignCenter().setPaddings(0, 0, 20f, 0);

		// Contratante
		report.addGrid().setBorder(Borders.None)
				.addRow()
				.addValue("CONTRATANTE").setFontBold().setFontSize(11).setPaddings(0, 0, 6f, 0);

		report.addGrid().setBorder(Borders.None)
				.addRow()
				.addValue(montarParagrafoContratante())
				.setFontSize(10).sethAlignJustified().setPaddings(0, 0, 14f, 0);

		// Contratado
		report.addGrid().setBorder(Borders.None)
				.addRow()
				.addValue("CONTRATADO").setFontBold().setFontSize(11).setPaddings(0, 0, 6f, 0);

		report.addGrid().setBorder(Borders.None)
				.addRow()
				.addValue("LIV Assessoria Previdenciária LTDA, pessoa jurídica de direito privado, "
						+ "portadora do CNPJ nº 48.994.154/0001-72, com sede na Av. 4 de julho nº 387, "
						+ "Jereissati II, Maracanaú/CE, contato (85) 9 8628-5349, "
						+ "e-mail liv.assessoria.previdenciaria@outlook.com.")
				.setFontSize(10).sethAlignJustified().setPaddings(0, 0, 14f, 0);

		// Preâmbulo
		report.addGrid().setBorder(Borders.None)
				.addRow()
				.addValue("As partes acima identificadas têm, entre si, justo e acertado o presente Contrato "
						+ "de Prestação de Serviços de Assessoria Previdenciária, que se regerá pelas cláusulas "
						+ "seguintes e pelas condições descritas no presente instrumento.")
				.setFontSize(10).sethAlignJustified().setPaddings(0, 0, 20f, 0);

		// Cláusula 1
		clausula(report, "1. Objeto do contrato",
				"1.1. O presente contrato tem como objeto a prestação, pelo CONTRATADO, de serviços de "
						+ "assessoria previdenciária ao CONTRATANTE, no município de Maracanaú/CE.");

		// Cláusula 2
		clausula(report, "2. Valor e forma de pagamento",
				"2.1. O serviço consistirá em assessoria previdenciária na modalidade "
						+ beneficio.getModalidade() + " (" + beneficio.getDescricao() + "). O valor "
						+ "ajustado é de " + formatarMoeda(valor) + ", pago após a implementação do benefício. "
						+ "Caso não haja valores retroativos, será cobrado valor de entrada de 70% sobre o "
						+ "valor recebido, com saldo em parcelas fixas de R$ 600,00 conforme a data de "
						+ "recebimento do beneficiário. Aceitos pagamento em espécie, PIX, TED ou boleto bancário.");

		// Tabela de formas de pagamento
		report.addGrid().setBorder(Borders.None)
				.addColumn().setWidth(0.25f)
				.addColumn()
				.addRow()
				.addValue("PIX").setFontBold().setFontSize(9).setPaddings(0, 0, 6f, 0)
				.addValue("(85) 9 9958-2811 · Banco Itaú").setFontSize(9).setPaddings(0, 0, 6f, 0)
				.addRow()
				.addValue("Transferência").setFontBold().setFontSize(9).setPaddings(0, 0, 6f, 0)
				.addValue("Itaú Unibanco (341) · Agência 7979 · Conta 54046-0").setFontSize(9).setPaddings(0, 0, 6f, 0)
				.addRow()
				.addValue("Espécie").setFontBold().setFontSize(9).setPaddings(0, 0, 20f, 0)
				.addValue("Recebido no próprio escritório.").setFontSize(9).setPaddings(0, 0, 20f, 0);

		report.addGrid().setBorder(Borders.None)
				.addRow()
				.addValue("2.2. Este contrato tem força de título executivo e vale como protesto extrajudicial.")
				.setFontSize(10).sethAlignJustified().setPaddings(0, 0, 8f, 0);

		report.addGrid().setBorder(Borders.None)
				.addRow()
				.addValue("2.3. O acesso ao MEU INSS será restaurado e o requerente só terá acesso após o deferimento.")
				.setFontSize(10).sethAlignJustified().setPaddings(0, 0, 16f, 0);

		// Cláusula 3
		clausula(report, "3. Vigência e Rescisão",
				"3.1. O presente contrato inicia na data de sua assinatura em Maracanaú/CE, "
						+ formatarDataPorExtenso(inicio) + ", e terminará após o deferimento declarado "
						+ "pelo INSS. Em caso de rescisão antecipada por parte do CONTRATANTE, será "
						+ "cobrada multa no valor de 50% (cinquenta por cento) do valor de contratação "
						+ "do serviço, conforme cláusula 2.1.");

		// Cláusula 4 - tabela de obrigações
		report.addGrid().setBorder(Borders.None)
				.addRow()
				.addValue("4. Obrigações das partes").setFontBold().setFontSize(11).setPaddings(0, 0, 8f, 0);

		report.addGrid().setBorder(Borders.None)
				.addColumn("Contratante").setWidth(0.5f)
				.addColumn("Contratado").setWidth(0.5f)
				.addRow()
				.addValue("Efetuar o pagamento no prazo e na forma combinados.").setFontSize(9).sethAlignJustified().setPaddings(0, 8f, 6f, 0).setVAlignTop()
				.addValue("Executar os serviços previstos neste contrato.").setFontSize(9).sethAlignJustified().setPaddings(0, 0, 6f, 8f).setVAlignTop()
				.addRow()
				.addValue("Oferecer as informações necessárias para a execução dos serviços.").setFontSize(9).sethAlignJustified().setPaddings(0, 8f, 14f, 0).setVAlignTop()
				.addValue("Manter a confidencialidade das informações fornecidas pelo Contratante.").setFontSize(9).sethAlignJustified().setPaddings(0, 0, 14f, 8f).setVAlignTop();

		// Cláusula 5
		clausula(report, "5. Obrigações da Contratada",
				"5.1. A CONTRATADA se obriga a acompanhar todos os atos relacionados ao serviço de "
						+ "assessoria descrito na cláusula 2, executando tarefas necessárias para solução "
						+ "de problemas, de forma preventiva ou paliativa.");

		report.addGrid().setBorder(Borders.None)
				.addRow()
				.addValue("5.2. A CONTRATADA se obriga a utilizar técnicas condizentes com o serviço de "
						+ "assessoria a ser prestado, utilizando-se de todos os esforços para a sua consecução.")
				.setFontSize(10).sethAlignJustified().setPaddings(0, 0, 8f, 0);

		report.addGrid().setBorder(Borders.None)
				.addRow()
				.addValue("5.3. A CONTRATADA utilizará todo o seu corpo técnico para realização de pesquisa "
						+ "e desenvolvimento na área assessorada, nomeando um responsável para a administração "
						+ "das atividades.")
				.setFontSize(10).sethAlignJustified().setPaddings(0, 0, 24f, 0);

		// Local e data
		report.addGrid().setBorder(Borders.None)
				.addRow()
				.addValue("Maracanaú/CE, " + formatarDataPorExtenso(inicio) + ".")
				.setFontSize(10).sethAlignRight().setPaddings(0, 0, 36f, 0);

		// Bloco de assinaturas
		String nomeContratanteAssinatura = cliente.getRepresentante() == null
				? cliente.getContato().getNome().toUpperCase()
				: cliente.getRepresentante().getContato().getNome().toUpperCase();
		String cpfContratanteAssinatura = cliente.getRepresentante() == null
				? cliente.getCpf()
				: cliente.getRepresentante().getCpf();

		LayoutGrid assinaturas = report.addGrid().setBorder(Borders.None);
		assinaturas.addColumn().setWidth(0.5f);
		assinaturas.addColumn().setWidth(0.5f);
		assinaturas.addRow()
				.addValue("____________________________________").sethAlignCenter().setFontSize(9).setPaddings(0, 10f, 4f, 0)
				.addValue("____________________________________").sethAlignCenter().setFontSize(9).setPaddings(0, 0, 4f, 10f);
		assinaturas.addRow()
				.addValue(nomeContratanteAssinatura).sethAlignCenter().setFontBold().setFontSize(8).setPaddings(0, 10f, 2f, 0)
				.addValue("LIV ASSESSORIA PREVIDENCIÁRIA").sethAlignCenter().setFontBold().setFontSize(8).setPaddings(0, 0, 2f, 10f);
		assinaturas.addRow()
				.addValue("CPF " + formatarCPF(cpfContratanteAssinatura)).sethAlignCenter().setFontSize(8).setPaddings(0, 10f, 28f, 0)
				.addValue("CNPJ 48.994.154/0001-72").sethAlignCenter().setFontSize(8).setPaddings(0, 0, 28f, 10f);
		assinaturas.addRow()
				.addValue("____________________________________").sethAlignCenter().setFontSize(9).setPaddings(0, 10f, 4f, 0)
				.addValue("____________________________________").sethAlignCenter().setFontSize(9).setPaddings(0, 0, 4f, 10f);
		assinaturas.addRow()
				.addValue("TESTEMUNHA 1").sethAlignCenter().setFontBold().setFontSize(8).setPaddings(0, 10f, 0, 0)
				.addValue("TESTEMUNHA 2").sethAlignCenter().setFontBold().setFontSize(8).setPaddings(0, 0, 0, 10f);

		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			ReportExporterPDF.exportTo(report, baos);
			String filename = "contrato-" + numero + "-"
					+ cliente.getContato().getNome().replace(" ", "-") + ".pdf";
			return new Object[] { baos.toByteArray(), filename };
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void clausula(Report report, String titulo, String texto) {
		report.addGrid().setBorder(Borders.None)
				.addRow()
				.addValue(titulo).setFontBold().setFontSize(11).setPaddings(0, 0, 6f, 0);
		report.addGrid().setBorder(Borders.None)
				.addRow()
				.addValue(texto).setFontSize(10).sethAlignJustified().setPaddings(0, 0, 14f, 0);
	}

	private String montarParagrafoContratante() {
		String contratante;
		if (cliente.getRepresentante() == null) {
			contratante = cliente.getContato().getNome().toUpperCase()
					+ ", brasileiro(a), portador(a) do CPF nº " + formatarCPF(cliente.getCpf())
					+ ", contato (" + formatarTelefone(cliente.getContato().getTelefone()) + ")"
					+ ", residente e domiciliado(a) no endereço " + cliente.getEndereco().toString() + ".";
		} else {
			String menor = calcularIdade(cliente.getNascimento()) < 18 ? ", menor," : ",";
			contratante = cliente.getContato().getNome().toUpperCase() + menor
					+ " portador(a) do CPF nº " + formatarCPF(cliente.getCpf())
					+ ", neste ato representado(a) por "
					+ cliente.getRepresentante().getContato().getNome().toUpperCase()
					+ ", brasileiro(a), portador(a) do CPF nº "
					+ formatarCPF(cliente.getRepresentante().getCpf())
					+ ", contato (" + formatarTelefone(cliente.getRepresentante().getContato().getTelefone()) + ")"
					+ ", residente e domiciliado(a) no endereço " + cliente.getEndereco().toString() + ".";
		}
		return contratante;
	}

	private String formatarDataPorExtenso(Date data) {
		return new SimpleDateFormat("dd 'de' MMMM 'de' yyyy", new Locale("pt", "BR")).format(data);
	}

	private String formatarMoeda(BigDecimal valor) {
		if (valor == null) return "R$ 0,00";
		return String.format(new Locale("pt", "BR"), "R$ %,.2f", valor);
	}
	
	@Update
	public void update() {
		List<Processo> processos = repository.getAll("SELECT p FROM Processo p WHERE p.contrato.id = ?1", this.getId());
	    Processo primeiroProcesso = processos.stream().findFirst().orElse(null);
	    if (primeiroProcesso != null && inicio.compareTo(primeiroProcesso.getDataCriacao()) != 0) {
	    	primeiroProcesso.setDataCriacao(inicio);
	    	repository.add(primeiroProcesso);
	    }

	    if (conclusao != null) {
	        for (Processo processo : processos) {
	            if (!processo.getStatus().equals(Status.APROVADO) && !processo.getStatus().equals(Status.REPROVADO)) {
	                throw new DomainException("Data de conclusão do contrato deverá ser informada somente após o(s) processo(s) estiver(em) concluído(s).");
	            }
	        }
	    }

	    this.setNumero(gerarNumeroContrato(repository.get(Cliente.class, cliente.getId()).get().getCpf(), inicio));
	    repository.add(this);
	}
	
	@Delete
	public void delete() {
		repository.removeAll("DELETE FROM Processo p WHERE p.contrato.id = ?1", this.getId());
		repository.remove(this);
	}
	
	// @TODO: Adicionar funcionalidade para adicionar contratos retroativos
	
	@Inject
	@Transient
	private IRepository repository;
	
	private String gerarNumeroContrato(String cpf, Date data) {
        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyy");
        String dataFormatada = sdf.format(data);
        
        String ultimos5DigitosCpf = cpf.substring(cpf.length() - 5);
        
        return dataFormatada + ultimos5DigitosCpf;
    }
	
	private String formatarTelefone(String numero) {
        if (numero != null && numero.length() == 11) {
            return "(" + numero.substring(0, 2) + ") " + numero.charAt(2) + " " + numero.substring(3, 7) + "-" + numero.substring(7);
        } else {
            throw new IllegalArgumentException("Número inválido. O número deve ter 11 dígitos.");
        }
    }
	
	private int calcularIdade(Date dataNascimento) {
        Calendar hoje = Calendar.getInstance();
        
        Calendar nascimento = Calendar.getInstance();
        nascimento.setTime(dataNascimento);
        
        int idade = hoje.get(Calendar.YEAR) - nascimento.get(Calendar.YEAR);
        
        if (hoje.get(Calendar.MONTH) < nascimento.get(Calendar.MONTH) || 
            (hoje.get(Calendar.MONTH) == nascimento.get(Calendar.MONTH) && hoje.get(Calendar.DAY_OF_MONTH) < nascimento.get(Calendar.DAY_OF_MONTH))) {
            idade--;
        }
        
        return idade;
    }
	
	private String formatarCPF(String cpf) {
        if (cpf != null && cpf.length() == 11) {
            return String.format("%s.%s.%s-%s", 
                                 cpf.substring(0, 3), 
                                 cpf.substring(3, 6), 
                                 cpf.substring(6, 9), 
                                 cpf.substring(9, 11));
        } else {
            throw new IllegalArgumentException("CPF inválido. Deve ter 11 dígitos.");
        }
    }

	public Contrato() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public String getNumero() {
		return numero;
	}

	public void setNumero(String numero) {
		this.numero = numero;
	}

	public String getIndicacao() {
		return indicacao;
	}

	public void setIndicacao(String indicacao) {
		this.indicacao = indicacao;
	}

	public BigDecimal getValor() {
		return valor;
	}

	public void setValor(BigDecimal valor) {
		this.valor = valor;
	}

	public Modalidade getBeneficio() {
		return beneficio;
	}

	public void setBeneficio(Modalidade beneficio) {
		this.beneficio = beneficio;
	}

	public Date getInicio() {
		return inicio;
	}

	public void setInicio(Date inicio) {
		this.inicio = inicio;
	}

	public Date getConclusao() {
		return conclusao;
	}

	public void setConclusao(Date conclusao) {
		this.conclusao = conclusao;
	}

	public IRepository getRepository() {
		return repository;
	}

	public void setRepository(IRepository repository) {
		this.repository = repository;
	}

}
