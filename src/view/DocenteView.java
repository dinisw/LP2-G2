package view;

import Common.DesignUtils;
import Common.MenuUtils;
import Common.SenhaUtils;
import controller.DocenteController;
import controller.UnidadeCurricularController;
import model.Docente;
import model.UnidadeCurricular;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class DocenteView {
    private final DocenteController docenteController;
    private final Scanner scanner;

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
        opcoes.add("0. Logout");

        do {
            MenuUtils.exibirSubTitulo("MENU DOCENTE: " + docente.getNome(), opcoes);
            System.out.print("\n" + DesignUtils.GetWhiteBold() + "Selecione uma opção: " + DesignUtils.GetReset());
            opcao = scanner.nextLine().trim();

            switch (opcao) {
                case "1":
                    verUC();
                    break;
                case "2":
                    alterarPasswordPropria(docente);
                    break;
                case "3":
                    lancarNotaDocente();
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Opção inválida!");
                    MenuUtils.pressionarEnter(scanner);
            }
        } while (true);
    }

    private void alterarPasswordPropria(Docente d) {
        System.out.print("Nova Palavra-passe: ");
        String passDigitada = scanner.nextLine();

        if (!passDigitada.isEmpty()) {
            SenhaUtils su = new SenhaUtils();
            String pass = su.gerarHashComSalt(passDigitada);

            if (docenteController.alterarPassword(d.getNif(), pass)) {
                System.out.println("Password alterada com sucesso!");
            } else {
                System.out.println("Erro ao guardar alteração da password.");
            }
        }
        MenuUtils.pressionarEnter(scanner);
    }

    private void lancarNotaDocente() {
        System.out.println("\n--- LANÇAR NOTA ---");
        System.out.print("Nº Mecanográfico do Estudante: ");
        int numMec = Integer.parseInt(scanner.nextLine().trim());

        DAL.EstudanteCRUD estudanteCRUD = new DAL.EstudanteCRUD();
        model.Estudante estudante = estudanteCRUD.lerEstudante(numMec);

        if (estudante == null) {
            System.out.println("Erro: Estudante não encontrado!");
            Common.MenuUtils.pressionarEnter(scanner);
            return;
        }
        System.out.print("Nome da Unidade Curricular:  ");
        String nomeUC = scanner.nextLine().trim();
        DAL.UnidadeCurricularCRUD unidadeCurricularCRUD = new DAL.UnidadeCurricularCRUD();
        model.UnidadeCurricular unidadeCurricular = unidadeCurricularCRUD.procurarPorNome(nomeUC);

        if (unidadeCurricular == null) {
            System.out.println("Erro: UC não encontrada!");
            Common.MenuUtils.pressionarEnter(scanner);
            return;
        }
        System.out.print("Época de Avaliação (ex. Frequência, Exame): ");
        String momento = scanner.nextLine().trim();

        System.out.println("Nota (Deixe em branco e dê Enter se for 'Aguardar Lançamento'): ");
        String notaInput = scanner.nextLine().trim();

        Double nota = null;
        if (!notaInput.isEmpty()) {
            nota = Double.parseDouble(notaInput.replace(",", "."));
        }

        model.Avaliacao novaAvaliacao = new model.Avaliacao(momento, nota, unidadeCurricular, estudante);
        DAL.AvaliacaoCRUD avaliacaoCRUD = new DAL.AvaliacaoCRUD();

        if (avaliacaoCRUD.registarAvaliacao(novaAvaliacao)) {
            System.out.println(Common.DesignUtils.GetGreen() + "Avaliação registada com sucesso!" + Common.DesignUtils.GetReset());
        } else {
            System.out.println(Common.DesignUtils.GetRed() + "Erro ao registar avaliação." + Common.DesignUtils.GetReset());
        }
        Common.MenuUtils.pressionarEnter(scanner);
    }

    private void verUC() {

        String siglaDocente = docenteController.getSiglaDoDocenteAtual();

        model.Docente docenteAtual = docenteController.procurarDocentePorSigla(siglaDocente);

        List<UnidadeCurricular> minhasUcs = docenteAtual.getUnidadesCurriculares();

        System.out.println("\n--- MINHAS UNIDADES CURRICULARES ---");

        if (minhasUcs == null || minhasUcs.isEmpty()) {
            System.out.println("Não tem Unidades Curriculares atribuídas neste momento.");
        } else {
            for (UnidadeCurricular uc : minhasUcs) {
                System.out.println(uc.getNome() + " (Ano: " + uc.getAnoCurricular() + ")");
            }
        }

        MenuUtils.pressionarEnter(scanner);

    }



















}
