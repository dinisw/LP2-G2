package BLL;

import model.Avaliacao;
import model.Curso;
import model.Estudante;
import model.UnidadeCurricular;
import java.util.List;

public class EstudanteCalculo {

    private  static boolean isUCAprovada (List<Avaliacao> avaliacoes, String nomeUC) {
        List<Avaliacao> avaliacoesDestaUC = avaliacoes.stream().filter(a -> a.getUnidadeCurricular().getNome().equalsIgnoreCase(nomeUC) && a.getNota() != null).toList();

        if (avaliacoesDestaUC.isEmpty()) {
            return false;
        }

        UnidadeCurricular ucAtualizada = avaliacoesDestaUC.get(0).getUnidadeCurricular();
        List<String> momentosValidos = ucAtualizada.getMomentosAvaliacao();
        int totalMomentosExigidos = momentosValidos.size();

        if (totalMomentosExigidos == 0) return false;

        boolean todosOsMomentosTêmNota = momentosValidos.stream()
                .allMatch(momento -> avaliacoesDestaUC.stream()
                        .anyMatch(a -> a.getMomento().equalsIgnoreCase(momento)));
        if (!todosOsMomentosTêmNota) return false;

        double somaNotas = 0;
        for (Avaliacao avaliacao : avaliacoesDestaUC) {
            if (momentosValidos.stream().anyMatch(m -> m.equalsIgnoreCase(avaliacao.getMomento()))) {
                somaNotas += avaliacao.getNota();
            }
        }

        double mediaFinal = somaNotas / totalMomentosExigidos;
        return mediaFinal >= 9.5;
    }

    public static int calcularAnoDesbloqueado(Estudante estudante, Curso curso) {
        if (estudante == null || curso == null) return 1;

        List<Avaliacao> avaliacoes = estudante.getListaAvaliacoes();

        long totalInscritas = curso.getUnidadeCurriculars().stream().filter(uc -> uc.getAnoCurricular() <= estudante.getAnoLetivo()).count();

        if (totalInscritas == 0) return 1;

        long aprovadasGlobais = curso.getUnidadeCurriculars().stream().filter(u -> isUCAprovada(avaliacoes, u.getNome())).count();

        double aproveitamento = (double) aprovadasGlobais / totalInscritas;
        if (aproveitamento >= 0.60) {
            return Math.min(estudante.getAnoLetivo() + 1, curso.getDuracao());
        }
        return estudante.getAnoLetivo();
    }

    public static boolean isCursoConcluido(Estudante estudante, Curso curso) {
        if (estudante == null || curso == null || curso.getUnidadeCurriculars().isEmpty()) return false;
        int totalUCsCurso = curso.getUnidadeCurriculars().size();
        long totalAprovadas = curso.getUnidadeCurriculars().stream().filter(u -> isUCAprovada(estudante.getListaAvaliacoes(), u.getNome())).count();
        return totalAprovadas == totalUCsCurso;
    }
}