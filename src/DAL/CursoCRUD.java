package DAL;

import model.*;

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
                    String anosIniciadosStr = dados[3];
                    List<Integer> anosIniciados = new ArrayList<>();

                    if (!anosIniciadosStr.equalsIgnoreCase("Nenhum Curso Iniciado") && !anosIniciadosStr.isEmpty()) {
                        String[] anos = anosIniciadosStr.split(",");
                        for (String ano : anos) {
                            anosIniciados.add(Integer.parseInt(ano.trim()));
                        }
                    }
                    curso.setAnosIniciados(anosIniciados);

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
            throw new RuntimeException("Erro interno ao carregar o ficheiro de cursos.", e);
        }
    }

    private void guardarTodosNoFicheiro() {
        try (PrintWriter print = new PrintWriter(new FileWriter(CAMINHO_FICHEIRO))) {
            for (Curso curso : cursos) {
                String anosStr = curso.getAnosIniciados().stream()
                        .map(String::valueOf)
                        .collect(java.util.stream.Collectors.joining(","));

                if (anosStr.isEmpty()) {
                    anosStr = "Nenhum Curso Iniciado";
                }

                StringBuilder linha = new StringBuilder();
                linha.append(safe(curso.getNome())).append(";");
                linha.append(curso.getDuracao()).append(";");
                linha.append(safe(curso.getDepartamento() != null ? curso.getDepartamento().getSigla() : null)).append(";");
                linha.append(anosStr);

                for (UnidadeCurricular uc : curso.getUnidadeCurriculars()) {
                    linha.append(";").append(safe(uc.getNome()));
                }

                print.println(linha.toString());
            }
        } catch (IOException e) {
            throw new RuntimeException("Erro interno ao guardar o ficheiro de cursos.", e);
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

    public Resultado atualizarCurso(String nomeAntigo, Curso cursoNovo) {
        Resultado resultado = new Resultado();

        if (temPessoasAlocadas(nomeAntigo)) {
            resultado.success = false;
            resultado.errorMessage = "O curso tem estudantes alocados e não pode ser alterado!";
            return resultado;
        }

        for (int i = 0; i < cursos.size(); i++) {
            if (cursos.get(i).getNome().equalsIgnoreCase(nomeAntigo)) {
                cursos.set(i, cursoNovo);
                guardarTodosNoFicheiro();
                resultado.success = true;
                return resultado;
            }
        }
        resultado.success = false;
        resultado.errorMessage = "Curso não encontrado para atualização.";
        return resultado;
    }

    public Resultado eliminarCurso(String nome) {
        Resultado resultado = new Resultado();

        if (temPessoasAlocadas(nome)) {
            resultado.success = false;
            resultado.errorMessage = "O curso tem estudantes alocados e não pode ser eliminado!";
            return resultado; // Retorna a mensagem em vez de fazer SOU
        }

        for (int i = 0; i < cursos.size(); i++) {
            if (cursos.get(i).getNome().equalsIgnoreCase(nome)) {
                cursos.remove(i);
                guardarTodosNoFicheiro();
                resultado.success = true;
                return resultado;
            }
        }
        resultado.success = false;
        resultado.errorMessage = "Curso não encontrado para eliminação.";
        return resultado;
    }
    
    private String safe(Object o) {
        return (o == null) ? "SEM REGISTO" : o.toString();
    }

    public boolean existeCursoComDepartamento(String siglaDepartamento) {
        for (Curso curso : cursos) {
            if (curso.getDepartamento() != null && curso.getDepartamento().getSigla().equalsIgnoreCase(siglaDepartamento)) {
                return true;
            }
        }
        return false;
    }
}