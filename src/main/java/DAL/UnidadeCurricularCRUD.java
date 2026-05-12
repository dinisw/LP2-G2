package DAL;

import model.Docente;
import model.UnidadeCurricular;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class UnidadeCurricularCRUD extends AbstractCsvCRUD<UnidadeCurricular> {

    public UnidadeCurricularCRUD() {
        super("ucs.csv");
    }

    @Override
    protected UnidadeCurricular mapearLinhaParaEntidade(String[] colunas) {
        try {
            int id = Integer.parseInt(colunas[0]);
            String nome = colunas[1];
            int ano = Integer.parseInt(colunas[2]);
            int semestre = Integer.parseInt(colunas[3]);
            String siglaDocente = colunas[4];

            DocenteCRUD docenteCRUD = new DocenteCRUD();
            Docente docente = docenteCRUD.procurarPorSigla(siglaDocente);

            List<String> momentos = new ArrayList<>();
            if (colunas.length > 5 && !colunas[5].isEmpty()) {
                momentos = Arrays.asList(colunas[5].split(","));
            }

            return new UnidadeCurricular(nome, id, ano, semestre, docente, momentos);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected String mapearEntidadeParaLinha(UnidadeCurricular uc) {
        String siglaDoc = uc.getDocente() != null ? uc.getDocente().getSigla() : "N/A";
        String momentosStr = uc.getMomentosAvaliacao() != null ? String.join(",", uc.getMomentosAvaliacao()) : "";
        return String.format("%d;%s;%d;%d;%s;%s",
                uc.getId(), uc.getNome(), uc.getAnoCurricular(), uc.getSemestre(), siglaDoc, momentosStr);
    }

    public boolean registarUC(UnidadeCurricular uc) {
        int proximoId = dados.isEmpty() ? 1 : dados.get(dados.size() - 1).getId() + 1;
        uc.setId(proximoId);
        dados.add(uc);
        guardarTodosNoFicheiro();
        return true;
    }

    public List<UnidadeCurricular> getUnidadeCurriculars() { return dados; }

    public UnidadeCurricular procurarPorNome(String nome) {
        return dados.stream().filter(u -> u.getNome().equalsIgnoreCase(nome)).findFirst().orElse(null);
    }

    public UnidadeCurricular procurarPorId(int id) {
        return dados.stream().filter(u -> u.getId() == id).findFirst().orElse(null);
    }

    public boolean atualizarUC(String nomeAtual, UnidadeCurricular uc) {
        for (int i = 0; i < dados.size(); i++) {
            if (dados.get(i).getNome().equalsIgnoreCase(nomeAtual)) {
                uc.setId(dados.get(i).getId());
                dados.set(i, uc);
                guardarTodosNoFicheiro();
                return true;
            }
        }
        return false;
    }

    public boolean atualizarUCPorId(int id, UnidadeCurricular uc) {
        for (int i = 0; i < dados.size(); i++) {
            if (dados.get(i).getId() == id) {
                uc.setId(id);
                dados.set(i, uc);
                guardarTodosNoFicheiro();
                return true;
            }
        }
        return false;
    }

    public boolean eliminarUCPorId(int id) {
        UnidadeCurricular remover = procurarPorId(id);
        if (remover != null) {
            dados.remove(remover);
            guardarTodosNoFicheiro();
            return true;
        }
        return false;
    }

    public boolean eliminarUC(String nomeAtual) {
        UnidadeCurricular remover = procurarPorNome(nomeAtual);
        if (remover != null) {
            dados.remove(remover);
            guardarTodosNoFicheiro();
            return true;
        }
        return false;
    }
}