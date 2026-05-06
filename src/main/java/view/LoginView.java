package view;

import common.utils.BackendUtils;
import common.utils.MenuUtils;
import controller.LoginController;
import model.Docente;
import model.Estudante;
import model.Gestor;
import model.Utilizador;

import java.util.Scanner;
import static common.utils.DesignUtils.*;

public class LoginView {

    public static void Login() {
        Scanner scanner = new Scanner(System.in);
        LoginController loginController = new LoginController();
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
                        emailValido = BackendUtils.emailISSMFGestorValido(email)
                                || BackendUtils.emailISSMFDocenteValido(email)
                                || BackendUtils.emailISSMFEstudanteValido(email);

                        if (!emailValido) {
                            System.out.println(GetRed() + "Email invalido. Verifique o dominio (@issmf.ipp.pt) e tente novamente!" + GetReset());
                        }
                    }
                }

                if (sair) break;
                if (recuperarSenha) {
                    RecuperarSenhaView.RecuperarSenha();
                    continue;
                }

                Utilizador utilizador = null;
                boolean senhaCorreta = false;

                while (!senhaCorreta) {
                    String senha = BackendUtils.lerSenhaOculta("Senha: ", scanner);

                    if (senha.equals("0")) {
                        System.out.println(GetYellow() + "\nOperacao cancelada." + GetReset());
                        break;
                    }

                    utilizador = loginController.login(email, senha);

                    if (utilizador == null) {
                        System.out.println(GetRed() + "Credenciais invalidas! Tente novamente (ou digite '0' para voltar)." + GetReset());
                    } else {
                        senhaCorreta = true;
                    }
                }

                if (senhaCorreta && utilizador != null) {
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
                System.out.println("\n" + GetRed() + "Erro inesperado no Login: " + e.getMessage() + GetReset());
                MenuUtils.pressionarEnter(scanner);
            }

        } while (!sair);
    }
}
