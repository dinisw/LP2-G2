package view;

import Common.DesignUtils;
import Common.MenuUtils;
import controller.DocenteController;
import controller.UnidadeCurricularController;
import model.Docente;
import model.UnidadeCurricular;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class UnidadeCurricularView {
    private final UnidadeCurricularController ucController;
    private final DocenteController docenteController;
    private final Scanner scanner;

    public UnidadeCurricularView() {
        this.ucController = new UnidadeCurricularController();
        this.docenteController = new DocenteController();
        this.scanner = new Scanner(System.in);
    }

    public void exibirMenuUnidadesCurriculares() {
        String opcao;
        ArrayList<String> opcoes = new ArrayList<>();
        opcoes.add("1. Registar Unidade Curricular");
        opcoes.add("2. Listar Unidades Curriculares");
        opcoes.add("3. Procurar Unidade Curricular");
        opcoes.add("4. Atualizar Unidade Curricular");
        opcoes.add("5. Eliminar Unidade Curricular");
        opcoes.add("0. Voltar ao Menu de Gestão");

        do {
            MenuUtils.exibirSubTitulo("GESTÃO DE UNIDADES CURRICULARES", opcoes);
            System.out.print("\n" + DesignUtils.GetWhiteBold() + "Selecione uma opção: " + DesignUtils.GetReset());
            opcao = scanner.nextLine().trim();

            switch (opcao) {
                case "1": registarUnidadeCurricular(); break;
                case "2": listarUnidadesCurriculares(); break;
                case "3": procurarUnidadeCurricular(); break;
                case "4": atualizarUnidadeCurricular(); break;
                case "5": eliminarUnidadeCurricular(); break;
                case "0": return;
                default:
                    System.out.println(DesignUtils.GetRed() + "Opção inválida!" + DesignUtils.GetReset());
                    MenuUtils.pressionarEnter(scanner);
            }
        } while (!opcao.equals("0"));
    }

    private void registarUnidadeCurricular() {
        System.out.println("\n--- REGISTO DE UNIDADE CURRICULAR ---");
        System.out.print("Nome da UC: ");
        String nome = scanner.nextLine().trim();

        if (nome.isEmpty()) {
            System.out.println(DesignUtils.GetRed() + "Erro: Nome não pode estar vazio." + DesignUtils.GetReset());
            MenuUtils.pressionarEnter(scanner);
            return;
        }

        System.out.print("Ano Curricular (1, 2 ou 3): ");
        int ano = 0;
        boolean anoValido = false;
        while (!anoValido) {
            try {
                ano = Integer.parseInt(scanner.nextLine().trim());
                if (ano < 1 || ano > 3) {
                    System.out.println(DesignUtils.GetYellow() + "Aviso: Ano deve ser 1, 2 ou 3." + DesignUtils.GetReset());
                    System.out.print("Ano Curricular (1, 2 ou 3): ");
                } else {
                    anoValido = true;
                }
            } catch (NumberFormatException e) {
                System.out.println(DesignUtils.GetYellow() + "Aviso: Ano deve ser um número inteiro válido. Tente novamente." + DesignUtils.GetReset());
                System.out.print("Ano Curricular (1, 2 ou 3): ");
            }
        }

        listarDocentesDisponiveis();
        System.out.print("Sigla do Docente Responsável (ou prima Enter para nenhum): ");
        String siglaDocente = scanner.nextLine().trim().toUpperCase();

        Docente docente = null;
        if (!siglaDocente.isEmpty()) {
            docente = docenteCRUD.procurarPorSigla(siglaDocente);
            if (docente == null) {
                System.out.println(DesignUtils.GetRed() + "Erro: Docente com sigla '" + siglaDocente + "' não encontrado!" + DesignUtils.GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }
        }

        UnidadeCurricular novaUC = new UnidadeCurricular(nome, ano, docente);

        if (ucCRUD.registarUC(novaUC)) {
            System.out.println(DesignUtils.GetGreen() + "UC registada com sucesso!" + DesignUtils.GetReset());
            imprimirDadosUnidadeCurricular(novaUC);
        } else {
            System.out.println(DesignUtils.GetRed() + "Erro: UC com esse nome já existe ou dados inválidos." + DesignUtils.GetReset());
        }

        MenuUtils.pressionarEnter(scanner);
    }

    private void listarUnidadesCurriculares() {
        System.out.println("\n--- LISTA DE UNIDADES CURRICULARES ---");
        List<UnidadeCurricular> ucs = ucCRUD.getUcs();

        if (ucs.isEmpty()) {
            System.out.println(DesignUtils.GetYellow() + "Nenhuma UC registada até ao momento!" + DesignUtils.GetReset());
        } else {
            System.out.println(DesignUtils.GetWhiteBold() + "\nNome | Ano | Docente | ECTS" + DesignUtils.GetReset());
            System.out.println("---------------------------------------------------");
            for (UnidadeCurricular uc : ucs) {
                String docenteNome = (uc.getDocente() != null) ? uc.getDocente().getNome() : "NÃO ATRIBUÍDO";
                System.out.printf("%s | %d | %s | %d\n", uc.getNome(), uc.getAnoCurricular(), docenteNome, uc.getEcts());
            }
        }

        MenuUtils.pressionarEnter(scanner);
    }

    private void procurarUnidadeCurricular() {
        listarUnidadesCurriculares();
        System.out.print("\nDigite o nome da UC a procurar: ");
        String nome = scanner.nextLine().trim();

        if (nome.isEmpty()) {
            System.out.println(DesignUtils.GetRed() + "Erro: Nome não pode estar vazio." + DesignUtils.GetReset());
            MenuUtils.pressionarEnter(scanner);
            return;
        }

        UnidadeCurricular uc = ucCRUD.procurarPorNome(nome);

        if (uc != null) {
            System.out.println(DesignUtils.GetGreen() + "\nUC Encontrada:" + DesignUtils.GetReset());
            imprimirDadosUnidadeCurricular(uc);
        } else {
            System.out.println(DesignUtils.GetRed() + "UC não encontrada." + DesignUtils.GetReset());
        }

        MenuUtils.pressionarEnter(scanner);
    }

    private void atualizarUnidadeCurricular() {
        listarUnidadesCurriculares();
        System.out.print("\nDigite o nome da UC a atualizar: ");
        String nomeAtual = scanner.nextLine().trim();

        if (nomeAtual.isEmpty()) {
            System.out.println(DesignUtils.GetRed() + "Erro: Nome não pode estar vazio." + DesignUtils.GetReset());
            MenuUtils.pressionarEnter(scanner);
            return;
        }

        UnidadeCurricular ucExistente = ucCRUD.procurarPorNome(nomeAtual);

        if (ucExistente == null) {
            System.out.println(DesignUtils.GetRed() + "UC não encontrada." + DesignUtils.GetReset());
            MenuUtils.pressionarEnter(scanner);
            return;
        }

        System.out.println("\n--- ATUALIZAR UNIDADE CURRICULAR ---");
        System.out.println("Dados atuais: " + ucExistente.getNome());
        System.out.print("Novo nome (Enter para manter): ");
        String novoNome = scanner.nextLine().trim();
        if (novoNome.isEmpty()) {
            novoNome = ucExistente.getNome();
        }

        System.out.print("Novo ano curricular (Enter para manter): ");
        int novoAno = ucExistente.getAnoCurricular();
        String anoInput = scanner.nextLine().trim();
        if (!anoInput.isEmpty()) {
            try {
                novoAno = Integer.parseInt(anoInput);
                if (novoAno < 1 || novoAno > 3) {
                    System.out.println(DesignUtils.GetYellow() + "Aviso: Ano deve ser 1, 2 ou 3. Mantendo o original." + DesignUtils.GetReset());
                    novoAno = ucExistente.getAnoCurricular();
                }
            } catch (NumberFormatException e) {
                System.out.println(DesignUtils.GetYellow() + "Aviso: Ano inválido. Mantendo o original." + DesignUtils.GetReset());
                novoAno = ucExistente.getAnoCurricular();
            }
        }

        listarDocentesDisponiveis();
        System.out.print("Sigla do novo Docente (Enter para manter): ");
        String novaSignaDocente = scanner.nextLine().trim().toUpperCase();

        Docente novoDocente = ucExistente.getDocente();
        if (!novaSignaDocente.isEmpty()) {
            novoDocente = docenteCRUD.procurarPorSigla(novaSignaDocente);
            if (novoDocente == null) {
                System.out.println(DesignUtils.GetRed() + "Erro: Docente não encontrado. Mantendo o original." + DesignUtils.GetReset());
                novoDocente = ucExistente.getDocente();
            }
        }

        UnidadeCurricular ucAtualizada = new UnidadeCurricular(novoNome, novoAno, novoDocente);

        if (ucCRUD.atualizarUC(nomeAtual, ucAtualizada)) {
            System.out.println(DesignUtils.GetGreen() + "UC atualizada com sucesso!" + DesignUtils.GetReset());
            imprimirDadosUnidadeCurricular(ucAtualizada);
        } else {
            System.out.println(DesignUtils.GetRed() + "Erro: Não é permitida alteração. UC tem docente alocado e estudantes inscritos." + DesignUtils.GetReset());
        }

        MenuUtils.pressionarEnter(scanner);
    }

    private void eliminarUnidadeCurricular() {
        listarUnidadesCurriculares();
        System.out.print("\nDigite o nome da UC a eliminar: ");
        String nome = scanner.nextLine().trim();

        if (nome.isEmpty()) {
            System.out.println(DesignUtils.GetRed() + "Erro: Nome não pode estar vazio." + DesignUtils.GetReset());
            MenuUtils.pressionarEnter(scanner);
            return;
        }

        UnidadeCurricular uc = ucCRUD.procurarPorNome(nome);

        if (uc == null) {
            System.out.println(DesignUtils.GetRed() + "UC não encontrada." + DesignUtils.GetReset());
            MenuUtils.pressionarEnter(scanner);
            return;
        }

        System.out.print(DesignUtils.GetYellow() + "Tem a certeza que deseja eliminar a UC '" + nome + "'? (S/N): " + DesignUtils.GetReset());
        String confirmacao = scanner.nextLine().trim().toUpperCase();

        if (confirmacao.equals("S")) {
            if (ucCRUD.eliminarUC(nome)) {
                System.out.println(DesignUtils.GetGreen() + "UC eliminada com sucesso!" + DesignUtils.GetReset());
            } else {
                System.out.println(DesignUtils.GetRed() + "Erro: Não é permitida eliminação. UC tem docente alocado e estudantes inscritos." + DesignUtils.GetReset());
            }
        } else {
            System.out.println("Eliminação cancelada.");
        }

        MenuUtils.pressionarEnter(scanner);
    }

    private void listarDocentesDisponiveis() {
        System.out.println("\n--- Docentes Disponíveis ---");
        List<Docente> docentes = docenteCRUD.getDocentes();
        if (docentes.isEmpty()) {
            System.out.println("Nenhum docente registado.");
        } else {
            for (Docente d : docentes) {
                System.out.println(d.getSigla() + " - " + d.getNome());
            }
        }
    }

    private void imprimirDadosUnidadeCurricular(UnidadeCurricular uc) {
        System.out.println("\n" + DesignUtils.GetWhiteBold() + "--- Dados da Unidade Curricular ---" + DesignUtils.GetReset());
        System.out.println("Nome: " + uc.getNome());
        System.out.println("Ano Curricular: " + uc.getAnoCurricular());
        System.out.println("ECTS: " + uc.getEcts());
        if (uc.getDocente() != null) {
            System.out.println("Docente Responsável: " + uc.getDocente().getNome() + " (" + uc.getDocente().getSigla() + ")");
        } else {
            System.out.println("Docente Responsável: " + DesignUtils.GetYellow() + "NÃO ATRIBUÍDO" + DesignUtils.GetReset());
        }
        System.out.println(DesignUtils.GetWhiteBold() + "-----------------------------------" + DesignUtils.GetReset());
    }
}