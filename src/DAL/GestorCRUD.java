package DAL;

import model.Docente;
import model.Gestor;
import model.Resultado;

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
                if (dados.length == 7) {
                    Gestor gestor = new Gestor(
                            dados[0],//nome
                            dados[1],//morada
                            Integer.parseInt(dados[2]),//nif
                            LocalDate.parse(dados[3]),//data de nascimento
                            dados[4],//email
                            dados[5],//senha
                            dados[6]//Cargo
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
                String linha = String.format("%s;%s;%s;%s;%s;%s;%s",
                        safe(gestor.getNome()),
                        safe(gestor.getMorada()),
                        safe(gestor.getNif()),
                        safe(gestor.getDataNascimento()),
                        safe(gestor.getEmail()),
                        safe(gestor.getHash()),
                        safe(gestor.getCargo()));
                print.println(linha);
            }
        } catch (IOException | NumberFormatException e) {
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

    public Resultado atualizarSenha(Gestor gestor){
        Resultado res = new Resultado();
        if(gestor != null){
            for (int i = 0; i < gestores.size(); i++) {
                if (gestores.get(i).getNif() == gestor.getNif()) {
                    gestores.set(i, gestor);
                    guardarTodosNoFicheiro();
                    res.success = true;
                    return res;
                }
            }
        }
        res.success = false;
        res.errorMessage = "Erro ao atualizar o ficheiro do gestor";
        return res;
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

    private String safe(Object o){
        return (o == null) ? "SEM REGISTO" : o.toString();
    }
}
