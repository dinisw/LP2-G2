package controller;

import model.Curso;
import view.CursoView;

import java.util.ArrayList;


//criar interface para os metodos que a clase faz
//calculo do 60 % na BLL
//        Dal fica com o CRUD
//criar uma classe para devolver um objeto, sucesso e erro para todos os casos e na view só apresenta
//criar interfaces para pegar as coisas, ex inteface unidade curricular para o docente ter acesso

public class CursoController {
    public ArrayList<String> pegarCursos(){
        var a = new ArrayList<String>();
        a.add("1. Matemática");
        a.add("2. Portugues");

        return a;
    }
}
