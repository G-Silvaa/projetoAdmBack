package com.liv.domain;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.util.ddd.domain.IRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.zxing.WriterException;
import com.liv.infra.bbpay.Solicitacoes;
import com.liv.infra.bbpay.models.enums.EstadoSolicitacao;

@Service
public class FinanceiroService {
	
	@Autowired
	private IRepository repository;
	
	public void verificarPagamentos() throws IOException, WriterException {
		List<Financeiro> financeiros = repository.getAll("SELECT f FROM Financeiro f WHERE f.situacaoPagamento <> true AND f.numeroSolicitacaoPagamento IS NOT NULL");
		
		for(Financeiro financeiro : financeiros) {
			EstadoSolicitacao estadoSolicitacao = Solicitacoes.verificarSolicitacaoPagamento(financeiro.getNumeroSolicitacaoPagamento(), 98825);
			financeiro.setSituacaoParcela(estadoSolicitacao.getDescription());
			if(estadoSolicitacao.equals(EstadoSolicitacao.PAGA) && financeiro.getDataPagamentoParcela() == null) {
				financeiro.setParcelasPagas(financeiro.getParcelasPagas() + 1);
				financeiro.setParcelasRestantes(financeiro.getParcelasRestantes() - 1);

				double valorPago = Solicitacoes.pegarSolicitacaoPagamento(financeiro.getNumeroSolicitacaoPagamento(), 98825)
						.getValorSolicitacao();
				financeiro.setValorPagoDaParcela(new BigDecimal(valorPago));
				financeiro.setDataPagamentoParcela(new Date());
				financeiro.setMontantePago(financeiro.getMontantePago().add(new BigDecimal(valorPago)));
			}
		}
	}

}
