package DAL;

import model.Departamento;
import java.util.List;

public interface IDepartamentoDAO {
    boolean registarDepartamento(Departamento departamento);
    List<Departamento> getDepartamentos();
    Departamento procurarPorSigla(String sigla);
    boolean atualizarDepartamento(Departamento dep);
    boolean eliminarDepartamento(String sigla);
}
