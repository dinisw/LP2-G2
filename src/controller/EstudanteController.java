package controller;

import BLL.EstudanteCalculo;
import DAL.EstudanteCRUD;
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
        Resultado resultado = new Resultado();

        if (estudante == null) {
            resultado.success = false;
            resultado.errorMessage = "Estudante inválido para a operação.";
            return resultado;
        }

        boolean passou = bll.verificarProgressao(estudante, totalUCsInscritas);

        if (passou) {
            resultado.success = true;
            resultado.object = "Sucesso: O estudante " + estudante.getNome() + " transitou para o " + estudante.getAnoLetivo() + "º ano letivo.";
        } else {
            resultado.success = false;
            resultado.errorMessage = "Inscrição no ano seguinte não permitida. Aproveitamento insuficiente (mínimo exigido: >60%).";
        }

        return resultado;
    }

    public boolean temAproveitamentoSuficiente(Estudante estudante, int totalUCsInscritas) {
        if (estudante == null) return false;
        return bll.calculoPercentagem(estudante, totalUCsInscritas) > 0.60;
    }

    public int gerarNumeroMecanografico() {
        return estudanteCRUD.gerarNumeroMecanografico();
    }

    public Resultado registarEstudante(String nome, String morada, int nif, LocalDate dataNascimento, String curso, String hash) {
        Resultado resultado = new Resultado();

        if (nome == null || nome.trim().isEmpty() || morada == null || morada.trim().isEmpty() ||
                curso == null || curso.trim().isEmpty() || hash == null || hash.trim().isEmpty()) {
            resultado.success = false;
            resultado.errorMessage = "Todos os campos de texto (nome, morada, curso, senha) são obrigatórios.";
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

        int numeroMec = estudanteCRUD.gerarNumeroMecanografico();
        String email = numeroMec + "@issmf.ipp.pt";

        Estudante estudante = new Estudante(nome, morada, nif, dataNascimento, email, numeroMec, hash, curso,true);

        if (estudanteCRUD.registarEstudante(estudante)) {
            resultado.success = true;
            resultado.object = numeroMec;
        } else {
            resultado.success = false;
            resultado.errorMessage = "Ocorreu um erro na base de dados (verifique se o NIF já existe).";
        }

        return resultado;
    }

    public List<Estudante> listarEstudantes() {
        return estudanteCRUD.getEstudantes();
    }

    public Estudante procurarEstudantePorNif(int nif) {
        if (nif <= 0) {
            return null;
        }
        return estudanteCRUD.procurarPorNif(nif);
    }

    public Estudante procurarEstudantePorNumeroMec(int numeroMec) {
        if (numeroMec <= 0) return null;
        return estudanteCRUD.lerEstudante(numeroMec);
    }

    public Resultado atualizarEstudante(int numeroMec, String nome, String morada, String email, String curso) {
        Resultado resultado = new Resultado();

        if (numeroMec <= 0) {
            resultado.success = false;
            resultado.errorMessage = "Número Mecanográfico inválido.";
            return resultado;
        }

        Estudante estudante = estudanteCRUD.lerEstudante(numeroMec);
        if (estudante == null) {
            resultado.success = false;
            resultado.errorMessage = "Estudante não encontrado com o Número Mecanográfico informado.";
            return resultado;
        }

        if (nome != null && !nome.trim().isEmpty()) estudante.setNome(nome);
        if (morada != null && !morada.trim().isEmpty()) estudante.setMorada(morada);
        if (email != null && !email.trim().isEmpty()) estudante.setEmail(email);
        if (curso != null && !curso.trim().isEmpty()) estudante.setNomeCurso(curso);

        if (estudanteCRUD.atualizarEstudante(estudante)) {
            resultado.success = true;
        } else {
            resultado.success = false;
            resultado.errorMessage = "Erro ao guardar as alterações na base de dados.";
        }

        return resultado;
    }

    public Resultado alterarPassword(int numeroMec, String novaSenhaHash) {
        Resultado resultado = new Resultado();

        if (numeroMec <= 0) {
            resultado.success = false;
            resultado.errorMessage = "Número Mecanográfico inválido.";
            return resultado;
        }

        if (novaSenhaHash == null || novaSenhaHash.trim().isEmpty()) {
            resultado.success = false;
            resultado.errorMessage = "A nova senha não pode estar vazia.";
            return resultado;
        }

        Estudante estudante = estudanteCRUD.lerEstudante(numeroMec);
        if (estudante == null) {
            resultado.success = false;
            resultado.errorMessage = "Estudante não encontrado.";
            return resultado;
        }

        estudante.setHash(novaSenhaHash);

        if (estudanteCRUD.atualizarSenha(estudante).success) {
            resultado.success = true;
        } else {
            resultado.success = false;
            resultado.errorMessage = "Erro ao atualizar a password na base de dados.";
        }

        return resultado;
    }

    public Resultado eliminarEstudante(int numeroMec) {
        Resultado resultado = new Resultado();

        if (numeroMec <= 0) {
            resultado.success = false;
            resultado.errorMessage = "Número Mecanográfico inválido.";
            return resultado;
        }

        Estudante estudante = estudanteCRUD.lerEstudante(numeroMec);

        if (estudanteCRUD.lerEstudante(numeroMec) == null) {
            resultado.success = false;
            resultado.errorMessage = "Estudante não encontrado com o Número Mecanográfico informado.";
            return resultado;
        }

        if (estudante.getNomeCurso() != null && !estudante.getNomeCurso().equals("SEM REGISTO")) {
            if (!estudante.isAtivo()) {
                resultado.success = false;
                resultado.errorMessage = "O Estudante já se encontra inativo.";
                return resultado;
            }
            estudante.setAtivo(false);

            if (estudanteCRUD.atualizarEstudante(estudante)) {
                resultado.success = true;
                resultado.object = "INATIVADO";
            } else {
                resultado.success = false;
                resultado.errorMessage = "Erro ao inativar o estudante na base de dados.";
            }
        } else {
            if (estudanteCRUD.eliminarEstudante(numeroMec)) {
                resultado.success = true;
                resultado.object = "ELIMINADO";
            } else {
                resultado.success = false;
                resultado.errorMessage = "Erro na base de dados ao tentar eliminar o estudante.";
            }
        }
        return resultado;
    }
}