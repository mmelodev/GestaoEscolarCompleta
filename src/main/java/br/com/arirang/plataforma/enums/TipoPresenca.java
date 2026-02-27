package br.com.arirang.plataforma.enums;

/**
 * Enum para tipos de presença em aulas
 */
public enum TipoPresenca {
    PRESENTE("Presente"),
    FALTA("Falta"),
    FALTA_JUSTIFICADA("Falta Justificada"),
    ATRASO("Atraso");

    private final String descricao;

    TipoPresenca(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
