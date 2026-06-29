package controller;

import DAL.*;
import model.*;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes das correções de dois bugs encontrados em testes manuais:
 *
 * <p><b>Bug 2 — UC reprovada deve voltar a estar disponível para inscrição.</b>
 * A disponibilidade passou a ser calculada em
 * {@link EstudanteController#listarUCsDisponiveisParaInscricao(Estudante)} usando aprovação
 * por <i>média</i> ({@link BLL.EstudanteCalculo#isUCAprovada}). Valida-se em particular o caso
 * em que um momento isolado é &ge; 9.5 mas a média reprova (o filtro antigo escondia-a).</p>
 *
 * <p><b>Bug 1 — ao repetir o ano por reprovação, a propina volta a ficar por pagar.</b>
 * Coberto por {@link PropinaController#reporPropinaParaRepeticao(int, int)} e pela integração
 * em {@link EstudanteController#simularTransicaoAnoLetivoGlobal()}.</p>
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BugfixInscricaoEPropinaTest {

    private static final String NOME_CURSO = "Curso_Bugfix_IP";
    private static final String SIGLA_DEPT = "DBIP";
    private static final String SIGLA_DOC  = "DOC_BIP";

    private static final String UC_Y1_A = "BIP_UC_Ano1_A";
    private static final String UC_Y1_B = "BIP_UC_Ano1_B";
    private static final String UC_Y1_C = "BIP_UC_Ano1_C";
    private static final String UC_Y1_D = "BIP_UC_Ano1_D";
    private static final String UC_Y1_E = "BIP_UC_Ano1_E";
    private static final String UC_Y2_A = "BIP_UC_Ano2_A";
    private static final String UC_Y3_A = "BIP_UC_Ano3_A";
    private static final String[] TODAS_UCS = {UC_Y1_A, UC_Y1_B, UC_Y1_C, UC_Y1_D, UC_Y1_E, UC_Y2_A, UC_Y3_A};
    private static final String[] UCS_ANO1 = {UC_Y1_A, UC_Y1_B, UC_Y1_C, UC_Y1_D, UC_Y1_E};

    private static final int NIF_DOC  = 281000001;
    private static final int NIF_BASE = 281000010;
    private static final int[] mec = new int[7];

    @BeforeAll
    static void setup() {
        DAOFactory.setModo("CSV");
        limpar();

        new DepartamentoCRUD().registarDepartamento(new Departamento("Depart Bugfix IP", SIGLA_DEPT));

        new DocenteController().registarDocente(
                "Docente BIP", "Sala BIP", NIF_DOC, LocalDate.of(1975, 5, 5),
                "doc.bip@issmf.ipp.pt", "hash123", SIGLA_DOC, Collections.emptyList());
        Docente docente = new DocenteCRUD().procurarPorSigla(SIGLA_DOC);
        assertNotNull(docente, "Docente deve existir após registo");

        UnidadeCurricularCRUD ucCRUD = new UnidadeCurricularCRUD();
        for (String nome : UCS_ANO1) {
            UnidadeCurricular uc = new UnidadeCurricular(nome, 1, 1, docente);
            uc.adicionarMomento("Freq");
            uc.adicionarMomento("Exam");
            ucCRUD.registarUC(uc);
        }
        UnidadeCurricular ucY2 = new UnidadeCurricular(UC_Y2_A, 2, 1, docente);
        ucY2.adicionarMomento("Freq");
        ucCRUD.registarUC(ucY2);
        UnidadeCurricular ucY3 = new UnidadeCurricular(UC_Y3_A, 3, 1, docente);
        ucY3.adicionarMomento("Freq");
        ucCRUD.registarUC(ucY3);

        Departamento dep = new DepartamentoCRUD().procurarPorSigla(SIGLA_DEPT);
        new CursoCRUD().registarCurso(new Curso(NOME_CURSO, 3, dep));

        // Após writes directos, sincronizar a cache do DAOFactory
        DAOFactory.resetarInstancias();

        CursoController cursoController = new CursoController();
        for (String nome : TODAS_UCS) {
            Resultado<?> res = cursoController.associarUCAoCurso(NOME_CURSO, nome);
            assertTrue(res.sucesso, "Falhou associar UC '" + nome + "': " + res.mensagemErro);
        }

        EstudanteController ec = new EstudanteController();
        for (int i = 0; i < 7; i++) {
            Resultado<Integer> res = ec.registarEstudante(
                    "Estudante BIP " + i, "Rua " + i, NIF_BASE + i,
                    LocalDate.of(2001, 1, 1), NOME_CURSO, "hash123");
            assertTrue(res.sucesso, "Falhou criar estudante " + i + ": " + res.mensagemErro);
            mec[i] = res.dados;
        }

        // Iniciar o ano 1 (cria automaticamente os registos de avaliação nulos)
        Resultado<Curso> ini = cursoController.iniciarAnoLetivo(NOME_CURSO, 1);
        assertTrue(ini.sucesso, "Deve iniciar o ano 1: " + ini.mensagemErro);
    }

    @AfterAll
    static void limpar() {
        AvaliacaoCRUD avalCRUD = new AvaliacaoCRUD();
        PropinaCRUD propCRUD = new PropinaCRUD();
        EstudanteCRUD estCRUD = new EstudanteCRUD();

        for (int i = 0; i < 7; i++) {
            int nif = NIF_BASE + i;
            int m = (mec != null && i < mec.length && mec[i] > 0)
                    ? mec[i]
                    : (estCRUD.procurarPorNif(nif) != null ? estCRUD.procurarPorNif(nif).getNumeroMec() : 0);
            if (m > 0) {
                avalCRUD.eliminarAvaliacoesPorEstudante(m);
                propCRUD.eliminarPropinasPorEstudante(m);
            }
            Estudante e = estCRUD.procurarPorNif(nif);
            if (e != null) estCRUD.eliminarEstudante(e.getNumeroMec());
        }

        new CursoCRUD().eliminarCurso(NOME_CURSO);

        UnidadeCurricularCRUD ucCRUD = new UnidadeCurricularCRUD();
        for (String nome : TODAS_UCS) {
            while (ucCRUD.procurarPorNome(nome) != null) ucCRUD.eliminarUC(nome);
        }

        DocenteCRUD docCRUD = new DocenteCRUD();
        Docente doc = docCRUD.procurarPorSigla(SIGLA_DOC);
        if (doc != null) docCRUD.eliminarDocente(doc.getNif());

        DepartamentoCRUD depCRUD = new DepartamentoCRUD();
        if (depCRUD.procurarPorSigla(SIGLA_DEPT) != null) depCRUD.eliminarDepartamento(SIGLA_DEPT);
    }

    // ── helpers ──────────────────────────────────────────────────────────────
    private static void lancarNota(int numeroMec, String nomeUC, String momento, double nota) {
        UnidadeCurricular uc = new UnidadeCurricularCRUD().procurarPorNome(nomeUC);
        Estudante est = new EstudanteCRUD().lerEstudante(numeroMec);
        Resultado<Avaliacao> r = new AvaliacaoController().registarAvaliacao(new Avaliacao(momento, nota, uc, est));
        assertTrue(r.sucesso, "Deve lançar nota " + momento + "=" + nota + " em " + nomeUC + ": " + r.mensagemErro);
    }

    private static List<String> nomesDisponiveis(int numeroMec) {
        Estudante est = new EstudanteController().procurarEstudantePorNumeroMec(numeroMec);
        return new EstudanteController().listarUCsDisponiveisParaInscricao(est)
                .stream().map(UnidadeCurricular::getNome).toList();
    }

    private static Propina propinaDoAno(int numeroMec, int ano) {
        return new PropinaController().consultarPropinasEstudante(numeroMec)
                .stream().filter(p -> p.getAnoLetivo() == ano).findFirst().orElse(null);
    }

    // =========================================================================
    // BUG 2 — UCs disponíveis para inscrição
    // =========================================================================

    @Test
    @Order(1)
    void bug2_ucReprovada_deveAparecerDisponivel() {
        // aluno 0 reprova UC_Y1_A com 8/8 (média 8.0 < 9.5)
        lancarNota(mec[0], UC_Y1_A, "Freq", 8.0);
        lancarNota(mec[0], UC_Y1_A, "Exam", 8.0);

        List<String> disp = nomesDisponiveis(mec[0]);
        assertTrue(disp.contains(UC_Y1_A),
                "UC reprovada (8/8) deve voltar a estar disponível para inscrição. Disponíveis: " + disp);
    }

    @Test
    @Order(2)
    void bug2_ucReprovadaNaMedia_masUmMomentoPositivo_deveAparecer() {
        // aluno 1: Freq=14 (>=9.5) e Exam=4 → média 9.0 < 9.5 → REPROVADO.
        // O filtro ANTIGO marcava 'aprovado' por causa do Freq>=9.5 e escondia a UC (defeito 2a).
        lancarNota(mec[1], UC_Y1_A, "Freq", 14.0);
        lancarNota(mec[1], UC_Y1_A, "Exam", 4.0);

        List<String> disp = nomesDisponiveis(mec[1]);
        assertTrue(disp.contains(UC_Y1_A),
                "UC reprovada na média (9.0) com um momento >=9.5 deve aparecer disponível. Disponíveis: " + disp);
    }

    @Test
    @Order(3)
    void bug2_ucAprovada_naoDeveAparecer() {
        // aluno 2 aprova UC_Y1_A com 14/14 (média 14.0)
        lancarNota(mec[2], UC_Y1_A, "Freq", 14.0);
        lancarNota(mec[2], UC_Y1_A, "Exam", 14.0);

        List<String> disp = nomesDisponiveis(mec[2]);
        assertFalse(disp.contains(UC_Y1_A),
                "UC aprovada (14/14) NÃO deve aparecer disponível. Disponíveis: " + disp);
        assertTrue(disp.contains(UC_Y1_B),
                "As restantes UCs (não aprovadas) devem continuar disponíveis. Disponíveis: " + disp);
    }

    @Test
    @Order(4)
    void bug2_semNotas_mostraTodasAno1_eRespeitaAnoDesbloqueado() {
        // aluno 3 não tem notas → todas as 5 UCs do ano 1 disponíveis; UCs de anos superiores não.
        List<String> disp = nomesDisponiveis(mec[3]);
        for (String uc : UCS_ANO1) {
            assertTrue(disp.contains(uc), "UC do ano 1 (" + uc + ") deve estar disponível. Disponíveis: " + disp);
        }
        assertFalse(disp.contains(UC_Y2_A), "UC do 2º ano não deve estar disponível para aluno do 1º ano.");
        assertFalse(disp.contains(UC_Y3_A), "UC do 3º ano não deve estar disponível para aluno do 1º ano.");
    }

    // =========================================================================
    // BUG 1 — propina reposta ao repetir o ano
    // =========================================================================

    @Test
    @Order(5)
    void bug1_reporPropina_metodoDireto() {
        PropinaController pc = new PropinaController();

        // Estado inicial: propina do 1º ano existe e está por pagar
        Propina antes = propinaDoAno(mec[5], 1);
        assertNotNull(antes, "Aluno deve ter propina do 1º ano");
        assertFalse(antes.isTotalmentePaga(), "Propina deve começar por pagar");

        // Repor uma propina já em dívida não faz nada
        assertFalse(pc.reporPropinaParaRepeticao(mec[5], 1),
                "Repor uma propina já em dívida deve devolver false (nada a fazer)");

        // Pagar totalmente
        BigDecimal divida = propinaDoAno(mec[5], 1).getValorEmDivida();
        assertTrue(pc.pagarPropina(mec[5], 1, divida).sucesso, "Deve pagar a propina");
        assertTrue(propinaDoAno(mec[5], 1).isTotalmentePaga(), "Propina deve ficar paga");

        // Repor após paga → reposta para pagamento
        assertTrue(pc.reporPropinaParaRepeticao(mec[5], 1),
                "Repor uma propina paga deve devolver true");
        Propina depois = propinaDoAno(mec[5], 1);
        assertFalse(depois.isTotalmentePaga(), "Após reposição a propina deve estar em dívida");
        assertEquals(0, depois.getValorPago().compareTo(BigDecimal.ZERO),
                "Após reposição o valor pago deve ser zero");
        assertEquals(0, depois.getValorEmDivida().compareTo(depois.getValorTotal()),
                "Após reposição a dívida deve igualar o valor total");

        // Segunda reposição (já em dívida) → false
        assertFalse(pc.reporPropinaParaRepeticao(mec[5], 1),
                "Repor de novo (já em dívida) deve devolver false");
    }

    @Test
    @Order(6)
    void bug1_simulacao_repoePropinaDeQuemReprova_eNaoDeQuemAvanca() {
        PropinaController pc = new PropinaController();

        // --- aluno 4: paga propina e NÃO tem aprovações → vai repetir o ano ---
        BigDecimal divida4 = propinaDoAno(mec[4], 1).getValorEmDivida();
        assertTrue(pc.pagarPropina(mec[4], 1, divida4).sucesso);
        assertTrue(propinaDoAno(mec[4], 1).isTotalmentePaga(), "Propina do aluno 4 deve estar paga antes da simulação");

        // --- aluno 6: aprova 3/5 UCs e paga propina → deve AVANÇAR ---
        for (String uc : new String[]{UC_Y1_A, UC_Y1_B, UC_Y1_C}) {
            lancarNota(mec[6], uc, "Freq", 12.0);
            lancarNota(mec[6], uc, "Exam", 12.0);
        }
        BigDecimal divida6 = propinaDoAno(mec[6], 1).getValorEmDivida();
        assertTrue(pc.pagarPropina(mec[6], 1, divida6).sucesso);

        Resultado<List<String>> relatorio = new EstudanteController().simularTransicaoAnoLetivoGlobal();
        assertTrue(relatorio.sucesso, "Simulação deve ter sucesso");

        // aluno 4: reprovou → propina do 1º ano reposta (em dívida)
        Propina p4 = propinaDoAno(mec[4], 1);
        assertNotNull(p4);
        assertFalse(p4.isTotalmentePaga(),
                "Aluno que reprovou deve ficar com a propina do 1º ano por pagar (reposta).");
        boolean linha4Retido = relatorio.dados.stream()
                .anyMatch(l -> l.contains("Mec: " + mec[4]) && l.contains("[RETIDO]"));
        assertTrue(linha4Retido, "Aluno 4 deve constar como [RETIDO]. Relatório: " + relatorio.dados);

        // aluno 6: avançou → propina do 1º ano continua PAGA (não reposta)
        boolean linha6Avancou = relatorio.dados.stream()
                .anyMatch(l -> l.contains("Mec: " + mec[6]) && l.contains("[AVANÇOU]"));
        assertTrue(linha6Avancou, "Aluno 6 deve constar como [AVANÇOU]. Relatório: " + relatorio.dados);
        assertTrue(propinaDoAno(mec[6], 1).isTotalmentePaga(),
                "Quem avança mantém a propina do ano concluído como paga.");
        assertNotNull(propinaDoAno(mec[6], 2),
                "Quem avança deve ter a propina do ano seguinte gerada.");
    }
}
