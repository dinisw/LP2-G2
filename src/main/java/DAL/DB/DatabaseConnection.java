package DAL.DB;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.cdimascio.dotenv.Dotenv;

import java.security.Security;
import java.sql.*;
import java.util.ArrayList;

/**
 * Gestão de ligações à base de dados via HikariCP connection pool.
 *
 * Antes: cada query abria uma nova ligação TCP ao SQL Server → lento e dispendioso.
 * Agora: o pool mantém 2-10 ligações reutilizáveis → muito mais rápido.
 *
 * A configuração (dotenv) é lida UMA ÚNICA VEZ na primeira instanciação.
 */
public class DatabaseConnection {

    // ── Pool estático – partilhado por toda a JVM ─────────────────────────────
    private static volatile HikariDataSource pool;
    private static volatile boolean inicializado = false;

    private static boolean erroConexao = false;
    public static boolean houveErroConexao() { return erroConexao; }

    public DatabaseConnection() {
        // Double-checked locking: pool criado uma única vez
        if (!inicializado) {
            synchronized (DatabaseConnection.class) {
                if (!inicializado) {
                    inicializarPool();
                    inicializado = true;
                }
            }
        }
    }

    private static void inicializarPool() {
        // SQL Server 2016 usa TLS 1.0/1.1; Java 11+ desativa-os por defeito.
        Security.setProperty("jdk.tls.disabledAlgorithms",
                "SSLv3, RC4, DES, MD5withRSA, DH keySize < 1024, EC keySize < 224, 3DES_EDE_CBC, anon, NULL");
        Security.setProperty("jdk.certpath.disabledAlgorithms",
                "MD2, MD5, SHA1 jdkCA & usage TLSServer, RSA keySize < 1024, DSA keySize < 1024, EC keySize < 224");

        try {
            Dotenv dotenv = Dotenv.configure()
                    .directory("src/main/resources")
                    .ignoreIfMalformed()
                    .ignoreIfMissing()
                    .load();

            String server   = nvl(dotenv.get("DB_SERVER"));
            String database = nvl(dotenv.get("DB_DATABASE"));
            String user     = nvl(dotenv.get("DB_USER"));
            String password = nvl(dotenv.get("DB_PASSWORD"));

            HikariConfig cfg = new HikariConfig();
            cfg.setJdbcUrl("jdbc:sqlserver://" + server
                    + ";databaseName=" + database + ";encrypt=false;trustServerCertificate=true");
            cfg.setUsername(user);
            cfg.setPassword(password);

            cfg.setMaximumPoolSize(10);   // máximo de ligações em paralelo
            cfg.setMinimumIdle(2);        // mínimo sempre prontas
            cfg.setConnectionTimeout(30_000);   // 30 s para obter ligação do pool
            cfg.setIdleTimeout(600_000);        // 10 min inactiva → devolve à BD
            cfg.setMaxLifetime(1_800_000);      // 30 min máximo de vida de uma ligação
            cfg.setAutoCommit(true);            // cada operação independente por defeito

            pool = new HikariDataSource(cfg);

        } catch (Exception e) {
            System.err.println("Aviso: Não foi possível criar o pool de ligações: " + e.getMessage());
            pool = null;
        }
    }

    private static String nvl(String s) { return s != null ? s : ""; }

    // ── Interface funcional para transações ──────────────────────────────────
    @FunctionalInterface
    public interface TransactionConsumer {
        void execute(Connection conn) throws SQLException;
    }

    private Connection openConnection() {
        if (pool == null) {
            erroConexao = true;
            return null;
        }
        try {
            Connection conn = pool.getConnection();
            erroConexao = false;
            return conn;
        } catch (Exception ex) {
            erroConexao = true;
            System.out.println("Erro ao obter ligação do pool: " + ex.getMessage());
            return null;
        }
    }

    /**
     * Devolve uma ligação do pool para uso em testes.
     * O chamador é responsável por fechar a ligação (devolve ao pool).
     */
    public static Connection getPooledConnection() throws SQLException {
        if (!inicializado) new DatabaseConnection();
        if (pool == null) throw new SQLException("Pool de ligações não disponível.");
        return pool.getConnection();
    }

    /** Verifica se uma tabela existe na BD actual (SQL Server). */
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
        try (conn; PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (params != null) {
                for (int i = 0; i < params.length; i++) stmt.setObject(i + 1, params[i]);
            }
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) result = keys.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("Erro ao executar INSERT: " + e.getMessage());
        }
        return result;
    }

    // ── UPDATE / DELETE / INSERT simples ─────────────────────────────────────
    public int execute(String sql, Object... params) {
        int rowsAffected = 0;
        Connection conn = openConnection();
        if (conn == null) return rowsAffected;
        try (conn; PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (params != null) {
                for (int i = 0; i < params.length; i++) stmt.setObject(i + 1, params[i]);
            }
            rowsAffected = stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erro ao executar UPDATE/DELETE: " + e.getMessage());
        }
        return rowsAffected;
    }

    /**
     * Executa múltiplas operações numa única transação ACID.
     * Se qualquer operação falhar, todas são revertidas (rollback).
     *
     * Uso: db.runTransaction(conn -> { INSERT…; INSERT…; });
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
            } finally {
                conn.setAutoCommit(true); // repõe antes de devolver ao pool
            }
        } catch (SQLException e) {
            System.out.println("Erro de ligação: " + e.getMessage());
            return false;
        }
    }
}
