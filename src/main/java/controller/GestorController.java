package controller;

import DAL.DAOFactory;
import DAL.ICursoDAO;
import DAL.IEstudanteDAO;
import DAL.IGestorDAO;
import model.Curso;
import model.Gestor;
import model.Resultado;

import java.time.LocalDate;
import java.util.List;

public class GestorController {
    private final IGestorDAO gestorDAO;

    public GestorController() {
        this.gestorDAO = DAOFactory.getGestorDAO();
    }

    public Resultado<Curso> arrancarAnoLetivo(String nomeCurso, int anoAlvo) {
        ICursoDAO cursoDAO = DAOFactory.getCursoDAO();
        Curso curso = cursoDAO.procurarPorNome(nomeCurso);
        if (curso == null) return new Resultado<>(false, "Curso não encontrado.");

        if (curso.isAnoIniciado(anoAlvo)) {
            return new Resultado<>(false, "O " + anoAlvo + "º ano deste curso já foi iniciado anteriormente.");
        }

        IEstudanteDAO estudanteDAO = DAOFactory.getEstudanteDAO();
        // Conta apenas os alunos ativos do curso QUE ESTÃO NO ANO ALVO
        long totalInscritos = estudanteDAO.getEstudantes().stream()
                .filter(e -> e.getNomeCurso() != null
                        && e.getNomeCurso().equalsIgnoreCase(nomeCurso)
                        && e.isAtivo()
                        && e.getAnoLetivo() == anoAlvo)
                .count();

        if (anoAlvo == 1 && totalInscritos < 5) {
            return new Resultado<>(false, "Bloqueado: Para iniciar o 1º ano, são necessários pelo menos 5 alunos inscritos nesse ano. (Atuais: " + totalInscritos + ")");
        } else if (anoAlvo > 1 && totalInscritos < 1) {
            return new Resultado<>(false, "Bloqueado: Para iniciar o " + anoAlvo + "º ano, é necessário pelo menos 1 aluno nesse ano. (Atuais: 0)");
        }

        curso.adicionarAnoIniciado(anoAlvo);
        return cursoDAO.registarArranqueAno(nomeCurso, curso);
    }

    public Resultado<Gestor> registarGestor(String nome, String morada, int nif, LocalDate dataNascimento, String email, String hash, String cargo) {
        if (nome == null || nome.trim().isEmpty() || morada == null || morada.trim().isEmpty() ||
                email == null || email.trim().isEmpty() || hash == null || hash.trim().isEmpty() || cargo == null || cargo.trim().isEmpty()) {
            return new Resultado<>(false, "Todos os campos de texto são obrigatórios.");
        }
        if (nif <= 0) return new Resultado<>(false, "O NIF fornecido é inválido.");
        if (dataNascimento == null) return new Resultado<>(false, "A data de nascimento é inválida.");

        // Normaliza email para minúsculas antes de guardar
        String emailNorm = email.trim().toLowerCase();

        if (gestorDAO.procurarPorNif(nif) != null) return new Resultado<>(false, "Já existe um gestor com este NIF.");
        if (gestorDAO.procurarPorEmail(emailNorm) != null) return new Resultado<>(false, "Já existe um gestor com este email.");

        Gestor gestor = new Gestor(nome, morada, nif, dataNascimento, emailNorm, hash, cargo);
        return gestorDAO.registarGestor(gestor)
                ? new Resultado<>(gestor, true)
                : new Resultado<>(false, "Ocorreu um erro na base de dados ao registar.");
    }

    public Resultado<Gestor> atualizarGestor(int nif, String novaMorada, LocalDate novaDataNascimento, String novoCargo) {
        if (nif <= 0) return new Resultado<>(false, "NIF inválido.");
        Gestor existente = gestorDAO.procurarPorNif(nif);
        if (existente == null) return new Resultado<>(false, "Gestor não encontrado.");

        String moradaFinal = (novaMorada != null && !novaMorada.trim().isEmpty()) ? novaMorada : existente.getMorada();
        LocalDate dataFinal = (novaDataNascimento != null) ? novaDataNascimento : existente.getDataNascimento();
        String cargoFinal = (novoCargo != null && !novoCargo.trim().isEmpty()) ? novoCargo : existente.getCargo();

        Gestor atualizado = new Gestor(existente.getNome(), moradaFinal, existente.getNif(), dataFinal, existente.getEmail(), existente.getHash(), cargoFinal);
        atualizado.setAtivo(existente.isAtivo());
        return gestorDAO.atualizarGestor(atualizado)
                ? new Resultado<>(atualizado, true)
                : new Resultado<>(false, "Erro ao guardar alterações.");
    }

    public Resultado<Gestor> alterarPassword(int nif, String novoHash) {
        if (novoHash == null || novoHash.trim().isEmpty()) return new Resultado<>(false, "A nova senha não pode estar vazia.");
        Gestor existente = gestorDAO.procurarPorNif(nif);
        if (existente == null) return new Resultado<>(false, "Gestor não encontrado.");

        Gestor atualizado = new Gestor(existente.getNome(), existente.getMorada(), existente.getNif(),
                existente.getDataNascimento(), existente.getEmail(), novoHash, existente.getCargo());
        atualizado.setAtivo(existente.isAtivo());
        return gestorDAO.atualizarGestor(atualizado)
                ? new Resultado<>(atualizado, true)
                : new Resultado<>(false, "Erro ao atualizar a password.");
    }

    public Resultado<Gestor> eliminarGestor(int nif) {
        if (gestorDAO.procurarPorNif(nif) == null) return new Resultado<>(false, "Gestor não encontrado.");
        return gestorDAO.eliminarGestor(nif)
                ? new Resultado<>(null, true)
                : new Resultado<>(false, "Erro ao tentar eliminar o gestor.");
    }

    public Resultado<Gestor> ativarDesativarGestor(int nif, boolean ativar) {
        Gestor existente = gestorDAO.procurarPorNif(nif);
        if (existente == null) return new Resultado<>(false, "Gestor não encontrado.");
        if (existente.isAtivo() == ativar) {
            return new Resultado<>(false, "O gestor já se encontra " + (ativar ? "ativo" : "inativo") + ".");
        }
        existente.setAtivo(ativar);
        return gestorDAO.atualizarGestor(existente)
                ? new Resultado<>(existente, true)
                : new Resultado<>(false, "Erro ao atualizar o estado do gestor.");
    }

    public List<Gestor> listarGestores() { return gestorDAO.getGestores(); }
    public Gestor procurarGestorPorNif(int nif) { return gestorDAO.procurarPorNif(nif); }
}
