package model;

import java.time.LocalTime;

/**
 * Representa um bloco horário semanal de uma UC num dado ano letivo.
 * Restrições do enunciado v1.3:
 *   - Horário apenas entre 18:00 e 23:30
 *   - Pausa de jantar obrigatória: 20:00–20:30 (sem aulas nesse intervalo)
 *   - Blocos de 1h ou 2h (sem meios horários)
 *   - Máx. 5h letivas por dia por turma/docente
 *   - UC: mín. 2h e máx. 6h por semana
 *   - Sem sobreposição para o mesmo docente nem para a mesma UC
 */
public class Horario {
    private int id;
    private UnidadeCurricular unidadeCurricular;
    private int anoLetivoId;
    private DiaSemana diaSemana;
    private LocalTime horaInicio;
    private LocalTime horaFim;
    private String sala;

    public Horario() {}

    public Horario(UnidadeCurricular unidadeCurricular, int anoLetivoId,
                   DiaSemana diaSemana, LocalTime horaInicio, LocalTime horaFim, String sala) {
        this.unidadeCurricular = unidadeCurricular;
        this.anoLetivoId = anoLetivoId;
        this.diaSemana = diaSemana;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
        this.sala = sala;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public UnidadeCurricular getUnidadeCurricular() { return unidadeCurricular; }
    public void setUnidadeCurricular(UnidadeCurricular uc) { this.unidadeCurricular = uc; }

    public int getAnoLetivoId() { return anoLetivoId; }
    public void setAnoLetivoId(int anoLetivoId) { this.anoLetivoId = anoLetivoId; }

    public DiaSemana getDiaSemana() { return diaSemana; }
    public void setDiaSemana(DiaSemana diaSemana) { this.diaSemana = diaSemana; }

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalTime getHoraFim() { return horaFim; }
    public void setHoraFim(LocalTime horaFim) { this.horaFim = horaFim; }

    public String getSala() { return sala; }
    public void setSala(String sala) { this.sala = sala; }

    public int getDuracaoHoras() {
        return (int) java.time.Duration.between(horaInicio, horaFim).toHours();
    }

    public boolean sobrepoeCom(Horario outro) {
        if (this.diaSemana != outro.diaSemana) return false;
        return this.horaInicio.isBefore(outro.horaFim) && outro.horaInicio.isBefore(this.horaFim);
    }

    @Override
    public String toString() {
        return String.format("%s | %s | %s–%s | Sala: %s",
                diaSemana.getDescricao(),
                unidadeCurricular != null ? unidadeCurricular.getNome() : "?",
                horaInicio, horaFim, sala);
    }
}
