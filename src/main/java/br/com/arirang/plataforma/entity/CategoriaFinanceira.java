package br.com.arirang.plataforma.entity;

public enum CategoriaFinanceira {
    // Receitas
    MATRICULA("Matrícula"),
    MENSALIDADE("Mensalidade"),
    MATERIAL_DIDATICO("Material Didático"),
    CERTIFICADO("Certificado"),
    OUTRAS_RECEITAS("Outras Receitas"),
    
    // Despesas
    SALARIOS("Salários"),
    MATERIAL_ESCOLAR("Material Escolar"),
    ALUGUEL("Aluguel"),
    ENERGIA("Energia"),
    AGUA("Água"),
    TELEFONE("Telefone"),
    INTERNET("Internet"),
    MANUTENCAO("Manutenção"),
    MARKETING("Marketing"),
    OUTRAS_DESPESAS("Outras Despesas");
    
    private final String descricao;
    
    CategoriaFinanceira(String descricao) {
        this.descricao = descricao;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    public boolean isReceita() {
        return this == MATRICULA || this == MENSALIDADE || this == MATERIAL_DIDATICO || 
               this == CERTIFICADO || this == OUTRAS_RECEITAS;
    }
    
    public boolean isDespesa() {
        return !isReceita();
    }
}
