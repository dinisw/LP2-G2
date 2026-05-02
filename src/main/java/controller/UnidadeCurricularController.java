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

    public Resultado<UnidadeCurricular> atualizarUC(int id, String novoNome, int novoAno, int novoSemestre, String novaSiglaDocente) {
        // 1. Verificar se a UC que queremos atualizar realmente existe
        UnidadeCurricular ucExistente = ucCRUD.procurarPorId(id);
        if (ucExistente == null) {
            return new Resultado<>(false, "Erro: A Unidade Curricular selecionada não existe.");
        }

        // 2. Validar o novo nome
        if (novoNome == null || novoNome.trim().isEmpty()) {
            return new Resultado<>(false, "Nome da UC não pode estar vazio.");
        }

        // Se o nome foi alterado, verificar se já existe outra UC a usar o novo nome
        if (!ucExistente.getNome().equalsIgnoreCase(novoNome)) {
            if (ucCRUD.procurarPorNome(novoNome) != null) {
                return new Resultado<>(false, "Já existe outra UC com esse nome no sistema.");
            }
        }

        // 3. Validar novo ano e semestre
        if (novoAno < 1 || novoAno > 3) return new Resultado<>(false, "Ano deve ser 1, 2 ou 3.");
        if (novoSemestre < 1 || novoSemestre > 2) return new Resultado<>(false, "Semestre deve ser 1 ou 2.");

        // 4. Validar e associar o novo Docente (Regra de Negócio)
        if (novaSiglaDocente == null || novaSiglaDocente.trim().isEmpty()) {
            return new Resultado<>(false, "Bloqueado: É obrigatório associar um docente à Unidade Curricular.");
        }

        Docente novoDocente = docenteCRUD.procurarPorSigla(novaSiglaDocente);
        if (novoDocente == null) {
            return new Resultado<>(false, "Bloqueado: Docente com a sigla '" + novaSiglaDocente + "' não existe no sistema.");
        }

        // 5. Criar a instância atualizada
        UnidadeCurricular ucAtualizada = new UnidadeCurricular(novoNome, novoAno, novoSemestre, novoDocente);

        // Dependendo de como tens o teu model, poderás ter de preservar o ID antigo na nova instância.
        // Se a tua classe UnidadeCurricular tiver um setId(int id), descomenta a linha abaixo:
        // ucAtualizada.setId(id);

        // 6. Enviar para a camada CRUD
        // NOTA: Certifica-te de que tens um método no UnidadeCurricularCRUD semelhante a: atualizarUC(int id, UnidadeCurricular uc)
        return ucCRUD.atualizarUC(novoNome, ucAtualizada)
                ? new Resultado<>(ucAtualizada, true)
                : new Resultado<>(false, "Ocorreu um erro na base de dados ao atualizar a UC.");
    }
}