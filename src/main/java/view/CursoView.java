package view;

import common.exceptions.CancelarRegistoException;
import common.utils.BackendUtils;
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
    private final Scanner scanner;

    public CursoView() {
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
        opcoes.add("6. Iniciar Curso (Bloqueio de Inscrições)");
        opcoes.add("7. Associar UC a Curso");
        opcoes.add("0. Voltar ao Menu de Gestão");

        do {
            try {
                MenuUtils.exibirSubTitulo("PORTAL GESTOR > MENU PRINCIPAL > CURSOS", opcoes);
                System.out.print("\n" + GetWhiteBold() + "Selecione uma opção: " + GetReset());
                opcao = scanner.nextLine().trim();

                switch (opcao) {
                    case "1": registarCurso(); break;
                    case "2": listarCursos(); break;
                    case "3": procurarCurso(); break;
                    case "4": atualizarCurso(); break;
                    case "5": eliminarCurso(); break;
                    case "6": iniciarAnoLetivoCurso(); break;
                    case "7": associarUCAoCurso(); break;
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

    private void registarCurso() {
        try {
            System.out.println(GetBlue() + "\n--- REGISTO DE CURSO ---" + GetReset());
            System.out.println(GetYellow() + "[Digite '0' a qualquer momento para cancelar a operação!]" + GetReset());

            String nome = BackendUtils.lerInputString(scanner, "Nome do Curso: ");

            int duracao = -1;
            while (duracao <= 0) {
                try {
                    String duracaoStr = BackendUtils.lerInputString(scanner, "Duração do Curso (em anos): ");
                    if (duracaoStr.equals("0")) throw new CancelarRegistoException("Operação cancelada pelo utilizador.");
                    duracao = Integer.parseInt(duracaoStr);
                } catch (NumberFormatException e) {
                    System.out.println(GetRed() + "Formato inválido. Insira um número inteiro." + GetReset());
                }
            }

            System.out.println("\n" + GetBlue() + "--- Departamentos Disponíveis ---" + GetReset());
            DepartamentoController departamentoController = new DepartamentoController();
            List<Departamento> departamentos = departamentoController.listarTodosDepartamentos();

            if (departamentos.isEmpty()) {
                System.out.println(GetYellow() + "Nenhum departamento registado. Crie um departamento primeiro." + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }
            for (Departamento departamento : departamentos) {
                System.out.println(departamento.getSigla() + " - " + departamento.getNome());
            }

            String siglaDep = "";
            Departamento departamentoSelecionado = null;
            boolean depExiste = false;

            while (!depExiste) {
                siglaDep = BackendUtils.lerInputString(scanner, "\nSigla do Departamento Associado (ex: EI): ").toUpperCase();
                if (siglaDep.equals("0")) throw new CancelarRegistoException("Operação cancelada pelo utilizador.");

                for(Departamento departamento : departamentos) {
                    if(departamento.getSigla().equalsIgnoreCase(siglaDep)) {
                        departamentoSelecionado = departamento;
                        depExiste = true;
                        break;
                    }
                }

                if(!depExiste) {
                    System.out.println(GetRed() + "Erro: O departamento '" + siglaDep + "' não existe. Tente novamente." + GetReset());
                }
            }

            double preco = -1;
            while (preco < 0) {
                try {
                    String precoStr = BackendUtils.lerInputString(scanner, "Preço Anual do Curso (€): ");
                    if (precoStr.equals("0")) throw new CancelarRegistoException("Operação cancelada pelo utilizador.");
                    preco = Double.parseDouble(precoStr.replace(",", "."));
                    if (preco < 0) System.out.println(GetRed() + "O preço não pode ser negativo." + GetReset());
                } catch (NumberFormatException e) {
                    System.out.println(GetRed() + "Formato inválido. Insira um número (ex: 1000.50)." + GetReset());
                }
            }

            Curso novoCurso = new Curso(nome, duracao, departamentoSelecionado);
            novoCurso.setPrecoAnual(preco);

            System.out.println("\n" + GetBlue() + "--- Unidades Curriculares Disponíveis ---" + GetReset());
            UnidadeCurricularController ucController = new UnidadeCurricularController();
            List<UnidadeCurricular> unidadeCurriculars = ucController.listarTodasUCs();

            if (unidadeCurriculars.isEmpty()) {
                System.out.println(GetYellow() + "Nenhuma UC registada. O Curso será registado sem UCs associadas." + GetReset());
            } else {
                for (UnidadeCurricular uc : unidadeCurriculars) {
                    System.out.println("- " + uc.getNome());
                }
                String input = BackendUtils.lerInputString(scanner, "\nDigite os nomes das UCs a associar (separados por vírgula, ou Enter para nenhuma): ");
                if (!input.isEmpty()) {
                    String[] parts = input.split(",");
                    for (String part : parts) {
                        String nomeUC = part.trim();
                        UnidadeCurricular ucEncontrada = ucController.procurarUCPorNome(nomeUC);
                        if (ucEncontrada != null) {
                            novoCurso.adicionarUnidadeCurricular(ucEncontrada);
                        } else {
                            System.out.println(GetYellow() + "Aviso: A UC '" + nomeUC + "' não foi encontrada e será ignorada." + GetReset());
                        }
                    }
                }
            }

            CursoController cursoController = new CursoController();

            Resultado resultado = cursoController.registarCurso(novoCurso);

            if (resultado.success) {
                System.out.println(GetGreen() + "\nCurso registado com sucesso!" + GetReset());
            } else {
                System.out.println(GetRed() + "\nErro ao registar: " + resultado.errorMessage + GetReset());
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
            CursoController cursoController = new CursoController();
            List<Curso> cursos = cursoController.listarCursos();

            if (cursos.isEmpty()) {
                System.out.println(GetYellow() + "Nenhum curso registado até ao momento!" + GetReset());
            } else {
                int count = 1;
                for (Curso c : cursos) {
                    System.out.printf("%d - %s | Preço: %.2f€\n", count, c.toString(), c.getPrecoAnual());
                    count++;
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

            CursoController cursoController = new CursoController();
            List<Curso> listaCursos = cursoController.listarCursos();
            if (listaCursos.isEmpty()) {
                System.out.println(GetYellow() + "Não há cursos registados no sistema." + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }

            System.out.println("\n" + GetWhiteBold() + "Cursos Disponíveis:" + GetReset());
            for (int i = 0; i < listaCursos.size(); i++) {
                System.out.printf("%d - %s\n", i + 1, listaCursos.get(i).getNome());
            }

            int escolha = -1;
            while (escolha < 0 || escolha > listaCursos.size()) {
                try {
                    String op = BackendUtils.lerInputString(scanner, "\nDigite a opção desejada: ");
                    escolha = Integer.parseInt(op);

                    if (escolha == 0) {
                        throw new CancelarRegistoException("Operação cancelada pelo utilizador.");
                    }

                    if (escolha < 1 || escolha > listaCursos.size()) {
                        System.out.println(GetRed() + "Opção inválida. Escolha um número entre 1 e " + listaCursos.size() + "." + GetReset());
                    }
                } catch (NumberFormatException e) {
                    System.out.println(GetRed() + "Por favor, digite apenas números." + GetReset());
                    escolha = -1;
                }
            }

            Curso curso = listaCursos.get(escolha - 1);

            if (curso != null) {
                System.out.println(GetGreen() + "\nDados encontrados:" + GetReset());
                System.out.println(curso.toString());
                System.out.println("Preço Anual: " + String.format("%.2f€", curso.getPrecoAnual()));
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

            CursoController cursoController = new CursoController();
            List<Curso> listaCursos = cursoController.listarCursos();
            if (listaCursos.isEmpty()) {
                System.out.println(GetYellow() + "Não há cursos registados no sistema." + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }

            System.out.println("\n" + GetWhiteBold() + "Cursos Disponíveis:" + GetReset());
            for (int i = 0; i < listaCursos.size(); i++) {
                System.out.printf("%d - %s\n", i + 1, listaCursos.get(i).getNome());
            }

            int escolha = -1;
            while (escolha < 0 || escolha > listaCursos.size()) {
                try {
                    String op = BackendUtils.lerInputString(scanner, "\nDigite a opção desejada: ");
                    escolha = Integer.parseInt(op);

                    if (escolha == 0) {
                        throw new CancelarRegistoException("Operação cancelada pelo utilizador.");
                    }

                    if (escolha < 1 || escolha > listaCursos.size()) {
                        System.out.println(GetRed() + "Opção inválida. Escolha um número entre 1 e " + listaCursos.size() + "." + GetReset());
                    }
                } catch (NumberFormatException e) {
                    System.out.println(GetRed() + "Por favor, digite apenas números." + GetReset());
                    escolha = -1;
                }
            }

            Curso curso = listaCursos.get(escolha - 1);
            String nomeAtual = curso.getNome();

            System.out.println(GetGreen() + "\nDados atuais:" + GetReset());
            System.out.println(curso.toString());
            System.out.println("Preço Anual: " + String.format("%.2f€", curso.getPrecoAnual()));

            System.out.println(GetYellow() + "\n[Pressione ENTER nos campos que deseja manter iguais]" + GetReset());

            String novoNome = BackendUtils.lerInputString(scanner, "Novo Nome: ");
            if (!novoNome.isEmpty()) curso.setNome(novoNome);

            String precoNovoStr = BackendUtils.lerInputString(scanner, "Novo Preço Anual (deixe em branco para manter): ");
            if (!precoNovoStr.isEmpty()) {
                try {
                    double preco = Double.parseDouble(precoNovoStr.replace(",", "."));
                    if(preco >= 0) {
                        curso.setPrecoAnual(preco);
                    } else {
                        System.out.println(GetRed() + "Preço ignorado (não pode ser negativo)." + GetReset());
                    }
                } catch (NumberFormatException e) {
                    System.out.println(GetRed() + "Formato inválido. Preço não alterado." + GetReset());
                }
            }

            Resultado resultado = cursoController.atualizarCurso(nomeAtual, curso);

            if (resultado.success) {
                System.out.println(GetGreen() + "\nCurso atualizado com sucesso!" + GetReset());
            } else {
                System.out.println(GetRed() + "\nErro ao atualizar: " + resultado.errorMessage + GetReset());
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

            CursoController cursoController = new CursoController();
            List<Curso> listaCursos = cursoController.listarCursos();
            if (listaCursos.isEmpty()) {
                System.out.println(GetYellow() + "Não há cursos registados no sistema." + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }

            System.out.println("\n" + GetWhiteBold() + "Cursos Disponíveis:" + GetReset());
            for (int i = 0; i < listaCursos.size(); i++) {
                System.out.printf("%d - %s\n", i + 1, listaCursos.get(i).getNome());
            }

            int escolha = -1;
            while (escolha < 0 || escolha > listaCursos.size()) {
                try {
                    String op = BackendUtils.lerInputString(scanner, "\nDigite a opção desejada: ");
                    escolha = Integer.parseInt(op);

                    if (escolha == 0) {
                        throw new CancelarRegistoException("Operação cancelada pelo utilizador.");
                    }

                    if (escolha < 1 || escolha > listaCursos.size()) {
                        System.out.println(GetRed() + "Opção inválida. Escolha um número entre 1 e " + listaCursos.size() + "." + GetReset());
                    }
                } catch (NumberFormatException e) {
                    System.out.println(GetRed() + "Por favor, digite apenas números." + GetReset());
                    escolha = -1;
                }
            }

            Curso curso = listaCursos.get(escolha - 1);
            String nomeAtual = curso.getNome();

            // Dupla confirmação
            System.out.println(GetYellow() + "\nTem a certeza que deseja eliminar o curso " + nomeAtual + "? (s/n)" + GetReset());
            String confirmacao1 = scanner.nextLine().trim();
            if (!confirmacao1.equalsIgnoreCase("s")) {
                System.out.println(GetYellow() + "Operação cancelada." + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }

            System.out.println(GetRed() + "ESTA AÇÃO É IRREVERSÍVEL! Todos os dados associados podem ser perdidos. Deseja mesmo continuar? (s/n)" + GetReset());
            String confirmacao2 = scanner.nextLine().trim();
            if (confirmacao2.equalsIgnoreCase("s")) {
                Resultado resultado = cursoController.eliminarCurso(nomeAtual);

                if (resultado.success) {
                    System.out.println(GetGreen() + "\nCurso eliminado com sucesso!" + GetReset());
                } else {
                    System.out.println(GetRed() + "\nErro ao eliminar: " + resultado.errorMessage + GetReset());
                }
            } else {
                System.out.println(GetBlue() + "\nOperação cancelada pelo utilizador." + GetReset());
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

    private void iniciarAnoLetivoCurso() {
        try {
            System.out.println(GetBlue() + "\n--- INICIAR ANO LETIVO DO CURSO ---" + GetReset());
            System.out.println(GetYellow() + "[Digite '0' a qualquer momento para cancelar a operação!]" + GetReset());

            CursoController cursoController = new CursoController();
            List<Curso> listaCursos = cursoController.listarCursos();
            if (listaCursos.isEmpty()) {
                System.out.println(GetYellow() + "Não há cursos registados no sistema." + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }

            System.out.println("\n" + GetWhiteBold() + "Cursos Disponíveis:" + GetReset());
            for (int i = 0; i < listaCursos.size(); i++) {
                System.out.printf("%d - %s\n", i + 1, listaCursos.get(i).getNome());
            }

            int escolha = -1;
            while (escolha < 0 || escolha > listaCursos.size()) {
                try {
                    String op = BackendUtils.lerInputString(scanner, "\nDigite a opção desejada: ");
                    escolha = Integer.parseInt(op);

                    if (escolha == 0) {
                        throw new CancelarRegistoException("Operação cancelada pelo utilizador.");
                    }

                    if (escolha < 1 || escolha > listaCursos.size()) {
                        System.out.println(GetRed() + "Opção inválida. Escolha um número entre 1 e " + listaCursos.size() + "." + GetReset());
                    }
                } catch (NumberFormatException e) {
                    System.out.println(GetRed() + "Por favor, digite apenas números." + GetReset());
                    escolha = -1;
                }
            }

            Curso curso = listaCursos.get(escolha - 1);

            System.out.println("\nQual ano letivo deseja iniciar para o curso " + curso.getNome() + "?");
            System.out.println("1. Primeiro Ano (Exige 5 alunos)");
            System.out.println("2. Segundo Ano (Exige 1 aluno)");
            System.out.println("3. Terceiro Ano (Exige 1 aluno)");

            int anoParaIniciar = -1;
            while (anoParaIniciar < 1 || anoParaIniciar > 3) {
                try {
                    String anoParaIniciarString = BackendUtils.lerInputString(scanner, "Opção (1-3): ");
                    if (anoParaIniciarString.equals("0")) throw new CancelarRegistoException("Operação cancelada pelo utilizador.");
                    anoParaIniciar = Integer.parseInt(anoParaIniciarString);
                    if (anoParaIniciar < 1 || anoParaIniciar > 3) {
                        System.out.println(GetRed() + "Opção inválida. Escolha 1, 2 ou 3." + GetReset());
                    }
                } catch (NumberFormatException e) {
                    System.out.println(GetRed() + "Por favor, digite apenas números." + GetReset());
                }
            }

            Resultado resultado = cursoController.iniciarAnoLetivo(curso.getNome(), anoParaIniciar);

            if (resultado.success) {
                System.out.println(GetGreen() + "\nSucesso! O " + anoParaIniciar + "º ano do curso '" + curso.getNome() + "' foi iniciado." + GetReset());
            } else {
                System.out.println(GetRed() + "\nErro: " + resultado.errorMessage + GetReset());
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

    private void associarUCAoCurso() {
        try {
            System.out.println(GetBlue() + "\n--- ASSOCIAR UNIDADE CURRICULAR A UM CURSO ---" + GetReset());
            System.out.println(GetYellow() + "[Digite '0' a qualquer momento para cancelar a operação!]" + GetReset());

            CursoController cursoController = new CursoController();
            List<Curso> listaCursos = cursoController.listarCursos();
            if (listaCursos.isEmpty()) {
                System.out.println(GetYellow() + "Não há cursos registados no sistema." + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }

            System.out.println("\n" + GetWhiteBold() + "Cursos Disponíveis:" + GetReset());
            for (int i = 0; i < listaCursos.size(); i++) {
                System.out.printf("%d - %s\n", i + 1, listaCursos.get(i).getNome());
            }

            int escolha = -1;
            while (escolha < 0 || escolha > listaCursos.size()) {
                try {
                    String op = BackendUtils.lerInputString(scanner, "\nDigite a opção desejada: ");
                    escolha = Integer.parseInt(op);

                    if (escolha == 0) {
                        throw new CancelarRegistoException("Operação cancelada pelo utilizador.");
                    }

                    if (escolha < 1 || escolha > listaCursos.size()) {
                        System.out.println(GetRed() + "Opção inválida. Escolha um número entre 1 e " + listaCursos.size() + "." + GetReset());
                    }
                } catch (NumberFormatException e) {
                    System.out.println(GetRed() + "Por favor, digite apenas números." + GetReset());
                    escolha = -1;
                }
            }

            Curso curso = listaCursos.get(escolha - 1);
            String nomeAtual = curso.getNome();

            UnidadeCurricularController ucController = new UnidadeCurricularController();
            List<UnidadeCurricular> unidadeCurriculars = ucController.listarTodasUCs();

            if(unidadeCurriculars.isEmpty()){
                System.out.println(GetYellow() + "Não existem UCs registadas no sistema." + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }

            System.out.println("\n" + GetWhiteBold() + "UCs Disponíveis:" + GetReset());
            for (int i = 0; i < unidadeCurriculars.size(); i++) {
                UnidadeCurricular uc = unidadeCurriculars.get(i);
                System.out.printf("%d - %s (Ano: %d | Sem: %d)\n", i + 1, uc.getNome(), uc.getAnoCurricular(), uc.getSemestre());
            }

            int escolhaUc = -1;
            while (escolhaUc < 0 || escolhaUc > unidadeCurriculars.size()) {
                try {
                    String opUc = BackendUtils.lerInputString(scanner, "\nDigite a opção da UC desejada: ");
                    escolhaUc = Integer.parseInt(opUc);

                    if (escolhaUc == 0) {
                        throw new CancelarRegistoException("Operação cancelada pelo utilizador.");
                    }

                    if (escolhaUc < 1 || escolhaUc > unidadeCurriculars.size()) {
                        System.out.println(GetRed() + "Opção inválida. Escolha um número entre 1 e " + unidadeCurriculars.size() + "." + GetReset());
                    }
                } catch (NumberFormatException e) {
                    System.out.println(GetRed() + "Por favor, digite apenas números." + GetReset());
                    escolhaUc = -1;
                }
            }

            UnidadeCurricular ucSelecionada = unidadeCurriculars.get(escolhaUc - 1);
            String nomeUC = ucSelecionada.getNome();

            Resultado resultado = cursoController.associarUCAoCurso(nomeAtual, nomeUC);

            if (resultado.success) {
                System.out.println(GetGreen() + "\nAssociação guardada com sucesso! A UC foi ligada ao Curso." + GetReset());
            } else {
                System.out.println(GetRed() + "\nErro ao associar: " + resultado.errorMessage + GetReset());
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