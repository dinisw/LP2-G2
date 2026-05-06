package BLL;

import model.Avaliacao;
import model.Curso;
import model.Estudante;

import java.util.List;

public class EstudanteCalculo {

    public static int calcularAnoDesbloqueado(Estudante estudante, Curso curso) {
        if (estudante == null || curso == null) return 1;

        List<Avaliacao> avaliacoes = estudante.getListaAvaliacoes();
        int anoDesbloqueado = 1;

        long totalAno1 = curso.getUnidadeCurriculars().stream().filter(u -> u.getAnoCurricular() == 1).count();
        long aprovadasAno1 = avaliacoes.stream().filter(a -> a.getUnidadeCurricular().getAnoCurricular() == 1 && a.getNota() != null && a.getNota() >= 9.5).count();
        if (totalAno1 == 0) totalAno1 = 5;

        long inscritasAno1 = avaliacoes.stream().filter(a -> a.getUnidadeCurricular().getAnoCurricular() == 1).count();

        if (inscritasAno1 > 0) {
            double aproveitamentoAno1 = (double) aprovadasAno1 / totalAno1;

            if (aproveitamentoAno1 >= 0.60) {
                anoDesbloqueado = 2;

                long totalAno2 = curso.getUnidadeCurriculars().stream().filter(u -> u.getAnoCurricular() == 2).count();
                long aprovadasAno2 = avaliacoes.stream().filter(a -> a.getUnidadeCurricular().getAnoCurricular() == 2 && a.getNota() != null && a.getNota() >= 9.5).count();
                if (totalAno2 == 0) totalAno2 = 5;

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
        long totalAprovadas = estudante.getListaAvaliacoes().stream()
                .filter(a -> a.getNota() != null && a.getNota() >= 9.5)
                .count();

        return totalAprovadas == totalUCsCurso;
    }
}