package controller;

import DAL.CursoCRUD;
import DAL.EstudanteCRUD;
import DAL.PropinaCRUD;
import model.Curso;
import model.Estudante;
import model.Propina;
import model.Resultado;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

public class EstudanteController {
    private final EstudanteCRUD estudanteCRUD;
    private final CursoCRUD cursoCRUD;

    public EstudanteController() {
        this.estudanteCRUD = new EstudanteCRUD();
        this.cursoCRUD = new CursoCRUD();
    }


    public String obterFichaEstudanteFormatada(Estudante estudante) {
        if (estudante == null) return "Erro: Estudante nao encontrado.";

        String dataNascimentoStr = (estudante.getDataNascimento() != null) ? estudante.getDataNascimento().toString() : "Nao definida";
        String cursoStr = (estudante.getNomeCurso() != null && !estudante.getNomeCurso().trim().isEmpty()) ? estudante.getNomeCurso() : "Sem curso atribuído";

        boolean isConcluido = verificarSeCursoConcluido(estudante);
        String statusCurso = isConcluido ? "CONCLUÍDO" : "EM CURSO";

        int anoLetivoAtual = obterAnoDesbloqueado(estudante);

        PropinaController propinaController = new PropinaController();
        List<model.Propina> propinas = propinaController.consultarPropinasEstudante(estudante.getNumeroMec());
        double dividaTotal = 0;
        if (propinas != null) {
            for (model.Propina propina : propinas) {
                dividaTotal += propina.getValorEmDivida();
            }
        }

        return """
        --- FICHA DE ESTUDANTE ---
        Nome: %s
        N. Mecanográfico: %s
        Email: %s
        NIF: %d
        Data Nascimento: %s
        Morada: %s
        Curso (Inscrição): %s
        Estado do Curso: %s
        Ano Letivo Atual: %do Ano
        Dívida Total Acumulada: %.2f EUR
        """.formatted(estudante.getNome(), estudante.getNumeroMec(), estudante.getEmail(),
                estudante.getNif(), dataNascimentoStr, estudante.getMorada(), cursoStr, statusCurso, anoLetivoAtual, dividaTotal);
    }

    public int obterAnoDesbloqueado(Estudante estudante) {
        Curso curso = cursoCRUD.procurarPorNome(estudante.getNomeCurso());
        if (curso == null) return 1;
        DAL.AvaliacaoCRUD avaliacaoCRUD = new DAL.AvaliacaoCRUD();
        estudante.setListaAvaliacoes(avaliacaoCRUD.listarPorEstudante(estudante.getNumeroMec()));
        int anoPorNotas = BLL.EstudanteCalculo.calcularAnoDesbloqueado(estudante, curso);
        PropinaController pc = new PropinaController();
        if (anoPorNotas >= 2 && !pc.isPropinaPaga(estudante.getNumeroMec(), 1)) return 1;
        if (anoPorNotas == 3 && !pc.isPropinaPaga(estudante.getNumeroMec(), 2)) return 2;
        return anoPorNotas;
    }
    public void garantirPropinaGerada(Estudante estudante) {
        int ano = obterAnoDesbloqueado(estudante);
        new PropinaController().gerarPropinaAnual(estudante.getNumeroMec(), ano);
    }
    public boolean verificarSeCursoConcluido(Estudante estudante) {
        Curso curso = cursoCRUD.procurarPorNome(estudante.getNomeCurso());
        if (curso == null) return false;

        DAL.AvaliacaoCRUD avaliacaoCRUD = new DAL.AvaliacaoCRUD();
        estudante.setListaAvaliacoes(avaliacaoCRUD.listarPorEstudante(estudante.getNumeroMec()));

        if (BLL.EstudanteCalculo.isCursoConcluido(estudante, curso)) {
            PropinaController propinaController = new PropinaController();
            for (int i = 1; i <= curso.getDuracao(); i++) {
                if (!propinaController.isPropinaPaga(estudante.getNumeroMec(), i)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public int gerarNumeroMecanografico() {
        return estudanteCRUD.gerarNumeroMecanografico();
    }

    public Resultado<Integer> registarEstudante(String nome, String morada, int nif, LocalDate dataNascimento, String curso, String hash) {
        if (nome == null || nome.trim().isEmpty() || morada == null || morada.trim().isEmpty() ||
                curso == null || curso.trim().isEmpty() || hash == null || hash.trim().isEmpty()) {
            return new Resultado<>(false, "Todos os campos de texto são obrigatórios.");
        }
        if (String.valueOf(nif).length() != 9) {
            return new Resultado<>(false, "O NIF fornecido é inválido. Deve conter exatamente 9 dígitos.");
        }

        if (procurarEstudantePorNif(nif) != null) {
            return new Resultado<>(false, "Operação Recusada: O NIF '" + nif + "' já se encontra associado a outro estudante.");
        }

        if (dataNascimento == null) return new Resultado<>(false, "A data de nascimento fornecida é inválida.");

        int numeroMec = estudanteCRUD.gerarNumeroMecanografico();
        String email = numeroMec + "@issmf.ipp.pt";

        Estudante estudante = new Estudante(nome, morada, nif, dataNascimento, email, numeroMec, hash, curso, true);

        Resultado<Estudante> res = estudanteCRUD.registarEstudante(estudante);
        if (!res.sucesso) return new Resultado<>(false, res.mensagemErro);

        double precoAnual = 1000.0;
        Curso cursoObj = cursoCRUD.procurarPorNome(curso);
        if (cursoObj != null && cursoObj.getPrecoAnual() > 0) {
            precoAnual = cursoObj.getPrecoAnual();
        }
        PropinaCRUD propinaCRUD = new PropinaCRUD();
        propinaCRUD.registarPropina(new Propina(numeroMec, 1, precoAnual, 0.0));

        return new Resultado<>(numeroMec, true);
    }

    public Resultado<Estudante> atualizarEstudante(int numeroMec, String nome, String morada, String curso) {
        if (numeroMec <= 0) return new Resultado<>(false, "Número Mecanográfico inválido.");

        Estudante estudante = estudanteCRUD.lerEstudante(numeroMec);
        if (estudante == null) return new Resultado<>(false, "Estudante não encontrado.");

        if (nome != null && !nome.trim().isEmpty()) estudante.setNome(nome);
        if (morada != null && !morada.trim().isEmpty()) estudante.setMorada(morada);
        if (curso != null && !curso.trim().isEmpty()) estudante.setNomeCurso(curso);

        return estudanteCRUD.atualizarEstudante(estudante);
    }

    public Resultado<Estudante> alterarPassword(int numeroMec, String novaSenhaHash) {
        if (novaSenhaHash == null || novaSenhaHash.trim().isEmpty()) return new Resultado<>(false, "A nova senha não pode estar vazia.");

        Estudante estudante = estudanteCRUD.lerEstudante(numeroMec);
        if (estudante == null) return new Resultado<>(false, "Estudante não encontrado.");

        estudante.setHash(novaSenhaHash);
        return estudanteCRUD.atualizarSenha(estudante);
    }

    public Resultado<String> eliminarEstudante(int numeroMec) {
        Estudante estudante = estudanteCRUD.lerEstudante(numeroMec);
        if (estudante == null) return new Resultado<>(false, "Estudante não encontrado.");

        if (estudante.getNomeCurso() != null && !estudante.getNomeCurso().equals("SEM REGISTO")) {
            Curso cursoInfo = cursoCRUD.procurarPorNome(estudante.getNomeCurso());

            if (cursoInfo != null && cursoInfo.getAnosIniciados() != null && !cursoInfo.getAnosIniciados().isEmpty()) {
                return new Resultado<>(false, "Bloqueado: Não é possível eliminar um estudante cujo curso já iniciou atividade letiva.");
            }

            if (!estudante.isAtivo()) return new Resultado<>(false, "O Estudante já se encontra inativo.");

            estudante.setAtivo(false);
            Resultado<Estudante> res = estudanteCRUD.atualizarEstudante(estudante);
            return res.sucesso ? new Resultado<>("INATIVADO", true) : new Resultado<>(false, res.mensagemErro);
        }

        Resultado<Estudante> res = estudanteCRUD.eliminarEstudante(numeroMec);
        return res.sucesso ? new Resultado<>("ELIMINADO", true) : new Resultado<>(false, res.mensagemErro);
    }

    public List<Estudante> listarEstudantes() { return estudanteCRUD.getEstudantes(); }
    public Estudante procurarEstudantePorNif(int nif) { return nif <= 0 ? null : estudanteCRUD.procurarPorNif(nif); }
    public Estudante procurarEstudantePorNumeroMec(int mec) { return mec <= 0 ? null : estudanteCRUD.lerEstudante(mec); }

    public Resultado<List<String>> simularTransicaoAnoLetivoGlobal() {
        List<String> relatorio = new java.util.ArrayList<>();
        List<Estudante> estudantes = estudanteCRUD.getEstudantes();

        if (estudantes.isEmpty()) {
            return new Resultado<>(false, "Não há estudantes registados no sistema para simular a transição.");
        }

        PropinaController propinaController = new PropinaController();
        DAL.AvaliacaoCRUD avaliacaoCRUD = new DAL.AvaliacaoCRUD();

        for (Estudante estudante : estudantes) {
            if (!estudante.isAtivo()) continue;

            Curso curso = cursoCRUD.procurarPorNome(estudante.getNomeCurso());
            if (curso == null) continue;

            estudante.setListaAvaliacoes(avaliacaoCRUD.listarPorEstudante(estudante.getNumeroMec()));

            int anoPorNotas = BLL.EstudanteCalculo.calcularAnoDesbloqueado(estudante, curso);
            int anoReal = anoPorNotas;
            String motivo = "";

            if (anoPorNotas >= 2 && !propinaController.isPropinaPaga(estudante.getNumeroMec(), 1)) {
                anoReal = 1;
            } else if (anoPorNotas == 3 && !propinaController.isPropinaPaga(estudante.getNumeroMec(), 2)) {
                anoReal = 2;
            }

            if (anoPorNotas > anoReal) {
                motivo = " - RETIDO: Falta de pagamento da propina do " + anoReal + "º ano.";
            } else {
                if (anoReal < curso.getDuracao()) {
                    motivo = " - AGUARDA ACADÉMICO: Ainda não completou 60% das UCs para passar ao " + (anoReal + 1) + "º ano.";
                } else {
                    motivo = " - NO ÚLTIMO ANO DO CURSO.";
                }
            }

            List<model.Propina> historicoPropinas = propinaController.consultarPropinasEstudante(estudante.getNumeroMec());
            boolean jaFaturadoEsteAno = false;

            if (historicoPropinas != null) {
                for (model.Propina propina : historicoPropinas) {
                    if (propina.getAnoLetivo() == anoReal){
                        jaFaturadoEsteAno = true;
                        break;
                    }
                }
            }
            if (!jaFaturadoEsteAno) {
                propinaController.gerarPropinaAnual(estudante.getNumeroMec(),anoReal);
            }
            boolean isConcluido = verificarSeCursoConcluido(estudante);

            if (isConcluido) {
                relatorio.add("[SUCESSO] Mec: " + estudante.getNumeroMec() + " (" + estudante.getNome() + ") -> Concluiu o Curso! (Sem novas propinas)");
            } else {
                relatorio.add("[INFO] Mec: " + estudante.getNumeroMec() + " (" + estudante.getNome() + ") -> Processado para o " + anoReal + "º Ano" + motivo);
            }
        }
        return new Resultado<>(relatorio, true);
    }
}