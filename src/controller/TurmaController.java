package controller;

import DAL.TurmaCRUD;
import model.Curso;
import model.Estudante;
import model.Turma;
import model.UnidadeCurricular;

import java.util.List;

public class TurmaController {

    public Turma criarTurma(Curso curso, int anoCurricular, String anoLetivo) {
        if (existeTurma(curso, anoCurricular, anoLetivo)) {
            throw new IllegalStateException(String.format("Já existe uma turma para o curso '%s', %dº ano.", curso.getNome(), anoCurricular));
        }
        Turma novaTurma = new Turma(anoCurricular, anoLetivo, curso);
        TurmaCRUD.guardar(novaTurma);
        return novaTurma;
    }

    public Turma obterTurma(Curso curso, int anoCurricular, String anoLetivo) {
        return TurmaCRUD.encontrar(curso.getNome(), anoCurricular, anoLetivo);
    }

    public List<Turma> listarTodas() {
        return TurmaCRUD.listarTodas();
    }

    public boolean existeTurma(Curso curso, int anoCurricular, String anoLetivo) {
        return TurmaCRUD.procurarPorCursoEAno(curso.getNome(), anoCurricular) != null;
    }

    public void inscreverEstudante(Curso curso, int anoCurricular, String anoLetivo, Estudante estudante) {
        Turma turma = obterTurma(curso, anoCurricular, anoLetivo);
        if (turma == null) {
            throw new IllegalStateException("Turma não encontrada.");
        }
        turma.adicionarEstudante(estudante);
    }

    public void adicionarUC(Curso curso, int anoCurricular, String anoLetivo, UnidadeCurricular uc) {
        Turma turma = obterTurma(curso, anoCurricular, anoLetivo);
        if (turma == null) {
            throw new IllegalStateException("Turma não encontrada.");
        }
        turma.adicionarUnidadeCurricular(uc);
    }
}