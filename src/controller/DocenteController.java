package controller;

import DAL.DocenteCRUD;
import DAL.UnidadeCurricularCRUD;
import model.Docente;
import model.UnidadeCurricular;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DocenteController {
    private final DocenteCRUD docenteCRUD;
    private final UnidadeCurricularCRUD ucCRUD;
    private final Docente docente;

    public DocenteController() {
        this.docenteCRUD = new DocenteCRUD();
        this.ucCRUD = new UnidadeCurricularCRUD();
        this.docente = new Docente();
    }

    // CREATE - Registar docente
    public boolean registarDocente(String nome, String morada, int nif, LocalDate dataNascimento, String email, String hash, String sigla, List<String> nomesUC) {
        if (nome == null || nome.isEmpty()) {
            System.out.println("Erro: Nome não pode estar vazio.");
            return false;
        }

        if (morada == null || morada.isEmpty()) {
            System.out.println("Erro: Morada não pode estar vazia.");
            return false;
        }

        if (nif <= 0) {
            System.out.println("Erro: NIF inválido.");
            return false;
        }

        if (dataNascimento == null) {
            System.out.println("Erro: Data de nascimento inválida.");
            return false;
        }

        if (email == null || email.isEmpty()) {
            System.out.println("Erro: Email não pode estar vazio.");
            return false;
        }

        if (hash == null || hash.isEmpty()) {
            System.out.println("Erro: Dados de senha inválidos.");
            return false;
        }

        if (sigla == null || sigla.isEmpty()) {
            System.out.println("Erro: Sigla não pode estar vazia.");
            return false;
        }

        // Verificar se já existe docente com mesmo NIF
        if (docenteCRUD.procurarPorNif(nif) != null) {
            System.out.println("Erro: Já existe docente com este NIF.");
            return false;
        }

        // Verificar se já existe docente com mesma sigla
        if (docenteCRUD.procurarPorSigla(sigla) != null) {
            System.out.println("Erro: Já existe docente com esta sigla.");
            return false;
        }

        Docente docente = new Docente(nome, morada, nif, dataNascimento, email, hash, sigla, new ArrayList<>(), new ArrayList<>());
        for (String nomeUC : nomesUC) {
            UnidadeCurricular uc = ucCRUD.procurarPorNome(nomeUC.trim());
            if (uc != null) {
                docente.adicionarUnidadeCurricular(uc);
            } else {
                System.out.println("Aviso: UC '" + nomeUC + "' não encontrada, ignorada.");
            }
        }
        return docenteCRUD.registarDocente(docente);
    }

    // READ - Listar todos os docentes
    public List<Docente> listarDocentes() {
        return docenteCRUD.getDocentes();
    }

    // READ - Procurar docente por NIF
    public Docente procurarDocentePorNif(int nif) {
        if (nif <= 0) {
            return null;
        }
        return docenteCRUD.procurarPorNif(nif);
    }

    // READ - Procurar docente por sigla
    public Docente procurarDocentePorSigla(String sigla) {
        if (sigla == null || sigla.isEmpty()) {
            return null;
        }
        return docenteCRUD.procurarPorSigla(sigla);
    }

    // UPDATE - Atualizar docente
    public boolean atualizarDocente(int nif, String novoNome, String novaMorada, LocalDate novaDataNascimento, String novoEmail) {
        if (nif <= 0) {
            System.out.println("Erro: NIF inválido.");
            return false;
        }

        Docente docenteExistente = docenteCRUD.procurarPorNif(nif);
        if (docenteExistente == null) {
            System.out.println("Erro: Docente não encontrado.");
            return false;
        }

        // Usar valores existentes se novos valores forem null/vazios
        String nomeFinal = (novoNome != null && !novoNome.isEmpty()) ? novoNome : docenteExistente.getNome();
        String moradaFinal = (novaMorada != null && !novaMorada.isEmpty()) ? novaMorada : docenteExistente.getMorada();
        LocalDate dataFinal = (novaDataNascimento != null) ? novaDataNascimento : docenteExistente.getDataNascimento();
        String emailFinal = (novoEmail != null && !novoEmail.isEmpty()) ? novoEmail : docenteExistente.getEmail();

        Docente docenteAtualizado = new Docente(
            nomeFinal,
            moradaFinal,
            docenteExistente.getNif(),
            dataFinal,
            emailFinal,
            docenteExistente.getHash(),
            docenteExistente.getSigla(),
            docenteExistente.getListaAvaliacao(),
            docenteExistente.getUnidadesCurriculares()
        );

        return docenteCRUD.atualizarDocente(docenteAtualizado);
    }

    // UPDATE - Alterar password
    public boolean alterarPassword(int nif, String novoHash) {
        if (nif <= 0) {
            System.out.println("Erro: NIF inválido.");
            return false;
        }

        if (novoHash == null || novoHash.isEmpty()) {
            System.out.println("Erro: Dados de senha inválidos.");
            return false;
        }

        Docente docenteExistente = docenteCRUD.procurarPorNif(nif);
        if (docenteExistente == null) {
            System.out.println("Erro: Docente não encontrado.");
            return false;
        }

        Docente docenteAtualizado = new Docente(
            docenteExistente.getNome(),
            docenteExistente.getMorada(),
            docenteExistente.getNif(),
            docenteExistente.getDataNascimento(),
            docenteExistente.getEmail(),
            novoHash,
            docenteExistente.getSigla(),
            docenteExistente.getListaAvaliacao(),
            docenteExistente.getUnidadesCurriculares()
        );

        return docenteCRUD.atualizarDocente(docenteAtualizado);
    }

    // DELETE - Eliminar docente
    public boolean eliminarDocente(int nif) {
        if (nif <= 0) {
            System.out.println("Erro: NIF inválido.");
            return false;
        }

        if (docenteCRUD.procurarPorNif(nif) == null) {
            System.out.println("Erro: Docente não encontrado.");
            return false;
        }

        return docenteCRUD.eliminarDocente(nif);
    }


    public String getSiglaDoDocenteAtual() {
        return this.docente.getSigla();
    }

}
