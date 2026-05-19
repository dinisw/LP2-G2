package model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Docente extends Utilizador {
    private List<UnidadeCurricular> unidadesCurriculares;
    private String sigla;
    private List<Avaliacao> listaAvaliacao = new ArrayList<>();


    public Docente() {
        super("", "", 0, null, "", "");
        this.unidadesCurriculares = new ArrayList<>();
        this.sigla = "";
        this.listaAvaliacao = new ArrayList<>();
    }

    public Docente(String nome, String morada, int nif, LocalDate dataNascimento, String email, String hash, String sigla, List<Avaliacao> listaAvaliacao, List<UnidadeCurricular> unidadesCurriculares) {
        super(nome, morada, nif, dataNascimento, email, hash);
        this.listaAvaliacao = (listaAvaliacao != null) ? listaAvaliacao : new ArrayList<>();
        this.sigla = sigla;
        this.unidadesCurriculares = (unidadesCurriculares != null) ? unidadesCurriculares : new ArrayList<>();
    }

    public String getSigla() {
        return sigla;
    }

    public void setSigla(String sigla) {
        this.sigla = sigla;
    }

    public List<UnidadeCurricular> getUnidadesCurriculares() {
        return new ArrayList<>(unidadesCurriculares);
    }

    public void setUnidadesCurriculares(List<UnidadeCurricular> unidadesCurriculares) {
        this.unidadesCurriculares = unidadesCurriculares;
    }

    public List<Avaliacao> getListaAvaliacao() {
        return new ArrayList<>(listaAvaliacao);
    }

    public void setListaAvaliacao(List<Avaliacao> listaAvaliacao) {
        this.listaAvaliacao = (listaAvaliacao != null) ? listaAvaliacao : new ArrayList<>();
    }

    public boolean adicionarUnidadeCurricular(UnidadeCurricular unidadeCurricular) {
        if (unidadesCurriculares.contains(unidadeCurricular)) {
            return false;
        }
        unidadesCurriculares.add(unidadeCurricular);
        return true;
    }

    @Override
    public String toString() {
        return String.format(
                "\n==============================\n" +
                "       FICHA DE DOCENTE       \n" +
                "==============================\n" +
                "%s\n" +
                "  Sigla: %s\n" +
                "==============================",
                super.toString(),
                sigla
        );
    }
}
