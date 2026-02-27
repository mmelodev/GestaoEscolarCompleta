package br.com.arirang.plataforma.service;

import br.com.arirang.plataforma.dto.DeclaracaoMatriculaDTO;
import br.com.arirang.plataforma.dto.FichaMatriculaDTO;
import br.com.arirang.plataforma.entity.*;
import br.com.arirang.plataforma.entity.StatusContrato;
import br.com.arirang.plataforma.exception.BusinessException;
import br.com.arirang.plataforma.exception.ResourceNotFoundException;
import br.com.arirang.plataforma.repository.ContratoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DocumentoService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentoService.class);

    @Autowired
    private ContratoRepository contratoRepository;

    @Autowired
    private AlunoService alunoService;

    @Autowired
    private TurmaService turmaService;

    @Transactional(readOnly = true)
    public DeclaracaoMatriculaDTO gerarDeclaracaoMatricula(Long alunoId, Long turmaId) {
        try {
            logger.info("Gerando declaração de matrícula para aluno ID: {} e turma ID: {}", alunoId, turmaId);

            Aluno aluno = alunoService.buscarAlunoPorId(alunoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Aluno não encontrado com ID: " + alunoId));

            Turma turma = turmaService.buscarTurmaPorId(turmaId)
                    .orElseThrow(() -> new ResourceNotFoundException("Turma não encontrada com ID: " + turmaId));

            // Verificar se existe contrato ativo
            Optional<Contrato> contratoOpt = contratoRepository.findByAlunoIdAndTurmaIdAndSituacaoContrato(
                    alunoId, turmaId, StatusContrato.ATIVO.name());

            if (contratoOpt.isEmpty()) {
                throw new BusinessException("Aluno não possui contrato ativo nesta turma");
            }

            Contrato contrato = contratoOpt.get();

            DeclaracaoMatriculaDTO declaracao = new DeclaracaoMatriculaDTO(
                    aluno.getId(),
                    aluno.getNomeCompleto(),
                    aluno.getCpf(),
                    aluno.getRg(),
                    aluno.getDataNascimento(),
                    aluno.getEndereco(),
                    turma.getId(),
                    turma.getNomeTurma(),
                    turma.getNivelProficiencia(),
                    "Professor não atribuído", // Removido referência a Professor
                    turma.getDiaTurma(),
                    turma.getTurno() != null ? turma.getTurno().getDescricao() : null,
                    turma.getHoraInicio(),
                    turma.getHoraTermino(),
                    turma.getInicioTurma(),
                    turma.getTerminoTurma(),
                    contrato.getDataContrato(),
                    contrato.getValorMatricula(),
                    contrato.getValorMensalidade(),
                    contrato.getNumeroParcelas(),
                    LocalDate.now(),
                    gerarNumeroDeclaracao(aluno, turma)
            );

            logger.info("Declaração de matrícula gerada com sucesso");
            return declaracao;

        } catch (ResourceNotFoundException | BusinessException e) {
            logger.error("Erro de negócio ao gerar declaração: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Erro inesperado ao gerar declaração: ", e);
            throw new BusinessException("Erro ao gerar declaração: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public FichaMatriculaDTO gerarFichaMatricula(Long alunoId) {
        try {
            logger.info("Gerando ficha de matrícula para aluno ID: {}", alunoId);

            Aluno aluno = alunoService.buscarAlunoPorId(alunoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Aluno não encontrado com ID: " + alunoId));

            // Buscar todos os contratos ativos do aluno
            List<Contrato> contratos = contratoRepository.findByAlunoIdOrderByDataCriacaoDesc(alunoId)
                    .stream()
                    .filter(c -> "ATIVO".equals(c.getSituacaoContrato()))
                    .collect(Collectors.toList());

            FichaMatriculaDTO ficha = new FichaMatriculaDTO(
                    aluno.getId(),
                    aluno.getNomeCompleto(),
                    aluno.getNomeSocial(),
                    aluno.getCpf(),
                    aluno.getRg(),
                    aluno.getOrgaoExpeditorRg(),
                    aluno.getNacionalidade(),
                    aluno.getUf(),
                    aluno.getDataNascimento(),
                    aluno.getGenero(),
                    aluno.getEmail(),
                    aluno.getTelefone(),
                    aluno.getEndereco(),
                    aluno.getSituacao(),
                    aluno.getUltimoNivel(),
                    aluno.getResponsavel() != null ? aluno.getResponsavel().getNomeCompleto() : null,
                    aluno.getResponsavel() != null ? aluno.getResponsavel().getCpf() : null,
                    aluno.getResponsavel() != null ? aluno.getResponsavel().getTelefone() : null,
                    aluno.getResponsavel() != null ? aluno.getResponsavel().getEmail() : null,
                    aluno.getGrauParentesco(),
                    aluno.isResponsavelFinanceiro(),
                    contratos.stream().map(contrato -> new FichaMatriculaDTO.ContratoResumoDTO(
                            contrato.getId(),
                            contrato.getTurma().getNomeTurma(),
                            contrato.getTurma().getNivelProficiencia(),
                            contrato.getDataContrato(),
                            contrato.getValorMatricula(),
                            contrato.getValorMensalidade(),
                            contrato.getNumeroParcelas(),
                            contrato.getSituacaoContrato() != null ? 
                                StatusContrato.valueOf(contrato.getSituacaoContrato()) : null
                    )).collect(Collectors.toList()),
                    LocalDate.now(),
                    gerarNumeroFicha(aluno)
            );

            logger.info("Ficha de matrícula gerada com sucesso");
            return ficha;

        } catch (ResourceNotFoundException e) {
            logger.error("Recurso não encontrado ao gerar ficha: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Erro inesperado ao gerar ficha: ", e);
            throw new BusinessException("Erro ao gerar ficha: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<DeclaracaoMatriculaDTO> gerarDeclaracoesPorTurma(Long turmaId) {
        try {
            logger.info("Gerando declarações para turma ID: {}", turmaId);

            List<Contrato> contratos = contratoRepository.findByTurmaIdOrderByDataCriacaoDesc(turmaId)
                    .stream()
                    .filter(c -> "ATIVO".equals(c.getSituacaoContrato()))
                    .collect(Collectors.toList());

            List<DeclaracaoMatriculaDTO> declaracoes = contratos.stream()
                    .map(contrato -> {
                        try {
                            return gerarDeclaracaoMatricula(contrato.getAluno().getId(), turmaId);
                        } catch (Exception e) {
                            logger.warn("Erro ao gerar declaração para aluno ID {}: {}", 
                                      contrato.getAluno().getId(), e.getMessage());
                            return null;
                        }
                    })
                    .filter(declaracao -> declaracao != null)
                    .collect(Collectors.toList());

            logger.info("Geradas {} declarações para turma ID: {}", declaracoes.size(), turmaId);
            return declaracoes;

        } catch (Exception e) {
            logger.error("Erro inesperado ao gerar declarações por turma: ", e);
            throw new BusinessException("Erro ao gerar declarações: " + e.getMessage());
        }
    }

    private String gerarNumeroDeclaracao(Aluno aluno, Turma turma) {
        String dataAtual = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return String.format("DEC-%s-%d-%d", dataAtual, aluno.getId(), turma.getId());
    }

    private String gerarNumeroFicha(Aluno aluno) {
        String dataAtual = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return String.format("FICHA-%s-%d", dataAtual, aluno.getId());
    }
}
