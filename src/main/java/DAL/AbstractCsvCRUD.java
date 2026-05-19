package DAL;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractCsvCRUD<T> {
    protected final String caminhoFicheiro;
    protected List<T> dados;

    public AbstractCsvCRUD(String nomeFicheiro) {
        this.caminhoFicheiro = "src/main/CSVs/" + nomeFicheiro;
        this.dados = new ArrayList<>();
        carregarFicheiro();
    }

    protected void carregarFicheiro() {
        File ficheiro = new File(caminhoFicheiro);
        if (!ficheiro.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(ficheiro))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                if (!linha.trim().isEmpty()) {
                    String[] colunas = linha.split(";", -1);
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

    protected void guardarTodosNoFicheiro() {
        try {
            File ficheiro = new File(caminhoFicheiro);
            File diretorio = ficheiro.getParentFile();
            if (diretorio != null && !diretorio.exists()) {
                diretorio.mkdirs();
            }

            try (PrintWriter writer = new PrintWriter(new FileWriter(ficheiro))) {
                for (T entidade : dados) {
                    writer.println(mapearEntidadeParaLinha(entidade));
                }
            }
        } catch (IOException e) {
            System.err.println("Erro interno ao guardar o ficheiro: " + caminhoFicheiro);
        }
    }

    protected abstract T mapearLinhaParaEntidade(String[] colunas);
    protected abstract String mapearEntidadeParaLinha(T entidade);

    protected String sanitizar(String texto) {
        if (texto == null) return "";
        return texto.replace(";", ",");
    }


}