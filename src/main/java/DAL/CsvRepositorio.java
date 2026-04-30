package DAL;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe genérica que encapsula toda a lógica de Leitura e Escrita de CSVs.
 * Qualquer CRUD apenas precisa de dizer como converter um Objeto para String[] e vice-versa.
 */
public abstract class CsvRepositorio<T> {
    private final String caminhoFicheiro;
    protected final String DELIMITADOR = ";";

    public CsvRepositorio(String caminhoFicheiro) {
        this.caminhoFicheiro = caminhoFicheiro;
    }

    // --- LÓGICA GENÉRICA DE LEITURA ---
    protected List<T> carregarTodos() {
        List<T> entidades = new ArrayList<>();
        File ficheiro = new File(caminhoFicheiro);
        if (!ficheiro.exists()) return entidades;

        try (BufferedReader reader = new BufferedReader(new FileReader(ficheiro))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                if (linha.trim().isEmpty()) continue;

                String[] dados = linha.split(DELIMITADOR);
                T entidade = mapearLinhaParaEntidade(dados);
                if (entidade != null) {
                    entidades.add(entidade);
                }
            }
        } catch (IOException e) {
            System.err.println("Erro crítico ao ler o ficheiro: " + caminhoFicheiro);
        }
        return entidades;
    }

    // --- LÓGICA GENÉRICA DE ESCRITA ---
    protected boolean guardarTodos(List<T> entidades) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(caminhoFicheiro))) {
            for (T entidade : entidades) {
                writer.println(mapearEntidadeParaLinha(entidade));
            }
            return true;
        } catch (IOException e) {
            System.err.println("Erro crítico ao guardar no ficheiro: " + caminhoFicheiro);
            return false;
        }
    }

    // --- UTILITÁRIO GENÉRICO DE FORMATAÇÃO ---
    protected String safe(Object o) {
        return (o == null) ? "SEM REGISTO" : o.toString().trim();
    }

    // --- MÉTODOS QUE AS SUBCLASSES VÃO IMPLEMENTAR ---
    protected abstract T mapearLinhaParaEntidade(String[] dados);
    protected abstract String mapearEntidadeParaLinha(T entidade);
}
