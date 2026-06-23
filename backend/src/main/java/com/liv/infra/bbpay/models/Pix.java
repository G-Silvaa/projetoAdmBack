package com.liv.infra.bbpay.models;

public class Pix {

	private String textoQrCode;
	private String txId;

	public Pix() {
	}

	public Pix(String textoQrCode, String txId) {
		super();
		this.textoQrCode = textoQrCode;
		this.txId = txId;
	}

	public String getTextoQrCode() {
		return textoQrCode;
	}

	public void setTextoQrCode(String textoQrCode) {
		this.textoQrCode = textoQrCode;
	}

	public String getTxId() {
		return txId;
	}

	public void setTxId(String txId) {
		this.txId = txId;
	}

}
