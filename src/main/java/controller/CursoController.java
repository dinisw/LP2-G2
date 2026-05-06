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

    public Resultado <Curso> registarCurso(Curso curso) {
        Resultado <Curso> resultado = new Resultado();

        if (curso == null || curso.getNome() == null || curso.getNome().trim().isEmpty()) {
            resultado.sucesso = false;
            resultado.mensagemErro = "O nome do curso é obrigatório.";
            return resultado;
        }

        if (curso.getDepartamento() == null) {
            resultado.sucesso = false;
            resultado.mensagemErro = "O departamento associado é obrigatório.";
            return resultado;
        }

        if (cursoCRUD.registarCurso(curso) != null) {
            resultado.sucesso = true;
        } else {
            resultado.sucesso = false;
            resultado.mensagemErro = "Já existe um curso com o nome '" + curso.getNome() + "' no sistema.";
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

    public Resultado atualizarCurso(String nomeAntigo, Curso cursoNovo) {
        Resultado resultado = new Resultado();

        if (nomeAntigo == null || nomeAntigo.trim().isEmpty()) {
            resultado.sucesso = false;
            resultado.mensagemErro = "O nome do curso a atualizar é obrigatório.";
            return resultado;
        }

        if (cursoNovo == null || cursoNovo.getNome() == null || cursoNovo.getNome().trim().isEmpty()) {
            resultado.sucesso = false;
            resultado.mensagemErro = "O novo nome do curso não pode estar vazio.";
            return resultado;
        }

        Curso cursoOriginal = cursoCRUD.procurarPorNome(nomeAntigo);
        if (cursoOriginal == null) {
            resultado.sucesso = false;
            resultado.mensagemErro = "O curso original não foi encontrado na base de dados.";
            return resultado;
        }
        DAL.EstudanteCRUD estudanteCRUD = new DAL.EstudanteCRUD();
        boolean temEstudantes = false;
        for (model.Estudante e : estudanteCRUD.getEstudantes()) {
            if (e.getNomeCurso() != null && e.getNomeCurso().equalsIgnoreCase(nomeAntigo)) {
                temEstudantes = true;
                break;
            }
        }

        boolean temProfessores = false;
        for (model.UnidadeCurricular uc : cursoOriginal.getUnidadeCurriculars()) {
            if (uc.getDocente() != null) {
                temProfessores = true;
                break;
            }
        }

        boolean mudouEstrutura = !cursoOriginal.getNome().equalsIgnoreCase(cursoNovo.getNome()) ||
                cursoOriginal.getDuracao() != cursoNovo.getDuracao() ||
                !cursoOriginal.getDepartamento().getSigla().equalsIgnoreCase(cursoNovo.getDepartamento().getSigla());

        if (temEstudantes && temProfessores && mudouEstrutura) {
            resultado.sucesso = false;
            resultado.mensagemErro = "Bloqueado (Regra de Negócio): O curso já possui estudantes e professores. Apenas o valor da propina pode ser ajustado para anos futuros.";
            return resultado;
        }
        Resultado res = cursoCRUD.atualizarCurso(nomeAntigo, cursoNovo);
        if (res.sucesso) {
            resultado.sucesso = true;
            if (!nomeAntigo.equalsIgnoreCase(cursoNovo.getNome())) {
                try {
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
            resultado.sucesso = false;
            resultado.mensagemErro = res.mensagemErro;
        }

        return resultado;
    }

    public Resultado eliminarCurso(String nomeAntigo) {
        Resultado resultado = new Resultado();
        if (nomeAntigo == null || nomeAntigo.trim().isEmpty()) {
            resultado.sucesso = false;
            resultado.mensagemErro = "O nome do curso a eliminar é obrigatório.";
            return resultado;
        }

        Curso cursoOriginal = cursoCRUD.procurarPorNome(nomeAntigo);
        if (cursoOriginal == null) {
            resultado.sucesso = false;
            resultado.mensagemErro = "O curso especificado não foi encontrado no sistema.";
            return resultado;
        }

        if (cursoOriginal.isIniciado()) {
            resultado.sucesso = false;
            resultado.mensagemErro = "O sistema não pode permitir apagar um curso com alunos/iniciado.";
            return resultado;
        }

        EstudanteController ec = new EstudanteController();
        for (Estudante e : ec.listarEstudantes()) {
            if (e.getNomeCurso() != null && e.getNomeCurso().equalsIgnoreCase(nomeAntigo)) {
                resultado.sucesso = false;
                resultado.mensagemErro = "O sistema não pode permitir apagar um curso com alunos/iniciado.";
                return resultado;
            }
        }

        Resultado <Curso> res = cursoCRUD.eliminarCurso(nomeAntigo);
        if (res.sucesso) {
            resultado.sucesso = true;
        } else {
            resultado.sucesso = false;
            resultado.mensagemErro = res.mensagemErro;
        }
        return resultado;
    }

    public Resultado <Curso> iniciarAnoLetivo(String nome, int anoLetivo) {
        Resultado <Curso> resultado = new Resultado<>();

        if (anoLetivo < 1 || anoLetivo > 3) {
            resultado.sucesso = false;
            resultado.mensagemErro = "Ano letivo inválido. Os cursos têm 3 anos curriculares.";
            return resultado;
        }

        Curso curso = cursoCRUD.procurarPorNome(nome);
        if (curso == null) {
            resultado.sucesso = false;
            resultado.mensagemErro = "Curso não encontrado.";
            return resultado;
        }

        if (curso.isAnoIniciado(anoLetivo)) {
            resultado.sucesso = false;
            resultado.mensagemErro = "O " + anoLetivo + "º ano deste curso já se encontra iniciado.";
            return resultado;
        }

        if (!isEstruturaCurricularValida(curso)) {
            resultado.sucesso = false;
            resultado.mensagemErro = "Estrutura curricular incompleta (é obrigatório ter pelo menos uma UC em cada um dos 3 anos).";
            return resultado;
        }

        List<String> unidadesCurricularesSemMomentos = obterUCsSemMomentosDeAvaliacao(curso);
        if (!unidadesCurricularesSemMomentos.isEmpty()) {
            resultado.sucesso = false;
            resultado.mensagemErro = "As seguintes UCs não têm Momentos de Avaliação: " + String.join(",", unidadesCurricularesSemMomentos) + ".";
            return resultado;
        }

        controller.EstudanteController estudanteController = new controller.EstudanteController();
        List<Estudante> todosEstudantes = estudanteController.listarEstudantes();

        int alunosNesteAno = 0;
        for (Estudante estudante : todosEstudantes) {
            if (estudante.getNomeCurso() != null && estudante.getNomeCurso().equalsIgnoreCase(curso.getNome())) {

                int anoDoEstudante = estudanteController.obterAnoDesbloqueado(estudante);

                if (anoDoEstudante == anoLetivo) {
                    alunosNesteAno++;
                }
            }
        }

        int minimoExigido = (anoLetivo == 1) ? 5 : 1;

        if (alunosNesteAno < minimoExigido) {
            resultado.sucesso = false;
            resultado.mensagemErro = "O " + anoLetivo + "º ano exige no mínimo " + minimoExigido + " aluno(s). Atualmente tem " + alunosNesteAno + " aluno(s) apto(s).";
            return resultado;
        }

        curso.adicionarAnoIniciado(anoLetivo);
        Resultado <Curso> res = cursoCRUD.registarArranqueAno(curso.getNome(), curso);
        if (res.sucesso) {
            resultado.sucesso = true;
        } else {
            resultado.sucesso = false;
            resultado.mensagemErro = res.mensagemErro;
        }
        return resultado;
    }

    public Resultado<Curso> associarUCAoCurso (String nomeCurso, String nomeUC) {
        Resultado <Curso> resultado = new Resultado<>();

        if (nomeCurso == null || nomeCurso.trim().isEmpty() || nomeUC == null || nomeUC.trim().isEmpty()) {
            resultado.sucesso = false;
            resultado.mensagemErro = "O nome do curso e da UC são obrigatórios.";
            return resultado;
        }

        Curso curso = this.cursoCRUD.procurarPorNome(nomeCurso);
        if (curso == null) {
            resultado.sucesso = false;
            resultado.mensagemErro = "Curso não encontrado.";
            return resultado;
        }

        UnidadeCurricular unidadeCurricular = this.ucCRUD.procurarPorNome(nomeUC);
        if (unidadeCurricular == null) {
            resultado.sucesso = false;
            resultado.mensagemErro = "Unidade Curricular não encontrada.";
            return resultado;
        }

        for (UnidadeCurricular unidadeCurricularExistente : curso.getUnidadeCurriculars()) {
            if (unidadeCurricularExistente.getNome().equalsIgnoreCase(nomeUC)) {
                resultado.sucesso = false;
                resultado.mensagemErro = "A UC '" + nomeUC + "' já está associada a este curso.";
                return resultado;
            }
        }

        if (curso.adicionarUnidadeCurricular(unidadeCurricular)) {
            Resultado <Curso> resultado1 = this.cursoCRUD.registarArranqueAno(curso.getNome(), curso);
            if (resultado1.sucesso) {
                resultado.sucesso = true;
            } else {
                resultado.sucesso = false;
                resultado.mensagemErro = resultado1.mensagemErro;
            }
        } else {
            resultado.sucesso = false;
            resultado.mensagemErro = "Limite de 5 UCs por ano atingido.";
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