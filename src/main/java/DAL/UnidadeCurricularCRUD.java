package DAL;

import model.UnidadeCurricular;
import model.Docente;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class UnidadeCurricularCRUD {
    private static final String CAMINHO_FICHEIRO = "ucs.csv";
    private List<UnidadeCurricular> unidadeCurriculars;
    private int proximoid = 0;

    public UnidadeCurricularCRUD() {
        this.unidadeCurriculars = new ArrayList<>();
        carregarFicheiro();
    }

    private void carregarFicheiro() {
        File ficheiro = new File(CAMINHO_FICHEIRO);
        if (!ficheiro.exists()) return;

        DAL.DocenteCRUD docenteCRUD = new DAL.DocenteCRUD();

        try (BufferedReader reader = new BufferedReader(new FileReader(CAMINHO_FICHEIRO))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                String[] dados = linha.split(";", -1);
                if (dados.length < 4) continue;

                // Detectar formato: novo (id;nome;ano;sem;docente;momentos) vs antigo (nome;ano;sem;docente;momentos)
                boolean formatoNovo = false;
                try {
                    Integer.parseInt(dados[0].trim());
                    formatoNovo = true;
                } catch (NumberFormatException ignored) {}

                int id, ano, semestre;
                String nome, siglaDocente, momentosStr;

                if (formatoNovo && dados.length >= 5) {
                    id = Integer.parseInt(dados[0].trim());
                    nome = dados[1];
                    ano = Integer.parseInt(dados[2]);
                    semestre = Integer.parseInt(dados[3]);
                    siglaDocente = dados[4];
                    momentosStr = dados.length >= 6 ? dados[5] : "";
                } else {
                    proximoid++;
                    id = proximoid;
                    nome = dados[0];
                    ano = Integer.parseInt(dados[1]);
                    semestre = Integer.parseInt(dados[2]);
                    siglaDocente = dados[3];
                    momentosStr = dados.length >= 5 ? dados[4] : "";
                }

                if (id > proximoid) proximoid = id;

                Docente docente = null;
                if (!siglaDocente.equals("SEM REGISTO")) {
                    docente = docenteCRUD.procurarPorSigla(siglaDocente);
                }

                UnidadeCurricular unidadeCurricular = new UnidadeCurricular(nome, id, ano, semestre, docente, new java.util.ArrayList<>());

                if (!momentosStr.trim().isEmpty()) {
                    String[] momentos = momentosStr.split(",");
                    for (String momento : momentos) {
                        unidadeCurricular.adicionarMomento(momento);
                    }
                }
                unidadeCurriculars.add(unidadeCurricular);
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
                String linha = String.format("%d;%s;%d;%d;%s;%s",
                        unidadeCurricular.getId(),
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

        proximoid++;
        unidadeCurricular.setId(proximoid);
        unidadeCurriculars.add(unidadeCurricular);
        guardarTodosNoFicheiro();
        return true;
    }

    public List<UnidadeCurricular> getUnidadeCurriculars() {
        return new ArrayList<>(unidadeCurriculars);
    }

    // READ - Procurar por ID
    public UnidadeCurricular procurarPorId(int id) {
        for (UnidadeCurricular uc : unidadeCurriculars) {
            if (uc.getId() == id) {
                return uc;
            }
        }
        return null;
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
                ucAtualizada.setId(unidadeCurriculars.get(i).getId());
                unidadeCurriculars.set(i, ucAtualizada);
                guardarTodosNoFicheiro();
                return true;
            }
        }
        return false;
    }

    // UPDATE por ID
    public boolean atualizarUCPorId(int id, UnidadeCurricular ucAtualizada) {
        if (ucAtualizada == null) return false;

        for (int i = 0; i < unidadeCurriculars.size(); i++) {
            if (unidadeCurriculars.get(i).getId() == id) {
                String nomeAtual = unidadeCurriculars.get(i).getNome();
                if (!nomeAtual.equalsIgnoreCase(ucAtualizada.getNome()) && procurarPorNome(ucAtualizada.getNome()) != null) {
                    return false;
                }
                ucAtualizada.setId(id);
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

    // DELETE - Eliminar UC por ID
    public boolean eliminarUCPorId(int id) {
        for (int i = 0; i < unidadeCurriculars.size(); i++) {
            if (unidadeCurriculars.get(i).getId() == id) {
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