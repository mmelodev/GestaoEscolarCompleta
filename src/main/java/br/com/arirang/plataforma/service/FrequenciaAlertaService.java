package br.com.arirang.plataforma.service;

import br.com.arirang.plataforma.dto.RelatorioFrequenciaDTO;
import br.com.arirang.plataforma.entity.Turma;
import br.com.arirang.plataforma.repository.TurmaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service para gerenciar alertas automáticos de frequência
 */
@Service
@Transactional
public class FrequenciaAlertaService {

    private static final Logger logger = LoggerFactory.getLogger(FrequenciaAlertaService.class);
    private static final Double PERCENTUAL_MINIMO_PADRAO = 75.0;

    @Autowired
    private FrequenciaService frequenciaService;

    @Autowired
    private TurmaRepository turmaRepository;

    /**
     * Verifica e gera alertas para alunos com frequência baixa
     * Executa diariamente às 8h
     */
    @Scheduled(cron = "0 0 8 * * ?") // Todo dia às 8h
    public void verificarFrequenciaBaixa() {
        logger.info("Iniciando verificação automática de frequência baixa");
        
        LocalDate hoje = LocalDate.now();
        LocalDate dataInicio = hoje.minusMonths(1); // Último mês
        
        List<Turma> turmasAtivas = turmaRepository.findAll()
                .stream()
                .filter(t -> t.getSituacaoTurma() != null && 
                            (t.getSituacaoTurma().equalsIgnoreCase("ATIVA") || 
                             t.getSituacaoTurma().equalsIgnoreCase("EM_ANDAMENTO")))
                .collect(Collectors.toList());

        for (Turma turma : turmasAtivas) {
            try {
                List<RelatorioFrequenciaDTO> alunosComFrequenciaBaixa = 
                        frequenciaService.listarAlunosComFrequenciaBaixa(
                                turma.getId(), dataInicio, hoje, PERCENTUAL_MINIMO_PADRAO);

                if (!alunosComFrequenciaBaixa.isEmpty()) {
                    logger.warn("Turma {} - {} alunos com frequência abaixo de {}%", 
                               turma.getNomeTurma(), 
                               alunosComFrequenciaBaixa.size(), 
                               PERCENTUAL_MINIMO_PADRAO);
                    
                    // Aqui você pode adicionar lógica para enviar emails, notificações, etc.
                    // Por enquanto, apenas logamos
                    alunosComFrequenciaBaixa.forEach(aluno -> {
                        logger.warn("ALERTA: Aluno {} - Frequência: {:.2f}%", 
                                   aluno.alunoNome(), 
                                   aluno.percentualFrequencia());
                    });
                }
            } catch (Exception e) {
                logger.error("Erro ao verificar frequência da turma {}: {}", 
                           turma.getId(), e.getMessage(), e);
            }
        }
        
        logger.info("Verificação automática de frequência concluída");
    }

    /**
     * Gera alertas para uma turma específica
     */
    @Transactional(readOnly = true)
    public List<RelatorioFrequenciaDTO> gerarAlertasTurma(Long turmaId, 
                                                          LocalDate dataInicio, 
                                                          LocalDate dataFim,
                                                          Double percentualMinimo) {
        if (percentualMinimo == null) {
            percentualMinimo = PERCENTUAL_MINIMO_PADRAO;
        }

        return frequenciaService.listarAlunosComFrequenciaBaixa(
                turmaId, dataInicio, dataFim, percentualMinimo);
    }

    /**
     * Verifica se um aluno está em risco de reprovação por frequência
     */
    @Transactional(readOnly = true)
    public boolean verificarRiscoReprovacao(Long alunoId, Long turmaId, 
                                            LocalDate dataInicio, 
                                            LocalDate dataFim,
                                            Double percentualMinimo) {
        if (percentualMinimo == null) {
            percentualMinimo = PERCENTUAL_MINIMO_PADRAO;
        }

        RelatorioFrequenciaDTO relatorio = frequenciaService.gerarRelatorioAluno(
                alunoId, turmaId, dataInicio, dataFim);

        return relatorio.abaixoDoMinimo();
    }
}
