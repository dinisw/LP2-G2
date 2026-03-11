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
            System.out.println("Ficheiro de estudantes não encontrado. Tente novamente.");
            return;
        }
        try(BufferedReader reader = new BufferedReader(new FileReader(CAMINHO_FICHEIRO))){
            String linha;
            while((linha = reader.readLine()) != null){
                String[] dados = linha.split(";");
                if(dados.length == 10){
                    Estudante estudante = new Estudante(
                            dados[0], // nome
                            dados[1], // morada
                            Integer.parseInt(dados[2]), // nif
                            LocalDate.parse(dados[3]), // dataNascimento
                            dados[4], // email
                            Integer.parseInt(dados[5]), // numeroMec
                            dados[6], // palavraPasse
                            dados[7]);
                    estudantes.add(estudante);
                }
            }
        } catch (IOException e) {
            System.out.println("Erro ao carregar estudantes: " + e.getMessage());
        }
    }

    //CREATE
    public void registarEstudante(Estudante estudante){
        String numero = "Est" + numeroMecCounter++;
        String email = numero.toLowerCase() + "@isep.ipp.pt";
        String palavraPasse = "ISEP";
        int anoLetivo = 1;

        estudantes.add(estudante);
        guardarFicheiro(estudante);
    }


    public boolean guardarFicheiro(Estudante estudante) {
        try (FileWriter writer = new FileWriter(CAMINHO_FICHEIRO, true);
        PrintWriter print = new PrintWriter(writer)) {
            String formato = String.format("%s,%s,%d,%s,%s,%s,%d,%s,%d,%s",
                    estudante.getNome(),
                    estudante.getMorada(),
                    estudante.getNif(),
                    estudante.getDataNascimento(),
                    estudante.getEmail(),
                    estudante.getPalavraPasse(),
                    estudante.getNumeroMec(),
                    estudante.getNomeCurso(),
                    estudante.getAnoLetivo(),
                    estudante.getListaAvaliacoes());
            print.println(formato);
            return true;
        } catch (IOException e) {
            System.out.println("Erro ao criar estudante: " + e.getMessage());
            return false;
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
                    guardarFicheiro(estudante);
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
