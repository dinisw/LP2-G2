package BLL;

import model.Avaliacao;
import java.util.List;

public class NotasCalculo {

    public static Double calcularMedia(List<Avaliacao> avaliacoes) {
        if (avaliacoes == null || avaliacoes.isEmpty()) {
            return null;
        }

        double soma = 0;
        int count = 0;

        for (Avaliacao avaliacao : avaliacoes) {
            if (avaliacao.getNota() != null) {
                soma += avaliacao.getNota();
                count++;
            }
        }

        return count > 0 ? soma / count : null;
    }
}