package view;

import Common.Utils;
import model.Curso;
import model.Estudante;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import controller.EstudanteController;

public class EstudanteView {
    //region Design
    public static final String RESET = "\033[0m";
    public static final String CYAN_BOLD = "\033[1;36m";
    public static final String WHITE_BOLD = "\033[1;37m";
    public static final String BLUE = "\033[0;34m";
    public static final String GREEN = "\033[0;32m";
    public static final String RED = "\033[0;31m";
    public static final String YELLOW = "\033[0;33m";
    private static final int LARGURA = 84;
    public static final String bordaSuperior = "╔" + "═".repeat(LARGURA) + "╗";
    public static final String bordaMeio = "╠" + "═".repeat(LARGURA) + "╣";
    public static final String bordaInferior = "╚" + "═".repeat(LARGURA) + "╝";


    public static void Menu(){
        Scanner ler = new Scanner(System.in);
        Utils menu = new Utils();

        String opcao = "";
        ArrayList<String> opcoes = new ArrayList<>();
        opcoes.add("1. Inscrever em Curso");
        opcoes.add("2. Consultar Ficha de Estudante");
        opcoes.add("3. Verificar Notas de Avaliação");
        opcoes.add("4. Gerir Estudantes (CRUD)");
        opcoes.add("0. Voltar ao Menu Principal");

        do {
            menu.exibirTitulo();
            menu.exibirSubTitulo("OPÇÕES ESTUDANTE", opcoes);

            System.out.print("\n" + WHITE_BOLD + "Selecione uma opção: " + RESET);
            opcao = ler.nextLine().trim();

            switch (opcao) {
                case "1":
                    System.out.println("\n" + YELLOW + "[EM MANUTENÇÃO] Esta funcionalidade ainda não está finalizada." + RESET);
                    menu.pressionarEnter(ler);
                    break;
                case "2":
					consultarFichaEstudante(menu, ler);
                    break;
                case "3":
                    System.out.println("\n" + YELLOW + "[EM MANUTENÇÃO] Esta funcionalidade ainda não está finalizada." + RESET);
                    menu.pressionarEnter(ler);
                    break;
                case "4":
                    exibirMenuCRUD(menu, ler);
                    break;
                case "0":
                    return;
                default:
                    System.out.println("\n" + RED + "Opção inválida! Tente novamente." + RESET);
                    menu.pressionarEnter(ler);
            }
        } while (!opcao.equals("0"));
    }
    private static void exibirMenuCRUD(Utils menu, Scanner ler) {
        DAL.EstudanteCRUD crud = new DAL.EstudanteCRUD();
        String opcao = "";
        ArrayList<String> opcoes = new ArrayList<>();
        opcoes.add("1. Registar Estudante");
        opcoes.add("2. Listar Estudantes");
        opcoes.add("3. Procurar Estudante (Número Mec)");
        opcoes.add("4. Eliminar Estudante (Número Mec)");
        opcoes.add("0. Voltar");

        do {
            menu.exibirSubTitulo("CRUD - ESTUDANTES", opcoes);
            System.out.print("\n" + WHITE_BOLD + "Selecione uma opção: " + RESET);
            opcao = ler.nextLine().trim();

            switch (opcao) {
                case "1":
                    System.out.println("\n--- REGISTO DE ESTUDANTE ---");
                    System.out.print("Nome: ");
                    String nome = ler.nextLine();
                    System.out.print("Morada: ");
                    String morada = ler.nextLine();
                    System.out.print("NIF: ");
                    int nif = Integer.parseInt(ler.nextLine());
                    System.out.print("Data de Nascimento (AAAA-MM-DD): ");
                    LocalDate data = LocalDate.parse(ler.nextLine());
                    System.out.print("Email: ");
                    String email = ler.nextLine();
                    System.out.print("Número Mecanográfico: ");
                    int mec = Integer.parseInt(ler.nextLine());
                    System.out.print("Palavra-passe: ");
                    String pass = ler.nextLine();
                    System.out.print("Curso: ");
                    String curso = ler.nextLine();

                    model.Estudante novo = new model.Estudante(nome, morada, nif, data, email, mec, pass, curso);
                    if (crud.registarEstudante(novo)) {
                        System.out.println(GREEN + "Estudante registado com sucesso!" + RESET);
                    } else {
                        System.out.println(RED + "Erro ao registar estudante." + RESET);
                    }
                    menu.pressionarEnter(ler);
                    break;
                case "2":
                    System.out.println("\n--- LISTA DE ESTUDANTES ---");
                    List<model.Estudante> lista = crud.getEstudantes();
                    if (lista.isEmpty()) {
                        System.out.println("Nenhum estudante registado.");
                    } else {
                        for (model.Estudante e : lista) {
                            System.out.println("Mec: " + e.getNumeroMec() + " | Nome: " + e.getNome() + " | Curso: " + e.getNomeCurso());
                        }
                    }
                    menu.pressionarEnter(ler);
                    break;
                case "3":
                    System.out.print("\nDigite o Número Mecanográfico: ");
                    int nmec = Integer.parseInt(ler.nextLine());
                    model.Estudante est = crud.lerEstudante(nmec);
                    if (est != null) {
                        System.out.println("Dados: " + est.getNome() + " - " + est.getNomeCurso());
                    } else {
                        System.out.println(RED + "Estudante não encontrado." + RESET);
                    }
                    menu.pressionarEnter(ler);
                    break;
                case "4":
                    System.out.print("\nDigite o Número Mecanográfico a eliminar: ");
                    int dmec = Integer.parseInt(ler.nextLine());
                    if (crud.eliminarEstudante(dmec)) {
                        System.out.println(GREEN + "Estudante eliminado com sucesso!" + RESET);
                    } else {
                        System.out.println(RED + "Erro ao eliminar: Estudante não encontrado." + RESET);
                    }
                    menu.pressionarEnter(ler);
                    break;
                case "0":
                    return;
                default:
                    System.out.println(RED + "Opção inválida!" + RESET);
                    menu.pressionarEnter(ler);
            }
        } while (!opcao.equals("0"));
    }

    public static void inscreverEmCurso(Utils menu, Scanner ler) {
        Curso c = new Curso();
        String opcao = "";

        do {
            var opcoes = c.pegarCursos();
            menu.exibirSubTitulo("CURSOS", opcoes);
            System.out.println(CYAN_BOLD + bordaInferior + RESET);

            System.out.print("\n" + WHITE_BOLD + "Selecione uma opção: " + RESET);
            opcao = ler.nextLine().trim();
        }while (!opcao.equals("0"));
    }

    public static void consultarFichaEstudante(Utils menu, Scanner ler) {

        System.out.println(CYAN_BOLD + bordaSuperior + RESET);
        System.out.println(CYAN_BOLD + "║" + WHITE_BOLD + "            CONSULTAR FICHA DE ESTUDANTE            " + CYAN_BOLD + "║" + RESET);
        System.out.println(CYAN_BOLD + bordaInferior + RESET);

        System.out.print("\n" + WHITE_BOLD + "Insira o seu Número Mecanográfico: " + RESET);

        try {
            int numMec = Integer.parseInt(ler.nextLine().trim());

            DAL.EstudanteCRUD crud = new DAL.EstudanteCRUD();
            Estudante alunoEncontrado = crud.lerEstudante(numMec);

            if (alunoEncontrado != null) {
                EstudanteView ev = new EstudanteView();
                EstudanteController controller = new EstudanteController(alunoEncontrado, ev);

                System.out.println();

                controller.exibirFichaEstudante();
            } else {
                System.out.println("\n" + RED + "Erro: Estudante com o número " + numMec + " não foi encontrado no sistema." + RESET);
            }

        } catch (NumberFormatException e) {
            System.out.println("\n" + RED + "Erro: Formato de número inválido. Digite apenas algarismos." + RESET);
        }
        menu.pressionarEnter(ler);
    }
}
