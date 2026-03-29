package view;

import Common.DesignUtils;
import Common.MenuUtils;
import Common.SenhaUtils;
import DAL.DocenteCRUD;
import model.Docente;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class DocenteView {
    private final DocenteCRUD docenteCRUD;
    private final Scanner scanner;

    public DocenteView() {
        this.docenteCRUD = new DocenteCRUD();
        this.scanner = new Scanner(System.in);
    }

    public void exibirMenuDocentes() {
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
        String salt = SenhaUtils.gerarSalt();
        String passAuto = Common.SenhaUtils.gerarPalavraPasseAleatoria();

        String pass = SenhaUtils.gerarHashComSalt(passAuto, salt);

        String sigla = nome.length() >= 3 ? nome.substring(0, 3).toLowerCase() : nome.toUpperCase();
        String email = sigla + "@isep.ipp.pt";
        
        System.out.println("\n-- Dados Gerados Automaticamente --");
        System.out.println("Sigla: " + sigla);
        System.out.println("Email: " + email);
        System.out.println("Palavra Passe: " + pass);
        System.out.println("------------------------------------");

        Docente novo = new Docente(nome, morada, nif, dataNascimento, email, pass, salt, sigla, null, null);
        if (docenteCRUD.registarDocente(novo)) {
            System.out.println("Docente registado com sucesso!");
        } else {
            System.out.println("Erro ao registar: NIF já existe ou dados inválidos.");
        }
        MenuUtils.pressionarEnter(scanner);
    }

    private void listarDocentes() {
        System.out.println("\n--- LISTA DE DOCENTES ---");
        List<Docente> docentes = docenteCRUD.getDocentes();
        if (docentes.isEmpty()) {
            System.out.println("Nenhum docente registado.");
        } else {
            for (Docente d : docentes) {
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
        Docente d = docenteCRUD.procurarPorNif(nif);
        if (d != null) {
            System.out.println("Dados encontrados: " + d.getNome() + " - " + d.getEmail());
        } else {
            System.out.println("Docente não encontrado.");
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
        if (docenteCRUD.eliminarDocente(nif)) {
            System.out.println("Docente eliminado com sucesso!");
        } else {
            System.out.println("Erro ao eliminar: Docente não encontrado.");
        }
        MenuUtils.pressionarEnter(scanner);
    }

    private void alterarPasswordDocente() {
        System.out.print("\nDigite o NIF do docente para alterar a password: ");
        int nif = 0;
        boolean nifValido = false;
        while (!nifValido) {
            try {
                nif = Integer.parseInt(scanner.nextLine());
                nifValido = true;
            } catch (NumberFormatException e) {
                System.out.println("Aviso: NIF deve ser um número inteiro válido. Tente novamente.");
                System.out.print("Digite o NIF do docente para alterar a password: ");
            }
        }
        
        Docente d = docenteCRUD.procurarPorNif(nif);

        if (d != null) {
            System.out.println("Docente encontrado: " + d.getNome());
            System.out.print("Nova Palavra-passe: ");
            String passDigitada = scanner.nextLine();
            
            if (!passDigitada.isEmpty()) {
                String salt = SenhaUtils.gerarSalt();
                String pass = SenhaUtils.gerarHashComSalt(passDigitada, salt);
                d.setSalt(salt);
                d.setHash(pass);
                
                if (docenteCRUD.atualizarDocente(d)) {
                    System.out.println("Password alterada com sucesso!");
                } else {
                    System.out.println("Erro ao guardar alteração da password.");
                }
            } else {
                System.out.println("Operação cancelada: Password não pode ser vazia.");
            }
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
        Docente d = docenteCRUD.procurarPorNif(nif);

        if (d != null) {
            System.out.println("Dados atuais: " + d.getNome() + " - " + d.getEmail());
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

            if (docenteCRUD.atualizarDocente(d)) {
                System.out.println("Docente atualizado com sucesso!");
            } else {
                System.out.println("Erro ao atualizar docente.");
            }
        } else {
            System.out.println("Docente não encontrado.");
        }
        MenuUtils.pressionarEnter(scanner);
    }
    public void exibirMenuPessoalDocente(Docente docente) {
        String opcao;
        ArrayList<String> opcoes = new ArrayList<>();
        opcoes.add("1. Ver minhas Unidades Curriculares");
        opcoes.add("2. Alterar minha Password");
        opcoes.add("0. Logout");

        do {
            MenuUtils.exibirSubTitulo("MENU DOCENTE: " + docente.getNome(), opcoes);
            System.out.print("\n" + DesignUtils.GetWhiteBold() + "Selecione uma opção: " + DesignUtils.GetReset());
            opcao = scanner.nextLine().trim();

            switch (opcao) {
                case "1":
                    new controller.UnidadeCurricularController().listarUCsPorDocente(docente.getSigla());
                    MenuUtils.pressionarEnter(scanner);
                    break;
                case "2":
                    alterarPasswordPropria(docente);
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Opção inválida!");
                    MenuUtils.pressionarEnter(scanner);
            }
        } while (true);
    }

    private void alterarPasswordPropria(Docente d) {
        System.out.print("Nova Palavra-passe: ");
        String passDigitada = scanner.nextLine();

        if (!passDigitada.isEmpty()) {
            String salt = SenhaUtils.gerarSalt();
            String pass = SenhaUtils.gerarHashComSalt(passDigitada, salt);
            d.setSalt(salt);
            d.setHash(pass);

            if (docenteCRUD.atualizarDocente(d)) {
                System.out.println("Password alterada com sucesso!");
            } else {
                System.out.println("Erro ao guardar alteração da password.");
            }
        }
        MenuUtils.pressionarEnter(scanner);
    }
}
