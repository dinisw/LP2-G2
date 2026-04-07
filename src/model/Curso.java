package model;

import java.util.ArrayList;
import java.util.List;

public class Curso {
    private String nome;
    private int duracao = 3;
    private Departamento departamento;
    private List<UnidadeCurricular> uc;
    private boolean iniciado;

    public Curso(String nome, int duracao, Departamento departamento) {
        this.nome = nome;
        this.duracao = duracao;
        this.departamento = departamento;
        this.uc = new ArrayList<>();
        this.iniciado = false;
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

    public List<UnidadeCurricular> getUc() {
        return new ArrayList<>(uc);
    }

    public boolean isIniciado() {
        return iniciado;
    }

    public void setIniciado(boolean iniciado) {
        this.iniciado = iniciado;
    }

    public boolean adicionarUnidadeCurricular(UnidadeCurricular novaUc) {
        int contagemAno = 0;

        // Conta quantas UCs já existem no mesmo ano da nova UC
        for (UnidadeCurricular uc : uc) {
            if (uc.getAnoCurricular() == novaUc.getAnoCurricular()) {
                contagemAno++;
            }
        }

        // Se ainda não chegou às 5, adiciona e devolve sucesso
        if (contagemAno < 5) {
            this.uc.add(novaUc);
            return true;
        }

        // Se já tem 5, bloqueia a inserção
        return false;
    }

    @Override
    public String toString() {
        String nomeDepartamento = (departamento != null) ? departamento.getNome() : "Sem Departamento Associado";
        String estado = iniciado ? "Em curso (Bloqueado a inscrições)" : "Não iniciado (Inscrições Abertas)";

        return String.format("Curso: %s | Duração: %d anos | Departamento: %s | UCs Inseridas: %d | Estado: %s",
                nome, duracao, nomeDepartamento, uc.size(), estado);
    }
}
