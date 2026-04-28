package controller;

import DAL.DocenteCRUD;
import DAL.UnidadeCurricularCRUD;
import model.Docente;
import model.Resultado;
import model.UnidadeCurricular;

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

    public UnidadeCurricular procurarUCPorId(int id) {
        return ucCRUD.procurarPorId(id);
    }

    public Resultado eliminarUCPorId(int id) {
        Resultado resultado = new Resultado();

        if (ucCRUD.procurarPorId(id) == null) {
            resultado.success = false;
            resultado.errorMessage = "Unidade Curricular não encontrada no sistema.";
            return resultado;
        }

        if (ucCRUD.eliminarUCPorId(id)) {
            resultado.success = true;
        } else {
            resultado.success = false;
            resultado.errorMessage = "Erro na base de dados ao eliminar a UC.";
        }

        return resultado;
    }

    public Resultado atualizarUCPorId(int id, String novoNome, int novoAno, int novoSemestre, String novaSiglaDocente) {
        Resultado resultado = new Resultado();

        UnidadeCurricular ucExistente = ucCRUD.procurarPorId(id);
        if (ucExistente == null) {
            resultado.success = false;
            resultado.errorMessage = "Unidade Curricular não encontrada na base de dados.";
            return resultado;
        }

        String nomeAtual = ucExistente.getNome();
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
                    UnidadeCurricular temp = new UnidadeCurricular(novoNomeReal, novoAnoReal, novoSemestreReal, docenteEncontrado);
                    docenteEncontrado.adicionarUnidadeCurricular(temp);
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

        if (ucCRUD.atualizarUCPorId(id, ucAtualizada)) {
            resultado.success = true;

            if (!novoNomeReal.equals(nomeAtual) && ucAtualizada.getDocente() != null) {
                DAL.DocenteCRUD docenteCRUD = new DAL.DocenteCRUD();
                model.Docente docenteAfetado = docenteCRUD.procurarPorNif(ucAtualizada.getDocente().getNif());
                if (docenteAfetado != null) {
                    for (model.UnidadeCurricular uc : docenteAfetado.getUnidadesCurriculares()) {
                        if (uc.getNome().equalsIgnoreCase(nomeAtual)) {
                            uc.setNome(novoNomeReal);
                        }
                    }
                    docenteCRUD.atualizarDocente(docenteAfetado);
                }
            }

            if (!novoNomeReal.equals(nomeAtual)) {
                DAL.CursoCRUD cursoCRUD = new DAL.CursoCRUD();
                for (model.Curso curso : cursoCRUD.getCursos()) {
                    boolean modificado = false;
                    for (model.UnidadeCurricular ucDoCurso : curso.getUnidadeCurriculars()) {
                        if (ucDoCurso.getId() == id) {
                            ucDoCurso.setNome(novoNomeReal);
                            modificado = true;
                        }
                    }
                    if (modificado) {
                        cursoCRUD.atualizarCurso(curso.getNome(), curso); // Força a atualização na DB do Curso
                    }
                }
            }

        } else {
            resultado.success = false;
            resultado.errorMessage = "Erro na base de dados ao atualizar a UC.";
        }

        return resultado;
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

        UnidadeCurricular ucAtualizada = new UnidadeCurricular(novoNomeReal, novoAnoReal, novoSemestreReal, novoDocente);

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

                DAL.CursoCRUD cursoCRUD = new DAL.CursoCRUD();
                for (model.Curso curso : cursoCRUD.getCursos()) {
                    boolean modificado = false;
                    for (model.UnidadeCurricular ucDoCurso : curso.getUnidadeCurriculars()) {
                        if (ucDoCurso.getNome().equalsIgnoreCase(nomeAtual)) {
                            ucDoCurso.setNome(novoNome);
                            modificado = true;
                        }
                    }
                    if (modificado) {
                        cursoCRUD.atualizarCurso(curso.getNome(), curso);
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

    public Resultado definirMomentosAvaliacao(int idUc, List<String> momentos) {
        Resultado resultado = new Resultado();

        UnidadeCurricular unidadeCurricular = ucCRUD.procurarPorId(idUc);

        if (unidadeCurricular == null) {
            resultado.success = false;
            resultado.errorMessage = "Unidade Curricular não encontrada.";
            return resultado;
        }
        unidadeCurricular.setMomentosAvaliacao(momentos);

        if (ucCRUD.atualizarUCPorId(idUc, unidadeCurricular)) {
            resultado.success = true;

            if (unidadeCurricular.getDocente() != null) {
                Docente docenteAfetado = docenteCRUD.procurarPorNif(unidadeCurricular.getDocente().getNif());

                if (docenteAfetado != null) {
                    for (UnidadeCurricular ucDoDocente : docenteAfetado.getUnidadesCurriculares()) {
                        if (ucDoDocente.getId() == idUc) {
                            ucDoDocente.setMomentosAvaliacao(momentos);
                            break;
                        }
                    }
                    docenteCRUD.atualizarDocente(docenteAfetado);
                }
            }

        } else {
            resultado.success = false;
            resultado.errorMessage = "Erro ao guardar as alterações na base de dados.";
        }

        return resultado;
    }
}