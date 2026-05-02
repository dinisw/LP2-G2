package controller;

import DAL.CursoCRUD;
import DAL.EstudanteCRUD;
import DAL.PropinaCRUD;
import model.Curso;
import model.Estudante;
import model.Propina;
import model.Resultado;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

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

        double precoAConfigurar = VALOR_PROPINA_PADRAO;
        EstudanteCRUD estudanteCRUD = new EstudanteCRUD();
        Estudante estudante = estudanteCRUD.lerEstudante(numeroMec);

        if (estudante != null && estudante.getNomeCurso() != null) {
            CursoCRUD cursoCRUD = new CursoCRUD();
            Curso curso = cursoCRUD.procurarPorNome(estudante.getNomeCurso());
            if (curso != null) precoAConfigurar = curso.getPrecoAnual();
        }

        Propina novaPropina = new Propina(numeroMec, anoLetivo, precoAConfigurar, 0.0);

        return propinaCRUD.registarPropina(novaPropina) ? new Resultado<>(novaPropina, true)
                : new Resultado<>(false, "Erro ao gerar a propina na base de dados.");
    }

    public Resultado<Propina> pagarPropina(int numeroMec, int anoLetivo, double valorPagamento) {
        if (valorPagamento <= 0) return new Resultado<>(false, "O valor do pagamento deve ser superior a zero.");

        Propina propina = propinaCRUD.procurarPropina(numeroMec, anoLetivo);
        if (propina == null) return new Resultado<>(false, "Propina não encontrada para o " + anoLetivo + "º ano.");
        if (propina.isTotalmentePaga()) return new Resultado<>(false, "Esta propina já se encontra totalmente paga. Obrigado!");

        if (valorPagamento > propina.getValorEmDivida()) {
            return new Resultado<>(false, "O valor inserido excede o valor em dívida (" + String.format("%.2f", propina.getValorEmDivida()) + "€).");
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
        List<Integer> mecsAdicionados = new ArrayList<>();

        for (Propina p : todas) {
            if (!p.isTotalmentePaga() && !mecsAdicionados.contains(p.getNumeroMecEstudante())) {
                Estudante est = estudanteCRUD.lerEstudante(p.getNumeroMecEstudante());
                if (est != null) {
                    devedores.add(est);
                    mecsAdicionados.add(p.getNumeroMecEstudante());
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