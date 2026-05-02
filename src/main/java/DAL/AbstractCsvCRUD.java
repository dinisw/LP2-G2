package DAL;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractCsvCRUD<T> {
    protected final String caminhoFicheiro;
    protected List<T> dados; // Cache em memória comum a todos os CRUDs

    public AbstractCsvCRUD(String caminhoFicheiro) {
        this.caminhoFicheiro = caminhoFicheiro;
        this.dados = new ArrayList<>();
        carregarFicheiro();
    }

    // Método universal de leitura
    protected void carregarFicheiro() {
        File ficheiro = new File(caminhoFicheiro);
        if (!ficheiro.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(ficheiro))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                if (!linha.trim().isEmpty()) {
                    String[] colunas = linha.split(";");
                    T entidade = mapearLinhaParaEntidade(colunas);
                    if (entidade != null) {
                        dados.add(entidade);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Erro interno ao carregar o ficheiro: " + caminhoFicheiro);
        }
    }

    // Método universal de escrita (Resolve o teu problema no AvaliacaoCRUD!)
    protected void guardarTodosNoFicheiro() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(caminhoFicheiro))) {
            for (T entidade : dados) {
                writer.println(mapearEntidadeParaLinha(entidade));
            }
        } catch (IOException e) {
            System.err.println("Erro interno ao guardar o ficheiro: " + caminhoFicheiro);
        }
    }

    // Métodos abstratos que obrigam as subclasses a ensinar como converter a sua Entidade específica
    protected abstract T mapearLinhaParaEntidade(String[] colunas);
    protected abstract String mapearEntidadeParaLinha(T entidade);
}