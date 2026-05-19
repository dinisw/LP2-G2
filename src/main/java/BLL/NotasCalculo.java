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

        for (Avaliacao avaliacao : avaliacoes) {
            if (avaliacao.getNota() != null) {
                String nomeUC = avaliacao.getUnidadeCurricular().getNome();
                melhoresNotas.put(nomeUC, Math.max(melhoresNotas.getOrDefault(nomeUC, 0.0), avaliacao.getNota()));
            }
        }

        double soma = 0;
        for (Double nota : melhoresNotas.values()) {
                soma += nota;
        }
        return melhoresNotas.size() > 0 ? soma / melhoresNotas.size() : 0.0;
    }
}