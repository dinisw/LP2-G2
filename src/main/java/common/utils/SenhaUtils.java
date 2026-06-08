package common.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class SenhaUtils {

    // Salt fixo mantido apenas para compatibilidade com passwords antigas (formato legado)
    private static final String SALT_LEGADO = "AzTCXmiWY6lDiLVSj0RHkA==";

    /**
     * Gera hash com salt ALEATÓRIO por utilizador.
     * Formato armazenado: "salt$hash" (permite verificação futura sem salt global)
     * Dois utilizadores com a mesma senha produzem hashes completamente diferentes.
     */
    public String gerarHashComSalt(String senha) {
        SecureRandom random = new SecureRandom();
        byte[] saltBytes = new byte[16];
        random.nextBytes(saltBytes);
        String randomSalt = Base64.getEncoder().encodeToString(saltBytes);
        return randomSalt + "$" + calcularHash(senha, randomSalt);
    }

    /**
     * Verifica senha suportando tanto o novo formato ("salt$hash")
     * como o formato legado (hash com salt fixo).
     * Garante retrocompatibilidade com contas já existentes na BD.
     */
    public boolean verificarSenha(String senhaDigitada, String hashSalvo) {
        if (senhaDigitada == null || hashSalvo == null || hashSalvo.isEmpty()) return false;

        if (hashSalvo.contains("$")) {
            // Novo formato: "randomSalt$hash"
            String[] partes = hashSalvo.split("\\$", 2);
            if (partes.length != 2) return false;
            return calcularHash(senhaDigitada, partes[0]).equals(partes[1]);
        }

        // Formato legado: hash gerado com salt fixo
        return calcularHashLegado(senhaDigitada).equals(hashSalvo);
    }

    private String calcularHash(String senha, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest((senha + salt).getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erro de criptografia", e);
        }
    }

    /** Mantido apenas para verificar senhas antigas (não usar para novas senhas). */
    private String calcularHashLegado(String senha) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest((senha + SALT_LEGADO).getBytes());
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erro de criptografia", e);
        }
    }

    public static String gerarPalavraPasseAleatoria() {
        String maiusculas = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String minusculas = "abcdefghijklmnopqrstuvwxyz";
        String numeros    = "0123456789";
        String especiais  = "!@#$%^&*-_=";
        String todos      = maiusculas + minusculas + numeros + especiais;

        SecureRandom random = new SecureRandom();
        StringBuilder passe = new StringBuilder();

        // Garante pelo menos 1 de cada categoria (requisito de validação)
        passe.append(maiusculas.charAt(random.nextInt(maiusculas.length())));
        passe.append(minusculas.charAt(random.nextInt(minusculas.length())));
        passe.append(numeros.charAt(random.nextInt(numeros.length())));
        passe.append(especiais.charAt(random.nextInt(especiais.length())));

        // Preenche até 12 caracteres (antes eram só 8 — vulnerável a brute-force)
        for (int i = 0; i < 8; i++) {
            passe.append(todos.charAt(random.nextInt(todos.length())));
        }

        // Baralhar
        char[] chars = passe.toString().toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char tmp = chars[i]; chars[i] = chars[j]; chars[j] = tmp;
        }
        return new String(chars);
    }
}
