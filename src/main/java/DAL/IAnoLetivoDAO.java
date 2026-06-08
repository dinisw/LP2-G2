package DAL;

import model.*;

import java.util.List;

public interface IAnoLetivoDAO {
    boolean registarAnoLetivo(AnoLetivo anoLetivo);
    AnoLetivo obterAnoAtual();
    List<AnoLetivo> listarTodos();
    AnoLetivo obterPorAnoCalendario(int anoCalendario);
    boolean atualizarAnoLetivo(AnoLetivo anoLetivo);

    // Snapshot: guardar o estado do ano ao concluí-lo
    int salvarCursoSnapshot(AnoLetivoCursoSnapshot snapshot);
    void salvarUCSnapshot(AnoLetivoUCSnapshot snapshot);
    int salvarEstudanteSnapshot(AnoLetivoEstudanteSnapshot snapshot);
    void salvarNotaSnapshot(AnoLetivoNotaSnapshot snapshot);

    // Snapshot: ler o estado de um ano passado
    List<AnoLetivoCursoSnapshot> obterCursosSnapshot(int anoLetivoId);
    List<AnoLetivoUCSnapshot> obterUCsSnapshot(int anoLetivoCursoId);
    List<AnoLetivoEstudanteSnapshot> obterEstudantesSnapshot(int anoLetivoCursoId);
    List<AnoLetivoNotaSnapshot> obterNotasSnapshot(int anoLetivoEstudanteId);
}
