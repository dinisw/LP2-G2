package model;

import java.math.BigDecimal;
import java.math.RoundingMode; // <-- ADICIONADO: Import necessário
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

public class Propina {
    private int numeroMecEstudante;
    private int anoLetivo;
    private BigDecimal valorTotal;
    private BigDecimal valorPago;
    private List<String> historicoPagamentos;

    public Propina(int numeroMecEstudante, int anoLetivo, BigDecimal valorTotal, BigDecimal valorPago) {
        this.numeroMecEstudante = numeroMecEstudante;
        this.anoLetivo = anoLetivo;
        this.valorTotal = valorTotal;
        this.valorPago = valorPago;
        this.historicoPagamentos = new ArrayList<>();
    }

    public int getNumeroMecEstudante() { return numeroMecEstudante; }
    public void setNumeroMecEstudante(int numeroMecEstudante) { this.numeroMecEstudante = numeroMecEstudante; }

    public int getAnoLetivo() { return anoLetivo; }
    public void setAnoLetivo(int anoLetivo) { this.anoLetivo = anoLetivo; }

    public BigDecimal getValorTotal() { return valorTotal; }
    public void setValorTotal(BigDecimal valorTotal) { this.valorTotal = valorTotal; }

    public BigDecimal getValorPago() { return valorPago; }
    public void setValorPago(BigDecimal valorPago) { this.valorPago = valorPago; }

    public BigDecimal getValorEmDivida() {
        return this.valorTotal.subtract(this.valorPago);
    }

    public boolean isTotalmentePaga() {
        return getValorEmDivida().compareTo(BigDecimal.ZERO) <= 0;
    }

    public List<String> getHistoricoPagamentos() {
        return historicoPagamentos;
    }

    public void setHistoricoPagamentos(List<String> historicoPagamentos) {
        this.historicoPagamentos = historicoPagamentos;
    }

    public void registarPagamento(BigDecimal valor) {
        if (valor != null && valor.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal valorArredondado = valor.setScale(2, RoundingMode.HALF_UP);

            this.valorPago = this.valorPago.add(valorArredondado);

            String registo = LocalDate.now().toString() + " -> " + valorArredondado.toString() + " EUR";
            this.historicoPagamentos.add(registo);
        }
    }
}