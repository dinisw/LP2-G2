package main.model;

import java.util.ArrayList;
import java.util.List;

public class Curso {
    private String nome;
    private int duracao = 3;
    private Departamento departamento;
    private final List<UnidadeCurricular> unidadeCurriculars;
    private List<Integer> anosIniciados;

    public Curso(String nome, int duracao, Departamento departamento) {
        this.nome = nome;
        this.duracao = duracao;
        this.departamento = departamento;
        this.unidadeCurriculars = new ArrayList<>();
        this.anosIniciados = new ArrayList<>();
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

    public List<UnidadeCurricular> getUnidadeCurriculars() {
        return new ArrayList<>(unidadeCurriculars);
    }

    public boolean isIniciado() {
        return !anosIniciados.isEmpty();
    }

    public boolean isAnoIniciado(int ano) {
        return anosIniciados.contains(ano);
    }

    public void adicionarAnoIniciado(int ano) {
        if (!anosIniciados.contains(ano)) {
            anosIniciados.add(ano);
        }
    }

    public List<Integer> getAnosIniciados() {
        return new ArrayList<>(anosIniciados);
    }

    public void setAnosIniciados(List<Integer> anos) {
        this.anosIniciados = anos != null ? anos : new ArrayList<>();
    }

    public boolean adicionarUnidadeCurricular(UnidadeCurricular novaUc) {
        int contagemAno = 0;

        for (UnidadeCurricular unidadeCurricular : unidadeCurriculars) {
            if (unidadeCurricular.getAnoCurricular() == novaUc.getAnoCurricular()) {
                contagemAno++;
            }
        }
        if (contagemAno < 5) {
            this.unidadeCurriculars.add(novaUc);
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        String nomeDepartamento = (departamento != null) ? departamento.getNome() : "Sem Departamento Associado";
        String estado = isIniciado() ? "Em curso (Anos ativos: " + anosIniciados.toString() + ")" : "Não iniciado (Inscrições Abertas)";

        return String.format("Curso: %s | Duração: %d anos | Departamento: %s | UCs Inseridas: %d | Estado: %s",
                nome, duracao, nomeDepartamento, unidadeCurriculars.size(), estado);
    }
}
