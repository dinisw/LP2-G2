package controller;

import DAL.UnidadeCurricularCRUD;
import DAL.DocenteCRUD;
import model.Docente;
import model.UnidadeCurricular;
import view.UnidadeCurricularView;
import java.util.List;

public class UnidadeCurricularController {
    private final UnidadeCurricularCRUD ucCRUD;
    private final DocenteCRUD docenteCRUD;

    public UnidadeCurricularController() {
        this.ucCRUD = new UnidadeCurricularCRUD();
        this.docenteCRUD = new DocenteCRUD();
    }

    // Método para exibir o menu completo
    public void exibirMenuUnidadesCurriculares() {
        UnidadeCurricularView view = new UnidadeCurricularView();
        view.exibirMenuUnidadesCurriculares();
    }

    // Método para registar uma UC (pode ser usado por outros controladores se necessário)
    public boolean registarUC(String nome, int ano, String siglaDocente) {
        if (nome == null || nome.isEmpty()) {
            System.out.println("Erro: Nome da UC não pode estar vazio.");
            return false;
        }

        if (ucCRUD.procurarPorNome(nome) != null) {
            System.out.println("Erro: Já existe uma UC com esse nome!");
            return false;
        }

        if (ano < 1 || ano > 3) {
            System.out.println("Erro: Ano curricular deve ser 1, 2 ou 3.");
            return false;
        }

        Docente docente = null;
        if (siglaDocente != null && !siglaDocente.isEmpty()) {
            docente = docenteCRUD.procurarPorSigla(siglaDocente);
            if (docente == null) {
                System.out.println("Erro: Docente com sigla '" + siglaDocente + "' não encontrado!");
                return false;
            }
        }

        UnidadeCurricular novaUC = new UnidadeCurricular(nome, ano, docente);
        if (ucCRUD.registarUC(novaUC)) {
            System.out.println("UC registada com sucesso!");
            return true;
        } else {
            System.out.println("Erro ao registar a UC no sistema.");
            return false;
        }
    }

    // Método para listar todas as UCs
    public List<UnidadeCurricular> listarTodasUCs() {
        return ucCRUD.getUcs();
    }

    // Método para procurar UC por nome
    public UnidadeCurricular procurarUC(String nome) {
        if (nome == null || nome.isEmpty()) {
            return null;
        }
        return ucCRUD.procurarPorNome(nome);
    }

    // Método para atualizar UC
    public boolean atualizarUC(String nomeAtual, String novoNome, int novoAno, String novaSiglaDocente) {
        if (nomeAtual == null || nomeAtual.isEmpty()) {
            System.out.println("Erro: Nome atual não pode estar vazio.");
            return false;
        }

        UnidadeCurricular ucExistente = ucCRUD.procurarPorNome(nomeAtual);
        if (ucExistente == null) {
            System.out.println("Erro: UC não encontrada.");
            return false;
        }

        String novoNomeReal = (novoNome == null || novoNome.isEmpty()) ? nomeAtual : novoNome;
        int novoAnoReal = (novoAno <= 0) ? ucExistente.getAnoCurricular() : novoAno;

        Docente novoDocente = ucExistente.getDocente();
        if (novaSiglaDocente != null && !novaSiglaDocente.isEmpty()) {
            novoDocente = docenteCRUD.procurarPorSigla(novaSiglaDocente);
            if (novoDocente == null) {
                System.out.println("Erro: Docente não encontrado.");
                return false;
            }
        }

        UnidadeCurricular ucAtualizada = new UnidadeCurricular(novoNomeReal, novoAnoReal, novoDocente);
        return ucCRUD.atualizarUC(nomeAtual, ucAtualizada);
    }

    // Método para eliminar UC
    public boolean eliminarUC(String nome) {
        if (nome == null || nome.isEmpty()) {
            System.out.println("Erro: Nome não pode estar vazio.");
            return false;
        }

        if (ucCRUD.procurarPorNome(nome) == null) {
            System.out.println("Erro: UC não encontrada.");
            return false;
        }

        return ucCRUD.eliminarUC(nome);
    }

    // Método para listar UCs de um docente
    public List<UnidadeCurricular> listarUCsPorDocente(String siglaDocente) {
        if (siglaDocente == null || siglaDocente.isEmpty()) {
            return null;
        }
        return ucCRUD.procurarPorDocente(siglaDocente);
    }

    // Método para listar UCs por ano curricular
    public List<UnidadeCurricular> listarUCsPorAno(int ano) {
        if (ano < 1 || ano > 3) {
            return null;
        }
        return ucCRUD.procurarPorAno(ano);
    }
}
