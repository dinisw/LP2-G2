package controller;

import DAL.DocenteCRUD;
import DAL.EstudanteCRUD;
import DAL.GestorCRUD;
import common.utils.BackendUtils;
import common.utils.SenhaUtils;
import model.Docente;
import model.Estudante;
import model.Utilizador;

public class LoginController {

    public Utilizador login(String email, String senhaDigitada) {
        Utilizador utilizadorEncontrado = null;

        if (BackendUtils.emailISSMFEstudanteValido(email)) {
            EstudanteCRUD estudanteCRUD = new EstudanteCRUD();
            for (Estudante e : estudanteCRUD.getEstudantes()) {
                if (e.getEmail().equalsIgnoreCase(email) && e.isAtivo()) {
                    utilizadorEncontrado = e;
                    break;
                }
            }
        } else if (BackendUtils.emailISSMFDocenteValido(email)) {
            DocenteCRUD docenteCRUD = new DocenteCRUD();
            for (Docente d : docenteCRUD.getDocentes()) {
                if (d.getEmail().equalsIgnoreCase(email)) {
                    utilizadorEncontrado = d;
                    break;
                }
            }
        } else if (BackendUtils.emailISSMFGestorValido(email)) {
            GestorCRUD gestorCRUD = new GestorCRUD();
            utilizadorEncontrado = gestorCRUD.procurarPorEmail(email);
        }

        if (utilizadorEncontrado == null) {
            return null;
        }

        SenhaUtils su = new SenhaUtils();
        if (su.verificarSenha(senhaDigitada, utilizadorEncontrado.getHash())) {
            return utilizadorEncontrado;
        }

        return null;
    }
}