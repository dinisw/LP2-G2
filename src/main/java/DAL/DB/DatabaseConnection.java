package DAL.DB;

import io.github.cdimascio.dotenv.Dotenv;

import java.sql.*;
import java.util.ArrayList;

public class DatabaseConnection {

    // ── Config estática: carregada UMA VEZ, na primeira instanciação ─────────
    // (não num bloco static{} — evita NoClassDefFoundError se dotenv falhar
    //  antes do class-loading estar completo)
    private static volatile String serverName;
    private static volatile String databaseName;
    private static volatile String username;
    private static volatile String password;
    private static volatile boolean configCarregada = false;

    private static boolean erroConexao = false;
    public static boolean houveErroConexao() { return erroConexao; }

    public DatabaseConnection() {
        // Double-checked locking: config lida apenas uma vez por JVM
        if (!configCarregada) {
            synchronized (DatabaseConnection.class) {
                if (!configCarregada) {
                    carregarConfig();
                    configCarregada = true;
                }
            }
        }
    }

    private static void carregarConfig() {
        try {
            Dotenv dotenv = Dotenv.configure()
                    .directory("src/main/resources")
                    .ignoreIfMalformed()
                    .ignoreIfMissing()
                    .load();
            serverName   = nvl(dotenv.get("DB_SERVER"));
            databaseName = nvl(dotenv.get("DB_DATABASE"));
            username     = nvl(dotenv.get("DB_USER"));
            password     = nvl(dotenv.get("DB_PASSWORD"));
        } catch (Exception e) {
            System.err.println("Aviso: Não foi possível carregar configurações da BD: " + e.getMessage());
            serverName = databaseName = username = password = "";
        }
    }

    private static String nvl(String s) { return s != null ? s : ""; }

    /** Verifica se uma tabela existe na BD actual (SQL Server). Não imprime erros. */
    public boolean tabelaExiste(String nomeTabela) {
        try {
            ArrayList<Integer> result = select(
                    "SELECT COUNT(*) AS total FROM INFORMATION_SCHEMA.TABLES " +
                    "WHERE TABLE_TYPE='BASE TABLE' AND TABLE_NAME=?",
                    rs -> rs.getInt("total"), nomeTabela);
            return !result.isEmpty() && result.get(0) > 0;
        } catch (Exception e) {
            return false;
        }
    }

    // ── Interface funcional para transações ──────────────────────────────────
    @FunctionalInterface
    public interface TransactionConsumer {
        void execute(Connection conn) throws SQLException;
    }

    private Connection openConnection() {
        try {
            String url = "jdbc:sqlserver://" + serverName
                    + ";databaseName=" + databaseName
                    + ";user="         + username
                    + ";password="     + password
                    + ";encrypt=false";
            Connection conn = DriverManager.getConnection(url);
            erroConexao = false;
            return conn;
        } catch (Exception ex) {
            erroConexao = true;
            System.out.println("Erro ao ligar à base de dados: " + ex.getMessage());
            return null;
        }
    }

    // ── SELECT ────────────────────────────────────────────────────────────────
    public <T> ArrayList<T> select(String sql, RowMapper<T> mapper, Object... params) {
        ArrayList<T> results = new ArrayList<>();
        Connection conn = openConnection();
        if (conn == null) return results;
        try (conn) {
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                if (params != null) {
                    for (int i = 0; i < params.length; i++) stmt.setObject(i + 1, params[i]);
                }
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) results.add(mapper.mapRow(rs));
                }
            }
        } catch (Exception ex) {
            System.out.println("Erro ao executar SELECT: " + ex.getMessage());
        }
        return results;
    }

    // ── INSERT com chave gerada ───────────────────────────────────────────────
    public int create(String sql, Object... params) {
        int result = 0;
        Connection conn = openConnection();
        if (conn == null) return result;
        try (conn) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                if (params != null) {
                    for (int i = 0; i < params.length; i++) stmt.setObject(i + 1, params[i]);
                }
                stmt.executeUpdate();
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) result = keys.getInt(1);
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                System.out.println("Erro ao executar INSERT (rollback efectuado): " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("Erro de ligação: " + e.getMessage());
        }
        return result;
    }

    // ── UPDATE / DELETE ───────────────────────────────────────────────────────
    public int execute(String sql, Object... params) {
        int rowsAffected = 0;
        Connection conn = openConnection();
        if (conn == null) return rowsAffected;
        try (conn) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                if (params != null) {
                    for (int i = 0; i < params.length; i++) stmt.setObject(i + 1, params[i]);
                }
                rowsAffected = stmt.executeUpdate();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                System.out.println("Erro ao executar UPDATE/DELETE (rollback efectuado): " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("Erro de ligação: " + e.getMessage());
        }
        return rowsAffected;
    }

    /**
     * Executa múltiplas operações numa única transação.
     * Se qualquer operação falhar, todas são revertidas (rollback).
     */
    public boolean runTransaction(TransactionConsumer work) {
        Connection conn = openConnection();
        if (conn == null) return false;
        try (conn) {
            conn.setAutoCommit(false);
            try {
                work.execute(conn);
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                System.out.println("Erro na transação (rollback efectuado): " + e.getMessage());
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Erro de ligação: " + e.getMessage());
            return false;
        }
    }
}
