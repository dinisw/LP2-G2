package model;

import java.time.LocalDate;

public class UnidadeCurricular {
    private String nome;
    private int anoCurricular;
    private Docente docente;
    private final int ects = 6; // Valor fixo conforme requisito

    public UnidadeCurricular(String nome, int anoCurricular, Docente docente) {
        this.nome = nome;
        this.anoCurricular = anoCurricular;
        this.docente = docente;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getAnoCurricular() {
        return anoCurricular;
    }

    public void setAnoCurricular(int anoCurricular) {
        this.anoCurricular = anoCurricular;
    }

    public Docente getDocente() {
        return docente;
    }

    public void setDocente(Docente docente) {
        this.docente = docente;
    }

    public int getEcts() {
        return ects;
    }

    @Override
    public String toString() {
        return "UnidadeCurricular{" +
                "nome='" + nome + '\'' +
                ", anoCurricular=" + anoCurricular +
                ", docente=" + docente +
                ", ects=" + ects +
                '}';
    }
}
