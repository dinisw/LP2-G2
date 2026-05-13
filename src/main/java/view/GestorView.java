package view;

import common.exceptions.CancelarRegistoException;
import common.utils.BackendUtils;
import common.utils.MenuUtils;
import common.utils.SenhaUtils;
import controller.*;
import model.*;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import service.EmailService;

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

    public void exibirMenuGestao() {
        String opcao;
        ArrayList<String> opcoes = new ArrayList<>();
        opcoes.add("1. Gerir Gestores");
        opcoes.add("2. Gerir Docentes");
        opcoes.add("3. Gerir Estudantes");
        opcoes.add("4. Gerir Cursos");
        opcoes.add("5. Gerir Departamentos");
        opcoes.add("6. Gerir Unidades Curriculares");
        opcoes.add("7. Consultar Alunos em Dívida (Tesouraria)");
        opcoes.add("8. Simular Passagem de Ano Letivo (Global)");
        opcoes.add("0. Logout");

        do {
            try {
                MenuUtils.exibirSubTitulo("PORTAL GESTOR > MENU PRINCIPAL", opcoes);

                System.out.print("\nSelecione uma opção: ");
                opcao = scanner.nextLine().trim();

                switch (opcao) {
                    case "1":
                        exibirMenuGestores();
                        break;
                    case "2":
                        exibirMenuDocentes();
                        break;
                    case "3":
                        exibirMenuEstudantes();
                        break;
                    case "4":
                        cursoView.exibirMenuCursos();
                        break;
                    case "5":
                        departamentoView.exibirMenuDepartamentos();
                        break;
                    case "6":
                        unidadeCurricularView.exibirMenuUnidadesCurriculares();
                        break;
                    case "7":
                        consultarAlunosEmDivida();
                        break;
                    case "8":
                        simularPassagemDeAno();
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

    //region Gestor
    public void exibirMenuGestores() {
        String opcao;
        do {
            GestorController gc = new GestorController();
            boolean temGestores = !gc.listarGestores().isEmpty();

            ArrayList<String> opcoes = new ArrayList<>();
            opcoes.add("1. Registar Gestor");
            if (temGestores) {
                opcoes.add("2. Listar Gestores");
                opcoes.add("3. Procurar Gestor");
                opcoes.add("4. Atualizar Gestor");
                opcoes.add("5. Eliminar Gestor");
            }
            opcoes.add("0. Voltar ao Menu Principal");

            MenuUtils.limparTela();
            MenuUtils.exibirSubTitulo("PORTAL GESTOR > GESTORES", opcoes);
            System.out.print("\nSelecione uma opção: ");
            opcao = scanner.nextLine().trim();

            switch (opcao) {
                case "1": registarGestor(); break;
                case "2": if (temGestores) listarGestores(); else mostrarErroMenu(); break;
                case "3": if (temGestores) procurarGestor(); else mostrarErroMenu(); break;
                case "4": if (temGestores) atualizarGestor(); else mostrarErroMenu(); break;
                case "5": if (temGestores) eliminarGestor(); else mostrarErroMenu(); break;
                case "0": return;
                default: mostrarErroMenu();
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
                System.out.println("Email: ");
                email = scanner.nextLine().trim();
                if (email.equals("0")) throw new CancelarRegistoException("Operação cancelada pelo utilizador.");
                emailValido = BackendUtils.emailISSMFGestorValido(email);
                if (!emailValido)
                    System.out.println(GetRed() + "EMAIL deve ter o formato xxxx.gestor@issmf.ipp.pt. Tente novamente." + GetReset());
            }

            String passDigitada = "";
            boolean senhaValida = false;
            while (!senhaValida) {
                passDigitada = BackendUtils.lerSenhaOculta("Senha: ", scanner);
                if (passDigitada.equals("0")) {
                    throw new CancelarRegistoException("Operação cancelada pelo utilizador.");
                }
                senhaValida = BackendUtils.isSenhaValida(passDigitada);
                if (!senhaValida) {
                    System.out.println(GetRed() + "SENHA deve conter pelo menos uma letra maiúscula, um número e um caracter especial. Tente novamente." + GetReset());
                }
            }

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

            Resultado<Gestor> resultado = gestorControllerAtualizado.atualizarGestor(gestor.getNif(), novoNome.isEmpty() ? null : novoNome, morada.isEmpty() ? null : morada, null, cargo.isEmpty() ? null : cargo);

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
            DocenteController dc = new DocenteController();
            int counter = 1;

            while (docenteControllerAtualizado.procurarDocentePorSigla(siglaFinal) != null) {

                if (siglaBase.length() >= 3) {
                    siglaFinal = siglaBase.substring(0, 2) + counter;
                } else {
                    siglaFinal = siglaBase + counter;
                }
                counter++;
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
            UnidadeCurricularController unidadeCurricularControllerAtualizado = new UnidadeCurricularController();
            List<UnidadeCurricular> unidadeCurriculars = unidadeCurricularControllerAtualizado.listarTodasUCs();
            List<String> nomesUC = new ArrayList<>();
            if (unidadeCurriculars.isEmpty()) {
                System.out.println(GetYellow() + "Nenhuma UC registada. Docente será registado sem UCs associadas." + GetReset());
            } else {
                for (UnidadeCurricular unidadeCurricular : unidadeCurriculars) {
                    System.out.println("- " + unidadeCurricular.getNome());
                }
                String input = BackendUtils.lerInputString(scanner, "Digite os nomes das UCs a associar (separados por vírgula, ou Enter para nenhuma): ");
                if (!input.isEmpty()) {
                    String[] parts = input.split(",");
                    for (String part : parts) {
                        String nomeUC = part.trim();
                        UnidadeCurricular unidadeCurricularCheck = unidadeCurricularControllerAtualizado.procurarUCPorNome(nomeUC);

                        if (unidadeCurricularCheck != null) {


                            if (unidadeCurricularCheck.getDocente() != null) {
                                String resp = BackendUtils.lerInputString(scanner, GetYellow() + "Aviso: A UC '" + nomeUC + "' já tem o docente " + unidadeCurricularCheck.getDocente().getSigla() + " atribuído. Deseja substituir pelo novo docente? (S/N): " + GetReset());
                                if (resp.equalsIgnoreCase("S")) {
                                    nomesUC.add(nomeUC);
                                } else {
                                    System.out.println(GetYellow() + "Atribuição da UC '" + nomeUC + "' ignorada." + GetReset());
                                }
                            } else {
                                nomesUC.add(nomeUC);
                            }
                        } else {
                            System.out.println(GetRed() + "Erro: A UC '" + nomeUC + "' não existe no sistema." + GetReset());
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
                    System.out.println("ID: " + (i + 1) + " | NIF: " + docente.getNif() + " | Nome: " + docente.getNome() + " | Sigla: " + docente.getSigla());
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

            Terminal terminal = TerminalBuilder.terminal();
            LineReader reader = LineReaderBuilder.builder().terminal(terminal).build();

            String novaPass = "";
            boolean senhaValida = false;
            while (!senhaValida) {
                novaPass = BackendUtils.lerSenhaOculta("Nova senha: ");

                if (novaPass.equals("0")) throw new CancelarRegistoException("Operação cancelada pelo utilizador.");
                senhaValida = BackendUtils.isSenhaValida(novaPass);
                if (!senhaValida) {
                    System.out.println(GetRed() + "SENHA deve conter pelo menos uma letra maiúscula, um número e um caracter especial. Tente novamente." + GetReset());
                }
            }

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
                System.out.println(GetGreen() + "\nDocente eliminado com sucesso!" + GetReset());
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

            DepartamentoController departamentoController = new DepartamentoController();
            List<Departamento> departamentos = departamentoController.listarTodosDepartamentos();
            if (departamentos.isEmpty()) {
                System.out.println(GetYellow() + "\nAviso: Não existem Departamentos registados no sistema." + GetReset());
                System.out.println(GetRed() + "Por favor, vá a 'Gerir Departamentos' e crie um antes de registar estudantes." + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }

            CursoController cursoController = new CursoController();
            List<Curso> cursos = cursoController.listarCursos();

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
                System.out.println(GetCyanBold() + "--------------------------------------------------------------------------------" + GetReset());
                System.out.printf(GetWhiteBold() + " %-5s | %-15s | %-30s | %-25s \n" + GetReset(), "ID", "Nº MEC", "NOME", "CURSO");
                System.out.println(GetCyanBold() + "--------------------------------------------------------------------------------" + GetReset());
                for (int i = 0; i < lista.size(); i++) {
                    Estudante e = lista.get(i);
                    System.out.printf(" %-5d | %-15d | %-30s | %-25s \n", (i + 1), e.getNumeroMec(), e.getNome(), e.getNomeCurso());
                }
                System.out.println(GetCyanBold() + "--------------------------------------------------------------------------------" + GetReset());
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
                CursoController cursoController = new CursoController();
                List<Curso> cursos = cursoController.listarCursos();

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

            Resultado <String> resultado = estudanteControllerAtualizado.eliminarEstudante(estudanteAapagar.getNumeroMec());

            if (resultado.sucesso) {
                System.out.println(GetGreen() + "\nEstudante eliminado com sucesso!" + GetReset());
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

            String novaPass = "";
            boolean senhaValida = false;
            while (!senhaValida) {
                novaPass = BackendUtils.lerSenhaOculta("Nova Senha: ");

                if (novaPass.equals("0")) throw new CancelarRegistoException("Operação cancelada pelo utilizador.");

                senhaValida = BackendUtils.isSenhaValida(novaPass);
                if (!senhaValida) {
                    System.out.println(GetRed() + "SENHA deve conter pelo menos uma letra maiúscula, um número e um caracter especial. Tente novamente." + GetReset());
                }
            }

            common.utils.SenhaUtils su = new common.utils.SenhaUtils();
            String novoHash = su.gerarHashComSalt(novaPass);

            Resultado<Estudante> res = ec.alterarPassword(estudanteSelecionado.getNumeroMec(), novoHash);

            if (res.sucesso) {
                System.out.println(GetGreen() + "\nPassword do estudante alterada com sucesso!" + GetReset());
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
                    double dividaTotal = 0.0;
                    StringBuilder situacaoAnos = new StringBuilder();

                    for (Propina p : propinas) {
                        if (!p.isTotalmentePaga()) {
                            dividaTotal += p.getValorEmDivida();
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
    private void simularPassagemDeAno() {
        System.out.println(GetBlue() + "\n--- SIMULADOR DE PASSAGEM DE ANO LETIVO ---" + GetReset());
        System.out.println(GetYellow() + "Esta ação irá simular a viragem do ano letivo no sistema." + GetReset());
        System.out.println(GetYellow() + "O sistema vai avaliar as notas de todos os alunos, aplicar a regra dos 60%," + GetReset());
        System.out.println(GetYellow() + "verificar dívidas e faturar as propinas do novo ano para quem transitar." + GetReset());

        String confirmacao = BackendUtils.lerInputString(scanner, GetWhiteBold() + "\nDeseja prosseguir com a simulação global? (S/N): " + GetReset());

        if (confirmacao.equalsIgnoreCase("S")) {
            EstudanteController ec = new EstudanteController();
            Resultado<List<String>> res = ec.simularTransicaoAnoLetivoGlobal();

            if (res.sucesso) {
                System.out.println(GetGreen() + "\n====== RELATÓRIO DE TRANSIÇÃO ======" + GetReset());
                for (String log : res.dados) {
                    if(log.contains("[SUCESSO]")) {
                        System.out.println(GetGreen() + log + GetReset());
                    } else {
                        System.out.println(GetCyanBold() + log + GetReset());
                    }
                }
                System.out.println(GetGreen() + "====================================" + GetReset());
                System.out.println(GetWhiteBold() + "Transição concluída. Vá à tesouraria ou às fichas para confirmar o resultado!" + GetReset());
            } else {
                System.out.println(GetRed() + "\nErro ao simular transição: " + res.mensagemErro + GetReset());
            }
        } else {
            System.out.println(GetYellow() + "Simulação cancelada pelo utilizador." + GetReset());
        }
        MenuUtils.pressionarEnter(scanner);
    }
}