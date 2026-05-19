package model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UnidadeCurricularTest {

    @Test
    public void testarAdicionarMomentosAvaliacao() {
        UnidadeCurricular uc = new UnidadeCurricular("Matemática", 1, 1, null);

        // Adicionar momentos válidos
        uc.adicionarMomento("Frequência");
        uc.adicionarMomento("Exame Final");

        assertEquals(2, uc.getMomentosAvaliacao().size(), "A UC deve ter 2 momentos registados.");
        assertTrue(uc.getMomentosAvaliacao().contains("Frequência"));
    }

    @Test
    public void testarAssociacaoDocente() {
        Docente professor = new Docente("Carlos Silva", "Rua A", 123123123, null, "carlos@issmf.ipp.pt", "hash", "CAS", null, null);
        UnidadeCurricular uc = new UnidadeCurricular("Física", 1, 2, professor);

        assertNotNull(uc.getDocente(), "A UC deve ter um docente associado.");
        assertEquals("CAS", uc.getDocente().getSigla(), "A sigla do docente deve coincidir.");
    }
}