package view;

import Common.MenuUtils;
import Common.SenhaUtils;
import model.Estudante;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import controller.EstudanteController;

import static Common.DesignUtils.*;

public class EstudanteView {
    public static void exibirMenu(Estudante estudante){
        Scanner ler = new Scanner(System.in);
        String opcao = "";
        ArrayList<String> opcoes = new ArrayList<>();
        opcoes.add("1. Inscrever em Curso");
        opcoes.add("2. Consultar Ficha de Estudante");
        opcoes.add("3. Verificar Notas de Avaliação");
        opcoes.add("4. Gerir Estudantes (CRUD)");
        opcoes.add("0. Voltar ao Menu Principal");

        do {
            MenuUtils.exibirTitulo();
            MenuUtils.exibirSubTitulo("OPÇÕES ESTUDANTE", opcoes);

            System.out.print("\n" + WHITE_BOLD + "Selecione uma opção: " + RESET);
            opcao = ler.nextLine().trim();

            switch (opcao) {
                case "1":
                    System.out.println("\n" + YELLOW + "[EM MANUTENÇÃO] Esta funcionalidade ainda não está finalizada." + RESET);
                    MenuUtils.pressionarEnter(ler);
                    break;
                case "2":
					consultarFichaEstudante(estudante, ler);
                    break;
                case "3":
                    consultarNotasEstudante(estudante, ler);
                    break;
                case "4":
                    exibirMenuCRUD(ler);
                    break;
                case "0":
                    return;
                default:
                    System.out.println("\n" + RED + "Opção inválida! Tente novamente." + RESET);
                    MenuUtils.pressionarEnter(ler);
            }
        } while (!opcao.equals("0"));
    }
    private static void exibirMenuCRUD(Scanner ler) {
        DAL.EstudanteCRUD crud = new DAL.EstudanteCRUD();
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
            opcao = ler.nextLine().trim();

            switch (opcao) {
                case "1":
                    System.out.println("\n--- REGISTO DE ESTUDANTE ---");
                    System.out.print("Nome: ");
                    String nome = ler.nextLine();
                    System.out.print("Morada: ");
                    String morada = ler.nextLine();
                    System.out.print("NIF: ");
                    int nif = 0;
                    boolean nifValido = false;
                    while (!nifValido) {
                        try {
                            nif = Integer.parseInt(ler.nextLine());
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
                            data = LocalDate.parse(ler.nextLine());
                            dataValida = true;
                        } catch (Exception e) {
                            System.out.println("Aviso: Data deve estar no formato AAAA-MM-DD. Tente novamente.");
                            System.out.print("Data de Nascimento (AAAA-MM-DD): ");
                        }
                    }
                    System.out.print("Curso: ");
                    String curso = ler.nextLine();
                    int mecAuto = crud.gerarNumeroMecanografico();
                    String emailAuto = mecAuto + "@isep.ipp.pt";
                    String salt = SenhaUtils.gerarSalt();
                    String passAuto = Common.SenhaUtils.gerarPalavraPasseAleatoria();

                    String senha = SenhaUtils.gerarHashComSalt(passAuto, salt);

                    System.out.println("\n-- Credenciais Geradas Automaticamente --");
                    System.out.println("Nº Mecanográfico: " + mecAuto);
                    System.out.println("Email do Estudante: " + emailAuto);
                    System.out.println("Palavra-passe: " + passAuto);
                    System.out.println("-----------------------------------------");



                    model.Estudante novo = new model.Estudante(nome, morada, nif, data, emailAuto, mecAuto, passAuto, salt, curso);
                    if (crud.registarEstudante(novo)) {
                        System.out.println(GREEN + "Estudante registado com sucesso!" + RESET);
                    } else {
                        System.out.println(RED + "Erro ao registar estudante." + RESET);
                    }
                    MenuUtils.pressionarEnter(ler);
                    break;
                case "2":
                    System.out.println("\n--- LISTA DE ESTUDANTES ---");
                    List<model.Estudante> lista = crud.getEstudantes();
                    if (lista.isEmpty()) {
                        System.out.println("Nenhum estudante registado.");
                    } else {
                        for (model.Estudante e : lista) {
                            System.out.println("Mec: " + e.getNumeroMec() + " | Nome: " + e.getNome() + " | Curso: " + e.getNomeCurso());
                        }
                    }
                    MenuUtils.pressionarEnter(ler);
                    break;
                case "3":
                    System.out.print("\nDigite o Número Mecanográfico: ");
                    int nmec = 0;
                    boolean nmecValido = false;
                    while (!nmecValido) {
                        try {
                            nmec = Integer.parseInt(ler.nextLine());
                            nmecValido = true;
                        } catch (NumberFormatException e) {
                            System.out.println("Aviso: Número Mecanográfico deve ser um número inteiro válido. Tente novamente.");
                            System.out.print("Digite o Número Mecanográfico: ");
                        }
                    }
                    model.Estudante est = crud.lerEstudante(nmec);
                    if (est != null) {
                        System.out.println("Dados: " + est.getNome() + " - " + est.getNomeCurso());
                    } else {
                        System.out.println(RED + "Estudante não encontrado." + RESET);
                    }
                    MenuUtils.pressionarEnter(ler);
                    break;
                case "4":
                    System.out.print("\nDigite o Número Mecanográfico a atualizar: ");
                    int amec = Integer.parseInt(ler.nextLine());
                    model.Estudante eate = crud.lerEstudante(amec);
                    if (eate != null) {
                        System.out.println("Dados atuais: " + eate.getNome() + " - " + eate.getNomeCurso());
                        System.out.print("Novo Nome (Enter para manter): ");
                        String nomeAt = ler.nextLine();
                        if (!nomeAt.isEmpty()) eate.setNome(nomeAt);

                        System.out.print("Nova Morada (Enter para manter): ");
                        String moradaAt = ler.nextLine();
                        if (!moradaAt.isEmpty()) eate.setMorada(moradaAt);

                        System.out.print("Novo Email (Enter para manter): ");
                        String emailAt = ler.nextLine();
                        if (!emailAt.isEmpty()) eate.setEmail(emailAt);

                        System.out.print("Novo Curso (Enter para manter): ");
                        String cursoAt = ler.nextLine();
                        if (!cursoAt.isEmpty()) eate.setNomeCurso(cursoAt);

                        if (crud.atualizarEstudante(eate)) {
                            System.out.println(GREEN + "Estudante atualizado com sucesso!" + RESET);
                        } else {
                            System.out.println(RED + "Erro ao atualizar estudante." + RESET);
                        }
                    } else {
                        System.out.println(RED + "Estudante não encontrado." + RESET);
                    }
                    MenuUtils.pressionarEnter(ler);
                    break;
                case "5":
                    System.out.print("\nDigite o Número Mecanográfico a eliminar: ");
                    int dmec = Integer.parseInt(ler.nextLine());
                    if (crud.eliminarEstudante(dmec)) {
                        System.out.println(GREEN + "Estudante eliminado com sucesso!" + RESET);
                    } else {
                        System.out.println(RED + "Erro ao eliminar: Estudante não encontrado." + RESET);
                    }
                    MenuUtils.pressionarEnter(ler);
                    break;
                case "0":
                    return;
                default:
                    System.out.println(RED + "Opção inválida!" + RESET);
                    MenuUtils.pressionarEnter(ler);
            }
        } while (!opcao.equals("0"));
    }

    public static void inscreverEmCurso(Scanner ler) {
//        Curso c = new Curso();
        String opcao = "";

        do {
//            var opcoes = c.pegarCursos();
//            menu.exibirSubTitulo("CURSOS", opcoes);
//            System.out.println(CYAN_BOLD + bordaInferior + RESET);
//
//            System.out.print("\n" + WHITE_BOLD + "Selecione uma opção: " + RESET);
//            opcao = ler.nextLine().trim();
        }while (!opcao.equals("0"));
    }

    public static void consultarFichaEstudante(Estudante estudante, Scanner ler) {

        System.out.println(CYAN_BOLD + bordaSuperior + RESET);
        System.out.println(CYAN_BOLD + "║" + WHITE_BOLD + "            CONSULTAR FICHA DE ESTUDANTE            " + CYAN_BOLD + "║" + RESET);
        System.out.println(CYAN_BOLD + bordaInferior + RESET);

        EstudanteView ev = new EstudanteView();
        EstudanteController controller = new EstudanteController(estudante, ev);
        System.out.println(controller.exibirFichaEstudante());
        MenuUtils.pressionarEnter(ler);
    }

    public static void consultarNotasEstudante(Estudante estudante, Scanner ler) {
        DAL.AvaliacaoCRUD avaliacaoCRUD = new DAL.AvaliacaoCRUD();
        List<model.Avaliacao> minhasNotas = avaliacaoCRUD.listarPorEstudante(estudante.getNumeroMec());

        System.out.println("\033[H\033[2J");
        System.out.println(CYAN_BOLD + bordaSuperior + RESET);
        System.out.println(CYAN_BOLD + "║" + WHITE_BOLD + "                 PAUTA DE AVALIAÇÕES                  " + CYAN_BOLD + "║" + RESET);
        System.out.println(CYAN_BOLD + bordaMeio + RESET);
        System.out.printf(CYAN_BOLD + "║" + RESET + " %-20s | %-15s | %-10s | %-20s " + CYAN_BOLD + "║\n" + RESET, "Disciplina", "Época", "Nota", "Estado");
        System.out.println(CYAN_BOLD + bordaMeio + RESET);

        if (minhasNotas.isEmpty()) {
            System.out.println(CYAN_BOLD + "║" + YELLOW + " Ainda não existem notas registadas no seu perfil.  " + CYAN_BOLD + "║" + RESET);
        } else {
            double soma = 0;
            int count = 0;

            for (model.Avaliacao avaliacao : minhasNotas) {
                String notaStr;
                String estado;

                if (avaliacao.getNota() == null) {
                    notaStr = "-";
                    estado = YELLOW + "A Aguardar" + RESET;
                } else {
                    notaStr = String.format("%.2f", avaliacao.getNota());
                    soma += avaliacao.getNota();
                    count++;

                    if (avaliacao.getNota() >= 9.5) {
                        estado = GREEN + "Aprovado" + RESET;
                    } else {
                        estado = RED + "Reprovado" + RESET;
                    }
                }

                System.out.printf(CYAN_BOLD + "║" + RESET + " %-20s | %-15s | %-10s | %-29s " + CYAN_BOLD + "║\n" + RESET,
                        avaliacao.getUnidadeCurricular().getNome(), avaliacao.getMomento(), notaStr, estado);
            }

            System.out.println(CYAN_BOLD + bordaMeio + RESET);
            if (count > 0) {
                double media = soma / count;
                System.out.printf(CYAN_BOLD + "║" + WHITE_BOLD + " MÉDIA ATUAL DO CURSO: %-46.2f" + CYAN_BOLD + "║\n" + RESET, media);
            }
        }
        System.out.println(CYAN_BOLD + bordaInferior + RESET);
        Common.MenuUtils.pressionarEnter(ler);
    }
}
