import view.LoginView;
import view.SelecionarModoView;

import java.security.Security;

public class Main {

    public static void main(String[] args) {
        Security.setProperty("jdk.tls.disabledAlgorithms", "");
        System.setProperty("jdk.tls.client.protocols", "TLSv1,TLSv1.1,TLSv1.2,TLSv1.3");

        SelecionarModoView.selecionar();
        LoginView.Login();
    }
}
