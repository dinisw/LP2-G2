package BLL;

import model.Avaliacao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotasCalculo {

    /**
     * Calcula a média geral de um estudante.
     * Consistente com EstudanteCalculo.isUCAprovada():
     *  - Para cada UC, deduplica momentos (guarda nota máxima por momento duplicado).
     *  - A média de cada UC é a média aritmética dos seus momentos únicos.
     *  - A média geral é a média das médias por UC.
     */
    public static double calcularMedia(List<Avaliacao> avaliacoes) {
        if (avaliacoes == null || avaliacoes.isEmpty()) return 0.0;

        // UC → (momento → nota máxima)
        Map<String, Map<String, Double>> notasPorUCeMomento = new HashMap<>();
        for (Avaliacao av : avaliacoes) {
            if (av.getNota() != null && av.getUnidadeCurricular() != null && av.getMomento() != null) {
                String nomeUC = av.getUnidadeCurricular().getNome();
                String chave  = av.getMomento().trim().toLowerCase();
                notasPorUCeMomento
                        .computeIfAbsent(nomeUC, k -> new HashMap<>())
                        .merge(chave, av.getNota(), Math::max);
            }
        }

        if (notasPorUCeMomento.isEmpty()) return 0.0;

        double somaMediasUC = 0.0;
        for (Map<String, Double> momentos : notasPorUCeMomento.values()) {
            double mediaUC = momentos.values().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            somaMediasUC += mediaUC;
        }

        double mediaGeral = somaMediasUC / notasPorUCeMomento.size();
        return Math.round(mediaGeral * 100.0) / 100.0;
    }
}
