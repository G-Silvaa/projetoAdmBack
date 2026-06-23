package com.liv.infra.bbpay;

import java.io.IOException;

import javax.util.facades.net.HttpClientFacade;
import javax.util.facades.net.HttpClientResponse;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.WriterException;

import com.liv.infra.bbpay.models.Erros;
import com.liv.infra.bbpay.models.ParticipantesPostRequest;
import com.liv.infra.bbpay.models.ParticipantesPostResponse;

public class Participantes {

	private static final String URL = "https://api.extranet.hm.bb.com.br/checkout/v2";

	/**
	 * Adiciona um participante para ficar disponível adicionar no split da
	 * solicitação de um pagamento.
	 */
	public static ParticipantesPostResponse adicionarParticipanteAoSplitPagamento(ParticipantesPostRequest request)
			throws IOException, WriterException {
		String uri = "/participantes?gw-dev-app-key=" + Acesso.DEVELOPER_APPLICATION_KEY;
		ObjectMapper objectMapper = new ObjectMapper();
		String requestJson = objectMapper.writeValueAsString(request);
		HttpClientResponse response = new HttpClientFacade(URL)
				.setConnectTimeout(1000)
				.setReadTimeout(3000)
				.setVerbose(true)
				.addRequestProperty("Content-Type", "application/json")
				.addRequestProperty("Authorization", "Bearer " + Acesso.gerarTokenAcesso())
				.POST(uri, requestJson);

		if (response.getCode() >= 400) {
			JsonNode rootNode = objectMapper.readTree(response.getContent());
			String jsonContent = rootNode.toString();
			Erros errInfo = objectMapper.readValue(jsonContent, Erros.class);
			errInfo.setStatusCode(response.getCode());
			ParticipantesPostResponse dto = new ParticipantesPostResponse();
			dto.setErros(errInfo);
			return dto;
		}

		JsonNode rootNode = objectMapper.readTree(response.getContent());
		String jsonContent = rootNode.toString();
		ParticipantesPostResponse solicitacaoPagamento = objectMapper.readValue(jsonContent,
				new TypeReference<ParticipantesPostResponse>() {
				});
		solicitacaoPagamento.setErros(new Erros(null, response.getCode()));

		return solicitacaoPagamento;
	}

}
