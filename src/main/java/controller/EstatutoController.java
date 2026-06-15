package controller;

import DAL.DAOFactory;
import DAL.IEstatutoEstudanteDAO;
import DAL.ITipoEstatutoDAO;
import model.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Gere estatutos especiais de estudantes (Enunciado v1.3).
 * O gestor cria tipos de estatuto e associa-os a estudantes.
 */
public class EstatutoController {
    private final ITipoEstatutoDAO tipoDAO;
    private final IEstatutoEstudanteDAO estatutoDAO;

    public EstatutoController() {
        this.tipoDAO     = DAOFactory.getTipoEstatutoDAO();
        this.estatutoDAO = DAOFactory.getEstatutoEstudanteDAO();
    }

    // ── Tipos de Estatuto ────────────────────────────────────────────

    public Resultado<TipoEstatuto> registarTipoEstatuto(String nome, String descricao) {
        if (nome == null || nome.trim().isEmpty()) return new Resultado<>(false, "O nome do estatuto é obrigatório.");
        if (tipoDAO.procurarPorNome(nome.trim()) != null)
            return new Resultado<>(false, "Já existe um tipo de estatuto com esse nome.");

        TipoEstatuto tipo = new TipoEstatuto(nome.trim(), descricao != null ? descricao.trim() : "");
        return tipoDAO.registarTipoEstatuto(tipo)
                ? new Resultado<>(tipo, true)
                : new Resultado<>(false, "Erro ao registar o tipo de estatuto.");
    }

    public Resultado<String> eliminarTipoEstatuto(int id) {
        if (tipoDAO.procurarPorId(id) == null) return new Resultado<>(false, "Tipo de estatuto não encontrado.");
        boolean emUso = estatutoDAO.listarTodos().stream()
                .anyMatch(e -> e.getTipoEstatuto().getId() == id);
        if (emUso) return new Resultado<>(false, "Não é possível eliminar: existem estudantes com este estatuto.");
        return tipoDAO.eliminarTipoEstatuto(id)
                ? new Resultado<>("Tipo eliminado.", true)
                : new Resultado<>(false, "Erro ao eliminar.");
    }

    public List<TipoEstatuto> listarTiposEstatuto() { return tipoDAO.listarTodos(); }

    // ── Estatutos de Estudantes ──────────────────────────────────────

    public Resultado<EstatutoEstudante> atribuirEstatuto(int numeroMec, int tipoId,
                                                          LocalDate inicio, LocalDate fim) {
        TipoEstatuto tipo = tipoDAO.procurarPorId(tipoId);
        if (tipo == null) return new Resultado<>(false, "Tipo de estatuto não encontrado.");

        EstudanteController estCtrl = new EstudanteController();
        Estudante est = estCtrl.procurarEstudantePorNumeroMec(numeroMec);
        if (est == null) return new Resultado<>(false, "Estudante não encontrado.");
        if (!est.isAtivo()) return new Resultado<>(false, "O estudante está inativo.");

        if (inicio == null) return new Resultado<>(false, "A data de início é obrigatória.");
        if (fim != null && fim.isBefore(inicio))
            return new Resultado<>(false, "A data de fim não pode ser anterior à data de início.");

        boolean jaTemAtivo = estatutoDAO.listarPorEstudante(numeroMec).stream()
                .anyMatch(e -> e.getTipoEstatuto().getId() == tipoId && e.isAtivo());
        if (jaTemAtivo)
            return new Resultado<>(false, "O estudante já possui este estatuto activo.");

        EstatutoEstudante estatuto = new EstatutoEstudante(est, tipo, inicio, fim);
        estatutoDAO.registarEstatuto(estatuto);
        return new Resultado<>(estatuto, true);
    }

    public Resultado<String> removerEstatuto(int estatutoId) {
        if (estatutoDAO.procurarPorId(estatutoId) == null)
            return new Resultado<>(false, "Estatuto não encontrado.");
        return estatutoDAO.eliminarEstatuto(estatutoId)
                ? new Resultado<>("Estatuto removido.", true)
                : new Resultado<>(false, "Erro ao remover estatuto.");
    }

    public List<EstatutoEstudante> listarEstatutosPorEstudante(int numeroMec) {
        return estatutoDAO.listarPorEstudante(numeroMec);
    }

    public List<EstatutoEstudante> listarTodosEstatutos() { return estatutoDAO.listarTodos(); }

    public boolean estudantePossuiEstatuto(int numeroMec, String nomeEstatuto) {
        return estatutoDAO.listarPorEstudante(numeroMec).stream()
                .anyMatch(e -> e.isAtivo() && e.getTipoEstatuto().getNome().equalsIgnoreCase(nomeEstatuto));
    }
}
