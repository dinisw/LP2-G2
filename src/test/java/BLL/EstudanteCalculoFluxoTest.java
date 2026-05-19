package BLL;

import model.*;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testa a lógica de cálculo de progressão de ano e aprovação de UCs.
 * Todos os testes são unitários puros (sem acesso a CSV).
 * NOTA: Os testes do EstudanteCalculoTest existente estão quebrados porque
 *       criam UCs sem momentos de avaliação, o que faz isUCAprovada retornar sempre false.
 */
public class EstudanteCalculoFluxoTest {

    private Curso curso;
    private Estudante aluno;

    private static UnidadeCurricular ucComMomentos(String nome, int ano, String... momentos) {
        UnidadeCurricular uc = new UnidadeCurricular(nome, ano, 1, null);
        for (String m : momentos) uc.adicionarMomento(m);
        return uc;
    }

    @BeforeEach
    void setup() {
        Departamento dep = new Departamento("DEI", "DEI");
        curso = new Curso("Engenharia Informática", 3, dep);
        aluno = new Estudante("Ana Silva", "Rua A", 123456789,
                LocalDate.of(2000, 1, 1), "ana@issmf.ipp.pt", 10001, "hash",
                "Engenharia Informática", true);
    }

    // ===== isUCAprovada (testada indirectamente via isCursoConcluido) =====

    @Test
    void ucComUmMomento_NotaAcimaDe9v5_Aprovada() {
        UnidadeCurricular uc = ucComMomentos("Prog I", 1, "Frequência");
        curso.adicionarUnidadeCurricular(uc);
        aluno.adicionarAvaliacao(new Avaliacao("Frequência", 12.0, uc, aluno));

        assertTrue(EstudanteCalculo.isCursoConcluido(aluno, curso),
                "Nota 12.0 num único momento deve resultar em aprovação.");
    }

    @Test
    void ucComUmMomento_NotaExatamente9v5_Aprovada() {
        UnidadeCurricular uc = ucComMomentos("Matemática", 1, "Exame");
        curso.adicionarUnidadeCurricular(uc);
        aluno.adicionarAvaliacao(new Avaliacao("Exame", 9.5, uc, aluno));

        assertTrue(EstudanteCalculo.isCursoConcluido(aluno, curso),
                "Nota exactamente 9.5 é aprovação (>=).");
    }

    @Test
    void ucComUmMomento_NotaAbaixoDe9v5_Reprovada() {
        UnidadeCurricular uc = ucComMomentos("Prog I", 1, "Frequência");
        curso.adicionarUnidadeCurricular(uc);
        aluno.adicionarAvaliacao(new Avaliacao("Frequência", 9.4, uc, aluno));

        assertFalse(EstudanteCalculo.isCursoConcluido(aluno, curso),
                "Nota 9.4, abaixo de 9.5, deve resultar em reprovação.");
    }

    @Test
    void ucComDoisMomentos_MediaAcimaDeLimite_Aprovada() {
        UnidadeCurricular uc = ucComMomentos("BD", 1, "Frequência", "Exame");
        curso.adicionarUnidadeCurricular(uc);
        // Média = (12 + 9) / 2 = 10.5 → aprovado
        aluno.adicionarAvaliacao(new Avaliacao("Frequência", 12.0, uc, aluno));
        aluno.adicionarAvaliacao(new Avaliacao("Exame", 9.0, uc, aluno));

        assertTrue(EstudanteCalculo.isCursoConcluido(aluno, curso),
                "Média 10.5 com 2 momentos deve ser aprovação.");
    }

    @Test
    void ucComDoisMomentos_SoUmNotaLancada_DividePoloTotal() {
        // UC com 2 momentos mas só 1 nota: media = 15 / 2 = 7.5 → reprovado
        UnidadeCurricular uc = ucComMomentos("BD", 1, "Frequência", "Exame");
        curso.adicionarUnidadeCurricular(uc);
        aluno.adicionarAvaliacao(new Avaliacao("Frequência", 15.0, uc, aluno));

        assertFalse(EstudanteCalculo.isCursoConcluido(aluno, curso),
                "Com só 1 de 2 momentos lançados (15.0), média = 7.5 → reprovado.");
    }

    @Test
    void ucComTresMomentos_MediaCalculadaCorrectamente() {
        UnidadeCurricular uc = ucComMomentos("SO", 1, "T1", "T2", "Exame");
        curso.adicionarUnidadeCurricular(uc);
        // Média = (10 + 12 + 14) / 3 = 12 → aprovado
        aluno.adicionarAvaliacao(new Avaliacao("T1", 10.0, uc, aluno));
        aluno.adicionarAvaliacao(new Avaliacao("T2", 12.0, uc, aluno));
        aluno.adicionarAvaliacao(new Avaliacao("Exame", 14.0, uc, aluno));

        assertTrue(EstudanteCalculo.isCursoConcluido(aluno, curso),
                "Média de 12.0 com 3 momentos deve ser aprovação.");
    }

    @Test
    void ucSemMomentos_NuncaAprovada() {
        // UC criada sem chamar adicionarMomento
        UnidadeCurricular ucVazia = new UnidadeCurricular("UC Sem Momentos", 1, 1, null);
        curso.adicionarUnidadeCurricular(ucVazia);
        aluno.adicionarAvaliacao(new Avaliacao("Qualquer", 20.0, ucVazia, aluno));

        assertFalse(EstudanteCalculo.isCursoConcluido(aluno, curso),
                "UC sem momentos definidos nunca pode ser aprovada (isUCAprovada retorna false).");
    }

    @Test
    void notaNull_NaoContaParaMedia() {
        UnidadeCurricular uc = ucComMomentos("Prog II", 1, "Frequência");
        curso.adicionarUnidadeCurricular(uc);
        aluno.adicionarAvaliacao(new Avaliacao("Frequência", null, uc, aluno));

        assertFalse(EstudanteCalculo.isCursoConcluido(aluno, curso),
                "Avaliação com nota null não deve ser contabilizada.");
    }

    @Test
    void momentoForaDaLista_NaoContaParaMedia() {
        // UC tem "Frequência" mas a nota foi lançada para "Recurso" (não definido)
        UnidadeCurricular uc = ucComMomentos("Física", 1, "Frequência");
        curso.adicionarUnidadeCurricular(uc);
        aluno.adicionarAvaliacao(new Avaliacao("Recurso", 20.0, uc, aluno));

        assertFalse(EstudanteCalculo.isCursoConcluido(aluno, curso),
                "Nota lançada para momento não definido na UC não deve contar.");
    }

    // ===== calcularAnoDesbloqueado =====

    @Test
    void estudanteNull_RetornaAno1() {
        assertEquals(1, EstudanteCalculo.calcularAnoDesbloqueado(null, curso));
    }

    @Test
    void cursoNull_RetornaAno1() {
        assertEquals(1, EstudanteCalculo.calcularAnoDesbloqueado(aluno, null));
    }

    @Test
    void cursoSemUCs_RetornaAno1() {
        // totalInscritas == 0 → return 1
        assertEquals(1, EstudanteCalculo.calcularAnoDesbloqueado(aluno, curso),
                "Curso sem UCs deve retornar ano 1.");
    }

    @Test
    void semAvaliacoes_RetornaAno1() {
        curso.adicionarUnidadeCurricular(ucComMomentos("UC1", 1, "Exame"));
        // Nenhuma avaliação: aprovadasGlobais = 0 → fica no ano 1
        assertEquals(1, EstudanteCalculo.calcularAnoDesbloqueado(aluno, curso),
                "Sem avaliações lançadas, aluno deve ficar no 1º ano.");
    }

    @Test
    void exatamente60PorCentoAprovacao_AvancaParaAno2() {
        List<UnidadeCurricular> ucs = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            UnidadeCurricular uc = ucComMomentos("UC" + i, 1, "Frequência");
            curso.adicionarUnidadeCurricular(uc);
            ucs.add(uc);
        }
        // 3 aprovadas em 5 = 60% exacto
        for (int i = 0; i < 5; i++) {
            aluno.adicionarAvaliacao(new Avaliacao("Frequência", i < 3 ? 14.0 : 5.0, ucs.get(i), aluno));
        }

        assertEquals(2, EstudanteCalculo.calcularAnoDesbloqueado(aluno, curso),
                "Com 60% de aprovação, aluno deve avançar para o 2º ano.");
    }

    @Test
    void menosDe60PorCentoAprovacao_FicaNoMesmoAno() {
        List<UnidadeCurricular> ucs = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            UnidadeCurricular uc = ucComMomentos("UC" + i, 1, "Frequência");
            curso.adicionarUnidadeCurricular(uc);
            ucs.add(uc);
        }
        // 2 aprovadas em 5 = 40%
        for (int i = 0; i < 5; i++) {
            aluno.adicionarAvaliacao(new Avaliacao("Frequência", i < 2 ? 14.0 : 5.0, ucs.get(i), aluno));
        }

        assertEquals(1, EstudanteCalculo.calcularAnoDesbloqueado(aluno, curso),
                "Com 40% de aprovação, aluno deve ficar no 1º ano.");
    }

    @Test
    void alunoNoUltimoAnoComAprovacao_NaoUltrapassaDuracao() {
        // Aluno no 3º ano (último), passa → deve ficar no 3 (não vai para 4)
        aluno.setAnoLetivo(3);
        List<UnidadeCurricular> ucs = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            UnidadeCurricular uc = ucComMomentos("UC3_" + i, 3, "Exame");
            curso.adicionarUnidadeCurricular(uc);
            ucs.add(uc);
        }
        for (int i = 0; i < 5; i++) {
            aluno.adicionarAvaliacao(new Avaliacao("Exame", i < 3 ? 14.0 : 5.0, ucs.get(i), aluno));
        }

        int ano = EstudanteCalculo.calcularAnoDesbloqueado(aluno, curso);
        assertEquals(3, ano,
                "Aluno no último ano não pode avançar além da duração do curso.");
    }

    @Test
    void alunoAno2_SoContaUCsAteAno2_ParaCalculoAprovacao() {
        // Aluno está no ano 2: totalInscritas filtra por anoCurricular <= 2
        aluno.setAnoLetivo(2);

        UnidadeCurricular uc1 = ucComMomentos("UC_ANO1", 1, "Exame");
        UnidadeCurricular uc2 = ucComMomentos("UC_ANO2", 2, "Exame");
        UnidadeCurricular uc3 = ucComMomentos("UC_ANO3", 3, "Exame"); // não conta
        curso.adicionarUnidadeCurricular(uc1);
        curso.adicionarUnidadeCurricular(uc2);
        curso.adicionarUnidadeCurricular(uc3);

        // Aprova UC1 e UC2 (100% das UCs até ano 2) → avança para 3
        aluno.adicionarAvaliacao(new Avaliacao("Exame", 12.0, uc1, aluno));
        aluno.adicionarAvaliacao(new Avaliacao("Exame", 12.0, uc2, aluno));

        assertEquals(3, EstudanteCalculo.calcularAnoDesbloqueado(aluno, curso));
    }

    // ===== isCursoConcluido =====

    @Test
    void todasUCsAprovadas_CursoConcluido() {
        UnidadeCurricular uc1 = ucComMomentos("UC1", 1, "Exame");
        UnidadeCurricular uc2 = ucComMomentos("UC2", 2, "Exame");
        curso.adicionarUnidadeCurricular(uc1);
        curso.adicionarUnidadeCurricular(uc2);

        aluno.adicionarAvaliacao(new Avaliacao("Exame", 12.0, uc1, aluno));
        aluno.adicionarAvaliacao(new Avaliacao("Exame", 15.0, uc2, aluno));

        assertTrue(EstudanteCalculo.isCursoConcluido(aluno, curso));
    }

    @Test
    void umaUCReprovada_CursoNaoConcluido() {
        UnidadeCurricular uc1 = ucComMomentos("UC1", 1, "Exame");
        UnidadeCurricular uc2 = ucComMomentos("UC2", 2, "Exame");
        curso.adicionarUnidadeCurricular(uc1);
        curso.adicionarUnidadeCurricular(uc2);

        aluno.adicionarAvaliacao(new Avaliacao("Exame", 12.0, uc1, aluno));
        aluno.adicionarAvaliacao(new Avaliacao("Exame", 5.0, uc2, aluno));

        assertFalse(EstudanteCalculo.isCursoConcluido(aluno, curso));
    }

    @Test
    void cursoSemUCsNaoPodeEstarConcluido() {
        assertFalse(EstudanteCalculo.isCursoConcluido(aluno, curso),
                "Curso sem UCs definidas nunca pode estar concluído.");
    }

    @Test
    void estudanteNullNaoPodeEstarConcluido() {
        curso.adicionarUnidadeCurricular(ucComMomentos("UC1", 1, "Exame"));
        assertFalse(EstudanteCalculo.isCursoConcluido(null, curso));
    }
}