package DAL;

import DAL.DB.DatabaseConnection;
import DAL.DB.RowMapper;
import model.Avaliacao;
import model.Estudante;
import model.Resultado;
import model.UnidadeCurricular;

import java.util.ArrayList;
import java.util.List;

public class AvaliacaoSqlDAO implements IAvaliacaoDAO {

    private final DatabaseConnection db;

    public AvaliacaoSqlDAO() {
        this.db = new DatabaseConnection();
    }

    private RowMapper<Avaliacao> avaliacaoMapper() {
        IEstudanteDAO estudanteDAO = DAOFactory.getEstudanteDAO();
        IUnidadeCurricularDAO ucDAO = DAOFactory.getUnidadeCurricularDAO();
        return rs -> {
            int ucId = rs.getInt("ucId");
            int numeroMec = rs.getInt("estudanteNumeroMec");
            UnidadeCurricular uc = ucDAO.procurarPorId(ucId);
            Estudante estudante = estudanteDAO.lerEstudante(numeroMec);

            double notaRaw = rs.getDouble("nota");
            Double nota = rs.wasNull() ? null : notaRaw;

            return new Avaliacao(rs.getString("momento"), nota, uc, estudante);
        };
    }

    @Override
    public Resultado<Avaliacao> registarAvaliacao(Avaliacao avaliacao) {
        // Resolve ucId a partir do nome da UC
        int ucId = resolverUcId(avaliacao.getUnidadeCurricular().getNome());
        int numeroMec = avaliacao.getEstudante().getNumeroMec();

        // Upsert: se já existe, atualiza a nota
        ArrayList<Integer> existe = db.select(
                "SELECT COUNT(*) AS total FROM Avaliacao WHERE momento=? AND ucId=? AND estudanteNumeroMec=?",
                rs -> rs.getInt("total"),
                avaliacao.getMomento(), ucId, numeroMec
        );

        int rows;
        if (!existe.isEmpty() && existe.get(0) > 0) {
            rows = db.execute(
                    "UPDATE Avaliacao SET nota=? WHERE momento=? AND ucId=? AND estudanteNumeroMec=?",
                    avaliacao.getNota(), avaliacao.getMomento(), ucId, numeroMec
            );
        } else {
            rows = db.execute(
                    "INSERT INTO Avaliacao (momento, nota, ucId, estudanteNumeroMec) VALUES (?, ?, ?, ?)",
                    avaliacao.getMomento(), avaliacao.getNota(), ucId, numeroMec
            );
        }

        if (rows > 0) return new Resultado<>(avaliacao, true);
        return new Resultado<>(false, "Erro ao registar avaliação.");
    }

    @Override
    public List<Avaliacao> listarPorEstudante(int numeroMec) {
        return db.select(
                "SELECT * FROM Avaliacao WHERE estudanteNumeroMec=?",
                avaliacaoMapper(), numeroMec);
    }

    @Override
    public List<Avaliacao> listarPorUnidadeCurricular(String nomeUC) {
        int ucId = resolverUcId(nomeUC);
        if (ucId <= 0) return new ArrayList<>();
        return db.select(
                "SELECT * FROM Avaliacao WHERE ucId=?",
                avaliacaoMapper(), ucId);
    }

    @Override
    public boolean eliminarAvaliacoesPorEstudante(int numeroMec) {
        int rows = db.execute("DELETE FROM Avaliacao WHERE numeroMecEstudante=?", numeroMec);
        return rows > 0;
    }

    private int resolverUcId(String nomeUC) {
        ArrayList<Integer> ids = db.select(
                "SELECT id FROM UnidadeCurricular WHERE nome=?",
                rs -> rs.getInt("id"), nomeUC
        );
        return ids.isEmpty() ? 0 : ids.get(0);
    }
}
