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
        Resultado resultado = new Resultado();

        if (nome == null || nome.trim().isEmpty() || sigla == null || sigla.trim().isEmpty()) {
            resultado.success = false;
            resultado.errorMessage = "O nome e a sigla do departamento são obrigatórios.";
            return resultado;
        }

        Departamento novo = new Departamento(nome, sigla);

        if (crud.registarDepartamento(novo)) {
            resultado.success = true;
        } else {
            resultado.success = false;
            resultado.errorMessage = "Já existe um departamento registado com a sigla '" + sigla + "'.";
        }

        return resultado;
    }

    public List<Departamento> listarTodosDepartamentos() {
        return crud.getDepartamentos();
    }

    public Departamento procurarDepartamento(String sigla) {
        return crud.procurarPorSigla(sigla);
    }

    public Resultado atualizarDepartamento(String siglaAntiga, String novoNome) {
        Resultado resultado = new Resultado();

        if (cursoCRUD.existeCursoComDepartamento(siglaAntiga)) {
            resultado.success = false;
            resultado.errorMessage = "O departamento possui cursos associados e não pode ser alterado.";
            return resultado;
        }

        Departamento dep = crud.procurarPorSigla(siglaAntiga);

        if (dep == null) {
            resultado.success = false;
            resultado.errorMessage = "Departamento não encontrado com a sigla informada.";
            return resultado;
        }

        if (novoNome == null || novoNome.trim().isEmpty()) {
            resultado.success = false;
            resultado.errorMessage = "O novo nome do departamento não pode estar vazio.";
            return resultado;
        }

        dep.setNome(novoNome);

        if (crud.atualizarDepartamento(dep)) {
            resultado.success = true;
        } else {
            resultado.success = false;
            resultado.errorMessage = "Ocorreu um erro ao guardar a atualização na base de dados.";
        }

        return resultado;
    }

    public Resultado eliminarDepartamento(String sigla) {
        Resultado resultado = new Resultado();

        if (cursoCRUD.existeCursoComDepartamento(sigla)) {
            resultado.success = false;
            resultado.errorMessage = "O departamento possui cursos associados e não pode ser eliminado.";
            return resultado;
        }

        if (crud.eliminarDepartamento(sigla)) {
            resultado.success = true;
        } else {
            resultado.success = false;
            resultado.errorMessage = "Departamento não encontrado com a sigla informada.";
        }

        return resultado;
    }
}