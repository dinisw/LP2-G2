package DAL;

import DAL.DB.DatabaseConnection;
import DAL.DB.RowMapper;
import model.Propina;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class PropinaSqlDAO implements IPropinaDAO {

    private final DatabaseConnection db;

    public PropinaSqlDAO() {
        this.db = new DatabaseConnection();
    }

    private RowMapper<Propina> propinaMapper() {
        return rs -> {
            Propina propina = new Propina(
                    rs.getInt("numeroMecEstudante"),
                    rs.getInt("anoLetivo"),
                    rs.getBigDecimal("valorTotal"),
                    rs.getBigDecimal("valorPago")
            );
            propina.setId(rs.getInt("id"));

            List<String> historico = db.select(
                    "SELECT dataPagamento, valor FROM PropinaPagamento WHERE propinaId=? ORDER BY dataPagamento",
                    r -> r.getString("dataPagamento") + " -> " + r.getBigDecimal("valor").toPlainString() + " EUR",
                    propina.getId()
            );
            propina.setHistoricoPagamentos(historico);

            return propina;
        };
    }

    private void guardarHistorico(int propinaId, List<String> historico) {
        db.execute("DELETE FROM PropinaPagamento WHERE propinaId=?", propinaId);
        if (historico == null) return;
        for (String entrada : historico) {
            try {
                String[] partes = entrada.split(" -> ");
                String data = partes[0].trim();
                String valor = partes[1].replace(" EUR", "").trim();
                db.execute(
                        "INSERT INTO PropinaPagamento (propinaId, dataPagamento, valor) VALUES (?, ?, ?)",
                        propinaId, data, new BigDecimal(valor)
                );
            } catch (Exception e) {
                System.err.println("Aviso: não foi possível guardar entrada do histórico: " + entrada);
            }
        }
    }

    @Override
    public boolean registarPropina(Propina propina) {
        String sql = "INSERT INTO Propina (numeroMecEstudante, anoLetivo, valorTotal, valorPago) VALUES (?, ?, ?, ?)";
        int idGerado = db.create(sql,
                propina.getNumeroMecEstudante(),
                propina.getAnoLetivo(),
                propina.getValorTotal(),
                propina.getValorPago()
        );
        if (idGerado > 0) {
            propina.setId(idGerado);
            guardarHistorico(idGerado, propina.getHistoricoPagamentos());
            return true;
        }
        return false;
    }

    @Override
    public Propina procurarPropina(int numeroMec, int anoLetivo) {
        ArrayList<Propina> lista = db.select(
                "SELECT * FROM Propina WHERE numeroMecEstudante=? AND anoLetivo=?",
                propinaMapper(), numeroMec, anoLetivo);
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
        if (rows > 0) {
            guardarHistorico(propinaAtualizada.getId(), propinaAtualizada.getHistoricoPagamentos());
            return true;
        }
        return false;
    }

    @Override
    public List<Propina> listarPropinasPorEstudante(int numeroMec) {
        return db.select("SELECT * FROM Propina WHERE numeroMecEstudante=?", propinaMapper(), numeroMec);
    }

    @Override
    public List<Propina> getTodasPropinas() {
        return db.select("SELECT * FROM Propina", propinaMapper(), (Object[]) null);
    }

    @Override
    public boolean eliminarPropinasPorEstudante(int numeroMec) {
        // apagar histórico antes de apagar as propinas (integridade referencial)
        List<Propina> propinas = listarPropinasPorEstudante(numeroMec);
        for (Propina p : propinas) {
            db.execute("DELETE FROM PropinaPagamento WHERE propinaId=?", p.getId());
        }
        int rows = db.execute("DELETE FROM Propina WHERE numeroMecEstudante=?", numeroMec);
        return rows > 0;
    }
}
