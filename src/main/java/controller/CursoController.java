package controller;

import DAL.CursoCRUD;
import DAL.DepartamentoCRUD;
import DAL.EstudanteCRUD;
import DAL.UnidadeCurricularCRUD;
import model.*;

import java.util.List;

public class CursoController {

    private CursoCRUD cursoCRUD;
    private DepartamentoCRUD depCRUD;
    private UnidadeCurricularCRUD ucCRUD;

    public CursoController() {
        this.cursoCRUD = new CursoCRUD();
        this.depCRUD = new DepartamentoCRUD();
        this.ucCRUD = new UnidadeCurricularCRUD();
    }

    // NOVA ASSINATURA: Recebe diretamente o objeto Curso montado pela View
    public Resultado registarCurso(Curso curso) {
        Resultado resultado = new Resultado();

        if (curso == null || curso.getNome() == null || curso.getNome().trim().isEmpty()) {
            resultado.success = false;
            resultado.errorMessage = "O nome do curso é obrigatório.";
            return resultado;
        }

        if (curso.getDepartamento() == null) {
            resultado.success = false;
            resultado.errorMessage = "O departamento associado é obrigatório.";
            return resultado;
        }

        if (cursoCRUD.registarCurso(curso)) {
            resultado.success = true;
        } else {
            resultado.success = false;
            resultado.errorMessage = "Já existe um curso com o nome '" + curso.getNome() + "' no sistema.";
        }

        return resultado;
    }

    public List<Curso> listarCursos() {
        return cursoCRUD.getCursos();
    }

    public Curso procurarCurso(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            return null;
        }
        return cursoCRUD.procurarPorNome(nome);
    }

    // NOVA ASSINATURA: Recebe o nome antigo (para o procurar) e o objeto Curso atualizado
    public Resultado atualizarCurso(String nomeAntigo, Curso cursoNovo) {
        Resultado resultado = new Resultado();

        if (nomeAntigo == null || nomeAntigo.trim().isEmpty()) {
            resultado.success = false;
            resultado.errorMessage = "O nome do curso a atualizar é obrigatório.";
            return resultado;
        }

        if (cursoNovo == null || cursoNovo.getNome() == null || cursoNovo.getNome().trim().isEmpty()) {
            resultado.success = false;
            resultado.errorMessage = "O novo nome do curso não pode estar vazio.";
            return resultado;
        }

        Curso cursoOriginal = cursoCRUD.procurarPorNome(nomeAntigo);

        if (cursoOriginal == null) {
            resultado.success = false;
            resultado.errorMessage = "O curso original não foi encontrado na base de dados.";
            return resultado;
        }

        // Atualizar na base de dados
        Resultado res = cursoCRUD.atualizarCurso(nomeAntigo, cursoNovo);

        if (res.success) {
            resultado.success = true;

            // Se o nome do curso mudou, precisamos de atualizar o campo 'nomeCurso' dos Estudantes inscritos
            if (!nomeAntigo.equalsIgnoreCase(cursoNovo.getNome())) {
                try {
                    EstudanteCRUD estudanteCRUD = new EstudanteCRUD();
                    EstudanteController estudanteController = new EstudanteController();
                    List<Estudante> todosEstudantes = estudanteController.listarEstudantes();

                    for (Estudante estudante : todosEstudantes) {
                        if (estudante.getNomeCurso() != null && estudante.getNomeCurso().equalsIgnoreCase(nomeAntigo)) {
                            estudante.setNomeCurso(cursoNovo.getNome());
                            estudanteCRUD.atualizarEstudante(estudante);
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Aviso: Não foi possível sincronizar o novo nome nos perfis dos estudantes.");
                }
            }
        } else {
            resultado.success = false;
            resultado.errorMessage = res.errorMessage;
        }

        return resultado;
    }

    public Resultado eliminarCurso(String nomeAntigo) {
        Resultado resultado = new Resultado();

        if (nomeAntigo == null || nomeAntigo.trim().isEmpty()) {
            resultado.success = false;
            resultado.errorMessage = "O nome do curso a eliminar é obrigatório.";
            return resultado;
        }

        if (cursoCRUD.procurarPorNome(nomeAntigo) == null) {
            resultado.success = false;
            resultado.errorMessage = "O curso especificado não foi encontrado no sistema.";
            return resultado;
        }

        Resultado res = cursoCRUD.eliminarCurso(nomeAntigo);

        if (res.success) {
            resultado.success = true;
            try {
                EstudanteCRUD estudanteCRUD = new EstudanteCRUD();
                EstudanteController estudanteController = new EstudanteController();
                List<Estudante> todosEstudantes = estudanteController.listarEstudantes();

                for (Estudante estudante : todosEstudantes) {
                    if (estudante.getNomeCurso() != null && estudante.getNomeCurso().equalsIgnoreCase(nomeAntigo)) {
                        estudante.setNomeCurso("SEM REGISTO");
                        estudanteCRUD.atualizarEstudante(estudante);
                    }
                }
            } catch (Exception e) {
                System.out.println("Aviso: Não foi possível atualizar os perfis dos estudantes órfãos.");
            }

        } else {
            resultado.success = false;
            resultado.errorMessage = res.errorMessage;
        }
        return resultado;
    }

    public Resultado iniciarAnoLetivo(String nome, int anoLetivo) {
        Resultado resultado = new Resultado();

        if (anoLetivo < 1 || anoLetivo > 3) {
            resultado.success = false;
            resultado.errorMessage = "Ano letivo inválido. Os cursos têm 3 anos curriculares.";
            return resultado;
        }

        Curso curso = cursoCRUD.procurarPorNome(nome);
        if (curso == null) {
            resultado.success = false;
            resultado.errorMessage = "Curso não encontrado.";
            return resultado;
        }

        if (curso.isAnoIniciado(anoLetivo)) {
            resultado.success = false;
            resultado.errorMessage = "O " + anoLetivo + "º ano deste curso já se encontra iniciado.";
            return resultado;
        }

        if (!isEstruturaCurricularValida(curso)) {
            resultado.success = false;
            resultado.errorMessage = "Estrutura curricular incompleta (é obrigatório ter pelo menos uma UC em cada um dos 3 anos).";
            return resultado;
        }

        List<String> unidadesCurricularesSemMomentos = obterUCsSemMomentosDeAvaliacao(curso);
        if (!unidadesCurricularesSemMomentos.isEmpty()) {
            resultado.success = false;
            resultado.errorMessage = "As seguintes UCs não têm Momentos de Avaliação: " + String.join(",", unidadesCurricularesSemMomentos) + ".";
            return resultado;
        }

        controller.EstudanteController estudanteController = new controller.EstudanteController();
        List<model.Estudante> todosEstudantes = estudanteController.listarEstudantes();

        int alunosNesteAno = 0;
        for (model.Estudante estudante : todosEstudantes) {
            if (estudante.getNomeCurso() != null && estudante.getNomeCurso().equalsIgnoreCase(curso.getNome())) {

                int anoDoEstudante = estudanteController.obterAnoDesbloqueado(estudante);

                if (anoDoEstudante == anoLetivo) {
                    alunosNesteAno++;
                }
            }
        }

        int minimoExigido = (anoLetivo == 1) ? 5 : 1;

        if (alunosNesteAno < minimoExigido) {
            resultado.success = false;
            resultado.errorMessage = "O " + anoLetivo + "º ano exige no mínimo " + minimoExigido + " aluno(s). Atualmente tem " + alunosNesteAno + " aluno(s) apto(s).";
            return resultado;
        }

        curso.adicionarAnoIniciado(anoLetivo);
        Resultado res = cursoCRUD.registarArranqueAno(curso.getNome(), curso);
        if (res.success) {
            resultado.success = true;
        } else {
            resultado.success = false;
            resultado.errorMessage = res.errorMessage;
        }
        return resultado;
    }

    public Resultado associarUCAoCurso (String nomeCurso, String nomeUC) {
        Resultado resultado = new Resultado();

        if (nomeCurso == null || nomeCurso.trim().isEmpty() || nomeUC == null || nomeUC.trim().isEmpty()) {
            resultado.success = false;
            resultado.errorMessage = "O nome do curso e da UC são obrigatórios.";
            return resultado;
        }

        Curso curso = this.cursoCRUD.procurarPorNome(nomeCurso);
        if (curso == null) {
            resultado.success = false;
            resultado.errorMessage = "Curso não encontrado.";
            return resultado;
        }

        UnidadeCurricular unidadeCurricular = this.ucCRUD.procurarPorNome(nomeUC);
        if (unidadeCurricular == null) {
            resultado.success = false;
            resultado.errorMessage = "Unidade Curricular não encontrada.";
            return resultado;
        }

        for (UnidadeCurricular unidadeCurricularExistente : curso.getUnidadeCurriculars()) {
            if (unidadeCurricularExistente.getNome().equalsIgnoreCase(nomeUC)) {
                resultado.success = false;
                resultado.errorMessage = "A UC '" + nomeUC + "' já está associada a este curso.";
                return resultado;
            }
        }

        if (curso.adicionarUnidadeCurricular(unidadeCurricular)) {
            Resultado resultado1 = this.cursoCRUD.registarArranqueAno(curso.getNome(), curso);
            if (resultado1.success) {
                resultado.success = true;
            } else {
                resultado.success = false;
                resultado.errorMessage = resultado1.errorMessage;
            }
        } else {
            resultado.success = false;
            resultado.errorMessage = "Limite de 5 UCs por ano atingido.";
        }
        return resultado;
    }

    private boolean isEstruturaCurricularValida (model.Curso curso) {
        boolean temAno1 = false;
        boolean temAno2 = false;
        boolean temAno3 = false;

        for (model.UnidadeCurricular unidadeCurricular : curso.getUnidadeCurriculars()) {
            if (unidadeCurricular.getAnoCurricular() == 1) temAno1 = true;
            if (unidadeCurricular.getAnoCurricular() == 2) temAno2 = true;
            if (unidadeCurricular.getAnoCurricular() == 3) temAno3 = true;
        }
        return temAno1 && temAno2 && temAno3;
    }

    private java.util.List<String> obterUCsSemMomentosDeAvaliacao(model.Curso curso) {
        java.util.List<String> unidadesCurricularesEmFalta = new java.util.ArrayList<>();

        for (model.UnidadeCurricular unidadeCurricular : curso.getUnidadeCurriculars()) {
            if (unidadeCurricular.getMomentosAvaliacao() == null || unidadeCurricular.getMomentosAvaliacao().isEmpty()) {
                unidadesCurricularesEmFalta.add(unidadeCurricular.getNome());
            }
        }
        return unidadesCurricularesEmFalta;
    }
}