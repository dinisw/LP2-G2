package view;

import common.exceptions.CancelarRegistoException;
import common.utils.BackendUtils;
import common.utils.DesignUtils;
import common.utils.MenuUtils;
import common.utils.SenhaUtils;
import DAL.CursoCRUD;
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
import static common.utils.DesignUtils.*;

public class GestorView {
    private final GestorController gestorController;
    private final EstudanteController estudanteController;
    private final DocenteController docenteController;
    private final UnidadeCurricularView unidadeCurricularView;
    private final DepartamentoView departamentoView;
    private final CursoView cursoView;
    private final Scanner scanner;

    public GestorView() {
        this.gestorController = new GestorController();
        this.estudanteController = new EstudanteController();
        this.docenteController = new DocenteController();
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
        opcoes.add("7. Consultar Alunos em Dívida (Tesouraria)"); // NOVA OPÇÃO
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
                    case "7": // NOVO CASE
                        consultarAlunosEmDivida();
                        break;
                    case "0":
                        System.out.println(GetYellow() + "\nA efetuar logout..." + GetReset());
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
        ArrayList<String> opcoes = new ArrayList<>();
        opcoes.add("1. Registar Gestor");
        opcoes.add("2. Listar Gestores");
        opcoes.add("3. Procurar Gestor (NIF)");
        opcoes.add("4. Atualizar Gestor (NIF)");
        opcoes.add("5. Eliminar Gestor (NIF)");
        opcoes.add("0. Voltar ao Menu Principal");

        do {
            MenuUtils.exibirSubTitulo("PORTAL GESTOR > MENU PRINCIPAL > GESTORES", opcoes);
            System.out.print("\n" + DesignUtils.GetWhiteBold() + "Selecione uma opção: " + DesignUtils.GetReset());
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
                case "0":
                    return;
                default:
                    System.out.println("Opção inválida!");
                    MenuUtils.pressionarEnter(scanner);
            }
        } while (true);
    }

    private void registarGestor() {
        try {
            Terminal terminal = TerminalBuilder.terminal();
            LineReader reader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .build();

            System.out.println(GetBlue() + "\n--- REGISTO DE GESTOR ---" + GetReset());
            System.out.println(GetYellow() + "[Digite '0' a qualquer momento para cancelar a operação!]" + GetReset());

            String nome = BackendUtils.lerInputString(scanner, "Nome: ");
            String morada = BackendUtils.lerInputString(scanner, "Morada: ");

            int nif = 0;
            boolean nifValido = false;
            while (!nifValido) {
                try {
                    String nifString = BackendUtils.lerInputString(scanner, "NIF: ");
                    nif = Integer.parseInt(nifString);
                    nifValido = BackendUtils.nifEValido(nifString);
                    if (!nifValido) {
                        System.out.println(GetRed() + "NIF deve ser um número inteiro válido e conter 9 dígitos. Tente novamente." + GetReset());
                    }else{
                        var nifExiste = BackendUtils.nifExiste(nif);
                        if(nifExiste)
                            System.out.println(GetRed() + "NIF já existente no sistema. Tente com outro NIF." + GetReset());
                        nifValido = false;
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
                    if(Period.between(dataNascimento, LocalDate.now()).getYears() >= 18){
                        System.out.println(GetRed() + "Sistem só permite pessoas maiores de 18 anos. Tente novamente." + GetReset());
                    }else {
                        break;
                    }
                } catch (DateTimeParseException e) {
                    System.out.println(GetRed() + "Data deve estar no formato AAAA-MM-DD. Tente novamente." + GetReset());
                }
            }

            String email = "";
            boolean emailValido = false;
            while (!emailValido) {
                email = BackendUtils.lerInputString(scanner, "Email: ");
                emailValido = BackendUtils.emailISSMFGestorValido(email);
                if(!emailValido)
                    System.out.println(GetRed() + "EMAIL deve ter o formato xxxx.gestor@issmf.ipp.pt. Tente novamente." + GetReset());
            }

            String passDigitada = "";
            boolean senhaValida = false;
            while (!senhaValida) {
                passDigitada = reader.readLine("Senha: ", '*');
                if (passDigitada.equals("0")) {
                    throw new CancelarRegistoException("Operação cancelada pelo utilizador.");
                }
                senhaValida = BackendUtils.isSenhaValida(passDigitada);
                if(!senhaValida)
                    System.out.println(GetRed() + "SENHA deve conter pelo menos uma letra maiúscula, um número e um caracter especial. Tente novamente." + GetReset());
            }

            SenhaUtils su = new SenhaUtils();
            String hash = su.gerarHashComSalt(passDigitada);

            String cargo = BackendUtils.lerInputString(scanner, "Cargo: ");

            GestorController gestorControllerAtualizado = new GestorController();
            Resultado resultado = gestorControllerAtualizado.registarGestor(nome, morada, nif, dataNascimento, email, hash, cargo);

            if (resultado.success) {
                System.out.println(GetGreen() + "\nGestor registado com sucesso!" + GetReset());
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

    private void listarGestores() {
        try {
            System.out.println(GetBlue() + "\n--- LISTA DE GESTORES ---" + GetReset());

            GestorController gestorControllerAtualizado = new GestorController();
            List<Gestor> gestores = gestorControllerAtualizado.listarGestores();

            if (gestores.isEmpty()) {
                System.out.println(GetYellow() + "Nenhum gestor registado no sistema." + GetReset());
            } else {
                for (Gestor gestor : gestores) {
                    System.out.println("NIF: " + gestor.getNif() + " | Nome: " + gestor.getNome() + " | Cargo: " + gestor.getCargo());
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
                    String op = BackendUtils.lerInputString(scanner, "\nDigite a opção do gestor a atualizar: ");
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

            System.out.println(GetGreen() + "\nDados atuais:" + GetReset());
            System.out.println(gestor.toString());
            System.out.println(GetYellow() + "\n[Pressione ENTER nos campos que deseja manter iguais]" + GetReset());

            String nome = BackendUtils.lerInputString(scanner, "Novo Nome: ");
            if (!nome.isEmpty()) gestor.setNome(nome);

            String morada = BackendUtils.lerInputString(scanner, "Nova Morada: ");
            if (!morada.isEmpty()) gestor.setMorada(morada);

            String email = "";
            boolean emailValido = false;
            while (!emailValido) {
                email = BackendUtils.lerInputString(scanner, "Novo Email: ");
                if (email.isEmpty()) {
                    emailValido = true;
                } else {
                    emailValido = BackendUtils.emailISSMFGestorValido(email);
                    if(!emailValido) {
                        System.out.println(GetRed() + "EMAIL deve ter o formato xxxx.gestor@issmf.ipp.pt. Tente novamente." + GetReset());
                    }
                }
            }
            if (!email.isEmpty()) gestor.setEmail(email);

            String cargo = BackendUtils.lerInputString(scanner, "Novo Cargo: ");
            if (!cargo.isEmpty()) gestor.setCargo(cargo);

            Resultado resultado = gestorControllerAtualizado.atualizarGestor(gestor.getNif(), nome.isEmpty() ? null : nome, morada.isEmpty() ? null : morada, null, email.isEmpty() ? null : email, cargo.isEmpty() ? null : cargo);

            if (resultado.success) {
                System.out.println(GetGreen() + "\nGestor atualizado com sucesso!" + GetReset());
            } else {
                System.out.println(GetRed() + "\nErro ao atualizar gestor: " + resultado.errorMessage + GetReset());
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
            Resultado resultado = gestorControllerAtualizado.eliminarGestor(gestor.getNif());

            if (resultado.success) {
                System.out.println(GetGreen() + "\nGestor eliminado com sucesso!" + GetReset());
            } else {
                System.out.println(GetRed() + "\nErro ao eliminar: " + resultado.errorMessage + GetReset());
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
        ArrayList<String> opcoes = new ArrayList<>();
        opcoes.add("1. Registar Docente");
        opcoes.add("2. Listar Docentes");
        opcoes.add("3. Procurar Docente (NIF)");
        opcoes.add("4. Atualizar Docente (NIF)");
        opcoes.add("5. Alterar Password (NIF)");
        opcoes.add("6. Eliminar Docente (NIF)");
        opcoes.add("0. Voltar ao Menu Principal");

        do {
            try {
                MenuUtils.exibirSubTitulo("PORTAL GESTOR > MENU PRINCIPAL > DOCENTES", opcoes);
                System.out.print("\n" + GetWhiteBold() + "Selecione uma opção: " + GetReset());
                opcao = scanner.nextLine().trim();

                switch (opcao) {
                    case "1":
                        registarDocente();
                        break;
                    case "2":
                        listarDocentes();
                        break;
                    case "3":
                        procurarDocente();
                        break;
                    case "4":
                        atualizarDocente();
                        break;
                    case "5":
                        alterarPasswordDocente();
                        break;
                    case "6":
                        eliminarDocente();
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

    private void registarDocente() {
        try {
            System.out.println(GetBlue() + "\n--- REGISTO DE DOCENTE ---" + GetReset());
            System.out.println(GetYellow() + "[Digite '0' a qualquer momento para cancelar a operação!]" + GetReset());

            String nome = BackendUtils.lerInputString(scanner, "Nome: ");
            String morada = BackendUtils.lerInputString(scanner, "Morada: ");

            int nif = 0;
            boolean nifValido = false;
            while (!nifValido) {
                try {
                    String nifString = BackendUtils.lerInputString(scanner, "NIF: ");
                    nif = Integer.parseInt(nifString);
                    nifValido = BackendUtils.nifEValido(nifString);
                    if (!nifValido) {
                        System.out.println(GetRed() + "NIF deve ser um número inteiro válido e conter 9 dígitos. Tente novamente." + GetReset());
                    }else{
                        var nifExiste = BackendUtils.nifExiste(nif);
                        if(nifExiste)
                            System.out.println(GetRed() + "NIF já existente no sistema. Tente com outro NIF." + GetReset());
                        nifValido = false;
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
                    if(Period.between(dataNascimento, LocalDate.now()).getYears() >= 18){
                        System.out.println(GetRed() + "Sistem só permite pessoas maiores de 18 anos. Tente novamente." + GetReset());
                    }else {
                        break;
                    }
                } catch (DateTimeParseException e) {
                    System.out.println(GetRed() + "Data deve estar no formato AAAA-MM-DD. Tente novamente." + GetReset());
                }
            }

            System.out.println(GetYellow() + "\nA gerar credenciais e a tentar enviar o email..." + GetReset());
            String passDigitada = SenhaUtils.gerarPalavraPasseAleatoria();
            SenhaUtils su = new SenhaUtils();
            String hash = su.gerarHashComSalt(passDigitada);
            String sigla = nome.length() >= 3 ? nome.substring(0, 3).toUpperCase() : nome.toUpperCase();
            String email = sigla.toLowerCase() + "@issmf.ipp.pt";

            EmailService es = new EmailService();
            String corpoEmail = "-- Credenciais Geradas Automaticamente --\n" +
                    "Sigla: " + sigla + "\n" +
                    "Email: " + email + "\n" +
                    "Palavra-passe: " + passDigitada;

            var resEmail = es.enviarEmailRegisto(email, corpoEmail, TipoDeUtilizador.DOCENTE);

            if(resEmail.success){
                System.out.println(GetGreen() + "Email com as credenciais de acesso enviado com sucesso!" + GetReset());
            }else{
                System.out.println(GetRed() + "Falha ao enviar email: " + resEmail.errorMessage + GetReset());
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

            DocenteController docenteControllerAtualizado = new DocenteController();
            Resultado res = docenteControllerAtualizado.registarDocente(nome, morada, nif, dataNascimento, email, hash, sigla, nomesUC);

            if (res.success) {
                System.out.println(GetGreen() + "\nDocente registado com sucesso!" + GetReset());

                // Exibir avisos de UCs não encontradas, caso existam
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

    private void listarDocentes() {
        try {
            System.out.println(GetBlue() + "\n--- LISTA DE DOCENTES ---" + GetReset());

            DocenteController docenteControllerAtualizado = new DocenteController();
            List<model.Docente> docentes = docenteControllerAtualizado.listarDocentes();

            if (docentes.isEmpty()) {
                System.out.println(GetYellow() + "Nenhum docente registado." + GetReset());
            } else {
                for (model.Docente docente : docentes) {
                    System.out.println("NIF: " + docente.getNif() + " | Nome: " + docente.getNome() + " | Sigla: " + docente.getSigla());
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
            List<model.Docente> listaDocentes = docenteControllerAtualizado.listarDocentes();
            if (listaDocentes.isEmpty()) {
                System.out.println(GetYellow() + "Não há docentes registados no sistema." + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }

            System.out.println("\n" + GetWhiteBold() + "Docentes Disponíveis:" + GetReset());
            for (int i = 0; i < listaDocentes.size(); i++) {
                model.Docente d = listaDocentes.get(i);
                System.out.printf("%d - %s (Sigla: %s | NIF: %d)\n", i + 1, d.getNome(), d.getSigla(), d.getNif());
            }

            int escolha = -1;
            while (escolha < 0 || escolha > listaDocentes.size()) {
                try {
                    String op = BackendUtils.lerInputString(scanner, "\nDigite a opção desejada: ");
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

            model.Docente docente = listaDocentes.get(escolha - 1);
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
            List<model.Docente> listaDocentes = docenteControllerAtualizado.listarDocentes();
            if (listaDocentes.isEmpty()) {
                System.out.println(GetYellow() + "Não há docentes registados no sistema." + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }

            System.out.println("\n" + GetWhiteBold() + "Docentes Disponíveis:" + GetReset());
            for (int i = 0; i < listaDocentes.size(); i++) {
                model.Docente d = listaDocentes.get(i);
                System.out.printf("%d - %s (Sigla: %s | NIF: %d)\n", i + 1, d.getNome(), d.getSigla(), d.getNif());
            }

            int escolha = -1;
            while (escolha < 0 || escolha > listaDocentes.size()) {
                try {
                    String op = BackendUtils.lerInputString(scanner, "\nDigite a opção do docente a atualizar: ");
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

            model.Docente docente = listaDocentes.get(escolha - 1);

            System.out.println(GetGreen() + "\nDados atuais:" + GetReset());
            System.out.println(docente.toString());
            System.out.println(GetYellow() + "\n[Pressione ENTER nos campos que deseja manter iguais]" + GetReset());

            String nome = BackendUtils.lerInputString(scanner, "Novo Nome: ");
            if (!nome.isEmpty()) docente.setNome(nome);

            String morada = BackendUtils.lerInputString(scanner, "Nova Morada: ");
            if (!morada.isEmpty()) docente.setMorada(morada);

            String email = BackendUtils.lerInputString(scanner, "Novo Email: ");
            if (!email.isEmpty()) docente.setEmail(email);

            String sigla = BackendUtils.lerInputString(scanner, "Nova Sigla: ");
            if (!sigla.isEmpty()) docente.setSigla(sigla);

            Resultado resultado = docenteControllerAtualizado.atualizarDocente(docente.getNif(), nome.isEmpty() ? null : nome, morada.isEmpty() ? null : morada, null, email.isEmpty() ? null : email);

            if (resultado.success) {
                System.out.println(GetGreen() + "\nDocente atualizado com sucesso!" + GetReset());
            } else {
                System.out.println(GetRed() + "\nErro ao atualizar docente: " + resultado.errorMessage + GetReset());
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
            List<model.Docente> listaDocentes = docenteControllerAtualizado.listarDocentes();
            if (listaDocentes.isEmpty()) {
                System.out.println(GetYellow() + "Não há docentes registados no sistema." + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }

            System.out.println("\n" + GetWhiteBold() + "Docentes Disponíveis:" + GetReset());
            for (int i = 0; i < listaDocentes.size(); i++) {
                model.Docente d = listaDocentes.get(i);
                System.out.printf("%d - %s (Sigla: %s | NIF: %d)\n", i + 1, d.getNome(), d.getSigla(), d.getNif());
            }

            int escolha = -1;
            while (escolha < 0 || escolha > listaDocentes.size()) {
                try {
                    String op = BackendUtils.lerInputString(scanner, "\nDigite a opção do docente: ");
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

            model.Docente docente = listaDocentes.get(escolha - 1);

            Terminal terminal = TerminalBuilder.terminal();
            LineReader reader = LineReaderBuilder.builder().terminal(terminal).build();

            String novaPass = "";
            boolean senhaValida = false;
            while (!senhaValida) {
                novaPass = reader.readLine("Nova senha: ", '*');
                if (novaPass.equals("0")) throw new CancelarRegistoException("Operação cancelada pelo utilizador.");
                senhaValida = BackendUtils.isSenhaValida(novaPass);
                if(!senhaValida) {
                    System.out.println(GetRed() + "SENHA deve conter pelo menos uma letra maiúscula, um número e um caracter especial. Tente novamente." + GetReset());
                }
            }

            SenhaUtils su = new SenhaUtils();
            String novoHash = su.gerarHashComSalt(novaPass);

            Resultado resultado = docenteControllerAtualizado.alterarPassword(docente.getNif(), novoHash);

            if (resultado.success) {
                System.out.println(GetGreen() + "\nPassword alterada com sucesso!" + GetReset());
            } else {
                System.out.println(GetRed() + "\nErro ao alterar password: " + resultado.errorMessage + GetReset());
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
            List<model.Docente> listaDocentes = docenteControllerAtualizado.listarDocentes();
            if (listaDocentes.isEmpty()) {
                System.out.println(GetYellow() + "Não há docentes registados no sistema." + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }

            System.out.println("\n" + GetWhiteBold() + "Docentes Disponíveis:" + GetReset());
            for (int i = 0; i < listaDocentes.size(); i++) {
                model.Docente d = listaDocentes.get(i);
                System.out.printf("%d - %s (Sigla: %s | NIF: %d)\n", i + 1, d.getNome(), d.getSigla(), d.getNif());
            }

            int escolha = -1;
            while (escolha < 0 || escolha > listaDocentes.size()) {
                try {
                    String op = BackendUtils.lerInputString(scanner, "\nDigite a opção do docente a eliminar: ");
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

            model.Docente docente = listaDocentes.get(escolha - 1);
            Resultado resultado = docenteControllerAtualizado.eliminarDocente(docente.getNif());

            if (resultado.success) {
                System.out.println(GetGreen() + "\nDocente eliminado com sucesso!" + GetReset());
            } else {
                System.out.println(GetRed() + "\nErro ao eliminar: " + resultado.errorMessage + GetReset());
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
        ArrayList<String> opcoes = new ArrayList<>();
        opcoes.add("1. Registar Estudante");
        opcoes.add("2. Listar Estudantes");
        opcoes.add("3. Procurar Estudante (Número Mec)");
        opcoes.add("4. Atualizar Estudante (Número Mec)");
        opcoes.add("5. Eliminar Estudante (Número Mec)");
        opcoes.add("0. Voltar");

        do {
            try {
                MenuUtils.exibirSubTitulo("PORTAL GESTOR > MENU PRINCIPAL > ESTUDANTES", opcoes);
                System.out.print("\n" + GetWhiteBold() + "Selecione uma opção: " + GetReset());
                opcao = scanner.nextLine().trim();

                switch (opcao) {
                    case "1":
                        registarEstudante();
                        break;
                    case "2":
                        listarEstudantes();
                        break;
                    case "3":
                        procurarEstudante();
                        break;
                    case "4":
                        atualizarEstudante();
                        break;
                    case "5":
                        eliminarEstudante();
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

    private void registarEstudante() {
        try {
            System.out.println(GetBlue() + "\n--- REGISTO DE ESTUDANTE ---" + GetReset());
            System.out.println(GetYellow() + "[Digite '0' a qualquer momento para cancelar a operação!]" + GetReset());

            String nome = BackendUtils.lerInputString(scanner, "Nome: ");
            String morada = BackendUtils.lerInputString(scanner, "Morada: ");

            int nif = 0;
            boolean nifValido = false;
            while (!nifValido) {
                try {
                    String nifString = BackendUtils.lerInputString(scanner, "NIF: ");
                    nif = Integer.parseInt(nifString);

                    nifValido = BackendUtils.nifEValido(nifString);
                    if (!nifValido) {
                        System.out.println(GetRed() + "NIF deve ser um número inteiro válido e conter 9 dígitos. Tente novamente." + GetReset());
                    }else{
                        var nifExiste = BackendUtils.nifExiste(nif);
                        if(nifExiste) {
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
                    if(Period.between(dataNascimento, LocalDate.now()).getYears() >= 18){
                        System.out.println(GetRed() + "Sistem só permite pessoas maiores de 18 anos. Tente novamente." + GetReset());
                    }else {
                        break;
                    }
                } catch (DateTimeParseException e) {
                    System.out.println(GetRed() + "Data deve estar no formato AAAA-MM-DD. Tente novamente." + GetReset());
                }
            }

            CursoCRUD cc = new CursoCRUD();
            List<Curso> cursos = cc.getCursos();

            if (cursos.isEmpty()) {
                System.out.println(GetYellow() + "\nAviso: Não existem cursos registados no sistema. Crie um curso primeiro." + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
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

            if (resEmail.success) {
                System.out.println(GetGreen() + "Email com as credenciais de acesso enviado com sucesso!" + GetReset());
            } else {
                System.out.println(GetRed() + "Falha ao enviar email: " + resEmail.errorMessage + GetReset());
                System.out.println(GetRed() + "O registo foi abortado para evitar inconsistência de credenciais." + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }

            Resultado resultado = estudanteControllerAtualizado.registarEstudante(nome, morada, nif, dataNascimento, cursoNomeSelecionado, senha);

            if (resultado.success) {
                System.out.println(GetGreen() + "\nEstudante registado com sucesso!" + GetReset());
                System.out.println("Nº Mecanográfico atribuído: " + mecAuto);
            } else {
                System.out.println(GetRed() + "\nErro ao registar estudante: " + resultado.errorMessage + GetReset());
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
            List<model.Estudante> lista = estudanteControllerAtualizado.listarEstudantes();

            if (lista.isEmpty()) {
                System.out.println(GetYellow() + "Nenhum estudante registado no sistema." + GetReset());
            } else {
                System.out.println(GetCyanBold() + "--------------------------------------------------------------------------------" + GetReset());
                System.out.printf(GetWhiteBold() + " %-15s | %-30s | %-25s \n" + GetReset(), "Nº MEC", "NOME", "CURSO");
                System.out.println(GetCyanBold() + "--------------------------------------------------------------------------------" + GetReset());
                for (model.Estudante e : lista) {
                    System.out.printf(" %-15d | %-30s | %-25s \n", e.getNumeroMec(), e.getNome(), e.getNomeCurso());
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
            List<model.Estudante> listaEstudantes = estudanteControllerAtualizado.listarEstudantes();
            if (listaEstudantes.isEmpty()) {
                System.out.println(GetYellow() + "Não há estudantes registados no sistema." + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }

            System.out.println("\n" + GetWhiteBold() + "Estudantes Disponíveis:" + GetReset());
            for (int i = 0; i < listaEstudantes.size(); i++) {
                model.Estudante e = listaEstudantes.get(i);
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

            model.Estudante estudante = listaEstudantes.get(escolha - 1);
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
            List<model.Estudante> listaEstudantes = estudanteControllerAtualizado.listarEstudantes();
            if (listaEstudantes.isEmpty()) {
                System.out.println(GetYellow() + "Não há estudantes registados no sistema." + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }

            System.out.println("\n" + GetWhiteBold() + "Estudantes Disponíveis:" + GetReset());
            for (int i = 0; i < listaEstudantes.size(); i++) {
                model.Estudante e = listaEstudantes.get(i);
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

            model.Estudante estudante = listaEstudantes.get(escolha - 1);

            System.out.println(GetGreen() + "\nDados atuais:" + GetReset());
            System.out.println(estudante.toString());
            System.out.println(GetYellow() + "\n[Pressione ENTER nos campos que deseja manter iguais]" + GetReset());

            String nomeAt = BackendUtils.lerInputString(scanner, "Novo Nome: ");
            if (!nomeAt.isEmpty()) estudante.setNome(nomeAt);

            String moradaAt = BackendUtils.lerInputString(scanner, "Nova Morada: ");
            if (!moradaAt.isEmpty()) estudante.setMorada(moradaAt);

            String emailAt = BackendUtils.lerInputString(scanner, "Novo Email: ");
            if (!emailAt.isEmpty()) estudante.setEmail(emailAt);

            String cursoAt = BackendUtils.lerInputString(scanner, "Novo Curso: ");
            if (!cursoAt.isEmpty()) estudante.setNomeCurso(cursoAt);

            Resultado resultado = estudanteControllerAtualizado.atualizarEstudante(estudante.getNumeroMec(), nomeAt.isEmpty() ? null : nomeAt, moradaAt.isEmpty() ? null : moradaAt, emailAt.isEmpty() ? null : emailAt, cursoAt.isEmpty() ? null : cursoAt);

            if (resultado.success) {
                System.out.println(GetGreen() + "\nEstudante atualizado com sucesso!" + GetReset());
            } else {
                System.out.println(GetRed() + "\nErro ao atualizar estudante: " + resultado.errorMessage + GetReset());
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
            List<model.Estudante> listaEstudantes = estudanteControllerAtualizado.listarEstudantes();
            if (listaEstudantes.isEmpty()) {
                System.out.println(GetYellow() + "Não há estudantes registados no sistema." + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }

            System.out.println("\n" + GetWhiteBold() + "Estudantes Disponíveis:" + GetReset());
            for (int i = 0; i < listaEstudantes.size(); i++) {
                model.Estudante e = listaEstudantes.get(i);
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

            model.Estudante estudanteAapagar = listaEstudantes.get(escolha - 1);

            System.out.print(GetYellow() + "\nAVISO: Tem a certeza que deseja eliminar o estudante " + estudanteAapagar.getNome() + "? (S/N): " + GetReset());
            String confirmacao = scanner.nextLine().trim();

            if (confirmacao.equalsIgnoreCase("S")) {
                Resultado resultado = estudanteControllerAtualizado.eliminarEstudante(estudanteAapagar.getNumeroMec());

                if (resultado.success) {
                    System.out.println(GetGreen() + "\nEstudante eliminado com sucesso!" + GetReset());
                } else {
                    System.out.println(GetRed() + "\nErro ao eliminar: " + resultado.errorMessage + GetReset());
                }
            } else {
                System.out.println(GetBlue() + "\nOperação de eliminação cancelada pelo utilizador." + GetReset());
            }

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

    // NOVO MÉTODO DE TESOURARIA PARA O GESTOR
    private void consultarAlunosEmDivida() {
        try {
            System.out.println(GetBlue() + "\n--- TESOURARIA: ALUNOS EM DÍVIDA ---" + GetReset());

            PropinaController propinaController = new PropinaController();
            List<Estudante> devedores = propinaController.obterAlunosEmDivida();

            if (devedores.isEmpty()) {
                System.out.println(GetGreen() + "\nExcelente notícia! Não há nenhum estudante com propinas em atraso no sistema." + GetReset());
            } else {
                System.out.println(GetYellow() + "\nLista de Estudantes com pagamentos pendentes:" + GetReset());
                System.out.println(GetCyanBold() + "--------------------------------------------------------------------------------" + GetReset());
                System.out.printf(GetWhiteBold() + " %-15s | %-30s | %-25s \n" + GetReset(), "Nº MEC", "NOME", "CURSO");
                System.out.println(GetCyanBold() + "--------------------------------------------------------------------------------" + GetReset());

                for (Estudante e : devedores) {
                    System.out.printf(" %-15d | %-30s | %-25s \n", e.getNumeroMec(), e.getNome(), e.getNomeCurso());
                }
                System.out.println(GetCyanBold() + "--------------------------------------------------------------------------------" + GetReset());
            }

            MenuUtils.pressionarEnter(scanner);

        } catch (Exception e) {
            System.out.println(GetRed() + "Ocorreu um erro ao carregar a lista de tesouraria: " + e.getMessage() + GetReset());
            MenuUtils.pressionarEnter(scanner);
        }
    }
}