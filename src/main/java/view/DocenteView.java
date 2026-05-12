package view;

import common.exceptions.CancelarRegistoException;
import common.utils.BackendUtils;
import common.utils.MenuUtils;
import common.utils.SenhaUtils;
import controller.AvaliacaoController;
import controller.DocenteController;
import controller.UnidadeCurricularController;
import model.*;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import static common.utils.DesignUtils.*;

public class DocenteView {
    private final Scanner scanner;

    public DocenteView() {
        this.scanner = new Scanner(System.in);
    }

    public void exibirMenuPessoalDocente(Docente docenteLogado) {
        String opcao;
        ArrayList<String> opcoes = new ArrayList<>();
        opcoes.add("1. Ver minhas Unidades Curriculares");
        opcoes.add("2. Alterar minha Password");
        opcoes.add("3. Lançar Nota de Avaliação");
        opcoes.add("4. Consultar ficha docente");
        opcoes.add("5. Consultar Pauta de Alunos (Ordenada)");
        opcoes.add("6. Definir Momentos de Avaliação");
        opcoes.add("7. Listar Alunos da Minha UC");
        opcoes.add("0. Logout");

        do {
            try {
                DocenteController dc = new DocenteController();
                Docente docente = dc.procurarDocentePorNif(docenteLogado.getNif());

                MenuUtils.exibirSubTitulo("PORTAL DOCENTE > " + docente.getNome().toUpperCase(), opcoes);
                System.out.print("\n" + GetWhiteBold() + "Selecione uma opção: " + GetReset());
                opcao = scanner.nextLine().trim();

                switch (opcao) {
                    case "1": verUnidadeCurricular(docente); break;
                    case "2": alterarPasswordPropria(docente); break;
                    case "3": lancarNotaDocente(docente); break;
                    case "4": consultarFichaDocente(docente); break;
                    case "5": consultarPautaOrdenada(docente); break;
                    case "6": definirMomentosAvaliacao(docente); break;
                    case "7": listarAlunosDaMinhaUC(docente); break;
                    case "0":
                        System.out.println(GetYellow() + "\nA efetuar logout..." + GetReset());
                        return;
                    default:
                        System.out.println(GetRed() + "Opção inválida! Por favor, escolha uma opção da lista." + GetReset());
                        MenuUtils.pressionarEnter(scanner);
                }
            } catch (Exception e) {
                System.out.println("\n" + GetRed() + "Ocorreu um erro na navegação: " + e.getMessage() + GetReset());
                MenuUtils.pressionarEnter(scanner);
            }
        } while (true);
    }

    private void verUnidadeCurricular(Docente docenteAtual) {
        try {
            System.out.println(GetBlue() + "\n--- MINHAS UNIDADES CURRICULARES ---" + GetReset());

            UnidadeCurricularController unidadeCurricularController = new UnidadeCurricularController();
            List<UnidadeCurricular> minhasUcs = unidadeCurricularController.listarUCsPorDocente(docenteAtual.getSigla());

            if (minhasUcs == null || minhasUcs.isEmpty()) {
                System.out.println(GetYellow() + "Não tem Unidades Curriculares atribuídas neste momento." + GetReset());
            } else {
                for (int i = 0; i < minhasUcs.size(); i++) {
                    UnidadeCurricular uc = minhasUcs.get(i);
                    System.out.println((i + 1) + ". " + uc.getNome() + " (Ano: " + uc.getAnoCurricular() + ")");
                }
            }
            MenuUtils.pressionarEnter(scanner);

        } catch (Exception e) {
            System.out.println(GetRed() + "Ocorreu um erro inesperado: " + e.getMessage() + GetReset());
            MenuUtils.pressionarEnter(scanner);
        }
    }

    private void alterarPasswordPropria(Docente docente) {
        try {
            System.out.println(GetBlue() + "\n--- ALTERAR A MINHA PASSWORD ---" + GetReset());
            System.out.println(GetYellow() + "[Digite '0' a qualquer momento para cancelar a operação!]" + GetReset());

            Terminal terminal = TerminalBuilder.terminal();
            LineReader reader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .build();

            String novaPass = "";
            boolean senhaValida = false;
            while (!senhaValida) {
                novaPass = BackendUtils.lerSenhaOculta("Nova senha: ");

                if (novaPass.equals("0")) throw new CancelarRegistoException("Operação cancelada pelo utilizador.");

                senhaValida = BackendUtils.isSenhaValida(novaPass);
                if(!senhaValida) {
                    System.out.println(GetRed() + "SENHA deve conter pelo menos uma letra maiúscula, um número e um caracter especial. Tente novamente." + GetReset());
                }
            }

            SenhaUtils su = new SenhaUtils();
            String passHash = su.gerarHashComSalt(novaPass);

            DocenteController docenteControllerAtualizado = new DocenteController();
            Resultado <Docente> resultado = docenteControllerAtualizado.alterarPassword(docente.getNif(), passHash);

            if (resultado.sucesso) {
                System.out.println(GetGreen() + "\nPassword alterada com sucesso!" + GetReset());
            } else {
                System.out.println(GetRed() + "\nErro ao guardar alteração da password: " + resultado.mensagemErro + GetReset());
            }

            MenuUtils.pressionarEnter(scanner);

        } catch (CancelarRegistoException e) {
            System.out.println("\n" + GetYellow() + "Aviso: " + e.getMessage() + GetReset());
            System.out.println(GetRed() + "Operação interrompida!" + GetReset());
            MenuUtils.pressionarEnter(scanner);
        } catch (Exception e) {
            System.out.println(GetRed() + "Ocorreu um erro inesperado: " + e.getMessage() + GetReset());
            MenuUtils.pressionarEnter(scanner);
        }
    }

    private void lancarNotaDocente(Docente docenteLogado) {
        try {
            System.out.println(GetBlue() + "\n--- LANÇAR NOTA ---" + GetReset());
            System.out.println(GetYellow() + "[Digite '0' a qualquer momento para cancelar | Dica: Nota zero é '0.0']" + GetReset());

            UnidadeCurricularController ucc = new UnidadeCurricularController();
            List<UnidadeCurricular> ucsDoDocente = ucc.listarUCsPorDocente(docenteLogado.getSigla());

            if (ucsDoDocente == null || ucsDoDocente.isEmpty()) {
                System.out.println(GetYellow() + "Não tem Unidades Curriculares atribuídas neste momento." + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }

            System.out.println("\n" + GetWhiteBold() + "As suas Unidades Curriculares:" + GetReset());
            for (int i = 0; i < ucsDoDocente.size(); i++) {
                System.out.printf("%d - %s (Ano: %d)\n", i + 1, ucsDoDocente.get(i).getNome(), ucsDoDocente.get(i).getAnoCurricular());
            }

            int escolhaUc = -1;
            while (escolhaUc < 1 || escolhaUc > ucsDoDocente.size()) {
                try {
                    String op = BackendUtils.lerInputString(scanner, "\nEscolha a UC (1-" + ucsDoDocente.size() + "): ");
                    if (op.equals("0")) return;
                    escolhaUc = Integer.parseInt(op);
                    if (escolhaUc < 1 || escolhaUc > ucsDoDocente.size()) System.out.println(GetRed() + "Opção inválida." + GetReset());
                } catch (NumberFormatException e) {
                    System.out.println(GetRed() + "Por favor, introduza um número." + GetReset());
                }
            }

            UnidadeCurricular ucSelecionada = ucsDoDocente.get(escolhaUc - 1);

            List<String> momentos = ucSelecionada.getMomentosAvaliacao();
            if (momentos == null || momentos.isEmpty()) {
                System.out.println(GetRed() + "Erro: Esta UC ainda não tem momentos de avaliação definidos. Utilize a opção 6 primeiro." + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }

            System.out.println("\n" + GetWhiteBold() + "Momentos de Avaliação Disponíveis:" + GetReset());
            for (int i = 0; i < momentos.size(); i++) {
                System.out.printf("%d - %s\n", i + 1, momentos.get(i));
            }

            int escolhaMomento = -1;
            while (escolhaMomento < 1 || escolhaMomento > momentos.size()) {
                try {
                    String op = BackendUtils.lerInputString(scanner, "\nEscolha o momento (1-" + momentos.size() + "): ");
                    if (op.equals("0")) return;
                    escolhaMomento = Integer.parseInt(op);
                    if (escolhaMomento < 1 || escolhaMomento > momentos.size()) System.out.println(GetRed() + "Aviso: Escolha um número da lista." + GetReset());
                } catch (NumberFormatException e) {
                    System.out.println(GetRed() + "Aviso: Entrada inválida." + GetReset());
                }
            }

            String momentoSelecionado = momentos.get(escolhaMomento - 1);

            boolean continuarLancando = true;

            while (continuarLancando) {
                controller.DocenteController docenteController = new controller.DocenteController();
                List<Estudante> alunos = docenteController.listarAlunosPorUC(ucSelecionada.getNome());

                if (alunos == null || alunos.isEmpty()) {
                    System.out.println(GetYellow() + "Não há estudantes inscritos nesta UC." + GetReset());
                    break;
                }

                System.out.println("\n" + GetCyanBold() + "--- Alunos Inscritos em " + ucSelecionada.getNome() + " ---" + GetReset());
                for (int i = 0; i < alunos.size(); i++) {
                    System.out.printf("%d - Nome: %s | Nº Mec: %d\n", i + 1, alunos.get(i).getNome(), alunos.get(i).getNumeroMec());
                }

                int escolhaAluno = -1;
                while (escolhaAluno < 1 || escolhaAluno > alunos.size()) {
                    try {
                        String op = BackendUtils.lerInputString(scanner, "\nEscolha o aluno (1-" + alunos.size() + ") ou 0 para voltar: ");
                        if (op.equals("0")) {
                            continuarLancando = false;
                            break;
                        }
                        escolhaAluno = Integer.parseInt(op);
                        if (escolhaAluno < 1 || escolhaAluno > alunos.size()) System.out.println(GetRed() + "Opção inválida." + GetReset());
                    } catch (NumberFormatException e) {
                        System.out.println(GetRed() + "Por favor, introduza um número." + GetReset());
                    }
                }

                if (!continuarLancando) break;

                Estudante estudanteSelecionado = alunos.get(escolhaAluno - 1);

                Avaliacao avaliacaoExistente = null;
                if (estudanteSelecionado.getListaAvaliacoes() != null) {
                    for (Avaliacao av : estudanteSelecionado.getListaAvaliacoes()) {
                        if (av.getUnidadeCurricular().getNome().equalsIgnoreCase(ucSelecionada.getNome()) &&
                                av.getMomento().equalsIgnoreCase(momentoSelecionado)) {
                            avaliacaoExistente = av;
                            break;
                        }
                    }
                }

                if (avaliacaoExistente != null && avaliacaoExistente.getNota() != null) {
                    String resp = BackendUtils.lerInputString(scanner, "\n" + GetYellow() + "O aluno já possui a nota " + avaliacaoExistente.getNota() + " neste momento. Deseja sobrepor? (s/n): " + GetReset());
                    if (!resp.equalsIgnoreCase("s")) {
                        System.out.println(GetYellow() + "Operação cancelada para este aluno." + GetReset());
                        System.out.print("\n" + GetYellow() + "Deseja lançar outra nota nesta UC para o momento '" + momentoSelecionado + "'? (S/N): " + GetReset());
                        String resposta = scanner.nextLine().trim().toUpperCase();
                        if (!resposta.equals("S")) {
                            continuarLancando = false;
                        }
                        continue;
                    }
                }

                Double nota = null;
                boolean notaValida = false;
                while (!notaValida) {
                    String notaStr = BackendUtils.lerInputString(scanner, "\nNota (Deixe em branco e dê Enter se for 'Aguardar Lançamento' ou 0 para voltar): ");

                    if (notaStr.equals("0")) {
                        notaValida = true;
                        continue;
                    }

                    if (notaStr.isEmpty()) {
                        notaValida = true;
                    } else {
                        try {
                            nota = Double.parseDouble(notaStr.replace(",", "."));
                            if (nota >= 0.0 && nota <= 20.0) {
                                notaValida = true;
                            } else {
                                System.out.println(GetRed() + "Aviso: A nota deve estar entre 0.0 e 20.0." + GetReset());
                            }
                        } catch (NumberFormatException e) {
                            System.out.println(GetRed() + "Aviso: Formato de nota inválido (use números)." + GetReset());
                        }
                    }
                }

                if (nota != null || BackendUtils.lerInputString(scanner, "A nota ficará vazia. Confirmar? (s/n): ").equalsIgnoreCase("s")) {
                    Avaliacao novaAvaliacao = new Avaliacao(momentoSelecionado, nota, ucSelecionada, estudanteSelecionado);
                    controller.AvaliacaoController avaliacaoController = new controller.AvaliacaoController();

                    Resultado<Avaliacao> resultado = avaliacaoController.registarAvaliacao(novaAvaliacao);

                    if (resultado.sucesso) {
                        System.out.println(GetGreen() + "\nAvaliação registada com sucesso!" + GetReset());
                    } else {
                        System.out.println(GetRed() + "\nErro ao registar avaliação na base de dados: " + resultado.mensagemErro + GetReset());
                    }
                }

                System.out.print("\n" + GetYellow() + "Deseja lançar outra nota nesta UC para o momento '" + momentoSelecionado + "'? (S/N): " + GetReset());
                String resposta = scanner.nextLine().trim().toUpperCase();
                if (!resposta.equals("S")) {
                    continuarLancando = false;
                }
            }

        } catch (Exception e) {
            System.out.println(GetRed() + "Ocorreu um erro inesperado: " + e.getMessage() + GetReset());
            MenuUtils.pressionarEnter(scanner);
        }
    }

    private void consultarFichaDocente(Docente docenteAtual){
        try {
            DocenteController dc = new DocenteController();
            Docente docenteFresco = dc.procurarDocentePorNif(docenteAtual.getNif());

            System.out.println(docenteFresco.toString());
            System.out.println("Unidades Curriculares Atribuídas:");
            if (docenteFresco.getUnidadesCurriculares() == null || docenteFresco.getUnidadesCurriculares().isEmpty()) {
                System.out.println(GetYellow() + "Nenhuma unidade curricular atribuída." + GetReset());
            } else {
                for (UnidadeCurricular unidadeCurricular : docenteFresco.getUnidadesCurriculares()) {
                    System.out.println("- " + unidadeCurricular.getNome() + " (Ano: " + unidadeCurricular.getAnoCurricular() + ")");
                }
            }
            MenuUtils.pressionarEnter(scanner);
        } catch (Exception e) {
            System.out.println(GetRed() + "Ocorreu um erro inesperado: " + e.getMessage() + GetReset());
            MenuUtils.pressionarEnter(scanner);
        }
    }

    private void consultarPautaOrdenada(Docente docenteAtual) {
        try {
            System.out.println(GetBlue() + "\n--- CONSULTAR PAUTA DE ALUNOS ---" + GetReset());

            UnidadeCurricularController ucc = new UnidadeCurricularController();
            List<UnidadeCurricular> unidadesCurriculares = ucc.listarUCsPorDocente(docenteAtual.getSigla());

            if(unidadesCurriculares == null || unidadesCurriculares.isEmpty()) {
                System.out.println(GetYellow() + "Não tem Unidades Curriculares atribuídas neste momento." + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }

            System.out.println(GetWhiteBold() + "As suas Unidades Curriculares:" + GetReset());
            for (int i = 0; i < unidadesCurriculares.size(); i++) {
                System.out.println((i + 1) + ". " + unidadesCurriculares.get(i).getNome());
            }

            int escolhaUC = -1;
            boolean ucValida = false;
            while (!ucValida) {
                try {
                    String ucStr = BackendUtils.lerInputString(scanner, "\nSelecione o número da UC para ver a pauta (ou 0 para cancelar): ");
                    escolhaUC = Integer.parseInt(ucStr);

                    if (escolhaUC == 0) return;

                    if (escolhaUC >= 1 && escolhaUC <= unidadesCurriculares.size()) {
                        ucValida = true;
                    } else {
                        System.out.println(GetRed() + "Aviso: Escolha inválida." + GetReset());
                    }
                } catch (NumberFormatException e) {
                    System.out.println(GetRed() + "Aviso: O valor deve ser um número." + GetReset());
                }
            }

            UnidadeCurricular ucSelecionada = unidadesCurriculares.get(escolhaUC - 1);

            AvaliacaoController avaliacaoController = new AvaliacaoController();
            List<Avaliacao> avaliacoesUC = avaliacaoController.listarAvaliacoesPorUC(ucSelecionada.getNome());

            if (avaliacoesUC == null || avaliacoesUC.isEmpty()) {
                System.out.println(GetYellow() + "\nAinda não existem avaliações registadas para a UC " + ucSelecionada.getNome() + "." + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }

            System.out.println("\n" + GetWhiteBold() + "Como deseja ordenar a pauta?" + GetReset());
            System.out.println("1. Ordem Alfabética (Nome do Aluno)");
            System.out.println("2. Nota mais alta para mais baixa");
            System.out.println("3. Nota mais baixa para mais alta");

            String ordem = "";
            while (!ordem.equals("1") && !ordem.equals("2") && !ordem.equals("3")) {
                System.out.print("Escolha uma opção (1-3): ");
                ordem = scanner.nextLine().trim();
            }

            switch (ordem) {
                case "1":
                    avaliacoesUC.sort((a1, a2) -> {
                        String n1 = a1.getEstudante() != null ? a1.getEstudante().getNome() : "";
                        String n2 = a2.getEstudante() != null ? a2.getEstudante().getNome() : "";
                        return n1.compareToIgnoreCase(n2);
                    });                    break;
                case "2":
                    avaliacoesUC.sort((a1, a2) -> {
                        Double nota1 = (a1.getNota() != null) ? a1.getNota() : -1.0;
                        Double nota2 = (a2.getNota() != null) ? a2.getNota() : -1.0;
                        return nota2.compareTo(nota1);
                    });
                    break;
                case "3":
                    avaliacoesUC.sort((a1, a2) -> {
                        Double nota1 = (a1.getNota() != null) ? a1.getNota() : -1.0;
                        Double nota2 = (a2.getNota() != null) ? a2.getNota() : -1.0;
                        return nota1.compareTo(nota2);
                    });
                    break;
            }
            System.out.println("\n" + GetCyanBold() + "--------------------------------------------------------------------------------" + GetReset());
            System.out.printf(GetWhiteBold() + " %-30s | %-15s | %-15s | %-10s | %-10s\n" + GetReset(), "NOME DO ESTUDANTE", "Nº MEC", "ÉPOCA", "NOTA", "ESTADO");
            System.out.println(GetCyanBold() + "--------------------------------------------------------------------------------" + GetReset());

            for (Avaliacao avaliacao : avaliacoesUC) {
                String nomeEstudante = avaliacao.getEstudante().getNome();
                int mec = avaliacao.getEstudante().getNumeroMec();
                String epoca = avaliacao.getMomento();
                String notaStr = (avaliacao.getNota() == null) ? GetYellow() + "A Aguardar" + GetReset() : String.format("%.2f", avaliacao.getNota());
                String estadoInscricao = GetGreen() + "Ativo" + GetReset();
                System.out.printf(" %-30s | %-15d | %-15s | %-10s | %-10s\n", nomeEstudante, mec, epoca, notaStr, estadoInscricao);
            }
            System.out.println(GetCyanBold() + "--------------------------------------------------------------------------------" + GetReset());
            System.out.println(GetWhiteBold() + "Total de estudantes inscritos na pauta: " + avaliacoesUC.size() + GetReset());
            MenuUtils.pressionarEnter(scanner);
        }
        catch (Exception e) {
            System.out.println(GetRed() + "Ocorreu um erro ao carregar a pauta: " + e.getMessage() + GetReset());
            MenuUtils.pressionarEnter(scanner);
        }
    }

    private void definirMomentosAvaliacao(Docente docenteLogado) {
        DocenteController docenteController = new DocenteController();
        UnidadeCurricularController ucc = new UnidadeCurricularController();
        System.out.println(GetBlue() + "\n--- DEFINIR MOMENTOS DE AVALIAÇÃO ---" + GetReset());

        List<UnidadeCurricular> ucsDoDocente = ucc.listarUCsPorDocente(docenteLogado.getSigla());

        if (ucsDoDocente == null || ucsDoDocente.isEmpty()) {
            System.out.println(GetYellow() + "Não tem Unidades Curriculares atribuídas neste momento." + GetReset());
            MenuUtils.pressionarEnter(scanner);
            return;
        }

        System.out.println(GetWhiteBold() + "\nAs suas UCs:" + GetReset());
        for (int i = 0; i < ucsDoDocente.size(); i++) {
            System.out.println((i + 1) + ". " + ucsDoDocente.get(i).getNome() + " (Ano: " + ucsDoDocente.get(i).getAnoCurricular() + ")");
        }

        int escolhaUc = -1;
        while (escolhaUc < 1 || escolhaUc > ucsDoDocente.size()) {
            try {
                String op = BackendUtils.lerInputString(scanner, "\nEscolha o número da UC (ou 0 para cancelar): ");
                if (op.equals("0")) return;
                escolhaUc = Integer.parseInt(op);
                if (escolhaUc < 1 || escolhaUc > ucsDoDocente.size()) System.out.println(GetRed() + "Opção inválida." + GetReset());
            } catch (NumberFormatException e) {
                System.out.println(GetRed() + "Por favor, introduza um número." + GetReset());
            }
        }

        UnidadeCurricular ucSelecionada = ucsDoDocente.get(escolhaUc - 1);

        List<String> momentos = new ArrayList<>();
        while (true) {
            String inputMomentos = BackendUtils.lerInputString(scanner, "\nDigite os momentos de avaliação separados por vírgula (ex: Frequência, Trabalho Prático): ");
            momentos = Arrays.asList(inputMomentos.split(","));

            if (momentos.size() > 3) {
                System.out.println(GetRed() + "Erro: Uma UC pode ter no máximo 3 momentos de avaliação. Tente novamente." + GetReset());
            } else {
                break;
            }
        }

        momentos.replaceAll(String::trim);

        Resultado<UnidadeCurricular> res = docenteController.definirMomentosAvaliacao(docenteLogado.getSigla(), ucSelecionada.getId(), momentos);

        if (res.sucesso) {
            System.out.println(GetGreen() + "\nSucesso! Momentos atualizados para a UC: " + res.dados.getNome() + GetReset());
        } else {
            System.out.println(GetRed() + "\nErro: " + res.mensagemErro + GetReset());
        }
        MenuUtils.pressionarEnter(scanner);
    }

    private void listarAlunosDaMinhaUC(Docente docenteLogado) {
        DocenteController docenteController = new DocenteController();
        AvaliacaoController avaliacaoController = new AvaliacaoController();
        System.out.print("Digite o nome da sua UC para ver os alunos inscritos: ");
        String nomeUc = scanner.nextLine();

        List<Estudante> alunos = docenteController.listarAlunosPorUC(nomeUc);

        if (alunos.isEmpty()) {
            System.out.println("Não há alunos com avaliações registadas nesta UC.");
        } else {
            System.out.println("\n--- Alunos na UC " + nomeUc + " ---");
            for (Estudante est : alunos) {
                Resultado<String> status = avaliacaoController.obterStatusAprovacao(est.getNumeroMec(), nomeUc);

                System.out.println("Mec: " + est.getNumeroMec() + " | Nome: " + est.getNome() + " | " + status.dados);
            }
        }
    }

    private int lerInteiroSeguro(String mensagem) {
        while (true) {
            System.out.print(mensagem);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida. Por favor, introduza um número.");
            }
        }
    }
}