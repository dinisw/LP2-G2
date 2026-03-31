package controller;

import DAL.CursoCRUD;
import DAL.DepartamentoCRUD;
import DAL.UnidadeCurricularCRUD;
import model.Curso;
import model.Departamento;
import model.UnidadeCurricular;
import java.util.List;

public class CursoController {

    // O Controller dos cursos precisa de falar com os dois ficheiros (Cursos e Departamentos)
    private CursoCRUD cursoCRUD;
    private DepartamentoCRUD depCRUD;
    private UnidadeCurricularCRUD ucCRUD;

    public CursoController() {
        this.cursoCRUD = new CursoCRUD();
        this.depCRUD = new DepartamentoCRUD();
        this.ucCRUD = new UnidadeCurricularCRUD();
    }

    public int registarCurso(String nome, String siglaDep, List<String> nomesUC) {
        Departamento dep = depCRUD.procurarPorSigla(siglaDep);

        if (dep == null) {
            return -1;
        }
        Curso novo = new Curso(nome, 3, dep);
        for (String nomeUC : nomesUC) {
            UnidadeCurricular uc = ucCRUD.procurarPorNome(nomeUC.trim());
            if (uc != null) {
                novo.adicionarUnidadeCurricular(uc);
            } else {
                System.out.println("Aviso: UC '" + nomeUC + "' não encontrada, ignorada.");
            }
        }
        if (cursoCRUD.registarCurso(novo)) {
            return 1;
        }

        return 0;
    }

    public List<Curso> listarCursos() {
        return cursoCRUD.getCursos();
    }

    public Curso procurarCurso(String nome) {
        return cursoCRUD.procurarPorNome(nome);
    }

    // Atualiza o curso mantendo o seu departamento original
    public boolean atualizarCurso(String nomeAntigo, String novoNome) {
        Curso curso = cursoCRUD.procurarPorNome(nomeAntigo);

        if (curso != null) {
            if (novoNome != null && !novoNome.isEmpty()) {
                // Criamos um curso atualizado, mantendo a duração (3) e o Departamento original
                Curso cursoAtualizado = new Curso(novoNome, curso.getDuracao(), curso.getDepartamento());
                return cursoCRUD.atualizarCurso(nomeAntigo, cursoAtualizado);
            }
        }
        return false;
    }

    // Elimina o curso
    public boolean eliminarCurso(String nome) {
        return cursoCRUD.eliminarCurso(nome);
    }
}