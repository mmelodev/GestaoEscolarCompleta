package br.com.arirang.plataforma.entity;

public enum StatusParcela {
    PENDENTE("Pendente"),
    PAGA("Paga"),
    EM_ATRASO("Em Atraso"),
    CANCELADA("Cancelada");
    
    private final String descricao;
    
    StatusParcela(String descricao) {
        this.descricao = descricao;
    }
    
    public String getDescricao() {
        return descricao;
    }
}
