package DAL;

import model.Estudante;
import model.Resultado;
import java.time.LocalDate;
import java.util.List;

public class EstudanteCRUD extends AbstractCsvCRUD<Estudante> {

    public EstudanteCRUD() {
        super("estudantes.csv");
    }

    @Override
    protected Estudante mapearLinhaParaEntidade(String[] colunas) {
        try {
            int numeroMec = Integer.parseInt(colunas[0]);
            String nome = colunas[1];
            String morada = colunas[2];
            int nif = Integer.parseInt(colunas[3]);
            LocalDate dataNascimento = LocalDate.parse(colunas[4]);
            String email = colunas[5];
            String hash = colunas[6];
            String nomeCurso = colunas[7];
            boolean ativo = Boolean.parseBoolean(colunas[8]);

            return new Estudante(nome, morada, nif, dataNascimento, email, numeroMec, hash, nomeCurso, ativo);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected String mapearEntidadeParaLinha(Estudante e) {
        return String.format("%d;%s;%s;%d;%s;%s;%s;%s;%b",
                e.getNumeroMec(), sanitizar(e.getNome()), sanitizar(e.getMorada()), e.getNif(),
                e.getDataNascimento().toString(), sanitizar(e.getEmail()), sanitizar(e.getHash()),
                sanitizar(e.getNomeCurso()), e.isAtivo());
    }

    public int gerarNumeroMecanografico() {
        int anoAtual = java.time.LocalDate.now().getYear();
        int yy = anoAtual % 100;
        int prefixo = 1000000 + (yy * 10000);
        int baseComparacao = 100 + yy;
        int maxSequencia = 0;

        for (model.Estudante e : dados) {
            int baseEstudante = e.getNumeroMec() / 10000;

            if (baseEstudante == baseComparacao) {
                int sequencia = e.getNumeroMec() % 10000;
                if (sequencia > maxSequencia) {
                    maxSequencia = sequencia;
                }
            }
        }
        return prefixo + maxSequencia + 1;
    }

    public Resultado<Estudante> registarEstudante(Estudante estudante) {
        if (procurarPorNif(estudante.getNif()) != null) {
            return new Resultado<>(false, "Já existe um estudante com este NIF.");
        }
        dados.add(estudante);
        guardarTodosNoFicheiro();
        return new Resultado<>(estudante, true);
    }

    public Resultado<Estudante> atualizarEstudante(Estudante estudante) {
        for (int i = 0; i < dados.size(); i++) {
            if (dados.get(i).getNumeroMec() == estudante.getNumeroMec()) {
                dados.set(i, estudante);
                guardarTodosNoFicheiro();
                return new Resultado<>(estudante, true);
            }
        }
        return new Resultado<>(false, "Estudante não encontrado.");
    }

    public Resultado<Estudante> atualizarSenha(Estudante estudante) {
        return atualizarEstudante(estudante);
    }

    public Resultado<Estudante> eliminarEstudante(int numeroMec) {
        Estudante remover = lerEstudante(numeroMec);
        if (remover != null) {
            dados.remove(remover);
            guardarTodosNoFicheiro();
            return new Resultado<>(remover, true);
        }
        return new Resultado<>(false, "Estudante não encontrado.");
    }

    public List<Estudante> getEstudantes() {
        return dados;
    }

    public Estudante procurarPorNif(int nif) {
        return dados.stream().filter(e -> e.getNif() == nif).findFirst().orElse(null);
    }

    public Estudante lerEstudante(int numeroMec) {
        return dados.stream().filter(e -> e.getNumeroMec() == numeroMec).findFirst().orElse(null);
    }
}