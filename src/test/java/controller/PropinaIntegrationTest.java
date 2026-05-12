package controller;

import model.Resultado;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PropinaIntegrationTest {

    private PropinaController controller = new PropinaController();

    @Test
    public void testarImpedirPagamentosNegativosOuZero() {
        // Tentar pagar 0 euros ou valor negativo
        Resultado resZero = controller.pagarPropina(99999, 1, 0.0);
        Resultado resNegativo = controller.pagarPropina(99999, 1, -50.0);

        assertFalse(resZero.sucesso, "O sistema não pode aceitar pagamentos de 0 euros.");
        assertFalse(resNegativo.sucesso, "O sistema não pode aceitar pagamentos negativos.");
    }

    @Test
    public void testarImpedirPagamentoSuperiorADivida() {
        // 1. Gerar uma propina falsa (ano letivo 99 para não chocar com dados reais)
        int mecTeste = 88888;
        controller.gerarPropinaAnual(mecTeste, 99);

        // 2. Tentar pagar 2000€ (a propina é só de 1000€)
        Resultado resExcesso = controller.pagarPropina(mecTeste, 99, 2000.0);

        assertFalse(resExcesso.sucesso, "O sistema deve impedir que o aluno pague mais do que o valor da dívida.");
        assertTrue(resExcesso.mensagemErro.contains("excede"), "A mensagem deve avisar que o valor excede a dívida.");

        // NOTA: Como não fizeste um método de "Eliminar Propina", esta propina do ano 99 vai ficar no teu CSV.
        // Podes apagá-la à mão do ficheiro 'propinas.csv' depois de correres o teste!
    }
}