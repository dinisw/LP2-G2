package model;

/**
 * Tipo de estatuto especial de estudante, gerido pelo gestor.
 * Exemplos: Atleta de Alta Competição, Trabalhador-Estudante, Pai/Mãe.
 */
public class TipoEstatuto {
    private int id;
    private String nome;
    private String descricao;

    public TipoEstatuto() {}

    public TipoEstatuto(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    @Override
    public String toString() {
        return String.format("%s — %s", nome, descricao);
    }
}
