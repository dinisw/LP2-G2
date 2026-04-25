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
        Resultado resultado = new Resultado();

        if (nome == null || nome.trim().isEmpty() || morada == null || morada.trim().isEmpty() ||
                email == null || email.trim().isEmpty() || hash == null || hash.trim().isEmpty() ||
                sigla == null || sigla.trim().isEmpty()) {
            resultado.success = false;
            resultado.errorMessage = "Todos os campos de texto (nome, morada, email, senha, sigla) são obrigatórios.";
            return resultado;
        }

        if (nif <= 0) {
            resultado.success = false;
            resultado.errorMessage = "O NIF fornecido é inválido.";
            return resultado;
        }

        if (dataNascimento == null) {
            resultado.success = false;
            resultado.errorMessage = "A data de nascimento fornecida é inválida.";
            return resultado;
        }

        if (docenteCRUD.procurarPorNif(nif) != null) {
            resultado.success = false;
            resultado.errorMessage = "Já existe um docente registado com este NIF.";
            return resultado;
        }

        if (docenteCRUD.procurarPorSigla(sigla) != null) {
            resultado.success = false;
            resultado.errorMessage = "Já existe um docente registado com esta sigla.";
            return resultado;
        }

        Docente docente = new Docente(nome, morada, nif, dataNascimento, email, hash, sigla, new ArrayList<>(), new ArrayList<>());
        StringBuilder avisos = new StringBuilder();

        DAL.UnidadeCurricularCRUD unidadeCurricularCRUDAtualizado = new DAL.UnidadeCurricularCRUD();

        if (nomesUC != null) {
            for ( String nomeUC : nomesUC) {
                if (nomeUC != null && !nomeUC.trim().isEmpty()) {
                    UnidadeCurricular unidadeCurricular = unidadeCurricularCRUDAtualizado.procurarPorNome(nomeUC.trim());
                    if (unidadeCurricular != null) {
                        if (unidadeCurricular.getDocente() != null && !unidadeCurricular.getDocente().getSigla().equals(docente.getSigla())) {
                            DAL.DocenteCRUD docenteCRUDParaRemover = new DAL.DocenteCRUD();
                            model.Docente docenteAntigo = docenteCRUDParaRemover.procurarPorNif(unidadeCurricular.getDocente().getNif());

                            if (docenteAntigo != null) {
                                docenteAntigo.getUnidadesCurriculares().removeIf(u -> u.getNome().equalsIgnoreCase(unidadeCurricular.getNome()));
                                docenteCRUDParaRemover.atualizarDocente(docenteAntigo);
                            }
                        }



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
            resultado.success = true;
            resultado.object = avisos.toString();
        } else {
            resultado.success = false;
            resultado.errorMessage = "Ocorreu um erro na base de dados ao tentar registar o docente.";
        }

        return resultado;
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
        Resultado resultado = new Resultado();

        if (nif <= 0) {
            resultado.success = false;
            resultado.errorMessage = "NIF inválido.";
            return resultado;
        }

        Docente docenteExistente = docenteCRUD.procurarPorNif(nif);

        if (docenteExistente == null) {
            resultado.success = false;
            resultado.errorMessage = "Docente não encontrado com o NIF informado.";
            return resultado;
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
            resultado.success = true;
        } else {
            resultado.success = false;
            resultado.errorMessage = "Erro ao guardar as alterações na base de dados.";
        }

        return resultado;
    }

    public Resultado alterarPassword(int nif, String novoHash) {
        Resultado resultado = new Resultado();

        if (nif <= 0) {
            resultado.success = false;
            resultado.errorMessage = "NIF inválido.";
            return resultado;
        }

        if (novoHash == null || novoHash.trim().isEmpty()) {
            resultado.success = false;
            resultado.errorMessage = "Dados de senha inválidos.";
            return resultado;
        }

        Docente docenteExistente = docenteCRUD.procurarPorNif(nif);

        if (docenteExistente == null) {
            resultado.success = false;
            resultado.errorMessage = "Docente não encontrado.";
            return resultado;
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
            resultado.success = true;
        } else {
            resultado.success = false;
            resultado.errorMessage = "Erro ao atualizar a password na base de dados.";
        }

        return resultado;
    }

    public Resultado eliminarDocente(int nif) {
        Resultado resultado = new Resultado();

        if (nif <= 0) {
            resultado.success = false;
            resultado.errorMessage = "NIF inválido.";
            return resultado;
        }

        if (docenteCRUD.procurarPorNif(nif) == null) {
            resultado.success = false;
            resultado.errorMessage = "Docente não encontrado com o NIF informado.";
            return resultado;
        }

        if (docenteCRUD.eliminarDocente(nif)) {
            resultado.success = true;
        } else {
            resultado.success = false;
            resultado.errorMessage = "Erro na base de dados ao eliminar o docente.";
        }

        return resultado;
    }
}