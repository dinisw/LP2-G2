package controller;

import DAL.AvaliacaoCRUD;
import model.Avaliacao;
import model.Resultado;

import java.util.List;

public class AvaliacaoController {
    private final AvaliacaoCRUD avaliacaoCRUD;

    public AvaliacaoController() {
        this.avaliacaoCRUD = new AvaliacaoCRUD();
    }

    public Resultado<Avaliacao> registarAvaliacao(Avaliacao avaliacao) {
        if (avaliacao == null || avaliacao.getEstudante() == null || avaliacao.getUnidadeCurricular() == null) {
            return new Resultado<>(false, "Dados da avaliação incompletos.");
        }

        if (avaliacao.getNota() != null && (avaliacao.getNota() < 0.0 || avaliacao.getNota() > 20.0)) {
            return new Resultado<>(false, "A nota deve estar entre 0.0 e 20.0 valores.");
        }

        if (avaliacao.getMomento() == null || avaliacao.getMomento().trim().isEmpty()) {
            return new Resultado<>(false, "O momento de avaliação é obrigatório.");
        }

        List<Avaliacao> avaliacoesExistentes = avaliacaoCRUD.listarPorUnidadeCurricular(avaliacao.getUnidadeCurricular().getNome());

        if (avaliacoesExistentes != null) {
            boolean eAtualizacao = avaliacoesExistentes.stream()
                    .anyMatch(a -> a.getEstudante().getNumeroMec() == avaliacao.getEstudante().getNumeroMec()
                            && a.getMomento().equalsIgnoreCase(avaliacao.getMomento()));

            if (!eAtualizacao) {
                long contagem = avaliacoesExistentes.stream()
                        .filter(a -> a.getEstudante().getNumeroMec() == avaliacao.getEstudante().getNumeroMec())
                        .count();

                if (contagem >= 3) {
                    return new Resultado<>(false, "O estudante já atingiu o limite máximo de 3 avaliações para esta UC.");
                }
            }
        }

        return avaliacaoCRUD.registarAvaliacao(avaliacao);
    }

    public Resultado<String> obterStatusAprovacao(int numeroMec, String nomeUC) {
        List<Avaliacao> avaliacoesAluno = avaliacaoCRUD.listarPorEstudante(numeroMec);
        if(avaliacoesAluno == null || avaliacoesAluno.isEmpty()) {
            return new Resultado<>("Sem classificação atribuída", true);
        }

        DAL.UnidadeCurricularCRUD unidadeCurricularCRUD = new DAL.UnidadeCurricularCRUD();
        model.UnidadeCurricular unidadeCurricular = unidadeCurricularCRUD.procurarPorNome(nomeUC);

        if (unidadeCurricular == null || unidadeCurricular.getNome() == null || unidadeCurricular.getMomentosAvaliacao().isEmpty()) {
            return new Resultado<>("Erro: UC sem momentos de avaliação definidos.", false);
        }

        List<String> momentosValidos = unidadeCurricular.getMomentosAvaliacao();
        double somaNotas = 0.0;

        for (Avaliacao av : avaliacoesAluno) {
            if (av.getUnidadeCurricular().getNome().equalsIgnoreCase(nomeUC)
                    && av.getNota() != null
                    ) {
                somaNotas += av.getNota();
            }
        }
        int totalMomentosExigidos = avaliacoesAluno.size();
        double media = somaNotas / totalMomentosExigidos;
        media = Math.round(media * 100.0) / 100.0;

        String estado = (media >= 9.5) ? "APROVADO" : "REPROVADO";
        String mensagemFinal = String.format("Média: %.2f valores - %s", media, estado);

        return new Resultado<>(mensagemFinal, true);
    }

    public List<Avaliacao> listarAvaliacoesPorUC(String nomeUC) {
        return (nomeUC == null || nomeUC.trim().isEmpty()) ? null : avaliacaoCRUD.listarPorUnidadeCurricular(nomeUC);
    }
}