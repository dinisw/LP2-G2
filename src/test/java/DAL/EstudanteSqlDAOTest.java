package DAL;

import model.Estudante;
import model.Resultado;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EstudanteSqlDAOTest extends SetupBDTest {

    private static EstudanteSqlDAO dao;
    private static final int NUMERO_MEC_TESTE = 1026999;
    private static String nomeCursoTeste;

    @BeforeAll
    static void setup() throws Exception {
        configurarTLS();
        dao = new EstudanteSqlDAO();

        // Obtém ou cria um curso de teste
        try (Connection conn = new EstudanteSqlDAOTest().getConnection()) {
            var rs = conn.prepareStatement("SELECT TOP 1 nome FROM Curso").executeQuery();
            if (rs.next()) {
                nomeCursoTeste = rs.getString("nome");
            } else {
                // Cria departamento e curso de teste se a BD estiver vazia
                EstudanteSqlDAOTest helper = new EstudanteSqlDAOTest();
                int deptId = helper.obterOuCriarDepartamentoTeste(conn);
                var ins = conn.prepareStatement(
                        "INSERT INTO Curso (nome, duracao, departamentoId, precoAnual) VALUES (?, ?, ?, ?)",
                        java.sql.Statement.RETURN_GENERATED_KEYS);
                ins.setString(1, "Curso Teste Auto");
                ins.setInt(2, 3);
                ins.setInt(3, deptId);
                ins.setBigDecimal(4, java.math.BigDecimal.valueOf(1500));
                ins.executeUpdate();
                nomeCursoTeste = "Curso Teste Auto";
            }
        }
        System.out.println("[ESTUDANTE] Curso para testes: " + nomeCursoTeste);
    }

    @BeforeEach
    void garantirAusencia() {
        executarLimpeza("DELETE FROM Avaliacao   WHERE estudanteNumeroMec = ?", NUMERO_MEC_TESTE);
        executarLimpeza("DELETE FROM Propina     WHERE numeroMecEstudante = ?", NUMERO_MEC_TESTE);
        executarLimpeza("DELETE FROM Estudante   WHERE numeroMec = ?",          NUMERO_MEC_TESTE);
    }

    @AfterAll
    static void teardown() {
        EstudanteSqlDAOTest helper = new EstudanteSqlDAOTest();
        helper.executarLimpeza("DELETE FROM Avaliacao WHERE estudanteNumeroMec = ?", NUMERO_MEC_TESTE);
        helper.executarLimpeza("DELETE FROM Propina   WHERE numeroMecEstudante = ?", NUMERO_MEC_TESTE);
        helper.executarLimpeza("DELETE FROM Estudante WHERE numeroMec = ?",          NUMERO_MEC_TESTE);
        System.out.println("[ESTUDANTE] Limpeza concluída.");
    }

    private Estudante criarEstudanteTeste() {
        return new Estudante(
                "Estudante Teste",
                "Rua dos Estudantes, 3",
                NIF_ESTUDANTE_TESTE,
                LocalDate.of(2001, 9, 10),
                "estudante.teste@issmf.ipp.pt",
                NUMERO_MEC_TESTE,
                "hashEstudante123",
                nomeCursoTeste,
                true
        );
    }

    @Test @Order(1)
    @DisplayName("Registar estudante com sucesso")
    void registarEstudante_sucesso() {
        assumirCursoDisponivel();
        Resultado<Estudante> res = dao.registarEstudante(criarEstudanteTeste());
        assertTrue(res.sucesso, "registarEstudante() deve retornar sucesso: " + res.mensagemErro);
        System.out.println("  ✅ Estudante registado (numeroMec=" + NUMERO_MEC_TESTE + ")");
    }

    @Test @Order(2)
    @DisplayName("Ler estudante por numeroMec")
    void lerEstudante_encontra() {
        assumirCursoDisponivel();
        dao.registarEstudante(criarEstudanteTeste());

        Estudante encontrado = dao.lerEstudante(NUMERO_MEC_TESTE);

        assertNotNull(encontrado, "lerEstudante() deve encontrar o estudante");
        assertEquals("Estudante Teste", encontrado.getNome());
        assertEquals(NIF_ESTUDANTE_TESTE, encontrado.getNif());
        assertEquals("hashEstudante123", encontrado.getHash());
        System.out.println("  ✅ Estudante encontrado: " + encontrado.getNome() + " | Curso: " + encontrado.getNomeCurso());
    }

    @Test @Order(3)
    @DisplayName("Procurar estudante por NIF")
    void procurarPorNif_encontra() {
        assumirCursoDisponivel();
        dao.registarEstudante(criarEstudanteTeste());

        Estudante encontrado = dao.procurarPorNif(NIF_ESTUDANTE_TESTE);

        assertNotNull(encontrado);
        assertEquals(NUMERO_MEC_TESTE, encontrado.getNumeroMec());
        System.out.println("  ✅ Estudante encontrado por NIF");
    }

    @Test @Order(4)
    @DisplayName("Atualizar senha do estudante na BD")
    void atualizarSenha_persisteNaBD() {
        assumirCursoDisponivel();
        dao.registarEstudante(criarEstudanteTeste());

        Estudante existente = dao.lerEstudante(NUMERO_MEC_TESTE);
        assertNotNull(existente);
        existente.setHash("novaHashEstudante999");

        Resultado<Estudante> res = dao.atualizarSenha(existente);
        assertTrue(res.sucesso, "atualizarSenha() deve retornar sucesso");

        Estudante verificado = dao.lerEstudante(NUMERO_MEC_TESTE);
        assertEquals("novaHashEstudante999", verificado.getHash(), "Hash deve estar atualizada na BD");
        System.out.println("  ✅ Senha do estudante atualizada na BD");
    }

    @Test @Order(5)
    @DisplayName("Listar todos os estudantes inclui o de teste")
    void getEstudantes_incluiTestudo() {
        assumirCursoDisponivel();
        dao.registarEstudante(criarEstudanteTeste());

        List<Estudante> lista = dao.getEstudantes();

        assertNotNull(lista);
        assertTrue(lista.stream().anyMatch(e -> e.getNumeroMec() == NUMERO_MEC_TESTE),
                "Lista deve conter o estudante de teste");
        System.out.println("  ✅ getEstudantes() retornou " + lista.size() + " estudante(s)");
    }

    @Test @Order(6)
    @DisplayName("Gerar numeroMec único para o ano atual")
    void gerarNumeroMecanografico_unicoEValido() {
        int gerado = dao.gerarNumeroMecanografico();
        int anoAtual = java.time.LocalDate.now().getYear() % 100;
        int prefixo = 1000000 + (anoAtual * 10000);

        assertTrue(gerado >= prefixo, "numeroMec deve ter o prefixo do ano atual");
        System.out.println("  ✅ numeroMec gerado: " + gerado);
    }

    @Test @Order(7)
    @DisplayName("Eliminar estudante")
    void eliminarEstudante_sucesso() {
        assumirCursoDisponivel();
        dao.registarEstudante(criarEstudanteTeste());

        Resultado<Estudante> res = dao.eliminarEstudante(NUMERO_MEC_TESTE);
        assertTrue(res.sucesso);
        assertNull(dao.lerEstudante(NUMERO_MEC_TESTE), "Estudante eliminado não deve ser encontrado");
        System.out.println("  ✅ Estudante eliminado com sucesso");
    }

    private void assumirCursoDisponivel() {
        org.junit.jupiter.api.Assumptions.assumeTrue(
                nomeCursoTeste != null,
                "Nenhum curso disponível na BD — teste ignorado"
        );
    }
}
