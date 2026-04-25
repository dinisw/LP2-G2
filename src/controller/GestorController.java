package controller;

import DAL.GestorCRUD;
import model.Gestor;
import model.Resultado;
import java.time.LocalDate;
import java.util.List;

public class GestorController {
    private final GestorCRUD gestorCRUD;

    public GestorController() {
        this.gestorCRUD = new GestorCRUD();
    }

    public Resultado registarGestor(String nome, String morada, int nif, LocalDate dataNascimento, String email, String hash, String cargo) {
        Resultado resultado = new Resultado();

        if (nome == null || nome.trim().isEmpty() || morada == null || morada.trim().isEmpty() ||
                email == null || email.trim().isEmpty() || hash == null || hash.trim().isEmpty() ||
                cargo == null || cargo.trim().isEmpty()) {
            resultado.success = false;
            resultado.errorMessage = "Todos os campos de texto (nome, morada, email, senha, cargo) são obrigatórios.";
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

        if (gestorCRUD.procurarPorNif(nif) != null) {
            resultado.success = false;
            resultado.errorMessage = "Já existe um gestor registado com este NIF.";
            return resultado;
        }

        if (gestorCRUD.procurarPorEmail(email) != null) {
            resultado.success = false;
            resultado.errorMessage = "Já existe um gestor registado com este email.";
            return resultado;
        }

        Gestor gestor = new Gestor(nome, morada, nif, dataNascimento, email, hash, cargo);

        if (gestorCRUD.registarGestor(gestor)) {
            resultado.success = true;
        } else {
            resultado.success = false;
            resultado.errorMessage = "Ocorreu um erro na base de dados ao registar o gestor.";
        }

        return resultado;
    }

    public List<Gestor> listarGestores() {
        return gestorCRUD.getGestores();
    }

    public Gestor procurarGestorPorNif(int nif) {
        if (nif <= 0) {
            return null;
        }
        return gestorCRUD.procurarPorNif(nif);
    }

    public Gestor procurarGestorPorEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }
        return gestorCRUD.procurarPorEmail(email);
    }

    public Resultado atualizarGestor(int nif, String novoNome, String novaMorada, LocalDate novaDataNascimento, String novoEmail, String novoCargo) {
        Resultado resultado = new Resultado();

        if (nif <= 0) {
            resultado.success = false;
            resultado.errorMessage = "NIF inválido.";
            return resultado;
        }

        Gestor gestorExistente = gestorCRUD.procurarPorNif(nif);
        if (gestorExistente == null) {
            resultado.success = false;
            resultado.errorMessage = "Gestor não encontrado com o NIF informado.";
            return resultado;
        }

        String nomeFinal = (novoNome != null && !novoNome.trim().isEmpty()) ? novoNome : gestorExistente.getNome();
        String moradaFinal = (novaMorada != null && !novaMorada.trim().isEmpty()) ? novaMorada : gestorExistente.getMorada();
        LocalDate dataFinal = (novaDataNascimento != null) ? novaDataNascimento : gestorExistente.getDataNascimento();
        String emailFinal = (novoEmail != null && !novoEmail.trim().isEmpty()) ? novoEmail : gestorExistente.getEmail();
        String cargoFinal = (novoCargo != null && !novoCargo.trim().isEmpty()) ? novoCargo : gestorExistente.getCargo();

        Gestor gestorAtualizado = new Gestor(
                nomeFinal,
                moradaFinal,
                gestorExistente.getNif(),
                dataFinal,
                emailFinal,
                gestorExistente.getHash(),
                cargoFinal
        );

        if (gestorCRUD.atualizarGestor(gestorAtualizado)) {
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
            resultado.errorMessage = "A nova senha não pode estar vazia.";
            return resultado;
        }

        Gestor gestorExistente = gestorCRUD.procurarPorNif(nif);
        if (gestorExistente == null) {
            resultado.success = false;
            resultado.errorMessage = "Gestor não encontrado.";
            return resultado;
        }

        Gestor gestorAtualizado = new Gestor(
                gestorExistente.getNome(),
                gestorExistente.getMorada(),
                gestorExistente.getNif(),
                gestorExistente.getDataNascimento(),
                gestorExistente.getEmail(),
                novoHash,
                gestorExistente.getCargo()
        );

        // Se a sua classe GestorCRUD tiver o método atualizarSenha(gestorAtualizado),
        // pode usá-lo. Aqui uso o atualizarGestor para manter a consistência do seu original.
        if (gestorCRUD.atualizarGestor(gestorAtualizado)) {
            resultado.success = true;
        } else {
            resultado.success = false;
            resultado.errorMessage = "Erro ao atualizar a password na base de dados.";
        }

        return resultado;
    }

    public Resultado eliminarGestor(int nif) {
        Resultado resultado = new Resultado();

        if (nif <= 0) {
            resultado.success = false;
            resultado.errorMessage = "NIF inválido.";
            return resultado;
        }

        if (gestorCRUD.procurarPorNif(nif) == null) {
            resultado.success = false;
            resultado.errorMessage = "Gestor não encontrado com o NIF informado.";
            return resultado;
        }

        if (gestorCRUD.eliminarGestor(nif)) {
            resultado.success = true;
        } else {
            resultado.success = false;
            resultado.errorMessage = "Erro na base de dados ao tentar eliminar o gestor.";
        }

        return resultado;
    }
}