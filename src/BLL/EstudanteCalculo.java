package BLL;

import model.Avaliacao;
import model.Estudante;

public class EstudanteCalculo {

    public EstudanteCalculo() {
    }

    public double calculoPercentagem(Estudante estudante, int totalUCsInscritas) {
        if (estudante == null || estudante.getListaAvaliacoes() == null ||
                estudante.getListaAvaliacoes().isEmpty() || totalUCsInscritas <= 0) {
            return 0.0;
        }

        int notasPositivas = 0;

        for (Avaliacao avaliacao : estudante.getListaAvaliacoes()) {
            if (avaliacao.getNota() != null && avaliacao.getNota() >= 9.5) {
                notasPositivas++;
            }
        }

        return (double) notasPositivas / totalUCsInscritas;
    }

    public boolean verificarProgressao(Estudante estudante, int totalUCsInscritas) {
        double percentagem = calculoPercentagem(estudante, totalUCsInscritas);

        if (percentagem >= 0.60) {
            estudante.setAnoLetivo(estudante.getAnoLetivo() + 1);
            return true;
        }

        return false;
    }
}