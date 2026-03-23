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
}
