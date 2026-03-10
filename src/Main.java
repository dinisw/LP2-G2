import BLL.EstudanteCalculo;
import controller.EstudanteController;
import model.Estudante;
import model.Avaliacao;
import model.UnidadeCurricular;
import view.EstudanteView;
import view.LoginView;

import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        var run  = new LoginView();
        run.Login();
    }
}
