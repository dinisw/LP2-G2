package model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class AnoLetivoEstudanteSnapshot {
    private int id;
    private int anoLetivoCursoId;
    private int numeroMec;
    private String nomeSnapshot;
    private int anoCurricularInicio;
    private int anoCurricularFim;
    private BigDecimal propinaTotal;
    private BigDecimal propinaPaga;
    private String resultado;
    private List<AnoLetivoNotaSnapshot> notas;

    public AnoLetivoEstudanteSnapshot(int anoLetivoCursoId, int numeroMec, String nomeSnapshot,
                                      int anoCurricularInicio, int anoCurricularFim,
                                      BigDecimal propinaTotal, BigDecimal propinaPaga, String resultado) {
        this.anoLetivoCursoId = anoLetivoCursoId;
        this.numeroMec = numeroMec;
        this.nomeSnapshot = nomeSnapshot;
        this.anoCurricularInicio = anoCurricularInicio;
        this.anoCurricularFim = anoCurricularFim;
        this.propinaTotal = propinaTotal != null ? propinaTotal : BigDecimal.ZERO;
        this.propinaPaga = propinaPaga != null ? propinaPaga : BigDecimal.ZERO;
        this.resultado = resultado;
        this.notas = new ArrayList<>();
    }

    public AnoLetivoEstudanteSnapshot(int id, int anoLetivoCursoId, int numeroMec, String nomeSnapshot,
                                      int anoCurricularInicio, int anoCurricularFim,
                                      BigDecimal propinaTotal, BigDecimal propinaPaga, String resultado) {
        this(anoLetivoCursoId, numeroMec, nomeSnapshot, anoCurricularInicio, anoCurricularFim,
                propinaTotal, propinaPaga, resultado);
        this.id = id;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getAnoLetivoCursoId() { return anoLetivoCursoId; }
    public int getNumeroMec() { return numeroMec; }
    public String getNomeSnapshot() { return nomeSnapshot; }
    public int getAnoCurricularInicio() { return anoCurricularInicio; }
    public int getAnoCurricularFim() { return anoCurricularFim; }
    public BigDecimal getPropinaTotal() { return propinaTotal; }
    public BigDecimal getPropinaPaga() { return propinaPaga; }
    public BigDecimal getValorEmDivida() { return propinaTotal.subtract(propinaPaga); }
    public boolean isPropinaPaga() { return propinaTotal.compareTo(propinaPaga) <= 0; }
    public String getResultado() { return resultado; }
    public List<AnoLetivoNotaSnapshot> getNotas() { return notas; }
    public void adicionarNota(AnoLetivoNotaSnapshot nota) { notas.add(nota); }
}
