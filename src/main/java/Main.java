import DAL.DAOFactory;
import view.LoginView;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        selecionarModoArmazenamento();
        LoginView.Login();
    }

    private static void selecionarModoArmazenamento() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║        ISSMF — Sistema de Gestão              ║");
        System.out.println("╠══════════════════════════════════════════════╣");
        System.out.println("║   Selecione o modo de armazenamento:          ║");
        System.out.println("║                                                ║");
        System.out.println("║   [1] Ficheiros CSV (local)                    ║");
        System.out.println("║   [2] Base de Dados SQL Server                 ║");
        System.out.println("╚══════════════════════════════════════════════╝");

        String escolha = "";
        while (true) {
            System.out.print("\nOpção: ");
            escolha = scanner.nextLine().trim();

            if (escolha.equals("1")) {
                DAOFactory.setModo("CSV");
                System.out.println("\n✔ Modo: Ficheiros CSV selecionado.\n");
                break;
            } else if (escolha.equals("2")) {
                DAOFactory.setModo("SQL");
                System.out.println("\n✔ Modo: Base de Dados SQL Server selecionado.\n");
                break;
            } else {
                System.out.println("Opção inválida. Digite 1 ou 2.");
            }
        }
    }
}
