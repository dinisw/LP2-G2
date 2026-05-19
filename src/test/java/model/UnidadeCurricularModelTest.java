package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UnidadeCurricularModelTest {

    private UnidadeCurricular uc;

    @BeforeEach
    void setup() {
        uc = new UnidadeCurricular("Matemática", 1, 1, null);
    }

    @Test
    void novaUC_ListaMomentosVazia() {
        assertTrue(uc.getMomentosAvaliacao().isEmpty(),
                "UC recém-criada deve ter lista de momentos vazia.");
    }

    @Test
    void adicionarMomentoValido_AdicionadoCorrectamente() {
        uc.adicionarMomento("Frequência");
        assertEquals(1, uc.getMomentosAvaliacao().size());
        assertEquals("Frequência", uc.getMomentosAvaliacao().get(0));
    }

    @Test
    void adicionarMomentoNull_Ignorado() {
        uc.adicionarMomento(null);
        assertTrue(uc.getMomentosAvaliacao().isEmpty(),
                "Momento null deve ser ignorado.");
    }

    @Test
    void adicionarMomentoVazio_Ignorado() {
        uc.adicionarMomento("   ");
        assertTrue(uc.getMomentosAvaliacao().isEmpty(),
                "Momento vazio/espaços deve ser ignorado.");
    }

    @Test
    void adicionarAte3Momentos_Sucesso() {
        uc.adicionarMomento("T1");
        uc.adicionarMomento("T2");
        uc.adicionarMomento("Exame");
        assertEquals(3, uc.getMomentosAvaliacao().size());
    }

    @Test
    void setMomentosAvaliacao_SubstituiLista() {
        uc.adicionarMomento("Antigo");
        List<String> novaLista = new ArrayList<>(List.of("Novo T1", "Novo T2"));
        uc.setMomentosAvaliacao(novaLista);

        assertEquals(2, uc.getMomentosAvaliacao().size());
        assertFalse(uc.getMomentosAvaliacao().contains("Antigo"),
                "setMomentosAvaliacao deve substituir completamente.");
    }

    @Test
    void momentoLancadoNaUC_EstaNaLista() {
        uc.adicionarMomento("Frequência");
        assertTrue(uc.getMomentosAvaliacao().contains("Frequência"));
    }

    @Test
    void momentoNaoDefinido_NaoEstaLista() {
        uc.adicionarMomento("Frequência");
        assertFalse(uc.getMomentosAvaliacao().contains("Recurso"),
                "'Recurso' não foi adicionado, não deve estar na lista.");
    }
}