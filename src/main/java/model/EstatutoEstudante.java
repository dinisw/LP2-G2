package model;

import java.time.LocalDate;

/**
 * Associação entre um estudante e um tipo de estatuto especial.
 * Um estudante pode ter zero ou um estatuto ativo de cada tipo.
 */
public class EstatutoEstudante {
    private int id;
    private Estudante estudante;
    private TipoEstatuto tipoEstatuto;
    private LocalDate dataInicio;
    private LocalDate dataFim;

    public EstatutoEstudante() {}

    public EstatutoEstudante(Estudante estudante, TipoEstatuto tipoEstatuto, LocalDate dataInicio, LocalDate dataFim) {
        this.estudante = estudante;
        this.tipoEstatuto = tipoEstatuto;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Estudante getEstudante() { return estudante; }
    public void setEstudante(Estudante estudante) { this.estudante = estudante; }

    public TipoEstatuto getTipoEstatuto() { return tipoEstatuto; }
    public void setTipoEstatuto(TipoEstatuto tipoEstatuto) { this.tipoEstatuto = tipoEstatuto; }

    public LocalDate getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDate dataInicio) { this.dataInicio = dataInicio; }

    public LocalDate getDataFim() { return dataFim; }
    public void setDataFim(LocalDate dataFim) { this.dataFim = dataFim; }

    public boolean isAtivo() {
        LocalDate hoje = LocalDate.now();
        return !hoje.isBefore(dataInicio) && (dataFim == null || !hoje.isAfter(dataFim));
    }

    @Override
    public String toString() {
        return String.format("Estatuto: %s | Válido: %s → %s",
                tipoEstatuto != null ? tipoEstatuto.getNome() : "?",
                dataInicio,
                dataFim != null ? dataFim.toString() : "Indefinido");
    }
}
