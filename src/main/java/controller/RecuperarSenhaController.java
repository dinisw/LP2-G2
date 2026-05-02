package controller;

import DAL.DocenteCRUD;
import DAL.EstudanteCRUD;
import DAL.GestorCRUD;
import common.utils.BackendUtils;
import common.utils.SenhaUtils;
import model.*;

public class RecuperarSenhaController {
    private EmailService emailService;

    public RecuperarSenhaController() {
        this.emailService = new EmailService();
    }

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

    public Resultado<String> atualizarSenha(String email, String senhaCruaGerada) {
        Resultado<String> resultado = new Resultado<>();
        if (email == null || email.trim().isEmpty() || senhaCruaGerada == null || senhaCruaGerada.trim().isEmpty()) {
            resultado.sucesso = false;
            resultado.mensagemErro = "Dados inválidos para atualizar a senha.";
            return resultado;
        }

        SenhaUtils su = new SenhaUtils();
        String hash = su.gerarHashComSalt(senhaCruaGerada);

        if (BackendUtils.emailISSMFEstudanteValido(email)) {
            EstudanteCRUD estudanteCRUD = new EstudanteCRUD();
            for (Estudante e : estudanteCRUD.getEstudantes()) {
                if (e.getEmail().equalsIgnoreCase(email)) {
                    EstudanteController ec = new EstudanteController();
                    var res = ec.alterarPassword(e.getNumeroMec(), hash);
                    return new Resultado<>(res.sucesso ? "Senha atualizada" : null, res.sucesso);
                }
            }
        } else if (BackendUtils.emailISSMFDocenteValido(email)) {
            DocenteCRUD docenteCRUD = new DocenteCRUD();
            for (Docente d : docenteCRUD.getDocentes()) {
                if (d.getEmail().equalsIgnoreCase(email)) {
                    DocenteController dc = new DocenteController();
                    var res = dc.alterarPassword(d.getNif(), hash);
                    return new Resultado<>(res.sucesso ? "Senha atualizada" : null, res.sucesso);
                }
            }
        } else if (BackendUtils.emailISSMFGestorValido(email)) {
            GestorCRUD gestorCRUD = new GestorCRUD();
            Gestor g = gestorCRUD.procurarPorEmail(email);
            if (g != null) {
                GestorController gc = new GestorController();
                var res = gc.alterarPassword(g.getNif(), hash);
                return new Resultado<>(res.sucesso ? "Senha atualizada" : null, res.sucesso);
            }
        }

        resultado.sucesso = false;
        resultado.mensagemErro = "Erro crítico: Utilizador não encontrado na base de dados.";
        return resultado;
    }
}