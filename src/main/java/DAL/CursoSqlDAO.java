package DAL;

import DAL.DB.DatabaseConnection;
import DAL.DB.RowMapper;
import model.Curso;
import model.Departamento;
import model.Resultado;
import model.UnidadeCurricular;

import java.util.ArrayList;
import java.util.List;

public class CursoSqlDAO implements ICursoDAO {

    private final DatabaseConnection db;

    public CursoSqlDAO() {
        this.db = new DatabaseConnection();
    }

    // ── Mapper ───────────────────────────────────────────────────────────────

    private RowMapper<Curso> cursoMapper() {
        return rs -> {
            // Departamento via JOIN
            Departamento dep = new Departamento(
                    rs.getString("depNome"),
                    rs.getString("depSigla")
            );
            Curso curso = new Curso(rs.getString("nome"), rs.getInt("duracao"), dep);
            curso.setPrecoAnual(rs.getDouble("precoAnual"));

            int cursoId = rs.getInt("id");

            // Anos iniciados via CursoAnoIniciado
            List<Integer> anos = db.select(
                    "SELECT ano FROM CursoAnoIniciado WHERE curso=?",
                    r -> r.getInt("ano"), cursoId);
            curso.setAnosIniciados(anos);

            // UCs via CursoUnidadeCurricular
            IUnidadeCurricularDAO ucDAO = DAOFactory.getUnidadeCurricularDAO();
            List<Integer> ucIds = db.select(
                    "SELECT UcId FROM CursoUnidadeCurricular WHERE cursoId=?",
                    r -> r.getInt("UcId"), cursoId);
            for (int ucId : ucIds) {
                UnidadeCurricular uc = ucDAO.procurarPorId(ucId);
                if (uc != null) curso.adicionarUnidadeCurricular(uc);
            }

            return curso;
        };
    }

    private static final String SELECT_CURSO =
            "SELECT c.id, c.nome, c.duracao, c.precoAnual, " +
            "d.nome AS depNome, d.sigla AS depSigla " +
            "FROM Curso c " +
            "LEFT JOIN Departamento d ON c.departamentoId = d.id";

    // ── Helpers ──────────────────────────────────────────────────────────────

    private int resolverDepartamentoId(Departamento dep) {
        if (dep == null) return 0;
        ArrayList<Integer> ids = db.select(
                "SELECT id FROM Departamento WHERE sigla=?",
                rs -> rs.getInt("id"), dep.getSigla());
        return ids.isEmpty() ? 0 : ids.get(0);
    }

    private int obterIdPorNome(String nome) {
        ArrayList<Integer> ids = db.select(
                "SELECT id FROM Curso WHERE nome=?",
                rs -> rs.getInt("id"), nome);
        return ids.isEmpty() ? 0 : ids.get(0);
    }

    private void guardarAnosIniciados(int cursoId, List<Integer> anos) {
        db.execute("DELETE FROM CursoAnoIniciado WHERE curso=?", cursoId);
        if (anos == null) return;
        for (int ano : anos) {
            db.execute("INSERT INTO CursoAnoIniciado (curso, ano) VALUES (?, ?)", cursoId, ano);
        }
    }

    private void guardarUCs(int cursoId, List<UnidadeCurricular> ucs) {
        db.execute("DELETE FROM CursoUnidadeCurricular WHERE cursoId=?", cursoId);
        if (ucs == null) return;
        for (UnidadeCurricular uc : ucs) {
            if (uc.getId() > 0) {
                db.execute("INSERT INTO CursoUnidadeCurricular (cursoId, UcId) VALUES (?, ?)",
                        cursoId, uc.getId());
            }
        }
    }

    // ── CRUD ─────────────────────────────────────────────────────────────────

    @Override
    public Resultado<Curso> registarCurso(Curso curso) {
        if (procurarPorNome(curso.getNome()) != null)
            return new Resultado<>(false, "Já existe um curso com esse nome.");

        int depId = resolverDepartamentoId(curso.getDepartamento());
        if (depId <= 0) return new Resultado<>(false, "Departamento não encontrado na base de dados.");

        String sql = "INSERT INTO Curso (nome, duracao, departamentoId, precoAnual) VALUES (?, ?, ?, ?)";
        int id = db.create(sql,
                curso.getNome(), curso.getDuracao(), depId, curso.getPrecoAnual());

        if (id > 0) {
            guardarAnosIniciados(id, curso.getAnosIniciados());
            guardarUCs(id, curso.getUnidadeCurriculars());
            return new Resultado<>(curso, true);
        }
        return new Resultado<>(false, "Erro ao registar curso.");
    }

    @Override
    public List<Curso> getCursos() {
        return db.select(SELECT_CURSO, cursoMapper(), (Object[]) null);
    }

    @Override
    public Curso procurarPorNome(String nome) {
        ArrayList<Curso> lista = db.select(
                SELECT_CURSO + " WHERE c.nome=?", cursoMapper(), nome);
        return lista.isEmpty() ? null : lista.get(0);
    }

    @Override
    public Resultado<Curso> atualizarCurso(String nomeAntigo, Curso cursoNovo) {
        int id = obterIdPorNome(nomeAntigo);
        if (id <= 0) return new Resultado<>(false, "Curso não encontrado.");

        int depId = resolverDepartamentoId(cursoNovo.getDepartamento());
        if (depId <= 0) return new Resultado<>(false, "Departamento não encontrado na base de dados.");

        String sql = "UPDATE Curso SET nome=?, duracao=?, departamentoId=?, precoAnual=? WHERE id=?";
        int rows = db.execute(sql,
                cursoNovo.getNome(), cursoNovo.getDuracao(), depId, cursoNovo.getPrecoAnual(), id);

        if (rows > 0) {
            guardarAnosIniciados(id, cursoNovo.getAnosIniciados());
            guardarUCs(id, cursoNovo.getUnidadeCurriculars());
            return new Resultado<>(cursoNovo, true);
        }
        return new Resultado<>(false, "Erro ao atualizar curso.");
    }

    @Override
    public Resultado<Curso> eliminarCurso(String nome) {
        int id = obterIdPorNome(nome);
        if (id <= 0) return new Resultado<>(false, "Curso não encontrado.");

        guardarAnosIniciados(id, null);
        guardarUCs(id, null);
        int rows = db.execute("DELETE FROM Curso WHERE id=?", id);
        if (rows > 0) return new Resultado<>(null, true);
        return new Resultado<>(false, "Erro ao eliminar curso.");
    }

    @Override
    public Resultado<Curso> registarArranqueAno(String nomeCurso, Curso cursoAtualizado) {
        return atualizarCurso(nomeCurso, cursoAtualizado);
    }

    @Override
    public boolean existeCursoComDepartamento(String siglaDepartamento) {
        ArrayList<Integer> lista = db.select(
                "SELECT COUNT(*) AS total FROM Curso WHERE departamentoId = " +
                "(SELECT id FROM Departamento WHERE sigla=?)",
                rs -> rs.getInt("total"), siglaDepartamento);
        return !lista.isEmpty() && lista.get(0) > 0;
    }
}
