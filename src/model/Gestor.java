package model;
import java.time.LocalDate;

public class Gestor extends Utilizador {
    private int id;
    private String cargo;

    public Gestor() {
        super();
        this.cargo = "";
        this.id = 0;
    }

    public Gestor(int id, String nome, String morada, int nif, LocalDate dataNascimento, String email, String hash, String cargo) { //usado para ler com id
        super(nome, morada, nif, dataNascimento, email, hash);
        this.cargo = cargo;
        this.id = id;
    }

    public Gestor(String nome, String morada, int nif, LocalDate dataNascimento, String email, String hash, String cargo) { //usada para o registo do gestor
        super(nome, morada, nif, dataNascimento, email, hash);
        this.cargo = cargo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    @Override
    public String toString() {
        return super.toString() +
                "\n  ID: " + id +
                "\n  Cargo: " + cargo;
    }
}
