package DAL;

import DAL.DB.DatabaseConnection;
import DAL.DB.RowMapper;
import model.Docente;
import model.Resultado;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class DocenteSqlDAO implements IDocenteDAO {

    private final DatabaseConnection db;

    private final RowMapper<Docente> docenteMapper = rs -> {
        Docente d = new Docente(
                rs.getString("nome"),
                rs.getString("morada"),
                rs.getInt("nif"),
                rs.getDate("dataNascimento").toLocalDate(),
                rs.getString("email"),
                rs.getString("hash"),
                rs.getString("sigla"),
                new ArrayList<>(),
                new ArrayList<>()
        );
        d.setAtivo(rs.getBoolean("ativo"));
        return d;
    };

    public DocenteSqlDAO() {
        this.db = new DatabaseConnection();
    }

    @Override
    public Resultado<Docente> registarDocente(Docente docente) {
        String sql = "INSERT INTO Docente (nome, morada, nif, dataNascimento, email, hash, sigla, ativo) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        int rows = db.execute(sql,
                docente.getNome(),
                docente.getMorada(),
                docente.getNif(),
                Date.valueOf(docente.getDataNascimento()),
                docente.getEmail(),
                docente.getHash(),
                docente.getSigla(),
                docente.isAtivo()
        );
        if (rows > 0) return new Resultado<>(docente, true);
        return new Resultado<>(false, "Erro ao registar docente na base de dados.");
    }

    @Override
    public Resultado<Docente> atualizarDocente(Docente docente) {
        String sql = "UPDATE Docente SET nome=?, morada=?, dataNascimento=?, email=?, hash=?, sigla=?, ativo=? WHERE nif=?";
        int rows = db.execute(sql,
                docente.getNome(),
                docente.getMorada(),
                Date.valueOf(docente.getDataNascimento()),
                docente.getEmail(),
                docente.getHash(),
                docente.getSigla(),
                docente.isAtivo(),
                docente.getNif()
        );
        if (rows > 0) return new Resultado<>(docente, true);
        return new Resultado<>(false, "Docente não encontrado.");
    }

    @Override
    public Resultado<Docente> eliminarDocente(int nif) {
        String sql = "DELETE FROM Docente WHERE nif=?";
        int rows = db.execute(sql, nif);
        if (rows > 0) return new Resultado<>(null, true);
        return new Resultado<>(false, "Docente não encontrado.");
    }

    @Override
    public List<Docente> getDocentes() {
        return db.select("SELECT * FROM Docente", docenteMapper, (Object[]) null);
    }

    @Override
    public Docente procurarPorSigla(String sigla) {
        ArrayList<Docente> lista = db.select("SELECT * FROM Docente WHERE sigla=?", docenteMapper, sigla);
        return lista.isEmpty() ? null : lista.get(0);
    }

    @Override
    public Docente procurarPorNif(int nif) {
        ArrayList<Docente> lista = db.select("SELECT * FROM Docente WHERE nif=?", docenteMapper, nif);
        return lista.isEmpty() ? null : lista.get(0);
    }

    @Override
    public Docente procurarPorEmail(String email) {
        ArrayList<Docente> lista = db.select(
                "SELECT * FROM Docente WHERE LOWER(email) = ?", docenteMapper,
                email != null ? email.toLowerCase() : "");
        return lista.isEmpty() ? null : lista.get(0);
    }
}
