package DAL;

import model.Curso;
import model.Turma;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TurmaCRUD {

    private static TurmaCRUD instance;
    private final List<Turma> turmas;

    private TurmaCRUD() {
        this.turmas = new ArrayList<>();
    }

    public static TurmaCRUD getInstance() {
        if (instance == null) {
            instance = new TurmaCRUD();
        }
        return instance;
    }


    public void guardar(Turma turma) {
        if (existeTurma(turma.getCurso(), turma.getAnoCurricular(), turma.getAnoLetivo())) {
            throw new IllegalStateException(
                    String.format("Já existe uma turma para o curso '%s', %dº ano, no ano letivo %s.",
                            turma.getCurso().getNome(), turma.getAnoCurricular(), turma.getAnoLetivo())
            );
        }
        turmas.add(turma);
    }

    public boolean existeTurma(Curso curso, int anoCurricular, String anoLetivo) {
        return turmas.stream().anyMatch(t ->
                t.getCurso().equals(curso) &&
                        t.getAnoCurricular() == anoCurricular &&
                        t.getAnoLetivo().equals(anoLetivo)
        );
    }

    public Optional<Turma> encontrar(Curso curso, int anoCurricular, String anoLetivo) {
        return turmas.stream().filter(t ->
                t.getCurso().equals(curso) &&
                        t.getAnoCurricular() == anoCurricular &&
                        t.getAnoLetivo().equals(anoLetivo)
        ).findFirst();
    }

    public List<Turma> listarTodas() {
        return new ArrayList<>(turmas);
    }

    public List<Turma> listarPorCurso(Curso curso) {
        return turmas.stream()
                .filter(t -> t.getCurso().equals(curso))
                .toList();
    }
}