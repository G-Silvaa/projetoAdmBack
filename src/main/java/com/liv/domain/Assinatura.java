package com.liv.domain;

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
import jakarta.persistence.UniqueConstraint;

/**
 * Assinatura de uma empresa a um plano. Uma assinatura por empresa.
 * Não é @TenantId: é criada no cadastro (antes de existir contexto de tenant)
 * e localizada explicitamente por empresa_id.
 */
@Entity
@Table(name = "assinaturas", uniqueConstraints = {
		@UniqueConstraint(name = "assinaturas_uk_empresa", columnNames = "empresa_id")
})
public class Assinatura {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "empresa_id", nullable = false)
	private Long empresaId;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "plano_id", foreignKey = @ForeignKey(name = "assinaturas_fk_plano"), nullable = false)
	private Plano plano;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", length = 20, nullable = false)
	private StatusAssinatura status;

	@Temporal(TemporalType.DATE)
	@Column(name = "trial_ate")
	private Date trialAte;

	@Temporal(TemporalType.DATE)
	@Column(name = "inicio", nullable = false)
	private Date inicio;

	/** Acesso permitido até esta data (vencimento do período pago/trial). */
	@Temporal(TemporalType.DATE)
	@Column(name = "vencimento")
	private Date vencimento;

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
		if (inicio == null) {
			inicio = now;
		}
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

	public StatusAssinatura getStatus() {
		return status;
	}

	public void setStatus(StatusAssinatura status) {
		this.status = status;
	}

	public Date getTrialAte() {
		return trialAte;
	}

	public void setTrialAte(Date trialAte) {
		this.trialAte = trialAte;
	}

	public Date getInicio() {
		return inicio;
	}

	public void setInicio(Date inicio) {
		this.inicio = inicio;
	}

	public Date getVencimento() {
		return vencimento;
	}

	public void setVencimento(Date vencimento) {
		this.vencimento = vencimento;
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

	/** True se a assinatura permite acesso agora (status + dentro do vencimento). */
	public boolean permiteAcesso() {
		if (status == null || !status.liberaAcesso()) {
			return false;
		}
		if (vencimento == null) {
			return true;
		}
		// Acesso permitido se hoje <= vencimento (comparação por dia).
		Date hoje = new Date();
		return !hoje.after(endOfDay(vencimento));
	}

	private static Date endOfDay(Date date) {
		return new Date(date.getTime() + 24L * 60 * 60 * 1000 - 1);
	}
}
