package DAL;

import model.Presenca;
import java.util.List;

public interface IPresencaDAO {
    boolean registarPresenca(Presenca presenca);
    boolean atualizarPresenca(Presenca presenca);
    Presenca procurarPorId(int id);
    List<Presenca> listarPorHorario(int horarioId);
    List<Presenca> listarPorEstudante(int numeroMec);
    List<Presenca> listarFaltasPorUC(int ucId);
    List<Presenca> listarTodas();
    boolean eliminarPresenca(int id);
}
