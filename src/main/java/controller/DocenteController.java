package controller;

import DAL.DAOFactory;
import DAL.IAvaliacaoDAO;
import DAL.ICursoDAO;
import DAL.IDocenteDAO;
import DAL.IUnidadeCurricularDAO;
import model.Avaliacao;
import model.Curso;
import model.Docente;
import model.Estudante;
import model.Resultado;
import model.UnidadeCurricular;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DocenteController {
    private final IDocenteDAO docenteDAO;
    private final IAvaliacaoDAO avaliacaoDAO;

    public DocenteController() {
        this.docenteDAO = DAOFactory.getDocenteDAO();
        this.avaliacaoDAO = DAOFactory.getAvaliacaoDAO();
    }

    public Resultado<UnidadeCurricular> definirMomentosAvaliacao(String siglaDocente, int idUc, List<String> momentos) {
        IUnidadeCurricularDAO ucDAO = DAOFactory.getUnidadeCurricularDAO();
        UnidadeCurricular uc = ucDAO.procurarPorId(idUc);

        if (uc == null) return new Resultado<>(false, "Unidade Curricular não encontrada.");

        if (uc.getDocente() == null || !uc.getDocente().getSigla().equalsIgnoreCase(siglaDocente)) {
            return new Resultado<>(false, "Acesso Negado: Não é o docente responsável por esta Unidade Curricular.");
        }

        // Bloquear se o ano letivo desta UC já foi iniciado em algum curso
        ICursoDAO cursoDAO = DAOFactory.getCursoDAO();
        for (Curso curso : cursoDAO.getCursos()) {
            boolean ucPertence = curso.getUnidadeCurriculars().stream()
                    .anyMatch(u -> u.getNome().equalsIgnoreCase(uc.getNome()));
            if (ucPertence && curso.isAnoIniciado(uc.getAnoCurricular())) {
                return new Resultado<>(false,
                        "Bloqueado: Não é possível alterar os momentos de avaliação após o início do ano letivo.");
            }
        }

        uc.setMomentosAvaliacao(momentos);
        boolean sucesso = ucDAO.atualizarUCPorId(idUc, uc);

        return sucesso ? new Resultado<>(uc, true) : new Resultado<>(false, "Erro ao gravar momentos de avaliação.");
    }

    public Resultado<Docente> registarDocente(String nome, String morada, int nif, LocalDate dataNascimento, String email, String hash, String sigla, List<String> nomesUC) {
        if (nome == null || nome.trim().isEmpty() || morada == null || morada.trim().isEmpty() || email == null || email.trim().isEmpty() || hash == null || hash.trim().isEmpty() || sigla == null || sigla.trim().isEmpty()) {
            return new Resultado<>(false, "Todos os campos de texto são obrigatórios.");
        }
        if (nif <= 0) return new Resultado<>(false, "NIF inválido.");
        if (dataNascimento == null) return new Resultado<>(false, "Data de nascimento inválida.");
        if (docenteDAO.procurarPorNif(nif) != null) return new Resultado<>(false, "Já existe um docente com este NIF.");
        // Verificar duplicado com sigla já normalizada para maiúsculas
        String siglaNormCheck = sigla.trim().toUpperCase();
        if (docenteDAO.procurarPorSigla(siglaNormCheck) != null) return new Resultado<>(false, "Já existe um docente com esta sigla.");

        // Email → minúsculas; sigla → MAIÚSCULAS (norma de apresentação)
        String emailNorm = email.trim().toLowerCase();
        String siglaNorm = sigla.trim().toUpperCase();

        Docente docente = new Docente(nome, morada, nif, dataNascimento, emailNorm, hash, siglaNorm, new ArrayList<>(), new ArrayList<>());
        return docenteDAO.registarDocente(docente);
    }

    public Resultado<Docente> atualizarDocente(int nif, String novoNome, String novaMorada, LocalDate novaData) {
        if (nif <= 0) return new Resultado<>(false, "NIF inválido.");
        Docente existente = docenteDAO.procurarPorNif(nif);
        if (existente == null) return new Resultado<>(false, "Docente não encontrado.");

        if (novoNome != null && !novoNome.trim().isEmpty()) existente.setNome(novoNome);
        if (novaMorada != null && !novaMorada.trim().isEmpty()) existente.setMorada(novaMorada);
        if (novaData != null) existente.setDataNascimento(novaData);

        return docenteDAO.atualizarDocente(existente);
    }

    public Resultado<Docente> alterarPassword(int nif, String novoHash) {
        if (novoHash == null || novoHash.trim().isEmpty()) return new Resultado<>(false, "A nova senha não pode estar vazia.");
        Docente existente = docenteDAO.procurarPorNif(nif);
        if (existente == null) return new Resultado<>(false, "Docente não encontrado.");

        existente.setHash(novoHash);
        return docenteDAO.atualizarDocente(existente);
    }

    public Resultado<String> eliminarDocente(int nif) {
        Docente docente = docenteDAO.procurarPorNif(nif);
        if (docente == null) return new Resultado<>(false, "Docente não encontrado.");

        // Verifica se o docente está atribuído a alguma UC
        IUnidadeCurricularDAO ucDAO = DAOFactory.getUnidadeCurricularDAO();
        boolean temUCsAtribuidas = ucDAO.getUnidadeCurriculars().stream()
                .anyMatch(uc -> uc.getDocente() != null
                        && uc.getDocente().getSigla().equalsIgnoreCase(docente.getSigla()));

        if (temUCsAtribuidas) {
            // Soft-delete: inativa em vez de eliminar (mantém integridade referencial)
            docente.setAtivo(false);
            Resultado<Docente> res = docenteDAO.atualizarDocente(docente);
            return res.sucesso
                    ? new Resultado<>("INATIVADO", true)
                    : new Resultado<>(false, "Erro ao desativar o docente.");
        }

        // Hard-delete: sem UCs associadas, pode eliminar
        return docenteDAO.eliminarDocente(nif).sucesso
                ? new Resultado<>("ELIMINADO", true)
                : new Resultado<>(false, "Erro ao eliminar docente.");
    }

    public Resultado<Docente> ativarDesativarDocente(int nif, boolean ativar) {
        Docente existente = docenteDAO.procurarPorNif(nif);
        if (existente == null) return new Resultado<>(false, "Docente não encontrado.");
        if (existente.isAtivo() == ativar) {
            return new Resultado<>(false, "O docente já se encontra " + (ativar ? "ativo" : "inativo") + ".");
        }
        existente.setAtivo(ativar);
        return docenteDAO.atualizarDocente(existente);
    }

    public List<Docente> listarDocentes() { return docenteDAO.getDocentes(); }
    public Docente procurarDocentePorNif(int nif) { return nif <= 0 ? null : docenteDAO.procurarPorNif(nif); }
    public Docente procurarDocentePorSigla(String sigla) { return sigla == null ? null : docenteDAO.procurarPorSigla(sigla); }

    public List<Estudante> listarAlunosPorUC(String nomeUC) {
        if (nomeUC == null || nomeUC.trim().isEmpty()) return new ArrayList<>();
        List<Avaliacao> avaliacoes = avaliacaoDAO.listarPorUnidadeCurricular(nomeUC);
        List<Estudante> alunosUnicos = new ArrayList<>();
        List<Integer> mec = new ArrayList<>();

        for (Avaliacao av : avaliacoes) {
            Estudante est = av.getEstudante();
            if (est != null && est.isAtivo() && !mec.contains(est.getNumeroMec())) {
                alunosUnicos.add(est);
                mec.add(est.getNumeroMec());
            }
        }
        alunosUnicos.sort((a, b) -> a.getNome().compareToIgnoreCase(b.getNome()));
        return alunosUnicos;
    }
}
