package BLL;

import model.Avaliacao;
import model.Estudante;
import view.EstudanteView;

public class EstudanteCalculo {

    private Estudante model;
    private EstudanteView view;

    public EstudanteCalculo (Estudante model, EstudanteView view) {
        this.model = model;
        this.view = view;
    }
    public double calculoPercentagem(int totalUCsInscritas) {
        if (model.getListaAvaliacoes() == null || model.getListaAvaliacoes().isEmpty() || totalUCsInscritas <= 0) {
            return 0.0;
        }
        int notasPositivas = 0;
        for (Avaliacao avaliacao : model.getListaAvaliacoes()) {
            if (avaliacao.getNota() >= 9.5) {
                notasPositivas++;
            }
        }
        return (double) notasPositivas / totalUCsInscritas;
    }
    public boolean verificarProgressao(int totalUCsInscritas) {
        double percentagem = calculoPercentagem(totalUCsInscritas);
        if (percentagem >= 0.60) {
            model.setAnoLetivo(model.getAnoLetivo() + 1);
            return true;
        }
        return false;
    }
}
