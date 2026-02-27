package br.com.arirang.plataforma.dto;

import java.util.List;

public class AlunoTurmaDTO {
    private Long id;
    private String nomeCompleto;
    private List<TurmaDTO> turmas;

    public AlunoTurmaDTO() {}

    public AlunoTurmaDTO(Long id, String nomeCompleto, List<TurmaDTO> turmas) {
        this.id = id;
        this.nomeCompleto = nomeCompleto;
        this.turmas = turmas;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public void setNomeCompleto(String nomeCompleto) {
        this.nomeCompleto = nomeCompleto;
    }

    public List<TurmaDTO> getTurmas() {
        return turmas;
    }

    public void setTurmas(List<TurmaDTO> turmas) {
        this.turmas = turmas;
    }
}
