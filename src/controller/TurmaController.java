package controller;

import DAL.TurmaCRUD;
import model.*;

public class TurmaController {

    /**
     * Cria uma turma validando se já não existe uma para aquele ano do curso.
     */
//    public boolean criarTurma(Curso curso, int ano) {
//        if (TurmaCRUD.procurarPorCursoEAno(curso.getNome(), ano) != null) {
//            return false; // Regra: Só existe uma turma para cada ano letivo
//        }
//        Turma nova = new Turma(curso, ano);
//        TurmaCRUD.guardar(nova);
//        return true;
//    }

    /**
     * Promove um estudante para o ano seguinte se tiver >60% de aproveitamento.
     */
//    public String promoverEstudante(Estudante estudante, int anoAtual) {
//        if (estudante.temAproveitamentoSuficiente()) {
//            int proximoAno = anoAtual + 1;
//            if (proximoAno > estudante.getCurso().getDuracao()) {
//                return "Estudante concluiu o curso com sucesso!";
//            }
//
//            Turma proximaTurma = TurmaCRUD.procurarPorCursoEAno(estudante.getCurso().getNome(), proximoAno);
//            if (proximaTurma != null) {
//                proximaTurma.adicionarEstudante(estudante);
//                return "Promovido para o " + proximoAno + "º ano.";
//            }
//            return "Erro: Turma do próximo ano ainda não foi criada.";
//        }
//        return "Reprovado: Aproveitamento inferior a 60%.";
//    }

    public void imprimirRelatorioTurmas() {
        for (Turma t : TurmaCRUD.listarTodas()) {
            System.out.println(t);
        }
    }
}