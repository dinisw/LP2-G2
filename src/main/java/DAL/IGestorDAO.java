package DAL;

import model.Gestor;
import java.util.List;

public interface IGestorDAO {
    boolean registarGestor(Gestor gestor);
    List<Gestor> getGestores();
    Gestor procurarPorEmail(String email);
    Gestor procurarPorNif(int nif);
    boolean atualizarGestor(Gestor gestor);
    boolean eliminarGestor(int nif);
    Gestor getGestorPorID(int id);
}
