package DAL;

import model.Estudante;
import model.Horario;
import model.Presenca;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PresencaCRUD extends AbstractCsvCRUD<Presenca> implements IPresencaDAO {

    public PresencaCRUD() {
        super("presencas.csv");
    }

    private int proximoId() {
        return dados.stream().mapToInt(Presenca::getId).max().orElse(0) + 1;
    }

    @Override
    protected Presenca mapearLinhaParaEntidade(String[] colunas) {
        try {
            // id;horarioId;numeroMec;data;presencaDocente;presencaEstudante
            int id                = Integer.parseInt(colunas[0]);
            int horarioId         = Integer.parseInt(colunas[1]);
            int numeroMec         = Integer.parseInt(colunas[2]);
            LocalDate data        = LocalDate.parse(colunas[3]);
            boolean docente       = Boolean.parseBoolean(colunas[4]);
            boolean estudante     = Boolean.parseBoolean(colunas[5]);

            HorarioCRUD horarioCRUD   = new HorarioCRUD();
            EstudanteCRUD estudanteCRUD = new EstudanteCRUD();

            Horario h  = horarioCRUD.procurarPorId(horarioId);
            Estudante e = estudanteCRUD.lerEstudante(numeroMec);
            if (h == null || e == null) return null;

            Presenca p = new Presenca(e, h, data);
            p.setId(id);
            p.setPresencaDocente(docente);
            if (docente) p.setPresencaEstudante(estudante);
            return p;
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    protected String mapearEntidadeParaLinha(Presenca p) {
        return String.format("%d;%d;%d;%s;%b;%b",
                p.getId(),
                p.getHorario().getId(),
                p.getEstudante().getNumeroMec(),
                p.getData(),
                p.isPresencaDocente(),
                p.isPresencaEstudante());
    }

    @Override
    public boolean registarPresenca(Presenca presenca) {
        presenca.setId(proximoId());
        dados.add(presenca);
        guardarTodosNoFicheiro();
        return true;
    }

    @Override
    public boolean atualizarPresenca(Presenca presenca) {
        for (int i = 0; i < dados.size(); i++) {
            if (dados.get(i).getId() == presenca.getId()) {
                dados.set(i, presenca);
                guardarTodosNoFicheiro();
                return true;
            }
        }
        return false;
    }

    @Override
    public Presenca procurarPorId(int id) {
        return dados.stream().filter(p -> p.getId() == id).findFirst().orElse(null);
    }

    @Override
    public List<Presenca> listarPorHorario(int horarioId) {
        return dados.stream()
                .filter(p -> p.getHorario() != null && p.getHorario().getId() == horarioId)
                .collect(Collectors.toList());
    }

    @Override
    public List<Presenca> listarPorEstudante(int numeroMec) {
        return dados.stream()
                .filter(p -> p.getEstudante() != null && p.getEstudante().getNumeroMec() == numeroMec)
                .collect(Collectors.toList());
    }

    @Override
    public List<Presenca> listarFaltasPorUC(int ucId) {
        return dados.stream()
                .filter(p -> p.isFalta()
                        && p.getHorario() != null
                        && p.getHorario().getUnidadeCurricular() != null
                        && p.getHorario().getUnidadeCurricular().getId() == ucId)
                .collect(Collectors.toList());
    }

    @Override
    public List<Presenca> listarTodas() {
        return new ArrayList<>(dados);
    }

    @Override
    public boolean eliminarPresenca(int id) {
        boolean removida = dados.removeIf(p -> p.getId() == id);
        if (removida) guardarTodosNoFicheiro();
        return removida;
    }
}
