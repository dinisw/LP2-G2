package model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Docente extends Pessoa{
    private UnidadeCurricular unidadeCurricular;
    private List<Avaliacao> listaAvaliacao = new ArrayList<>();

    public Docente() {
    }

    public Docente(List<Avaliacao> listaAvaliacao, UnidadeCurricular unidadeCurricular) {
        this.listaAvaliacao = listaAvaliacao;
        this.unidadeCurricular = unidadeCurricular;
    }

    public Docente(String nome, String morada, int nif, LocalDate dataNascimento, String email, String sigla, int numeroMec, List<Avaliacao> listaAvaliacao) {
        super(nome, morada, nif, dataNascimento, email, sigla, numeroMec, "");
        this.listaAvaliacao = listaAvaliacao;}

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
