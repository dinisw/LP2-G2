package DAL;

import DAL.core.CsvRepositorio;
import model.Estudante;
import model.Resultado;

import java.time.LocalDate;
import java.util.List;

public class EstudanteCRUD extends CsvRepositorio<Estudante> {
    private final List<Estudante> estudantes;
    private int numeroMecCounter = 10000;

    public EstudanteCRUD() {
        super("estudantes.csv");
        this.estudantes = carregarTodos();
        
        for (Estudante e : estudantes) {
            if (e.getNumeroMec() >= numeroMecCounter) {
                numeroMecCounter = e.getNumeroMec() + 1;
            }
        }
    }

    // --- IMPLEMENTAÇÃO OBRIGATÓRIA DO CSV REPOSITORIO ---
    
    @Override
    protected Estudante mapearLinhaParaEntidade(String[] dados) {
        if (dados.length < 9) return null;
        try {
            return new Estudante(
                    dados[0], // nome
                    dados[1], // morada
                    Integer.parseInt(dados[2]), // nif
                    LocalDate.parse(dados[3]), // dataNascimento
                    dados[4], // email
                    Integer.parseInt(dados[5]), // numeroMec
                    dados[6], // hash
                    dados[7], // nomeCurso
                    Boolean.parseBoolean(dados[8]) // ativo
            );
        } catch (Exception e) {
            return null; // Ignora linhas mal formatadas
        }
    }

    @Override
    protected String mapearEntidadeParaLinha(Estudante estudante) {
        return String.join(DELIMITADOR,
                safe(estudante.getNome()),
                safe(estudante.getMorada()),
                safe(estudante.getNif()),
                safe(estudante.getDataNascimento()),
                safe(estudante.getEmail()),
                safe(estudante.getNumeroMec()),
                safe(estudante.getHash()),
                safe(estudante.getNomeCurso()),
                String.valueOf(estudante.isAtivo())
        );
    }

    // --- OPERAÇÕES CRUD COM 'RESULTADO<T>' ---

    public Resultado<Estudante> registarEstudante(Estudante estudante) {
        if (estudante == null) {
            return new Resultado<>(false, "Dados do estudante são inválidos.");
        }
        if (procurarPorNif(estudante.getNif()) != null) {
            return new Resultado<>(false, "Já existe um estudante registado com o NIF: " + estudante.getNif());
        }
        
        estudantes.add(estudante);
        boolean sucesso = guardarTodos(estudantes);
        
        return sucesso ? new Resultado<>(estudante, true) : new Resultado<>(false, "Erro ao gravar no ficheiro CSV.");
    }

    public Resultado<Estudante> atualizarEstudante(Estudante estudante) {
        if (estudante == null) return new Resultado<>(false, "Estudante inválido.");
        
        for (int i = 0; i < estudantes.size(); i++) {
            if (estudantes.get(i).getNumeroMec() == estudante.getNumeroMec()) {
                estudantes.set(i, estudante);
                boolean sucesso = guardarTodos(estudantes);
                return sucesso ? new Resultado<>(estudante, true) : new Resultado<>(false, "Erro ao atualizar ficheiro.");
            }
        }
        return new Resultado<>(false, "Estudante não encontrado.");
    }

    public Resultado<Estudante> atualizarSenha(Estudante estudante) {
        // Aproveitamos o mesmo método de atualização
        return atualizarEstudante(estudante);
    }

    public Resultado<Estudante> eliminarEstudante(int numeroMec) {
        for (int i = 0; i < estudantes.size(); i++) {
            if (estudantes.get(i).getNumeroMec() == numeroMec) {
                Estudante removido = estudantes.remove(i);
                boolean sucesso = guardarTodos(estudantes);
                return sucesso ? new Resultado<>(removido, true) : new Resultado<>(false, "Erro ao remover ficheiro.");
            }
        }
        return new Resultado<>(false, "Estudante não encontrado.");
    }

    // --- LEITURAS (READ) ---

    public List<Estudante> getEstudantes() {
        return estudantes;
    }

    public Estudante lerEstudante(int numeroMec) {
        return estudantes.stream().filter(e -> e.getNumeroMec() == numeroMec).findFirst().orElse(null);
    }

    public Estudante procurarPorNif(int nif) {
        return estudantes.stream().filter(e -> e.getNif() == nif).findFirst().orElse(null);
    }

    public int gerarNumeroMecanografico() {
        int anoAtual = LocalDate.now().getYear() % 100;
        int prefixo = 100 + anoAtual;
        int maxSequencia = estudantes.stream()
                .map(Estudante::getNumeroMec)
                .filter(mec -> (mec / 10000) % 100 == anoAtual)
                .map(mec -> mec % 10000)
                .max(Integer::compareTo)
                .orElse(0);
        return (prefixo * 10000) + maxSequencia + 1;
    }
}
