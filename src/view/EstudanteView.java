package view;

import model.Avaliacao;
import java.util.List;

public class EstudanteView {
    public void imprimirFichaEstudante (String nome, int numMec, String email, int nif, String dataNascimento, String morada, String curso, int anoLetivo) {
        System.out.println("FICHA DE ESTUDANTE");
        System.out.println("Nome: " + nome);
        System.out.println("Nº Mecanográfico: " + numMec);
        System.out.println("Email: " + email);
        System.out.println("NIF: " + nif);
        System.out.println("Data Nascimento: " + dataNascimento);
        System.out.println("Morada: " + morada);
        System.out.println("Curso (Inscrição): " + curso);
        System.out.println("Ano Letivo Atual: " + anoLetivo + "º Ano");
    }

    public void imprimirNotas (List<Avaliacao> notas) {
        System.out.println("NOTAS DE AVALIAÇÃO");
        if (notas.isEmpty()) {
            System.out.println("O estudante ainda não possui notas registadas.");
        } else {
            for (Avaliacao avaliacao : notas) {
                String nomeUC = (avaliacao.getUnidadeCurricular() != null) ? avaliacao.getUnidadeCurricular().getNome() : "Desconhecida";
                System.out.println("UC: " + nomeUC + " | Momento: " + avaliacao.getMomento() + " | Nota: " + avaliacao.getNota());
            }
        }
    }
    public void mostrarMensagem(String mensagem) {
        System.out.println(mensagem);
    }
}
