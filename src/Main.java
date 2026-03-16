import view.DocenteView;
import view.EstudanteView;
import view.GestorView;
import view.LoginView;
import view.Menu;

import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Menu menu = new Menu();
        GestorView gestorView = new GestorView();
        DocenteView docenteView = new DocenteView();
        EstudanteView estudanteView = new EstudanteView();

        String opcao;
        ArrayList<String> opcoes = new ArrayList<>();
        opcoes.add("1. Login");
        opcoes.add("2. Menu Estudante (Original)");
        opcoes.add("3. Gestão Administrativa (CRUDs)");
        opcoes.add("0. Sair");

        do {
            menu.exibirTitulo();
            menu.exibirSubTitulo("SISTEMA ISSMF - MENU PRINCIPAL", opcoes);
            System.out.print("\nSelecione uma opção: ");
            opcao = scanner.nextLine().trim();

            switch (opcao) {
                case "1":
                    LoginView.Login();
                    break;
                case "2":
                    EstudanteView.Menu();
                    break;
                case "3":
                    exibirMenuGestao(menu, scanner, gestorView, docenteView, estudanteView);
                    break;
                case "0":
                    System.out.println("A sair...");
                    break;
                default:
                    System.out.println("Opção inválida!");
                    menu.pressionarEnter(scanner);
            }
        } while (!opcao.equals("0"));
    }

    private static void exibirMenuGestao(Menu menu, Scanner scanner, GestorView gestorView, DocenteView docenteView, EstudanteView estudanteView) {
        String opcao;
        ArrayList<String> opcoes = new ArrayList<>();
        opcoes.add("1. Gerir Gestores");
        opcoes.add("2. Gerir Docentes");
        opcoes.add("3. Gerir Estudantes");
        opcoes.add("0. Voltar");

        do {
            menu.exibirTitulo();
            menu.exibirSubTitulo("GESTÃO ADMINISTRATIVA - TESTE CRUD", opcoes);
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
                    estudanteView.Menu(); // EstudanteView.Menu() already handles management
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
