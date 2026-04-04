package view;

import common.exceptions.CancelarRegistoException;
import common.utils.BackendUtils;
import common.utils.DesignUtils;
import common.utils.MenuUtils;
import controller.CursoController;
import controller.DepartamentoController;
import controller.UnidadeCurricularController;
import model.Curso;
import model.Departamento;
import model.Resultado;
import model.UnidadeCurricular;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static common.utils.DesignUtils.*;

public class CursoView {
    private final CursoController cursoController;
    private final DepartamentoController departamentoController;
    private final UnidadeCurricularController ucController;
    private final Scanner scanner;

    public CursoView() {
        this.cursoController = new CursoController();
        this.departamentoController = new DepartamentoController();
        this.ucController = new UnidadeCurricularController();
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
            try {
                MenuUtils.exibirSubTitulo("GESTÃO DE CURSOS", opcoes);
                System.out.print("\n" + GetWhiteBold() + "Selecione uma opção: " + GetReset());
                opcao = scanner.nextLine().trim();

                switch (opcao) {
                    case "1": registarCurso(); break;
                    case "2": listarCursos(); break;
                    case "3": procurarCurso(); break;
                    case "4": atualizarCurso(); break;
                    case "5": eliminarCurso(); break;
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
        } while (true); // Trocado para true, pois o return já resolve a saída
    }

    private void registarCurso() {
        try {
            System.out.println(GetBlue() + "\n--- REGISTO DE CURSO ---" + GetReset());
            System.out.println(GetYellow() + "[Digite '0' a qualquer momento para cancelar a operação!]" + GetReset());

            String nome = BackendUtils.lerInputString(scanner, "Nome do Curso: ");

            System.out.println("\n" + GetBlue() + "--- Departamentos Disponíveis ---" + GetReset());
            List<Departamento> deps = departamentoController.listarTodosDepartamentos();
            if (deps.isEmpty()) {
                System.out.println(GetYellow() + "Nenhum departamento registado. Crie um departamento primeiro." + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }
            for (Departamento d : deps) {
                System.out.println(d.getSigla() + " - " + d.getNome());
            }

            String siglaDep = BackendUtils.lerInputString(scanner, "\nSigla do Departamento Associado (ex: EI): ").toUpperCase();

            System.out.println("\n" + GetBlue() + "--- Unidades Curriculares Disponíveis ---" + GetReset());
            List<UnidadeCurricular> ucs = ucController.listarTodasUCs();
            List<String> nomesUC = new ArrayList<>();

            if (ucs.isEmpty()) {
                System.out.println(GetYellow() + "Nenhuma UC registada. O Curso será registado sem UCs associadas." + GetReset());
            } else {
                for (UnidadeCurricular uc : ucs) {
                    System.out.println(uc.getNome());
                }
                String input = BackendUtils.lerInputString(scanner, "\nDigite os nomes das UCs a associar (separados por vírgula, ou Enter para nenhuma): ");
                if (!input.isEmpty()) {
                    String[] parts = input.split(",");
                    for (String part : parts) {
                        nomesUC.add(part.trim());
                    }
                }
            }

            Resultado res = cursoController.registarCurso(nome, siglaDep, nomesUC);

            if (res.success) {
                System.out.println(GetGreen() + "\nCurso registado com sucesso!" + GetReset());

                String avisos = (String) res.object;
                if (avisos != null && !avisos.isEmpty()) {
                    System.out.println(GetYellow() + "Notas: " + avisos + GetReset());
                }
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

    private void listarCursos() {
        try {
            System.out.println(GetBlue() + "\n--- LISTA DE CURSOS ---" + GetReset());
            List<Curso> cursos = cursoController.listarCursos();

            if (cursos.isEmpty()) {
                System.out.println(GetYellow() + "Nenhum curso registado até ao momento!" + GetReset());
            } else {
                for (Curso c : cursos) {
                    // Idealmente o toString() do curso deve estar formatado verticalmente
                    System.out.println(c.toString());
                }
            }
            MenuUtils.pressionarEnter(scanner);
        } catch (Exception e) {
            System.out.println(GetRed() + "Ocorreu um erro ao listar os cursos: " + e.getMessage() + GetReset());
            MenuUtils.pressionarEnter(scanner);
        }
    }

    private void procurarCurso() {
        try {
            System.out.println(GetBlue() + "\n--- PROCURAR CURSO ---" + GetReset());
            System.out.println(GetYellow() + "[Digite '0' a qualquer momento para cancelar a operação!]" + GetReset());

            String nome = BackendUtils.lerInputString(scanner, "Digite o nome do curso a procurar: ");

            Curso c = cursoController.procurarCurso(nome);
            if (c != null) {
                System.out.println(GetGreen() + "\nDados encontrados:" + GetReset());
                System.out.println(c.toString());
            } else {
                System.out.println(GetYellow() + "\nCurso não encontrado." + GetReset());
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

    private void atualizarCurso() {
        try {
            System.out.println(GetBlue() + "\n--- ATUALIZAR CURSO ---" + GetReset());
            System.out.println(GetYellow() + "[Digite '0' a qualquer momento para cancelar a operação!]" + GetReset());

            String nomeAtual = BackendUtils.lerInputString(scanner, "Digite o nome do curso a atualizar: ");

            Curso c = cursoController.procurarCurso(nomeAtual);

            if (c != null) {
                System.out.println(GetGreen() + "\nDados atuais:" + GetReset());
                System.out.println(c.toString());

                System.out.println(GetYellow() + "\n[Pressione ENTER nos campos que deseja manter iguais]" + GetReset());

                String novoNome = BackendUtils.lerInputString(scanner, "Novo Nome: ");

                // Se devolver Resultado, fica igual ao registarCurso
                Resultado res = cursoController.atualizarCurso(nomeAtual, novoNome.isEmpty() ? nomeAtual : novoNome);

                if (res.success) {
                    System.out.println(GetGreen() + "\nCurso atualizado com sucesso!" + GetReset());
                } else {
                    System.out.println(GetRed() + "\nErro ao atualizar: " + res.errorMessage + GetReset());
                }
            } else {
                System.out.println(GetYellow() + "\nCurso não encontrado com o nome informado." + GetReset());
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

    private void eliminarCurso() {
        try {
            System.out.println(GetBlue() + "\n--- ELIMINAR CURSO ---" + GetReset());
            System.out.println(GetYellow() + "[Digite '0' a qualquer momento para cancelar a operação!]" + GetReset());

            String nome = BackendUtils.lerInputString(scanner, "Digite o nome do curso a eliminar: ");

            Resultado res = cursoController.eliminarCurso(nome);

            if (res.success) {
                System.out.println(GetGreen() + "\nCurso eliminado com sucesso!" + GetReset());
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