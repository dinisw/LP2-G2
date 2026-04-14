package common.utils;

import common.exceptions.CancelarRegistoException;

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
        String valor = scanner.nextLine();

        if (valor.equals("0")) {
            throw new CancelarRegistoException("Operação cancelada pelo utilizador.");
        }

        return valor;
    }

    private static final String REGEX_NIF = "^[1235689]\\d{8}$";
    private static final Pattern PATTERN_NIF = Pattern.compile(REGEX_NIF);

    public static boolean nifIsValid(String nif) {
        return PATTERN_NIF.matcher(nif).matches();
    }
}
