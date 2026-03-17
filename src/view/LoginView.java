package view;

import Common.Utils;
import controller.LoginController;
import model.Docente;
import model.Estudante;
import model.Gestor;
import model.Pessoa;

import java.util.ArrayList;
import java.util.Scanner;

public class LoginView {
    public static final String RESET = "\033[0m";
    public static final String RED = "\033[0;31m";

    public static void Login(){
        Scanner ler = new Scanner(System.in);
        Utils utils = new Utils();
        LoginController loginController = new LoginController();
        boolean sair = false;

        do {
            Utils.exibirTitulo();

            System.out.println(Utils.GetCyanBold() +  "LOGIN" + Utils.GetReset());
            System.out.print("Email (digite '0' para sair): ");
            String email = ler.nextLine().trim();
            
            if (email.equals("0")) {
                sair = true;
                continue;
            }

            System.out.print("Senha: ");
            String senha = ler.nextLine().trim();

            Pessoa pessoa = loginController.login(email, senha);

            if(pessoa != null){
                if (pessoa instanceof Estudante) {
                    EstudanteView.Menu();
                } else if (pessoa instanceof Docente) {
                    DocenteView docenteView = new DocenteView();
                    docenteView.exibirMenuDocentes();
                } else if (pessoa instanceof Gestor) {
                    GestorView gestorView = new GestorView();
                    exibirMenuGestaoGlobal(utils, ler, gestorView, new DocenteView(), new EstudanteView());
                }
            } else {
                System.out.println("\n" + RED + "Credenciais inválidas! Tente novamente." + RESET);
                utils.pressionarEnter(ler);
            }
        } while (!sair);
    }

    private static void exibirMenuGestaoGlobal(Utils menu, Scanner scanner, GestorView gestorView, DocenteView docenteView, EstudanteView estudanteView) {
        String opcao;
        ArrayList<String> opcoes = new ArrayList<>();
        opcoes.add("1. Gerir Gestores");
        opcoes.add("2. Gerir Docentes");
        opcoes.add("3. Gerir Estudantes");
        opcoes.add("0. Logout");

        do {
            menu.exibirSubTitulo("MENU DE GESTÃO ADMINISTRATIVA", opcoes);
            System.out.print("\nSelecione uma opção: ");
            opcao = scanner.nextLine().trim();

            switch (opcao) {
                case "1":
                    gestorView.exibirMenuGestores();
                    break;
                case "2":
                    docenteView.exibirMenuDocentes();
                    break;
                case "3":
                    EstudanteView.Menu();
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Opção inválida!");
                    menu.pressionarEnter(scanner);
            }
        } while (!opcao.equals("0"));
    }
}
