import view.LoginView;
import view.SelecionarModoView;

public class Main {

    public static void main(String[] args) {
        System.setProperty("jdk.tls.client.protocols", "TLSv1.2,TLSv1.3");

        SelecionarModoView.selecionar();
        LoginView.Login();
    }
}
