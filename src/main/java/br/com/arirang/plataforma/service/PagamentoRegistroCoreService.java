package br.com.arirang.plataforma.service;

import br.com.arirang.plataforma.dto.PagamentoDTO;
import br.com.arirang.plataforma.entity.Pagamento;
import br.com.arirang.plataforma.entity.Receita;
import br.com.arirang.plataforma.exception.BusinessException;
import br.com.arirang.plataforma.exception.ResourceNotFoundException;
import br.com.arirang.plataforma.mapper.PagamentoMapper;
import br.com.arirang.plataforma.repository.PagamentoRepository;
import br.com.arirang.plataforma.repository.ReceitaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Executa o núcleo do registro de pagamento em transação própria.
 * Usado por PagamentoService para evitar self-invocation e ciclo de dependência.
 */
@Service
public class PagamentoRegistroCoreService {

    private static final Logger logger = LoggerFactory.getLogger(PagamentoRegistroCoreService.class);

    @Autowired
    private ReceitaRepository receitaRepository;

    @Autowired
    private PagamentoRepository pagamentoRepository;

    @Autowired
    private PagamentoMapper pagamentoMapper;

    /**
     * Registra o pagamento e atualiza a receita. DTO já deve estar validado pelo chamador.
     */
    @Transactional
    public PagamentoDTO registrarPagamentoCore(PagamentoDTO pagamentoDTO) {
        logger.debug("Registrando pagamento para receita ID: {}", pagamentoDTO.receitaId());

        Receita receita = receitaRepository.findById(pagamentoDTO.receitaId())
                .orElseThrow(() -> new ResourceNotFoundException("Receita não encontrada com ID: " + pagamentoDTO.receitaId()));
        if (receita.getContrato() != null) {
            receita.getContrato().getId();
            if (receita.getContrato().getAluno() != null) {
                receita.getContrato().getAluno().getId();
            }
        }
        if (receita.getAluno() != null) {
            receita.getAluno().getId();
        }
        if ("PAGO".equals(receita.getSituacao())) {
            throw new BusinessException("Esta receita já foi paga integralmente");
        }

        Pagamento pagamento = pagamentoMapper.toEntity(pagamentoDTO);
        pagamento.setReceita(receita);
        Pagamento pagamentoSalvo = pagamentoRepository.save(pagamento);
        logger.info("Pagamento registrado com sucesso. ID: {}", pagamentoSalvo.getId());

        atualizarSituacaoReceita(receita, pagamentoDTO.valorPago(), Boolean.TRUE.equals(pagamentoDTO.isIntegral()));
        return pagamentoMapper.toDto(pagamentoSalvo);
    }

    private void atualizarSituacaoReceita(Receita receita, BigDecimal valorPago, boolean isIntegral) {
        if (isIntegral) {
            receita.setSituacao("PAGO");
            receita.setDataPagamento(LocalDate.now());
        } else {
            BigDecimal valorRestante = receita.getValorFinal().subtract(valorPago != null ? valorPago : BigDecimal.ZERO);
            if (valorRestante.compareTo(BigDecimal.ZERO) <= 0) {
                receita.setSituacao("PAGO");
                receita.setDataPagamento(LocalDate.now());
            } else {
                receita.setSituacao("PARCIAL");
            }
        }
        receitaRepository.save(receita);
    }
}
