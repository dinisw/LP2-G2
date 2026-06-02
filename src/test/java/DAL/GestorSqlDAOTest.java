package DAL;

import model.Gestor;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GestorSqlDAOTest extends SetupBDTest {

    private static GestorSqlDAO dao;

    private static final Gestor GESTOR_TESTE = new Gestor(
            0,
            "Gestor Teste",
            "Rua dos Testes, 1",
            NIF_GESTOR_TESTE,
            LocalDate.of(1985, 6, 15),
            "gestor.teste@issmf.ipp.pt",
            "hashTeste123",
            "Diretor de Teste"
    );

    @BeforeAll
    static void setup() {
        configurarTLS();
        dao = new GestorSqlDAO();
    }

    @BeforeEach
    void garantirAusencia() {
        executarLimpeza("DELETE FROM Gestor WHERE nif = ?", NIF_GESTOR_TESTE);
    }

    @AfterAll
    static void teardown() {
        new GestorSqlDAOTest().executarLimpeza("DELETE FROM Gestor WHERE nif = ?", NIF_GESTOR_TESTE);
        System.out.println("[GESTOR] Limpeza concluída.");
    }

    @Test @Order(1)
    @DisplayName("Registar gestor com sucesso")
    void registarGestor_sucesso() {
        boolean resultado = dao.registarGestor(GESTOR_TESTE);
        assertTrue(resultado, "registarGestor() deve retornar true");
        System.out.println("  ✅ Gestor registado. ID gerado: " + GESTOR_TESTE.getId());
    }

    @Test @Order(2)
    @DisplayName("Encontrar gestor por email")
    void procurarPorEmail_encontra() {
        dao.registarGestor(GESTOR_TESTE);
        Gestor encontrado = dao.procurarPorEmail(GESTOR_TESTE.getEmail());

        assertNotNull(encontrado, "Deve encontrar o gestor pelo email");
        assertEquals(GESTOR_TESTE.getNome(), encontrado.getNome());
        assertEquals(GESTOR_TESTE.getNif(),  encontrado.getNif());
        assertEquals(GESTOR_TESTE.getHash(), encontrado.getHash());
        System.out.println("  ✅ Gestor encontrado por email: " + encontrado.getEmail());
    }

    @Test @Order(3)
    @DisplayName("Email inexistente retorna null")
    void procurarPorEmail_naoEncontra() {
        Gestor resultado = dao.procurarPorEmail("nao.existe@issmf.ipp.pt");
        assertNull(resultado);
        System.out.println("  ✅ Email inexistente retornou null");
    }

    @Test @Order(4)
    @DisplayName("Encontrar gestor por NIF")
    void procurarPorNif_encontra() {
        dao.registarGestor(GESTOR_TESTE);
        Gestor encontrado = dao.procurarPorNif(NIF_GESTOR_TESTE);

        assertNotNull(encontrado);
        assertEquals("Gestor Teste", encontrado.getNome());
        System.out.println("  ✅ Gestor encontrado por NIF: " + encontrado.getNif());
    }

    @Test @Order(5)
    @DisplayName("Atualizar hash/senha do gestor na BD")
    void atualizarGestor_senha() {
        dao.registarGestor(GESTOR_TESTE);
        Gestor existente = dao.procurarPorNif(NIF_GESTOR_TESTE);
        assertNotNull(existente);

        existente.setHash("novaHashAtualizada456");
        boolean atualizado = dao.atualizarGestor(existente);
        assertTrue(atualizado, "atualizarGestor() deve retornar true");

        Gestor verificado = dao.procurarPorNif(NIF_GESTOR_TESTE);
        assertEquals("novaHashAtualizada456", verificado.getHash(), "Hash deve estar atualizada na BD");
        System.out.println("  ✅ Senha atualizada na BD com sucesso");
    }

    @Test @Order(6)
    @DisplayName("Listar todos os gestores")
    void getGestores_retornaLista() {
        dao.registarGestor(GESTOR_TESTE);
        List<Gestor> lista = dao.getGestores();

        assertNotNull(lista);
        assertFalse(lista.isEmpty());
        assertTrue(lista.stream().anyMatch(g -> g.getNif() == NIF_GESTOR_TESTE));
        System.out.println("  ✅ getGestores() retornou " + lista.size() + " gestor(es)");
    }

    @Test @Order(7)
    @DisplayName("Eliminar gestor")
    void eliminarGestor_sucesso() {
        dao.registarGestor(GESTOR_TESTE);
        boolean eliminado = dao.eliminarGestor(NIF_GESTOR_TESTE);

        assertTrue(eliminado);
        assertNull(dao.procurarPorNif(NIF_GESTOR_TESTE), "Gestor eliminado não deve ser encontrado");
        System.out.println("  ✅ Gestor eliminado com sucesso");
    }
}
