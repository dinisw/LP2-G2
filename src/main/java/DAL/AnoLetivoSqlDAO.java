package DAL;

import DAL.DB.DatabaseConnection;
import DAL.DB.RowMapper;
import model.*;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class AnoLetivoSqlDAO implements IAnoLetivoDAO {

    private final DatabaseConnection db;

    private final RowMapper<AnoLetivo> anoLetivoMapper = rs -> {
        java.sql.Date dataFimSql = rs.getDate("dataFim");
        return new AnoLetivo(
                rs.getInt("id"),
                rs.getInt("anoCalendario"),
                rs.getDate("dataInicio").toLocalDate(),
                dataFimSql != null ? dataFimSql.toLocalDate() : null,
                rs.getString("estado")
        );
    };

    public AnoLetivoSqlDAO() {
        this.db = new DatabaseConnection();
    }

    @Override
    public boolean tabelasExistem() {
        return db.tabelaExiste("AnoLetivo");
    }

    // ── AnoLetivo principal ────────────────────────────────────

    @Override
    public boolean registarAnoLetivo(AnoLetivo al) {
        String sql = "INSERT INTO AnoLetivo (anoCalendario, dataInicio, dataFim, estado) VALUES (?, ?, ?, ?)";
        int rows = db.execute(sql,
                al.getAnoCalendario(),
                Date.valueOf(al.getDataInicio()),
                al.getDataFim() != null ? Date.valueOf(al.getDataFim()) : null,
                al.getEstado());
        return rows > 0;
    }

    @Override
    public AnoLetivo obterAnoAtual() {
        ArrayList<AnoLetivo> lista = db.select(
                "SELECT TOP 1 * FROM AnoLetivo WHERE estado='ATIVO' ORDER BY anoCalendario DESC",
                anoLetivoMapper, (Object[]) null);
        return lista.isEmpty() ? null : lista.get(0);
    }

    @Override
    public List<AnoLetivo> listarTodos() {
        return db.select("SELECT * FROM AnoLetivo ORDER BY anoCalendario DESC", anoLetivoMapper, (Object[]) null);
    }

    @Override
    public AnoLetivo obterPorAnoCalendario(int anoCalendario) {
        ArrayList<AnoLetivo> lista = db.select(
                "SELECT * FROM AnoLetivo WHERE anoCalendario=?", anoLetivoMapper, anoCalendario);
        return lista.isEmpty() ? null : lista.get(0);
    }

    @Override
    public boolean atualizarAnoLetivo(AnoLetivo al) {
        String sql = "UPDATE AnoLetivo SET dataFim=?, estado=? WHERE id=?";
        int rows = db.execute(sql,
                al.getDataFim() != null ? Date.valueOf(al.getDataFim()) : null,
                al.getEstado(),
                al.getId());
        return rows > 0;
    }

    // ── Snapshot: guardar ──────────────────────────────────────

    @Override
    public int salvarCursoSnapshot(AnoLetivoCursoSnapshot s) {
        String sql = "INSERT INTO AnoLetivoCurso (anoLetivoId, cursoId, nomeSnapshot, estadoCurso) VALUES (?, ?, ?, ?)";
        int id = db.create(sql, s.getAnoLetivoId(), s.getCursoId(), s.getNomeSnapshot(), s.getEstadoCurso());
        if (id > 0) s.setId(id);
        return id;
    }

    @Override
    public void salvarUCSnapshot(AnoLetivoUCSnapshot s) {
        String sql = "INSERT INTO AnoLetivoUC (anoLetivoCursoId, nomeUC, anoCurricular, docenteNome, docenteSigla, momentos) VALUES (?, ?, ?, ?, ?, ?)";
        int id = db.create(sql, s.getAnoLetivoCursoId(), s.getNomeUC(), s.getAnoCurricular(),
                s.getDocenteNome(), s.getDocenteSigla(), s.getMomentos());
        if (id > 0) s.setId(id);
    }

    @Override
    public int salvarEstudanteSnapshot(AnoLetivoEstudanteSnapshot s) {
        String sql = "INSERT INTO AnoLetivoEstudante (anoLetivoCursoId, numeroMec, nomeSnapshot, anoCurricularInicio, anoCurricularFim, propinaTotal, propinaPaga, resultado) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        int id = db.create(sql,
                s.getAnoLetivoCursoId(), s.getNumeroMec(), s.getNomeSnapshot(),
                s.getAnoCurricularInicio(), s.getAnoCurricularFim(),
                s.getPropinaTotal(), s.getPropinaPaga(), s.getResultado());
        if (id > 0) s.setId(id);
        return id;
    }

    @Override
    public void salvarNotaSnapshot(AnoLetivoNotaSnapshot s) {
        String sql = "INSERT INTO AnoLetivoNota (anoLetivoEstudanteId, nomeUC, momento, nota) VALUES (?, ?, ?, ?)";
        int id = db.create(sql, s.getAnoLetivoEstudanteId(), s.getNomeUC(), s.getMomento(), s.getNota());
        if (id > 0) s.setId(id);
    }

    // ── Snapshot: ler ──────────────────────────────────────────

    @Override
    public List<AnoLetivoCursoSnapshot> obterCursosSnapshot(int anoLetivoId) {
        return db.select(
                "SELECT * FROM AnoLetivoCurso WHERE anoLetivoId=? ORDER BY nomeSnapshot",
                rs -> {
                    int cid = rs.getInt("cursoId");
                    return new AnoLetivoCursoSnapshot(
                            rs.getInt("id"), anoLetivoId,
                            rs.wasNull() ? null : cid,
                            rs.getString("nomeSnapshot"),
                            rs.getString("estadoCurso"));
                }, anoLetivoId);
    }

    @Override
    public List<AnoLetivoUCSnapshot> obterUCsSnapshot(int anoLetivoCursoId) {
        return db.select(
                "SELECT * FROM AnoLetivoUC WHERE anoLetivoCursoId=? ORDER BY anoCurricular, nomeUC",
                rs -> new AnoLetivoUCSnapshot(
                        rs.getInt("id"), anoLetivoCursoId,
                        rs.getString("nomeUC"), rs.getInt("anoCurricular"),
                        rs.getString("docenteNome"), rs.getString("docenteSigla"),
                        rs.getString("momentos")),
                anoLetivoCursoId);
    }

    @Override
    public List<AnoLetivoEstudanteSnapshot> obterEstudantesSnapshot(int anoLetivoCursoId) {
        return db.select(
                "SELECT * FROM AnoLetivoEstudante WHERE anoLetivoCursoId=? ORDER BY nomeSnapshot",
                rs -> new AnoLetivoEstudanteSnapshot(
                        rs.getInt("id"), anoLetivoCursoId,
                        rs.getInt("numeroMec"), rs.getString("nomeSnapshot"),
                        rs.getInt("anoCurricularInicio"), rs.getInt("anoCurricularFim"),
                        rs.getBigDecimal("propinaTotal"), rs.getBigDecimal("propinaPaga"),
                        rs.getString("resultado")),
                anoLetivoCursoId);
    }

    @Override
    public List<AnoLetivoNotaSnapshot> obterNotasSnapshot(int anoLetivoEstudanteId) {
        return db.select(
                "SELECT * FROM AnoLetivoNota WHERE anoLetivoEstudanteId=? ORDER BY nomeUC, momento",
                rs -> {
                    double notaRaw = rs.getDouble("nota");
                    Double nota = rs.wasNull() ? null : notaRaw;
                    return new AnoLetivoNotaSnapshot(
                            rs.getInt("id"), anoLetivoEstudanteId,
                            rs.getString("nomeUC"), rs.getString("momento"), nota);
                },
                anoLetivoEstudanteId);
    }
}
