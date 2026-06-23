package com.liv.infra.util;

public class StringUtils {
	
	public static String formatarCpf(String cpf) {
        if (cpf == null) {
            throw new IllegalArgumentException("CPF não pode ser nulo.");
        }

        // Remove caracteres não numéricos
        cpf = cpf.replaceAll("\\D", "");

        // Verifica se o CPF possui exatamente 11 dígitos
        if (cpf.length() != 11) {
            throw new IllegalArgumentException("CPF deve ter 11 dígitos.");
        }

        // Utiliza StringBuilder para melhor performance
        StringBuilder cpfFormatado = new StringBuilder();
        cpfFormatado.append(cpf, 0, 3).append('.')
                    .append(cpf, 3, 6).append('.')
                    .append(cpf, 6, 9).append('-')
                    .append(cpf.substring(9));

        return cpfFormatado.toString();
    }

}
