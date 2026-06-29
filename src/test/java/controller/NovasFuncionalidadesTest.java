package controller;

import DAL.*;
import model.*;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes para as três funcionalidades implementadas:
 *  1. Ativar / Desativar Curso
 *  2. Bloqueio de alteração de preço em cursos iniciados
 *  3. Estudantes que concluem o curso ficam inativos após passagem de ano
 *
 * Os controllers e DAOs são criados UMA VEZ no @BeforeAll e guardados como
 * campos static. Assim as referências internas (this.cursoDAO, etc.) apontam
 * sempre para o singleton correto, mesmo que o DAOFactory seja acedido noutros
 * contextos.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class NovasFuncionalidadesTest {

    // ── Constantes ────────────────────────────────────────────────────────────
    private static final String SIGLA_DEPT = "NF_TST";
    private static final String SIGLA_DOC  = "NFD";
    private static final String NOME_CURSO = "Curso_NovasFuncionalidades";
    private static final String UC_Y1      = "NF_UC_Ano1";
    private static final String UC_Y2      = "NF_UC_Ano2";
    private static final String UC_Y3      = "NF_UC_Ano3";
    private static final int NIF_DOC   = 340000001;
    private static final int NIF_EST_A = 340000010; // vai concluir o curso
    private static final int NIF_EST_B = 340000011; // vai ficar retido

    // ── Referências estáticas criadas no @BeforeAll ───────────────────────────
    private static ICursoDAO             cursoDAO;
    private static IUnidadeCurricularDAO ucDAO;
    private static IEstudanteDAO         estudanteDAO;
    private static IAvaliacaoDAO         avaliacaoDAO;
    private static IPropinaDAO           propinaDAO;
    private static CursoController       cursoCtrl;
    private static EstudanteController   estCtrl;
    private static PropinaController     propCtrl;

    private static int mecA;
    private static int mecB;

    // ── Setup / Teardown ──────────────────────────────────────────────────────

    @BeforeAll
    static void configurar() {
        DAOFactory.setModo("CSV");  // inclui resetarInstancias()
        limpar();                    // limpar possíveis restos de runs anteriores

        // 1. Departamento
        DAOFactory.getDepartamentoDAO()
                .registarDepartamento(new Departamento("Dept NF Test", SIGLA_DEPT));

        // 2. Docente
        new DocenteController().registarDocente(
                "Docente NF", "Sala 1", NIF_DOC, LocalDate.of(1975, 6, 1),
                "nf@test.pt", "hash123", SIGLA_DOC, List.of());

        // 3. UCs — via singleton para ficarem visíveis ao DocenteController
        Docente doc = DAOFactory.getDocenteDAO().procurarPorNif(NIF_DOC);
        ucDAO = DAOFactory.getUnidadeCurricularDAO();
        ucDAO.registarUC(new UnidadeCurricular(UC_Y1, 1, 1, doc));
        ucDAO.registarUC(new UnidadeCurricular(UC_Y2, 2, 1, doc));
        ucDAO.registarUC(new UnidadeCurricular(UC_Y3, 3, 1, doc));

        // 4. Momentos de avaliação
        DocenteController docenteCtrl = new DocenteController();
        docenteCtrl.definirMomentosAvaliacao(SIGLA_DOC, ucDAO.procurarPorNome(UC_Y1).getId(), List.of("T1"));
        docenteCtrl.definirMomentosAvaliacao(SIGLA_DOC, ucDAO.procurarPorNome(UC_Y2).getId(), List.of("T1"));
        docenteCtrl.definirMomentosAvaliacao(SIGLA_DOC, ucDAO.procurarPorNome(UC_Y3).getId(), List.of("T1"));

        // 5. Curso — obter o singleton AGORA (pode ter sido criado por definirMomentosAvaliacao)
        //    e registar o curso de teste nesse singleton
        cursoDAO = DAOFactory.getCursoDAO();
        Departamento dep = DAOFactory.getDepartamentoDAO().procurarPorSigla(SIGLA_DEPT);
        Curso curso = new Curso(NOME_CURSO, 3, dep);
        curso.setPrecoAnual(BigDecimal.valueOf(1000));
        cursoDAO.registarCurso(curso);

        // 6. Criar o CursoController com o MESMO cursoDAO singleton
        cursoCtrl = new CursoController();
        cursoCtrl.associarUCAoCurso(NOME_CURSO, UC_Y1);
        cursoCtrl.associarUCAoCurso(NOME_CURSO, UC_Y2);
        cursoCtrl.associarUCAoCurso(NOME_CURSO, UC_Y3);

        // 7. Estudantes
        estudanteDAO = DAOFactory.getEstudanteDAO();
        estCtrl = new EstudanteController();
        estCtrl.registarEstudante("Aluno Conclui", "Rua A", NIF_EST_A,
                LocalDate.of(2000, 1, 1), NOME_CURSO, "hash");
        estCtrl.registarEstudante("Aluno Retido", "Rua B", NIF_EST_B,
                LocalDate.of(2000, 2, 2), NOME_CURSO, "hash");

        mecA = estudanteDAO.getEstudantes().stream()
                .filter(e -> e.getNif() == NIF_EST_A).findFirst()
                .map(Estudante::getNumeroMec).orElse(-1);
        mecB = estudanteDAO.getEstudantes().stream()
                .filter(e -> e.getNif() == NIF_EST_B).findFirst()
                .map(Estudante::getNumeroMec).orElse(-1);

        // 8. Guardar restantes DAOs
        avaliacaoDAO = DAOFactory.getAvaliacaoDAO();
        propinaDAO   = DAOFactory.getPropinaDAO();
        propCtrl     = new PropinaController();

        // Verificação de sanidade — falha aqui com mensagem clara se o setup falhou
        assertNotNull(cursoDAO.procurarPorNome(NOME_CURSO),
                "SETUP FALHOU: curso de teste não encontrado no singleton após registar");
        assertTrue(mecA > 0, "SETUP FALHOU: mecA não obtido");
        assertTrue(mecB > 0, "SETUP FALHOU: mecB não obtido");
    }

    @AfterAll
    static void limparAposTudo() {
        DAOFactory.resetarInstancias();
        limpar();
    }

    /** Limpa dados de teste dos CSVs usando instâncias directas (não usa DAOFactory). */
    private static void limpar() {
        // Ordem: primeiro avaliações e propinas, depois estudantes, depois curso/UCs/docente/dep
        EstudanteCRUD estCRUD   = new EstudanteCRUD();
        AvaliacaoCRUD avCRUD    = new AvaliacaoCRUD();
        PropinaCRUD   propCRUD  = new PropinaCRUD();

        for (Estudante e : List.copyOf(estCRUD.getEstudantes())) {
            if (e.getNif() == NIF_EST_A || e.getNif() == NIF_EST_B) {
                avCRUD.eliminarAvaliacoesPorEstudante(e.getNumeroMec());
                propCRUD.eliminarPropinasPorEstudante(e.getNumeroMec());
                estCRUD.eliminarEstudante(e.getNumeroMec());
            }
        }

        CursoCRUD cursoCRUD = new CursoCRUD();
        if (cursoCRUD.procurarPorNome(NOME_CURSO) != null)
            cursoCRUD.eliminarCurso(NOME_CURSO);

        UnidadeCurricularCRUD ucCRUD = new UnidadeCurricularCRUD();
        for (String nome : List.of(UC_Y1, UC_Y2, UC_Y3)) {
            UnidadeCurricular uc = ucCRUD.procurarPorNome(nome);
            if (uc != null) ucCRUD.eliminarUCPorId(uc.getId());
        }

        DocenteCRUD docenteCRUD = new DocenteCRUD();
        if (docenteCRUD.procurarPorNif(NIF_DOC) != null)
            docenteCRUD.eliminarDocente(NIF_DOC);

        DepartamentoCRUD depCRUD = new DepartamentoCRUD();
        if (depCRUD.procurarPorSigla(SIGLA_DEPT) != null)
            depCRUD.eliminarDepartamento(SIGLA_DEPT);
    }

    // ── Utilitário ─────────────────────────────────────────────────────────────

    /** Copia superficial de Curso com preço e anosIniciados personalizáveis. */
    private static Curso copiar(Curso original, BigDecimal preco, List<Integer> anos) {
        Curso c = new Curso(original.getNome(), original.getDuracao(), original.getDepartamento());
        c.setPrecoAnual(preco);
        c.setAnosIniciados(new ArrayList<>(anos)); // garantir lista mutável
        c.setAtivo(original.isAtivo());
        for (UnidadeCurricular uc : original.getUnidadeCurriculars()) c.adicionarUnidadeCurricular(uc);
        return c;
    }

    // ════════════════════════════════════════════════════════════════════════
    // BLOCO 1 — Ativar / Desativar Curso
    // ════════════════════════════════════════════════════════════════════════

    @Test
    @Order(1)
    @DisplayName("Curso recém-criado deve estar ativo por defeito")
    void cursoCriadoEstaAtivoPorDefeito() {
        Curso curso = cursoDAO.procurarPorNome(NOME_CURSO);
        assertNotNull(curso, "O curso deve existir no singleton");
        assertTrue(curso.isAtivo(), "Um curso recém-criado deve estar ativo");
    }

    @Test
    @Order(2)
    @DisplayName("Desativar curso muda estado para inativo")
    void desativarCursoMudaEstado() {
        Resultado<Curso> res = cursoCtrl.ativarDesativarCurso(NOME_CURSO, false);

        assertTrue(res.sucesso, "A desativação deve ter sucesso — erro: " + res.mensagemErro);
        assertFalse(res.dados.isAtivo(), "O curso deve estar inativo após desativação");

        // Confirmar que persiste lendo directamente do CSV (nova instância sem singleton)
        Curso csv = new CursoCRUD().procurarPorNome(NOME_CURSO);
        assertNotNull(csv, "O curso deve existir no CSV");
        assertFalse(csv.isAtivo(), "Estado inativo deve ter sido persistido no CSV");
    }

    @Test
    @Order(3)
    @DisplayName("Ativar curso de volta muda estado para ativo")
    void ativarCursoMudaEstado() {
        // Curso ficou inativo no teste 2 — reativar
        Resultado<Curso> res = cursoCtrl.ativarDesativarCurso(NOME_CURSO, true);

        assertTrue(res.sucesso, "A ativação deve ter sucesso — erro: " + res.mensagemErro);
        assertTrue(res.dados.isAtivo(), "O curso deve estar ativo após reativação");
    }

    @Test
    @Order(4)
    @DisplayName("Desativar curso já inativo retorna erro")
    void desativarCursoJaInativoRetornaErro() {
        cursoCtrl.ativarDesativarCurso(NOME_CURSO, false); // desativar

        Resultado<Curso> res = cursoCtrl.ativarDesativarCurso(NOME_CURSO, false); // tentar de novo

        assertFalse(res.sucesso, "Desativar curso já inativo deve retornar erro");
        assertTrue(res.mensagemErro.toLowerCase().contains("já"),
                "Mensagem deve indicar que o estado já é esse: " + res.mensagemErro);

        cursoCtrl.ativarDesativarCurso(NOME_CURSO, true); // repor para testes seguintes
    }

    @Test
    @Order(5)
    @DisplayName("Desativar curso inexistente retorna erro")
    void desativarCursoInexistenteRetornaErro() {
        Resultado<Curso> res = cursoCtrl.ativarDesativarCurso("Curso Que Não Existe", false);
        assertFalse(res.sucesso, "Deve retornar erro para curso inexistente");
    }

    // ════════════════════════════════════════════════════════════════════════
    // BLOCO 2 — Bloqueio de alteração de preço em cursos iniciados
    // ════════════════════════════════════════════════════════════════════════

    @Test
    @Order(10)
    @DisplayName("Alterar preço de curso NÃO iniciado deve ter sucesso")
    void alterarPrecoEmCursoNaoIniciado() {
        Curso original = cursoDAO.procurarPorNome(NOME_CURSO);
        assertNotNull(original, "Curso deve existir");
        assertFalse(original.isIniciado(), "Curso não deve estar iniciado neste teste");

        Resultado res = cursoCtrl.atualizarCurso(NOME_CURSO, copiar(original, BigDecimal.valueOf(1500.0), List.of()));
        assertTrue(res.sucesso, "Alterar preço de curso não iniciado deve funcionar");

        // Repor preço original
        cursoCtrl.atualizarCurso(NOME_CURSO, copiar(cursoDAO.procurarPorNome(NOME_CURSO), BigDecimal.valueOf(1000.0), List.of()));
    }

    @Test
    @Order(11)
    @DisplayName("Alterar preço de curso INICIADO deve ser bloqueado")
    void alterarPrecoEmCursoIniciadoEBloqueado() {
        // Marcar o curso como iniciado directamente no singleton
        Curso curso = cursoDAO.procurarPorNome(NOME_CURSO);
        assertNotNull(curso);
        curso.adicionarAnoIniciado(1);
        cursoDAO.atualizarCurso(NOME_CURSO, curso);

        Curso iniciado = cursoDAO.procurarPorNome(NOME_CURSO);
        assertTrue(iniciado.isIniciado(), "Curso deve estar marcado como iniciado");

        Resultado res = cursoCtrl.atualizarCurso(NOME_CURSO, copiar(iniciado, BigDecimal.valueOf(2000.0), iniciado.getAnosIniciados()));
        assertFalse(res.sucesso, "Alterar preço de curso iniciado deve ser bloqueado");
        assertTrue(res.mensagemErro.contains("Bloqueado"),
                "Mensagem deve conter 'Bloqueado': " + res.mensagemErro);

        // Repor: remover anosIniciados
        cursoDAO.atualizarCurso(NOME_CURSO, copiar(cursoDAO.procurarPorNome(NOME_CURSO), BigDecimal.valueOf(1000.0), List.of()));
    }

    @Test
    @Order(12)
    @DisplayName("Alterar nome de curso INICIADO deve ser bloqueado")
    void alterarNomeEmCursoIniciadoEBloqueado() {
        Curso curso = cursoDAO.procurarPorNome(NOME_CURSO);
        assertNotNull(curso);
        curso.adicionarAnoIniciado(1);
        cursoDAO.atualizarCurso(NOME_CURSO, curso);

        Curso iniciado = cursoDAO.procurarPorNome(NOME_CURSO);
        Curso comNomeNovo = new Curso("Nome Proibido", iniciado.getDuracao(), iniciado.getDepartamento());
        comNomeNovo.setPrecoAnual(iniciado.getPrecoAnual());
        comNomeNovo.setAnosIniciados(iniciado.getAnosIniciados());

        Resultado res = cursoCtrl.atualizarCurso(NOME_CURSO, comNomeNovo);
        assertFalse(res.sucesso, "Alterar nome de curso iniciado deve ser bloqueado");

        // Repor
        cursoDAO.atualizarCurso(NOME_CURSO, copiar(cursoDAO.procurarPorNome(NOME_CURSO), BigDecimal.valueOf(1000.0), List.of()));
    }

    @Test
    @Order(13)
    @DisplayName("Manter o mesmo preço e nome em curso iniciado deve ter sucesso")
    void manterPrecoEmCursoIniciadoFunciona() {
        Curso curso = cursoDAO.procurarPorNome(NOME_CURSO);
        assertNotNull(curso);
        curso.adicionarAnoIniciado(1);
        cursoDAO.atualizarCurso(NOME_CURSO, curso);

        Curso iniciado = cursoDAO.procurarPorNome(NOME_CURSO);
        // Cópia idêntica: mesmo nome, mesmo preço → deve passar
        Resultado res = cursoCtrl.atualizarCurso(NOME_CURSO,
                copiar(iniciado, iniciado.getPrecoAnual(), iniciado.getAnosIniciados()));
        assertTrue(res.sucesso,
                "Atualização sem mudança de preço/nome deve ter sucesso mesmo iniciado");

        // Repor
        cursoDAO.atualizarCurso(NOME_CURSO, copiar(cursoDAO.procurarPorNome(NOME_CURSO), BigDecimal.valueOf(1000.0), List.of()));
    }

    // ════════════════════════════════════════════════════════════════════════
    // BLOCO 3 — Estudante concluído fica inativo após passagem de ano
    // ════════════════════════════════════════════════════════════════════════

    @Test
    @Order(20)
    @DisplayName("Estudante recém-registado está ativo")
    void estudanteNovoEstaAtivo() {
        Estudante est = estudanteDAO.lerEstudante(mecA);
        assertNotNull(est, "Estudante A deve existir");
        assertTrue(est.isAtivo(), "Estudante recém-registado deve estar ativo");
    }

    @Test
    @Order(21)
    @DisplayName("Estudante com nota insuficiente fica retido no ano 1")
    void estudanteRetidoNaoAvanca() {
        Estudante estB = estudanteDAO.lerEstudante(mecB);
        assertNotNull(estB, "Estudante B deve existir");

        avaliacaoDAO.registarAvaliacao(
                new Avaliacao("T1", 5.0, ucDAO.procurarPorNome(UC_Y1), estB));

        estB.setListaAvaliacoes(avaliacaoDAO.listarPorEstudante(mecB));
        Curso curso = cursoDAO.procurarPorNome(NOME_CURSO);

        int ano = BLL.EstudanteCalculo.calcularAnoDesbloqueado(estB, curso);
        assertEquals(1, ano, "Aluno com nota < 9.5 deve ficar retido no ano 1");
        assertTrue(estB.isAtivo(), "Estudante retido deve continuar ativo");
    }

    @Test
    @Order(22)
    @DisplayName("verificarSeCursoConcluido retorna false sem notas aprovadas em todos os anos")
    void cursoNaoConcluido() {
        Estudante estA = estudanteDAO.lerEstudante(mecA);
        assertNotNull(estA);
        estA.setListaAvaliacoes(avaliacaoDAO.listarPorEstudante(mecA));
        assertFalse(estCtrl.verificarSeCursoConcluido(estA),
                "Curso não deve estar concluído sem notas aprovadas em todos os anos");
    }

    @Test
    @Order(23)
    @DisplayName("Estudante com todas UCs aprovadas e propinas pagas fica inativo após passagem de ano")
    void estudanteConcluidoFicaInativo() {
        Estudante estA = estudanteDAO.lerEstudante(mecA);
        assertNotNull(estA, "Estudante A deve existir");

        // Registar notas de aprovação (≥ 9.5) para as 3 UCs, via o singleton já criado
        for (String nomeUC : List.of(UC_Y1, UC_Y2, UC_Y3)) {
            avaliacaoDAO.registarAvaliacao(
                    new Avaliacao("T1", 14.0, ucDAO.procurarPorNome(nomeUC), estA));
        }

        // Pagar propinas dos 3 anos
        for (int ano = 1; ano <= 3; ano++) {
            propCtrl.gerarPropinaAnual(mecA, ano);
            Propina p = propinaDAO.procurarPropina(mecA, ano);
            if (p != null) propCtrl.pagarPropina(mecA, ano, p.getValorTotal());
        }

        // Verificar que o sistema reconhece a conclusão
        estA.setListaAvaliacoes(avaliacaoDAO.listarPorEstudante(mecA));
        assertTrue(estCtrl.verificarSeCursoConcluido(estA),
                "Com todas as UCs aprovadas e propinas pagas, o curso deve estar concluído");

        // Simular passagem de ano — deve desativar estA
        Resultado<List<String>> res = estCtrl.simularTransicaoAnoLetivoGlobal();
        assertTrue(res.sucesso, "A transição deve ter sucesso");

        // Verificar desativação via singleton (atualizado por simularTransicao)
        Estudante estAAtualizado = estudanteDAO.lerEstudante(mecA);
        assertFalse(estAAtualizado.isAtivo(),
                "Estudante que concluiu o curso deve ficar inativo após passagem de ano");

        boolean temConcluido = res.dados.stream()
                .anyMatch(l -> l.contains("[CONCLUÍDO]") && l.contains(String.valueOf(mecA)));
        assertTrue(temConcluido,
                "Relatório deve indicar [CONCLUÍDO] para o aluno que terminou o curso");
    }

    @Test
    @Order(24)
    @DisplayName("Estudante retido continua ativo após passagem de ano")
    void estudanteRetidoContinuaAtivo() {
        Estudante estB = estudanteDAO.lerEstudante(mecB);
        assertNotNull(estB, "Estudante B deve existir");
        assertTrue(estB.isAtivo(), "Estudante retido deve continuar ativo após passagem de ano");
    }
}
