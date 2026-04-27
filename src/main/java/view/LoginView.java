package main.view;

import main.common.utils.BackendUtils;
import main.common.utils.MenuUtils;
import main.common.utils.SenhaUtils;
import main.controller.LoginController;
import main.model.Docente;
import main.model.Estudante;
import main.model.Gestor;
import main.model.Utilizador;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import java.util.Scanner;
import static main.common.utils.DesignUtils.*;

public class LoginView {
    public static final String RESET = "\033[0m";
    public static final String RED = "\033[0;31m";

    public static void Login() {
        Scanner scanner = new Scanner(System.in);
        LoginController loginController = new LoginController();

        try {
            Terminal terminal = TerminalBuilder.terminal();
            LineReader reader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .build();

            boolean sair = false;

            do {
                try {
                    MenuUtils.exibirTitulo();
                    System.out.println(GetCyanBold() + "LOGIN" + GetReset());
                    System.out.println(GetYellow() + "[Digite '0' para sair | Digite '9' para recuperar password]" + GetReset());

                    String email = "";
                    boolean emailValido = false;
                    boolean recuperarSenha = false;

                    while (!emailValido) {
                        System.out.print("\nEmail: ");
                        email = scanner.nextLine().trim();

                        if (email.equals("0")) {
                            System.out.println(GetYellow() + "\nA encerrar o sistema..." + GetReset());
                            sair = true;
                            break;
                        } else if (email.equals("9")) {
                            recuperarSenha = true;
                            break;
                        } else {
                            emailValido = BackendUtils.emailISSMFGestorValido(email) ||
                                    BackendUtils.emailISSMFDocenteValido(email) ||
                                    BackendUtils.emailISSMFEstudanteValido(email);

                            if (!emailValido) {
                                System.out.println(GetRed() + "Email inválido. Verifique o domínio (@issmf.ipp.pt) e tente novamente!" + GetReset());
                            }
                        }
                    }

                    if (sair) {
                        break;
                    }
                    if (recuperarSenha) {
                        RecuperarSenhaView.RecuperarSenha();
                        continue;
                    }

                    Utilizador utilizador = loginController.login(email);

                    if (utilizador == null) {
                        System.out.println("\n" + GetRed() + "Credenciais inválidas! Tente novamente." + GetReset());
                        MenuUtils.pressionarEnter(scanner);
                        continue;
                    }

                    boolean senhaCorreta = false;
                    SenhaUtils su = new SenhaUtils();

                    while (!senhaCorreta) {
                        String senha = reader.readLine("Senha: ", '*');

                        if (senha.equals("0")) {
                            System.out.println(GetYellow() + "\nOperação cancelada." + GetReset());
                            break;
                        }

                        senhaCorreta = su.verificarSenha(senha, utilizador.getHash());

                        if (!senhaCorreta) {
                            System.out.println(GetRed() + "Senha incorreta. Tente novamente (ou digite '0' para voltar)." + GetReset());
                        }
                    }

                    if (senhaCorreta) {
                        System.out.println(GetGreen() + "\nLogin efetuado com sucesso! Bem-vindo, " + utilizador.getNome() + "!" + GetReset());
                        MenuUtils.pressionarEnter(scanner);

                        if (utilizador instanceof Estudante) {
                            EstudanteView.exibirMenu((Estudante) utilizador);
                        } else if (utilizador instanceof Docente) {
                            DocenteView docenteView = new DocenteView();
                            docenteView.exibirMenuPessoalDocente((Docente) utilizador);
                        } else if (utilizador instanceof Gestor) {
                            GestorView gestorView = new GestorView();
                            gestorView.exibirMenuGestao();
                        }
                    }

                } catch (Exception e) {
                    System.out.println("\n" + GetRed() + "Erro inesperado no sistema de Login: " + e.getMessage() + GetReset());
                    MenuUtils.pressionarEnter(scanner);
                }

            } while (!sair);

        } catch (Exception e) {
            System.out.println(GetRed() + "Erro crítico ao inicializar a consola. Contacte o suporte." + GetReset());
            e.printStackTrace();
        }
    }
}
