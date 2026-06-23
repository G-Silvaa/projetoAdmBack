package com.liv.infra.abacatepay;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuração da integração com o AbacatePay.
 *
 * <p>Sem {@code api-key} configurada o cliente opera em <b>modo mock</b>: gera
 * QR codes localmente e a confirmação de pagamento é feita pelo endpoint de
 * simulação — permite demonstrar o fluxo de ponta a ponta sem uma conta real.
 */
@Component
@ConfigurationProperties(prefix = "app.abacatepay")
public class AbacatePayProperties {

	/** Base da API. Default: produção/sandbox AbacatePay. */
	private String baseUrl = "https://api.abacatepay.com/v1";

	/** Chave de API (dev: {@code abc_dev_...}). Vazia = modo mock. */
	private String apiKey = "";

	/** Segredo esperado na query string do webhook (validação). Vazio = sem validação. */
	private String webhookSecret = "";

	/** Tempo de expiração do QR code, em segundos. */
	private int expiresInSeconds = 3600;

	public boolean isMock() {
		return apiKey == null || apiKey.isBlank();
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getWebhookSecret() {
		return webhookSecret;
	}

	public void setWebhookSecret(String webhookSecret) {
		this.webhookSecret = webhookSecret;
	}

	public int getExpiresInSeconds() {
		return expiresInSeconds;
	}

	public void setExpiresInSeconds(int expiresInSeconds) {
		this.expiresInSeconds = expiresInSeconds;
	}
}
