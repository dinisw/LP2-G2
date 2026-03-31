package Common;

public class DesignUtils {
    //region Design
    public static final String RESET = "\033[0m";
    public static final String CYAN_BOLD = "\033[1;36m";
    public static final String WHITE_BOLD = "\033[1;37m";
    public static final String BLUE = "\033[0;34m";
    public static final String GREEN = "\033[0;32m";
    public static final String RED = "\033[0;31m";
    public static final String YELLOW = "\033[0;33m";
    static final int LARGURA = 84;
    public static final String bordaSuperior = "╔" + "═".repeat(LARGURA) + "╗";
    public static final String bordaMeio = "╠" + "═".repeat(LARGURA) + "╣";
    public static final String bordaInferior = "╚" + "═".repeat(LARGURA) + "╝";

    public static String GetReset(){return RESET;}
    public static String GetCyanBold(){return CYAN_BOLD;}
    public static String GetWhiteBold(){return WHITE_BOLD;}
    public static String GetBlue(){return BLUE;}
    public static String GetGreen(){return GREEN;}
    public static String GetRed(){return RED;}
    public static String GetYellow(){return YELLOW;}
    public static int GetLargura(){return LARGURA;}
    //endregion
}
