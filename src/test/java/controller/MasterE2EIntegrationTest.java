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

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MasterE2EIntegrationTest {

    private static final Random r = new Random();
    private static final int ID_UNICO = r.nextInt(900000);
    private static final int NIF_BASE = 200000000 + ID_UNICO;

    private static final String NOME_CURSO = "Engenharia E2E " + ID_UNICO;
    private static final String SIGLA_DEP = gerarSigla();
    private static final String SIGLA_DOC = gerarSigla();

    private static final String UC_ALGORITMOS = "Algoritmos " + ID_UNICO;
    private static final String UC_BD = "Base de Dados " + ID_UNICO;
    private static final String UC_CALCULO = "Cálculo " + ID_UNICO;

    private static int mecJoao;
    private static int mecMaria;
    private static List<Integer> mecsFicticios = new ArrayList<>();

    private static String gerarSigla() {
        String letras = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        return "" + letras.charAt(r.nextInt(26)) + letras.charAt(r.nextInt(26)) + letras.charAt(r.nextInt(26));
    }

    @Test
    @Order(1)
    public void test01_EdgeCases_BloqueiosIniciais() {
        System.out.println("1. Testando Edge Cases e Bloqueios de Negócio...");
        CursoController cursoController = new CursoController();
        UnidadeCurricularController ucController = new UnidadeCurricularController();

        Curso cursoInvalido = new Curso("Curso Fantasma", 3, null);
        Resultado<Curso> resCurso = cursoController.registarCurso(cursoInvalido);
        assertFalse(resCurso.sucesso, "O sistema não deve permitir criar um curso sem departamento.");

        Resultado<UnidadeCurricular> resUc = ucController.registarUC("UC Fantasma", 1, 1, "DOC_INEXISTENTE");
        assertFalse(resUc.sucesso, "O sistema não deve permitir criar UCs sem um docente real.");
        assertTrue(resUc.mensagemErro.contains("não existe no sistema"));
    }

    @Test
    @Order(2)
    public void test02_CRUD_Departamentos() {
        System.out.println("2. Testando CRUD de Departamentos...");
        DepartamentoController depController = new DepartamentoController();

        assertTrue(depController.registarDepartamento("Dep Teste " + ID_UNICO, SIGLA_DEP).sucesso);
        assertTrue(depController.atualizarDepartamento(SIGLA_DEP, "Departamento Atualizado " + ID_UNICO).sucesso);
        assertNotNull(depController.procurarDepartamento(SIGLA_DEP));
    }

    @Test
    @Order(3)
    public void test03_CRUD_Docentes() {
        System.out.println("3. Testando CRUD de Docentes...");
        DocenteController docController = new DocenteController();
        SenhaUtils su = new SenhaUtils();
        String hash = su.gerarHashComSalt("SenhaForte@123");

        assertTrue(docController.registarDocente("Alan Turing", "Rua A", NIF_BASE + 1, LocalDate.of(1912, 6, 23), "alan" + ID_UNICO + "@issmf.ipp.pt", hash, SIGLA_DOC, new ArrayList<>()).sucesso);
    }

    @Test
    @Order(4)
    public void test04_CRUD_UnidadesCurriculares_Cursos() {
        System.out.println("4. Montando UCs e Cursos...");
        UnidadeCurricularController ucController = new UnidadeCurricularController();

        // 1. Regista as UCs (Isto vai gravar no CSV)
        assertTrue(ucController.registarUC(UC_ALGORITMOS, 1, 1, SIGLA_DOC).sucesso);
        assertTrue(ucController.registarUC(UC_BD, 1, 2, SIGLA_DOC).sucesso);
        assertTrue(ucController.registarUC(UC_CALCULO, 1, 1, SIGLA_DOC).sucesso);

        // 2. Regista o Curso
        DepartamentoController depController = new DepartamentoController();
        Departamento depBase = depController.procurarDepartamento(SIGLA_DEP);
        Curso curso = new Curso(NOME_CURSO, 3, depBase);

        // NOTA: Assumo que Curso ainda tem o precoAnual como "double".
        // Se mudou na classe Curso para BigDecimal, altere esta linha para: curso.setPrecoAnual(BigDecimal.valueOf(1000.0));
        curso.setPrecoAnual(BigDecimal.valueOf(1000.0));

        CursoController cursoControllerWrite = new CursoController();
        assertTrue(cursoControllerWrite.registarCurso(curso).sucesso);

        // 3. Instanciar CursoController de fresco para ler as UCs acabadas de gravar!
        CursoController cursoControllerAssocia = new CursoController();
        assertTrue(cursoControllerAssocia.associarUCAoCurso(NOME_CURSO, UC_ALGORITMOS).sucesso);
        assertTrue(cursoControllerAssocia.associarUCAoCurso(NOME_CURSO, UC_BD).sucesso);
        assertTrue(cursoControllerAssocia.associarUCAoCurso(NOME_CURSO, UC_CALCULO).sucesso);

        assertEquals(3, new CursoController().procurarCurso(NOME_CURSO).getUnidadeCurriculars().size());
    }

    @Test
    @Order(5)
    public void test05_Povoar_Estudantes_E_Propinas() {
        System.out.println("5. Registando Estudantes e gerando Propinas...");
        EstudanteController estController = new EstudanteController();
        SenhaUtils su = new SenhaUtils();
        String hash = su.gerarHashComSalt("Aluno@123");

        Resultado<Integer> resJoao = estController.registarEstudante("João Sucesso", "Rua X", NIF_BASE + 3, LocalDate.of(2000, 1, 1), NOME_CURSO, hash);
        Resultado<Integer> resMaria = estController.registarEstudante("Maria Devedora", "Rua Y", NIF_BASE + 4, LocalDate.of(2001, 2, 2), NOME_CURSO, hash);

        assertTrue(resJoao.sucesso && resMaria.sucesso);
        mecJoao = resJoao.dados;
        mecMaria = resMaria.dados;

        for (int i = 1; i <= 3; i++) {
            Resultado<Integer> resFicticio = estController.registarEstudante("Aluno Extra " + i, "Rua Z", NIF_BASE + 4 + i, LocalDate.of(2000, 1, 1), NOME_CURSO, hash);
            mecsFicticios.add(resFicticio.dados);
        }

        // Força a geração automática de propinas
        EstudanteController frEst = new EstudanteController();
        frEst.obterAnoDesbloqueado(frEst.procurarEstudantePorNumeroMec(mecJoao));
        frEst.obterAnoDesbloqueado(frEst.procurarEstudantePorNumeroMec(mecMaria));
        for (int mecFicticio : mecsFicticios) {
            frEst.obterAnoDesbloqueado(frEst.procurarEstudantePorNumeroMec(mecFicticio));
        }

        // Controlador de fresco para ler as propinas geradas
        PropinaController propControllerLer = new PropinaController();
        List<Propina> propinasJoao = propControllerLer.consultarPropinasEstudante(mecJoao);
        assertFalse(propinasJoao.isEmpty(), "A propina do João deve ter sido gerada.");
    }

    @Test
    @Order(6)
    public void test06_Avaliacoes_E_Medias() {
        System.out.println("6. Lançando Notas e Validando Aprovações...");
        EstudanteController estController = new EstudanteController();

        // Marcar o ano 1 do curso como iniciado (via DAL) para permitir o lançamento de notas
        // — bypassa o mínimo de alunos exigido por CursoController.iniciarAnoLetivo(), irrelevante
        // para o que este teste valida (cálculo de médias e aprovação).
        CursoCRUD cursoCRUDLocal = new CursoCRUD();
        Curso curso = cursoCRUDLocal.procurarPorNome(NOME_CURSO);
        curso.adicionarAnoIniciado(1);
        cursoCRUDLocal.atualizarCurso(NOME_CURSO, curso);

        UnidadeCurricularCRUD ucCRUDLocal = new UnidadeCurricularCRUD();
        UnidadeCurricular ucAlgoritmos = ucCRUDLocal.procurarPorNome(UC_ALGORITMOS);
        ucAlgoritmos.adicionarMomento("Exame");
        ucCRUDLocal.atualizarUC(ucAlgoritmos);
        DAOFactory.resetarInstancias();

        Estudante joao = estController.procurarEstudantePorNumeroMec(mecJoao);
        UnidadeCurricular ucFresca = new UnidadeCurricularController().procurarUCPorNome(UC_ALGORITMOS);
        AvaliacaoController avalController = new AvaliacaoController();

        Resultado<Avaliacao> r = avalController.registarAvaliacao(new Avaliacao("Exame", 16.0, ucFresca, joao));
        assertTrue(r.sucesso, "Erro ao lançar nota: " + r.mensagemErro);

        Resultado<String> status = avalController.obterStatusAprovacao(mecJoao, UC_ALGORITMOS);
        assertTrue(status.sucesso);
        assertTrue(status.dados.contains("APROVADO"), "Nota 16.0 deveria resultar em APROVADO: " + status.dados);
    }
}