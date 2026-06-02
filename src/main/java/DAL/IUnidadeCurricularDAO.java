package DAL;

import model.UnidadeCurricular;
import java.util.List;

public interface IUnidadeCurricularDAO {
    boolean registarUC(UnidadeCurricular uc);
    List<UnidadeCurricular> getUnidadeCurriculars();
    UnidadeCurricular procurarPorNome(String nome);
    UnidadeCurricular procurarPorId(int id);
    boolean atualizarUC(String nomeAtual, UnidadeCurricular uc);
    boolean atualizarUCPorId(int id, UnidadeCurricular uc);
    boolean eliminarUCPorId(int id);
    boolean eliminarUC(String nomeAtual);
}
