package DAL;

import model.Avaliacao;
import model.Resultado;
import java.util.List;

public interface IAvaliacaoDAO {
    Resultado<Avaliacao> registarAvaliacao(Avaliacao avaliacao);
    List<Avaliacao> listarPorEstudante(int numeroMec);
    List<Avaliacao> listarPorUnidadeCurricular(String nomeUC);
    boolean eliminarAvaliacoesPorEstudante(int numeroMec);
}
