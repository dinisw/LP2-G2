package model;

import java.time.LocalDate;

public class AnoLetivo {
    private int id;
    private int anoCalendario;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private String estado;

    public AnoLetivo(int anoCalendario, LocalDate dataInicio) {
        this.anoCalendario = anoCalendario;
        this.dataInicio = dataInicio;
        this.dataFim = null;
        this.estado = "ATIVO";
    }

    public AnoLetivo(int id, int anoCalendario, LocalDate dataInicio, LocalDate dataFim, String estado) {
        this.id = id;
        this.anoCalendario = anoCalendario;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.estado = estado;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getAnoCalendario() { return anoCalendario; }
    public LocalDate getDataInicio() { return dataInicio; }
    public LocalDate getDataFim() { return dataFim; }
    public void setDataFim(LocalDate dataFim) { this.dataFim = dataFim; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getDescricao() {
        return anoCalendario + "/" + (anoCalendario + 1);
    }

    public boolean isAtivo() {
        return "ATIVO".equals(estado);
    }
}
