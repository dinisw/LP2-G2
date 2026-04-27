package controller;

import common.utils.SenhaUtils;
import model.*;

public class RecuperarSenhaController {

    private EmailService emailService;

    public RecuperarSenhaController() {
        this.emailService = new EmailService();
    }

    public Resultado iniciarProcessoRecuperacao(String email) {
        Resultado resRecuperacao = new Resultado();

        if (email == null || email.trim().isEmpty()) {
            resRecuperacao.success = false;
            resRecuperacao.errorMessage = "O email não pode estar vazio.";
            return resRecuperacao;
        }

        try {
            String token = SenhaUtils.gerarPalavraPasseAleatoria();

            Resultado resEmail = emailService.enviarEmailRecuperacaoDeSenha(email, token);

            if (resEmail.success) {
                resRecuperacao.success = true;
                resRecuperacao.object = token;
            } else {
                resRecuperacao.success = false;
                resRecuperacao.errorMessage = "Falha no servidor de email: " + resEmail.errorMessage;
            }
        } catch (Exception e) {
            resRecuperacao.success = false;
            resRecuperacao.errorMessage = "Erro inesperado ao iniciar a recuperação: " + e.getMessage();
        }

        return resRecuperacao;
    }

    public Resultado atualizarSenha(Utilizador utilizador, String senhaCruaGerada) {
        Resultado resultado = new Resultado();

        if (utilizador == null || senhaCruaGerada == null || senhaCruaGerada.trim().isEmpty()) {
            resultado.success = false;
            resultado.errorMessage = "Dados inválidos para atualizar a senha.";
            return resultado;
        }

        SenhaUtils su = new SenhaUtils();
        String hash = su.gerarHashComSalt(senhaCruaGerada);

        if (utilizador instanceof Estudante) {
            EstudanteController ec = new EstudanteController();
            return ec.alterarPassword(((Estudante) utilizador).getNumeroMec(), hash);

        } else if (utilizador instanceof Docente) {
            DocenteController dc = new DocenteController();
            return dc.alterarPassword(((Docente) utilizador).getNif(), hash);

        } else if (utilizador instanceof Gestor) {
            GestorController gc = new GestorController();
            return gc.alterarPassword(((Gestor) utilizador).getNif(), hash);
        }

        resultado.success = false;
        resultado.errorMessage = "Tipo de utilizador desconhecido. Não foi possível atualizar a senha.";
        return resultado;
    }
}