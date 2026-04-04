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
        Resultado res = new Resultado();

        // 1. Validação de segurança dos inputs
        if (nome == null || nome.trim().isEmpty()) {
            res.success = false;
            res.errorMessage = "O nome do curso é obrigatório.";
            return res;
        }

        if (siglaDep == null || siglaDep.trim().isEmpty()) {
            res.success = false;
            res.errorMessage = "A sigla do departamento associado é obrigatória.";
            return res;
        }

        // 2. Regra de Negócio: O Departamento tem de existir
        Departamento dep = depCRUD.procurarPorSigla(siglaDep);
        if (dep == null) {
            res.success = false;
            res.errorMessage = "O Departamento com a sigla '" + siglaDep + "' não existe! Registe-o primeiro.";
            return res;
        }

        // Assumindo a duração padrão de 3 anos
        Curso novo = new Curso(nome, 3, dep);
        StringBuilder avisos = new StringBuilder();

        // 3. Associar UCs
        if (nomesUC != null) {
            for (String nomeUC : nomesUC) {
                if (nomeUC != null && !nomeUC.trim().isEmpty()) {
                    UnidadeCurricular uc = ucCRUD.procurarPorNome(nomeUC.trim());
                    if (uc != null) {
                        novo.adicionarUnidadeCurricular(uc);
                    } else {
                        avisos.append("UC '").append(nomeUC.trim()).append("' não encontrada (ignorada). ");
                    }
                }
            }
        }

        // 4. Gravar na DAL
        if (cursoCRUD.registarCurso(novo)) {
            res.success = true;
            res.object = avisos.toString();
        } else {
            res.success = false;
            res.errorMessage = "Já existe um curso com o nome '" + nome + "' no sistema.";
        }

        return res;
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
        Resultado res = new Resultado();

        if (nomeAntigo == null || nomeAntigo.trim().isEmpty()) {
            res.success = false;
            res.errorMessage = "O nome do curso a atualizar é obrigatório.";
            return res;
        }

        if (novoNome == null || novoNome.trim().isEmpty()) {
            res.success = false;
            res.errorMessage = "O novo nome do curso não pode estar vazio.";
            return res;
        }

        Curso curso = cursoCRUD.procurarPorNome(nomeAntigo);

        if (curso == null) {
            res.success = false;
            res.errorMessage = "O curso original não foi encontrado na base de dados.";
            return res;
        }

        Curso cursoAtualizado = new Curso(novoNome, curso.getDuracao(), curso.getDepartamento());

        if (cursoCRUD.atualizarCurso(nomeAntigo, cursoAtualizado)) {
            res.success = true;
        } else {
            res.success = false;
            res.errorMessage = "Não foi possível atualizar. Verifique se já existe um curso com o nome '" + novoNome + "'.";
        }

        return res;
    }

    public Resultado eliminarCurso(String nome) {
        Resultado res = new Resultado();

        if (nome == null || nome.trim().isEmpty()) {
            res.success = false;
            res.errorMessage = "O nome do curso a eliminar é obrigatório.";
            return res;
        }

        if (cursoCRUD.procurarPorNome(nome) == null) {
            res.success = false;
            res.errorMessage = "O curso especificado não foi encontrado no sistema.";
            return res;
        }

        if (cursoCRUD.eliminarCurso(nome)) {
            res.success = true;
        } else {
            res.success = false;
            res.errorMessage = "Erro na base de dados ao eliminar o curso (ex: tem alunos alocados).";
        }

        return res;
    }
}