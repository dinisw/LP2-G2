package controller;

import DAL.DocenteCRUD;
import DAL.UnidadeCurricularCRUD;
import model.Docente;
import model.Resultado;
import model.UnidadeCurricular;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DocenteController {
    private final DocenteCRUD docenteCRUD;
    private final UnidadeCurricularCRUD ucCRUD;

    public DocenteController() {
        this.docenteCRUD = new DocenteCRUD();
        this.ucCRUD = new UnidadeCurricularCRUD();
    }

    public Resultado registarDocente(String nome, String morada, int nif, LocalDate dataNascimento, String email, String hash, String sigla, List<String> nomesUC) {
        Resultado res = new Resultado();

        if (nome == null || nome.trim().isEmpty() || morada == null || morada.trim().isEmpty() ||
                email == null || email.trim().isEmpty() || hash == null || hash.trim().isEmpty() ||
                sigla == null || sigla.trim().isEmpty()) {
            res.success = false;
            res.errorMessage = "Todos os campos de texto (nome, morada, email, senha, sigla) são obrigatórios.";
            return res;
        }

        if (nif <= 0) {
            res.success = false;
            res.errorMessage = "O NIF fornecido é inválido.";
            return res;
        }

        if (dataNascimento == null) {
            res.success = false;
            res.errorMessage = "A data de nascimento fornecida é inválida.";
            return res;
        }

        if (docenteCRUD.procurarPorNif(nif) != null) {
            res.success = false;
            res.errorMessage = "Já existe um docente registado com este NIF.";
            return res;
        }

        if (docenteCRUD.procurarPorSigla(sigla) != null) {
            res.success = false;
            res.errorMessage = "Já existe um docente registado com esta sigla.";
            return res;
        }

        Docente docente = new Docente(nome, morada, nif, dataNascimento, email, hash, sigla, new ArrayList<>(), new ArrayList<>());
        StringBuilder avisos = new StringBuilder();

        DAL.UnidadeCurricularCRUD unidadeCurricularCRUDAtualizado = new DAL.UnidadeCurricularCRUD();

        if (nomesUC != null) {
            for ( String nomeUC : nomesUC) {
                if (nomeUC != null && !nomeUC.trim().isEmpty()) {
                    UnidadeCurricular unidadeCurricular = unidadeCurricularCRUDAtualizado.procurarPorNome(nomeUC.trim());
                    if (unidadeCurricular != null) {
                        docente.adicionarUnidadeCurricular(unidadeCurricular);
                        unidadeCurricular.setDocente(docente);
                        unidadeCurricularCRUDAtualizado.atualizarUC(unidadeCurricular.getNome(), unidadeCurricular);
                    } else {
                        avisos.append("UC '").append(nomeUC.trim()).append("' não encontrado (ignorada). ");
                    }
                }
            }
        }

        if (docenteCRUD.registarDocente(docente)) {
            res.success = true;
            res.object = avisos.toString();
        } else {
            res.success = false;
            res.errorMessage = "Ocorreu um erro na base de dados ao tentar registar o docente.";
        }

        return res;
    }

    public List<Docente> listarDocentes() {
        return docenteCRUD.getDocentes();
    }

    public Docente procurarDocentePorNif(int nif) {
        if (nif <= 0) {
            return null;
        }
        return docenteCRUD.procurarPorNif(nif);
    }

    public Docente procurarDocentePorSigla(String sigla) {
        if (sigla == null || sigla.trim().isEmpty()) {
            return null;
        }
        return docenteCRUD.procurarPorSigla(sigla);
    }

    public Resultado atualizarDocente(int nif, String novoNome, String novaMorada, LocalDate novaDataNascimento, String novoEmail) {
        Resultado res = new Resultado();

        if (nif <= 0) {
            res.success = false;
            res.errorMessage = "NIF inválido.";
            return res;
        }

        Docente docenteExistente = docenteCRUD.procurarPorNif(nif);

        if (docenteExistente == null) {
            res.success = false;
            res.errorMessage = "Docente não encontrado com o NIF informado.";
            return res;
        }

        String nomeFinal = (novoNome != null && !novoNome.trim().isEmpty()) ? novoNome : docenteExistente.getNome();
        String moradaFinal = (novaMorada != null && !novaMorada.trim().isEmpty()) ? novaMorada : docenteExistente.getMorada();
        LocalDate dataFinal = (novaDataNascimento != null) ? novaDataNascimento : docenteExistente.getDataNascimento();
        String emailFinal = (novoEmail != null && !novoEmail.trim().isEmpty()) ? novoEmail : docenteExistente.getEmail();

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

        if (docenteCRUD.atualizarDocente(docenteAtualizado)) {
            res.success = true;
        } else {
            res.success = false;
            res.errorMessage = "Erro ao guardar as alterações na base de dados.";
        }

        return res;
    }

    public Resultado alterarPassword(int nif, String novoHash) {
        Resultado res = new Resultado();

        if (nif <= 0) {
            res.success = false;
            res.errorMessage = "NIF inválido.";
            return res;
        }

        if (novoHash == null || novoHash.trim().isEmpty()) {
            res.success = false;
            res.errorMessage = "Dados de senha inválidos.";
            return res;
        }

        Docente docenteExistente = docenteCRUD.procurarPorNif(nif);

        if (docenteExistente == null) {
            res.success = false;
            res.errorMessage = "Docente não encontrado.";
            return res;
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

        if (docenteCRUD.atualizarDocente(docenteAtualizado)) {
            res.success = true;
        } else {
            res.success = false;
            res.errorMessage = "Erro ao atualizar a password na base de dados.";
        }

        return res;
    }

    public Resultado eliminarDocente(int nif) {
        Resultado res = new Resultado();

        if (nif <= 0) {
            res.success = false;
            res.errorMessage = "NIF inválido.";
            return res;
        }

        if (docenteCRUD.procurarPorNif(nif) == null) {
            res.success = false;
            res.errorMessage = "Docente não encontrado com o NIF informado.";
            return res;
        }

        if (docenteCRUD.eliminarDocente(nif)) {
            res.success = true;
        } else {
            res.success = false;
            res.errorMessage = "Erro na base de dados ao eliminar o docente.";
        }

        return res;
    }
}