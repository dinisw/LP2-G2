package view;

import common.exceptions.CancelarRegistoException;
import common.utils.BackendUtils;
import common.utils.MenuUtils;
import common.utils.SenhaUtils;
import controller.AvaliacaoController;
import controller.DocenteController;
import controller.UnidadeCurricularController;
import model.*;
import model.Horario;
import model.Presenca;

import controller.HorarioController;
import controller.PresencaController;

import java.time.LocalDate;
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
        opcoes.add("8. Marcar Presenças de Aula (v1.3)");
        opcoes.add("9. Ver Faltas por UC (v1.3)");
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
                    case "8": marcarPresencasAula(docente); break;
                    case "9": verFaltasPorUC(docente); break;
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


            String novaPass = "";
            boolean senhaValida = false;
            while (!senhaValida) {
                novaPass = BackendUtils.lerSenhaOculta("Nova senha: ", scanner);

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

            UnidadeCurricularController ucc = new UnidadeCurricularController();
            List<UnidadeCurricular> ucsDocente = ucc.listarUCsPorDocente(docenteFresco.getSigla());

            if (ucsDocente == null || ucsDocente.isEmpty()) {
                System.out.println(GetYellow() + "Nenhuma unidade curricular atribuída." + GetReset());
            } else {
                for (UnidadeCurricular uc : ucsDocente) {
                    System.out.println("- " + uc.getNome() + " (Ano: " + uc.getAnoCurricular() + ", Semestre: " + uc.getSemestre() + ")");
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
                boolean estudanteAtivo = avaliacao.getEstudante() != null && avaliacao.getEstudante().isAtivo();
                String estadoInscricao = estudanteAtivo ? GetGreen() + "Ativo" + GetReset() : GetRed() + "Inativo" + GetReset();
                System.out.printf(" %-30s | %-15d | %-15s | %-10s | %-10s\n", nomeEstudante, mec, epoca, notaStr, estadoInscricao);
            }
            System.out.println(GetCyanBold() + "--------------------------------------------------------------------------------" + GetReset());
            // Média de cada estudante na UC (uma linha por aluno, não por momento)
            java.util.LinkedHashMap<Integer, String> nomePorMec = new java.util.LinkedHashMap<>();
            for (Avaliacao a : avaliacoesUC) {
                if (a.getEstudante() != null) {
                    nomePorMec.putIfAbsent(a.getEstudante().getNumeroMec(), a.getEstudante().getNome());
                }
            }
            System.out.println("\n" + GetWhiteBold() + "Média de cada estudante:" + GetReset());
            System.out.println(GetCyanBold() + "--------------------------------------------------------------------------------" + GetReset());
            for (java.util.Map.Entry<Integer, String> e : nomePorMec.entrySet()) {
                Resultado<String> status = avaliacaoController.obterStatusAprovacao(e.getKey(), ucSelecionada.getNome());
                String statusStr = status.dados != null ? status.dados : "";
                String corStatus;
                if (statusStr.contains("APROVADO"))       corStatus = GetGreen();
                else if (statusStr.contains("REPROVADO")) corStatus = GetRed();
                else                                      corStatus = GetYellow();
                System.out.printf(" %-30s | %-15d | %s%n",
                        e.getValue(), e.getKey(), corStatus + statusStr + GetReset());
            }
            System.out.println(GetCyanBold() + "--------------------------------------------------------------------------------" + GetReset());
            System.out.println(GetWhiteBold() + "Total de estudantes na pauta: " + nomePorMec.size() + GetReset());
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

        List<String> momentos;
        while (true) {
            String inputMomentos = BackendUtils.lerInputString(scanner, "\nDigite os momentos de avaliação separados por vírgula (ex: Frequência, Trabalho Prático): ");
            
            // O teu requisito de usar o CsvUtils foi aplicado aqui!
            momentos = common.utils.CsvUtils.separarStringPorVirgula(inputMomentos);

            // A regra da MAIN que limita a um máximo de 3 momentos
            if (momentos.size() > 3) {
                System.out.println(GetRed() + "Erro: Uma UC pode ter no máximo 3 momentos de avaliação. Tente novamente." + GetReset());
            } else if (momentos.isEmpty()) {
                System.out.println(GetRed() + "Erro: Tem de introduzir pelo menos um momento válido." + GetReset());
            } else {
                break;
            }
        }

        Resultado<UnidadeCurricular> res = docenteController.definirMomentosAvaliacao(docenteLogado.getSigla(), ucSelecionada.getId(), momentos);

        if (res.sucesso) {
            System.out.println(GetGreen() + "\nSucesso! Momentos atualizados para a UC: " + res.dados.getNome() + GetReset());
        } else {
            System.out.println(GetRed() + "\nErro: " + res.mensagemErro + GetReset());
        }
        MenuUtils.pressionarEnter(scanner);
    }

    private void marcarPresencasAula(Docente docenteLogado) {
        try {
            System.out.println(GetBlue() + "\n--- MARCAR PRESENÇAS DE AULA ---" + GetReset());

            UnidadeCurricularController ucc = new UnidadeCurricularController();
            List<UnidadeCurricular> ucsDocente = ucc.listarUCsPorDocente(docenteLogado.getSigla());
            if (ucsDocente.isEmpty()) {
                System.out.println(GetYellow() + "Sem UCs atribuídas." + GetReset());
                MenuUtils.pressionarEnter(scanner); return;
            }

            System.out.println(GetWhiteBold() + "\nAs suas UCs:" + GetReset());
            for (int i = 0; i < ucsDocente.size(); i++)
                System.out.printf("  %d. %s%n", i+1, ucsDocente.get(i).getNome());

            int ucIdx = -1;
            while (ucIdx < 1 || ucIdx > ucsDocente.size()) {
                try {
                    String op = BackendUtils.lerInputString(scanner, "\nEscolha a UC (0 para cancelar): ");
                    ucIdx = Integer.parseInt(op);
                    if (ucIdx < 1 || ucIdx > ucsDocente.size()) {
                        System.out.println(GetRed() + "Opção inválida." + GetReset());
                        ucIdx = -1;
                    }
                } catch (NumberFormatException ex) {
                    System.out.println(GetRed() + "Introduza um número válido." + GetReset());
                }
            }
            UnidadeCurricular uc = ucsDocente.get(ucIdx - 1);

            HorarioController hCtrl = new HorarioController();
            List<Horario> horarios = hCtrl.listarHorariosPorUC(uc.getId());
            if (horarios.isEmpty()) {
                System.out.println(GetYellow() + "Sem horários definidos para esta UC." + GetReset());
                MenuUtils.pressionarEnter(scanner); return;
            }

            System.out.println(GetWhiteBold() + "\nHorários disponíveis:" + GetReset());
            System.out.println(GetCyanBold() + "  ────────────────────────────────────────────────────" + GetReset());
            for (int i = 0; i < horarios.size(); i++) {
                Horario h = horarios.get(i);
                System.out.printf("  %d. %-10s  %s–%s  Sala: %s%n",
                        i+1, h.getDiaSemana().getDescricao(), h.getHoraInicio(), h.getHoraFim(), h.getSala());
            }
            System.out.println(GetCyanBold() + "  ────────────────────────────────────────────────────" + GetReset());

            int hIdx = -1;
            while (hIdx < 1 || hIdx > horarios.size()) {
                try {
                    String op = BackendUtils.lerInputString(scanner, "Escolha o horário (0 para cancelar): ");
                    hIdx = Integer.parseInt(op);
                    if (hIdx < 1 || hIdx > horarios.size()) {
                        System.out.println(GetRed() + "Opção inválida." + GetReset());
                        hIdx = -1;
                    }
                } catch (NumberFormatException ex) {
                    System.out.println(GetRed() + "Introduza um número válido." + GetReset());
                }
            }
            Horario horarioEscolhido = horarios.get(hIdx - 1);

            LocalDate data = null;
            while (data == null) {
                try {
                    System.out.print("Data da aula (AAAA-MM-DD, Enter = hoje): ");
                    String dataStr = scanner.nextLine().trim();
                    data = dataStr.isEmpty() ? LocalDate.now() : LocalDate.parse(dataStr);
                } catch (Exception ex) {
                    System.out.println(GetRed() + "Data inválida. Use o formato AAAA-MM-DD." + GetReset());
                }
            }

            DocenteController dc = new DocenteController();
            List<Estudante> alunos = dc.listarAlunosPorUC(uc.getNome());
            if (alunos.isEmpty()) {
                System.out.println(GetYellow() + "Sem alunos inscritos nesta UC." + GetReset());
                MenuUtils.pressionarEnter(scanner); return;
            }

            System.out.println(GetWhiteBold() + "\nMarcar presença para cada aluno (s/n):" + GetReset());
            System.out.println(GetCyanBold() + "  ────────────────────────────────────────────────────" + GetReset());
            PresencaController pCtrl = new PresencaController();
            int marcados = 0;
            for (Estudante est : alunos) {
                String resp = "";
                while (!resp.equalsIgnoreCase("s") && !resp.equalsIgnoreCase("n")) {
                    System.out.print("  " + est.getNome() + " (Mec: " + est.getNumeroMec() + ") presente? (s/n): ");
                    resp = scanner.nextLine().trim();
                    if (!resp.equalsIgnoreCase("s") && !resp.equalsIgnoreCase("n"))
                        System.out.println(GetRed() + "  Responda 's' ou 'n'." + GetReset());
                }
                if (resp.equalsIgnoreCase("s")) {
                    Resultado<Presenca> res = pCtrl.marcarPresencaDocente(horarioEscolhido.getId(), est.getNumeroMec(), data);
                    if (res.sucesso) marcados++;
                    else System.out.println(GetRed() + "  Aviso: " + res.mensagemErro + GetReset());
                }
            }
            System.out.println(GetCyanBold() + "  ────────────────────────────────────────────────────" + GetReset());
            System.out.println(GetGreen() + "\n" + marcados + " de " + alunos.size() + " presenças marcadas." + GetReset());
        } catch (Exception e) {
            System.out.println(GetRed() + "Erro: " + e.getMessage() + GetReset());
        }
        MenuUtils.pressionarEnter(scanner);
    }

    private void verFaltasPorUC(Docente docenteLogado) {
        try {
            System.out.println(GetBlue() + "\n--- FALTAS POR UC ---" + GetReset());
            UnidadeCurricularController ucc = new UnidadeCurricularController();
            List<UnidadeCurricular> ucs = ucc.listarUCsPorDocente(docenteLogado.getSigla());
            if (ucs.isEmpty()) {
                System.out.println(GetYellow() + "Sem UCs atribuídas." + GetReset());
                MenuUtils.pressionarEnter(scanner); return;
            }

            System.out.println(GetWhiteBold() + "\nAs suas UCs:" + GetReset());
            for (int i = 0; i < ucs.size(); i++)
                System.out.printf("  %d. %s%n", i+1, ucs.get(i).getNome());

            int idx = -1;
            while (idx < 1 || idx > ucs.size()) {
                try {
                    String op = BackendUtils.lerInputString(scanner, "\nEscolha a UC (0 para cancelar): ");
                    idx = Integer.parseInt(op);
                    if (idx < 1 || idx > ucs.size()) {
                        System.out.println(GetRed() + "Opção inválida." + GetReset());
                        idx = -1;
                    }
                } catch (NumberFormatException ex) {
                    System.out.println(GetRed() + "Introduza um número válido." + GetReset());
                }
            }
            UnidadeCurricular uc = ucs.get(idx - 1);

            PresencaController pCtrl = new PresencaController();
            List<Presenca> faltas = pCtrl.listarFaltasPorUC(uc.getId());

            System.out.println("\n" + GetCyanBold() + "────────────────────────────────────────────────────────────────" + GetReset());
            System.out.printf(GetWhiteBold() + " %-8s | %-12s | %-7s | %-22s | %-20s%n" + GetReset(),
                    "ID", "DATA", "MEC", "ESTUDANTE", "HORÁRIO");
            System.out.println(GetCyanBold() + "────────────────────────────────────────────────────────────────" + GetReset());

            if (faltas.isEmpty()) {
                System.out.println(GetGreen() + " Sem faltas registadas para " + uc.getNome() + "." + GetReset());
            } else {
                for (Presenca f : faltas) {
                    String nomeEst  = f.getEstudante() != null ? f.getEstudante().getNome() : "?";
                    int mec         = f.getEstudante() != null ? f.getEstudante().getNumeroMec() : 0;
                    String dataStr  = f.getData() != null ? f.getData().toString() : "?";
                    String horario  = f.getHorario() != null
                            ? f.getHorario().getDiaSemana().getDescricao() + " " + f.getHorario().getHoraInicio()
                            : "?";
                    System.out.printf(" %-8d | %-12s | %-7d | %-22s | %-20s%n",
                            f.getId(), dataStr, mec, nomeEst, horario);
                }
            }
            System.out.println(GetCyanBold() + "────────────────────────────────────────────────────────────────" + GetReset());
            if (!faltas.isEmpty())
                System.out.println(GetWhiteBold() + " Total de faltas: " + faltas.size() + GetReset());
        } catch (Exception e) {
            System.out.println(GetRed() + "Erro: " + e.getMessage() + GetReset());
        }
        MenuUtils.pressionarEnter(scanner);
    }

    private void listarAlunosDaMinhaUC(Docente docenteLogado) {
        try {
            System.out.println(GetBlue() + "\n--- LISTAR ALUNOS DA MINHA UC ---" + GetReset());

            UnidadeCurricularController ucc = new UnidadeCurricularController();
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
            String nomeUc = ucSelecionada.getNome();

            DocenteController docenteController = new DocenteController();
            AvaliacaoController avaliacaoController = new AvaliacaoController();
            List<Estudante> alunos = docenteController.listarAlunosPorUC(nomeUc);

            if (alunos.isEmpty()) {
                System.out.println(GetYellow() + "\nNão há alunos inscritos na UC '" + nomeUc + "'." + GetReset());
            } else {
                System.out.println("\n" + GetCyanBold() + "--- Alunos na UC: " + nomeUc + " ---" + GetReset());
                for (Estudante est : alunos) {
                    Resultado<String> status = avaliacaoController.obterStatusAprovacao(est.getNumeroMec(), nomeUc);
                    System.out.println("Mec: " + est.getNumeroMec() + " | Nome: " + est.getNome() + " | " + status.dados);
                }
            }
            MenuUtils.pressionarEnter(scanner);
        } catch (Exception e) {
            System.out.println(GetRed() + "Ocorreu um erro inesperado: " + e.getMessage() + GetReset());
            MenuUtils.pressionarEnter(scanner);
        }
    }
}