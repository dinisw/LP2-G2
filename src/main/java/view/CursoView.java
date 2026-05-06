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
        do {
            try {
                CursoController cursoController = new CursoController();
                boolean temCursos = !cursoController.listarCursos().isEmpty();

                ArrayList<String> opcoes = new ArrayList<>();
                opcoes.add("1. Registar Curso");
                if (temCursos) {
                    opcoes.add("2. Listar Cursos");
                    opcoes.add("3. Procurar Curso");
                    opcoes.add("4. Atualizar Curso");
                    opcoes.add("5. Eliminar Curso");
                    opcoes.add("6. Iniciar Curso (Bloqueio de Inscrições)");
                    opcoes.add("7. Associar UC a Curso");
                    opcoes.add("8. Listar UCs do Curso");
                    opcoes.add("9. Listar Alunos por Curso e Ano");
                }
                opcoes.add("0. Voltar ao Menu de Gestão");

                MenuUtils.limparTela();
                MenuUtils.exibirSubTitulo("PORTAL GESTOR > MENU PRINCIPAL > CURSOS", opcoes);
                System.out.print("\n" + GetWhiteBold() + "Selecione uma opção: " + GetReset());
                opcao = scanner.nextLine().trim();

                switch (opcao) {
                    case "1":
                        registarCurso();
                        break;
                    case "2":
                        if (temCursos) listarCursos();
                        else mostrarErroOpcao();
                        break;
                    case "3":
                        if (temCursos) procurarCurso();
                        else mostrarErroOpcao();
                        break;
                    case "4":
                        if (temCursos) atualizarCurso();
                        else mostrarErroOpcao();
                        break;
                    case "5":
                        if (temCursos) eliminarCurso();
                        else mostrarErroOpcao();
                        break;
                    case "6":
                        if (temCursos) iniciarAnoLetivoCurso();
                        else mostrarErroOpcao();
                        break;
                    case "7":
                        if (temCursos) associarUCAoCurso();
                        else mostrarErroOpcao();
                        break;
                    case "8":
                        if (temCursos) listarUCsDoCurso();
                        else mostrarErroOpcao();
                        break;
                    case "9":
                        if (temCursos) listarAlunosPorCursoEAno();
                        else mostrarErroOpcao();
                        break;
                    case "0":
                        System.out.println(GetYellow() + "\nA voltar..." + GetReset());
                        return;
                    default:
                        mostrarErroOpcao();
                }
            } catch (Exception e) {
                System.out.println("\n" + GetRed() + "Ocorreu um erro: " + e.getMessage() + GetReset());
                MenuUtils.pressionarEnter(scanner);
            }
        } while (true);
    }

    private void mostrarErroOpcao() {
        System.out.println(GetRed() + "Opção inválida! Por favor, escolha uma opção visível na lista." + GetReset());
        MenuUtils.pressionarEnter(scanner);
    }

    private void registarCurso() {
        try {
            System.out.println(GetBlue() + "\n--- REGISTO DE CURSO ---" + GetReset());
            DepartamentoController departamentoController = new DepartamentoController();
            List<Departamento> departamentos = departamentoController.listarTodosDepartamentos();
            if (departamentos.isEmpty()) {
                System.out.println(GetRed() + "Ação Bloqueada: Não existem departamentos. Registe um departamento primeiro." + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }

            System.out.println(GetYellow() + "[Digite '0' a qualquer momento para cancelar a operação!]" + GetReset());

            String nome = "";
            while (nome.isEmpty()) {
                nome = BackendUtils.lerInputString(scanner, "Nome do Curso: ");
                if (nome.isEmpty()) System.out.println(GetRed() + "O campo Nome do Curso não pode estar vazio." + GetReset());
            }

            int duracao = -1;
            while (duracao <= 0 || duracao > 10) {
                try {
                    String duracaoStr = BackendUtils.lerInputString(scanner, "Duração do Curso (em anos): ");
                    if (duracaoStr.equals("0")) throw new CancelarRegistoException("Operação cancelada pelo utilizador.");
                    duracao = Integer.parseInt(duracaoStr);
                    if (duracao > 10) System.out.println(GetRed() + "Erro: A duração máxima é de 10 anos." + GetReset());
                } catch (NumberFormatException e) {
                    System.out.println(GetRed() + "Formato inválido. Insira um número inteiro." + GetReset());
                }
            }

            System.out.println("\n" + GetBlue() + "--- Departamentos Disponíveis ---" + GetReset());
            for (int i = 0; i < departamentos.size(); i++) {
                System.out.println((i + 1) + ". " + departamentos.get(i).getNome());
            }

            Departamento departamentoSelecionado = null;
            while (departamentoSelecionado == null) {
                try {
                    String op = BackendUtils.lerInputString(scanner, "\nEscolha o ID do Departamento: ");
                    int escolha = Integer.parseInt(op);
                    if (escolha >= 1 && escolha <= departamentos.size()) {
                        departamentoSelecionado = departamentos.get(escolha - 1);
                    } else {
                        System.out.println(GetRed() + "ID inválido." + GetReset());
                    }
                } catch (NumberFormatException e) {
                    System.out.println(GetRed() + "Insira apenas números." + GetReset());
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

            if (resultado.sucesso) {
                System.out.println(GetGreen() + "\nCurso registado com sucesso!" + GetReset());
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
            String nomeFinal = novoNome.isEmpty() ? nomeAtual : novoNome;

            String precoNovoStr = BackendUtils.lerInputString(scanner, "Novo Preço Anual (deixe em branco para manter): ");
            double precoFinal = curso.getPrecoAnual();
            if (!precoNovoStr.isEmpty()) {
                try {
                    double preco = Double.parseDouble(precoNovoStr.replace(",", "."));
                    if(preco >= 0) precoFinal = preco;
                } catch (NumberFormatException e) {
                    System.out.println(GetRed() + "Formato inválido. Preço não alterado." + GetReset());
                }
            }

            Curso cursoAtualizado = new Curso(nomeFinal, curso.getDuracao(), curso.getDepartamento());
            cursoAtualizado.setPrecoAnual(precoFinal);

            cursoAtualizado.setAnosIniciados(curso.getAnosIniciados());
            for(UnidadeCurricular uc : curso.getUnidadeCurriculars()) {
                cursoAtualizado.adicionarUnidadeCurricular(uc);
            }

            Resultado resultado = cursoController.atualizarCurso(nomeAtual, cursoAtualizado);

            if (resultado.sucesso) {
                System.out.println(GetGreen() + "\nCurso atualizado com sucesso!" + GetReset());
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

                if (resultado.sucesso) {
                    System.out.println(GetGreen() + "\nCurso eliminado com sucesso!" + GetReset());
                } else {
                    System.out.println(GetRed() + "\nErro ao eliminar: " + resultado.mensagemErro + GetReset());
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

            // CORREÇÃO AQUI: .sucesso e .mensagemErro
            if (resultado.sucesso) {
                System.out.println(GetGreen() + "\nSucesso! O " + anoParaIniciar + "º ano do curso '" + curso.getNome() + "' foi iniciado." + GetReset());
            } else {
                System.out.println(GetRed() + "\nErro: " + resultado.mensagemErro + GetReset());
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

            boolean sucessoInput = false;
            while (!sucessoInput) {
                String opUc = BackendUtils.lerInputString(scanner, "\nDigite os números das UCs desejadas (separados por vírgula, ex: 1,3,5) ou 0 para cancelar: ");

                if (opUc.trim().equals("0")) throw new CancelarRegistoException("Cancelado.");

                String[] opcoes = opUc.split(",");
                System.out.println();

                for (String op : opcoes) {
                    try {
                        int escolhaUc = Integer.parseInt(op.trim());
                        if (escolhaUc >= 1 && escolhaUc <= unidadeCurriculars.size()) {
                            UnidadeCurricular ucSelecionada = unidadeCurriculars.get(escolhaUc - 1);
                            Resultado<Curso> resultado = cursoController.associarUCAoCurso(nomeAtual, ucSelecionada.getNome());

                            if (resultado.sucesso) {
                                System.out.println(GetGreen() + "- Sucesso: A UC '" + ucSelecionada.getNome() + "' foi associada ao curso!" + GetReset());
                            } else {
                                System.out.println(GetRed() + "- Erro ao associar '" + ucSelecionada.getNome() + "': " + resultado.mensagemErro + GetReset());
                            }
                        } else {
                            System.out.println(GetRed() + "- Aviso: A opção '" + escolhaUc + "' não existe na lista." + GetReset());
                        }
                    } catch (NumberFormatException e) {
                        System.out.println(GetRed() + "- Aviso: Formato inválido ('" + op.trim() + "'). Ignorado." + GetReset());
                    }
                }
                sucessoInput = true;
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

    private void listarUCsDoCurso() {
        try {
            System.out.println(GetBlue() + "\n--- LISTAR UCS DO CURSO ---" + GetReset());
            CursoController cursoController = new CursoController();
            List<Curso> listaCursos = cursoController.listarCursos();

            System.out.println("\n" + GetWhiteBold() + "Cursos Disponíveis:" + GetReset());
            for (int i = 0; i < listaCursos.size(); i++) {
                System.out.printf("%d - %s\n", i + 1, listaCursos.get(i).getNome());
            }

            int escolha = -1;
            while (escolha < 1 || escolha > listaCursos.size()) {
                try {
                    String op = BackendUtils.lerInputString(scanner, "\nDigite o ID do Curso (ou 0 para cancelar): ");
                    if (op.equals("0")) return;
                    escolha = Integer.parseInt(op);
                } catch (NumberFormatException e) {
                    System.out.println(GetRed() + "Por favor, digite apenas números." + GetReset());
                }
            }

            Curso curso = listaCursos.get(escolha - 1);
            List<UnidadeCurricular> ucs = curso.getUnidadeCurriculars();

            System.out.println("\n" + GetGreen() + "--- UCs do Curso: " + curso.getNome() + " ---" + GetReset());
            if (ucs.isEmpty()) {
                System.out.println(GetYellow() + "Nenhuma UC associada a este curso." + GetReset());
            } else {
                System.out.println(GetCyanBold() + "-------------------------------------------------------------------------" + GetReset());
                System.out.printf(GetWhiteBold() + " %-4s | %-25s | %-5s | %-3s | %-15s | %-4s \n" + GetReset(), "ID", "NOME DA UC", "ANO", "SEM", "DOCENTE", "ECTS");
                System.out.println(GetCyanBold() + "-------------------------------------------------------------------------" + GetReset());

                for (UnidadeCurricular uc : ucs) {
                    String docenteNome = (uc.getDocente() != null) ? uc.getDocente().getSigla() : GetYellow() + "N/A" + GetReset();
                    System.out.printf(" %-4d | %-25s | %-5d |%-3d | %-15s | %-4d \n",
                            uc.getId(), uc.getNome(), uc.getAnoCurricular(), uc.getSemestre(), docenteNome, uc.getEcts());
                }
                System.out.println(GetCyanBold() + "-------------------------------------------------------------------------" + GetReset());
            }

            MenuUtils.pressionarEnter(scanner);
        } catch (Exception e) {
            System.out.println(GetRed() + "Erro: " + e.getMessage() + GetReset());
            MenuUtils.pressionarEnter(scanner);
        }
    }

    private void listarAlunosPorCursoEAno() {
        try {
            System.out.println(GetBlue() + "\n--- LISTAR ALUNOS POR CURSO E ANO LETIVO ---" + GetReset());
            CursoController cursoController = new CursoController();
            List<Curso> listaCursos = cursoController.listarCursos();

            System.out.println("\n" + GetWhiteBold() + "Cursos Disponíveis:" + GetReset());
            for (int i = 0; i < listaCursos.size(); i++) {
                System.out.printf("%d - %s\n", i + 1, listaCursos.get(i).getNome());
            }

            int escolha = -1;
            while (escolha < 1 || escolha > listaCursos.size()) {
                try {
                    String op = BackendUtils.lerInputString(scanner, "\nDigite o ID do Curso (ou 0 para cancelar): ");
                    if (op.equals("0")) return;
                    escolha = Integer.parseInt(op);
                } catch (NumberFormatException e) {
                    System.out.println(GetRed() + "Apenas números." + GetReset());
                }
            }
            Curso curso = listaCursos.get(escolha - 1);

            int anoEscolhido = -1;
            while (anoEscolhido < 1 || anoEscolhido > curso.getDuracao()) {
                try {
                    String op = BackendUtils.lerInputString(scanner, "Qual o Ano Curricular que deseja listar? (1 a " + curso.getDuracao() + "): ");
                    if (op.equals("0")) return;
                    anoEscolhido = Integer.parseInt(op);
                } catch (NumberFormatException e) {
                    System.out.println(GetRed() + "Apenas números." + GetReset());
                }
            }

            controller.EstudanteController ec = new controller.EstudanteController();
            List<model.Estudante> todos = ec.listarEstudantes();
            List<model.Estudante> filtrados = new ArrayList<>();

            for (model.Estudante e : todos) {
                if (e.isAtivo() && e.getNomeCurso().equalsIgnoreCase(curso.getNome())) {
                    if (ec.obterAnoDesbloqueado(e) == anoEscolhido) {
                        filtrados.add(e);
                    }
                }
            }

            if (filtrados.isEmpty()) {
                System.out.println(GetYellow() + "\nNão existem alunos inscritos no " + anoEscolhido + "º ano de " + curso.getNome() + "." + GetReset());
            } else {
                System.out.println(GetGreen() + "\nAlunos no " + anoEscolhido + "º Ano de " + curso.getNome() + ":" + GetReset());
                for (model.Estudante e : filtrados) {
                    System.out.println("- Mec: " + e.getNumeroMec() + " | Nome: " + e.getNome());
                }
            }
            MenuUtils.pressionarEnter(scanner);

        } catch (Exception e) {
            System.out.println(GetRed() + "Erro: " + e.getMessage() + GetReset());
            MenuUtils.pressionarEnter(scanner);
        }
    }
}