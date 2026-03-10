package controller;

import model.UnidadeCurricular;
import view.UnidadeCurricularView;

public class UnidadeCurricularController {
    private UnidadeCurricular model;
    private UnidadeCurricularView view;

    public UnidadeCurricularController(UnidadeCurricular model, UnidadeCurricularView view) {
        this.model = model;
        this.view = view;
    }

    public UnidadeCurricular getModel() {
        return model;
    }

    public void setModel(UnidadeCurricular model) {
        this.model = model;
    }

    public UnidadeCurricularView getView() {
        return view;
    }

    public void setView(UnidadeCurricularView view) {
        this.view = view;
    }
}
