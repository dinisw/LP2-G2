package Common;

import java.util.regex.Pattern;

public class BackendUtils {
    //region Validador de Email
    private static final String REGEX_EMAIL_ISEP = "^(?:\\d{7}|[a-zA-Z]{3}|[^@\\s]+\\.gestor)@isep\\.ipp\\.pt$";
    private static final Pattern PATTERN_ISEP = Pattern.compile(REGEX_EMAIL_ISEP);

    public static boolean isEmailIsepValido(String email) {
        return PATTERN_ISEP.matcher(email).matches();
    }
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
}
