package DAL;

import model.DiaSemana;
import model.Horario;
import model.UnidadeCurricular;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HorarioCRUD extends AbstractCsvCRUD<Horario> implements IHorarioDAO {

    public HorarioCRUD() {
        super("horarios.csv");
    }

    private int proximoId() {
        return dados.stream().mapToInt(Horario::getId).max().orElse(0) + 1;
    }

    @Override
    protected Horario mapearLinhaParaEntidade(String[] colunas) {
        try {
            // id;ucId;anoLetivoId;diaSemana;horaInicio;horaFim;sala
            int id           = Integer.parseInt(colunas[0]);
            int ucId         = Integer.parseInt(colunas[1]);
            int anoLetivoId  = Integer.parseInt(colunas[2]);
            DiaSemana dia    = DiaSemana.fromString(colunas[3]);
            LocalTime inicio = LocalTime.parse(colunas[4]);
            LocalTime fim    = LocalTime.parse(colunas[5]);
            String sala      = colunas[6];

            UnidadeCurricularCRUD ucCRUD = new UnidadeCurricularCRUD();
            UnidadeCurricular uc = ucCRUD.procurarPorId(ucId);
            if (uc == null) return null;

            Horario h = new Horario(uc, anoLetivoId, dia, inicio, fim, sala);
            h.setId(id);
            return h;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected String mapearEntidadeParaLinha(Horario h) {
        return String.format("%d;%d;%d;%s;%s;%s;%s",
                h.getId(),
                h.getUnidadeCurricular().getId(),
                h.getAnoLetivoId(),
                h.getDiaSemana().name(),
                h.getHoraInicio(),
                h.getHoraFim(),
                sanitizar(h.getSala()));
    }

    @Override
    public boolean registarHorario(Horario horario) {
        horario.setId(proximoId());
        dados.add(horario);
        guardarTodosNoFicheiro();
        return true;
    }

    @Override
    public boolean eliminarHorario(int id) {
        boolean removido = dados.removeIf(h -> h.getId() == id);
        if (removido) guardarTodosNoFicheiro();
        return removido;
    }

    @Override
    public Horario procurarPorId(int id) {
        return dados.stream().filter(h -> h.getId() == id).findFirst().orElse(null);
    }

    @Override
    public List<Horario> listarPorUC(int ucId) {
        return dados.stream()
                .filter(h -> h.getUnidadeCurricular() != null && h.getUnidadeCurricular().getId() == ucId)
                .collect(Collectors.toList());
    }

    @Override
    public List<Horario> listarPorAnoLetivo(int anoLetivoId) {
        return dados.stream()
                .filter(h -> h.getAnoLetivoId() == anoLetivoId)
                .collect(Collectors.toList());
    }

    @Override
    public List<Horario> listarTodos() {
        return new ArrayList<>(dados);
    }
}
