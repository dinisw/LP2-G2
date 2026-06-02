package controller;

import DAL.DAOFactory;
import DAL.IAvaliacaoDAO;
import model.Avaliacao;
import model.Resultado;

import java.util.List;

public class AvaliacaoController {
    private final IAvaliacaoDAO avaliacaoDAO;

    public AvaliacaoController() {
        this.avaliacaoDAO = DAOFactory.getAvaliacaoDAO();
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

        List<Avaliacao> avaliacoesExistentes = avaliacaoDAO.listarPorUnidadeCurricular(avaliacao.getUnidadeCurricular().getNome());
        if (avaliacoesExistentes != null) {
            long contagem = avaliacoesExistentes.stream()
                    .filter(a -> a.getEstudante().getNumeroMec() == avaliacao.getEstudante().getNumeroMec())
                    .count();
            if (contagem >= 3) {
                return new Resultado<>(false, "O estudante já atingiu o limite máximo de 3 avaliações para esta UC.");
            }
        }

        return avaliacaoDAO.registarAvaliacao(avaliacao);
    }

    public Resultado<String> obterStatusAprovacao(int numeroMec, String nomeUC) {
        List<Avaliacao> avaliacoesAluno = avaliacaoDAO.listarPorEstudante(numeroMec);
        if (avaliacoesAluno == null || avaliacoesAluno.isEmpty()) {
            return new Resultado<>("Sem classificação atribuída", true);
        }

        DAL.UnidadeCurricularCRUD unidadeCurricularCRUD = new DAL.UnidadeCurricularCRUD();
        model.UnidadeCurricular unidadeCurricular = unidadeCurricularCRUD.procurarPorNome(nomeUC);

        if (unidadeCurricular == null || unidadeCurricular.getNome() == null || unidadeCurricular.getMomentosAvaliacao().isEmpty()) {
            return new Resultado<>("Erro: UC sem momentos de avaliação definidos.", false);
        }

        List<String> momentosValidos = unidadeCurricular.getMomentosAvaliacao();
        double somaNotas = 0.0;
        int notasEncontradas = 0;

        for (Avaliacao av : avaliacoesAluno) {
            if (av.getUnidadeCurricular().getNome().equalsIgnoreCase(nomeUC)
                    && av.getNota() != null
                    && momentosValidos.stream().anyMatch(m -> m.trim().equalsIgnoreCase(av.getMomento().trim()))) {
                somaNotas += av.getNota();
                notasEncontradas++;
            }
        }

        if (contagemNotas == 0) return new Resultado<>("Sem classificação atribuída", true);

        double media = Math.round((somaNotas / contagemNotas) * 100.0) / 100.0;
        String estado = (media >= 9.5) ? "APROVADO" : "REPROVADO";
        return new Resultado<>(String.format("Média: %.2f valores - %s", media, estado), true);
    }

    public List<Avaliacao> listarAvaliacoesPorUC(String nomeUC) {
        return (nomeUC == null || nomeUC.trim().isEmpty()) ? null : avaliacaoDAO.listarPorUnidadeCurricular(nomeUC);
    }
}
