package view;

import controller.PessoaController;
import model.Pessoa;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class LoginView {
    // CRIAR O LOGIN E O REGISTRAR AQUI, QUANDO DEVOLVE SUCESSO VAI PARA AS OUTRAS VIEWS, SE DEVOLVER ERRO, PERMITE TENTAR DE NOVO

    //region Design
    public static final String RESET = "\033[0m";
    public static final String CYAN_BOLD = "\033[1;36m";
    public static final String WHITE_BOLD = "\033[1;37m";
    public static final String BLUE = "\033[0;34m";
    public static final String GREEN = "\033[0;32m";
    public static final String RED = "\033[0;31m";
    public static final String YELLOW = "\033[0;33m";
    private static final int LARGURA = 84;
    //endregion
    //region MENU
    public static void exibirTitulo() {
        System.out.print("\033[H\033[2J");
        System.out.flush();

        String bordaSuperior = "╔" + "═".repeat(LARGURA) + "╗";
        String bordaMeio = "╠" + "═".repeat(LARGURA) + "╣";
        String bordaInferior = "╚" + "═".repeat(LARGURA) + "╝";

        String titulo = "SISTEMA DE GESTÃO ISSMF";
        String dataHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        int paddingTitulo = (LARGURA - titulo.length()) / 2;
        String textoTitulo = String.format("%" + paddingTitulo + "s%s%-" + paddingTitulo + "s", "", titulo, "");

        if (textoTitulo.length() < LARGURA) textoTitulo += " ";

        String statusLabel = "Status: ";
        String statusValor = "ONLINE";
        String statusCompleto = statusLabel + statusValor;

        int espacoDisponivel = LARGURA - statusCompleto.length() - dataHora.length() - 1;
        String paddingData = " ".repeat(Math.max(0, espacoDisponivel));

        System.out.println(CYAN_BOLD + bordaSuperior + RESET);
        System.out.println(CYAN_BOLD + "║" + WHITE_BOLD + textoTitulo + CYAN_BOLD + "║" + RESET);
        System.out.println(CYAN_BOLD + bordaMeio + RESET);
        System.out.println(CYAN_BOLD + "║" + RESET + statusLabel + GREEN + statusValor + RESET + paddingData + BLUE + dataHora + " " + CYAN_BOLD + "║" + RESET);
        System.out.println(CYAN_BOLD + bordaInferior + RESET);
    }
    //endregion
    public static void Login(){
        Scanner ler = new Scanner(System.in);
        exibirTitulo();
        System.out.println("Digite a opção desejada");
        var a = ler.nextLine();
    }


    // Exemplo de uso da estrutura MVC
//    Pessoa modelo = new Pessoa("Gonçalo", "Rua X", 123456789, LocalDate.of(2000, 1, 1), "goncalo@email.com", "G", 123) {};
//    PessoaView vista = new PessoaView();
//    PessoaController controlador = new PessoaController(modelo, vista);
//
//        controlador.atualizarView();
//
//    // Atualizar dados através do controlador
//        controlador.setNome("Gonçalo Silva");
//        controlador.atualizarView();
}
