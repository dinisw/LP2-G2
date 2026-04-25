package controller;

import model.Avaliacao;
import java.util.List;

public class AvaliacaoController {

    private DAL.AvaliacaoCRUD avaliacaoCRUD;

    public AvaliacaoController() {
        this.avaliacaoCRUD = new DAL.AvaliacaoCRUD();
    }

    public boolean registarAvaliacao(model.Avaliacao avaliacao) {
        if (avaliacao == null || avaliacao.getEstudante() == null || avaliacao.getUnidadeCurricular() == null) {
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

        List<Avaliacao> avaliacoesExistentes = avaliacaoCRUD.listarPorUnidadeCurricular(avaliacao.getUnidadeCurricular().getNome());

        if (avaliacoesExistentes != null) {
            long contagem = avaliacoesExistentes.stream()
                    .filter(a -> a.getEstudante().getNumeroMec() == avaliacao.getEstudante().getNumeroMec())
                    .count();

            if (contagem >= 3) {
                System.out.println("Erro: O estudante já atingiu o limite máximo de 3 avaliações para esta UC.");
                return false;
            }
        }

        return avaliacaoCRUD.registarAvaliacao(avaliacao);
    }

    public List<Avaliacao> listarAvaliacoesPorUC(String nomeUC) {
        if (nomeUC == null || nomeUC.trim().isEmpty()) {
            return null;
        }
        return avaliacaoCRUD.listarPorUnidadeCurricular(nomeUC);
    }
}
