package DAL;

import DAL.DB.DatabaseConnection;
import DAL.DB.RowMapper;
import model.Docente;
import model.UnidadeCurricular;

import java.util.ArrayList;
import java.util.List;

public class UnidadeCurricularSqlDAO implements IUnidadeCurricularDAO {

    private final DatabaseConnection db;

    public UnidadeCurricularSqlDAO() {
        this.db = new DatabaseConnection();
    }

    private RowMapper<UnidadeCurricular> ucMapper() {
        return rs -> {
            int docenteId = rs.getInt("docenteId");
            // Resolve docente pelo id da BD (não pelo nif)
            ArrayList<Docente> listaDoc = db.select(
                    "SELECT * FROM Docente WHERE id=?",
                    rs2 -> {
                        Docente d = new Docente(
                                rs2.getString("nome"), rs2.getString("morada"),
                                rs2.getInt("nif"), rs2.getDate("dataNascimento").toLocalDate(),
                                rs2.getString("email"), rs2.getString("hash"),
                                rs2.getString("sigla"), new ArrayList<>(), new ArrayList<>());
                        d.setAtivo(rs2.getBoolean("ativo"));
                        return d;
                    }, docenteId);
            Docente docente = listaDoc.isEmpty() ? null : listaDoc.get(0);

            int ucId = rs.getInt("id");
            List<String> momentos = obterMomentos(ucId);

            return new UnidadeCurricular(
                    rs.getString("nome"),
                    ucId,
                    rs.getInt("anoCurricular"),
                    rs.getInt("semestre"),
                    docente,
                    momentos
            );
        };
    }

    private List<String> obterMomentos(int ucId) {
        return db.select(
                "SELECT momento FROM UnidadeCurricularMomento WHERE ucId=?",
                rs -> rs.getString("momento"),
                ucId
        );
    }

    private int resolverDocenteId(Docente docente) {
        if (docente == null) return 0;
        ArrayList<Integer> ids = db.select(
                "SELECT id FROM Docente WHERE nif=?",
                rs -> rs.getInt("id"),
                docente.getNif()
        );
        return ids.isEmpty() ? 0 : ids.get(0);
    }

    @Override
    public boolean registarUC(UnidadeCurricular uc) {
        int docenteId = resolverDocenteId(uc.getDocente());
        if (docenteId <= 0) {
            System.out.println("Erro ao registar UC: Docente não encontrado na base de dados.");
            return false;
        }
        String sql = "INSERT INTO UnidadeCurricular (nome, anoCurricular, semestre, docenteId, ects) VALUES (?, ?, ?, ?, ?)";
        int id = db.create(sql,
                uc.getNome(),
                uc.getAnoCurricular(),
                uc.getSemestre(),
                docenteId,
                uc.getEcts() > 0 ? uc.getEcts() : 6
        );
        if (id > 0) {
            uc.setId(id);
            guardarMomentos(id, uc.getMomentosAvaliacao());
            return true;
        }
        return false;
    }

    private void guardarMomentos(int ucId, List<String> momentos) {
        if (momentos == null) return;
        db.execute("DELETE FROM UnidadeCurricularMomento WHERE ucId=?", ucId);
        for (String momento : momentos) {
            if (momento != null && !momento.trim().isEmpty()) {
                db.execute("INSERT INTO UnidadeCurricularMomento (ucId, momento) VALUES (?, ?)", ucId, momento.trim());
            }
        }
    }

    @Override
    public List<UnidadeCurricular> getUnidadeCurriculars() {
        return db.select("SELECT * FROM UnidadeCurricular", ucMapper(), (Object[]) null);
    }

    @Override
    public UnidadeCurricular procurarPorNome(String nome) {
        ArrayList<UnidadeCurricular> lista = db.select(
                "SELECT * FROM UnidadeCurricular WHERE nome=?", ucMapper(), nome);
        return lista.isEmpty() ? null : lista.get(0);
    }

    @Override
    public UnidadeCurricular procurarPorId(int id) {
        ArrayList<UnidadeCurricular> lista = db.select(
                "SELECT * FROM UnidadeCurricular WHERE id=?", ucMapper(), id);
        return lista.isEmpty() ? null : lista.get(0);
    }

    @Override
    public boolean atualizarUC(String nomeAtual, UnidadeCurricular uc) {
        UnidadeCurricular existente = procurarPorNome(nomeAtual);
        if (existente == null) return false;
        return atualizarUCPorId(existente.getId(), uc);
    }

    @Override
    public boolean atualizarUCPorId(int id, UnidadeCurricular uc) {
        int docenteId = resolverDocenteId(uc.getDocente());
        if (docenteId <= 0) return false;
        String sql = "UPDATE UnidadeCurricular SET nome=?, anoCurricular=?, semestre=?, docenteId=?, ects=? WHERE id=?";
        int rows = db.execute(sql,
                uc.getNome(), uc.getAnoCurricular(), uc.getSemestre(),
                docenteId,
                uc.getEcts() > 0 ? uc.getEcts() : 6,
                id
        );
        if (rows > 0) {
            uc.setId(id);
            guardarMomentos(id, uc.getMomentosAvaliacao());
            return true;
        }
        return false;
    }

    @Override
    public boolean eliminarUCPorId(int id) {
        db.execute("DELETE FROM UnidadeCurricularMomento WHERE id=?", id);
        return db.execute("DELETE FROM UnidadeCurricular WHERE id=?", id) > 0;
    }

    @Override
    public boolean eliminarUC(String nome) {
        UnidadeCurricular uc = procurarPorNome(nome);
        if (uc == null) return false;
        return eliminarUCPorId(uc.getId());
    }
}
