package DAL.DB;

import io.github.cdimascio.dotenv.Dotenv;

import java.sql.*;
import java.util.ArrayList;

public class DatabaseConnection {

    // ── Configuração carregada UMA ÚNICA VEZ ao arranque da JVM ──────────────
    private static final String serverName;
    private static final String databaseName;
    private static final String username;
    private static final String password;

    static {
        Dotenv dotenv = Dotenv.configure()
                .directory("src/main/resources")
                .ignoreIfMalformed()
                .ignoreIfMissing()
                .load();
        serverName   = nvl(dotenv.get("DB_SERVER"));
        databaseName = nvl(dotenv.get("DB_DATABASE"));
        username     = nvl(dotenv.get("DB_USER"));
        password     = nvl(dotenv.get("DB_PASSWORD"));
    }

    // ── Estado de erro de ligação (visível na view de login) ─────────────────
    private static boolean erroConexao = false;
    public static boolean houveErroConexao() { return erroConexao; }

    private static String nvl(String s) { return s != null ? s : ""; }

    public DatabaseConnection() {
        // Construtor vazio — configuração já carregada estaticamente
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
     * Uso: db.runTransaction(conn -> { INSERT...; INSERT...; });
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
