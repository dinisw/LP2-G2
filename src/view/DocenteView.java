package view;

import DAL.DocenteCRUD;
import model.Docente;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import static view.Menu.*;

public class DocenteView {
    private final DocenteCRUD docenteCRUD;
    private final Scanner scanner;
    private final Menu menu;

    public DocenteView() {
        this.docenteCRUD = new DocenteCRUD();
        this.scanner = new Scanner(System.in);
        this.menu = new Menu();
    }

    public void exibirMenuDocentes() {
        String opcao;
        ArrayList<String> opcoes = new ArrayList<>();
        opcoes.add("1. Registar Docente");
        opcoes.add("2. Listar Docentes");
        opcoes.add("3. Procurar Docente (NIF)");
        opcoes.add("4. Atualizar Docente (NIF)");
        opcoes.add("5. Eliminar Docente (NIF)");
        opcoes.add("0. Voltar ao Menu Principal");

        do {
            menu.exibirSubTitulo("GESTÃO DE DOCENTES", opcoes);
            System.out.print("\n" + WHITE_BOLD + "Selecione uma opção: " + RESET);
            opcao = scanner.nextLine().trim();

            switch (opcao) {
                case "1":
                    registarDocente();
                    break;
                case "2":
                    listarDocentes();
                    break;
                case "3":
                    procurarDocente();
                    break;
                case "4":
                    System.out.println("\n" + YELLOW + "[EM MANUTENÇÃO] Esta funcionalidade ainda não está finalizada." + RESET);
                    menu.pressionarEnter(scanner);
                    break;
                case "5":
                    eliminarDocente();
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Opção inválida!");
                    menu.pressionarEnter(scanner);
            }
        } while (!opcao.equals("0"));
    }

    private void registarDocente() {
        System.out.println("\n--- REGISTO DE DOCENTE ---");
        System.out.print("Nome: ");
        String nome = scanner.nextLine();
        System.out.print("Morada: ");
        String morada = scanner.nextLine();
        System.out.print("NIF: ");
        int nif = Integer.parseInt(scanner.nextLine());
        System.out.print("Data de Nascimento (AAAA-MM-DD): ");
        LocalDate dataNascimento = LocalDate.parse(scanner.nextLine());
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Palavra-passe: ");
        String pass = scanner.nextLine();
        System.out.print("Sigla: ");
        String sigla = scanner.nextLine();

        Docente novo = new Docente(nome, morada, nif, dataNascimento, email, pass, sigla, null, null);
        if (docenteCRUD.registarDocente(novo)) {
            System.out.println("Docente registado com sucesso!");
        } else {
            System.out.println("Erro ao registar: NIF já existe ou dados inválidos.");
        }
        menu.pressionarEnter(scanner);
    }

    private void listarDocentes() {
        System.out.println("\n--- LISTA DE DOCENTES ---");
        List<Docente> docentes = docenteCRUD.getDocentes();
        if (docentes.isEmpty()) {
            System.out.println("Nenhum docente registado.");
        } else {
            for (Docente d : docentes) {
                System.out.println("NIF: " + d.getNif() + " | Nome: " + d.getNome() + " | Sigla: " + d.getSigla());
            }
        }
        menu.pressionarEnter(scanner);
    }

    private void procurarDocente() {
        System.out.print("\nDigite o NIF do docente: ");
        int nif = Integer.parseInt(scanner.nextLine());
        Docente d = docenteCRUD.procurarPorNif(nif);
        if (d != null) {
            System.out.println("Dados encontrados: " + d.getNome() + " - " + d.getEmail());
        } else {
            System.out.println("Docente não encontrado.");
        }
        menu.pressionarEnter(scanner);
    }

    private void eliminarDocente() {
        System.out.print("\nDigite o NIF do docente a eliminar: ");
        int nif = Integer.parseInt(scanner.nextLine());
        if (docenteCRUD.eliminarDocente(nif)) {
            System.out.println("Docente eliminado com sucesso!");
        } else {
            System.out.println("Erro ao eliminar: Docente não encontrado.");
        }
        menu.pressionarEnter(scanner);
    }
}
