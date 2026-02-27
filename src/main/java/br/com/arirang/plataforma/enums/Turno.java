package br.com.arirang.plataforma.enums;

public enum Turno {
    MATUTINO("Matutino", "07:00 - 12:00"),
    VESPERTINO("Vespertino", "13:00 - 18:00"),
    NOTURNO("Noturno", "18:00 - 22:00"),
    INTEGRAL("Integral", "07:00 - 18:00");

    private final String descricao;
    private final String horario;

    Turno(String descricao, String horario) {
        this.descricao = descricao;
        this.horario = horario;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getHorario() {
        return horario;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
