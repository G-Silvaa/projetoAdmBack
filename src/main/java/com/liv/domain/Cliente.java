package com.liv.domain;

import java.util.Date;

import javax.inject.Inject;
import javax.util.ddd.annotation.Create;
import javax.util.ddd.annotation.Delete;
import javax.util.ddd.domain.IRepository;

import org.hibernate.validator.constraints.br.CPF;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
@Entity
@Table(name = "clientes", uniqueConstraints = {
		@UniqueConstraint(name = "clientes_uk_cpf", columnNames = "cpf")
})
public class Cliente {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Valid
	@Embedded
	private Contato contato;
	
	@CPF
	@NotBlank(message = "Informe o cpf do cliente")
	@Column(name = "cpf", length = 15)
	private String cpf;
	
	@Column(name = "rg", length = 20)
	private String rg;
	
	@Temporal(TemporalType.DATE)
	@Column(columnDefinition = "date")
	private Date nascimento;
	
	@Valid
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(column = @Column(name = "endereco_cep", length = 8), name = "cep"),
        @AttributeOverride(column = @Column(name = "endereco_logradouro", length = 100), name = "logradouro"),
        @AttributeOverride(column = @Column(name = "endereco_complemento", length = 20), name = "complemento"),
        @AttributeOverride(column = @Column(name = "endereco_bairro", length = 30), name = "bairro"),
        @AttributeOverride(column = @Column(name = "endereco_cidade", length = 50), name = "cidade")
    })
    private Endereco endereco;
	
	@Valid
	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "representante_id", foreignKey = @ForeignKey(name = "cliente_fk_representante"))
	private Representante representante;
	
	//@Column(name = "usuario", length = 100)
	//private String usuario;
	
	//@Temporal(TemporalType.TIMESTAMP)
	//@Column(columnDefinition = "timestamp", nullable = false)
	//private Date dataInsercao;
	
	// POST /liv-api/domain/cliente/add
	@Create
	public void add() {
		if(!(this.getRepresentante() == null)) {
			repository.addAll(this.getRepresentante(), this);
			return;
		}
		
		repository.add(this);
	}
	
	@Delete
	public void remove() {
		repository.removeAll("DELETE FROM Processo p WHERE p.contrato.cliente.id = ?1", this.getId());
		repository.removeAll("DELETE FROM Financeiro f WHERE f.contrato.cliente.id = ?1", this.getId());
    	repository.removeAll("DELETE FROM Contrato c WHERE c.cliente.id = ?1", this.getId());
    	repository.remove(this);
    	repository.removeAll("DELETE FROM Representante r WHERE r.id = (SELECT c.representante.id FROM Cliente c WHERE c.id = ?1)", this.getId());
	}
	
	// @TODO: Deletar e alterar só mediante as senhas do admin
	
	@Inject
	@Transient
	private IRepository repository;

	public Cliente() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Contato getContato() {
		return contato;
	}

	public void setContato(Contato contato) {
		this.contato = contato;
	}

	public String getCpf() {
		return cpf;
	}

	public void setCpf(String cpf) {
		this.cpf = cpf;
	}

	public String getRg() {
		return rg;
	}

	public void setRg(String rg) {
		this.rg = rg;
	}

	public Date getNascimento() {
		return nascimento;
	}

	public void setNascimento(Date nascimento) {
		this.nascimento = nascimento;
	}

	public Endereco getEndereco() {
		return endereco;
	}

	public void setEndereco(Endereco endereco) {
		this.endereco = endereco;
	}

	public Representante getRepresentante() {
		return representante;
	}

	public void setRepresentante(Representante representante) {
		this.representante = representante;
	}

	public IRepository getRepository() {
		return repository;
	}

	public void setRepository(IRepository repository) {
		this.repository = repository;
	}

}
