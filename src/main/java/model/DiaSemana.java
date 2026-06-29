package model;

public enum DiaSemana {
    SEGUNDA("Segunda-feira"),
    TERCA("Terça-feira"),
    QUARTA("Quarta-feira"),
    QUINTA("Quinta-feira"),
    SEXTA("Sexta-feira");

    private final String descricao;

    DiaSemana(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() { return descricao; }

    @Override
    public String toString() { return descricao; }

    public static DiaSemana fromOrdinal(int ordinal) {
        for (DiaSemana d : values()) {
            if (d.ordinal() == ordinal) return d;
        }
        throw new IllegalArgumentException("Dia inválido: " + ordinal);
    }

    public static DiaSemana fromString(String s) {
        for (DiaSemana d : values()) {
            if (d.name().equalsIgnoreCase(s) || d.descricao.equalsIgnoreCase(s)) return d;
        }
        throw new IllegalArgumentException("Dia inválido: " + s);
    }
}
