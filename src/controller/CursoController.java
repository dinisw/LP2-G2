package controller;

import DAL.CursoCRUD;
import DAL.DepartamentoCRUD;
import DAL.UnidadeCurricularCRUD;
import model.Curso;
import model.Departamento;
import model.Resultado;
import model.UnidadeCurricular;
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

    public Resultado registarCurso(String nome, String siglaDep, List<String> nomesUC) {
        Resultado resultado = new Resultado();

        // 1. Validação de segurança dos inputs
        if (nome == null || nome.trim().isEmpty()) {
            resultado.success = false;
            resultado.errorMessage = "O nome do curso é obrigatório.";
            return resultado;
        }

        if (siglaDep == null || siglaDep.trim().isEmpty()) {
            resultado.success = false;
            resultado.errorMessage = "A sigla do departamento associado é obrigatória.";
            return resultado;
        }

        // 2. Regra de Negócio: O Departamento tem de existir
        Departamento dep = depCRUD.procurarPorSigla(siglaDep);
        if (dep == null) {
            resultado.success = false;
            resultado.errorMessage = "O Departamento com a sigla '" + siglaDep + "' não existe! Registe-o primeiro.";
            return resultado;
        }

        // Assumindo a duração padrão de 3 anos
        Curso novo = new Curso(nome, 3, dep);
        StringBuilder avisos = new StringBuilder();

        // 3. Associar UCs
        if (nomesUC != null) {
            for (String nomeUC : nomesUC) {
                if (nomeUC != null && !nomeUC.trim().isEmpty()) {
                    UnidadeCurricular unidadeCurricular = ucCRUD.procurarPorNome(nomeUC.trim());
                    if (unidadeCurricular != null) {
                        novo.adicionarUnidadeCurricular(unidadeCurricular);
                    } else {
                        avisos.append("UC '").append(nomeUC.trim()).append("' não encontrada (ignorada). ");
                    }
                }
            }
        }

        // 4. Gravar na DAL
        if (cursoCRUD.registarCurso(novo)) {
            resultado.success = true;
            resultado.object = avisos.toString();
        } else {
            resultado.success = false;
            resultado.errorMessage = "Já existe um curso com o nome '" + nome + "' no sistema.";
        }

        return resultado;
    }

    public List<Curso> listarCursos() {
        return cursoCRUD.getCursos();
    }

    public Curso procurarCurso(String nome) {
        // Validação de segurança
        if (nome == null || nome.trim().isEmpty()) {
            return null;
        }
        return cursoCRUD.procurarPorNome(nome);
    }

    public Resultado atualizarCurso(String nomeAntigo, String novoNome) {
        Resultado resultado = new Resultado();

        if (nomeAntigo == null || nomeAntigo.trim().isEmpty()) {
            resultado.success = false;
            resultado.errorMessage = "O nome do curso a atualizar é obrigatório.";
            return resultado;
        }

        if (novoNome == null || novoNome.trim().isEmpty()) {
            resultado.success = false;
            resultado.errorMessage = "O novo nome do curso não pode estar vazio.";
            return resultado;
        }

        Curso curso = cursoCRUD.procurarPorNome(nomeAntigo);

        if (curso == null) {
            resultado.success = false;
            resultado.errorMessage = "O curso original não foi encontrado na base de dados.";
            return resultado;
        }

        Curso cursoAtualizado = new Curso(novoNome, curso.getDuracao(), curso.getDepartamento());
        cursoAtualizado.setIniciado(curso.isIniciado());
        for (UnidadeCurricular unidadeCurricular : curso.getUnidadeCurriculars()) {
            cursoAtualizado.adicionarUnidadeCurricular(unidadeCurricular);
        }
        if (cursoCRUD.atualizarCurso(nomeAntigo, cursoAtualizado)) {
            resultado.success = true;
        } else {
            resultado.success = false;
            resultado.errorMessage = "Não foi possível atualizar. Verifique se já existe um curso com o nome '" + novoNome + "'.";
        }

        return resultado;
    }

    public Resultado eliminarCurso(String nome) {
        Resultado resultado = new Resultado();

        if (nome == null || nome.trim().isEmpty()) {
            resultado.success = false;
            resultado.errorMessage = "O nome do curso a eliminar é obrigatório.";
            return resultado;
        }

        if (cursoCRUD.procurarPorNome(nome) == null) {
            resultado.success = false;
            resultado.errorMessage = "O curso especificado não foi encontrado no sistema.";
            return resultado;
        }

        if (cursoCRUD.eliminarCurso(nome)) {
            resultado.success = true;

            try {
                DAL.EstudanteCRUD estudanteCRUD = new DAL.EstudanteCRUD();
                controller.EstudanteController estudanteController = new controller.EstudanteController();
                java.util.List<model.Estudante> todosEstudantes = estudanteController.listarEstudantes();

                for (model.Estudante estudante : todosEstudantes) {
                    if (estudante.getNomeCurso() != null && estudante.getNomeCurso().equalsIgnoreCase(nome)) {
                        estudante.setNomeCurso("SEM REGISTO");
                        estudanteCRUD.atualizarEstudante(estudante.getNumeroMec(), estudante);
                    }
                }
            } catch (Exception e) {
                System.out.println("Aviso interno: Não foi possível atualizar os perfis dos estudantes afetados.");
            }
        } else {
            resultado.success = false;
            resultado.errorMessage = "Erro na base de dados ao eliminar o curso (ex: tem alunos alocados).";
        }

        return resultado;
    }

    public Resultado iniciarCurso(String nome) {
        Resultado resultado = new Resultado();

        Curso curso = cursoCRUD.procurarPorNome(nome);
        if (curso == null) {
            resultado.success = false;
            resultado.errorMessage = "Curso não encontrado.";
            return resultado;
        }

        if (curso.isIniciado()) {
            resultado.success = false;
            resultado.errorMessage = "Este curso já se encontra iniciado.";
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
            resultado.errorMessage = "Não é possível iniciar o curso. As seguintes UCs não têm Momentos de Avaliação: " + String.join(",", unidadesCurricularesSemMomentos) + ".";
            return resultado;
        }

        controller.EstudanteController estudanteController = new controller.EstudanteController();
        List<model.Estudante> todosEstudantes = estudanteController.listarEstudantes();

        int alunosInscritos = 0;
        for (model.Estudante estudante : todosEstudantes) {
            if (estudante.getNomeCurso() != null && estudante.getNomeCurso().equalsIgnoreCase(curso.getNome())) {
                alunosInscritos++;
            }
        }

        if (alunosInscritos < 5) {
            resultado.success = false;
            resultado.errorMessage = "O curso não pode ser iniciado. Tem de ter no mínimo 5 alunos inscritos (Atualmente tem: " + alunosInscritos + ").";
            return resultado;
        }

        curso.setIniciado(true);
        if (cursoCRUD.atualizarCurso(curso.getNome(), curso)) {
            resultado.success = true;
        } else {
            resultado.success = false;
            resultado.errorMessage = "Erro ao guardar o estado do curso na base de dados.";
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

        CursoCRUD cursoCRUDAtualizado = new CursoCRUD();
        UnidadeCurricularCRUD unidadeCurricularCRUDAtualizado = new UnidadeCurricularCRUD();

        Curso curso = cursoCRUDAtualizado.procurarPorNome(nomeCurso);
        if (curso == null) {
            resultado.success = false;
            resultado.errorMessage = "Curso não encontrado.";
            return resultado;
        }

        UnidadeCurricular unidadeCurricular = unidadeCurricularCRUDAtualizado.procurarPorNome(nomeUC);
        if (unidadeCurricular == null) {
            resultado.success = false;
            resultado.errorMessage = "Unidade Curricular não encontrado";
            return resultado;
        }

        for (UnidadeCurricular unidadeCurricularExistente : curso.getUnidadeCurriculars()) {
            if (unidadeCurricularExistente.getNome().equalsIgnoreCase(nomeUC)) {
                resultado.success = false;
                resultado.errorMessage = "A UC '" + nomeUC + "' já está associada a este curso.";
                return resultado;
            }
        }

        boolean adicionadoComSucesso = curso.adicionarUnidadeCurricular(unidadeCurricular);

        if (adicionadoComSucesso) {
            if (cursoCRUDAtualizado.atualizarCurso(curso.getNome(), curso)) {
                resultado.success = true;
            } else {
                resultado.success = false;
                resultado.errorMessage = "Erro ao guardar a associação no ficheiro CSV.";
            }
        } else {
            resultado.success = false;
            resultado.errorMessage = "Limite Atingido: Não é possível associar a UC! O curso já tem 5 UCs no ano curricular" + unidadeCurricular.getAnoCurricular() + ".";
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