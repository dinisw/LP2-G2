package model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

public class CursoTest {

    @Test
    public void testarAdicionarUC() {
        Curso curso = new Curso("Design", 3, null);
        UnidadeCurricular uc = new UnidadeCurricular("Desenho 3D", 1, 1, null);

        curso.adicionarUnidadeCurricular(uc);

        assertEquals(1, curso.getUnidadeCurriculars().size(), "O curso deve ter exatamente 1 UC.");
        assertEquals("Desenho 3D", curso.getUnidadeCurriculars().get(0).getNome());
    }

    @Test
    public void testarAnosIniciados() {
        Curso curso = new Curso("Gestão", 3, null);

        // Por defeito, um curso não deve estar iniciado
        assertFalse(curso.isIniciado(), "O curso recém-criado não deve estar iniciado.");

        List<Integer> anos = new ArrayList<>();
        anos.add(1);
        curso.setAnosIniciados(anos);

        assertTrue(curso.isIniciado(), "O curso agora deve constar como iniciado.");
        assertTrue(curso.getAnosIniciados().contains(1), "O 1º ano deve estar na lista de anos iniciados.");
    }

    @Test
    public void testarNaoPermitirUCDuplicada() {
        Curso curso = new Curso("Engenharia Informática", 3, null);
        UnidadeCurricular uc = new UnidadeCurricular("Programação", 1, 1, null);

        curso.adicionarUnidadeCurricular(uc);
        // Tentar adicionar a MESMA disciplina uma segunda vez
        curso.adicionarUnidadeCurricular(uc);

        // O tamanho da lista deve ser 1, ignorando o duplicado!
        // NOTA: Se este teste falhar (ficar vermelho), significa que tens de ir à classe 'Curso.java'
        // e alterar o método 'adicionarUnidadeCurricular' para verificar se a UC já existe na lista antes de fazer '.add()'
        assertEquals(1, curso.getUnidadeCurriculars().size(), "O curso não pode ter UCs duplicadas.");
    }
}