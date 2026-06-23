package com.liv.infra.bbpay;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.*;
import java.security.cert.X509Certificate;
import javax.util.facades.net.HttpClientFacade;
import javax.util.facades.net.HttpClientResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.WriterException;
import com.liv.infra.bbpay.models.EfetuarPagamento;
import com.liv.infra.bbpay.models.Erros;
import com.liv.infra.bbpay.models.FormaPagamento;
import com.liv.infra.bbpay.models.GetSolicitacaoResponse;
import com.liv.infra.bbpay.models.SolicitacaoGeral;
import com.liv.infra.bbpay.models.SolicitacaoPagamentoRequest;
import com.liv.infra.bbpay.models.SolicitacaoPagamentoResponse;
import com.liv.infra.bbpay.models.SolicitacoesDevedor;
import com.liv.infra.bbpay.models.Vencimento;
import com.liv.infra.bbpay.models.enums.CodigoTipoPagamento;
import com.liv.infra.bbpay.models.enums.EstadoSolicitacao;
import com.liv.infra.bbpay.utils.BoletoPdf;

public class Solicitacoes {

	private static final String URL = "https://api.extranet.hm.bb.com.br/checkout/v2";

	public static GetSolicitacaoResponse pegarSolicitacaoPagamento(int numeroSolicitacao, int numeroConvenio)
			throws JsonMappingException, JsonProcessingException, UnsupportedEncodingException {
		String uri = "/solicitacoes/" + numeroSolicitacao + "?gw-dev-app-key=" + Acesso.DEVELOPER_APPLICATION_KEY
				+ "&numeroConvenio=" + numeroConvenio;
		ObjectMapper objectMapper = new ObjectMapper();
		HttpClientResponse response = new HttpClientFacade(URL)
				.setConnectTimeout(10000)
				.setReadTimeout(30000)
				.setVerbose(true)
				.addRequestProperty("Content-Type", "application/json")
				.addRequestProperty("Authorization", "Bearer " + Acesso.gerarTokenAcesso())
				.GET(uri);

		JsonNode rootNode = objectMapper.readTree(response.getContent());
		String jsonContent = rootNode.toString();
		GetSolicitacaoResponse getSolicitacaoResponse = objectMapper.readValue(jsonContent,
				new TypeReference<GetSolicitacaoResponse>() {
				});

		return getSolicitacaoResponse;
	}

	public static void pagar(String textoQrPix)
			throws JsonMappingException, JsonProcessingException, UnsupportedEncodingException {
		String uri = "https://api.hm.bb.com.br/testes-portal-desenvolvedor/v1/boletos-pix/pagar?gw-dev-app-key=95cad3f03fd9013a9d15005056825665  ";

		ObjectMapper objectMapper = new ObjectMapper();
		EfetuarPagamento efetuarPagamento = new EfetuarPagamento();
		efetuarPagamento.setPix(textoQrPix);
		String requestJson = objectMapper.writeValueAsString(efetuarPagamento);
		HttpClientResponse response = new HttpClientFacade(uri)
				.setConnectTimeout(10000)
				.setReadTimeout(30000)
				.setVerbose(true)
				.addRequestProperty("Content-Type", "application/json")
				.addRequestProperty("Authorization", "Bearer " + Acesso.gerarTokenAcesso())
				.POST("", requestJson);
	}

	/**
	 * Gera o PDF do boleto. QR Code está incluso no documento.
	 * Nota: CPF deve-se ignorar zeros à esquerda, por exemplo: 06978171840 ->
	 * 6978171840
	 */
	public static byte[] gerarBoleto(int numeroSolicitacao, int numeroConvenio, String nomeDevedor, long cpf, int cep,
			String endereco, String cidade, String uf) throws IOException, WriterException {

		GetSolicitacaoResponse getSolicitacaoResponse = pegarSolicitacaoPagamento(numeroSolicitacao, numeroConvenio);

		String dataVencimento = LocalDateTime.parse(getSolicitacaoResponse.getTimestampLimiteSolicitacao())
				.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
		String dataDocumento = LocalDateTime.parse(getSolicitacaoResponse.getTimestampCriacaoSolicitacao())
				.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

		byte[] boletoPdf = BoletoPdf.emitirBoleto(getSolicitacaoResponse.getInformacoesBoleto().getLinhaDigitavel(),
				getSolicitacaoResponse.getInformacoesBoleto().getTextoCodigoBarras(),
				getSolicitacaoResponse.getInformacoesPix().getTextoQrCode(), nomeDevedor, String.valueOf(cpf),
				String.valueOf(getSolicitacaoResponse.getNumeroSolicitacao()), dataDocumento,
				dataVencimento, String.valueOf(getSolicitacaoResponse.getValorSolicitacao()),
				String.valueOf(getSolicitacaoResponse.getNumeroSolicitacao()),
				"LIV ASSESSORIA PREVIDENCIARIA LTDA", String.valueOf("48994154000172"), "N", "N",
				getSolicitacaoResponse.getDescricaoSolicitacao(), endereco, cidade, uf, String.valueOf(cep));
		return boletoPdf;
	}

	/**
	 * Gera solicitação de pagamento. Adicione as informações gerais da solicitação
	 * no campo e informe os dados do devedor, Além de adicionar o número que
	 * identifica o Recebedor que participa da Split de pagamento.
	 */
	public static SolicitacaoPagamentoResponse gerarSolicitacaoPagamento(SolicitacaoGeral geral,
			SolicitacoesDevedor devedor)
			throws IOException, WriterException {
		String uri = "/solicitacoes?gw-dev-app-key=" + Acesso.DEVELOPER_APPLICATION_KEY;
		List<FormaPagamento> formasPagamento = new ArrayList<>();
		FormaPagamento f1 = new FormaPagamento(CodigoTipoPagamento.PIX, 1);
		FormaPagamento f2 = new FormaPagamento(CodigoTipoPagamento.BLT, 1);
		formasPagamento.add(f1);
		formasPagamento.add(f2);

		Vencimento vencimento = new Vencimento(0.0, 0.0, null, LocalDateTime
				.parse(geral.getTimestampLimiteSolicitacao()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), 0.0);
		SolicitacaoPagamentoRequest request = new SolicitacaoPagamentoRequest(geral, devedor, vencimento,
				formasPagamento);

		ObjectMapper objectMapper = new ObjectMapper();
		String requestJson = objectMapper.writeValueAsString(request);
		HttpClientResponse response = new HttpClientFacade(URL)
				.setConnectTimeout(10000)
				.setReadTimeout(30000)
				.setVerbose(true)
				.addRequestProperty("Content-Type", "application/json")
				.addRequestProperty("Authorization", "Bearer " + Acesso.gerarTokenAcesso())
				.POST(uri, requestJson);

		if (response.getCode() >= 400) {
			JsonNode rootNode = objectMapper.readTree(response.getContent());
			String jsonContent = rootNode.toString();
			Erros errInfo = objectMapper.readValue(jsonContent, Erros.class);
			errInfo.setStatusCode(response.getCode());
			SolicitacaoPagamentoResponse dto = new SolicitacaoPagamentoResponse();
			dto.setErros(errInfo);
			return dto;
		}

		JsonNode rootNode = objectMapper.readTree(response.getContent());
		String jsonContent = rootNode.toString();
		SolicitacaoPagamentoResponse solicitacaoPagamento = objectMapper.readValue(jsonContent,
				new TypeReference<SolicitacaoPagamentoResponse>() {
				});
		solicitacaoPagamento.setErros(new Erros(null, response.getCode()));

		return solicitacaoPagamento;
	}

	public static void disableSSLVerification() {
		try {
			// Cria um TrustManager que não valida os certificados
			TrustManager[] trustAllCertificates = new TrustManager[] {
					new X509TrustManager() {
						public X509Certificate[] getAcceptedIssuers() {
							return null;
						}

						public void checkClientTrusted(X509Certificate[] certs, String authType) {
						}

						public void checkServerTrusted(X509Certificate[] certs, String authType) {
						}
					}
			};

			// Instala o TrustManager
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, trustAllCertificates, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

			// Instala um HostnameVerifier que aceita qualquer hostname
			HostnameVerifier allHostsValid = (hostname, session) -> true;
			HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

			System.out.println("Verificação SSL desabilitada.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Verifica a solicitação do pagamento. Os estados da solicitação de pagamento
	 * são: Aguardando Pagamento; Paga; Expirada; Abandonada; Excluída.
	 */
	public static EstadoSolicitacao verificarSolicitacaoPagamento(int numeroSolicitacao, int numeroConvenio)
			throws IOException, WriterException {
		String uri = "/solicitacoes/" + numeroSolicitacao + "?gw-dev-app-key=" + Acesso.DEVELOPER_APPLICATION_KEY
				+ "&numeroConvenio=" + numeroConvenio;
		ObjectMapper objectMapper = new ObjectMapper();
		HttpClientResponse response = new HttpClientFacade(URL)
				.setConnectTimeout(10000)
				.setReadTimeout(30000)
				.setVerbose(true)
				.addRequestProperty("Content-Type", "application/json")
				.addRequestProperty("Authorization", "Bearer " + Acesso.gerarTokenAcesso())
				.GET(uri);

		JsonNode rootNode = objectMapper.readTree(response.getContent());
		String jsonContent = rootNode.toString();
		GetSolicitacaoResponse getSolicitacaoResponse = objectMapper.readValue(jsonContent,
				new TypeReference<GetSolicitacaoResponse>() {
				});

		return getSolicitacaoResponse.getCodigoEstadoSolicitacao();
	}

}
