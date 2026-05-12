package controller;

import common.utils.SenhaUtils;
import model.*;
import org.junit.jupiter.api.*;

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
        c.setPrecoAnual(1000.0); // Definir propina base inicial para o 1º Ano
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

        Avaliacao nota1 = new Avaliacao("Frequência 1", 10.0, uc, estudante);
        avalController.registarAvaliacao(nota1);

        Avaliacao nota2 = new Avaliacao("Frequência 1", 8.0, uc, estudante);
        avalController.registarAvaliacao(nota2);

        AvaliacaoController avalCalc = new AvaliacaoController();
        Resultado<String> status = avalCalc.obterStatusAprovacao(mecEstudante, NOME_UC_Y1);
        assertTrue(status.dados.contains("8,00") || status.dados.contains("8.00"), "US 14 Falhou: Upsert não substituiu a nota.");
        assertTrue(status.dados.contains("REPROVADO"), "US 14 Falhou: Aluno com 8.0 devia estar Reprovado.");
    }

    @Test
    @Order(7)
    public void US16_LoginRegexOtimizado() {
        System.out.println("Validando US 16: Identificação no Login por Regex...");
        LoginController loginController = new LoginController();

        String emailRealDoEstudante = mecEstudante + "@issmf.ipp.pt";
        Utilizador uEstudante = loginController.login(emailRealDoEstudante, "NovaSenha@2026");
        assertNotNull(uEstudante, "US 16 Falhou: Regex de estudante não identificou o utilizador.");
        assertTrue(uEstudante instanceof Estudante, "US 16 Falhou: Objeto devolvido não é um Estudante.");
    }

    @Test
    @Order(8)
    public void US12_ProtegerCursosIniciados() {
        System.out.println("Validando US 12: Integridade de Cursos Iniciados...");
        EstudanteController ec = new EstudanteController();

        // Forçar 5 alunos no curso para permitir arranque do 1º ano
        SenhaUtils su = new SenhaUtils();
        String hash = su.gerarHashComSalt("S@123");
        for (int i = 0; i < 4; i++) {
            ec.registarEstudante("Extra " + i, "R", 300000000 + ID + i, LocalDate.of(2000,1,1), NOME_CURSO, hash);
        }

        GestorController gcFresco = new GestorController();
        assertTrue(gcFresco.arrancarAnoLetivo(NOME_CURSO, 1).sucesso);

        EstudanteController ecFresco = new EstudanteController();
        Resultado<String> resDelEst = ecFresco.eliminarEstudante(mecEstudante);
        assertFalse(resDelEst.sucesso, "US 12 Falhou: Sistema permitiu apagar estudante de um curso em andamento.");

        CursoController ccFresco = new CursoController();
        Resultado resDelCurso = ccFresco.eliminarCurso(NOME_CURSO);
        assertFalse(resDelCurso.sucesso, "US 12 Falhou: Sistema permitiu apagar um curso que já arrancou.");
    }

    @Test
    @Order(9)
    public void US_Progressao_Ano1_Para_Ano2() {
        System.out.println("Validando Passagem do 1º para o 2º Ano e Faturação de Propinas...");
        AvaliacaoController avalController = new AvaliacaoController();
        UnidadeCurricularController ucController = new UnidadeCurricularController();
        EstudanteController ec = new EstudanteController();
        PropinaController pc = new PropinaController();

        // 1. Pagar a propina do 1º Ano (Se não pagar, o sistema retém o aluno no 1º Ano!)
        Resultado<Propina> resPagamento = pc.pagarPropina(mecEstudante, 1, 1000.0);
        assertTrue(resPagamento.sucesso, "Erro ao pagar a propina do 1º ano.");

        // 2. Lançar nota de aprovação (> 60%) na UC do 1º Ano
        UnidadeCurricular ucY1 = ucController.procurarUCPorNome(NOME_UC_Y1);
        Estudante estudante = ec.procurarEstudantePorNumeroMec(mecEstudante);
        Avaliacao notaAprovacao = new Avaliacao("Frequência 1", 16.0, ucY1, estudante);
        avalController.registarAvaliacao(notaAprovacao);

        // 3. Simular alteração da propina do curso no cenário real
        DAL.CursoCRUD cursoCRUD = new DAL.CursoCRUD();
        Curso cursoBD = cursoCRUD.procurarPorNome(NOME_CURSO);
        cursoBD.setPrecoAnual(1250.0); // Novo preço para anos seguintes
        cursoCRUD.atualizarCurso(NOME_CURSO, cursoBD);

        // 4. Executar o Simulador Global de Passagem de Ano
        Resultado<List<String>> simulacao = ec.simularTransicaoAnoLetivoGlobal();
        assertTrue(simulacao.sucesso);

        // --- CORREÇÃO: Instanciar controladores de FRESCO para ler o CSV atualizado ---
        EstudanteController ecFresco = new EstudanteController();
        PropinaController pcFresco = new PropinaController();

        // 5. Validar se o aluno passou efetivamente para o 2º Ano
        Estudante estudanteAtualizado = ecFresco.procurarEstudantePorNumeroMec(mecEstudante);
        int anoAtualCalculado = ecFresco.obterAnoDesbloqueado(estudanteAtualizado);
        assertEquals(2, anoAtualCalculado, "O Aluno não transitou para o 2º Ano como esperado.");

        // 6. Verificar se a propina do 2º Ano foi gerada e se tem o novo valor (1250.0)
        List<Propina> propinasGeradas = pcFresco.consultarPropinasEstudante(mecEstudante);
        Propina propinaSegundoAno = propinasGeradas.stream().filter(p -> p.getAnoLetivo() == 2).findFirst().orElse(null);

        assertNotNull(propinaSegundoAno, "A propina do 2º Ano não foi gerada!");
        assertEquals(1250.0, propinaSegundoAno.getValorTotal(), "A nova propina não assumiu o valor atualizado do curso.");
    }

    @Test
    @Order(10)
    public void US_Progressao_Ano2_Para_Ano3() {
        System.out.println("Validando Passagem do 2º para o 3º Ano...");
        AvaliacaoController avalController = new AvaliacaoController();
        UnidadeCurricularController ucController = new UnidadeCurricularController();
        EstudanteController ec = new EstudanteController();
        PropinaController pc = new PropinaController();

        // 1. Pagar a propina do 2º Ano (Valor 1250.0 configurado no teste anterior)
        pc.pagarPropina(mecEstudante, 2, 1250.0);

        // 2. Lançar notas para a UC do 2º Ano
        UnidadeCurricular ucY2 = ucController.procurarUCPorNome(NOME_UC_Y2);
        Estudante estudante = ec.procurarEstudantePorNumeroMec(mecEstudante);

        // Docente define momentos para a UC do 2º Ano
        List<String> momentos = new ArrayList<>();
        momentos.add("Exame");
        new DocenteController().definirMomentosAvaliacao(SIGLA_DOC, ucY2.getId(), momentos);

        Avaliacao notaAprovacaoY2 = new Avaliacao("Exame", 14.0, ucY2, estudante);
        avalController.registarAvaliacao(notaAprovacaoY2);

        // 3. Executar Transição Global
        ec.simularTransicaoAnoLetivoGlobal();

        // 4. Validar se passou para o 3º Ano (Ler do CSV novamente com controlador fresco)
        EstudanteController ecFresco = new EstudanteController();
        Estudante estudanteAtualizado = ecFresco.procurarEstudantePorNumeroMec(mecEstudante);
        int anoAtualCalculado = ecFresco.obterAnoDesbloqueado(estudanteAtualizado);
        assertEquals(3, anoAtualCalculado, "O Aluno não transitou para o 3º Ano.");
    }
    @Test
    @Order(11)
    public void US_Retencao_Por_Propina_Em_Atraso() {
        System.out.println("Validando Retenção: Aluno com boas notas NÃO passa de ano se dever propinas...");
        EstudanteController ec = new EstudanteController();
        PropinaController pc = new PropinaController();

        Estudante estudante = ec.procurarEstudantePorNumeroMec(mecEstudante);

        // Neste momento o aluno está no 3º Ano (porque passou no Order 10).
        // A propina do 3º Ano foi gerada. Vamos verificar que ele está em dívida.
        List<Propina> propinas = pc.consultarPropinasEstudante(mecEstudante);
        Propina propinaY3 = propinas.stream().filter(p -> p.getAnoLetivo() == 3).findFirst().orElse(null);

        assertNotNull(propinaY3, "Propina do 3º ano não encontrada.");
        assertFalse(propinaY3.isTotalmentePaga(), "A propina não devia estar paga.");

        // Simular que o aluno tira nota máxima (20) em todas as cadeiras do 3º Ano
        AvaliacaoController avalController = new AvaliacaoController();
        UnidadeCurricularController ucController = new UnidadeCurricularController();
        UnidadeCurricular ucY3 = ucController.procurarUCPorNome(NOME_UC_Y3);

        List<String> momentos = new ArrayList<>();
        momentos.add("Projeto Final");
        new DocenteController().definirMomentosAvaliacao(SIGLA_DOC, ucY3.getId(), momentos);

        Avaliacao notaAprovacaoY3 = new Avaliacao("Projeto Final", 20.0, ucY3, estudante);
        avalController.registarAvaliacao(notaAprovacaoY3);

        // Correr o simulador de fim de ano
        ec.simularTransicaoAnoLetivoGlobal();

        // VALIDAÇÃO: Mesmo com nota 20.0, o aluno NÃO pode ter o curso como Concluído, porque deve a propina do 3º ano!
        EstudanteController ecFresco = new EstudanteController();
        boolean concluiuCurso = ecFresco.verificarSeCursoConcluido(ecFresco.procurarEstudantePorNumeroMec(mecEstudante));

        assertFalse(concluiuCurso, "O sistema deixou o aluno concluir o curso sem pagar a última propina!");
    }

    @Test
    @Order(12)
    public void US_AlterarPreco_Sem_Quebrar_Regra_Estrutural() {
        System.out.println("Validando Alteração de Preço permitida em curso já iniciado...");
        CursoController cc = new CursoController();

        Curso cursoAtual = cc.procurarCurso(NOME_CURSO);

        // 1. Tentar mudar o NOME do curso (Estrutural) -> DEVE FALHAR
        Curso cursoNomeMudado = new Curso("Nome Ilegal", 3, cursoAtual.getDepartamento());
        Resultado resFalha = cc.atualizarCurso(NOME_CURSO, cursoNomeMudado);
        assertFalse(resFalha.sucesso, "O sistema falhou ao bloquear uma alteração estrutural.");

        // 2. Tentar mudar APENAS a propina (Financeiro) -> DEVE FUNCIONAR
        cursoAtual.setPrecoAnual(1500.0);
        Resultado resSucesso = cc.atualizarCurso(NOME_CURSO, cursoAtual);
        assertTrue(resSucesso.sucesso, "O sistema bloqueou a alteração da propina, o que não devia acontecer.");

        // Confirmar na BD
        CursoController ccFresco = new CursoController();
        assertEquals(1500.0, ccFresco.procurarCurso(NOME_CURSO).getPrecoAnual());
    }

    @Test
    @Order(13)
    public void US_Conclusao_Final_Apos_Pagamento() {
        System.out.println("Validando Conclusão do Curso após regularizar dívida...");
        PropinaController pc = new PropinaController();
        EstudanteController ec = new EstudanteController();

        // Pagar a propina do 3º Ano (Ainda estava com o preço anterior de 1250.0 quando foi gerada)
        pc.pagarPropina(mecEstudante, 3, 1250.0);

        // O aluno já tinha nota 20.0 (no Order 11), e agora já tem a propina paga.
        ec.simularTransicaoAnoLetivoGlobal();

        // Validar Conclusão
        EstudanteController ecFresco = new EstudanteController();
        boolean concluiuCurso = ecFresco.verificarSeCursoConcluido(ecFresco.procurarEstudantePorNumeroMec(mecEstudante));

        assertTrue(concluiuCurso, "O aluno deveria ter o curso concluído após pagar a propina e ter notas positivas.");
    }
}