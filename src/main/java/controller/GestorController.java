package controller;

import DAL.CursoCRUD;
import DAL.EstudanteCRUD;
import DAL.GestorCRUD;
import model.Curso;
import model.Gestor;
import model.Resultado;

import java.time.LocalDate;
import java.util.List;

public class GestorController {
    private final GestorCRUD gestorCRUD;

    public GestorController() {
        this.gestorCRUD = new GestorCRUD();
    }

    public Resultado<Curso> arrancarAnoLetivo(String nomeCurso, int anoAlvo) {
        DAL.CursoCRUD cursoCRUD = new DAL.CursoCRUD();
        Curso curso = cursoCRUD.procurarPorNome(nomeCurso);

        if (curso == null) return new Resultado<>(false, "Curso não encontrado.");

        if (curso.isAnoIniciado(anoAlvo)) {
            return new Resultado<>(false, "O " + anoAlvo + "º ano deste curso já foi iniciado anteriormente.");
        }

        DAL.EstudanteCRUD estudanteCRUD = new DAL.EstudanteCRUD();
        long totalInscritos = estudanteCRUD.getEstudantes().stream()
                .filter(e -> e.getNomeCurso() != null && e.getNomeCurso().equalsIgnoreCase(nomeCurso) && e.isAtivo())
                .count();

        if (anoAlvo == 1 && totalInscritos < 5) {
            return new Resultado<>(false, "Bloqueado: Para iniciar o 1º ano, são necessários pelo menos 5 alunos. (Atuais: " + totalInscritos + ")");
        } else if (anoAlvo > 1 && totalInscritos < 1) {
            return new Resultado<>(false, "Bloqueado: Para iniciar o " + anoAlvo + "º ano, é necessário pelo menos 1 aluno. (Atuais: 0)");
        }

        curso.adicionarAnoIniciado(anoAlvo);

        return cursoCRUD.registarArranqueAno(nomeCurso, curso);
    }


    public Resultado<Gestor> registarGestor(String nome, String morada, int nif, LocalDate dataNascimento, String email, String hash, String cargo) {
        if (nome == null || nome.trim().isEmpty() || morada == null || morada.trim().isEmpty() ||
                email == null || email.trim().isEmpty() || hash == null || hash.trim().isEmpty() || cargo == null || cargo.trim().isEmpty()) {
            return new Resultado<>(false, "Todos os campos de texto são obrigatórios.");
        }
        if (nif <= 0) return new Resultado<>(false, "O NIF fornecido é inválido.");
        if (dataNascimento == null) return new Resultado<>(false, "A data de nascimento é inválida.");

        if (gestorCRUD.procurarPorNif(nif) != null) return new Resultado<>(false, "Já existe um gestor com este NIF.");
        if (gestorCRUD.procurarPorEmail(email) != null) return new Resultado<>(false, "Já existe um gestor com este email.");

        Gestor gestor = new Gestor(nome, morada, nif, dataNascimento, email, hash, cargo);

        return gestorCRUD.registarGestor(gestor) ? new Resultado<>(gestor, true)
                : new Resultado<>(false, "Ocorreu um erro na base de dados ao registar.");
    }

    public Resultado<Gestor> atualizarGestor(int nif, String novoNome,
                                             String novaMorada, LocalDate novaData, String novoCargo) {
        Gestor existente = gestorCRUD.procurarPorNif(nif);
        if (existente == null) return new Resultado<>(false, "Gestor não encontrado.");

        if (novoNome != null && !novoNome.trim().isEmpty()) existente.setNome(novoNome);
        if (novaMorada != null && !novaMorada.trim().isEmpty()) existente.setMorada(novaMorada);
        if (novaData != null) existente.setDataNascimento(novaData);
        if (novoCargo != null && !novoCargo.trim().isEmpty()) existente.setCargo(novoCargo);

        return gestorCRUD.atualizarGestor(existente) ?
                new Resultado<>(existente, true) :
                new Resultado<>(false, "Erro ao guardar alterações.");
    }

    public Resultado<Gestor> alterarPassword(int nif, String novoHash) {
        if (novoHash == null || novoHash.trim().isEmpty()) return new Resultado<>(false, "A nova senha não pode estar vazia.");

        Gestor existente = gestorCRUD.procurarPorNif(nif);
        if (existente == null) return new Resultado<>(false, "Gestor não encontrado.");

        Gestor atualizado = new Gestor(existente.getNome(), existente.getMorada(), existente.getNif(), existente.getDataNascimento(), existente.getEmail(), novoHash, existente.getCargo());

        return gestorCRUD.atualizarGestor(atualizado) ? new Resultado<>(atualizado, true)
                : new Resultado<>(false, "Erro ao atualizar a password.");
    }

    public Resultado<Gestor> eliminarGestor(int nif) {
        if (gestorCRUD.procurarPorNif(nif) == null) return new Resultado<>(false, "Gestor não encontrado.");

        return gestorCRUD.eliminarGestor(nif) ? new Resultado<>(null, true)
                : new Resultado<>(false, "Erro ao tentar eliminar o gestor.");
    }

    public List<Gestor> listarGestores() { return gestorCRUD.getGestores(); }
    public Gestor procurarGestorPorNif(int nif) {
        return gestorCRUD.procurarPorNif(nif);
    }
}