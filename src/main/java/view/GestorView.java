package view;

import common.exceptions.CancelarRegistoException;
import common.utils.BackendUtils;
import common.utils.DesignUtils;
import common.utils.MenuUtils;
import common.utils.SenhaUtils;
import controller.*;

import java.util.LinkedHashMap;
import java.util.Map;
import model.*;
import service.EmailService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import static common.utils.DesignUtils.*;

public class GestorView {
    private final UnidadeCurricularView unidadeCurricularView;
    private final DepartamentoView departamentoView;
    private final CursoView cursoView;
    private final Scanner scanner;

    public GestorView() {
        this.unidadeCurricularView = new UnidadeCurricularView();
        this.departamentoView = new DepartamentoView();
        this.cursoView = new CursoView();
        this.scanner = new Scanner(System.in);
    }

    public void exibirMenuGestao(Gestor gestorLog) {
        do {
            try {
                MenuUtils.limparTela();

                DepartamentoController depCtrl  = new DepartamentoController();
                CursoController        cursoCtrl = new CursoController();
                DocenteController      docCtrl   = new DocenteController();
                EstudanteController    estCtrl   = new EstudanteController();

                boolean temDepartamentos = !depCtrl.listarTodosDepartamentos().isEmpty();
                boolean temCursos        = temDepartamentos && !cursoCtrl.listarCursos().isEmpty();
                boolean temDocentes      = !docCtrl.listarDocentes().isEmpty();
                boolean temEstudantes    = !estCtrl.listarEstudantes().isEmpty();

                ArrayList<String>      opcoes = new ArrayList<>();
                Map<Integer, Runnable> acoes  = new LinkedHashMap<>();
                int n = 1;

                opcoes.add(n + ". Gerir Gestores");
                final int nGestores = n; acoes.put(n++, () -> exibirMenuGestores(gestorLog));

                opcoes.add(n + ". Gerir Docentes");
                acoes.put(n++, () -> exibirMenuDocentes());

                // Sempre disponível (base da hierarquia)
                opcoes.add(n + ". Gerir Departamentos");
                acoes.put(n++, () -> departamentoView.exibirMenuDepartamentos());

                // Requer Departamentos
                if (temDepartamentos) {
                    opcoes.add(n + ". Gerir Cursos");
                    acoes.put(n++, () -> cursoView.exibirMenuCursos());
                }

                // Requer Cursos
                if (temCursos) {
                    opcoes.add(n + ". Gerir Unidades Curriculares");
                    acoes.put(n++, () -> unidadeCurricularView.exibirMenuUnidadesCurriculares());

                    opcoes.add(n + ". Gerir Estudantes");
                    acoes.put(n++, () -> exibirMenuEstudantes());
                }

                // Requer Estudantes
                if (temEstudantes) {
                    opcoes.add(n + ". Consultar Alunos em Dívida (Tesouraria)");
                    acoes.put(n++, () -> consultarAlunosEmDivida());
                }

                // Requer Cursos
                if (temCursos) {
                    opcoes.add(n + ". Gerir Ano Letivo (Global)");
                    acoes.put(n++, () -> exibirMenuAnoLetivo());
                }

                // v1.3 — Requer Cursos e Docentes
                if (temCursos && temDocentes) {
                    opcoes.add(n + ". Gerir Horários (v1.3)");
                    acoes.put(n++, () -> exibirMenuHorarios());
                }

                // v1.3 — Requer Estudantes
                if (temEstudantes) {
                    opcoes.add(n + ". Gerir Justificações de Falta (v1.3)");
                    acoes.put(n++, () -> exibirMenuJustificacoes());

                    opcoes.add(n + ". Gerir Estatutos de Estudante (v1.3)");
                    acoes.put(n++, () -> exibirMenuEstatutos());
                }

                opcoes.add("0. Logout");

                MenuUtils.exibirSubTitulo("PORTAL GESTOR > MENU PRINCIPAL", opcoes);

                // Dica sobre o que está bloqueado
                if (!temDepartamentos) {
                    System.out.println(GetYellow() + "  ℹ  Crie pelo menos um Departamento para desbloquear: Cursos, UCs, Estudantes, Ano Letivo." + GetReset());
                } else if (!temCursos) {
                    System.out.println(GetYellow() + "  ℹ  Crie pelo menos um Curso para desbloquear: UCs, Estudantes, Ano Letivo." + GetReset());
                } else if (!temEstudantes) {
                    System.out.println(GetYellow() + "  ℹ  Registe Estudantes para desbloquear: Tesouraria." + GetReset());
                }

                System.out.print("\nSelecione uma opção: ");
                String opcao = scanner.nextLine().trim();

                if (opcao.equals("0")) {
                    System.out.println(GetYellow() + "\nA terminar sessão..." + GetReset());
                    return;
                }

                try {
                    int numOpcao = Integer.parseInt(opcao);
                    Runnable acao = acoes.get(numOpcao);
                    if (acao != null) {
                        acao.run();
                    } else {
                        System.out.println(GetRed() + "Opção inválida! Por favor, escolha uma opção da lista." + GetReset());
                        MenuUtils.pressionarEnter(scanner);
                    }
                } catch (NumberFormatException e) {
                    System.out.println(GetRed() + "Opção inválida! Por favor, escolha uma opção da lista." + GetReset());
                    MenuUtils.pressionarEnter(scanner);
                }
            } catch (Exception e) {
                System.out.println("\n" + GetRed() + "Ocorreu um erro na navegação: " + e.getMessage() + GetReset());
                MenuUtils.pressionarEnter(scanner);
            }
        } while (true);
    }

    //region Gestor
    public void exibirMenuGestores(Gestor gestorLog) {
        String opcao;
        ArrayList<String> opcoes = new ArrayList<>();
        opcoes.add("1. Registar Gestores");
        opcoes.add("2. Listar Gestores");
        opcoes.add("3. Procurar Gestores");
        opcoes.add("4. Atualizar Gestores");
        opcoes.add("5. Eliminar Gestores");
        opcoes.add("6. Alterar a minha Password");
        opcoes.add("7. Ativar / Desativar Gestor");
        opcoes.add("0. Voltar ao Menu Principal");

        do {
            try {
                GestorController gc = new GestorController();
                Gestor gestor = gc.procurarGestorPorNif(gestorLog.getNif());
                MenuUtils.limparTela();
                MenuUtils.exibirSubTitulo("PORTAL GESTOR > MENU PRINCIPAL", opcoes);

                System.out.print("\nSelecione uma opção: ");
                opcao = scanner.nextLine().trim();

                switch (opcao) {
                    case "1":
                        registarGestor();
                        break;
                    case "2":
                        listarGestores();
                        break;
                    case "3":
                        procurarGestor();
                        break;
                    case "4":
                        atualizarGestor();
                        break;
                    case "5":
                        eliminarGestor();
                        break;
                    case "6":
                        alterarPassword(gestor);
                        break;
                    case "7":
                        ativarDesativarGestor();
                        break;
                    case "0":
                        System.out.println(GetYellow() + "\nA voltar ao menu principal..." + GetReset());
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

    private void registarGestor() {
        try {
            System.out.println(GetBlue() + "\n--- REGISTO DE GESTOR ---" + GetReset());
            System.out.println(GetYellow() + "[Digite '0' a qualquer momento para cancelar a operação!]" + GetReset());

            String nome = "";
            while (true) {
                nome = BackendUtils.lerInputString(scanner, "Nome: ");
                if (BackendUtils.isNomeValido(nome))
                    break;
                System.out.println(GetRed() + "Nome inválido. Não use números ou símbolos. Tente novamente." + GetReset());
            }

            String morada = BackendUtils.lerInputString(scanner, "Morada: ");
            while (morada.isEmpty()) {
                morada = BackendUtils.lerInputString(scanner, "Morada: ");
                if (morada.isEmpty())
                    System.out.println(GetRed() + "O campo Morada não pode estar vazio. Tente novamente." + GetReset());
            }

            int nif = 0;
            boolean nifValido = false;
            while (!nifValido) {
                try {
                    String nifString = BackendUtils.lerInputString(scanner, "NIF: ");
                    nif = Integer.parseInt(nifString);
                    nifValido = BackendUtils.nifEValido(nifString);
                    if (!nifValido) {
                        System.out.println(GetRed() + "NIF deve ser um número inteiro válido e conter 9 dígitos. Tente novamente." + GetReset());
                    } else {
                        var nifExiste = BackendUtils.nifExiste(nif);
                        if (nifExiste) {
                            System.out.println(GetRed() + "NIF já existente no sistema. Tente com outro NIF." + GetReset());
                            nifValido = false;
                        }
                    }
                } catch (NumberFormatException e) {
                    System.out.println(GetRed() + "Aviso: O valor introduzido não é um número. Tente novamente." + GetReset());
                }
            }

            LocalDate dataNascimento = null;
            while (true) {
                try {
                    String dataString = BackendUtils.lerInputString(scanner, "Data de Nascimento (AAAA-MM-DD): ");
                    dataNascimento = LocalDate.parse(dataString);
                    if (Period.between(dataNascimento, LocalDate.now()).getYears() >= 18) break;
                    System.out.println(GetRed() + "O sistema só permite pessoas maiores de 18 anos. Tente novamente." + GetReset());
                } catch (DateTimeParseException e) {
                    System.out.println(GetRed() + "Data deve estar no formato AAAA-MM-DD. Tente novamente." + GetReset());
                }
            }

            String email = "";
            boolean emailValido = false;
            while (!emailValido) {
                System.out.print("Email: ");
                email = scanner.nextLine().toLowerCase().trim();
                if (email.equals("0")) throw new CancelarRegistoException("Operação cancelada pelo utilizador.");
                emailValido = BackendUtils.emailISSMFGestorValido(email);
                if (!emailValido)
                    System.out.println(GetRed() + "EMAIL deve ter o formato xxxx.gestor@issmf.ipp.pt. Tente novamente." + GetReset());
            }

            String passDigitada = BackendUtils.lerSenhaComConfirmacao(
                    "Senha: ", "Confirmar Senha: ", scanner);

            SenhaUtils su = new SenhaUtils();
            String hash = su.gerarHashComSalt(passDigitada);

            String cargo = "";
            while (cargo.isEmpty()) {
                cargo = BackendUtils.lerInputString(scanner, "Cargo: ");
                if (cargo.isEmpty())
                    System.out.println(GetRed() + "O campo Cargo não pode estar vazio. Tente novamente." + GetReset());
            }

            GestorController gestorControllerAtualizado = new GestorController();
            Resultado<Gestor> resultado = gestorControllerAtualizado.registarGestor(nome, morada, nif, dataNascimento, email, hash, cargo);

            if (resultado.sucesso) {
                System.out.println(GetGreen() + "\nGestor registado com sucesso!" + GetReset());
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

    private void listarGestores() {
        try {
            System.out.println(GetBlue() + "\n--- LISTA DE GESTORES ---" + GetReset());

            GestorController gestorControllerAtualizado = new GestorController();
            List<Gestor> gestores = gestorControllerAtualizado.listarGestores();

            if (gestores.isEmpty()) {
                System.out.println(GetYellow() + "Nenhum gestor registado no sistema." + GetReset());
            } else {
                for (int i = 0; i < gestores.size(); i++) {
                    Gestor gestor = gestores.get(i);
                    System.out.println("ID: " + (i + 1) + " | NIF: " + gestor.getNif() + " | Nome: " + gestor.getNome() + " | Cargo: " + gestor.getCargo());
                }
            }

            MenuUtils.pressionarEnter(scanner);

        } catch (Exception e) {
            System.out.println(GetRed() + "Ocorreu um erro inesperado ao listar os gestores: " + e.getMessage() + GetReset());
            MenuUtils.pressionarEnter(scanner);
        }
    }

    private void procurarGestor() {
        try {
            System.out.println(GetBlue() + "\n--- PROCURAR GESTOR ---" + GetReset());
            System.out.println(GetYellow() + "[Digite '0' a qualquer momento para cancelar a operação!]" + GetReset());

            GestorController controllerAux = new GestorController();
            List<Gestor> listaGestores = controllerAux.listarGestores();
            if (listaGestores.isEmpty()) {
                System.out.println(GetYellow() + "Não há gestores registados no sistema." + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }

            System.out.println("\n" + GetWhiteBold() + "Gestores Disponíveis:" + GetReset());
            for (int i = 0; i < listaGestores.size(); i++) {
                Gestor g = listaGestores.get(i);
                System.out.printf("%d - %s (NIF: %d)\n", i + 1, g.getNome(), g.getNif());
            }

            int escolha = -1;
            while (escolha < 0 || escolha > listaGestores.size()) {
                try {
                    String op = BackendUtils.lerInputString(scanner, "\nDigite a opção desejada: ");
                    escolha = Integer.parseInt(op);

                    if (escolha == 0) throw new CancelarRegistoException("Operação cancelada pelo utilizador.");

                    if (escolha < 1 || escolha > listaGestores.size()) {
                        System.out.println(GetRed() + "Opção inválida. Escolha um número entre 1 e " + listaGestores.size() + "." + GetReset());
                    }
                } catch (NumberFormatException e) {
                    System.out.println(GetRed() + "Por favor, digite apenas números." + GetReset());
                    escolha = -1;
                }
            }

            Gestor gestor = listaGestores.get(escolha - 1);

            System.out.println(GetGreen() + "\nDados encontrados:" + GetReset());
            System.out.println(gestor.toString());
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

    private void atualizarGestor() {
        try {
            System.out.println(GetBlue() + "\n--- ATUALIZAR GESTOR ---" + GetReset());
            System.out.println(GetYellow() + "[Digite '0' a qualquer momento para cancelar a operação!]" + GetReset());
            GestorController gestorControllerAtualizado = new GestorController();
            List<Gestor> listaGestores = gestorControllerAtualizado.listarGestores();

            System.out.println("\n" + GetWhiteBold() + "Gestores Disponíveis:" + GetReset());
            for (int i = 0; i < listaGestores.size(); i++) {
                System.out.printf("%d - %s (NIF: %d)\n", i + 1, listaGestores.get(i).getNome(), listaGestores.get(i).getNif());
            }

            int escolha = -1;
            while (escolha < 1 || escolha > listaGestores.size()) {
                try {
                    String op = BackendUtils.lerInputString(scanner, "\nDigite a opção do gestor a atualizar: ");
                    escolha = Integer.parseInt(op);
                    if (escolha < 1 || escolha > listaGestores.size()) {
                        System.out.println(GetRed() + "Opção inválida. Escolha um número entre 1 e " + listaGestores.size() + "." + GetReset());
                    }
                } catch (NumberFormatException e) {
                    System.out.println(GetRed() + "Por favor, digite apenas números." + GetReset());
                    escolha = -1;
                }
            }

            Gestor gestor = listaGestores.get(escolha - 1);
            System.out.println(GetGreen() + "\nDados atuais:" + GetReset());
            System.out.println(gestor.toString());
            System.out.println(GetYellow() + "\n[Pressione ENTER nos campos que deseja manter iguais]" + GetReset());

            String novoNome = "";
            while (true) {
                novoNome = BackendUtils.lerInputString(scanner, "Novo Nome: ");
                if (novoNome.isEmpty()) break;
                if (BackendUtils.isNomeValido(novoNome)) break;
                System.out.println(GetRed() + "Nome inválido (não use números). Tente novamente." + GetReset());
            }
            if (!novoNome.isEmpty()) gestor.setNome(novoNome);

            String morada = BackendUtils.lerInputString(scanner, "Nova Morada: ");
            if (!morada.isEmpty()) gestor.setMorada(morada);

            String cargo = BackendUtils.lerInputString(scanner, "Novo Cargo: ");
            if (!cargo.isEmpty()) gestor.setCargo(cargo);

            Resultado<Gestor> resultado = gestorControllerAtualizado.atualizarGestor(gestor.getNif(), morada.isEmpty() ? null : morada, null, cargo.isEmpty() ? null : cargo);

            if (resultado.sucesso) {
                System.out.println(GetGreen() + "\nGestor atualizado com sucesso!" + GetReset());
            } else {
                System.out.println(GetRed() + "\nErro ao atualizar gestor: " + resultado.mensagemErro + GetReset());
            }

            MenuUtils.pressionarEnter(scanner);

        } catch (CancelarRegistoException e) {
            System.out.println("\n" + GetYellow() + "Aviso: " + e.getMessage() + GetReset());
            System.out.println(GetRed() + "Operação de atualização interrompida!" + GetReset());
            MenuUtils.pressionarEnter(scanner);
        } catch (Exception e) {
            System.out.println(GetRed() + "Ocorreu um erro inesperado: " + e.getMessage() + GetReset());
            MenuUtils.pressionarEnter(scanner);
        }
    }

    private void eliminarGestor() {
        try {
            System.out.println(GetBlue() + "\n--- ELIMINAR GESTOR ---" + GetReset());
            System.out.println(GetYellow() + "[Digite '0' a qualquer momento para cancelar a operação!]" + GetReset());

            GestorController gestorControllerAtualizado = new GestorController();
            List<Gestor> listaGestores = gestorControllerAtualizado.listarGestores();
            if (listaGestores.isEmpty()) {
                System.out.println(GetYellow() + "Não há gestores registados no sistema." + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }

            System.out.println("\n" + GetWhiteBold() + "Gestores Disponíveis:" + GetReset());
            for (int i = 0; i < listaGestores.size(); i++) {
                System.out.printf("%d - %s (NIF: %d)\n", i + 1, listaGestores.get(i).getNome(), listaGestores.get(i).getNif());
            }

            int escolha = -1;
            while (escolha < 0 || escolha > listaGestores.size()) {
                try {
                    String op = BackendUtils.lerInputString(scanner, "\nDigite a opção do gestor a eliminar: ");
                    escolha = Integer.parseInt(op);
                    if (escolha == 0) throw new CancelarRegistoException("Operação cancelada pelo utilizador.");
                    if (escolha < 1 || escolha > listaGestores.size()) {
                        System.out.println(GetRed() + "Opção inválida. Escolha um número entre 1 e " + listaGestores.size() + "." + GetReset());
                    }
                } catch (NumberFormatException e) {
                    System.out.println(GetRed() + "Por favor, digite apenas números." + GetReset());
                    escolha = -1;
                }
            }

            Gestor gestor = listaGestores.get(escolha - 1);

            System.out.println(GetYellow() + "\nTem a certeza que deseja eliminar o gestor " + gestor.getNome() + "? (s/n)" + GetReset());
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

            Resultado<Gestor> resultado = gestorControllerAtualizado.eliminarGestor(gestor.getNif());

            if (resultado.sucesso) {
                System.out.println(GetGreen() + "\nGestor eliminado com sucesso!" + GetReset());
            } else {
                System.out.println(GetRed() + "\nErro ao eliminar: " + resultado.mensagemErro + GetReset());
            }

            MenuUtils.pressionarEnter(scanner);

        } catch (CancelarRegistoException e) {
            System.out.println("\n" + GetYellow() + "Aviso: " + e.getMessage() + GetReset());
            System.out.println(GetRed() + "Operação de eliminação interrompida!" + GetReset());
            MenuUtils.pressionarEnter(scanner);
        } catch (Exception e) {
            System.out.println(GetRed() + "Ocorreu um erro inesperado: " + e.getMessage() + GetReset());
            MenuUtils.pressionarEnter(scanner);
        }
    }

    private void alterarPassword(Gestor gestor) {
        try {
            System.out.println(GetBlue() + "\n--- ALTERAR A PASSWORD DO GESTOR---" + GetReset());
            System.out.println(GetYellow() + "[Digite '0' a qualquer momento para cancelar a operação!]" + GetReset());


            String novaPass = BackendUtils.lerSenhaComConfirmacao(
                    "Nova senha: ", "Confirmar Nova senha: ", scanner);

            SenhaUtils su = new SenhaUtils();
            String passHash = su.gerarHashComSalt(novaPass);

            GestorController gestorControllerAtualizado = new GestorController();
            Resultado<Gestor> resultado = gestorControllerAtualizado.alterarPassword(gestor.getNif(), passHash);

            if (resultado.sucesso) {
                System.out.println(GetGreen() + "\nPassword alterada com sucesso!" + GetReset());
            } else {
                System.out.println(GetRed() + "\nErro ao guardar alteração da password: " + resultado.mensagemErro + GetReset());
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
    //endregion

    //region Docente
    private void exibirMenuDocentes() {
        String opcao;
        do {
            try {
                DocenteController docenteController = new DocenteController();
                boolean temDocentes = !docenteController.listarDocentes().isEmpty();

                ArrayList<String> opcoes = new ArrayList<>();
                opcoes.add("1. Registar Docente");
                if (temDocentes) {
                    opcoes.add("2. Listar Docentes");
                    opcoes.add("3. Procurar Docente");
                    opcoes.add("4. Atualizar Docente");
                    opcoes.add("5. Alterar Password");
                    opcoes.add("6. Eliminar Docente");
                    opcoes.add("7. Ativar / Desativar Docente");
                }
                opcoes.add("0. Voltar ao Menu Principal");

                MenuUtils.limparTela();
                MenuUtils.exibirSubTitulo("PORTAL GESTOR > MENU PRINCIPAL > DOCENTES", opcoes);
                System.out.print("\n" + GetWhiteBold() + "Selecione uma opção: " + GetReset());
                opcao = scanner.nextLine().trim();

                switch (opcao) {
                    case "1":
                        registarDocente();
                        break;
                    case "2":
                        if (temDocentes) listarDocentes();
                        else mostrarErroMenu();
                        break;
                    case "3":
                        if (temDocentes) procurarDocente();
                        else mostrarErroMenu();
                        break;
                    case "4":
                        if (temDocentes) atualizarDocente();
                        else mostrarErroMenu();
                        break;
                    case "5":
                        if (temDocentes) alterarPasswordDocente();
                        else mostrarErroMenu();
                        break;
                    case "6":
                        if (temDocentes) eliminarDocente();
                        else mostrarErroMenu();
                        break;
                    case "7":
                        if (temDocentes) ativarDesativarDocente();
                        else mostrarErroMenu();
                        break;
                    case "0":
                        System.out.println(GetYellow() + "\nA voltar ao menu principal..." + GetReset());
                        return;
                    default:
                        mostrarErroMenu();
                }
            } catch (Exception e) {
                System.out.println("\n" + GetRed() + "Ocorreu um erro na navegação: " + e.getMessage() + GetReset());
                MenuUtils.pressionarEnter(scanner);
            }
        } while (true);
    }

    private void registarDocente() {
        try {
            System.out.println(GetBlue() + "\n--- REGISTO DE DOCENTE ---" + GetReset());
            System.out.println(GetYellow() + "[Digite '0' a qualquer momento para cancelar!]" + GetReset());
            DocenteController docenteControllerAtualizado = new DocenteController();

            String nome = "";
            while (true) {
                nome = BackendUtils.lerInputString(scanner, "Nome: ");
                if (BackendUtils.isNomeValido(nome) && nome.trim().split("\\s+").length >= 2) break;
                System.out.println(GetRed() + "Deve introduzir nome e sobrenome válidos (sem números)." + GetReset());
            }

            String morada = "";
            while (morada.isEmpty()) {
                morada = BackendUtils.lerInputString(scanner, "Morada: ");
                if (morada.isEmpty())
                    System.out.println(GetRed() + "O campo Morada não pode estar vazio. Tente novamente." + GetReset());
            }

            int nif = 0;
            boolean nifValido = false;
            while (!nifValido) {
                try {
                    String nifString = BackendUtils.lerInputString(scanner, "NIF: ");
                    nif = Integer.parseInt(nifString);
                    nifValido = BackendUtils.nifEValido(nifString);
                    if (!nifValido) {
                        System.out.println(GetRed() + "NIF deve ser um número inteiro válido e conter 9 dígitos. Tente novamente." + GetReset());
                    } else {
                        var nifExiste = BackendUtils.nifExiste(nif);
                        if (nifExiste) {
                            System.out.println(GetRed() + "NIF já existente no sistema. Tente com outro NIF." + GetReset());
                            nifValido = false;
                        }
                    }
                } catch (NumberFormatException e) {
                    System.out.println(GetRed() + "Aviso: O valor introduzido não é um número. Tente novamente." + GetReset());
                }
            }

            LocalDate dataNascimento = null;
            while (true) {
                try {
                    String dataString = BackendUtils.lerInputString(scanner, "Data de Nascimento (AAAA-MM-DD): ");
                    dataNascimento = LocalDate.parse(dataString);
                    int idade = Period.between(dataNascimento, LocalDate.now()).getYears();
                    if (idade >= 18) {
                        break;
                    } else {
                        System.out.println(GetRed() + "Sistema só permite pessoas maiores de 18 anos. Tente novamente." + GetReset());
                    }
                } catch (DateTimeParseException e) {
                    System.out.println(GetRed() + "Data deve estar no formato AAAA-MM-DD. Tente novamente." + GetReset());
                }
            }

            System.out.println(GetYellow() + "\nA gerar credenciais e a tentar enviar o email..." + GetReset());
            String passDigitada = common.utils.SenhaUtils.gerarPalavraPasseAleatoria();
            common.utils.SenhaUtils su = new common.utils.SenhaUtils();
            String hash = su.gerarHashComSalt(passDigitada);
            String[] partesNome = nome.trim().split("\\s+");
            String primeiroNome = partesNome[0];
            String ultimoNome = partesNome[partesNome.length - 1];

            String siglaBase;
            if (ultimoNome.length() >= 2) {
                siglaBase = (primeiroNome.substring(0, 1) + ultimoNome.substring(0, 2)).toUpperCase();
            } else {
                siglaBase = (primeiroNome.substring(0, 1) + ultimoNome).toUpperCase();
            }

            String siglaFinal = siglaBase;

            if (docenteControllerAtualizado.procurarDocentePorSigla(siglaFinal) != null) {
                String prefixo = siglaBase.substring(0, siglaBase.length() - 1);
                List<Character> letras = new ArrayList<>();
                for (char c = 'A'; c <= 'Z'; c++) letras.add(c);
                Collections.shuffle(letras);
                for (char letra : letras) {
                    siglaFinal = prefixo + letra;
                    if (docenteControllerAtualizado.procurarDocentePorSigla(siglaFinal) == null) break;
                }
            }
            String email = siglaFinal.toLowerCase() + "@issmf.ipp.pt";

            EmailService es = new EmailService();
            String corpoEmail = "-- Credenciais Geradas Automaticamente --\n" +
                    "Sigla: " + siglaFinal + "\n" +
                    "Email: " + email + "\n" +
                    "Palavra-passe: " + passDigitada;

            var resEmail = es.enviarEmailRegisto(email, corpoEmail, TipoDeUtilizador.DOCENTE);

            if (resEmail.sucesso) {
                System.out.println(GetGreen() + "Email com as credenciais de acesso enviado com sucesso!" + GetReset());
            } else {
                System.out.println(GetRed() + "Falha ao enviar email: " + resEmail.mensagemErro + GetReset());
                System.out.println(GetRed() + "O registo foi abortado para evitar inconsistência de credenciais." + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }

            System.out.println("\n" + GetBlue() + "--- Unidades Curriculares Disponíveis ---" + GetReset());
            UnidadeCurricularController ucCtrl = new UnidadeCurricularController();
            List<UnidadeCurricular> todasUCs = ucCtrl.listarTodasUCs();
            List<String> nomesUC = new ArrayList<>();

            if (todasUCs.isEmpty()) {
                System.out.println(GetYellow() + "Nenhuma UC registada. Docente será registado sem UCs associadas." + GetReset());
            } else {
                // Mostrar tabela numerada
                System.out.println(GetCyanBold() + "  Nº  | Nome                          | Ano | Docente actual" + GetReset());
                System.out.println(GetCyanBold() + "------+-------------------------------+-----+--------------------" + GetReset());
                for (int i = 0; i < todasUCs.size(); i++) {
                    UnidadeCurricular uc = todasUCs.get(i);
                    String docenteAtual = (uc.getDocente() != null)
                            ? uc.getDocente().getSigla()
                            : "—";
                    System.out.printf("  %-3d | %-29s | %-3d | %s%n",
                            i + 1, uc.getNome(), uc.getAnoCurricular(), docenteAtual);
                }
                System.out.println();

                System.out.println(GetYellow() + "[Digite os IDs separados por espaço, ex: 1 3 5 | ENTER para nenhuma]" + GetReset());
                System.out.print(GetWhiteBold() + "IDs das UCs a associar: " + GetReset());
                String input = scanner.nextLine().trim();

                if (!input.isEmpty()) {
                    String[] partes = input.split("[\\s,]+");
                    for (String parte : partes) {
                        try {
                            int idx = Integer.parseInt(parte.trim());
                            if (idx < 1 || idx > todasUCs.size()) {
                                System.out.println(GetRed() + "  ID " + idx + " inválido — ignorado." + GetReset());
                                continue;
                            }
                            UnidadeCurricular ucEscolhida = todasUCs.get(idx - 1);
                            if (ucEscolhida.getDocente() != null) {
                                String resp = BackendUtils.lerInputString(scanner,
                                        GetYellow() + "  A UC '" + ucEscolhida.getNome() + "' já tem o docente "
                                                + ucEscolhida.getDocente().getSigla()
                                                + " atribuído. Substituir? (S/N): " + GetReset());
                                if (resp.equalsIgnoreCase("S")) {
                                    nomesUC.add(ucEscolhida.getNome());
                                    System.out.println(GetGreen() + "  ✓ " + ucEscolhida.getNome() + " adicionada (substituição)." + GetReset());
                                } else {
                                    System.out.println(GetYellow() + "  Ignorada: " + ucEscolhida.getNome() + GetReset());
                                }
                            } else {
                                nomesUC.add(ucEscolhida.getNome());
                                System.out.println(GetGreen() + "  ✓ " + ucEscolhida.getNome() + " adicionada." + GetReset());
                            }
                        } catch (NumberFormatException e) {
                            System.out.println(GetRed() + "  '" + parte + "' não é um número válido — ignorado." + GetReset());
                        }
                    }
                }
            }

            Resultado<Docente> res = docenteControllerAtualizado.registarDocente(nome, morada, nif, dataNascimento, email, hash, siglaFinal, nomesUC);

            if (res.sucesso) {
                System.out.println(GetGreen() + "\nDocente registado com sucesso!" + GetReset());

                String avisos = (String) res.mensagemErro;
                if (avisos != null && !avisos.isEmpty()) {
                    System.out.println(GetYellow() + "Notas: " + avisos + GetReset());
                }
            } else {
                System.out.println(GetRed() + "\nErro ao registar: " + res.mensagemErro + GetReset());
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

    private void listarDocentes() {
        try {
            System.out.println(GetBlue() + "\n--- LISTA DE DOCENTES ---" + GetReset());

            DocenteController docenteControllerAtualizado = new DocenteController();
            List<Docente> docentes = docenteControllerAtualizado.listarDocentes();

            if (docentes.isEmpty()) {
                System.out.println(GetYellow() + "Nenhum docente registado." + GetReset());
            } else {
                for (int i = 0; i < docentes.size(); i++) {
                    Docente docente = docentes.get(i);
                    String estadoDocente = docente.isAtivo() ? GetGreen() + "[ATIVO]" + GetReset() : GetRed() + "[INATIVO]" + GetReset();
                    System.out.println("ID: " + (i + 1) + " | NIF: " + docente.getNif() + " | Nome: " + docente.getNome() + " | Sigla: " + docente.getSigla() + " | " + estadoDocente);
                }
            }
            MenuUtils.pressionarEnter(scanner);
        } catch (Exception e) {
            System.out.println(GetRed() + "Ocorreu um erro inesperado ao listar os docentes: " + e.getMessage() + GetReset());
            MenuUtils.pressionarEnter(scanner);
        }
    }

    private void procurarDocente() {
        try {
            System.out.println(GetBlue() + "\n--- PROCURAR DOCENTE ---" + GetReset());
            System.out.println(GetYellow() + "[Digite '0' a qualquer momento para cancelar a operação!]" + GetReset());

            DocenteController docenteControllerAtualizado = new DocenteController();
            List<Docente> listaDocentes = docenteControllerAtualizado.listarDocentes();
            if (listaDocentes.isEmpty()) {
                System.out.println(GetYellow() + "Não há docentes registados no sistema." + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }

            System.out.println("\n" + GetWhiteBold() + "Docentes Disponíveis:" + GetReset());
            for (int i = 0; i < listaDocentes.size(); i++) {
                Docente d = listaDocentes.get(i);
                System.out.printf("%d - %s (Sigla: %s)\n", i + 1, d.getNome(), d.getSigla());
            }

            int escolha = -1;
            while (escolha < 0 || escolha > listaDocentes.size()) {
                try {
                    String op = BackendUtils.lerInputString(scanner, "\nDigite o ID do docente: ");
                    escolha = Integer.parseInt(op);
                    if (escolha == 0) throw new CancelarRegistoException("Operação cancelada pelo utilizador.");
                    if (escolha < 1 || escolha > listaDocentes.size()) {
                        System.out.println(GetRed() + "Opção inválida. Escolha um número entre 1 e " + listaDocentes.size() + "." + GetReset());
                    }
                } catch (NumberFormatException e) {
                    System.out.println(GetRed() + "Por favor, digite apenas números." + GetReset());
                    escolha = -1;
                }
            }

            Docente docente = listaDocentes.get(escolha - 1);
            System.out.println(GetGreen() + "\nDados encontrados:" + GetReset());
            System.out.println(docente.toString());
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

    private void atualizarDocente() {
        try {
            System.out.println(GetBlue() + "\n--- ATUALIZAR DOCENTE ---" + GetReset());
            System.out.println(GetYellow() + "[Digite '0' a qualquer momento para cancelar a operação!]" + GetReset());

            DocenteController docenteControllerAtualizado = new DocenteController();
            List<Docente> listaDocentes = docenteControllerAtualizado.listarDocentes();
            if (listaDocentes.isEmpty()) {
                System.out.println(GetYellow() + "Não há docentes registados no sistema." + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }

            System.out.println("\n" + GetWhiteBold() + "Docentes Disponíveis:" + GetReset());
            for (int i = 0; i < listaDocentes.size(); i++) {
                Docente d = listaDocentes.get(i);
                System.out.printf("%d - %s (Sigla: %s)\n", i + 1, d.getNome(), d.getSigla());
            }

            int escolha = -1;
            while (escolha < 0 || escolha > listaDocentes.size()) {
                try {
                    String op = BackendUtils.lerInputString(scanner, "\nDigite o ID do docente a atualizar: ");
                    escolha = Integer.parseInt(op);
                    if (escolha == 0) throw new CancelarRegistoException("Operação cancelada pelo utilizador.");
                    if (escolha < 1 || escolha > listaDocentes.size()) {
                        System.out.println(GetRed() + "Opção inválida. Escolha um número entre 1 e " + listaDocentes.size() + "." + GetReset());
                    }
                } catch (NumberFormatException e) {
                    System.out.println(GetRed() + "Por favor, digite apenas números." + GetReset());
                    escolha = -1;
                }
            }

            Docente docente = listaDocentes.get(escolha - 1);

            System.out.println(GetGreen() + "\nDados atuais:" + GetReset());
            System.out.println(docente.toString());
            System.out.println(GetYellow() + "\n[Pressione ENTER nos campos que deseja manter iguais]" + GetReset());

            String nomeFinal = docente.getNome();
            while (true) {
                String nome = BackendUtils.lerInputString(scanner, "Novo Nome: ");
                if (nome.isEmpty()) {
                    break;
                }
                if (nome.trim().split("\\s+").length >= 2) {
                    nomeFinal = nome;
                    break;
                }
                System.out.println(GetRed() + "Deve introduzir pelo menos nome e sobrenome. Tente novamente ou prima ENTER para manter o atual." + GetReset());
            }

            String moradaFinal = docente.getMorada();
            String morada = BackendUtils.lerInputString(scanner, "Nova Morada: ");
            if (!morada.isEmpty()) moradaFinal = morada;

            Resultado<Docente> resultado = docenteControllerAtualizado.atualizarDocente(docente.getNif(), nomeFinal, moradaFinal, null);

            if (resultado.sucesso) {
                System.out.println(GetGreen() + "\nDocente atualizado com sucesso!" + GetReset());
            } else {
                System.out.println(GetRed() + "\nErro ao atualizar docente: " + resultado.mensagemErro + GetReset());
            }
            MenuUtils.pressionarEnter(scanner);

        } catch (CancelarRegistoException e) {
            System.out.println("\n" + GetYellow() + "Aviso: " + e.getMessage() + GetReset());
            System.out.println(GetRed() + "Operação de atualização interrompida!" + GetReset());
            MenuUtils.pressionarEnter(scanner);
        } catch (Exception e) {
            System.out.println(GetRed() + "Ocorreu um erro inesperado: " + e.getMessage() + GetReset());
            MenuUtils.pressionarEnter(scanner);
        }
    }

    private void alterarPasswordDocente() {
        try {
            System.out.println(GetBlue() + "\n--- ALTERAR PASSWORD DO DOCENTE ---" + GetReset());
            System.out.println(GetYellow() + "[Digite '0' a qualquer momento para cancelar a operação!]" + GetReset());

            DocenteController docenteControllerAtualizado = new DocenteController();
            List<Docente> listaDocentes = docenteControllerAtualizado.listarDocentes();
            if (listaDocentes.isEmpty()) {
                System.out.println(GetYellow() + "Não há docentes registados no sistema." + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }

            System.out.println("\n" + GetWhiteBold() + "Docentes Disponíveis:" + GetReset());
            for (int i = 0; i < listaDocentes.size(); i++) {
                Docente d = listaDocentes.get(i);
                System.out.printf("%d - %s (Sigla: %s)\n", i + 1, d.getNome(), d.getSigla());
            }

            int escolha = -1;
            while (escolha < 0 || escolha > listaDocentes.size()) {
                try {
                    String op = BackendUtils.lerInputString(scanner, "\nDigite o ID do docente: ");
                    escolha = Integer.parseInt(op);
                    if (escolha == 0) throw new CancelarRegistoException("Operação cancelada pelo utilizador.");
                    if (escolha < 1 || escolha > listaDocentes.size()) {
                        System.out.println(GetRed() + "Opção inválida. Escolha um número entre 1 e " + listaDocentes.size() + "." + GetReset());
                    }
                } catch (NumberFormatException e) {
                    System.out.println(GetRed() + "Por favor, digite apenas números." + GetReset());
                    escolha = -1;
                }
            }

            Docente docente = listaDocentes.get(escolha - 1);

            String novaPass = BackendUtils.lerSenhaComConfirmacao(
                    "Nova senha: ", "Confirmar Nova senha: ", scanner);

            SenhaUtils su = new SenhaUtils();
            String novoHash = su.gerarHashComSalt(novaPass);

            Resultado<Docente> resultado = docenteControllerAtualizado.alterarPassword(docente.getNif(), novoHash);

            if (resultado.sucesso) {
                System.out.println(GetGreen() + "\nPassword alterada com sucesso!" + GetReset());
            } else {
                System.out.println(GetRed() + "\nErro ao alterar password: " + resultado.mensagemErro + GetReset());
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

    private void eliminarDocente() {
        try {
            System.out.println(GetBlue() + "\n--- ELIMINAR DOCENTE ---" + GetReset());
            System.out.println(GetYellow() + "[Digite '0' a qualquer momento para cancelar a operação!]" + GetReset());

            DocenteController docenteControllerAtualizado = new DocenteController();
            List<Docente> listaDocentes = docenteControllerAtualizado.listarDocentes();
            if (listaDocentes.isEmpty()) {
                System.out.println(GetYellow() + "Não há docentes registados no sistema." + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }

            System.out.println("\n" + GetWhiteBold() + "Docentes Disponíveis:" + GetReset());
            for (int i = 0; i < listaDocentes.size(); i++) {
                Docente d = listaDocentes.get(i);
                System.out.printf("%d - %s (Sigla: %s)\n", i + 1, d.getNome(), d.getSigla());
            }

            int escolha = -1;
            while (escolha < 0 || escolha > listaDocentes.size()) {
                try {
                    String op = BackendUtils.lerInputString(scanner, "\nDigite o ID do docente a eliminar: ");
                    escolha = Integer.parseInt(op);
                    if (escolha == 0) throw new CancelarRegistoException("Operação cancelada pelo utilizador.");
                    if (escolha < 1 || escolha > listaDocentes.size()) {
                        System.out.println(GetRed() + "Opção inválida. Escolha um número entre 1 e " + listaDocentes.size() + "." + GetReset());
                    }
                } catch (NumberFormatException e) {
                    System.out.println(GetRed() + "Por favor, digite apenas números." + GetReset());
                    escolha = -1;
                }
            }

            Docente docente = listaDocentes.get(escolha - 1);

            System.out.println(GetYellow() + "\nTem a certeza que deseja eliminar o docente " + docente.getNome() + "? (s/n)" + GetReset());
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

            Resultado<String> resultado = docenteControllerAtualizado.eliminarDocente(docente.getNif());

            if (resultado.sucesso) {
                if ("INATIVADO".equals(resultado.dados)) {
                    System.out.println(GetYellow() + "\nDocente desativado com sucesso." + GetReset());
                    System.out.println(GetYellow() + "(O docente está atribuído a UCs — o registo foi mantido como inativo em vez de eliminado.)" + GetReset());
                } else {
                    System.out.println(GetGreen() + "\nDocente eliminado com sucesso!" + GetReset());
                }
            } else {
                System.out.println(GetRed() + "\nErro ao eliminar: " + resultado.mensagemErro + GetReset());
            }
            MenuUtils.pressionarEnter(scanner);

        } catch (CancelarRegistoException e) {
            System.out.println("\n" + GetYellow() + "Aviso: " + e.getMessage() + GetReset());
            System.out.println(GetRed() + "Operação de eliminação interrompida!" + GetReset());
            MenuUtils.pressionarEnter(scanner);
        } catch (Exception e) {
            System.out.println(GetRed() + "Ocorreu um erro inesperado: " + e.getMessage() + GetReset());
            MenuUtils.pressionarEnter(scanner);
        }
    }
    //endregion

    //region Estudante
    private void exibirMenuEstudantes() {
        String opcao;

        do {
            try {
                EstudanteController estudanteController = new EstudanteController();
                boolean temEstudantes = !estudanteController.listarEstudantes().isEmpty();

                ArrayList<String> opcoes = new ArrayList<>();
                opcoes.add("1. Registar Estudante");

                if (temEstudantes) {
                    opcoes.add("2. Listar Estudantes");
                    opcoes.add("3. Procurar Estudante (Número Mec)");
                    opcoes.add("4. Atualizar Estudante (Número Mec)");
                    opcoes.add("5. Eliminar Estudante (Número Mec)");
                    opcoes.add("6. Alterar Password do Estudante");
                    opcoes.add("7. Ativar / Desativar Estudante");
                }
                opcoes.add("0. Voltar");


                MenuUtils.limparTela();
                MenuUtils.exibirSubTitulo("PORTAL GESTOR > MENU PRINCIPAL > ESTUDANTES", opcoes);
                System.out.print("\n" + GetWhiteBold() + "Selecione uma opção: " + GetReset());
                opcao = scanner.nextLine().trim();

                switch (opcao) {
                    case "1":
                        registarEstudante();
                        break;
                    case "2":
                        if (temEstudantes) listarEstudantes();
                        else mostrarErroMenu();
                        break;
                    case "3":
                        if (temEstudantes) procurarEstudante();
                        else mostrarErroMenu();
                        break;
                    case "4":
                        if (temEstudantes) atualizarEstudante();
                        else mostrarErroMenu();
                        break;
                    case "5":
                        if (temEstudantes) eliminarEstudante();
                        else mostrarErroMenu();
                        break;
                    case "6":
                        if (temEstudantes) alterarPasswordEstudante();
                        else mostrarErroMenu();
                        break;
                    case "7":
                        if (temEstudantes) ativarDesativarEstudante();
                        else mostrarErroMenu();
                        break;
                    case "0":
                        return;
                    default:
                        mostrarErroMenu();
                }
            } catch (Exception e) {
                System.out.println("\n" + GetRed() + "Ocorreu um erro na navegação: " + e.getMessage() + GetReset());
                MenuUtils.pressionarEnter(scanner);
            }
        } while (true);
    }


    private void registarEstudante() {
        try {
            System.out.println(GetBlue() + "\n--- REGISTO DE ESTUDANTE ---" + GetReset());

            DepartamentoController depController = new DepartamentoController();
            if (depController.listarTodosDepartamentos().isEmpty()) {
                System.out.println(GetYellow() + "\nAviso: Não existem Departamentos registados no sistema." + GetReset());
                System.out.println(GetRed() + "Por favor, vá a 'Gerir Departamentos' e crie um antes de registar estudantes." + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }

            CursoController cursoCtrl = new CursoController();
            List<Curso> cursos = cursoCtrl.listarCursos();

            if (cursos.isEmpty()) {
                System.out.println(GetYellow() + "\nAviso: Não existem Cursos registados no sistema." + GetReset());
                System.out.println(GetRed() + "Por favor, vá a 'Gerir Cursos' e crie um antes de registar estudantes." + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }

            System.out.println(GetYellow() + "[Digite '0' a qualquer momento para cancelar a operação!]" + GetReset());

            String nome = "";
            while (true) {
                nome = BackendUtils.lerInputString(scanner, "Nome: ");
                if (BackendUtils.isNomeValido(nome) && nome.trim().split("\\s+").length >= 2) {
                    break;
                }
                System.out.println(GetRed() + "Deve introduzir pelo menos nome e sobrenome. Tente novamente." + GetReset());
            }

            String morada = "";
            while (morada.isEmpty()) {
                morada = BackendUtils.lerInputString(scanner, "Morada: ");
                if (morada.isEmpty()) System.out.println(GetRed() + "O campo Morada não pode estar vazio. Tente novamente." + GetReset());
            }

            int nif = 0;
            boolean nifValido = false;
            while (!nifValido) {
                try {
                    String nifString = BackendUtils.lerInputString(scanner, "NIF: ");
                    nif = Integer.parseInt(nifString);

                    nifValido = BackendUtils.nifEValido(nifString);
                    if (!nifValido) {
                        System.out.println(GetRed() + "NIF deve ser um número inteiro válido e conter 9 dígitos. Tente novamente." + GetReset());
                    } else {
                        var nifExiste = BackendUtils.nifExiste(nif);
                        if (nifExiste) {
                            System.out.println(GetRed() + "NIF já existente no sistema. Tente com outro NIF." + GetReset());
                            nifValido = false;
                        }
                    }
                } catch (NumberFormatException e) {
                    System.out.println(GetRed() + "Aviso: O valor introduzido não é um número. Tente novamente." + GetReset());
                }
            }

            LocalDate dataNascimento = null;
            while (true) {
                try {
                    String dataString = BackendUtils.lerInputString(scanner, "Data de Nascimento (AAAA-MM-DD): ");
                    dataNascimento = LocalDate.parse(dataString);
                    int idade = Period.between(dataNascimento, LocalDate.now()).getYears();
                    if (idade >= 18) {
                        break;
                    } else {
                        System.out.println(GetRed() + "Sistem só permite pessoas maiores de 18 anos. Tente novamente." + GetReset());
                    }
                } catch (DateTimeParseException e) {
                    System.out.println(GetRed() + "Data deve estar no formato AAAA-MM-DD. Tente novamente." + GetReset());
                }
            }

            System.out.println("\n" + GetBlue() + "Cursos Disponíveis:" + GetReset());
            for (int i = 0; i < cursos.size(); i++) {
                System.out.printf("%d - %s\n", i + 1, cursos.get(i).getNome());
            }

            String cursoNomeSelecionado = "";
            while (true) {
                try {
                    String cursoString = BackendUtils.lerInputString(scanner, "Selecione o ID do Curso: ");
                    int cursoID = Integer.parseInt(cursoString);

                    if (cursoID >= 1 && cursoID <= cursos.size()) {
                        cursoNomeSelecionado = cursos.get(cursoID - 1).getNome();
                        break;
                    } else {
                        System.out.println(GetRed() + "ID inválido. Escolha um número entre 1 e " + cursos.size() + "." + GetReset());
                    }
                } catch (NumberFormatException e) {
                    System.out.println(GetRed() + "Aviso: O valor introduzido não é um número válido." + GetReset());
                }
            }

            System.out.println(GetYellow() + "\nA gerar credenciais e a tentar enviar o email..." + GetReset());

            EstudanteController estudanteControllerAtualizado = new EstudanteController();
            int mecAuto = estudanteControllerAtualizado.gerarNumeroMecanografico();
            String emailAuto = mecAuto + "@issmf.ipp.pt";
            String passAuto = SenhaUtils.gerarPalavraPasseAleatoria();
            SenhaUtils su = new SenhaUtils();
            String senha = su.gerarHashComSalt(passAuto);

            EmailService es = new EmailService();
            String corpoEmail = "-- Credenciais Geradas Automaticamente --\n" +
                    "Nº Mecanográfico: " + mecAuto + "\n" +
                    "Email: " + emailAuto + "\n" +
                    "Palavra-passe: " + passAuto;

            var resEmail = es.enviarEmailRegisto(emailAuto, corpoEmail, TipoDeUtilizador.ESTUDANTE);

            if (resEmail.sucesso) {
                System.out.println(GetGreen() + "Email com as credenciais de acesso enviado com sucesso!" + GetReset());
            } else {
                System.out.println(GetRed() + "Falha ao enviar email: " + resEmail.mensagemErro + GetReset());
                System.out.println(GetRed() + "O registo foi abortado para evitar inconsistência de credenciais." + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }

            Resultado <Integer> resultado = estudanteControllerAtualizado.registarEstudante(nome, morada, nif, dataNascimento, cursoNomeSelecionado, senha);

            if (resultado.sucesso) {
                System.out.println(GetGreen() + "\nEstudante registado com sucesso!" + GetReset());
                System.out.println("Nº Mecanográfico atribuído: " + mecAuto);
            } else {
                System.out.println(GetRed() + "\nErro ao registar estudante: " + resultado.mensagemErro+ GetReset());
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

    private void listarEstudantes() {
        try {
            System.out.println(GetBlue() + "\n--- LISTA DE ESTUDANTES ---" + GetReset());

            EstudanteController estudanteControllerAtualizado = new EstudanteController();
            List<Estudante> lista = estudanteControllerAtualizado.listarEstudantes();

            if (lista.isEmpty()) {
                System.out.println(GetYellow() + "Nenhum estudante registado no sistema." + GetReset());
            } else {
                System.out.println(GetCyanBold() + "----------------------------------------------------------------------------------------" + GetReset());
                System.out.printf(GetWhiteBold() + " %-5s | %-15s | %-30s | %-25s | %-10s\n" + GetReset(), "ID", "Nº MEC", "NOME", "CURSO", "ESTADO");
                System.out.println(GetCyanBold() + "----------------------------------------------------------------------------------------" + GetReset());
                for (int i = 0; i < lista.size(); i++) {
                    Estudante e = lista.get(i);
                    String estadoEst = e.isAtivo() ? GetGreen() + "Ativo" + GetReset() : GetRed() + "Inativo" + GetReset();
                    System.out.printf(" %-5d | %-15d | %-30s | %-25s | %-10s\n", (i + 1), e.getNumeroMec(), e.getNome(), e.getNomeCurso(), estadoEst);
                }
                System.out.println(GetCyanBold() + "----------------------------------------------------------------------------------------" + GetReset());
            }

            MenuUtils.pressionarEnter(scanner);

        } catch (Exception e) {
            System.out.println(GetRed() + "Ocorreu um erro inesperado ao listar os estudantes: " + e.getMessage() + GetReset());
            MenuUtils.pressionarEnter(scanner);
        }
    }

    private void procurarEstudante() {
        try {
            System.out.println(GetBlue() + "\n--- PROCURAR ESTUDANTE ---" + GetReset());
            System.out.println(GetYellow() + "[Digite '0' a qualquer momento para cancelar a operação!]" + GetReset());

            EstudanteController estudanteControllerAtualizado = new EstudanteController();
            List<Estudante> listaEstudantes = estudanteControllerAtualizado.listarEstudantes();
            if (listaEstudantes.isEmpty()) {
                System.out.println(GetYellow() + "Não há estudantes registados no sistema." + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }

            System.out.println("\n" + GetWhiteBold() + "Estudantes Disponíveis:" + GetReset());
            for (int i = 0; i < listaEstudantes.size(); i++) {
                Estudante e = listaEstudantes.get(i);
                System.out.printf("%d - %s (Nº Mec: %d | Curso: %s)\n", i + 1, e.getNome(), e.getNumeroMec(), e.getNomeCurso());
            }

            int escolha = -1;
            while (escolha < 0 || escolha > listaEstudantes.size()) {
                try {
                    String op = BackendUtils.lerInputString(scanner, "\nDigite a opção desejada: ");
                    escolha = Integer.parseInt(op);
                    if (escolha == 0) throw new CancelarRegistoException("Operação cancelada pelo utilizador.");
                    if (escolha < 1 || escolha > listaEstudantes.size()) {
                        System.out.println(GetRed() + "Opção inválida. Escolha um número entre 1 e " + listaEstudantes.size() + "." + GetReset());
                    }
                } catch (NumberFormatException e) {
                    System.out.println(GetRed() + "Por favor, digite apenas números." + GetReset());
                    escolha = -1;
                }
            }

            Estudante estudante = listaEstudantes.get(escolha - 1);
            System.out.println(GetGreen() + "\nDados encontrados:" + GetReset());
            System.out.println(estudante.toString());
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

    private void atualizarEstudante() {
        try {
            System.out.println(GetBlue() + "\n--- ATUALIZAR ESTUDANTE ---" + GetReset());
            System.out.println(GetYellow() + "[Digite '0' a qualquer momento para cancelar a operação!]" + GetReset());

            EstudanteController estudanteControllerAtualizado = new EstudanteController();
            List<Estudante> listaEstudantes = estudanteControllerAtualizado.listarEstudantes();
            if (listaEstudantes.isEmpty()) {
                System.out.println(GetYellow() + "Não há estudantes registados no sistema." + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }

            System.out.println("\n" + GetWhiteBold() + "Estudantes Disponíveis:" + GetReset());
            for (int i = 0; i < listaEstudantes.size(); i++) {
                Estudante e = listaEstudantes.get(i);
                System.out.printf("%d - %s (Nº Mec: %d | Curso: %s)\n", i + 1, e.getNome(), e.getNumeroMec(), e.getNomeCurso());
            }

            int escolha = -1;
            while (escolha < 0 || escolha > listaEstudantes.size()) {
                try {
                    String op = BackendUtils.lerInputString(scanner, "\nDigite a opção do estudante a atualizar: ");
                    escolha = Integer.parseInt(op);
                    if (escolha == 0) throw new CancelarRegistoException("Operação cancelada pelo utilizador.");
                    if (escolha < 1 || escolha > listaEstudantes.size()) {
                        System.out.println(GetRed() + "Opção inválida. Escolha um número entre 1 e " + listaEstudantes.size() + "." + GetReset());
                    }
                } catch (NumberFormatException e) {
                    System.out.println(GetRed() + "Por favor, digite apenas números." + GetReset());
                    escolha = -1;
                }
            }

            Estudante estudante = listaEstudantes.get(escolha - 1);

            System.out.println(GetGreen() + "\nDados atuais:" + GetReset());
            System.out.println(estudante.toString());
            System.out.println(GetYellow() + "\n[Pressione ENTER nos campos que deseja manter iguais]" + GetReset());

            System.out.println(GetYellow() + "\n[Pressione ENTER nos campos que deseja manter iguais]" + GetReset());

            String nomeFinal = estudante.getNome();
            while (true) {
                String nome = BackendUtils.lerInputString(scanner, "Novo Nome: ");
                if (nome.isEmpty()) {
                    break;
                }
                if (BackendUtils.isNomeValido(nome) && nome.trim().split("\\s+").length >= 2) {
                    nomeFinal = nome;
                    break;
                }
                System.out.println(GetRed() + "Deve introduzir pelo menos nome e sobrenome. Tente novamente ou prima ENTER para manter o atual." + GetReset());
            }

            String moradaFinal = estudante.getMorada();
            String morada = BackendUtils.lerInputString(scanner, "Nova Morada: ");
            if (!morada.isEmpty()) moradaFinal = morada;

            String cursoNomeFinal = estudante.getNomeCurso();
            System.out.print("Deseja alterar o curso? (S/N) [Pressione ENTER para N]: ");
            String alterarCurso = scanner.nextLine().trim();

            if (alterarCurso.equalsIgnoreCase("S")) {
                List<Curso> cursos = new CursoController().listarCursos();

                if (cursos.isEmpty()) {
                    System.out.println(GetYellow() + "Não existem cursos registados no sistema. O curso será mantido." + GetReset());
                } else {
                    System.out.println("\n" + GetWhiteBold() + "Cursos Disponíveis:" + GetReset());
                    for (int i = 0; i < cursos.size(); i++) {
                        System.out.printf("%d - %s\n", i + 1, cursos.get(i).getNome());
                    }

                    int escolhaCurso = -1;
                    while (escolhaCurso < 1 || escolhaCurso > cursos.size()) {
                        try {
                            String op = BackendUtils.lerInputString(scanner, "\nSelecione o ID do Novo Curso (ou 0 para cancelar): ");
                            if (op.equals("0")) throw new CancelarRegistoException("Operação cancelada pelo utilizador.");

                            escolhaCurso = Integer.parseInt(op);
                            if (escolhaCurso >= 1 && escolhaCurso <= cursos.size()) {
                                cursoNomeFinal = cursos.get(escolhaCurso - 1).getNome();
                            } else {
                                System.out.println(GetRed() + "ID inválido. Escolha um número da lista." + GetReset());
                            }
                        } catch (NumberFormatException e) {
                            System.out.println(GetRed() + "Aviso: O valor introduzido não é um número válido." + GetReset());
                            escolhaCurso = -1;
                        }
                    }
                }
            }

            Resultado<Estudante> resultado = estudanteControllerAtualizado.atualizarEstudante(estudante.getNumeroMec(), nomeFinal, moradaFinal, cursoNomeFinal);

            if (resultado.sucesso) {
                System.out.println(GetGreen() + "\nEstudante atualizado com sucesso!" + GetReset());
            } else {
                System.out.println(GetRed() + "\nErro ao atualizar estudante: " + resultado.mensagemErro + GetReset());
            }
            MenuUtils.pressionarEnter(scanner);

        } catch (CancelarRegistoException e) {
            System.out.println("\n" + GetYellow() + "Aviso: " + e.getMessage() + GetReset());
            System.out.println(GetRed() + "Operação de atualização interrompida!" + GetReset());
            MenuUtils.pressionarEnter(scanner);
        } catch (Exception e) {
            System.out.println(GetRed() + "Ocorreu um erro inesperado: " + e.getMessage() + GetReset());
            MenuUtils.pressionarEnter(scanner);
        }
    }

    private void eliminarEstudante() {
        try {
            System.out.println(GetBlue() + "\n--- ELIMINAR ESTUDANTE ---" + GetReset());
            System.out.println(GetYellow() + "[Digite '0' a qualquer momento para cancelar a operação!]" + GetReset());

            EstudanteController estudanteControllerAtualizado = new EstudanteController();
            List<Estudante> listaEstudantes = estudanteControllerAtualizado.listarEstudantes();
            if (listaEstudantes.isEmpty()) {
                System.out.println(GetYellow() + "Não há estudantes registados no sistema." + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }

            System.out.println("\n" + GetWhiteBold() + "Estudantes Disponíveis:" + GetReset());
            for (int i = 0; i < listaEstudantes.size(); i++) {
                Estudante e = listaEstudantes.get(i);
                System.out.printf("%d - %s (Nº Mec: %d | Curso: %s)\n", i + 1, e.getNome(), e.getNumeroMec(), e.getNomeCurso());
            }

            int escolha = -1;
            while (escolha < 0 || escolha > listaEstudantes.size()) {
                try {
                    String op = BackendUtils.lerInputString(scanner, "\nDigite a opção do estudante a eliminar: ");
                    escolha = Integer.parseInt(op);
                    if (escolha == 0) throw new CancelarRegistoException("Operação cancelada pelo utilizador.");
                    if (escolha < 1 || escolha > listaEstudantes.size()) {
                        System.out.println(GetRed() + "Opção inválida. Escolha um número entre 1 e " + listaEstudantes.size() + "." + GetReset());
                    }
                } catch (NumberFormatException e) {
                    System.out.println(GetRed() + "Por favor, digite apenas números." + GetReset());
                    escolha = -1;
                }
            }

            Estudante estudanteAapagar = listaEstudantes.get(escolha - 1);

            System.out.println(GetYellow() + "\nTem a certeza que deseja eliminar o estudante " + estudanteAapagar.getNome() + "? (s/n)" + GetReset());
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

            Resultado<String> resultado = estudanteControllerAtualizado.eliminarEstudante(estudanteAapagar.getNumeroMec());

            if (resultado.sucesso) {
                if ("INATIVADO".equals(resultado.dados)) {
                    System.out.println(GetYellow() + "\nEstudante desativado com sucesso." + GetReset());
                    System.out.println(GetYellow() + "(Curso já iniciado — o registo foi mantido como inativo em vez de eliminado.)" + GetReset());
                } else {
                    System.out.println(GetGreen() + "\nEstudante eliminado com sucesso!" + GetReset());
                }
            } else {
                System.out.println(GetRed() + "\nErro ao eliminar: " + resultado.mensagemErro + GetReset());
            }
            MenuUtils.pressionarEnter(scanner);

        } catch (CancelarRegistoException e) {
            System.out.println("\n" + GetYellow() + "Aviso: " + e.getMessage() + GetReset());
            System.out.println(GetRed() + "Operação de eliminação interrompida!" + GetReset());
            MenuUtils.pressionarEnter(scanner);
        } catch (Exception e) {
            System.out.println(GetRed() + "Ocorreu um erro inesperado: " + e.getMessage() + GetReset());
            MenuUtils.pressionarEnter(scanner);
        }
    }

    private void alterarPasswordEstudante() {
        try {
            System.out.println(GetBlue() + "\n--- ALTERAR PASSWORD DO ESTUDANTE ---" + GetReset());
            System.out.println(GetYellow() + "[Digite '0' a qualquer momento para cancelar a operação!]" + GetReset());

            controller.EstudanteController ec = new controller.EstudanteController();
            List<Estudante> listaEstudantes = ec.listarEstudantes();

            if (listaEstudantes == null || listaEstudantes.isEmpty()) {
                System.out.println(GetYellow() + "Não existem estudantes registados no sistema." + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }

            System.out.println("\n" + GetWhiteBold() + "Estudantes Disponíveis:" + GetReset());
            for (int i = 0; i < listaEstudantes.size(); i++) {
                Estudante e = listaEstudantes.get(i);
                System.out.printf("%d - %s (Nº Mec: %d | Curso: %s)\n", i + 1, e.getNome(), e.getNumeroMec(), e.getNomeCurso());
            }

            int escolha = -1;
            while (escolha < 1 || escolha > listaEstudantes.size()) {
                try {
                    String op = BackendUtils.lerInputString(scanner, "\nEscolha o número do estudante da lista: ");
                    if (op.equals("0")) throw new CancelarRegistoException("Operação cancelada pelo utilizador.");

                    escolha = Integer.parseInt(op);
                    if (escolha < 1 || escolha > listaEstudantes.size()) {
                        System.out.println(GetRed() + "Opção inválida. Escolha um número entre 1 e " + listaEstudantes.size() + "." + GetReset());
                    }
                } catch (NumberFormatException e) {
                    System.out.println(GetRed() + "Por favor, digite apenas números." + GetReset());
                }
            }

            Estudante estudanteSelecionado = listaEstudantes.get(escolha - 1);

            String novaPass = BackendUtils.lerSenhaComConfirmacao(
                    "Nova Senha: ", "Confirmar Nova Senha: ", scanner);

            SenhaUtils su = new SenhaUtils();
            String novoHash = su.gerarHashComSalt(novaPass);

            Resultado<Estudante> res = ec.alterarPassword(estudanteSelecionado.getNumeroMec(), novoHash);

            if (res.sucesso) {
                System.out.println(GetGreen() + "\nPassword do estudante alterada com sucesso!" + GetReset());
                System.out.println(GetYellow() + "A enviar email de notificação ao aluno..." + GetReset());
                EmailService emailService = new EmailService();
                var resEmail = emailService.enviarEmailRecuperacaoDeSenha(estudanteSelecionado.getEmail(), novaPass);

                if (resEmail.sucesso) {
                    System.out.println(GetGreen() + "Email com a nova password enviado com sucesso!" + GetReset());
                } else {
                    System.out.println(GetRed() + "Aviso: A password foi alterada, mas ocorreu um erro ao enviar o email: " + resEmail.mensagemErro + GetReset());
                }
            } else {
                System.out.println(GetRed() + "\nErro ao alterar password: " + res.mensagemErro + GetReset());
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

//endregion

    private void consultarAlunosEmDivida() {
        try {
            System.out.println(GetBlue() + "\n--- TESOURARIA: ALUNOS EM DÍVIDA ---" + GetReset());

            PropinaController propinaController = new PropinaController();
            List<Estudante> devedores = propinaController.obterAlunosEmDivida();

            if (devedores.isEmpty()) {
                System.out.println(GetGreen() + "\nExcelente notícia! Não há nenhum estudante com propinas em atraso no sistema." + GetReset());
            } else {
                System.out.println(GetYellow() + "\nLista de Estudantes com pagamentos pendentes:" + GetReset());
                System.out.println(GetCyanBold() + "------------------------------------------------------------------------------------------------------------------" + GetReset());
                System.out.printf(GetWhiteBold() + " %-12s | %-25s | %-20s | %-15s | %-25s \n" + GetReset(), "Nº MEC", "NOME", "CURSO", "DÍVIDA TOTAL", "SITUAÇÃO (ANOS)");
                System.out.println(GetCyanBold() + "------------------------------------------------------------------------------------------------------------------" + GetReset());

                for (Estudante e : devedores) {
                    List<Propina> propinas = propinaController.consultarPropinasEstudante(e.getNumeroMec());

                    // CORREÇÃO 1: Usar BigDecimal.ZERO
                    BigDecimal dividaTotal = BigDecimal.ZERO;
                    StringBuilder situacaoAnos = new StringBuilder();

                    for (Propina p : propinas) {
                        if (!p.isTotalmentePaga()) {
                            // CORREÇÃO 2: Usar o método .add() em vez de +=
                            dividaTotal = dividaTotal.add(p.getValorEmDivida());

                            if (situacaoAnos.length() > 0) situacaoAnos.append(", ");
                            situacaoAnos.append(p.getAnoLetivo()).append("º Ano");
                        }
                    }

                    String nome = e.getNome();
                    if (nome.length() > 25) nome = nome.substring(0, 22) + "...";
                    String curso = e.getNomeCurso();
                    if (curso.length() > 20) curso = curso.substring(0, 17) + "...";
                    String anosStr = situacaoAnos.toString();
                    if(anosStr.length() > 25) anosStr = anosStr.substring(0, 22) + "...";

                    // CORREÇÃO 3: Quando imprimimos BigDecimal formatado com String.format, passamos o próprio objeto
                    System.out.printf(" %-12d | %-25s | %-20s | %-15s | %-25s \n",
                            e.getNumeroMec(), nome, curso, String.format("%.2f€", dividaTotal), anosStr);
                }
                System.out.println(GetCyanBold() + "------------------------------------------------------------------------------------------------------------------" + GetReset());
            }

            MenuUtils.pressionarEnter(scanner);

        } catch (Exception e) {
            System.out.println(GetRed() + "Ocorreu um erro ao carregar a lista de tesouraria: " + e.getMessage() + GetReset());
            MenuUtils.pressionarEnter(scanner);
        }
    }

    private void mostrarErroMenu() {
        System.out.println(GetRed() + "Opção inválida ou indisponível de momento. Por favor, escolha uma opção visível na lista." + GetReset());
        MenuUtils.pressionarEnter(scanner);
    }

    public void exibirMenuAnoLetivo() {
        String opcao;
        ArrayList<String> opcoes = new ArrayList<>();
        opcoes.add("1. Verificar Ano Letivo Atual");
        opcoes.add("2. Buscar por Ano Letivo");
        opcoes.add("3. Simular Passagem de Ano");
        opcoes.add("0. Voltar");

        do {
            try {
                MenuUtils.exibirSubTitulo("PORTAL GESTOR > MENU PRINCIPAL", opcoes);
                System.out.print("\nSelecione uma opção: ");
                opcao = scanner.nextLine().trim();

                switch (opcao) {
                    case "1":
                        verificarAnoLetivoAtual();
                        break;
                    case "2":
                        buscarPorAnoLetivo();
                        break;
                    case "3":
                        simularPassagemDeAno();
                        break;
                    case "0":
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


    private static final String MSG_MIGRACAO =
            "As tabelas de Ano Letivo ainda não existem na base de dados.\n" +
                    "Execute o script SQL antes de usar esta funcionalidade:\n" +
                    "  Ficheiro: sql/AnoLetivo_Migration.sql\n" +
                    "  Ferramenta: SQL Server Management Studio";

    private void verificarAnoLetivoAtual() {
        try {
            System.out.println(GetBlue() + "\n--- SITUAÇÃO DO ANO LETIVO ATUAL ---" + GetReset());

            controller.AnoLetivoController alc = new controller.AnoLetivoController();

            if (!alc.bdPreparada()) {
                System.out.println(GetYellow() + "\n" + MSG_MIGRACAO + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }

            model.AnoLetivo anoAtual = alc.obterOuCriarAnoAtual();

            if (anoAtual != null) {
                System.out.println(GetCyanBold() + "Ano Letivo: " + anoAtual.getDescricao()
                        + " | Início: " + anoAtual.getDataInicio()
                        + " | Estado: " + anoAtual.getEstado() + GetReset());
            }
            System.out.println();

            List<String> relatorio = alc.gerarRelatorioAnoAtual();
            for (String linha : relatorio) {
                if (linha.startsWith("══")) {
                    System.out.println(GetCyanBold() + linha + GetReset());
                } else if (linha.startsWith("  Alunos") || linha.startsWith("  Unidades") || linha.startsWith("──")) {
                    System.out.println(GetWhiteBold() + linha + GetReset());
                } else if (linha.startsWith("    •")) {
                    System.out.println(GetGreen() + linha + GetReset());
                } else if (linha.startsWith("    [")) {
                    System.out.println(linha);
                } else {
                    System.out.println(linha);
                }
            }

            MenuUtils.pressionarEnter(scanner);
        } catch (Exception e) {
            System.out.println(GetRed() + "Erro ao carregar situação do ano letivo: " + e.getMessage() + GetReset());
            MenuUtils.pressionarEnter(scanner);
        }
    }

    private void buscarPorAnoLetivo() {
        try {
            System.out.println(GetBlue() + "\n--- BUSCAR ANO LETIVO ---" + GetReset());

            controller.AnoLetivoController alc = new controller.AnoLetivoController();

            if (!alc.bdPreparada()) {
                System.out.println(GetYellow() + "\n" + MSG_MIGRACAO + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }

            List<model.AnoLetivo> todos = alc.listarTodos();

            if (todos.isEmpty()) {
                System.out.println(GetYellow() + "Não existem anos letivos registados no sistema." + GetReset());
                System.out.println(GetYellow() + "Inicie a gestão do ano letivo para criar o primeiro registo." + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }

            int anoMinimo = todos.stream().mapToInt(model.AnoLetivo::getAnoCalendario).min().orElse(0);

            System.out.println(GetWhiteBold() + "\nAnos letivos disponíveis:" + GetReset());
            for (int i = 0; i < todos.size(); i++) {
                model.AnoLetivo al = todos.get(i);
                String estado = al.isAtivo() ? GetGreen() + "[ATUAL]" + GetReset() : "[CONCLUÍDO]";
                String dataFimStr = al.getDataFim() != null ? al.getDataFim().toString() : "em curso";
                System.out.printf("  %d. %s %s | %s → %s%n",
                        i + 1, al.getDescricao(), estado, al.getDataInicio(), dataFimStr);
            }

            System.out.println(GetYellow() + "\n[Ano mínimo disponível: " + anoMinimo + "]" + GetReset());
            System.out.print(GetWhiteBold() + "Introduza o ano de início (ex: " + anoMinimo + " para " + anoMinimo + "/" + (anoMinimo + 1) + "): " + GetReset());
            String input = scanner.nextLine().trim();
            if (input.equals("0")) return;

            int anoEscolhido;
            try {
                anoEscolhido = Integer.parseInt(input);
            } catch (NumberFormatException ex) {
                System.out.println(GetRed() + "Valor inválido." + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }

            if (anoEscolhido < anoMinimo) {
                System.out.println(GetRed() + "Não existem dados anteriores a " + anoMinimo + "." + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }

            model.AnoLetivo encontrado = alc.buscarPorAno(anoEscolhido);
            if (encontrado == null) {
                System.out.println(GetYellow() + "Nenhum registo encontrado para o ano " + anoEscolhido + "/" + (anoEscolhido + 1) + "." + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }

            System.out.println(GetCyanBold() + "\n══ Ano Letivo " + encontrado.getDescricao() + " ══" + GetReset());
            System.out.println("  Início: " + encontrado.getDataInicio());
            System.out.println("  Fim:    " + (encontrado.getDataFim() != null ? encontrado.getDataFim() : "em curso"));
            System.out.println("  Estado: " + encontrado.getEstado());
            System.out.println();

            List<String> relatorio = alc.gerarRelatorioAnoPorCalendario(anoEscolhido);
            for (String linha : relatorio) {
                if (linha.startsWith("══")) {
                    System.out.println(GetCyanBold() + linha + GetReset());
                } else if (linha.startsWith("  Alunos")) {
                    System.out.println(GetWhiteBold() + linha + GetReset());
                } else if (linha.startsWith("    •")) {
                    System.out.println(linha);
                } else {
                    System.out.println(linha);
                }
            }

            MenuUtils.pressionarEnter(scanner);
        } catch (Exception e) {
            System.out.println(GetRed() + "Erro ao buscar ano letivo: " + e.getMessage() + GetReset());
            MenuUtils.pressionarEnter(scanner);
        }
    }

    private void simularPassagemDeAno() {
        System.out.println(GetBlue() + "\n--- SIMULADOR DE PASSAGEM DE ANO LETIVO ---" + GetReset());
        System.out.println(GetYellow() + "Esta ação irá verificar todas as condições necessárias para avançar o ano letivo:" + GetReset());
        System.out.println(GetYellow() + "  • Todas as notas lançadas em todos os momentos de avaliação" + GetReset());
        System.out.println(GetYellow() + "  • Todas as propinas pagas por todos os alunos ativos" + GetReset());
        System.out.println(GetYellow() + "Apenas avança se tudo estiver em conformidade." + GetReset());

        System.out.println(GetWhiteBold() + "\nA verificar condições..." + GetReset());

        controller.AnoLetivoController alc = new controller.AnoLetivoController();

        if (!alc.bdPreparada()) {
            System.out.println(GetYellow() + "\n" + MSG_MIGRACAO + GetReset());
            MenuUtils.pressionarEnter(scanner);
            return;
        }

        Resultado<List<String>> verificacao = alc.verificarCondicioesSaltoDeAno();

        if (!verificacao.sucesso) {
            System.out.println(GetRed() + "\n╔══ BLOQUEIOS ENCONTRADOS — NÃO É POSSÍVEL AVANÇAR O ANO LETIVO ══╗" + GetReset());
            System.out.println(GetRed() + "  Total de bloqueios: " + verificacao.dados.size() + GetReset());
            System.out.println(GetRed() + "╠═════════════════════════════════════════════════════════════════╣" + GetReset());

            for (String bloqueio : verificacao.dados) {
                if (bloqueio.startsWith("[PROPINA]")) {
                    System.out.println(GetRed() + "  💰 " + bloqueio + GetReset());
                } else if (bloqueio.startsWith("[SEM ACTIVIDADE]")) {
                    System.out.println(GetYellow() + "  ⚠  " + bloqueio + GetReset());
                } else {
                    System.out.println(GetYellow() + "  📋 " + bloqueio + GetReset());
                }
            }

            System.out.println(GetRed() + "╚═════════════════════════════════════════════════════════════════╝" + GetReset());
            System.out.println(GetYellow() + "\nResolva todos os bloqueios acima antes de avançar o ano letivo." + GetReset());
            MenuUtils.pressionarEnter(scanner);
            return;
        }

        System.out.println(GetGreen() + "✓ Todas as condições verificadas com sucesso!" + GetReset());

        String confirmacao = BackendUtils.lerInputString(scanner, GetWhiteBold() + "\nDeseja prosseguir com a passagem de ano letivo? (S/N): " + GetReset());

        if (!confirmacao.equalsIgnoreCase("S")) {
            System.out.println(GetYellow() + "Passagem de ano cancelada pelo utilizador." + GetReset());
            MenuUtils.pressionarEnter(scanner);
            return;
        }

        EstudanteController ec = new EstudanteController();
        Resultado<List<String>> res = ec.simularTransicaoAnoLetivoGlobal();

        if (res.sucesso) {
            boolean avancouAno = alc.avancarAnoLetivo();
            model.AnoLetivo novoAno = alc.obterAnoAtual();

            System.out.println(GetGreen() + "\n====== RELATÓRIO DE TRANSIÇÃO ======" + GetReset());
            for (String log : res.dados) {
                if (log.contains("[CONCLUÍDO]") || log.contains("[AVANÇOU]")) {
                    System.out.println(GetGreen() + log + GetReset());
                } else if (log.contains("[RETIDO]")) {
                    System.out.println(GetRed() + log + GetReset());
                } else {
                    System.out.println(GetCyanBold() + log + GetReset());
                }
            }
            System.out.println(GetGreen() + "====================================" + GetReset());

            if (avancouAno && novoAno != null) {
                System.out.println(GetGreen() + "Novo ano letivo iniciado: " + novoAno.getDescricao() + GetReset());
            }
            System.out.println(GetWhiteBold() + "Transição concluída. Consulte a tesouraria ou as fichas para confirmar." + GetReset());
        } else {
            System.out.println(GetRed() + "\nErro ao processar a transição: " + res.mensagemErro + GetReset());
        }

        MenuUtils.pressionarEnter(scanner);
    }

    //region Ativar/Desativar utilizadores
    private void ativarDesativarGestor() {
        try {
            System.out.println(GetBlue() + "\n--- ATIVAR / DESATIVAR GESTOR ---" + GetReset());
            System.out.println(GetYellow() + "[Digite '0' para cancelar]" + GetReset());

            GestorController gc = new GestorController();
            List<Gestor> lista = gc.listarGestores();
            if (lista.isEmpty()) {
                System.out.println(GetYellow() + "Não há gestores registados." + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }

            System.out.println("\n" + GetWhiteBold() + "Gestores:" + GetReset());
            for (int i = 0; i < lista.size(); i++) {
                Gestor g = lista.get(i);
                String estado = g.isAtivo() ? GetGreen() + "[ATIVO]" + GetReset() : GetRed() + "[INATIVO]" + GetReset();
                System.out.printf("  %d - %s (NIF: %d) %s%n", i + 1, g.getNome(), g.getNif(), estado);
            }

            int escolha = -1;
            while (escolha < 1 || escolha > lista.size()) {
                try {
                    String op = BackendUtils.lerInputString(scanner, "\nEscolha o gestor: ");
                    if (op.equals("0")) return;
                    escolha = Integer.parseInt(op);
                    if (escolha < 1 || escolha > lista.size())
                        System.out.println(GetRed() + "Opção inválida." + GetReset());
                } catch (NumberFormatException e) {
                    System.out.println(GetRed() + "Digite um número." + GetReset());
                }
            }

            Gestor gestor = lista.get(escolha - 1);
            String acao = gestor.isAtivo() ? "desativar" : "ativar";
            String conf = BackendUtils.lerInputString(scanner, GetYellow() + "Deseja " + acao + " o gestor " + gestor.getNome() + "? (S/N): " + GetReset());
            if (!conf.equalsIgnoreCase("S")) {
                System.out.println(GetYellow() + "Operação cancelada." + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }

            Resultado<Gestor> res = gc.ativarDesativarGestor(gestor.getNif(), !gestor.isAtivo());
            if (res.sucesso) {
                System.out.println(GetGreen() + "\nGestor " + (res.dados.isAtivo() ? "ativado" : "desativado") + " com sucesso!" + GetReset());
            } else {
                System.out.println(GetRed() + "\nErro: " + res.mensagemErro + GetReset());
            }
            MenuUtils.pressionarEnter(scanner);

        } catch (Exception e) {
            System.out.println(GetRed() + "Erro inesperado: " + e.getMessage() + GetReset());
            MenuUtils.pressionarEnter(scanner);
        }
    }

    private void ativarDesativarDocente() {
        try {
            System.out.println(GetBlue() + "\n--- ATIVAR / DESATIVAR DOCENTE ---" + GetReset());
            System.out.println(GetYellow() + "[Digite '0' para cancelar]" + GetReset());

            DocenteController dc = new DocenteController();
            List<Docente> lista = dc.listarDocentes();
            if (lista.isEmpty()) {
                System.out.println(GetYellow() + "Não há docentes registados." + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }

            System.out.println("\n" + GetWhiteBold() + "Docentes:" + GetReset());
            for (int i = 0; i < lista.size(); i++) {
                Docente d = lista.get(i);
                String estado = d.isAtivo() ? GetGreen() + "[ATIVO]" + GetReset() : GetRed() + "[INATIVO]" + GetReset();
                System.out.printf("  %d - %s (%s) %s%n", i + 1, d.getNome(), d.getSigla(), estado);
            }

            int escolha = -1;
            while (escolha < 1 || escolha > lista.size()) {
                try {
                    String op = BackendUtils.lerInputString(scanner, "\nEscolha o docente: ");
                    if (op.equals("0")) return;
                    escolha = Integer.parseInt(op);
                    if (escolha < 1 || escolha > lista.size())
                        System.out.println(GetRed() + "Opção inválida." + GetReset());
                } catch (NumberFormatException e) {
                    System.out.println(GetRed() + "Digite um número." + GetReset());
                }
            }

            Docente docente = lista.get(escolha - 1);
            String acao = docente.isAtivo() ? "desativar" : "ativar";
            String conf = BackendUtils.lerInputString(scanner, GetYellow() + "Deseja " + acao + " o docente " + docente.getNome() + "? (S/N): " + GetReset());
            if (!conf.equalsIgnoreCase("S")) {
                System.out.println(GetYellow() + "Operação cancelada." + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }

            Resultado<Docente> res = dc.ativarDesativarDocente(docente.getNif(), !docente.isAtivo());
            if (res.sucesso) {
                System.out.println(GetGreen() + "\nDocente " + (res.dados.isAtivo() ? "ativado" : "desativado") + " com sucesso!" + GetReset());
            } else {
                System.out.println(GetRed() + "\nErro: " + res.mensagemErro + GetReset());
            }
            MenuUtils.pressionarEnter(scanner);

        } catch (Exception e) {
            System.out.println(GetRed() + "Erro inesperado: " + e.getMessage() + GetReset());
            MenuUtils.pressionarEnter(scanner);
        }
    }

    private void ativarDesativarEstudante() {
        try {
            System.out.println(GetBlue() + "\n--- ATIVAR / DESATIVAR ESTUDANTE ---" + GetReset());
            System.out.println(GetYellow() + "[Digite '0' para cancelar]" + GetReset());

            EstudanteController ec = new EstudanteController();
            List<Estudante> lista = ec.listarEstudantes();
            if (lista.isEmpty()) {
                System.out.println(GetYellow() + "Não há estudantes registados." + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }

            System.out.println("\n" + GetWhiteBold() + "Estudantes:" + GetReset());
            for (int i = 0; i < lista.size(); i++) {
                Estudante e = lista.get(i);
                String estado = e.isAtivo() ? GetGreen() + "[ATIVO]" + GetReset() : GetRed() + "[INATIVO]" + GetReset();
                System.out.printf("  %d - %s (Nº %d | %s) %s%n", i + 1, e.getNome(), e.getNumeroMec(), e.getNomeCurso(), estado);
            }

            int escolha = -1;
            while (escolha < 1 || escolha > lista.size()) {
                try {
                    String op = BackendUtils.lerInputString(scanner, "\nEscolha o estudante: ");
                    if (op.equals("0")) return;
                    escolha = Integer.parseInt(op);
                    if (escolha < 1 || escolha > lista.size())
                        System.out.println(GetRed() + "Opção inválida." + GetReset());
                } catch (NumberFormatException e) {
                    System.out.println(GetRed() + "Digite um número." + GetReset());
                }
            }

            Estudante estudante = lista.get(escolha - 1);
            String acao = estudante.isAtivo() ? "desativar" : "ativar";
            String conf = BackendUtils.lerInputString(scanner, GetYellow() + "Deseja " + acao + " o estudante " + estudante.getNome() + "? (S/N): " + GetReset());
            if (!conf.equalsIgnoreCase("S")) {
                System.out.println(GetYellow() + "Operação cancelada." + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }

            Resultado<Estudante> res = ec.ativarDesativarEstudante(estudante.getNumeroMec(), !estudante.isAtivo());
            if (res.sucesso) {
                System.out.println(GetGreen() + "\nEstudante " + (res.dados.isAtivo() ? "ativado" : "desativado") + " com sucesso!" + GetReset());
            } else {
                System.out.println(GetRed() + "\nErro: " + res.mensagemErro + GetReset());
            }
            MenuUtils.pressionarEnter(scanner);

        } catch (Exception e) {
            System.out.println(GetRed() + "Erro inesperado: " + e.getMessage() + GetReset());
            MenuUtils.pressionarEnter(scanner);
        }
    }
    //endregion

    // ══════════════════════════════════════════════════════════════
    //  v1.3 — HORÁRIOS
    // ══════════════════════════════════════════════════════════════
    private void exibirMenuHorarios() {
        String opcao;
        do {
            try {
                ArrayList<String> opcoes = new ArrayList<>();
                opcoes.add("1. Definir Horário de UC");
                opcoes.add("2. Listar Horários por UC");
                opcoes.add("3. Listar Horários do Ano Letivo Atual");
                opcoes.add("4. Eliminar Horário");
                opcoes.add("0. Voltar");

                MenuUtils.exibirSubTitulo("GESTOR > HORÁRIOS", opcoes);
                System.out.print("\n" + GetWhiteBold() + "Selecione uma opção: " + GetReset());
                opcao = scanner.nextLine().trim();

                switch (opcao) {
                    case "1": definirHorario(); break;
                    case "2": listarHorariosPorUC(); break;
                    case "3": listarHorariosAnoLetivo(); break;
                    case "4": eliminarHorario(); break;
                    case "0": return;
                    default:
                        System.out.println(GetRed() + "Opção inválida. Escolha entre 0 e 4." + GetReset());
                        MenuUtils.pressionarEnter(scanner);
                }
            } catch (Exception e) {
                System.out.println(GetRed() + "Erro: " + e.getMessage() + GetReset());
                MenuUtils.pressionarEnter(scanner);
            }
        } while (true);
    }

    private void definirHorario() {
        try {
            System.out.println(GetBlue() + "\n--- DEFINIR HORÁRIO ---" + GetReset());
            System.out.println(GetYellow() + "[Digite '0' para cancelar a qualquer momento]" + GetReset());

            UnidadeCurricularController ucc = new UnidadeCurricularController();
            List<model.UnidadeCurricular> ucs = ucc.listarTodasUCs();
            if (ucs.isEmpty()) {
                System.out.println(GetYellow() + "Não existem UCs registadas." + GetReset());
                MenuUtils.pressionarEnter(scanner); return;
            }

            System.out.println(GetWhiteBold() + "\nUCs disponíveis:" + GetReset());
            System.out.println(GetCyanBold() + "  ──────────────────────────────────────────────────────" + GetReset());
            for (int i = 0; i < ucs.size(); i++) {
                model.UnidadeCurricular uc = ucs.get(i);
                String docNome = uc.getDocente() != null ? uc.getDocente().getSigla() : "sem docente";
                System.out.printf("  %d. %-35s Ano %d  [%s]%n", i+1, uc.getNome(), uc.getAnoCurricular(), docNome);
            }
            System.out.println(GetCyanBold() + "  ──────────────────────────────────────────────────────" + GetReset());

            int ucIdx = -1;
            while (ucIdx < 1 || ucIdx > ucs.size()) {
                try {
                    String op = BackendUtils.lerInputString(scanner, "\nEscolha a UC (número): ");
                    ucIdx = Integer.parseInt(op);
                    if (ucIdx < 1 || ucIdx > ucs.size()) {
                        System.out.println(GetRed() + "Opção inválida." + GetReset());
                        ucIdx = -1;
                    }
                } catch (NumberFormatException ex) {
                    System.out.println(GetRed() + "Introduza um número válido." + GetReset());
                }
            }
            model.UnidadeCurricular ucEscolhida = ucs.get(ucIdx - 1);

            controller.AnoLetivoController alCtrl = new controller.AnoLetivoController();
            model.AnoLetivo anoAtual = alCtrl.obterOuCriarAnoAtual();
            if (anoAtual == null) {
                System.out.println(GetRed() + "Não existe ano letivo ativo (modo SQL necessário)." + GetReset());
                MenuUtils.pressionarEnter(scanner); return;
            }

            System.out.println(GetWhiteBold() + "\nDias da semana:" + GetReset());
            model.DiaSemana[] dias = model.DiaSemana.values();
            for (int i = 0; i < dias.length; i++)
                System.out.printf("  %d. %s%n", i+1, dias[i].getDescricao());

            int diaIdx = -1;
            while (diaIdx < 1 || diaIdx > dias.length) {
                try {
                    String op = BackendUtils.lerInputString(scanner, "Escolha o dia: ");
                    diaIdx = Integer.parseInt(op);
                    if (diaIdx < 1 || diaIdx > dias.length) {
                        System.out.println(GetRed() + "Dia inválido." + GetReset());
                        diaIdx = -1;
                    }
                } catch (NumberFormatException ex) {
                    System.out.println(GetRed() + "Introduza um número válido." + GetReset());
                }
            }
            model.DiaSemana dia = dias[diaIdx - 1];

            java.time.LocalTime horaInicio = null;
            while (horaInicio == null) {
                try {
                    String horaStr = BackendUtils.lerInputString(scanner, "Hora de início (ex: 18 ou 18:30): ");
                    horaInicio = common.utils.BackendUtils.parseHoraFlexivel(horaStr);
                } catch (IllegalArgumentException ex) {
                    System.out.println(GetRed() + ex.getMessage() + GetReset());
                }
            }

            System.out.println(GetWhiteBold() + "Duração: " + GetReset() + "1. 1 hora   2. 2 horas");
            int horas = -1;
            while (horas != 1 && horas != 2) {
                try {
                    String op = BackendUtils.lerInputString(scanner, "Escolha (1 ou 2): ");
                    horas = Integer.parseInt(op);
                    if (horas != 1 && horas != 2) System.out.println(GetRed() + "Duração deve ser 1 ou 2 horas." + GetReset());
                } catch (NumberFormatException ex) {
                    System.out.println(GetRed() + "Introduza 1 ou 2." + GetReset());
                }
            }
            java.time.LocalTime horaFim = horaInicio.plusHours(horas);

            String sala = BackendUtils.lerInputString(scanner, "Sala (ex: B123): ");

            HorarioController hCtrl = new HorarioController();
            model.Resultado<model.Horario> res = hCtrl.registarHorario(ucEscolhida.getId(), anoAtual.getId(), dia, horaInicio, horaFim, sala);

            if (res.sucesso) {
                System.out.println(GetGreen() + "\n✔ Horário registado com sucesso!" + GetReset());
                System.out.printf("  %s  |  %s–%s  |  UC: %s  |  Sala: %s%n",
                        dia.getDescricao(), horaInicio, horaFim, ucEscolhida.getNome(), sala);
            } else {
                System.out.println(GetRed() + "\nErro: " + res.mensagemErro + GetReset());
            }
        } catch (Exception e) {
            System.out.println(GetRed() + "Erro: " + e.getMessage() + GetReset());
        }
        MenuUtils.pressionarEnter(scanner);
    }

    private void listarHorariosPorUC() {
        try {
            System.out.println(GetBlue() + "\n--- HORÁRIOS POR UC ---" + GetReset());
            UnidadeCurricularController ucc = new UnidadeCurricularController();
            List<model.UnidadeCurricular> ucs = ucc.listarTodasUCs();
            if (ucs.isEmpty()) {
                System.out.println(GetYellow() + "Sem UCs registadas." + GetReset());
                MenuUtils.pressionarEnter(scanner); return;
            }

            System.out.println(GetWhiteBold() + "\nUCs disponíveis:" + GetReset());
            for (int i = 0; i < ucs.size(); i++)
                System.out.printf("  %d. %s%n", i+1, ucs.get(i).getNome());

            int idx = -1;
            while (idx < 1 || idx > ucs.size()) {
                try {
                    String op = BackendUtils.lerInputString(scanner, "\nEscolha a UC: ");
                    idx = Integer.parseInt(op);
                    if (idx < 1 || idx > ucs.size()) {
                        System.out.println(GetRed() + "Opção inválida." + GetReset());
                        idx = -1;
                    }
                } catch (NumberFormatException ex) {
                    System.out.println(GetRed() + "Introduza um número válido." + GetReset());
                }
            }

            HorarioController hCtrl = new HorarioController();
            List<model.Horario> horarios = hCtrl.listarHorariosPorUC(ucs.get(idx - 1).getId());

            System.out.println("\n" + GetCyanBold() + "────────────────────────────────────────────────────────────────" + GetReset());
            System.out.printf(GetWhiteBold() + " %-12s | %-8s | %-8s | %-8s%n" + GetReset(), "DIA", "INÍCIO", "FIM", "SALA");
            System.out.println(GetCyanBold() + "────────────────────────────────────────────────────────────────" + GetReset());

            if (horarios.isEmpty()) {
                System.out.println(GetYellow() + " Sem horários definidos para esta UC." + GetReset());
            } else {
                for (model.Horario h : horarios.stream()
                        .sorted(java.util.Comparator.comparing(model.Horario::getDiaSemana)
                                .thenComparing(model.Horario::getHoraInicio))
                        .collect(java.util.stream.Collectors.toList())) {
                    System.out.printf(" %-12s | %-8s | %-8s | %-8s%n",
                            h.getDiaSemana().getDescricao(), h.getHoraInicio(), h.getHoraFim(), h.getSala());
                }
            }
            System.out.println(GetCyanBold() + "────────────────────────────────────────────────────────────────" + GetReset());
        } catch (Exception e) {
            System.out.println(GetRed() + "Erro: " + e.getMessage() + GetReset());
        }
        MenuUtils.pressionarEnter(scanner);
    }

    private void listarHorariosAnoLetivo() {
        try {
            System.out.println(GetBlue() + "\n--- HORÁRIOS DO ANO LETIVO ATUAL ---" + GetReset());
            controller.AnoLetivoController alCtrl = new controller.AnoLetivoController();
            model.AnoLetivo anoAtual = alCtrl.obterOuCriarAnoAtual();
            if (anoAtual == null) {
                System.out.println(GetRed() + "Modo SQL necessário para aceder ao Ano Letivo." + GetReset());
                MenuUtils.pressionarEnter(scanner); return;
            }

            HorarioController hCtrl = new HorarioController();
            List<model.Horario> horarios = hCtrl.listarHorariosPorAnoLetivo(anoAtual.getId());

            if (horarios.isEmpty()) {
                System.out.println(GetYellow() + "Sem horários definidos para o ano letivo atual." + GetReset());
            } else {
                List<model.Horario> ordenados = horarios.stream()
                        .sorted(java.util.Comparator.comparing(model.Horario::getDiaSemana)
                                .thenComparing(model.Horario::getHoraInicio))
                        .collect(java.util.stream.Collectors.toList());

                System.out.println("\n" + GetCyanBold() + "────────────────────────────────────────────────────────────────────────" + GetReset());
                System.out.printf(GetWhiteBold() + " %-12s | %-8s | %-8s | %-30s | %-6s%n" + GetReset(),
                        "DIA", "INÍCIO", "FIM", "UNIDADE CURRICULAR", "SALA");
                System.out.println(GetCyanBold() + "────────────────────────────────────────────────────────────────────────" + GetReset());

                for (model.Horario h : ordenados) {
                    String ucNome = h.getUnidadeCurricular() != null ? h.getUnidadeCurricular().getNome() : "?";
                    System.out.printf(" %-12s | %-8s | %-8s | %-30s | %-6s%n",
                            h.getDiaSemana().getDescricao(), h.getHoraInicio(), h.getHoraFim(), ucNome, h.getSala());
                }
                System.out.println(GetCyanBold() + "────────────────────────────────────────────────────────────────────────" + GetReset());
                System.out.println(GetWhiteBold() + " Total: " + horarios.size() + " horário(s)" + GetReset());
            }

            String aviso = hCtrl.validarMinHorasSemanaisUCs(anoAtual.getId());
            if (!aviso.isEmpty())
                System.out.println(GetYellow() + "\nAvisos:\n" + aviso + GetReset());
        } catch (Exception e) {
            System.out.println(GetRed() + "Erro: " + e.getMessage() + GetReset());
        }
        MenuUtils.pressionarEnter(scanner);
    }

    private void eliminarHorario() {
        try {
            controller.AnoLetivoController alCtrl = new controller.AnoLetivoController();
            model.AnoLetivo anoAtual = alCtrl.obterOuCriarAnoAtual();
            if (anoAtual == null) {
                System.out.println(GetRed() + "Modo SQL necessário." + GetReset());
                MenuUtils.pressionarEnter(scanner); return;
            }

            HorarioController hCtrl = new HorarioController();
            List<model.Horario> horarios = hCtrl.listarHorariosPorAnoLetivo(anoAtual.getId());
            if (horarios.isEmpty()) {
                System.out.println(GetYellow() + "Sem horários para eliminar." + GetReset());
                MenuUtils.pressionarEnter(scanner); return;
            }

            System.out.println(GetWhiteBold() + "\nHorários disponíveis:" + GetReset());
            System.out.println(GetCyanBold() + "  ──────────────────────────────────────────────────────" + GetReset());
            for (int i = 0; i < horarios.size(); i++) {
                model.Horario h = horarios.get(i);
                String ucNome = h.getUnidadeCurricular() != null ? h.getUnidadeCurricular().getNome() : "?";
                System.out.printf("  %d. %-12s  %s–%s  %-25s  Sala: %s%n",
                        i+1, h.getDiaSemana().getDescricao(), h.getHoraInicio(), h.getHoraFim(), ucNome, h.getSala());
            }
            System.out.println(GetCyanBold() + "  ──────────────────────────────────────────────────────" + GetReset());

            int idx = -1;
            while (idx < 1 || idx > horarios.size()) {
                try {
                    String op = BackendUtils.lerInputString(scanner, "\nEscolha o horário a eliminar: ");
                    idx = Integer.parseInt(op);
                    if (idx < 1 || idx > horarios.size()) {
                        System.out.println(GetRed() + "Opção inválida." + GetReset());
                        idx = -1;
                    }
                } catch (NumberFormatException ex) {
                    System.out.println(GetRed() + "Introduza um número válido." + GetReset());
                }
            }

            model.Resultado<String> res = hCtrl.eliminarHorario(horarios.get(idx - 1).getId());
            System.out.println(res.sucesso
                    ? GetGreen() + res.dados + GetReset()
                    : GetRed() + res.mensagemErro + GetReset());
        } catch (Exception e) {
            System.out.println(GetRed() + "Erro: " + e.getMessage() + GetReset());
        }
        MenuUtils.pressionarEnter(scanner);
    }

    // ══════════════════════════════════════════════════════════════
    //  v1.3 — JUSTIFICAÇÕES DE FALTA
    // ══════════════════════════════════════════════════════════════
    private void exibirMenuJustificacoes() {
        String opcao;
        do {
            try {
                ArrayList<String> opcoes = new ArrayList<>();
                opcoes.add("1. Ver Justificações Pendentes");
                opcoes.add("2. Aprovar Justificação");
                opcoes.add("3. Rejeitar Justificação");
                opcoes.add("4. Ver Todas as Justificações");
                opcoes.add("0. Voltar");

                MenuUtils.exibirSubTitulo("GESTOR > JUSTIFICAÇÕES DE FALTA", opcoes);
                System.out.print("\n" + GetWhiteBold() + "Selecione uma opção: " + GetReset());
                opcao = scanner.nextLine().trim();

                JustificacaoFaltaController jCtrl = new JustificacaoFaltaController();
                switch (opcao) {
                    case "1": listarJustificacoesPendentes(jCtrl); break;
                    case "2": decidirJustificacao(jCtrl, true); break;
                    case "3": decidirJustificacao(jCtrl, false); break;
                    case "4": listarTodasJustificacoes(jCtrl); break;
                    case "0": return;
                    default:
                        System.out.println(GetRed() + "Opção inválida. Escolha entre 0 e 4." + GetReset());
                        MenuUtils.pressionarEnter(scanner);
                }
            } catch (Exception e) {
                System.out.println(GetRed() + "Erro: " + e.getMessage() + GetReset());
                MenuUtils.pressionarEnter(scanner);
            }
        } while (true);
    }

    private void listarJustificacoesPendentes(JustificacaoFaltaController jCtrl) {
        System.out.println(GetBlue() + "\n--- JUSTIFICAÇÕES PENDENTES ---" + GetReset());
        List<model.JustificacaoFalta> pendentes = jCtrl.listarPendentes();

        System.out.println(GetCyanBold() + "────────────────────────────────────────────────────────────────" + GetReset());
        System.out.printf(GetWhiteBold() + " %-5s | %-8s | %-20s | %-20s%n" + GetReset(),
                "ID", "MEC", "TIPO", "DATA");
        System.out.println(GetCyanBold() + "────────────────────────────────────────────────────────────────" + GetReset());

        if (pendentes.isEmpty()) {
            System.out.println(GetGreen() + " Não há justificações pendentes." + GetReset());
        } else {
            for (model.JustificacaoFalta j : pendentes) {
                System.out.printf(" %-5d | %-8d | %-20s | %-20s%n",
                        j.getId(), j.getEstudante() != null ? j.getEstudante().getNumeroMec() : 0,
                        j.getTipo() != null ? j.getTipo().getDescricao() : "?",
                        j.getDataSubmissao() != null ? j.getDataSubmissao().toString() : "?");
                if (j.getDescricao() != null && !j.getDescricao().isEmpty())
                    System.out.println("         Motivo: " + j.getDescricao());
            }
        }
        System.out.println(GetCyanBold() + "────────────────────────────────────────────────────────────────" + GetReset());
        MenuUtils.pressionarEnter(scanner);
    }

    private void decidirJustificacao(JustificacaoFaltaController jCtrl, boolean aprovar) {
        try {
            List<model.JustificacaoFalta> pendentes = jCtrl.listarPendentes();
            if (pendentes.isEmpty()) {
                System.out.println(GetYellow() + "Não há justificações pendentes." + GetReset());
                MenuUtils.pressionarEnter(scanner); return;
            }

            System.out.println(GetWhiteBold() + "\nJustificações pendentes:" + GetReset());
            System.out.println(GetCyanBold() + "  ──────────────────────────────────────────────────────" + GetReset());
            pendentes.forEach(j -> System.out.printf("  [ID:%-3d] Mec:%-8d  %s  — %s%n",
                    j.getId(), j.getEstudante() != null ? j.getEstudante().getNumeroMec() : 0,
                    j.getTipo() != null ? j.getTipo().getDescricao() : "?",
                    j.getDescricao() != null ? j.getDescricao() : ""));
            System.out.println(GetCyanBold() + "  ──────────────────────────────────────────────────────" + GetReset());

            int id = -1;
            while (id < 0) {
                try {
                    String idStr = BackendUtils.lerInputString(scanner, "\nID da justificação: ");
                    int candidato = Integer.parseInt(idStr);
                    boolean existe = pendentes.stream().anyMatch(j -> j.getId() == candidato);
                    if (!existe) {
                        System.out.println(GetRed() + "ID não encontrado nas pendentes." + GetReset());
                    } else {
                        id = candidato;
                    }
                } catch (NumberFormatException ex) {
                    System.out.println(GetRed() + "Introduza um número válido." + GetReset());
                }
            }

            System.out.print("Observação (Enter para deixar em branco): ");
            String obs = scanner.nextLine().trim();

            final int idFinal = id;
            model.Resultado<model.JustificacaoFalta> res = aprovar
                    ? jCtrl.aprovarJustificacao(idFinal, obs)
                    : jCtrl.rejeitarJustificacao(idFinal, obs);
            System.out.println(res.sucesso
                    ? GetGreen() + "Justificação " + (aprovar ? "aprovada" : "rejeitada") + " com sucesso." + GetReset()
                    : GetRed() + res.mensagemErro + GetReset());
        } catch (Exception e) {
            System.out.println(GetRed() + "Erro: " + e.getMessage() + GetReset());
        }
        MenuUtils.pressionarEnter(scanner);
    }

    private void listarTodasJustificacoes(JustificacaoFaltaController jCtrl) {
        System.out.println(GetBlue() + "\n--- TODAS AS JUSTIFICAÇÕES ---" + GetReset());
        List<model.JustificacaoFalta> todas = jCtrl.listarTodas();

        System.out.println(GetCyanBold() + "────────────────────────────────────────────────────────────────" + GetReset());
        System.out.printf(GetWhiteBold() + " %-5s | %-8s | %-12s | %-20s | %-10s%n" + GetReset(),
                "ID", "MEC", "ESTADO", "TIPO", "DATA");
        System.out.println(GetCyanBold() + "────────────────────────────────────────────────────────────────" + GetReset());

        if (todas.isEmpty()) {
            System.out.println(GetYellow() + " Sem justificações registadas." + GetReset());
        } else {
            for (model.JustificacaoFalta j : todas) {
                String corEstado;
                if (j.getEstado() == model.JustificacaoFalta.Estado.APROVADA)       corEstado = GetGreen();
                else if (j.getEstado() == model.JustificacaoFalta.Estado.REJEITADA) corEstado = GetRed();
                else                                                                  corEstado = GetYellow();
                System.out.printf(" %-5d | %-8d | %s%-12s%s | %-20s | %-10s%n",
                        j.getId(), j.getEstudante() != null ? j.getEstudante().getNumeroMec() : 0,
                        corEstado, j.getEstado(), GetReset(),
                        j.getTipo() != null ? j.getTipo().getDescricao() : "?",
                        j.getDataSubmissao() != null ? j.getDataSubmissao().toString() : "?");
            }
        }
        System.out.println(GetCyanBold() + "────────────────────────────────────────────────────────────────" + GetReset());
        if (!todas.isEmpty())
            System.out.println(GetWhiteBold() + " Total: " + todas.size() + " justificação(ões)" + GetReset());
        MenuUtils.pressionarEnter(scanner);
    }

    // ══════════════════════════════════════════════════════════════
    //  v1.3 — ESTATUTOS DE ESTUDANTE
    // ══════════════════════════════════════════════════════════════
    private void exibirMenuEstatutos() {
        String opcao;
        do {
            try {
                ArrayList<String> opcoes = new ArrayList<>();
                opcoes.add("1. Criar Tipo de Estatuto");
                opcoes.add("2. Listar Tipos de Estatuto");
                opcoes.add("3. Atribuir Estatuto a Estudante");
                opcoes.add("4. Ver Estatutos de Estudante");
                opcoes.add("5. Remover Estatuto de Estudante");
                opcoes.add("6. Eliminar Tipo de Estatuto");
                opcoes.add("0. Voltar");

                MenuUtils.exibirSubTitulo("GESTOR > ESTATUTOS DE ESTUDANTE", opcoes);
                System.out.print("\n" + GetWhiteBold() + "Selecione uma opção: " + GetReset());
                opcao = scanner.nextLine().trim();

                EstatutoController eCtrl = new EstatutoController();
                switch (opcao) {
                    case "1": criarTipoEstatuto(eCtrl); break;
                    case "2": listarTiposEstatuto(eCtrl); break;
                    case "3": atribuirEstatuto(eCtrl); break;
                    case "4": verEstatutosEstudante(eCtrl); break;
                    case "5": removerEstatuto(eCtrl); break;
                    case "6": eliminarTipoEstatuto(eCtrl); break;
                    case "0": return;
                    default:
                        System.out.println(GetRed() + "Opção inválida. Escolha entre 0 e 6." + GetReset());
                        MenuUtils.pressionarEnter(scanner);
                }
            } catch (Exception e) {
                System.out.println(GetRed() + "Erro: " + e.getMessage() + GetReset());
                MenuUtils.pressionarEnter(scanner);
            }
        } while (true);
    }

    private void criarTipoEstatuto(EstatutoController eCtrl) {
        try {
            System.out.println(GetBlue() + "\n--- CRIAR TIPO DE ESTATUTO ---" + GetReset());
            String nome = BackendUtils.lerInputString(scanner, "Nome do estatuto (0 para cancelar): ");
            if (nome.equals("0")) return;
            String desc = BackendUtils.lerInputString(scanner, "Descrição: ");
            model.Resultado<model.TipoEstatuto> res = eCtrl.registarTipoEstatuto(nome, desc);
            System.out.println(res.sucesso ? GetGreen() + "Tipo criado: " + res.dados + GetReset() : GetRed() + res.mensagemErro + GetReset());
        } catch (Exception e) { System.out.println(GetRed() + "Erro: " + e.getMessage() + GetReset()); }
        MenuUtils.pressionarEnter(scanner);
    }

    private void listarTiposEstatuto(EstatutoController eCtrl) {
        System.out.println(GetBlue() + "\n--- TIPOS DE ESTATUTO ---" + GetReset());
        List<model.TipoEstatuto> tipos = eCtrl.listarTiposEstatuto();

        System.out.println(GetCyanBold() + "────────────────────────────────────────────────────────────────" + GetReset());
        System.out.printf(GetWhiteBold() + " %-5s | %-20s | %-35s%n" + GetReset(), "ID", "NOME", "DESCRIÇÃO");
        System.out.println(GetCyanBold() + "────────────────────────────────────────────────────────────────" + GetReset());
        if (tipos.isEmpty()) {
            System.out.println(GetYellow() + " Nenhum tipo criado ainda." + GetReset());
        } else {
            tipos.forEach(t -> System.out.printf(" %-5d | %-20s | %-35s%n",
                    t.getId(), t.getNome(), t.getDescricao() != null ? t.getDescricao() : ""));
        }
        System.out.println(GetCyanBold() + "────────────────────────────────────────────────────────────────" + GetReset());
        MenuUtils.pressionarEnter(scanner);
    }

    private void atribuirEstatuto(EstatutoController eCtrl) {
        try {
            System.out.println(GetBlue() + "\n--- ATRIBUIR ESTATUTO ---" + GetReset());
            List<model.TipoEstatuto> tipos = eCtrl.listarTiposEstatuto();
            if (tipos.isEmpty()) {
                System.out.println(GetYellow() + "Crie primeiro um tipo de estatuto." + GetReset());
                MenuUtils.pressionarEnter(scanner); return;
            }

            int mec = -1;
            while (mec <= 0) {
                try {
                    String mecStr = BackendUtils.lerInputString(scanner, "Nº Mecanográfico do estudante: ");
                    mec = Integer.parseInt(mecStr);
                    if (mec <= 0) { System.out.println(GetRed() + "Número inválido." + GetReset()); mec = -1; }
                } catch (NumberFormatException ex) {
                    System.out.println(GetRed() + "Introduza um número válido." + GetReset());
                }
            }

            System.out.println(GetWhiteBold() + "\nTipos disponíveis:" + GetReset());
            tipos.forEach(t -> System.out.printf("  [%d] %s — %s%n", t.getId(), t.getNome(),
                    t.getDescricao() != null ? t.getDescricao() : ""));

            int tipoId = -1;
            while (tipoId < 0) {
                try {
                    String tipoStr = BackendUtils.lerInputString(scanner, "ID do tipo de estatuto: ");
                    tipoId = Integer.parseInt(tipoStr);
                    final int tid = tipoId;
                    if (tipos.stream().noneMatch(t -> t.getId() == tid)) {
                        System.out.println(GetRed() + "ID não encontrado." + GetReset());
                        tipoId = -1;
                    }
                } catch (NumberFormatException ex) {
                    System.out.println(GetRed() + "Introduza um número válido." + GetReset());
                }
            }

            LocalDate inicio = null;
            while (inicio == null) {
                try {
                    String s = BackendUtils.lerInputString(scanner, "Data de início (AAAA-MM-DD): ");
                    inicio = LocalDate.parse(s);
                } catch (Exception ex) {
                    System.out.println(GetRed() + "Data inválida. Use o formato AAAA-MM-DD." + GetReset());
                }
            }

            LocalDate fim = null;
            boolean fimValido = false;
            while (!fimValido) {
                try {
                    System.out.print("Data de fim (AAAA-MM-DD ou Enter para sem fim): ");
                    String fimStr = scanner.nextLine().trim();
                    if (fimStr.isEmpty() || fimStr.equalsIgnoreCase("sem fim")) { fimValido = true; }
                    else { fim = LocalDate.parse(fimStr); fimValido = true; }
                } catch (Exception ex) {
                    System.out.println(GetRed() + "Data inválida. Use AAAA-MM-DD ou deixe em branco." + GetReset());
                }
            }

            model.Resultado<model.EstatutoEstudante> res = eCtrl.atribuirEstatuto(mec, tipoId, inicio, fim);
            System.out.println(res.sucesso
                    ? GetGreen() + "Estatuto atribuído com sucesso." + GetReset()
                    : GetRed() + res.mensagemErro + GetReset());
        } catch (Exception e) {
            System.out.println(GetRed() + "Erro: " + e.getMessage() + GetReset());
        }
        MenuUtils.pressionarEnter(scanner);
    }

    private void verEstatutosEstudante(EstatutoController eCtrl) {
        try {
            String mecStr = BackendUtils.lerInputString(scanner, "Nº Mecanográfico (0 para cancelar): ");
            if (mecStr.equals("0")) return;
            int mec = Integer.parseInt(mecStr);
            List<model.EstatutoEstudante> estatutos = eCtrl.listarEstatutosPorEstudante(mec);
            if (estatutos.isEmpty()) System.out.println(GetYellow() + "Este estudante não possui estatutos." + GetReset());
            else estatutos.forEach(e -> System.out.println("[ID:" + e.getId() + "] " + e + (e.isAtivo() ? GetGreen() + " [ATIVO]" + GetReset() : GetRed() + " [EXPIRADO]" + GetReset())));
        } catch (Exception e) { System.out.println(GetRed() + "Erro: " + e.getMessage() + GetReset()); }
        MenuUtils.pressionarEnter(scanner);
    }

    private void removerEstatuto(EstatutoController eCtrl) {
        try {
            String idStr = BackendUtils.lerInputString(scanner, "ID do estatuto a remover (0 para cancelar): ");
            if (idStr.equals("0")) return;
            model.Resultado<String> res = eCtrl.removerEstatuto(Integer.parseInt(idStr));
            System.out.println(res.sucesso ? GetGreen() + res.dados + GetReset() : GetRed() + res.mensagemErro + GetReset());
        } catch (Exception e) { System.out.println(GetRed() + "Erro: " + e.getMessage() + GetReset()); }
        MenuUtils.pressionarEnter(scanner);
    }

    private void eliminarTipoEstatuto(EstatutoController eCtrl) {
        try {
            listarTiposEstatuto(eCtrl);
            String idStr = BackendUtils.lerInputString(scanner, "ID do tipo a eliminar (0 para cancelar): ");
            if (idStr.equals("0")) return;
            model.Resultado<String> res = eCtrl.eliminarTipoEstatuto(Integer.parseInt(idStr));
            System.out.println(res.sucesso ? GetGreen() + res.dados + GetReset() : GetRed() + res.mensagemErro + GetReset());
        } catch (Exception e) { System.out.println(GetRed() + "Erro: " + e.getMessage() + GetReset()); }
        MenuUtils.pressionarEnter(scanner);
    }
}