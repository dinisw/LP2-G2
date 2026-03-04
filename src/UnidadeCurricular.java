import java.time.LocalDate;

public class UnidadeCurricular {
    private String nome;
    private LocalDate anoCurricular;
    private Docente docente;

    public UnidadeCurricular() {
    }

    public UnidadeCurricular(String nome, LocalDate anoCurricular, Docente docente) {
        this.nome = nome;
        this.anoCurricular = anoCurricular;
        this.docente = docente;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public LocalDate getAnoCurricular() {
        return anoCurricular;
    }

    public void setAnoCurricular(LocalDate anoCurricular) {
        this.anoCurricular = anoCurricular;
    }

    public Docente getDocente() {
        return docente;
    }

    public void setDocente(Docente docente) {
        this.docente = docente;
    }
}
