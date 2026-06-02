package DAL;

import DAL.DB.DatabaseConnection;
import DAL.DB.RowMapper;
import model.Docente;
import model.UnidadeCurricular;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UnidadeCurricularSqlDAO implements IUnidadeCurricularDAO {

    private final DatabaseConnection db;

    public UnidadeCurricularSqlDAO() {
        this.db = new DatabaseConnection();
    }

    // Resolve o docente pela sigla para construir o objeto UC completo
    private RowMapper<UnidadeCurricular> ucMapper() {
        IDocenteDAO docenteDAO = DAOFactory.getDocenteDAO();
        return rs -> {
            String siglaDocente = rs.getString("sigla_docente");
            Docente docente = docenteDAO.procurarPorSigla(siglaDocente);

            String momentosRaw = rs.getString("momentos_avaliacao");
            List<String> momentos = new ArrayList<>();
            if (momentosRaw != null && !momentosRaw.isEmpty()) {
                momentos = Arrays.asList(momentosRaw.split(","));
            }

            return new UnidadeCurricular(
                    rs.getString("nome"),
                    rs.getInt("id"),
                    rs.getInt("ano_curricular"),
                    rs.getInt("semestre"),
                    docente,
                    momentos
            );
        };
    }

    @Override
    public boolean registarUC(UnidadeCurricular uc) {
        String momentosStr = uc.getMomentosAvaliacao() != null ? String.join(",", uc.getMomentosAvaliacao()) : "";
        String siglaDoc = uc.getDocente() != null ? uc.getDocente().getSigla() : "N/A";
        String sql = "INSERT INTO UnidadesCurriculares (nome, ano_curricular, semestre, sigla_docente, momentos_avaliacao) VALUES (?, ?, ?, ?, ?)";
        int id = db.create(sql, uc.getNome(), uc.getAnoCurricular(), uc.getSemestre(), siglaDoc, momentosStr);
        if (id > 0) {
            uc.setId(id);
            return true;
        }
        return false;
    }

    @Override
    public List<UnidadeCurricular> getUnidadeCurriculars() {
        return db.select("SELECT * FROM UnidadesCurriculares", ucMapper(), (Object[]) null);
    }

    @Override
    public UnidadeCurricular procurarPorNome(String nome) {
        ArrayList<UnidadeCurricular> lista = db.select(
                "SELECT * FROM UnidadesCurriculares WHERE nome=?", ucMapper(), nome);
        return lista.isEmpty() ? null : lista.get(0);
    }

    @Override
    public UnidadeCurricular procurarPorId(int id) {
        ArrayList<UnidadeCurricular> lista = db.select(
                "SELECT * FROM UnidadesCurriculares WHERE id=?", ucMapper(), id);
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
        String momentosStr = uc.getMomentosAvaliacao() != null ? String.join(",", uc.getMomentosAvaliacao()) : "";
        String siglaDoc = uc.getDocente() != null ? uc.getDocente().getSigla() : "N/A";
        String sql = "UPDATE UnidadesCurriculares SET nome=?, ano_curricular=?, semestre=?, sigla_docente=?, momentos_avaliacao=? WHERE id=?";
        int rows = db.execute(sql, uc.getNome(), uc.getAnoCurricular(), uc.getSemestre(), siglaDoc, momentosStr, id);
        if (rows > 0) { uc.setId(id); return true; }
        return false;
    }

    @Override
    public boolean eliminarUCPorId(int id) {
        return db.execute("DELETE FROM UnidadesCurriculares WHERE id=?", id) > 0;
    }

    @Override
    public boolean eliminarUC(String nomeAtual) {
        return db.execute("DELETE FROM UnidadesCurriculares WHERE nome=?", nomeAtual) > 0;
    }
}
