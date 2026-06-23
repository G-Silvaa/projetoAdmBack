package com.liv.api.controller;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.liv.api.dto.BeneficioConcedidoDTO;
import com.liv.api.dto.ProcessoCessandoDTO;
import com.liv.infra.repository.DDDRepository;

@RestController
@RequestMapping(value = "/processo")
public class ProcessoController {
	
	@Autowired
	private DDDRepository repository;
	
	@GetMapping("/cessacao")
	public ResponseEntity<?> getProcessosCessando(
	        @RequestParam(defaultValue = "0") int page,
	        @RequestParam(defaultValue = "10") int size) {

	    String jpql = "SELECT new com.liv.api.dto.ProcessoCessandoDTO(" +
	            "    c.cliente.contato.nome, " +
	            "    c.cliente.cpf, " +
	            "    c.cliente.contato.telefone, " +
	            "    p.numeroProtocolo, " +
	            "    p.status, " +
	            "    c.beneficio, " +
	            "    p.dataConcessao, " +
	            "    p.cessacao" +
	            ") " +
	            "FROM Processo p " +
	            "JOIN p.contrato c " +
	            "WHERE p.cessacao IS NOT NULL " +
	            "ORDER BY p.cessacao";

	    List<ProcessoCessandoDTO> processos = repository.getAll(jpql, new Date());
	    List<ProcessoCessandoDTO> processosEmCessacao = new ArrayList<>();
	    
	    Calendar calendar = Calendar.getInstance();
	    calendar.add(Calendar.DAY_OF_YEAR, 10);
	    Date dataLimite = calendar.getTime();
	    
	    for (ProcessoCessandoDTO processo : processos) {
	        Date cessacao = processo.getCessacao();
	        
	        if (cessacao != null) {
	            // Ajusta a data caso seja final de semana
	            Calendar cessacaoCalendar = Calendar.getInstance();
	            cessacaoCalendar.setTime(cessacao);
	            
	            int dayOfWeek = cessacaoCalendar.get(Calendar.DAY_OF_WEEK);
	            if (dayOfWeek == Calendar.SATURDAY) {
	                cessacaoCalendar.add(Calendar.DAY_OF_MONTH, +2);
	            } else if (dayOfWeek == Calendar.SUNDAY) {
	                cessacaoCalendar.add(Calendar.DAY_OF_MONTH, +1);
	            }
	            
	            Date adjustedCessacao = cessacaoCalendar.getTime();

	            if (!adjustedCessacao.before(new Date()) && !adjustedCessacao.after(dataLimite)) {
	                processosEmCessacao.add(processo);
	            }
	        }
	    }
	    
	    int start = Math.min(page * size, processosEmCessacao.size());
	    int end = Math.min((page + 1) * size, processosEmCessacao.size());

	    List<ProcessoCessandoDTO> processosPaginados = processosEmCessacao.subList(start, end);
	    Page<ProcessoCessandoDTO> processoPage = new PageImpl<>(processosPaginados, PageRequest.of(page, size), processosEmCessacao.size());
	    
	    return ResponseEntity.ok(processoPage);
	}
	
	@GetMapping("/concedido")
	public ResponseEntity<?> getProcessosComBeneficioConcedido(
			@RequestParam(defaultValue = "0") int page,
	        @RequestParam(defaultValue = "10") int size) {
		
		String jpql = "SELECT new com.liv.api.dto.BeneficioConcedidoDTO(" +
	            "    c.cliente.contato.nome, " +
	            "    c.cliente.cpf, " +
	            "    c.cliente.contato.telefone, " +
	            "    p.numeroProtocolo, " +
	            "    p.status, " +
	            "    c.beneficio, " +
	            "    p.dataConcessao " +
	            ") " +
	            "FROM Processo p " +
	            "JOIN p.contrato c " +
	            "WHERE p.dataConcessao IS NOT NULL " +
	            "AND p.contrato.beneficio <> 'AUXILIO_INCAPACIDADE_TEMPORARIA'" +
	            "ORDER BY p.dataConcessao";

	    List<BeneficioConcedidoDTO> processos = repository.getAll(jpql, new Date());
	    List<BeneficioConcedidoDTO> processosAprovados = new ArrayList<>();
	    
	    Calendar calendarLimiteInferior = Calendar.getInstance();
	    calendarLimiteInferior.add(Calendar.DAY_OF_YEAR, -61);
	    Date dataLimiteInferior = calendarLimiteInferior.getTime();

	    Calendar calendarLimiteSuperior = Calendar.getInstance();
	    calendarLimiteSuperior.add(Calendar.DAY_OF_YEAR, -50);
	    Date dataLimiteSuperior = calendarLimiteSuperior.getTime();

	    for (BeneficioConcedidoDTO processo : processos) {
	        Date concessao = processo.getDataConcessao();
	        
	        if (concessao != null) {
	            // Ajusta a data caso seja final de semana
	            Calendar concessaoCalendar = Calendar.getInstance();
	            concessaoCalendar.setTime(concessao);
	            
	            int dayOfWeek = concessaoCalendar.get(Calendar.DAY_OF_WEEK);
	            if (dayOfWeek == Calendar.SATURDAY) {
	                concessaoCalendar.add(Calendar.DAY_OF_MONTH, +2);
	            } else if (dayOfWeek == Calendar.SUNDAY) {
	                concessaoCalendar.add(Calendar.DAY_OF_MONTH, +1);
	            }
	            
	            Date adjustedConcessao = concessaoCalendar.getTime();

	            // Verifica se a data de concessão ajustada está entre 50 e 60 dias atrás
	            if (adjustedConcessao.before(new Date()) && adjustedConcessao.after(dataLimiteInferior)) {
	                if (adjustedConcessao.before(dataLimiteSuperior)) {
	                    processosAprovados.add(processo);
	                }
	            }
	        }
	    }

	    
	    int start = Math.min(page * size, processosAprovados.size());
	    int end = Math.min((page + 1) * size, processosAprovados.size());

	    List<BeneficioConcedidoDTO> processosPaginados = processosAprovados.subList(start, end);
	    Page<BeneficioConcedidoDTO> processoPage = new PageImpl<>(processosPaginados, PageRequest.of(page, size), processosAprovados.size());
	    
	    return ResponseEntity.ok(processoPage);
	}

}
