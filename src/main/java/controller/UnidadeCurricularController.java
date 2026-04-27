package main.controller;

import main.DAL.DocenteCRUD;
import main.DAL.UnidadeCurricularCRUD;
import main.model.Docente;
import main.model.Resultado;
import main.model.UnidadeCurricular;

import java.util.ArrayList;
import java.util.List;

public class UnidadeCurricularController {

    private final UnidadeCurricularCRUD ucCRUD;
    private final DocenteCRUD docenteCRUD;

    public UnidadeCurricularController() {
        this.ucCRUD = new UnidadeCurricularCRUD();
        this.docenteCRUD = new DocenteCRUD();
    }

    public Resultado registarUC(String nome, int ano, int semestre, String siglaDocente) {
        Resultado resultado = new Resultado();

        if (docenteCRUD.getDocentes().isEmpty()) {
            resultado.success = false;
            resultado.errorMessage = "Ação bloqueada: Não existem docentes registados no sistema. Registe um docente primeiro.";
            return resultado;
        }

        if (nome == null || nome.trim().isEmpty()) {
            resultado.success = false;
            resultado.errorMessage = "O nome da Unidade Curricular não pode estar vazio.";
            return resultado;
        }

        if (ano < 1 || ano > 3) {
            resultado.success = false;
            resultado.errorMessage = "O ano curricular deve ser 1, 2 ou 3.";
            return resultado;
        }

        if (semestre < 1 || semestre > 2) {
            resultado.success = false;
            resultado.errorMessage = "O semestre deve ser 1 ou 2.";
            return resultado;
        }

        if (ucCRUD.procurarPorNome(nome) != null) {
            resultado.success = false;
            resultado.errorMessage = "Já existe uma Unidade Curricular registada com esse nome.";
            return resultado;
        }

        Docente docente = null;
        if (siglaDocente != null && !siglaDocente.trim().isEmpty()) {
            docente = docenteCRUD.procurarPorSigla(siglaDocente);
            if (docente == null) {
                resultado.success = false;
                resultado.errorMessage = "Docente com sigla '" + siglaDocente + "' não encontrado.";
                return resultado;
            }
        }

        UnidadeCurricular novaUC = new UnidadeCurricular(nome, ano, semestre, docente);

        if (ucCRUD.registarUC(novaUC)) {
            resultado.success = true;
        } else {
            resultado.success = false;
            resultado.errorMessage = "Ocorreu um erro na base de dados ao registar a Unidade Curricular.";
        }

        return resultado;
    }

    public List<UnidadeCurricular> listarTodasUCs() {
        return ucCRUD.getUnidadeCurriculars();
    }

    public UnidadeCurricular procurarUCPorNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            return null;
        }
        return ucCRUD.procurarPorNome(nome);
    }

    public Resultado atualizarUC(String nomeAtual, String novoNome, int novoAno, int novoSemestre, String novaSiglaDocente) {
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
            main.DAL.DocenteCRUD docenteCRUD = new DocenteCRUD();
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


        UnidadeCurricular ucAtualizada = new UnidadeCurricular(novoNomeReal, novoAnoReal, novoSemestreReal, novoDocente);

        if (ucCRUD.atualizarUC(nomeAtual, ucAtualizada)) {
            resultado.success = true;

            if (novoNome != null && !novoNome.equals(nomeAtual)) {
                if (ucAtualizada.getDocente() != null) {
                    main.DAL.DocenteCRUD docenteCRUD = new main.DAL.DocenteCRUD();
                    main.model.Docente docenteAfetado = docenteCRUD.procurarPorNif(ucAtualizada.getDocente().getNif());

                    if (docenteAfetado != null) {
                        for (main.model.UnidadeCurricular unidadeCurricular : docenteAfetado.getUnidadesCurriculares()) {
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
        Resultado resultado = new Resultado();

        if (nome == null || nome.trim().isEmpty()) {
            resultado.success = false;
            resultado.errorMessage = "O nome da UC a eliminar é obrigatório.";
            return resultado;
        }

        if (ucCRUD.procurarPorNome(nome) == null) {
            resultado.success = false;
            resultado.errorMessage = "Unidade Curricular não encontrada no sistema.";
            return resultado;
        }

        if (ucCRUD.eliminarUC(nome)) {
            resultado.success = true;
        } else {
            resultado.success = false;
            resultado.errorMessage = "Erro na base de dados ao eliminar a UC (pode ter alunos ou docente alocados).";
        }

        return resultado;
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

    public Resultado definirMomentos(String nomeUnidadeCurricular, String momentosSeparadosPorVirgula) {
        Resultado resultado = new Resultado();
        main.DAL.UnidadeCurricularCRUD unidadeCurricularCRUDAtualizado = new main.DAL.UnidadeCurricularCRUD();
        main.model.UnidadeCurricular unidadeCurricular = unidadeCurricularCRUDAtualizado.procurarPorNome(nomeUnidadeCurricular);

        if (unidadeCurricular != null) {
            unidadeCurricular.setMomentosAvaliacao(new ArrayList<>());
            String[] momentos = momentosSeparadosPorVirgula.split(",");
            for (String momento : momentos) {
                if (!momento.trim().isEmpty()) {
                    unidadeCurricular.adicionarMomento(momento.trim());
                }
            }
            if (unidadeCurricularCRUDAtualizado.atualizarUC(unidadeCurricular.getNome(), unidadeCurricular)) {
                resultado.success = true;
            } else {
                resultado.success = false;
                resultado.errorMessage = "Erro na base de dados ao guardar os momentos.";
            }
        } else {
            resultado.success = false;
            resultado.errorMessage = "Unidade Curricular não encontrada.";
        }
        return resultado;
    }
}