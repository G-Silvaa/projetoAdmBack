package com.liv.infra.abacatepay;

import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

/**
 * Cliente da API de cobranças PIX do AbacatePay.
 *
 * <p>Quando não há {@code api-key} configurada opera em <b>modo mock</b>: o QR
 * code é gerado localmente (zxing) e o pagamento só é confirmado via
 * {@link #simularPagamento(String)} — útil para demonstrar o fluxo sandbox sem
 * uma conta real. Com api-key, fala com a API real (use uma chave {@code dev}
 * para o ambiente sandbox).
 */
@Component
public class AbacatePayClient {

	private static final Logger log = LoggerFactory.getLogger(AbacatePayClient.class);

	private final AbacatePayProperties props;
	private final RestClient restClient;

	public AbacatePayClient(AbacatePayProperties props) {
		this.props = props;

		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(10_000);
		requestFactory.setReadTimeout(30_000);

		this.restClient = RestClient.builder()
				.requestFactory(requestFactory)
				.baseUrl(props.getBaseUrl())
				.build();

		if (props.isMock()) {
			log.warn("AbacatePay em MODO MOCK (sem api-key): QR codes são gerados localmente "
					+ "e o pagamento é confirmado apenas via simulação.");
		}
	}

	public boolean isMock() {
		return props.isMock();
	}

	/** Cria uma cobrança PIX (QR code dinâmico). */
	public PixCharge criarPixQrCode(
			long amountCents,
			String description,
			String customerName,
			String customerEmail,
			String customerTaxId,
			String customerCellphone
	) {
		if (props.isMock()) {
			return mockCharge(amountCents);
		}

		Map<String, Object> customer = new LinkedHashMap<>();
		customer.put("name", customerName);
		customer.put("email", customerEmail);
		customer.put("taxId", customerTaxId);
		customer.put("cellphone", customerCellphone);

		Map<String, Object> body = new LinkedHashMap<>();
		body.put("amount", amountCents);
		body.put("expiresIn", props.getExpiresInSeconds());
		body.put("description", description);
		body.put("customer", customer);

		ApiResponse response = post("/pixQrCode/create", body);
		return toPixCharge(response);
	}

	/** Consulta o status de uma cobrança. Retorna {@code null} no modo mock. */
	public PixCharge consultarPix(String providerId) {
		if (props.isMock()) {
			return null;
		}
		try {
			ApiResponse response = restClient.get()
					.uri(uriBuilder -> uriBuilder.path("/pixQrCode/check").queryParam("id", providerId).build())
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + props.getApiKey())
					.retrieve()
					.body(ApiResponse.class);
			return toPixCharge(response);
		} catch (Exception exception) {
			log.error("Falha ao consultar cobrança {} no AbacatePay", providerId, exception);
			throw gatewayError();
		}
	}

	/** Simula o pagamento de uma cobrança (disponível apenas em ambiente dev/sandbox). */
	public void simularPagamento(String providerId) {
		if (props.isMock()) {
			return;
		}
		try {
			restClient.post()
					.uri(uriBuilder -> uriBuilder.path("/pixQrCode/simulate-payment")
							.queryParam("id", providerId).build())
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + props.getApiKey())
					.contentType(MediaType.APPLICATION_JSON)
					.body(Map.of("metadata", Map.of()))
					.retrieve()
					.toBodilessEntity();
		} catch (Exception exception) {
			log.error("Falha ao simular pagamento da cobrança {} no AbacatePay", providerId, exception);
			throw gatewayError();
		}
	}

	private ApiResponse post(String path, Object body) {
		try {
			return restClient.post()
					.uri(path)
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + props.getApiKey())
					.contentType(MediaType.APPLICATION_JSON)
					.body(body)
					.retrieve()
					.body(ApiResponse.class);
		} catch (Exception exception) {
			log.error("Falha na chamada {} ao AbacatePay", path, exception);
			throw gatewayError();
		}
	}

	private PixCharge toPixCharge(ApiResponse response) {
		if (response == null || response.data() == null) {
			throw gatewayError();
		}
		ApiResponse.Data data = response.data();
		return new PixCharge(data.id(), data.status(), data.brCode(), data.brCodeBase64(), parseDate(data.expiresAt()));
	}

	private static ResponseStatusException gatewayError() {
		return new ResponseStatusException(HttpStatus.BAD_GATEWAY,
				"Não foi possível comunicar com o provedor de pagamento. Tente novamente.");
	}

	// ----- modo mock ---------------------------------------------------------

	private PixCharge mockCharge(long amountCents) {
		String providerId = "pix_dev_" + UUID.randomUUID().toString().replace("-", "");
		String brCode = mockBrCode(providerId, amountCents);
		Date expiresAt = Date.from(Instant.now().plus(props.getExpiresInSeconds(), ChronoUnit.SECONDS));
		return new PixCharge(providerId, "PENDING", brCode, gerarQrCodeBase64(brCode), expiresAt);
	}

	/** Monta um payload PIX "copia e cola" plausível (sem CRC válido — apenas demo). */
	private static String mockBrCode(String providerId, long amountCents) {
		String valor = String.format("%d.%02d", amountCents / 100, amountCents % 100);
		String txid = providerId.replace("pix_dev_", "").substring(0, 25);
		return "00020126580014br.gov.bcb.pix0136" + txid
				+ "5204000053039865406" + valor
				+ "5802BR5913ARCTECH ADM6008SAOPAULO62070503***6304ABCD";
	}

	/** Gera a imagem do QR code (PNG em data URI) a partir do payload PIX. */
	private static String gerarQrCodeBase64(String conteudo) {
		try {
			BitMatrix matrix = new MultiFormatWriter().encode(conteudo, BarcodeFormat.QR_CODE, 320, 320);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			MatrixToImageWriter.writeToStream(matrix, "PNG", out);
			return "data:image/png;base64," + Base64.getEncoder().encodeToString(out.toByteArray());
		} catch (Exception exception) {
			log.error("Falha ao gerar QR code mock", exception);
			return null;
		}
	}

	private static Date parseDate(String iso) {
		if (iso == null || iso.isBlank()) {
			return null;
		}
		try {
			return Date.from(Instant.parse(iso));
		} catch (Exception exception) {
			return null;
		}
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	record ApiResponse(Data data, Object error) {

		@JsonIgnoreProperties(ignoreUnknown = true)
		record Data(String id, String status, String brCode, String brCodeBase64, String expiresAt) {
		}
	}
}
