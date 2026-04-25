package DAL;

import model.Departamento;
import model.Curso;
import model.Estudante;
import model.UnidadeCurricular;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CursoCRUD {
    private static final String CAMINHO_FICHEIRO = "cursos.csv";
    private List<Curso> cursos;

    public CursoCRUD() {
        this.cursos = new ArrayList<>();
        carregarFicheiro();
    }

    private void carregarFicheiro() {
        File ficheiro = new File(CAMINHO_FICHEIRO);
        if (!ficheiro.exists()) return;

        // Instanciamos o CRUD dos departamentos para fazer a "tradução" da sigla
        DepartamentoCRUD depCRUD = new DepartamentoCRUD();
        UnidadeCurricularCRUD ucCRUD = new UnidadeCurricularCRUD();

        try (BufferedReader reader = new BufferedReader(new FileReader(CAMINHO_FICHEIRO))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                String[] dados = linha.split(";");
                if (dados.length >= 4) {
                    String nomeCurso = dados[0];
                    int duracao = Integer.parseInt(dados[1]);
                    String siglaDep = dados[2];
                    Departamento dep = depCRUD.procurarPorSigla(siglaDep);

                    Curso curso = new Curso(nomeCurso, duracao, dep);
                    curso.setIniciado(Boolean.parseBoolean(dados[3]));
                    if (dados.length > 4) {
                        for (int i = 4; i < dados.length; i++) {
                            UnidadeCurricular unidadeCurricular = ucCRUD.procurarPorNome(dados[i]);
                            if (unidadeCurricular != null) {
                                curso.adicionarUnidadeCurricular(unidadeCurricular);
                            }
                        }
                    }
                    cursos.add(curso);
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.out.println("Erro ao carregar cursos: " + e.getMessage());
        }
    }

    private void guardarTodosNoFicheiro() {
        try (PrintWriter print = new PrintWriter(new FileWriter(CAMINHO_FICHEIRO))) {
            for (Curso curso : cursos) {
                String sigla = (curso.getDepartamento() != null) ? curso.getDepartamento().getSigla() : "SEM_DEP";
                StringBuilder sb = new StringBuilder();
                sb.append(safe(curso.getNome())).append(";").append(curso.getDuracao()).append(";").append(sigla).append(";").append(curso.isIniciado());
                
                for (UnidadeCurricular unidadeCurricular : curso.getUnidadeCurriculars()) {
                    sb.append(";").append(safe(unidadeCurricular.getNome()));
                }
                
                print.println(sb.toString());
            }
        } catch (IOException e) {
            System.out.println("Erro ao guardar cursos: " + e.getMessage());
        }
    }

    public boolean registarCurso(Curso curso) {
        if (curso != null && procurarPorNome(curso.getNome()) == null) {
            cursos.add(curso);
            guardarTodosNoFicheiro();
            return true;
        }
        return false;
    }

    public List<Curso> getCursos() {
        return new ArrayList<>(cursos);
    }

    public Curso procurarPorNome(String nome) {
        for (Curso curso : cursos) {
            if (curso.getNome().equalsIgnoreCase(nome)) {
                return curso;
            }
        }
        return null;
    }

    private boolean temPessoasAlocadas(String nomeCurso) {
        EstudanteCRUD estudanteCRUD = new EstudanteCRUD();
        for (Estudante est : estudanteCRUD.getEstudantes()) {
            if (est.getNomeCurso() != null && est.getNomeCurso().equalsIgnoreCase(nomeCurso)) {
                return true;
            }
        }
        return false;
    }

    public boolean atualizarCurso(String nomeAntigo, Curso cursoNovo) {
        if (temPessoasAlocadas(nomeAntigo)) {
            System.out.println("Erro: O curso tem estudantes alocados e não pode ser alterado!");
            return false;
        }

        for (int i = 0; i < cursos.size(); i++) {
            if (cursos.get(i).getNome().equalsIgnoreCase(nomeAntigo)) {
                cursos.set(i, cursoNovo);
                guardarTodosNoFicheiro();
                return true;
            }
        }
        return false;
    }

    public boolean eliminarCurso(String nome) {
        if (temPessoasAlocadas(nome)) {
            System.out.println("Erro: O curso tem estudantes alocados e não pode ser eliminado!");
            return false;
        }

        for (int i = 0; i < cursos.size(); i++) {
            if (cursos.get(i).getNome().equalsIgnoreCase(nome)) {
                cursos.remove(i);
                guardarTodosNoFicheiro();
                return true;
            }
        }
        return false;
    }
    
    private String safe(Object o) {
        return (o == null) ? "SEM REGISTO" : o.toString();
    }

    // Verifica se existe algum curso associado a um departamento (pela sigla)
    public boolean existeCursoComDepartamento(String siglaDepartamento) {
        for (Curso curso : cursos) {
            if (curso.getDepartamento() != null && curso.getDepartamento().getSigla().equalsIgnoreCase(siglaDepartamento)) {
                return true;
            }
        }
        return false;
    }
}