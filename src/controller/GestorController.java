package controller;

import DAL.GestorCRUD;
import model.Gestor;
import view.GestorView;
import java.time.LocalDate;
import java.util.List;

public class GestorController {
    private final GestorCRUD gestorCRUD;

    public GestorController() {
        this.gestorCRUD = new GestorCRUD();
    }

    // CREATE - Registar gestor
    public boolean registarGestor(String nome, String morada, int nif, LocalDate dataNascimento, String email, String hash, String cargo) {
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

        if (cargo == null || cargo.isEmpty()) {
            System.out.println("Erro: Cargo não pode estar vazio.");
            return false;
        }

        // Verificar se já existe gestor com mesmo NIF
        if (gestorCRUD.procurarPorNif(nif) != null) {
            System.out.println("Erro: Já existe gestor com este NIF.");
            return false;
        }

        // Verificar se já existe gestor com mesmo email
        if (gestorCRUD.procurarPorEmail(email) != null) {
            System.out.println("Erro: Já existe gestor com este email.");
            return false;
        }

        Gestor gestor = new Gestor(nome, morada, nif, dataNascimento, email, hash, cargo);
        return gestorCRUD.registarGestor(gestor);
    }

    // READ - Listar todos os gestores
    public List<Gestor> listarGestores() {
        return gestorCRUD.getGestores();
    }

    // READ - Procurar gestor por NIF
    public Gestor procurarGestorPorNif(int nif) {
        if (nif <= 0) {
            return null;
        }
        return gestorCRUD.procurarPorNif(nif);
    }

    // READ - Procurar gestor por email
    public Gestor procurarGestorPorEmail(String email) {
        if (email == null || email.isEmpty()) {
            return null;
        }
        return gestorCRUD.procurarPorEmail(email);
    }

    // UPDATE - Atualizar gestor
    public boolean atualizarGestor(int nif, String novoNome, String novaMorada, LocalDate novaDataNascimento, String novoEmail, String novoCargo) {
        if (nif <= 0) {
            System.out.println("Erro: NIF inválido.");
            return false;
        }

        Gestor gestorExistente = gestorCRUD.procurarPorNif(nif);
        if (gestorExistente == null) {
            System.out.println("Erro: Gestor não encontrado.");
            return false;
        }

        // Usar valores existentes se novos valores forem null/vazios
        String nomeFinal = (novoNome != null && !novoNome.isEmpty()) ? novoNome : gestorExistente.getNome();
        String moradaFinal = (novaMorada != null && !novaMorada.isEmpty()) ? novaMorada : gestorExistente.getMorada();
        LocalDate dataFinal = (novaDataNascimento != null) ? novaDataNascimento : gestorExistente.getDataNascimento();
        String emailFinal = (novoEmail != null && !novoEmail.isEmpty()) ? novoEmail : gestorExistente.getEmail();
        String cargoFinal = (novoCargo != null && !novoCargo.isEmpty()) ? novoCargo : gestorExistente.getCargo();

        Gestor gestorAtualizado = new Gestor(
            nomeFinal,
            moradaFinal,
            gestorExistente.getNif(),
            dataFinal,
            emailFinal,
            gestorExistente.getHash(),
            cargoFinal
        );

        return gestorCRUD.atualizarGestor(gestorAtualizado);
    }

    // UPDATE - Alterar password
    public boolean alterarPassword(int nif, String novoHash, String novoSalt) {
        if (nif <= 0) {
            System.out.println("Erro: NIF inválido.");
            return false;
        }

        if (novoHash == null || novoHash.isEmpty() || novoSalt == null || novoSalt.isEmpty()) {
            System.out.println("Erro: Dados de senha inválidos.");
            return false;
        }

        Gestor gestorExistente = gestorCRUD.procurarPorNif(nif);
        if (gestorExistente == null) {
            System.out.println("Erro: Gestor não encontrado.");
            return false;
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

        return gestorCRUD.atualizarGestor(gestorAtualizado);
    }

    // DELETE - Eliminar gestor
    public boolean eliminarGestor(int nif) {
        if (nif <= 0) {
            System.out.println("Erro: NIF inválido.");
            return false;
        }

        if (gestorCRUD.procurarPorNif(nif) == null) {
            System.out.println("Erro: Gestor não encontrado.");
            return false;
        }

        return gestorCRUD.eliminarGestor(nif);
    }
}
