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

        double somaNotas = 0;
        for (Avaliacao avaliacao : avaliacoesDestaUC) {
            if (momentosValidos.contains(avaliacao.getMomento())) {
                somaNotas += avaliacao.getNota();
            }
        }

        double mediaFinal = somaNotas / totalMomentosExigidos;
        return mediaFinal >= 9.5;
    }

    public static int calcularAnoDesbloqueado(Estudante estudante, Curso curso) {
        if (estudante == null || curso == null) return 1;

        List<Avaliacao> avaliacoes = estudante.getListaAvaliacoes();
        int anoDesbloqueado = 1;

        long totalAno1 = curso.getUnidadeCurriculars().stream().filter(u -> u.getAnoCurricular() == 1).count();
        if (totalAno1 == 0) totalAno1 = 5;
        long aprovadasAno1 = curso.getUnidadeCurriculars().stream().filter(u -> u.getAnoCurricular() == 1 && isUCAprovada(avaliacoes, u.getNome())).count();

        long inscritasAno1 = avaliacoes.stream().filter(a -> a.getUnidadeCurricular().getAnoCurricular() == 1).count();

        if (inscritasAno1 > 0) {
            double aproveitamentoAno1 = (double) aprovadasAno1 / totalAno1;

            if (aproveitamentoAno1 >= 0.60) {
                anoDesbloqueado = 2;

                long totalAno2 = curso.getUnidadeCurriculars().stream().filter(u -> u.getAnoCurricular() == 2).count();
                if (totalAno2 == 0) totalAno2 = 5;

                long aprovadasAno2 = curso.getUnidadeCurriculars().stream().filter(u -> u.getAnoCurricular() == 2 && isUCAprovada(avaliacoes, u.getNome())).count();
                long inscritasAno2 = avaliacoes.stream().filter(a -> a.getUnidadeCurricular().getAnoCurricular() == 2).count();

                if (inscritasAno2 > 0) {
                    double aproveitamentoAno2 = (double) aprovadasAno2 / totalAno2;
                    if (aproveitamentoAno2 >= 0.60) {
                        anoDesbloqueado = 3;
                    }
                }
            }
        }
        return anoDesbloqueado;
    }

    public static boolean isCursoConcluido(Estudante estudante, Curso curso) {
        if (estudante == null || curso == null || curso.getUnidadeCurriculars().isEmpty()) return false;
        int totalUCsCurso = curso.getUnidadeCurriculars().size();
        long totalAprovadas = curso.getUnidadeCurriculars().stream().filter(u -> isUCAprovada(estudante.getListaAvaliacoes(), u.getNome())).count();
        return totalAprovadas == totalUCsCurso;
    }
}