package view;

import Common.BackendUtils;
import Common.DesignUtils;
import Common.MenuUtils;
import Common.SenhaUtils;
import controller.LoginController;
import model.Docente;
import model.Estudante;
import model.Gestor;
import model.Pessoa;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.util.Scanner;

public class LoginView {
    public static final String RESET = "\033[0m";
    public static final String RED = "\033[0;31m";

    public static void Login() {
        Scanner ler = new Scanner(System.in);
        LoginController loginController = new LoginController();
        boolean sair = false;
        try{
            Terminal terminal = TerminalBuilder.terminal();
            LineReader reader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .build();
            do {
                MenuUtils.exibirTitulo();
                String email = "";
                String senha = "";
                boolean emailValido = false;
                while (!emailValido) {
                    System.out.println(DesignUtils.GetCyanBold() + "LOGIN" + DesignUtils.GetReset());
                    System.out.println("digite '0' para sair\ndigite '9' para recuperar password");
                    System.out.print("\nEmail: ");
                    email = ler.nextLine().trim();
                    if (email.equals("0")) {
                        sair = true;
                        break;
                    } else if (email.equals("9")) {
                        RecuperarSenhaView.RecuperarSenha();
                    }
                    emailValido = BackendUtils.isEmailISSMFValido(email);
                    if (!emailValido) {
                        System.out.print("Email inválido, tente novamente!!");
                        MenuUtils.pressionarEnter(ler);
                    }
                }
                if (sair) {
                    continue;
                }

                boolean senhaValida = false;
                Pessoa pessoa = null;

                    while (!senhaValida) {
                        senha = reader.readLine("Senha: ", '*');
                        pessoa = loginController.login(email);

                        if (pessoa != null) {
                            SenhaUtils su = new SenhaUtils();
                            senhaValida = su.verificarSenha(senha, pessoa.getHash());
                        } else {
                            System.out.print("Utilizador não encontrado. Tente novamente...");
                            break;
                        }

                        if (!senhaValida) {
                            System.out.print("Senha incorreta, tente novamente!!");
                            MenuUtils.pressionarEnter(ler);
                        }
                    }

                if (pessoa != null && senhaValida) {
                    if (pessoa instanceof Estudante) {
                        EstudanteView.exibirMenu((Estudante) pessoa);
                    } else if (pessoa instanceof Docente) {
                        DocenteView docenteView = new DocenteView();
                        docenteView.exibirMenuPessoalDocente((Docente) pessoa);
                    } else if (pessoa instanceof Gestor) {
                        GestorView gestorView = new GestorView();
                        gestorView.exibirMenuGestao();
                    }
                } else {
                    System.out.println("\n" + RED + "Credenciais inválidas! Tente novamente." + RESET);
                    MenuUtils.pressionarEnter(ler);
                }
            } while (!sair);
        } catch (IOException e) {

        }
    }
}
