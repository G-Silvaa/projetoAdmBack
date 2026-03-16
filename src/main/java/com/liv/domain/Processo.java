package com.liv.domain;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.util.ddd.annotation.Update;
import javax.util.ddd.domain.DomainException;
import javax.util.ddd.domain.IRepository;

import org.greport.Borders;
import org.greport.Report;
import org.greport.pdf.ReportExporterPDF;

import jakarta.transaction.Transactional;
import jakarta.persistence.Column;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "processos", uniqueConstraints = {
		@UniqueConstraint(name = "processos_uk_numero_protocolo", columnNames = "numero_protocolo")
})
public class Processo {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "numero_protocolo", length = 15)
	private String numeroProtocolo;

	@NotNull(message = "Informe o status")
	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private Status status;

	@Column(name = "documentos_pendentes", length = 255)
	private String documentosPendentes;

	@NotNull(message = "Informe o contrato")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "contrato_id", foreignKey = @ForeignKey(name = "processo_fk_contrato"))
	private Contrato contrato;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(columnDefinition = "timestamp")
	private Date periciaMedica;

	@Column(name = "endereco_pericia_medica", length = 255)
	private String enderecoPericiaMedica;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(columnDefinition = "timestamp")
	private Date avaliacaoSocial;

	@Column(name = "endereco_avaliacao_social", length = 255)
	private String enderecoAvaliacaoSocial;

	@Temporal(TemporalType.DATE)
	@Column(columnDefinition = "date")
	private Date entradaDoProtocolo;

	// @TODO: Alerta pra 60 dias
	@Temporal(TemporalType.DATE)
	@Column(name = "data_concessao", columnDefinition = "date")
	private Date dataConcessao;

	// data de cessação é somente para o benefício 31
	@Temporal(TemporalType.DATE)
	@Column(name = "cessacao", columnDefinition = "date")
	private Date cessacao;
	
	// @TODO: isso é somente informado quando o benefício é concedido na modalidade 31
	private BigDecimal valorConcedido;

	// @TODO: Adicionar esse campo ao script
	@Temporal(TemporalType.DATE)
	@Column(name = "data_criacao", columnDefinition = "date")
	private Date dataCriacao;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(columnDefinition = "timestamp", nullable = false)
	private Date ultimaAtualizacao;

	@PrePersist
	public void onPrePersist() {
		status = Status.AGUARDANDO;
		numeroProtocolo = null;
		ultimaAtualizacao = new Date();
		dataCriacao = new Date();
		documentosPendentes = null;
		periciaMedica = null;
		avaliacaoSocial = null;
		entradaDoProtocolo = null;
		dataConcessao = null;
		cessacao = null;
		enderecoPericiaMedica = null;
		enderecoAvaliacaoSocial = null;
		valorConcedido = null;
	}

	// Aqui irá conter a lógica de atualização do status do processo,
	// datas de perícia médica, avaliação social e entrada do protocolo
	@PreUpdate
	public void onPreUpdate() {
		if ((status.equals(Status.ANALISE) || status.equals(Status.CUMPRIMENTO_EXIGENCIA)
				|| status.equals(Status.ANALISE_ADMINISTRATIVA) || status.equals(Status.APROVADO)
				|| status.equals(Status.REPROVADO)) && (entradaDoProtocolo == null || numeroProtocolo == null)) {
			throw new DomainException("É necessário dar entrada do protocolo");
		}

		// @TODO: adicionar alerta (página inicial)
		// Status PENDENTE: informar qual o tipo de pendência para entrar em contato com
		// o cliente
		// Benefício, modalidade 31: a data de concessão (para entrar com novo atest
		// med)
		// 60 dias após concluído para digitar empréstimo
		if (status.equals(Status.PENDENTE) && (documentosPendentes == null || documentosPendentes.isBlank())) {
			throw new DomainException("Informe os documentos pendentes");
		} else if(status.equals(Status.CUMPRIMENTO_EXIGENCIA) && (documentosPendentes == null || documentosPendentes.isBlank())) {
			throw new DomainException("Informe os documentos à serem solicitados");
		} else if (!status.equals(Status.PENDENTE) && !status.equals(Status.CUMPRIMENTO_EXIGENCIA)) {
			documentosPendentes = null;
		}
		
		if(status.equals(Status.ANALISE_ADMINISTRATIVA) && periciaMedica == null) {
			throw new DomainException("É necessário ter perícia médica para entrar em análise administrativa");
		}

		if (!contrato.getBeneficio().getModalidade().equals("87") && avaliacaoSocial != null) {
			throw new DomainException("Não é necessário avaliação social para este benefício");
		}

		// @TODO: atest med é uma data pra modalidade 31
		if (!contrato.getBeneficio().getModalidade().equals("87")
				&& !contrato.getBeneficio().getModalidade().equals("31")
				&& periciaMedica != null) {
			throw new DomainException("Perícia médica só é necessário para as modalidades 87 e 31");
		}

		if (contrato.getBeneficio().equals(Modalidade.AUXILIO_INCAPACIDADE_TEMPORARIA) && status.equals(Status.APROVADO)
				&& cessacao == null) {
			throw new DomainException("É necessário informar a data de cessação para esta modalidade!");
		}

		// @TODO: Adicionar em que período a avaliação social e perícia médica
		// podem ser adicionadas

		if (avaliacaoSocial == null && enderecoAvaliacaoSocial != null) {
			throw new DomainException("Informe a data da avaliação social");
		}

		if (avaliacaoSocial != null && enderecoAvaliacaoSocial == null) {
			throw new DomainException("Informe o endereço da avaliação social");
		}

		if (periciaMedica == null && enderecoPericiaMedica != null) {
			throw new DomainException("Informe a data da perícia médica");
		}

		if (periciaMedica != null && enderecoPericiaMedica == null) {
			throw new DomainException("Informe o endereço da perícia médica");
		}

		if (status.equals(Status.APROVADO) && dataConcessao == null) {
			throw new DomainException("Informe a data de concessão desse processo!");
		} else if (!status.equals(Status.APROVADO)) {
			dataConcessao = null;
		}
		
		if(!contrato.getBeneficio().equals(Modalidade.AUXILIO_INCAPACIDADE_TEMPORARIA) && cessacao != null) {
			throw new DomainException("Data de cessação do benefício só é necessária para a modalidade 31");
		}
		
		if(!contrato.getBeneficio().equals(Modalidade.AUXILIO_INCAPACIDADE_TEMPORARIA) && valorConcedido != null) {
			throw new DomainException("Valor concedido só deve ser informado ao benefício da modalidade 31");
		}
		
		if(!status.equals(Status.APROVADO) && cessacao != null) {
			throw new DomainException("Data de cessação deve ser informada após benefício ser concedido");
		}
		
		if(!status.equals(Status.APROVADO) && valorConcedido != null) {
			throw new DomainException("Valor concedido só deve ser informado após benefício ser concedido");
		}
		
		if(dataConcessao != null && cessacao != null) {
			if(dataConcessao.after(cessacao)) {
				throw new DomainException("Data de cessação está cronologicamente errada em relação à data de concessão. Data de cessação deve está após à data de concessão.");
			}
		}
		
		if(entradaDoProtocolo != null && avaliacaoSocial != null) {
			if(entradaDoProtocolo.after(avaliacaoSocial)) {
				throw new DomainException("Data de avaliação social está cronologicamente errada em relação à data de entrada do protocolo. Data de avaliação social deve está após à data de entrada do protocolo.");
			}
		}
		
		if(entradaDoProtocolo != null && periciaMedica != null) {
			if(entradaDoProtocolo.after(periciaMedica)) {
				throw new DomainException("Data de perícia médica está cronologicamente errada em relação à data de entrada do protocolo. Data de perícia médica deve está após à data de entrada do protocolo.");
			}
		}
		
		if(entradaDoProtocolo != null && dataConcessao != null) {
			if(entradaDoProtocolo.after(dataConcessao)) {
				throw new DomainException("Data de concessão do benefício está cronologicamente errada em relação à data de entrada do protocolo. Data de concessão deve está após à data de entrada do protocolo.");
			}
		}
		
		if(entradaDoProtocolo != null && cessacao != null) {
			if(entradaDoProtocolo.after(cessacao)) {
				throw new DomainException("Data de cessação do benefício está cronologicamente errada em relação à data de entrada do protocolo. Data de cessação deve está após à data de entrada do protocolo.");
			}
		}
		
		if(avaliacaoSocial != null && dataConcessao != null) {
			if(avaliacaoSocial.after(dataConcessao)) {
				throw new DomainException("Data de concessão do benefício está cronologicamente errada em relação à data de avaliação social. Data de concessão deve está após à data de avaliação social.");
			}
		}
		
		if(avaliacaoSocial != null && cessacao != null) {
			if(avaliacaoSocial.after(cessacao)) {
				throw new DomainException("Data de cessação do benefício está cronologicamente errada em relação à data de avaliação social. Data de cessação deve está após à data de avaliação social.");
			}
		}
		
		if(periciaMedica != null && dataConcessao != null) {
			if(periciaMedica.after(dataConcessao)) {
				throw new DomainException("Data de concessão do benefício está cronologicamente errada em relação à data de perícia médica. Data de concessão deve está após à data de perícia médica.");
			}
		}
		
		if(periciaMedica != null && cessacao != null) {
			if(periciaMedica.after(cessacao)) {
				throw new DomainException("Data de cessação do benefício está cronologicamente errada em relação à data de perícia médica. Data de cessação deve está após à data de perícia médica.");
			}
		}
		
		if(dataCriacao != null && entradaDoProtocolo != null) {
			if(dataCriacao.after(entradaDoProtocolo)) {
				throw new DomainException("Data de entrada do protocolo está cronologicamente errada em relação à data de criação deste processo. Data de entrada do protocolo deve está após à data de criação do processo.");
			}
		}
		
		if(dataCriacao != null && cessacao != null) {
			if(dataCriacao.after(cessacao)) {
				throw new DomainException("Data de cessação está cronologicamente errada em relação à data de criação deste processo. Data de cessação deve está após à data de criação do processo.");
			}
		}
		
		if(dataCriacao != null && dataConcessao != null) {
			if(dataCriacao.after(dataConcessao)) {
				throw new DomainException("Data de concessão está cronologicamente errada em relação à data de criação deste processo. Data de concessão deve está após à data de criação do processo.");
			}
		}
		
		if(dataCriacao != null && periciaMedica != null) {
			if(dataCriacao.after(periciaMedica)) {
				throw new DomainException("Data de perícia médica está cronologicamente errada em relação à data de criação deste processo. Data de perícia médica deve está após à data de criação do processo.");
			}
		}
		
		if(dataCriacao != null && avaliacaoSocial != null) {
			if(dataCriacao.after(avaliacaoSocial)) {
				throw new DomainException("Data de avaliação social está cronologicamente errada em relação à data de criação deste processo. Data de avaliação social deve está após à data de criação do processo.");
			}
		}

		ultimaAtualizacao = new Date();
	}
	
	// @TODO: Adicionar mecanismo lá no front que adiciona processo à um contrato com base no CPF e modalidade
	// e editar a data de criação do processo
	@Update
	@Transactional
	public void update() {
		List<Processo> processos = repository.getAll("SELECT p FROM Processo p WHERE p.contrato.id = ?1", this.getContrato().getId());
		
		if(processos.size() > 1) {
		    Processo processoMaisRecente = null;

		    for (Processo processo : processos) {
		        if (processoMaisRecente == null || processo.getDataCriacao().after(processoMaisRecente.getDataCriacao())) {
		            processoMaisRecente = processo;
		        }
		    }

		    boolean isProcessoAtualMaisRecente = this.getId() == processoMaisRecente.getId();

		    if ((!isProcessoAtualMaisRecente && processoMaisRecente.getStatus().equals(Status.APROVADO)) || (!isProcessoAtualMaisRecente)) {
		        throw new DomainException("Não é possível editar este contrato, pois ele foi pré-condição para que o contrato "
		                + "fosse renovado. Remover processo mais recente.");
		    }
		}
		
		long processosComNumeroProtocolo = repository.size("FROM Processo p WHERE p.numeroProtocolo = ?1 AND p.id <> ?2", this.getNumeroProtocolo(), this.getId());
			
		if(processosComNumeroProtocolo > 0) {
			throw new DomainException("Processo já existente! Já existe um número de protocolo igual à este.");
		}
		
		for(Processo processo : processos) {
			if(this.getId() == processo.getId() && this.getPericiaMedica() != null && processo.getPericiaMedica() == null) {
				Financeiro financeiro = new Financeiro();
				financeiro.setContrato(this.getContrato());
				financeiro.setValorTotalPagar(this.getContrato().getValor());
				repository.add(financeiro);
			}
		}
		
		repository.add(this);
	}

	// @TODO: Criar uma maneira de deixar flexível essa troca do número de telefone
	// para contato
	// PATCH /liv-api/domain/processo/{id}/carta-de-pericia-medica
	/*
	 * body:
	 * { }
	 */
	public Object[] cartaDePericiaMedica() {
	    if (periciaMedica == null) {
	        throw new DomainException("É necessário ter agendado perícia médica");
	    }

	    Report report = new Report();
	    report.setBackgroundFileName(getClass().getResource("/static/img/background.jpeg").getFile());

	    String dataFormatada = formatarData(periciaMedica);

	    String nomeCliente = contrato.getCliente().getContato().getNome();
	    String endereco = enderecoPericiaMedica.toUpperCase();

	    report.addGrid().setBorder(Borders.None)
	            .addRow()
	            .addValue("\n\n\n\n\n\n\n\n\n\n\n\n\n\nMaracanaú/CE").setPaddings(0, 60, 0, 60)
	            .addRow()
	            .addValue("\n\nPrezado(a), " + nomeCliente + ". O escritório LIV Assessoria Previdenciária, vem por este meio informar")
	            .setPaddings(0, 60, 0, 60).sethAlignJustified()
	            .addRow()
	            .addValue("\n\nPERÍCIA MÉDICA MARCADA PARA O DIA: " + dataFormatada + ", NO ENDEREÇO " + endereco + ".")
	            .setFontSize(12).setPaddings(0, 60, 0, 60).sethAlignJustified()
	            .addRow()
	            .addValue("\n\nOBS.: Levar RG, CPF e todos os documentos médicos e chegar com 30 minutos de antecedência.")
	            .setPaddings(0, 60, 0, 60).sethAlignJustified()
	            .addRow()
	            .addValue("\n\n\n\n\"EM CASO DE AUSÊNCIA, ENTRAR EM CONTATO COM 3 DIAS DE ANTECEDÊNCIA\"")
	            .sethAlignCenter()
	            .addRow()
	            .addValue("\n\n\nDemais esclarecimentos, entrar em contato pelo telefone (85) 98628-5349")
	            .sethAlignCenter()
	            .addRow()
	            .addValue("\n\nAtenciosamente, LIV Assessoria Previdenciária").sethAlignCenter();

	    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
	        ReportExporterPDF.exportTo(report, baos);
	        return new Object[]{
	                baos.toByteArray(),
	                "carta-pericia-medica-" + nomeCliente.replace(" ", "-") + ".pdf"
	        };
	    } catch (Exception e) {
	        throw new RuntimeException("Erro ao gerar a carta de perícia médica.", e);
	    }
	}

	// @TODO: Criar uma maneira de deixar flexível essa troca do número de telefone
	// para contato
	// PATCH /liv-api/domain/processo/{id}/carta-de-avaliacao-social
	/*
	 * body:
	 * { }
	 */
	public Object[] cartaDeAvaliacaoSocial() {
	    if (avaliacaoSocial == null) {
	        throw new DomainException("É necessário ter agendado avaliação social");
	    }

	    Report report = new Report();
	    report.setBackgroundFileName(getClass().getResource("/static/img/background.jpeg").getFile());

	    String dataFormatada = formatarData(avaliacaoSocial);

	    String nomeCliente = contrato.getCliente().getContato().getNome();
	    String endereco = enderecoAvaliacaoSocial.toUpperCase();

	    report.addGrid().setBorder(Borders.None).addRow()
	            .addValue("\n\n\n\n\n\n\n\n\n\n\n\n\n\nMaracanaú/CE")
	            .setPaddings(0, 60, 0, 60)
	            .addRow()
	            .addValue("\n\nPrezado(a), " + nomeCliente + ". O escritório LIV Assessoria Previdenciária, vem por este meio informar")
	            .setPaddings(0, 60, 0, 60).sethAlignJustified()
	            .addRow()
	            .addValue("\n\nAVALIAÇÃO SOCIAL MARCADA PARA O DIA: " + dataFormatada + ", NO ENDEREÇO " + endereco + ".")
	            .setFontSize(12).setPaddings(0, 60, 0, 60).sethAlignJustified()
	            .addRow()
	            .addValue("\n\nOBS.: Levar RG, CPF e todos os documentos médicos e chegar com 30 minutos de antecedência.")
	            .setPaddings(0, 60, 0, 60).sethAlignJustified()
	            .addRow()
	            .addValue("\n\n\n\n\"EM CASO DE AUSÊNCIA, ENTRAR EM CONTATO COM 3 DIAS DE ANTECEDÊNCIA\"")
	            .sethAlignCenter()
	            .addRow()
	            .addValue("\n\n\nDemais esclarecimentos, entrar em contato pelo telefone (85) 98628-5349")
	            .sethAlignCenter()
	            .addRow()
	            .addValue("\n\nAtenciosamente, LIV Assessoria Previdenciária").sethAlignCenter();

	    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
	        ReportExporterPDF.exportTo(report, baos);
	        return new Object[]{
	                baos.toByteArray(),
	                "carta-avaliacao-social-" + nomeCliente.replace(" ", "-") + ".pdf"
	        };
	    } catch (Exception e) {
	        throw new RuntimeException("Erro ao gerar a carta de avaliação social.", e);
	    }
	}

	// PATCH /liv-api/domain/processo/{id}/carta-de-concessao
	/*
	 * body:
	 * { }
	 */
	public Object[] cartaDeConcessao() {
	    if (!status.equals(Status.APROVADO)) {
	        throw new DomainException("É necessário que o benefício em processo se enquadre como concedido");
	    }

	    Report report = new Report();
	    report.setBackgroundFileName(getClass().getResource("/static/img/background.jpeg").getFile());

	    String saudacao = "Prezado(a) S.r (a), " + contrato.getCliente().getContato().getNome();
	    String detalhesConcessao = getDetalhesConcessao();
	    
	    report.addGrid().setBorder(Borders.None)
	        .addRow().addValue("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n" + saudacao).setPaddings(0, 60, 0, 60)
	        .addRow().addValue("\n\n\nInformamos que o seu benefício foi concedido, conforme detalhes abaixo:")
	        .setPaddings(0, 60, 0, 60)
	        .addRow().addValue(detalhesConcessao).setPaddings(0, 60, 0, 60).sethAlignJustified()
	        .addRow().addValue("\n\n\n\n\n_____________________________________________________").sethAlignCenter()
	        .addRow().addValue("LIV Assessoria Previdenciária").sethAlignCenter();

	    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
	        ReportExporterPDF.exportTo(report, baos);
	        return new Object[] { baos.toByteArray(), "carta-de-concessao-"
	                + contrato.getCliente().getContato().getNome().replace(" ", "-") + ".pdf" };
	    } catch (Exception e) {
	        throw new RuntimeException("Erro ao gerar carta de concessão", e);
	    }
	}
	
	@Inject
	@Transient
	private IRepository repository;
	
	// @TODO: testar implementação
	private String getDetalhesConcessao() {
	    String modalidade = contrato.getBeneficio().getModalidade();
	    switch (modalidade) {
	        case "88":
	            return "\n\nEm atenção ao requerimento de Benefício de Prestação Continuada da Assistência Social ao idoso, a "
	                    + "Previdência Social comunica que foi reconhecido o direito ao benefício. O beneficiário deve manter o CadUnico "
	                    + "atualizado, não podendo passar mais de 2 anos sem efetuar atualização, conforme disposto no Decreto n 11.016, de 2022.";
	        case "87":
	            return "\n\nEm atenção ao requerimento de Benefício de Prestação Continuada da Assistência Social ao deficiente, "
	                    + "a Previdência Social comunica que foi reconhecido o direito ao benefício, em razão de a renda mensal bruta familiar, "
	                    + "dividida pelo número de seus integrantes, atender ao critério de miserabilidade para renda mensal familiar per capita "
	                    + "de 1/4 do salário mínimo vigente na data do requerimento e do enquadramento do interessado como pessoa com deficiência "
	                    + "nos termos do artigo 20, 2 e 10, da Lei 8.742, de 1993.";
	        case "41":
	            return "\n\nEm atenção ao requerimento de Aposentadoria por idade, a Previdência Social comunica que foi reconhecido o "
	                    + "direito, pelas regras de transição com base no Artigo 18 da EC103/2019 – idade mínima e Tempo de Contribuição.";
	        case "31":
	            return "\n\nO benefício de Auxílio por Incapacidade Temporária foi concedido. O início do benefício foi fixado em "
	                    + new SimpleDateFormat("dd/MM/yyyy").format(dataConcessao)
	                    + " e a cessação será em "
	                    + new SimpleDateFormat("dd/MM/yyyy").format(cessacao);
	        case "21":
	            return "\n\nTrata-se do Benefício de Pensão por Morte Urbana Concedido em razão de ter ficado comprovada a Qualidade "
	                    + "de Segurado do(a) Instituidor(a), que se encontrava em atividade ou em período de manutenção dessa condição data do "
	                    + "óbito; e de ter ficado comprovada a condição de Dependente.";
	        default:
	            return "\n\nEm atenção ao requerimento de "
	                    + Modalidade.fromBeneficio(contrato.getBeneficio().getModalidade()).getDescricao()
	                    + ". A Previdência Social comunica que foi reconhecido o direito ao benefício por "
	                    + contrato.getBeneficio().getDescricao() + ".";
	    }
	}
	
	private String formatarData(Date data) {
	    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy 'às' HH:mm");
	    return dateFormat.format(data);
	}

}