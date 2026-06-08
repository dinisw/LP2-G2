package DAL;

import model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AnoLetivoMemDAO implements IAnoLetivoDAO {

    private static final List<AnoLetivo> anos = new ArrayList<>();
    private static final List<AnoLetivoCursoSnapshot> cursosSnap = new ArrayList<>();
    private static final List<AnoLetivoUCSnapshot> ucsSnap = new ArrayList<>();
    private static final List<AnoLetivoEstudanteSnapshot> estudantesSnap = new ArrayList<>();
    private static final List<AnoLetivoNotaSnapshot> notasSnap = new ArrayList<>();

    private static final AtomicInteger anoSeq = new AtomicInteger(1);
    private static final AtomicInteger cursoSeq = new AtomicInteger(1);
    private static final AtomicInteger ucSeq = new AtomicInteger(1);
    private static final AtomicInteger estSeq = new AtomicInteger(1);
    private static final AtomicInteger notaSeq = new AtomicInteger(1);

    @Override
    public boolean registarAnoLetivo(AnoLetivo al) {
        if (obterPorAnoCalendario(al.getAnoCalendario()) != null) return false;
        al.setId(anoSeq.getAndIncrement());
        anos.add(al);
        return true;
    }

    @Override
    public AnoLetivo obterAnoAtual() {
        return anos.stream().filter(AnoLetivo::isAtivo)
                .max((a, b) -> Integer.compare(a.getAnoCalendario(), b.getAnoCalendario()))
                .orElse(null);
    }

    @Override
    public List<AnoLetivo> listarTodos() {
        List<AnoLetivo> r = new ArrayList<>(anos);
        r.sort((a, b) -> Integer.compare(b.getAnoCalendario(), a.getAnoCalendario()));
        return r;
    }

    @Override
    public AnoLetivo obterPorAnoCalendario(int ano) {
        return anos.stream().filter(a -> a.getAnoCalendario() == ano).findFirst().orElse(null);
    }

    @Override
    public boolean atualizarAnoLetivo(AnoLetivo al) {
        for (int i = 0; i < anos.size(); i++) {
            if (anos.get(i).getId() == al.getId()) { anos.set(i, al); return true; }
        }
        return false;
    }

    @Override
    public int salvarCursoSnapshot(AnoLetivoCursoSnapshot s) {
        int id = cursoSeq.getAndIncrement();
        s.setId(id);
        cursosSnap.add(s);
        return id;
    }

    @Override
    public void salvarUCSnapshot(AnoLetivoUCSnapshot s) {
        s.setId(ucSeq.getAndIncrement());
        ucsSnap.add(s);
    }

    @Override
    public int salvarEstudanteSnapshot(AnoLetivoEstudanteSnapshot s) {
        int id = estSeq.getAndIncrement();
        s.setId(id);
        estudantesSnap.add(s);
        return id;
    }

    @Override
    public void salvarNotaSnapshot(AnoLetivoNotaSnapshot s) {
        s.setId(notaSeq.getAndIncrement());
        notasSnap.add(s);
    }

    @Override
    public List<AnoLetivoCursoSnapshot> obterCursosSnapshot(int anoLetivoId) {
        return cursosSnap.stream().filter(c -> c.getAnoLetivoId() == anoLetivoId)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<AnoLetivoUCSnapshot> obterUCsSnapshot(int anoLetivoCursoId) {
        return ucsSnap.stream().filter(u -> u.getAnoLetivoCursoId() == anoLetivoCursoId)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<AnoLetivoEstudanteSnapshot> obterEstudantesSnapshot(int anoLetivoCursoId) {
        return estudantesSnap.stream().filter(e -> e.getAnoLetivoCursoId() == anoLetivoCursoId)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<AnoLetivoNotaSnapshot> obterNotasSnapshot(int anoLetivoEstudanteId) {
        return notasSnap.stream().filter(n -> n.getAnoLetivoEstudanteId() == anoLetivoEstudanteId)
                .collect(java.util.stream.Collectors.toList());
    }
}
