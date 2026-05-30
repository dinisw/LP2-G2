package DAL;

import java.io.InputStream;
import java.util.Properties;

/**
 * Fábrica central de DAOs.
 * Lê "armazenamento.tipo" de config.properties (CSV | SQL).
 * Adicionar novas entidades aqui basta para que todo o programa mude de backend.
 */
public class DAOFactory {

    private static final String tipoArmazenamento;

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

    public static boolean isSql() {
        return "SQL".equals(tipoArmazenamento);
    }

    // ── Estudante ──────────────────────────────────────────────────────────────
    public static IEstudanteDAO getEstudanteDAO() {
        return isSql() ? new EstudanteSqlDAO() : new EstudanteCsvDAO();
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
