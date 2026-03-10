package view;

import model.Curso;
import model.Estudante;
import model.Menu;

import java.time.LocalDate;
import java.util.ArrayList;
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
        Menu menu = new Menu();
        menu.exibirTitulo();
        Estudante es = new Estudante("","",0, LocalDate.now(), "", "", 123, "","");
        EstudanteView ev = new EstudanteView();
        EstudanteController e = new EstudanteController(es, ev);
        String opcao = "";
        ArrayList<String> opcoes = new ArrayList<>();
        do {
            opcoes.add("1. Inscrever em Curso");
            opcoes.add("2. Consultar Ficha de Estudante");
            opcoes.add("3. Verificar Notas de Avaliação");
            opcoes.add("0. Voltar ao Menu Principal");

            menu.exibirSubTitulo("OPÇÕES", opcoes);
            System.out.println(CYAN_BOLD + bordaInferior + RESET);

            System.out.print("\n" + WHITE_BOLD + "Selecione uma opção: " + RESET);
            opcao = ler.nextLine().trim();

            switch (opcao) {
                case "1":
                    inscreverEmCurso(menu, ler);
                    break;
                case "2":
    //                listarFilaEspera(ler);
                    break;
                case "0":
                    return;
                default:
                    System.out.println("\n" + RED + "Opção inválida! Tente novamente." + RESET);
                    menu.pressionarEnter(ler);
            }
        } while (!opcao.equals("0"));
    }
    public static void inscreverEmCurso(Menu menu, Scanner ler) {
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

}
