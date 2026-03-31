package view;

import Common.MenuUtils;
import Common.SenhaUtils;
import model.Estudante;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import controller.EstudanteController;

import static Common.DesignUtils.*;

public class EstudanteView {
    public static void exibirMenu(Estudante estudante){
        Scanner ler = new Scanner(System.in);
        String opcao = "";
        ArrayList<String> opcoes = new ArrayList<>();
        opcoes.add("1. Inscrever em Curso");
        opcoes.add("2. Consultar Ficha de Estudante");
        opcoes.add("3. Verificar Notas de Avaliação");
        opcoes.add("0. Voltar ao Menu Principal");

        do {
            MenuUtils.exibirTitulo();
            MenuUtils.exibirSubTitulo("OPÇÕES ESTUDANTE", opcoes);

            System.out.print("\n" + WHITE_BOLD + "Selecione uma opção: " + RESET);
            opcao = ler.nextLine().trim();

            switch (opcao) {
                case "1":
                    System.out.println("\n" + YELLOW + "[EM MANUTENÇÃO] Esta funcionalidade ainda não está finalizada." + RESET);
                    MenuUtils.pressionarEnter(ler);
                    break;
                case "2":
					consultarFichaEstudante(estudante, ler);
                    break;
                case "3":
                    consultarNotasEstudante(estudante, ler);
                    break;
                case "0":
                    return;
                default:
                    System.out.println("\n" + RED + "Opção inválida! Tente novamente." + RESET);
                    MenuUtils.pressionarEnter(ler);
            }
        } while (!opcao.equals("0"));
    }

    public static void inscreverEmCurso(Scanner ler) {
//        Curso c = new Curso();
        String opcao = "";

        do {
//            var opcoes = c.pegarCursos();
//            menu.exibirSubTitulo("CURSOS", opcoes);
//            System.out.println(CYAN_BOLD + bordaInferior + RESET);
//
//            System.out.print("\n" + WHITE_BOLD + "Selecione uma opção: " + RESET);
//            opcao = ler.nextLine().trim();
        }while (!opcao.equals("0"));
    }

    public static void consultarFichaEstudante(Estudante estudante, Scanner ler) {

        System.out.println(CYAN_BOLD + bordaSuperior + RESET);
        System.out.println(CYAN_BOLD + "║" + WHITE_BOLD + "            CONSULTAR FICHA DE ESTUDANTE            " + CYAN_BOLD + "║" + RESET);
        System.out.println(CYAN_BOLD + bordaInferior + RESET);

        EstudanteView ev = new EstudanteView();
        EstudanteController controller = new EstudanteController(estudante, ev);
        System.out.println(controller.exibirFichaEstudante());
        MenuUtils.pressionarEnter(ler);
    }

    public static void consultarNotasEstudante(Estudante estudante, Scanner ler) {
        DAL.AvaliacaoCRUD avaliacaoCRUD = new DAL.AvaliacaoCRUD();
        List<model.Avaliacao> minhasNotas = avaliacaoCRUD.listarPorEstudante(estudante.getNumeroMec());

        System.out.println("\033[H\033[2J");
        System.out.println(CYAN_BOLD + bordaSuperior + RESET);
        System.out.println(CYAN_BOLD + "║" + WHITE_BOLD + "                 PAUTA DE AVALIAÇÕES                  " + CYAN_BOLD + "║" + RESET);
        System.out.println(CYAN_BOLD + bordaMeio + RESET);
        System.out.printf(CYAN_BOLD + "║" + RESET + " %-20s | %-15s | %-10s | %-20s " + CYAN_BOLD + "║\n" + RESET, "Disciplina", "Época", "Nota", "Estado");
        System.out.println(CYAN_BOLD + bordaMeio + RESET);

        if (minhasNotas.isEmpty()) {
            System.out.println(CYAN_BOLD + "║" + YELLOW + " Ainda não existem notas registadas no seu perfil.  " + CYAN_BOLD + "║" + RESET);
        } else {
            for (model.Avaliacao avaliacao : minhasNotas) {
                String notaStr;
                String estado;

                if (avaliacao.getNota() == null) {
                    notaStr = "-";
                    estado = YELLOW + "A Aguardar" + RESET;
                } else {
                    notaStr = String.format("%.2f", avaliacao.getNota());

                    if (avaliacao.getNota() >= 9.5) {
                        estado = GREEN + "Aprovado" + RESET;
                    } else {
                        estado = RED + "Reprovado" + RESET;
                    }
                }

                System.out.printf(CYAN_BOLD + "║" + RESET + " %-20s | %-15s | %-10s | %-29s " + CYAN_BOLD + "║\n" + RESET,
                        avaliacao.getUnidadeCurricular().getNome(), avaliacao.getMomento(), notaStr, estado);
            }

            System.out.println(CYAN_BOLD + bordaMeio + RESET);
            double media = BLL.NotasCalculo.calcularMedia(minhasNotas);
            if (media > 0) {
                System.out.printf(CYAN_BOLD + "║" + WHITE_BOLD + " MÉDIA ATUAL DO CURSO: %-46.2f" + CYAN_BOLD + "║\n" + RESET, media);
            }
        }
        System.out.println(CYAN_BOLD + bordaInferior + RESET);
        Common.MenuUtils.pressionarEnter(ler);
    }
}
