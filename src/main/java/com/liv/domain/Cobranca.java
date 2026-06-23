package com.liv.domain;

import java.math.BigDecimal;
import java.util.Date;

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

/**
 * Cobrança PIX de uma assinatura. Localizada por empresa_id (não é @TenantId:
 * pode ser criada/consultada com a assinatura bloqueada, fora do gate de tenant).
 */
@Entity
@Table(name = "cobrancas")
public class Cobranca {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "empresa_id", nullable = false)
	private Long empresaId;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "plano_id", foreignKey = @ForeignKey(name = "cobrancas_fk_plano"), nullable = false)
	private Plano plano;

	/** Id da cobrança no provedor de pagamento (AbacatePay). */
	@Column(name = "provider_id", length = 120)
	private String providerId;

	@Column(name = "valor", precision = 19, scale = 2, nullable = false)
	private BigDecimal valor;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", length = 20, nullable = false)
	private StatusCobranca status;

	/** PIX copia e cola (payload EMV). */
	@Column(name = "br_code", columnDefinition = "text")
	private String brCode;

	/** Imagem do QR code em data URI (base64). */
	@Column(name = "br_code_base64", columnDefinition = "text")
	private String brCodeBase64;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "expira_em")
	private Date expiraEm;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "pago_em")
	private Date pagoEm;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "criado_em", nullable = false)
	private Date criadoEm;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "atualizado_em", nullable = false)
	private Date atualizadoEm;

	@PrePersist
	public void onPrePersist() {
		Date now = new Date();
		if (criadoEm == null) {
			criadoEm = now;
		}
		atualizadoEm = now;
	}

	@PreUpdate
	public void onPreUpdate() {
		atualizadoEm = new Date();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getEmpresaId() {
		return empresaId;
	}

	public void setEmpresaId(Long empresaId) {
		this.empresaId = empresaId;
	}

	public Plano getPlano() {
		return plano;
	}

	public void setPlano(Plano plano) {
		this.plano = plano;
	}

	public String getProviderId() {
		return providerId;
	}

	public void setProviderId(String providerId) {
		this.providerId = providerId;
	}

	public BigDecimal getValor() {
		return valor;
	}

	public void setValor(BigDecimal valor) {
		this.valor = valor;
	}

	public StatusCobranca getStatus() {
		return status;
	}

	public void setStatus(StatusCobranca status) {
		this.status = status;
	}

	public String getBrCode() {
		return brCode;
	}

	public void setBrCode(String brCode) {
		this.brCode = brCode;
	}

	public String getBrCodeBase64() {
		return brCodeBase64;
	}

	public void setBrCodeBase64(String brCodeBase64) {
		this.brCodeBase64 = brCodeBase64;
	}

	public Date getExpiraEm() {
		return expiraEm;
	}

	public void setExpiraEm(Date expiraEm) {
		this.expiraEm = expiraEm;
	}

	public Date getPagoEm() {
		return pagoEm;
	}

	public void setPagoEm(Date pagoEm) {
		this.pagoEm = pagoEm;
	}

	public Date getCriadoEm() {
		return criadoEm;
	}

	public void setCriadoEm(Date criadoEm) {
		this.criadoEm = criadoEm;
	}

	public Date getAtualizadoEm() {
		return atualizadoEm;
	}

	public void setAtualizadoEm(Date atualizadoEm) {
		this.atualizadoEm = atualizadoEm;
	}
}
