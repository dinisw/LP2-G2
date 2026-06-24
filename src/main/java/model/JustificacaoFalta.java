package model;

import java.time.LocalDate;

/**
 * Pedido de justificação de falta submetido por um estudante.
 * Fluxo: estudante submete → gestor aprova ou rejeita.
 */
public class JustificacaoFalta {
    public enum Estado { PENDENTE, APROVADA, REJEITADA }

    private int id;
    private Estudante estudante;
    private Presenca presenca;
    private TipoJustificacao tipo;
    private String descricao;
    private LocalDate dataSubmissao;
    private Estado estado;
    private String observacaoGestor;

    public JustificacaoFalta() {}

    public JustificacaoFalta(Estudante estudante, Presenca presenca, TipoJustificacao tipo, String descricao) {
        this.estudante = estudante;
        this.presenca = presenca;
        this.tipo = tipo;
        this.descricao = descricao;
        this.dataSubmissao = LocalDate.now();
        this.estado = Estado.PENDENTE;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Estudante getEstudante() { return estudante; }
    public void setEstudante(Estudante estudante) { this.estudante = estudante; }

    public Presenca getPresenca() { return presenca; }
    public void setPresenca(Presenca presenca) { this.presenca = presenca; }

    public TipoJustificacao getTipo() { return tipo; }
    public void setTipo(TipoJustificacao tipo) { this.tipo = tipo; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public LocalDate getDataSubmissao() { return dataSubmissao; }
    public void setDataSubmissao(LocalDate dataSubmissao) { this.dataSubmissao = dataSubmissao; }

    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }

    public String getObservacaoGestor() { return observacaoGestor; }
    public void setObservacaoGestor(String obs) { this.observacaoGestor = obs; }

    @Override
    public String toString() {
        return String.format("Justificação #%d | Tipo: %s | Estado: %s | Data: %s\n  Descrição: %s",
                id, tipo, estado, dataSubmissao, descricao);
    }
}
