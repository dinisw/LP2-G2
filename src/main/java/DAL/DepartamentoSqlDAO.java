package DAL;

import DAL.DB.DatabaseConnection;
import DAL.DB.RowMapper;
import model.Departamento;

import java.util.ArrayList;
import java.util.List;

public class DepartamentoSqlDAO implements IDepartamentoDAO {

    private final DatabaseConnection db;

    private final RowMapper<Departamento> depMapper = rs -> new Departamento(
            rs.getString("nome"),
            rs.getString("sigla")
    );

    public DepartamentoSqlDAO() {
        this.db = new DatabaseConnection();
    }

    @Override
    public boolean registarDepartamento(Departamento departamento) {
        if (procurarPorSigla(departamento.getSigla()) != null) return false;
        String sql = "INSERT INTO Departamentos (sigla, nome) VALUES (?, ?)";
        int rows = db.execute(sql, departamento.getSigla(), departamento.getNome());
        return rows > 0;
    }

    @Override
    public List<Departamento> getDepartamentos() {
        return db.select("SELECT * FROM Departamentos", depMapper, (Object[]) null);
    }

    @Override
    public Departamento procurarPorSigla(String sigla) {
        ArrayList<Departamento> lista = db.select("SELECT * FROM Departamentos WHERE sigla=?", depMapper, sigla);
        return lista.isEmpty() ? null : lista.get(0);
    }

    @Override
    public boolean atualizarDepartamento(Departamento dep) {
        String sql = "UPDATE Departamentos SET nome=? WHERE sigla=?";
        int rows = db.execute(sql, dep.getNome(), dep.getSigla());
        return rows > 0;
    }

    @Override
    public boolean eliminarDepartamento(String sigla) {
        int rows = db.execute("DELETE FROM Departamentos WHERE sigla=?", sigla);
        return rows > 0;
    }
}
