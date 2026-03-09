package model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public abstract class Pessoa {

    private String nome;
    private int numeroMec;
    private String sigla;
    private String email;
    private int nif;
    private LocalDate dataNascimento;
    private String morada;
    private String palavraPasse;


    public Pessoa() {
        nome = "";
        numeroMec = 0;
        sigla = "";
        email = "";
        nif = 0;
        dataNascimento = null;
        morada = "";
        palavraPasse = "";
    }

    public Pessoa(String nome, String morada, int nif, LocalDate dataNascimento, String email, String sigla, int numeroMec, String palavraPasse) {
        this.nome = nome;
        this.morada = morada;
        this.nif = nif;
        this.dataNascimento = dataNascimento;
        this.email = email;
        this.sigla = sigla;
        this.numeroMec = numeroMec;
        this.palavraPasse = palavraPasse;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getNumeroMec() {
        return numeroMec;
    }

    public void setNumeroMec(int numeroMec) {
        this.numeroMec = numeroMec;
    }

    public String getSigla() {
        return sigla;
    }

    public void setSigla(String sigla) {
        this.sigla = sigla;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getNif() {
        return nif;
    }

    public void setNif(int nif) {
        this.nif = nif;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public String getMorada() {
        return morada;
    }

    public void setMorada(String morada) {
        this.morada = morada;
    }

    public String getPalavraPasse() {
        return palavraPasse;
    }

    public void setPalavraPasse(String palavraPasse) {
        this.palavraPasse = palavraPasse;
    }

    @Override
    public String toString() {
        return "Pessoa {" +
                "nome='" + nome + '\'' +
                ", numeroMec=" + numeroMec +
                ", sigla='" + sigla + '\'' +
                ", email='" + email + '\'' +
                ", nif=" + nif +
                ", dataNascimento='" + dataNascimento + '\'' +
                ", morada='" + morada + '\'' +
                '}';
    }
}