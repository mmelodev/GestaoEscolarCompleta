package br.com.arirang.plataforma.enums;

public enum Modalidade {
    REGULAR("Regular", "Modalidade padrão com frequência obrigatória"),
    INTENSIVO("Intensivo", "Curso acelerado com carga horária concentrada"),
    EXTENSIVO("Extensivo", "Curso com duração prolongada e aprofundamento"),
    SEMI_INTENSIVO("Semi-Intensivo", "Modalidade intermediária entre regular e intensivo"),
    PREPARATORIO("Preparatório", "Foco em preparação para exames específicos"),
    CONVERSACAO("Conversação", "Ênfase na prática oral e comunicação"),
    GRAMATICA("Gramática", "Foco no estudo estrutural da língua"),
    BUSINESS("Business", "Inglês para ambiente corporativo"),
    ACADEMICO("Acadêmico", "Preparação para estudos internacionais"),
    VIAGEM("Viagem", "Inglês para turismo e viagens");

    private final String descricao;
    private final String explicacao;

    Modalidade(String descricao, String explicacao) {
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
