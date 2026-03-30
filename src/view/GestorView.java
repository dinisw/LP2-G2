package view;

import Common.DesignUtils;
import Common.MenuUtils;
import Common.SenhaUtils;
import controller.GestorController;
import model.Gestor;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GestorView {
    private final GestorController gestorController;
    private final Scanner ler;

    public GestorView() {
        this.gestorController = new GestorController();
        this.ler = new Scanner(System.in);
    }
    private static void exibirMenuGestao(Gestor gestor) {
        // DocenteView docenteView, EstudanteView estudanteView, CursoView cursoView, DepartamentoView departamentoView, UnidadeCurricularView unidadeCurricularView
        String opcao;
        Scanner ler = new Scanner(System.in);
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
            opcao = ler.nextLine().trim();

            switch (opcao) {
                case "1":
                    exibirMenuGestores(gestor, ler);
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
                    unidadeCurricularView.exibirMenuUnidadesCurriculares();
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Opção inválida!");
                    MenuUtils.pressionarEnter(ler);
            }
        } while (true);
    }
    
    public static void exibirMenuGestores(Gestor gestor, Scanner ler) {
        String opcao;
        ArrayList<String> opcoes = new ArrayList<>();
        opcoes.add("1. Registar Gestor");
        opcoes.add("2. Listar Gestores");
        opcoes.add("3. Procurar Gestor (NIF)");
        opcoes.add("4. Atualizar Gestor (NIF)");
        opcoes.add("5. Eliminar Gestor (NIF)");
        opcoes.add("0. Voltar ao Menu Principal");

        do {
            MenuUtils.exibirSubTitulo("GESTÃO DE GESTORES", opcoes);
            System.out.print("\n" + DesignUtils.GetWhiteBold() + "Selecione uma opção: " + DesignUtils.GetReset());
            opcao = ler.nextLine().trim();

            switch (opcao) {
                case "1":
                    registarGestor(ler);
                    break;
                case "2":
                    listarGestores(ler);
                    break;
                case "3":
                    procurarGestor(ler);
                    break;
                case "4":
                    atualizarGestor(ler);
                    break;
                case "5":
                    eliminarGestor(ler);
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Opção inválida!");
                    MenuUtils.pressionarEnter(ler);
            }
        } while (true);
    }

    private static void registarGestor(Scanner ler) {
        System.out.println("\n--- REGISTO DE GESTOR ---");
        System.out.print("Nome: ");
        String nome = ler.nextLine();
        System.out.print("Morada: ");
        String morada = ler.nextLine();
        System.out.print("NIF: ");
        int nif = 0;
        boolean nifValido = false;
        while (!nifValido) {
            try {
                nif = Integer.parseInt(ler.nextLine());
                nifValido = true;
            } catch (NumberFormatException e) {
                System.out.println("Aviso: NIF deve ser um número inteiro válido. Tente novamente.");
                System.out.print("NIF: ");
            }
        }
        System.out.print("Data de Nascimento (AAAA-MM-DD): ");
        LocalDate dataNascimento = null;
        boolean dataValida = false;
        while (!dataValida) {
            try {
                dataNascimento = LocalDate.parse(ler.nextLine());
                dataValida = true;
            } catch (Exception e) {
                System.out.println("Aviso: Data deve estar no formato AAAA-MM-DD. Tente novamente.");
                System.out.print("Data de Nascimento (AAAA-MM-DD): ");
            }
        }
        System.out.print("Email: ");
        String email = ler.nextLine();
        System.out.print("Palavra-passe: ");
        String passDigitada = ler.nextLine();
        String salt = SenhaUtils.gerarSalt();
        String hash = SenhaUtils.gerarHashComSalt(passDigitada, salt);
        System.out.print("Cargo: ");
        String cargo = ler.nextLine();
        
        if (gestorController.registarGestor(nome, morada, nif, dataNascimento, email, hash, salt, cargo)) {
            System.out.println("Gestor registado com sucesso!");
        } else {
            System.out.println("Erro ao registar: NIF já existe ou dados inválidos.");
        }
        MenuUtils.pressionarEnter(ler);
    }

    private static void listarGestores(Scanner ler) {
        System.out.println("\n--- LISTA DE GESTORES ---");
        List<Gestor> gestores = gestorController.listarGestores();
        if (gestores.isEmpty()) {
            System.out.println("Nenhum gestor registado.");
        } else {
            for (Gestor g : gestores) {
                System.out.println("NIF: " + g.getNif() + " | Nome: " + g.getNome() + " | Cargo: " + g.getCargo());
            }
        }
        MenuUtils.pressionarEnter(ler);
    }

    private static void procurarGestor(Scanner ler) {
        System.out.print("\nDigite o NIF do gestor: ");
        int nif = 0;
        boolean nifValido = false;
        while (!nifValido) {
            try {
                nif = Integer.parseInt(ler.nextLine());
                nifValido = true;
            } catch (NumberFormatException e) {
                System.out.println("Aviso: NIF deve ser um número inteiro válido. Tente novamente.");
                System.out.print("Digite o NIF do gestor: ");
            }
        }
        Gestor g = gestorController.procurarGestorPorNif(nif);
        if (g != null) {
            System.out.println("Dados encontrados: " + g);
        } else {
            System.out.println("Gestor não encontrado.");
        }
        MenuUtils.pressionarEnter(ler);
    }

    private void eliminarGestor(Scanner ler) {
        System.out.print("\nDigite o NIF do gestor a eliminar: ");
        int nif = 0;
        boolean nifValido = false;
        while (!nifValido) {
            try {
                nif = Integer.parseInt(ler.nextLine());
                nifValido = true;
            } catch (NumberFormatException e) {
                System.out.println("Aviso: NIF deve ser um número inteiro válido. Tente novamente.");
                System.out.print("Digite o NIF do gestor a eliminar: ");
            }
        }
        if (gestorController.eliminarGestor(nif)) {
            System.out.println("Gestor eliminado com sucesso!");
        } else {
            System.out.println("Erro ao eliminar: Gestor não encontrado.");
        }
        MenuUtils.pressionarEnter(ler);
    }

    private void atualizarGestor(Scanner ler) {
        System.out.print("\nDigite o NIF do gestor a atualizar: ");
        int nif = 0;
        boolean nifValido = false;
        while (!nifValido) {
            try {
                nif = Integer.parseInt(ler.nextLine());
                nifValido = true;
            } catch (NumberFormatException e) {
                System.out.println("Aviso: NIF deve ser um número inteiro válido. Tente novamente.");
                System.out.print("Digite o NIF do gestor a atualizar: ");
            }
        }
        Gestor g = gestorController.procurarGestorPorNif(nif);

        if (g != null) {
            System.out.println("Dados atuais: " + g);
            System.out.print("Novo Nome (Enter para manter): ");
            String nome = ler.nextLine();
            if (!nome.isEmpty()) g.setNome(nome);

            System.out.print("Nova Morada (Enter para manter): ");
            String morada = ler.nextLine();
            if (!morada.isEmpty()) g.setMorada(morada);

            System.out.print("Novo Email (Enter para manter): ");
            String email = ler.nextLine();
            if (!email.isEmpty()) g.setEmail(email);

            System.out.print("Novo Cargo (Enter para manter): ");
            String cargo = ler.nextLine();
            if (!cargo.isEmpty()) g.setCargo(cargo);

            if (gestorController.atualizarGestor(nif, nome.isEmpty() ? null : nome, morada.isEmpty() ? null : morada, null, email.isEmpty() ? null : email, cargo.isEmpty() ? null : cargo)) {
                System.out.println("Gestor atualizado com sucesso!");
            } else {
                System.out.println("Erro ao atualizar gestor.");
            }
        } else {
            System.out.println("Gestor não encontrado.");
        }
        MenuUtils.pressionarEnter(ler);
    }
}
