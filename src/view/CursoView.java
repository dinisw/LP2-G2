package view;

import Common.DesignUtils;
import Common.MenuUtils;
import controller.CursoController;
import controller.DepartamentoController;
import model.Curso;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CursoView {
    private final CursoController cursoController;
    private final DepartamentoController departamentoController;
    private final Scanner scanner;

    public CursoView() {
        this.cursoController = new CursoController();
        this.departamentoController = new DepartamentoController();
        this.scanner = new Scanner(System.in);
    }

    public void exibirMenuCursos() {
        String opcao;
        ArrayList<String> opcoes = new ArrayList<>();
        opcoes.add("1. Registar Curso");
        opcoes.add("2. Listar Cursos");
        opcoes.add("3. Procurar Curso");
        opcoes.add("4. Atualizar Curso");
        opcoes.add("5. Eliminar Curso");
        opcoes.add("0. Voltar ao Menu de Gestão");

        do {
            MenuUtils.exibirSubTitulo("GESTÃO DE CURSOS", opcoes);
            System.out.print("\n" + DesignUtils.GetWhiteBold() + "Selecione uma opção: " + DesignUtils.GetReset());
            opcao = scanner.nextLine().trim();

            switch (opcao) {
                case "1": registarCurso(); break;
                case "2": listarCursos(); break;
                case "3": procurarCurso(); break;
                case "4": atualizarCurso(); break;
                case "5": eliminarCurso(); break;
                case "0": return;
                default:
                    System.out.println(DesignUtils.GetRed() + "Opção inválida!" + DesignUtils.GetReset());
                    MenuUtils.pressionarEnter(scanner);
            }
        } while (!opcao.equals("0"));
    }

    private void registarCurso() {
        System.out.println("\n--- REGISTO DE CURSO ---");
        System.out.print("Nome do Curso: ");
        String nome = scanner.nextLine().trim();
        departamentoController.listarDepartamentos();
        System.out.print("Sigla do Departamento Associado (ex: EI): ");
        String siglaDep = scanner.nextLine().trim().toUpperCase();

        int resultado = cursoController.registarCurso(nome, siglaDep);

        if (resultado == 1) {
            System.out.println(DesignUtils.GetGreen() + "Curso registado com sucesso!" + DesignUtils.GetReset());
        } else if (resultado == -1) {
            System.out.println(DesignUtils.GetRed() + "Erro: O Departamento com a sigla '" + siglaDep + "' não existe! Registe-o primeiro." + DesignUtils.GetReset());
        } else {
            System.out.println(DesignUtils.GetRed() + "Erro: Já existe um curso com esse nome no sistema." + DesignUtils.GetReset());
        }

        MenuUtils.pressionarEnter(scanner);
    }

    private void listarCursos() {
        System.out.println("\n--- LISTA DE CURSOS ---");
        List<Curso> lista = cursoController.listarCursos();
        if (lista.isEmpty()) {
            System.out.println("Nenhum curso registado.");
        } else {
            for (Curso c : lista) {
                // Usamos o toString() limpo que criámos no modelo Curso
                System.out.println(c.toString());
            }
        }
        MenuUtils.pressionarEnter(scanner);
    }

    private void procurarCurso() {
        listarCursos();
        System.out.print("\nDigite o nome do curso a procurar: ");
        String nome = scanner.nextLine().trim();

        Curso c = cursoController.procurarCurso(nome);
        if (c != null) {
            System.out.println("Encontrado: " + c.toString());
        } else {
            System.out.println(DesignUtils.GetRed() + "Curso não encontrado." + DesignUtils.GetReset());
        }
        MenuUtils.pressionarEnter(scanner);
    }

    private void atualizarCurso() {
        listarCursos();
        System.out.print("\nDigite o nome do curso a atualizar: ");
        String nomeAtual = scanner.nextLine().trim();

        Curso c = cursoController.procurarCurso(nomeAtual);

        if (c != null) {
            System.out.println("Dados atuais: " + c.getNome());
            System.out.print("Novo Nome (Enter para manter): ");
            String novoNome = scanner.nextLine().trim();

            if (cursoController.atualizarCurso(nomeAtual, novoNome)) {
                System.out.println(DesignUtils.GetGreen() + "Curso atualizado com sucesso!" + DesignUtils.GetReset());
            } else {
                System.out.println(DesignUtils.GetRed() + "Nenhuma alteração efetuada (ou curso tem alunos alocados)." + DesignUtils.GetReset());
            }
        } else {
            System.out.println(DesignUtils.GetRed() + "Curso não encontrado." + DesignUtils.GetReset());
        }
        MenuUtils.pressionarEnter(scanner);
    }

    private void eliminarCurso() {
        listarCursos();
        System.out.print("\nDigite o nome do curso a eliminar: ");
        String nome = scanner.nextLine().trim();

        if (cursoController.eliminarCurso(nome)) {
            System.out.println(DesignUtils.GetGreen() + "Curso eliminado com sucesso!" + DesignUtils.GetReset());
        } else {
            System.out.println(DesignUtils.GetRed() + "Erro ao eliminar: Curso não encontrado ou tem alunos alocados." + DesignUtils.GetReset());
        }
        MenuUtils.pressionarEnter(scanner);
    }
}