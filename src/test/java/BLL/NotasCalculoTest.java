package BLL;

import model.Avaliacao;
import model.UnidadeCurricular;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

public class NotasCalculoTest {

    @Test
    public void testarCalculoMediaComNotasValidas() {
        List<Avaliacao> pauta = new ArrayList<>();
        UnidadeCurricular uc1 = new UnidadeCurricular("Matemática", 1, 1, null);
        UnidadeCurricular uc2 = new UnidadeCurricular("Física", 1, 1, null);
        UnidadeCurricular uc3 = new UnidadeCurricular("Programação", 1, 2, null);

        // Adicionar notas (12 + 16 = 28 / 2 = 14)
        pauta.add(new Avaliacao("Época Normal", 12.0, uc1, null));
        pauta.add(new Avaliacao("Época Normal", 16.0, uc2, null));

        // Esta nota está nula (o aluno inscreveu-se mas ainda não fez exame), deve ser ignorada na média!
        pauta.add(new Avaliacao("Época Normal", null, uc3, null));

        double media = NotasCalculo.calcularMedia(pauta);

        // O delta 0.01 serve para tolerar pequenas diferenças nas casas decimais
        assertEquals(14.0, media, 0.01, "A média calculada deve ser 14.0");
    }

    @Test
    public void testarCalculoMediaSemNotasLancadas() {
        List<Avaliacao> pauta = new ArrayList<>();
        // Aluno tem pauta vazia
        double media = NotasCalculo.calcularMedia(pauta);
        assertEquals(0.0, media, "Se não há notas, a média deve ser 0.");
    }
}