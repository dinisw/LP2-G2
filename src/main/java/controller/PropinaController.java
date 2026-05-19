package controller;

import DAL.CursoCRUD;
import DAL.EstudanteCRUD;
import DAL.PropinaCRUD;
import model.Curso;
import model.Estudante;
import model.Propina;
import model.Resultado;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PropinaController {
    private final PropinaCRUD propinaCRUD;
    private final double VALOR_PROPINA_PADRAO = 1000.0;

    public PropinaController() {
        this.propinaCRUD = new PropinaCRUD();
    }

    public Resultado<Propina> gerarPropinaAnual(int numeroMec, int anoLetivo) {
        if (propinaCRUD.procurarPropina(numeroMec, anoLetivo) != null) {
            return new Resultado<>(false, "A propina para o " + anoLetivo + "º ano já foi gerada.");
        }

        EstudanteCRUD estudanteCRUD = new EstudanteCRUD();
        Estudante estudante = estudanteCRUD.lerEstudante(numeroMec);

        if (estudante == null || estudante.getNomeCurso() == null) {
            return new Resultado<>(false, "Estudante ou Curso inválido.");
        }

            CursoCRUD cursoCRUD = new CursoCRUD();
            Curso curso = cursoCRUD.procurarPorNome(estudante.getNomeCurso());
            if (curso == null) {
                return new Resultado<>(false, "Erro: O curso do estudante não existe. Propina não gerada.");
            }

            double precoAConfigurar = curso.getPrecoAnual();
            if (precoAConfigurar <= 0) {
                precoAConfigurar = VALOR_PROPINA_PADRAO;
            }
            Propina novaPropina = new Propina(numeroMec, anoLetivo, precoAConfigurar, 0.0);

            return propinaCRUD.registarPropina(novaPropina) ? new Resultado<>(novaPropina, true)
                : new Resultado<>(false, "Erro ao gerar a propina na base de dados.");
    }

    public Resultado<Propina> pagarPropina(int numeroMec, int anoLetivo, double valorPagamento) {

        valorPagamento = Math.round(valorPagamento * 100) / 100;

        if (valorPagamento <= 0) return new Resultado<>(false, "O valor do pagamento deve ser superior a zero.");

        Propina propina = propinaCRUD.procurarPropina(numeroMec, anoLetivo);
        if (propina == null) return new Resultado<>(false, "Propina não encontrada para o " + anoLetivo + "º ano.");

        if (valorPagamento > propina.getValorEmDivida()) {
            return new Resultado<>(false, "Operação Recusada: O valor inserido (" + valorPagamento + "€) é superior à dívida atual (" + propina.getValorEmDivida() + "€).");        }

        if (propina.isTotalmentePaga()) return new Resultado<>(false, "Esta propina já se encontra totalmente paga. Obrigado!");

        if (valorPagamento > propina.getValorEmDivida()) {
            return new Resultado<>(false, "Operação Recusada: O valor inserido (" + valorPagamento + "€) é superior à dívida atual(" + propina.getValorEmDivida() + "€).");
        }

        propina.registarPagamento(valorPagamento);

        return propinaCRUD.atualizarPropina(propina) ? new Resultado<>(propina, true)
                : new Resultado<>(false, "Erro ao guardar o pagamento no sistema.");
    }

    public List<Propina> consultarPropinasEstudante(int numeroMec) {
        return propinaCRUD.listarPropinasPorEstudante(numeroMec);
    }

    public List<Estudante> obterAlunosEmDivida() {
        List<Propina> todas = propinaCRUD.getTodasPropinas();
        EstudanteCRUD estudanteCRUD = new EstudanteCRUD();
        List<Estudante> devedores = new ArrayList<>();
        Set<Integer> mecAdicionado = new HashSet<>();

        for (Propina propina : todas) {
            if (!propina.isTotalmentePaga() && !mecAdicionado.contains(propina.getNumeroMecEstudante())) {
                Estudante estudante = estudanteCRUD.lerEstudante(propina.getNumeroMecEstudante());
                if (estudante != null && estudante.isAtivo()) {
                    devedores.add(estudante);
                    mecAdicionado.add(propina.getNumeroMecEstudante());
                }
            }
        }
        return devedores;
    }

    public boolean isPropinaPaga(int numeroMec, int anoLetivo) {
        Propina p = propinaCRUD.procurarPropina(numeroMec, anoLetivo);
        return p != null && p.isTotalmentePaga();
    }
}