package com.liv.domain;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Date;

import javax.inject.Inject;
import javax.util.ddd.domain.DomainException;
import javax.util.ddd.domain.IRepository;

import org.greport.Borders;
import org.greport.Report;
import org.greport.pdf.ReportExporterPDF;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.zxing.WriterException;
import com.liv.infra.bbpay.Solicitacoes;
import com.liv.infra.bbpay.models.SolicitacaoGeral;
import com.liv.infra.bbpay.models.SolicitacaoPagamentoResponse;
import com.liv.infra.bbpay.models.SolicitacoesDevedor;
import com.liv.infra.bbpay.models.enums.EstadoSolicitacao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
@Entity
@Table(name = "financeiros")
public class Financeiro {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "contrato_id", foreignKey = @ForeignKey(name = "financeiro_fk_contrato"))
	private Contrato contrato;

	@Column(nullable = false)
	private Integer parcelasRestantes = 0;

	@Column(nullable = false)
	private Integer parcelasPagas = 0;

	@Column(nullable = false)
	private BigDecimal valorProximaParcela = new BigDecimal(0);

	@Column(nullable = false)
	private BigDecimal montantePago = new BigDecimal(0);

	private Integer numeroSolicitacaoPagamento;

	// private String linkPagamento;
	
	private Date dataPagamentoParcela;
	
	private BigDecimal valorPagoDaParcela;
	
	private BigDecimal valorTotalPagar;

	private String situacaoParcela;

	private Boolean situacaoPagamento = false;

	@PreUpdate
	public void onPreUpdate() {
		if (parcelasRestantes != 0) {
			situacaoPagamento = false;
		}

		if (parcelasRestantes <= 0 && parcelasPagas > 0) {
			parcelasRestantes = 0;
			situacaoPagamento = true;
		}
	}

	// PATCH /liv-api/domain/financeiro/{id}/boleto
	/*
	 * body:
	 * { }
	 */
	public Object[] boleto() throws IOException, WriterException {
		if (situacaoPagamento) {
			throw new DomainException("Cliente já quitou todas as parcelas!");
		}

		Solicitacoes.disableSSLVerification();

		String cpfNumerico = obterCpfNumerico(contrato.getCliente());
		Long cpfLong = Long.parseLong(cpfNumerico);

		if (numeroSolicitacaoPagamento == null || !verificarEstadoPagamento().equals(EstadoSolicitacao.AGUARDANDO_PAGAMENTO)) {
			SolicitacaoPagamentoResponse solicitacao = gerarPagamento(cpfLong);
			
			if(solicitacao.getErros().getStatusCode() == 500 && solicitacao.getErros().getErros().size() == 1) {
				throw new DomainException(solicitacao.getErros().getErros().get(0).getMensagem());
			}
			
			numeroSolicitacaoPagamento = solicitacao.getNumeroSolicitacao();
			valorPagoDaParcela = null;
			dataPagamentoParcela = null;
			situacaoParcela = EstadoSolicitacao.AGUARDANDO_PAGAMENTO.getDescription();
		}

		byte[] boletoBytes = gerarBoletoBytes(cpfLong);

		repository.add(this);

		//System.out.println(Base64.getEncoder().encodeToString(boletoBytes));

		return new Object[] {
				boletoBytes,
				"boleto.pdf"
		};
	}

	// PATCH /liv-api/domain/financeiro/{id}/comprovante
	/*
	 * body:
	 * { }
	 */
	public Object[] comprovante() {
		if (situacaoParcela == null) {
			throw new DomainException("É necessário que o cliente tenha pago sua parcela para que possa gerar o comprovante");
		}

		Report report = new Report();
		report.setBackgroundFileName(getClass().getResource("/static/img/background.jpeg").getFile());

		if(contrato.getCliente().getRepresentante() == null) {
			report.addGrid().setBorder(Borders.None)
			.addRow()
			.addValue("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n").setPaddings(0, 60, 0, 60)
			.addRow()
			.addValue("\n\nCOMPROVANTE DE PAGAMENTO")
			.sethAlignCenter().setFontSize(12)
			.addRow()
			.addValue("\n\n\nRecebemos de " + contrato.getCliente().getContato().getNome() + " a importância de R$ " + valorPagoDaParcela + " no dia " + new SimpleDateFormat("dd/MM/yyyy").format(dataPagamentoParcela) + ", referente ao serviço previdenciário na modalidade " + contrato.getBeneficio().getModalidade() + " – " + contrato.getBeneficio().getDescricao() + ".")
			.setFontSize(12).setPaddings(0, 60, 0, 60).sethAlignJustified()
			.addRow().addValue("\n\n\n\n\n_____________________________________________________").sethAlignCenter()
			.addRow()
			.addValue("Atenciosamente, LIV Assessoria Previdenciária").sethAlignCenter();
		} else {
			report.addGrid().setBorder(Borders.None)
			.addRow()
			.addValue("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n").setPaddings(0, 60, 0, 60)
			.addRow()
			.addValue("\n\nCOMPROVANTE DE PAGAMENTO")
			.sethAlignCenter().setFontSize(12)
			.addRow()
			.addValue("\n\n\nRecebemos de " + contrato.getCliente().getRepresentante().getContato().getNome() + " a importância de R$ " + valorPagoDaParcela + " no dia " + new SimpleDateFormat("dd/MM/yyyy").format(dataPagamentoParcela) + ", referente ao serviço previdenciário na modalidade " + contrato.getBeneficio().getModalidade() + " – " + contrato.getBeneficio().getDescricao() + " de " + contrato.getCliente().getContato().getNome() + ".")
			.setFontSize(12).setPaddings(0, 60, 0, 60).sethAlignJustified()
			.addRow().addValue("\n\n\n\n\n_____________________________________________________").sethAlignCenter()
			.addRow()
			.addValue("Atenciosamente, LIV Assessoria Previdenciária").sethAlignCenter();
		}

		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			ReportExporterPDF.exportTo(report, baos);
			System.out.println(Base64.getEncoder().encodeToString(baos.toByteArray()));
			return new Object[] {
					baos.toByteArray(),
					"comprovante-" + contrato.getCliente().getContato().getNome().replace(" ", "-") + ".pdf"
			};
		} catch (Exception e) {
			throw new RuntimeException("Erro ao gerar a carta de perícia médica.", e);
		}
	}

	public void pagar() throws JsonMappingException, JsonProcessingException, UnsupportedEncodingException {
		Solicitacoes.pagar(Solicitacoes.pegarSolicitacaoPagamento(numeroSolicitacaoPagamento, 98825).getInformacoesPix()
				.getTextoQrCode());
	}

	@Inject
	@Transient
	private IRepository repository;

	private SolicitacaoPagamentoResponse gerarPagamento(Long cpf) throws IOException, WriterException {
		SolicitacaoGeral geral = new SolicitacaoGeral(98825, true, LocalDateTime.now().plusMonths(1),
				valorProximaParcela.doubleValue(),
				"PAGAMENTO DOS RETROATIVOS", "cobranças Open Banking", null);

		SolicitacoesDevedor devedor = new SolicitacoesDevedor(1, cpf,
				Integer.parseInt(contrato.getCliente().getEndereco().getCep()),
				contrato.getCliente().getEndereco().getLogradouro(),
				contrato.getCliente().getEndereco().getBairro(), contrato.getCliente().getEndereco().getCidade(), "CE",
				contrato.getCliente().getContato().getEmail(),
				Integer.parseInt(contrato.getCliente().getContato().getTelefone().substring(0, 2)),
				Integer.parseInt(contrato.getCliente().getContato().getTelefone().substring(2)), 8803937340L);

		SolicitacaoPagamentoResponse solicitacao = Solicitacoes.gerarSolicitacaoPagamento(geral, devedor);

		return solicitacao;
	}

	private String obterCpfNumerico(Cliente cliente) {
		return cliente.getCpf().replaceAll("[^0-9]", "");
	}

	private EstadoSolicitacao verificarEstadoPagamento() throws IOException, WriterException {
		return Solicitacoes.verificarSolicitacaoPagamento(numeroSolicitacaoPagamento, 98825);
	}

	private byte[] gerarBoletoBytes(Long cpfLong) throws IOException, WriterException {
		byte[] boleto = Solicitacoes.gerarBoleto(
				numeroSolicitacaoPagamento,
				98825,
				contrato.getCliente().getContato().getNome(),
				cpfLong,
				Integer.parseInt(contrato.getCliente().getEndereco().getCep()),
				contrato.getCliente().getEndereco().getLogradouro() + " - "
						+ contrato.getCliente().getEndereco().getBairro(),
				contrato.getCliente().getEndereco().getCidade(),
				"CE");

		return boleto;
	}

	public Financeiro() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Contrato getContrato() {
		return contrato;
	}

	public void setContrato(Contrato contrato) {
		this.contrato = contrato;
	}

	public Integer getParcelasRestantes() {
		return parcelasRestantes;
	}

	public void setParcelasRestantes(Integer parcelasRestantes) {
		this.parcelasRestantes = parcelasRestantes;
	}

	public Integer getParcelasPagas() {
		return parcelasPagas;
	}

	public void setParcelasPagas(Integer parcelasPagas) {
		this.parcelasPagas = parcelasPagas;
	}

	public BigDecimal getValorProximaParcela() {
		return valorProximaParcela;
	}

	public void setValorProximaParcela(BigDecimal valorProximaParcela) {
		this.valorProximaParcela = valorProximaParcela;
	}

	public BigDecimal getMontantePago() {
		return montantePago;
	}

	public void setMontantePago(BigDecimal montantePago) {
		this.montantePago = montantePago;
	}

	public Integer getNumeroSolicitacaoPagamento() {
		return numeroSolicitacaoPagamento;
	}

	public void setNumeroSolicitacaoPagamento(Integer numeroSolicitacaoPagamento) {
		this.numeroSolicitacaoPagamento = numeroSolicitacaoPagamento;
	}

	public Date getDataPagamentoParcela() {
		return dataPagamentoParcela;
	}

	public void setDataPagamentoParcela(Date dataPagamentoParcela) {
		this.dataPagamentoParcela = dataPagamentoParcela;
	}

	public BigDecimal getValorPagoDaParcela() {
		return valorPagoDaParcela;
	}

	public void setValorPagoDaParcela(BigDecimal valorPagoDaParcela) {
		this.valorPagoDaParcela = valorPagoDaParcela;
	}

	public BigDecimal getValorTotalPagar() {
		return valorTotalPagar;
	}

	public void setValorTotalPagar(BigDecimal valorTotalPagar) {
		this.valorTotalPagar = valorTotalPagar;
	}

	public String getSituacaoParcela() {
		return situacaoParcela;
	}

	public void setSituacaoParcela(String situacaoParcela) {
		this.situacaoParcela = situacaoParcela;
	}

	public Boolean getSituacaoPagamento() {
		return situacaoPagamento;
	}

	public void setSituacaoPagamento(Boolean situacaoPagamento) {
		this.situacaoPagamento = situacaoPagamento;
	}

	public IRepository getRepository() {
		return repository;
	}

	public void setRepository(IRepository repository) {
		this.repository = repository;
	}

}
