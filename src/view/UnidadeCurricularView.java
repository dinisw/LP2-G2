package view;

import model.Docente;

import java.time.LocalDate;

public class UnidadeCurricularView {
    public void imprimirDadosUnidadeCurricular(String nome, LocalDate ano, Docente docente) {
        System.out.println("Unidade Curricular: ");
        System.out.println("Nome: " + nome);
        System.out.println("Ano Curricular: " + ano);
        System.out.println("Docente: " + docente.getNome());
    }
}
