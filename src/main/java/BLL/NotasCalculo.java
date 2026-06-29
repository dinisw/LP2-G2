package BLL;

import model.Avaliacao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotasCalculo {

    /**
     * Calcula a média geral de um estudante, consistente com a lógica de
     * EstudanteCalculo.isUCAprovada():
     *   1. Para cada UC, calcula a média aritmética dos momentos de avaliação.
     *   2. A média geral é a média das médias por UC.
     *
     * (versão anterior usava o máximo por UC — inconsistente com a progressão de ano)
     */
    public static double calcularMedia(List<Avaliacao> avaliacoes) {
        if (avaliacoes == null || avaliacoes.isEmpty()) return 0.0;

        // Agrupa notas por UC
        Map<String, List<Double>> notasPorUC = new HashMap<>();
        for (Avaliacao av : avaliacoes) {
            if (av.getNota() != null && av.getUnidadeCurricular() != null) {
                String nomeUC = av.getUnidadeCurricular().getNome();
                notasPorUC.computeIfAbsent(nomeUC, k -> new ArrayList<>()).add(av.getNota());
            }
        }

        if (notasPorUC.isEmpty()) return 0.0;

        // Média das médias por UC
        double somaMediasUC = 0.0;
        for (List<Double> notas : notasPorUC.values()) {
            double mediaUC = notas.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            somaMediasUC += mediaUC;
        }

        double mediaGeral = somaMediasUC / notasPorUC.size();
        return Math.round(mediaGeral * 100.0) / 100.0;
    }
}
