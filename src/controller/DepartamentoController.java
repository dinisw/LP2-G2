package controller;

import DAL.DepartamentoCRUD;
import model.Departamento;
import java.util.List;

public class DepartamentoController {
    private DepartamentoCRUD crud;

    public DepartamentoController() {
        this.crud = new DepartamentoCRUD();
    }

    // Regista e devolve true (sucesso) ou false (erro) para a View
    public boolean registarDepartamento(String nome, String sigla) {
        Departamento novo = new Departamento(nome, sigla);
        return crud.registarDepartamento(novo);
    }

    // Devolve a lista para a View imprimir
    public List<Departamento> listarDepartamentos() {
        return crud.getDepartamentos();
    }

    // Procura e devolve o objeto (ou null)
    public Departamento procurarDepartamento(String sigla) {
        return crud.procurarPorSigla(sigla);
    }

    // Pega nos novos dados e manda o CRUD atualizar
    public boolean atualizarDepartamento(String siglaAntiga, String novoNome) {
        Departamento dep = crud.procurarPorSigla(siglaAntiga);

        if (dep != null) {
            // Atualiza apenas se o nome não for vazio
            if (novoNome != null && !novoNome.isEmpty()) {
                dep.setNome(novoNome);
            }
            return crud.atualizarDepartamento(dep);
        }
        return false; // Retorna falso se não encontrou o departamento
    }

    // Manda eliminar
    public boolean eliminarDepartamento(String sigla) {
        return crud.eliminarDepartamento(sigla);
    }
}