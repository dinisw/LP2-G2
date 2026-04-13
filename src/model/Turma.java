package model;

import java.util.ArrayList;
import java.util.List;

public class Turma {
    private Curso curso;
    private int anoCurricular; // 1, 2 ou 3 [cite: 18]
    private List<Estudante> estudantes;


    public Turma(String nome, Curso curso, int anoCurricular) {
        this.curso = curso;
        this.anoCurricular = anoCurricular;
        this.estudantes = new ArrayList<>();
    }

    public Curso getCurso() { return curso; }
    public int getAnoCurricular() { return anoCurricular; }
    public List<Estudante> getEstudantes() { return estudantes; }

    public void adicionarEstudante(Estudante e) {
        if (!estudantes.contains(e)) {
            estudantes.add(e);
        }
    }

    @Override
    public String toString() {
        return "Turma do " + anoCurricular + "º ano de " + curso.getNome() + " (Alunos: " + estudantes.size() + ")";
    }
}