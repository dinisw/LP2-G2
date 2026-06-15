package DAL;

import common.utils.SenhaUtils;
import controller.LoginController;
import controller.RecuperarSenhaController;
import model.Gestor;
import model.Resultado;
import model.Utilizador;
import org.junit.jupiter.api.*;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de integração para o fluxo de Login e Recuperação de Senha em modo SQL.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LoginRecuperarSenhaSqlTest extends SetupBDTest {

    private static GestorSqlDAO gestorDAO;
    private static LoginController loginController;
    private static RecuperarSenhaController recuperarController;

    private static final String SENHA_ORIGINAL = "SenhaTest@123";
    private static final String SENHA_NOVA     = "NovaSenha@456";
    // Padrão gestor: [\w.-]+\.gestor@issmf\.ipp\.pt
    private static final String EMAIL_TESTE    = "logintest.gestor@issmf.ipp.pt";
    private static SenhaUtils senhaUtils;

    @BeforeAll
    static void setup() {
        configurarTLS();
        senhaUtils       = new SenhaUtils();
        gestorDAO        = new GestorSqlDAO();
        loginController  = new LoginController();
        recuperarController = new RecuperarSenhaController() {
            // Override para não precisar de email real nos testes
        };

        // Garante que modo está em SQL
        DAOFactory.setModo("SQL");
    }

    @BeforeEach
    void prepararGestorTeste() {
        // Limpa e recria o gestor de teste com senha conhecida
        executarLimpeza("DELETE FROM Gestor WHERE nif = ?", NIF_GESTOR_TESTE);

        String hash = senhaUtils.gerarHashComSalt(SENHA_ORIGINAL);
        Gestor g = new Gestor(
                0, "Gestor Login Teste", "Rua Teste, 1",
                NIF_GESTOR_TESTE, LocalDate.of(1985, 1, 1),
                EMAIL_TESTE, hash, "Diretor"
        );
        gestorDAO.registarGestor(g);
    }

    @AfterAll
    static void teardown() {
        new LoginRecuperarSenhaSqlTest().executarLimpeza(
                "DELETE FROM Gestor WHERE nif = ?", NIF_GESTOR_TESTE);
        System.out.println("[LOGIN] Limpeza concluída.");
    }

    // ── 1. Login ──────────────────────────────────────────────────────────────

    @Test @Order(1)
    @DisplayName("Login com credenciais corretas retorna utilizador")
    void login_credenciaisCorretas_retornaUtilizador() {
        Utilizador resultado = loginController.login(EMAIL_TESTE, SENHA_ORIGINAL);

        assertNotNull(resultado, "Login com credenciais corretas deve retornar utilizador");
        assertEquals(EMAIL_TESTE, resultado.getEmail());
        assertTrue(resultado instanceof Gestor);
        System.out.println("  ✅ Login bem-sucedido para: " + resultado.getEmail());
    }

    @Test @Order(2)
    @DisplayName("Login com senha errada retorna null")
    void login_senhaErrada_retornaNull() {
        Utilizador resultado = loginController.login(EMAIL_TESTE, "senhaErrada999");

        assertNull(resultado, "Login com senha errada deve retornar null");
        System.out.println("  ✅ Senha errada rejeitada corretamente");
    }

    @Test @Order(3)
    @DisplayName("Login com email inexistente retorna null")
    void login_emailInexistente_retornaNull() {
        Utilizador resultado = loginController.login("nao.existe@issmf.ipp.pt", SENHA_ORIGINAL);

        assertNull(resultado, "Email inexistente deve retornar null");
        System.out.println("  ✅ Email inexistente rejeitado corretamente");
    }

    // ── 2. Atualizar senha via DAO (fluxo recuperação) ───────────────────────

    @Test @Order(4)
    @DisplayName("Atualizar senha na BD e conseguir fazer login com nova senha")
    void recuperarSenha_atualizaNaBD_loginFunciona() {
        // Simula o que RecuperarSenhaController.atualizarSenha() faz
        Gestor gestor = gestorDAO.procurarPorEmail(EMAIL_TESTE);
        assertNotNull(gestor, "Gestor deve existir na BD");

        // Gera nova hash
        String novaHash = senhaUtils.gerarHashComSalt(SENHA_NOVA);
        gestor.setHash(novaHash);
        boolean atualizado = gestorDAO.atualizarGestor(gestor);
        assertTrue(atualizado, "Atualização da senha deve ter sucesso");

        // Login com senha antiga deve falhar
        Utilizador comSenhaAntiga = loginController.login(EMAIL_TESTE, SENHA_ORIGINAL);
        assertNull(comSenhaAntiga, "Login com senha antiga deve falhar após atualização");

        // Login com nova senha deve funcionar
        Utilizador comSenhaNova = loginController.login(EMAIL_TESTE, SENHA_NOVA);
        assertNotNull(comSenhaNova, "Login com nova senha deve funcionar");
        System.out.println("  ✅ Recuperação de senha: nova senha funciona, senha antiga rejeitada");
    }

    // ── 3. Hash coerência ────────────────────────────────────────────────────

    @Test @Order(5)
    @DisplayName("Hash com salt aleatório: verificação funciona, dois hashes são diferentes")
    void hash_consistenteComSalt() {
        // gerarHashComSalt usa salt ALEATÓRIO — dois chamadas produzem hashes distintos.
        // O que importa verificar é que cada hash é verificável com a senha correcta
        // e rejeita a senha errada.
        String hash1 = senhaUtils.gerarHashComSalt("minhasenha");
        String hash2 = senhaUtils.gerarHashComSalt("minhasenha");

        assertNotEquals(hash1, hash2, "Salt aleatório: dois hashes do mesmo input devem ser diferentes");
        assertTrue(senhaUtils.verificarSenha("minhasenha", hash1),  "hash1 deve ser verificável com a senha original");
        assertTrue(senhaUtils.verificarSenha("minhasenha", hash2),  "hash2 deve ser verificável com a senha original");
        assertFalse(senhaUtils.verificarSenha("outrasenha", hash1), "senha errada não deve validar hash1");
        assertFalse(senhaUtils.verificarSenha("outrasenha", hash2), "senha errada não deve validar hash2");
        System.out.println("  ✅ Hash aleatório verificável: " + hash1.substring(0, 20) + "...");
    }

    @Test @Order(6)
    @DisplayName("Hash da BD coincide com hash gerado pela aplicação")
    void hash_bdCoincideComAplicacao() {
        // Confirma que o hash guardado na BD é legível e verificável
        Gestor gestor = gestorDAO.procurarPorEmail(EMAIL_TESTE);
        assertNotNull(gestor);

        boolean valido = senhaUtils.verificarSenha(SENHA_ORIGINAL, gestor.getHash());
        assertTrue(valido,
                "O hash guardado na BD deve ser válido para a senha original.\n" +
                "Hash BD: " + gestor.getHash() + "\n" +
                "Hash esperado: " + senhaUtils.gerarHashComSalt(SENHA_ORIGINAL));
        System.out.println("  ✅ Hash na BD é compatível com a senha da aplicação");
    }
}
