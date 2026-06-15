package DAL;

import DAL.DB.DatabaseConnection;
import DAL.DB.RowMapper;
import model.Estudante;
import model.Resultado;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class EstudanteSqlDAO implements IEstudanteDAO {

    private final DatabaseConnection db;

    // JOIN com Curso para obter o nomeCurso a partir do cursoId
    private static final String SELECT_BASE =
            "SELECT e.*, c.nome AS nomeCurso " +
            "FROM Estudante e " +
            "LEFT JOIN Curso c ON e.cursoId = c.id";

    private final RowMapper<Estudante> estudanteMapper = rs -> new Estudante(
            rs.getString("nome"),
            rs.getString("morada"),
            rs.getInt("nif"),
            rs.getDate("dataNascimento").toLocalDate(),
            rs.getString("email"),
            rs.getInt("numeroMec"),
            rs.getString("hashSenha"),
            rs.getString("nomeCurso"),
            rs.getBoolean("ativo")
    );

    public EstudanteSqlDAO() {
        this.db = new DatabaseConnection();
    }

    @Override
    public Resultado<Estudante> registarEstudante(Estudante estudante) {
        // Resolve cursoId a partir do nome do curso
        String sqlCurso = "SELECT id FROM Curso WHERE nome = ?";
        ArrayList<Integer> ids = db.select(sqlCurso, rs -> rs.getInt("id"), estudante.getNomeCurso());
        Integer cursoId = ids.isEmpty() ? null : ids.get(0);

        String sql = "INSERT INTO Estudante (numeroMec, nome, morada, nif, dataNascimento, email, hashSenha, ativo, cursoId, anoLetivo) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        int rows = db.execute(sql,
                estudante.getNumeroMec(),
                estudante.getNome(),
                estudante.getMorada(),
                estudante.getNif(),
                Date.valueOf(estudante.getDataNascimento()),
                estudante.getEmail(),
                estudante.getHash(),
                estudante.isAtivo(),
                cursoId,
                estudante.getAnoLetivo()
        );

        if (rows > 0) return new Resultado<>(estudante, true);
        return new Resultado<>(false, "Erro ao registar estudante na base de dados.");
    }

    @Override
    public Estudante lerEstudante(int numeroMec) {
        String sql = SELECT_BASE + " WHERE e.numeroMec = ?";
        ArrayList<Estudante> lista = db.select(sql, estudanteMapper, numeroMec);
        return lista.isEmpty() ? null : lista.get(0);
    }

    @Override
    public Resultado<Estudante> atualizarEstudante(Estudante estudante) {
        // Resolve cursoId a partir do nome do curso
        String sqlCurso = "SELECT id FROM Curso WHERE nome = ?";
        ArrayList<Integer> ids = db.select(sqlCurso, rs -> rs.getInt("id"), estudante.getNomeCurso());
        Integer cursoId = ids.isEmpty() ? null : ids.get(0);

        String sql = "UPDATE Estudante SET nome=?, morada=?, nif=?, dataNascimento=?, email=?, hashSenha=?, ativo=?, cursoId=?, anoLetivo=? " +
                     "WHERE numeroMec=?";

        int rows = db.execute(sql,
                estudante.getNome(),
                estudante.getMorada(),
                estudante.getNif(),
                Date.valueOf(estudante.getDataNascimento()),
                estudante.getEmail(),
                estudante.getHash(),
                estudante.isAtivo(),
                cursoId,
                estudante.getAnoLetivo(),
                estudante.getNumeroMec()
        );

        if (rows > 0) return new Resultado<>(estudante, true);
        return new Resultado<>(false, "Estudante não encontrado na base de dados.");
    }

    @Override
    public Resultado<Estudante> atualizarSenha(Estudante estudante) {
        String sql = "UPDATE Estudante SET hashSenha=? WHERE numeroMec=?";
        int rows = db.execute(sql, estudante.getHash(), estudante.getNumeroMec());
        if (rows > 0) return new Resultado<>(estudante, true);
        return new Resultado<>(false, "Erro ao atualizar senha na base de dados.");
    }

    @Override
    public Resultado<Estudante> eliminarEstudante(int numeroMec) {
        String sql = "DELETE FROM Estudante WHERE numeroMec=?";
        int rows = db.execute(sql, numeroMec);
        if (rows > 0) return new Resultado<>(null, true);
        return new Resultado<>(false, "Estudante não encontrado na base de dados.");
    }

    @Override
    public List<Estudante> getEstudantes() {
        return db.select(SELECT_BASE, estudanteMapper, (Object[]) null);
    }

    @Override
    public Estudante procurarPorNif(int nif) {
        String sql = SELECT_BASE + " WHERE e.nif = ?";
        ArrayList<Estudante> lista = db.select(sql, estudanteMapper, nif);
        return lista.isEmpty() ? null : lista.get(0);
    }

    @Override
    public Estudante procurarPorEmail(String email) {
        String sql = SELECT_BASE + " WHERE LOWER(e.email) = ?";
        ArrayList<Estudante> lista = db.select(sql, estudanteMapper,
                email != null ? email.toLowerCase() : "");
        return lista.isEmpty() ? null : lista.get(0);
    }

    @Override
    public int gerarNumeroMecanografico() {
        int anoAtual = java.time.LocalDate.now().getYear();
        int yy = anoAtual % 100;
        int prefixo = 1000000 + (yy * 10000);
        int max = prefixo + 9999;

        String sql = "SELECT MAX(numeroMec) AS maxMec FROM Estudante WHERE numeroMec >= ? AND numeroMec <= ?";
        ArrayList<Integer> resultado = db.select(sql, rs -> rs.getInt("maxMec"), prefixo, max);

        if (!resultado.isEmpty() && resultado.get(0) > 0) {
            return resultado.get(0) + 1;
        }
        return prefixo + 1;
    }
}
