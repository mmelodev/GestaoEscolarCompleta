package br.com.arirang.plataforma.entity;

public enum TipoMovimentoFinanceiro {
    RECEITA("Receita"),
    DESPESA("Despesa");
    
    private final String descricao;
    
    TipoMovimentoFinanceiro(String descricao) {
        this.descricao = descricao;
    }
    
    public String getDescricao() {
        return descricao;
    }
}
