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
        } catch (IOException e) {
            System.out.println("Erro ao carregar departamentos: " + e.getMessage());
        }
    }

    private void guardarTodosNoFicheiro() {
        try (PrintWriter print = new PrintWriter(new FileWriter(CAMINHO_FICHEIRO))) {
            for (Departamento dep : departamentos) {
                print.println(dep.getNome() + ";" + dep.getSigla());
            }
        } catch (IOException e) {
            System.out.println("Erro ao guardar departamentos: " + e.getMessage());
        }
    }

    // CREATE
    public boolean registarDepartamento(Departamento dep) {
        // Só regista se não existir nenhum com a mesma sigla
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

    // MÉTODO MÁGICO: Procura um objeto Departamento pela sua sigla
    public Departamento procurarPorSigla(String sigla) {
        for (Departamento dep : departamentos) {
            if (dep.getSigla().equalsIgnoreCase(sigla)) {
                return dep;
            }
        }
        return null;
    }

    // UPDATE
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
}