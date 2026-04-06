package common.utils;

public class DesignUtils {
    //region Design
    private static final String RESET = "\033[0m";
    private static final String CYAN_BOLD = "\033[1;36m";
    private static final String WHITE_BOLD = "\033[1;37m";
    private static final String BLUE = "\033[0;34m";
    private static final String GREEN = "\033[0;32m";
    private static final String RED = "\033[0;31m";
    private static final String YELLOW = "\033[0;33m";
    private static final int LARGURA = 84;
    private static final String bordaSuperior = "╔" + "═".repeat(GetLargura()) + "╗";
    private static final String bordaMeio = "╠" + "═".repeat(GetLargura()) + "╣";
    private static final String bordaInferior = "╚" + "═".repeat(GetLargura()) + "╝";

    public static String GetReset(){return RESET;}
    public static String GetCyanBold(){return CYAN_BOLD;}
    public static String GetWhiteBold(){return WHITE_BOLD;}
    public static String GetBlue(){return BLUE;}
    public static String GetGreen(){return GREEN;}
    public static String GetRed(){return RED;}
    public static String GetYellow(){return YELLOW;}
    public static int GetLargura(){return LARGURA;}
    public static String GetBordaSuperior(){return bordaSuperior;}
    public static String GetBordaMeio(){return bordaMeio;}
    public static String GetBordaInferior(){return bordaInferior;}
    //endregion
}
