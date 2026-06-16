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

    // JOIN para evitar N sub-queries por avaliação (era 4N queries, agora 1)
    private static final String SELECT_AVALIACAO =
            "SELECT a.momento, a.nota, a.ucId, a.estudanteNumeroMec, " +
            "uc.nome AS ucNome, uc.anoCurricular, uc.semestre, " +
            "e.nome AS estNome, e.morada AS estMorada, e.nif AS estNif, " +
            "e.dataNascimento AS estDataNasc, e.email AS estEmail, " +
            "e.hashSenha AS estHash, e.ativo AS estAtivo, " +
            "c.nome AS nomeCurso " +
            "FROM Avaliacao a " +
            "LEFT JOIN UnidadeCurricular uc ON a.ucId = uc.id " +
            "LEFT JOIN Estudante e ON a.estudanteNumeroMec = e.numeroMec " +
            "LEFT JOIN Curso c ON e.cursoId = c.id";

    public AvaliacaoSqlDAO() {
        this.db = new DatabaseConnection();
    }

    private final RowMapper<Avaliacao> avaliacaoMapper = rs -> {
        UnidadeCurricular uc = new UnidadeCurricular(
                rs.getString("ucNome"),
                rs.getInt("ucId"),
                rs.getInt("anoCurricular"),
                rs.getInt("semestre"),
                null,
                new ArrayList<>()
        );

        Estudante estudante = new Estudante(
                rs.getString("estNome"),
                rs.getString("estMorada"),
                rs.getInt("estNif"),
                rs.getDate("estDataNasc").toLocalDate(),
                rs.getString("estEmail"),
                rs.getInt("estudanteNumeroMec"),
                rs.getString("estHash"),
                rs.getString("nomeCurso"),
                rs.getBoolean("estAtivo")
        );

        double notaRaw = rs.getDouble("nota");
        Double nota = rs.wasNull() ? null : notaRaw;

        return new Avaliacao(rs.getString("momento"), nota, uc, estudante);
    };

    @Override
    public Resultado<Avaliacao> registarAvaliacao(Avaliacao avaliacao) {
        int ucId = resolverUcId(avaliacao.getUnidadeCurricular().getNome());
        int numeroMec = avaliacao.getEstudante().getNumeroMec();

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
                SELECT_AVALIACAO + " WHERE a.estudanteNumeroMec=?",
                avaliacaoMapper, numeroMec);
    }

    @Override
    public List<Avaliacao> listarPorUnidadeCurricular(String nomeUC) {
        int ucId = resolverUcId(nomeUC);
        if (ucId <= 0) return new ArrayList<>();
        return db.select(
                SELECT_AVALIACAO + " WHERE a.ucId=?",
                avaliacaoMapper, ucId);
    }

    @Override
    public boolean eliminarAvaliacoesPorEstudante(int numeroMec) {
        int rows = db.execute("DELETE FROM Avaliacao WHERE estudanteNumeroMec=?", numeroMec);
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
