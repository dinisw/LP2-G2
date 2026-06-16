package view;

import common.utils.BackendUtils;
import common.utils.MenuUtils;
import model.Estudante;
import model.Avaliacao;
import controller.EstudanteController;
import common.exceptions.CancelarRegistoException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static common.utils.DesignUtils.*;

public class EstudanteView {

    public static void exibirMenu(Estudante estudanteLogado) {
        MenuUtils.limparTela();
        Scanner ler = new Scanner(System.in);
        String opcao;
        ArrayList<String> opcoes = new ArrayList<>();
        opcoes.add("1. Consultar Ficha de Estudante");
        opcoes.add("2. Verificar Notas de Avaliação");
        opcoes.add("3. Consultar Propinas");
        opcoes.add("4. Pagar Propinas");
        opcoes.add("0. Logout");

        do {
            try {
                EstudanteController ec = new EstudanteController();
                Estudante estudante = ec.procurarEstudantePorNumeroMec(estudanteLogado.getNumeroMec());

                MenuUtils.exibirTitulo();
                MenuUtils.exibirSubTitulo("PORTAL ESTUDANTE > " + estudante.getNome().toUpperCase(), opcoes);

                System.out.print("\n" + GetWhiteBold() + "Selecione uma opção: " + GetReset());
                opcao = ler.nextLine().trim();

                switch (opcao) {
                    case "1":
                        consultarFichaEstudante(estudante, ler);
                        break;
                    case "2":
                        consultarNotasEstudante(estudante, ler);
                        break;
                    case "3":
                        consultarPropinas(estudante, ler);
                        break;
                    case "4":
                        pagarPropinas(estudante, ler);
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


    public static void consultarFichaEstudante(Estudante estudanteAtual, Scanner ler) {
        try {
            System.out.println(GetCyanBold() + GetBordaSuperior() + GetReset());
            System.out.println(GetCyanBold() + "║" + GetWhiteBold() + "            CONSULTAR FICHA DE ESTUDANTE            " + GetCyanBold() + "║" + GetReset());
            System.out.println(GetCyanBold() + GetBordaInferior() + GetReset());

            EstudanteController controller = new EstudanteController();
            Estudante estudante = controller.procurarEstudantePorNumeroMec(estudanteAtual.getNumeroMec());

            System.out.println(controller.obterFichaEstudanteFormatada(estudante));

            System.out.println("\n" + GetWhiteBold() + "--- Unidades Curriculares Inscritas ---" + GetReset());
            List<Avaliacao> avaliacoes = estudante.getListaAvaliacoes();
            boolean temInscricoes = false;

            if (avaliacoes != null) {
                for (Avaliacao a : avaliacoes) {
                    if (a.getNota() == null) {
                        System.out.println("- " + a.getUnidadeCurricular().getNome() + " (" + a.getUnidadeCurricular().getAnoCurricular() + "º Ano)");
                        temInscricoes = true;
                    }
                }
            }

            if (!temInscricoes) {
                System.out.println(GetYellow() + "Nenhuma inscrição ativa de momento." + GetReset());
            }

            MenuUtils.pressionarEnter(ler);

        } catch (Exception e) {
            System.out.println(GetRed() + "Ocorreu um erro ao carregar a ficha: " + e.getMessage() + GetReset());
            MenuUtils.pressionarEnter(ler);
        }
    }

    public static void consultarNotasEstudante(Estudante estudanteAtual, Scanner ler) {
        try {
            EstudanteController controller = new EstudanteController();
            Estudante estudante = controller.procurarEstudantePorNumeroMec(estudanteAtual.getNumeroMec());

            // Usar o DAOFactory (respeita o modo CSV/SQL) — antes instanciava AvaliacaoCRUD (só CSV),
            // pelo que em modo SQL a pauta de notas vinha vazia/errada.
            DAL.IAvaliacaoDAO avaliacaoDAO = DAL.DAOFactory.getAvaliacaoDAO();
            estudante.setListaAvaliacoes(avaliacaoDAO.listarPorEstudante(estudante.getNumeroMec()));

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
                        anoLetivo = avaliacao.getUnidadeCurricular().getAnoCurricular() + "º Ano";
                        semestre = avaliacao.getUnidadeCurricular().getSemestre() + "º Sem";
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

    private static void consultarPropinas(Estudante estudante, Scanner ler) {
        System.out.println(GetBlue() + "\n--- CONSULTAR PROPINAS ---" + GetReset());
        controller.PropinaController propinaController = new controller.PropinaController();
        java.util.List<model.Propina> propinas = propinaController.consultarPropinasEstudante(estudante.getNumeroMec());

        if (propinas == null || propinas.isEmpty()) {
            System.out.println(GetYellow() + "Não tem nenhuma propina gerada neste momento." + GetReset());
            MenuUtils.pressionarEnter(ler);
            return;
        }

        System.out.println("O seu Histórico Financeiro:\n");
        for (int i = 0; i < propinas.size(); i++) {
            model.Propina p = propinas.get(i);
            String estado = p.isTotalmentePaga() ? GetGreen() + "[PAGO]" + GetReset() : GetRed() + "[EM DÍVIDA]" + GetReset();
            System.out.println("--------------------------------------------------");
            System.out.printf("  %d. %dº Ano\n  Total: %.2f EUR | Pago: %.2f EUR | Em Falta: %.2f EUR  %s\n",
                    i + 1, p.getAnoLetivo(), p.getValorTotal(), p.getValorPago(), p.getValorEmDivida(), estado);
        }
        System.out.println("--------------------------------------------------");
        MenuUtils.pressionarEnter(ler);
    }

    private static void pagarPropinas(Estudante estudante, Scanner ler) {
        System.out.println(GetBlue() + "\n--- PAGAR PROPINAS ---" + GetReset());
        System.out.println(GetYellow() + "[Digite '0' a qualquer momento para cancelar a operação!]" + GetReset());

        controller.PropinaController propinaController = new controller.PropinaController();
        java.util.List<model.Propina> propinas = propinaController.consultarPropinasEstudante(estudante.getNumeroMec());

        if (propinas == null || propinas.isEmpty()) {
            System.out.println(GetYellow() + "Não tem nenhuma propina gerada neste momento." + GetReset());
            MenuUtils.pressionarEnter(ler);
            return;
        }

        System.out.println();
        for (int i = 0; i < propinas.size(); i++) {
            model.Propina p = propinas.get(i);
            String estado = p.isTotalmentePaga() ? GetGreen() + "[PAGO]" + GetReset() : GetRed() + "[EM DÍVIDA]" + GetReset();
            System.out.printf("  %d. %dº Ano | Em Falta: %.2f EUR  %s\n", i + 1, p.getAnoLetivo(), p.getValorEmDivida(), estado);
        }

        int escolha = -1;
        while (escolha < 0 || escolha > propinas.size()) {
            System.out.print("\nEscolha o número da propina que deseja pagar (ou '0' para cancelar): ");
            String op = ler.nextLine().trim();
            if (op.equals("0")) {
                System.out.println(GetYellow() + "Operação cancelada pelo utilizador." + GetReset());
                MenuUtils.pressionarEnter(ler);
                return;
            }
            try {
                escolha = Integer.parseInt(op);
                if (escolha < 1 || escolha > propinas.size()) {
                    System.out.println(GetRed() + "Opção inválida. Escolha um número da lista." + GetReset());
                    escolha = -1;
                }
            } catch (NumberFormatException e) {
                System.out.println(GetRed() + "Entrada inválida. Digite apenas números inteiros correspondentes à lista." + GetReset());
                escolha = -1;
            }
        }

        model.Propina propinaSelecionada = propinas.get(escolha - 1);

        if (propinaSelecionada.isTotalmentePaga()) {
            System.out.println(GetYellow() + "Esta propina já se encontra totalmente paga. Não são necessários mais pagamentos." + GetReset());
            MenuUtils.pressionarEnter(ler);
            return;
        }

        BigDecimal valorPagamento = BigDecimal.valueOf(-1);
        while (valorPagamento.compareTo(BigDecimal.ZERO) <= 0) {
            System.out.print("\nIntroduza o valor a pagar agora (ex: 250.50 ou 250,50) ou '0' para cancelar: ");
            String inputValor = ler.nextLine().trim();

            if (inputValor.equals("0")) {
                System.out.println(GetYellow() + "Operação de pagamento cancelada." + GetReset());
                MenuUtils.pressionarEnter(ler);
                return;
            }

            try {
                valorPagamento = new BigDecimal(inputValor.replace(",", "."));
                if (valorPagamento.compareTo(BigDecimal.ZERO) <= 0) {
                    System.out.println(GetRed() + "O valor deve ser superior a zero." + GetReset());
                } else if (valorPagamento.compareTo(propinaSelecionada.getValorEmDivida()) > 0) {
                    System.out.println(GetRed() + "Erro: O valor inserido (" + String.format("%.2f", valorPagamento) + " EUR) é superior ao valor em dívida (" + String.format("%.2f", propinaSelecionada.getValorEmDivida()) + " EUR). Tente novamente." + GetReset());
                    valorPagamento = BigDecimal.valueOf(-1);
                }
            } catch (NumberFormatException e) {
                System.out.println(GetRed() + "Erro: Deve introduzir um valor numérico válido. Use ponto (.) ou vírgula (,) para separar os cêntimos." + GetReset());
                valorPagamento = BigDecimal.valueOf(-1);
            }
        }

        model.Resultado<model.Propina> resultado = propinaController.pagarPropina(estudante.getNumeroMec(), propinaSelecionada.getAnoLetivo(), valorPagamento);

        if (resultado.sucesso) {
            System.out.println(GetGreen() + "\nPagamento de " + String.format("%.2f", valorPagamento) + " EUR processado com sucesso!" + GetReset());
        } else {
            System.out.println(GetRed() + "\nErro: " + resultado.mensagemErro + GetReset());
        }
        MenuUtils.pressionarEnter(ler);
    }
}