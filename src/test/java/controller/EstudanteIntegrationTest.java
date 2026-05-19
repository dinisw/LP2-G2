package controller;

import DAL.EstudanteCRUD;
import model.Estudante;
import model.Resultado;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class EstudanteIntegrationTest {

    private EstudanteController controller = new EstudanteController();
    private EstudanteCRUD crud = new EstudanteCRUD();
    private int mecanograficoGerado = 0;

    @Test
    public void testarGeracaoMecanograficaEAtualizacaoCaseSensitive() {
        // 1. Criar um estudante
        int nifAleatorio = (int) (Math.random() * 1000000000);
        Resultado <Integer> res = controller.registarEstudante("Aluno Teste", "Morada", nifAleatorio, LocalDate.of(2002, 2, 2), "Engenharia", "Senha123!");
        if (!res.sucesso) {
            System.out.println("[DEBUG_LOG] Erro ao registar estudante: " + res.mensagemErro);
        }
        assertTrue(res.sucesso, "Estudante deve ser registado com sucesso.");

        // Guardar o Nº gerado para limparmos depois
        mecanograficoGerado = (int) res.dados;
        assertTrue(mecanograficoGerado > 10000, "O número mecanográfico deve ser superior a 10000.");

        // 2. Testar Atualização com Case Sensitive ("ENGENHARIA" em minúsculas com espaços extra)
        Resultado <Estudante> resUpdate = controller.atualizarEstudante(mecanograficoGerado, "aluno teste modificado", " ", "  engenharia  ");

        assertTrue(resUpdate.sucesso, "A atualização deve funcionar mesmo com formatações estranhas de texto.");

        Estudante atualizado = controller.procurarEstudantePorNumeroMec(mecanograficoGerado);
        assertEquals("aluno teste modificado", atualizado.getNome(), "O nome devia ter sido atualizado.");
    }

    @Test
    public void testarNifNegativoOuZero() {
        // Tentar enviar um NIF 0 ou negativo
        Resultado <Integer> res = controller.registarEstudante("Aluno Nif Mau", "Morada", -500, LocalDate.of(2000, 1, 1), "Curso", "Senha");

        assertFalse(res.sucesso, "O sistema deve bloquear NIFs menores ou iguais a zero.");
        assertTrue(res.mensagemErro.contains("inválido") || res.mensagemErro.contains("NIF"), "A mensagem de erro deve referir o NIF.");
    }

    @AfterEach
    public void limpeza() {
        // Limpar o estudante falso criado no teste
        if (mecanograficoGerado > 0) {
            crud.eliminarEstudante(mecanograficoGerado);
        }
    }
}