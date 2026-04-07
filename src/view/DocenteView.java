package view;

import common.exceptions.CancelarRegistoException;
import common.utils.BackendUtils;
import common.utils.DesignUtils;
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
    private final DocenteController docenteController;
    private final Scanner scanner;
    private EstudanteController estudanteController = new EstudanteController();
    private UnidadeCurricularController ucController = new UnidadeCurricularController();
    private AvaliacaoController avaliacaoController = new AvaliacaoController();

    public DocenteView() {
        this.docenteController = new DocenteController();
        this.scanner = new Scanner(System.in);
    }

    public void exibirMenuPessoalDocente(Docente docente) {
        String opcao;
        ArrayList<String> opcoes = new ArrayList<>();
        opcoes.add("1. Ver minhas Unidades Curriculares");
        opcoes.add("2. Alterar minha Password");
        opcoes.add("3. Lançar Nota de Avaliação");
        opcoes.add("4. Consultar ficha docente");
        opcoes.add("0. Logout");

        do {
            try {
                MenuUtils.exibirSubTitulo("MENU DOCENTE: " + docente.getNome().toUpperCase(), opcoes);
                System.out.print("\n" + GetWhiteBold() + "Selecione uma opção: " + GetReset());
                opcao = scanner.nextLine().trim();

                switch (opcao) {
                    case "1":
                        verUC(docente);
                        break;
                    case "2":
                        alterarPasswordPropria(docente);
                        break;
                    case "3":
                        lancarNotaDocente(docente);
                        break;
                    case "4":
                        consultarFichaDocente(docente);
                        break;
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

            List<UnidadeCurricular> minhasUcs = docenteAtual.getUnidadesCurriculares();

            if (minhasUcs == null || minhasUcs.isEmpty()) {
                System.out.println(GetYellow() + "Não tem Unidades Curriculares atribuídas neste momento." + GetReset());
            } else {
                for (UnidadeCurricular uc : minhasUcs) {
                    System.out.println(uc.getNome() + " (Ano: " + uc.getAnoCurricular() + ")");
                }
            }
            MenuUtils.pressionarEnter(scanner);

        } catch (Exception e) {
            System.out.println(GetRed() + "Ocorreu um erro inesperado: " + e.getMessage() + GetReset());
            MenuUtils.pressionarEnter(scanner);
        }
    }

    private void alterarPasswordPropria(Docente d) {
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

            Resultado res = docenteController.alterarPassword(d.getNif(), passHash);

            if (res.success) {
                System.out.println(GetGreen() + "\nPassword alterada com sucesso!" + GetReset());
            } else {
                System.out.println(GetRed() + "\nErro ao guardar alteração da password: " + res.errorMessage + GetReset());
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

    private void lancarNotaDocente(Docente docente) {
        try {
            System.out.println(GetBlue() + "\n--- LANÇAR NOTA ---" + GetReset());
            System.out.println(GetYellow() + "[Digite '0' para cancelar | Dica: Para atribuir nota zero, digite '0.0']" + GetReset());

            int numMec = 0;
            boolean mecValido = false;
            while (!mecValido) {
                try {
                    String mecStr = BackendUtils.lerInputString(scanner, "Nº Mecanográfico do Estudante: ");
                    numMec = Integer.parseInt(mecStr);
                    mecValido = true;
                } catch (NumberFormatException e) {
                    System.out.println(GetRed() + "Aviso: O valor deve ser numérico." + GetReset());
                }
            }

            model.Estudante estudante = estudanteController.procurarEstudantePorNumeroMec(numMec);
            if (estudante == null) {
                System.out.println(GetYellow() + "\nErro: Estudante não encontrado!" + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }

            String nomeUC = BackendUtils.lerInputString(scanner, "Nome da Unidade Curricular: ");
            model.UnidadeCurricular uc = ucController.procurarUCPorNome(nomeUC);

            if (uc == null) {
                System.out.println(GetYellow() + "\nErro: Unidade Curricular não encontrada!" + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }

            String momento = BackendUtils.lerInputString(scanner, "Época de Avaliação (ex. Frequência, Exame): ");

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

            model.Avaliacao novaAvaliacao = new model.Avaliacao(momento, nota, uc, estudante);

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

    private void consultarFichaDocente(Docente docente){
        try {
            System.out.println(docente.toString());
            System.out.println("Unidades Curriculares Atribuídas:");
            if (docente.getUnidadesCurriculares() == null || docente.getUnidadesCurriculares().isEmpty()) {
                System.out.println(GetYellow() + "Nenhuma unidade curricular atribuída." + GetReset());
            } else {
                for (UnidadeCurricular uc : docente.getUnidadesCurriculares()) {
                    System.out.println("- " + uc.getNome() + " (Ano: " + uc.getAnoCurricular() + ")");
                }
            }
            MenuUtils.pressionarEnter(scanner);
        } catch (Exception e) {
            System.out.println(GetRed() + "Ocorreu um erro inesperado: " + e.getMessage() + GetReset());
            MenuUtils.pressionarEnter(scanner);
        }
    }

}
