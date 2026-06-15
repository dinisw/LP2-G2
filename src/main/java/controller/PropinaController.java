package controller;

import DAL.DAOFactory;
import DAL.ICursoDAO;
import DAL.IEstudanteDAO;
import DAL.IPropinaDAO;
import model.Curso;
import model.Estudante;
import model.Propina;
import model.Resultado;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PropinaController {
    private final IPropinaDAO propinaDAO;
    private final double VALOR_PROPINA_PADRAO = 1000.0;

    public PropinaController() {
        this.propinaDAO = DAOFactory.getPropinaDAO();
    }

    public Resultado<Propina> gerarPropinaAnual(int numeroMec, int anoLetivo) {
        if (propinaDAO.procurarPropina(numeroMec, anoLetivo) != null) {
            return new Resultado<>(false, "A propina para o " + anoLetivo + "º ano já foi gerada.");
        }

        double precoAConfigurar = VALOR_PROPINA_PADRAO;
        IEstudanteDAO estudanteDAO = DAOFactory.getEstudanteDAO();
        Estudante estudante = estudanteDAO.lerEstudante(numeroMec);

        if (estudante != null && estudante.getNomeCurso() != null) {
            ICursoDAO cursoDAO = DAOFactory.getCursoDAO();
            Curso curso = cursoDAO.procurarPorNome(estudante.getNomeCurso());
            if (curso != null && curso.getPrecoAnual() > 0) precoAConfigurar = curso.getPrecoAnual();
        }

        Propina novaPropina = new Propina(numeroMec, anoLetivo, precoAConfigurar, 0.0);
        return propinaDAO.registarPropina(novaPropina)
                ? new Resultado<>(novaPropina, true)
                : new Resultado<>(false, "Erro ao gerar a propina na base de dados.");
    }

    public Resultado<Propina> pagarPropina(int numeroMec, int anoLetivo, double valorPagamento) {
        if (valorPagamento <= 0) return new Resultado<>(false, "O valor do pagamento deve ser superior a zero.");

        Propina propina = propinaDAO.procurarPropina(numeroMec, anoLetivo);
        if (propina == null) return new Resultado<>(false, "Propina não encontrada para o " + anoLetivo + "º ano.");

        if (valorPagamento > propina.getValorEmDivida()) {
            return new Resultado<>(false, "Operação Recusada: O valor inserido (" + valorPagamento + "€) é superior à dívida atual (" + propina.getValorEmDivida() + "€).");
        }
        if (propina.isTotalmentePaga()) return new Resultado<>(false, "Esta propina já se encontra totalmente paga. Obrigado!");

        propina.registarPagamento(valorPagamento);
        return propinaDAO.atualizarPropina(propina)
                ? new Resultado<>(propina, true)
                : new Resultado<>(false, "Erro ao guardar o pagamento no sistema.");
    }

    public List<Propina> consultarPropinasEstudante(int numeroMec) {
        return propinaDAO.listarPropinasPorEstudante(numeroMec);
    }

    public List<Estudante> obterAlunosEmDivida() {
        List<Propina> todas = propinaDAO.getTodasPropinas();
        IEstudanteDAO estudanteDAO = DAOFactory.getEstudanteDAO();
        List<Estudante> devedores = new ArrayList<>();
        Set<Integer> mecAdicionado = new HashSet<>();

        for (Propina propina : todas) {
            if (!propina.isTotalmentePaga() && !mecAdicionado.contains(propina.getNumeroMecEstudante())) {
                Estudante estudante = estudanteDAO.lerEstudante(propina.getNumeroMecEstudante());
                if (estudante != null && estudante.isAtivo()) {
                    devedores.add(estudante);
                    mecAdicionado.add(propina.getNumeroMecEstudante());
                }
            }
        }
        return devedores;
    }

    public boolean isPropinaPaga(int numeroMec, int anoLetivo) {
        Propina p = propinaDAO.procurarPropina(numeroMec, anoLetivo);
        return p != null && p.isTotalmentePaga();
    }
}
