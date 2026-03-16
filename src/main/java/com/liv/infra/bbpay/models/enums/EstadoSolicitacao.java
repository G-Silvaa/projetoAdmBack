package com.liv.infra.bbpay.models.enums;

public enum EstadoSolicitacao {

    AGUARDANDO_PAGAMENTO(0, "Aguardando Pagamento"),
    PAGA(1, "Paga"),
    EXPIRADA(800, "Expirada"),
    ABANDONADA(850, "Abandonada"),
    EXCLUIDA(900, "Excluída");

    private int id;
    private String description;

    EstadoSolicitacao(int id, String description) {
        this.id = id;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public int id() {
        return id;
    }

}
