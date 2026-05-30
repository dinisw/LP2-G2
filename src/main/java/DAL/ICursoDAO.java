package DAL;

import model.Curso;
import model.Resultado;
import java.util.List;

public interface ICursoDAO {
    Resultado<Curso> registarCurso(Curso curso);
    List<Curso> getCursos();
    Curso procurarPorNome(String nome);
    Resultado<Curso> atualizarCurso(String nomeAntigo, Curso cursoNovo);
    Resultado<Curso> eliminarCurso(String nome);
    Resultado<Curso> registarArranqueAno(String nomeCurso, Curso cursoAtualizado);
    boolean existeCursoComDepartamento(String siglaDepartamento);
}
