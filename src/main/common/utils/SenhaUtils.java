package main.common.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class SenhaUtils {
    private final String salt = "AzTCXmiWY6lDiLVSj0RHkA==";

    public String gerarHashComSalt(String senha) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String senhaComSalt = senha + this.salt;
            byte[] hashBytes = digest.digest(senhaComSalt.getBytes());

            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erro de criptografia", e);
        }
    }

    public boolean verificarSenha(String senhaDigitada, String hashSalvo) {
        String novoHashGerado = gerarHashComSalt(senhaDigitada);
        return novoHashGerado.equals(hashSalvo);
    }

    public static String gerarPalavraPasseAleatoria() {
        String maiusculas = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String minusculas = "abcdefghijklmnopqrstuvwxyz";
        String numeros = "0123456789";
        String especiais = "!@#$%^&*-_=";
        String todosCaracteres = maiusculas + minusculas + numeros + especiais;

        java.security.SecureRandom random = new java.security.SecureRandom();
        StringBuilder passe = new StringBuilder();

        passe.append(maiusculas.charAt(random.nextInt(maiusculas.length())));
        passe.append(minusculas.charAt(random.nextInt(minusculas.length())));
        passe.append(numeros.charAt(random.nextInt(numeros.length())));
        passe.append(especiais.charAt(random.nextInt(especiais.length())));

        for (int i = 0; i < 4; i ++) {
            passe.append(todosCaracteres.charAt(random.nextInt(todosCaracteres.length())));
        }

        char[] caracteres = passe.toString().toCharArray();
        for (int i = caracteres.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = caracteres[i];
            caracteres[i] = caracteres[j];
            caracteres[j] = temp;
        }
        return new String(caracteres);
    }
}
