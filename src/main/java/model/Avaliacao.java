package model;

public class Avaliacao {
    private String momento;
    private Double nota;
    private UnidadeCurricular unidadeCurricular;
    private Estudante estudante;

    public Avaliacao() {
    }

    public Avaliacao(String momento, Double nota, UnidadeCurricular unidadeCurricular, Estudante estudante) {
        this.momento = momento;
        this.nota = nota;
        this.unidadeCurricular = unidadeCurricular;
        this.estudante = estudante;
    }

    public String getMomento() {
        return momento;
    }

    public void setMomento(String momento) {
        this.momento = momento;
    }

    public Double getNota() {
        return nota;
    }

    public void setNota(Double nota) {
        this.nota = nota;
    }

    public UnidadeCurricular getUnidadeCurricular() {
        return unidadeCurricular;
    }

    public void setUnidadeCurricular(UnidadeCurricular unidadeCurricular) {
        this.unidadeCurricular = unidadeCurricular;
    }

    public Estudante getEstudante() {
        return estudante;
    }

    public void setEstudante(Estudante estudante) {
        this.estudante = estudante;
    }
}
