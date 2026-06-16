package controller;

import DAL.DAOFactory;
import DAL.ICursoDAO;
import DAL.IEstudanteDAO;
import DAL.IPropinaDAO;
import model.Curso;
import model.Estudante;
import model.Propina;
import model.Resultado;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PropinaController {
    private final IPropinaDAO propinaDAO;

    // CORREÇÃO: Transformar o valor padrão num BigDecimal
    private final BigDecimal VALOR_PROPINA_PADRAO = BigDecimal.valueOf(1000.0);

    public PropinaController() {
        this.propinaDAO = DAOFactory.getPropinaDAO();
    }

    public Resultado<Propina> gerarPropinaAnual(int numeroMec, int anoLetivo) {
        if (propinaDAO.procurarPropina(numeroMec, anoLetivo) != null) {
            return new Resultado<>(false, "A propina para o " + anoLetivo + "º ano já foi gerada.");
        }

        BigDecimal precoAConfigurar = VALOR_PROPINA_PADRAO;
        IEstudanteDAO estudanteDAO = DAOFactory.getEstudanteDAO();
        Estudante estudante = estudanteDAO.lerEstudante(numeroMec);

        if (estudante != null && estudante.getNomeCurso() != null) {
            ICursoDAO cursoDAO = DAOFactory.getCursoDAO();
            Curso curso = cursoDAO.procurarPorNome(estudante.getNomeCurso());

            if (curso != null && curso.getPrecoAnual() != null && curso.getPrecoAnual().compareTo(BigDecimal.ZERO)  > 0) {
                precoAConfigurar = curso.getPrecoAnual();
            }
        }

        // CORREÇÃO: Substituir 0.0 por BigDecimal.ZERO na criação da propina
        Propina novaPropina = new Propina(numeroMec, anoLetivo, precoAConfigurar, BigDecimal.ZERO);

        return propinaDAO.registarPropina(novaPropina)
                ? new Resultado<>(novaPropina, true)
                : new Resultado<>(false, "Erro ao gerar a propina na base de dados.");
    }

    /**
     * Bug 1: quando um estudante repete o ano por reprovação, a propina desse ano
     * tem de voltar a estar por pagar (uma propina é anual, paga por cada ano de frequência).
     *
     * <ul>
     *   <li>Se a propina não existir, gera uma nova (por pagar).</li>
     *   <li>Se existir e estiver totalmente paga, repõe o valor pago a zero
     *       (mantendo o histórico, com uma marca de reposição) e devolve {@code true}.</li>
     *   <li>Se já estiver por pagar, não faz nada e devolve {@code false}.</li>
     * </ul>
     *
     * @return {@code true} apenas se uma propina paga foi efetivamente reposta para pagamento.
     */
    public boolean reporPropinaParaRepeticao(int numeroMec, int anoLetivo) {
        Propina propina = propinaDAO.procurarPropina(numeroMec, anoLetivo);
        if (propina == null) {
            gerarPropinaAnual(numeroMec, anoLetivo);
            return false;
        }
        if (propina.isTotalmentePaga()) {
            propina.setValorPago(BigDecimal.ZERO);
            if (propina.getHistoricoPagamentos() != null) {
                propina.getHistoricoPagamentos().add(
                        LocalDate.now() + " -> Ano repetido: propina reposta para novo pagamento");
            }
            propinaDAO.atualizarPropina(propina);
            return true;
        }
        return false;
    }

    public Resultado<Propina> pagarPropina(int numeroMec, int anoLetivo, BigDecimal valorPagamento) {

        // CORREÇÃO: Usar .compareTo() para verificar se é menor ou igual a zero
        if (valorPagamento == null || valorPagamento.compareTo(BigDecimal.ZERO) <= 0) {
            return new Resultado<>(false, "O valor do pagamento deve ser superior a zero.");
        }

        Propina propina = propinaDAO.procurarPropina(numeroMec, anoLetivo);
        if (propina == null) return new Resultado<>(false, "Propina não encontrada para o " + anoLetivo + "º ano.");

        // CORREÇÃO: Usar .compareTo() para verificar se o pagamento é superior à dívida
        if (valorPagamento.compareTo(propina.getValorEmDivida()) > 0) {
            return new Resultado<>(false, "Operação Recusada: O valor inserido (" + valorPagamento + "€) é superior à dívida atual (" + propina.getValorEmDivida() + "€).");
        }

        if (propina.isTotalmentePaga()) return new Resultado<>(false, "Esta propina já se encontra totalmente paga. Obrigado!");

        // CORREÇÃO: O método da sua classe modelo deve bater certo (registrar vs registar).
        // Verifique se o nome na classe Propina é registrarPagamento ou registarPagamento
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