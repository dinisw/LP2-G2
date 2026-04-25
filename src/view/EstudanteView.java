package view;

import common.utils.MenuUtils;
import model.Estudante;
import model.Avaliacao;
import model.Propina; // Importação necessária para a Propina
import controller.EstudanteController;
import controller.PropinaController; // Importação necessária para o Controller da Propina

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
        opcoes.add("4. Consultar e Pagar Propinas"); // NOVA OPÇÃO
        opcoes.add("0. Logout");

        do {
            try {
                MenuUtils.exibirTitulo();
                MenuUtils.exibirSubTitulo("PORTAL ESTUDANTE > " + estudante.getNome().toUpperCase(), opcoes);

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
                    case "4": // NOVO CASE
                        consultarEPagarPropinas(estudante, ler);
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

        // --- A MAGIA DO MVC: Substituímos dezenas de linhas por apenas uma chamada! ---
        int anoDesbloqueado = estudanteController.obterAnoDesbloqueado(estudante);
        // -------------------------------------------------------------------------------

        List<model.Avaliacao> avaliacoes = estudante.getListaAvaliacoes();

        // Um pequeno aviso simpático caso ele não tenha avançado de ano
        long inscritasNoAnoAtual = avaliacoes.stream().filter(a -> a.getUnidadeCurricular().getAnoCurricular() == anoDesbloqueado).count();
        if (inscritasNoAnoAtual > 0) {
            System.out.println(GetYellow() + "\nInscrição no ano seguinte não permitida (Aproveitamento insuficiente ou propina em atraso)." + GetReset());
            System.out.println(GetYellow() + "Apenas estão disponíveis disciplinas em atraso ou do seu nível atual (" + anoDesbloqueado + "º Ano)." + GetReset());
        }

        List<model.UnidadeCurricular> disponiveis = new ArrayList<>();
        List<model.UnidadeCurricular> todasUCsCurso = curso.getUnidadeCurriculars();

        for (model.UnidadeCurricular unidadeCurricular : todasUCsCurso) {
            boolean aAguardarNota = false;
            boolean aprovado = false;
            for (model.Avaliacao a : avaliacoes) {
                if (a.getUnidadeCurricular().getNome().equalsIgnoreCase(unidadeCurricular.getNome())) {
                    aAguardarNota = true;
                    if (a.getNota() != null && a.getNota() >= 9.5) {
                        aprovado = true;
                    }
                }
            }

            if (aprovado || aAguardarNota) continue;

            // Só permite inscrever em UCs até ao ano que tem desbloqueado!
            if (unidadeCurricular.getAnoCurricular() <= anoDesbloqueado) {
                disponiveis.add(unidadeCurricular);
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

        int escolha = -1;
        while (escolha < 0 || escolha > disponiveis.size()) {
            try {
                String op = common.utils.BackendUtils.lerInputString(ler, "\nEscolha o número da UC para se inscrever: ");
                escolha = Integer.parseInt(op);
                if (escolha == 0) return;

                if (escolha < 1 || escolha > disponiveis.size()) {
                    System.out.println(GetRed() + "Opção inválida. Escolha um número da lista." + GetReset());
                }
            } catch (NumberFormatException e) {
                System.out.println(GetRed() + "Entrada inválida. Digite apenas números." + GetReset());
                escolha = -1;
            }
        }

        model.UnidadeCurricular ucEscolhida = disponiveis.get(escolha - 1);
        model.Avaliacao novaInscricao = new model.Avaliacao("A Definir", null, ucEscolhida, estudante);
        controller.AvaliacaoController avaliacaoController = new controller.AvaliacaoController();

        if (avaliacaoController.registarAvaliacao(novaInscricao)) {
            estudante.adicionarAvaliacao(novaInscricao);
            System.out.println(GetGreen() + "\nInscrição na UC '" + ucEscolhida.getNome() + "' realizada com sucesso!" + GetReset());
        } else {
            System.out.println(GetRed() + "\nErro ao registar a inscrição no sistema." + GetReset());
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

    // NOVO MÉTODO PARA PROPINAS
    public static void consultarEPagarPropinas(Estudante estudante, Scanner ler) {
        try {
            System.out.println(GetCyanBold() + GetBordaSuperior() + GetReset());
            System.out.println(GetCyanBold() + "║" + GetWhiteBold() + "           CONSULTAR E PAGAR PROPINAS         " + GetCyanBold() + "║" + GetReset());
            System.out.println(GetCyanBold() + GetBordaInferior() + GetReset());

            PropinaController propinaController = new PropinaController();
            List<Propina> propinas = propinaController.consultarPropinasEstudante(estudante.getNumeroMec());

            if (propinas == null || propinas.isEmpty()) {
                System.out.println(GetYellow() + "\nNão tem nenhuma propina gerada neste momento." + GetReset());
                System.out.println("A sua propina será gerada automaticamente ao ser aprovado e inscrito num ano letivo.");
                MenuUtils.pressionarEnter(ler);
                return;
            }

            System.out.println("\n" + GetWhiteBold() + "O seu Histórico Financeiro:" + GetReset());
            for (int i = 0; i < propinas.size(); i++) {
                Propina p = propinas.get(i);
                String estado = p.isTotalmentePaga() ? GetGreen() + "PAGO" + GetReset() : GetRed() + "EM DÍVIDA" + GetReset();
                System.out.printf("%d. %dº Ano | Total: %.2f€ | Pago: %.2f€ | Em Falta: %.2f€ [%s]\n",
                        i + 1, p.getAnoLetivo(), p.getValorTotal(), p.getValorPago(), p.getValorEmDivida(), estado);
            }

            int escolha = -1;
            while (escolha < 0 || escolha > propinas.size()) {
                try {
                    String op = common.utils.BackendUtils.lerInputString(ler, "\nEscolha o número da propina que deseja pagar (ou 0 para voltar): ");
                    escolha = Integer.parseInt(op);
                    if (escolha == 0) return;

                    if (escolha < 1 || escolha > propinas.size()) {
                        System.out.println(GetRed() + "Opção inválida. Escolha um número da lista." + GetReset());
                    }
                } catch (NumberFormatException e) {
                    System.out.println(GetRed() + "Por favor, digite apenas números." + GetReset());
                    escolha = -1;
                }
            }

            Propina propinaSelecionada = propinas.get(escolha - 1);

            if (propinaSelecionada.isTotalmentePaga()) {
                System.out.println(GetGreen() + "\nEsta propina já se encontra totalmente paga. Não são necessários mais pagamentos." + GetReset());
                MenuUtils.pressionarEnter(ler);
                return;
            }

            System.out.println("\nValor em dívida: " + GetRed() + String.format("%.2f€", propinaSelecionada.getValorEmDivida()) + GetReset());
            double valorPagamento = -1;

            while (valorPagamento <= 0) {
                try {
                    String valorStr = common.utils.BackendUtils.lerInputString(ler, "Introduza o valor a pagar agora (ex: 250.50): ");
                    valorPagamento = Double.parseDouble(valorStr.replace(",", "."));

                    if (valorPagamento <= 0) {
                        System.out.println(GetRed() + "O valor deve ser superior a zero." + GetReset());
                    }
                } catch (NumberFormatException e) {
                    System.out.println(GetRed() + "Formato inválido. Use números e ponto/vírgula para decimais." + GetReset());
                    valorPagamento = -1;
                }
            }

            model.Resultado resultado = propinaController.pagarPropina(estudante.getNumeroMec(), propinaSelecionada.getAnoLetivo(), valorPagamento);

            if (resultado.success) {
                System.out.println(GetGreen() + "\nPagamento de " + String.format("%.2f€", valorPagamento) + " processado com sucesso!" + GetReset());
            } else {
                System.out.println(GetRed() + "\nErro: " + resultado.errorMessage + GetReset());
            }

            MenuUtils.pressionarEnter(ler);

        } catch (Exception e) {
            System.out.println(GetRed() + "Ocorreu um erro: " + e.getMessage() + GetReset());
            MenuUtils.pressionarEnter(ler);
        }
    }
}