package model;

import java.util.ArrayList;
import java.util.List;

public class AnoLetivoCursoSnapshot {
    private int id;
    private int anoLetivoId;
    private Integer cursoId;
    private String nomeSnapshot;
    private String estadoCurso;
    private List<AnoLetivoUCSnapshot> ucs;
    private List<AnoLetivoEstudanteSnapshot> estudantes;

    public AnoLetivoCursoSnapshot(int anoLetivoId, Integer cursoId, String nomeSnapshot, String estadoCurso) {
        this.anoLetivoId = anoLetivoId;
        this.cursoId = cursoId;
        this.nomeSnapshot = nomeSnapshot;
        this.estadoCurso = estadoCurso;
        this.ucs = new ArrayList<>();
        this.estudantes = new ArrayList<>();
    }

    public AnoLetivoCursoSnapshot(int id, int anoLetivoId, Integer cursoId, String nomeSnapshot, String estadoCurso) {
        this(anoLetivoId, cursoId, nomeSnapshot, estadoCurso);
        this.id = id;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getAnoLetivoId() { return anoLetivoId; }
    public Integer getCursoId() { return cursoId; }
    public String getNomeSnapshot() { return nomeSnapshot; }
    public String getEstadoCurso() { return estadoCurso; }
    public List<AnoLetivoUCSnapshot> getUcs() { return ucs; }
    public List<AnoLetivoEstudanteSnapshot> getEstudantes() { return estudantes; }
    public void adicionarUC(AnoLetivoUCSnapshot uc) { ucs.add(uc); }
    public void adicionarEstudante(AnoLetivoEstudanteSnapshot e) { estudantes.add(e); }
}
