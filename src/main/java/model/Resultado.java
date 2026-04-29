package model;

public class Resultado<T> {
    public T dados;
    public boolean sucesso;
    public String mensagemErro;

    public Resultado() {}

    public Resultado(T dados, boolean sucesso) {
        this.dados = dados;
        this.sucesso = sucesso;
    }

    public Resultado(boolean sucesso, String mensagemErro) {
        this.sucesso = sucesso;
        this.mensagemErro = mensagemErro;
    }
}
