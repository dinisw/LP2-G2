package model;

import java.util.ArrayList;
import java.util.List;

public class Turma {

    private int anoCurricular;
    private String anoLetivo;
    private Curso curso;
    private List<Estudante> estudantes;
    private List<UnidadeCurricular> unidadesCurriculares;

    public Turma(int anoCurricular, String anoLetivo, Curso curso) {
        if (anoCurricular < 1 || anoCurricular > 3) {
            throw new IllegalArgumentException("O ano curricular deve ser entre 1 e 3.");
        }
        if (anoLetivo == null || anoLetivo.isBlank()) {
            throw new IllegalArgumentException("O ano letivo não pode ser vazio.");
        }
        if (curso == null) {
            throw new IllegalArgumentException("O curso não pode ser nulo.");
        }
        this.anoCurricular = anoCurricular;
        this.anoLetivo = anoLetivo;
        this.curso = curso;
        this.estudantes = new ArrayList<>();
        this.unidadesCurriculares = new ArrayList<>();
    }

    // ── Regras de negócio ──────────────────────────────────────────────────────

    public void adicionarEstudante(Estudante estudante) {
        if (estudante == null) {
            throw new IllegalArgumentException("Estudante inválido.");
        }
        if (estudantes.contains(estudante)) {
            throw new IllegalStateException("O estudante já está inscrito nesta turma.");
        }
        estudantes.add(estudante);
    }

    public void adicionarUnidadeCurricular(UnidadeCurricular uc) {
        if (uc == null) {
            throw new IllegalArgumentException("Unidade curricular inválida.");
        }
        if (unidadesCurriculares.size() >= 5) {
            throw new IllegalStateException("Limite máximo de 5 unidades curriculares por ano atingido.");
        }
        if (unidadesCurriculares.contains(uc)) {
            throw new IllegalStateException("A unidade curricular já está registada nesta turma.");
        }
        unidadesCurriculares.add(uc);
    }

    // ── Getters ────────────────────────────────────────────────────────────────

    public int getAnoCurricular()                          { return anoCurricular; }
    public String getAnoLetivo()                           { return anoLetivo; }
    public Curso getCurso()                                { return curso; }
    public List<Estudante> getEstudantes()                 { return new ArrayList<>(estudantes); }
    public List<UnidadeCurricular> getUnidadesCurriculares() { return new ArrayList<>(unidadesCurriculares); }

    @Override
    public String toString() {
        return String.format("Turma | Curso: %s | Ano Curricular: %dº | Ano Letivo: %s | Estudantes: %d | UCs: %d",
                curso.getNome(), anoCurricular, anoLetivo, estudantes.size(), unidadesCurriculares.size());
    }
}