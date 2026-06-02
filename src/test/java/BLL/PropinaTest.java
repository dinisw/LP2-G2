package BLL; // Coloca na mesma package ou na raiz, dependendo de onde o criares

import model.Propina;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal; // <-- NOVO IMPORT
import static org.junit.jupiter.api.Assertions.*;

public class PropinaTest {

    @Test
    public void testarValorEmDivida() {
        Propina propina = new Propina(10101, 1, BigDecimal.valueOf(1000.0), BigDecimal.valueOf(250.0));
        BigDecimal divida = propina.getValorEmDivida();
        assertEquals(0, divida.compareTo(BigDecimal.valueOf(750.0)), "A dívida deve ser 750 euros (1000 - 250).");
    }

    @Test
    public void testarPropinaTotalmentePaga() {
        Propina propina = new Propina(10101, 1, BigDecimal.valueOf(1000.0), BigDecimal.valueOf(1000.0));
        assertTrue(propina.isTotalmentePaga(), "A propina devia constar como totalmente paga.");
    }

    @Test
    public void testarPagamentoParcial() {
        Propina propina = new Propina(10101, 1, BigDecimal.valueOf(1000.0), BigDecimal.ZERO);

        propina.registarPagamento(BigDecimal.valueOf(300.0));

        assertEquals(0, propina.getValorPago().compareTo(BigDecimal.valueOf(300.0)), "O valor pago devia ser atualizado para 300.");
        assertFalse(propina.isTotalmentePaga(), "A propina não deve estar paga na totalidade.");
    }
}