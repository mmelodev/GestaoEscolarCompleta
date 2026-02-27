package br.com.arirang.plataforma.entity;

public enum TipoNota {
    EXERCICIO("Exercício"),
    TRABALHO("Trabalho"),
    AVALIACAO("Avaliação"),
    PRODUCAO_ORAL("Produção Oral"),
    PRODUCAO_ESCRITA("Produção Escrita"),
    COMPREENSAO_ORAL("Compreensão Oral"),
    COMPREENSAO_ESCRITA("Compreensão Escrita"),
    PROVA_FINAL("Prova Final"),
    PRESENCA("Presença");
    
    private final String descricao;
    
    TipoNota(String descricao) {
        this.descricao = descricao;
    }
    
    public String getDescricao() {
        return descricao;
    }
}
