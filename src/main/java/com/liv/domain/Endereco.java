package com.liv.domain;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class Endereco implements Serializable {

	@NotBlank(message = "Informe o cep")
    @Column(name = "endereco_cep", length = 8)
    private String cep;

    @NotBlank(message = "Informe o logradouro")
    @Column(name = "endereco_logradouro", length = 100)
    private String logradouro;

    
    @Column(length = 20, name = "endereco_complemento")
    private String complemento;

    @NotBlank(message = "Informe o nome do bairro")
    @Column(name = "endereco_bairro", length = 30)
    private String bairro;
    
    @NotBlank(message = "Informe o nome da cidade")
    @Column(name = "endereco_cidade", length = 50)
    private String cidade;

    @Override
    public String toString() {
        return logradouro
                + (complemento != null && !complemento.isBlank() ? " (" + complemento + ") " : "") + " - "
                + bairro + ", "
                + (cidade == null ? "" : cidade)
                + ", " + cep;
    }
}
