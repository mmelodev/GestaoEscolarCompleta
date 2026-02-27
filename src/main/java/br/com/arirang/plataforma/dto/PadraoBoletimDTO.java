package br.com.arirang.plataforma.dto;

public record PadraoBoletimDTO(
        Long id,
        Long turmaId,
        String turmaNome,
        String descricaoExercicio,
        String descricaoTrabalho,
        String descricaoAvaliacao,
        String descricaoProducaoOral,
        String descricaoProducaoEscrita,
        String descricaoCompreensaoOral,
        String descricaoCompreensaoEscrita,
        String descricaoProvaFinal,
        String descricaoPresenca
) {}
