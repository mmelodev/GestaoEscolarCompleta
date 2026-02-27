package br.com.arirang.plataforma.service;

import br.com.arirang.plataforma.dto.FrequenciaDTO;
import br.com.arirang.plataforma.dto.FrequenciaFormDTO;
import br.com.arirang.plataforma.dto.RelatorioFrequenciaDTO;
import br.com.arirang.plataforma.entity.Aluno;
import br.com.arirang.plataforma.entity.Frequencia;
import br.com.arirang.plataforma.entity.Turma;
import br.com.arirang.plataforma.entity.Usuario;
import br.com.arirang.plataforma.enums.TipoPresenca;
import br.com.arirang.plataforma.exception.ResourceNotFoundException;
import br.com.arirang.plataforma.repository.AlunoRepository;
import br.com.arirang.plataforma.repository.FrequenciaRepository;
import br.com.arirang.plataforma.repository.TurmaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class FrequenciaService {

    private static final Logger logger = LoggerFactory.getLogger(FrequenciaService.class);
    private static final Double PERCENTUAL_MINIMO_PADRAO = 75.0;

    @Autowired
    private FrequenciaRepository frequenciaRepository;

    @Autowired
    private AlunoRepository alunoRepository;

    @Autowired
    private TurmaRepository turmaRepository;

    @Autowired
    private UsuarioService usuarioService;

    /**
     * Registra ou atualiza frequência de um aluno
     */
    @Transactional
    public FrequenciaDTO registrarFrequencia(FrequenciaFormDTO formDTO) {
        Aluno aluno = alunoRepository.findById(formDTO.alunoId())
                .orElseThrow(() -> new ResourceNotFoundException("Aluno não encontrado: " + formDTO.alunoId()));
        
        Turma turma = turmaRepository.findById(formDTO.turmaId())
                .orElseThrow(() -> new ResourceNotFoundException("Turma não encontrada: " + formDTO.turmaId()));

        // Verificar se já existe registro para esta data
        Optional<Frequencia> frequenciaExistente = frequenciaRepository
                .findByAlunoAndTurmaAndDataAula(aluno, turma, formDTO.dataAula());

        Frequencia frequencia;
        if (frequenciaExistente.isPresent()) {
            frequencia = frequenciaExistente.get();
            frequencia.setTipoPresenca(formDTO.tipoPresenca());
            frequencia.setObservacao(formDTO.observacao());
            frequencia.setJustificativa(formDTO.justificativa());
        } else {
            frequencia = new Frequencia(aluno, turma, formDTO.dataAula(), formDTO.tipoPresenca());
            frequencia.setObservacao(formDTO.observacao());
            frequencia.setJustificativa(formDTO.justificativa());
        }

        // Registrar quem fez o registro
        Usuario usuarioAtual = getUsuarioAtual();
        frequencia.setRegistradoPor(usuarioAtual);

        Frequencia salva = frequenciaRepository.save(frequencia);
        logger.info("Frequência registrada: Aluno {} - Turma {} - Data {}", 
                   aluno.getId(), turma.getId(), formDTO.dataAula());

        return toDTO(salva);
    }

    /**
     * Registra frequência em lote para uma turma em uma data
     */
    @Transactional
    public List<FrequenciaDTO> registrarFrequenciaLote(Long turmaId, LocalDate dataAula, 
                                                       List<FrequenciaFormDTO> frequencias) {
        Turma turma = turmaRepository.findById(turmaId)
                .orElseThrow(() -> new ResourceNotFoundException("Turma não encontrada: " + turmaId));

        Usuario usuarioAtual = getUsuarioAtual();

        return frequencias.stream()
                .map(formDTO -> {
                    Aluno aluno = alunoRepository.findById(formDTO.alunoId())
                            .orElseThrow(() -> new ResourceNotFoundException("Aluno não encontrado: " + formDTO.alunoId()));

                    Optional<Frequencia> frequenciaExistente = frequenciaRepository
                            .findByAlunoAndTurmaAndDataAula(aluno, turma, dataAula);

                    Frequencia frequencia;
                    if (frequenciaExistente.isPresent()) {
                        frequencia = frequenciaExistente.get();
                        frequencia.setTipoPresenca(formDTO.tipoPresenca());
                        frequencia.setObservacao(formDTO.observacao());
                        frequencia.setJustificativa(formDTO.justificativa());
                    } else {
                        frequencia = new Frequencia(aluno, turma, dataAula, formDTO.tipoPresenca());
                        frequencia.setObservacao(formDTO.observacao());
                        frequencia.setJustificativa(formDTO.justificativa());
                    }

                    frequencia.setRegistradoPor(usuarioAtual);
                    return frequenciaRepository.save(frequencia);
                })
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Busca frequência por ID
     */
    @Transactional(readOnly = true)
    public FrequenciaDTO buscarPorId(Long id) {
        Frequencia frequencia = frequenciaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Frequência não encontrada: " + id));
        return toDTO(frequencia);
    }

    /**
     * Lista frequências de um aluno em uma turma
     */
    @Transactional(readOnly = true)
    public List<FrequenciaDTO> listarPorAlunoETurma(Long alunoId, Long turmaId) {
        Aluno aluno = alunoRepository.findById(alunoId)
                .orElseThrow(() -> new ResourceNotFoundException("Aluno não encontrado: " + alunoId));
        
        Turma turma = turmaRepository.findById(turmaId)
                .orElseThrow(() -> new ResourceNotFoundException("Turma não encontrada: " + turmaId));

        return frequenciaRepository.findByAlunoAndTurmaOrderByDataAulaDesc(aluno, turma)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lista frequências de uma turma em uma data
     */
    @Transactional(readOnly = true)
    public List<FrequenciaDTO> listarPorTurmaEData(Long turmaId, LocalDate dataAula) {
        Turma turma = turmaRepository.findById(turmaId)
                .orElseThrow(() -> new ResourceNotFoundException("Turma não encontrada: " + turmaId));

        return frequenciaRepository.findByTurmaAndDataAula(turma, dataAula)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Gera relatório de frequência de um aluno em uma turma
     */
    @Transactional(readOnly = true)
    public RelatorioFrequenciaDTO gerarRelatorioAluno(Long alunoId, Long turmaId, 
                                                      LocalDate dataInicio, LocalDate dataFim) {
        Aluno aluno = alunoRepository.findById(alunoId)
                .orElseThrow(() -> new ResourceNotFoundException("Aluno não encontrado: " + alunoId));
        
        Turma turma = turmaRepository.findById(turmaId)
                .orElseThrow(() -> new ResourceNotFoundException("Turma não encontrada: " + turmaId));

        // Buscar frequências do aluno no período
        List<Frequencia> frequencias = frequenciaRepository.findByTurmaAndPeriodo(turma, dataInicio, dataFim)
                .stream()
                .filter(f -> f.getAluno().getId().equals(alunoId))
                .collect(Collectors.toList());
        
        // Usar as frequências para cálculos mais precisos se necessário
        // Por enquanto, usamos os métodos de contagem do repository

        Long totalAulas = frequenciaRepository.countAulasByTurmaAndPeriodo(turma, dataInicio, dataFim);
        Long totalPresencas = frequenciaRepository.countByAlunoAndTurmaAndTipoPresenca(
                aluno, turma, TipoPresenca.PRESENTE);
        Long totalFaltas = frequenciaRepository.countByAlunoAndTurmaAndTipoPresenca(
                aluno, turma, TipoPresenca.FALTA);
        Long totalFaltasJustificadas = frequenciaRepository.countByAlunoAndTurmaAndTipoPresenca(
                aluno, turma, TipoPresenca.FALTA_JUSTIFICADA);
        Long totalAtrasos = frequenciaRepository.countByAlunoAndTurmaAndTipoPresenca(
                aluno, turma, TipoPresenca.ATRASO);

        return RelatorioFrequenciaDTO.calcular(
                alunoId, aluno.getNomeCompleto(),
                turmaId, turma.getNomeTurma(),
                totalAulas, totalPresencas, totalFaltas,
                totalFaltasJustificadas, totalAtrasos,
                dataInicio, dataFim, PERCENTUAL_MINIMO_PADRAO
        );
    }

    /**
     * Lista alunos com frequência abaixo do mínimo
     */
    @Transactional(readOnly = true)
    public List<RelatorioFrequenciaDTO> listarAlunosComFrequenciaBaixa(Long turmaId, 
                                                                       LocalDate dataInicio, 
                                                                       LocalDate dataFim,
                                                                       Double percentualMinimo) {
        Turma turma = turmaRepository.findById(turmaId)
                .orElseThrow(() -> new ResourceNotFoundException("Turma não encontrada: " + turmaId));

        final Double percentualMinimoFinal = (percentualMinimo != null) ? percentualMinimo : PERCENTUAL_MINIMO_PADRAO;

        List<Object[]> resultados = frequenciaRepository.findAlunosComFrequenciaBaixa(
                turma, dataInicio, dataFim, percentualMinimoFinal);

        return resultados.stream()
                .map(result -> {
                    Aluno aluno = (Aluno) result[0];
                    Long presencas = ((Number) result[1]).longValue();
                    Long totalAulas = ((Number) result[2]).longValue();
                    Long faltas = totalAulas - presencas;

                    return RelatorioFrequenciaDTO.calcular(
                            aluno.getId(), aluno.getNomeCompleto(),
                            turmaId, turma.getNomeTurma(),
                            totalAulas, presencas, faltas,
                            0L, 0L,
                            dataInicio, dataFim, percentualMinimoFinal
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * Remove frequência
     */
    @Transactional
    public void removerFrequencia(Long id) {
        if (!frequenciaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Frequência não encontrada: " + id);
        }
        frequenciaRepository.deleteById(id);
        logger.info("Frequência removida: {}", id);
    }

    // Métodos auxiliares

    private Usuario getUsuarioAtual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        
        String username = authentication.getName();
        return usuarioService.buscarPorUsername(username).orElse(null);
    }

    private FrequenciaDTO toDTO(Frequencia frequencia) {
        return new FrequenciaDTO(
                frequencia.getId(),
                frequencia.getAluno().getId(),
                frequencia.getAluno().getNomeCompleto(),
                frequencia.getTurma().getId(),
                frequencia.getTurma().getNomeTurma(),
                frequencia.getDataAula(),
                frequencia.getTipoPresenca(),
                frequencia.getObservacao(),
                frequencia.getJustificativa(),
                frequencia.getDataRegistro(),
                frequencia.getRegistradoPor() != null ? frequencia.getRegistradoPor().getUsername() : null
        );
    }
}
