package common.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SenhaUtilsTest {

    @Test
    public void testarGeracaoDeHash() {
        SenhaUtils senhaUtils = new SenhaUtils();
        String senhaOriginal = "Admin@2026";

        String hash = senhaUtils.gerarHashComSalt(senhaOriginal);

        // Verifica se gerou alguma coisa
        assertNotNull(hash, "O hash não pode ser nulo.");
        assertFalse(hash.isEmpty(), "O hash não pode estar vazio.");

        // Verifica se a senha foi efetivamente "escondida" (O hash tem de ser diferente da senha original)
        assertNotEquals(senhaOriginal, hash, "O hash gerado não pode ser igual à senha em texto limpo!");
    }

    @Test
    public void testarGeracaoSenhaAleatoria() {
        String senhaAuto = SenhaUtils.gerarPalavraPasseAleatoria();

        assertNotNull(senhaAuto);
        assertTrue(senhaAuto.length() >= 8, "A senha gerada automaticamente deve ter pelo menos 8 caracteres.");
    }
}