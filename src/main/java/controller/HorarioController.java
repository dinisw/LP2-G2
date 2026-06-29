package controller;

import DAL.DAOFactory;
import DAL.IHorarioDAO;
import DAL.IUnidadeCurricularDAO;
import model.*;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Regras de negócio para horários (Enunciado v1.3):
 *   - Blocos só entre 18:00 e 23:30
 *   - Pausa de jantar: 20:00–20:30 proibida
 *   - Blocos de 1h ou 2h exatamente (sem 30 min)
 *   - Máx. 5h letivas/dia por docente e por UC
 *   - UC: mín. 2h e máx. 6h/semana
 *   - Sem sobreposição para o mesmo docente nem para a mesma UC
 */
public class HorarioController {
    private static final LocalTime HORA_MIN      = LocalTime.of(18, 0);
    private static final LocalTime HORA_MAX      = LocalTime.of(23, 30);
    private static final LocalTime JANTAR_INICIO = LocalTime.of(20, 0);
    private static final LocalTime JANTAR_FIM    = LocalTime.of(20, 30);
    private static final int MAX_HORAS_DIA       = 5;
    private static final int MIN_HORAS_UC_SEMANA = 2;
    private static final int MAX_HORAS_UC_SEMANA = 6;

    private final IHorarioDAO horarioDAO;
    private final IUnidadeCurricularDAO ucDAO;

    public HorarioController() {
        this.horarioDAO = DAOFactory.getHorarioDAO();
        this.ucDAO      = DAOFactory.getUnidadeCurricularDAO();
    }

    public Resultado<Horario> registarHorario(int ucId, int anoLetivoId,
                                               DiaSemana dia, LocalTime inicio, LocalTime fim,
                                               String sala) {
        UnidadeCurricular uc = ucDAO.procurarPorId(ucId);
        if (uc == null) return new Resultado<>(false, "Unidade Curricular não encontrada.");
        if (uc.getDocente() == null) return new Resultado<>(false, "A UC não tem docente atribuído.");
        if (!uc.getDocente().isAtivo()) return new Resultado<>(false, "O docente da UC está inativo.");
        if (sala == null || sala.trim().isEmpty()) return new Resultado<>(false, "A sala é obrigatória.");

        // Validação de horário permitido
        if (inicio.isBefore(HORA_MIN) || fim.isAfter(HORA_MAX))
            return new Resultado<>(false, "Horário fora do intervalo permitido (18:00–23:30).");
        if (inicio.isBefore(fim) == false)
            return new Resultado<>(false, "A hora de início deve ser anterior à hora de fim.");

        // Blocos exactos de 1h ou 2h
        long minutos = java.time.Duration.between(inicio, fim).toMinutes();
        if (minutos != 60 && minutos != 120)
            return new Resultado<>(false, "Os blocos devem ser de exactamente 1h ou 2h (sem meios horários).");

        // Pausa de jantar: aula não pode atravessar 20:00–20:30
        if (inicio.isBefore(JANTAR_FIM) && fim.isAfter(JANTAR_INICIO))
            return new Resultado<>(false, "Bloqueado: o horário sobrepõe-se com a pausa de jantar (20:00–20:30).");

        Horario novo = new Horario(uc, anoLetivoId, dia, inicio, fim, sala.trim());

        List<Horario> todosAnoLetivo = horarioDAO.listarPorAnoLetivo(anoLetivoId);

        // Sobreposição para a mesma UC
        for (Horario h : horarioDAO.listarPorUC(ucId)) {
            if (h.getAnoLetivoId() == anoLetivoId && h.sobrepoeCom(novo))
                return new Resultado<>(false, "Sobreposição de horário: esta UC já tem aula neste bloco.");
        }

        // Sobreposição para o mesmo docente
        String siglaDocente = uc.getDocente().getSigla();
        for (Horario h : todosAnoLetivo) {
            if (h.getUnidadeCurricular() != null
                    && h.getUnidadeCurricular().getDocente() != null
                    && h.getUnidadeCurricular().getDocente().getSigla().equalsIgnoreCase(siglaDocente)
                    && h.sobrepoeCom(novo)) {
                return new Resultado<>(false,
                        "Sobreposição de horário: o docente " + siglaDocente + " já tem aula neste bloco.");
            }
        }

        // Máx. 5h/dia para o docente
        int horasDocenteNoDia = todosAnoLetivo.stream()
                .filter(h -> h.getDiaSemana() == dia
                        && h.getUnidadeCurricular() != null
                        && h.getUnidadeCurricular().getDocente() != null
                        && h.getUnidadeCurricular().getDocente().getSigla().equalsIgnoreCase(siglaDocente))
                .mapToInt(Horario::getDuracaoHoras).sum();
        if (horasDocenteNoDia + (int)(minutos / 60) > MAX_HORAS_DIA)
            return new Resultado<>(false, "O docente " + siglaDocente + " já atingiu o máximo de 5h letivas neste dia.");

        // UC: mín 2h e máx 6h/semana
        int horasUCSemana = horarioDAO.listarPorUC(ucId).stream()
                .filter(h -> h.getAnoLetivoId() == anoLetivoId)
                .mapToInt(Horario::getDuracaoHoras).sum();
        int duracaoBloco = (int)(minutos / 60);
        if (horasUCSemana + duracaoBloco > MAX_HORAS_UC_SEMANA)
            return new Resultado<>(false,
                    "Bloqueado: a UC já atingiu o máximo de " + MAX_HORAS_UC_SEMANA + "h semanais.");

        boolean sucesso = horarioDAO.registarHorario(novo);
        return sucesso ? new Resultado<>(novo, true) : new Resultado<>(false, "Erro ao guardar o horário.");
    }

    public Resultado<String> eliminarHorario(int id) {
        if (horarioDAO.procurarPorId(id) == null) return new Resultado<>(false, "Horário não encontrado.");
        return horarioDAO.eliminarHorario(id)
                ? new Resultado<>("Horário eliminado.", true)
                : new Resultado<>(false, "Erro ao eliminar horário.");
    }

    /** Valida sobreposições e limites sem persistir. Retorna null se válido, mensagem de erro caso contrário. */
    public String validarSlotHorario(int ucId, int anoLetivoId, DiaSemana dia, LocalTime inicio, LocalTime fim) {
        UnidadeCurricular uc = ucDAO.procurarPorId(ucId);
        if (uc == null) return "UC não encontrada.";
        Horario candidato = new Horario(uc, anoLetivoId, dia, inicio, fim, "?");
        List<Horario> todosAnoLetivo = horarioDAO.listarPorAnoLetivo(anoLetivoId);

        for (Horario h : horarioDAO.listarPorUC(ucId)) {
            if (h.getAnoLetivoId() == anoLetivoId && h.sobrepoeCom(candidato))
                return "Sobreposição: esta UC já tem aula neste bloco.";
        }
        if (uc.getDocente() != null) {
            String sigla = uc.getDocente().getSigla();
            for (Horario h : todosAnoLetivo) {
                if (h.getUnidadeCurricular() != null
                        && h.getUnidadeCurricular().getDocente() != null
                        && h.getUnidadeCurricular().getDocente().getSigla().equalsIgnoreCase(sigla)
                        && h.sobrepoeCom(candidato))
                    return "Sobreposição: o docente " + sigla + " já tem aula neste bloco.";
            }
            int horasDocDia = todosAnoLetivo.stream()
                    .filter(h -> h.getDiaSemana() == dia
                            && h.getUnidadeCurricular() != null
                            && h.getUnidadeCurricular().getDocente() != null
                            && h.getUnidadeCurricular().getDocente().getSigla().equalsIgnoreCase(sigla))
                    .mapToInt(Horario::getDuracaoHoras).sum();
            int blocoH = (int) java.time.Duration.between(inicio, fim).toHours();
            if (horasDocDia + blocoH > MAX_HORAS_DIA)
                return "O docente " + sigla + " já atingiu o máximo de 5h letivas neste dia.";
        }
        int horasUCSemana = horarioDAO.listarPorUC(ucId).stream()
                .filter(h -> h.getAnoLetivoId() == anoLetivoId)
                .mapToInt(Horario::getDuracaoHoras).sum();
        int blocoH = (int) java.time.Duration.between(inicio, fim).toHours();
        if (horasUCSemana + blocoH > MAX_HORAS_UC_SEMANA)
            return "A UC já atingiu o máximo de " + MAX_HORAS_UC_SEMANA + "h semanais.";
        return null;
    }

    public List<Horario> listarHorariosPorUC(int ucId)             { return horarioDAO.listarPorUC(ucId); }
    public List<Horario> listarHorariosPorAnoLetivo(int anoLetivoId) { return horarioDAO.listarPorAnoLetivo(anoLetivoId); }
    public List<Horario> listarTodos()                             { return horarioDAO.listarTodos(); }

    public List<Horario> listarHorariosPorDocente(String siglaDocente, int anoLetivoId) {
        return horarioDAO.listarPorAnoLetivo(anoLetivoId).stream()
                .filter(h -> h.getUnidadeCurricular() != null
                        && h.getUnidadeCurricular().getDocente() != null
                        && h.getUnidadeCurricular().getDocente().getSigla().equalsIgnoreCase(siglaDocente))
                .sorted(java.util.Comparator.comparing(Horario::getDiaSemana)
                        .thenComparing(Horario::getHoraInicio))
                .collect(Collectors.toList());
    }

    public List<Horario> listarHorariosPorEstudante(int numeroMec, int anoLetivoId) {
        EstudanteController estCtrl = new EstudanteController();
        model.Estudante est = estCtrl.procurarEstudantePorNumeroMec(numeroMec);
        if (est == null) return List.of();

        return horarioDAO.listarPorAnoLetivo(anoLetivoId).stream()
                .filter(h -> {
                    if (h.getUnidadeCurricular() == null) return false;
                    return ucDAO.getUnidadeCurriculars().stream()
                            .filter(uc -> uc.getId() == h.getUnidadeCurricular().getId())
                            .findFirst()
                            .map(uc -> true).orElse(false);
                })
                .sorted(java.util.Comparator.comparing(Horario::getDiaSemana)
                        .thenComparing(Horario::getHoraInicio))
                .collect(Collectors.toList());
    }

    public String validarMinHorasSemanaisUCs(int anoLetivoId) {
        StringBuilder aviso = new StringBuilder();
        for (UnidadeCurricular uc : ucDAO.getUnidadeCurriculars()) {
            int total = horarioDAO.listarPorUC(uc.getId()).stream()
                    .filter(h -> h.getAnoLetivoId() == anoLetivoId)
                    .mapToInt(Horario::getDuracaoHoras).sum();
            if (total < MIN_HORAS_UC_SEMANA) {
                aviso.append("  ⚠ UC '").append(uc.getNome())
                     .append("' tem apenas ").append(total).append("h semanais (mín. 2h).\n");
            }
        }
        return aviso.toString();
    }
}
