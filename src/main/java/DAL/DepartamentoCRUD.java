package DAL;

import model.Departamento;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DepartamentoCRUD {
    private static final String CAMINHO_FICHEIRO = "departamentos.csv";
    private List<Departamento> departamentos;

    public DepartamentoCRUD() {
        this.departamentos = new ArrayList<>();
        carregarFicheiro();
    }

    private void carregarFicheiro() {
        File ficheiro = new File(CAMINHO_FICHEIRO);
        if (!ficheiro.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(CAMINHO_FICHEIRO))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                String[] dados = linha.split(";");
                if (dados.length >= 2) {
                    Departamento dep = new Departamento(dados[0], dados[1]);
                    departamentos.add(dep);
                }
            }
        } catch (IOException | NumberFormatException e) {
            throw new RuntimeException("Erro interno ao carregar o ficheiro de departamentos.", e);
        }
    }

    private void guardarTodosNoFicheiro() {
        try (PrintWriter print = new PrintWriter(new FileWriter(CAMINHO_FICHEIRO))) {
            for (Departamento dep : departamentos) {
                String linha = String.format("%s;%s",
                        safe(dep.getNome()),
                        safe(dep.getSigla()));
                print.println(linha);
            }
        } catch (IOException | NumberFormatException e) {
            throw new RuntimeException("Erro interno ao carregar o ficheiro de departamentos.", e);
        }
    }

    // CREATE
    public boolean registarDepartamento(Departamento dep) {
        if (dep != null && procurarPorSigla(dep.getSigla()) == null) {
            departamentos.add(dep);
            guardarTodosNoFicheiro();
            return true;
        }
        return false;
    }

    // READ
    public List<Departamento> getDepartamentos() {
        return new ArrayList<>(departamentos);
    }

    public Departamento procurarPorSigla(String sigla) {
        for (Departamento dep : departamentos) {
            if (dep.getSigla().equalsIgnoreCase(sigla)) {
                return dep;
            }
        }
        return null;
    }

    //UPDATE
    public boolean atualizarDepartamento(Departamento depAtualizado) {
        for (int i = 0; i < departamentos.size(); i++) {
            if (departamentos.get(i).getSigla().equalsIgnoreCase(depAtualizado.getSigla())) {
                departamentos.set(i, depAtualizado);
                guardarTodosNoFicheiro();
                return true;
            }
        }
        return false;
    }

    // DELETE
    public boolean eliminarDepartamento(String sigla) {
        for (int i = 0; i < departamentos.size(); i++) {
            if (departamentos.get(i).getSigla().equalsIgnoreCase(sigla)) {
                departamentos.remove(i);
                guardarTodosNoFicheiro();
                return true;
            }
        }
        return false;
    }

    private String safe(Object o) {
        return (o == null) ? "SEM REGISTO" : o.toString();
    }
}