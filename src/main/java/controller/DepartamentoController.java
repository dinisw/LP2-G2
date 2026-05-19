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

    public Resultado <Departamento> registarDepartamento(String nome, String sigla) {
        Resultado <Departamento> resultado = new Resultado<>();

        if (nome == null || nome.trim().isEmpty() || sigla == null || sigla.trim().isEmpty()) {
            resultado.sucesso = false;
            resultado.mensagemErro = "O nome e a sigla do departamento são obrigatórios.";
            return resultado;
        }

        Departamento novo = new Departamento(nome, sigla);

        if (crud.registarDepartamento(novo)) {
            resultado.sucesso = true;
        } else {
            resultado.sucesso = false;
            resultado.mensagemErro = "Já existe um departamento registado com a sigla '" + sigla + "'.";
        }

        return resultado;
    }

    public List<Departamento> listarTodosDepartamentos() {
        return crud.getDepartamentos();
    }

    public Departamento procurarDepartamento(String sigla) {
        return crud.procurarPorSigla(sigla);
    }

    public Resultado <Departamento> atualizarDepartamento(String siglaAntiga, String novoNome) {
        Resultado <Departamento> resultado = new Resultado<>();

        if (cursoCRUD.existeCursoComDepartamento(siglaAntiga)) {
            resultado.sucesso = false;
            resultado.mensagemErro = "O departamento possui cursos associados e não pode ser alterado.";
            return resultado;
        }

        Departamento dep = crud.procurarPorSigla(siglaAntiga);

        if (dep == null) {
            resultado.sucesso = false;
            resultado.mensagemErro = "Departamento não encontrado com a sigla informada.";
            return resultado;
        }

        if (novoNome == null || novoNome.trim().isEmpty()) {
            resultado.sucesso = false;
            resultado.mensagemErro = "O novo nome do departamento não pode estar vazio.";
            return resultado;
        }

        dep.setNome(novoNome);

        if (crud.atualizarDepartamento(dep)) {
            resultado.sucesso = true;
        } else {
            resultado.sucesso = false;
            resultado.mensagemErro = "Ocorreu um erro ao guardar a atualização na base de dados.";
        }

        return resultado;
    }

    public Resultado <Departamento> eliminarDepartamento(String sigla) {

        DAL.CursoCRUD cursoCRUD1 = new DAL.CursoCRUD();
        boolean temCursos = cursoCRUD.getCursos().stream().anyMatch(c -> c.getDepartamento().getSigla().equalsIgnoreCase(sigla));

        if (temCursos) {
            return new Resultado<>(false, "Bloqueado: Não pode eliminar este departamento porque existem Cursos associados a ele.");
        }

        Resultado <Departamento> resultado = new Resultado<>();

        if (cursoCRUD.existeCursoComDepartamento(sigla)) {
            resultado.sucesso = false;
            resultado.mensagemErro = "O departamento possui cursos associados e não pode ser eliminado.";
            return resultado;
        }

        if (crud.eliminarDepartamento(sigla)) {
            resultado.sucesso = true;
        } else {
            resultado.sucesso = false;
            resultado.mensagemErro = "Departamento não encontrado com a sigla informada.";
        }

        return resultado;
    }
}