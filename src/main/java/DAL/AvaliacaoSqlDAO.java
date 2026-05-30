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
            String nomeUC = rs.getString("nome_uc");
            int numeroMec = rs.getInt("numero_mec");
            UnidadeCurricular uc = ucDAO.procurarPorNome(nomeUC);
            Estudante estudante = estudanteDAO.lerEstudante(numeroMec);

            double notaRaw = rs.getDouble("nota");
            Double nota = rs.wasNull() ? null : notaRaw;

            return new Avaliacao(rs.getString("momento"), nota, uc, estudante);
        };
    }

    @Override
    public Resultado<Avaliacao> registarAvaliacao(Avaliacao avaliacao) {
        // Upsert: se já existe o par (momento, nome_uc, numero_mec), atualiza a nota
        String sqlCheck = "SELECT COUNT(*) AS total FROM Avaliacoes WHERE momento=? AND nome_uc=? AND numero_mec=?";
        ArrayList<Integer> existe = db.select(sqlCheck,
                rs -> rs.getInt("total"),
                avaliacao.getMomento(),
                avaliacao.getUnidadeCurricular().getNome(),
                avaliacao.getEstudante().getNumeroMec()
        );

        int rows;
        if (!existe.isEmpty() && existe.get(0) > 0) {
            String sqlUpdate = "UPDATE Avaliacoes SET nota=? WHERE momento=? AND nome_uc=? AND numero_mec=?";
            rows = db.execute(sqlUpdate,
                    avaliacao.getNota(),
                    avaliacao.getMomento(),
                    avaliacao.getUnidadeCurricular().getNome(),
                    avaliacao.getEstudante().getNumeroMec()
            );
        } else {
            String sqlInsert = "INSERT INTO Avaliacoes (momento, nota, nome_uc, numero_mec) VALUES (?, ?, ?, ?)";
            rows = db.execute(sqlInsert,
                    avaliacao.getMomento(),
                    avaliacao.getNota(),
                    avaliacao.getUnidadeCurricular().getNome(),
                    avaliacao.getEstudante().getNumeroMec()
            );
        }

        if (rows > 0) return new Resultado<>(avaliacao, true);
        return new Resultado<>(false, "Erro ao registar avaliação.");
    }

    @Override
    public List<Avaliacao> listarPorEstudante(int numeroMec) {
        return db.select("SELECT * FROM Avaliacoes WHERE numero_mec=?", avaliacaoMapper(), numeroMec);
    }

    @Override
    public List<Avaliacao> listarPorUnidadeCurricular(String nomeUC) {
        return db.select("SELECT * FROM Avaliacoes WHERE nome_uc=?", avaliacaoMapper(), nomeUC);
    }
}
