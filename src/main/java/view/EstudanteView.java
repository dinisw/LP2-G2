package view;

import common.utils.BackendUtils;
import common.utils.MenuUtils;
import controller.EstatutoController;
import controller.EstudanteController;
import controller.HorarioController;
import controller.JustificacaoFaltaController;
import controller.PresencaController;
import common.exceptions.CancelarRegistoException;
import model.*;

import java.math.BigDecimal;
import java.time.LocalDate;
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
        opcoes.add("5. Ver Meu Horário (v1.3)");
        opcoes.add("6. Marcar Minha Presença (v1.3)");
        opcoes.add("7. Submeter Justificação de Falta (v1.3)");
        opcoes.add("8. Ver Minhas Justificações (v1.3)");
        opcoes.add("9. Ver Meus Estatutos (v1.3)");
        opcoes.add("0. Logout");

        do {
            try {
                EstudanteController ec = new EstudanteController();
                Estudante estudante = ec.procurarEstudantePorNumeroMec(estudanteLogado.getNumeroMec());
                DAL.IAvaliacaoDAO avaliacaoDAO = DAL.DAOFactory.getAvaliacaoDAO();
                estudante.setListaAvaliacoes(avaliacaoDAO.listarPorEstudante(estudante.getNumeroMec()));

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
                    case "5":
                        verMeuHorario(estudante, ler);
                        break;
                    case "6":
                        marcarMinhaPresenca(estudante, ler);
                        break;
                    case "7":
                        submeterJustificacao(estudante, ler);
                        break;
                    case "8":
                        verMinhasJustificacoes(estudante, ler);
                        break;
                    case "9":
                        verMeusEstatutos(estudante, ler);
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

            // Carregar avaliações e curso para mostrar UCs com notas
            DAL.IAvaliacaoDAO avaliacaoDAO = DAL.DAOFactory.getAvaliacaoDAO();
            estudante.setListaAvaliacoes(avaliacaoDAO.listarPorEstudante(estudante.getNumeroMec()));
            List<Avaliacao> avaliacoes = estudante.getListaAvaliacoes();

            DAL.ICursoDAO cursoDAO = DAL.DAOFactory.getCursoDAO();
            model.Curso curso = cursoDAO.procurarPorNome(estudante.getNomeCurso());

            System.out.println("\n" + GetWhiteBold() + "--- Unidades Curriculares do Curso ---" + GetReset());

            if (curso == null || curso.getUnidadeCurriculars() == null || curso.getUnidadeCurriculars().isEmpty()) {
                System.out.println(GetYellow() + "Nenhuma UC atribuída ao curso de momento." + GetReset());
            } else {
                int anoAtual = -1;
                for (model.UnidadeCurricular uc : curso.getUnidadeCurriculars()) {
                    if (uc.getAnoCurricular() != anoAtual) {
                        anoAtual = uc.getAnoCurricular();
                        System.out.println(GetCyanBold() + "\n  " + anoAtual + "º Ano:" + GetReset());
                    }

                    // Calcular média da UC a partir das avaliações do aluno
                    List<Avaliacao> avsUC = new java.util.ArrayList<>();
                    if (avaliacoes != null) {
                        for (Avaliacao a : avaliacoes) {
                            if (a.getUnidadeCurricular() != null
                                    && a.getUnidadeCurricular().getNome().equalsIgnoreCase(uc.getNome())
                                    && a.getNota() != null) {
                                avsUC.add(a);
                            }
                        }
                    }

                    String estadoUC;
                    if (avsUC.isEmpty()) {
                        estadoUC = GetYellow() + "Aguarda avaliação" + GetReset();
                    } else {
                        double soma = avsUC.stream().mapToDouble(Avaliacao::getNota).sum();
                        double media = soma / uc.getMomentosAvaliacao().size();
                        String cor = media >= 9.5 ? GetGreen() : GetRed();
                        estadoUC = cor + String.format("Média: %.1f  %s", media, media >= 9.5 ? "✓ Aprovado" : "✗ Reprovado") + GetReset();
                    }
                    System.out.println("    • " + uc.getNome() + "  —  " + estadoUC);
                }
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

    // ══════════════════════════════════════════════════════════════
    //  v1.3 — HORÁRIO
    // ══════════════════════════════════════════════════════════════
    private static void verMeuHorario(Estudante estudante, Scanner ler) {
        try {
            System.out.println(GetBlue() + "\n--- MEU HORÁRIO ---" + GetReset());
            HorarioController hCtrl = new HorarioController();
            // Usa AnoLetivo atual se disponível; caso contrário lista todos
            List<Horario> horarios = hCtrl.listarTodos().stream()
                    .filter(h -> {
                        if (h.getUnidadeCurricular() == null) return false;
                        // Verifica se o estudante tem avaliação (inscrição) nesta UC
                        return estudante.getListaAvaliacoes() != null &&
                               estudante.getListaAvaliacoes().stream()
                                   .anyMatch(av -> av.getUnidadeCurricular() != null &&
                                       av.getUnidadeCurricular().getNome()
                                           .equalsIgnoreCase(h.getUnidadeCurricular().getNome()));
                    })
                    .sorted(java.util.Comparator.comparing(Horario::getDiaSemana)
                            .thenComparing(Horario::getHoraInicio))
                    .collect(java.util.stream.Collectors.toList());

            if (horarios.isEmpty()) {
                System.out.println(GetYellow() + "Não tem horários atribuídos às suas UCs." + GetReset());
            } else {
                DiaSemana diaAtual = null;
                for (Horario h : horarios) {
                    if (h.getDiaSemana() != diaAtual) {
                        diaAtual = h.getDiaSemana();
                        System.out.println(GetCyanBold() + "\n" + diaAtual.getDescricao() + GetReset());
                    }
                    System.out.printf("  %s–%s | %s | Sala %s%n",
                            h.getHoraInicio(), h.getHoraFim(),
                            h.getUnidadeCurricular().getNome(), h.getSala());
                }
            }
        } catch (Exception e) {
            System.out.println(GetRed() + "Erro: " + e.getMessage() + GetReset());
        }
        MenuUtils.pressionarEnter(ler);
    }

    // ══════════════════════════════════════════════════════════════
    //  v1.3 — PRESENÇA
    // ══════════════════════════════════════════════════════════════
    private static void marcarMinhaPresenca(Estudante estudante, Scanner ler) {
        try {
            System.out.println(GetBlue() + "\n--- MARCAR MINHA PRESENÇA ---" + GetReset());
            HorarioController hCtrl = new HorarioController();
            List<Horario> horarios = hCtrl.listarTodos().stream()
                    .filter(h -> h.getUnidadeCurricular() != null &&
                            estudante.getListaAvaliacoes() != null &&
                            estudante.getListaAvaliacoes().stream().anyMatch(av ->
                                av.getUnidadeCurricular() != null &&
                                av.getUnidadeCurricular().getNome()
                                    .equalsIgnoreCase(h.getUnidadeCurricular().getNome())))
                    .collect(java.util.stream.Collectors.toList());

            if (horarios.isEmpty()) { System.out.println(GetYellow() + "Sem horários disponíveis." + GetReset()); MenuUtils.pressionarEnter(ler); return; }

            for (int i = 0; i < horarios.size(); i++) System.out.printf("%d. %s%n", i+1, horarios.get(i));
            String input = BackendUtils.lerInputString(ler, "Escolha a aula (0 para cancelar): ");
            if (input.equals("0")) return;
            Horario h = horarios.get(Integer.parseInt(input) - 1);

            String dataStr = BackendUtils.lerInputString(ler, "Data da aula (AAAA-MM-DD, Enter = hoje): ");
            LocalDate data = dataStr.trim().isEmpty() ? LocalDate.now() : LocalDate.parse(dataStr);

            PresencaController pCtrl = new PresencaController();
            Resultado<Presenca> res = pCtrl.marcarPresencaEstudante(h.getId(), estudante.getNumeroMec(), data);
            System.out.println(res.sucesso
                    ? GetGreen() + "Presença confirmada com sucesso!" + GetReset()
                    : GetRed() + "Erro: " + res.mensagemErro + GetReset());
        } catch (Exception e) {
            System.out.println(GetRed() + "Erro: " + e.getMessage() + GetReset());
        }
        MenuUtils.pressionarEnter(ler);
    }

    // ══════════════════════════════════════════════════════════════
    //  v1.3 — JUSTIFICAÇÕES
    // ══════════════════════════════════════════════════════════════
    private static void submeterJustificacao(Estudante estudante, Scanner ler) {
        try {
            System.out.println(GetBlue() + "\n--- SUBMETER JUSTIFICAÇÃO DE FALTA ---" + GetReset());

            PresencaController pCtrl = new PresencaController();
            List<Presenca> faltas = pCtrl.listarPresencasPorEstudante(estudante.getNumeroMec()).stream()
                    .filter(Presenca::isFalta).collect(java.util.stream.Collectors.toList());

            if (faltas.isEmpty()) { System.out.println(GetGreen() + "Não tem faltas por justificar." + GetReset()); MenuUtils.pressionarEnter(ler); return; }

            System.out.println(GetWhiteBold() + "As suas faltas:" + GetReset());
            for (int i = 0; i < faltas.size(); i++) System.out.printf("%d. [ID:%d] %s%n", i+1, faltas.get(i).getId(), faltas.get(i));

            String input = BackendUtils.lerInputString(ler, "Escolha a falta a justificar (0 para cancelar): ");
            if (input.equals("0")) return;
            Presenca faltaEscolhida = faltas.get(Integer.parseInt(input) - 1);

            System.out.println(GetWhiteBold() + "\nTipos de justificação:" + GetReset());
            TipoJustificacao[] tipos = TipoJustificacao.values();
            for (int i = 0; i < tipos.length; i++) System.out.printf("%d. %s%n", i+1, tipos[i].getDescricao());
            input = BackendUtils.lerInputString(ler, "Escolha o tipo: ");
            TipoJustificacao tipo = tipos[Integer.parseInt(input) - 1];

            String desc = BackendUtils.lerInputString(ler, "Descrição/motivo: ");

            JustificacaoFaltaController jCtrl = new JustificacaoFaltaController();
            Resultado<JustificacaoFalta> res = jCtrl.submeterJustificacao(
                    faltaEscolhida.getId(), estudante.getNumeroMec(), tipo, desc);
            System.out.println(res.sucesso
                    ? GetGreen() + "Pedido submetido com sucesso. Aguarde aprovação do gestor." + GetReset()
                    : GetRed() + "Erro: " + res.mensagemErro + GetReset());
        } catch (Exception e) {
            System.out.println(GetRed() + "Erro: " + e.getMessage() + GetReset());
        }
        MenuUtils.pressionarEnter(ler);
    }

    private static void verMinhasJustificacoes(Estudante estudante, Scanner ler) {
        try {
            System.out.println(GetBlue() + "\n--- MINHAS JUSTIFICAÇÕES ---" + GetReset());
            JustificacaoFaltaController jCtrl = new JustificacaoFaltaController();
            List<JustificacaoFalta> lista = jCtrl.listarPorEstudante(estudante.getNumeroMec());
            if (lista.isEmpty()) {
                System.out.println(GetYellow() + "Não submeteu nenhuma justificação." + GetReset());
            } else {
                lista.forEach(j -> {
                    String cor = j.getEstado() == JustificacaoFalta.Estado.APROVADA ? GetGreen()
                            : j.getEstado() == JustificacaoFalta.Estado.REJEITADA ? GetRed() : GetYellow();
                    System.out.println(cor + "[" + j.getEstado() + "] " + GetReset() + j);
                    if (j.getObservacaoGestor() != null && !j.getObservacaoGestor().isEmpty())
                        System.out.println("  Observação do gestor: " + j.getObservacaoGestor());
                });
            }
        } catch (Exception e) {
            System.out.println(GetRed() + "Erro: " + e.getMessage() + GetReset());
        }
        MenuUtils.pressionarEnter(ler);
    }

    // ══════════════════════════════════════════════════════════════
    //  v1.3 — ESTATUTOS
    // ══════════════════════════════════════════════════════════════
    private static void verMeusEstatutos(Estudante estudante, Scanner ler) {
        try {
            System.out.println(GetBlue() + "\n--- MEUS ESTATUTOS ---" + GetReset());
            EstatutoController eCtrl = new EstatutoController();
            List<EstatutoEstudante> estatutos = eCtrl.listarEstatutosPorEstudante(estudante.getNumeroMec());
            if (estatutos.isEmpty()) {
                System.out.println(GetYellow() + "Não possui nenhum estatuto especial." + GetReset());
            } else {
                estatutos.forEach(e -> System.out.println(
                        (e.isAtivo() ? GetGreen() + "[ATIVO] " : GetRed() + "[EXPIRADO] ") + GetReset() + e));
            }
        } catch (Exception e) {
            System.out.println(GetRed() + "Erro: " + e.getMessage() + GetReset());
        }
        MenuUtils.pressionarEnter(ler);
    }
}