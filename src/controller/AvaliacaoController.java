package controller;

public class AvaliacaoController {

    private DAL.AvaliacaoCRUD avaliacaoCRUD;

    public AvaliacaoController() {
        this.avaliacaoCRUD = new DAL.AvaliacaoCRUD();
    }

    public boolean registarAvaliacao(model.Avaliacao avaliacao) {
        if (avaliacao == null) {
            return false;
        }

        if (avaliacao.getEstudante() == null || avaliacao.getUnidadeCurricular() == null) {
            return false;
        }

        if (avaliacao.getNota() != null) {
            if (avaliacao.getNota() < 0.0 || avaliacao.getNota() > 20.0) {
                return false;
            }
        }

        if (avaliacao.getMomento() == null || avaliacao.getMomento().trim().isEmpty()) {
            return false;
        }

        return avaliacaoCRUD.registarAvaliacao(avaliacao);
    }

}
