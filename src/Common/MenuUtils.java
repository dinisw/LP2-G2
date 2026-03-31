package Common;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Pattern;

import static Common.DesignUtils.*;

public class MenuUtils {
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
    public  static void exibirSubTitulo(String titulo, ArrayList<String> opcoes){
        System.out.println("\033[H\033[2J");
        System.out.flush();

        String bordaSuperior = "╔" + "═".repeat(LARGURA) + "╗";
        String bordaMeio = "╠" + "═".repeat(LARGURA) + "╣";
        String bordaInferior = "╚" + "═".repeat(LARGURA) + "╝";

        int paddingTitulo = (LARGURA - titulo.length()) / 2;
        String textoTitulo = String.format("%" + paddingTitulo + "s%s%-" + paddingTitulo + "s", "", titulo, "");
        if (textoTitulo.length() < LARGURA) textoTitulo += " ";

        System.out.println(CYAN_BOLD + bordaSuperior + RESET);
        System.out.println(CYAN_BOLD + "║" + WHITE_BOLD + textoTitulo + CYAN_BOLD + "║" + RESET);
        System.out.println(CYAN_BOLD + bordaMeio + RESET);

        for (String opcao2 : opcoes) {
            System.out.println(CYAN_BOLD + "║ " + RESET + String.format("%-" + (LARGURA - 1) + "s", opcao2) + CYAN_BOLD + "║" + RESET);
        }
        System.out.println(CYAN_BOLD + bordaInferior + RESET);
    }
    public static void pressionarEnter(Scanner ler) {
        System.out.print("\nPressione " + WHITE_BOLD + "ENTER" + RESET + " para continuar...");
        ler.nextLine();
    }
    //endregion

    //region LimparTela
    public static void limparTela() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
    //endregion
}
