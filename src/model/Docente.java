package model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Docente extends Pessoa{
    private UnidadeCurricular unidadeCurricular;
    private String sigla;
    private List<Avaliacao> listaAvaliacao = new ArrayList<>();

    public Docente() {
    }

    public Docente(List<Avaliacao> listaAvaliacao, UnidadeCurricular unidadeCurricular, String sigla) {
        this.listaAvaliacao = listaAvaliacao;
        this.unidadeCurricular = unidadeCurricular;
        this.sigla = sigla;
    }

    public Docente(String nome, String morada, int nif, LocalDate dataNascimento, String email, String hash, String salt, String sigla, List<Avaliacao> listaAvaliacao, UnidadeCurricular unidadeCurricular) {
        super(nome, morada, nif, dataNascimento, email, hash, salt);
        this.listaAvaliacao = (listaAvaliacao != null) ? listaAvaliacao : new ArrayList<>();
        this.sigla = sigla;
        this.unidadeCurricular = unidadeCurricular;
    }

    public String getSigla() {
        return sigla;
    }

    public void setSigla(String sigla) {
        this.sigla = sigla;
    }

    public UnidadeCurricular getUnidadeCurricular() {
        return unidadeCurricular;
    }

    public void setUnidadeCurricular(UnidadeCurricular unidadeCurricular) {
        this.unidadeCurricular = unidadeCurricular;
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
