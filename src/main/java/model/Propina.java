package model;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

public class Propina {
    private int numeroMecEstudante;
    private int anoLetivo;
    private double valorTotal;
    private double valorPago;
    private List<String> historicoPagamentos;

    public Propina(int numeroMecEstudante, int anoLetivo, double valorTotal, double valorPago) {
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

    public double getValorTotal() { return valorTotal; }
    public void setValorTotal(double valorTotal) { this.valorTotal = valorTotal; }

    public double getValorPago() { return valorPago; }
    public void setValorPago(double valorPago) { this.valorPago = valorPago; }

    public double getValorEmDivida() {
        double divida = valorTotal - valorPago;
        return Math.round(divida * 100) /100;
    }

    public boolean isTotalmentePaga() {
        return getValorEmDivida() <= 0.0;
    }

    public List<String> getHistoricoPagamentos() {
        return historicoPagamentos;
    }

    public void setHistoricoPagamentos(List<String> historicoPagamentos) {
        this.historicoPagamentos = historicoPagamentos;
    }



    public void registarPagamento(double valor) {
        if (valor > 0) {
            valor = Math.round(valor * 100) / 100;
            this.valorPago += valor;

            String registo = LocalDate.now().toString() + " -> " + String.valueOf(valor) + " EUR";
            this.historicoPagamentos.add(registo);
        }
    }
}