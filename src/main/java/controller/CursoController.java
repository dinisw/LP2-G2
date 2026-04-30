package controller;

import DAL.CursoCRUD;
import DAL.EstudanteCRUD;
import model.Curso;
import model.Estudante;
import model.Resultado;

import java.time.LocalDate;
import java.util.List;

public class CursoController {
    private final EstudanteCRUD estudanteCRUD;
    private final CursoCRUD cursoCRUD; // Injetado para validar regras de negócio transversais

    public EstudanteController() {
        this.estudanteCRUD = new EstudanteCRUD();
        this.cursoCRUD = new CursoCRUD();
    }

    // --- LÓGICA DE PROGRESSÃO E FICHA (Mantida com pequenas otimizações) ---

    public String obterFichaEstudanteFormatada(Estudante estudante) {
        if (estudante == null) return "Erro: Estudante não encontrado.";

        String dataNascimentoStr = (estudante.getDataNascimento() != null) ? estudante.getDataNascimento().toString() : "Não definida";
        String cursoStr = (estudante.getNomeCurso() != null && !estudante.getNomeCurso().trim().isEmpty()) ? estudante.getNomeCurso() : "Sem curso atribuído";

        boolean isConcluido = verificarSeCursoConcluido(estudante);
        String statusCurso = isConcluido ? "🎓 CONCLUÍDO" : "⏳ EM CURSO";

        int anoLetivoAtual = obterAnoDesbloqueado(estudante);

        return """
        --- FICHA DE ESTUDANTE ---
        Nome: %s
        Nº Mecanográfico: %s
        Email: %s
        NIF: %d
        Data Nascimento: %s
        Morada: %s
        Curso (Inscrição): %s
        Estado do Curso: %s
        Ano Letivo Atual: %dº Ano
        """.formatted(estudante.getNome(), estudante.getNumeroMec(), estudante.getEmail(),
                estudante.getNif(), dataNascimentoStr, estudante.getMorada(), cursoStr, statusCurso, anoLetivoAtual);
    }

    public int obterAnoDesbloqueado(Estudante estudante) {
        Curso curso = cursoCRUD.procurarPorNome(estudante.getNomeCurso());
        if (curso == null) return 1;

        int anoPorNotas = BLL.EstudanteCalculo.calcularAnoDesbloqueado(estudante, curso);
        PropinaController propinaController = new PropinaController();
        int anoReal = anoPorNotas;

        if (anoPorNotas >= 2 && !propinaController.isPropinaPaga(estudante.getNumeroMec(), 1)) {
            anoReal = 1; 
        } else if (anoPorNotas == 3 && !propinaController.isPropinaPaga(estudante.getNumeroMec(), 2)) {
            anoReal = 2; 
        }

        propinaController.gerarPropinaAnual(estudante.getNumeroMec(), anoReal);
        return anoReal;
    }

    public boolean verificarSeCursoConcluido(Estudante estudante) {
        Curso curso = cursoCRUD.procurarPorNome(estudante.getNomeCurso());
        if (curso == null) return false;

        if (BLL.EstudanteCalculo.isCursoConcluido(estudante, curso)) {
            PropinaController propinaController = new PropinaController();
            return propinaController.isPropinaPaga(estudante.getNumeroMec(), curso.getDuracao());
        }
        return false;
    }

    // --- OPERAÇÕES CRUD COM 'RESULTADO<T>' ---

    public int gerarNumeroMecanografico() {
        return estudanteCRUD.gerarNumeroMecanografico();
    }

    public Resultado<Integer> registarEstudante(String nome, String morada, int nif, LocalDate dataNascimento, String curso, String hash) {
        if (nome == null || nome.trim().isEmpty() || morada == null || morada.trim().isEmpty() ||
                curso == null || curso.trim().isEmpty() || hash == null || hash.trim().isEmpty()) {
            return new Resultado<>(false, "Todos os campos de texto são obrigatórios.");
        }
        if (nif <= 0) return new Resultado<>(false, "O NIF fornecido é inválido.");
        if (dataNascimento == null) return new Resultado<>(false, "A data de nascimento fornecida é inválida.");

        int numeroMec = estudanteCRUD.gerarNumeroMecanografico();
        String email = numeroMec + "@issmf.ipp.pt";

        Estudante estudante = new Estudante(nome, morada, nif, dataNascimento, email, numeroMec, hash, curso, true);
        
        Resultado<Estudante> res = estudanteCRUD.registarEstudante(estudante);
        if (res.sucesso) return new Resultado<>(numeroMec, true);
        
        return new Resultado<>(false, res.mensagemErro);
    }

    public Resultado<Estudante> atualizarEstudante(int numeroMec, String nome, String morada, String curso) {
        if (numeroMec <= 0) return new Resultado<>(false, "Número Mecanográfico inválido.");

        Estudante estudante = estudanteCRUD.lerEstudante(numeroMec);
        if (estudante == null) return new Resultado<>(false, "Estudante não encontrado.");

        if (nome != null && !nome.trim().isEmpty()) estudante.setNome(nome);
        if (morada != null && !morada.trim().isEmpty()) estudante.setMorada(morada);
        if (curso != null && !curso.trim().isEmpty()) estudante.setNomeCurso(curso);

        return estudanteCRUD.atualizarEstudante(estudante);
    }

    public Resultado<Estudante> alterarPassword(int numeroMec, String novaSenhaHash) {
        if (novaSenhaHash == null || novaSenhaHash.trim().isEmpty()) return new Resultado<>(false, "A nova senha não pode estar vazia.");
        
        Estudante estudante = estudanteCRUD.lerEstudante(numeroMec);
        if (estudante == null) return new Resultado<>(false, "Estudante não encontrado.");

        estudante.setHash(novaSenhaHash);
        return estudanteCRUD.atualizarSenha(estudante);
    }

    public Resultado<String> eliminarEstudante(int numeroMec) {
        Estudante estudante = estudanteCRUD.lerEstudante(numeroMec);
        if (estudante == null) return new Resultado<>(false, "Estudante não encontrado.");

        // REGRA DE NEGÓCIO: Não inativar/eliminar se o curso já iniciou
        if (estudante.getNomeCurso() != null && !estudante.getNomeCurso().equals("SEM REGISTO")) {
            Curso cursoInfo = cursoCRUD.procurarPorNome(estudante.getNomeCurso());
            
            if (cursoInfo != null && cursoInfo.getAnosIniciados() != null && !cursoInfo.getAnosIniciados().isEmpty()) {
                return new Resultado<>(false, "Bloqueado: Não é possível eliminar um estudante cujo curso já iniciou atividade letiva.");
            }

            if (!estudante.isAtivo()) return new Resultado<>(false, "O Estudante já se encontra inativo.");
            
            estudante.setAtivo(false);
            Resultado<Estudante> res = estudanteCRUD.atualizarEstudante(estudante);
            return res.sucesso ? new Resultado<>("INATIVADO", true) : new Resultado<>(false, res.mensagemErro);
        }

        Resultado<Estudante> res = estudanteCRUD.eliminarEstudante(numeroMec);
        return res.sucesso ? new Resultado<>("ELIMINADO", true) : new Resultado<>(false, res.mensagemErro);
    }

    // --- LEITURAS SIMPLES ---
    public List<Estudante> listarEstudantes() { return estudanteCRUD.getEstudantes(); }
    public Estudante procurarEstudantePorNif(int nif) { return nif <= 0 ? null : estudanteCRUD.procurarPorNif(nif); }
    public Estudante procurarEstudantePorNumeroMec(int mec) { return mec <= 0 ? null : estudanteCRUD.lerEstudante(mec); }
}
