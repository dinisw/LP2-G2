package DAL;

import model.Curso;
import model.Departamento;
import model.Resultado;
import model.UnidadeCurricular;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CursoCRUD extends CsvRepositorio<Curso> {
    private final List<Curso> cursos;

    public CursoCRUD() {
        super("cursos.csv");
        this.cursos = carregarTodos();
    }

    // --- IMPLEMENTAÇÃO OBRIGATÓRIA DO CSV REPOSITORIO ---

    @Override
    protected Curso mapearLinhaParaEntidade(String[] dados) {
        if (dados.length < 5) return null;
        try {
            String nomeCurso = dados[0].trim();
            int duracao = Integer.parseInt(dados[1].trim());
            String siglaDep = dados[2].trim();
            double precoAnual = Double.parseDouble(dados[3].replace(",", "."));

            DepartamentoCRUD depCRUD = new DepartamentoCRUD();
            Departamento dep = depCRUD.procurarPorSigla(siglaDep);

            Curso curso = new Curso(nomeCurso, duracao, dep, precoAnual);

            String anosIniciadosStr = dados[4].trim();
            List<Integer> anosIniciados = new ArrayList<>();
            if (!anosIniciadosStr.equalsIgnoreCase("Nenhum Curso Iniciado") && !anosIniciadosStr.isEmpty()) {
                for (String ano : anosIniciadosStr.split(",")) {
                    anosIniciados.add(Integer.parseInt(ano.trim()));
                }
            }
            curso.setAnosIniciados(anosIniciados);

            if (dados.length > 5) {
                UnidadeCurricularCRUD ucCRUD = new UnidadeCurricularCRUD();
                for (int i = 5; i < dados.length; i++) {
                    UnidadeCurricular uc = ucCRUD.procurarPorNome(dados[i].trim());
                    if (uc != null) {
                        curso.adicionarUnidadeCurricular(uc);
                    }
                }
            }
            return curso;
        } catch (Exception e) {
            return null; // Ignora linhas com formatação corrompida
        }
    }

    @Override
    protected String mapearEntidadeParaLinha(Curso curso) {
        String anosStr = curso.getAnosIniciados().isEmpty() ? "Nenhum Curso Iniciado" : 
                curso.getAnosIniciados().stream().map(String::valueOf).collect(Collectors.joining(","));

        StringBuilder linha = new StringBuilder();
        linha.append(safe(curso.getNome())).append(DELIMITADOR);
        linha.append(curso.getDuracao()).append(DELIMITADOR);
        linha.append(safe(curso.getDepartamento() != null ? curso.getDepartamento().getSigla() : null)).append(DELIMITADOR);
        linha.append(String.format("%.2f", curso.getPrecoAnual()).replace(",", ".")).append(DELIMITADOR);
        linha.append(anosStr);

        for (UnidadeCurricular uc : curso.getUnidadeCurriculars()) {
            linha.append(DELIMITADOR).append(safe(uc.getNome()));
        }
        return linha.toString();
    }

    // --- OPERAÇÕES CRUD COM 'RESULTADO<T>' ---

    public Resultado<Curso> registarCurso(Curso curso) {
        if (curso == null) return new Resultado<>(false, "Dados de curso inválidos.");
        
        // Regra de Negócio do Gestor: Valida a existência de Departamento
        if (curso.getDepartamento() == null) {
            return new Resultado<>(false, "É obrigatório associar um Departamento existente para criar um Curso.");
        }
        
        if (procurarPorNome(curso.getNome()) != null) {
            return new Resultado<>(false, "Já existe um curso com esse nome.");
        }

        cursos.add(curso);
        boolean sucesso = guardarTodos(cursos);
        return sucesso ? new Resultado<>(curso, true) : new Resultado<>(false, "Erro ao gravar no ficheiro CSV.");
    }

    public Resultado<Curso> atualizarCurso(String nomeAntigo, Curso cursoNovo) {
        if (temPessoasAlocadas(nomeAntigo)) {
            return new Resultado<>(false, "O curso tem estudantes alocados e não pode ser alterado!");
        }

        for (int i = 0; i < cursos.size(); i++) {
            if (cursos.get(i).getNome().trim().equalsIgnoreCase(nomeAntigo)) {
                cursos.set(i, cursoNovo);
                boolean sucesso = guardarTodos(cursos);
                return sucesso ? new Resultado<>(cursoNovo, true) : new Resultado<>(false, "Erro ao atualizar curso.");
            }
        }
        return new Resultado<>(false, "Curso não encontrado.");
    }

    public Resultado<Curso> eliminarCurso(String nome) {
        if (temPessoasAlocadas(nome)) {
            return new Resultado<>(false, "O curso tem estudantes alocados e não pode ser eliminado!");
        }

        for (int i = 0; i < cursos.size(); i++) {
            if (cursos.get(i).getNome().trim().equalsIgnoreCase(nome)) {
                Curso removido = cursos.remove(i);
                boolean sucesso = guardarTodos(cursos);
                return sucesso ? new Resultado<>(removido, true) : new Resultado<>(false, "Erro ao eliminar curso.");
            }
        }
        return new Resultado<>(false, "Curso não encontrado para eliminação.");
    }

    public Resultado<Curso> registarArranqueAno(String nomeCurso, Curso cursoAtualizado) {
        for (int i = 0; i < cursos.size(); i++) {
            if (cursos.get(i).getNome().equalsIgnoreCase(nomeCurso)) {
                cursos.set(i, cursoAtualizado);
                boolean sucesso = guardarTodos(cursos);
                return sucesso ? new Resultado<>(cursoAtualizado, true) : new Resultado<>(false, "Erro ao registar arranque.");
            }
        }
        return new Resultado<>(false, "Curso não encontrado.");
    }

    // --- LEITURAS E VALIDAÇÕES AUXILIARES ---

    public List<Curso> getCursos() {
        return new ArrayList<>(cursos);
    }

    public Curso procurarPorNome(String nome) {
        return cursos.stream()
                .filter(c -> c.getNome().trim().equalsIgnoreCase(nome.trim()))
                .findFirst().orElse(null);
    }

    private boolean temPessoasAlocadas(String nomeCurso) {
        EstudanteCRUD estudanteCRUD = new EstudanteCRUD();
        return estudanteCRUD.getEstudantes().stream()
                .anyMatch(est -> est.getNomeCurso() != null && est.getNomeCurso().trim().equalsIgnoreCase(nomeCurso.trim()));
    }

    public boolean existeCursoComDepartamento(String siglaDepartamento) {
        return cursos.stream()
                .anyMatch(c -> c.getDepartamento() != null && c.getDepartamento().getSigla().trim().equalsIgnoreCase(siglaDepartamento));
    }
}
