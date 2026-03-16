package com.liv.domain;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.util.ddd.domain.IRepository;

import org.greport.Borders;
import org.greport.LayoutGrid;
import org.greport.Report;
import org.greport.pdf.ReportExporterPDF;
import org.hibernate.annotations.Immutable;

import com.liv.infra.util.DateUtils;
import com.liv.infra.util.MesConverter;
import com.liv.infra.util.StringUtils;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Immutable
@Table(name = "vw_relatorios")
@NamedQueries({
                // GET /liv-api/domain/relatorio/query/recentes
                @NamedQuery(name = "Recentes", query = "SELECT r FROM Relatorio r ORDER BY r.id DESC")
})
public class Relatorio {

        @Id
        private Long id;

        @Column(name = "ano")
        private Integer ano;

        @Column(name = "mes")
        @Convert(converter = MesConverter.class)
        private Mes mes;

        @Column(name = "total_contratos")
        private Integer totalContratos = 0;

        @Column(name = "total_beneficios_concedidos")
        private Integer totalBeneficiosConcedidos = 0;

        @Column(name = "total_beneficios_aguardando")
        private Integer totalBeneficiosAguardando = 0;

        @Column(name = "dado_entrada")
        private Integer dadoEntrada = 0;

        // PATCH /liv-api/domain/relatorio/{id}/relacao-contratos-do-mes
        /*
         * body:
         * { }
         */
        public Object[] relacaoContratosDoMes() {
                String titulo = "RELAÇÃO DE TODOS OS CONTRATOS MÊS DE " + this.getMes().getDescricao().toUpperCase()
                                + " DE "
                                + this.getAno();
                String jpql = "SELECT p FROM Processo p WHERE YEAR(p.dataCriacao) = ?1 AND MONTH(p.dataCriacao) = ?2";
                return gerarRelatorio(titulo, jpql,
                                "relacao-de-todos-os-contratos-" + this.getMes().getDescricao().toLowerCase() + ".pdf");
        }

        // PATCH /liv-api/domain/relatorio/{id}/concessoes-do-mes
        /*
         * body:
         * { }
         */
        public Object[] concessoesDoMes() {
                String titulo = "CONCESSÃO DO MÊS DE " + this.getMes().getDescricao().toUpperCase() + " DE "
                                + this.getAno();
                String jpql = "SELECT p FROM Processo p WHERE YEAR(p.dataConcessao) = ?1 AND MONTH(p.dataConcessao) = ?2 AND (p.status = 'APROVADO')";
                return gerarRelatorio(titulo, jpql,
                                "concessoes-do-mes-" + this.getMes().getDescricao().toLowerCase() + ".pdf");
        }

        // PATCH /liv-api/domain/relatorio/{id}/pericia-avaliacao-social-do-mes
        /*
         * body:
         * { }
         */
        public Object[] periciaAvaliacaoSocialDoMes() {
                String titulo = "PERÍCIA E AVALIAÇÃO SOCIAL MÊS DE " + this.getMes().getDescricao().toUpperCase()
                                + " DE "
                                + this.getAno();
                String jpql = "SELECT p FROM Processo p WHERE ((YEAR(p.avaliacaoSocial) = ?1 AND MONTH(p.avaliacaoSocial) = ?2) OR (YEAR(p.periciaMedica) = ?1 AND MONTH(p.periciaMedica) = ?2)) AND (p.periciaMedica IS NOT NULL OR p.avaliacaoSocial IS NOT NULL)";
                return gerarRelatorio(titulo, jpql,
                                "pericia-e-avaliacao-social-" + this.getMes().getDescricao().toLowerCase() + ".pdf");
        }

        @Inject
        @Transient
        private IRepository repository;

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
                        grid.addValue(processo.getContrato().getCliente().getContato().getNome()).sethAlignLeft()
                                        .setFontSize(8);
                        grid.addValue(StringUtils.formatarCpf(processo.getContrato().getCliente().getCpf()))
                                        .sethAlignLeft().setFontSize(8);
                        grid.addValue(processo.getContrato().getBeneficio().getModalidade()).sethAlignLeft()
                                        .setFontSize(8);
                        grid.addValue(DateUtils.toBrazilianFormat(processo.getContrato().getInicio())).sethAlignLeft()
                                        .setFontSize(8);
                        grid.addValue(processo.getStatus().getNome()).sethAlignLeft().setFontSize(8);
                        grid.addValue(DateUtils.toBrazilianFormat(processo.getEntradaDoProtocolo())).sethAlignRight()
                                        .setFontSize(8);
                        grid.addValue(DateUtils.toBrazilianFormat(processo.getPericiaMedica())).sethAlignRight()
                                        .setFontSize(8);
                        grid.addValue(DateUtils.toBrazilianFormat(processo.getAvaliacaoSocial())).sethAlignRight()
                                        .setFontSize(8);
                        grid.addValue(DateUtils.toBrazilianFormat(processo.getContrato().getConclusao()))
                                        .sethAlignRight().setFontSize(8);
                }
        }

        // @TODO: Retirar data de conclusão; adicionar número de protocolo
        private Object[] gerarRelatorio(String tipo, String jpql, String fileName) {
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

                List<Processo> processos = repository.getAll(jpql, this.getAno(), this.getMes().getOrdinal());

                adicionarLinhasDoRelatorio(grid, processos);

                report.addGrid().setBorder(Borders.None).addRow()
                                .addValue("\nTotal de contratos fechados: " + this.getTotalContratos());
                report.addGrid().setBorder(Borders.None).addRow().addValue("Dado entrada: " + this.getDadoEntrada());
                report.addGrid().setBorder(Borders.None).addRow()
                                .addValue("Benefícios aguardando: " + this.getTotalBeneficiosAguardando());
                report.addGrid().setBorder(Borders.None).addRow()
                                .addValue("Total de benefícios concedidos: " + this.getTotalBeneficiosConcedidos());

                try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                        ReportExporterPDF.exportTo(report, baos);
                        System.out.println(Base64.getEncoder().encodeToString(baos.toByteArray()));
                        return new Object[] { baos.toByteArray(), fileName };
                } catch (Exception e) {
                        throw new RuntimeException(e);
                }
        }

}