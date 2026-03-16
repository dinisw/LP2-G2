package DAL;

import model.Docente;
import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DocenteCRUD {
    private static final String CAMINHO_FICHEIRO = "docentes.csv";
    private List<Docente> docentes;

    public DocenteCRUD() {
        this.docentes = new ArrayList<>();
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
                if (dados.length >= 7) {
                    Docente docente = new Docente(
                            dados[0], // nome
                            dados[1], // morada
                            Integer.parseInt(dados[2]), // nif
                            LocalDate.parse(dados[3]), // dataNascimento
                            dados[4], // email
                            dados[5], // palavraPasse
                            dados[6], // sigla
                            null,     // listaAvaliacao (simplificado para CSV por agora)
                            null      // unidadeCurricular (simplificado para CSV por agora)
                    );
                    docentes.add(docente);
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.out.println("Erro ao carregar docentes: " + e.getMessage());
        }
    }

    private void guardarTodosNoFicheiro() {
        try (PrintWriter print = new PrintWriter(new FileWriter(CAMINHO_FICHEIRO))) {
            for (Docente docente : docentes) {
                String linha = String.format("%s;%s;%d;%s;%s;%s;%s",
                        docente.getNome(),
                        docente.getMorada(),
                        docente.getNif(),
                        docente.getDataNascimento(),
                        docente.getEmail(),
                        docente.getPalavraPasse(),
                        docente.getSigla());
                print.println(linha);
            }
        } catch (IOException e) {
            System.out.println("Erro ao guardar docentes: " + e.getMessage());
        }
    }

    // CREATE
    public boolean registarDocente(Docente docente) {
        if (docente != null && procurarPorNif(docente.getNif()) == null) {
            docentes.add(docente);
            guardarTodosNoFicheiro();
            return true;
        }
        return false;
    }

    // READ
    public List<Docente> getDocentes() {
        return new ArrayList<>(docentes);
    }

    public Docente procurarPorNif(int nif) {
        for (Docente docente : docentes) {
            if (docente.getNif() == nif) {
                return docente;
            }
        }
        return null;
    }

    public Docente procurarPorSigla(String sigla) {
        for (Docente docente : docentes) {
            if (docente.getSigla().equalsIgnoreCase(sigla)) {
                return docente;
            }
        }
        return null;
    }

    // UPDATE
    public boolean atualizarDocente(Docente docenteAtualizado) {
        for (int i = 0; i < docentes.size(); i++) {
            if (docentes.get(i).getNif() == docenteAtualizado.getNif()) {
                docentes.set(i, docenteAtualizado);
                guardarTodosNoFicheiro();
                return true;
            }
        }
        return false;
    }

    // DELETE
    public boolean eliminarDocente(int nif) {
        for (int i = 0; i < docentes.size(); i++) {
            if (docentes.get(i).getNif() == nif) {
                docentes.remove(i);
                guardarTodosNoFicheiro();
                return true;
            }
        }
        return false;
    }
}
