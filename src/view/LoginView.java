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

import java.util.ArrayList;
import java.util.Scanner;

public class LoginView {
    public static final String RESET = "\033[0m";
    public static final String RED = "\033[0;31m";

    public static void Login() {
        Scanner ler = new Scanner(System.in);
        LoginController loginController = new LoginController();
        boolean sair = false;

        do {
            MenuUtils.exibirTitulo();
            String email = "";
            String senha = "";
            boolean emailValido = false;
            while (!emailValido) {
                System.out.println(DesignUtils.GetCyanBold() + "LOGIN" + DesignUtils.GetReset());
                System.out.println("digite '0' para sair");
                System.out.print("\nEmail: ");
                email = ler.nextLine().trim();
                if (email.equals("0")) {
                    sair = true;
                    break;
                }
                emailValido = BackendUtils.isEmailIsepValido(email);
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
                System.out.print("Senha: ");
                senha = ler.nextLine();
                pessoa = loginController.login(email);

                if (pessoa != null) {
                    senhaValida = SenhaUtils.verificarSenha(senha, pessoa.getSalt(), pessoa.getHash());
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
                    EstudanteView estudanteView = new EstudanteView();
                    estudanteView.exibirMenu((Estudante) pessoa);
                } else if (pessoa instanceof Docente) {
                    DocenteView docenteView = new DocenteView();
                    docenteView.exibirMenuPessoalDocente((Docente) pessoa);
                } else if (pessoa instanceof Gestor) {
                    GestorView gestorView = new GestorView();
                    gestorView.exibirMenuGestores((Gestor) pessoa);
                }
            } else {
                System.out.println("\n" + RED + "Credenciais inválidas! Tente novamente." + RESET);
                MenuUtils.pressionarEnter(ler);
            }
        } while (!sair);
    }

    private static void exibirMenuGestaoGlobal(Scanner scanner, GestorView gestorView, DocenteView docenteView, EstudanteView estudanteView, CursoView cursoView, DepartamentoView departamentoView, controller.UnidadeCurricularController ucController) {
        String opcao;
        ArrayList<String> opcoes = new ArrayList<>();
        opcoes.add("1. Gerir Gestores");
        opcoes.add("2. Gerir Docentes");
        opcoes.add("3. Gerir Estudantes");
        opcoes.add("4. Gerir Cursos");
        opcoes.add("5. Gerir Departamentos");
        opcoes.add("6. Gerir Unidades Curriculares");
        opcoes.add("0. Logout");

        do {
            MenuUtils.exibirSubTitulo("MENU DE GESTÃO ADMINISTRATIVA", opcoes);
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
                    estudanteView.exibirMenu();
                    break;
                case "4":
                    cursoView.exibirMenuCursos();
                    break;
                case "5":
                    departamentoView.exibirMenuDepartamentos();
                    break;
                case "6":
                    ucController.exibirMenuGestaoUCs();
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Opção inválida!");
                    MenuUtils.pressionarEnter(scanner);
            }
        } while (true);
    }
}
