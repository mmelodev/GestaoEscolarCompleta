package br.com.arirang.plataforma.dto;

import java.time.LocalDateTime;
import java.util.List;

public record BoletimDTO(
        Long id,
        Long alunoId,
        String alunoNome,
        Long turmaId,
        String turmaNome,
        List<NotaDTO> notas,
        Double mediaFinal,
        String situacaoFinal,
        LocalDateTime dataLancamento,
        boolean finalizado
) {}