package br.com.arirang.plataforma.enums;

public enum Formato {
    PRESENCIAL("Presencial", "Aulas presenciais na instituição"),
    ONLINE("Online", "Aulas virtuais via plataforma digital"),
    HIBRIDO("Híbrido", "Combinação de aulas presenciais e online"),
    SEMIPRESENCIAL("Semipresencial", "Maioria online com algumas aulas presenciais");

    private final String descricao;
    private final String explicacao;

    Formato(String descricao, String explicacao) {
        this.descricao = descricao;
        this.explicacao = explicacao;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getExplicacao() {
        return explicacao;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
