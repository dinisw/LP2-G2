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
        Resultado res = new Resultado();

        if (nome == null || nome.trim().isEmpty() || morada == null || morada.trim().isEmpty() ||
                email == null || email.trim().isEmpty() || hash == null || hash.trim().isEmpty() ||
                cargo == null || cargo.trim().isEmpty()) {
            res.success = false;
            res.errorMessage = "Todos os campos de texto (nome, morada, email, senha, cargo) são obrigatórios.";
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

        if (gestorCRUD.procurarPorNif(nif) != null) {
            res.success = false;
            res.errorMessage = "Já existe um gestor registado com este NIF.";
            return res;
        }

        if (gestorCRUD.procurarPorEmail(email) != null) {
            res.success = false;
            res.errorMessage = "Já existe um gestor registado com este email.";
            return res;
        }

        Gestor gestor = new Gestor(nome, morada, nif, dataNascimento, email, hash, cargo);

        if (gestorCRUD.registarGestor(gestor)) {
            res.success = true;
        } else {
            res.success = false;
            res.errorMessage = "Ocorreu um erro na base de dados ao registar o gestor.";
        }

        return res;
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
        Resultado res = new Resultado();

        if (nif <= 0) {
            res.success = false;
            res.errorMessage = "NIF inválido.";
            return res;
        }

        Gestor gestorExistente = gestorCRUD.procurarPorNif(nif);
        if (gestorExistente == null) {
            res.success = false;
            res.errorMessage = "Gestor não encontrado com o NIF informado.";
            return res;
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
            res.errorMessage = "A nova senha não pode estar vazia.";
            return res;
        }

        Gestor gestorExistente = gestorCRUD.procurarPorNif(nif);
        if (gestorExistente == null) {
            res.success = false;
            res.errorMessage = "Gestor não encontrado.";
            return res;
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
            res.success = true;
        } else {
            res.success = false;
            res.errorMessage = "Erro ao atualizar a password na base de dados.";
        }

        return res;
    }

    public Resultado eliminarGestor(int nif) {
        Resultado res = new Resultado();

        if (nif <= 0) {
            res.success = false;
            res.errorMessage = "NIF inválido.";
            return res;
        }

        if (gestorCRUD.procurarPorNif(nif) == null) {
            res.success = false;
            res.errorMessage = "Gestor não encontrado com o NIF informado.";
            return res;
        }

        if (gestorCRUD.eliminarGestor(nif)) {
            res.success = true;
        } else {
            res.success = false;
            res.errorMessage = "Erro na base de dados ao tentar eliminar o gestor.";
        }

        return res;
    }
}