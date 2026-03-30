package controller;

import DAL.DepartamentoCRUD;
import DAL.CursoCRUD;
import model.Departamento;
import java.util.List;

public class DepartamentoController {
    private DepartamentoCRUD crud;
    private CursoCRUD cursoCRUD;
    
    public DepartamentoController() {
        this.crud = new DepartamentoCRUD();
        this.cursoCRUD = new CursoCRUD();
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
        if (cursoCRUD.existeCursoComDepartamento(siglaAntiga)) {
            System.out.println("Erro: O departamento tem cursos associados e não pode ser alterado!");
            return false;
        }

        Departamento dep = crud.procurarPorSigla(siglaAntiga);

        if (dep != null) {
            if (novoNome != null && !novoNome.isEmpty()) {
                dep.setNome(novoNome);
            }
            return crud.atualizarDepartamento(dep);
        }
        return false;
    }

    // Manda eliminar
    public boolean eliminarDepartamento(String sigla) {
        if (cursoCRUD.existeCursoComDepartamento(sigla)) {
            return false;
        }
        return crud.eliminarDepartamento(sigla);
    }
}