package DAL;

import DAL.DB.DatabaseConnection;
import DAL.DB.RowMapper;
import model.Gestor;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class GestorSqlDAO implements IGestorDAO {

    private final DatabaseConnection db;

    private final RowMapper<Gestor> gestorMapper = rs -> new Gestor(
            rs.getInt("id"),
            rs.getString("nome"),
            rs.getString("morada"),
            rs.getInt("nif"),
            rs.getDate("data_nascimento").toLocalDate(),
            rs.getString("email"),
            rs.getString("hash_senha"),
            rs.getString("cargo")
    );

    public GestorSqlDAO() {
        this.db = new DatabaseConnection();
    }

    @Override
    public boolean registarGestor(Gestor gestor) {
        String sql = "INSERT INTO Gestores (nome, morada, nif, data_nascimento, email, hash_senha, cargo) VALUES (?, ?, ?, ?, ?, ?, ?)";
        int idGerado = db.create(sql,
                gestor.getNome(),
                gestor.getMorada(),
                gestor.getNif(),
                Date.valueOf(gestor.getDataNascimento()),
                gestor.getEmail(),
                gestor.getHash(),
                gestor.getCargo()
        );
        if (idGerado > 0) {
            gestor.setId(idGerado);
            return true;
        }
        return false;
    }

    @Override
    public List<Gestor> getGestores() {
        return db.select("SELECT * FROM Gestores", gestorMapper, (Object[]) null);
    }

    @Override
    public Gestor procurarPorEmail(String email) {
        ArrayList<Gestor> lista = db.select("SELECT * FROM Gestores WHERE email=?", gestorMapper, email);
        return lista.isEmpty() ? null : lista.get(0);
    }

    @Override
    public Gestor procurarPorNif(int nif) {
        ArrayList<Gestor> lista = db.select("SELECT * FROM Gestores WHERE nif=?", gestorMapper, nif);
        return lista.isEmpty() ? null : lista.get(0);
    }

    @Override
    public boolean atualizarGestor(Gestor gestor) {
        String sql = "UPDATE Gestores SET nome=?, morada=?, data_nascimento=?, email=?, hash_senha=?, cargo=? WHERE nif=?";
        int rows = db.execute(sql,
                gestor.getNome(),
                gestor.getMorada(),
                Date.valueOf(gestor.getDataNascimento()),
                gestor.getEmail(),
                gestor.getHash(),
                gestor.getCargo(),
                gestor.getNif()
        );
        return rows > 0;
    }

    @Override
    public boolean eliminarGestor(int nif) {
        return db.execute("DELETE FROM Gestores WHERE nif=?", nif) > 0;
    }

    @Override
    public Gestor getGestorPorID(int id) {
        ArrayList<Gestor> lista = db.select("SELECT * FROM Gestores WHERE id=?", gestorMapper, id);
        return lista.isEmpty() ? null : lista.get(0);
    }
}
