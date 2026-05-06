package view;

import common.exceptions.CancelarRegistoException;
import common.utils.BackendUtils;
import common.utils.MenuUtils;
import controller.DepartamentoController;
import model.Departamento;
import model.Resultado;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.zip.DeflaterOutputStream;

import static common.utils.DesignUtils.*;

public class DepartamentoView {
    private final Scanner scanner;

    public DepartamentoView() {
        this.scanner = new Scanner(System.in);
    }

    public void exibirMenuDepartamentos() {
        String opcao;
        ArrayList<String> opcoes = new ArrayList<>();
        opcoes.add("1. Registar Departamento");
        opcoes.add("2. Listar Departamentos");
        opcoes.add("3. Atualizar Departamento");
        opcoes.add("4. Eliminar Departamento");
        opcoes.add("0. Voltar ao Menu de Gestão");

        do {
            try {
                MenuUtils.limparTela();
                MenuUtils.exibirSubTitulo("PORTAL GESTOR > MENU PRINCIPAL > DEPARTAMENTOS", opcoes);
                System.out.print("\n" + GetWhiteBold() + "Selecione uma opção: " + GetReset());
                opcao = scanner.nextLine().trim();

                switch (opcao) {
                    case "1": registarDepartamento(); break;
                    case "2": listarDepartamentos(); break;
                    case "3": atualizarDepartamento(); break;
                    case "4": eliminarDepartamento(); break;
                    case "0":
                        System.out.println(GetYellow() + "\nA voltar..." + GetReset());
                        return;
                    default:
                        mostrarErroOpcao();
                }
            } catch (Exception e) {
                System.out.println("\n" + GetRed() + "Ocorreu um erro na navegação: " + e.getMessage() + GetReset());
                MenuUtils.pressionarEnter(scanner);
            }
        } while (true);
    }

    private void mostrarErroOpcao() {
        System.out.println(GetRed() + "Opção inválida! Por favor, escolha uma opção visível na lista." + GetReset());
        MenuUtils.pressionarEnter(scanner);
    }

    private void registarDepartamento() {
        try {
            System.out.println(GetBlue() + "\n--- REGISTO DE DEPARTAMENTO ---" + GetReset());
            System.out.println(GetYellow() + "[Digite '0' a qualquer momento para cancelar a operação!]" + GetReset());

            String nome = "";
            while (nome.isEmpty()) {
                nome = BackendUtils.lerInputString(scanner, "Nome do Departamento: ");
                if (nome.isEmpty()) System.out.println(GetRed() + "O campo Nome não pode estar vazio." + GetReset());
            }

            String sigla = "";
            while (sigla.isEmpty()) {
                sigla = BackendUtils.lerInputString(scanner, "Sigla (ex: EI, MAT): ").toUpperCase();
                if (sigla.isEmpty()) System.out.println(GetRed() + "A Sigla não pode estar vazia." + GetReset());
            }

            DepartamentoController departamentoController = new DepartamentoController();
            Resultado <Departamento> resultado = departamentoController.registarDepartamento(nome, sigla);

            if (resultado.sucesso) {
                System.out.println(GetGreen() + "\nDepartamento registado com sucesso!" + GetReset());
            } else {
                System.out.println(GetRed() + "\nErro ao registar: " + resultado.mensagemErro + GetReset());
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

            DepartamentoController departamentoController = new DepartamentoController();
            List<Departamento> lista = departamentoController.listarTodosDepartamentos();

            if (lista.isEmpty()) {
                System.out.println(GetYellow() + "Nenhum departamento registado no sistema." + GetReset());
            } else {
                // Tabela bonita e informativa
                System.out.println(GetCyanBold() + "----------------------------------------------------------------------" + GetReset());
                System.out.printf(GetWhiteBold() + " %-5s | %-15s | %-30s \n" + GetReset(), "ID", "SIGLA", "NOME DO DEPARTAMENTO");
                System.out.println(GetCyanBold() + "----------------------------------------------------------------------" + GetReset());
                for (int i = 0; i < lista.size(); i++) {
                    Departamento departamento = lista.get(i);
                    System.out.printf(" %-5d | %-15s | %-30s \n", (i + 1), departamento.getSigla(), departamento.getNome());
                }
                System.out.println(GetCyanBold() + "----------------------------------------------------------------------" + GetReset());
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
            if (sigla.equals("0")) throw new CancelarRegistoException("Operação cancelada pelo utilizador.");

            DepartamentoController departamentoController = new DepartamentoController();
            Departamento departamento = departamentoController.procurarDepartamento(sigla);

            if (departamento != null) {
                System.out.println(GetGreen() + "\nDados encontrados:" + GetReset());
                System.out.println(departamento.getNome() + " (" + departamento.getSigla() + ")");
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

            DepartamentoController departamentoController = new DepartamentoController();
            List<Departamento> lista = departamentoController.listarTodosDepartamentos();

            System.out.println("\n" + GetWhiteBold() + "Departamentos Disponíveis:" + GetReset());
            for (int i = 0; i < lista.size(); i++) {
                System.out.printf("%d - %s (%s)\n", i + 1, lista.get(i).getNome(), lista.get(i).getSigla());
            }

            Departamento departamento = null;
            while (departamento == null) {
                try {
                    String input = BackendUtils.lerInputString(scanner, "\nEscolha o ID do departamento a atualizar: ");
                    int escolha = Integer.parseInt(input);

                    if (escolha >= 1 && escolha <= lista.size()) {
                        departamento = lista.get(escolha - 1);
                    } else {
                        System.out.println(GetRed() + "ID inválido. Escolha um número da lista." + GetReset());
                    }
                } catch (NumberFormatException e) {
                    System.out.println(GetRed() + "Por favor, digite apenas números." + GetReset());
                }
            }

            System.out.println(GetGreen() + "\nDados atuais:" + GetReset());
            System.out.println("Nome: " + departamento.getNome());

            System.out.println(GetYellow() + "\n[Pressione ENTER para manter o nome igual]" + GetReset());
            String nome = BackendUtils.lerInputString(scanner, "Novo Nome: ");

            String nomeFinal = nome.isEmpty() ? departamento.getNome() : nome;

            Resultado <Departamento> resultado = departamentoController.atualizarDepartamento(departamento.getSigla(), nomeFinal);

            if (resultado.sucesso) {
                System.out.println(GetGreen() + "\nDepartamento atualizado com sucesso!" + GetReset());
            } else {
                System.out.println(GetRed() + "\nErro ao atualizar: " + resultado.mensagemErro + GetReset());
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

            DepartamentoController departamentoController = new DepartamentoController();
            Departamento departamento = null;
            String sigla = "";

            while (departamento == null) {
                sigla = BackendUtils.lerInputString(scanner, "\nDigite a sigla do departamento a eliminar: ").toUpperCase();
                if (sigla.equals("0")) throw new CancelarRegistoException("Operação cancelada pelo utilizador.");

                departamento = departamentoController.procurarDepartamento(sigla);

                if (departamento == null) {
                    System.out.println(GetRed() + "Erro: Departamento não encontrado. Tente novamente." + GetReset());
                }
            }

            // Dupla confirmação
            System.out.println(GetYellow() + "\nTem a certeza que deseja eliminar o departamento " + departamento.getNome() + "? (s/n)" + GetReset());
            String confirmacao1 = scanner.nextLine().trim();
            if (!confirmacao1.equalsIgnoreCase("s")) {
                System.out.println(GetYellow() + "Operação cancelada." + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }

            System.out.println(GetRed() + "ESTA AÇÃO É IRREVERSÍVEL! Deseja mesmo continuar? (s/n)" + GetReset());
            String confirmacao2 = scanner.nextLine().trim();
            if (!confirmacao2.equalsIgnoreCase("s")) {
                System.out.println(GetYellow() + "Operação cancelada." + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }

            Resultado <Departamento> resultado = departamentoController.eliminarDepartamento(sigla);

            if (resultado.sucesso) {
                System.out.println(GetGreen() + "\nDepartamento eliminado com sucesso!" + GetReset());
            } else {
                System.out.println(GetRed() + "\nErro ao eliminar: " + resultado.mensagemErro + GetReset());
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