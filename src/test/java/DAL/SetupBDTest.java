package DAL;

import DAL.DB.DatabaseConnection;
import org.junit.jupiter.api.BeforeAll;

import java.security.Security;
import java.sql.*;

/**
 * Classe base com configuração partilhada para todos os testes SQL.
 * Garante compatibilidade TLS com SQL Server 2016 no Java 17.
 */
public abstract class SetupBDTest {

    // URL mantida para referência; os testes usam o pool HikariCP via getConnection()
    protected static final String URL = "jdbc:sqlserver://CTESPBD.DEI.ISEP.IPP.PT" +
            ";databaseName=2026_LP2_G2_FEIRA;encrypt=false";

    // NIFs de teste — fora do range real para não colidir com dados reais
    // NOTA: 987654322 colide com "Maria Santos" (msa, id=27) na BD — mudado para 150000001
    protected static final int  NIF_GESTOR_TESTE    = 987654321;
    protected static final int  NIF_DOCENTE_TESTE   = 150000001;
    protected static final int  NIF_ESTUDANTE_TESTE = 987654323;

    @BeforeAll
    static void configurarTLS() {
        Security.setProperty("jdk.tls.disabledAlgorithms", "");
        System.setProperty("jdk.tls.client.protocols", "TLSv1,TLSv1.1,TLSv1.2,TLSv1.3");
    }

    /** Usa o pool HikariCP (mesmo que os DAOs) em vez de DriverManager direto. */
    protected Connection getConnection() throws SQLException {
        return DatabaseConnection.getPooledConnection();
    }

    /** Executa SQL de limpeza ignorando erros (para teardown) */
    protected void executarLimpeza(String sql, Object... params) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) stmt.setObject(i + 1, params[i]);
            stmt.executeUpdate();
        } catch (Exception ignored) {}
    }

    /** Obtém o ID de um Departamento existente (ou cria um de teste) */
    protected int obterOuCriarDepartamentoTeste(Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT TOP 1 id FROM Departamento")) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("id");
        }
        // Cria departamento de teste se não existir
        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO Departamento (nome, sigla) VALUES (?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, "Dept Teste");
            stmt.setString(2, "TST");
            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            keys.next();
            return keys.getInt(1);
        }
    }

    /** Obtém o ID de um Curso existente (ou cria um de teste) */
    protected int obterOuCriarCursoTeste(Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT TOP 1 id FROM Curso")) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("id");
        }
        int deptId = obterOuCriarDepartamentoTeste(conn);
        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO Curso (nome, duracao, departamentoId, precoAnual) VALUES (?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, "Curso Teste");
            stmt.setInt(2, 3);
            stmt.setInt(3, deptId);
            stmt.setBigDecimal(4, java.math.BigDecimal.valueOf(1500));
            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            keys.next();
            return keys.getInt(1);
        }
    }
}
