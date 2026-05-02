package DAL;

import model.Avaliacao;
import model.Estudante;
import model.Resultado;
import model.UnidadeCurricular;

import java.util.List;
import java.util.stream.Collectors;

public class AvaliacaoCRUD extends AbstractCsvCRUD<Avaliacao> {

    public AvaliacaoCRUD() {
        super("avaliacoes.csv"); // O construtor pai trata de criar a lista 'dados' e carregar o ficheiro
    }

    @Override
    protected Avaliacao mapearLinhaParaEntidade(String[] colunas) {
        try {
            String momento = colunas[0];
            Double nota = colunas[1].equalsIgnoreCase("null") ? null : Double.parseDouble(colunas[1]);
            String nomeUC = colunas[2];
            int numMec = Integer.parseInt(colunas[3]);

            UnidadeCurricularCRUD ucCRUD = new UnidadeCurricularCRUD();
            UnidadeCurricular uc = ucCRUD.procurarPorNome(nomeUC);

            EstudanteCRUD estudanteCRUD = new EstudanteCRUD();
            Estudante estudante = estudanteCRUD.lerEstudante(numMec);

            if (uc != null && estudante != null) {
                return new Avaliacao(momento, nota, uc, estudante);
            }
        } catch (Exception e) {

        }
        return null;
    }

    @Override
    protected String mapearEntidadeParaLinha(Avaliacao a) {
        String notaStr = a.getNota() == null ? "null" : String.valueOf(a.getNota());
        return String.format("%s;%s;%s;%d",
                a.getMomento(),
                notaStr,
                a.getUnidadeCurricular().getNome(),
                a.getEstudante().getNumeroMec());
    }

    // --- REGRAS DE NEGÓCIO ---

    public Resultado<Avaliacao> registarAvaliacao(Avaliacao avaliacao) {
        boolean atualizado = false;

        for (Avaliacao av : dados) {
            if (av.getEstudante().getNumeroMec() == avaliacao.getEstudante().getNumeroMec() &&
                    av.getUnidadeCurricular().getNome().equalsIgnoreCase(avaliacao.getUnidadeCurricular().getNome()) &&
                    av.getMomento().equalsIgnoreCase(avaliacao.getMomento())) {

                av.setNota(avaliacao.getNota());
                atualizado = true;
                break;
            }
        }

        if (!atualizado) {
            dados.add(avaliacao);
        }

        guardarTodosNoFicheiro();
        return new Resultado<>(avaliacao, true);
    }

    public List<Avaliacao> listarPorEstudante(int numeroMec) {
        return dados.stream()
                .filter(a -> a.getEstudante().getNumeroMec() == numeroMec)
                .collect(Collectors.toList());
    }

    public List<Avaliacao> listarPorUnidadeCurricular(String nomeUC) {
        return dados.stream()
                .filter(a -> a.getUnidadeCurricular().getNome().equalsIgnoreCase(nomeUC))
                .collect(Collectors.toList());
    }
}