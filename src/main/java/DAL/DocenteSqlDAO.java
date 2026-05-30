package DAL;

import DAL.DB.DatabaseConnection;
import DAL.DB.RowMapper;
import model.Docente;
import model.Resultado;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DocenteSqlDAO implements IDocenteDAO {

    private final DatabaseConnection db;

    private final RowMapper<Docente> docenteMapper = rs -> new Docente(
            rs.getString("nome"),
            rs.getString("morada"),
            rs.getInt("nif"),
            rs.getDate("data_nascimento").toLocalDate(),
            rs.getString("email"),
            rs.getString("hash_senha"),
            rs.getString("sigla"),
            new ArrayList<>(),
            new ArrayList<>()
    );

    public DocenteSqlDAO() {
        this.db = new DatabaseConnection();
    }

    @Override
    public Resultado<Docente> registarDocente(Docente docente) {
        if (procurarPorNif(docente.getNif()) != null) {
            return new Resultado<>(false, "NIF já existe.");
        }
        String sql = "INSERT INTO Docentes (nome, morada, nif, data_nascimento, email, hash_senha, sigla) VALUES (?, ?, ?, ?, ?, ?, ?)";
        int rows = db.execute(sql,
                docente.getNome(),
                docente.getMorada(),
                docente.getNif(),
                Date.valueOf(docente.getDataNascimento()),
                docente.getEmail(),
                docente.getHash(),
                docente.getSigla()
        );
        if (rows > 0) return new Resultado<>(docente, true);
        return new Resultado<>(false, "Erro ao registar docente na base de dados.");
    }

    @Override
    public Resultado<Docente> atualizarDocente(Docente docente) {
        String sql = "UPDATE Docentes SET nome=?, morada=?, data_nascimento=?, email=?, hash_senha=?, sigla=? WHERE nif=?";
        int rows = db.execute(sql,
                docente.getNome(),
                docente.getMorada(),
                Date.valueOf(docente.getDataNascimento()),
                docente.getEmail(),
                docente.getHash(),
                docente.getSigla(),
                docente.getNif()
        );
        if (rows > 0) return new Resultado<>(docente, true);
        return new Resultado<>(false, "Docente não encontrado.");
    }

    @Override
    public Resultado<Docente> eliminarDocente(int nif) {
        String sql = "DELETE FROM Docentes WHERE nif=?";
        int rows = db.execute(sql, nif);
        if (rows > 0) return new Resultado<>(null, true);
        return new Resultado<>(false, "Docente não encontrado.");
    }

    @Override
    public List<Docente> getDocentes() {
        return db.select("SELECT * FROM Docentes", docenteMapper, (Object[]) null);
    }

    @Override
    public Docente procurarPorSigla(String sigla) {
        ArrayList<Docente> lista = db.select("SELECT * FROM Docentes WHERE sigla=?", docenteMapper, sigla);
        return lista.isEmpty() ? null : lista.get(0);
    }

    @Override
    public Docente procurarPorNif(int nif) {
        ArrayList<Docente> lista = db.select("SELECT * FROM Docentes WHERE nif=?", docenteMapper, nif);
        return lista.isEmpty() ? null : lista.get(0);
    }
}
