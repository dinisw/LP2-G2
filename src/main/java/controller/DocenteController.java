package controller;

import DAL.AvaliacaoCRUD;
import DAL.DocenteCRUD;
import DAL.UnidadeCurricularCRUD;
import model.Avaliacao;
import model.Docente;
import model.Estudante;
import model.Resultado;
import model.UnidadeCurricular;

import javax.print.Doc;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DocenteController {
    private final DocenteCRUD docenteCRUD;
    private final AvaliacaoCRUD avaliacaoCRUD;

    public DocenteController() {
        this.docenteCRUD = new DocenteCRUD();
        this.avaliacaoCRUD = new AvaliacaoCRUD();
    }

    // --- PODER EXCLUSIVO DO DOCENTE: Definir Momentos de Avaliação ---
    public Resultado<UnidadeCurricular> definirMomentosAvaliacao(String siglaDocente, int idUc, List<String> momentos) {
        UnidadeCurricularCRUD ucCRUD = new UnidadeCurricularCRUD();
        UnidadeCurricular uc = ucCRUD.procurarPorId(idUc);

        if (uc == null) return new Resultado<>(false, "Unidade Curricular não encontrada.");

        // Validação de Segurança: O docente só pode alterar a sua própria UC
        if (uc.getDocente() == null || !uc.getDocente().getSigla().equalsIgnoreCase(siglaDocente)) {
            return new Resultado<>(false, "Acesso Negado: Não é o docente responsável por esta Unidade Curricular.");
        }

        uc.setMomentosAvaliacao(momentos);
        boolean sucesso = ucCRUD.atualizarUCPorId(idUc, uc);

        return sucesso ? new Resultado<>(uc, true) : new Resultado<>(false, "Erro ao gravar momentos de avaliação.");
    }

    public Resultado<Docente> registarDocente(String nome, String morada, int nif, LocalDate dataNascimento, String email, String hash, String sigla, List<String> nomesUC) {
        if (nome == null || nome.trim().isEmpty() || morada == null || morada.trim().isEmpty() || email == null || email.trim().isEmpty() || hash == null || hash.trim().isEmpty() || sigla == null || sigla.trim().isEmpty()) {
            return new Resultado<>(false, "Todos os campos de texto são obrigatórios.");
        }
        if (nif <= 0) return new Resultado<>(false, "NIF inválido.");
        if (dataNascimento == null) return new Resultado<>(false, "Data de nascimento inválida.");
        if (docenteCRUD.procurarPorNif(nif) != null) return new Resultado<>(false, "Já existe um docente com este NIF.");
        if (docenteCRUD.procurarPorSigla(sigla) != null) return new Resultado<>(false, "Já existe um docente com esta sigla.");

        Docente docente = new Docente(nome, morada, nif, dataNascimento, email, hash, sigla, new ArrayList<>(), new ArrayList<>());

        // A associação de UCs foi simplificada, o Gestor orquestra isto melhor pelo UnidadeCurricularController
        return docenteCRUD.registarDocente(docente);
    }

    public Resultado<Docente> atualizarDocente(int nif, String novoNome, String novaMorada, LocalDate novaData) {
        if (nif <= 0) return new Resultado<>(false, "NIF inválido.");
        Docente existente = docenteCRUD.procurarPorNif(nif);
        if (existente == null) return new Resultado<>(false, "Docente não encontrado.");

        if (novoNome != null && !novoNome.trim().isEmpty()) existente.setNome(novoNome);
        if (novaMorada != null && !novaMorada.trim().isEmpty()) existente.setMorada(novaMorada);
        if (novaData != null) existente.setDataNascimento(novaData);

        return docenteCRUD.atualizarDocente(existente);
    }

    public Resultado<Docente> alterarPassword(int nif, String novoHash) {
        if (novoHash == null || novoHash.trim().isEmpty()) return new Resultado<>(false, "A nova senha não pode estar vazia.");
        Docente existente = docenteCRUD.procurarPorNif(nif);
        if (existente == null) return new Resultado<>(false, "Docente não encontrado.");

        existente.setHash(novoHash);
        return docenteCRUD.atualizarDocente(existente);
    }

    public Resultado<String> eliminarDocente(int nif) {
        if (docenteCRUD.procurarPorNif(nif) == null) return new Resultado<>(false, "Docente não encontrado.");
        return docenteCRUD.eliminarDocente(nif).sucesso ? new Resultado<>("ELIMINADO", true) : new Resultado<>(false, "Erro ao eliminar docente.");
    }

    public List<Docente> listarDocentes() { return docenteCRUD.getDocentes(); }
    public Docente procurarDocentePorNif(int nif) { return nif <= 0 ? null : docenteCRUD.procurarPorNif(nif); }
    public Docente procurarDocentePorSigla(String sigla) { return sigla == null ? null : docenteCRUD.procurarPorSigla(sigla); }

    public List<Estudante> listarAlunosPorUC(String nomeUC) {
        if (nomeUC == null || nomeUC.trim().isEmpty()) return new ArrayList<>();
        List<Avaliacao> avaliacoes = avaliacaoCRUD.listarPorUnidadeCurricular(nomeUC);
        List<Estudante> alunosUnicos = new ArrayList<>();
        List<Integer> mecs = new ArrayList<>();

        for (Avaliacao av : avaliacoes) {
            Estudante est = av.getEstudante();
            if (est != null && !mecs.contains(est.getNumeroMec())) {
                alunosUnicos.add(est);
                mecs.add(est.getNumeroMec());
            }
        }
        alunosUnicos.sort((a, b) -> a.getNome().compareToIgnoreCase(b.getNome()));
        return alunosUnicos;
    }
}