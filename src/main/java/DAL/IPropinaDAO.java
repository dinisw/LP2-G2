package DAL;

import model.Propina;
import java.util.List;

public interface IPropinaDAO {
    boolean registarPropina(Propina propina);
    Propina procurarPropina(int numeroMec, int anoLetivo);
    boolean atualizarPropina(Propina propinaAtualizada);
    List<Propina> listarPropinasPorEstudante(int numeroMec);
    List<Propina> getTodasPropinas();
    boolean eliminarPropinasPorEstudante(int numeroMec);
}
