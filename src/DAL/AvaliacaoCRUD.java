package DAL;

import model.Avaliacao;
import model.Estudante;
import model.UnidadeCurricular;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class AvaliacaoCRUD {
    public static final String CAMINHO_FICHEIRO = "avaliacoes.csv";
    private List<Avaliacao> avaliacoes;

    public AvaliacaoCRUD() {
        this.avaliacoes = new ArrayList<>();
        carregarFicheiro();
    }
    private void carregarFicheiro() {
        File ficheiro = new File(CAMINHO_FICHEIRO);
        if (!ficheiro.exists()) return;

        EstudanteCRUD estudanteCRUD = new EstudanteCRUD();
        UnidadeCurricularCRUD unidadeCurricularCRUD = new UnidadeCurricularCRUD();

        try (BufferedReader reader = new BufferedReader(new FileReader(CAMINHO_FICHEIRO))){
            String linha;
            while ((linha = reader.readLine()) != null) {
                String[] dados = linha.split(";");
                if (dados.length >= 4) {
                    String momento = dados[0];
                    Double nota = dados[1].equals("null") ? null : Double.parseDouble(dados[1]);
                    String nomeUnidadeCurricular = dados[2];
                    int numMec = Integer.parseInt(dados[3]);

                    UnidadeCurricular unidadeCurricular = unidadeCurricularCRUD.procurarPorNome(nomeUnidadeCurricular);
                    Estudante estudante = estudanteCRUD.lerEstudante(numMec);

                    if(unidadeCurricular != null && estudante != null) {
                        Avaliacao avalicao = new Avaliacao(momento, nota, unidadeCurricular, estudante);
                        avaliacoes.add(avalicao);
                    }
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.out.println("Erro ao carregar avaliações: " + e.getMessage());
        }
    }

    public void guardarTodosNoFicheiro() {
        try (PrintWriter printWriter = new PrintWriter(new FileWriter(CAMINHO_FICHEIRO))) {
            for (Avaliacao avaliacao : avaliacoes) {
                String notaStr = (avaliacao.getNota() == null) ? "null" : String.valueOf(avaliacao.getNota());
                String linha = String.format("%s;%s;%s;%d",
                        avaliacao.getMomento(),
                        notaStr,
                        avaliacao.getUnidadeCurricular().getNome(),
                        avaliacao.getEstudante().getNumeroMec());
                printWriter.println(linha);
            }
        } catch (IOException e) {
            System.out.println("Erro ao guardar avaliações: " + e.getMessage());
        }
    }

    public boolean registarAvaliacao(Avaliacao avaliacao) {
        if (avaliacao != null) {
            avaliacoes.add(avaliacao);
            guardarTodosNoFicheiro();
            return true;
        }
        return false;
    }

    public List<Avaliacao> listarPorEstudante(int numMec) {
        List<Avaliacao> notaEstudante = new ArrayList<>();
        for (Avaliacao avaliacao : avaliacoes) {
            if (avaliacao.getEstudante().getNumeroMec() == numMec) {
                notaEstudante.add(avaliacao);
            }
        }
        return notaEstudante;
    }
}
