package controller;

import model.Estudante;
import view.EstudanteView;

public class EstudanteController {
    private Estudante model;
    private EstudanteView view;

    public EstudanteController(Estudante model, EstudanteView view) {
        this.model = model;
        this.view = view;
    }

    public void exibirFichaEstudante() {
        String dataNascimentoStr = (model.getDataNascimento() != null) ? model.getDataNascimento().toString() : "Não definida";
        String cursoStr = (model.getNomeCurso() != null && !model.getNomeCurso().isEmpty()) ? model.getNomeCurso() : "Sem curso";

        view.imprimirFichaEstudante(
                model.getNome(),
                model.getNumeroMec(),
                model.getEmail(),
                model.getNif(),
                dataNascimentoStr,
                model.getMorada(),
                cursoStr,
                model.getAnoLetivo()
        );
    }
    public void exibirNotas() {
        view.imprimirNotas(model.getListaAvaliacoes());
    }
    public void tentarPassarDeAno(int totalUCsInscritas) {
        boolean passou = model.verificarProgessaoAno(totalUCsInscritas);
        if (passou) {
            view.mostrarMensagem("Sucesso: O estudante transitou para o " + model.getAnoLetivo() + "º ano letivo.");
        } else {
            view.mostrarMensagem("Falhou: O estudante falhou em cumprir os 60% de aproveitamento e manter-se-á no " + model.getAnoLetivo() + "º ano.");
        }
    }
}
