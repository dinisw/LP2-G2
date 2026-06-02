package view;

import DAL.DAOFactory;
import common.utils.MenuUtils;

import java.util.ArrayList;
import java.util.Scanner;

import static common.utils.DesignUtils.*;

public class SelecionarModoView {

    public static void selecionar() {
        Scanner scanner = new Scanner(System.in);

        ArrayList<String> opcoes = new ArrayList<>();
        opcoes.add("");
        opcoes.add("  [1]  Ficheiros CSV  " + getTag("LOCAL", GetGreen()));
        opcoes.add("       Utiliza ficheiros locais .csv como base de dados.");
        opcoes.add("");
        opcoes.add("  [2]  Base de Dados SQL Server  " + getTag("REMOTO", GetBlue()));
        opcoes.add("       Liga ao servidor ISSMF via SQL Server.");
        opcoes.add("");

        while (true) {
            MenuUtils.exibirSubTitulo("MODO DE ARMAZENAMENTO", opcoes);

            System.out.print("\n" + GetWhiteBold() + "Opção: " + GetReset());
            String escolha = scanner.nextLine().trim();

            switch (escolha) {
                case "1" -> {
                    DAOFactory.setModo("CSV");
                    MenuUtils.limparTela();
                    System.out.println("\n" + GetCyanBold() + GetBordaSuperior() + GetReset());
                    System.out.println(GetCyanBold() + "║ " + GetReset()
                            + GetGreen() + "✔  Modo CSV selecionado." + GetReset()
                            + " Os dados serão lidos dos ficheiros locais."
                            + String.format("%" + (GetLargura() - 57) + "s", "")
                            + GetCyanBold() + "║" + GetReset());
                    System.out.println(GetCyanBold() + GetBordaInferior() + GetReset());
                    aguardar(800);
                    return;
                }
                case "2" -> {
                    DAOFactory.setModo("SQL");
                    MenuUtils.limparTela();
                    System.out.println("\n" + GetCyanBold() + GetBordaSuperior() + GetReset());
                    System.out.println(GetCyanBold() + "║ " + GetReset()
                            + GetGreen() + "✔  Modo SQL Server selecionado." + GetReset()
                            + " A ligar ao servidor remoto..."
                            + String.format("%" + (GetLargura() - 58) + "s", "")
                            + GetCyanBold() + "║" + GetReset());
                    System.out.println(GetCyanBold() + GetBordaInferior() + GetReset());
                    aguardar(800);
                    return;
                }
                default -> System.out.println(GetRed() + "\n  Opção inválida. Digite 1 ou 2." + GetReset());
            }
        }
    }

    private static String getTag(String texto, String cor) {
        return GetReset() + "[" + cor + texto + GetReset() + "]";
    }

    private static void aguardar(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
