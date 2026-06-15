package DAL;

import java.io.InputStream;
import java.util.Properties;

public class DAOFactory {

    private static String tipoArmazenamento;

    static {
        String tipo = "CSV";
        try (InputStream input = DAOFactory.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input != null) {
                Properties props = new Properties();
                props.load(input);
                tipo = props.getProperty("armazenamento.tipo", "CSV").toUpperCase();
            }
        } catch (Exception ex) {
            System.err.println("Aviso: Falha ao ler config.properties. A usar CSV por defeito.");
        }
        tipoArmazenamento = tipo;
    }

    public static void setModo(String modo) {
        if (modo != null && (modo.equalsIgnoreCase("CSV") || modo.equalsIgnoreCase("SQL"))) {
            tipoArmazenamento = modo.toUpperCase();
        }
    }

    public static String getModo() {
        return tipoArmazenamento;
    }

    public static boolean isSql() {
        return "SQL".equals(tipoArmazenamento);
    }

    public static IEstudanteDAO getEstudanteDAO() {
        return isSql() ? new EstudanteSqlDAO() : new EstudanteCRUD();
    }

    public static IDocenteDAO getDocenteDAO() {
        return isSql() ? new DocenteSqlDAO() : new DocenteCRUD();
    }

    public static IDepartamentoDAO getDepartamentoDAO() {
        return isSql() ? new DepartamentoSqlDAO() : new DepartamentoCRUD();
    }

    public static IUnidadeCurricularDAO getUnidadeCurricularDAO() {
        return isSql() ? new UnidadeCurricularSqlDAO() : new UnidadeCurricularCRUD();
    }

    public static ICursoDAO getCursoDAO() {
        return isSql() ? new CursoSqlDAO() : new CursoCRUD();
    }

    public static IAvaliacaoDAO getAvaliacaoDAO() {
        return isSql() ? new AvaliacaoSqlDAO() : new AvaliacaoCRUD();
    }

    public static IPropinaDAO getPropinaDAO() {
        return isSql() ? new PropinaSqlDAO() : new PropinaCRUD();
    }

    public static IGestorDAO getGestorDAO() {
        return isSql() ? new GestorSqlDAO() : new GestorCRUD();
    }
}
