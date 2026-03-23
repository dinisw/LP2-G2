package model;

import java.util.ArrayList;
import java.util.List;

public class Curso {
    private String nome;
    private int duracao = 3;
    private Departamento departamento;

    public Curso(String nome, int duracao, Departamento departamento) {
        this.nome = nome;
        this.duracao = duracao;
        this.departamento = departamento;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getDuracao() {
        return duracao;
    }

    public void setDuracao(int duracao) {
        this.duracao = duracao;
    }

    public Departamento getDepartamento() {
        return departamento;
    }

    public void setDepartamento(Departamento departamento) {
        this.departamento = departamento;
    }
}
