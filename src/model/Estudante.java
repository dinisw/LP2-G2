package model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Estudante extends Utilizador {

    private String nomeCurso;
    private int numeroMec;
    private int anoLetivo;
    private List<Avaliacao> listaAvaliacoes;

    public Estudante(String nome, String morada, int nif, LocalDate dataNascimento, String email, int numeroMec, String hash, String nomeCurso, boolean ativo) {
        super(nome, morada, nif, dataNascimento, email, hash);
        this.nomeCurso = nomeCurso;
        this.anoLetivo = 1;
        this.listaAvaliacoes = new ArrayList<>();
        this.numeroMec = numeroMec;
        this.setAtivo(ativo);
    }

    public int getNumeroMec() {
        return numeroMec;
    }

    public void setNumeroMec(int numeroMec) {
        this.numeroMec = numeroMec;
    }

    public void setListaAvaliacoes(List<Avaliacao> listaAvaliacoes) {
        this.listaAvaliacoes = listaAvaliacoes;
    }

    public String getNomeCurso() {
        return nomeCurso;
    }

    public void setNomeCurso(String nomeCurso) {
        this.nomeCurso = nomeCurso;
    }

    public int getAnoLetivo() {
        return anoLetivo;
    }

    public void setAnoLetivo(int anoLetivo) {
        this.anoLetivo = anoLetivo;
    }

    public List<Avaliacao> getListaAvaliacoes() {
        return listaAvaliacoes;
    }

    public void adicionarAvaliacao(Avaliacao avaliacao) {
        this.listaAvaliacoes.add(avaliacao);
    }

}
