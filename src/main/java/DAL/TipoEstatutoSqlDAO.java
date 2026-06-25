package DAL;

import DAL.DB.DatabaseConnection;
import model.TipoEstatuto;

import java.util.List;

public class TipoEstatutoSqlDAO implements ITipoEstatutoDAO {

    private final DatabaseConnection db;

    public TipoEstatutoSqlDAO() {
        this.db = new DatabaseConnection();
    }

    private final DAL.DB.RowMapper<TipoEstatuto> mapper = rs -> {
        TipoEstatuto t = new TipoEstatuto(rs.getString("nome"), rs.getString("descricao"));
        t.setId(rs.getInt("id"));
        return t;
    };

    @Override
    public boolean registarTipoEstatuto(TipoEstatuto tipo) {
        if (procurarPorNome(tipo.getNome()) != null) return false;
        int id = db.create("INSERT INTO TipoEstatuto (nome, descricao) VALUES (?, ?)",
                tipo.getNome(), tipo.getDescricao() != null ? tipo.getDescricao() : "");
        if (id > 0) { tipo.setId(id); return true; }
        return false;
    }

    @Override
    public boolean eliminarTipoEstatuto(int id) {
        return db.execute("DELETE FROM TipoEstatuto WHERE id=?", id) > 0;
    }

    @Override
    public TipoEstatuto procurarPorId(int id) {
        List<TipoEstatuto> lista = db.select("SELECT * FROM TipoEstatuto WHERE id=?", mapper, id);
        return lista.isEmpty() ? null : lista.get(0);
    }

    @Override
    public TipoEstatuto procurarPorNome(String nome) {
        if (nome == null) return null;
        List<TipoEstatuto> lista = db.select("SELECT * FROM TipoEstatuto WHERE nome=?", mapper, nome);
        return lista.isEmpty() ? null : lista.get(0);
    }

    @Override
    public List<TipoEstatuto> listarTodos() {
        return db.select("SELECT * FROM TipoEstatuto ORDER BY nome", mapper, (Object[]) null);
    }
}
