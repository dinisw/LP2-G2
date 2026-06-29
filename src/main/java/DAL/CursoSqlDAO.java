package DAL;

import DAL.DB.DatabaseConnection;
import DAL.DB.RowMapper;
import model.Curso;
import model.Departamento;
import model.Resultado;
import model.UnidadeCurricular;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CursoSqlDAO implements ICursoDAO {

    private final DatabaseConnection db;

    public CursoSqlDAO() {
        this.db = new DatabaseConnection();
    }


    // Mapper simples: lê apenas as colunas da query principal, sem queries aninhadas.
    // O enriquecimento com anos e UCs é feito em enrichCurso(), após o ResultSet ser fechado.
    private RowMapper<int[]> idMapper() {
        return rs -> new int[]{rs.getInt("id")};
    }

    private RowMapper<Curso> basicCursoMapper(java.util.Map<Curso, Integer> idMap) {
        return rs -> {
            Departamento dep = new Departamento(rs.getString("depNome"), rs.getString("depSigla"));
            Curso curso = new Curso(rs.getString("nome"), rs.getInt("duracao"), dep);
            curso.setPrecoAnual(rs.getBigDecimal("precoAnual"));
            idMap.put(curso, rs.getInt("id"));
            return curso;
        };
    }

    private void enrichCurso(Curso curso, int cursoId) {
        List<Integer> anos = db.select("SELECT ano FROM CursoAnoIniciado WHERE curso=?",
                r -> r.getInt("ano"), cursoId);
        curso.setAnosIniciados(anos);

        IUnidadeCurricularDAO ucDAO = DAOFactory.getUnidadeCurricularDAO();
        List<Integer> ucIds = db.select("SELECT UcId FROM CursoUnidadeCurricular WHERE cursoId=?",
                r -> r.getInt("UcId"), cursoId);
        for (int ucId : ucIds) {
            UnidadeCurricular uc = ucDAO.procurarPorId(ucId);
            if (uc != null) curso.adicionarUnidadeCurricular(uc);
        }
    }

    private static final String SELECT_CURSO =
            "SELECT c.id, c.nome, c.duracao, c.precoAnual, " +
            "d.nome AS depNome, d.sigla AS depSigla " +
            "FROM Curso c " +
            "LEFT JOIN Departamento d ON c.departamentoId = d.id";


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


    @Override
    public Resultado<Curso> registarCurso(Curso curso) {
        ArrayList<Integer> existe = db.select(
                "SELECT COUNT(*) AS total FROM Curso WHERE nome=?",
                rs -> rs.getInt("total"), curso.getNome());
        if (!existe.isEmpty() && existe.get(0) > 0)
            return new Resultado<>(false, "Já existe um curso com esse nome.");

        int depId = resolverDepartamentoId(curso.getDepartamento());
        if (depId <= 0) return new Resultado<>(false, "Departamento não encontrado na base de dados.");

        final int[] idGerado = {0};
        boolean ok = db.runTransaction(conn -> {
            // 1. INSERT Curso
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO Curso (nome, duracao, departamentoId, precoAnual) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, curso.getNome());
                ps.setInt(2, curso.getDuracao());
                ps.setInt(3, depId);
                ps.setBigDecimal(4, curso.getPrecoAnual());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) idGerado[0] = keys.getInt(1);
                }
            }
            if (idGerado[0] <= 0) throw new java.sql.SQLException("ID do curso não foi gerado.");

            // 2. INSERT CursoAnoIniciado
            if (curso.getAnosIniciados() != null) {
                for (int ano : curso.getAnosIniciados()) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO CursoAnoIniciado (curso, ano) VALUES (?, ?)")) {
                        ps.setInt(1, idGerado[0]);
                        ps.setInt(2, ano);
                        ps.executeUpdate();
                    }
                }
            }

            // 3. INSERT CursoUnidadeCurricular
            if (curso.getUnidadeCurriculars() != null) {
                for (UnidadeCurricular uc : curso.getUnidadeCurriculars()) {
                    if (uc.getId() > 0) {
                        try (PreparedStatement ps = conn.prepareStatement(
                                "INSERT INTO CursoUnidadeCurricular (cursoId, UcId) VALUES (?, ?)")) {
                            ps.setInt(1, idGerado[0]);
                            ps.setInt(2, uc.getId());
                            ps.executeUpdate();
                        }
                    }
                }
            }
        });

        return ok ? new Resultado<>(curso, true) : new Resultado<>(false, "Erro ao registar curso (transação revertida).");
    }

    @Override
    public List<Curso> getCursos() {
        java.util.Map<Curso, Integer> idMap = new java.util.LinkedHashMap<>();
        // Fase 1: carregar info básica (ResultSet fechado ao sair do select)
        List<Curso> cursos = db.select(SELECT_CURSO, basicCursoMapper(idMap), (Object[]) null);
        // Fase 2: enriquecer com anos e UCs (sem queries aninhadas)
        for (Curso curso : cursos) {
            Integer id = idMap.get(curso);
            if (id != null) enrichCurso(curso, id);
        }
        return cursos;
    }

    @Override
    public Curso procurarPorNome(String nome) {
        java.util.Map<Curso, Integer> idMap = new java.util.LinkedHashMap<>();
        ArrayList<Curso> lista = db.select(SELECT_CURSO + " WHERE c.nome=?", basicCursoMapper(idMap), nome);
        if (lista.isEmpty()) return null;
        Curso curso = lista.get(0);
        Integer id = idMap.get(curso);
        if (id != null) enrichCurso(curso, id);
        return curso;
    }

    @Override
    public Resultado<Curso> atualizarCurso(String nomeAntigo, Curso cursoNovo) {
        int id = obterIdPorNome(nomeAntigo);
        if (id <= 0) return new Resultado<>(false, "Curso não encontrado.");

        int depId = resolverDepartamentoId(cursoNovo.getDepartamento());
        if (depId <= 0) return new Resultado<>(false, "Departamento não encontrado na base de dados.");

        final int cursoId = id;
        boolean ok = db.runTransaction(conn -> {
            // 1. UPDATE Curso
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE Curso SET nome=?, duracao=?, departamentoId=?, precoAnual=? WHERE id=?")) {
                ps.setString(1, cursoNovo.getNome());
                ps.setInt(2, cursoNovo.getDuracao());
                ps.setInt(3, depId);
                ps.setBigDecimal(4, cursoNovo.getPrecoAnual());
                ps.setInt(5, cursoId);
                int rows = ps.executeUpdate();
                if (rows == 0) throw new java.sql.SQLException("Curso não encontrado para atualizar.");
            }

            // 2. Substituir CursoAnoIniciado
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM CursoAnoIniciado WHERE curso=?")) {
                ps.setInt(1, cursoId); ps.executeUpdate();
            }
            if (cursoNovo.getAnosIniciados() != null) {
                for (int ano : cursoNovo.getAnosIniciados()) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO CursoAnoIniciado (curso, ano) VALUES (?, ?)")) {
                        ps.setInt(1, cursoId); ps.setInt(2, ano); ps.executeUpdate();
                    }
                }
            }

            // 3. Substituir CursoUnidadeCurricular
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM CursoUnidadeCurricular WHERE cursoId=?")) {
                ps.setInt(1, cursoId); ps.executeUpdate();
            }
            if (cursoNovo.getUnidadeCurriculars() != null) {
                for (UnidadeCurricular uc : cursoNovo.getUnidadeCurriculars()) {
                    if (uc.getId() > 0) {
                        try (PreparedStatement ps = conn.prepareStatement(
                                "INSERT INTO CursoUnidadeCurricular (cursoId, UcId) VALUES (?, ?)")) {
                            ps.setInt(1, cursoId); ps.setInt(2, uc.getId()); ps.executeUpdate();
                        }
                    }
                }
            }
        });

        return ok ? new Resultado<>(cursoNovo, true) : new Resultado<>(false, "Erro ao atualizar curso (transação revertida).");
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
