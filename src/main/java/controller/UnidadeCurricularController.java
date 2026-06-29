package controller;

import DAL.DAOFactory;
import DAL.IAvaliacaoDAO;
import DAL.ICursoDAO;
import DAL.IDocenteDAO;
import DAL.IUnidadeCurricularDAO;
import model.Docente;
import model.Resultado;
import model.UnidadeCurricular;

import java.util.ArrayList;
import java.util.List;

public class UnidadeCurricularController {
    private final IUnidadeCurricularDAO ucDAO;
    private final IDocenteDAO docenteDAO;

    public UnidadeCurricularController() {
        this.ucDAO = DAOFactory.getUnidadeCurricularDAO();
        this.docenteDAO = DAOFactory.getDocenteDAO();
    }

    public Resultado<UnidadeCurricular> registarUC(String nome, int ano, int semestre, String siglaDocente) {
        if (nome == null || nome.trim().isEmpty()) return new Resultado<>(false, "Nome da UC não pode estar vazio.");
        if (ano < 1 || ano > 3) return new Resultado<>(false, "Ano deve ser 1, 2 ou 3.");
        if (semestre < 1 || semestre > 2) return new Resultado<>(false, "Semestre deve ser 1 ou 2.");
        if (ucDAO.procurarPorNome(nome) != null) return new Resultado<>(false, "Já existe uma UC com esse nome.");
        if (siglaDocente == null || siglaDocente.trim().isEmpty()) {
            return new Resultado<>(false, "Bloqueado: É obrigatório associar um docente à Unidade Curricular.");
        }

        Docente docente = docenteDAO.procurarPorSigla(siglaDocente);
        if (docente == null) {
            return new Resultado<>(false, "Bloqueado: Docente com a sigla '" + siglaDocente + "' não existe no sistema. Crie o docente primeiro.");
        }
        if (!docente.isAtivo()) {
            return new Resultado<>(false, "Bloqueado: O docente '" + docente.getNome() + "' está inativo. Ative a conta primeiro.");
        }

        UnidadeCurricular novaUC = new UnidadeCurricular(nome, ano, semestre, docente);
        return ucDAO.registarUC(novaUC)
                ? new Resultado<>(novaUC, true)
                : new Resultado<>(false, "Ocorreu um erro na base de dados ao registar a UC.");
    }

    public List<UnidadeCurricular> listarTodasUCs() { return ucDAO.getUnidadeCurriculars(); }
    public UnidadeCurricular procurarUCPorNome(String nome) { return ucDAO.procurarPorNome(nome); }
    public UnidadeCurricular procurarUCPorId(int id) { return ucDAO.procurarPorId(id); }

    public Resultado<UnidadeCurricular> eliminarUCPorId(int id) {
        UnidadeCurricular ucExistente = ucDAO.procurarPorId(id);
        if (ucExistente == null) return new Resultado<>(false, "Erro: UC não encontrada.");

        String nomeUC = ucExistente.getNome();

        IAvaliacaoDAO avaliacaoDAO = DAOFactory.getAvaliacaoDAO();
        if (!avaliacaoDAO.listarPorUnidadeCurricular(nomeUC).isEmpty()) {
            return new Resultado<>(false, "Bloqueado: Esta UC já possui notas lançadas no sistema. Não pode ser eliminada por razões de histórico escolar.");
        }

        ICursoDAO cursoDAO = DAOFactory.getCursoDAO();
        boolean estaNumCurso = cursoDAO.getCursos().stream()
                .anyMatch(c -> c.getUnidadeCurriculars().stream().anyMatch(uc -> uc.getNome().equalsIgnoreCase(nomeUC)));

        if (estaNumCurso) {
            return new Resultado<>(false, "Bloqueado: Esta UC pertence a um ou mais cursos. Tem de a remover dos cursos primeiro.");
        }

        return ucDAO.eliminarUCPorId(id)
                ? new Resultado<>(null, true)
                : new Resultado<>(false, "Erro interno ao eliminar UC.");
    }

    public Resultado<UnidadeCurricular> atualizarUC(int id, String novoNome, int novoAno, int novoSemestre, String novaSiglaDocente) {
        UnidadeCurricular ucExistente = ucDAO.procurarPorId(id);
        if (ucExistente == null) return new Resultado<>(false, "Erro: A Unidade Curricular selecionada não existe.");

        String nomeAntigo = ucExistente.getNome();
        if (novoNome == null || novoNome.trim().isEmpty()) return new Resultado<>(false, "Nome da UC não pode estar vazio.");

        if (!nomeAntigo.equalsIgnoreCase(novoNome)) {
            if (ucDAO.procurarPorNome(novoNome) != null) {
                return new Resultado<>(false, "Já existe outra UC com esse nome no sistema.");
            }

            IAvaliacaoDAO avaliacaoDAO = DAOFactory.getAvaliacaoDAO();
            if (!avaliacaoDAO.listarPorUnidadeCurricular(nomeAntigo).isEmpty()) {
                return new Resultado<>(false, "Bloqueado: Não pode alterar o NOME de uma UC que já tem pautas/notas registadas.");
            }

            ICursoDAO cursoDAO = DAOFactory.getCursoDAO();
            boolean estaNumCurso = cursoDAO.getCursos().stream()
                    .anyMatch(c -> c.getUnidadeCurriculars().stream().anyMatch(uc -> uc.getNome().equalsIgnoreCase(nomeAntigo)));
            if (estaNumCurso) {
                return new Resultado<>(false, "Bloqueado: Não pode alterar o NOME de uma UC que está associada a um Curso. (Dica: Remova do curso primeiro).");
            }
        }

        if (novoAno < 1 || novoAno > 3) return new Resultado<>(false, "Ano deve ser 1, 2 ou 3.");
        if (novoSemestre < 1 || novoSemestre > 2) return new Resultado<>(false, "Semestre deve ser 1 ou 2.");
        if (novaSiglaDocente == null || novaSiglaDocente.trim().isEmpty()) {
            return new Resultado<>(false, "Bloqueado: É obrigatório associar um docente à Unidade Curricular.");
        }

        Docente novoDocente = docenteDAO.procurarPorSigla(novaSiglaDocente);
        if (novoDocente == null) {
            return new Resultado<>(false, "Bloqueado: Docente com a sigla '" + novaSiglaDocente + "' não existe no sistema.");
        }
        if (!novoDocente.isAtivo()) {
            return new Resultado<>(false, "Bloqueado: O docente '" + novoDocente.getNome() + "' está inativo. Ative a conta primeiro.");
        }

        ucExistente.setNome(novoNome);
        ucExistente.setAnoCurricular(novoAno);
        ucExistente.setSemestre(novoSemestre);
        ucExistente.setDocente(novoDocente);

        return ucDAO.atualizarUCPorId(id, ucExistente)
                ? new Resultado<>(ucExistente, true)
                : new Resultado<>(false, "Ocorreu um erro na base de dados ao atualizar a UC.");
    }

    public List<UnidadeCurricular> listarUCsPorDocente(String siglaDocente) {
        List<UnidadeCurricular> ucsDoDocente = new ArrayList<>();
        if (siglaDocente == null || siglaDocente.trim().isEmpty()) return ucsDoDocente;

        for (UnidadeCurricular uc : ucDAO.getUnidadeCurriculars()) {
            if (uc.getDocente() != null && uc.getDocente().getSigla().equalsIgnoreCase(siglaDocente)) {
                ucsDoDocente.add(uc);
            }
        }
        return ucsDoDocente;
    }
}
