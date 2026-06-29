package DAL;

import model.Docente;
import model.Resultado;
import java.util.List;

public interface IDocenteDAO {
    Resultado<Docente> registarDocente(Docente docente);
    Resultado<Docente> atualizarDocente(Docente docente);
    Resultado<Docente> eliminarDocente(int nif);
    List<Docente> getDocentes();
    Docente procurarPorSigla(String sigla);
    Docente procurarPorNif(int nif);
    Docente procurarPorEmail(String email);
}
