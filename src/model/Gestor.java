package model;
import java.time.LocalDate;

public class Gestor extends Pessoa {

    private String cargo;

    public Gestor() {
        super();
        this.cargo = "";
    }

    public Gestor(String nome, String morada, int nif, LocalDate dataNascimento, String email, String hash, String salt, String cargo) {
        super(nome, morada, nif, dataNascimento, email, hash, salt);
        this.cargo = cargo;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    @Override
    public String toString() {
        return super.toString() + " Gestor{" +
                "cargo='" + cargo + '\'' +
                '}';
    }
}
