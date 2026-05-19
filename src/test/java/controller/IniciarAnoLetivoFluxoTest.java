package controller;

import DAL.*;
import model.*;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testa todas as condições de bloqueio e sucesso do iniciarAnoLetivo.
 * Verifica especificamente:
 *  - Estrutura curricular (UCs em todos os anos)
 *  - Momentos de avaliação obrigatórios em todas as UCs
 *  - Mínimo de estudantes (5 para ano 1, 1 para anos 2 e 3)
 *  - Ano já iniciado
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class IniciarAnoLetivoFluxoTest {

    private static final String NOME_CURSO = "Curso_IniciarAno_Test";
    private static final String SIGLA_DEP = "DTA";
    private static final String SIGLA_DOC = "DOC_IAN";
    private static final int NIF_DOC = 277000001;

    private static CursoCRUD cursoCRUD;
    private static DepartamentoCRUD depCRUD;
    private static UnidadeCurricularCRUD ucCRUD;
    private static EstudanteCRUD estudanteCRUD;
    private static DocenteCRUD docenteCRUD;

    private static Curso cursoTeste;
    private static Departamento depTeste;
    private static Docente docenteTeste;

    @BeforeAll
    static void setupGlobal() {
        cursoCRUD = new CursoCRUD();
        depCRUD = new DepartamentoCRUD();
        ucCRUD = new UnidadeCurricularCRUD();
        estudanteCRUD = new EstudanteCRUD();
        docenteCRUD = new DocenteCRUD();

        // Criar departamento e docente de suporte
        depTeste = new Departamento("Dep Testes Arranque", SIGLA_DEP);
        depCRUD.registarDepartamento(depTeste);

        docenteTeste = new Docente("Doc Arranque", "Gabinete", NIF_DOC,
                LocalDate.of(1970, 1, 1), "doc.arranque@issmf.ipp.pt", "Hash123!",
                SIGLA_DOC, List.of(), List.of());
        docenteCRUD.registarDocente(docenteTeste);
    }

    @AfterAll
    static void limpezaGlobal() {
        // Remover estudantes de teste
        for (int i = 1; i <= 10; i++) {
            int nif = 27800000 + i;
            Estudante e = estudanteCRUD.procurarPorNif(nif);
            if (e != null) estudanteCRUD.eliminarEstudante(e.getNumeroMec());
        }
        // Remover UCs de teste (loop para apagar eventuais duplicados)
        for (String nome : List.of("UC_ANO1_IAN", "UC_ANO2_IAN", "UC_ANO3_IAN", "UC_SEM_MOMENTO_IAN")) {
            while (ucCRUD.procurarPorNome(nome) != null) ucCRUD.eliminarUC(nome);
        }
        // Remover curso (usar instância fresca para garantir estado actual do CSV)
        new CursoCRUD().eliminarCurso(NOME_CURSO);
        depCRUD.eliminarDepartamento(SIGLA_DEP);
        docenteCRUD.eliminarDocente(NIF_DOC);
    }

    // Helper: cria UC com momentos e persiste no CSV (remove duplicados antes de criar)
    private UnidadeCurricular criarUCComMomento(String nome, int ano, String momento) {
        while (ucCRUD.procurarPorNome(nome) != null) ucCRUD.eliminarUC(nome);
        UnidadeCurricular uc = new UnidadeCurricular(nome, ano, 1, docenteTeste);
        uc.adicionarMomento(momento);
        ucCRUD.registarUC(uc);
        return ucCRUD.procurarPorNome(nome);
    }

    // Helper: cria curso completo (1 UC por ano, com momentos) e persiste
    private Curso criarCursoCompleto() {
        cursoCRUD.eliminarCurso(NOME_CURSO); // limpeza prévia
        Curso curso = new Curso(NOME_CURSO, 3, depTeste);

        UnidadeCurricular uc1 = criarUCComMomento("UC_ANO1_IAN", 1, "Frequência");
        UnidadeCurricular uc2 = criarUCComMomento("UC_ANO2_IAN", 2, "Frequência");
        UnidadeCurricular uc3 = criarUCComMomento("UC_ANO3_IAN", 3, "Frequência");
        curso.adicionarUnidadeCurricular(uc1);
        curso.adicionarUnidadeCurricular(uc2);
        curso.adicionarUnidadeCurricular(uc3);
        cursoCRUD.registarCurso(curso);
        return cursoCRUD.procurarPorNome(NOME_CURSO);
    }

    // Helper: regista N estudantes no curso
    private void registarEstudantes(String nomeCurso, int quantidade, int nifBase, int mecBase) {
        EstudanteController ec = new EstudanteController();
        for (int i = 1; i <= quantidade; i++) {
            Estudante e = new Estudante("Aluno IAN " + i, "Rua " + i, nifBase + i,
                    LocalDate.of(2002, 1, 1), "aluno.ian" + i + "@issmf.ipp.pt",
                    mecBase + i, "Hash123!", nomeCurso, true);
            estudanteCRUD.registarEstudante(e);
        }
    }

    // ===== Bloqueios =====

    @Test
    @Order(1)
    void cursoNaoExiste_DeveRetornarErro() {
        Resultado<Curso> res = new CursoController().iniciarAnoLetivo("CURSO_INEXISTENTE_XYZ", 1);
        assertFalse(res.sucesso);
        assertTrue(res.mensagemErro.contains("não encontrado") || res.mensagemErro.contains("encontrado"),
                "Mensagem deve indicar que o curso não foi encontrado.");
    }

    @Test
    @Order(2)
    void anoLetivoInvalido_Zero_DeveRetornarErro() {
        cursoCRUD.eliminarCurso(NOME_CURSO);
        Curso c = new Curso(NOME_CURSO, 3, depTeste);
        cursoCRUD.registarCurso(c);

        Resultado<Curso> res = new CursoController().iniciarAnoLetivo(NOME_CURSO, 0);
        assertFalse(res.sucesso, "Ano 0 é inválido.");
        cursoCRUD.eliminarCurso(NOME_CURSO);
    }

    @Test
    @Order(3)
    void anoLetivoInvalido_AcimaDaDuracao_DeveRetornarErro() {
        cursoCRUD.eliminarCurso(NOME_CURSO);
        Curso c = new Curso(NOME_CURSO, 3, depTeste);
        cursoCRUD.registarCurso(c);

        Resultado<Curso> res = new CursoController().iniciarAnoLetivo(NOME_CURSO, 4);
        assertFalse(res.sucesso, "Ano 4 ultrapassa duração de 3 anos.");
        cursoCRUD.eliminarCurso(NOME_CURSO);
    }

    @Test
    @Order(4)
    void estruturaCurricularIncompleta_SemUCsDeAlgumAno_DeveRetornarErro() {
        cursoCRUD.eliminarCurso(NOME_CURSO);
        Curso c = new Curso(NOME_CURSO, 3, depTeste);
        // Só UCs do ano 1 e 2 — sem ano 3
        UnidadeCurricular uc1 = criarUCComMomento("UC_ANO1_IAN", 1, "Exame");
        UnidadeCurricular uc2 = criarUCComMomento("UC_ANO2_IAN", 2, "Exame");
        c.adicionarUnidadeCurricular(uc1);
        c.adicionarUnidadeCurricular(uc2);
        cursoCRUD.registarCurso(c);

        Resultado<Curso> res = new CursoController().iniciarAnoLetivo(NOME_CURSO, 1);
        assertFalse(res.sucesso, "Sem UC no 3º ano, estrutura curricular é inválida.");
        assertTrue(res.mensagemErro.contains("curricular") || res.mensagemErro.contains("UC"),
                "Mensagem deve referir a estrutura curricular.");
        cursoCRUD.eliminarCurso(NOME_CURSO);
    }

    @Test
    @Order(5)
    void ucSemMomentosDeAvaliacao_DeveRetornarErro() {
        cursoCRUD.eliminarCurso(NOME_CURSO);
        Curso c = new Curso(NOME_CURSO, 3, depTeste);

        // UC do ano 1 sem momentos de avaliação (limpar duplicados antes de criar)
        while (ucCRUD.procurarPorNome("UC_SEM_MOMENTO_IAN") != null) ucCRUD.eliminarUC("UC_SEM_MOMENTO_IAN");
        UnidadeCurricular ucSemMomento = new UnidadeCurricular("UC_SEM_MOMENTO_IAN", 1, 1, docenteTeste);
        ucCRUD.registarUC(ucSemMomento);

        UnidadeCurricular uc2 = criarUCComMomento("UC_ANO2_IAN", 2, "Exame");
        UnidadeCurricular uc3 = criarUCComMomento("UC_ANO3_IAN", 3, "Exame");

        c.adicionarUnidadeCurricular(ucCRUD.procurarPorNome("UC_SEM_MOMENTO_IAN"));
        c.adicionarUnidadeCurricular(uc2);
        c.adicionarUnidadeCurricular(uc3);
        cursoCRUD.registarCurso(c);

        Resultado<Curso> res = new CursoController().iniciarAnoLetivo(NOME_CURSO, 1);
        assertFalse(res.sucesso,
                "Curso com UC sem momentos de avaliação não pode ser iniciado.");
        assertTrue(res.mensagemErro.contains("Momentos") || res.mensagemErro.contains("momento"),
                "Mensagem deve referir os momentos de avaliação em falta.");
        cursoCRUD.eliminarCurso(NOME_CURSO);
    }

    @Test
    @Order(6)
    void menosDe5AlunosParaAno1_DeveRetornarErro() {
        Curso c = criarCursoCompleto();

        // Registar apenas 4 estudantes (insuficiente para ano 1)
        registarEstudantes(NOME_CURSO, 4, 27800000, 26800000);

        Resultado<Curso> res = new CursoController().iniciarAnoLetivo(NOME_CURSO, 1);
        assertFalse(res.sucesso,
                "Menos de 5 alunos no 1º ano deve bloquear o arranque.");
        assertTrue(res.mensagemErro.contains("5") || res.mensagemErro.contains("mínimo"),
                "Mensagem deve referir o mínimo de 5 alunos.");
    }

    // ===== Sucesso =====

    @Test
    @Order(10)
    void com5AlunosEEstruturValida_AnoPode1SerIniciado() {
        // Teste 6 registou NIFs 27800001..27800004; adicionar o 5º com o NIF seguinte
        registarEstudantes(NOME_CURSO, 1, 27800004, 26800004); // NIF 27800005, mec 26800005

        Resultado<Curso> res = new CursoController().iniciarAnoLetivo(NOME_CURSO, 1);
        assertTrue(res.sucesso,
                "Com estrutura válida e 5+ alunos, ano 1 deve poder ser iniciado. Erro: " + res.mensagemErro);
    }

    @Test
    @Order(11)
    void anoJaIniciado_DeveSerBloqueado() {
        // Após o teste anterior, o ano 1 já está iniciado
        Resultado<Curso> res = new CursoController().iniciarAnoLetivo(NOME_CURSO, 1);
        assertFalse(res.sucesso, "Ano já iniciado não pode ser iniciado de novo.");
        assertTrue(res.mensagemErro.contains("iniciado"),
                "Mensagem deve indicar que o ano já foi iniciado.");
    }
}