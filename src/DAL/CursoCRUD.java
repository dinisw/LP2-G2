package DAL;

import model.Departamento;
import model.Curso;
import model.Estudante;
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

        try (BufferedReader reader = new BufferedReader(new FileReader(CAMINHO_FICHEIRO))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                String[] dados = linha.split(";");
                if (dados.length >= 3) {
                    String nomeCurso = dados[0];
                    int duracao = Integer.parseInt(dados[1]);
                    String siglaDep = dados[2];

                    // TRUQUE: Transformar a sigla num Objeto real
                    Departamento dep = depCRUD.procurarPorSigla(siglaDep);

                    Curso curso = new Curso(nomeCurso, duracao, dep);
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
                // Ao guardar, extraímos apenas a sigla para o ficheiro de texto
                String sigla = (curso.getDepartamento() != null) ? curso.getDepartamento().getSigla() : "SEM_DEP";
                print.println(curso.getNome() + ";" + curso.getDuracao() + ";" + sigla);
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

    // REGRA DE NEGÓCIO: Verifica se há alunos neste curso
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
}