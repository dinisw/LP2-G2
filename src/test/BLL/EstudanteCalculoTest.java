package test.BLL;

import model.Avaliacao;
import model.Curso;
import model.Estudante;
import model.UnidadeCurricular;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

public class EstudanteCalculoTest {

    private Curso cursoEngenharia;
    private Estudante aluno;

    @BeforeEach
    public void setup() {
        // Antes de CADA teste, criamos um curso e um aluno limpo!
        cursoEngenharia = new Curso("Engenharia", 3, null);

        // Adicionar 5 UCs de 1º Ano
        for(int i=1; i<=5; i++) {
            cursoEngenharia.adicionarUnidadeCurricular(new UnidadeCurricular("UC " + i, 1, 1, null));
        }

        aluno = new Estudante("João", "Rua X", 123456789, LocalDate.of(2000, 1, 1), "joao@email.com", 10001, "hash", "Engenharia", true);
    }

    @Test
    public void testarAlunoRetidoNoPrimeiroAno() {
        for (UnidadeCurricular uc : cursoEngenharia.getUnidadeCurriculars()) {
            aluno.adicionarAvaliacao(new Avaliacao("Época Normal", 8.0, uc, aluno));
        }

        int ano = EstudanteCalculo.calcularAnoDesbloqueado(aluno, cursoEngenharia);
        assertEquals(1, ano, "Aluno reprovou a tudo, deve ficar no 1º ano.");
    }

    @Test
    public void testarAlunoPassaParaSegundoAno() {
        int count = 0;
        for (UnidadeCurricular uc : cursoEngenharia.getUnidadeCurriculars()) {
            double nota = (count < 3) ? 14.0 : 8.0; // 3 positivas, 2 negativas (60%)
            aluno.adicionarAvaliacao(new Avaliacao("Época Normal", nota, uc, aluno));
            count++;
        }

        int ano = EstudanteCalculo.calcularAnoDesbloqueado(aluno, cursoEngenharia);
        assertEquals(2, ano, "Aluno teve 60% de aprovação, devia transitar para o 2º ano.");
    }
}