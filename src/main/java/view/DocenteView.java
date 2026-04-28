package view;

import common.exceptions.CancelarRegistoException;
import common.utils.BackendUtils;
import common.utils.MenuUtils;
import common.utils.SenhaUtils;
import controller.AvaliacaoController;
import controller.DocenteController;
import controller.EstudanteController;
import controller.UnidadeCurricularController;
import model.Docente;
import model.Resultado;
import model.UnidadeCurricular;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.util.ArrayList;
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
        opcoes.add("0. Logout");

        do {
            try {
                DocenteController dc = new DocenteController();
                Docente docente = dc.procurarDocentePorNif(docenteLogado.getNif());

                MenuUtils.exibirSubTitulo("PORTAL DOCENTE > " + docente.getNome().toUpperCase(), opcoes);
                System.out.print("\n" + GetWhiteBold() + "Selecione uma opção: " + GetReset());
                opcao = scanner.nextLine().trim();

                switch (opcao) {
                    case "1": verUC(docente); break;
                    case "2": alterarPasswordPropria(docente); break;
                    case "3": lancarNotaDocente(docente); break;
                    case "4": consultarFichaDocente(docente); break;
                    case "5": consultarPautaOrdenada(docente); break;
                    case "6": definirMomentosAvaliacao(docente); break;
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

    private void verUC(Docente docenteAtual) {
        try {
            System.out.println(GetBlue() + "\n--- MINHAS UNIDADES CURRICULARES ---" + GetReset());

            DocenteController dc = new DocenteController();
            Docente docenteFresco = dc.procurarDocentePorNif(docenteAtual.getNif());

            List<UnidadeCurricular> minhasUcs = docenteFresco.getUnidadesCurriculares();

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
                novaPass = reader.readLine("Nova senha: ", '*');

                if (novaPass.equals("0")) {
                    throw new CancelarRegistoException("Operação cancelada pelo utilizador.");
                }

                senhaValida = BackendUtils.isSenhaValida(novaPass);
                if(!senhaValida) {
                    System.out.println(GetRed() + "SENHA deve conter pelo menos uma letra maiúscula, um número e um caracter especial. Tente novamente." + GetReset());
                }
            }

            SenhaUtils su = new SenhaUtils();
            String passHash = su.gerarHashComSalt(novaPass);

            DocenteController docenteControllerAtualizado = new DocenteController();
            Resultado resultado = docenteControllerAtualizado.alterarPassword(docente.getNif(), passHash);

            if (resultado.success) {
                System.out.println(GetGreen() + "\nPassword alterada com sucesso!" + GetReset());
            } else {
                System.out.println(GetRed() + "\nErro ao guardar alteração da password: " + resultado.errorMessage + GetReset());
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

    private void lancarNotaDocente(Docente docenteAtual) {
        try {
            System.out.println(GetBlue() + "\n--- LANÇAR NOTA ---" + GetReset());
            System.out.println(GetYellow() + "[Digite '0' a qualquer momento para cancelar | Dica: Nota zero é '0.0']" + GetReset());

            EstudanteController estudanteController = new EstudanteController();
            model.Estudante estudante = null;

            while (estudante == null) {
                try {
                    String mecStr = BackendUtils.lerInputString(scanner, "\nNº Mecanográfico do Estudante: ");
                    if (mecStr.equals("0")) throw new CancelarRegistoException("Operação cancelada pelo utilizador.");

                    int numMec = Integer.parseInt(mecStr);
                    estudante = estudanteController.procurarEstudantePorNumeroMec(numMec);

                    if (estudante == null) {
                        System.out.println(GetRed() + "Erro: Estudante não encontrado! Verifique o número." + GetReset());
                    }
                } catch (NumberFormatException e) {
                    System.out.println(GetRed() + "Aviso: O valor deve ser numérico." + GetReset());
                }
            }

            UnidadeCurricularController ucController = new UnidadeCurricularController();
            model.UnidadeCurricular unidadeCurricular = null;

            while (unidadeCurricular == null) {
                String nomeUC = BackendUtils.lerInputString(scanner, "Nome da Unidade Curricular: ");
                if (nomeUC.equals("0")) throw new CancelarRegistoException("Operação cancelada pelo utilizador.");

                unidadeCurricular = ucController.procurarUCPorNome(nomeUC);

                if (unidadeCurricular == null) {
                    System.out.println(GetRed() + "Erro: Unidade Curricular não encontrada!" + GetReset());
                }
            }

            boolean matriculado = false;
            for (model.Avaliacao inscricao : estudante.getListaAvaliacoes()) {
                if (inscricao.getUnidadeCurricular().getNome().equalsIgnoreCase(unidadeCurricular.getNome())) {
                    matriculado = true;
                    break;
                }
            }

            if (!matriculado) {
                System.out.println(GetRed() + "\nErro: O estudante " + estudante.getNome() + " não se encontra inscrito na UC de " + unidadeCurricular.getNome() + "." + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }

            List<String> momentos = unidadeCurricular.getMomentosAvaliacao();
            if (momentos == null || momentos.isEmpty()) {
                System.out.println(GetRed() + "\nErro: Esta UC ainda não tem momentos de avaliação definidos." + GetReset());
                System.out.println(GetYellow() + "Por favor, utilize a opção 6 do menu primeiro." + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }

            System.out.println("\nSelecione o Momento de Avaliação:");
            for (int i = 0; i < momentos.size(); i++) {
                System.out.println((i + 1) + ". " + momentos.get(i));
            }

            int escolha = -1;
            while (escolha < 1 || escolha > momentos.size()) {
                try {
                    String input = BackendUtils.lerInputString(scanner, "Escolha (1-" + momentos.size() + "): ");
                    if (input.equals("0")) throw new CancelarRegistoException("Operação cancelada pelo utilizador.");
                    escolha = Integer.parseInt(input);
                } catch (NumberFormatException e) {
                    System.out.println(GetRed() + "Aviso: Escolha um número da lista." + GetReset());
                }
            }

            String momentoSelecionado = momentos.get(escolha - 1);

            Double nota = null;
            boolean notaValida = false;

            while (!notaValida) {
                try {
                    System.out.print("Nota (Deixe em branco e dê Enter se for 'Aguardar Lançamento'): ");
                    String notaInput = scanner.nextLine().trim();

                    if (notaInput.equals("0")) {
                        throw new CancelarRegistoException("Operação cancelada pelo utilizador.");
                    }

                    if (notaInput.isEmpty()) {
                        notaValida = true;
                    } else {
                        nota = Double.parseDouble(notaInput.replace(",", "."));

                        if (nota >= 0.0 && nota <= 20.0) {
                            notaValida = true;
                        } else {
                            System.out.println(GetRed() + "Aviso: A nota deve estar entre 0.0 e 20.0." + GetReset());
                        }
                    }
                } catch (NumberFormatException e) {
                    System.out.println(GetRed() + "Aviso: Formato de nota inválido (use números)." + GetReset());
                }
            }

            boolean notaJaExiste = false;
            for(model.Avaliacao avaliacao : estudante.getListaAvaliacoes()) {
                if(avaliacao.getUnidadeCurricular().getNome().equalsIgnoreCase(unidadeCurricular.getNome())
                        && avaliacao.getMomento().equalsIgnoreCase(momentoSelecionado)) {
                    notaJaExiste = true;
                    break;
                }
            }

            if(notaJaExiste) {
                System.out.println(GetRed() + "\nErro: O aluno já tem uma nota lançada para o momento '" + momentoSelecionado + "'." + GetReset());
                System.out.println(GetYellow() + "A edição de notas não é permitida por esta via." + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }

            model.Avaliacao novaAvaliacao = new model.Avaliacao(momentoSelecionado, nota, unidadeCurricular, estudante);

            AvaliacaoController avaliacaoController = new AvaliacaoController();
            if (avaliacaoController.registarAvaliacao(novaAvaliacao)) {
                System.out.println(GetGreen() + "\nAvaliação registada com sucesso!" + GetReset());
            } else {
                System.out.println(GetRed() + "\nErro ao registar avaliação na base de dados." + GetReset());
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

            DocenteController dc = new DocenteController();
            Docente docenteFresco = dc.procurarDocentePorNif(docenteAtual.getNif());

            List<UnidadeCurricular> unidadesCurriculares = docenteFresco.getUnidadesCurriculares();

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
            List<model.Avaliacao> avaliacoesUC = avaliacaoController.listarAvaliacoesPorUC(ucSelecionada.getNome());

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
                        return nota2.compareTo(nota1); // Ordem Descendente
                    });
                    break;
                case "3":
                    avaliacoesUC.sort((a1, a2) -> {
                        Double nota1 = (a1.getNota() != null) ? a1.getNota() : -1.0;
                        Double nota2 = (a2.getNota() != null) ? a2.getNota() : -1.0;
                        return nota1.compareTo(nota2); // Ordem Ascendente
                    });
                    break;
            }
            System.out.println("\n" + GetCyanBold() + "--------------------------------------------------------------------------------" + GetReset());
            System.out.printf(GetWhiteBold() + " %-30s | %-15s | %-15s | %-10s | %-10s\n" + GetReset(), "NOME DO ESTUDANTE", "Nº MEC", "ÉPOCA", "NOTA", "ESTADO");
            System.out.println(GetCyanBold() + "--------------------------------------------------------------------------------" + GetReset());

            for (model.Avaliacao avaliacao : avaliacoesUC) {
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

    private void definirMomentosAvaliacao(Docente docenteAtual) {
        try {
            System.out.println(GetBlue() + "\n--- DEFINIR MOMENTOS DE AVALIAÇÃO ---" + GetReset());

            DocenteController dc = new DocenteController();
            Docente docenteFresco = dc.procurarDocentePorNif(docenteAtual.getNif());
            List<UnidadeCurricular> unidadesCurriculares = docenteFresco.getUnidadesCurriculares();

            if (unidadesCurriculares == null || unidadesCurriculares.isEmpty()) {
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
                    String ucStr = BackendUtils.lerInputString(scanner, "\nSelecione o número da UC (ou 0 para cancelar): ");
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
            UnidadeCurricular unidadeCurricularSelecionada = unidadesCurriculares.get(escolhaUC - 1);

            System.out.println("\nUC Selecionada: " + unidadeCurricularSelecionada.getNome());
            if (unidadeCurricularSelecionada.getMomentosAvaliacao() != null && !unidadeCurricularSelecionada.getMomentosAvaliacao().isEmpty()) {
                System.out.println(GetYellow() + "Momentos atuais: " + String.join(", ", unidadeCurricularSelecionada.getMomentosAvaliacao()) + GetReset());
            }

            String novosMomentos = BackendUtils.lerInputString(scanner, "Digite os momentos separados por vírgula (ex: Teste 1, Exame): ");

            if (!novosMomentos.isEmpty()) {

                String[] momentosArray = novosMomentos.split(",");
                if (momentosArray.length > 3) {
                    System.out.println(GetRed() + "\nErro: O regulamento permite um máximo de 3 avaliações por UC." + GetReset());
                    System.out.println(GetYellow() + "Tentou registar " + momentosArray.length + " momentos. Operação cancelada." + GetReset());
                    MenuUtils.pressionarEnter(scanner);
                    return;
                }
                List<String> listaMomentos = new ArrayList<>();
                for (String m : momentosArray) {
                    if (!m.trim().isEmpty()) {
                        listaMomentos.add(m.trim());
                    }
                }

                UnidadeCurricularController unidadeCurricularControllerAtualizado = new UnidadeCurricularController();
                Resultado resultado = unidadeCurricularControllerAtualizado.definirMomentosAvaliacao(unidadeCurricularSelecionada.getId(), listaMomentos);

                if (resultado.success) {
                    System.out.println(GetGreen() + "\nMomentos de Avaliação guardados com sucesso no sistema!" + GetReset());
                } else {
                    System.out.println(GetRed() + "\nErro ao guardar: " + resultado.errorMessage + GetReset());
                }
            }
            MenuUtils.pressionarEnter(scanner);
        } catch (Exception e) {
            System.out.println(GetRed() + "Ocorreu um erro: " + e.getMessage() + GetReset());
            MenuUtils.pressionarEnter(scanner);
        }
    }
}