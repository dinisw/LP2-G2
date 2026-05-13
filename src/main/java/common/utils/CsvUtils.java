package common.utils;

import java.util.ArrayList;
import java.util.List;

public class CsvUtils {
    public static List<String> separarStringPorVirgula(String input) {
        List<String> lista = new ArrayList<>();
        if (input == null || input.trim().isEmpty()) {
            return lista;
        }
        String[] partes = input.split(",");
        for (String parte : partes) {
            String valorLimpo = parte.trim();
            if (!valorLimpo.isEmpty()) {
                lista.add(valorLimpo);
            }
        }
        return lista;
    }
}
