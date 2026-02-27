package br.com.arirang.plataforma.entity;

public enum FormaPagamento {
    DINHEIRO("Dinheiro"),
    PIX("PIX"),
    CARTAO_CREDITO("Cartão de Crédito"),
    CARTAO_DEBITO("Cartão de Débito"),
    TRANSFERENCIA_BANCARIA("Transferência Bancária"),
    BOLETO("Boleto"),
    CHEQUE("Cheque"),
    DEPOSITO("Depósito"),
    OUTROS("Outros");
    
    private final String descricao;
    
    FormaPagamento(String descricao) {
        this.descricao = descricao;
    }
    
    public String getDescricao() {
        return descricao;
    }
}
