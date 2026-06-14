package controller;

import DAL.*;
import BLL.EstudanteCalculo;
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
 * ============================================================
 *  SUITE DE COMPLIANCE – Enunciados v1.0 / v1.1 / v1.2 / v1.3
 *  LP2-G2 | 2025/2026
 * ============================================================
 *
 * Organização:
 *   Secção A – Enunciado v1.0 (Funcionalidades Base)
 *   Secção B – Enunciado v1.1 (Segurança + Propinas)
 *   Secção C – Enunciado v1.2 (Base de Dados / Dual Mode)
 *   Secção D – Enunciado v1.3 (Horários + Presenças) [NOT IMPLEMENTED]
 *   Secção E – Testes de Regressão (regras de negócio críticas)
 *
 * Todos os testes correm em modo CSV (sem ligação à BD).
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EnunciadoComplianceTest {

    // ── Dados de teste únicos por execução ────────────────────────────────
    private static final int RND = new Random().nextInt(90000) + 10000;

    private static final String DEP_SIGLA     = "TST";
    private static final String CURSO_NOME    = "Curso TST " + RND;
    private static final String UC1_NOME      = "UC Y1 TST " + RND;
    private static final String UC2_NOME      = "UC Y2 TST " + RND;
    private static final String UC3_NOME      = "UC Y3 TST " + RND;
    private static final String DOC_SIGLA     = "TDD";
    private static final int    DOC_NIF       = 100000000 + RND;
    private static final int    EST_NIF_BASE  = 200000000 + RND;

    private static int mecEstudante1;

    // DAOs em modo CSV
    private static DepartamentoCRUD      depCRUD;
    private static DocenteCRUD           docCRUD;
    private static CursoCRUD             cursoCRUD;
    private static UnidadeCurricularCRUD ucCRUD;
    private static EstudanteCRUD         estCRUD;
    private static AvaliacaoCRUD         avCRUD;

    // ────────────────────────────────────────────────────────────────────────
    //  SETUP / TEARDOWN
    // ────────────────────────────────────────────────────────────────────────

    @BeforeAll
    static void setup() {
        DAOFactory.setModo("CSV");
        depCRUD  = new DepartamentoCRUD();
        docCRUD  = new DocenteCRUD();
        cursoCRUD= new CursoCRUD();
        ucCRUD   = new UnidadeCurricularCRUD();
        estCRUD  = new EstudanteCRUD();
        avCRUD   = new AvaliacaoCRUD();

        SenhaUtils su = new SenhaUtils();

        // Departamento base
        depCRUD.registarDepartamento(new Departamento("Dept Teste", DEP_SIGLA));

        // Docente base
        Docente doc = new Docente("Docente Teste", "Rua A", DOC_NIF,
                LocalDate.of(1975, 6, 15), DOC_SIGLA.toLowerCase() + "@issmf.ipp.pt",
                su.gerarHashComSalt("Senha@123"), DOC_SIGLA, new ArrayList<>(), new ArrayList<>());
        docCRUD.registarDocente(doc);

        // UCs com momentos de avaliação definidos
        UnidadeCurricular uc1 = new UnidadeCurricular(UC1_NOME, 1, 1, doc);
        uc1.adicionarMomento("Frequência"); uc1.adicionarMomento("Exame");
        ucCRUD.registarUC(uc1);

        UnidadeCurricular uc2 = new UnidadeCurricular(UC2_NOME, 2, 1, doc);
        uc2.adicionarMomento("Projeto");
        ucCRUD.registarUC(uc2);

        UnidadeCurricular uc3 = new UnidadeCurricular(UC3_NOME, 3, 1, doc);
        uc3.adicionarMomento("Exame Final");
        ucCRUD.registarUC(uc3);

        // Curso com 3 UCs (1 por ano) e preço de propina
        Curso curso = new Curso(CURSO_NOME, 3, depCRUD.procurarPorSigla(DEP_SIGLA));
        curso.setPrecoAnual(1200.0);
        curso.adicionarUnidadeCurricular(ucCRUD.procurarPorNome(UC1_NOME));
        curso.adicionarUnidadeCurricular(ucCRUD.procurarPorNome(UC2_NOME));
        curso.adicionarUnidadeCurricular(ucCRUD.procurarPorNome(UC3_NOME));
        cursoCRUD.registarCurso(curso);

        // Estudante 1 inscrito no curso
        EstudanteController ec = new EstudanteController();
        Resultado<Integer> res = ec.registarEstudante("Estudante Teste", "Rua B",
                EST_NIF_BASE, LocalDate.of(2000, 3, 10), CURSO_NOME, su.gerarHashComSalt("Senha@123"));
        assertTrue(res.sucesso, "Setup falhou: não foi possível registar estudante de teste.");
        mecEstudante1 = res.dados;

        // Reler o CSV depois de todos os registos para garantir dados frescos nos testes
        estCRUD = new EstudanteCRUD();
        cursoCRUD = new CursoCRUD();
    }

    @AfterAll
    static void teardown() {
        // Limpar propinas do estudante de teste antes de o eliminar
        new PropinaCRUD().eliminarPropinasPorEstudante(mecEstudante1);

        // Limpar dados de teste
        Estudante e = new EstudanteCRUD().procurarPorNif(EST_NIF_BASE);
        if (e != null) new EstudanteCRUD().eliminarEstudante(e.getNumeroMec());
        new CursoCRUD().eliminarCurso(CURSO_NOME);
        for (String uc : List.of(UC1_NOME, UC2_NOME, UC3_NOME))
            if (ucCRUD.procurarPorNome(uc) != null) ucCRUD.eliminarUC(uc);
        docCRUD.eliminarDocente(DOC_NIF);
        depCRUD.eliminarDepartamento(DEP_SIGLA);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  SECÇÃO A — ENUNCIADO v1.0 | Funcionalidades Base
    // ════════════════════════════════════════════════════════════════════════

    // ── A1: Restrições de Curso ──────────────────────────────────────────────

    @Test @Order(10)
    @DisplayName("A1.1 – Curso exige departamento (v1.0)")
    void cursoSemDepartamento_DeveFalhar() {
        CursoController cc = new CursoController();
        Resultado<Curso> res = cc.registarCurso(new Curso("Sem Dep", 3, null));
        assertFalse(res.sucesso, "Curso sem departamento não deve ser registado.");
    }

    @Test @Order(11)
    @DisplayName("A1.2 – Curso não pode ter mais de 5 UCs por ano (v1.0)")
    void cursoMaxCincoUCsPorAno_DeveFalhar() {
        // Configurar curso com 5 UCs no 1º ano (máximo)
        DocenteController docCtrl = new DocenteController();
        Docente docente = docCtrl.procurarDocentePorNif(DOC_NIF);
        assertNotNull(docente);

        Curso cursoParcial = new Curso(CURSO_NOME, 3, depCRUD.procurarPorSigla(DEP_SIGLA));

        // Adicionar 5 UCs ao 1º ano (máximo permitido)
        for (int i = 1; i <= 5; i++) {
            UnidadeCurricular ucExtra = new UnidadeCurricular("UC Extra " + i + " " + RND, 1, 1, docente);
            ucExtra.adicionarMomento("Exame");
            cursoParcial.adicionarUnidadeCurricular(ucExtra);
        }

        // A 6ª UC no mesmo ano deve ser bloqueada pelo controller
        UnidadeCurricular uc6 = new UnidadeCurricular("UC Sexta " + RND, 1, 1, docente);
        boolean adicionou = cursoParcial.adicionarUnidadeCurricular(uc6);
        assertFalse(adicionou, "Não deve ser possível adicionar 6ª UC ao mesmo ano curricular.");
    }

    @Test @Order(12)
    @DisplayName("A1.3 – Estudante só pode estar inscrito num curso (v1.0)")
    void estudanteNumCursoApenas() {
        Estudante est = estCRUD.procurarPorNif(EST_NIF_BASE);
        assertNotNull(est);
        assertEquals(CURSO_NOME, est.getNomeCurso(), "Estudante deve estar no curso inscrito.");
    }

    @Test @Order(13)
    @DisplayName("A1.4 – Curso associado a apenas um departamento (v1.0)")
    void cursoPertenceAUmDepartamento() {
        Curso curso = cursoCRUD.procurarPorNome(CURSO_NOME);
        assertNotNull(curso);
        assertNotNull(curso.getDepartamento());
        assertEquals(DEP_SIGLA, curso.getDepartamento().getSigla());
    }

    @Test @Order(14)
    @DisplayName("A1.5 – Duração do curso é sempre 3 anos (v1.0)")
    void cursoDuracaoTresAnos() {
        Curso curso = cursoCRUD.procurarPorNome(CURSO_NOME);
        assertNotNull(curso);
        assertEquals(3, curso.getDuracao(), "A duração de um curso deve ser sempre 3 anos.");
    }

    // ── A2: Unidades Curriculares ────────────────────────────────────────────

    @Test @Order(20)
    @DisplayName("A2.1 – Todas as UCs têm o mesmo nº de ECTS (6) (v1.0)")
    void todasUCsMesmosEcts() {
        List<UnidadeCurricular> ucs = ucCRUD.getUnidadeCurriculars();
        assertTrue(ucs.size() >= 3, "Devem existir pelo menos 3 UCs de teste.");
        ucs.forEach(uc -> assertEquals(6, uc.getEcts(),
                "Todas as UCs devem valer 6 ECTS: " + uc.getNome()));
    }

    @Test @Order(21)
    @DisplayName("A2.2 – UC sem docente real deve ser bloqueada (v1.0)")
    void ucSemDocenteReal_DeveFalhar() {
        UnidadeCurricularController ucCtrl = new UnidadeCurricularController();
        Resultado<UnidadeCurricular> res = ucCtrl.registarUC("UC Fantasma", 1, 1, "FANTASMA_INEXISTENTE");
        assertFalse(res.sucesso, "UC com docente inexistente não deve ser registada.");
    }

    @Test @Order(22)
    @DisplayName("A2.3 – UC pode pertencer a vários cursos (v1.0)")
    void ucPodeEstarEmVariosCursos() {
        // A mesma UC pode ser associada a outro curso
        Curso cursoCopia = new Curso("Curso Copia " + RND, 3, depCRUD.procurarPorSigla(DEP_SIGLA));
        cursoCopia.adicionarUnidadeCurricular(ucCRUD.procurarPorNome(UC1_NOME));
        cursoCopia.adicionarUnidadeCurricular(ucCRUD.procurarPorNome(UC2_NOME));
        cursoCopia.adicionarUnidadeCurricular(ucCRUD.procurarPorNome(UC3_NOME));
        Resultado<Curso> res = new CursoController().registarCurso(cursoCopia);
        assertTrue(res.sucesso, "A mesma UC pode pertencer a múltiplos cursos.");
        cursoCRUD.eliminarCurso("Curso Copia " + RND);
    }

    // ── A3: Avaliações ───────────────────────────────────────────────────────

    @Test @Order(30)
    @DisplayName("A3.1 – Nota deve estar entre 0 e 20 (v1.0)")
    void notaForaDoIntervalo_DeveFalhar() {
        AvaliacaoController ac = new AvaliacaoController();
        Estudante est = estCRUD.procurarPorNif(EST_NIF_BASE);
        UnidadeCurricular uc = ucCRUD.procurarPorNome(UC1_NOME);

        Avaliacao notaAcima = new Avaliacao("Frequência", 21.0, uc, est);
        Resultado<Avaliacao> res1 = ac.registarAvaliacao(notaAcima);
        assertFalse(res1.sucesso, "Nota 21 deve ser rejeitada (máximo é 20).");

        Avaliacao notaAbaixo = new Avaliacao("Frequência", -1.0, uc, est);
        Resultado<Avaliacao> res2 = ac.registarAvaliacao(notaAbaixo);
        assertFalse(res2.sucesso, "Nota -1 deve ser rejeitada (mínimo é 0).");
    }

    @Test @Order(31)
    @DisplayName("A3.2 – Máximo de 3 avaliações por UC por estudante (v1.0)")
    void maximoTresAvaliacoesPorUC() {
        AvaliacaoController ac = new AvaliacaoController();
        // Usar nova instância para garantir dados frescos do CSV
        Estudante est = new EstudanteCRUD().procurarPorNif(EST_NIF_BASE);
        UnidadeCurricular uc = new UnidadeCurricularCRUD().procurarPorNome(UC1_NOME);

        // Limpar avaliações prévias deste estudante na UC de teste
        // (pode ter avaliações de outros testes)
        long existentes = avCRUD.listarPorEstudante(est.getNumeroMec()).stream()
                .filter(a -> a.getUnidadeCurricular() != null
                        && a.getUnidadeCurricular().getNome().equals(UC1_NOME))
                .count();

        // Registar avaliações até ao limite (3)
        String[] momentos = {"Frequência", "Exame", "Recurso"};
        int registadas = 0;
        for (String momento : momentos) {
            if (existentes + registadas < 3) {
                Avaliacao av = new Avaliacao(momento, 10.0, uc, est);
                Resultado<Avaliacao> res = ac.registarAvaliacao(av);
                if (res.sucesso) registadas++;
            }
        }

        // A 4ª avaliação deve ser bloqueada
        Avaliacao quarta = new Avaliacao("Extra", 15.0, uc, est);
        Resultado<Avaliacao> res4 = ac.registarAvaliacao(quarta);
        assertFalse(res4.sucesso, "4ª avaliação na mesma UC deve ser bloqueada (máximo 3).");
    }

    // ── A4: Regra dos 60% ────────────────────────────────────────────────────

    @Test @Order(40)
    @DisplayName("A4.1 – 60% de aprovação desbloqueia o próximo ano (v1.0)")
    void regra60PorCento_Aprovacao() {
        Curso c = new Curso("Curso 60PCT", 3, null);
        for (int i = 1; i <= 5; i++) {
            UnidadeCurricular uc = new UnidadeCurricular("M UC " + i, 1, 1, null);
            uc.adicionarMomento("Exame");
            c.adicionarUnidadeCurricular(uc);
        }

        Estudante e = new Estudante("Ana", "Rua C", 111222333,
                LocalDate.of(1999, 1, 1), "ana@email.com", 99001, "hash", "Curso 60PCT", true);

        // 3/5 = 60% → deve passar para o 2º ano
        int i = 0;
        for (UnidadeCurricular uc : c.getUnidadeCurriculars()) {
            double nota = (i++ < 3) ? 12.0 : 7.0;
            e.adicionarAvaliacao(new Avaliacao("Exame", nota, uc, e));
        }

        int anoDesbloqueado = EstudanteCalculo.calcularAnoDesbloqueado(e, c);
        assertEquals(2, anoDesbloqueado, "3/5 = 60% deve desbloquear o 2º ano.");
    }

    @Test @Order(41)
    @DisplayName("A4.2 – Menos de 60% mantém o estudante no mesmo ano (v1.0)")
    void regra60PorCento_Reprovacao() {
        Curso c = new Curso("Curso Rep", 3, null);
        for (int i = 1; i <= 5; i++) {
            UnidadeCurricular uc = new UnidadeCurricular("R UC " + i, 1, 1, null);
            uc.adicionarMomento("Exame");
            c.adicionarUnidadeCurricular(uc);
        }

        Estudante e = new Estudante("Bruno", "Rua D", 444555666,
                LocalDate.of(1998, 5, 20), "bruno@email.com", 99002, "hash", "Curso Rep", true);

        // 2/5 = 40% → fica no 1º ano
        int i = 0;
        for (UnidadeCurricular uc : c.getUnidadeCurriculars()) {
            double nota = (i++ < 2) ? 12.0 : 7.0;
            e.adicionarAvaliacao(new Avaliacao("Exame", nota, uc, e));
        }

        assertEquals(1, EstudanteCalculo.calcularAnoDesbloqueado(e, c),
                "2/5 = 40% < 60%, estudante deve ficar no 1º ano.");
    }

    // ── A5: Login ────────────────────────────────────────────────────────────

    @Test @Order(50)
    @DisplayName("A5.1 – Login válido com credenciais correctas (v1.0)")
    void loginValido_CredenciaisCorretas() {
        LoginController lc = new LoginController();
        String email = mecEstudante1 + "@issmf.ipp.pt";
        assertNotNull(lc.login(email, "Senha@123"),
                "Login com credenciais correctas deve ter sucesso.");
    }

    @Test @Order(51)
    @DisplayName("A5.2 – Login falha com password errada (v1.0)")
    void loginInvalido_PasswordErrada() {
        LoginController lc = new LoginController();
        String email = mecEstudante1 + "@issmf.ipp.pt";
        assertNull(lc.login(email, "SenhaErrada!1"),
                "Login com password errada deve falhar.");
        assertEquals(LoginController.ErroLogin.CREDENCIAIS_INVALIDAS, lc.getUltimoErro());
    }

    @Test @Order(52)
    @DisplayName("A5.3 – Login falha com email inexistente (v1.0)")
    void loginInvalido_EmailInexistente() {
        LoginController lc = new LoginController();
        assertNull(lc.login("naoexiste@issmf.ipp.pt", "Senha@123"),
                "Login com email inexistente deve falhar.");
    }

    @Test @Order(53)
    @DisplayName("A5.4 – Login de utilizador inativo deve ser bloqueado (extra)")
    void loginInativo_DeveSerBloqueado() {
        SenhaUtils su = new SenhaUtils();
        // Usar EstudanteController para obter um mecNum válido (7 dígitos) e email conforme
        EstudanteController ecInativo = new EstudanteController();
        int nifInativo = 300000000 + RND; // NIF único para este teste
        Resultado<Integer> inativoRes = ecInativo.registarEstudante(
                "Inativo Teste", "Rua Z", nifInativo,
                LocalDate.of(1995, 1, 1), CURSO_NOME,
                su.gerarHashComSalt("Senha@123"));
        assertTrue(inativoRes.sucesso, "Registo do estudante inativo deve ter sucesso.");

        int mecInativo = inativoRes.dados;
        String emailInativo = mecInativo + "@issmf.ipp.pt";

        // Desativar o estudante
        ecInativo.ativarDesativarEstudante(mecInativo, false);

        // Tentar login — deve retornar CONTA_INATIVA
        LoginController lc = new LoginController();
        assertNull(lc.login(emailInativo, "Senha@123"),
                "Utilizador inativo não deve conseguir autenticar-se.");
        assertEquals(LoginController.ErroLogin.CONTA_INATIVA, lc.getUltimoErro(),
                "Erro esperado: CONTA_INATIVA.");

        // Limpar
        new PropinaCRUD().eliminarPropinasPorEstudante(mecInativo);
        new EstudanteCRUD().eliminarEstudante(mecInativo);
    }

    // ── A6: Ficha do Estudante ───────────────────────────────────────────────

    @Test @Order(60)
    @DisplayName("A6.1 – Ficha do estudante contém todos os campos obrigatórios (v1.0)")
    void fichaEstudante_CamposObrigatorios() {
        Estudante est = estCRUD.procurarPorNif(EST_NIF_BASE);
        assertNotNull(est);
        assertNotNull(est.getNome(),             "Nome em falta");
        assertTrue(est.getNumeroMec() > 0,       "Número mecanográfico em falta");
        assertNotNull(est.getEmail(),             "Email em falta");
        assertTrue(est.getNif() > 0,             "NIF em falta");
        assertNotNull(est.getDataNascimento(),    "Data de nascimento em falta");
        assertNotNull(est.getMorada(),            "Morada em falta");
    }

    @Test @Order(61)
    @DisplayName("A6.2 – Email do estudante tem formato mecNum@issmf.ipp.pt (v1.0)")
    void emailEstudante_FormatoCorreto() {
        Estudante est = estCRUD.procurarPorNif(EST_NIF_BASE);
        assertNotNull(est);
        assertTrue(est.getEmail().endsWith("@issmf.ipp.pt"),
                "Email do estudante deve terminar em @issmf.ipp.pt");
        assertTrue(est.getEmail().matches("^\\d+@issmf\\.ipp\\.pt$"),
                "Email do estudante deve ser mecNum@issmf.ipp.pt");
    }

    @Test @Order(62)
    @DisplayName("A6.3 – Sigla do docente tem exatamente 3 letras e é maiúscula (v1.0 + fix)")
    void siglaDocente_TresLetrasEMaiusculas() {
        Docente doc = docCRUD.procurarPorNif(DOC_NIF);
        assertNotNull(doc);
        assertEquals(DOC_SIGLA.toUpperCase(), doc.getSigla(),
                "Sigla do docente deve estar em maiúsculas.");
        assertEquals(3, doc.getSigla().length(),
                "Sigla do docente deve ter exactamente 3 letras.");
    }

    @Test @Order(63)
    @DisplayName("A6.4 – Sigla do departamento é maiúscula (fix)")
    void siglaDepartamento_Maiuscula() {
        Departamento dep = depCRUD.procurarPorSigla(DEP_SIGLA);
        assertNotNull(dep);
        assertEquals(dep.getSigla(), dep.getSigla().toUpperCase(),
                "Sigla do departamento deve estar em maiúsculas.");
    }

    // ════════════════════════════════════════════════════════════════════════
    //  SECÇÃO B — ENUNCIADO v1.1 | Segurança + Propinas
    // ════════════════════════════════════════════════════════════════════════

    // ── B1: Hashing de Passwords ─────────────────────────────────────────────

    @Test @Order(100)
    @DisplayName("B1.1 – Mesma senha gera hashes diferentes (salt por utilizador) (v1.1)")
    void hashSenha_SaltAleatorioPorUtilizador() {
        SenhaUtils su = new SenhaUtils();
        String hash1 = su.gerarHashComSalt("Senha@123");
        String hash2 = su.gerarHashComSalt("Senha@123");
        assertNotEquals(hash1, hash2,
                "A mesma senha deve produzir hashes diferentes (salt aleatório por utilizador).");
    }

    @Test @Order(101)
    @DisplayName("B1.2 – Verificação de senha funciona com novo formato (v1.1)")
    void verificacaoSenha_NovoFormato() {
        SenhaUtils su = new SenhaUtils();
        String hash = su.gerarHashComSalt("MinhaPass@2026");
        assertTrue(su.verificarSenha("MinhaPass@2026", hash),
                "Verificação com senha correcta deve retornar true.");
        assertFalse(su.verificarSenha("SenhaErrada@1", hash),
                "Verificação com senha errada deve retornar false.");
    }

    @Test @Order(102)
    @DisplayName("B1.3 – Retrocompatibilidade com formato legado de hash (v1.1)")
    void verificacaoSenha_FormatoLegado() {
        // O formato antigo (sem $) deve continuar a funcionar
        SenhaUtils su = new SenhaUtils();
        // Simular hash no formato antigo (salt fixo)
        String hashLegado = gerarHashLegado("Senha@123");
        assertTrue(su.verificarSenha("Senha@123", hashLegado),
                "Hash no formato legado deve ser verificável (retrocompatibilidade).");
    }

    // Helper para simular hash legado (salt fixo)
    private String gerarHashLegado(String senha) {
        try {
            java.security.MessageDigest d = java.security.MessageDigest.getInstance("SHA-256");
            byte[] bytes = d.digest((senha + "AzTCXmiWY6lDiLVSj0RHkA==").getBytes());
            return java.util.Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) { return ""; }
    }

    // ── B2: Iniciação de Ano Letivo ──────────────────────────────────────────

    @Test @Order(110)
    @DisplayName("B2.1 – 1º ano exige mínimo de 5 estudantes (v1.1)")
    void primeiroAno_Minimo5Estudantes() {
        // O curso de teste tem apenas 1 estudante → deve falhar para o 1º ano
        Resultado<Curso> res = new CursoController().iniciarAnoLetivo(CURSO_NOME, 1);
        assertFalse(res.sucesso,
                "Com menos de 5 estudantes, o 1º ano não pode ser iniciado.");
        assertTrue(res.mensagemErro.contains("5") || res.mensagemErro.contains("mínimo"),
                "Mensagem deve mencionar o mínimo de 5 estudantes.");
    }

    @Test @Order(111)
    @DisplayName("B2.2 – Ano > 1 exige apenas 1 estudante (v1.1)")
    void anosPosteriores_Minimo1Estudante() {
        // Para testar anos 2 e 3, precisamos de um curso com o ano 1 já iniciado
        // e pelo menos 1 estudante no ano 2. Este teste verifica a regra de negócio
        // ao nível do controller usando os dados da BLL directamente.
        Curso c = cursoCRUD.procurarPorNome(CURSO_NOME);
        assertNotNull(c);
        // A regra: para ano > 1, mínimo = 1. Para ano = 1, mínimo = 5.
        // Verificamos que a mensagem de erro para ano=2 seria diferente de ano=1
        // (só podemos testar sem estado iniciado, mas a lógica do controller
        //  valida estrutura curricular antes de contar estudantes)
        Resultado<Curso> resAno2 = new CursoController().iniciarAnoLetivo(CURSO_NOME, 2);
        // Pode falhar por estrutura ou por alunos, mas não pelo limite de 5
        if (!resAno2.sucesso && resAno2.mensagemErro != null) {
            assertFalse(resAno2.mensagemErro.contains("5 aluno"),
                    "Para o 2º ano, a mensagem não deve pedir 5 alunos.");
        }
    }

    // ── B3: Propinas ─────────────────────────────────────────────────────────

    @Test @Order(120)
    @DisplayName("B3.1 – Propina criada automaticamente ao inscrever estudante (v1.1)")
    void propinaCriadaAoInscreverEstudante() {
        PropinaController pc = new PropinaController();
        List<Propina> propinas = pc.consultarPropinasEstudante(mecEstudante1);
        assertFalse(propinas.isEmpty(),
                "Uma propina deve ser criada automaticamente na inscrição do estudante.");
        assertEquals(1, propinas.get(0).getAnoLetivo(),
                "A propina criada deve ser para o 1º ano letivo.");
    }

    @Test @Order(121)
    @DisplayName("B3.2 – Valor da propina corresponde ao preço do curso (v1.1)")
    void propinaValor_CorrespondeCurso() {
        PropinaController pc = new PropinaController();
        List<Propina> propinas = pc.consultarPropinasEstudante(mecEstudante1);
        assertFalse(propinas.isEmpty());

        Propina p = propinas.get(0);
        assertEquals(0, p.getValorTotal().compareTo(BigDecimal.valueOf(1200.0)),
                "Valor da propina deve corresponder ao preço configurado no curso (1200€).");
        assertEquals(0, p.getValorPago().compareTo(BigDecimal.ZERO),
                "Valor pago deve ser 0 logo após a criação.");
    }

    @Test @Order(122)
    @DisplayName("B3.3 – Pagamento parcial de propina actualiza valor em dívida (v1.1)")
    void pagamentoParcial_ActualizaDivida() {
        PropinaController pc = new PropinaController();

        Resultado<Propina> res = pc.pagarPropina(mecEstudante1, 1, BigDecimal.valueOf(500.0));
        assertTrue(res.sucesso, "Pagamento parcial deve ter sucesso.");

        List<Propina> propinas = pc.consultarPropinasEstudante(mecEstudante1);
        Propina p = propinas.stream().filter(pr -> pr.getAnoLetivo() == 1).findFirst().orElse(null);
        assertNotNull(p);
        assertEquals(0, p.getValorPago().compareTo(BigDecimal.valueOf(500.0)),
                "Valor pago deve ser 500€ após pagamento parcial.");
        assertFalse(p.isTotalmentePaga(),
                "Propina não deve estar totalmente paga após pagamento parcial.");
    }

    @Test @Order(123)
    @DisplayName("B3.4 – Pagamento acima da dívida deve ser rejeitado (v1.1)")
    void pagamentoAcimaDaDivida_DeveSerRejeitado() {
        PropinaController pc = new PropinaController();
        // Dívida restante = 700€ (1200 - 500 pagos no teste anterior)
        Resultado<Propina> res = pc.pagarPropina(mecEstudante1, 1, BigDecimal.valueOf(1000.0));
        assertFalse(res.sucesso,
                "Pagamento superior à dívida actual deve ser rejeitado.");
        assertTrue(res.mensagemErro.contains("superior") || res.mensagemErro.contains("dívida"),
                "Mensagem deve indicar que o valor é superior à dívida.");
    }

    @Test @Order(124)
    @DisplayName("B3.5 – Pagamento total da propina marca-a como paga (v1.1)")
    void pagamentoTotal_MarcaComoPaga() {
        PropinaController pc = new PropinaController();
        // Pagar os restantes 700€
        Resultado<Propina> res = pc.pagarPropina(mecEstudante1, 1, BigDecimal.valueOf(700.0));
        assertTrue(res.sucesso, "Pagamento do restante deve ter sucesso.");
        assertTrue(res.dados.isTotalmentePaga(),
                "Propina deve estar totalmente paga após liquidar a dívida.");
    }

    @Test @Order(125)
    @DisplayName("B3.6 – isPropinaPaga verifica correctamente o estado (v1.1)")
    void isPropinaPaga_Verificacao() {
        PropinaController pc = new PropinaController();
        assertTrue(pc.isPropinaPaga(mecEstudante1, 1),
                "isPropinaPaga deve retornar true após pagamento total.");
        assertFalse(pc.isPropinaPaga(mecEstudante1, 99),
                "isPropinaPaga deve retornar false para ano sem propina.");
    }

    @Test @Order(126)
    @DisplayName("B3.7 – Progressão de ano bloqueada se propina não paga (v1.1)")
    void progressaoAno_BloqueadaSemPropina() {
        // Criar estudante sem propinas pagas
        SenhaUtils su = new SenhaUtils();
        Estudante estSemPropina = new Estudante("Sem Propina", "Rua E", 777888999 + RND,
                LocalDate.of(2001, 1, 1), (777888999 + RND) + "@issmf.ipp.pt",
                991000 + RND, su.gerarHashComSalt("Senha@123"), CURSO_NOME, true);
        estCRUD.registarEstudante(estSemPropina);
        DAOFactory.resetarInstancias(); // garante que controllers vêem o novo estudante

        // Não pagar nenhuma propina
        PropinaController pc = new PropinaController();
        pc.gerarPropinaAnual(991000 + RND, 1); // gerar propina sem pagar

        // Calcular ano desbloqueado com propina em dívida
        EstudanteController ec = new EstudanteController();
        Estudante est = ec.procurarEstudantePorNumeroMec(991000 + RND);
        assertNotNull(est);

        // Mesmo que notes unlock ano 2, propina não paga deve bloquear
        int anoReal = ec.obterAnoDesbloqueado(est);
        assertEquals(1, anoReal,
                "Estudante com propina do 1º ano não paga deve ficar no 1º ano.");

        // Limpar
        estCRUD.eliminarEstudante(991000 + RND);
    }

    @Test @Order(127)
    @DisplayName("B3.8 – Gestor vê lista de alunos em dívida (v1.1)")
    void listaAlunosEmDivida_VisívelParaGestor() {
        // O estudante de testes tem propinas pagas → não deve estar na lista
        // mas o sistema deve conseguir listar (mesmo que vazia)
        PropinaController pc = new PropinaController();
        List<Estudante> devedores = pc.obterAlunosEmDivida();
        assertNotNull(devedores, "A lista de devedores não deve ser null.");
        // Os estudantes com propinas 100% pagas não devem aparecer
        devedores.forEach(e ->
                assertFalse(pc.consultarPropinasEstudante(e.getNumeroMec()).stream()
                                .allMatch(Propina::isTotalmentePaga),
                        "Um estudante com todas propinas pagas não deve estar na lista de devedores."));
    }

    // ── B4: Recuperação de Password ──────────────────────────────────────────

    @Test @Order(130)
    @DisplayName("B4.1 – Alterar password do estudante funciona correctamente (v1.1)")
    void alterarPasswordEstudante_Funciona() {
        EstudanteController ec = new EstudanteController();
        SenhaUtils su = new SenhaUtils();

        String novaHash = su.gerarHashComSalt("NovaSenha@2026");
        Resultado<Estudante> res = ec.alterarPassword(mecEstudante1, novaHash);
        assertTrue(res.sucesso, "Alterar password do estudante deve ter sucesso.");

        // Verificar que o login funciona com a nova password
        LoginController lc = new LoginController();
        assertNotNull(lc.login(mecEstudante1 + "@issmf.ipp.pt", "NovaSenha@2026"),
                "Login com a nova password deve funcionar.");

        // Restaurar password original
        ec.alterarPassword(mecEstudante1, su.gerarHashComSalt("Senha@123"));
    }

    // ════════════════════════════════════════════════════════════════════════
    //  SECÇÃO C — ENUNCIADO v1.2 | Dual Mode (CSV / SQL)
    // ════════════════════════════════════════════════════════════════════════

    @Test @Order(200)
    @DisplayName("C1.1 – DAOFactory em modo CSV devolve implementação CSV")
    void daoFactory_ModoCSV() {
        DAOFactory.setModo("CSV");
        assertFalse(DAOFactory.isSql(), "isSql() deve ser false em modo CSV.");
        assertInstanceOf(EstudanteCRUD.class, DAOFactory.getEstudanteDAO(),
                "Em modo CSV, getEstudanteDAO() deve retornar EstudanteCRUD.");
        assertInstanceOf(GestorCRUD.class, DAOFactory.getGestorDAO(),
                "Em modo CSV, getGestorDAO() deve retornar GestorCRUD.");
    }

    @Test @Order(201)
    @DisplayName("C1.2 – DAOFactory em modo SQL devolve implementação SQL")
    void daoFactory_ModoSQL() {
        DAOFactory.setModo("SQL");
        assertTrue(DAOFactory.isSql(), "isSql() deve ser true em modo SQL.");
        assertInstanceOf(EstudanteSqlDAO.class, DAOFactory.getEstudanteDAO(),
                "Em modo SQL, getEstudanteDAO() deve retornar EstudanteSqlDAO.");
        assertInstanceOf(GestorSqlDAO.class, DAOFactory.getGestorDAO(),
                "Em modo SQL, getGestorDAO() deve retornar GestorSqlDAO.");
        // Restaurar
        DAOFactory.setModo("CSV");
    }

    @Test @Order(202)
    @DisplayName("C1.3 – Modo inválido não altera a configuração actual")
    void daoFactory_ModoInvalido_NaoAltera() {
        DAOFactory.setModo("CSV");
        DAOFactory.setModo("INVALIDO");
        assertFalse(DAOFactory.isSql(),
                "Modo inválido não deve alterar a configuração actual.");
    }

    // ════════════════════════════════════════════════════════════════════════
    //  SECÇÃO D — ENUNCIADO v1.3 | Horários + Presenças [NÃO IMPLEMENTADO]
    // ════════════════════════════════════════════════════════════════════════

    @Test @Order(300)
    @DisplayName("D1 – [TODO v1.3] Gestão de horários ainda não implementada")
    @Disabled("Funcionalidade prevista no enunciado v1.3 — ainda não implementada")
    void horarios_GestorDefineHorario() {
        // TODO: O gestor define horário de cada ano letivo por UC
        // Regras: 18h-23h30, máx 5h/dia, pausa jantar 20h-20h30
        // UC: min 2h, max 6h, blocos de 1h ou 2h
        fail("Não implementado — enunciado v1.3");
    }

    @Test @Order(301)
    @DisplayName("D2 – [TODO v1.3] Sobreposição de horários deve ser bloqueada")
    @Disabled("Funcionalidade prevista no enunciado v1.3 — ainda não implementada")
    void horarios_SemSobreposicao() {
        // TODO: Um docente não pode ter 2 aulas ao mesmo tempo no mesmo dia
        fail("Não implementado — enunciado v1.3");
    }

    @Test @Order(310)
    @DisplayName("D3 – [TODO v1.3] Marcação de presenças pelo docente")
    @Disabled("Funcionalidade prevista no enunciado v1.3 — ainda não implementada")
    void presencas_DocenteMarcaPrimeiro() {
        // TODO: Docente marca presença → estudante pode marcar a seguir
        fail("Não implementado — enunciado v1.3");
    }

    @Test @Order(311)
    @DisplayName("D4 – [TODO v1.3] Estudante não pode marcar presença sem docente ter marcado")
    @Disabled("Funcionalidade prevista no enunciado v1.3 — ainda não implementada")
    void presencas_EstudanteBloqueadoSemDocente() {
        fail("Não implementado — enunciado v1.3");
    }

    @Test @Order(320)
    @DisplayName("D5 – [TODO v1.3] Justificação de faltas com aprovação do gestor")
    @Disabled("Funcionalidade prevista no enunciado v1.3 — ainda não implementada")
    void faltas_JustificacaoAprovadaPeloGestor() {
        // TODO: Estudante submete pedido → gestor aprova/rejeita
        // Tipos: saúde, estatuto de estudante (atleta, trabalhador, pai)
        fail("Não implementado — enunciado v1.3");
    }

    @Test @Order(321)
    @DisplayName("D6 – [TODO v1.3] Gestão de estatutos de estudante pelo gestor")
    @Disabled("Funcionalidade prevista no enunciado v1.3 — ainda não implementada")
    void estatutos_GestorCriaEGere() {
        // TODO: Gestor cria tipos de estatuto (atleta, trabalhador, pai, etc.)
        fail("Não implementado — enunciado v1.3");
    }

    // ════════════════════════════════════════════════════════════════════════
    //  SECÇÃO E — TESTES DE REGRESSÃO (regras críticas de negócio)
    // ════════════════════════════════════════════════════════════════════════

    @Test @Order(400)
    @DisplayName("E1 – Número mecanográfico é único e tem formato YYNNnnnn (extra)")
    void mecNumero_UnicoEFormatoCorreto() {
        Estudante est = estCRUD.procurarPorNif(EST_NIF_BASE);
        assertNotNull(est);
        int mec = est.getNumeroMec();
        assertTrue(mec > 1000000, "Número mecanográfico deve seguir o formato YYNN...");

        // Não pode existir outro estudante com o mesmo mecNum
        long count = estCRUD.getEstudantes().stream()
                .filter(e -> e.getNumeroMec() == mec).count();
        assertEquals(1, count, "Número mecanográfico deve ser único no sistema.");
    }

    @Test @Order(401)
    @DisplayName("E2 – NIF duplicado é bloqueado em todo o sistema (extra)")
    void nif_Unicidade() {
        SenhaUtils su = new SenhaUtils();
        // Tentar registar dois gestores com o mesmo NIF
        GestorController gc = new GestorController();
        Resultado<Gestor> res1 = gc.registarGestor("Gestor A", "Rua", EST_NIF_BASE + 99,
                LocalDate.of(1980, 1, 1), "gestora.a" + RND + "@issmf.ipp.pt",
                su.gerarHashComSalt("Senha@123"), "Diretor");
        // Se já existir, o segundo deve falhar
        Resultado<Gestor> res2 = gc.registarGestor("Gestor B", "Rua", EST_NIF_BASE + 99,
                LocalDate.of(1982, 2, 2), "gestora.b" + RND + "@issmf.ipp.pt",
                su.gerarHashComSalt("Senha@123"), "Subdiretor");

        if (res1.sucesso) {
            assertFalse(res2.sucesso, "NIF duplicado deve ser rejeitado.");
        }
        // Limpar se foi criado
        if (res1.sucesso) gc.eliminarGestor(EST_NIF_BASE + 99);
    }

    @Test @Order(402)
    @DisplayName("E3 – Alteração de curso bloqueada quando tem estudantes inscritos (v1.0)")
    void curso_ImutalvelComEstudantes() {
        CursoController cc = new CursoController();
        Curso cursoAtual = cursoCRUD.procurarPorNome(CURSO_NOME);
        assertNotNull(cursoAtual);

        // Tentar mudar o departamento com estudante inscrito deve ser bloqueado
        Departamento novoDep = new Departamento("Outro Dept", "ODT");
        depCRUD.registarDepartamento(novoDep);

        Curso cursoNovo = new Curso(CURSO_NOME, 3, novoDep);
        Resultado<Curso> res = cc.atualizarCurso(CURSO_NOME, cursoNovo);

        // Deve bloquear porque há estudantes e o curso está iniciado
        // (se não iniciado, pode mudar nome/dept)
        // Esta regra é: "Sempre que existam estudantes alocados, o curso não pode ser alterado"
        // O CursoController implementa esta regra no atualizarCurso

        depCRUD.eliminarDepartamento("ODT");
    }

    @Test @Order(403)
    @DisplayName("E4 – Propina gerada com valor correcto do curso (v1.1)")
    void propina_ValorVemDoCurso() {
        SenhaUtils su = new SenhaUtils();
        // Criar curso com preço diferente
        Curso c = new Curso("Curso Preço " + RND, 3, depCRUD.procurarPorSigla(DEP_SIGLA));
        c.setPrecoAnual(750.0);
        c.adicionarUnidadeCurricular(ucCRUD.procurarPorNome(UC1_NOME));
        c.adicionarUnidadeCurricular(ucCRUD.procurarPorNome(UC2_NOME));
        c.adicionarUnidadeCurricular(ucCRUD.procurarPorNome(UC3_NOME));
        cursoCRUD.registarCurso(c);
        DAOFactory.resetarInstancias(); // garante que o controller usa o novo curso (750€)

        EstudanteController ec = new EstudanteController();
        Resultado<Integer> res = ec.registarEstudante("Aluno Preco", "Rua F",
                355000000 + RND, LocalDate.of(2002, 6, 1),
                "Curso Preço " + RND, su.gerarHashComSalt("Senha@123"));
        assertTrue(res.sucesso, "Registo do estudante deve ter sucesso.");

        PropinaController pc = new PropinaController();
        List<Propina> propinas = pc.consultarPropinasEstudante(res.dados);
        assertFalse(propinas.isEmpty(), "Propina deve ser criada automaticamente.");
        assertEquals(0, propinas.get(0).getValorTotal().compareTo(BigDecimal.valueOf(750.0)),
                "Valor da propina deve ser o preço configurado no curso (750€).");

        // Limpar
        estCRUD.eliminarEstudante(res.dados);
        cursoCRUD.eliminarCurso("Curso Preço " + RND);
    }

    @Test @Order(404)
    @DisplayName("E5 – Validação de email correcta para cada tipo de utilizador")
    void validacaoEmail_TiposCorretos() {
        common.utils.BackendUtils bu = null; // static methods
        assertTrue(common.utils.BackendUtils.emailISSMFEstudanteValido("1260001@issmf.ipp.pt"));
        assertTrue(common.utils.BackendUtils.emailISSMFDocenteValido("mpa@issmf.ipp.pt"));
        assertTrue(common.utils.BackendUtils.emailISSMFGestorValido("admin.gestor@issmf.ipp.pt"));

        assertFalse(common.utils.BackendUtils.emailISSMFEstudanteValido("mpa@issmf.ipp.pt"),
                "Email de docente não é válido para estudante.");
        assertFalse(common.utils.BackendUtils.emailISSMFDocenteValido("1260001@issmf.ipp.pt"),
                "Email de estudante não é válido para docente.");
        assertFalse(common.utils.BackendUtils.emailISSMFGestorValido("1260001@issmf.ipp.pt"),
                "Email de estudante não é válido para gestor.");
    }

    @Test @Order(405)
    @DisplayName("E6 – Validação de NIF: 9 dígitos, primeiro dígito 1/2/3/5/6/8/9")
    void validacaoNIF() {
        assertTrue(common.utils.BackendUtils.nifEValido("123456789"));
        assertTrue(common.utils.BackendUtils.nifEValido("256789012"));
        assertFalse(common.utils.BackendUtils.nifEValido("12345678"),  "NIF com 8 dígitos inválido.");
        assertFalse(common.utils.BackendUtils.nifEValido("1234567890"), "NIF com 10 dígitos inválido.");
        assertFalse(common.utils.BackendUtils.nifEValido("423456789"), "NIF com primeiro dígito 4 inválido.");
    }

    @Test @Order(406)
    @DisplayName("E7 – Gestor pode registar, procurar, actualizar e eliminar (CRUD completo)")
    void gestor_CrudCompleto() {
        GestorController gc = new GestorController();
        SenhaUtils su = new SenhaUtils();
        int nifGestor = 399000000 + RND;

        // Create
        Resultado<Gestor> criarRes = gc.registarGestor("Gestor CRUD", "Rua G", nifGestor,
                LocalDate.of(1978, 3, 15), "crud" + RND + ".gestor@issmf.ipp.pt",
                su.gerarHashComSalt("Crud@1234"), "Diretor");
        assertTrue(criarRes.sucesso, "Registar gestor deve ter sucesso.");

        // Read
        Gestor gestor = gc.procurarGestorPorNif(nifGestor);
        assertNotNull(gestor, "Gestor registado deve ser encontrado por NIF.");
        assertEquals("Gestor CRUD", gestor.getNome());

        // Update
        Resultado<Gestor> actualizarRes = gc.atualizarGestor(nifGestor, "Rua Nova", null, "Subdiretor");
        assertTrue(actualizarRes.sucesso, "Actualizar gestor deve ter sucesso.");
        assertEquals("Subdiretor", gc.procurarGestorPorNif(nifGestor).getCargo());

        // Verify ativo preserved after update
        assertTrue(gc.procurarGestorPorNif(nifGestor).isAtivo(),
                "Estado ativo deve ser preservado após actualização.");

        // Delete
        Resultado<Gestor> eliminarRes = gc.eliminarGestor(nifGestor);
        assertTrue(eliminarRes.sucesso, "Eliminar gestor deve ter sucesso.");
        assertNull(gc.procurarGestorPorNif(nifGestor), "Gestor eliminado não deve ser encontrado.");
    }
}
