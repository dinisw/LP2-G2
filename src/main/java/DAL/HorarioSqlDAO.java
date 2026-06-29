package DAL;

import DAL.DB.DatabaseConnection;
import model.*;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

public class HorarioSqlDAO implements IHorarioDAO {

    private final DatabaseConnection db;

    public HorarioSqlDAO() {
        this.db = new DatabaseConnection();
    }

    private Horario mapHorario(java.sql.ResultSet rs) throws java.sql.SQLException {
        int id          = rs.getInt("id");
        int ucId        = rs.getInt("ucId");
        int anoLetivoId = rs.getInt("anoLetivoId");
        DiaSemana dia   = DiaSemana.fromString(rs.getString("diaSemana"));
        LocalTime inicio = LocalTime.parse(rs.getString("horaInicio"));
        LocalTime fim    = LocalTime.parse(rs.getString("horaFim"));
        String sala      = rs.getString("sala");

        UnidadeCurricular uc = DAOFactory.getUnidadeCurricularDAO().procurarPorId(ucId);
        if (uc == null) return null;

        Horario h = new Horario(uc, anoLetivoId, dia, inicio, fim, sala);
        h.setId(id);
        return h;
    }

    @Override
    public boolean registarHorario(Horario horario) {
        String sql = "INSERT INTO Horario (ucId, anoLetivoId, diaSemana, horaInicio, horaFim, sala) VALUES (?, ?, ?, ?, ?, ?)";
        int id = db.create(sql,
                horario.getUnidadeCurricular().getId(),
                horario.getAnoLetivoId(),
                horario.getDiaSemana().name(),
                horario.getHoraInicio().toString(),
                horario.getHoraFim().toString(),
                horario.getSala());
        if (id > 0) { horario.setId(id); return true; }
        return false;
    }

    @Override
    public boolean eliminarHorario(int id) {
        return db.execute("DELETE FROM Horario WHERE id=?", id) > 0;
    }

    @Override
    public Horario procurarPorId(int id) {
        return db.select("SELECT * FROM Horario WHERE id=?",
                rs -> { try { return mapHorario(rs); } catch (Exception e) { return null; } }, id)
                .stream().filter(h -> h != null).findFirst().orElse(null);
    }

    @Override
    public List<Horario> listarPorUC(int ucId) {
        return db.select("SELECT * FROM Horario WHERE ucId=?",
                rs -> { try { return mapHorario(rs); } catch (Exception e) { return null; } }, ucId)
                .stream().filter(h -> h != null).collect(Collectors.toList());
    }

    @Override
    public List<Horario> listarPorAnoLetivo(int anoLetivoId) {
        return db.select("SELECT * FROM Horario WHERE anoLetivoId=?",
                rs -> { try { return mapHorario(rs); } catch (Exception e) { return null; } }, anoLetivoId)
                .stream().filter(h -> h != null).collect(Collectors.toList());
    }

    @Override
    public List<Horario> listarTodos() {
        return db.select("SELECT * FROM Horario ORDER BY anoLetivoId, diaSemana, horaInicio",
                rs -> { try { return mapHorario(rs); } catch (Exception e) { return null; } }, (Object[]) null)
                .stream().filter(h -> h != null).collect(Collectors.toList());
    }
}
