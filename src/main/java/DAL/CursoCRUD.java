package DAL;

import model.Curso;
import model.Departamento;
import model.Resultado;
import model.UnidadeCurricular;
import java.util.ArrayList;
import java.util.List;

public class CursoCRUD extends AbstractCsvCRUD<Curso> implements ICursoDAO {

    public CursoCRUD() {
        super("cursos.csv");
    }

    @Override
    protected Curso mapearLinhaParaEntidade(String[] colunas) {
        try {
            String nomeCurso = colunas[0];
            int duracao = Integer.parseInt(colunas[1]);
            String siglaDep = colunas[2];
            double precoAnual = Double.parseDouble(colunas[3].replace(",", "."));

            DepartamentoCRUD depCRUD = new DepartamentoCRUD();
            Departamento departamento = depCRUD.procurarPorSigla(siglaDep);

            Curso curso = new Curso(nomeCurso, duracao, departamento);
            curso.setPrecoAnual(precoAnual);

            if (colunas.length > 4 && !colunas[4].equals("Nenhum Curso Iniciado")) {
                List<Integer> anos = new ArrayList<>();
                for (String a : colunas[4].split(",")) {
                    anos.add(Integer.parseInt(a.trim()));
                }
                curso.setAnosIniciados(anos);
            }

            if (colunas.length > 5 && !colunas[5].isEmpty()) {
                UnidadeCurricularCRUD ucCRUD = new UnidadeCurricularCRUD();
                for (String nomeUc : colunas[5].split(",")) {
                    UnidadeCurricular uc = ucCRUD.procurarPorNome(nomeUc.trim());
                    if (uc != null) curso.adicionarUnidadeCurricular(uc);
                }
            }
            if (colunas.length > 6) {
                curso.setAtivo(!"false".equalsIgnoreCase(colunas[6].trim()));
            }
            return curso;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected String mapearEntidadeParaLinha(Curso curso) {
        String siglaDep = curso.getDepartamento() != null ? curso.getDepartamento().getSigla() : "N/A";

        String anosIniciadosStr = "Nenhum Curso Iniciado";
        if (curso.getAnosIniciados() != null && !curso.getAnosIniciados().isEmpty()) {
            List<String> anos = new ArrayList<>();
            for(int a : curso.getAnosIniciados()) anos.add(String.valueOf(a));
            anosIniciadosStr = String.join(",", anos);
        }

        String ucsStr = "";
        if (curso.getUnidadeCurriculars() != null && !curso.getUnidadeCurriculars().isEmpty()) {
            List<String> ucs = new ArrayList<>();
            for(UnidadeCurricular uc : curso.getUnidadeCurriculars()) ucs.add(uc.getNome());
            ucsStr = String.join(",", ucs);
        }

        return String.format("%s;%d;%s;%.2f;%s;%s;%s",
                curso.getNome(), curso.getDuracao(), siglaDep, curso.getPrecoAnual(), anosIniciadosStr, ucsStr, curso.isAtivo());
    }

    public Resultado<Curso> registarCurso(Curso curso) {
        if (procurarPorNome(curso.getNome()) != null) return new Resultado<>(false, "Já existe um curso com esse nome.");
        dados.add(curso);
        guardarTodosNoFicheiro();
        return new Resultado<>(curso, true);
    }

    public List<Curso> getCursos() { return dados; }

    public Curso procurarPorNome(String nome) {
        return dados.stream().filter(c -> c.getNome().equalsIgnoreCase(nome)).findFirst().orElse(null);
    }

    public Resultado<Curso> atualizarCurso(String nomeAntigo, Curso cursoNovo) {
        for (int i = 0; i < dados.size(); i++) {
            if (dados.get(i).getNome().equalsIgnoreCase(nomeAntigo)) {
                dados.set(i, cursoNovo);
                guardarTodosNoFicheiro();
                return new Resultado<>(cursoNovo, true);
            }
        }
        return new Resultado<>(false, "Curso não encontrado.");
    }

    public Resultado<Curso> eliminarCurso(String nome) {
        Curso remover = procurarPorNome(nome);
        if (remover != null) {
            dados.remove(remover);
            guardarTodosNoFicheiro();
            return new Resultado<>(remover, true);
        }
        return new Resultado<>(false, "Curso não encontrado.");
    }

    public Resultado<Curso> registarArranqueAno(String nomeCurso, Curso cursoAtualizado) {
        return atualizarCurso(nomeCurso, cursoAtualizado);
    }

    public boolean existeCursoComDepartamento(String siglaDepartamento) {
        return dados.stream().anyMatch(c -> c.getDepartamento() != null && c.getDepartamento().getSigla().equalsIgnoreCase(siglaDepartamento));
    }
}