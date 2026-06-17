package DAL;

import DAL.DB.DatabaseConnection;
import org.junit.jupiter.api.Test;

import java.sql.*;

public class SchemaBDTest {

    @Test
    void listarTabelasEColunas() throws Exception {
        try (Connection conn = DatabaseConnection.getPooledConnection()) {
            System.out.println("\n========== TABELAS E COLUNAS DA BD ==========");
            DatabaseMetaData meta = conn.getMetaData();

            ResultSet tabelas = meta.getTables(null, "dbo", "%", new String[]{"TABLE"});
            while (tabelas.next()) {
                String tabela = tabelas.getString("TABLE_NAME");
                System.out.println("\n  TABELA: [" + tabela + "]");

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
