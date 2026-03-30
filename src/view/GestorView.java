package view;

import Common.DesignUtils;
import Common.MenuUtils;
import Common.SenhaUtils;
import DAL.EstudanteCRUD;
import controller.EstudanteController;
import controller.DocenteController;
import controller.GestorController;
import model.Gestor;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static Common.DesignUtils.*;
import static Common.DesignUtils.GREEN;
import static Common.DesignUtils.RED;
import static Common.DesignUtils.RESET;

public class GestorView {
    private final GestorController gestorController;
    private final EstudanteController estudanteController;
    private final DocenteController docenteController;
    private final Scanner scanner;

    public GestorView() {
        this.gestorController = new GestorController();
        this.estudanteController = new EstudanteController();
        this.docenteController = new DocenteController();
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
        opcoes.add("0. Logout");

        do {
            MenuUtils.exibirSubTitulo("MENU DE GESTÃO ADMINISTRATIVA", opcoes);
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
                    System.out.println("Funcionalidade em desenvolvimento.");
                    MenuUtils.pressionarEnter(scanner);
                    break;
                case "5":
                    System.out.println("Funcionalidade em desenvolvimento.");
                    MenuUtils.pressionarEnter(scanner);
                    break;
                case "6":
                    System.out.println("Funcionalidade em desenvolvimento.");
                    MenuUtils.pressionarEnter(scanner);
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Opção inválida!");
                    MenuUtils.pressionarEnter(scanner);
            }
        } while (true);
    }

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
            MenuUtils.exibirSubTitulo("GESTÃO DE GESTORES", opcoes);
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
        System.out.println("\n--- REGISTO DE GESTOR ---");
        System.out.print("Nome: ");
        String nome = scanner.nextLine();
        System.out.print("Morada: ");
        String morada = scanner.nextLine();
        System.out.print("NIF: ");
        int nif = 0;
        boolean nifValido = false;
        while (!nifValido) {
            try {
                nif = Integer.parseInt(scanner.nextLine());
                nifValido = true;
            } catch (NumberFormatException e) {
                System.out.println("Aviso: NIF deve ser um número inteiro válido. Tente novamente.");
                System.out.print("NIF: ");
            }
        }
        System.out.print("Data de Nascimento (AAAA-MM-DD): ");
        LocalDate dataNascimento = null;
        boolean dataValida = false;
        while (!dataValida) {
            try {
                dataNascimento = LocalDate.parse(scanner.nextLine());
                dataValida = true;
            } catch (Exception e) {
                System.out.println("Aviso: Data deve estar no formato AAAA-MM-DD. Tente novamente.");
                System.out.print("Data de Nascimento (AAAA-MM-DD): ");
            }
        }
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Palavra-passe: ");
        String passDigitada = scanner.nextLine();
        String salt = SenhaUtils.gerarSalt();
        String hash = SenhaUtils.gerarHashComSalt(passDigitada, salt);
        System.out.print("Cargo: ");
        String cargo = scanner.nextLine();

        if (gestorController.registarGestor(nome, morada, nif, dataNascimento, email, hash, salt, cargo)) {
            System.out.println("Gestor registado com sucesso!");
        } else {
            System.out.println("Erro ao registar: NIF já existe ou dados inválidos.");
        }
        MenuUtils.pressionarEnter(scanner);
    }

    private void listarGestores() {
        System.out.println("\n--- LISTA DE GESTORES ---");
        List<Gestor> gestores = gestorController.listarGestores();
        if (gestores.isEmpty()) {
            System.out.println("Nenhum gestor registado.");
        } else {
            for (Gestor g : gestores) {
                System.out.println("NIF: " + g.getNif() + " | Nome: " + g.getNome() + " | Cargo: " + g.getCargo());
            }
        }
        MenuUtils.pressionarEnter(scanner);
    }

    private void procurarGestor() {
        System.out.print("\nDigite o NIF do gestor: ");
        int nif = 0;
        boolean nifValido = false;
        while (!nifValido) {
            try {
                nif = Integer.parseInt(scanner.nextLine());
                nifValido = true;
            } catch (NumberFormatException e) {
                System.out.println("Aviso: NIF deve ser um número inteiro válido. Tente novamente.");
                System.out.print("Digite o NIF do gestor: ");
            }
        }
        Gestor g = gestorController.procurarGestorPorNif(nif);
        if (g != null) {
            System.out.println("Dados encontrados: " + g);
        } else {
            System.out.println("Gestor não encontrado.");
        }
        MenuUtils.pressionarEnter(scanner);
    }

    private void eliminarGestor() {
        System.out.print("\nDigite o NIF do gestor a eliminar: ");
        int nif = 0;
        boolean nifValido = false;
        while (!nifValido) {
            try {
                nif = Integer.parseInt(scanner.nextLine());
                nifValido = true;
            } catch (NumberFormatException e) {
                System.out.println("Aviso: NIF deve ser um número inteiro válido. Tente novamente.");
                System.out.print("Digite o NIF do gestor a eliminar: ");
            }
        }
        if (gestorController.eliminarGestor(nif)) {
            System.out.println("Gestor eliminado com sucesso!");
        } else {
            System.out.println("Erro ao eliminar: Gestor não encontrado.");
        }
        MenuUtils.pressionarEnter(scanner);
    }

    private void atualizarGestor() {
        System.out.print("\nDigite o NIF do gestor a atualizar: ");
        int nif = 0;
        boolean nifValido = false;
        while (!nifValido) {
            try {
                nif = Integer.parseInt(scanner.nextLine());
                nifValido = true;
            } catch (NumberFormatException e) {
                System.out.println("Aviso: NIF deve ser um número inteiro válido. Tente novamente.");
                System.out.print("Digite o NIF do gestor a atualizar: ");
            }
        }
        Gestor g = gestorController.procurarGestorPorNif(nif);

        if (g != null) {
            System.out.println("Dados atuais: " + g);
            System.out.print("Novo Nome (Enter para manter): ");
            String nome = scanner.nextLine();
            if (!nome.isEmpty()) g.setNome(nome);

            System.out.print("Nova Morada (Enter para manter): ");
            String morada = scanner.nextLine();
            if (!morada.isEmpty()) g.setMorada(morada);

            System.out.print("Novo Email (Enter para manter): ");
            String email = scanner.nextLine();
            if (!email.isEmpty()) g.setEmail(email);

            System.out.print("Novo Cargo (Enter para manter): ");
            String cargo = scanner.nextLine();
            if (!cargo.isEmpty()) g.setCargo(cargo);

            if (gestorController.atualizarGestor(nif, nome.isEmpty() ? null : nome, morada.isEmpty() ? null : morada, null, email.isEmpty() ? null : email, cargo.isEmpty() ? null : cargo)) {
                System.out.println("Gestor atualizado com sucesso!");
            } else {
                System.out.println("Erro ao atualizar gestor.");
            }
        } else {
            System.out.println("Gestor não encontrado.");
        }
        MenuUtils.pressionarEnter(scanner);
    }
    
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
            MenuUtils.exibirSubTitulo("GESTÃO DE DOCENTES", opcoes);
            System.out.print("\n" + DesignUtils.GetWhiteBold() + "Selecione uma opção: " + DesignUtils.GetReset());
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
                    return;
                default:
                    System.out.println("Opção inválida!");
                    MenuUtils.pressionarEnter(scanner);
            }
        } while (true);
    }

    private void registarDocente() {
        System.out.println("\n--- REGISTO DE DOCENTE ---");
        System.out.print("Nome: ");
        String nome = scanner.nextLine();
        System.out.print("Morada: ");
        String morada = scanner.nextLine();
        System.out.print("NIF: ");
        int nif = 0;
        boolean nifValido = false;
        while (!nifValido) {
            try {
                nif = Integer.parseInt(scanner.nextLine());
                nifValido = true;
            } catch (NumberFormatException e) {
                System.out.println("Aviso: NIF deve ser um número inteiro válido. Tente novamente.");
                System.out.print("NIF: ");
            }
        }
        System.out.print("Data de Nascimento (AAAA-MM-DD): ");
        LocalDate dataNascimento = null;
        boolean dataValida = false;
        while (!dataValida) {
            try {
                dataNascimento = LocalDate.parse(scanner.nextLine());
                dataValida = true;
            } catch (Exception e) {
                System.out.println("Aviso: Data deve estar no formato AAAA-MM-DD. Tente novamente.");
                System.out.print("Data de Nascimento (AAAA-MM-DD): ");
            }
        }
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Palavra-passe: ");
        String passDigitada = scanner.nextLine();
        String salt = SenhaUtils.gerarSalt();
        String hash = SenhaUtils.gerarHashComSalt(passDigitada, salt);
        System.out.print("Sigla: ");
        String sigla = scanner.nextLine();

        if (docenteController.registarDocente(nome, morada, nif, dataNascimento, email, hash, salt, sigla)) {
            System.out.println("Docente registado com sucesso!");
        } else {
            System.out.println("Erro ao registar: NIF ou sigla já existe ou dados inválidos.");
        }
        MenuUtils.pressionarEnter(scanner);
    }

    private void listarDocentes() {
        System.out.println("\n--- LISTA DE DOCENTES ---");
        List<model.Docente> docentes = docenteController.listarDocentes();
        if (docentes.isEmpty()) {
            System.out.println("Nenhum docente registado.");
        } else {
            for (model.Docente d : docentes) {
                System.out.println("NIF: " + d.getNif() + " | Nome: " + d.getNome() + " | Sigla: " + d.getSigla());
            }
        }
        MenuUtils.pressionarEnter(scanner);
    }

    private void procurarDocente() {
        System.out.print("\nDigite o NIF do docente: ");
        int nif = 0;
        boolean nifValido = false;
        while (!nifValido) {
            try {
                nif = Integer.parseInt(scanner.nextLine());
                nifValido = true;
            } catch (NumberFormatException e) {
                System.out.println("Aviso: NIF deve ser um número inteiro válido. Tente novamente.");
                System.out.print("Digite o NIF do docente: ");
            }
        }
        model.Docente d = docenteController.procurarDocentePorNif(nif);
        if (d != null) {
            System.out.println("Dados encontrados: " + d);
        } else {
            System.out.println("Docente não encontrado.");
        }
        MenuUtils.pressionarEnter(scanner);
    }

    private void atualizarDocente() {
        System.out.print("\nDigite o NIF do docente a atualizar: ");
        int nif = 0;
        boolean nifValido = false;
        while (!nifValido) {
            try {
                nif = Integer.parseInt(scanner.nextLine());
                nifValido = true;
            } catch (NumberFormatException e) {
                System.out.println("Aviso: NIF deve ser um número inteiro válido. Tente novamente.");
                System.out.print("Digite o NIF do docente a atualizar: ");
            }
        }
        model.Docente d = docenteController.procurarDocentePorNif(nif);

        if (d != null) {
            System.out.println("Dados atuais: " + d);
            System.out.print("Novo Nome (Enter para manter): ");
            String nome = scanner.nextLine();
            if (!nome.isEmpty()) d.setNome(nome);

            System.out.print("Nova Morada (Enter para manter): ");
            String morada = scanner.nextLine();
            if (!morada.isEmpty()) d.setMorada(morada);

            System.out.print("Novo Email (Enter para manter): ");
            String email = scanner.nextLine();
            if (!email.isEmpty()) d.setEmail(email);

            System.out.print("Nova Sigla (Enter para manter): ");
            String sigla = scanner.nextLine();
            if (!sigla.isEmpty()) d.setSigla(sigla);

            if (docenteController.atualizarDocente(nif, nome.isEmpty() ? null : nome, morada.isEmpty() ? null : morada, null, email.isEmpty() ? null : email)) {
                System.out.println("Docente atualizado com sucesso!");
            } else {
                System.out.println("Erro ao atualizar docente.");
            }
        } else {
            System.out.println("Docente não encontrado.");
        }
        MenuUtils.pressionarEnter(scanner);
    }

    private void alterarPasswordDocente() {
        System.out.print("\nDigite o NIF do docente: ");
        int nif = 0;
        boolean nifValido = false;
        while (!nifValido) {
            try {
                nif = Integer.parseInt(scanner.nextLine());
                nifValido = true;
            } catch (NumberFormatException e) {
                System.out.println("Aviso: NIF deve ser um número inteiro válido. Tente novamente.");
                System.out.print("Digite o NIF do docente: ");
            }
        }
        System.out.print("Nova palavra-passe: ");
        String novaPass = scanner.nextLine();
        String novoSalt = SenhaUtils.gerarSalt();
        String novoHash = SenhaUtils.gerarHashComSalt(novaPass, novoSalt);

        if (docenteController.alterarPassword(nif, novoHash, novoSalt)) {
            System.out.println("Password alterada com sucesso!");
        } else {
            System.out.println("Erro ao alterar password: Docente não encontrado.");
        }
        MenuUtils.pressionarEnter(scanner);
    }

    private void eliminarDocente() {
        System.out.print("\nDigite o NIF do docente a eliminar: ");
        int nif = 0;
        boolean nifValido = false;
        while (!nifValido) {
            try {
                nif = Integer.parseInt(scanner.nextLine());
                nifValido = true;
            } catch (NumberFormatException e) {
                System.out.println("Aviso: NIF deve ser um número inteiro válido. Tente novamente.");
                System.out.print("Digite o NIF do docente a eliminar: ");
            }
        }
        if (docenteController.eliminarDocente(nif)) {
            System.out.println("Docente eliminado com sucesso!");
        } else {
            System.out.println("Erro ao eliminar: Docente não encontrado.");
        }
        MenuUtils.pressionarEnter(scanner);
    }
    
    private void exibirMenuEstudantes() {
        String opcao = "";
        ArrayList<String> opcoes = new ArrayList<>();
        opcoes.add("1. Registar Estudante");
        opcoes.add("2. Listar Estudantes");
        opcoes.add("3. Procurar Estudante (Número Mec)");
        opcoes.add("4. Atualizar Estudante (Número Mec)");
        opcoes.add("5. Eliminar Estudante (Número Mec)");
        opcoes.add("0. Voltar");

        do {
            MenuUtils.exibirSubTitulo("CRUD - ESTUDANTES", opcoes);
            System.out.print("\n" + WHITE_BOLD + "Selecione uma opção: " + RESET);
            opcao = scanner.nextLine().trim();

            switch (opcao) {
                case "1":
                    System.out.println("\n--- REGISTO DE ESTUDANTE ---");
                    System.out.print("Nome: ");
                    String nome = scanner.nextLine();
                    System.out.print("Morada: ");
                    String morada = scanner.nextLine();
                    System.out.print("NIF: ");
                    int nif = 0;
                    boolean nifValido = false;
                    while (!nifValido) {
                        try {
                            nif = Integer.parseInt(scanner.nextLine());
                            nifValido = true;
                        } catch (NumberFormatException e) {
                            System.out.println("Aviso: NIF deve ser um número inteiro válido. Tente novamente.");
                            System.out.print("NIF: ");
                        }
                    }
                    System.out.print("Data de Nascimento (AAAA-MM-DD): ");
                    LocalDate data = null;
                    boolean dataValida = false;
                    while (!dataValida) {
                        try {
                            data = LocalDate.parse(scanner.nextLine());
                            dataValida = true;
                        } catch (Exception e) {
                            System.out.println("Aviso: Data deve estar no formato AAAA-MM-DD. Tente novamente.");
                            System.out.print("Data de Nascimento (AAAA-MM-DD): ");
                        }
                    }
                    System.out.print("Curso: ");
                    String curso = scanner.nextLine();
                    int mecAuto = estudanteController.gerarNumeroMecanografico();
                    String emailAuto = mecAuto + "@isep.ipp.pt";
                    String salt = SenhaUtils.gerarSalt();
                    String passAuto = SenhaUtils.gerarPalavraPasseAleatoria();

                    String senha = SenhaUtils.gerarHashComSalt(passAuto, salt);

                    System.out.println("\n-- Credenciais Geradas Automaticamente --");
                    System.out.println("Nº Mecanográfico: " + mecAuto);
                    System.out.println("Email do Estudante: " + emailAuto);
                    System.out.println("Palavra-passe: " + passAuto);
                    System.out.println("-----------------------------------------");

                    if (estudanteController.registarEstudante(nome, morada, nif, data, curso)) {
                        System.out.println(GREEN + "Estudante registado com sucesso!" + RESET);
                    } else {
                        System.out.println(RED + "Erro ao registar estudante." + RESET);
                    }
                    MenuUtils.pressionarEnter(scanner);
                    break;
                case "2":
                    System.out.println("\n--- LISTA DE ESTUDANTES ---");
                    List<model.Estudante> lista = estudanteController.listarEstudantes();
                    if (lista.isEmpty()) {
                        System.out.println("Nenhum estudante registado.");
                    } else {
                        for (model.Estudante e : lista) {
                            System.out.println("Mec: " + e.getNumeroMec() + " | Nome: " + e.getNome() + " | Curso: " + e.getNomeCurso());
                        }
                    }
                    MenuUtils.pressionarEnter(scanner);
                    break;
                case "3":
                    System.out.print("\nDigite o Número Mecanográfico: ");
                    int nmec = 0;
                    boolean nmecValido = false;
                    while (!nmecValido) {
                        try {
                            nmec = Integer.parseInt(scanner.nextLine());
                            nmecValido = true;
                        } catch (NumberFormatException e) {
                            System.out.println("Aviso: Número Mecanográfico deve ser um número inteiro válido. Tente novamente.");
                            System.out.print("Digite o Número Mecanográfico: ");
                        }
                    }
                    model.Estudante est = estudanteController.procurarEstudantePorNumeroMec(nmec);
                    if (est != null) {
                        System.out.println("Dados: " + est.getNome() + " - " + est.getNomeCurso());
                    } else {
                        System.out.println(RED + "Estudante não encontrado." + RESET);
                    }
                    MenuUtils.pressionarEnter(scanner);
                    break;
                case "4":
                    System.out.print("\nDigite o Número Mecanográfico a atualizar: ");
                    int amec = Integer.parseInt(scanner.nextLine());
                    model.Estudante eate = estudanteController.procurarEstudantePorNumeroMec(amec);
                    if (eate != null) {
                        System.out.println("Dados atuais: " + eate.getNome() + " - " + eate.getNomeCurso());
                        System.out.print("Novo Nome (Enter para manter): ");
                        String nomeAt = scanner.nextLine();
                        if (!nomeAt.isEmpty()) eate.setNome(nomeAt);

                        System.out.print("Nova Morada (Enter para manter): ");
                        String moradaAt = scanner.nextLine();
                        if (!moradaAt.isEmpty()) eate.setMorada(moradaAt);

                        System.out.print("Novo Email (Enter para manter): ");
                        String emailAt = scanner.nextLine();
                        if (!emailAt.isEmpty()) eate.setEmail(emailAt);

                        System.out.print("Novo Curso (Enter para manter): ");
                        String cursoAt = scanner.nextLine();
                        if (!cursoAt.isEmpty()) eate.setNomeCurso(cursoAt);

                        if (estudanteController.atualizarEstudante(amec, nomeAt.isEmpty() ? null : nomeAt, moradaAt.isEmpty() ? null : moradaAt, emailAt.isEmpty() ? null : emailAt, cursoAt.isEmpty() ? null : cursoAt)) {
                            System.out.println(GREEN + "Estudante atualizado com sucesso!" + RESET);
                        } else {
                            System.out.println(RED + "Erro ao atualizar estudante." + RESET);
                        }
                    } else {
                        System.out.println(RED + "Estudante não encontrado." + RESET);
                    }
                    MenuUtils.pressionarEnter(scanner);
                    break;
                case "5":
                    System.out.print("\nDigite o Número Mecanográfico a eliminar: ");
                    int dmec = Integer.parseInt(scanner.nextLine());
                    if (estudanteController.eliminarEstudante(dmec)) {
                        System.out.println(GREEN + "Estudante eliminado com sucesso!" + RESET);
                    } else {
                        System.out.println(RED + "Erro ao eliminar: Estudante não encontrado." + RESET);
                    }
                    MenuUtils.pressionarEnter(scanner);
                    break;
                case "0":
                    return;
                default:
                    System.out.println(RED + "Opção inválida!" + RESET);
                    MenuUtils.pressionarEnter(scanner);
            }
        } while (!opcao.equals("0"));
    }
}
