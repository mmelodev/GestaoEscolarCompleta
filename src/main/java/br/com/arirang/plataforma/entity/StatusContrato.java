package br.com.arirang.plataforma.entity;

public enum StatusContrato {
    ATIVO("Ativo"),
    CANCELADO("Cancelado"),
    FINALIZADO("Finalizado"),
    SUSPENSO("Suspenso");
    
    private final String descricao;
    
    StatusContrato(String descricao) {
        this.descricao = descricao;
    }
    
    public String getDescricao() {
        return descricao;
    }
}
