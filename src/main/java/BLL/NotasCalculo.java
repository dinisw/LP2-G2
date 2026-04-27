package BLL;

import model.Avaliacao;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotasCalculo {

    public static double calcularMedia(List<Avaliacao> avaliacoes) {

        if (avaliacoes == null || avaliacoes.isEmpty()) {
            return 0.0;
        }

        Map<String, Double> melhoresNotas = new HashMap<>();

        for (Avaliacao av : avaliacoes) {
            if (av.getNota() != null) {
                String nomeUC = av.getUnidadeCurricular().getNome();
                if (!melhoresNotas.containsKey(nomeUC) || av.getNota() > melhoresNotas.get(nomeUC)) {
                    melhoresNotas.put(nomeUC, av.getNota());
                }
            }
        }

        double soma = 0;
        int count = 0;
        for (Double nota : melhoresNotas.values()) {
            if (nota >= 9.5) {
                soma += nota;
                count++;
            }
        }

        return count > 0 ? soma / count : 0.0;
    }
}