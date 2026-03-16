package com.liv.domain;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
		
		report.addGrid().setBorder(Borders.None)
		.addRow()
		.addValue("CONTRATO\n"
				+ "Prestação de Serviços\n"
				+ "Previdenciário").setFontSize(20).setPaddings(0, 0, 0, 15f)
		.addRow()
		.addValue("\nContratante").setFontSize(14).setPaddings(0, 0, 0, 30f);
		
		if(cliente.getRepresentante() == null) {
			report.addGrid().setBorder(Borders.None)
			.addRow()
			.addValue(cliente.getContato().getNome().toUpperCase() + ", brasileiro(a), do lar, portador(a) do CPF de N. " +
					formatarCPF(cliente.getCpf()) + ", com contato de N. " + formatarTelefone(cliente.getContato().getTelefone()) + ", "
							+ "residente e domiciliado(a) no endereço " + cliente.getEndereco().toString() + ".").setPaddings(0, 15f, 0, 60f).sethAlignJustified().setFontSize(12);
		} else {
			String complemento = cliente.getContato().getNome().toUpperCase() + ",";
			
			if(calcularIdade(cliente.getNascimento()) < 18) {
				complemento = complemento.concat(" menor,"); 
			}
			
			report.addGrid().setBorder(Borders.None)
			.addRow()
			.addValue(complemento + " portador(a) do CPF de N. " + formatarCPF(cliente.getCpf()) + ", neste ato representado(a) por " + cliente.getRepresentante().getContato().getNome().toUpperCase() + ", "
					+ "brasileiro(a), do lar, portador(a) do CPF de N. " + formatarCPF(cliente.getRepresentante().getCpf()) + ", com contato de N. " + 
					formatarTelefone(cliente.getRepresentante().getContato().getTelefone()) + ", " + "residente e domiciliado(a) no endereço " 
					+ cliente.getEndereco().toString() + ".").setPaddings(0, 15f, 0, 60f).sethAlignJustified().setFontSize(12);
		}
		
		report.addGrid().setBorder(Borders.None)
		.addRow()
		.addValue("\n\nContratado").setFontSize(14).setPaddings(0, 0, 0, 30f)
		.addRow()
		.addValue("LIV Assessoria Previdenciária LTDA, portadora do CNPJ de N. 48.994.154/0001-72, com sede no endereço "
				+ "Av. 4 de julho N. 387, Jereissati II, Maracanaú/CE, com contato de N. (85) 9 8628-5349, "
				+ "e-mail: liv.assessoria.previdenciaria@outlook.com.").setPaddings(0, 15f, 0, 60f).sethAlignJustified().setFontSize(12)
		.addRow()
		.addValue("\n\nAs partes acima identificadas têm, entre si, justo e acertado o presente Contrato de Prestação de Serviços de "
				+ "Assessoria Previdenciária, que se regerá pelas cláusulas seguintes e pelas condições descritas no presente.").setPaddings(0, 15f, 0, 30f).sethAlignJustified().setFontSize(12)
		.addRow()
		.addValue("\n1. Objeto do contrato").setFontSize(14).setPaddings(0, 0, 0, 30f)
		.addRow()
		.addValue("1.1. Cláusula 1ª. O presente contrato tem como OBJETO, a prestação, pelo CONTRATADO, de serviços "
				+ "de assessoria ao CONTRATANTE, em seu estabelecimento comercial, localizado no município de Maracanaú no Estado "
				+ "do Ceará.").setPaddings(0, 15f, 0, 40f).sethAlignJustified().setFontSize(12)
		.addRow()
		.addValue("\n2. Valor e forma de pagamento").setFontSize(14).setPaddings(0, 0, 0, 30f)
		.addRow()
		.addValue("2.1. Cláusula 2ª. O presente serviço, consistirá em assessoria previdenciária na modalidade "
				+ beneficio.getModalidade() + "benefício de " + beneficio.getDescricao() + ". Com o valor a ser cobrado "
				+ "na importância de R$ " + valor + ". Pago após a implementação do benefício. Caso não haja valores retroativos "
				+ "do valor integral do contrato, será cobrado um valor de entrada de 70% sobre o valor que sair, o restante "
				+ "vindo com parcelas fixas de R$ 600,00 pagos conforme a data de recebimento do beneficiário. Sendo assim, "
				+ "efetuando pagamento em espécie, PIX, TED ou boleto bancário.").setPaddings(0, 15f, 0, 40f).sethAlignJustified().setFontSize(12)
		.addRow()
		.addValue("PIX: (85) 9 9958-2811. (Banco Itaú)").setPaddings(0, 0, 0, 40f).setFontSize(12)
		.addRow()
		.addValue("TRANSFERÊNCIA BANCÁRIA: Itaú Unibanco (341) / Agência: 7979 / Conta: 54046-0.").setPaddings(0, 0, 0, 40f).setFontSize(12)
		.addRow()
		.addValue("ESPÉCIE: No próprio escritório.").setPaddings(0, 0, 0, 40f).setFontSize(12)
		.addRow()
		.addValue("2.2. Cláusula 2ª. Este contrato tem força de titulo executivo e vale como protesto extra judicial.").setPaddings(0, 15f, 0, 40f).setFontSize(12)
		.addRow()
		.addValue("2.3. Cláusula 2ª. O acesso ao MEU INSS será restaurado, o requerente só terá acesso após o deferimento.").setPaddings(0, 15f, 0, 40f).setFontSize(12)
		.addRow()
		.addValue("\n3. Vigência e Rescisão").setFontSize(14).setPaddings(0, 0, 0, 30f)
		.addRow()
		.addValue("3.1. O presente contrato inicia na data de sua assinatura em Maracanaú/CE, " + new SimpleDateFormat("dd/MM/yyyy").format(inicio) + " e "
				+ "terminará após o deferimento declarado pelo INSS (Instituto Nacional de Seguro Social). Em caso de rescisão antecipada "
				+ "por parte do CONTRATANTE, será cobrado multa no valor de 50% (cinquenta por cento) do valor de contratação do serviço. "
				+ "Conforme está na cláusula 2ª em 2.1.").setPaddings(0, 15f, 0, 30f).sethAlignJustified().setFontSize(12)
		.addRow()
		.addValue("\n\n4. Obrigações das").setFontSize(14).setPaddings(0, 0, 0, 30f);
		
		report.addGrid().setBorder(Borders.None)
		.addColumn()
		.addColumn()
		.addValue("partes Contratantes").setFontSize(14).setPaddings(0, 0, 0, 50f)
		.addValue("Contratado").setFontSize(14).setPaddings(0, 0, 0, 50f)
		.addRow()
		.addValue("Efetuar o pagamento no prazo e conforme combinados;").setPaddings(0, 30f, 0, 80f).sethAlignJustified().setFontSize(12)
		.addValue("Executar os serviços previstos neste contrato;").setPaddings(0, 30f, 0, 80f).sethAlignJustified().setFontSize(12)
		.addRow()
		.addValue("Oferecer as informações necessárias para que o contratado possa executar os serviços previstos neste contrato.").setPaddings(20, 30f, 0, 80f).setVAlignTop().sethAlignJustified().setFontSize(12)
		.addValue("Manter a confidencialidade das informações fornecidas pela contratante.").setPaddings(20, 30f, 0, 80f).setVAlignTop().sethAlignJustified().setFontSize(12);
		
		report.addGrid().setBorder(Borders.None)
		.addRow()
		.addValue("\n\n5. Obrigações da contratada").setFontSize(14).setPaddings(0, 0, 0, 30f)
		.addRow()
		.addValue("5.1. A CONTRATADA se obriga a acompanhar todos os atos relacionados com o serviço de assessoria descrito na cláusula 2ª, executando "
				+ "tarefas necessárias para solução de problemas, de forma preventiva ou paliativa, nos moldes dos parágrafos seguintes.").setPaddings(0, 15f, 0, 40f).sethAlignJustified().setFontSize(12)
		.addRow()
		.addValue("5.2. Parágrafo primeiro. A CONTRATADA se obriga a utilizar técnicas condizentes com o serviço de assessoria a ser prestado "
				+ ", utilizando-se de todos os esforços para a sua consecução.").setPaddings(0, 15f, 0, 40f).sethAlignJustified().setFontSize(12)
		.addRow()
		.addValue("5.3. Parágrafo segundo. A CONTRATADA utilizará de todo o seu corpo técnico para a realização de pesquisa "
				+ "e desenvolvimento na área assessorada, bem como para a solução e prevenção de eventuais problemas, nomeando "
				+ "um responsável para a administração das atividades.").setPaddings(0, 15f, 0, 40f).sethAlignJustified().setFontSize(12)
		.addRow()
		.addValue("\n\n\n");
		
		LayoutGrid grid = report.addGrid();
		
		grid.setBorder(Borders.None)
		.addColumn()
		.addColumn()
		.addRow()
		.addValue("\n\n")
		.addValue()
		.addRow()
		.addValue("___________________________________________").setPaddings(0, 15f, 0, 30f).sethAlignCenter()
		.addValue("___________________________________________").setPaddings(0, 15f, 0, 30f).sethAlignCenter();
		
		if(cliente.getRepresentante() == null) {
			grid.addRow()
			.addValue(cliente.getContato().getNome().toUpperCase()).setFontSize(8).sethAlignCenter()
			.addValue("LIV ASSESSORIA PREVIDENCIÁRIA").setFontSize(8).sethAlignCenter()
			.addRow()
			.addValue("CPF: " + formatarCPF(cliente.getCpf())).setFontSize(8).sethAlignCenter()
			.addValue("48.994.154/0001-72").setFontSize(8).sethAlignCenter();
		} else {
			grid.addRow()
			.addValue(cliente.getRepresentante().getContato().getNome().toUpperCase()).setFontSize(8).sethAlignCenter()
			.addValue("LIV ASSESSORIA PREVIDENCIÁRIA").setFontSize(8).sethAlignCenter()
			.addRow()
			.addValue("CPF: " + formatarCPF(cliente.getRepresentante().getCpf())).setFontSize(8).sethAlignCenter()
			.addValue("48.994.154/0001-72").setFontSize(8).sethAlignCenter();
		}
		
		grid.addRow()
		.addValue("\n\n\n")
		.addValue()
		.addRow()
		.addValue("___________________________________________").sethAlignCenter()
		.addValue("___________________________________________").sethAlignCenter()
		.addRow()
		.addValue("TESTEMUNHA 1").setFontSize(8).sethAlignCenter()
		.addValue("TESTEMUNHA 2").setFontSize(8).sethAlignCenter();
		
		report.addGrid().setBorder(Borders.None)
		.addRow()
		.addValue("\n\nMaracanaú/CE, " + new SimpleDateFormat("dd/MM/yyyy").format(inicio)).setFontSize(7).sethAlignCenter()
		.addRow()
		.addValue("LIV Assessoria Previdenciária LTDA,").setFontSize(7).sethAlignCenter()
		.addRow()
		.addValue("48.994.154/0001-72").setFontSize(7).sethAlignCenter()
		.addRow()
		.addValue("Av. 4 de julho n. 387, Jereissati I, Maracanaú/CE.").setFontSize(7).sethAlignCenter()
		.addRow()
		.addValue("Contrato de N. " + numero).setFontSize(7).sethAlignCenter();

		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			ReportExporterPDF.exportTo(report, baos);
			System.out.println(Base64.getEncoder().encodeToString(baos.toByteArray()));
			return new Object[] { baos.toByteArray(), "contrato-" + cliente.getContato().getNome().replace(" ", "-") + ".pdf" };
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
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
