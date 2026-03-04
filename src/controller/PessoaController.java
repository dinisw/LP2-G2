package controller;

import model.Pessoa;
import view.PessoaView;

public class PessoaController {
    private Pessoa model;
    private PessoaView view;

    public PessoaController(Pessoa model, PessoaView view) {
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
