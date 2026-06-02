package DAL;

import DAL.DB.DatabaseConnection;
import DAL.DB.RowMapper;
import model.Gestor;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class GestorSqlDAO implements IGestorDAO {

    private final DatabaseConnection db;

    private final RowMapper<Gestor> gestorMapper = rs -> {
        Gestor g = new Gestor(
                rs.getInt("id"),
                rs.getString("nome"),
                rs.getString("morada"),
                rs.getInt("nif"),
                rs.getDate("dataNascimento").toLocalDate(),
                rs.getString("email"),
                rs.getString("hash"),
                rs.getString("cargo")
        );
        g.setAtivo(rs.getBoolean("ativo"));
        return g;
    };

    public GestorSqlDAO() {
        this.db = new DatabaseConnection();
    }

    @Override
    public boolean registarGestor(Gestor gestor) {
        String sql = "INSERT INTO Gestor (nome, morada, nif, dataNascimento, email, hash, cargo, ativo) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        int idGerado = db.create(sql,
                gestor.getNome(),
                gestor.getMorada(),
                gestor.getNif(),
                Date.valueOf(gestor.getDataNascimento()),
                gestor.getEmail(),
                gestor.getHash(),
                gestor.getCargo(),
                gestor.isAtivo()
        );
        if (idGerado > 0) {
            gestor.setId(idGerado);
            return true;
        }
        return false;
    }

    @Override
    public List<Gestor> getGestores() {
        return db.select("SELECT * FROM Gestor", gestorMapper, (Object[]) null);
    }

    @Override
    public Gestor procurarPorEmail(String email) {
        ArrayList<Gestor> lista = db.select("SELECT * FROM Gestor WHERE email=?", gestorMapper, email);
        return lista.isEmpty() ? null : lista.get(0);
    }

    @Override
    public Gestor procurarPorNif(int nif) {
        ArrayList<Gestor> lista = db.select("SELECT * FROM Gestor WHERE nif=?", gestorMapper, nif);
        return lista.isEmpty() ? null : lista.get(0);
    }

    @Override
    public boolean atualizarGestor(Gestor gestor) {
        String sql = "UPDATE Gestor SET nome=?, morada=?, dataNascimento=?, email=?, hash=?, cargo=?, ativo=? WHERE nif=?";
        int rows = db.execute(sql,
                gestor.getNome(),
                gestor.getMorada(),
                Date.valueOf(gestor.getDataNascimento()),
                gestor.getEmail(),
                gestor.getHash(),
                gestor.getCargo(),
                gestor.isAtivo(),
                gestor.getNif()
        );
        return rows > 0;
    }

    @Override
    public boolean eliminarGestor(int nif) {
        return db.execute("DELETE FROM Gestor WHERE nif=?", nif) > 0;
    }

    @Override
    public Gestor getGestorPorID(int id) {
        ArrayList<Gestor> lista = db.select("SELECT * FROM Gestor WHERE id=?", gestorMapper, id);
        return lista.isEmpty() ? null : lista.get(0);
    }
}
