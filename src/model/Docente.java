package model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Docente extends Utilizador {
    private List<UnidadeCurricular> unidadesCurriculares;
    private String sigla;
    private List<Avaliacao> listaAvaliacao = new ArrayList<>();


    public Docente(List<Avaliacao> listaAvaliacao, List<UnidadeCurricular> unidadesCurriculares, String sigla) {
        this.listaAvaliacao = listaAvaliacao;
        this.unidadesCurriculares = unidadesCurriculares;
        this.sigla = sigla;
    }

    public Docente() {
        this.listaAvaliacao = new ArrayList<>();
        this.unidadesCurriculares = new ArrayList<>();
        this.sigla = "";
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

    public boolean adicionarUnidadeCurricular(UnidadeCurricular uc) {
        if (unidadesCurriculares.contains(uc)) {
            return false;
        }
        unidadesCurriculares.add(uc);
        return true;
    }

    public boolean removerUnidadeCurricular(UnidadeCurricular uc) {
        return unidadesCurriculares.remove(uc);
    }

    public List<Avaliacao> getListaAvaliacao() {
        return listaAvaliacao;
    }

    public void setListaAvaliacao(List<Avaliacao> listaAvaliacao) {
        this.listaAvaliacao = listaAvaliacao;
    }

    public boolean registarAvaliacao(Avaliacao nota) {
        if(listaAvaliacao.size() >= 3){
            return false;
        }
        listaAvaliacao.add(nota);
        return true;
    }


}
