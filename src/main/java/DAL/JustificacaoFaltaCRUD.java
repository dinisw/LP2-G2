package DAL;

import model.Estudante;
import model.JustificacaoFalta;
import model.Presenca;
import model.TipoJustificacao;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class JustificacaoFaltaCRUD extends AbstractCsvCRUD<JustificacaoFalta> implements IJustificacaoFaltaDAO {

    public JustificacaoFaltaCRUD() {
        super("justificacoes_falta.csv");
    }

    private int proximoId() {
        return dados.stream().mapToInt(JustificacaoFalta::getId).max().orElse(0) + 1;
    }

    @Override
    protected JustificacaoFalta mapearLinhaParaEntidade(String[] colunas) {
        try {
            // id;numeroMec;presencaId;tipo;descricao;dataSubmissao;estado;observacaoGestor
            int id                          = Integer.parseInt(colunas[0]);
            int numeroMec                   = Integer.parseInt(colunas[1]);
            int presencaId                  = Integer.parseInt(colunas[2]);
            TipoJustificacao tipo           = TipoJustificacao.fromString(colunas[3]);
            String descricao               = colunas[4];
            LocalDate dataSubmissao        = LocalDate.parse(colunas[5]);
            JustificacaoFalta.Estado estado = JustificacaoFalta.Estado.valueOf(colunas[6]);
            String obsGestor               = colunas.length > 7 ? colunas[7] : "";

            EstudanteCRUD estudanteCRUD = new EstudanteCRUD();
            PresencaCRUD presencaCRUD   = new PresencaCRUD();

            Estudante est  = estudanteCRUD.lerEstudante(numeroMec);
            Presenca pres  = presencaCRUD.procurarPorId(presencaId);
            if (est == null || pres == null) return null;

            JustificacaoFalta j = new JustificacaoFalta(est, pres, tipo, descricao);
            j.setId(id);
            j.setDataSubmissao(dataSubmissao);
            j.setEstado(estado);
            j.setObservacaoGestor(obsGestor);
            return j;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected String mapearEntidadeParaLinha(JustificacaoFalta j) {
        return String.format("%d;%d;%d;%s;%s;%s;%s;%s",
                j.getId(),
                j.getEstudante().getNumeroMec(),
                j.getPresenca().getId(),
                j.getTipo().name(),
                sanitizar(j.getDescricao()),
                j.getDataSubmissao(),
                j.getEstado().name(),
                sanitizar(j.getObservacaoGestor() != null ? j.getObservacaoGestor() : ""));
    }

    @Override
    public boolean registarJustificacao(JustificacaoFalta justificacao) {
        justificacao.setId(proximoId());
        dados.add(justificacao);
        guardarTodosNoFicheiro();
        return true;
    }

    @Override
    public boolean atualizarJustificacao(JustificacaoFalta justificacao) {
        for (int i = 0; i < dados.size(); i++) {
            if (dados.get(i).getId() == justificacao.getId()) {
                dados.set(i, justificacao);
                guardarTodosNoFicheiro();
                return true;
            }
        }
        return false;
    }

    @Override
    public JustificacaoFalta procurarPorId(int id) {
        return dados.stream().filter(j -> j.getId() == id).findFirst().orElse(null);
    }

    @Override
    public List<JustificacaoFalta> listarPorEstudante(int numeroMec) {
        return dados.stream()
                .filter(j -> j.getEstudante() != null && j.getEstudante().getNumeroMec() == numeroMec)
                .collect(Collectors.toList());
    }

    @Override
    public List<JustificacaoFalta> listarPendentes() {
        return dados.stream()
                .filter(j -> j.getEstado() == JustificacaoFalta.Estado.PENDENTE)
                .collect(Collectors.toList());
    }

    @Override
    public List<JustificacaoFalta> listarTodas() {
        return new ArrayList<>(dados);
    }

    @Override
    public boolean eliminarJustificacao(int id) {
        boolean removido = dados.removeIf(j -> j.getId() == id);
        if (removido) guardarTodosNoFicheiro();
        return removido;
    }
}
