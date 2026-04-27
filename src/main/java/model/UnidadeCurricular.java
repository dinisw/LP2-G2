package main.model;

import java.util.List;
import java.util.ArrayList;

public class UnidadeCurricular {
    private String nome;
    private int anoCurricular;
    private int semestre;
    private Docente docente;
    private List<String> momentosAvaliacao;
    private final int ects = 6; // Valor fixo conforme requisito


    public UnidadeCurricular(String nome, int anoCurricular,int semestre, Docente docente) {
        this.nome = nome;
        this.anoCurricular = anoCurricular;
        this.semestre = semestre;
        this.docente = docente;
        this.momentosAvaliacao = new ArrayList<>();
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

    public int getSemestre() {
        return semestre;
    }

    public void setSemestre(int semestre) {
        this.semestre = semestre;
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

    public List<String> getMomentosAvaliacao() {
        return momentosAvaliacao;
    }

    public void adicionarMomento (String momento) {
        if (momento != null && !momento.trim().isEmpty()) {
            this.momentosAvaliacao.add(momento.trim());
        }
    }

    public void setMomentosAvaliacao(List<String> momentosAvaliacao) {
        this.momentosAvaliacao = momentosAvaliacao;
    }

    @Override
    public String toString() {
        return "UnidadeCurricular{" +
                "nome='" + nome + '\'' +
                ", anoCurricular=" + anoCurricular +
                ", semestre=" + semestre +
                ", docente=" + docente +
                ", ects=" + ects +
                '}';
    }
}
