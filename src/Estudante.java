import javax.swing.plaf.PanelUI;
import java.time.LocalDate;

public class Estudante extends Pessoa{

    private double notasDeAvaliacao;

    public Estudante() {
        notasDeAvaliacao = 0.0;
    }

    public Estudante(String nome, String morada, int nif, LocalDate dataNascimento, String email, String sigla, int numeroMec) {
        super(nome, morada, nif, dataNascimento, email, sigla, numeroMec);
        this.notasDeAvaliacao = 0.0;
    }

    public double getNotasDeAvaliacao() {
        return notasDeAvaliacao;
    }

    public void setNotasDeAvaliacao(double notasDeAvaliacao) {
        this.notasDeAvaliacao = notasDeAvaliacao;
    }


}
