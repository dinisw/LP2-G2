package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CursoModelTest {

    private Curso curso;
    private Departamento dep;

    @BeforeEach
    void setup() {
        dep = new Departamento("Informática", "DEI");
        curso = new Curso("Engenharia", 3, dep);
    }

    @Test
    void cursoNaoIniciado_IsIniciado_RetornaFalse() {
        assertFalse(curso.isIniciado(), "Curso recém-criado não deve estar iniciado.");
    }

    @Test
    void aposAdicionarAno_IsIniciado_RetornaTrue() {
        curso.adicionarAnoIniciado(1);
        assertTrue(curso.isIniciado());
    }

    @Test
    void isAnoIniciado_AnoNaoAdicionado_RetornaFalse() {
        assertFalse(curso.isAnoIniciado(1));
    }

    @Test
    void isAnoIniciado_AnoAdicionado_RetornaTrue() {
        curso.adicionarAnoIniciado(2);
        assertTrue(curso.isAnoIniciado(2));
        assertFalse(curso.isAnoIniciado(1));
    }

    @Test
    void adicionarAnoIniciado_Duplicado_NaoDuplicaLista() {
        curso.adicionarAnoIniciado(1);
        curso.adicionarAnoIniciado(1);
        assertEquals(1, curso.getAnosIniciados().size(),
                "Adicionar o mesmo ano duas vezes não deve duplicar.");
    }

    @Test
    void adicionarUC_Ate5PorAno_Sucesso() {
        for (int i = 1; i <= 5; i++) {
            UnidadeCurricular uc = new UnidadeCurricular("UC" + i, 1, 1, null);
            assertTrue(curso.adicionarUnidadeCurricular(uc),
                    "Deve aceitar até 5 UCs por ano.");
        }
        assertEquals(5, curso.getUnidadeCurriculars().size());
    }

    @Test
    void adicionarUC_6aNoMesmoAno_Bloqueada() {
        for (int i = 1; i <= 5; i++) {
            curso.adicionarUnidadeCurricular(new UnidadeCurricular("UC" + i, 1, 1, null));
        }
        boolean resultado = curso.adicionarUnidadeCurricular(
                new UnidadeCurricular("UC6", 1, 1, null));
        assertFalse(resultado, "6ª UC no mesmo ano deve ser bloqueada.");
        assertEquals(5, curso.getUnidadeCurriculars().size());
    }

    @Test
    void adicionarUCDuplicada_Bloqueada() {
        UnidadeCurricular uc = new UnidadeCurricular("Programação", 1, 1, null);
        curso.adicionarUnidadeCurricular(uc);

        UnidadeCurricular duplicada = new UnidadeCurricular("Programação", 1, 2, null);
        boolean resultado = curso.adicionarUnidadeCurricular(duplicada);
        assertFalse(resultado, "UC com nome duplicado deve ser bloqueada.");
    }

    @Test
    void adicionarUCNull_Bloqueada() {
        boolean resultado = curso.adicionarUnidadeCurricular(null);
        assertFalse(resultado, "UC null deve ser rejeitada.");
    }

    @Test
    void ucsDeAnosDiferentes_NaoInterferemNoLimite() {
        // 5 do ano 1 + 1 do ano 2 → deve funcionar
        for (int i = 1; i <= 5; i++) {
            curso.adicionarUnidadeCurricular(new UnidadeCurricular("UC_A1_" + i, 1, 1, null));
        }
        boolean resultado = curso.adicionarUnidadeCurricular(
                new UnidadeCurricular("UC_A2_1", 2, 1, null));
        assertTrue(resultado, "Limite de 5 é por ano, não global.");
    }
}