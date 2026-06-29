package DAL;

import model.Estudante;
import model.Resultado;
import java.util.List;

public interface IEstudanteDAO {
    Resultado<Estudante> registarEstudante(Estudante estudante);
    Estudante lerEstudante(int numeroMec);
    Resultado<Estudante> atualizarEstudante(Estudante estudante);
    Resultado<Estudante> atualizarSenha(Estudante estudante);
    Resultado<Estudante> eliminarEstudante(int numeroMec);
    List<Estudante> getEstudantes();
    Estudante procurarPorNif(int nif);
    Estudante procurarPorEmail(String email);
    int gerarNumeroMecanografico();
}
