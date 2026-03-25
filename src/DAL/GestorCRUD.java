package DAL;

import model.Gestor;
import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class GestorCRUD {
    private static final String CAMINHO_FICHEIRO = "gestores.csv";
    private List<Gestor> gestores;

    public GestorCRUD() {
        this.gestores = new ArrayList<>();
        carregarFicheiro();
    }

    private void carregarFicheiro() {
        File ficheiro = new File(CAMINHO_FICHEIRO);
        if (!ficheiro.exists()) {
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(CAMINHO_FICHEIRO))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                String[] dados = linha.split(";");
                if (dados.length == 8) {
                    Gestor gestor = new Gestor(
                            dados[0], // nome
                            dados[1], // morada
                            Integer.parseInt(dados[2]), // nif
                            LocalDate.parse(dados[3]), // dataNascimento
                            dados[4], // email
                            dados[5], // hash
                            dados[6], // salt
                            dados[7]  // cargo
                    );
                    gestores.add(gestor);
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.out.println("Erro ao carregar gestores: " + e.getMessage());
        }
    }

    private void guardarTodosNoFicheiro() {
        try (PrintWriter print = new PrintWriter(new FileWriter(CAMINHO_FICHEIRO))) {
            for (Gestor gestor : gestores) {
                String linha = String.format("%s;%s;%d;%s;%s;%s;%s;%s",
                        gestor.getNome(),
                        gestor.getMorada(),
                        gestor.getNif(),
                        gestor.getDataNascimento(),
                        gestor.getEmail(),
                        gestor.getHash(),
                        gestor.getSalt(),
                        gestor.getCargo());
                print.println(linha);
            }
        } catch (IOException e) {
            System.out.println("Erro ao guardar gestores: " + e.getMessage());
        }
    }

    // CREATE
    public boolean registarGestor(Gestor gestor) {
        if (gestor != null && procurarPorNif(gestor.getNif()) == null) {
            gestores.add(gestor);
            guardarTodosNoFicheiro();
            return true;
        }
        return false;
    }

    // READ
    public List<Gestor> getGestores() {
        return new ArrayList<>(gestores);
    }

    public Gestor procurarPorNif(int nif) {
        for (Gestor gestor : gestores) {
            if (gestor.getNif() == nif) {
                return gestor;
            }
        }
        return null;
    }

    public Gestor procurarPorEmail(String email) {
        for (Gestor gestor : gestores) {
            if (gestor.getEmail().equals(email)) {
                return gestor;
            }
        }
        return null;
    }

    // UPDATE
    public boolean atualizarGestor(Gestor gestorAtualizado) {
        for (int i = 0; i < gestores.size(); i++) {
            if (gestores.get(i).getNif() == gestorAtualizado.getNif()) {
                gestores.set(i, gestorAtualizado);
                guardarTodosNoFicheiro();
                return true;
            }
        }
        return false;
    }

    // DELETE
    public boolean eliminarGestor(int nif) {
        for (int i = 0; i < gestores.size(); i++) {
            if (gestores.get(i).getNif() == nif) {
                gestores.remove(i);
                guardarTodosNoFicheiro();
                return true;
            }
        }
        return false;
    }
}
