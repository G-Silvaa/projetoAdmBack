package com.liv.domain;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import org.greport.Borders;
import org.greport.LayoutGrid;
import org.greport.Report;
import org.greport.pdf.ReportExporterPDF;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.liv.infra.repository.DDDRepository;
import com.liv.infra.util.DateUtils;
import com.liv.infra.util.StringUtils;

@Service
public class RelatorioService {

	@Autowired
	private DDDRepository repository;

	// POST /liv-api/domain/service/relatorio-relatorio/relacao-contratos
	/*
	 * body:
	 * {
	 * 		"domain": null,
	 * 		"args": {
	 * 			"intervalo": {
	 * 				"inicio": {
	 * 					"ano": 2024,
	 * 					"mes": "JANEIRO"
	 * 				},
	 * 				"termino": {
	 * 					"ano": 2024,
	 * 					"mes": "NOVEMBRO"
	 * 				}
	 * 			}
	 * 		}
	 * }
	 */
	public Object[] relacaoContratos(Intervalo intervalo) {
		String titulo = ((intervalo.getInicio().getAno().equals(intervalo.getTermino().getAno()))
				&& (intervalo.getInicio().getMes().equals(intervalo.getTermino().getMes() != null ? Mes.values()[intervalo.getTermino().getMes().getOrdinal() - 1] : null)))
						? "RELAÇÃO DE TODOS OS CONTRATOS MÊS DE "
								+ intervalo.getInicio().getMes().getDescricao().toUpperCase() + " DE "
								+ intervalo.getInicio().getAno()
						: "RELAÇÃO DE TODOS OS CONTRATOS MÊS DE "
								+ intervalo.getInicio().getMes().getDescricao().toUpperCase() + " DE "
								+ intervalo.getInicio().getAno() + " À "
								+ intervalo.getTermino().getMes().getDescricao().toUpperCase() + " DE "
								+ intervalo.getTermino().getAno();
		String jpql = "SELECT p FROM Processo p WHERE (YEAR(p.dataCriacao) > ?1 OR (YEAR(p.dataCriacao) = ?1 AND MONTH(p.dataCriacao) >= ?2)) AND (YEAR(p.dataCriacao) < ?3 OR (YEAR(p.dataCriacao) = ?3 AND MONTH(p.dataCriacao) <= ?4))";
		String mes = ((intervalo.getInicio().getAno().equals(intervalo.getTermino().getAno()))
				&& (intervalo.getInicio().getMes().equals(intervalo.getTermino().getMes() != null ? Mes.values()[intervalo.getTermino().getMes().getOrdinal() - 1] : null))) 
					? intervalo.getInicio().getMes().getDescricao().toLowerCase() 
					: intervalo.getInicio().getMes().getDescricao().toLowerCase() + "-ate-" + intervalo.getTermino().getMes().getDescricao().toLowerCase();
		return gerarRelatorio(titulo, jpql, "relacao-de-todos-os-contratos-" + mes + ".pdf", intervalo);
	}

	// POST /liv-api/domain/service/relatorio-service/concessoess
	/*
	 * body:
	 * {
	 * 		"domain": null,
	 * 		"args": {
	 * 			"intervalo": {
	 * 				"inicio": {
	 * 					"ano": 2024,
	 * 					"mes": "JANEIRO"
	 * 				},
	 * 				"termino": {
	 * 					"ano": 2024,
	 * 					"mes": "NOVEMBRO"
	 * 				}
	 * 			}
	 * 		}
	 * }
	 */
	public Object[] concessoes(Intervalo intervalo) {
		String titulo = ((intervalo.getInicio().getAno().equals(intervalo.getTermino().getAno()))
				&& (intervalo.getInicio().getMes().equals(intervalo.getTermino().getMes() != null ? Mes.values()[intervalo.getTermino().getMes().getOrdinal() - 1] : null)))
						? "CONCESSÃO DO MÊS DE " + intervalo.getInicio().getMes().getDescricao().toUpperCase() + " DE "
								+ intervalo.getInicio().getAno()
						: "CONCESSÃO DO MÊS DE " + intervalo.getInicio().getMes().getDescricao().toUpperCase() + " DE "
								+ intervalo.getInicio().getAno() + " À "
								+ intervalo.getTermino().getMes().getDescricao().toUpperCase() + " DE "
								+ intervalo.getTermino().getAno();
		String jpql = "SELECT p FROM Processo p WHERE (p.status = 'APROVADO') AND (YEAR(p.dataConcessao) > ?1 OR (YEAR(p.dataConcessao) = ?1 AND MONTH(p.dataConcessao) >= ?2)) AND (YEAR(p.dataConcessao) < ?3 OR (YEAR(p.dataConcessao) = ?3 AND MONTH(p.dataConcessao) <= ?4))";
		String mes = ((intervalo.getInicio().getAno().equals(intervalo.getTermino().getAno()))
				&& (intervalo.getInicio().getMes().equals(intervalo.getTermino().getMes() != null ? Mes.values()[intervalo.getTermino().getMes().getOrdinal() - 1] : null))) 
					? intervalo.getInicio().getMes().getDescricao().toLowerCase() 
					: intervalo.getInicio().getMes().getDescricao().toLowerCase() + "-ate-" + intervalo.getTermino().getMes().getDescricao().toLowerCase();
		return gerarRelatorio(titulo, jpql, "concessoes-" + mes + ".pdf", intervalo);
	}

	// POST /liv-api/domain/service/relatorio-service/pericia-avaliacao-social
	/*
	 * body:
	 * {
	 * 		"domain": null,
	 * 		"args": {
	 * 			"intervalo": {
	 * 				"inicio": {
	 * 					"ano": 2024,
	 * 					"mes": "JANEIRO"
	 * 				},
	 * 				"termino": {
	 * 					"ano": 2024,
	 * 					"mes": "NOVEMBRO"
	 * 				}
	 * 			}
	 * 		}
	 * }
	 */
	public Object[] periciaAvaliacaoSocial(Intervalo intervalo) {
		String titulo = ((intervalo.getInicio().getAno().equals(intervalo.getTermino().getAno()))
				&& (intervalo.getInicio().getMes().equals(intervalo.getTermino().getMes() != null ? Mes.values()[intervalo.getTermino().getMes().getOrdinal() - 1] : null)))
						? "PERÍCIA E AVALIAÇÃO SOCIAL MÊS DE "
								+ intervalo.getInicio().getMes().getDescricao().toUpperCase() + " DE "
								+ intervalo.getInicio().getAno()
						: "PERÍCIA E AVALIAÇÃO SOCIAL MÊS DE "
								+ intervalo.getInicio().getMes().getDescricao().toUpperCase() + " DE "
								+ intervalo.getInicio().getAno() + " À "
								+ intervalo.getTermino().getMes().getDescricao().toUpperCase() + " DE "
								+ intervalo.getTermino().getAno();
		String jpql = "SELECT p FROM Processo p WHERE (p.periciaMedica IS NOT NULL OR p.avaliacaoSocial IS NOT NULL) AND "
				+ "((YEAR(p.avaliacaoSocial) > ?1 OR (YEAR(p.avaliacaoSocial) = ?1 AND MONTH(p.avaliacaoSocial) >= ?2)) AND (YEAR(p.avaliacaoSocial) < ?3 OR (YEAR(p.avaliacaoSocial) = ?3 AND MONTH(p.avaliacaoSocial) <= ?4))) OR "
				+ "((YEAR(p.periciaMedica) > ?1 OR (YEAR(p.periciaMedica) = ?1 AND MONTH(p.periciaMedica) >= ?2)) AND (YEAR(p.periciaMedica) < ?3 OR (YEAR(p.periciaMedica) = ?3 AND MONTH(p.periciaMedica) <= ?4)))";
		String mes = ((intervalo.getInicio().getAno().equals(intervalo.getTermino().getAno()))
				&& (intervalo.getInicio().getMes().equals(intervalo.getTermino().getMes() != null ? Mes.values()[intervalo.getTermino().getMes().getOrdinal() - 1] : null))) 
					? intervalo.getInicio().getMes().getDescricao().toLowerCase() 
					: intervalo.getInicio().getMes().getDescricao().toLowerCase() + "-ate-" + intervalo.getTermino().getMes().getDescricao().toLowerCase();
		return gerarRelatorio(titulo, jpql, "pericia-e-avaliacao-social-" + mes + ".pdf", intervalo);
	}

	private Report criarReport(String titulo) {
		Report report = new Report();
		report.addHeaderGrid().setBorder(Borders.None)
				.addRow()
				.addValue(titulo).sethAlignCenter();

		report.addFooterGrid().setFontSize(6).setBorder(Borders.None)
				.addRow()
				.addValue("LIV Assessoria Previdenciária").sethAlignLeft().setFontBold()
				.addValue().sethAlignRight()
				.addRow()
				.addValue("Avenida 4 de julho, Av. Carlos Jereissati, 387").sethAlignLeft()
				.addValue()
				.addRow()
				.addValue("CEP: 61.901.080 | Maracanaú, CE").sethAlignLeft()
				.addValue("Emitido em " + new Date().toString()).sethAlignRight();

		report.setPageHeight(595f);
		report.setPageWidth(842f);
		return report;
	}

	private void adicionarLinhasDoRelatorio(LayoutGrid grid, List<Processo> processos) {
		for (Processo processo : processos) {
			grid.addRow();
			grid.addValue(processo.getContrato().getCliente().getContato().getNome()).sethAlignLeft().setFontSize(8);
			grid.addValue(StringUtils.formatarCpf(processo.getContrato().getCliente().getCpf())).sethAlignLeft().setFontSize(8);
			grid.addValue(processo.getContrato().getBeneficio().getModalidade()).sethAlignLeft().setFontSize(8);
			grid.addValue(DateUtils.toBrazilianFormat(processo.getContrato().getInicio())).sethAlignLeft().setFontSize(8);
			grid.addValue(processo.getStatus().getNome()).sethAlignLeft().setFontSize(8);
			grid.addValue(DateUtils.toBrazilianFormat(processo.getEntradaDoProtocolo())).sethAlignRight()
					.setFontSize(8);
			grid.addValue(DateUtils.toBrazilianFormat(processo.getPericiaMedica())).sethAlignRight()
					.setFontSize(8);
			grid.addValue(DateUtils.toBrazilianFormat(processo.getAvaliacaoSocial())).sethAlignRight()
					.setFontSize(8);
			grid.addValue(DateUtils.toBrazilianFormat(processo.getContrato().getConclusao())).sethAlignRight().setFontSize(8);
		}
	}

	// @TODO: Retirar data de conclusão; adicionar número de protocolo
	private Object[] gerarRelatorio(String tipo, String jpql, String fileName, Intervalo range) {
		Report report = criarReport(tipo);
		LayoutGrid grid = report.addGrid();
		grid.addColumn("Beneficiário");
		grid.addColumn("CPF").setWidth(0.7f);
		grid.addColumn("Modalidade").setWidth(0.7f);
		grid.addColumn("Fechamento do Contrato");
		grid.addColumn("Situação");
		grid.addColumn("Data de Entrada no Protocolo");
		grid.addColumn("Perícia Médica");
		grid.addColumn("Avaliação Social");
		grid.addColumn("Conclusão");

		List<Processo> processos = repository.getAll(jpql, range.getInicio().getAno(),
				range.getInicio().getMes().getOrdinal(), range.getTermino().getAno(),
				range.getTermino().getMes().getOrdinal());

		adicionarLinhasDoRelatorio(grid, processos);

		jpql = "SELECT SUM(r.totalContratos) FROM Relatorio r WHERE r.id BETWEEN ?1 AND ?2";
		report.addGrid().setBorder(Borders.None).addRow()
				.addValue("\nTotal de contratos fechados: " + repository
						.getAll(jpql, (range.getInicio().getAno() * 100 + range.getInicio().getMes().getOrdinal()),
								(range.getTermino().getAno() * 100 + range.getTermino().getMes().getOrdinal()))
						.get(0));
		jpql = "SELECT SUM(r.dadoEntrada) FROM Relatorio r WHERE r.id BETWEEN ?1 AND ?2";
		report.addGrid().setBorder(Borders.None).addRow()
				.addValue("Dado entrada: " + repository
						.getAll(jpql, (range.getInicio().getAno() * 100 + range.getInicio().getMes().getOrdinal()),
								(range.getTermino().getAno() * 100 + range.getTermino().getMes().getOrdinal()))
						.get(0));
		jpql = "SELECT SUM(r.totalBeneficiosAguardando) FROM Relatorio r WHERE r.id BETWEEN ?1 AND ?2";
		report.addGrid().setBorder(Borders.None).addRow()
				.addValue("Benefícios aguardando: " + repository
						.getAll(jpql, (range.getInicio().getAno() * 100 + range.getInicio().getMes().getOrdinal()),
								(range.getTermino().getAno() * 100 + range.getTermino().getMes().getOrdinal()))
						.get(0));
		jpql = "SELECT SUM(r.totalBeneficiosConcedidos) FROM Relatorio r WHERE r.id BETWEEN ?1 AND ?2";
		report.addGrid().setBorder(Borders.None).addRow()
				.addValue("Total de benefícios concedidos: " + repository
						.getAll(jpql, (range.getInicio().getAno() * 100 + range.getInicio().getMes().getOrdinal()),
								(range.getTermino().getAno() * 100 + range.getTermino().getMes().getOrdinal()))
						.get(0));

		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			ReportExporterPDF.exportTo(report, baos);
			System.out.println(Base64.getEncoder().encodeToString(baos.toByteArray()));
			return new Object[] { baos.toByteArray(), fileName };
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}