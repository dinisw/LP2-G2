package controller;

import common.utils.SenhaUtils;
import model.*;

public class RecuperarSenhaController {

    private EmailService emailService;

    public RecuperarSenhaController() {
        this.emailService = new EmailService();
    }

    // CORREÇÃO: Alterado de Resultado<EmailService> para Resultado<String>
    public Resultado<String> iniciarProcessoRecuperacao(String email) {
        Resultado<String> resRecuperacao = new Resultado<>();

        if (email == null || email.trim().isEmpty()) {
            resRecuperacao.sucesso = false;
            resRecuperacao.mensagemErro = "O email não pode estar vazio.";
            return resRecuperacao;
        }

        try {
            String token = SenhaUtils.gerarPalavraPasseAleatoria();

            Resultado<String> resEmail = emailService.enviarEmailRecuperacaoDeSenha(email, token);

            if (resEmail.sucesso) {
                resRecuperacao.sucesso = true;
                resRecuperacao.dados = token;
            } else {
                resRecuperacao.sucesso = false;
                resRecuperacao.mensagemErro = "Falha no servidor de email: " + resEmail.mensagemErro;
            }
        } catch (Exception e) {
            resRecuperacao.sucesso = false;
            resRecuperacao.mensagemErro = "Erro inesperado ao iniciar a recuperação: " + e.getMessage();
        }

        return resRecuperacao;
    }

    public Resultado<? extends Utilizador> atualizarSenha(Utilizador utilizador, String senhaCruaGerada) {
        Resultado<Utilizador> resultado = new Resultado<>();

        if (utilizador == null || senhaCruaGerada == null || senhaCruaGerada.trim().isEmpty()) {
            resultado.sucesso = false;
            resultado.mensagemErro = "Dados inválidos para atualizar a senha.";
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

        resultado.sucesso = false;
        resultado.mensagemErro = "Tipo de utilizador desconhecido. Não foi possível atualizar a senha.";
        return resultado;
    }
}