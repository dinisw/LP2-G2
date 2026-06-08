package model;

public class AnoLetivoNotaSnapshot {
    private int id;
    private int anoLetivoEstudanteId;
    private String nomeUC;
    private String momento;
    private Double nota;

    public AnoLetivoNotaSnapshot(int anoLetivoEstudanteId, String nomeUC, String momento, Double nota) {
        this.anoLetivoEstudanteId = anoLetivoEstudanteId;
        this.nomeUC = nomeUC;
        this.momento = momento;
        this.nota = nota;
    }

    public AnoLetivoNotaSnapshot(int id, int anoLetivoEstudanteId, String nomeUC, String momento, Double nota) {
        this(anoLetivoEstudanteId, nomeUC, momento, nota);
        this.id = id;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getAnoLetivoEstudanteId() { return anoLetivoEstudanteId; }
    public String getNomeUC() { return nomeUC; }
    public String getMomento() { return momento; }
    public Double getNota() { return nota; }
}
