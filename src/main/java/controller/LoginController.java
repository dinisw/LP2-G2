package controller;

import DAL.DAOFactory;
import DAL.IDocenteDAO;
import DAL.IEstudanteDAO;
import DAL.IGestorDAO;
import common.utils.BackendUtils;
import common.utils.SenhaUtils;
import model.Docente;
import model.Estudante;
import model.Utilizador;

public class LoginController {

    public Utilizador login(String email, String senhaDigitada) {
        Utilizador utilizadorEncontrado = null;

        if (BackendUtils.emailISSMFEstudanteValido(email)) {
            IEstudanteDAO estudanteDAO = DAOFactory.getEstudanteDAO();
            for (Estudante e : estudanteDAO.getEstudantes()) {
                if (e.getEmail().equalsIgnoreCase(email)) {
                    utilizadorEncontrado = e;
                    break;
                }
            }
        } else if (BackendUtils.emailISSMFDocenteValido(email)) {
            IDocenteDAO docenteDAO = DAOFactory.getDocenteDAO();
            for (Docente d : docenteDAO.getDocentes()) {
                if (d.getEmail().equalsIgnoreCase(email)) {
                    utilizadorEncontrado = d;
                    break;
                }
            }
        } else if (BackendUtils.emailISSMFGestorValido(email)) {
            IGestorDAO gestorDAO = DAOFactory.getGestorDAO();
            utilizadorEncontrado = gestorDAO.procurarPorEmail(email);
        }

        if (utilizadorEncontrado == null) return null;

        SenhaUtils su = new SenhaUtils();
        return su.verificarSenha(senhaDigitada, utilizadorEncontrado.getHash()) ? utilizadorEncontrado : null;
    }
}
