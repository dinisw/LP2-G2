//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
import model.Pessoa;
import view.PessoaView;
import controller.PessoaController;
import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        // Exemplo de uso da estrutura MVC
        Pessoa modelo = new Pessoa("Gonçalo", "Rua X", 123456789, LocalDate.of(2000, 1, 1), "goncalo@email.com", "G", 123) {};
        PessoaView vista = new PessoaView();
        PessoaController controlador = new PessoaController(modelo, vista);

        controlador.atualizarView();

        // Atualizar dados através do controlador
        controlador.setNome("Gonçalo Silva");
        controlador.atualizarView();
    }
}
