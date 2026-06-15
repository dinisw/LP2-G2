package DAL;

import model.Horario;
import java.util.List;

public interface IHorarioDAO {
    boolean registarHorario(Horario horario);
    boolean eliminarHorario(int id);
    Horario procurarPorId(int id);
    List<Horario> listarPorUC(int ucId);
    List<Horario> listarPorAnoLetivo(int anoLetivoId);
    List<Horario> listarTodos();
}
