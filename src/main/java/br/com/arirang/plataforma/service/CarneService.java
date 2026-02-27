package br.com.arirang.plataforma.service;

import br.com.arirang.plataforma.dto.CarneDTO;
import br.com.arirang.plataforma.dto.ParcelaDTO;
import br.com.arirang.plataforma.entity.*;
import br.com.arirang.plataforma.exception.BusinessException;
import br.com.arirang.plataforma.exception.ResourceNotFoundException;
import br.com.arirang.plataforma.repository.ContratoRepository;
import br.com.arirang.plataforma.repository.ParcelaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CarneService {

    private static final Logger logger = LoggerFactory.getLogger(CarneService.class);

    @Autowired
    private ContratoRepository contratoRepository;

    @Autowired
    private ParcelaRepository parcelaRepository;

    @Transactional(readOnly = true)
    public CarneDTO gerarCarnePorContrato(Long contratoId) {
        try {
            logger.info("Gerando carnê para contrato ID: {}", contratoId);

            Contrato contrato = contratoRepository.findById(contratoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Contrato não encontrado com ID: " + contratoId));

            if (!"ATIVO".equals(contrato.getSituacaoContrato())) {
                throw new BusinessException("Não é possível gerar carnê para contrato inativo");
            }

            List<Parcela> parcelas = parcelaRepository.findByContratoId(contratoId);
            
            if (parcelas.isEmpty()) {
                throw new BusinessException("Contrato não possui parcelas para gerar carnê");
            }

            CarneDTO carneDTO = new CarneDTO(
                    contrato.getId(),
                    contrato.getAluno().getNomeCompleto(),
                    contrato.getAluno().getCpf(),
                    contrato.getAluno().getTelefone(),
                    contrato.getAluno().getEmail(),
                    contrato.getTurma().getNomeTurma(),
                    "Professor não atribuído", // Removido referência a Professor
                    contrato.getValorMatricula(),
                    contrato.getValorMensalidade(),
                    contrato.getNumeroParcelas(),
                    contrato.getDataContrato(),
                    contrato.getValorTotalContrato(),
                    parcelas.stream().map(this::convertParcelaToDTO).collect(Collectors.toList()),
                    LocalDate.now(),
                    gerarNumeroCarne(contrato)
            );

            logger.info("Carnê gerado com sucesso para contrato ID: {}", contratoId);
            return carneDTO;

        } catch (ResourceNotFoundException | BusinessException e) {
            logger.error("Erro de negócio ao gerar carnê: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Erro inesperado ao gerar carnê: ", e);
            throw new BusinessException("Erro ao gerar carnê: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public CarneDTO gerarCarnePorAluno(Long alunoId) {
        try {
            logger.info("Gerando carnê para aluno ID: {}", alunoId);

            List<Contrato> contratos = contratoRepository.findByAlunoIdOrderByDataCriacaoDesc(alunoId);
            
            if (contratos.isEmpty()) {
                throw new BusinessException("Aluno não possui contratos ativos");
            }

            // Buscar contrato ativo mais recente
            Optional<Contrato> contratoAtivo = contratos.stream()
                    .filter(c -> "ATIVO".equals(c.getSituacaoContrato()))
                    .findFirst();

            if (contratoAtivo.isEmpty()) {
                throw new BusinessException("Aluno não possui contratos ativos");
            }

            return gerarCarnePorContrato(contratoAtivo.get().getId());

        } catch (BusinessException e) {
            logger.error("Erro de negócio ao gerar carnê por aluno: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Erro inesperado ao gerar carnê por aluno: ", e);
            throw new BusinessException("Erro ao gerar carnê: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<CarneDTO> gerarCarnesPorTurma(Long turmaId) {
        try {
            logger.info("Gerando carnês para turma ID: {}", turmaId);

            List<Contrato> contratos = contratoRepository.findByTurmaIdOrderByDataCriacaoDesc(turmaId);
            
            List<CarneDTO> carnes = new ArrayList<>();
            
            for (Contrato contrato : contratos) {
                if ("ATIVO".equals(contrato.getSituacaoContrato())) {
                    try {
                        CarneDTO carne = gerarCarnePorContrato(contrato.getId());
                        carnes.add(carne);
                    } catch (Exception e) {
                        logger.warn("Erro ao gerar carnê para contrato ID {}: {}", contrato.getId(), e.getMessage());
                    }
                }
            }

            logger.info("Gerados {} carnês para turma ID: {}", carnes.size(), turmaId);
            return carnes;

        } catch (Exception e) {
            logger.error("Erro inesperado ao gerar carnês por turma: ", e);
            throw new BusinessException("Erro ao gerar carnês: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<CarneDTO> gerarCarnesVencidos() {
        try {
            logger.info("Gerando carnês com parcelas vencidas");

            List<Parcela> parcelasVencidas = parcelaRepository.findParcelasVencidas(LocalDate.now());
            
            List<CarneDTO> carnes = new ArrayList<>();
            
            for (Parcela parcela : parcelasVencidas) {
                try {
                    CarneDTO carne = gerarCarnePorContrato(parcela.getContrato().getId());
                    carnes.add(carne);
                } catch (Exception e) {
                    logger.warn("Erro ao gerar carnê para parcela ID {}: {}", parcela.getId(), e.getMessage());
                }
            }

            logger.info("Gerados {} carnês com parcelas vencidas", carnes.size());
            return carnes;

        } catch (Exception e) {
            logger.error("Erro inesperado ao gerar carnês vencidos: ", e);
            throw new BusinessException("Erro ao gerar carnês vencidos: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<CarneDTO> gerarCarnesPorPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        try {
            logger.info("Gerando carnês para período: {} a {}", dataInicio, dataFim);

            List<Parcela> parcelasPeriodo = parcelaRepository.findByPeriodoVencimento(dataInicio, dataFim);
            
            List<CarneDTO> carnes = new ArrayList<>();
            
            for (Parcela parcela : parcelasPeriodo) {
                try {
                    CarneDTO carne = gerarCarnePorContrato(parcela.getContrato().getId());
                    carnes.add(carne);
                } catch (Exception e) {
                    logger.warn("Erro ao gerar carnê para parcela ID {}: {}", parcela.getId(), e.getMessage());
                }
            }

            logger.info("Gerados {} carnês para o período", carnes.size());
            return carnes;

        } catch (Exception e) {
            logger.error("Erro inesperado ao gerar carnês por período: ", e);
            throw new BusinessException("Erro ao gerar carnês por período: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public CarneDTO gerarCarneEspecifico(Long contratoId, List<Long> parcelasIds) {
        try {
            logger.info("Gerando carnê específico para contrato ID: {} com parcelas: {}", contratoId, parcelasIds);

            Contrato contrato = contratoRepository.findById(contratoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Contrato não encontrado com ID: " + contratoId));

            List<Parcela> parcelasEspecificas = parcelaRepository.findAllById(parcelasIds);
            
            // Filtrar apenas parcelas do contrato
            parcelasEspecificas = parcelasEspecificas.stream()
                    .filter(p -> p.getContrato().getId().equals(contratoId))
                    .collect(Collectors.toList());

            if (parcelasEspecificas.isEmpty()) {
                throw new BusinessException("Nenhuma parcela válida encontrada para o contrato");
            }

            CarneDTO carneDTO = new CarneDTO(
                    contrato.getId(),
                    contrato.getAluno().getNomeCompleto(),
                    contrato.getAluno().getCpf(),
                    contrato.getAluno().getTelefone(),
                    contrato.getAluno().getEmail(),
                    contrato.getTurma().getNomeTurma(),
                    "Professor não atribuído", // Removido referência a Professor
                    contrato.getValorMatricula(),
                    contrato.getValorMensalidade(),
                    contrato.getNumeroParcelas(),
                    contrato.getDataContrato(),
                    contrato.getValorTotalContrato(),
                    parcelasEspecificas.stream().map(this::convertParcelaToDTO).collect(Collectors.toList()),
                    LocalDate.now(),
                    gerarNumeroCarne(contrato)
            );

            logger.info("Carnê específico gerado com sucesso para contrato ID: {}", contratoId);
            return carneDTO;

        } catch (ResourceNotFoundException | BusinessException e) {
            logger.error("Erro de negócio ao gerar carnê específico: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Erro inesperado ao gerar carnê específico: ", e);
            throw new BusinessException("Erro ao gerar carnê específico: " + e.getMessage());
        }
    }

    private String gerarNumeroCarne(Contrato contrato) {
        // Formato: CARNE-YYYY-MM-DD-CONTRATO_ID
        String dataAtual = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return String.format("CARNE-%s-%d", dataAtual, contrato.getId());
    }

    private ParcelaDTO convertParcelaToDTO(Parcela parcela) {
        return new ParcelaDTO(
                parcela.getId(),
                parcela.getContrato().getId(),
                parcela.getNumeroParcela(),
                parcela.getValorParcela(),
                parcela.getDataVencimento(),
                parcela.getDataPagamento(),
                parcela.getValorPago(),
                parcela.getJurosAplicados(),
                parcela.getMultaAplicada(),
                parcela.getDescontoAplicado(),
                parcela.getStatusParcela(),
                parcela.getObservacoes(),
                parcela.getValorComJurosEMulta(),
                parcela.isVencida(),
                parcela.isEmAtraso()
        );
    }
}
