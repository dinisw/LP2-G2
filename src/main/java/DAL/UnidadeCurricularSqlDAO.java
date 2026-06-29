package DAL;

import DAL.DB.DatabaseConnection;
import DAL.DB.RowMapper;
import model.Docente;
import model.UnidadeCurricular;

import java.util.ArrayList;
import java.util.List;

public class UnidadeCurricularSqlDAO implements IUnidadeCurricularDAO {

    private final DatabaseConnection db;

    // JOIN para trazer dados do docente sem query extra por linha (elimina N+1)
    private static final String SELECT_UC =
            "SELECT uc.id, uc.nome, uc.anoCurricular, uc.semestre, uc.docenteId, " +
            "d.nome AS docNome, d.morada AS docMorada, d.nif AS docNif, " +
            "d.dataNascimento AS docDataNasc, d.email AS docEmail, d.hash AS docHash, " +
            "d.sigla AS docSigla, d.ativo AS docAtivo " +
            "FROM UnidadeCurricular uc " +
            "LEFT JOIN Docente d ON uc.docenteId = d.id";

    public UnidadeCurricularSqlDAO() {
        this.db = new DatabaseConnection();
    }

    // Mapper simples: lê apenas as colunas da query, sem obterMomentos() aninhado.
    // Os momentos são carregados em separado após o ResultSet fechar.
    private RowMapper<UnidadeCurricular> ucMapper() {
        return rs -> {
            Docente docente = null;
            String docNome = rs.getString("docNome");
            if (docNome != null) {
                java.sql.Date dataNasc = rs.getDate("docDataNasc");
                docente = new Docente(
                        docNome, rs.getString("docMorada"),
                        rs.getInt("docNif"),
                        dataNasc != null ? dataNasc.toLocalDate() : java.time.LocalDate.of(1970, 1, 1),
                        rs.getString("docEmail"), rs.getString("docHash"),
                        rs.getString("docSigla"), new ArrayList<>(), new ArrayList<>());
                docente.setAtivo(rs.getBoolean("docAtivo"));
            }
            return new UnidadeCurricular(
                    rs.getString("nome"),
                    rs.getInt("id"),
                    rs.getInt("anoCurricular"),
                    rs.getInt("semestre"),
                    docente,
                    new ArrayList<>()   // momentos carregados após o ResultSet fechar
            );
        };
    }

    private List<String> obterMomentos(int ucId) {
        return db.select(
                "SELECT momento FROM UnidadeCurricularMomento WHERE id=?",
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

    private int obterIdPorNome(String nome) {
        ArrayList<Integer> ids = db.select(
                "SELECT id FROM UnidadeCurricular WHERE nome=?",
                rs -> rs.getInt("id"), nome);
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
        // Coluna FK chama-se 'id' na tabela UnidadeCurricularMomento
        db.execute("DELETE FROM UnidadeCurricularMomento WHERE id=?", ucId);
        for (String momento : momentos) {
            if (momento != null && !momento.trim().isEmpty()) {
                db.execute("INSERT INTO UnidadeCurricularMomento (id, momento) VALUES (?, ?)", ucId, momento.trim());
            }
        }
    }

    @Override
    public List<UnidadeCurricular> getUnidadeCurriculars() {
        // Fase 1: carregar UCs sem momentos (ResultSet fechado ao sair do select)
        List<UnidadeCurricular> lista = db.select(SELECT_UC, ucMapper(), (Object[]) null);
        // Fase 2: carregar momentos (sem queries aninhadas)
        for (UnidadeCurricular uc : lista) {
            uc.setMomentosAvaliacao(obterMomentos(uc.getId()));
        }
        return lista;
    }

    @Override
    public UnidadeCurricular procurarPorNome(String nome) {
        ArrayList<UnidadeCurricular> lista = db.select(SELECT_UC + " WHERE uc.nome=?", ucMapper(), nome);
        if (lista.isEmpty()) return null;
        UnidadeCurricular uc = lista.get(0);
        uc.setMomentosAvaliacao(obterMomentos(uc.getId()));
        return uc;
    }

    @Override
    public UnidadeCurricular procurarPorId(int id) {
        ArrayList<UnidadeCurricular> lista = db.select(SELECT_UC + " WHERE uc.id=?", ucMapper(), id);
        if (lista.isEmpty()) return null;
        UnidadeCurricular uc = lista.get(0);
        uc.setMomentosAvaliacao(obterMomentos(uc.getId()));
        return uc;
    }

    @Override
    public boolean atualizarUC(String nomeAtual, UnidadeCurricular uc) {
        int id = obterIdPorNome(nomeAtual);
        if (id <= 0) return false;
        return atualizarUCPorId(id, uc);
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
        int id = obterIdPorNome(nome);
        if (id <= 0) return false;
        return eliminarUCPorId(id);
    }
}
