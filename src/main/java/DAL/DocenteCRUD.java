package DAL;

import model.Docente;
import model.Resultado;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DocenteCRUD extends AbstractCsvCRUD<Docente> {

    public DocenteCRUD() {
        super("docentes.csv");
    }

    @Override
    protected Docente mapearLinhaParaEntidade(String[] colunas) {
        try {
            String nome = colunas[0];
            String morada = colunas[1];
            int nif = Integer.parseInt(colunas[2]);
            LocalDate dataNascimento = LocalDate.parse(colunas[3]);
            String email = colunas[4];
            String hash = colunas[5];
            String sigla = colunas[6];

            return new Docente(nome, morada, nif, dataNascimento, email, hash, sigla, new ArrayList<>(), new ArrayList<>());
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected String mapearEntidadeParaLinha(Docente d) {
        return String.format("%s;%s;%d;%s;%s;%s;%s",
                d.getNome(), d.getMorada(), d.getNif(),
                d.getDataNascimento().toString(), d.getEmail(), d.getHash(), d.getSigla());
    }

    public Resultado<Docente> registarDocente(Docente docente) {
        if (procurarPorNif(docente.getNif()) != null) {
            return new Resultado<>(false, "NIF já existe.");
        }
        dados.add(docente);
        guardarTodosNoFicheiro();
        return new Resultado<>(docente, true);
    }

    public Resultado<Docente> atualizarDocente(Docente docente) {
        for (int i = 0; i < dados.size(); i++) {
            if (dados.get(i).getNif() == docente.getNif()) {
                dados.set(i, docente);
                guardarTodosNoFicheiro();
                return new Resultado<>(docente, true);
            }
        }
        return new Resultado<>(false, "Docente não encontrado.");
    }

    public Resultado<Docente> eliminarDocente(int nif) {
        Docente remover = procurarPorNif(nif);
        if (remover != null) {
            dados.remove(remover);
            guardarTodosNoFicheiro();
            return new Resultado<>(remover, true);
        }
        return new Resultado<>(false, "Docente não encontrado.");
    }

    public List<Docente> getDocentes() {
        return dados;
    }

    public Docente procurarPorSigla(String sigla) {
        return dados.stream().filter(d -> d.getSigla().equalsIgnoreCase(sigla)).findFirst().orElse(null);
    }

    public Docente procurarPorNif(int nif) {
        return dados.stream().filter(d -> d.getNif() == nif).findFirst().orElse(null);
    }
}