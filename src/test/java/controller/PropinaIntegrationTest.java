package controller;

import DAL.DAOFactory;
import DAL.PropinaCRUD;
import model.Resultado;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal; // <-- ADICIONADO: Import necessário

import static org.junit.jupiter.api.Assertions.*;

public class PropinaIntegrationTest {

    private PropinaController controller;

    @BeforeEach
    public void setup() {
        DAOFactory.setModo("CSV"); // forçar CSV — estudante mecTeste=88888 não existe em SQL
        controller = new PropinaController();
    }

    @AfterEach
    public void teardown() {
        // Remove a propina fictícia (ano 99) criada em testarImpedirPagamentoSuperiorADivida,
        // evitando poluir propinas.csv entre execuções (eliminarPropinasPorEstudante já existe em PropinaCRUD).
        new PropinaCRUD().eliminarPropinasPorEstudante(88888);
    }

    @Test
    public void testarImpedirPagamentosNegativosOuZero() {
        // CORREÇÃO: Usar BigDecimal.ZERO e BigDecimal.valueOf()
        Resultado resZero = controller.pagarPropina(99999, 1, BigDecimal.ZERO);
        Resultado resNegativo = controller.pagarPropina(99999, 1, BigDecimal.valueOf(-50.0));

        assertFalse(resZero.sucesso, "O sistema não pode aceitar pagamentos de 0 euros.");
        assertFalse(resNegativo.sucesso, "O sistema não pode aceitar pagamentos negativos.");
    }

    @Test
    public void testarImpedirPagamentoSuperiorADivida() {
        // 1. Gerar uma propina falsa (ano letivo 99 para não chocar com dados reais)
        int mecTeste = 88888;
        controller.gerarPropinaAnual(mecTeste, 99);

        // 2. Tentar pagar 2000€ (a propina é só de 1000€)
        // CORREÇÃO: Usar BigDecimal.valueOf()
        Resultado resExcesso = controller.pagarPropina(mecTeste, 99, BigDecimal.valueOf(2000.0));

        assertFalse(resExcesso.sucesso, "O sistema deve impedir que o aluno pague mais do que o valor da dívida.");

        // CORREÇÃO: A mensagem no seu Controller usa a palavra "superior" e não "excede"
        assertTrue(resExcesso.mensagemErro.contains("superior"), "A mensagem deve avisar que o valor excede a dívida.");
    }
}