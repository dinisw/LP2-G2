package DAL;

import DAL.core.CsvRepositorio;
import model.Docente;
import model.Resultado;
import model.UnidadeCurricular;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DocenteCRUD extends CsvRepositorio<Docente> {
    private final List<Docente> docentes;
    private static boolean carregandoRelacoes = false;

    public DocenteCRUD() {
        super("docentes.csv");
        this.docentes = carregarTodos();
        carregarRelacoesUCs();
    }

    private void carregarRelacoesUCs() {
        if (!carregandoRelacoes) {
            carregandoRelacoes = true;
            UnidadeCurricularCRUD ucCRUD = new UnidadeCurricularCRUD();
            List<UnidadeCurricular> todasUCs = ucCRUD.getUnidadeCurriculars();

            for (Docente docente : docentes) {
                for (UnidadeCurricular uc : todasUCs) {
                    if (uc.getDocente() != null && uc.getDocente().getSigla().equalsIgnoreCase(docente.getSigla())) {
                        docente.adicionarUnidadeCurricular(uc);
                    }
                }
            }
            carregandoRelacoes = false;
        }
    }

    @Override
    protected Docente mapearLinhaParaEntidade(String[] dados) {
        if (dados.length < 7) return null;
        try {
            return new Docente(
                    dados[0], // nome
                    dados[1], // morada
                    Integer.parseInt(dados[2]), // nif
                    LocalDate.parse(dados[3]), // data
                    dados[4], // email
                    dados[5], // hash
                    dados[6], // sigla
                    new ArrayList<>(), new ArrayList<>()
            );
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected String mapearEntidadeParaLinha(Docente docente) {
        String ucNames = docente.getUnidadesCurriculares().stream()
                .map(UnidadeCurricular::getNome)
                .collect(Collectors.joining(","));

        return String.join(DELIMITADOR,
                safe(docente.getNome()), safe(docente.getMorada()), safe(docente.getNif()),
                safe(docente.getDataNascimento()), safe(docente.getEmail()), safe(docente.getHash()),
                safe(docente.getSigla()), safe(ucNames.isEmpty() ? null : ucNames)
        );
    }

    public Resultado<Docente> registarDocente(Docente docente) {
        if (docente == null) return new Resultado<>(false, "Dados inválidos.");
        if (procurarPorNif(docente.getNif()) != null) return new Resultado<>(false, "NIF já existe.");
        
        docentes.add(docente);
        return guardarTodos(docentes) ? new Resultado<>(docente, true) : new Resultado<>(false, "Erro ao gravar CSV.");
    }

    public Resultado<Docente> atualizarDocente(Docente docente) {
        for (int i = 0; i < docentes.size(); i++) {
            if (docentes.get(i).getNif() == docente.getNif()) {
                docentes.set(i, docente);
                return guardarTodos(docentes) ? new Resultado<>(docente, true) : new Resultado<>(false, "Erro ao gravar CSV.");
            }
        }
        return new Resultado<>(false, "Docente não encontrado.");
    }

    public Resultado<Docente> eliminarDocente(int nif) {
        for (int i = 0; i < docentes.size(); i++) {
            if (docentes.get(i).getNif() == nif) {
                Docente removido = docentes.remove(i);
                return guardarTodos(docentes) ? new Resultado<>(removido, true) : new Resultado<>(false, "Erro ao gravar CSV.");
            }
        }
        return new Resultado<>(false, "Docente não encontrado.");
    }

    public List<Docente> getDocentes() { return new ArrayList<>(docentes); }
    public Docente procurarPorNif(int nif) { return docentes.stream().filter(d -> d.getNif() == nif).findFirst().orElse(null); }
    public Docente procurarPorSigla(String sigla) { return docentes.stream().filter(d -> d.getSigla().equalsIgnoreCase(sigla)).findFirst().orElse(null); }
}
