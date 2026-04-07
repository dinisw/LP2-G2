package DAL;

import model.Docente;
import model.Estudante;
import model.Resultado;
import model.UnidadeCurricular;
import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DocenteCRUD {
    private static final String CAMINHO_FICHEIRO = "docentes.csv";
    private List<Docente> docentes;
    private UnidadeCurricularCRUD ucCRUD;

    public DocenteCRUD() {
        this.docentes = new ArrayList<>();
        this.ucCRUD = new UnidadeCurricularCRUD();
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
                if (dados.length >= 9) {
                    // New format with UC names
                    Docente docente = new Docente(
                            dados[0], // nome
                            dados[1], // morada
                            Integer.parseInt(dados[2]), // nif
                            LocalDate.parse(dados[3]), // dataNascimento
                            dados[4], // email
                            dados[5], // hash
                            dados[7], // sigla
                            new ArrayList<>(), // listaAvaliacao
                            new ArrayList<>()  // unidadesCurriculares
                    );
                    String ucNames = dados[8];
                    if (!ucNames.isEmpty()) {
                        for (String nome : ucNames.split(";")) {
                            UnidadeCurricular uc = ucCRUD.procurarPorNome(nome.trim());
                            if (uc != null) {
                                docente.adicionarUnidadeCurricular(uc);
                            }
                        }
                    }
                    docentes.add(docente);
                } else if (dados.length >= 8) {
                    // Old format without UC names
                    Docente docente = new Docente(
                            dados[0], // nome
                            dados[1], // morada
                            Integer.parseInt(dados[2]), // nif
                            LocalDate.parse(dados[3]), // dataNascimento
                            dados[4], // email
                            dados[5], // hash
                            dados[6], // sigla
                            new ArrayList<>(), // listaAvaliacao
                            new ArrayList<>()  // unidadesCurriculares
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
                String ucNames = docente.getUnidadesCurriculares().stream()
                        .map(UnidadeCurricular::getNome)
                        .collect(Collectors.joining(";"));
                String linha = String.format("%s;%s;%s;%s;%s;%s;%s;%s",
                        safe(docente.getNome()),
                        safe(docente.getMorada()),
                        safe(docente.getNif()),
                        safe(docente.getDataNascimento()),
                        safe(docente.getEmail()),
                        safe(docente.getHash()),
                        safe(docente.getSigla()),
                        safe(ucNames));
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

    public Resultado atualizarSenha(Docente docente){
        Resultado res = new Resultado();
        if(docente != null){
            for (int i = 0; i < docentes.size(); i++) {
                if (docentes.get(i).getNif() == docente.getNif()) {
                    docentes.set(i, docente);
                    guardarTodosNoFicheiro();
                    res.success = true;
                    return res;
                }
            }
        }
        res.success = false;
        res.errorMessage = "Erro ao atualizar o ficheiro do docente";
        return res;
    }

    public Docente procurarPorSigla(String sigla) {
        for (model.Docente docente : docentes) {
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

    private String safe(Object o){
        return (o == null) ? "SEM REGISTO" : o.toString();
    }
}
