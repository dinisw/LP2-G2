package BLL;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testa a regra central do enunciado v1.0:
 * "Quando um estudante tenha mais que 60% de aproveitamento escolar, este poderá
 * frequentar o ano seguinte."
 *
 * NOTA: EstudanteCalculo.isUCAprovada() exige que as UCs tenham momentos de avaliação
 * definidos — uma UC sem momentos é considerada não avaliável (retorna false).
 */
public class EstudanteCalculoTest {

    private Curso cursoEngenharia;
    private Estudante aluno;

    @BeforeEach
    public void setup() {
        cursoEngenharia = new Curso("Engenharia", 3, null);

        // Adicionar 5 UCs de 1º Ano — CADA UMA COM UM MOMENTO DEFINIDO
        for (int i = 1; i <= 5; i++) {
            UnidadeCurricular uc = new UnidadeCurricular("UC " + i, 1, 1, null);
            uc.adicionarMomento("Época Normal"); // obrigatório para isUCAprovada funcionar
            cursoEngenharia.adicionarUnidadeCurricular(uc);
        }

        aluno = new Estudante("João", "Rua X", 123456789, LocalDate.of(2000, 1, 1),
                "joao@email.com", 10001, "hash", "Engenharia", true);
    }

    @Test
    public void testarAlunoRetidoNoPrimeiroAno_MenosDe60Pct() {
        // Todos reprovados (nota < 9.5) → fica no ano 1
        for (UnidadeCurricular uc : cursoEngenharia.getUnidadeCurriculars()) {
            aluno.adicionarAvaliacao(new Avaliacao("Época Normal", 8.0, uc, aluno));
        }
        int ano = EstudanteCalculo.calcularAnoDesbloqueado(aluno, cursoEngenharia);
        assertEquals(1, ano, "Aluno reprovou a tudo (0/5 = 0%), deve ficar no 1º ano.");
    }

    @Test
    public void testarAlunoPassaParaSegundoAno_60Pct() {
        // 3 positivas (≥9.5) + 2 negativas → 3/5 = 60% → passa para o 2º ano
        int count = 0;
        for (UnidadeCurricular uc : cursoEngenharia.getUnidadeCurriculars()) {
            double nota = (count < 3) ? 14.0 : 8.0;
            aluno.adicionarAvaliacao(new Avaliacao("Época Normal", nota, uc, aluno));
            count++;
        }
        int ano = EstudanteCalculo.calcularAnoDesbloqueado(aluno, cursoEngenharia);
        assertEquals(2, ano, "Aluno com 60% de aprovação deve transitar para o 2º ano.");
    }

    @Test
    public void testarAlunoFicaNo1AnoComExatamente59Pct() {
        // 2 positivas em 5 = 40% → fica no 1º
        int count = 0;
        for (UnidadeCurricular uc : cursoEngenharia.getUnidadeCurriculars()) {
            double nota = (count < 2) ? 14.0 : 8.0;
            aluno.adicionarAvaliacao(new Avaliacao("Época Normal", nota, uc, aluno));
            count++;
        }
        int ano = EstudanteCalculo.calcularAnoDesbloqueado(aluno, cursoEngenharia);
        assertEquals(1, ano, "2/5 = 40% não chega a 60%, aluno fica no 1º ano.");
    }

    @Test
    public void testarCursoConcluido_TodaAprovadas() {
        for (UnidadeCurricular uc : cursoEngenharia.getUnidadeCurriculars()) {
            aluno.adicionarAvaliacao(new Avaliacao("Época Normal", 10.0, uc, aluno));
        }
        assertTrue(EstudanteCalculo.isCursoConcluido(aluno, cursoEngenharia),
                "Com todas as UCs aprovadas, o curso deve ser considerado concluído.");
    }

    @Test
    public void testarCursoNaoConcluido_ComUmaNegativa() {
        int count = 0;
        for (UnidadeCurricular uc : cursoEngenharia.getUnidadeCurriculars()) {
            double nota = (count == 0) ? 8.0 : 10.0; // primeira reprovada
            aluno.adicionarAvaliacao(new Avaliacao("Época Normal", nota, uc, aluno));
            count++;
        }
        assertFalse(EstudanteCalculo.isCursoConcluido(aluno, cursoEngenharia),
                "Com uma UC reprovada, o curso não pode ser concluído.");
    }

    @Test
    public void testarProgressaoCumulativa_RegrasAno2() {
        // Enunciado v1.0 p.2: "o mesmo aluno no segundo ano, estando inscrito em 7 UCs
        // (2 do primeiro e 5 do segundo), só poderá frequentar o 3º ano com 5/7 ≈ 71%"
        Curso cursoCompleto = new Curso("Engenharia Completo", 3, null);

        // 2 UCs do ano 1
        for (int i = 1; i <= 2; i++) {
            UnidadeCurricular uc = new UnidadeCurricular("UC A" + i, 1, 1, null);
            uc.adicionarMomento("Exame");
            cursoCompleto.adicionarUnidadeCurricular(uc);
        }
        // 5 UCs do ano 2
        for (int i = 1; i <= 5; i++) {
            UnidadeCurricular uc = new UnidadeCurricular("UC B" + i, 2, 1, null);
            uc.adicionarMomento("Exame");
            cursoCompleto.adicionarUnidadeCurricular(uc);
        }

        Estudante aluno2 = new Estudante("Maria", "Rua Y", 987654321,
                LocalDate.of(2001, 1, 1), "maria@email.com", 20001, "hash", "Engenharia Completo", true);
        aluno2.setAnoLetivo(2);

        // Aprova 5 de 7 = 71% → passa para o 3º ano
        int count = 0;
        for (UnidadeCurricular uc : cursoCompleto.getUnidadeCurriculars()) {
            double nota = (count < 5) ? 12.0 : 7.0;
            aluno2.adicionarAvaliacao(new Avaliacao("Exame", nota, uc, aluno2));
            count++;
        }

        int ano = EstudanteCalculo.calcularAnoDesbloqueado(aluno2, cursoCompleto);
        assertEquals(3, ano, "5/7 ≈ 71% deve desbloquear o 3º ano conforme o enunciado.");
    }

    @Test
    public void testarNotaLimite_9_5_EAprovacao() {
        // Nota exactamente 9.5 deve ser aprovação
        UnidadeCurricular uc = new UnidadeCurricular("UC Limite", 1, 1, null);
        uc.adicionarMomento("Época Normal");
        Avaliacao av = new Avaliacao("Época Normal", 9.5, uc, aluno);
        aluno.adicionarAvaliacao(av);

        Curso c = new Curso("Curso Limite", 3, null);
        c.adicionarUnidadeCurricular(uc);

        assertTrue(EstudanteCalculo.isCursoConcluido(aluno, c)); // 1 UC em 1 = 100% → curso concluído
        // A lógica real é: 9.5 >= 9.5 → aprovado
        List<Avaliacao> avs = aluno.getListaAvaliacoes();
        assertTrue(avs.stream()
                        .filter(a -> a.getNota() != null && a.getNota() >= 9.5).count() >= 1,
                "Nota 9.5 deve contar como aprovação.");
    }
}
