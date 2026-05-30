package DAL;

import model.Gestor;
import model.Resultado;
import java.time.LocalDate;
import java.util.List;

public class GestorCRUD extends AbstractCsvCRUD<Gestor> implements IGestorDAO {

    public GestorCRUD() {
        super("gestores.csv");
    }

    @Override
    protected Gestor mapearLinhaParaEntidade(String[] colunas) {
        try {
            int id = Integer.parseInt(colunas[0]);
            String nome = colunas[1];
            String morada = colunas[2];
            int nif = Integer.parseInt(colunas[3]);
            LocalDate dataNascimento = LocalDate.parse(colunas[4]);
            String email = colunas[5];
            String hash = colunas[6];
            String cargo = colunas[7];

            return new Gestor(id, nome, morada, nif, dataNascimento, email, hash, cargo);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected String mapearEntidadeParaLinha(Gestor g) {
        return String.format("%d;%s;%s;%d;%s;%s;%s;%s",
                g.getId(), g.getNome(), g.getMorada(), g.getNif(),
                g.getDataNascimento().toString(), g.getEmail(), g.getHash(), g.getCargo());
    }

    public boolean registarGestor(Gestor gestor) {
        int proximoID = dados.isEmpty() ? 1 : dados.get(dados.size() - 1).getId() + 1;
        gestor.setId(proximoID);
        dados.add(gestor);
        guardarTodosNoFicheiro();
        return true;
    }

    public List<Gestor> getGestores() {
        return dados;
    }

    public Gestor procurarPorEmail(String email) {
        return dados.stream().filter(g -> g.getEmail().equalsIgnoreCase(email)).findFirst().orElse(null);
    }

    public Gestor procurarPorNif(int nif) {
        return dados.stream().filter(g -> g.getNif() == nif).findFirst().orElse(null);
    }

    public boolean atualizarGestor(Gestor gestor) {
        for (int i = 0; i < dados.size(); i++) {
            if (dados.get(i).getNif() == gestor.getNif()) {
                gestor.setId(dados.get(i).getId());
                dados.set(i, gestor);
                guardarTodosNoFicheiro();
                return true;
            }
        }
        return false;
    }

    public boolean eliminarGestor(int nif) {
        Gestor remover = procurarPorNif(nif);
        if (remover != null) {
            dados.remove(remover);
            guardarTodosNoFicheiro();
            return true;
        }
        return false;
    }

    public Gestor getGestorPorID(int id) {
        return dados.stream().filter(g -> g.getId() == id).findFirst().orElse(null);
    }
}