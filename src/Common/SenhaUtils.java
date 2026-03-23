package Common;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class SenhaUtils {
    // 1. Gera um Salt aleatório para um novo usuário
    public static String gerarSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        // Retorna o Salt como uma String legível para você salvar no arquivo
        return Base64.getEncoder().encodeToString(salt);
    }

    // 2. Mistura a Senha + Salt e gera o Hash
    public static String gerarHashComSalt(String senha, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            // Junta a senha com o salt
            String senhaComSalt = senha + salt;
            byte[] hashBytes = digest.digest(senhaComSalt.getBytes());

            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erro de criptografia", e);
        }
    }

    // 3. Método que a sua BLL vai chamar no Login
    public static boolean verificarSenha(String senhaDigitada, String saltSalvo, String hashSalvo) {
        // Pega a senha que o cara acabou de digitar e mistura com o Salt que estava no arquivo
        String novoHashGerado = gerarHashComSalt(senhaDigitada, saltSalvo);

        // Se bater com o Hash do arquivo, a senha está certa!
        return novoHashGerado.equals(hashSalvo);
    }
}
