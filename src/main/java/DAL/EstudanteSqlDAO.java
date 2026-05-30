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

    private final RowMapper<Estudante> estudanteMapper = rs -> new Estudante(
            rs.getString("nome"),
            rs.getString("morada"),
            rs.getInt("nif"),
            rs.getDate("data_nascimento").toLocalDate(),
            rs.getString("email"),
            rs.getInt("numero_mec"),
            rs.getString("hash_senha"),
            rs.getString("curso"),
            rs.getBoolean("ativo")
    );

    public EstudanteSqlDAO() {
        this.db = new DatabaseConnection();
    }

    @Override
    public Resultado<Estudante> registarEstudante(Estudante estudante) {
        String sql = "INSERT INTO Estudantes (nome, morada, nif, data_nascimento, email, numero_mec, hash_senha, curso, ativo) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        int rows = db.execute(sql,
                estudante.getNome(),
                estudante.getMorada(),
                estudante.getNif(),
                Date.valueOf(estudante.getDataNascimento()),
                estudante.getEmail(),
                estudante.getNumeroMec(),
                estudante.getHash(),
                estudante.getNomeCurso(),
                estudante.isAtivo()
        );

        if (rows > 0) return new Resultado<>(estudante, true);
        return new Resultado<>(false, "Erro ao registar estudante na base de dados.");
    }

    @Override
    public Estudante lerEstudante(int numeroMec) {
        String sql = "SELECT * FROM Estudantes WHERE numero_mec = ?";
        ArrayList<Estudante> lista = db.select(sql, estudanteMapper, numeroMec);
        return lista.isEmpty() ? null : lista.get(0);
    }

    @Override
    public Resultado<Estudante> atualizarEstudante(Estudante estudante) {
        String sql = "UPDATE Estudantes SET nome=?, morada=?, nif=?, data_nascimento=?, email=?, hash_senha=?, curso=?, ativo=? " +
                     "WHERE numero_mec=?";

        int rows = db.execute(sql,
                estudante.getNome(),
                estudante.getMorada(),
                estudante.getNif(),
                Date.valueOf(estudante.getDataNascimento()),
                estudante.getEmail(),
                estudante.getHash(),
                estudante.getNomeCurso(),
                estudante.isAtivo(),
                estudante.getNumeroMec()
        );

        if (rows > 0) return new Resultado<>(estudante, true);
        return new Resultado<>(false, "Estudante não encontrado na base de dados.");
    }

    @Override
    public Resultado<Estudante> atualizarSenha(Estudante estudante) {
        String sql = "UPDATE Estudantes SET hash_senha=? WHERE numero_mec=?";

        int rows = db.execute(sql, estudante.getHash(), estudante.getNumeroMec());

        if (rows > 0) return new Resultado<>(estudante, true);
        return new Resultado<>(false, "Erro ao atualizar senha na base de dados.");
    }

    @Override
    public Resultado<Estudante> eliminarEstudante(int numeroMec) {
        String sql = "DELETE FROM Estudantes WHERE numero_mec=?";

        int rows = db.execute(sql, numeroMec);

        if (rows > 0) return new Resultado<>(null, true);
        return new Resultado<>(false, "Estudante não encontrado na base de dados.");
    }

    @Override
    public List<Estudante> getEstudantes() {
        String sql = "SELECT * FROM Estudantes";
        return db.select(sql, estudanteMapper, (Object[]) null);
    }

    @Override
    public Estudante procurarPorNif(int nif) {
        String sql = "SELECT * FROM Estudantes WHERE nif = ?";
        ArrayList<Estudante> lista = db.select(sql, estudanteMapper, nif);
        return lista.isEmpty() ? null : lista.get(0);
    }

    @Override
    public int gerarNumeroMecanografico() {
        int anoAtual = java.time.LocalDate.now().getYear();
        int yy = anoAtual % 100;
        int prefixo = 1000000 + (yy * 10000);
        int max = prefixo + 9999;

        String sql = "SELECT MAX(numero_mec) AS max_mec FROM Estudantes WHERE numero_mec >= ? AND numero_mec <= ?";

        ArrayList<Integer> resultado = db.select(sql, rs -> rs.getInt("max_mec"), prefixo, max);

        if (!resultado.isEmpty() && resultado.get(0) > 0) {
            return resultado.get(0) + 1;
        }
        return prefixo + 1;
    }
}
