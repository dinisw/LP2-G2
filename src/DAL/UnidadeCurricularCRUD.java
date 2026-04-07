package DAL;

import model.UnidadeCurricular;
import model.Docente;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class UnidadeCurricularCRUD {
    private static final String CAMINHO_FICHEIRO = "ucs.csv";
    private List<UnidadeCurricular> ucs;

    public UnidadeCurricularCRUD() {
        this.ucs = new ArrayList<>();
        carregarFicheiro();
    }

    private void carregarFicheiro() {
        File ficheiro = new File(CAMINHO_FICHEIRO);
        if (!ficheiro.exists()) return;

        // Instanciação Local para evitar o StackOverflowError
        DAL.DocenteCRUD docenteCRUD = new DAL.DocenteCRUD();

        try (BufferedReader reader = new BufferedReader(new FileReader(CAMINHO_FICHEIRO))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                String[] dados = linha.split(";");
                if (dados.length >= 2) {
                    String nome = dados[0];
                    int ano = Integer.parseInt(dados[1]);

                    Docente docente = null;
                    if (dados.length >= 3 && !dados[2].equals("SEM REGISTO")) {
                        docente = docenteCRUD.procurarPorSigla(dados[2]);
                    }

                    // CORREÇÃO: Estava a passar 'null' fixo, agora passa a variável docente correta!
                    UnidadeCurricular uc = new UnidadeCurricular(nome, ano, docente);
                    ucs.add(uc);
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.out.println("Erro ao carregar UCs: " + e.getMessage());
        }
    }

    private void guardarTodosNoFicheiro() {
        try (PrintWriter print = new PrintWriter(new FileWriter(CAMINHO_FICHEIRO))) {
            for (UnidadeCurricular uc : ucs) {
                String siglaDocente = (uc.getDocente() != null) ? uc.getDocente().getSigla() : "SEM REGISTO";

                // CORREÇÃO: Faltava adicionar o %s para gravar o docente no ficheiro
                String linha = String.format("%s;%s;%s",
                        safe(uc.getNome()),
                        uc.getAnoCurricular(),
                        siglaDocente);
                print.println(linha);
            }
        } catch (IOException e) {
            System.out.println("Erro ao guardar UCs: " + e.getMessage());
        }
    }

    // CREATE
    public boolean registarUC(UnidadeCurricular uc) {
        if (uc == null || uc.getNome() == null || uc.getNome().trim().isEmpty()) {
            return false;
        }

        if (procurarPorNome(uc.getNome()) != null) {
            return false;
        }

        ucs.add(uc);
        guardarTodosNoFicheiro();
        return true;
    }

    public List<UnidadeCurricular> getUcs() {
        return new ArrayList<>(ucs);
    }

    // READ - Procurar por nome
    public UnidadeCurricular procurarPorNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            return null;
        }
        for (UnidadeCurricular uc : ucs) {
            if (uc.getNome().equalsIgnoreCase(nome)) {
                return uc;
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
        for (UnidadeCurricular uc : ucs) {
            if (uc.getDocente() != null && uc.getDocente().getSigla().equalsIgnoreCase(siglaDocente)) {
                resultado.add(uc);
            }
        }
        return resultado;
    }

    // READ - Procurar UCs por ano curricular
    public List<UnidadeCurricular> procurarPorAno(int ano) {
        List<UnidadeCurricular> resultado = new ArrayList<>();
        for (UnidadeCurricular uc : ucs) {
            if (uc.getAnoCurricular() == ano) {
                resultado.add(uc);
            }
        }
        return resultado;
    }

    // UPDATE
    public boolean atualizarUC(String nomeAtual, UnidadeCurricular ucAtualizada) {
        if (nomeAtual == null || nomeAtual.trim().isEmpty() || ucAtualizada == null) {
            return false;
        }

        for (int i = 0; i < ucs.size(); i++) {
            if (ucs.get(i).getNome().equalsIgnoreCase(nomeAtual)) {

                // Nota: A regra de "não permitir atualizar se tiver alunos" deve estar no Controller

                if (!nomeAtual.equalsIgnoreCase(ucAtualizada.getNome()) && procurarPorNome(ucAtualizada.getNome()) != null) {
                    return false;
                }
                ucs.set(i, ucAtualizada);
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

        for (int i = 0; i < ucs.size(); i++) {
            if (ucs.get(i).getNome().equalsIgnoreCase(nome)) {

                // Nota: A regra de "não permitir eliminar se tiver alunos" deve estar no Controller

                ucs.remove(i);
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
        for (int i = ucs.size() - 1; i >= 0; i--) {
            if (ucs.get(i).getDocente() != null && ucs.get(i).getDocente().getSigla().equalsIgnoreCase(siglaDocente)) {
                ucs.remove(i);
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