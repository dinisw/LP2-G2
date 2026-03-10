package DAL;
import model.Estudante;

import java.io.*;
import java.time.LocalDate;
import java.util.List;

public class EstudanteCRUD {
    private static final String CAMINHO_FICHEIRO = "estudantes.csv";
    private List<Estudante> estudantes;

    private void carregarFicheiro(){
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
    public boolean criarEstudante(Estudante estudante){
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
            estudantes.add(estudante);
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
        return null; // Retorna null se o estudante não for encontrado
    }

}
