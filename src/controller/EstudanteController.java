package controller;

import BLL.EstudanteCalculo;
import DAL.EstudanteCRUD;
import model.Avaliacao;
import model.Estudante;
import model.Resultado;

import java.time.LocalDate;
import java.util.List;

public class EstudanteController {

    private EstudanteCRUD estudanteCRUD;
    private EstudanteCalculo bll;

    public EstudanteController() {
        this.estudanteCRUD = new EstudanteCRUD();
        this.bll = new EstudanteCalculo();
    }

    public String obterFichaEstudanteFormatada(Estudante estudante) {
        if (estudante == null) return "Erro: Estudante não encontrado.";

        String dataNascimentoStr = (estudante.getDataNascimento() != null) ? estudante.getDataNascimento().toString() : "Não definida";
        String cursoStr = (estudante.getNomeCurso() != null && !estudante.getNomeCurso().trim().isEmpty()) ? estudante.getNomeCurso() : "Sem curso atribuído";

        return """
        --- FICHA DE ESTUDANTE ---
        Nome: %s
        Nº Mecanográfico: %s
        Email: %s
        NIF: %d
        Data Nascimento: %s
        Morada: %s
        Curso (Inscrição): %s
        Ano Letivo Atual: %dº Ano
        """.formatted(
                estudante.getNome(),
                estudante.getNumeroMec(),
                estudante.getEmail(),
                estudante.getNif(),
                dataNascimentoStr,
                estudante.getMorada(),
                cursoStr,
                estudante.getAnoLetivo()
        );
    }

    public Resultado tentarPassarDeAno(Estudante estudante, int totalUCsInscritas) {
        Resultado res = new Resultado();

        if (estudante == null) {
            res.success = false;
            res.errorMessage = "Estudante inválido para a operação.";
            return res;
        }

        boolean passou = bll.verificarProgressao(estudante, totalUCsInscritas);

        if (passou) {
            res.success = true;
            res.object = "Sucesso: O estudante " + estudante.getNome() + " transitou para o " + estudante.getAnoLetivo() + "º ano letivo.";
        } else {
            res.success = false;
            res.errorMessage = "Falhou: O estudante não cumpriu os 60% de aproveitamento e manter-se-á no " + estudante.getAnoLetivo() + "º ano.";
        }

        return res;
    }

    public int gerarNumeroMecanografico() {
        return estudanteCRUD.gerarNumeroMecanografico();
    }

    public Resultado registarEstudante(String nome, String morada, int nif, LocalDate dataNascimento, String curso, String hash) {
        Resultado res = new Resultado();

        if (nome == null || nome.trim().isEmpty() || morada == null || morada.trim().isEmpty() ||
                curso == null || curso.trim().isEmpty() || hash == null || hash.trim().isEmpty()) {
            res.success = false;
            res.errorMessage = "Todos os campos de texto (nome, morada, curso, senha) são obrigatórios.";
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

        int numeroMec = estudanteCRUD.gerarNumeroMecanografico();
        String email = numeroMec + "@issmf.ipp.pt";

        Estudante estudante = new Estudante(nome, morada, nif, dataNascimento, email, numeroMec, hash, curso,true);

        if (estudanteCRUD.registarEstudante(estudante)) {
            res.success = true;
            res.object = numeroMec;
        } else {
            res.success = false;
            res.errorMessage = "Ocorreu um erro na base de dados (verifique se o NIF já existe).";
        }

        return res;
    }

    public List<Estudante> listarEstudantes() {
        return estudanteCRUD.getEstudantes();
    }

    public Estudante procurarEstudantePorNumeroMec(int numeroMec) {
        if (numeroMec <= 0) return null;
        return estudanteCRUD.lerEstudante(numeroMec);
    }

    public Resultado atualizarEstudante(int numeroMec, String nome, String morada, String email, String curso) {
        Resultado res = new Resultado();

        if (numeroMec <= 0) {
            res.success = false;
            res.errorMessage = "Número Mecanográfico inválido.";
            return res;
        }

        Estudante estudante = estudanteCRUD.lerEstudante(numeroMec);
        if (estudante == null) {
            res.success = false;
            res.errorMessage = "Estudante não encontrado com o Número Mecanográfico informado.";
            return res;
        }

        if (nome != null && !nome.trim().isEmpty()) estudante.setNome(nome);
        if (morada != null && !morada.trim().isEmpty()) estudante.setMorada(morada);
        if (email != null && !email.trim().isEmpty()) estudante.setEmail(email);
        if (curso != null && !curso.trim().isEmpty()) estudante.setNomeCurso(curso);

        if (estudanteCRUD.atualizarEstudante(estudante)) {
            res.success = true;
        } else {
            res.success = false;
            res.errorMessage = "Erro ao guardar as alterações na base de dados.";
        }

        return res;
    }

    public Resultado alterarPassword(int numeroMec, String novaSenhaHash) {
        Resultado res = new Resultado();

        if (numeroMec <= 0) {
            res.success = false;
            res.errorMessage = "Número Mecanográfico inválido.";
            return res;
        }

        if (novaSenhaHash == null || novaSenhaHash.trim().isEmpty()) {
            res.success = false;
            res.errorMessage = "A nova senha não pode estar vazia.";
            return res;
        }

        Estudante estudante = estudanteCRUD.lerEstudante(numeroMec);
        if (estudante == null) {
            res.success = false;
            res.errorMessage = "Estudante não encontrado.";
            return res;
        }

        estudante.setHash(novaSenhaHash);

        if (estudanteCRUD.atualizarSenha(estudante).success) {
            res.success = true;
        } else {
            res.success = false;
            res.errorMessage = "Erro ao atualizar a password na base de dados.";
        }

        return res;
    }

    public Resultado eliminarEstudante(int numeroMec) {
        Resultado res = new Resultado();

        if (numeroMec <= 0) {
            res.success = false;
            res.errorMessage = "Número Mecanográfico inválido.";
            return res;
        }

        Estudante estudante = estudanteCRUD.lerEstudante(numeroMec);

        if (estudanteCRUD.lerEstudante(numeroMec) == null) {
            res.success = false;
            res.errorMessage = "Estudante não encontrado com o Número Mecanográfico informado.";
            return res;
        }

        if (estudante.getNomeCurso() != null && !estudante.getNomeCurso().equals("SEM REGISTO")) {
            if (!estudante.isAtivo()) {
                res.success = false;
                res.errorMessage = "O Estudante já se encontra inativo.";
                return res;
            }
            estudante.setAtivo(false);

            if (estudanteCRUD.atualizarEstudante(estudante)) {
                res.success = true;
                res.object = "INATIVADO";
            } else {
                res.success = false;
                res.errorMessage = "Erro ao inativar o estudante na base de dados.";
            }
        } else {
            if (estudanteCRUD.eliminarEstudante(numeroMec)) {
                res.success = true;
                res.object = "ELIMINADO";
            } else {
                res.success = false;
                res.errorMessage = "Erro na base de dados ao tentar eliminar o estudante.";
            }
        }
        return res;
    }
}