package view;

import Common.DesignUtils;
import Common.MenuUtils;
import Common.SenhaUtils;
import controller.DocenteController;
import model.Docente;

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

    public void exibirMenuDocentes() {
        String opcao;
        ArrayList<String> opcoes = new ArrayList<>();
        opcoes.add("1. Listar Docentes");
        opcoes.add("2. Procurar Docente (NIF)");
        opcoes.add("3. Atualizar Docente (NIF)");
        opcoes.add("4. Alterar Password (NIF)");
        opcoes.add("5. Eliminar Docente (NIF)");
        opcoes.add("0. Voltar ao Menu Principal");

        do {
            MenuUtils.exibirSubTitulo("GESTÃO DE DOCENTES", opcoes);
            System.out.print("\n" + DesignUtils.GetWhiteBold() + "Selecione uma opção: " + DesignUtils.GetReset());
            opcao = scanner.nextLine().trim();

            switch (opcao) {
                case "1":
                    listarDocentes();
                    break;
                case "2":
                    procurarDocente();
                    break;
                case "3":
                    atualizarDocente();
                    break;
                case "4":
                    alterarPasswordDocente();
                    break;
                case "5":
                    eliminarDocente();
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Opção inválida!");
                    MenuUtils.pressionarEnter(scanner);
            }
        } while (true);
    }

    private void listarDocentes() {
        System.out.println("\n--- LISTA DE DOCENTES ---");
        List<Docente> docentes = docenteController.listarDocentes();
        if (docentes.isEmpty()) {
            System.out.println("Nenhum docente registado.");
        } else {
            for (Docente d : docentes) {
                System.out.println("NIF: " + d.getNif() + " | Nome: " + d.getNome() + " | Sigla: " + d.getSigla());
            }
        }
        MenuUtils.pressionarEnter(scanner);
    }

    private void procurarDocente() {
        System.out.print("\nDigite o NIF do docente: ");
        int nif = 0;
        boolean nifValido = false;
        while (!nifValido) {
            try {
                nif = Integer.parseInt(scanner.nextLine());
                nifValido = true;
            } catch (NumberFormatException e) {
                System.out.println("Aviso: NIF deve ser um número inteiro válido. Tente novamente.");
                System.out.print("Digite o NIF do docente: ");
            }
        }
        Docente d = docenteController.procurarDocentePorNif(nif);
        if (d != null) {
            System.out.println("Dados encontrados: " + d.getNome() + " - " + d.getEmail());
        } else {
            System.out.println("Docente não encontrado.");
        }
        MenuUtils.pressionarEnter(scanner);
    }

    private void eliminarDocente() {
        System.out.print("\nDigite o NIF do docente a eliminar: ");
        int nif = 0;
        boolean nifValido = false;
        while (!nifValido) {
            try {
                nif = Integer.parseInt(scanner.nextLine());
                nifValido = true;
            } catch (NumberFormatException e) {
                System.out.println("Aviso: NIF deve ser um número inteiro válido. Tente novamente.");
                System.out.print("Digite o NIF do docente a eliminar: ");
            }
        }
        if (docenteController.eliminarDocente(nif)) {
            System.out.println("Docente eliminado com sucesso!");
        } else {
            System.out.println("Erro ao eliminar: Docente não encontrado.");
        }
        MenuUtils.pressionarEnter(scanner);
    }

    private void alterarPasswordDocente() {
        System.out.print("\nDigite o NIF do docente para alterar a password: ");
        int nif = 0;
        boolean nifValido = false;
        while (!nifValido) {
            try {
                nif = Integer.parseInt(scanner.nextLine());
                nifValido = true;
            } catch (NumberFormatException e) {
                System.out.println("Aviso: NIF deve ser um número inteiro válido. Tente novamente.");
                System.out.print("Digite o NIF do docente para alterar a password: ");
            }
        }
        
        Docente d = docenteController.procurarDocentePorNif(nif);

        if (d != null) {
            System.out.println("Docente encontrado: " + d.getNome());
            System.out.print("Nova Palavra-passe: ");
            String passDigitada = scanner.nextLine();
            
            if (!passDigitada.isEmpty()) {
                String salt = SenhaUtils.gerarSalt();
                String pass = SenhaUtils.gerarHashComSalt(passDigitada, salt);
                
                if (docenteController.alterarPassword(nif, pass, salt)) {
                    System.out.println("Password alterada com sucesso!");
                } else {
                    System.out.println("Erro ao guardar alteração da password.");
                }
            } else {
                System.out.println("Operação cancelada: Password não pode ser vazia.");
            }
        } else {
            System.out.println("Docente não encontrado.");
        }
        MenuUtils.pressionarEnter(scanner);
    }

    private void atualizarDocente() {
        System.out.print("\nDigite o NIF do docente a atualizar: ");
        int nif = 0;
        boolean nifValido = false;
        while (!nifValido) {
            try {
                nif = Integer.parseInt(scanner.nextLine());
                nifValido = true;
            } catch (NumberFormatException e) {
                System.out.println("Aviso: NIF deve ser um número inteiro válido. Tente novamente.");
                System.out.print("Digite o NIF do docente a atualizar: ");
            }
        }
        Docente d = docenteController.procurarDocentePorNif(nif);

        if (d != null) {
            System.out.println("Dados atuais: " + d.getNome() + " - " + d.getEmail());
            System.out.print("Novo Nome (Enter para manter): ");
            String nome = scanner.nextLine();
            if (!nome.isEmpty()) d.setNome(nome);

            System.out.print("Nova Morada (Enter para manter): ");
            String morada = scanner.nextLine();
            if (!morada.isEmpty()) d.setMorada(morada);

            System.out.print("Novo Email (Enter para manter): ");
            String email = scanner.nextLine();
            if (!email.isEmpty()) d.setEmail(email);

            System.out.print("Nova Sigla (Enter para manter): ");
            String sigla = scanner.nextLine();
            if (!sigla.isEmpty()) d.setSigla(sigla);

            if (docenteController.atualizarDocente(nif, nome.isEmpty() ? null : nome, morada.isEmpty() ? null : morada, null, email.isEmpty() ? null : email)) {
                System.out.println("Docente atualizado com sucesso!");
            } else {
                System.out.println("Erro ao atualizar docente.");
            }
        } else {
            System.out.println("Docente não encontrado.");
        }
        MenuUtils.pressionarEnter(scanner);
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
                List<model.UnidadeCurricular> minhasUcs = new controller.UnidadeCurricularController().listarUCsPorDocente(docente.getSigla());

                System.out.println("\n--- MINHAS UNIDADES CURRICULARES ---");
                if (minhasUcs == null || minhasUcs.isEmpty()) {
                    System.out.println(DesignUtils.GetYellow() + "Não tem Unidades Curriculares atribuídas neste momento." + DesignUtils.GetReset());
                } else {
                    for (model.UnidadeCurricular uc : minhasUcs) {
                        System.out.println(DesignUtils.GetCyanBold() + uc.getNome() + DesignUtils.GetReset() + " (Ano Curricular: " + uc.getAnoCurricular() + ")");
                    }
                }
                MenuUtils.pressionarEnter(scanner);
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
            String salt = SenhaUtils.gerarSalt();
            String pass = SenhaUtils.gerarHashComSalt(passDigitada, salt);
            
            if (docenteController.alterarPassword(d.getNif(), pass, salt)) {
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
            nota = Double.parseDouble(notaInput.replace(",","."));
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





















}
