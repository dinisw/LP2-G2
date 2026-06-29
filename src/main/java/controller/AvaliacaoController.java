package controller;

import DAL.DAOFactory;
import DAL.IAvaliacaoDAO;
import DAL.ICursoDAO;
import DAL.IUnidadeCurricularDAO;
import model.Avaliacao;
import model.Curso;
import model.Resultado;
import model.UnidadeCurricular;

import java.util.List;
import java.util.stream.Collectors;

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

        // Bloquear lançamento de nota (não pré-inscrição com nota nula) se o ano ainda não foi iniciado
        if (avaliacao.getNota() != null && deveBloquearPorAnoNaoIniciado(avaliacao.getUnidadeCurricular())) {
            return new Resultado<>(false, "Bloqueado: O ano letivo desta UC ainda não foi iniciado.");
        }

        List<Avaliacao> avaliacoesExistentes = avaliacaoDAO.listarPorUnidadeCurricular(avaliacao.getUnidadeCurricular().getNome());
        if (avaliacoesExistentes != null) {
            // Filtrar as avaliações deste estudante nesta UC
            List<Avaliacao> doEstudante = avaliacoesExistentes.stream()
                    .filter(a -> a.getEstudante().getNumeroMec() == avaliacao.getEstudante().getNumeroMec())
                    .collect(Collectors.toList());

            // Contar momentos DISTINTOS já usados (não o total de registos)
            long momentosDistintos = doEstudante.stream()
                    .map(a -> a.getMomento().toLowerCase().trim())
                    .distinct()
                    .count();

            // O novo momento já existe → é um update (upsert), sempre permitido
            boolean momentoJaExiste = doEstudante.stream()
                    .anyMatch(a -> a.getMomento().trim().equalsIgnoreCase(avaliacao.getMomento().trim()));

            if (momentosDistintos >= 3 && !momentoJaExiste) {
                return new Resultado<>(false, "O estudante já atingiu o limite máximo de 3 momentos de avaliação para esta UC.");
            }
        }

        return avaliacaoDAO.registarAvaliacao(avaliacao);
    }

    public Resultado<String> obterStatusAprovacao(int numeroMec, String nomeUC) {
        List<Avaliacao> avaliacoesAluno = avaliacaoDAO.listarPorEstudante(numeroMec);
        if (avaliacoesAluno == null || avaliacoesAluno.isEmpty()) {
            return new Resultado<>("Sem classificação atribuída", true);
        }

        IUnidadeCurricularDAO unidadeCurricularDAO = DAOFactory.getUnidadeCurricularDAO();
        model.UnidadeCurricular unidadeCurricular = unidadeCurricularDAO.procurarPorNome(nomeUC);

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

        if (notasEncontradas == 0) return new Resultado<>("Sem classificação atribuída", true);

        double media = Math.round((somaNotas / notasEncontradas) * 100.0) / 100.0;
        String estado = (media >= 9.5) ? "APROVADO" : "REPROVADO";
        return new Resultado<>(String.format("Média: %.2f valores - %s", media, estado), true);
    }

    public List<Avaliacao> listarAvaliacoesPorUC(String nomeUC) {
        return (nomeUC == null || nomeUC.trim().isEmpty()) ? null : avaliacaoDAO.listarPorUnidadeCurricular(nomeUC);
    }

    /**
     * Bloqueia o registo de avaliação se o ano letivo correspondente ainda não tiver sido
     * iniciado para o curso que contém esta UC.
     */
    private boolean deveBloquearPorAnoNaoIniciado(UnidadeCurricular uc) {
        try {
            ICursoDAO cursoDAO = DAOFactory.getCursoDAO();
            for (Curso curso : cursoDAO.getCursos()) {
                boolean ucPertence = curso.getUnidadeCurriculars().stream()
                        .anyMatch(u -> u.getNome().equalsIgnoreCase(uc.getNome()));
                if (ucPertence) {
                    // Se esta UC pertence a um curso, o ano curricular dela deve estar iniciado
                    if (!curso.isAnoIniciado(uc.getAnoCurricular())) {
                        return true;
                    }
                    return false; // encontrou o curso — ano iniciado, não bloquear
                }
            }
        } catch (Exception e) {
            // Em caso de erro ao consultar cursos, não bloquear (fail-open)
        }
        return false; // UC sem curso associado → não bloquear
    }
}
