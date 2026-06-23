package com.liv.infra.bbpay.models.enums;

public enum CodigoTipoPagamento {

    PIX(1, "PIX"),
    BLT(2, "BLT"),
    OPB(3, "OPB");

    private int id;
    private String description;

    CodigoTipoPagamento(int id, String description) {
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
