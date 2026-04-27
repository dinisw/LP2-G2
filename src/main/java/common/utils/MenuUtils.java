package common.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;

import static common.utils.DesignUtils.*;

public class MenuUtils {
    //region MENU
    public static void exibirTitulo() {
        System.out.print("\033[H\033[2J");
        System.out.flush();

        String titulo = "SISTEMA DE GESTÃO ISSMF";
        String dataHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        int paddingTitulo = (GetLargura() - titulo.length()) / 2;
        String textoTitulo = String.format("%" + paddingTitulo + "s%s%-" + paddingTitulo + "s", "", titulo, "");

        if (textoTitulo.length() < GetLargura()) textoTitulo += " ";

        String statusLabel = "Status: ";
        String statusValor = "ONLINE";
        String statusCompleto = statusLabel + statusValor;

        int espacoDisponivel = GetLargura() - statusCompleto.length() - dataHora.length() - 1;
        String paddingData = " ".repeat(Math.max(0, espacoDisponivel));

        System.out.println(GetCyanBold() + GetBordaSuperior() + GetReset());
        System.out.println(GetCyanBold() + "║" + GetWhiteBold() + textoTitulo + GetCyanBold() + "║" + GetReset());
        System.out.println(GetCyanBold() + GetBordaMeio() + GetReset());
        System.out.println(GetCyanBold() + "║" + GetReset() + statusLabel + GetGreen() + statusValor + GetReset() + paddingData + GetBlue() + dataHora + " " + GetCyanBold() + "║" + GetReset());
        System.out.println(GetCyanBold() + GetBordaInferior() + GetReset());
    }
    public  static void exibirSubTitulo(String titulo, ArrayList<String> opcoes){
        System.out.println("\033[H\033[2J");
        System.out.flush();

        int paddingTitulo = (GetLargura() - titulo.length()) / 2;
        String textoTitulo = String.format("%" + paddingTitulo + "s%s%-" + paddingTitulo + "s", "", titulo, "");
        if (textoTitulo.length() < GetLargura()) textoTitulo += " ";

        System.out.println(GetCyanBold() + GetBordaSuperior() + GetReset());
        System.out.println(GetCyanBold() + "║" + GetWhiteBold() + textoTitulo + GetCyanBold() + "║" + GetReset());
        System.out.println(GetCyanBold() + GetBordaMeio() + GetReset());

        for (String opcao2 : opcoes) {
            System.out.println(GetCyanBold() + "║ " + GetReset() + String.format("%-" + (GetLargura() - 1) + "s", opcao2) + GetCyanBold() + "║" + GetReset());
        }
        System.out.println(GetCyanBold() + GetBordaInferior() + GetReset());
    }
    public static void pressionarEnter(Scanner ler) {
        System.out.print("\nPressione " + GetWhiteBold() + "ENTER" + GetReset() + " para continuar...");
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
