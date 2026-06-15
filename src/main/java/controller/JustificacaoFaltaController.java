package controller;

import DAL.DAOFactory;
import DAL.IJustificacaoFaltaDAO;
import DAL.IPresencaDAO;
import model.*;

import java.util.List;

/**
 * Gere o fluxo de justificação de faltas (Enunciado v1.3):
 *   Estudante submete pedido → Gestor aprova ou rejeita.
 */
public class JustificacaoFaltaController {
    private final IJustificacaoFaltaDAO justificacaoDAO;
    private final IPresencaDAO presencaDAO;

    public JustificacaoFaltaController() {
        this.justificacaoDAO = DAOFactory.getJustificacaoFaltaDAO();
        this.presencaDAO     = DAOFactory.getPresencaDAO();
    }

    public Resultado<JustificacaoFalta> submeterJustificacao(int presencaId, int numeroMec,
                                                              TipoJustificacao tipo, String descricao) {
        Presenca presenca = presencaDAO.procurarPorId(presencaId);
        if (presenca == null) return new Resultado<>(false, "Registo de presença não encontrado.");
        if (!presenca.isFalta()) return new Resultado<>(false, "Não existe falta registada nesta aula.");
        if (presenca.getEstudante().getNumeroMec() != numeroMec)
            return new Resultado<>(false, "Este registo de presença não pertence ao estudante indicado.");

        boolean jaExiste = justificacaoDAO.listarPorEstudante(numeroMec).stream()
                .anyMatch(j -> j.getPresenca().getId() == presencaId);
        if (jaExiste) return new Resultado<>(false, "Já existe um pedido de justificação para esta falta.");

        if (tipo == null) return new Resultado<>(false, "O tipo de justificação é obrigatório.");
        if (descricao == null || descricao.trim().isEmpty())
            return new Resultado<>(false, "A descrição é obrigatória.");

        EstudanteController estCtrl = new EstudanteController();
        Estudante est = estCtrl.procurarEstudantePorNumeroMec(numeroMec);
        if (est == null) return new Resultado<>(false, "Estudante não encontrado.");

        JustificacaoFalta j = new JustificacaoFalta(est, presenca, tipo, descricao.trim());
        justificacaoDAO.registarJustificacao(j);
        return new Resultado<>(j, true);
    }

    public Resultado<JustificacaoFalta> aprovarJustificacao(int id, String observacao) {
        return alterarEstado(id, JustificacaoFalta.Estado.APROVADA, observacao);
    }

    public Resultado<JustificacaoFalta> rejeitarJustificacao(int id, String observacao) {
        return alterarEstado(id, JustificacaoFalta.Estado.REJEITADA, observacao);
    }

    private Resultado<JustificacaoFalta> alterarEstado(int id, JustificacaoFalta.Estado novoEstado, String obs) {
        JustificacaoFalta j = justificacaoDAO.procurarPorId(id);
        if (j == null) return new Resultado<>(false, "Justificação não encontrada.");
        if (j.getEstado() != JustificacaoFalta.Estado.PENDENTE)
            return new Resultado<>(false, "Esta justificação já foi " + j.getEstado().name().toLowerCase() + ".");

        j.setEstado(novoEstado);
        j.setObservacaoGestor(obs != null ? obs.trim() : "");
        justificacaoDAO.atualizarJustificacao(j);
        return new Resultado<>(j, true);
    }

    public List<JustificacaoFalta> listarPendentes() { return justificacaoDAO.listarPendentes(); }
    public List<JustificacaoFalta> listarPorEstudante(int numeroMec) { return justificacaoDAO.listarPorEstudante(numeroMec); }
    public List<JustificacaoFalta> listarTodas() { return justificacaoDAO.listarTodas(); }
}
