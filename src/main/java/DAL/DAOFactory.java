package DAL;

import java.io.InputStream;
import java.util.Properties;

public class DAOFactory {
    private static String tipoArmazenamento = "CSV";

    static {
        try (InputStream input = DAOFactory.class.getClassLoader().getResourceAsStream("config.properties")){
            Properties properties = new Properties();
            if (input != null) {
                properties.load(input);
                tipoArmazenamento = properties.getProperty("armazenamento.tipo", "CSV").toUpperCase();
            }
        } catch (Exception ex) {
            System.err.println("Aviso: Falha ao ler config.properties. A usar CSV por defeito.");
        }
    }

    public static IEstudanteDAO getEstudanteDAO() {
        if ("SQL".equals(tipoArmazenamento)) {
            return new EstudanteSqlDAO();
        } else {
            return new EstudanteCsvDAO();
        }
    }
}
