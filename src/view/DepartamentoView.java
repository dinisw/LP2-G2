package view;

import Common.DesignUtils;
import Common.MenuUtils;
import controller.DepartamentoController;
import model.Departamento;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class DepartamentoView {
    private final DepartamentoController departamentoController;
    private final Scanner scanner;

    public DepartamentoView() {
        this.departamentoController = new DepartamentoController();
        this.scanner = new Scanner(System.in);
    }

    public void exibirMenuDepartamentos() {
        String opcao;
        ArrayList<String> opcoes = new ArrayList<>();
        opcoes.add("1. Registar Departamento");
        opcoes.add("2. Listar Departamentos");
        opcoes.add("3. Procurar Departamento (Sigla)");
        opcoes.add("4. Atualizar Departamento");
        opcoes.add("5. Eliminar Departamento");
        opcoes.add("0. Voltar ao Menu de Gestão");

        do {
            MenuUtils.exibirSubTitulo("GESTÃO DE DEPARTAMENTOS", opcoes);
            System.out.print("\n" + DesignUtils.GetWhiteBold() + "Selecione uma opção: " + DesignUtils.GetReset());
            opcao = scanner.nextLine().trim();

            switch (opcao) {
                case "1": registarDepartamento(); break;
                case "2": listarDepartamentos(); break;
                case "3": procurarDepartamento(); break;
                case "4": atualizarDepartamento(); break;
                case "5": eliminarDepartamento(); break;
                case "0": return;
                default:
                    System.out.println(DesignUtils.GetRed() + "Opção inválida!" + DesignUtils.GetReset());
                    MenuUtils.pressionarEnter(scanner);
            }
        } while (true);
    }

    private void registarDepartamento() {
        System.out.println("\n--- REGISTO DE DEPARTAMENTO ---");
        System.out.print("Nome do Departamento: ");
        String nome = scanner.nextLine().trim();
        System.out.print("Sigla (ex: EI, MAT): ");
        String sigla = scanner.nextLine().trim().toUpperCase();

        if (departamentoController.registarDepartamento(nome, sigla)) {
            System.out.println(DesignUtils.GetGreen() + "Departamento registado com sucesso!" + DesignUtils.GetReset());
        } else {
            System.out.println(DesignUtils.GetRed() + "Erro: Já existe um departamento com essa sigla." + DesignUtils.GetReset());
        }
        MenuUtils.pressionarEnter(scanner);
    }

    private void listarDepartamentos() {
        System.out.println("\n--- LISTA DE DEPARTAMENTOS ---");
        List<Departamento> lista = departamentoController.listarDepartamentos();
        if (lista.isEmpty()) {
            System.out.println("Nenhum departamento registado.");
        } else {
            for (Departamento d : lista) {
                System.out.println("Sigla: " + d.getSigla() + " | Nome: " + d.getNome());
            }
        }
        MenuUtils.pressionarEnter(scanner);
    }

    private void procurarDepartamento() {
        System.out.println("\n--- PROCURAR DEPARTAMENTO ---");
        listarDepartamentos();
        System.out.print("\nDigite a sigla do departamento: ");
        String sigla = scanner.nextLine().trim();
        Departamento d = departamentoController.procurarDepartamento(sigla);
        if (d != null) {
            System.out.println("Encontrado: " + d.getNome() + " (" + d.getSigla() + ")");
        } else {
            System.out.println(DesignUtils.GetRed() + "Departamento não encontrado." + DesignUtils.GetReset());
        }
        MenuUtils.pressionarEnter(scanner);
    }

    private void atualizarDepartamento() {
        listarDepartamentos();
        System.out.print("\nDigite a sigla do departamento a atualizar: ");
        String sigla = scanner.nextLine().trim();
        Departamento d = departamentoController.procurarDepartamento(sigla);

        if (d != null) {
            System.out.println("Dados atuais: " + d.getNome());
            System.out.print("Novo Nome (Enter para manter): ");
            String nome = scanner.nextLine().trim();
            if (!nome.isEmpty()) d.setNome(nome);

            if (departamentoController.atualizarDepartamento(sigla, nome)) {
                System.out.println(DesignUtils.GetGreen() + "Departamento atualizado com sucesso!" + DesignUtils.GetReset());
            }
        } else {
            System.out.println(DesignUtils.GetRed() + "Departamento não encontrado." + DesignUtils.GetReset());
        }
        MenuUtils.pressionarEnter(scanner);
    }

    private void eliminarDepartamento() {
        listarDepartamentos();
        System.out.print("\nDigite a sigla do departamento a eliminar: ");
        String sigla = scanner.nextLine().trim();
        Departamento d = departamentoController.procurarDepartamento(sigla);
        if (d != null) {
            if (departamentoController.eliminarDepartamento(sigla)) {
                System.out.println(DesignUtils.GetGreen() + "Departamento eliminado com sucesso!" + DesignUtils.GetReset());
            } else {
                System.out.println(DesignUtils.GetRed() + "Erro ao eliminar: Departamento tem cursos associados." + DesignUtils.GetReset());
            }
        } else {
            System.out.println(DesignUtils.GetRed() + "Erro ao eliminar: Departamento não encontrado." + DesignUtils.GetReset());
        }
        MenuUtils.pressionarEnter(scanner);
    }
}