package main.DAL;

import main.model.UnidadeCurricular;
import main.model.Docente;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class UnidadeCurricularCRUD {
    private static final String CAMINHO_FICHEIRO = "ucs.csv";
    private List<UnidadeCurricular> unidadeCurriculars;

    public UnidadeCurricularCRUD() {
        this.unidadeCurriculars = new ArrayList<>();
        carregarFicheiro();
    }

    private void carregarFicheiro() {
        File ficheiro = new File(CAMINHO_FICHEIRO);
        if (!ficheiro.exists()) return;

        main.DAL.DocenteCRUD docenteCRUD = new main.DAL.DocenteCRUD();

        try (BufferedReader reader = new BufferedReader(new FileReader(CAMINHO_FICHEIRO))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                String[] dados = linha.split(";");
                if (dados.length >= 4) {
                    String nome = dados[0];
                    int ano = Integer.parseInt(dados[1]);
                    int semestre = Integer.parseInt(dados[2]);

                    Docente docente = null;
                    if (!dados[3].equals("SEM REGISTO")) {
                        docente = docenteCRUD.procurarPorSigla(dados[3]);
                    }

                    UnidadeCurricular unidadeCurricular = new UnidadeCurricular(nome, ano, semestre, docente);

                    if(dados.length >= 5 && !dados[4].trim().isEmpty()) {
                        String[] momentos = dados[4].split(",");
                        for (String momento : momentos) {
                            unidadeCurricular.adicionarMomento(momento);
                        }
                    }
                    unidadeCurriculars.add(unidadeCurricular);
                }
            }
        } catch (IOException | NumberFormatException e) {
            throw new RuntimeException("Erro interno ao carregar o ficheiro de UCs.", e);
        }
    }

    private void guardarTodosNoFicheiro() {
        try (PrintWriter print = new PrintWriter(new FileWriter(CAMINHO_FICHEIRO))) {
            for (UnidadeCurricular unidadeCurricular : unidadeCurriculars) {
                String siglaDocente = (unidadeCurricular.getDocente() != null) ? unidadeCurricular.getDocente().getSigla() : "SEM REGISTO";
                String momentosStr = "";
                if (unidadeCurricular.getMomentosAvaliacao() != null && !unidadeCurricular.getMomentosAvaliacao().isEmpty()) {
                    momentosStr = String.join(",", unidadeCurricular.getMomentosAvaliacao());
                }
                String linha = String.format("%s;%d;%d;%s;%s",
                        safe(unidadeCurricular.getNome()),
                        unidadeCurricular.getAnoCurricular(),
                        unidadeCurricular.getSemestre(),
                        siglaDocente,
                        momentosStr);
                print.println(linha);
            }
        } catch (IOException e) {
            throw new RuntimeException("Erro interno ao guardar o ficheiro de UCs.", e);
        }
    }

    // CREATE
    public boolean registarUC(UnidadeCurricular unidadeCurricular) {
        if (unidadeCurricular == null || unidadeCurricular.getNome() == null || unidadeCurricular.getNome().trim().isEmpty()) {
            return false;
        }

        if (procurarPorNome(unidadeCurricular.getNome()) != null) {
            return false;
        }

        unidadeCurriculars.add(unidadeCurricular);
        guardarTodosNoFicheiro();
        return true;
    }

    public List<UnidadeCurricular> getUnidadeCurriculars() {
        return new ArrayList<>(unidadeCurriculars);
    }

    // READ - Procurar por nome
    public UnidadeCurricular procurarPorNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            return null;
        }
        for (UnidadeCurricular unidadeCurricular : unidadeCurriculars) {
            if (unidadeCurricular.getNome().equalsIgnoreCase(nome)) {
                return unidadeCurricular;
            }
        }
        return null;
    }

    // READ - Procurar UCs de um docente
    public List<UnidadeCurricular> procurarPorDocente(String siglaDocente) {
        List<UnidadeCurricular> resultado = new ArrayList<>();
        if (siglaDocente == null || siglaDocente.trim().isEmpty()) {
            return resultado;
        }
        for (UnidadeCurricular unidadeCurricular : unidadeCurriculars) {
            if (unidadeCurricular.getDocente() != null && unidadeCurricular.getDocente().getSigla().equalsIgnoreCase(siglaDocente)) {
                resultado.add(unidadeCurricular);
            }
        }
        return resultado;
    }

    // READ - Procurar UCs por ano curricular
    public List<UnidadeCurricular> procurarPorAno(int ano) {
        List<UnidadeCurricular> resultado = new ArrayList<>();
        for (UnidadeCurricular unidadeCurricular : unidadeCurriculars) {
            if (unidadeCurricular.getAnoCurricular() == ano) {
                resultado.add(unidadeCurricular);
            }
        }
        return resultado;
    }

    // UPDATE
    public boolean atualizarUC(String nomeAtual, UnidadeCurricular ucAtualizada) {
        if (nomeAtual == null || nomeAtual.trim().isEmpty() || ucAtualizada == null) {
            return false;
        }

        for (int i = 0; i < unidadeCurriculars.size(); i++) {
            if (unidadeCurriculars.get(i).getNome().equalsIgnoreCase(nomeAtual)) {
                if (!nomeAtual.equalsIgnoreCase(ucAtualizada.getNome()) && procurarPorNome(ucAtualizada.getNome()) != null) {
                    return false;
                }
                unidadeCurriculars.set(i, ucAtualizada);
                guardarTodosNoFicheiro();
                return true;
            }
        }
        return false;
    }

    // DELETE - Eliminar UC por nome
    public boolean eliminarUC(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            return false;
        }

        for (int i = 0; i < unidadeCurriculars.size(); i++) {
            if (unidadeCurriculars.get(i).getNome().equalsIgnoreCase(nome)) {
                unidadeCurriculars.remove(i);
                guardarTodosNoFicheiro();
                return true;
            }
        }
        return false;
    }

    // DELETE - Eliminar todas as UCs de um docente
    public boolean eliminarUCsDoDocente(String siglaDocente) {
        if (siglaDocente == null || siglaDocente.trim().isEmpty()) {
            return false;
        }

        boolean encontrou = false;
        for (int i = unidadeCurriculars.size() - 1; i >= 0; i--) {
            if (unidadeCurriculars.get(i).getDocente() != null && unidadeCurriculars.get(i).getDocente().getSigla().equalsIgnoreCase(siglaDocente)) {
                unidadeCurriculars.remove(i);
                encontrou = true;
            }
        }

        if (encontrou) {
            guardarTodosNoFicheiro();
        }
        return encontrou;
    }

    private String safe(Object o) {
        return (o == null) ? "SEM REGISTO" : o.toString();
    }
}