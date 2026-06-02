package DAL;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.security.Security;
import java.sql.*;

public class SchemaBDTest {

    private static final String URL =
            "jdbc:sqlserver://CTESPBD.DEI.ISEP.IPP.PT" +
            ";databaseName=2026_LP2_G2_FEIRA" +
            ";user=2026_LP2_G2_Feira" +
            ";password=Grupo2Ctesp" +
            ";encrypt=false;trustServerCertificate=true";

    @BeforeAll
    static void setup() {
        Security.setProperty("jdk.tls.disabledAlgorithms", "");
        System.setProperty("jdk.tls.client.protocols", "TLSv1,TLSv1.1,TLSv1.2,TLSv1.3");
    }

    @Test
    void listarTabelasEColunas() throws Exception {
        try (Connection conn = DriverManager.getConnection(URL)) {
            System.out.println("\n========== TABELAS E COLUNAS DA BD ==========");
            DatabaseMetaData meta = conn.getMetaData();

            // Lista todas as tabelas do utilizador
            ResultSet tabelas = meta.getTables(null, "dbo", "%", new String[]{"TABLE"});
            while (tabelas.next()) {
                String tabela = tabelas.getString("TABLE_NAME");
                System.out.println("\n  TABELA: [" + tabela + "]");

                // Lista colunas de cada tabela
                ResultSet colunas = meta.getColumns(null, "dbo", tabela, "%");
                while (colunas.next()) {
                    String coluna  = colunas.getString("COLUMN_NAME");
                    String tipo    = colunas.getString("TYPE_NAME");
                    int    tamanho = colunas.getInt("COLUMN_SIZE");
                    System.out.printf("    %-30s %s(%d)%n", coluna, tipo, tamanho);
                }
            }
            System.out.println("\n=============================================");
        }
    }
}
