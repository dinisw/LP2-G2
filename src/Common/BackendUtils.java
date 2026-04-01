package Common;

import java.util.regex.Pattern;

public class BackendUtils {
    //region Validador de Email
    private static final String REGEX_EMAIL_ISSMF = "^(?:\\d{7}|[a-zA-Z]{3}|[^@\\s]+\\.gestor|[^@\\s]+)@issmf\\.ipp\\.pt$";
    private static final Pattern PATTERN_ISSMF = Pattern.compile(REGEX_EMAIL_ISSMF);

    public static boolean isEmailISSMFValido(String email) {
        return PATTERN_ISSMF.matcher(email).matches();
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
