package view;

import Common.DesignUtils;
import Common.MenuUtils;
import DAL.CursoCRUD;
import model.Curso;
import model.Departamento;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CursoView {
    private final CursoCRUD cursoCRUD;
    private final Scanner scanner;

    public CursoView() {
        this.cursoCRUD = new CursoCRUD();
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
        System.out.print("Sigla do Departamento Associado: ");
        String siglaDep = scanner.nextLine().trim();

        // O CursoCRUD agora precisa do nome, duração (3) e o objeto Departamento (que ele próprio vai procurar)
        DAL.DepartamentoCRUD depCRUD = new DAL.DepartamentoCRUD();
        Departamento dep = depCRUD.procurarPorSigla(siglaDep);

        if (dep != null) {
            Curso novo = new Curso(nome, 3, dep);
            if (cursoCRUD.registarCurso(novo)) {
                System.out.println(DesignUtils.GetGreen() + "Curso registado com sucesso!" + DesignUtils.GetReset());
            } else {
                System.out.println(DesignUtils.GetRed() + "Erro: Já existe um curso com esse nome." + DesignUtils.GetReset());
            }
        } else {
            System.out.println(DesignUtils.GetRed() + "Erro: Departamento com a sigla '" + siglaDep + "' não existe! Registe o departamento primeiro." + DesignUtils.GetReset());
        }
        MenuUtils.pressionarEnter(scanner);
    }

    private void listarCursos() {
        System.out.println("\n--- LISTA DE CURSOS ---");
        List<Curso> lista = cursoCRUD.getCursos();
        if (lista.isEmpty()) {
            System.out.println("Nenhum curso registado.");
        } else {
            for (Curso c : lista) {
                String nomeDep = (c.getDepartamento() != null) ? c.getDepartamento().getNome() : "Sem departamento";
                System.out.println("Curso: " + c.getNome() + " | Duração: " + c.getDuracao() + " anos | Dep: " + nomeDep);
            }
        }
        MenuUtils.pressionarEnter(scanner);
    }

    private void procurarCurso() {
        System.out.print("\nDigite o nome do curso: ");
        String nome = scanner.nextLine().trim();
        Curso c = cursoCRUD.procurarPorNome(nome);
        if (c != null) {
            String nomeDep = (c.getDepartamento() != null) ? c.getDepartamento().getNome() : "Sem departamento";
            System.out.println("Curso: " + c.getNome() + " | Duração: " + c.getDuracao() + " anos | Dep: " + nomeDep);
        } else {
            System.out.println(DesignUtils.GetRed() + "Curso não encontrado." + DesignUtils.GetReset());
        }
        MenuUtils.pressionarEnter(scanner);
    }

    private void atualizarCurso() {
        System.out.print("\nDigite o nome do curso a atualizar: ");
        String nome = scanner.nextLine().trim();
        Curso c = cursoCRUD.procurarPorNome(nome);

        if (c != null) {
            System.out.print("Novo Nome (Enter para manter): ");
            String novoNome = scanner.nextLine().trim();
            if (novoNome.isEmpty()) novoNome = c.getNome();

            Curso cursoAtualizado = new Curso(novoNome, c.getDuracao(), c.getDepartamento());

            if (cursoCRUD.atualizarCurso(nome, cursoAtualizado)) {
                System.out.println(DesignUtils.GetGreen() + "Curso atualizado com sucesso!" + DesignUtils.GetReset());
            }
        } else {
            System.out.println(DesignUtils.GetRed() + "Curso não encontrado." + DesignUtils.GetReset());
        }
        MenuUtils.pressionarEnter(scanner);
    }

    private void eliminarCurso() {
        System.out.print("\nDigite o nome do curso a eliminar: ");
        String nome = scanner.nextLine().trim();
        if (cursoCRUD.eliminarCurso(nome)) {
            System.out.println(DesignUtils.GetGreen() + "Curso eliminado com sucesso!" + DesignUtils.GetReset());
        }
        MenuUtils.pressionarEnter(scanner);
    }
}