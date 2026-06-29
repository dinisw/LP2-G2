package DAL;

import model.EstatutoEstudante;
import java.util.List;

public interface IEstatutoEstudanteDAO {
    boolean registarEstatuto(EstatutoEstudante estatuto);
    boolean eliminarEstatuto(int id);
    EstatutoEstudante procurarPorId(int id);
    List<EstatutoEstudante> listarPorEstudante(int numeroMec);
    List<EstatutoEstudante> listarTodos();
}
