package common.utils;

import common.exceptions.CancelarRegistoException;
import controller.DocenteController;
import controller.EstudanteController;
import controller.GestorController;

import java.util.Scanner;
import java.util.regex.Pattern;

public class BackendUtils {
    //region Validador de Email
    private static final String REGEX_EMAIL_GESTOR_ISSMF = "^[\\w.-]+\\.gestor@issmf\\.ipp\\.pt$";
    private static final Pattern PATTERN_GESTOR_ISSMF = Pattern.compile(REGEX_EMAIL_GESTOR_ISSMF);

    private static final String REGEX_EMAIL_DOCENTE_ISSMF = "^[a-zA-Z]{3}@issmf\\.ipp\\.pt$";
    private static final Pattern PATTERN_DOCENTE_ISSMF = Pattern.compile(REGEX_EMAIL_DOCENTE_ISSMF);

    private static final String REGEX_EMAIL_ESTUDANTE_ISSMF = "^\\d{7}@issmf\\.ipp\\.pt$";
    private static final Pattern PATTERN_ESTUDANTE_ISSMF = Pattern.compile(REGEX_EMAIL_ESTUDANTE_ISSMF);

    public static boolean emailISSMFEstudanteValido(String email) {return PATTERN_ESTUDANTE_ISSMF.matcher(email.toLowerCase()).matches();}
    public static boolean emailISSMFDocenteValido(String email) {return PATTERN_DOCENTE_ISSMF.matcher(email.toLowerCase()).matches();}
    public static boolean emailISSMFGestorValido(String email) {return PATTERN_GESTOR_ISSMF.matcher(email.toLowerCase()).matches();}
    //endregion

    //region Validador de Senha
    private static final String REGEX_SENHA = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[^a-zA-Z0-9]).{8,}$";
    private static final Pattern PATTERN_SENHA = Pattern.compile(REGEX_SENHA);

    public static boolean isSenhaValida(String senha) {
        if (senha == null) {
            return false;
        }
        return PATTERN_SENHA.matcher(senha).matches();
    }
    //endregion

    public static String lerInputString(Scanner scanner, String prompt) {
        System.out.print(prompt);
        String valor = scanner.nextLine().trim();

        if (valor.equals("0")) {
            throw new CancelarRegistoException("Operação cancelada pelo utilizador.");
        }

        return valor;
    }

    private static final String REGEX_NIF = "^[1235689]\\d{8}$";
    private static final Pattern PATTERN_NIF = Pattern.compile(REGEX_NIF);

    public static boolean nifEValido(String nif) {
        return PATTERN_NIF.matcher(nif).matches();
    }
    public static boolean nifExiste(int nif) {
        EstudanteController e = new EstudanteController();
        DocenteController d = new DocenteController();
        GestorController g = new GestorController();
        return e.procurarEstudantePorNif(nif) != null || d.procurarDocentePorNif(nif) != null || g.procurarGestorPorNif(nif) != null;
    }

    public static String lerSenhaOculta(String prompt) {
        return lerSenhaOculta(prompt, null);
    }

    public static String lerSenhaOculta(String prompt, java.util.Scanner scannerExistente) {
        if (System.console() != null) {
            char[] pass = System.console().readPassword(prompt);
            return (pass != null) ? new String(pass).trim() : "";
        } else {
            System.out.print(prompt);
            if (scannerExistente != null) {
                return scannerExistente.nextLine().trim();
            }
            return new java.util.Scanner(System.in).nextLine().trim();
        }
    }

    public static boolean isNomeValido (String nome) {
        if (nome == null || nome.trim().isEmpty()) return false;
        return nome.matches("^[\\p{L} \\.'\\-]+$");
    }
}
