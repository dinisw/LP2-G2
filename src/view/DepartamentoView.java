package view;

import common.exceptions.CancelarRegistoException;
import common.utils.BackendUtils;
import common.utils.DesignUtils;
import common.utils.MenuUtils;
import controller.DepartamentoController;
import model.Departamento;
import model.Resultado;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static common.utils.DesignUtils.*;

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
            try {
                MenuUtils.exibirSubTitulo("GESTÃO DE DEPARTAMENTOS", opcoes);
                System.out.print("\n" + GetWhiteBold() + "Selecione uma opção: " + GetReset());
                opcao = scanner.nextLine().trim();

                switch (opcao) {
                    case "1": registarDepartamento(); break;
                    case "2": listarDepartamentos(); break;
                    case "3": procurarDepartamento(); break;
                    case "4": atualizarDepartamento(); break;
                    case "5": eliminarDepartamento(); break;
                    case "0":
                        System.out.println(GetYellow() + "\nA voltar ao menu de gestão..." + GetReset());
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

    private void registarDepartamento() {
        try {
            System.out.println(GetBlue() + "\n--- REGISTO DE DEPARTAMENTO ---" + GetReset());
            System.out.println(GetYellow() + "[Digite '0' a qualquer momento para cancelar a operação!]" + GetReset());

            String nome = BackendUtils.lerInputString(scanner, "Nome do Departamento: ");
            String sigla = BackendUtils.lerInputString(scanner, "Sigla (ex: EI, MAT): ").toUpperCase();

            Resultado res = departamentoController.registarDepartamento(nome, sigla);

            if (res.success) {
                System.out.println(GetGreen() + "\nDepartamento registado com sucesso!" + GetReset());
            } else {
                System.out.println(GetRed() + "\nErro ao registar: " + res.errorMessage + GetReset());
            }

            MenuUtils.pressionarEnter(scanner);

        } catch (CancelarRegistoException e) {
            System.out.println("\n" + GetYellow() + "Aviso: " + e.getMessage() + GetReset());
            System.out.println(GetRed() + "Registo interrompido!" + GetReset());
            MenuUtils.pressionarEnter(scanner);
        } catch (Exception e) {
            System.out.println(GetRed() + "Ocorreu um erro inesperado: " + e.getMessage() + GetReset());
            MenuUtils.pressionarEnter(scanner);
        }
    }

    private void listarDepartamentos() {
        try {
            System.out.println(GetBlue() + "\n--- LISTA DE DEPARTAMENTOS ---" + GetReset());

            List<Departamento> lista = departamentoController.listarTodosDepartamentos();

            if (lista.isEmpty()) {
                System.out.println(GetYellow() + "Nenhum departamento registado no sistema." + GetReset());
            } else {
                for (Departamento d : lista) {
                    System.out.println("Sigla: " + d.getSigla() + " | Nome: " + d.getNome());
                }
            }

            MenuUtils.pressionarEnter(scanner);

        } catch (Exception e) {
            System.out.println(GetRed() + "Ocorreu um erro ao listar os departamentos: " + e.getMessage() + GetReset());
            MenuUtils.pressionarEnter(scanner);
        }
    }

    private void procurarDepartamento() {
        try {
            System.out.println(GetBlue() + "\n--- PROCURAR DEPARTAMENTO ---" + GetReset());
            System.out.println(GetYellow() + "[Digite '0' a qualquer momento para cancelar a operação!]" + GetReset());

            listarDepartamentos();

            String sigla = BackendUtils.lerInputString(scanner, "\nDigite a sigla do departamento: ").toUpperCase();

            Departamento d = departamentoController.procurarDepartamento(sigla);

            if (d != null) {
                System.out.println(GetGreen() + "\nDados encontrados:" + GetReset());
                System.out.println(d.getNome() + " (" + d.getSigla() + ")");
            } else {
                System.out.println(GetYellow() + "\nDepartamento não encontrado com a sigla informada." + GetReset());
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

    private void atualizarDepartamento() {
        try {
            System.out.println(GetBlue() + "\n--- ATUALIZAR DEPARTAMENTO ---" + GetReset());
            System.out.println(GetYellow() + "[Digite '0' a qualquer momento para cancelar a operação!]" + GetReset());

            listarDepartamentos();

            String sigla = BackendUtils.lerInputString(scanner, "\nDigite a sigla do departamento a atualizar: ").toUpperCase();

            Departamento d = departamentoController.procurarDepartamento(sigla);

            if (d != null) {
                System.out.println(GetGreen() + "\nDados atuais:" + GetReset());
                System.out.println("Nome: " + d.getNome());

                System.out.println(GetYellow() + "\n[Pressione ENTER para manter o nome igual]" + GetReset());
                String nome = BackendUtils.lerInputString(scanner, "Novo Nome: ");

                String nomeFinal = nome.isEmpty() ? d.getNome() : nome;

                Resultado res = departamentoController.atualizarDepartamento(sigla, nomeFinal);

                if (res.success) {
                    System.out.println(GetGreen() + "\nDepartamento atualizado com sucesso!" + GetReset());
                } else {
                    System.out.println(GetRed() + "\nErro ao atualizar: " + res.errorMessage + GetReset());
                }
            } else {
                System.out.println(GetYellow() + "\nDepartamento não encontrado com a sigla informada." + GetReset());
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

    private void eliminarDepartamento() {
        try {
            System.out.println(GetBlue() + "\n--- ELIMINAR DEPARTAMENTO ---" + GetReset());
            System.out.println(GetYellow() + "[Digite '0' a qualquer momento para cancelar a operação!]" + GetReset());

            listarDepartamentos();

            String sigla = BackendUtils.lerInputString(scanner, "\nDigite a sigla do departamento a eliminar: ").toUpperCase();

            Resultado res = departamentoController.eliminarDepartamento(sigla);

            if (res.success) {
                System.out.println(GetGreen() + "\nDepartamento eliminado com sucesso!" + GetReset());
            } else {
                System.out.println(GetRed() + "\nErro ao eliminar: " + res.errorMessage + GetReset());
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
}