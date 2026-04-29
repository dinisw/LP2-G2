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

    public static int lerInteiroSeguro(Scanner scanner, String mensagem) {
        while (true) {
            System.out.print(mensagem);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Entrada invalida. Por favor, introduza um numero inteiro.");
            }
        }
    }
    // --- LEITURA SEGURA DE DECIMAIS (Notas, Propinas, Precos) ---
    public static double lerDoubleSeguro(Scanner scanner, String mensagem) {
        while (true) {
            System.out.print(mensagem);
            try {
                // Substitui virgula por ponto para evitar erros regionais comuns em Portugal
                String input = scanner.nextLine().trim().replace(",", ".");
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                System.out.println("Entrada invalida. Por favor, introduza um valor decimal (ex: 10.5 ou 10,5).");
            }
        }
    }

    // --- LEITURA SEGURA DE DATAS (Datas de Nascimento) ---
    public static LocalDate lerDataSegura(Scanner scanner, String mensagem) {
        while (true) {
            System.out.print(mensagem);
            try {
                String input = scanner.nextLine().trim();
                return LocalDate.parse(input);
            } catch (DateTimeParseException e) {
                System.out.println("Formato de data invalido. Por favor, use o formato AAAA-MM-DD (ex: 2000-12-31).");
            }
        }
    }
}
