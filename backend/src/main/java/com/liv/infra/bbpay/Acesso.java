package com.liv.infra.bbpay;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.util.facades.net.HttpClientFacade;
import javax.util.facades.net.HttpClientResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.liv.infra.bbpay.models.RespostaToken;

public class Acesso {

	// trocar pela chave de produção
	public static final String DEVELOPER_APPLICATION_KEY = "7b03f05d8e6113c3266d62ba3c7aa798";
	// trocar informações abaixo pela conta que estará a chave oficial
	public static final String CLIENT_ID = "eyJpZCI6IjA1YzI0ODktODU2MS00NTQ3LWIxMDYtIiwiY29kaWdvUHVibGljYWRvciI6MCwiY29kaWdvU29mdHdhcmUiOjExODI1OCwic2VxdWVuY2lhbEluc3RhbGFjYW8iOjF9";
	public static final String CLIENT_SECRET = "eyJpZCI6ImIyYWYzNTAtZGJkNS00YzdmLTliZDUtYzExYThhOTJjZWU5OWFiNTNhNzAtIiwiY29kaWdvUHVibGljYWRvciI6MCwiY29kaWdvU29mdHdhcmUiOjExODI1OCwic2VxdWVuY2lhbEluc3RhbGFjYW8iOjEsInNlcXVlbmNpYWxDcmVkZW5jaWFsIjoxLCJhbWJpZW50ZSI6ImhvbW9sb2dhY2FvIiwiaWF0IjoxNzMyOTAyMjYwNjE1fQ";
	public static final String BASIC = "Basic ZXlKcFpDSTZJakExWXpJME9Ea3RPRFUyTVMwME5UUTNMV0l4TURZdElpd2lZMjlrYVdkdlVIVmliR2xqWVdSdmNpSTZNQ3dpWTI5a2FXZHZVMjltZEhkaGNtVWlPakV4T0RJMU9Dd2ljMlZ4ZFdWdVkybGhiRWx1YzNSaGJHRmpZVzhpT2pGOTpleUpwWkNJNkltSXlZV1l6TlRBdFpHSmtOUzAwWXpkbUxUbGlaRFV0WXpFeFlUaGhPVEpqWldVNU9XRmlOVE5oTnpBdElpd2lZMjlrYVdkdlVIVmliR2xqWVdSdmNpSTZNQ3dpWTI5a2FXZHZVMjltZEhkaGNtVWlPakV4T0RJMU9Dd2ljMlZ4ZFdWdVkybGhiRWx1YzNSaGJHRmpZVzhpT2pFc0luTmxjWFZsYm1OcFlXeERjbVZrWlc1amFXRnNJam94TENKaGJXSnBaVzUwWlNJNkltaHZiVzlzYjJkaFkyRnZJaXdpYVdGMElqb3hOek15T1RBeU1qWXdOakUxZlE=";
	// trocar pelo endereço de produção
	private static final String URL = "https://oauth.hm.bb.com.br/oauth/token";

	public static String gerarTokenAcesso()
			throws JsonMappingException, JsonProcessingException, UnsupportedEncodingException {
		StringBuilder formData = new StringBuilder();
		formData.append("grant_type=").append(URLEncoder.encode("client_credentials", "UTF-8"));
		formData.append("&scope=").append(URLEncoder.encode(
				"checkout.solicitacoes-info checkout.solicitacoes-requisicao checkout.pagamentos-info checkout.pagamentos-requisicao checkout.devolucoes-info checkout.devolucoes-requisicao checkout.participantes-info checkout.participantes-requisicao checkout.openfinance-info checkout.openfinance-requisicao",
				"UTF-8"));
		HttpClientResponse response = new HttpClientFacade(URL)
				.setConnectTimeout(1000)
				.setReadTimeout(3000)
				.setVerbose(true)
				.addRequestProperty("Content-Type", "application/x-www-form-urlencoded")
				.addRequestProperty("Authorization", BASIC)
				.POST("", formData.toString());

		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode rootNode = objectMapper.readTree(response.getContent());
		String jsonContent = rootNode.toString();
		RespostaToken respostaToken = objectMapper.readValue(jsonContent,
				new TypeReference<RespostaToken>() {
				});

		return respostaToken.getAccess_token();
	}

}
