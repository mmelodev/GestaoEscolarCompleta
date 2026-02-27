package br.com.arirang.plataforma.dto;

import java.time.LocalDate;
import java.util.List;

public record ProfessorDTO(
        Long id,
        String nomeCompleto,
        LocalDate dataNascimento,
        String rg,
        String cpf,
        String email,
        String telefone,
        String cargo,
        String formacao,
        List<Long> turmaIds,
        List<String> turmaNomes
) {
    public static ProfessorDTO simple(Long id, String nomeCompleto, String cargo) {
        return new ProfessorDTO(id, nomeCompleto, null, null, null, null, null, cargo, null, null, null);
    }
}

