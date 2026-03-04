import model.Pessoa;

import java.time.LocalDate;

public class Docente extends Pessoa {
    private int avaliacao;
    private int nAvaliacoes;
    private String unidadeCurricular;


    public Docente() {
        nAvaliacoes = 0;
    }


    public Docente(int avaliacao, String unidadeCurricular) {
        this.avaliacao = avaliacao;
        this.unidadeCurricular = unidadeCurricular;
    }

    public Docente(String nome, String morada, int nif, LocalDate dataNascimento, String email, String sigla, int numeroMec, int avaliacao, String unidadeCurricular) {
        super(nome, morada, nif, dataNascimento, email, sigla, numeroMec);
        this.avaliacao = avaliacao;
        this.unidadeCurricular = unidadeCurricular;
    }

    public int getAvaliacao() {
        return avaliacao;
    }

    public void setAvaliacao(int avaliacao) {
        this.avaliacao = avaliacao;
    }

    public String getUnidadeCurricular() {
        return unidadeCurricular;
    }

    public void setUnidadeCurricular(String unidadeCurricular) {
        this.unidadeCurricular = unidadeCurricular;
    }

    public int getnAvaliacoes() {
        return nAvaliacoes;
    }

    public void setnAvaliacoes(int nAvaliacoes) {
        this.nAvaliacoes = nAvaliacoes;
    }

    public void registarAvaliacao(String unidadeCurricular, int avaliacao) {
        if(nAvaliacoes < 3){
            nAvaliacoes++;
        }
    }

}
