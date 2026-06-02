package DAL;

import model.UnidadeCurricular;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UnidadeCurricularSqlDAOTest extends SetupBDTest {

    private static UnidadeCurricularSqlDAO dao;

    @BeforeAll
    static void setup() {
        configurarTLS();
        DAOFactory.setModo("SQL");
        dao = new UnidadeCurricularSqlDAO();
    }

    @Test
    @DisplayName("Verificar dados raw na BD — tabelas UnidadeCurricular e Docente")
    void verificarDadosRawNaBD() throws Exception {
        try (Connection conn = getConnection()) {
            System.out.println("\n=== RAW: UnidadeCurricular ===");
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM UnidadeCurricular");
            int count = 0;
            while (rs.next()) {
                count++;
                System.out.printf("  id=%-3d nome=%-35s anoCurricular=%d semestre=%d docenteId=%d ects=%d%n",
                        rs.getInt("id"), rs.getString("nome"),
                        rs.getInt("anoCurricular"), rs.getInt("semestre"),
                        rs.getInt("docenteId"), rs.getInt("ects"));
            }
            System.out.println("  Total: " + count + " UCs");

            System.out.println("\n=== RAW: Docente ===");
            ResultSet rs2 = conn.createStatement().executeQuery("SELECT id, sigla, nome FROM Docente");
            while (rs2.next()) {
                System.out.printf("  id=%-3d sigla=%-5s nome=%s%n",
                        rs2.getInt("id"), rs2.getString("sigla"), rs2.getString("nome"));
            }
        }
    }

    @Test
    @DisplayName("getUnidadeCurriculars() deve retornar lista não vazia")
    void getUnidadeCurriculars_retornaLista() {
        List<UnidadeCurricular> lista = dao.getUnidadeCurriculars();

        System.out.println("\n=== DAO: getUnidadeCurriculars() ===");
        if (lista.isEmpty()) {
            System.out.println("  ❌ Lista VAZIA");
        } else {
            for (UnidadeCurricular uc : lista) {
                System.out.printf("  id=%-3d nome=%-35s docente=%s momentos=%s%n",
                        uc.getId(), uc.getNome(),
                        uc.getDocente() != null ? uc.getDocente().getSigla() : "NULL",
                        uc.getMomentosAvaliacao());
            }
        }

        assertFalse(lista.isEmpty(), "Lista de UCs não deve estar vazia");
        System.out.println("  Total: " + lista.size() + " UCs carregadas");
    }

    @Test
    @DisplayName("procurarPorId() deve encontrar UC com id=1")
    void procurarPorId_encontra() {
        UnidadeCurricular uc = dao.procurarPorId(1);

        System.out.println("\n=== procurarPorId(1) ===");
        if (uc == null) {
            System.out.println("  ❌ Retornou NULL");
        } else {
            System.out.println("  ✅ nome=" + uc.getNome() +
                    " | docente=" + (uc.getDocente() != null ? uc.getDocente().getSigla() : "NULL") +
                    " | momentos=" + uc.getMomentosAvaliacao());
        }

        assertNotNull(uc, "procurarPorId(1) não deve retornar null");
    }
}
