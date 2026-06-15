package DAL;

import model.Estudante;
import model.EstatutoEstudante;
import model.TipoEstatuto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EstatutoEstudanteCRUD extends AbstractCsvCRUD<EstatutoEstudante> implements IEstatutoEstudanteDAO {

    public EstatutoEstudanteCRUD() {
        super("estatutos_estudante.csv");
    }

    private int proximoId() {
        return dados.stream().mapToInt(EstatutoEstudante::getId).max().orElse(0) + 1;
    }

    @Override
    protected EstatutoEstudante mapearLinhaParaEntidade(String[] colunas) {
        try {
            // id;numeroMec;tipoEstatutoId;dataInicio;dataFim
            int id             = Integer.parseInt(colunas[0]);
            int numeroMec      = Integer.parseInt(colunas[1]);
            int tipoId         = Integer.parseInt(colunas[2]);
            LocalDate inicio   = LocalDate.parse(colunas[3]);
            LocalDate fim      = colunas[4].equalsIgnoreCase("null") ? null : LocalDate.parse(colunas[4]);

            EstudanteCRUD estudanteCRUD     = new EstudanteCRUD();
            TipoEstatutoCRUD tipoCRUD       = new TipoEstatutoCRUD();

            Estudante est       = estudanteCRUD.lerEstudante(numeroMec);
            TipoEstatuto tipo   = tipoCRUD.procurarPorId(tipoId);
            if (est == null || tipo == null) return null;

            EstatutoEstudante e = new EstatutoEstudante(est, tipo, inicio, fim);
            e.setId(id);
            return e;
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    protected String mapearEntidadeParaLinha(EstatutoEstudante e) {
        return String.format("%d;%d;%d;%s;%s",
                e.getId(),
                e.getEstudante().getNumeroMec(),
                e.getTipoEstatuto().getId(),
                e.getDataInicio(),
                e.getDataFim() != null ? e.getDataFim().toString() : "null");
    }

    @Override
    public boolean registarEstatuto(EstatutoEstudante estatuto) {
        estatuto.setId(proximoId());
        dados.add(estatuto);
        guardarTodosNoFicheiro();
        return true;
    }

    @Override
    public boolean eliminarEstatuto(int id) {
        boolean removido = dados.removeIf(e -> e.getId() == id);
        if (removido) guardarTodosNoFicheiro();
        return removido;
    }

    @Override
    public EstatutoEstudante procurarPorId(int id) {
        return dados.stream().filter(e -> e.getId() == id).findFirst().orElse(null);
    }

    @Override
    public List<EstatutoEstudante> listarPorEstudante(int numeroMec) {
        return dados.stream()
                .filter(e -> e.getEstudante() != null && e.getEstudante().getNumeroMec() == numeroMec)
                .collect(Collectors.toList());
    }

    @Override
    public List<EstatutoEstudante> listarTodos() {
        return new ArrayList<>(dados);
    }
}
