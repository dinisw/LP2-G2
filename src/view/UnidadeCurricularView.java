package view;

import model.Docente;
import model.UnidadeCurricular;
import model.Curso;
import java.util.List;
import java.util.Scanner;

public class UnidadeCurricularView {
    private Scanner scanner = new Scanner(System.in);

    public void imprimirDadosUnidadeCurricular(UnidadeCurricular uc) {
        System.out.println("\n--- Dados da Unidade Curricular ---");
        System.out.println("Nome: " + uc.getNome());
        System.out.println("Ano Curricular: " + uc.getAnoCurricular());
        System.out.println("ECTS: " + uc.getEcts());
        if (uc.getDocente() != null) {
            System.out.println("Docente Responsável: " + uc.getDocente().getNome() + " (" + uc.getDocente().getSigla() + ")");
        } else {
            System.out.println("Docente Responsável: NÃO ATRIBUÍDO");
        }
    }

    public String solicitarNome() {
        System.out.print("Introduza o nome da Unidade Curricular: ");
        return scanner.nextLine();
    }

    public int solicitarAno() {
        System.out.print("Introduza o ano curricular (ex: 1, 2, 3): ");
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public String solicitarSiglaDocente() {
        System.out.print("Introduza a sigla do Docente responsável: ");
        return scanner.nextLine();
    }

    public String solicitarNomeCurso() {
        System.out.print("Introduza o nome do curso para associar esta UC (ou prima Enter para terminar): ");
        return scanner.nextLine();
    }

    public void mostrarMensagem(String mensagem) {
        System.out.println(mensagem);
    }

    public void listarDocentesDisponiveis(List<Docente> docentes) {
        System.out.println("\n--- Docentes Disponíveis ---");
        for (Docente d : docentes) {
            System.out.println(d.getSigla() + " - " + d.getNome());
        }
    }
}
