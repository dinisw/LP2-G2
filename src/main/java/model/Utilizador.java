package main.model;

import java.time.LocalDate;

public abstract class Utilizador {

    private String nome;
    private String email;
    private int nif;
    private LocalDate dataNascimento;
    private String morada;
    private String hash;
    private boolean ativo;


    public Utilizador() {
        nome = "";
        email = "";
        nif = 0;
        dataNascimento = null;
        morada = "";
        hash = "";
        ativo = true;
    }

    public Utilizador(String nome, String morada, int nif, LocalDate dataNascimento, String email, String hash) {
        this.nome = nome;
        this.morada = morada;
        this.nif = nif;
        this.dataNascimento = dataNascimento;
        this.email = email;
        this.hash = hash;
        this.ativo = true;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
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

    public String getHash() {        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    @Override
    public String toString() {
        return  "\n  Nome: " + nome +
                "\n  NIF: " + nif +
                "\n  Data de Nascimento: " + dataNascimento +
                "\n  Email: " + email +
                "\n  Morada: " + morada;
    }
}