package main.model;

public class Propina {
    private int numeroMecEstudante; // A quem pertence a propina
    private int anoLetivo;          // A que ano curricular se refere (ex: 1º Ano)
    private double valorTotal;      // Valor configurado para o curso
    private double valorPago;       // Quanto o aluno já pagou (pode ser pago em prestações)

    // Construtor
    public Propina(int numeroMecEstudante, int anoLetivo, double valorTotal, double valorPago) {
        this.numeroMecEstudante = numeroMecEstudante;
        this.anoLetivo = anoLetivo;
        this.valorTotal = valorTotal;
        this.valorPago = valorPago;
    }

    // Getters e Setters
    public int getNumeroMecEstudante() { return numeroMecEstudante; }
    public void setNumeroMecEstudante(int numeroMecEstudante) { this.numeroMecEstudante = numeroMecEstudante; }

    public int getAnoLetivo() { return anoLetivo; }
    public void setAnoLetivo(int anoLetivo) { this.anoLetivo = anoLetivo; }

    public double getValorTotal() { return valorTotal; }
    public void setValorTotal(double valorTotal) { this.valorTotal = valorTotal; }

    public double getValorPago() { return valorPago; }
    public void setValorPago(double valorPago) { this.valorPago = valorPago; }

    // --- MÉTODOS DE INTELIGÊNCIA DE NEGÓCIO ---

    // Devolve o valor que ainda falta pagar
    public double getValorEmDivida() {
        return valorTotal - valorPago;
    }

    // Retorna true se a propina estiver totalmente paga
    public boolean isTotalmentePaga() {
        return valorPago >= valorTotal;
    }

    // Método para adicionar um pagamento (parcial ou total)
    public void registarPagamento(double valor) {
        if (valor > 0) {
            this.valorPago += valor;
        }
    }
}