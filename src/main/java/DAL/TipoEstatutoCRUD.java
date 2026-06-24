package DAL;

import model.TipoEstatuto;

import java.util.ArrayList;
import java.util.List;

public class TipoEstatutoCRUD extends AbstractCsvCRUD<TipoEstatuto> implements ITipoEstatutoDAO {

    public TipoEstatutoCRUD() {
        super("tipos_estatuto.csv");
    }

    private int proximoId() {
        return dados.stream().mapToInt(TipoEstatuto::getId).max().orElse(0) + 1;
    }

    @Override
    protected TipoEstatuto mapearLinhaParaEntidade(String[] colunas) {
        try {
            // id;nome;descricao
            int id         = Integer.parseInt(colunas[0]);
            String nome    = colunas[1];
            String descricao = colunas.length > 2 ? colunas[2] : "";
            TipoEstatuto t = new TipoEstatuto(nome, descricao);
            t.setId(id);
            return t;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected String mapearEntidadeParaLinha(TipoEstatuto t) {
        return String.format("%d;%s;%s",
                t.getId(),
                sanitizar(t.getNome()),
                sanitizar(t.getDescricao() != null ? t.getDescricao() : ""));
    }

    @Override
    public boolean registarTipoEstatuto(TipoEstatuto tipo) {
        if (procurarPorNome(tipo.getNome()) != null) return false;
        tipo.setId(proximoId());
        dados.add(tipo);
        guardarTodosNoFicheiro();
        return true;
    }

    @Override
    public boolean eliminarTipoEstatuto(int id) {
        boolean removido = dados.removeIf(t -> t.getId() == id);
        if (removido) guardarTodosNoFicheiro();
        return removido;
    }

    @Override
    public TipoEstatuto procurarPorId(int id) {
        return dados.stream().filter(t -> t.getId() == id).findFirst().orElse(null);
    }

    @Override
    public TipoEstatuto procurarPorNome(String nome) {
        if (nome == null) return null;
        return dados.stream()
                .filter(t -> t.getNome().equalsIgnoreCase(nome))
                .findFirst().orElse(null);
    }

    @Override
    public List<TipoEstatuto> listarTodos() {
        return new ArrayList<>(dados);
    }
}
