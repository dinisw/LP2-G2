package model;

public class AnoLetivoUCSnapshot {
    private int id;
    private int anoLetivoCursoId;
    private String nomeUC;
    private int anoCurricular;
    private String docenteNome;
    private String docenteSigla;
    private String momentos;

    public AnoLetivoUCSnapshot(int anoLetivoCursoId, String nomeUC, int anoCurricular,
                                String docenteNome, String docenteSigla, String momentos) {
        this.anoLetivoCursoId = anoLetivoCursoId;
        this.nomeUC = nomeUC;
        this.anoCurricular = anoCurricular;
        this.docenteNome = docenteNome;
        this.docenteSigla = docenteSigla;
        this.momentos = momentos;
    }

    public AnoLetivoUCSnapshot(int id, int anoLetivoCursoId, String nomeUC, int anoCurricular,
                                String docenteNome, String docenteSigla, String momentos) {
        this(anoLetivoCursoId, nomeUC, anoCurricular, docenteNome, docenteSigla, momentos);
        this.id = id;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getAnoLetivoCursoId() { return anoLetivoCursoId; }
    public String getNomeUC() { return nomeUC; }
    public int getAnoCurricular() { return anoCurricular; }
    public String getDocenteNome() { return docenteNome; }
    public String getDocenteSigla() { return docenteSigla; }
    public String getMomentos() { return momentos; }
}
