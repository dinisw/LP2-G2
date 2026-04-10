package DAL;
import model.Estudante;
import model.Resultado;

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
                if(dados.length >= 9){
                    String hash = dados[6];
                    boolean ativo = Boolean.parseBoolean(dados[8]);

                    Estudante estudante = new Estudante(
                            dados[0], // nome
                            dados[1], // morada
                            Integer.parseInt(dados[2]), // nif
                            LocalDate.parse(dados[3]), // dataNascimento
                            dados[4], // email
                            Integer.parseInt(dados[5]), // numeroMec
                            hash, // hash
                            dados[7],
                            ativo
                    );

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

    public Resultado atualizarSenha(Estudante estudante){
        Resultado res = new Resultado();
        if(estudante != null){
            for (int i = 0; i < estudantes.size(); i++) {
                if (estudantes.get(i).getNumeroMec() == estudante.getNumeroMec()) {
                    estudantes.set(i, estudante);
                    guardarTodosNoFicheiro();
                    res.success = true;
                    return res;
                }
            }
        }
        res.success = false;
        res.errorMessage = "Erro ao atualizar o ficheiro do estudante";
        return res;
    }

    private void guardarTodosNoFicheiro() {
        try (PrintWriter print = new PrintWriter(new FileWriter(CAMINHO_FICHEIRO))) {
            for (Estudante estudante : estudantes) {
                String linha = String.format("%s;%s;%s;%s;%s;%s;%s;%s;%s",
                        safe(estudante.getNome()),
                        safe(estudante.getMorada()),
                        safe(estudante.getNif()),
                        safe(estudante.getDataNascimento()),
                        safe(estudante.getEmail()),
                        safe(estudante.getNumeroMec()),
                        safe(estudante.getHash()),
                        safe(estudante.getNomeCurso()),
                        estudante.isAtivo());
                print.println(linha);
            }
        } catch (IOException e) {
            System.out.println("Erro ao guardar estudantes: " + e.getMessage());
        }
    }

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

    public int gerarNumeroMecanografico() {
        int anoAtual = java.time.LocalDate.now().getYear() % 100; // mudar quando for criado opção para avançar o tempo manualmento
        int prefixo = 100 + anoAtual;

        int maxSequencia = 0;

        for (Estudante estudante : estudantes) {
            int mec = estudante.getNumeroMec();
            int anoMec = (mec / 10000) % 100;

            if (anoMec == anoAtual) {
                int sequencia = mec % 10000;
                if (sequencia > maxSequencia) {
                    maxSequencia = sequencia;
                }
            }
        }
        int novaSequencia = maxSequencia + 1;

        return (prefixo * 10000) + novaSequencia;
    }

    public Estudante procurarNumeroMec(int numeroMecanografico) {
        for (int i = 0; i < estudantes.size(); i++) {
            if (estudantes.get(i).getNumeroMec() == numeroMecanografico) {
                return estudantes.get(i);
            }
        }
        return null;
    }

    private String safe(Object o){
        return (o == null) ? "SEM REGISTO" : o.toString();
    }
}
