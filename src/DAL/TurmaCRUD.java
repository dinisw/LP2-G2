package DAL;

import model.Turma;
import java.util.ArrayList;
import java.util.List;

public class TurmaCRUD {
    private static List<Turma> turmasBD = new ArrayList<>();

    public static void guardar(Turma turma) {
        turmasBD.add(turma);
    }

    public static List<Turma> listarTodas() {
        return new ArrayList<>(turmasBD);
    }

    public static Turma procurarPorCursoEAno(String nomeCurso, int ano) {
        return turmasBD.stream()
                .filter(t -> t.getCurso().getNome().equalsIgnoreCase(nomeCurso) && t.getAnoCurricular() == ano)
                .findFirst()
                .orElse(null);
    }

    // Exemplo de como seria a exportação para CSV
    public static String toCSVHeader() {
        return "Curso;AnoCurricular;TotalAlunos";
    }
}