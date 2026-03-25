package view;

import Common.DesignUtils;
import Common.MenuUtils;
import Common.SenhaUtils;
import DAL.GestorCRUD;
import model.Gestor;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GestorView {
    private final GestorCRUD gestorCRUD;
    private final Scanner scanner;
    private final MenuUtils menu;

    public GestorView() {
        this.gestorCRUD = new GestorCRUD();
        this.scanner = new Scanner(System.in);
        this.menu = new MenuUtils();
    }

    public void exibirMenuGestores() {
        String opcao;
        ArrayList<String> opcoes = new ArrayList<>();
        opcoes.add("1. Registar Gestor");
        opcoes.add("2. Listar Gestores");
        opcoes.add("3. Procurar Gestor (NIF)");
        opcoes.add("4. Atualizar Gestor (NIF)");
        opcoes.add("5. Eliminar Gestor (NIF)");
        opcoes.add("0. Voltar ao Menu Principal");

        do {
            menu.exibirSubTitulo("GESTÃO DE GESTORES", opcoes);
            System.out.print("\n" + DesignUtils.GetWhiteBold() + "Selecione uma opção: " + DesignUtils.GetReset());
            opcao = scanner.nextLine().trim();

            switch (opcao) {
                case "1":
                    registarGestor();
                    break;
                case "2":
                    listarGestores();
                    break;
                case "3":
                    procurarGestor();
                    break;
                case "4":
                    atualizarGestor();
                    break;
                case "5":
                    eliminarGestor();
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Opção inválida!");
                    menu.pressionarEnter(scanner);
            }
        } while (!opcao.equals("0"));
    }

    private void registarGestor() {
        System.out.println("\n--- REGISTO DE GESTOR ---");
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
        String passDigitada = scanner.nextLine();
        String salt = SenhaUtils.gerarSalt();
        String hash = SenhaUtils.gerarHashComSalt(passDigitada, salt);
        System.out.print("Cargo: ");
        String cargo = scanner.nextLine();
        Gestor novo = new Gestor(nome, morada, nif, dataNascimento, email, hash, salt, cargo);
        if (gestorCRUD.registarGestor(novo)) {
            System.out.println("Gestor registado com sucesso!");
        } else {
            System.out.println("Erro ao registar: NIF já existe ou dados inválidos.");
        }
        menu.pressionarEnter(scanner);
    }

    private void listarGestores() {
        System.out.println("\n--- LISTA DE GESTORES ---");
        List<Gestor> gestores = gestorCRUD.getGestores();
        if (gestores.isEmpty()) {
            System.out.println("Nenhum gestor registado.");
        } else {
            for (Gestor g : gestores) {
                System.out.println("NIF: " + g.getNif() + " | Nome: " + g.getNome() + " | Cargo: " + g.getCargo());
            }
        }
        menu.pressionarEnter(scanner);
    }

    private void procurarGestor() {
        System.out.print("\nDigite o NIF do gestor: ");
        int nif = Integer.parseInt(scanner.nextLine());
        Gestor g = gestorCRUD.procurarPorNif(nif);
        if (g != null) {
            System.out.println("Dados encontrados: " + g);
        } else {
            System.out.println("Gestor não encontrado.");
        }
        menu.pressionarEnter(scanner);
    }

    private void eliminarGestor() {
        System.out.print("\nDigite o NIF do gestor a eliminar: ");
        int nif = Integer.parseInt(scanner.nextLine());
        if (gestorCRUD.eliminarGestor(nif)) {
            System.out.println("Gestor eliminado com sucesso!");
        } else {
            System.out.println("Erro ao eliminar: Gestor não encontrado.");
        }
        menu.pressionarEnter(scanner);
    }

    private void atualizarGestor() {
        System.out.print("\nDigite o NIF do gestor a atualizar: ");
        int nif = Integer.parseInt(scanner.nextLine());
        Gestor g = gestorCRUD.procurarPorNif(nif);

        if (g != null) {
            System.out.println("Dados atuais: " + g);
            System.out.print("Novo Nome (Enter para manter): ");
            String nome = scanner.nextLine();
            if (!nome.isEmpty()) g.setNome(nome);

            System.out.print("Nova Morada (Enter para manter): ");
            String morada = scanner.nextLine();
            if (!morada.isEmpty()) g.setMorada(morada);

            System.out.print("Novo Email (Enter para manter): ");
            String email = scanner.nextLine();
            if (!email.isEmpty()) g.setEmail(email);

            System.out.print("Novo Cargo (Enter para manter): ");
            String cargo = scanner.nextLine();
            if (!cargo.isEmpty()) g.setCargo(cargo);

            if (gestorCRUD.atualizarGestor(g)) {
                System.out.println("Gestor atualizado com sucesso!");
            } else {
                System.out.println("Erro ao atualizar gestor.");
            }
        } else {
            System.out.println("Gestor não encontrado.");
        }
        menu.pressionarEnter(scanner);
    }
}
