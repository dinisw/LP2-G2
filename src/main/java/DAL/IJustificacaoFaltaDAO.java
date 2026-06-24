package DAL;

import model.JustificacaoFalta;
import java.util.List;

public interface IJustificacaoFaltaDAO {
    boolean registarJustificacao(JustificacaoFalta justificacao);
    boolean atualizarJustificacao(JustificacaoFalta justificacao);
    JustificacaoFalta procurarPorId(int id);
    List<JustificacaoFalta> listarPorEstudante(int numeroMec);
    List<JustificacaoFalta> listarPendentes();
    List<JustificacaoFalta> listarTodas();
}
