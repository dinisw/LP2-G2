package DAL;

import DAL.DB.DatabaseConnection;
import model.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class JustificacaoFaltaSqlDAO implements IJustificacaoFaltaDAO {

    private final DatabaseConnection db;

    public JustificacaoFaltaSqlDAO() {
        this.db = new DatabaseConnection();
    }

    private JustificacaoFalta mapJustificacao(java.sql.ResultSet rs) throws java.sql.SQLException {
        int id                       = rs.getInt("id");
        int numeroMec                = rs.getInt("numeroMec");
        int presencaId               = rs.getInt("presencaId");
        TipoJustificacao tipo        = TipoJustificacao.fromString(rs.getString("tipo"));
        String descricao             = rs.getString("descricao");
        LocalDate dataSubmissao      = rs.getDate("dataSubmissao").toLocalDate();
        JustificacaoFalta.Estado est = JustificacaoFalta.Estado.valueOf(rs.getString("estado"));
        String obsGestor             = rs.getString("observacaoGestor");

        Estudante estudante = DAOFactory.getEstudanteDAO().lerEstudante(numeroMec);
        Presenca presenca   = DAOFactory.getPresencaDAO().procurarPorId(presencaId);
        if (estudante == null || presenca == null) return null;

        JustificacaoFalta j = new JustificacaoFalta(estudante, presenca, tipo, descricao);
        j.setId(id);
        j.setDataSubmissao(dataSubmissao);
        j.setEstado(est);
        j.setObservacaoGestor(obsGestor);
        return j;
    }

    @Override
    public boolean registarJustificacao(JustificacaoFalta justificacao) {
        String sql = "INSERT INTO JustificacaoFalta (numeroMec, presencaId, tipo, descricao, dataSubmissao, estado, observacaoGestor) VALUES (?, ?, ?, ?, ?, ?, ?)";
        int id = db.create(sql,
                justificacao.getEstudante().getNumeroMec(),
                justificacao.getPresenca().getId(),
                justificacao.getTipo().name(),
                justificacao.getDescricao(),
                java.sql.Date.valueOf(justificacao.getDataSubmissao()),
                justificacao.getEstado().name(),
                justificacao.getObservacaoGestor() != null ? justificacao.getObservacaoGestor() : "");
        if (id > 0) { justificacao.setId(id); return true; }
        return false;
    }

    @Override
    public boolean atualizarJustificacao(JustificacaoFalta justificacao) {
        String sql = "UPDATE JustificacaoFalta SET estado=?, observacaoGestor=? WHERE id=?";
        return db.execute(sql,
                justificacao.getEstado().name(),
                justificacao.getObservacaoGestor() != null ? justificacao.getObservacaoGestor() : "",
                justificacao.getId()) > 0;
    }

    @Override
    public JustificacaoFalta procurarPorId(int id) {
        return db.select("SELECT * FROM JustificacaoFalta WHERE id=?",
                rs -> { try { return mapJustificacao(rs); } catch (Exception e) { return null; } }, id)
                .stream().filter(j -> j != null).findFirst().orElse(null);
    }

    @Override
    public List<JustificacaoFalta> listarPorEstudante(int numeroMec) {
        return db.select("SELECT * FROM JustificacaoFalta WHERE numeroMec=? ORDER BY dataSubmissao DESC",
                rs -> { try { return mapJustificacao(rs); } catch (Exception e) { return null; } }, numeroMec)
                .stream().filter(j -> j != null).collect(Collectors.toList());
    }

    @Override
    public List<JustificacaoFalta> listarPendentes() {
        return db.select("SELECT * FROM JustificacaoFalta WHERE estado='PENDENTE' ORDER BY dataSubmissao",
                rs -> { try { return mapJustificacao(rs); } catch (Exception e) { return null; } }, (Object[]) null)
                .stream().filter(j -> j != null).collect(Collectors.toList());
    }

    @Override
    public List<JustificacaoFalta> listarTodas() {
        return db.select("SELECT * FROM JustificacaoFalta ORDER BY dataSubmissao DESC",
                rs -> { try { return mapJustificacao(rs); } catch (Exception e) { return null; } }, (Object[]) null)
                .stream().filter(j -> j != null).collect(Collectors.toList());
    }

    @Override
    public boolean eliminarJustificacao(int id) {
        return db.execute("DELETE FROM JustificacaoFalta WHERE id=?", id) > 0;
    }
}
