package controller;

import BLL.EstudanteCalculo;
import DAL.TurmaCRUD;
import model.Curso;
import model.Estudante;
import model.Turma;
import model.UnidadeCurricular;

import java.util.List;

public class TurmaController {

    public Turma criarTurma(Curso curso, int anoCurricular, String anoLetivo) {
        // 1. Verifica se a turma já existe (Garante a regra: Só existe uma turma para cada ano letivo)
        if (existeTurma(curso, anoCurricular, anoLetivo)) {
            throw new IllegalStateException(
                    String.format("Já existe uma turma para o curso '%s', %dº ano, no ano letivo %s.",
                            curso.getNome(), anoCurricular, anoLetivo)
            );
        }

        // 2. Cria a nova instância da Turma (o construtor da Turma já valida se o ano é válido, etc.)
        Turma novaTurma = new Turma(anoCurricular, anoLetivo, curso);

        // 3. Guarda a turma em memória através da instância única (Singleton) do CRUD
        TurmaCRUD.getInstance().guardar(novaTurma);

        // 4. Devolve a turma criada para a View poder informar o utilizador
        return novaTurma;
    }

    public Turma obterTurma(Curso curso, int anoCurricular, String anoLetivo) {
        return TurmaCRUD.getInstance().encontrar(curso, anoCurricular, anoLetivo).orElse(null);
    }

    public List<Turma> listarTodas() {
        return TurmaCRUD.getInstance().listarTodas();
    }

    public boolean existeTurma(Curso curso, int anoCurricular, String anoLetivo) {
        return TurmaCRUD.getInstance().existeTurma(curso, anoCurricular, anoLetivo);
    }

    public void inscreverEstudante(Curso curso, int anoCurricular, String anoLetivo, Estudante estudante) {
        Turma turma = obterTurma(curso, anoCurricular, anoLetivo);
        if (turma == null) {
            throw new IllegalStateException("Erro: Turma não encontrada para o curso e ano especificados.");
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