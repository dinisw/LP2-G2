package model;

import java.time.LocalDate;

/**
 * Registo de presença de um estudante numa aula.
 * Regra v1.3: o docente marca presença primeiro; só depois o estudante pode confirmar a sua.
 */
public class Presenca {
    private int id;
    private Estudante estudante;
    private Horario horario;
    private LocalDate data;
    private boolean presencaDocente;
    private boolean presencaEstudante;

    public Presenca() {}

    public Presenca(Estudante estudante, Horario horario, LocalDate data) {
        this.estudante = estudante;
        this.horario = horario;
        this.data = data;
        this.presencaDocente = false;
        this.presencaEstudante = false;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Estudante getEstudante() { return estudante; }
    public void setEstudante(Estudante estudante) { this.estudante = estudante; }

    public Horario getHorario() { return horario; }
    public void setHorario(Horario horario) { this.horario = horario; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public boolean isPresencaDocente() { return presencaDocente; }
    public void setPresencaDocente(boolean presencaDocente) { this.presencaDocente = presencaDocente; }

    public boolean isPresencaEstudante() { return presencaEstudante; }
    public void setPresencaEstudante(boolean presencaEstudante) {
        if (!presencaDocente) throw new IllegalStateException("O docente tem de marcar presença antes do estudante.");
        this.presencaEstudante = presencaEstudante;
    }

    public boolean isFalta() { return presencaDocente && !presencaEstudante; }

    @Override
    public String toString() {
        return String.format("Data: %s | UC: %s | Docente marcou: %s | Estudante marcou: %s",
                data,
                horario != null && horario.getUnidadeCurricular() != null ? horario.getUnidadeCurricular().getNome() : "?",
                presencaDocente ? "Sim" : "Não",
                presencaEstudante ? "Sim" : "Não");
    }
}
