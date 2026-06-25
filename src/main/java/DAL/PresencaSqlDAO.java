package DAL;

import DAL.DB.DatabaseConnection;
import model.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class PresencaSqlDAO implements IPresencaDAO {

    private final DatabaseConnection db;

    public PresencaSqlDAO() {
        this.db = new DatabaseConnection();
    }

    private Presenca mapPresenca(java.sql.ResultSet rs) throws java.sql.SQLException {
        int id            = rs.getInt("id");
        int horarioId     = rs.getInt("horarioId");
        int numeroMec     = rs.getInt("numeroMec");
        LocalDate data    = rs.getDate("data").toLocalDate();
        boolean docente   = rs.getBoolean("presencaDocente");
        boolean estudante = rs.getBoolean("presencaEstudante");

        Horario h  = DAOFactory.getHorarioDAO().procurarPorId(horarioId);
        Estudante e = DAOFactory.getEstudanteDAO().lerEstudante(numeroMec);
        if (h == null || e == null) return null;

        Presenca p = new Presenca(e, h, data);
        p.setId(id);
        p.setPresencaDocente(docente);
        if (docente) p.setPresencaEstudante(estudante);
        return p;
    }

    @Override
    public boolean registarPresenca(Presenca presenca) {
        String sql = "INSERT INTO Presenca (horarioId, numeroMec, data, presencaDocente, presencaEstudante) VALUES (?, ?, ?, ?, ?)";
        int id = db.create(sql,
                presenca.getHorario().getId(),
                presenca.getEstudante().getNumeroMec(),
                java.sql.Date.valueOf(presenca.getData()),
                presenca.isPresencaDocente(),
                presenca.isPresencaEstudante());
        if (id > 0) { presenca.setId(id); return true; }
        return false;
    }

    @Override
    public boolean atualizarPresenca(Presenca presenca) {
        String sql = "UPDATE Presenca SET presencaDocente=?, presencaEstudante=? WHERE id=?";
        return db.execute(sql, presenca.isPresencaDocente(), presenca.isPresencaEstudante(), presenca.getId()) > 0;
    }

    @Override
    public Presenca procurarPorId(int id) {
        return db.select("SELECT * FROM Presenca WHERE id=?",
                rs -> { try { return mapPresenca(rs); } catch (Exception e) { return null; } }, id)
                .stream().filter(p -> p != null).findFirst().orElse(null);
    }

    @Override
    public List<Presenca> listarPorHorario(int horarioId) {
        return db.select("SELECT * FROM Presenca WHERE horarioId=?",
                rs -> { try { return mapPresenca(rs); } catch (Exception e) { return null; } }, horarioId)
                .stream().filter(p -> p != null).collect(Collectors.toList());
    }

    @Override
    public List<Presenca> listarPorEstudante(int numeroMec) {
        return db.select("SELECT * FROM Presenca WHERE numeroMec=?",
                rs -> { try { return mapPresenca(rs); } catch (Exception e) { return null; } }, numeroMec)
                .stream().filter(p -> p != null).collect(Collectors.toList());
    }

    @Override
    public List<Presenca> listarFaltasPorUC(int ucId) {
        String sql = "SELECT p.* FROM Presenca p " +
                     "JOIN Horario h ON p.horarioId = h.id " +
                     "WHERE h.ucId=? AND p.presencaDocente=1 AND p.presencaEstudante=0";
        return db.select(sql,
                rs -> { try { return mapPresenca(rs); } catch (Exception e) { return null; } }, ucId)
                .stream().filter(p -> p != null).collect(Collectors.toList());
    }

    @Override
    public List<Presenca> listarTodas() {
        return db.select("SELECT * FROM Presenca ORDER BY data",
                rs -> { try { return mapPresenca(rs); } catch (Exception e) { return null; } }, (Object[]) null)
                .stream().filter(p -> p != null).collect(Collectors.toList());
    }

    @Override
    public boolean eliminarPresenca(int id) {
        return db.execute("DELETE FROM Presenca WHERE id=?", id) > 0;
    }
}
