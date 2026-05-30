package DAL;

import DAL.DB.DatabaseConnection;
import DAL.DB.RowMapper;
import model.Curso;
import model.Departamento;
import model.Resultado;
import model.UnidadeCurricular;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CursoSqlDAO implements ICursoDAO {

    private final DatabaseConnection db;

    public CursoSqlDAO() {
        this.db = new DatabaseConnection();
    }

    // Resolve departamento e UCs via outros DAOs para construir o Curso completo
    private RowMapper<Curso> cursoMapper() {
        IDepartamentoDAO depDAO = DAOFactory.getDepartamentoDAO();
        IUnidadeCurricularDAO ucDAO = DAOFactory.getUnidadeCurricularDAO();
        return rs -> {
            String siglaDep = rs.getString("sigla_dep");
            Departamento dep = depDAO.procurarPorSigla(siglaDep);
            Curso curso = new Curso(rs.getString("nome"), rs.getInt("duracao"), dep);
            curso.setPrecoAnual(rs.getDouble("preco_anual"));

            String anosRaw = rs.getString("anos_iniciados");
            if (anosRaw != null && !anosRaw.isEmpty() && !anosRaw.equals("Nenhum Curso Iniciado")) {
                List<Integer> anos = new ArrayList<>();
                for (String a : anosRaw.split(",")) {
                    try { anos.add(Integer.parseInt(a.trim())); } catch (NumberFormatException ignored) {}
                }
                curso.setAnosIniciados(anos);
            }

            String ucsRaw = rs.getString("ucs");
            if (ucsRaw != null && !ucsRaw.isEmpty()) {
                for (String nomeUc : ucsRaw.split(",")) {
                    UnidadeCurricular uc = ucDAO.procurarPorNome(nomeUc.trim());
                    if (uc != null) curso.adicionarUnidadeCurricular(uc);
                }
            }
            return curso;
        };
    }

    private String serializarAnosIniciados(Curso curso) {
        if (curso.getAnosIniciados() == null || curso.getAnosIniciados().isEmpty()) return "Nenhum Curso Iniciado";
        List<String> anos = new ArrayList<>();
        for (int a : curso.getAnosIniciados()) anos.add(String.valueOf(a));
        return String.join(",", anos);
    }

    private String serializarUCs(Curso curso) {
        if (curso.getUnidadeCurriculars() == null || curso.getUnidadeCurriculars().isEmpty()) return "";
        List<String> nomes = new ArrayList<>();
        for (UnidadeCurricular uc : curso.getUnidadeCurriculars()) nomes.add(uc.getNome());
        return String.join(",", nomes);
    }

    @Override
    public Resultado<Curso> registarCurso(Curso curso) {
        if (procurarPorNome(curso.getNome()) != null) return new Resultado<>(false, "Já existe um curso com esse nome.");
        String siglaDep = curso.getDepartamento() != null ? curso.getDepartamento().getSigla() : "N/A";
        String sql = "INSERT INTO Cursos (nome, duracao, sigla_dep, preco_anual, anos_iniciados, ucs) VALUES (?, ?, ?, ?, ?, ?)";
        int rows = db.execute(sql,
                curso.getNome(), curso.getDuracao(), siglaDep,
                curso.getPrecoAnual(), serializarAnosIniciados(curso), serializarUCs(curso));
        if (rows > 0) return new Resultado<>(curso, true);
        return new Resultado<>(false, "Erro ao registar curso.");
    }

    @Override
    public List<Curso> getCursos() {
        return db.select("SELECT * FROM Cursos", cursoMapper(), (Object[]) null);
    }

    @Override
    public Curso procurarPorNome(String nome) {
        ArrayList<Curso> lista = db.select("SELECT * FROM Cursos WHERE nome=?", cursoMapper(), nome);
        return lista.isEmpty() ? null : lista.get(0);
    }

    @Override
    public Resultado<Curso> atualizarCurso(String nomeAntigo, Curso cursoNovo) {
        String siglaDep = cursoNovo.getDepartamento() != null ? cursoNovo.getDepartamento().getSigla() : "N/A";
        String sql = "UPDATE Cursos SET nome=?, duracao=?, sigla_dep=?, preco_anual=?, anos_iniciados=?, ucs=? WHERE nome=?";
        int rows = db.execute(sql,
                cursoNovo.getNome(), cursoNovo.getDuracao(), siglaDep,
                cursoNovo.getPrecoAnual(), serializarAnosIniciados(cursoNovo), serializarUCs(cursoNovo),
                nomeAntigo);
        if (rows > 0) return new Resultado<>(cursoNovo, true);
        return new Resultado<>(false, "Curso não encontrado.");
    }

    @Override
    public Resultado<Curso> eliminarCurso(String nome) {
        int rows = db.execute("DELETE FROM Cursos WHERE nome=?", nome);
        if (rows > 0) return new Resultado<>(null, true);
        return new Resultado<>(false, "Curso não encontrado.");
    }

    @Override
    public Resultado<Curso> registarArranqueAno(String nomeCurso, Curso cursoAtualizado) {
        return atualizarCurso(nomeCurso, cursoAtualizado);
    }

    @Override
    public boolean existeCursoComDepartamento(String siglaDepartamento) {
        ArrayList<Integer> lista = db.select(
                "SELECT COUNT(*) AS total FROM Cursos WHERE sigla_dep=?",
                rs -> rs.getInt("total"),
                siglaDepartamento
        );
        return !lista.isEmpty() && lista.get(0) > 0;
    }
}
