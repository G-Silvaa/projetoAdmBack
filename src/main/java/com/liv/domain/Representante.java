package com.liv.domain;

import java.util.Date;

import org.hibernate.validator.constraints.br.CPF;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "representantes")
public class Representante {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Valid
	@Embedded
	private Contato contato;

	@NotBlank(message = "Informe o grau de parentesco")
	@Column(name = "parentesco", length = 100, nullable = false)
	private String parentesco;

	@CPF
	@NotBlank(message = "Informe o cpf do representante")
	@Column(name = "cpf", length = 15)
	private String cpf;

	@Column(name = "rg", length = 20)
	private String rg;

	@Temporal(TemporalType.DATE)
	@Column(columnDefinition = "date")
	private Date nascimento;

}
