package view;

import Common.BackendUtils;
import Common.DesignUtils;
import Common.MenuUtils;

import java.util.Scanner;

public class RecuperarSenhaView {
    public static void RecuperarSenha(){
        Scanner ler = new Scanner(System.in);
        boolean sair = false;
        do {
            MenuUtils.exibirTitulo();
            String email = "";
            boolean emailValido = false;
            while (!emailValido) {
                System.out.println(DesignUtils.GetCyanBold() + "RECUPERAR SENHA" + DesignUtils.GetReset());
                System.out.println("digite '0' para sair");
                System.out.print("\nEmail: ");
                email = ler.nextLine().trim();
                if (email.equals("0")) {
                    sair = true;
                    break;
                }
                emailValido = BackendUtils.isEmailISSMFValido(email);
                if (!emailValido) {
                    System.out.print("Email inválido, tente novamente!!");
                    MenuUtils.pressionarEnter(ler);
                }
            }
            //enviar email de recuperação
            System.out.println("Email com a senha enviado para o seu email, verifique e tente login novamente!");
        }while (!sair);
    }
}
