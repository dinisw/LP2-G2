package controller;

import DAL.DAOFactory;
import DAL.IAvaliacaoDAO;
import DAL.ICursoDAO;
import DAL.IDepartamentoDAO;
import DAL.IEstudanteDAO;
import DAL.IUnidadeCurricularDAO;
import model.*;

import java.util.List;
import java.util.stream.Collectors;

public class CursoController {

    private final ICursoDAO cursoDAO;
    private final IDepartamentoDAO depDAO;
    private final IUnidadeCurricularDAO ucDAO;

    public CursoController() {
        this.cursoDAO = DAOFactory.getCursoDAO();
        this.depDAO   = DAOFactory.getDepartamentoDAO();
        this.ucDAO    = DAOFactory.getUnidadeCurricularDAO();
    }

    public Resultado<Curso> registarCurso(Curso curso) {
        if (curso == null || curso.getNome() == null || curso.getNome().trim().isEmpty()) {
            return new Resultado<>(false, "O nome do curso é obrigatório.");
        }
        if (curso.getDepartamento() == null) {
            return new Resultado<>(false, "O departamento associado é obrigatório.");
        }
        return cursoDAO.registarCurso(curso);
    }

    public List<Curso> listarCursos() {
        return cursoDAO.getCursos();
    }

    public Curso procurarCurso(String nome) {
        if (nome == null || nome.trim().isEmpty()) return null;
        return cursoDAO.procurarPorNome(nome);
    }

    public Resultado atualizarCurso(String nomeAntigo, Curso cursoNovo) {
        if (nomeAntigo == null || nomeAntigo.trim().isEmpty()) {
            return new Resultado<>(false, "O nome do curso a atualizar é obrigatório.");
        }
        if (cursoNovo == null || cursoNovo.getNome() == null || cursoNovo.getNome().trim().isEmpty()) {
            return new Resultado<>(false, "O novo nome do curso não pode estar vazio.");
        }

        Curso cursoOriginal = cursoDAO.procurarPorNome(nomeAntigo);
        if (cursoOriginal == null) {
            return new Resultado<>(false, "O curso original não foi encontrado na base de dados.");
        }

        if (cursoOriginal.isIniciado()) {
            boolean tentouMudarNome = !cursoOriginal.getNome().equalsIgnoreCase(cursoNovo.getNome());
            boolean tentouMudarDepartamento = cursoNovo.getDepartamento() != null && cursoNovo.getDepartamento().getSigla() != null
                    && !cursoOriginal.getDepartamento().getSigla().equalsIgnoreCase(cursoNovo.getDepartamento().getSigla());

            if (tentouMudarNome || tentouMudarDepartamento) {
                return new Resultado<>(false, "Bloqueado: Não é possível alterar o Nome ou o Departamento de um curso que já iniciou atividade letiva.");
            }
        }

        Resultado res = cursoDAO.atualizarCurso(nomeAntigo, cursoNovo);
        if (res.sucesso && !nomeAntigo.equalsIgnoreCase(cursoNovo.getNome())) {
            try {
                IEstudanteDAO estudanteDAO = DAOFactory.getEstudanteDAO();
                for (Estudante estudante : estudanteDAO.getEstudantes()) {
                    if (estudante.getNomeCurso() != null && estudante.getNomeCurso().equalsIgnoreCase(nomeAntigo)) {
                        estudante.setNomeCurso(cursoNovo.getNome());
                        estudanteDAO.atualizarEstudante(estudante);
                    }
                }
            } catch (Exception e) {
                System.out.println("Aviso: Não foi possível sincronizar o novo nome nos perfis dos estudantes.");
            }
        }
        return res;
    }

    public Resultado eliminarCurso(String nomeAntigo) {
        if (nomeAntigo == null || nomeAntigo.trim().isEmpty()) {
            return new Resultado<>(false, "O nome do curso a eliminar é obrigatório.");
        }

        Curso cursoOriginal = cursoDAO.procurarPorNome(nomeAntigo);
        if (cursoOriginal == null) {
            return new Resultado<>(false, "O curso especificado não foi encontrado no sistema.");
        }
        if (cursoOriginal.isIniciado()) {
            return new Resultado<>(false, "O sistema não pode permitir apagar um curso com alunos/iniciado.");
        }

        IEstudanteDAO estudanteDAO = DAOFactory.getEstudanteDAO();
        for (Estudante e : estudanteDAO.getEstudantes()) {
            if (e.getNomeCurso() != null && e.getNomeCurso().equalsIgnoreCase(nomeAntigo)) {
                return new Resultado<>(false, "O sistema não pode permitir apagar um curso com alunos/iniciado.");
            }
        }

        return cursoDAO.eliminarCurso(nomeAntigo);
    }

    public Resultado<Curso> iniciarAnoLetivo(String nome, int anoLetivo) {
        if (anoLetivo < 1 || anoLetivo > 3) {
            return new Resultado<>(false, "Ano letivo inválido. Os cursos têm 3 anos curriculares.");
        }

        Curso curso = cursoDAO.procurarPorNome(nome);
        if (curso == null) return new Resultado<>(false, "Curso não encontrado.");
        if (curso.isAnoIniciado(anoLetivo)) {
            return new Resultado<>(false, "O " + anoLetivo + "º ano deste curso já se encontra iniciado.");
        }
        if (!isEstruturaCurricularValida(curso)) {
            return new Resultado<>(false, "Estrutura curricular incompleta (é obrigatório ter pelo menos uma UC em cada um dos 3 anos).");
        }

        List<String> ucsSemMomentos = obterUCsSemMomentosDeAvaliacao(curso);
        if (!ucsSemMomentos.isEmpty()) {
            return new Resultado<>(false, "As seguintes UCs não têm Momentos de Avaliação: " + String.join(",", ucsSemMomentos) + ".");
        }

        EstudanteController estudanteController = new EstudanteController();
        List<Estudante> todosEstudantes = estudanteController.listarEstudantes();
        int alunosNesteAno = 0;
        for (Estudante estudante : todosEstudantes) {
            if (estudante.getNomeCurso() != null && estudante.getNomeCurso().equalsIgnoreCase(curso.getNome())) {
                if (estudanteController.obterAnoDesbloqueado(estudante) == anoLetivo) {
                    alunosNesteAno++;
                }
            }
        }

        int minimoExigido = (anoLetivo == 1) ? 5 : 1;
        if (alunosNesteAno < minimoExigido) {
            return new Resultado<>(false, "O " + anoLetivo + "º ano exige no mínimo " + minimoExigido + " aluno(s). Atualmente tem " + alunosNesteAno + " aluno(s) apto(s).");
        }

        curso.adicionarAnoIniciado(anoLetivo);
        Resultado<Curso> resultado = cursoDAO.registarArranqueAno(curso.getNome(), curso);

        // Auto-criar registos de avaliação nulos para cada estudante × UC do ano × momento
        if (resultado.sucesso) {
            try {
                IAvaliacaoDAO avaliacaoDAO = DAOFactory.getAvaliacaoDAO();
                List<UnidadeCurricular> ucsDoAno = curso.getUnidadeCurriculars().stream()
                        .filter(uc -> uc.getAnoCurricular() == anoLetivo)
                        .collect(Collectors.toList());
                for (Estudante estudante : todosEstudantes) {
                    if (estudante.getNomeCurso() != null
                            && estudante.getNomeCurso().equalsIgnoreCase(curso.getNome())
                            && estudanteController.obterAnoDesbloqueado(estudante) == anoLetivo) {
                        for (UnidadeCurricular uc : ucsDoAno) {
                            for (String momento : uc.getMomentosAvaliacao()) {
                                avaliacaoDAO.registarAvaliacao(new Avaliacao(momento, null, uc, estudante));
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Aviso: erro ao auto-criar avaliações para o ano " + anoLetivo + ": " + e.getMessage());
            }
        }

        return resultado;
    }

    public Resultado<Curso> associarUCAoCurso(String nomeCurso, String nomeUC) {
        if (nomeCurso == null || nomeCurso.trim().isEmpty() || nomeUC == null || nomeUC.trim().isEmpty()) {
            return new Resultado<>(false, "O nome do curso e da UC são obrigatórios.");
        }

        Curso curso = cursoDAO.procurarPorNome(nomeCurso);
        if (curso == null) return new Resultado<>(false, "Curso não encontrado.");

        UnidadeCurricular unidadeCurricular = ucDAO.procurarPorNome(nomeUC);
        if (unidadeCurricular == null) return new Resultado<>(false, "Unidade Curricular não encontrada.");

        for (UnidadeCurricular uc : curso.getUnidadeCurriculars()) {
            if (uc.getNome().equalsIgnoreCase(nomeUC)) {
                return new Resultado<>(false, "A UC '" + nomeUC + "' já está associada a este curso.");
            }
        }

        if (curso.adicionarUnidadeCurricular(unidadeCurricular)) {
            return cursoDAO.atualizarCurso(curso.getNome(), curso);
        }
        return new Resultado<>(false, "Limite de 5 UCs por ano atingido.");
    }

    private boolean isEstruturaCurricularValida(Curso curso) {
        boolean temAno1 = false, temAno2 = false, temAno3 = false;
        for (UnidadeCurricular uc : curso.getUnidadeCurriculars()) {
            if (uc.getAnoCurricular() == 1) temAno1 = true;
            if (uc.getAnoCurricular() == 2) temAno2 = true;
            if (uc.getAnoCurricular() == 3) temAno3 = true;
        }
        return temAno1 && temAno2 && temAno3;
    }

    private List<String> obterUCsSemMomentosDeAvaliacao(Curso curso) {
        List<String> emFalta = new java.util.ArrayList<>();
        for (UnidadeCurricular uc : curso.getUnidadeCurriculars()) {
            if (uc.getMomentosAvaliacao() == null || uc.getMomentosAvaliacao().isEmpty()) {
                emFalta.add(uc.getNome());
            }
        }
        return emFalta;
    }
}
