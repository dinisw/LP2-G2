package controller;

import model.Utilizador;
import view.UtilizadorView;

public class UtilizadorController {
    private Utilizador model;
    private UtilizadorView view;

    public UtilizadorController(Utilizador model, UtilizadorView view) {
        this.model = model;
        this.view = view;
    }

    public void setNome(String nome) {
        model.setNome(nome);
    }

    public String getNome() {
        return model.getNome();
    }

    public void setEmail(String email) {
        model.setEmail(email);
    }

    public String getEmail() {
        return model.getEmail();
    }

    public void atualizarView() {
        view.imprimirDadosPessoa(model.getNome(), model.getEmail());
    }
}
