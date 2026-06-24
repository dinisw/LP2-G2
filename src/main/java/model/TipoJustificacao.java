package model;

public enum TipoJustificacao {
    BAIXA_MEDICA("Baixa Médica"),
    CASAMENTO("Casamento"),
    ESTATUTO_ATLETA("Estatuto de Atleta"),
    ESTATUTO_TRABALHADOR("Estatuto de Trabalhador-Estudante"),
    ESTATUTO_PAI("Estatuto de Pai/Mãe");

    private final String descricao;

    TipoJustificacao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() { return descricao; }

    @Override
    public String toString() { return descricao; }

    public static TipoJustificacao fromString(String s) {
        for (TipoJustificacao t : values()) {
            if (t.name().equalsIgnoreCase(s) || t.descricao.equalsIgnoreCase(s)) return t;
        }
        throw new IllegalArgumentException("Tipo de justificação inválido: " + s);
    }
}
