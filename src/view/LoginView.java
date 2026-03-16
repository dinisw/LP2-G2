package view;

import java.util.Scanner;

import static view.Menu.*;

public class LoginView {
    // CRIAR O LOGIN E O REGISTRAR AQUI, QUANDO DEVOLVE SUCESSO VAI PARA AS OUTRAS VIEWS, SE DEVOLVER ERRO, PERMITE TENTAR DE NOVO


    //endregion
    public static void Login(){
        Scanner ler = new Scanner(System.in);
        Menu menu = new Menu();
        menu.exibirTitulo();
        System.out.println("\n" + YELLOW + "[EM MANUTENÇÃO] O sistema de login ainda não está finalizado." + RESET);
        menu.pressionarEnter(ler);
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
