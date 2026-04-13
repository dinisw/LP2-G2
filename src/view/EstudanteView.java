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
        opcoes.add("1. Inscrever em Unidades Curriculares");
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
                        inscreverEmUC(estudante, ler);
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

    public static void inscreverEmUC(Estudante estudante, Scanner ler) {
        System.out.println(GetCyanBold() + GetBordaSuperior() + GetReset());
        System.out.println(GetCyanBold() + "║" + GetWhiteBold() + "          INSCRIÇÃO EM UNIDADES CURRICULARES        " + GetCyanBold() + "║" + GetReset());
        System.out.println(GetCyanBold() + GetBordaInferior() + GetReset());

        if (estudante.getNomeCurso() == null || estudante.getNomeCurso().equals("SEM REGISTO")) {
            System.out.println(GetYellow() + "Aviso: Primeiro deve estar inscrito num curso para gerir inscrições em UCs." + GetReset());
            MenuUtils.pressionarEnter(ler);
            return;
        }

        EstudanteController estudanteController = new EstudanteController();
        DAL.CursoCRUD cursoCRUD = new DAL.CursoCRUD();
        model.Curso curso = cursoCRUD.procurarPorNome(estudante.getNomeCurso());

        if (curso == null) {
            System.out.println(GetRed() + "Erro: Curso '" + estudante.getNomeCurso() + "' não encontrado no sistema." + GetReset());
            MenuUtils.pressionarEnter(ler);
            return;
        }

        List<model.Avaliacao> avaliacoes = estudante.getListaAvaliacoes();
        int totalUCsAnoAnterior = 0;
        // Supomos que o curso tem 5 UCs por ano (conforme regra de negócio no modelo Curso)
        totalUCsAnoAnterior = 5; // Valor base para cálculo de aproveitamento do ano anterior

        boolean bloqueado = false;
        if (estudante.getAnoLetivo() > 1) {
             // Verificar aproveitamento do ano anterior (ou atual se ainda não passou)
             // No contexto desta tarefa, se ele tentar inscrever-se e tiver <60%, recebe a mensagem.
             if (!estudanteController.temAproveitamentoSuficiente(estudante, totalUCsAnoAnterior)) {
                 System.out.println(GetRed() + "\nInscrição no ano seguinte não permitida. Aproveitamento insuficiente (mínimo exigido: >60%)." + GetReset());
                 bloqueado = true;
             }
        }

        List<model.UnidadeCurricular> disponiveis = new ArrayList<>();
        List<model.UnidadeCurricular> todasUCsCurso = curso.getUc();

        for (model.UnidadeCurricular uc : todasUCsCurso) {
            // Verificar se o aluno já está inscrito ou já concluiu a UC
            boolean jaInscrito = false;
            boolean aprovado = false;
            for (model.Avaliacao a : avaliacoes) {
                if (a.getUnidadeCurricular().getNome().equalsIgnoreCase(uc.getNome())) {
                    jaInscrito = true;
                    if (a.getNota() != null && a.getNota() >= 9.5) {
                        aprovado = true;
                    }
                    break;
                }
            }

            if (aprovado) continue; // Já passou, não precisa inscrever

            if (bloqueado) {
                // Se bloqueado, apenas permite disciplinas em atraso do ano anterior
                if (uc.getAnoCurricular() < estudante.getAnoLetivo()) {
                    disponiveis.add(uc);
                }
            } else {
                // Se não bloqueado, permite inscrever em UCs do ano atual/seguinte (e atraso)
                if (!jaInscrito) {
                    disponiveis.add(uc);
                }
            }
        }

        if (disponiveis.isEmpty()) {
            System.out.println(GetYellow() + "\nNão existem Unidades Curriculares disponíveis para inscrição de momento." + GetReset());
            MenuUtils.pressionarEnter(ler);
            return;
        }

        System.out.println("\nUnidades Curriculares Disponíveis:\n");
        for (int i = 0; i < disponiveis.size(); i++) {
            model.UnidadeCurricular uc = disponiveis.get(i);
            System.out.println(GetWhiteBold() + (i + 1) + ". " + GetReset() + uc.getNome() + " (" + uc.getAnoCurricular() + "º Ano, " + uc.getSemestre() + "º Semestre)");
        }
        System.out.println(GetWhiteBold() + "0. " + GetReset() + "Voltar");

        System.out.print("\nEscolha o número da UC para se inscrever: ");
        try {
            int escolha = Integer.parseInt(ler.nextLine().trim());
            if (escolha == 0) return;

            if (escolha > 0 && escolha <= disponiveis.size()) {
                model.UnidadeCurricular ucEscolhida = disponiveis.get(escolha - 1);
                
                // Registar a inscrição como uma nova Avaliação (sem nota)
                model.Avaliacao novaInscricao = new model.Avaliacao("1ª Época", null, ucEscolhida, estudante);
                controller.AvaliacaoController avaliacaoController = new controller.AvaliacaoController();
                
                if (avaliacaoController.registarAvaliacao(novaInscricao)) {
                    estudante.adicionarAvaliacao(novaInscricao);
                    System.out.println(GetGreen() + "\nInscrição na UC '" + ucEscolhida.getNome() + "' realizada com sucesso!" + GetReset());
                } else {
                    System.out.println(GetRed() + "\nErro ao registar a inscrição no sistema." + GetReset());
                }
            } else {
                System.out.println(GetRed() + "Opção inválida." + GetReset());
            }
        } catch (NumberFormatException e) {
            System.out.println(GetRed() + "Entrada inválida. Digite apenas números." + GetReset());
        }
        MenuUtils.pressionarEnter(ler);
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