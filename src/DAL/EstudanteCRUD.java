package DAL;
import model.Estudante;

import java.io.*;
import java.time.LocalDate;
import java.util.List;

public class EstudanteCRUD {
    private static final String CAMINHO_FICHEIRO = "estudantes.csv";
    private List<Estudante> estudantes;
    private int numeroMecCounter;

    public EstudanteCRUD() {
        this.estudantes = new java.util.ArrayList<>();
        this.numeroMecCounter = 10000;
        carregarFicheiro();
    }

    private void carregarFicheiro(){
        File ficheiro = new File(CAMINHO_FICHEIRO);
        if(!ficheiro.exists()){
            return;
        }
        try(BufferedReader reader = new BufferedReader(new FileReader(CAMINHO_FICHEIRO))){
            String linha;
            while((linha = reader.readLine()) != null){
                String[] dados = linha.split(";");
                if(dados.length >= 8){
                    Estudante estudante = new Estudante(
                            dados[0], // nome
                            dados[1], // morada
                            Integer.parseInt(dados[2]), // nif
                            LocalDate.parse(dados[3]), // dataNascimento
                            dados[4], // email
                            Integer.parseInt(dados[6]), // numeroMec
                            dados[5], // palavraPasse
                            dados[7]);
                    estudantes.add(estudante);
                    if(estudante.getNumeroMec() >= numeroMecCounter) {
                        numeroMecCounter = estudante.getNumeroMec() + 1;
                    }
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.out.println("Erro ao carregar estudantes: " + e.getMessage());
        }
    }

    //CREATE
    public boolean registarEstudante(Estudante estudante){
        if (estudante != null) {
            estudantes.add(estudante);
            guardarTodosNoFicheiro();
            return true;
        }
        return false;
    }


    private void guardarTodosNoFicheiro() {
        try (PrintWriter print = new PrintWriter(new FileWriter(CAMINHO_FICHEIRO))) {
            for (Estudante estudante : estudantes) {
                String linha = String.format("%s;%s;%d;%s;%s;%d;%s;%s",
                        estudante.getNome(),
                        estudante.getMorada(),
                        estudante.getNif(),
                        estudante.getDataNascimento(),
                        estudante.getEmail(),
                        estudante.getNumeroMec(),
                        estudante.getPalavraPasse(),
                        estudante.getNomeCurso());
                print.println(linha);
            }
        } catch (IOException e) {
            System.out.println("Erro ao guardar estudantes: " + e.getMessage());
        }
    }

    //Retorna a lista de estudantes
    public List<Estudante> getEstudantes() {
            return estudantes;
    }

    //READ
    public Estudante lerEstudante(int numeroMec) {
        for (Estudante estudante : estudantes) {
            if (estudante.getNumeroMec() == numeroMec) {
                return estudante;
            }
        }
        return null;
    }

    //UPDATE
    public boolean atualizarEstudante(Estudante estudante) {
        if(estudante != null){
            for (int i = 0; i < estudantes.size(); i++) {
                if (estudantes.get(i).getNumeroMec() == estudante.getNumeroMec()) {
                    estudantes.set(i, estudante);
                    guardarTodosNoFicheiro();
                    return true;
                }
            }
        }
        return false;
        }

    //DELETE
    public boolean eliminarEstudante(int numeroMec) {
        for(int i = 0; i < estudantes.size(); i++){
            if(estudantes.get(i).getNumeroMec() == numeroMec){
                estudantes.remove(i);
                guardarTodosNoFicheiro();
                return true;
            }
        }
        return false;
    }

    public Estudante procurarNumeroMec(int numeroMecanografico) {
        for (int i = 0; i < estudantes.size(); i++) {
            if (estudantes.get(i).getNumeroMec() == numeroMecanografico) {
                return estudantes.get(i);
            }
        }
        return null;
    }
}
