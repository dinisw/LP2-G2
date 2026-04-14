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

    public Resultado registarUC(String nome, int ano,int semestre, String siglaDocente) {
        Resultado res = new Resultado();

        if (nome == null || nome.trim().isEmpty()) {
            res.success = false;
            res.errorMessage = "O nome da Unidade Curricular não pode estar vazio.";
            return res;
        }

        if (ano < 1 || ano > 3) {
            res.success = false;
            res.errorMessage = "O ano curricular deve ser 1, 2 ou 3.";
            return res;
        }

        if (semestre < 1 || semestre > 2) {
            res.success = false;
            res.errorMessage = "O semestre deve ser 1 ou 2.";
            return res;
        }

        if (ucCRUD.procurarPorNome(nome) != null) {
            res.success = false;
            res.errorMessage = "Já existe uma Unidade Curricular registada com esse nome.";
            return res;
        }

        Docente docente = null;
        if (siglaDocente != null && !siglaDocente.trim().isEmpty()) {
            docente = docenteCRUD.procurarPorSigla(siglaDocente);
            if (docente == null) {
                res.success = false;
                res.errorMessage = "Docente com sigla '" + siglaDocente + "' não encontrado.";
                return res;
            }
        }

        UnidadeCurricular novaUC = new UnidadeCurricular(nome, ano,semestre, docente);

        if (ucCRUD.registarUC(novaUC)) {
            res.success = true;
        } else {
            res.success = false;
            res.errorMessage = "Ocorreu um erro na base de dados ao registar a Unidade Curricular.";
        }

        return res;
    }

    public List<UnidadeCurricular> listarTodasUCs() {
        return ucCRUD.getUcs();
    }

    public UnidadeCurricular procurarUCPorNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            return null;
        }
        return ucCRUD.procurarPorNome(nome);
    }

    public Resultado atualizarUC(String nomeAtual, String novoNome, int novoAno,int novoSemestre, String novaSiglaDocente) {
        Resultado resultado = new Resultado();

        if (nomeAtual == null || nomeAtual.trim().isEmpty()) {
            resultado.success = false;
            resultado.errorMessage = "O nome atual da UC é obrigatório para a pesquisa.";
            return resultado;
        }

        UnidadeCurricular ucExistente = ucCRUD.procurarPorNome(nomeAtual);
        if (ucExistente == null) {
            resultado.success = false;
            resultado.errorMessage = "Unidade Curricular não encontrada na base de dados.";
            return resultado;
        }

        String novoNomeReal = (novoNome == null || novoNome.trim().isEmpty()) ? nomeAtual : novoNome;
        int novoAnoReal = (novoAno <= 0) ? ucExistente.getAnoCurricular() : novoAno;

        int novoSemestreReal = (novoSemestre <= 0) ? ucExistente.getSemestre() : novoSemestre;

        Docente novoDocente = ucExistente.getDocente();
        if (novaSiglaDocente != null && !novaSiglaDocente.trim().isEmpty()) {
            DAL.DocenteCRUD docenteCRUD = new DocenteCRUD();
            Docente docenteEncontrado = docenteCRUD.procurarPorSigla(novaSiglaDocente);

            if (docenteEncontrado != null) {
                if (ucExistente.getDocente() != null && !ucExistente.getDocente().getSigla().equalsIgnoreCase(novaSiglaDocente)) {
                    Docente docenteAntigo = docenteCRUD.procurarPorNif(ucExistente.getDocente().getNif());
                    if (docenteAntigo != null) {
                        docenteAntigo.getUnidadesCurriculares().removeIf(u -> u.getNome().equalsIgnoreCase(nomeAtual));
                        docenteCRUD.atualizarDocente(docenteAntigo);
                    }
                }

                if (ucExistente.getDocente() == null || !ucExistente.getDocente().getSigla().equalsIgnoreCase(novaSiglaDocente)) {
                    UnidadeCurricular unidadeCurricularTemp = new UnidadeCurricular(novoNomeReal, novoAnoReal, novoSemestreReal, docenteEncontrado);
                    docenteEncontrado.adicionarUnidadeCurricular(unidadeCurricularTemp);
                    docenteCRUD.atualizarDocente(docenteEncontrado);

                    novoDocente = docenteEncontrado;
                }
            } else {
                resultado.success = false;
                resultado.errorMessage = "Novo docente não encontrado com a sigla informada.";
                return resultado;
            }
        }


        UnidadeCurricular ucAtualizada = new UnidadeCurricular(novoNomeReal, novoAnoReal,novoSemestreReal, novoDocente);

        if (ucCRUD.atualizarUC(nomeAtual, ucAtualizada)) {
            resultado.success = true;

            if (novoNome != null && !novoNome.equals(nomeAtual)) {
                if (ucAtualizada.getDocente() != null) {
                    DAL.DocenteCRUD docenteCRUD = new DAL.DocenteCRUD();
                    model.Docente docenteAfetado = docenteCRUD.procurarPorNif(ucAtualizada.getDocente().getNif());

                    if (docenteAfetado != null) {
                        for (model.UnidadeCurricular unidadeCurricular : docenteAfetado.getUnidadesCurriculares()) {
                            if (unidadeCurricular.getNome().equalsIgnoreCase(nomeAtual)) {
                                unidadeCurricular.setNome(novoNome);
                            }
                        }
                        docenteCRUD.atualizarDocente(docenteAfetado);
                    }
                }
            }
        }

        return resultado;
    }

    public Resultado eliminarUC(String nome) {
        Resultado res = new Resultado();

        if (nome == null || nome.trim().isEmpty()) {
            res.success = false;
            res.errorMessage = "O nome da UC a eliminar é obrigatório.";
            return res;
        }

        if (ucCRUD.procurarPorNome(nome) == null) {
            res.success = false;
            res.errorMessage = "Unidade Curricular não encontrada no sistema.";
            return res;
        }

        if (ucCRUD.eliminarUC(nome)) {
            res.success = true;
        } else {
            res.success = false;
            res.errorMessage = "Erro na base de dados ao eliminar a UC (pode ter alunos ou docente alocados).";
        }

        return res;
    }

    public List<UnidadeCurricular> listarUCsPorDocente(String siglaDocente) {
        if (siglaDocente == null || siglaDocente.trim().isEmpty()) {
            return null;
        }
        return ucCRUD.procurarPorDocente(siglaDocente);
    }

    public List<UnidadeCurricular> listarUCsPorAno(int ano) {
        if (ano < 1 || ano > 3) {
            return null;
        }
        return ucCRUD.procurarPorAno(ano);
    }
}