package test.BLL; // Coloca na mesma package ou na raiz, dependendo de onde o criares

import model.Propina;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PropinaTest {

    @Test
    public void testarValorEmDivida() {
        Propina propina = new Propina(10101, 1, 1000.0, 250.0);
        double divida = propina.getValorEmDivida();
        assertEquals(750.0, divida, "A dívida deve ser 750 euros (1000 - 250).");
    }

    @Test
    public void testarPropinaTotalmentePaga() {
        Propina propina = new Propina(10101, 1, 1000.0, 1000.0);
        assertTrue(propina.isTotalmentePaga(), "A propina devia constar como totalmente paga.");
    }

    @Test
    public void testarPagamentoParcial() {
        Propina propina = new Propina(10101, 1, 1000.0, 0.0);
        propina.registarPagamento(300.0);

        assertEquals(300.0, propina.getValorPago(), "O valor pago devia ser atualizado para 300.");
        assertFalse(propina.isTotalmentePaga(), "A propina não deve estar paga na totalidade.");
    }
}