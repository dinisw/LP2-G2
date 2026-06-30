package controller;

import DAL.DAOFactory;
import DAL.IAvaliacaoDAO;
import DAL.ICursoDAO;
import DAL.IUnidadeCurricularDAO;
import model.Avaliacao;
import model.Curso;
import model.Estudante;
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
        if (avaliacao.getNota() != null && deveBloquearPorAnoNaoIniciado(avaliacao.getUnidadeCurricular(), avaliacao.getEstudante())) {
            return new Resultado<>(false, "Bloqueado: O ano letivo desta UC ainda não foi iniciado.");
        }

        List<Avaliacao> avaliacoesExistentes = avaliacaoDAO.listarPorUnidadeCurricular(avaliacao.getUnidadeCurricular().getNome());
        if (avaliacoesExistentes != null) {
            // Filtrar as avaliações deste estudante nesta UC
            // Filtro defensivo: ignorar registos órfãos (estudante null) que corromperiam o NPE
            List<Avaliacao> doEstudante = avaliacoesExistentes.stream()
                    .filter(a -> a.getEstudante() != null
                            && a.getEstudante().getNumeroMec() == avaliacao.getEstudante().getNumeroMec())
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

        // Deduplicar: para cada momento válido, guardar a nota mais alta (evita inflação por duplicados)
        java.util.Map<String, Double> notasPorMomento = new java.util.HashMap<>();
        for (Avaliacao av : avaliacoesAluno) {
            if (av.getUnidadeCurricular().getNome().equalsIgnoreCase(nomeUC)
                    && av.getNota() != null
                    && momentosValidos.stream().anyMatch(m -> m.trim().equalsIgnoreCase(av.getMomento().trim()))) {
                String chave = av.getMomento().trim().toLowerCase();
                notasPorMomento.merge(chave, av.getNota(), Math::max);
            }
        }

        if (notasPorMomento.isEmpty()) return new Resultado<>("Sem classificação atribuída", true);

        // Dividir pelo total de momentos exigidos (não apenas pelos encontrados)
        // Caso contrário, 1 teste com 14 valores numa UC de 3 momentos daria média 14 → APROVADO indevidamente
        int totalMomentosExigidos = momentosValidos.size();
        if (totalMomentosExigidos == 0) return new Resultado<>("Erro: UC sem momentos definidos", false);

        // Se nem todos os momentos têm nota → reprovado automaticamente
        if (notasPorMomento.size() < totalMomentosExigidos) {
            long faltam = momentosValidos.stream()
                    .filter(m -> !notasPorMomento.containsKey(m.trim().toLowerCase()))
                    .count();
            return new Resultado<>(String.format("Momentos em falta: %d — REPROVADO (avaliação incompleta)", faltam), true);
        }

        double somaNotas = notasPorMomento.values().stream().mapToDouble(Double::doubleValue).sum();
        double media = Math.round((somaNotas / totalMomentosExigidos) * 100.0) / 100.0;
        String estado = (media >= 9.5) ? "APROVADO" : "REPROVADO";
        return new Resultado<>(String.format("Média: %.2f valores - %s", media, estado), true);
    }

    public List<Avaliacao> listarAvaliacoesPorUC(String nomeUC) {
        return (nomeUC == null || nomeUC.trim().isEmpty()) ? null : avaliacaoDAO.listarPorUnidadeCurricular(nomeUC);
    }

    /**
     * Bloqueia o registo de avaliação se o ano letivo correspondente ainda não tiver sido
     * iniciado para o curso ESPECÍFICO do estudante que está a ser avaliado.
     * Usa o nomeCurso do estudante para evitar ambiguidade quando a UC existe em múltiplos cursos.
     */
    private boolean deveBloquearPorAnoNaoIniciado(UnidadeCurricular uc, Estudante estudante) {
        try {
            ICursoDAO cursoDAO = DAOFactory.getCursoDAO();
            String nomeCursoEstudante = (estudante != null) ? estudante.getNomeCurso() : null;

            // Se conhecemos o curso do estudante, validar apenas esse curso
            if (nomeCursoEstudante != null && !nomeCursoEstudante.isBlank()) {
                Curso cursoEstudante = cursoDAO.procurarPorNome(nomeCursoEstudante);
                if (cursoEstudante != null) {
                    boolean ucPertence = cursoEstudante.getUnidadeCurriculars().stream()
                            .anyMatch(u -> u.getNome().equalsIgnoreCase(uc.getNome()));
                    if (ucPertence && !cursoEstudante.isAnoIniciado(uc.getAnoCurricular())) return true;
                    return false;
                }
            }

            // Fallback: procurar em todos os cursos (comportamento anterior)
            for (Curso curso : cursoDAO.getCursos()) {
                boolean ucPertence = curso.getUnidadeCurriculars().stream()
                        .anyMatch(u -> u.getNome().equalsIgnoreCase(uc.getNome()));
                if (ucPertence) {
                    return !curso.isAnoIniciado(uc.getAnoCurricular());
                }
            }
        } catch (Exception e) {
            // fail-open em caso de erro
        }
        return false;
    }
}
