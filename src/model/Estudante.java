package model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Estudante extends Pessoa{

    private String nomeCurso;
    private int anoLetivo;
    private List<Avaliacao> listaAvaliacoes;

    public Estudante() {
        super();
        this.anoLetivo = 1;
        this.listaAvaliacoes = new ArrayList<>();
    }

    public Estudante(String nome, String morada, int nif, LocalDate dataNascimento, String email, String sigla, int numeroMec, String palavraPasse, String nomeCurso) {
        super(nome, morada, nif, dataNascimento, email, sigla, numeroMec, palavraPasse);
        this.nomeCurso = nomeCurso;
        this.anoLetivo = 1;
        this.listaAvaliacoes = new ArrayList<>();
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

    public boolean verificarProgessaoAno(int totalUCsInscritas) {
        if (listaAvaliacoes.isEmpty() || totalUCsInscritas == 0) {
            return false;
        }
        int notasPositivas = 0;
        for (Avaliacao avaliacao : listaAvaliacoes) {
            if (avaliacao.getNota() >= 9.5) {
                notasPositivas++;
            }
        }

        double percentagem = (double) notasPositivas / totalUCsInscritas;

        if (percentagem >= 0.60) {
            this.anoLetivo++;
            return true;
        }

        return false;
    }
}
