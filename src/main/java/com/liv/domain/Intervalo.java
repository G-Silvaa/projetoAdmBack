package com.liv.domain;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Intervalo {

    @NotNull
    @Valid
    private AnoMes inicio;

    @NotNull
    @Valid
    private AnoMes termino;

}
