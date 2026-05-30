package DAL;

import model.Propina;
import java.util.List;
import java.util.stream.Collectors;

public class PropinaCRUD extends AbstractCsvCRUD<Propina> implements IPropinaDAO {

    public PropinaCRUD() {
        super("propinas.csv");
    }

    @Override
    protected Propina mapearLinhaParaEntidade(String[] colunas) {
        try {
            int numMec = Integer.parseInt(colunas[0]);
            int ano = Integer.parseInt(colunas[1]);
            double total = Double.parseDouble(colunas[2].replace(",", "."));
            double pago = Double.parseDouble(colunas[3].replace(",", "."));

            return new Propina(numMec, ano, total, pago);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected String mapearEntidadeParaLinha(Propina p) {
        return String.format("%d;%d;%.2f;%.2f", p.getNumeroMecEstudante(), p.getAnoLetivo(), p.getValorTotal(), p.getValorPago());
    }

    public boolean registarPropina(Propina propina) {
        dados.add(propina);
        guardarTodosNoFicheiro();
        return true;
    }

    public Propina procurarPropina(int numeroMec, int anoLetivo) {
        return dados.stream().filter(p -> p.getNumeroMecEstudante() == numeroMec && p.getAnoLetivo() == anoLetivo).findFirst().orElse(null);
    }

    public boolean atualizarPropina(Propina propinaAtualizada) {
        for (int i = 0; i < dados.size(); i++) {
            if (dados.get(i).getNumeroMecEstudante() == propinaAtualizada.getNumeroMecEstudante() &&
                    dados.get(i).getAnoLetivo() == propinaAtualizada.getAnoLetivo()) {
                dados.set(i, propinaAtualizada);
                guardarTodosNoFicheiro();
                return true;
            }
        }
        return false;
    }

    public List<Propina> listarPropinasPorEstudante(int numeroMec) {
        return dados.stream().filter(p -> p.getNumeroMecEstudante() == numeroMec).collect(Collectors.toList());
    }

    public List<Propina> getTodasPropinas() {
        return dados;
    }
}