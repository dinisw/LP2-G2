package DAL;

import model.Departamento;
import java.util.List;

public class DepartamentoCRUD extends AbstractCsvCRUD<Departamento> {

    public DepartamentoCRUD() {
        super("departamentos.csv");
    }

    @Override
    protected Departamento mapearLinhaParaEntidade(String[] colunas) {
        try {
            return new Departamento(colunas[1], colunas[0]);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected String mapearEntidadeParaLinha(Departamento d) {
        return String.format("%s;%s", d.getSigla(), d.getNome());
    }

    public boolean registarDepartamento(Departamento departamento) {
        if (procurarPorSigla(departamento.getSigla()) != null) return false;
        dados.add(departamento);
        guardarTodosNoFicheiro();
        return true;
    }

    public List<Departamento> getDepartamentos() {
        return dados;
    }

    public Departamento procurarPorSigla(String sigla) {
        return dados.stream().filter(d -> d.getSigla().equalsIgnoreCase(sigla)).findFirst().orElse(null);
    }

    public boolean atualizarDepartamento(Departamento dep) {
        for (int i = 0; i < dados.size(); i++) {
            if (dados.get(i).getSigla().equalsIgnoreCase(dep.getSigla())) {
                dados.set(i, dep);
                guardarTodosNoFicheiro();
                return true;
            }
        }
        return false;
    }

    public boolean eliminarDepartamento(String sigla) {
        Departamento remover = procurarPorSigla(sigla);
        if (remover != null) {
            dados.remove(remover);
            guardarTodosNoFicheiro();
            return true;
        }
        return false;
    }
}