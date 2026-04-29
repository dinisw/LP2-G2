package DAL;

import DAL.core.CsvRepositorio;
import model.Avaliacao;
import model.Estudante;
import model.Resultado;
import model.UnidadeCurricular;

import java.util.ArrayList;
import java.util.List;

public class AvaliacaoCRUD extends CsvRepositorio<Avaliacao> {
    private final List<Avaliacao> avaliacoes;

    public AvaliacaoCRUD() {
        super("avaliacoes.csv");
        this.avaliacoes = carregarTodos();
    }

    @Override
    protected Avaliacao mapearLinhaParaEntidade(String[] dados) {
        if (dados.length < 4) return null;
        try {
            String momento = dados[0];
            Double nota = dados[1].trim().equalsIgnoreCase("null") ? null : Double.parseDouble(dados[1].replace(",", "."));
            String nomeUC = dados[2];
            int numMec = Integer.parseInt(dados[3]);

            UnidadeCurricular uc = new UnidadeCurricularCRUD().procurarPorNome(nomeUC);
            Estudante estudante = new EstudanteCRUD().lerEstudante(numMec);

            if (uc != null && estudante != null) {
                return new Avaliacao(momento, nota, uc, estudante);
            }
        } catch (Exception e) {
            return null; // Ignora linhas corrompidas
        }
        return null;
    }

    @Override
    protected String mapearEntidadeParaLinha(Avaliacao avaliacao) {
        String notaStr = (avaliacao.getNota() == null) ? "null" : String.format(java.util.Locale.US, "%.2f", avaliacao.getNota());
        return String.join(DELIMITADOR,
                safe(avaliacao.getMomento()),
                notaStr,
                safe(avaliacao.getUnidadeCurricular().getNome()),
                String.valueOf(avaliacao.getEstudante().getNumeroMec())
        );
    }

    public Resultado<Avaliacao> registarAvaliacao(Avaliacao avaliacao) {
        if (avaliacao == null) return new Resultado<>(false, "Avaliação inválida.");
        
        avaliacoes.add(avaliacao);
        return guardarTodos(avaliacoes) ? new Resultado<>(avaliacao, true) : new Resultado<>(false, "Erro ao guardar avaliação no CSV.");
    }

    // --- LEITURAS ---
    public List<Avaliacao> listarPorEstudante(int numMec) {
        List<Avaliacao> resultado = new ArrayList<>();
        for (Avaliacao a : avaliacoes) {
            if (a.getEstudante().getNumeroMec() == numMec) resultado.add(a);
        }
        return resultado;
    }

    public List<Avaliacao> listarPorUnidadeCurricular(String nomeUC) {
        List<Avaliacao> resultado = new ArrayList<>();
        for (Avaliacao a : avaliacoes) {
            if (a.getUnidadeCurricular().getNome().equalsIgnoreCase(nomeUC)) resultado.add(a);
        }
        return resultado;
    }
}
