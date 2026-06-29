package DAL;

import DAL.DB.DatabaseConnection;
import model.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class EstatutoEstudanteSqlDAO implements IEstatutoEstudanteDAO {

    private final DatabaseConnection db;

    public EstatutoEstudanteSqlDAO() {
        this.db = new DatabaseConnection();
    }

    private EstatutoEstudante mapEstatuto(java.sql.ResultSet rs) throws java.sql.SQLException {
        int id            = rs.getInt("id");
        int numeroMec     = rs.getInt("numeroMec");
        int tipoId        = rs.getInt("tipoEstatutoId");
        LocalDate inicio  = rs.getDate("dataInicio").toLocalDate();
        java.sql.Date fimSql = rs.getDate("dataFim");
        LocalDate fim     = fimSql != null ? fimSql.toLocalDate() : null;

        Estudante est      = DAOFactory.getEstudanteDAO().lerEstudante(numeroMec);
        TipoEstatuto tipo  = DAOFactory.getTipoEstatutoDAO().procurarPorId(tipoId);
        if (est == null || tipo == null) return null;

        EstatutoEstudante e = new EstatutoEstudante(est, tipo, inicio, fim);
        e.setId(id);
        return e;
    }

    @Override
    public boolean registarEstatuto(EstatutoEstudante estatuto) {
        String sql = "INSERT INTO EstatutoEstudante (numeroMec, tipoEstatutoId, dataInicio, dataFim) VALUES (?, ?, ?, ?)";
        int id = db.create(sql,
                estatuto.getEstudante().getNumeroMec(),
                estatuto.getTipoEstatuto().getId(),
                java.sql.Date.valueOf(estatuto.getDataInicio()),
                estatuto.getDataFim() != null ? java.sql.Date.valueOf(estatuto.getDataFim()) : null);
        if (id > 0) { estatuto.setId(id); return true; }
        return false;
    }

    @Override
    public boolean eliminarEstatuto(int id) {
        return db.execute("DELETE FROM EstatutoEstudante WHERE id=?", id) > 0;
    }

    @Override
    public EstatutoEstudante procurarPorId(int id) {
        return db.select("SELECT * FROM EstatutoEstudante WHERE id=?",
                rs -> { try { return mapEstatuto(rs); } catch (Exception e) { return null; } }, id)
                .stream().filter(e -> e != null).findFirst().orElse(null);
    }

    @Override
    public List<EstatutoEstudante> listarPorEstudante(int numeroMec) {
        return db.select("SELECT * FROM EstatutoEstudante WHERE numeroMec=? ORDER BY dataInicio DESC",
                rs -> { try { return mapEstatuto(rs); } catch (Exception e) { return null; } }, numeroMec)
                .stream().filter(e -> e != null).collect(Collectors.toList());
    }

    @Override
    public List<EstatutoEstudante> listarTodos() {
        return db.select("SELECT * FROM EstatutoEstudante ORDER BY dataInicio DESC",
                rs -> { try { return mapEstatuto(rs); } catch (Exception e) { return null; } }, (Object[]) null)
                .stream().filter(e -> e != null).collect(Collectors.toList());
    }
}
