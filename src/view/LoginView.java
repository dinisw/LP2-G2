package view;

import Common.BackendUtils;
import Common.DesignUtils;
import Common.MenuUtils;
import Common.SenhaUtils;
import DAL.EstudanteCRUD;
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
                System.out.print("Email (digite '0' para sair): ");
                email = ler.nextLine().trim();
                emailValido = BackendUtils.isEmailIsepValido(email);
                if (!emailValido) {
                    System.out.print("Email inválido, tente novamente!!");
                    MenuUtils.pressionarEnter(ler);
                }
            }
            if (email.equals("0")) {
                sair = true;
                continue;
            }

            boolean senhaValido = false;

            while (!senhaValido) {
                System.out.print("Senha: ");
                char[] senhaArray = System.console().readPassword();
                senha = new String(senhaArray);
                java.util.Arrays.fill(senhaArray, ' ');
                var est = new EstudanteCRUD();
                int numMec = Integer.parseInt(email.split("@")[0]);
                var estudante = est.procurarNumeroMec(numMec);
                senhaValido = SenhaUtils.verificarSenha(senha, estudante.getSalt(), estudante.getHash());

                if (!senhaValido) {
                    System.out.print("Senha incorreta, tente novamente!!");
                    MenuUtils.pressionarEnter(ler);
                }
            }
            Pessoa pessoa = loginController.login(email);

            if (pessoa != null) {
                if (pessoa instanceof Estudante) {
                    EstudanteView.Menu();
                } else if (pessoa instanceof Docente) {
                    DocenteView docenteView = new DocenteView();
                    docenteView.exibirMenuDocentes();
                } else if (pessoa instanceof Gestor) {
                    GestorView gestorView = new GestorView();
                    CursoView cursoView = new CursoView();
                    DepartamentoView departamentoView = new DepartamentoView();
                    exibirMenuGestaoGlobal(ler, gestorView, new DocenteView(), new EstudanteView(), new CursoView(), new DepartamentoView());
                }
            } else {
                System.out.println("\n" + RED + "Credenciais inválidas! Tente novamente." + RESET);
                MenuUtils.pressionarEnter(ler);
            }
        } while (!sair);
    }

    private static void exibirMenuGestaoGlobal(Scanner scanner, GestorView gestorView, DocenteView docenteView, EstudanteView estudanteView, CursoView cursoView, DepartamentoView departamentoView) {
        String opcao;
        ArrayList<String> opcoes = new ArrayList<>();
        opcoes.add("1. Gerir Gestores");
        opcoes.add("2. Gerir Docentes");
        opcoes.add("3. Gerir Estudantes");
        opcoes.add("4. Gerir Cursos");
        opcoes.add("5. Gerir Departamentos");
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
                    estudanteView.Menu();
                    break;
                case "4":
                    cursoView.exibirMenuCursos();
                    break;
                case "5":
                    departamentoView.exibirMenuDepartamentos();
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Opção inválida!");
                    MenuUtils.pressionarEnter(scanner);
            }
        } while (!opcao.equals("0"));
    }
}
