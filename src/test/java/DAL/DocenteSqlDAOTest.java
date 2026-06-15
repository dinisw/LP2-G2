package DAL;

import model.Docente;
import model.Resultado;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DocenteSqlDAOTest extends SetupBDTest {

    private static DocenteSqlDAO dao;

    private static final Docente DOCENTE_TESTE = new Docente(
            "Docente Teste",
            "Rua dos Docentes, 2",
            NIF_DOCENTE_TESTE,
            LocalDate.of(1978, 3, 20),
            "docente.teste@issmf.ipp.pt",
            "hashDocente123",
            "TST",
            new ArrayList<>(),
            new ArrayList<>()
    );

    @BeforeAll
    static void setup() {
        configurarTLS();
        dao = new DocenteSqlDAO();
    }

    @BeforeEach
    void garantirAusencia() {
        eliminarDocenteComCascade(NIF_DOCENTE_TESTE);
    }

    @AfterAll
    static void teardown() {
        new DocenteSqlDAOTest().eliminarDocenteComCascade(NIF_DOCENTE_TESTE);
        System.out.println("[DOCENTE] Limpeza concluída.");
    }

    /** Remove o docente e todas as dependências FK em cascata (UCs, momentos, avaliações). */
    private void eliminarDocenteComCascade(int nif) {
        // 1. Avaliações que referenciam UCs deste docente
        executarLimpeza(
            "DELETE FROM Avaliacao WHERE ucId IN " +
            "(SELECT id FROM UnidadeCurricular WHERE docenteId = " +
            "(SELECT id FROM Docente WHERE nif = ?))", nif);
        // 2. Momentos das UCs deste docente
        executarLimpeza(
            "DELETE FROM UnidadeCurricularMomento WHERE id IN " +
            "(SELECT id FROM UnidadeCurricular WHERE docenteId = " +
            "(SELECT id FROM Docente WHERE nif = ?))", nif);
        // 3. Associações Curso↔UC das UCs deste docente
        executarLimpeza(
            "DELETE FROM CursoUnidadeCurricular WHERE UcId IN " +
            "(SELECT id FROM UnidadeCurricular WHERE docenteId = " +
            "(SELECT id FROM Docente WHERE nif = ?))", nif);
        // 4. As próprias UCs
        executarLimpeza(
            "DELETE FROM UnidadeCurricular WHERE docenteId = " +
            "(SELECT id FROM Docente WHERE nif = ?)", nif);
        // 5. O docente
        executarLimpeza("DELETE FROM Docente WHERE nif = ?", nif);
    }

    @Test @Order(1)
    @DisplayName("Registar docente com sucesso")
    void registarDocente_sucesso() {
        Resultado<Docente> res = dao.registarDocente(DOCENTE_TESTE);
        assertTrue(res.sucesso, "registarDocente() deve retornar sucesso: " + res.mensagemErro);
        System.out.println("  ✅ Docente registado com sucesso");
    }

    @Test @Order(2)
    @DisplayName("Não duplicar docente com mesmo NIF")
    void registarDocente_nifDuplicado() {
        dao.registarDocente(DOCENTE_TESTE);
        Resultado<Docente> res = dao.registarDocente(DOCENTE_TESTE);
        assertFalse(res.sucesso, "Segundo registo com mesmo NIF deve falhar");
        System.out.println("  ✅ Duplicado rejeitado: " + res.mensagemErro);
    }

    @Test @Order(3)
    @DisplayName("Encontrar docente por NIF")
    void procurarPorNif_encontra() {
        dao.registarDocente(DOCENTE_TESTE);
        Docente encontrado = dao.procurarPorNif(NIF_DOCENTE_TESTE);

        assertNotNull(encontrado);
        assertEquals("Docente Teste", encontrado.getNome());
        assertEquals("TST", encontrado.getSigla());
        assertEquals(DOCENTE_TESTE.getHash(), encontrado.getHash());
        System.out.println("  ✅ Docente encontrado por NIF: " + encontrado.getNif());
    }

    @Test @Order(4)
    @DisplayName("Encontrar docente por sigla")
    void procurarPorSigla_encontra() {
        dao.registarDocente(DOCENTE_TESTE);
        Docente encontrado = dao.procurarPorSigla("TST");

        assertNotNull(encontrado);
        assertEquals(NIF_DOCENTE_TESTE, encontrado.getNif());
        System.out.println("  ✅ Docente encontrado por sigla: " + encontrado.getSigla());
    }

    @Test @Order(5)
    @DisplayName("Atualizar hash/senha do docente na BD")
    void atualizarDocente_senha() {
        dao.registarDocente(DOCENTE_TESTE);
        Docente existente = dao.procurarPorNif(NIF_DOCENTE_TESTE);
        assertNotNull(existente);

        existente.setHash("novaHashDocente789");
        Resultado<Docente> res = dao.atualizarDocente(existente);
        assertTrue(res.sucesso, "atualizarDocente() deve retornar sucesso");

        Docente verificado = dao.procurarPorNif(NIF_DOCENTE_TESTE);
        assertEquals("novaHashDocente789", verificado.getHash(), "Hash deve estar atualizada na BD");
        System.out.println("  ✅ Senha do docente atualizada na BD");
    }

    @Test @Order(6)
    @DisplayName("Listar todos os docentes")
    void getDocentes_retornaLista() {
        dao.registarDocente(DOCENTE_TESTE);
        List<Docente> lista = dao.getDocentes();

        assertNotNull(lista);
        assertFalse(lista.isEmpty());
        assertTrue(lista.stream().anyMatch(d -> d.getNif() == NIF_DOCENTE_TESTE));
        System.out.println("  ✅ getDocentes() retornou " + lista.size() + " docente(s)");
    }

    @Test @Order(7)
    @DisplayName("Eliminar docente")
    void eliminarDocente_sucesso() {
        dao.registarDocente(DOCENTE_TESTE);
        Resultado<Docente> res = dao.eliminarDocente(NIF_DOCENTE_TESTE);

        assertTrue(res.sucesso);
        assertNull(dao.procurarPorNif(NIF_DOCENTE_TESTE), "Docente eliminado não deve ser encontrado");
        System.out.println("  ✅ Docente eliminado com sucesso");
    }
}
