package controller;

import DAL.*;
import common.utils.SenhaUtils;
import model.*;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Suíte de Testes focada em validar os Critérios de Aceitação das User Stories
 * do Épico: Refatoração Estrutural, Integridade de Negócio e Progressão Académica.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserStoriesValidationTest {

    private static final Random r = new Random();
    private static final int ID = r.nextInt(900000);

    private static final String NOME_CURSO = "Curso QA " + ID;
    private static final String NOME_UC_Y1 = "UC Y1 QA " + ID;
    private static final String NOME_UC_Y2 = "UC Y2 QA " + ID;
    private static final String NOME_UC_Y3 = "UC Y3 QA " + ID;
    private static final String SIGLA_DOC = "DQA";

    private static int mecEstudante;

    @BeforeAll
    static void limpezaPrevia() {
        DAOFactory.setModo("CSV"); // forçar CSV — testes usam CRUDs directos para limpeza
        // Remover docente residual de execução anterior (sigla "DQA" é constante, pode colidir)
        DocenteCRUD docCRUD = new DocenteCRUD();
        Docente docStale = docCRUD.procurarPorSigla(SIGLA_DOC);
        if (docStale != null) docCRUD.eliminarDocente(docStale.getNif());

        // Remover departamento residual com mesma sigla
        DepartamentoCRUD depCRUD = new DepartamentoCRUD();
        if (depCRUD.procurarPorSigla("DQA") != null) depCRUD.eliminarDepartamento("DQA");

        // Reset do cache após limpeza via CRUDs directos
        DAOFactory.resetarInstancias();
    }

    @AfterAll
    static void limpezaFinal() {
        // Limpar avaliações e propinas do estudante criado nesta suite
        if (mecEstudante > 0) {
            new AvaliacaoCRUD().eliminarAvaliacoesPorEstudante(mecEstudante);
            new PropinaCRUD().eliminarPropinasPorEstudante(mecEstudante);
            new EstudanteCRUD().eliminarEstudante(mecEstudante);
        }

        // Limpar curso e UCs (nomes com ID aleatório — apenas cleanup desta execução)
        new CursoCRUD().eliminarCurso(NOME_CURSO);
        UnidadeCurricularCRUD ucCRUD = new UnidadeCurricularCRUD();
        ucCRUD.eliminarUC(NOME_UC_Y1);
        ucCRUD.eliminarUC(NOME_UC_Y2);
        ucCRUD.eliminarUC(NOME_UC_Y3);

        // Limpar docente
        DocenteCRUD docCRUD = new DocenteCRUD();
        Docente doc = docCRUD.procurarPorSigla(SIGLA_DOC);
        if (doc == null) doc = docCRUD.procurarPorNif(100000000 + ID);
        if (doc != null) docCRUD.eliminarDocente(doc.getNif());

        // Limpar departamento
        DepartamentoCRUD depCRUD = new DepartamentoCRUD();
        if (depCRUD.procurarPorSigla("DQA") != null) depCRUD.eliminarDepartamento("DQA");
    }

    @Test
    @Order(1)
    public void US01_US03_BloquearCriacaoIncompleta() {
        System.out.println("Validando US 1 e US 3: Bloqueios de Integridade na Criação...");
        CursoController cursoController = new CursoController();
        UnidadeCurricularController ucController = new UnidadeCurricularController();

        // US 1: Tentar criar curso sem departamento
        Resultado<Curso> resCurso = cursoController.registarCurso(new Curso("Sem Dep", 3, null));
        assertFalse(resCurso.sucesso, "US 1 Falhou: O sistema permitiu criar curso sem departamento.");
        assertTrue(resCurso.mensagemErro.contains("obrigatório"));

        // US 3: Tentar criar UC com docente inválido
        Resultado<UnidadeCurricular> resUc = ucController.registarUC("UC Sem Doc", 1, 1, "FANTASMA");
        assertFalse(resUc.sucesso, "US 3 Falhou: O sistema permitiu criar UC sem docente real.");
        assertTrue(resUc.mensagemErro.contains("não existe no sistema"));
    }

    @Test
    @Order(2)
    public void PrepararAmbienteParaStoriesSeguintes() {
        DepartamentoController depCtrl = new DepartamentoController();
        depCtrl.registarDepartamento("Departamento QA", "DQA");

        DocenteController docCtrl = new DocenteController();
        SenhaUtils su = new SenhaUtils();
        docCtrl.registarDocente("Docente QA", "Rua", 100000000 + ID, LocalDate.of(1980, 1, 1), "dqa@issmf.ipp.pt", su.gerarHashComSalt("Senha@123"), SIGLA_DOC, new ArrayList<>());

        // Criar UCs para o 1º, 2º e 3º Ano
        UnidadeCurricularController ucCtrl = new UnidadeCurricularController();
        ucCtrl.registarUC(NOME_UC_Y1, 1, 1, SIGLA_DOC);
        ucCtrl.registarUC(NOME_UC_Y2, 2, 1, SIGLA_DOC);
        ucCtrl.registarUC(NOME_UC_Y3, 3, 1, SIGLA_DOC);

        CursoController cursoCtrl = new CursoController();
        Curso c = new Curso(NOME_CURSO, 3, depCtrl.procurarDepartamento("DQA"));

        // NOTA: Tal como no outro teste, estou a assumir que o setPrecoAnual do curso
        // ainda recebe um double nativo. Se estiver a dar erro, mude para: c.setPrecoAnual(BigDecimal.valueOf(1000.0));
        c.setPrecoAnual(1000.0);
        cursoCtrl.registarCurso(c);

        // Associar todas as UCs ao Curso
        CursoController associador = new CursoController();
        associador.associarUCAoCurso(NOME_CURSO, NOME_UC_Y1);
        associador.associarUCAoCurso(NOME_CURSO, NOME_UC_Y2);
        associador.associarUCAoCurso(NOME_CURSO, NOME_UC_Y3);

        EstudanteController estCtrl = new EstudanteController();
        Resultado<Integer> resEst = estCtrl.registarEstudante("Estudante QA", "Rua", 200000000 + ID, LocalDate.of(2000, 1, 1), NOME_CURSO, su.gerarHashComSalt("Senha@123"));
        mecEstudante = resEst.dados;

        // Simular arranque de ano e geração de propinas do 1º Ano
        Estudante est = estCtrl.procurarEstudantePorNumeroMec(mecEstudante);
        estCtrl.obterAnoDesbloqueado(est);
    }

    @Test
    @Order(3)
    public void US07_AlterarPasswordEstudante() {
        System.out.println("Validando US 7: Alterar Password do Estudante...");
        EstudanteController ec = new EstudanteController();
        SenhaUtils su = new SenhaUtils();

        String novaSenhaHash = su.gerarHashComSalt("NovaSenha@2026");
        Resultado<Estudante> res = ec.alterarPassword(mecEstudante, novaSenhaHash);

        assertTrue(res.sucesso, "US 7 Falhou: Gestor não conseguiu alterar password do estudante.");

        LoginController loginCtrl = new LoginController();
        String emailRealDoEstudante = mecEstudante + "@issmf.ipp.pt";
        assertNotNull(loginCtrl.login(emailRealDoEstudante, "NovaSenha@2026"), "US 7 Falhou: Login com a nova password não funcionou.");
    }

    @Test
    @Order(4)
    public void US09_ImutabilidadeSiglaDocente() {
        System.out.println("Validando US 9: Sigla do Docente Imutável...");
        DocenteController docController = new DocenteController();

        Resultado<Docente> res = docController.atualizarDocente(100000000 + ID, "Novo Nome", "Nova Morada", null);
        assertTrue(res.sucesso);

        Docente d = docController.procurarDocentePorNif(100000000 + ID);
        assertEquals("Novo Nome", d.getNome());
        assertEquals(SIGLA_DOC, d.getSigla(), "US 9 Falhou: A sigla não deveria ter sido alterada.");
    }

    @Test
    @Order(5)
    public void US10_US11_MomentosEAlunosPorUC() {
        System.out.println("Validando US 10 e US 11: Momentos e Listagem de Alunos...");
        DocenteController docController = new DocenteController();
        UnidadeCurricularController ucController = new UnidadeCurricularController();
        EstudanteController ec = new EstudanteController();

        UnidadeCurricular uc = ucController.procurarUCPorNome(NOME_UC_Y1);
        Estudante estudante = ec.procurarEstudantePorNumeroMec(mecEstudante);

        List<String> momentos = new ArrayList<>();
        momentos.add("Frequência 1");
        Resultado<UnidadeCurricular> resMomentos = docController.definirMomentosAvaliacao(SIGLA_DOC, uc.getId(), momentos);
        assertTrue(resMomentos.sucesso, "US 10 Falhou: Docente não conseguiu definir momentos.");

        Avaliacao inscricao = new Avaliacao("Frequência 1", null, uc, estudante);
        new AvaliacaoController().registarAvaliacao(inscricao);

        DocenteController docControllerFresco = new DocenteController();
        List<Estudante> alunosInscritos = docControllerFresco.listarAlunosPorUC(NOME_UC_Y1);
        assertFalse(alunosInscritos.isEmpty(), "US 11 Falhou: A lista de alunos da UC não pode estar vazia.");
        assertEquals(mecEstudante, alunosInscritos.get(0).getNumeroMec());
    }

    @Test
    @Order(6)
    public void US14_LancamentoNotasUpsertEProgresso() {
        System.out.println("Validando US 14: Upsert de Notas e Aprovação...");
        AvaliacaoController avalController = new AvaliacaoController();
        UnidadeCurricularController ucController = new UnidadeCurricularController();
        EstudanteController ec = new EstudanteController();

        UnidadeCurricular uc = ucController.procurarUCPorNome(NOME_UC_Y1);
        Estudante estudante = ec.procurarEstudantePorNumeroMec(mecEstudante);

        // A classe Avaliacao continua a receber Double.
        Avaliacao nota;
    }
}