package controller;

import DAL.DAOFactory;
import DAL.IDocenteDAO;
import DAL.IEstudanteDAO;
import DAL.IGestorDAO;
import common.utils.BackendUtils;
import common.utils.SenhaUtils;
import model.Utilizador;

public class LoginController {

    public enum ErroLogin { NENHUM, CREDENCIAIS_INVALIDAS, CONTA_INATIVA, ERRO_LIGACAO }

    private ErroLogin ultimoErro = ErroLogin.NENHUM;

    /** Retorna o motivo do último login falhado. */
    public ErroLogin getUltimoErro() { return ultimoErro; }

    public Utilizador login(String email, String senhaDigitada) {
        ultimoErro = ErroLogin.NENHUM;

        final String emailNorm = (email != null) ? email.trim().toLowerCase() : "";
        Utilizador utilizadorEncontrado = null;

        // O(1) — usa procurarPorEmail() com query direta em vez de carregar todos
        if (BackendUtils.emailISSMFEstudanteValido(emailNorm)) {
            IEstudanteDAO estudanteDAO = DAOFactory.getEstudanteDAO();
            utilizadorEncontrado = estudanteDAO.procurarPorEmail(emailNorm);

        } else if (BackendUtils.emailISSMFDocenteValido(emailNorm)) {
            IDocenteDAO docenteDAO = DAOFactory.getDocenteDAO();
            utilizadorEncontrado = docenteDAO.procurarPorEmail(emailNorm);

        } else if (BackendUtils.emailISSMFGestorValido(emailNorm)) {
            IGestorDAO gestorDAO = DAOFactory.getGestorDAO();
            utilizadorEncontrado = gestorDAO.procurarPorEmail(emailNorm);
        }

        if (utilizadorEncontrado == null) {
            ultimoErro = ErroLogin.CREDENCIAIS_INVALIDAS;
            return null;
        }

        if (!utilizadorEncontrado.isAtivo()) {
            ultimoErro = ErroLogin.CONTA_INATIVA;
            return null;
        }

        SenhaUtils su = new SenhaUtils();
        if (!su.verificarSenha(senhaDigitada, utilizadorEncontrado.getHash())) {
            ultimoErro = ErroLogin.CREDENCIAIS_INVALIDAS;
            return null;
        }

        return utilizadorEncontrado;
    }
}
