package DAL;

import java.io.InputStream;
import java.util.Properties;

/**
 * Fábrica central de DAOs.
 * O modo é definido no arranque da aplicação via {@link #setModo(String)}.
 * Fallback: lê "armazenamento.tipo" de config.properties (CSV | SQL).
 */
public class DAOFactory {

    private static String tipoArmazenamento;

    static {
        // Fallback: ler do config.properties caso setModo() não seja chamado
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

    /**
     * Define o modo de armazenamento em runtime.
     * Deve ser chamado UMA VEZ no arranque da aplicação, antes de qualquer DAO ser instanciado.
     *
     * @param modo "CSV" ou "SQL"
     */
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

    // ── Estudante ──────────────────────────────────────────────────────────────
    public static IEstudanteDAO getEstudanteDAO() {
        return isSql() ? new EstudanteSqlDAO() : new EstudanteCRUD();
    }

    // ── Docente ────────────────────────────────────────────────────────────────
    public static IDocenteDAO getDocenteDAO() {
        return isSql() ? new DocenteSqlDAO() : new DocenteCRUD();
    }

    // ── Departamento ───────────────────────────────────────────────────────────
    public static IDepartamentoDAO getDepartamentoDAO() {
        return isSql() ? new DepartamentoSqlDAO() : new DepartamentoCRUD();
    }

    // ── Unidade Curricular ─────────────────────────────────────────────────────
    public static IUnidadeCurricularDAO getUnidadeCurricularDAO() {
        return isSql() ? new UnidadeCurricularSqlDAO() : new UnidadeCurricularCRUD();
    }

    // ── Curso ──────────────────────────────────────────────────────────────────
    public static ICursoDAO getCursoDAO() {
        return isSql() ? new CursoSqlDAO() : new CursoCRUD();
    }

    // ── Avaliação ──────────────────────────────────────────────────────────────
    public static IAvaliacaoDAO getAvaliacaoDAO() {
        return isSql() ? new AvaliacaoSqlDAO() : new AvaliacaoCRUD();
    }

    // ── Propina ────────────────────────────────────────────────────────────────
    public static IPropinaDAO getPropinaDAO() {
        return isSql() ? new PropinaSqlDAO() : new PropinaCRUD();
    }

    // ── Gestor ─────────────────────────────────────────────────────────────────
    public static IGestorDAO getGestorDAO() {
        return isSql() ? new GestorSqlDAO() : new GestorCRUD();
    }
}
