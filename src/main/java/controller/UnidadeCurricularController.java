package controller;

import DAL.DocenteCRUD;
import DAL.UnidadeCurricularCRUD;
import model.Docente;
import model.Resultado;
import model.UnidadeCurricular;

import java.util.List;

public class UnidadeCurricularController {
    private final UnidadeCurricularCRUD ucCRUD;
    private final DocenteCRUD docenteCRUD;

    public UnidadeCurricularController() {
        this.ucCRUD = new UnidadeCurricularCRUD();
        this.docenteCRUD = new DocenteCRUD();
    }

    public Resultado<UnidadeCurricular> registarUC(String nome, int ano, int semestre, String siglaDocente) {
        if (nome == null || nome.trim().isEmpty()) return new Resultado<>(false, "Nome da UC não pode estar vazio.");
        if (ano < 1 || ano > 3) return new Resultado<>(false, "Ano deve ser 1, 2 ou 3.");
        if (semestre < 1 || semestre > 2) return new Resultado<>(false, "Semestre deve ser 1 ou 2.");
        if (ucCRUD.procurarPorNome(nome) != null) return new Resultado<>(false, "Já existe uma UC com esse nome.");

        // REGRA DE NEGÓCIO: Obrigado a ter um Docente Existente
        if (siglaDocente == null || siglaDocente.trim().isEmpty()) {
            return new Resultado<>(false, "Bloqueado: É obrigatório associar um docente à Unidade Curricular.");
        }

        Docente docente = docenteCRUD.procurarPorSigla(siglaDocente);
        if (docente == null) {
            return new Resultado<>(false, "Bloqueado: Docente com a sigla '" + siglaDocente + "' não existe no sistema. Crie o docente primeiro.");
        }

        UnidadeCurricular novaUC = new UnidadeCurricular(nome, ano, semestre, docente);

        return ucCRUD.registarUC(novaUC) ? new Resultado<>(novaUC, true)
                : new Resultado<>(false, "Ocorreu um erro na base de dados ao registar a UC.");
    }

    // --- LEITURAS ---
    public List<UnidadeCurricular> listarTodasUCs() { return ucCRUD.getUnidadeCurriculars(); }
    public UnidadeCurricular procurarUCPorNome(String nome) { return ucCRUD.procurarPorNome(nome); }
    public UnidadeCurricular procurarUCPorId(int id) { return ucCRUD.procurarPorId(id); }

    public Resultado<UnidadeCurricular> eliminarUCPorId(int id) {
        return ucCRUD.eliminarUCPorId(id) ? new Resultado<>(null, true)
                : new Resultado<>(false, "Erro ao eliminar UC.");
    }
}