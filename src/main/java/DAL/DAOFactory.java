package DAL;

import java.io.InputStream;
import java.util.Properties;

/**
 * Fábrica central de DAOs com cache singleton por tipo.
 *
 * <p>Em modo CSV, cada DAO lê o ficheiro uma única vez (no primeiro acesso) e mantém
 * os dados em memória. Todas as escritas atualizam a memória E o CSV em simultâneo
 * (comportamento de {@link AbstractCsvCRUD}), pelo que nunca há dados desatualizados.</p>
 *
 * <p>O modo é definido no arranque via {@link #setModo(String)}.
 * Mudar de modo descarta as instâncias em cache para que novos DAOs do tipo correto
 * sejam criados na próxima chamada.</p>
 */
public class DAOFactory {

    private static String tipoArmazenamento;

    // ── Instâncias singleton (uma por tipo, criadas no primeiro acesso) ─────────
    private static IEstudanteDAO           estudanteDAO;
    private static IDocenteDAO             docenteDAO;
    private static IDepartamentoDAO        departamentoDAO;
    private static IUnidadeCurricularDAO   unidadeCurricularDAO;
    private static ICursoDAO               cursoDAO;
    private static IAvaliacaoDAO           avaliacaoDAO;
    private static IPropinaDAO             propinaDAO;
    private static IGestorDAO              gestorDAO;
    private static IAnoLetivoDAO           anoLetivoDAO;
    // v1.3 ────────────────────────────────────────────────────────────────────
    private static IHorarioDAO             horarioDAO;
    private static IPresencaDAO            presencaDAO;
    private static IJustificacaoFaltaDAO   justificacaoFaltaDAO;
    private static ITipoEstatutoDAO        tipoEstatutoDAO;
    private static IEstatutoEstudanteDAO   estatutoEstudanteDAO;

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

    /**
     * Define o modo de armazenamento em runtime.
     * Deve ser chamado UMA VEZ no arranque da aplicação, antes de qualquer DAO ser instanciado.
     * Se for chamado após instâncias já existirem (mudança de modo), descarta a cache.
     *
     * @param modo "CSV" ou "SQL"
     */
    public static void setModo(String modo) {
        if (modo != null && (modo.equalsIgnoreCase("CSV") || modo.equalsIgnoreCase("SQL"))) {
            tipoArmazenamento = modo.toUpperCase();
            // Sempre descarta a cache ao definir o modo:
            // • Em produção é chamado uma vez antes de qualquer acesso → sem custo real
            // • Em testes, chamar setModo("CSV") serve como reset explícito da cache
            resetarInstancias();
        }
    }

    /**
     * Descarta todas as instâncias em cache.
     * <p>Chamado automaticamente quando o modo muda. Em testes, deve ser invocado
     * explicitamente após escritas directas via {@code new XxxCRUD()} para garantir
     * que o próximo {@code DAOFactory.getXxxDAO()} lê o CSV actualizado.</p>
     */
    public static void resetarInstancias() {
        estudanteDAO         = null;
        docenteDAO           = null;
        departamentoDAO      = null;
        unidadeCurricularDAO = null;
        cursoDAO             = null;
        avaliacaoDAO         = null;
        propinaDAO           = null;
        gestorDAO            = null;
        anoLetivoDAO         = null;
        horarioDAO           = null;
        presencaDAO          = null;
        justificacaoFaltaDAO = null;
        tipoEstatutoDAO      = null;
        estatutoEstudanteDAO = null;
    }

    public static String getModo() { return tipoArmazenamento; }
    public static boolean isSql()  { return "SQL".equals(tipoArmazenamento); }

    public static IEstudanteDAO getEstudanteDAO() {
        if (estudanteDAO == null)
            estudanteDAO = isSql() ? new EstudanteSqlDAO() : new EstudanteCRUD();
        return estudanteDAO;
    }

    public static IDocenteDAO getDocenteDAO() {
        if (docenteDAO == null)
            docenteDAO = isSql() ? new DocenteSqlDAO() : new DocenteCRUD();
        return docenteDAO;
    }

    public static IDepartamentoDAO getDepartamentoDAO() {
        if (departamentoDAO == null)
            departamentoDAO = isSql() ? new DepartamentoSqlDAO() : new DepartamentoCRUD();
        return departamentoDAO;
    }

    public static IUnidadeCurricularDAO getUnidadeCurricularDAO() {
        if (unidadeCurricularDAO == null)
            unidadeCurricularDAO = isSql() ? new UnidadeCurricularSqlDAO() : new UnidadeCurricularCRUD();
        return unidadeCurricularDAO;
    }

    public static ICursoDAO getCursoDAO() {
        if (cursoDAO == null)
            cursoDAO = isSql() ? new CursoSqlDAO() : new CursoCRUD();
        return cursoDAO;
    }

    public static IAvaliacaoDAO getAvaliacaoDAO() {
        if (avaliacaoDAO == null)
            avaliacaoDAO = isSql() ? new AvaliacaoSqlDAO() : new AvaliacaoCRUD();
        return avaliacaoDAO;
    }

    public static IPropinaDAO getPropinaDAO() {
        if (propinaDAO == null)
            propinaDAO = isSql() ? new PropinaSqlDAO() : new PropinaCRUD();
        return propinaDAO;
    }

    public static IGestorDAO getGestorDAO() {
        if (gestorDAO == null)
            gestorDAO = isSql() ? new GestorSqlDAO() : new GestorCRUD();
        return gestorDAO;
    }

    // ── Ano Letivo ─────────────────────────────────────────────────────────────
    public static IAnoLetivoDAO getAnoLetivoDAO() {
        if (anoLetivoDAO == null)
            anoLetivoDAO = isSql() ? new AnoLetivoSqlDAO() : new AnoLetivoMemDAO();
        return anoLetivoDAO;
    }

    // ── v1.3: Horário ──────────────────────────────────────────────────────────
    public static IHorarioDAO getHorarioDAO() {
        if (horarioDAO == null)
            horarioDAO = isSql() ? new HorarioSqlDAO() : new HorarioCRUD();
        return horarioDAO;
    }

    // ── v1.3: Presença ─────────────────────────────────────────────────────────
    public static IPresencaDAO getPresencaDAO() {
        if (presencaDAO == null)
            presencaDAO = isSql() ? new PresencaSqlDAO() : new PresencaCRUD();
        return presencaDAO;
    }

    // ── v1.3: Justificação de Falta ────────────────────────────────────────────
    public static IJustificacaoFaltaDAO getJustificacaoFaltaDAO() {
        if (justificacaoFaltaDAO == null)
            justificacaoFaltaDAO = isSql() ? new JustificacaoFaltaSqlDAO() : new JustificacaoFaltaCRUD();
        return justificacaoFaltaDAO;
    }

    // ── v1.3: Tipo de Estatuto ─────────────────────────────────────────────────
    public static ITipoEstatutoDAO getTipoEstatutoDAO() {
        if (tipoEstatutoDAO == null)
            tipoEstatutoDAO = isSql() ? new TipoEstatutoSqlDAO() : new TipoEstatutoCRUD();
        return tipoEstatutoDAO;
    }

    // ── v1.3: Estatuto de Estudante ────────────────────────────────────────────
    public static IEstatutoEstudanteDAO getEstatutoEstudanteDAO() {
        if (estatutoEstudanteDAO == null)
            estatutoEstudanteDAO = isSql() ? new EstatutoEstudanteSqlDAO() : new EstatutoEstudanteCRUD();
        return estatutoEstudanteDAO;
    }
}
