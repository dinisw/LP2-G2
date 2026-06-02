package controller;

import DAL.DAOFactory;
import DAL.IDocenteDAO;
import DAL.IEstudanteDAO;
import DAL.IGestorDAO;
import common.utils.BackendUtils;
import common.utils.SenhaUtils;
import model.*;
import service.EmailService;

public class RecuperarSenhaController {
    private EmailService emailService;

    public RecuperarSenhaController() {
        this.emailService = new EmailService();
    }

    public Resultado<String> iniciarProcessoRecuperacao(String email) {
        if (email == null || email.trim().isEmpty()) {
            return new Resultado<>(false, "O email não pode estar vazio.");
        }
        try {
            String token = SenhaUtils.gerarPalavraPasseAleatoria();
            Resultado<String> resEmail = emailService.enviarEmailRecuperacaoDeSenha(email, token);
            if (resEmail.sucesso) return new Resultado<>(token, true);
            return new Resultado<>(false, "Falha no servidor de email: " + resEmail.mensagemErro);
        } catch (Exception e) {
            return new Resultado<>(false, "Erro inesperado ao iniciar a recuperação: " + e.getMessage());
        }
    }

    public Resultado<String> atualizarSenha(String email, String senhaCruaGerada) {
        if (email == null || email.trim().isEmpty() || senhaCruaGerada == null || senhaCruaGerada.trim().isEmpty()) {
            return new Resultado<>(false, "Dados inválidos para atualizar a senha.");
        }

        SenhaUtils su = new SenhaUtils();
        String hash = su.gerarHashComSalt(senhaCruaGerada);

        if (BackendUtils.emailISSMFEstudanteValido(email)) {
            IEstudanteDAO estudanteDAO = DAOFactory.getEstudanteDAO();
            for (Estudante e : estudanteDAO.getEstudantes()) {
                if (e.getEmail().equalsIgnoreCase(email)) {
                    EstudanteController ec = new EstudanteController();
                    var res = ec.alterarPassword(e.getNumeroMec(), hash);
                    return new Resultado<>(res.sucesso ? "Senha atualizada" : null, res.sucesso);
                }
            }
        } else if (BackendUtils.emailISSMFDocenteValido(email)) {
            IDocenteDAO docenteDAO = DAOFactory.getDocenteDAO();
            for (Docente d : docenteDAO.getDocentes()) {
                if (d.getEmail().equalsIgnoreCase(email)) {
                    DocenteController dc = new DocenteController();
                    var res = dc.alterarPassword(d.getNif(), hash);
                    return new Resultado<>(res.sucesso ? "Senha atualizada" : null, res.sucesso);
                }
            }
        } else if (BackendUtils.emailISSMFGestorValido(email)) {
            IGestorDAO gestorDAO = DAOFactory.getGestorDAO();
            Gestor g = gestorDAO.procurarPorEmail(email);
            if (g != null) {
                GestorController gc = new GestorController();
                var res = gc.alterarPassword(g.getNif(), hash);
                return new Resultado<>(res.sucesso ? "Senha atualizada" : null, res.sucesso);
            }
        }

        return new Resultado<>(false, "Erro crítico: Utilizador não encontrado na base de dados.");
    }
}
