package view;

import DAL.DB.DatabaseConnection;
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
                System.out.println(GetYellow() + "[Digite '0' para sair | Digite '9' para recuperar password | Digite 'login' para voltar ao menu de escolha]" + GetReset());

                String email = "";
                boolean emailValido = false;
                boolean recuperarSenha = false;
                boolean menuBDCSV = false;

                while (!emailValido) {
                    System.out.print("\nEmail: ");
                    email = scanner.nextLine().trim().toLowerCase();

                    if (email.equals("0")) {
                        System.out.println(GetYellow() + "\nA encerrar o sistema..." + GetReset());
                        sair = true;
                        break;
                    } else if (email.equals("9")) {
                        recuperarSenha = true;
                        break;
                    }else if (email.equals("login")){
                        System.out.println(GetYellow() + "\nA voltar ao menu de escolha..." + GetReset());
                        menuBDCSV = true;
                        break;
                    } else {
                        emailValido = BackendUtils.emailISSMFGestorValido(email)
                                || BackendUtils.emailISSMFDocenteValido(email)
                                || BackendUtils.emailISSMFEstudanteValido(email);

                        if (!emailValido) {
                            System.out.println(GetRed() + "Email inválido. Verifique o domínio (@issmf.ipp.pt) e tente novamente!" + GetReset());
                        }
                    }
                }

                if (sair) break;
                if (recuperarSenha) {
                    RecuperarSenhaView.RecuperarSenha();
                    continue;
                }
                if(menuBDCSV){
                    SelecionarModoView.selecionar();
                    continue;
                }
                Utilizador utilizador = null;
                boolean senhaCorreta = false;

                while (!senhaCorreta) {
                    String senha = BackendUtils.lerSenhaOculta("Senha: ", scanner);

                    if (senha.equals("0")) {
                        System.out.println(GetYellow() + "\nOperação cancelada." + GetReset());
                        break;
                    }

                    utilizador = loginController.login(email, senha);

                    if (utilizador == null) {
                        if (DatabaseConnection.houveErroConexao()) {
                            System.out.println(GetRed() + "Não foi possível ligar à base de dados. Verifique a ligação e tente novamente." + GetReset());
                        } else if (loginController.getUltimoErro() == controller.LoginController.ErroLogin.CONTA_INATIVA) {
                            System.out.println(GetRed() + "Esta conta está desativada. Contacte o gestor do sistema." + GetReset());
                            break; // não adianta tentar outra senha — conta inativa
                        } else {
                            System.out.println(GetRed() + "Credenciais inválidas! Tente novamente (ou digite '0' para voltar)." + GetReset());
                        }
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
                        gestorView.exibirMenuGestao((Gestor) utilizador);
                    }
                }

            } catch (Exception e) {
                System.out.println("\n" + GetRed() + "Erro inesperado no Login: " + e.getMessage() + GetReset());
                MenuUtils.pressionarEnter(scanner);
            }

        } while (!sair);
    }
}