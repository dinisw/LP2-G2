package controller;

import DAL.CursoCRUD;
import DAL.DocenteCRUD;
import DAL.AvaliacaoCRUD;
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

    public Resultado<UnidadeCurricular> registarUC(String nome, int ano, int semestre, String siglaDocente) {
        if (nome == null || nome.trim().isEmpty()) return new Resultado<>(false, "Nome da UC não pode estar vazio.");
        if (ano < 1 || ano > 3) return new Resultado<>(false, "Ano deve ser 1, 2 ou 3.");
        if (semestre < 1 || semestre > 2) return new Resultado<>(false, "Semestre deve ser 1 ou 2.");
        if (ucCRUD.procurarPorNome(nome) != null) return new Resultado<>(false, "Já existe uma UC com esse nome.");

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

    public List<UnidadeCurricular> listarTodasUCs() { return ucCRUD.getUnidadeCurriculars(); }
    public UnidadeCurricular procurarUCPorNome(String nome) { return ucCRUD.procurarPorNome(nome); }
    public UnidadeCurricular procurarUCPorId(int id) { return ucCRUD.procurarPorId(id); }

    public Resultado<UnidadeCurricular> eliminarUCPorId(int id) {
        UnidadeCurricular ucExistente = ucCRUD.procurarPorId(id);
        if (ucExistente == null) {
            return new Resultado<>(false, "Erro: UC não encontrada.");
        }

        String nomeUC = ucExistente.getNome();

        AvaliacaoCRUD avaliacaoCRUD = new AvaliacaoCRUD();
        if (!avaliacaoCRUD.listarPorUnidadeCurricular(nomeUC).isEmpty()) {
            return new Resultado<>(false, "Bloqueado: Esta UC já possui notas lançadas no sistema. Não pode ser eliminada por razões de histórico escolar.");
        }

        CursoCRUD cursoCRUD = new CursoCRUD();
        boolean estaNumCurso = cursoCRUD.getCursos().stream()
                .anyMatch(c -> c.getUnidadeCurriculars().stream().anyMatch(uc -> uc.getNome().equalsIgnoreCase(nomeUC)));

        if (estaNumCurso) {
            return new Resultado<>(false, "Bloqueado: Esta UC pertence a um ou mais cursos. Tem de a remover dos cursos primeiro.");
        }

        return ucCRUD.eliminarUCPorId(id) ? new Resultado<>(null, true)
                : new Resultado<>(false, "Erro interno ao eliminar UC.");
    }

    public Resultado<UnidadeCurricular> atualizarUC(int id, String novoNome, int novoAno, int novoSemestre, String novaSiglaDocente) {
        UnidadeCurricular ucExistente = ucCRUD.procurarPorId(id);
        if (ucExistente == null) {
            return new Resultado<>(false, "Erro: A Unidade Curricular selecionada não existe.");
        }

        String nomeAntigo = ucExistente.getNome();

        if (novoNome == null || novoNome.trim().isEmpty()) {
            return new Resultado<>(false, "Nome da UC não pode estar vazio.");
        }

        if (!nomeAntigo.equalsIgnoreCase(novoNome)) {
            if (ucCRUD.procurarPorNome(novoNome) != null) {
                return new Resultado<>(false, "Já existe outra UC com esse nome no sistema.");
            }

            AvaliacaoCRUD avaliacaoCRUD = new AvaliacaoCRUD();
            if (!avaliacaoCRUD.listarPorUnidadeCurricular(nomeAntigo).isEmpty()) {
                return new Resultado<>(false, "Bloqueado: Não pode alterar o NOME de uma UC que já tem pautas/notas registadas.");
            }
            CursoCRUD cursoCRUD = new CursoCRUD();
            boolean estaNumCurso = cursoCRUD.getCursos().stream()
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

        Docente novoDocente = docenteCRUD.procurarPorSigla(novaSiglaDocente);
        if (novoDocente == null) {
            return new Resultado<>(false, "Bloqueado: Docente com a sigla '" + novaSiglaDocente + "' não existe no sistema.");
        }

        ucExistente.setNome(novoNome);
        ucExistente.setAnoCurricular(novoAno);
        ucExistente.setSemestre(novoSemestre);
        ucExistente.setDocente(novoDocente);

        return ucCRUD.atualizarUC(novoNome, ucExistente)
                ? new Resultado<>(ucExistente, true)
                : new Resultado<>(false, "Ocorreu um erro na base de dados ao atualizar a UC.");
    }

    public List<UnidadeCurricular> listarUCsPorDocente(String siglaDocente) {
        List<UnidadeCurricular> ucsDoDocente = new ArrayList<>();

        if (siglaDocente == null || siglaDocente.trim().isEmpty()) {
            return ucsDoDocente;
        }

        for (UnidadeCurricular unidadeCurricular : ucCRUD.getUnidadeCurriculars()) {
            if (unidadeCurricular.getDocente() != null && unidadeCurricular.getDocente().getSigla().equalsIgnoreCase(siglaDocente)) {
                ucsDoDocente.add(unidadeCurricular);
            }
        }

        return ucsDoDocente;
    }
}