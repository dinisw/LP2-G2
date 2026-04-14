package controller;

import DAL.DocenteCRUD;
import DAL.EstudanteCRUD;
import DAL.GestorCRUD;
import model.Docente;
import model.Estudante;
import model.Gestor;
import model.Utilizador;

public class LoginController {

    public Utilizador login(String email){
        EstudanteCRUD estudanteCRUD = new EstudanteCRUD();
        for(Estudante estudante: estudanteCRUD.getEstudantes()){
            if(estudante.getEmail().equals(email)){
                return estudante;
            }
        }

        DocenteCRUD docenteCRUD = new DocenteCRUD();
        for(Docente docente: docenteCRUD.getDocentes()){
            if(docente.getEmail().equals(email)){
                return docente;
            }
        }

        GestorCRUD gestorCRUD = new GestorCRUD();
        Gestor gestor = gestorCRUD.procurarPorEmail(email);
        if (gestor != null){
            return gestor;
        }
        return null;
    }
}
