package model;

import java.util.ArrayList;
import java.util.List;

public class Curso {
    private String nome;
    private int duracao = 3;
    private Departamento departamento;

    public ArrayList<String> pegarCursos(){
        var a = new ArrayList<String>();
        a.add("1. Matemática");
        a.add("2. Portugues");

        return a;
    }
}
