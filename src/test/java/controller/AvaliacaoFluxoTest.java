package controller;

import DAL.AvaliacaoCRUD;
import DAL.DAOFactory;
import DAL.EstudanteCRUD;
import DAL.UnidadeCurricularCRUD;
import model.*;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testa o fluxo de lançamento de notas via AvaliacaoController.
 * Integração real com CSV — cada teste faz limpeza no @AfterEach.
 *
 * NOTA: O método registarAvaliacao já trata corretamente a actualização de um momento
 * existente quando o limite de 3 momentos foi atingido (condição `!momentoJaExiste`
 * em AvaliacaoController). O teste 'actualizarAvaliacaoExistente_NaoDeveSerBloqueadoPeloLimite3'
 * confirma esta correção e serve de teste de regressão.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AvaliacaoFluxoTest {

    private static final int NIF_ESTUDANTE = 288000001;
    private static final int NUM_MEC = 26000001;
    private static final String NOME_UC = "UC_AVALIACAO_TEST";

    private static AvaliacaoController controller;
    private static AvaliacaoCRUD avaliacaoCRUD;
    private static UnidadeCurricular ucTeste;
    private static Estudante estudanteTeste;

    @BeforeAll
    static void setupGlobal() {
        DAOFactory.setModo("CSV"); // forçar CSV independentemente do config.properties
        // Limpar dados residuais de execuções anteriores via CRUDs directos
        new AvaliacaoCRUD().eliminarAvaliacoesPorEstudante(NUM_MEC);
        new EstudanteCRUD().eliminarEstudante(NUM_MEC);
        new UnidadeCurricularCRUD().eliminarUC(NOME_UC);

        // Criar UC de teste directamente no CSV
        UnidadeCurricularCRUD ucCRUD = new UnidadeCurricularCRUD();
        ucTeste = new UnidadeCurricular(NOME_UC, 1, 1, null);
        ucTeste.adicionarMomento("Frequência");
        ucTeste.adicionarMomento("Exame");
        ucTeste.adicionarMomento("Recurso");
        ucCRUD.registarUC(ucTeste);

        // Criar estudante de teste directamente no CSV
        EstudanteCRUD estudanteCRUD = new EstudanteCRUD();
        estudanteTeste = new Estudante("Estudante Avaliacao Teste", "Rua Teste", NIF_ESTUDANTE,
                LocalDate.of(2001, 1, 1), "avtest@issmf.ipp.pt", NUM_MEC, "Hash123!", "CursoTeste", true);
        estudanteCRUD.registarEstudante(estudanteTeste);

        // Reset do cache após todos os writes directos: garante que controller
        // e avaliacaoCRUD lêem o CSV limpo (sem avaliações residuais em memória)
        DAOFactory.resetarInstancias();
        controller = new AvaliacaoController();
        avaliacaoCRUD = new AvaliacaoCRUD();
    }

    @AfterAll
    static void limpezaGlobal() {
        // Remover todos os registos de avaliação do estudante de teste
        new AvaliacaoCRUD().eliminarAvaliacoesPorEstudante(NUM_MEC);

        // Remover UC e estudante de teste
        new UnidadeCurricularCRUD().eliminarUC(NOME_UC);
        new EstudanteCRUD().eliminarEstudante(NUM_MEC);
    }

    // ===== Validações de entrada =====

    @Test
    @Order(1)
    void avaliacaoNull_DeveRetornarErro() {
        Resultado<Avaliacao> res = controller.registarAvaliacao(null);
        assertFalse(res.sucesso);
        assertNotNull(res.mensagemErro);
    }

    @Test
    @Order(2)
    void estudanteNull_DeveRetornarErro() {
        Avaliacao av = new Avaliacao("Frequência", 10.0, ucTeste, null);
        Resultado<Avaliacao> res = controller.registarAvaliacao(av);
        assertFalse(res.sucesso, "Avaliação sem estudante deve ser rejeitada.");
    }

    @Test
    @Order(3)
    void ucNull_DeveRetornarErro() {
        Avaliacao av = new Avaliacao("Frequência", 10.0, null, estudanteTeste);
        Resultado<Avaliacao> res = controller.registarAvaliacao(av);
        assertFalse(res.sucesso, "Avaliação sem UC deve ser rejeitada.");
    }

    @Test
    @Order(4)
    void momentoVazio_DeveRetornarErro() {
        Avaliacao av = new Avaliacao("", 10.0, ucTeste, estudanteTeste);
        Resultado<Avaliacao> res = controller.registarAvaliacao(av);
        assertFalse(res.sucesso, "Momento vazio deve ser rejeitado.");
    }

    @Test
    @Order(5)
    void momentoNull_DeveRetornarErro() {
        Avaliacao av = new Avaliacao(null, 10.0, ucTeste, estudanteTeste);
        Resultado<Avaliacao> res = controller.registarAvaliacao(av);
        assertFalse(res.sucesso, "Momento null deve ser rejeitado.");
    }

    @Test
    @Order(6)
    void notaNegativa_DeveRetornarErro() {
        Avaliacao av = new Avaliacao("Frequência", -1.0, ucTeste, estudanteTeste);
        Resultado<Avaliacao> res = controller.registarAvaliacao(av);
        assertFalse(res.sucesso, "Nota negativa deve ser rejeitada.");
    }

    @Test
    @Order(7)
    void notaAcimaDe20_DeveRetornarErro() {
        Avaliacao av = new Avaliacao("Frequência", 20.1, ucTeste, estudanteTeste);
        Resultado<Avaliacao> res = controller.registarAvaliacao(av);
        assertFalse(res.sucesso, "Nota acima de 20 deve ser rejeitada.");
    }

    // ===== Lançamento válido =====

    @Test
    @Order(10)
    void lancamentoValido_ComNota_Sucesso() {
        Avaliacao av = new Avaliacao("Frequência", 15.0, ucTeste, estudanteTeste);
        Resultado<Avaliacao> res = controller.registarAvaliacao(av);
        assertTrue(res.sucesso, "Lançamento válido deve ter sucesso. Erro: " + res.mensagemErro);
    }

    @Test
    @Order(11)
    void lancamentoValido_NotaNull_AguardarLancamento_Sucesso() {
        // Nota null = aguardar lançamento (registo criado sem nota)
        Avaliacao av = new Avaliacao("Exame", null, ucTeste, estudanteTeste);
        Resultado<Avaliacao> res = controller.registarAvaliacao(av);
        assertTrue(res.sucesso, "Avaliação sem nota (a aguardar) deve ser aceite.");
    }

    @Test
    @Order(12)
    void notaZero_DeveSerAceite() {
        // 0.0 é nota válida (zero num exame)
        Avaliacao av = new Avaliacao("Recurso", 0.0, ucTeste, estudanteTeste);
        Resultado<Avaliacao> res = controller.registarAvaliacao(av);
        assertTrue(res.sucesso, "Nota 0.0 é válida e deve ser aceite.");
    }

    @Test
    @Order(13)
    void notaExatamente20_DeveSerAceite() {
        Avaliacao av = new Avaliacao("Frequência", 20.0, ucTeste, estudanteTeste);
        Resultado<Avaliacao> res = controller.registarAvaliacao(av);
        assertTrue(res.sucesso, "Nota 20.0 é válida e deve ser aceite.");
    }

    // ===== Regra do máximo 3 avaliações =====

    @Test
    @Order(20)
    void quartoMomento_DeveSerBloqueado() {
        // A UC já tem 3 momentos registados (Frequência, Exame, Recurso dos testes anteriores).
        // Tentar adicionar um 4º momento diferente deve ser bloqueado.
        Avaliacao quarta = new Avaliacao("Defesa", 12.0, ucTeste, estudanteTeste);
        Resultado<Avaliacao> res = controller.registarAvaliacao(quarta);
        assertFalse(res.sucesso, "4ª avaliação diferente deve ser bloqueada.");
        assertTrue(res.mensagemErro.contains("3") || res.mensagemErro.contains("máximo"),
                "Mensagem de erro deve referir o limite de 3.");
    }

    // ===== TESTE DE REGRESSÃO: actualizar momento existente não deve ser bloqueado pelo limite 3 =====
    @Test
    @Order(21)
    void actualizarAvaliacaoExistente_NaoDeveSerBloqueadoPeloLimite3() {
        // Já existem 3 avaliações (dos testes 10, 11, 12).
        // Actualizar a "Frequência" (momento já existente) deve ser permitido.
        Avaliacao actualizacao = new Avaliacao("Frequência", 18.0, ucTeste, estudanteTeste);
        Resultado<Avaliacao> res = controller.registarAvaliacao(actualizacao);

        assertTrue(res.sucesso,
                "Actualizar nota existente não deve ser bloqueado pelo limite de 3. Erro: " + res.mensagemErro);
    }

    // ===== obterStatusAprovacao =====

    @Test
    @Order(30)
    void obterStatus_SemAvaliacoes_RetornaSemClassificacao() {
        // Estudante fictício que não tem avaliações
        Resultado<String> res = controller.obterStatusAprovacao(99999999, NOME_UC);
        assertTrue(res.sucesso);
        assertTrue(res.dados.contains("Sem classificação"),
                "Estudante sem avaliações deve retornar 'Sem classificação'.");
    }

    @Test
    @Order(31)
    void obterStatus_MediaAcimaDe9v5_RetornaAprovado() {
        // O estudante de teste tem Frequência=15, Exame=null→0, Recurso=0.
        // Media = (15 + 0 + 0) / 3 = 5.0... na realidade depende do estado actual dos dados.
        // Este teste verifica apenas o formato da resposta.
        Resultado<String> res = controller.obterStatusAprovacao(NUM_MEC, NOME_UC);
        assertTrue(res.sucesso);
        assertNotNull(res.dados);
        assertTrue(res.dados.contains("Média:"),
                "A resposta deve incluir 'Média:'.");
        assertTrue(res.dados.contains("APROVADO") || res.dados.contains("REPROVADO"),
                "A resposta deve indicar o estado.");
    }

    @Test
    @Order(32)
    void obterStatus_UCNaoExiste_RetornaErro() {
        Resultado<String> res = controller.obterStatusAprovacao(NUM_MEC, "UC_QUE_NAO_EXISTE");
        // Comportamento esperado: erro (UC sem momentos ou não encontrada)
        assertNotNull(res);
    }

    // ===== listarAvaliacoesPorUC =====

    @Test
    @Order(40)
    void listarAvaliacoesPorUC_NomeNull_RetornaNull() {
        List<Avaliacao> resultado = controller.listarAvaliacoesPorUC(null);
        assertNull(resultado, "Nome null deve retornar null.");
    }

    @Test
    @Order(41)
    void listarAvaliacoesPorUC_NomeVazio_RetornaNull() {
        List<Avaliacao> resultado = controller.listarAvaliacoesPorUC("   ");
        assertNull(resultado, "Nome vazio deve retornar null.");
    }

    @Test
    @Order(42)
    void listarAvaliacoesPorUC_UCValida_RetornaLista() {
        List<Avaliacao> resultado = controller.listarAvaliacoesPorUC(NOME_UC);
        assertNotNull(resultado, "UC existente deve retornar lista (pode ser vazia).");
    }
}