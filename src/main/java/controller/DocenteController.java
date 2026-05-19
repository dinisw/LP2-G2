package controller;

import DAL.AvaliacaoCRUD;
import DAL.DocenteCRUD;
import DAL.UnidadeCurricularCRUD;
import model.*;

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

    public Resultado<UnidadeCurricular> definirMomentosAvaliacao(String siglaDocente, int idUc, List<String> momentos) {
        UnidadeCurricularCRUD ucCRUD = new UnidadeCurricularCRUD();
        UnidadeCurricular uc = ucCRUD.procurarPorId(idUc);

        if (uc == null) return new Resultado<>(false, "Unidade Curricular não encontrada.");

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
        if (docenteCRUD.procurarPorNif(nif) != null)
            return new Resultado<>(false, "Já existe um docente com este NIF.");
        if (docenteCRUD.procurarPorSigla(sigla) != null)
            return new Resultado<>(false, "Já existe um docente com esta sigla.");

        Docente docente = new Docente(nome, morada, nif, dataNascimento, email, hash, sigla, new ArrayList<>(), new ArrayList<>());

        Resultado<Docente> resultado = docenteCRUD.registarDocente(docente);

        if (resultado.sucesso && nomesUC != null && !nomesUC.isEmpty()) {
            UnidadeCurricularCRUD unidadeCurricularCRUD = new UnidadeCurricularCRUD();

            for (String nomeUc : nomesUC) {
                UnidadeCurricular unidadeCurricular = unidadeCurricularCRUD.procurarPorNome(nomeUc);
                if (unidadeCurricular != null) {
                    unidadeCurricular.setDocente(docente);

                    unidadeCurricularCRUD.atualizarUC(unidadeCurricular);
                }
            }
            docente.setUnidadesCurriculares(unidadeCurricularCRUD.getUnidadeCurriculars().stream().filter(u -> u.getNome() != null && u.getDocente() != null && u.getDocente().getSigla().equalsIgnoreCase(sigla)).toList());
        }

        return resultado;
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
        if (novoHash == null || novoHash.trim().isEmpty())
            return new Resultado<>(false, "A nova senha não pode estar vazia.");
        Docente existente = docenteCRUD.procurarPorNif(nif);
        if (existente == null) return new Resultado<>(false, "Docente não encontrado.");

        existente.setHash(novoHash);
        return docenteCRUD.atualizarDocente(existente);
    }

    public Resultado<String> eliminarDocente(int nif) {
        Docente docente = docenteCRUD.procurarPorNif(nif);
        if (docente == null) return  new Resultado<>(false, "Docente não encontrado.");

        UnidadeCurricularCRUD unidadeCurricularCRUD = new UnidadeCurricularCRUD();
        boolean temUcsAtribuidas = unidadeCurricularCRUD.getUnidadeCurriculars().stream().anyMatch(uc -> uc.getDocente() != null && uc.getDocente().getSigla().equalsIgnoreCase(docente.getSigla()));

        if (temUcsAtribuidas) {
            return new Resultado<>(false, "Bloqueado: Este docente é responsável por uma ou mais Unidades Curriculares e não pode ser eliminado sem antes ser substituído.");
        }
        return docenteCRUD.eliminarDocente(nif).sucesso ? new Resultado<>("ELIMINADO", true) : new Resultado<>(false, "Erro ao eliminar docente.");
    }

    public List<Docente> listarDocentes() {
        return docenteCRUD.getDocentes();
    }

    public Docente procurarDocentePorNif(int nif) {
        return nif <= 0 ? null : docenteCRUD.procurarPorNif(nif);
    }

    public Docente procurarDocentePorSigla(String sigla) {
        return sigla == null ? null : docenteCRUD.procurarPorSigla(sigla);
    }

    public List<Estudante> listarAlunosPorUC(String nomeUC) {
        if (nomeUC == null || nomeUC.trim().isEmpty()) return new ArrayList<>();

        DAL.EstudanteCRUD estudanteCRUD = new DAL.EstudanteCRUD();
        DAL.CursoCRUD cursoCRUD = new DAL.CursoCRUD();
        List<Estudante> alunosInscritos = new ArrayList<>();

        for (Estudante estudante : estudanteCRUD.getEstudantes()) {
            if (!estudante.isAtivo() || estudante.getNomeCurso() == null) continue;

            Curso curso = cursoCRUD.procurarPorNome(estudante.getNomeCurso());
            if (curso != null) {
                boolean cursoTemUc = curso.getUnidadeCurriculars().stream().anyMatch(uc -> uc.getNome().equalsIgnoreCase(nomeUC));
                if (cursoTemUc) {
                    int anoDaUc = curso.getUnidadeCurriculars().stream().filter(uc -> uc.getNome().equalsIgnoreCase(nomeUC)).findFirst().get().getAnoCurricular();
                    if (estudante.getAnoLetivo() >= anoDaUc) {
                        alunosInscritos.add(estudante);
                    }
                }
            }
        }
        alunosInscritos.sort((a, b) -> a.getNome().compareToIgnoreCase(b.getNome()));
        return alunosInscritos;
    }
}