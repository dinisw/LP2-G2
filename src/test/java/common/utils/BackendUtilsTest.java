package common.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BackendUtilsTest {

    @Test
    public void testarNifValido() {
        // NIFs válidos (9 dígitos numéricos)
        assertTrue(BackendUtils.nifEValido("123456789"), "NIF com 9 dígitos deve ser válido.");

        // NIFs inválidos
        assertFalse(BackendUtils.nifEValido("12345678"), "NIF com 8 dígitos deve ser inválido.");
        assertFalse(BackendUtils.nifEValido("1234567890"), "NIF com 10 dígitos deve ser inválido.");
        assertFalse(BackendUtils.nifEValido("12345ABC9"), "NIF com letras deve ser inválido.");
        assertFalse(BackendUtils.nifEValido(""), "NIF vazio deve ser inválido.");
    }

    @Test
    public void testarSenhaValida() {
        // Senha forte: Pelo menos 1 maiúscula, 1 número, 1 caracter especial e 8 caracteres
        assertTrue(BackendUtils.isSenhaValida("Portugal@2026"), "Senha forte deve ser aceite.");

        // Senhas fracas
        assertFalse(BackendUtils.isSenhaValida("portugal@2026"), "Falta letra maiúscula.");
        assertFalse(BackendUtils.isSenhaValida("Portugal2026"), "Falta caracter especial.");
        assertFalse(BackendUtils.isSenhaValida("Portugal@"), "Muito curta (menos de 8 caracteres).");
    }

    @Test
    public void testarEmailGestor() {
        // Formato esperado: xxx.gestor@issmf.ipp.pt
        assertTrue(BackendUtils.emailISSMFGestorValido("joao.gestor@issmf.ipp.pt"));
        assertFalse(BackendUtils.emailISSMFGestorValido("joao.docente@issmf.ipp.pt"));
        assertFalse(BackendUtils.emailISSMFGestorValido("gestor@gmail.com"));
    }
}