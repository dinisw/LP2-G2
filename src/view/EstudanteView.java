package view;

import common.utils.MenuUtils;
import model.Estudante;
import model.Avaliacao;
import controller.EstudanteController;
import org.jline.builtins.ConsoleOptionGetter;

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
        opcoes.add("0. Logout");

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

    public static void inscreverEmCurso(Estudante estudante, Scanner ler) {
        System.out.println(GetCyanBold() + GetBordaSuperior() + GetReset());
        System.out.println(GetCyanBold() + "║" + GetWhiteBold() + "               INSCRIÇÃO EM CURSO               " + GetCyanBold() + "║" + GetReset());
        System.out.println(GetCyanBold() + GetBordaInferior() + GetReset());

        DAL.CursoCRUD cursoCRUD = new DAL.CursoCRUD();
        List<model.Curso> todosCursos = cursoCRUD.getCursos();
        List<model.Curso> cursosDisponiveis = new java.util.ArrayList<>();

        for (model.Curso curso : todosCursos) {
            if(!curso.isIniciado()) {
                cursosDisponiveis.add(curso);
            }
        }

        if (estudante.getNomeCurso() != null && !estudante.getNomeCurso().equals("SEM REGISTO")) {
            model.Curso cursoAtual = cursoCRUD.procurarPorNome(estudante.getNomeCurso());
            if (cursoAtual != null && cursoAtual.isIniciado()) {
                System.out.println(GetRed() + "O seu curso atual ('" + cursoAtual.getNome() + "') já iniciou o ano letivo." + GetReset());
                System.out.println(GetYellow() + "As transferências de curso encontram-se bloqueadas." + GetReset());
                MenuUtils.pressionarEnter(ler);
                return;
            }
        }

        if (cursosDisponiveis.isEmpty()) {
            System.out.println(GetYellow() + "De momento não existem curso com inscrições abertas." + GetReset());
            MenuUtils.pressionarEnter(ler);
            return;
        }
        System.out.println("\nCursos com Inscrições Abertas:\n");
        for (int i = 0; i < cursosDisponiveis.size(); i++) {
            System.out.println(GetWhiteBold() + (i + 1) + ". " + GetReset() + cursosDisponiveis.get(i).getNome() + " (" + cursosDisponiveis.get(i).getDuracao() + " anos)");
        }
        System.out.println(GetWhiteBold() + "0. " + GetReset() + "Cancelar e Voltar");

        System.out.print("\nEscolha o número do curso pretendido: ");
        try {
            int escolha = Integer.parseInt(ler.nextLine().trim());
            if (escolha == 0) return;

            if (escolha > 0 && escolha <= cursosDisponiveis.size()) {
                String cursoEscolhido = cursosDisponiveis.get(escolha - 1).getNome();
                EstudanteController estudanteController = new EstudanteController();
                if(estudanteController.atualizarEstudante(estudante.getNumeroMec(), null, null, null, cursoEscolhido).success) {
                    estudante.setNomeCurso(cursoEscolhido);
                    System.out.println(GetGreen() + "\nParabéns! Inscrição no curso de " + cursoEscolhido + " realizada com sucesso!" + GetReset());
                } else {
                    System.out.println(GetRed() + "\nErro ao processar a inscrição no sistema." + GetReset());
                }
            } else {
                System.out.println(GetRed() + "Opção inválida." + GetReset());
            }
        } catch (NumberFormatException e) {
            System.out.println(GetRed() + "Entrada inválida. Digite apenas números." + GetReset());
        }
        MenuUtils.pressionarEnter(ler);
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
            System.out.printf(GetCyanBold() + "║" + GetReset() + " %-10s | %-10s | %-20s | %-6s | %-25s " + GetCyanBold() + "║\n" + GetReset(), "Ano Letivo", "Semestre", "Disciplina", "Nota", "Estado");
            System.out.println(GetCyanBold() + GetBordaMeio() + GetReset());

            if (minhasNotas == null || minhasNotas.isEmpty()) {
                System.out.println(GetCyanBold() + "║" + GetYellow() + " Ainda não existem notas registadas no seu perfil.  " + GetCyanBold() + "║" + GetReset());
            } else {
                for (Avaliacao avaliacao : minhasNotas) {
                    String notaStr;
                    String estado;

                    if (avaliacao.getNota() == null) {
                        notaStr = "-";
                        estado = GetYellow() + "A Aguardar lançamento" + GetReset();
                    } else {
                        notaStr = String.format("%.2f", avaliacao.getNota());

                        if (avaliacao.getNota() >= 9.5) {
                            estado = GetGreen() + "Aprovado" + GetReset();
                        } else {
                            estado = GetRed() + "Reprovado" + GetReset();
                        }
                    }

                    String nomeUc = "Desconhecida";
                    String anoLetivo = "-";
                    String semestre = "-";

                    if (avaliacao.getUnidadeCurricular() != null) {
                        nomeUc = avaliacao.getUnidadeCurricular().getNome();
                        anoLetivo = String.valueOf(avaliacao.getUnidadeCurricular().getAnoCurricular()) + "º Ano";
                        semestre = String.valueOf(avaliacao.getUnidadeCurricular().getSemestre()) + "º Sem";
                    }

                    System.out.printf(GetCyanBold() + "║" + GetReset() + " %-10s | %-10s | %-20s | %-6s | %-34s " + GetCyanBold() + "║\n" + GetReset(),
                            anoLetivo, semestre, nomeUc, notaStr, estado);
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