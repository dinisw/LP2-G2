package view;

import Common.Utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;

import static view.Menu.*;

public class LoginView {
    // CRIAR O LOGIN E O REGISTRAR AQUI, QUANDO DEVOLVE SUCESSO VAI PARA AS OUTRAS VIEWS, SE DEVOLVER ERRO, PERMITE TENTAR DE NOVO
    //region Design
    public static final String RESET = "\033[0m";
    public static final String CYAN_BOLD = "\033[1;36m";
    public static final String WHITE_BOLD = "\033[1;37m";
    public static final String BLUE = "\033[0;34m";
    public static final String GREEN = "\033[0;32m";
    public static final String RED = "\033[0;31m";
    public static final String YELLOW = "\033[0;33m";
    private static final int LARGURA = 84;
    //endregion

    public static void Login(){
        Scanner ler = new Scanner(System.in);
        Utils menu = new Utils();
        var login = "1";//LoginCrontoller.Login(email, senha);
        do {
            menu.exibirTitulo();

            System.out.println(Utils.GetCyanBold() +  "LOGIN" + Utils.GetReset());//melhorar visual
            System.out.print("Email: ");
            var email = ler.nextLine().trim();
            System.out.print("Senha: ");
            var senha = ler.nextLine().trim();

            //Pegar da BD qual o tipo de utilizador
            //var login = "1";//LoginCrontoller.Login(email, senha);
            if(true){//Se devolver alguma coisa de sucesso do loginController
                switch (login) {
                    case "1":
                        EstudanteView.Menu();
                        break;
                    case "2":
                        //consultarFichaEstudante(menu, ler);
                        break;
                    case "3":
                        //System.out.println("\n" + YELLOW + "[EM MANUTENÇÃO] Esta funcionalidade ainda não está finalizada." + RESET);
                        //menu.pressionarEnter(ler);
                        break;
                    default:
                        System.out.println("\n" + RED + "Opção inválida! Tente novamente." + RESET);
                        menu.pressionarEnter(ler);
                }
            }
        }while (!login.equals("0"));
    }


    // Exemplo de uso da estrutura MVC
//    Pessoa modelo = new Pessoa("Gonçalo", "Rua X", 123456789, LocalDate.of(2000, 1, 1), "goncalo@email.com", "G", 123) {};
//    PessoaView vista = new PessoaView();
//    PessoaController controlador = new PessoaController(modelo, vista);
//
//        controlador.atualizarView();
//
//    // Atualizar dados através do controlador
//        controlador.setNome("Gonçalo Silva");
//        controlador.atualizarView();
}
