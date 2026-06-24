package DAL;

import model.TipoEstatuto;
import java.util.List;

public interface ITipoEstatutoDAO {
    boolean registarTipoEstatuto(TipoEstatuto tipo);
    boolean eliminarTipoEstatuto(int id);
    TipoEstatuto procurarPorId(int id);
    TipoEstatuto procurarPorNome(String nome);
    List<TipoEstatuto> listarTodos();
}
