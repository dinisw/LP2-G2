package common.utils;

import common.exceptions.CancelarRegistoException;
import controller.DocenteController;
import controller.EstudanteController;
import controller.GestorController;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

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

    public static String lerInputStringObrigatorio(Scanner scanner, String prompt) {
        String valor = "";
        while (valor.isEmpty()) {
            System.out.print(prompt);
            valor = scanner.nextLine().trim();
            if (valor.isEmpty()) {
                System.out.println(DesignUtils.GetRed() + "Erro: Este campo é obrigatório e não pode ficar vazio." + DesignUtils.GetReset());
            }
        }
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

    /**
     * Lê uma senha com confirmação: pede duas vezes e só aceita quando coincidem.
     * Também valida o formato (maiúscula + número + especial).
     * Lança CancelarRegistoException se o utilizador digitar "0".
     */
    public static String lerSenhaComConfirmacao(String promptSenha, String promptConfirmacao, java.util.Scanner scanner) {
        while (true) {
            String senha = lerSenhaOculta(promptSenha, scanner);
            if (senha.equals("0")) throw new CancelarRegistoException("Operação cancelada pelo utilizador.");

            if (!isSenhaValida(senha)) {
                System.out.println(DesignUtils.GetRed()
                        + "SENHA deve conter pelo menos uma letra maiúscula, um número e um carácter especial. Tente novamente."
                        + DesignUtils.GetReset());
                continue;
            }

            String confirmacao = lerSenhaOculta(promptConfirmacao, scanner);
            if (confirmacao.equals("0")) throw new CancelarRegistoException("Operação cancelada pelo utilizador.");

            if (senha.equals(confirmacao)) return senha;

            System.out.println(DesignUtils.GetRed()
                    + "As senhas não coincidem. Tente novamente."
                    + DesignUtils.GetReset());
        }
    }

    /**
     * Lê uma senha sem a mostrar no terminal.
     * Ordem de tentativas:
     *   1. JLine — abre /dev/tty diretamente, funciona em Maven, IDE e terminal normal.
     *   2. System.console() — fallback nativo da JVM.
     *   3. Scanner visível — último recurso (IDE sem TTY), sem mascaramento.
     */
    public static String lerSenhaOculta(String prompt, java.util.Scanner scannerExistente) {
        // Tentativa 1: JLine (não conflitua com Scanner — usa /dev/tty no Unix)
        try {
            Terminal terminal = TerminalBuilder.builder()
                    .system(true)
                    .build();
            try {
                LineReader reader = LineReaderBuilder.builder()
                        .terminal(terminal)
                        .build();
                String pass = reader.readLine(prompt, '*');
                return pass != null ? pass.trim() : "";
            } finally {
                terminal.close();
            }
        } catch (Exception ignored) {}

        // Tentativa 2: System.console() nativo
        if (System.console() != null) {
            char[] pass = System.console().readPassword(prompt);
            return pass != null ? new String(pass).trim() : "";
        }

        // Fallback: input visível (IDE sem TTY)
        System.out.print(prompt + "[sem mascaramento] ");
        if (scannerExistente != null) return scannerExistente.nextLine().trim();
        return new java.util.Scanner(System.in).nextLine().trim();
    }

    public static boolean isNomeValido (String nome) {
        if (nome == null || nome.trim().isEmpty()) return false;
        return nome.matches("^[\\p{L} \\.'\\-]+$");
    }

    /**
     * Aceita horas em formato flexível: "18", "18:00", "18:30", "1830", "9", "09:30".
     * Lança IllegalArgumentException se o formato não for reconhecido.
     */
    public static java.time.LocalTime parseHoraFlexivel(String input) {
        if (input == null || input.trim().isEmpty())
            throw new IllegalArgumentException("Hora em branco.");
        input = input.trim();
        try {
            if (input.matches("\\d{1,2}")) {
                return java.time.LocalTime.of(Integer.parseInt(input), 0);
            }
            if (input.matches("\\d{3,4}")) {
                int total = Integer.parseInt(input);
                return java.time.LocalTime.of(total / 100, total % 100);
            }
            if (input.matches("\\d{1,2}:\\d{2}")) {
                String[] p = input.split(":");
                return java.time.LocalTime.of(Integer.parseInt(p[0]), Integer.parseInt(p[1]));
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Hora inválida: '" + input + "'.");
        }
        throw new IllegalArgumentException("Formato de hora não reconhecido: '" + input + "'. Use HH:MM ou HH (ex: 18:00 ou 18).");
    }
}
