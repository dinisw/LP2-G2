package controller;

import DAL.GestorCRUD;
import model.Gestor;
import model.Resultado;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class GestorIntegrationTest {

    private GestorController controller;
    private GestorCRUD crud;

    @BeforeEach
    public void setup() {
        controller = new GestorController();
        crud = new GestorCRUD();
    }

    @Test
    public void testarNaoPermitirNifDuplicado() {
        int nifTeste = 999888777;

        // 1. Criar o primeiro gestor (deve funcionar)
        Resultado res1 = controller.registarGestor("Gestor Um", "Rua A", nifTeste, LocalDate.of(1990, 1, 1), "um.gestor@issmf.ipp.pt", "HashForte123!", "Diretor");
        assertTrue(res1.success, "O primeiro gestor deveria ser registado com sucesso.");

        // 2. Tentar criar um SEGUNDO gestor com o MESMO NIF (deve falhar!)
        Resultado res2 = controller.registarGestor("Gestor Dois", "Rua B", nifTeste, LocalDate.of(1985, 5, 5), "dois.gestor@issmf.ipp.pt", "HashForte123!", "Subdiretor");

        assertFalse(res2.success, "O sistema não pode permitir o registo de dois gestores com o mesmo NIF!");
        System.out.println("Mensagem de erro capturada com sucesso: " + res2.errorMessage);

        // Limpeza (para não poluir o CSV)
        crud.eliminarGestor(nifTeste);
    }

    @Test
    public void testarBloqueioDeCamposEmBranco() {
        // Tentar enviar o Nome vazio e a Morada apenas com espaços
        Resultado res = controller.registarGestor("", "   ", 123456789, LocalDate.of(1990, 1, 1), "teste.gestor@issmf.ipp.pt", "Hash", "Cargo");

        assertFalse(res.success, "O sistema deve bloquear o registo se campos obrigatórios estiverem em branco.");
        assertNotNull(res.errorMessage, "O sistema deve devolver uma mensagem de erro clara.");
    }
}