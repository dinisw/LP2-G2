package main.controller;

import main.DAL.DocenteCRUD;
import main.DAL.EstudanteCRUD;
import main.DAL.GestorCRUD;
import main.model.Docente;
import main.model.Estudante;
import main.model.Utilizador;

public class LoginController {

    public Utilizador login(String email){
        EstudanteCRUD estudanteCRUD = new EstudanteCRUD();
        for(Estudante estudante: estudanteCRUD.getEstudantes()){
            if(estudante.getEmail().equalsIgnoreCase(email)){
                return estudante;
            }
        }

        DocenteCRUD docenteCRUD = new DocenteCRUD();
        for(Docente docente: docenteCRUD.getDocentes()){
            if(docente.getEmail().equalsIgnoreCase(email)){
                return docente;
            }
        }

        GestorCRUD gestorCRUD = new GestorCRUD();
        return gestorCRUD.procurarPorEmail(email);
    }
}