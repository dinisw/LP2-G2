package view;

import common.utils.MenuUtils;
import model.Estudante;
import model.Avaliacao;
import controller.EstudanteController;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static common.utils.DesignUtils.*;

public class EstudanteView {

    public static void exibirMenu(Estudante estudante) {
        MenuUtils.limparTela();
        Scanner ler = new Scanner(System.in);
        String opcao;
        ArrayList<String> opcoes = new ArrayList<>();
        opcoes.add("1. Inscrever em Curso");
        opcoes.add("2. Consultar Ficha de Estudante");
        opcoes.add("3. Verificar Notas de Avaliação");
        opcoes.add("0. Voltar ao Menu Principal");

        do {
            try {
                MenuUtils.exibirTitulo();
                MenuUtils.exibirSubTitulo("OPÇÕES ESTUDANTE: " + estudante.getNome().toUpperCase(), opcoes);

                System.out.print("\n" + GetWhiteBold() + "Selecione uma opção: " + GetReset());
                opcao = ler.nextLine().trim();

                switch (opcao) {
                    case "1":
                        System.out.println("\n" + GetYellow() + "[EM MANUTENÇÃO] Esta funcionalidade ainda não está finalizada." + GetReset());
                        MenuUtils.pressionarEnter(ler);
                        break;
                    case "2":
                        consultarFichaEstudante(estudante, ler);
                        break;
                    case "3":
                        consultarNotasEstudante(estudante, ler);
                        break;
                    case "0":
                        System.out.println(GetYellow() + "\nA efetuar logout..." + GetReset());
                        return;
                    default:
                        System.out.println("\n" + GetRed() + "Opção inválida! Tente novamente." + GetReset());
                        MenuUtils.pressionarEnter(ler);
                }
            } catch (Exception e) {
                System.out.println("\n" + GetRed() + "Ocorreu um erro na navegação: " + e.getMessage() + GetReset());
                MenuUtils.pressionarEnter(ler);
            }
        } while (true);
    }

    public static void inscreverEmCurso(Scanner ler) {

        String opcao = "";
        do {
            // Lógica a implementar no futuro
        } while (!opcao.equals("0"));
    }

    public static void consultarFichaEstudante(Estudante estudante, Scanner ler) {
        try {
            System.out.println(GetCyanBold() + GetBordaSuperior() + GetReset());
            System.out.println(GetCyanBold() + "║" + GetWhiteBold() + "            CONSULTAR FICHA DE ESTUDANTE            " + GetCyanBold() + "║" + GetReset());
            System.out.println(GetCyanBold() + GetBordaInferior() + GetReset());

            EstudanteController controller = new EstudanteController();
            System.out.println(controller.obterFichaEstudanteFormatada(estudante));

            MenuUtils.pressionarEnter(ler);

        } catch (Exception e) {
            System.out.println(GetRed() + "Ocorreu um erro ao carregar a ficha: " + e.getMessage() + GetReset());
            MenuUtils.pressionarEnter(ler);
        }
    }

    public static void consultarNotasEstudante(Estudante estudante, Scanner ler) {
        try {
            List<Avaliacao> minhasNotas = estudante.getListaAvaliacoes();

            System.out.println("\033[H\033[2J");
            System.out.println(GetCyanBold() + GetBordaSuperior() + GetReset());
            System.out.println(GetCyanBold() + "║" + GetWhiteBold() + "                 PAUTA DE AVALIAÇÕES                  " + GetCyanBold() + "║" + GetReset());
            System.out.println(GetCyanBold() + GetBordaMeio() + GetReset());
            System.out.printf(GetCyanBold() + "║" + GetReset() + " %-20s | %-15s | %-10s | %-20s " + GetCyanBold() + "║\n" + GetReset(), "Disciplina", "Época", "Nota", "Estado");
            System.out.println(GetCyanBold() + GetBordaMeio() + GetReset());

            if (minhasNotas == null || minhasNotas.isEmpty()) {
                System.out.println(GetCyanBold() + "║" + GetYellow() + " Ainda não existem notas registadas no seu perfil.  " + GetCyanBold() + "║" + GetReset());
            } else {
                for (Avaliacao avaliacao : minhasNotas) {
                    String notaStr;
                    String estado;

                    if (avaliacao.getNota() == null) {
                        notaStr = "-";
                        estado = GetYellow() + "A Aguardar" + GetReset();
                    } else {
                        notaStr = String.format("%.2f", avaliacao.getNota());

                        if (avaliacao.getNota() >= 9.5) {
                            estado = GetGreen() + "Aprovado" + GetReset();
                        } else {
                            estado = GetRed() + "Reprovado" + GetReset();
                        }
                    }

                    String nomeUc = (avaliacao.getUnidadeCurricular() != null) ? avaliacao.getUnidadeCurricular().getNome() : "UC Desconhecida";

                    System.out.printf(GetCyanBold() + "║" + GetReset() + " %-20s | %-15s | %-10s | %-29s " + GetCyanBold() + "║\n" + GetReset(),
                            nomeUc, avaliacao.getMomento(), notaStr, estado);
                }

                System.out.println(GetCyanBold() + GetBordaMeio() + GetReset());

                double media = BLL.NotasCalculo.calcularMedia(minhasNotas);
                if (media > 0) {
                    System.out.printf(GetCyanBold() + "║" + GetWhiteBold() + " MÉDIA ATUAL DO CURSO: %-46.2f" + GetCyanBold() + "║\n" + GetReset(), media);
                }
            }
            System.out.println(GetCyanBold() + GetBordaInferior() + GetReset());
            MenuUtils.pressionarEnter(ler);

        } catch (Exception e) {
            System.out.println(GetRed() + "Ocorreu um erro ao carregar as notas: " + e.getMessage() + GetReset());
            MenuUtils.pressionarEnter(ler);
        }
    }
}