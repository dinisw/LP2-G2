package DAL.DB;

import io.github.cdimascio.dotenv.Dotenv;

import java.security.Security;
import java.sql.*;
import java.util.ArrayList;

public class DatabaseConnection {

    private static boolean erroConexao = false;
    public static boolean houveErroConexao() { return erroConexao; }

    private final String serverName;
    private final String databaseName;
    private final String username;
    private final String password;

    public DatabaseConnection() {
        // Garante compatibilidade TLS com SQL Server 2016 no Java 17
        Security.setProperty("jdk.tls.disabledAlgorithms", "");
        System.setProperty("jdk.tls.client.protocols", "TLSv1,TLSv1.1,TLSv1.2,TLSv1.3");

        Dotenv dotenv = Dotenv.configure()
                .directory("src/main/resources")
                .ignoreIfMalformed()
                .ignoreIfMissing()
                .load();

        this.serverName   = dotenv.get("DB_SERVER");
        this.databaseName = dotenv.get("DB_DATABASE");
        this.username     = dotenv.get("DB_USER");
        this.password     = dotenv.get("DB_PASSWORD");
    }

    /**
     * Abre uma ligação nova e independente.
     * Cada operação (select/create/execute) usa a sua própria Connection
     * para evitar que queries aninhadas fechem a ligação da query exterior.
     */
    private Connection openConnection() {
        try {
            String url = "jdbc:sqlserver://" + serverName +
                    ";databaseName=" + databaseName +
                    ";user="         + username +
                    ";password="     + password +
                    ";encrypt=false";
            Connection conn = DriverManager.getConnection(url);
            erroConexao = false;
            return conn;
        } catch (Exception ex) {
            erroConexao = true;
            System.out.println("Erro ao ligar à base de dados: " + ex.getMessage());
            return null;
        }
    }

    // ── SELECT ───────────────────────────────────────────────────────────────
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
            }
        } catch (SQLException e) {
            System.out.println("Erro ao executar INSERT: " + e.getMessage());
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
            }
        } catch (SQLException e) {
            System.out.println("Erro ao executar UPDATE/DELETE: " + e.getMessage());
        }
        return rowsAffected;
    }
}
