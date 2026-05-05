package view;

import common.utils.BackendUtils;
import common.utils.MenuUtils;
import controller.RecuperarSenhaController;

import java.util.Scanner;
import static common.utils.DesignUtils.*;

public class RecuperarSenhaView {

    public static void RecuperarSenha() {
        Scanner ler = new Scanner(System.in);
        RecuperarSenhaController recuperarSenhaController = new RecuperarSenhaController();

        try {
            MenuUtils.exibirTitulo();
            System.out.println(GetCyanBold() + "--- RECUPERAR SENHA ---" + GetReset());
            System.out.println(GetYellow() + "[Digite '0' a qualquer momento para cancelar a operação e voltar!]" + GetReset());

            String email = "";
            boolean emailValido = false;

            while (!emailValido) {
                System.out.print("\nEmail: ");
                email = ler.nextLine().trim();

                if (email.equals("0")) {
                    System.out.println(GetYellow() + "\nOperação cancelada. A voltar ao ecrã de Login..." + GetReset());
                    return;
                }

                emailValido = BackendUtils.emailISSMFGestorValido(email) ||
                        BackendUtils.emailISSMFDocenteValido(email) ||
                        BackendUtils.emailISSMFEstudanteValido(email);

                if (!emailValido) {
                    System.out.println(GetRed() + "Email inválido. Verifique o domínio (@issmf.ipp.pt) e tente novamente!" + GetReset());
                }
            }

            System.out.println(GetYellow() + "\nA processar o pedido e a contactar o servidor de email..." + GetReset());

            var resultado = recuperarSenhaController.iniciarProcessoRecuperacao(email);

            if (resultado.sucesso) {
                var resAtualizarSenha = recuperarSenhaController.atualizarSenha(email, resultado.dados);

                if (resAtualizarSenha.sucesso) {
                    System.out.println(GetGreen() + "\nEmail enviado com sucesso! Verifique a sua caixa de entrada com a nova senha e tente fazer login." + GetReset());
                } else {
                    System.out.println(GetRed() + "\nErro ao atualizar a nova senha no sistema: " + resAtualizarSenha.mensagemErro + GetReset());
                }
            } else {
                System.out.println(GetRed() + "\nFalha na recuperação de senha: " + resultado.mensagemErro + GetReset());
            }

            MenuUtils.pressionarEnter(ler);

        } catch (Exception e) {
            System.out.println(GetRed() + "\nOcorreu um erro inesperado no sistema de recuperação: " + e.getMessage() + GetReset());
            MenuUtils.pressionarEnter(ler);
        }
    }
}