package controller;

import DAL.*;
import model.*;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testa o fluxo de definição de momentos de avaliação por docentes.
 * Inclui: docente responsável, docente não responsável, UC inexistente.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DefinirMomentosAvaliacaoTest {

    private static final int NIF_DOC_A = 266000001;
    private static final int NIF_DOC_B = 266000002;
    private static final String SIGLA_DOC_A = "DOC_A_TEST";
    private static final String SIGLA_DOC_B = "DOC_B_TEST";
    private static final String NOME_UC = "UC_MOMENTOS_TEST";

    private static DocenteController docenteController;
    private static DocenteCRUD docenteCRUD;
    private static UnidadeCurricularCRUD ucCRUD;
    private static Docente docenteA;
    private static Docente docenteB;
    private static UnidadeCurricular ucTeste;

    @BeforeAll
    static void setup() {
        DAOFactory.setModo("CSV"); // forçar CSV independentemente do config.properties
        docenteCRUD = new DocenteCRUD();
        ucCRUD = new UnidadeCurricularCRUD();

        // Pré-limpeza de dados residuais de execuções anteriores
        ucCRUD.eliminarUC(NOME_UC);
        docenteCRUD.eliminarDocente(NIF_DOC_A);
        docenteCRUD.eliminarDocente(NIF_DOC_B);

        // Criar docente A (responsável pela UC)
        docenteA = new Docente("Docente A Momentos", "Gab A", NIF_DOC_A,
                LocalDate.of(1975, 3, 3), "doc.a@issmf.ipp.pt", "Hash123!",
                SIGLA_DOC_A, List.of(), List.of());
        docenteCRUD.registarDocente(docenteA);

        // Criar docente B (não responsável)
        docenteB = new Docente("Docente B Momentos", "Gab B", NIF_DOC_B,
                LocalDate.of(1978, 6, 6), "doc.b@issmf.ipp.pt", "Hash123!",
                SIGLA_DOC_B, List.of(), List.of());
        docenteCRUD.registarDocente(docenteB);

        // Criar UC associada ao docente A
        ucTeste = new UnidadeCurricular(NOME_UC, 1, 1, docenteA);
        ucCRUD.registarUC(ucTeste);
        ucTeste = ucCRUD.procurarPorNome(NOME_UC); // recarregar com ID atribuído

        // Reset após todas as escritas directas: o docenteController deve ver
        // os docentes recém-criados no CSV (e não um cache vazio/obsoleto)
        DAOFactory.resetarInstancias();
        docenteController = new DocenteController();
    }

    @AfterAll
    static void limpeza() {
        ucCRUD.eliminarUC(NOME_UC);
        docenteCRUD.eliminarDocente(NIF_DOC_A);
        docenteCRUD.eliminarDocente(NIF_DOC_B);
    }

    @Test
    @Order(1)
    void docenteResponsavel_PodeDefinirMomentos_Sucesso() {
        List<String> momentos = List.of("Frequência", "Exame");
        Resultado<UnidadeCurricular> res = docenteController.definirMomentosAvaliacao(
                SIGLA_DOC_A, ucTeste.getId(), momentos);

        assertTrue(res.sucesso,
                "Docente responsável deve poder definir momentos. Erro: " + res.mensagemErro);
        assertEquals(2, res.dados.getMomentosAvaliacao().size(),
                "Devem existir 2 momentos após a definição.");
        assertTrue(res.dados.getMomentosAvaliacao().contains("Frequência"));
        assertTrue(res.dados.getMomentosAvaliacao().contains("Exame"));
    }

    @Test
    @Order(2)
    void docenteNaoResponsavel_DeveSerBloqueado() {
        List<String> momentos = List.of("Tentativa");
        Resultado<UnidadeCurricular> res = docenteController.definirMomentosAvaliacao(
                SIGLA_DOC_B, ucTeste.getId(), momentos);

        assertFalse(res.sucesso,
                "Docente não responsável não deve poder definir momentos.");
        assertTrue(res.mensagemErro.contains("Acesso") || res.mensagemErro.contains("responsável"),
                "Mensagem deve indicar acesso negado.");
    }

    @Test
    @Order(3)
    void ucNaoExiste_DeveRetornarErro() {
        List<String> momentos = List.of("Qualquer");
        Resultado<UnidadeCurricular> res = docenteController.definirMomentosAvaliacao(
                SIGLA_DOC_A, 999999, momentos);

        assertFalse(res.sucesso, "UC inexistente deve retornar erro.");
        assertTrue(res.mensagemErro.contains("encontrada") || res.mensagemErro.contains("UC"),
                "Mensagem deve indicar que a UC não foi encontrada.");
    }

    @Test
    @Order(4)
    void definirMomentos_SubstituiListaAnterior() {
        // Primeiro define 2 momentos
        docenteController.definirMomentosAvaliacao(SIGLA_DOC_A, ucTeste.getId(),
                List.of("T1", "T2"));

        // Depois substitui por 1 momento
        Resultado<UnidadeCurricular> res = docenteController.definirMomentosAvaliacao(
                SIGLA_DOC_A, ucTeste.getId(), List.of("Exame Final"));

        assertTrue(res.sucesso);
        assertEquals(1, res.dados.getMomentosAvaliacao().size(),
                "A nova lista deve substituir completamente a anterior.");
        assertEquals("Exame Final", res.dados.getMomentosAvaliacao().get(0));
    }

    @Test
    @Order(5)
    void ucSemMomentos_AposDefinicao_PodeSerUsada() {
        // Carregar do CSV para ver o estado persistido pelos testes anteriores
        UnidadeCurricular ucActualizada = new UnidadeCurricularCRUD().procurarPorNome(NOME_UC);
        assertNotNull(ucActualizada, "UC deve existir.");
        assertFalse(ucActualizada.getMomentosAvaliacao().isEmpty(),
                "Após definição de momentos, a UC não deve ter lista vazia.");
    }
}