package DAL;

import DAL.DB.DatabaseConnection;
import DAL.DB.RowMapper;
import model.Propina;

import java.util.ArrayList;
import java.util.List;

public class PropinaSqlDAO implements IPropinaDAO {

    private final DatabaseConnection db;

    private final RowMapper<Propina> propinaMapper = rs -> new Propina(
            rs.getInt("numero_mec"),
            rs.getInt("ano_letivo"),
            rs.getDouble("valor_total"),
            rs.getDouble("valor_pago")
    );

    public PropinaSqlDAO() {
        this.db = new DatabaseConnection();
    }

    @Override
    public boolean registarPropina(Propina propina) {
        String sql = "INSERT INTO Propinas (numero_mec, ano_letivo, valor_total, valor_pago) VALUES (?, ?, ?, ?)";
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
                "SELECT * FROM Propinas WHERE numero_mec=? AND ano_letivo=?",
                propinaMapper, numeroMec, anoLetivo);
        return lista.isEmpty() ? null : lista.get(0);
    }

    @Override
    public boolean atualizarPropina(Propina propinaAtualizada) {
        String sql = "UPDATE Propinas SET valor_total=?, valor_pago=? WHERE numero_mec=? AND ano_letivo=?";
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
        return db.select("SELECT * FROM Propinas WHERE numero_mec=?", propinaMapper, numeroMec);
    }

    @Override
    public List<Propina> getTodasPropinas() {
        return db.select("SELECT * FROM Propinas", propinaMapper, (Object[]) null);
    }
}
