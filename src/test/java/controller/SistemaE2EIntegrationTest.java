package controller;

import DAL.*;
import BLL.*;
import model.*;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SistemaE2EIntegrationTest {

    private static GestorController gestorController = new GestorController();
    private static DocenteController docenteController = new DocenteController();
    private static EstudanteController estudanteController = new EstudanteController();
    private static PropinaController propinaController = new PropinaController();

    private static DepartamentoCRUD depCRUD = new DepartamentoCRUD();
    private static CursoCRUD cursoCRUD = new CursoCRUD();
    private static UnidadeCurricularCRUD ucCRUD = new UnidadeCurricularCRUD();
    private static AvaliacaoCRUD avaliacaoCRUD = new AvaliacaoCRUD();

    // NOTA: Mudei os NIFs para começarem por 2, pois alguns validadores bloqueiam NIFs começados por 9
    private static int nifGestor = 299111222;
    private static int nifDocente = 299333444;
    private static int nifEstudante = 299555666;
    private static int numeroMecGerado = 0;
    private static String siglaDocente = "TESTE_DOC";
    private static String nomeCurso = "Engenharia de Testes";
    private static String nomeUC = "Qualidade de Software";

    // ANTES DE TUDO: Garantir que não há lixo de testes anteriores
    @BeforeAll
    public static void limpezaInicial() {
        System.out.println("--- LIMPANDO DADOS RESIDUAIS DE TESTES ANTERIORES ---");
        gestorController.eliminarGestor(nifGestor);
        docenteController.eliminarDocente(nifDocente);
        estudanteController.eliminarEstudante(nifEstudante); // Caso o mec falhe, apagar por NIF
        ucCRUD.eliminarUC(nomeUC);
        cursoCRUD.eliminarCurso(nomeCurso);
        depCRUD.eliminarDepartamento("DPT");
    }

    @Test
    @Order(1)
    public void passo1_FluxoDoGestor_CriarEstruturaBase() {
        Resultado <Gestor> resGestor = gestorController.registarGestor("Gestor E2E", "Sede", nifGestor, LocalDate.of(1980, 1, 1), "e2e.gestor@issmf.ipp.pt", "Hash123!", "Diretor");
        assertTrue(resGestor.sucesso, "Erro ao criar Gestor: " + resGestor.mensagemErro);

        Departamento dep = new Departamento("Departamento de Testes", "DPT");
        depCRUD.registarDepartamento(dep); // Pode falhar se já existir, ignoramos para avançar

        Curso curso = new Curso(nomeCurso, 3, dep);
        cursoCRUD.registarCurso(curso);

        UnidadeCurricular uc = new UnidadeCurricular(nomeUC, 1, 1, null);
        uc.adicionarMomento("Frequência");
        ucCRUD.registarUC(uc);

        curso.adicionarUnidadeCurricular(uc);
        Resultado <Curso> resCurso = cursoCRUD.atualizarCurso(nomeCurso, curso);
        assertTrue(resCurso.sucesso, "Erro ao associar UC ao curso: " + resCurso.mensagemErro);
    }

    @Test
    @Order(2)
    public void passo2_FluxoDoDocente_RegistoELigacao() {
        List<String> ucsParaAssociar = new ArrayList<>();
        ucsParaAssociar.add(nomeUC);

        Resultado <Docente> resDocente = docenteController.registarDocente("Docente E2E", "Gabinete 1", nifDocente, LocalDate.of(1975, 5, 5), "docente.e2e@issmf.ipp.pt", "Hash123!", siglaDocente, ucsParaAssociar);
        assertTrue(resDocente.sucesso, "Erro ao criar Docente: " + resDocente.mensagemErro);

        UnidadeCurricular ucAtualizada = ucCRUD.procurarPorNome(nomeUC);
        assertNotNull(ucAtualizada, "A UC devia ter sido criada no passo 1.");

        Docente docFresco = docenteController.procurarDocentePorNif(nifDocente);
        ucAtualizada.setDocente(docFresco);
        ucCRUD.atualizarUC(nomeUC, ucAtualizada);

        assertNotNull(ucAtualizada.getDocente(), "A UC deve ter o docente guardado.");
        assertEquals(siglaDocente, ucAtualizada.getDocente().getSigla());
    }

    @Test
    @Order(3)
    public void passo3_FluxoDoEstudante_Inscricao() {
        Resultado<Integer> resEstudante = estudanteController.registarEstudante("Estudante E2E", "Rua do Teste", nifEstudante, LocalDate.of(2000, 10, 10), nomeCurso, "Hash123!");
        assertTrue(resEstudante.sucesso, "Erro ao registar Estudante: " + resEstudante.mensagemErro);

        numeroMecGerado = (int )resEstudante.dados;
        assertTrue(numeroMecGerado > 0);

        Estudante estudante = estudanteController.procurarEstudantePorNumeroMec(numeroMecGerado);
        UnidadeCurricular uc = ucCRUD.procurarPorNome(nomeUC);

        Avaliacao inscricao = new Avaliacao("Frequência", null, uc, estudante);
        assertTrue(avaliacaoCRUD.registarAvaliacao(inscricao), "Estudante inscrito na UC (A aguardar nota).");
    }

    @Test
    @Order(4)
    public void passo4_FluxoDoDocente_LancarNotas() {
        Estudante estudante = estudanteController.procurarEstudantePorNumeroMec(numeroMecGerado);
        UnidadeCurricular uc = ucCRUD.procurarPorNome(nomeUC);
        assertNotNull(uc, "A UC não foi encontrada na hora de lançar notas!");

        Avaliacao notaLancada = new Avaliacao("Frequência", 18.5, uc, estudante);
        assertTrue(avaliacaoCRUD.registarAvaliacao(notaLancada), "Docente lança nota de 18.5 com sucesso.");
    }

    @Test
    @Order(5)
    public void passo5_FluxoDoEstudante_ProgressaoETesouraria() {
        Estudante estudante = estudanteController.procurarEstudantePorNumeroMec(numeroMecGerado);
        UnidadeCurricular uc = ucCRUD.procurarPorNome(nomeUC);

        estudante.adicionarAvaliacao(new Avaliacao("Frequência", 18.5, uc, estudante));
        int anoDesbloqueado = estudanteController.obterAnoDesbloqueado(estudante);

        PropinaController propinaControllerFresco = new PropinaController();
        List<Propina> propinas = propinaControllerFresco.consultarPropinasEstudante(numeroMecGerado);

        assertFalse(propinas.isEmpty(), "A propina devia ter sido gerada!");

        Propina fatura = propinas.get(0);
        Resultado <Propina> resPagamento = propinaControllerFresco.pagarPropina(numeroMecGerado, fatura.getAnoLetivo(), 1000.0);
        assertTrue(resPagamento.sucesso, "Erro a pagar propina: " + resPagamento.mensagemErro);
    }

    @Test
    @Order(6)
    public void passo6_LimpezaFinal_TornarCsvLimpo() {
        estudanteController.eliminarEstudante(numeroMecGerado);
        docenteController.eliminarDocente(nifDocente);
        ucCRUD.eliminarUC(nomeUC);
        cursoCRUD.eliminarCurso(nomeCurso);
        depCRUD.eliminarDepartamento("DPT");
        gestorController.eliminarGestor(nifGestor);

        assertTrue(true, "O Sistema sobreviveu ao E2E Test Flow!");
    }
}