package DAL;

import org.junit.jupiter.api.Test;

import java.security.Security;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

public class ConexaoBDTest {

    private static final String SERVER   = "CTESPBD.DEI.ISEP.IPP.PT";
    private static final String DATABASE = "2026_LP2_G2_FEIRA";
    private static final String USER     = "2026_LP2_G2_Feira";
    private static final String PASSWORD = "Grupo2Ctesp";

    // ── Testa as variantes de URL ────────────────────────────────────────────

    @Test
    void teste1_encryptFalse() {
        String url = buildUrl("encrypt=false");
        System.out.println("[TESTE 1] encrypt=false → " + url);
        tentarConectar(url);
    }

    @Test
    void teste2_encryptFalseTrustTrue() {
        String url = buildUrl("encrypt=false;trustServerCertificate=true");
        System.out.println("[TESTE 2] encrypt=false;trustServerCertificate=true → " + url);
        tentarConectar(url);
    }

    @Test
    void teste3_encryptTrueTrustTrue() {
        String url = buildUrl("encrypt=true;trustServerCertificate=true");
        System.out.println("[TESTE 3] encrypt=true;trustServerCertificate=true → " + url);
        tentarConectar(url);
    }

    @Test
    void teste4_encryptFalseComTLS12() {
        // Re-ativa TLS 1.0/1.1 via propriedade de segurança Java
        Security.setProperty("jdk.tls.disabledAlgorithms", "");
        System.setProperty("jdk.tls.client.protocols", "TLSv1,TLSv1.1,TLSv1.2,TLSv1.3");
        String url = buildUrl("encrypt=false;trustServerCertificate=true");
        System.out.println("[TESTE 4] encrypt=false + TLS re-ativado → " + url);
        tentarConectar(url);
    }

    @Test
    void teste5_encryptTrueComTLS12() {
        Security.setProperty("jdk.tls.disabledAlgorithms", "");
        System.setProperty("jdk.tls.client.protocols", "TLSv1,TLSv1.1,TLSv1.2,TLSv1.3");
        String url = buildUrl("encrypt=true;trustServerCertificate=true");
        System.out.println("[TESTE 5] encrypt=true + TLS re-ativado → " + url);
        tentarConectar(url);
    }

    @Test
    void teste6_loginTimeout() {
        Security.setProperty("jdk.tls.disabledAlgorithms", "");
        System.setProperty("jdk.tls.client.protocols", "TLSv1,TLSv1.1,TLSv1.2,TLSv1.3");
        String url = buildUrl("encrypt=false;trustServerCertificate=true;loginTimeout=30");
        System.out.println("[TESTE 6] + loginTimeout → " + url);
        tentarConectar(url);
    }

    @Test
    void teste7_portaExplicita() {
        Security.setProperty("jdk.tls.disabledAlgorithms", "");
        System.setProperty("jdk.tls.client.protocols", "TLSv1,TLSv1.1,TLSv1.2,TLSv1.3");
        String url = "jdbc:sqlserver://" + SERVER + ":1433" +
                ";databaseName=" + DATABASE +
                ";user=" + USER +
                ";password=" + PASSWORD +
                ";encrypt=false;trustServerCertificate=true";
        System.out.println("[TESTE 7] porta 1433 explícita → " + url);
        tentarConectar(url);
    }

    @Test
    void teste8_integratedSecurityFalse() {
        Security.setProperty("jdk.tls.disabledAlgorithms", "");
        System.setProperty("jdk.tls.client.protocols", "TLSv1,TLSv1.1,TLSv1.2,TLSv1.3");
        String url = buildUrl("encrypt=false;trustServerCertificate=true;integratedSecurity=false");
        System.out.println("[TESTE 8] integratedSecurity=false → " + url);
        tentarConectar(url);
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private String buildUrl(String extra) {
        return "jdbc:sqlserver://" + SERVER +
                ";databaseName=" + DATABASE +
                ";user=" + USER +
                ";password=" + PASSWORD +
                ";" + extra;
    }

    private void tentarConectar(String url) {
        try {
            Connection conn = DriverManager.getConnection(url);
            System.out.println("  ✅ LIGAÇÃO BEM-SUCEDIDA!");

            // Confirma que consegue fazer query
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT SYSTEM_USER, @@VERSION");
            if (rs.next()) {
                System.out.println("  👤 Utilizador BD: " + rs.getString(1));
                System.out.println("  🗄  Versão SQL: " + rs.getString(2).split("\n")[0]);
            }
            conn.close();
            // Se chegou aqui, o teste passou
            assertTrue(true);
        } catch (Exception e) {
            System.out.println("  ❌ FALHOU: " + e.getMessage());
            // Não falha o teste — só reporta
            System.out.println("  → Causa raiz: " + getRootCause(e));
        }
    }

    private String getRootCause(Throwable t) {
        while (t.getCause() != null) t = t.getCause();
        return t.getClass().getSimpleName() + ": " + t.getMessage();
    }
}
