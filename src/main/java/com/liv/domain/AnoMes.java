package com.liv.domain;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnoMes {
	
	@NotNull
	private Integer ano;
	
	@NotNull
	
	private Mes mes;

}
