package controller;

import DAL.DAOFactory;
import DAL.ICursoDAO;
import DAL.IDepartamentoDAO;
import model.Departamento;
import model.Resultado;
import java.util.List;

public class DepartamentoController {

    private final IDepartamentoDAO departamentoDAO;
    private final ICursoDAO cursoDAO;

    public DepartamentoController() {
        this.departamentoDAO = DAOFactory.getDepartamentoDAO();
        this.cursoDAO = DAOFactory.getCursoDAO();
    }

    public Resultado<Departamento> registarDepartamento(String nome, String sigla) {
        if (nome == null || nome.trim().isEmpty() || sigla == null || sigla.trim().isEmpty()) {
            return new Resultado<>(false, "O nome e a sigla do departamento são obrigatórios.");
        }

        Departamento novo = new Departamento(nome, sigla);
        return departamentoDAO.registarDepartamento(novo)
                ? new Resultado<>(novo, true)
                : new Resultado<>(false, "Já existe um departamento registado com a sigla '" + sigla + "'.");
    }

    public List<Departamento> listarTodosDepartamentos() {
        return departamentoDAO.getDepartamentos();
    }

    public Departamento procurarDepartamento(String sigla) {
        return departamentoDAO.procurarPorSigla(sigla);
    }

    public Resultado<Departamento> atualizarDepartamento(String siglaAntiga, String novoNome) {
        if (cursoDAO.existeCursoComDepartamento(siglaAntiga)) {
            return new Resultado<>(false, "O departamento possui cursos associados e não pode ser alterado.");
        }

        Departamento dep = departamentoDAO.procurarPorSigla(siglaAntiga);
        if (dep == null) return new Resultado<>(false, "Departamento não encontrado com a sigla informada.");
        if (novoNome == null || novoNome.trim().isEmpty()) return new Resultado<>(false, "O novo nome do departamento não pode estar vazio.");

        dep.setNome(novoNome);
        return departamentoDAO.atualizarDepartamento(dep)
                ? new Resultado<>(dep, true)
                : new Resultado<>(false, "Ocorreu um erro ao guardar a atualização na base de dados.");
    }

    public Resultado<Departamento> eliminarDepartamento(String sigla) {
        boolean temCursos = cursoDAO.getCursos().stream()
                .anyMatch(c -> c.getDepartamento() != null && c.getDepartamento().getSigla().equalsIgnoreCase(sigla));

        if (temCursos) {
            return new Resultado<>(false, "Bloqueado: Não pode eliminar este departamento porque existem Cursos associados a ele.");
        }

        return departamentoDAO.eliminarDepartamento(sigla)
                ? new Resultado<>(null, true)
                : new Resultado<>(false, "Departamento não encontrado com a sigla informada.");
    }
}
