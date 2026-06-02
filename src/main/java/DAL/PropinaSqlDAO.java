package DAL;

import DAL.DB.DatabaseConnection;
import DAL.DB.RowMapper;
import model.Propina;

import java.util.ArrayList;
import java.util.List;

public class PropinaSqlDAO implements IPropinaDAO {

    private final DatabaseConnection db;

    private final RowMapper<Propina> propinaMapper = rs -> new Propina(
            rs.getInt("numeroMecEstudante"),
            rs.getInt("anoLetivo"),
            rs.getDouble("valorTotal"),
            rs.getDouble("valorPago")
    );

    public PropinaSqlDAO() {
        this.db = new DatabaseConnection();
    }

    @Override
    public boolean registarPropina(Propina propina) {
        String sql = "INSERT INTO Propina (numeroMecEstudante, anoLetivo, valorTotal, valorPago) VALUES (?, ?, ?, ?)";
        int rows = db.execute(sql,
                propina.getNumeroMecEstudante(),
                propina.getAnoLetivo(),
                propina.getValorTotal(),
                propina.getValorPago()
        );
        return rows > 0;
    }

    @Override
    public Propina procurarPropina(int numeroMec, int anoLetivo) {
        ArrayList<Propina> lista = db.select(
                "SELECT * FROM Propina WHERE numeroMecEstudante=? AND anoLetivo=?",
                propinaMapper, numeroMec, anoLetivo);
        return lista.isEmpty() ? null : lista.get(0);
    }

    @Override
    public boolean atualizarPropina(Propina propinaAtualizada) {
        String sql = "UPDATE Propina SET valorTotal=?, valorPago=? WHERE numeroMecEstudante=? AND anoLetivo=?";
        int rows = db.execute(sql,
                propinaAtualizada.getValorTotal(),
                propinaAtualizada.getValorPago(),
                propinaAtualizada.getNumeroMecEstudante(),
                propinaAtualizada.getAnoLetivo()
        );
        return rows > 0;
    }

    @Override
    public List<Propina> listarPropinasPorEstudante(int numeroMec) {
        return db.select("SELECT * FROM Propina WHERE numeroMecEstudante=?", propinaMapper, numeroMec);
    }

    @Override
    public List<Propina> getTodasPropinas() {
        return db.select("SELECT * FROM Propina", propinaMapper, (Object[]) null);
    }
}
