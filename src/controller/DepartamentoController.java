package controller;

import DAL.DepartamentoCRUD;
import DAL.CursoCRUD;
import model.Departamento;
import model.Resultado;
import java.util.List;

public class DepartamentoController {

    private DepartamentoCRUD crud;
    private CursoCRUD cursoCRUD;

    public DepartamentoController() {
        this.crud = new DepartamentoCRUD();
        this.cursoCRUD = new CursoCRUD();
    }

    public Resultado registarDepartamento(String nome, String sigla) {
        Resultado res = new Resultado();

        if (nome == null || nome.trim().isEmpty() || sigla == null || sigla.trim().isEmpty()) {
            res.success = false;
            res.errorMessage = "O nome e a sigla do departamento são obrigatórios.";
            return res;
        }

        Departamento novo = new Departamento(nome, sigla);

        if (crud.registarDepartamento(novo)) {
            res.success = true;
        } else {
            res.success = false;
            res.errorMessage = "Já existe um departamento registado com a sigla '" + sigla + "'.";
        }

        return res;
    }

    public List<Departamento> listarTodosDepartamentos() {
        return crud.getDepartamentos();
    }

    public Departamento procurarDepartamento(String sigla) {
        return crud.procurarPorSigla(sigla);
    }

    public Resultado atualizarDepartamento(String siglaAntiga, String novoNome) {
        Resultado res = new Resultado();

        if (cursoCRUD.existeCursoComDepartamento(siglaAntiga)) {
            res.success = false;
            res.errorMessage = "O departamento possui cursos associados e não pode ser alterado.";
            return res;
        }

        Departamento dep = crud.procurarPorSigla(siglaAntiga);

        if (dep == null) {
            res.success = false;
            res.errorMessage = "Departamento não encontrado com a sigla informada.";
            return res;
        }

        if (novoNome == null || novoNome.trim().isEmpty()) {
            res.success = false;
            res.errorMessage = "O novo nome do departamento não pode estar vazio.";
            return res;
        }

        dep.setNome(novoNome);

        if (crud.atualizarDepartamento(dep)) {
            res.success = true;
        } else {
            res.success = false;
            res.errorMessage = "Ocorreu um erro ao guardar a atualização na base de dados.";
        }

        return res;
    }

    public Resultado eliminarDepartamento(String sigla) {
        Resultado res = new Resultado();

        if (cursoCRUD.existeCursoComDepartamento(sigla)) {
            res.success = false;
            res.errorMessage = "O departamento possui cursos associados e não pode ser eliminado.";
            return res;
        }

        if (crud.eliminarDepartamento(sigla)) {
            res.success = true;
        } else {
            res.success = false;
            res.errorMessage = "Departamento não encontrado com a sigla informada.";
        }

        return res;
    }
}