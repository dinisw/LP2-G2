package controller;

import DAL.*;
import model.*;

import java.util.ArrayList;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AnoLetivoController {

    private final IAnoLetivoDAO anoLetivoDAO;
    private final ICursoDAO cursoDAO;
    private final IEstudanteDAO estudanteDAO;
    private final IAvaliacaoDAO avaliacaoDAO;
    private final IDocenteDAO docenteDAO;
    private final PropinaController propinaController;

    public AnoLetivoController() {
        this.anoLetivoDAO = DAOFactory.getAnoLetivoDAO();
        this.cursoDAO = DAOFactory.getCursoDAO();
        this.estudanteDAO = DAOFactory.getEstudanteDAO();
        this.avaliacaoDAO = DAOFactory.getAvaliacaoDAO();
        this.docenteDAO = DAOFactory.getDocenteDAO();
        this.propinaController = new PropinaController();
    }

    // ── Obter / criar ano atual ────────────────────────────────

    /** Retorna true se as tabelas AnoLetivo existem na BD. False = script não foi executado. */
    public boolean bdPreparada() {
        return anoLetivoDAO.tabelasExistem();
    }

    public AnoLetivo obterOuCriarAnoAtual() {
        if (!bdPreparada()) return null;
        AnoLetivo atual = anoLetivoDAO.obterAnoAtual();
        if (atual == null) {
            int mes = LocalDate.now().getMonthValue();
            int anoInicio = mes >= 9 ? LocalDate.now().getYear() : LocalDate.now().getYear() - 1;
            AnoLetivo novo = new AnoLetivo(anoInicio, LocalDate.now());
            anoLetivoDAO.registarAnoLetivo(novo);
            return anoLetivoDAO.obterAnoAtual();
        }
        return atual;
    }

    public AnoLetivo obterAnoAtual() {
        if (!bdPreparada()) return null;
        return anoLetivoDAO.obterAnoAtual();
    }

    public List<AnoLetivo> listarTodos() {
        if (!bdPreparada()) return new ArrayList<>();
        return anoLetivoDAO.listarTodos();
    }

    public AnoLetivo buscarPorAno(int anoCalendario) {
        if (!bdPreparada()) return null;
        return anoLetivoDAO.obterPorAnoCalendario(anoCalendario);
    }

    // ── Validação para avançar o ano ──────────────────────────

    /**
     * Verifica todas as condições necessárias para avançar o ano letivo.
     * Retorna lista vazia (sucesso=true) se pode avançar.
     * Retorna lista de bloqueios (sucesso=false) se não pode.
     */
    public Resultado<List<String>> verificarCondicioesSaltoDeAno() {
        List<String> bloqueios = new ArrayList<>();

        List<Curso> cursos = cursoDAO.getCursos();

        // Verifica se existe pelo menos um curso com actividade letiva iniciada
        boolean existeCursoIniciado = cursos.stream()
                .anyMatch(c -> c.getAnosIniciados() != null && !c.getAnosIniciados().isEmpty());

        if (!existeCursoIniciado) {
            bloqueios.add("[SEM ACTIVIDADE] Nenhum curso foi ainda iniciado. "
                    + "Use 'Gerir Cursos > Iniciar Ano Letivo' antes de avançar o ano letivo global.");
            return new Resultado<>(bloqueios, false);
        }

        for (Curso curso : cursos) {
            if (curso.getAnosIniciados() == null || curso.getAnosIniciados().isEmpty()) continue;

            List<Estudante> estudantesDoCurso = estudanteDAO.getEstudantes().stream()
                    .filter(e -> e.isAtivo() && curso.getNome().equalsIgnoreCase(e.getNomeCurso()))
                    .toList();

            if (estudantesDoCurso.isEmpty()) continue;

            if (curso.getUnidadeCurriculars() == null || curso.getUnidadeCurriculars().isEmpty()) {
                bloqueios.add("[SEM UCS] Curso '" + curso.getNome()
                        + "' está iniciado mas não tem UCs associadas — impossível avaliar aproveitamento.");
                continue;
            }

            for (Estudante estudante : estudantesDoCurso) {
                int anoEstudante = estudante.getAnoLetivo();

                if (!propinaController.isPropinaPaga(estudante.getNumeroMec(), anoEstudante)) {
                    bloqueios.add("[PROPINA] " + estudante.getNome()
                            + " (Nº " + estudante.getNumeroMec() + ")"
                            + " | Curso: " + curso.getNome()
                            + " | " + anoEstudante + "º Ano — propina não paga");
                }

                List<Avaliacao> avaliacoes = avaliacaoDAO.listarPorEstudante(estudante.getNumeroMec());

                for (UnidadeCurricular uc : curso.getUnidadeCurriculars()) {
                    if (uc.getAnoCurricular() != anoEstudante) continue;
                    if (uc.getMomentosAvaliacao() == null || uc.getMomentosAvaliacao().isEmpty()) continue;

                    for (String momento : uc.getMomentosAvaliacao()) {
                        boolean temNota = avaliacoes.stream().anyMatch(a ->
                                a.getUnidadeCurricular() != null
                                        && a.getUnidadeCurricular().getNome().equalsIgnoreCase(uc.getNome())
                                        && a.getMomento().equalsIgnoreCase(momento)
                                        && a.getNota() != null);

                        if (!temNota) {
                            bloqueios.add("[NOTA EM FALTA] " + estudante.getNome()
                                    + " (Nº " + estudante.getNumeroMec() + ")"
                                    + " | UC: " + uc.getNome()
                                    + " | Momento: " + momento);
                        }
                    }
                }
            }
        }

        return new Resultado<>(bloqueios, bloqueios.isEmpty());
    }

    // ── Avançar o ano e gravar snapshot completo ──────────────

    /**
     * Conclui o ano atual, grava snapshot histórico completo e inicia o próximo ano.
     * Deve ser chamado APÓS validação bem-sucedida.
     */
    public boolean avancarAnoLetivo() {
        AnoLetivo atual = anoLetivoDAO.obterAnoAtual();
        int proximoAno;

        if (atual != null) {
            gravarSnapshotAno(atual);
            atual.setDataFim(LocalDate.now());
            atual.setEstado("CONCLUIDO");
            anoLetivoDAO.atualizarAnoLetivo(atual);
            proximoAno = atual.getAnoCalendario() + 1;
        } else {
            int mes = LocalDate.now().getMonthValue();
            proximoAno = mes >= 9 ? LocalDate.now().getYear() : LocalDate.now().getYear() - 1;
        }

        AnoLetivo novo = new AnoLetivo(proximoAno, LocalDate.now());
        return anoLetivoDAO.registarAnoLetivo(novo);
    }

    private void gravarSnapshotAno(AnoLetivo anoLetivo) {
        List<Curso> cursos = cursoDAO.getCursos();
        List<Estudante> todosEstudantes = estudanteDAO.getEstudantes();

        for (Curso curso : cursos) {
            // Resolve cursoId (pode não existir em CSV)
            Integer cursoId = null;
            try { cursoId = resolverCursoId(curso.getNome()); } catch (Exception ignored) {}

            boolean iniciado = curso.getAnosIniciados() != null && !curso.getAnosIniciados().isEmpty();
            AnoLetivoCursoSnapshot cursoSnap = new AnoLetivoCursoSnapshot(
                    anoLetivo.getId(), cursoId, curso.getNome(),
                    iniciado ? "INICIADO" : "NAO_INICIADO");
            int cursoSnapId = anoLetivoDAO.salvarCursoSnapshot(cursoSnap);
            if (cursoSnapId <= 0) continue;

            // UCs
            if (curso.getUnidadeCurriculars() != null) {
                for (UnidadeCurricular uc : curso.getUnidadeCurriculars()) {
                    String docNome = uc.getDocente() != null ? uc.getDocente().getNome() : null;
                    String docSigla = uc.getDocente() != null ? uc.getDocente().getSigla() : null;
                    String momentos = uc.getMomentosAvaliacao() != null
                            ? String.join(",", uc.getMomentosAvaliacao()) : null;
                    AnoLetivoUCSnapshot ucSnap = new AnoLetivoUCSnapshot(
                            cursoSnapId, uc.getNome(), uc.getAnoCurricular(), docNome, docSigla, momentos);
                    anoLetivoDAO.salvarUCSnapshot(ucSnap);
                }
            }

            // Estudantes
            List<Estudante> estudantesDoCurso = todosEstudantes.stream()
                    .filter(e -> curso.getNome().equalsIgnoreCase(e.getNomeCurso()))
                    .toList();

            for (Estudante estudante : estudantesDoCurso) {
                int anoInicio = estudante.getAnoLetivo();
                List<Avaliacao> avaliacoes = avaliacaoDAO.listarPorEstudante(estudante.getNumeroMec());
                estudante.setListaAvaliacoes(avaliacoes);

                // Calcular ano final após transição
                int anoFim = BLL.EstudanteCalculo.calcularAnoDesbloqueado(estudante, curso);
                if (anoFim >= 2 && !propinaController.isPropinaPaga(estudante.getNumeroMec(), 1)) anoFim = 1;
                else if (anoFim == 3 && !propinaController.isPropinaPaga(estudante.getNumeroMec(), 2)) anoFim = 2;

                // Resultado
                String resultado;
                if (BLL.EstudanteCalculo.isCursoConcluido(estudante, curso)
                        && todosPropinasPagesPorCurso(estudante.getNumeroMec(), curso.getDuracao())) {
                    resultado = "CONCLUIDO_CURSO";
                } else if (anoFim > anoInicio) {
                    resultado = "TRANSICAO";
                } else {
                    resultado = "RETIDO";
                }

                // Propina
                List<Propina> propinas = propinaController.consultarPropinasEstudante(estudante.getNumeroMec());
                BigDecimal total = BigDecimal.ZERO, pago = BigDecimal.ZERO;
                for (Propina p : propinas) {
                    if (p.getAnoLetivo() == anoInicio) {
                        total = p.getValorTotal();
                        pago = p.getValorPago();
                    }
                }

                AnoLetivoEstudanteSnapshot estSnap = new AnoLetivoEstudanteSnapshot(
                        cursoSnapId, estudante.getNumeroMec(), estudante.getNome(),
                        anoInicio, anoFim, total, pago, resultado);
                int estSnapId = anoLetivoDAO.salvarEstudanteSnapshot(estSnap);
                if (estSnapId <= 0) continue;

                // Notas
                for (Avaliacao av : avaliacoes) {
                    if (av.getUnidadeCurricular() == null) continue;
                    AnoLetivoNotaSnapshot notaSnap = new AnoLetivoNotaSnapshot(
                            estSnapId,
                            av.getUnidadeCurricular().getNome(),
                            av.getMomento(),
                            av.getNota());
                    anoLetivoDAO.salvarNotaSnapshot(notaSnap);
                }
            }
        }
    }

    private boolean todosPropinasPagesPorCurso(int numeroMec, int duracao) {
        for (int i = 1; i <= duracao; i++) {
            if (!propinaController.isPropinaPaga(numeroMec, i)) return false;
        }
        return true;
    }

    /**
     * Tenta obter o id do curso via SQL. Retorna null em modo CSV ou se não encontrado.
     * O snapshot usa nomeSnapshot como identificador principal, pelo que null é seguro.
     */
    private Integer resolverCursoId(String nomeCurso) {
        if (!DAOFactory.isSql()) return null;
        try {
            ArrayList<Integer> ids = new DAL.DB.DatabaseConnection().select(
                    "SELECT id FROM Curso WHERE nome=?",
                    rs -> rs.getInt("id"), nomeCurso);
            return ids.isEmpty() ? null : ids.get(0);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Ponto de entrada único para a passagem de ano letivo.
     * Executa: verificação → snapshot → conclusão do ano → transição dos estudantes.
     * Retorna um relatório completo com o resultado de cada estudante.
     */
    public Resultado<List<String>> executarPassagemDeAno() {
        // 1. Verificar condições
        Resultado<List<String>> condicoes = verificarCondicioesSaltoDeAno();
        if (!condicoes.sucesso) {
            return new Resultado<>(false, "Existem bloqueios que impedem a passagem de ano:\n"
                    + (condicoes.dados != null ? String.join("\n", condicoes.dados) : ""));
        }

        // 2. Gravar snapshot + concluir ano atual + criar novo ano
        boolean avancouAno = avancarAnoLetivo();
        if (!avancouAno) {
            return new Resultado<>(false, "Falha ao avançar o ano letivo (snapshot/registo).");
        }

        // 3. Transição curricular dos estudantes
        EstudanteController ec = new EstudanteController();
        Resultado<List<String>> transicao = ec.simularTransicaoAnoLetivoGlobal();

        List<String> relatorioFinal = new java.util.ArrayList<>();
        relatorioFinal.add("Ano letivo concluído com sucesso.");
        if (transicao.dados != null) relatorioFinal.addAll(transicao.dados);

        return new Resultado<>(relatorioFinal, true);
    }

    // ── Relatórios ─────────────────────────────────────────────

    public List<String> gerarRelatorioAnoAtual() {
        List<String> linhas = new ArrayList<>();
        List<Curso> cursos = cursoDAO.getCursos();

        if (cursos.isEmpty()) {
            linhas.add("Não existem cursos registados no sistema.");
            return linhas;
        }

        List<Estudante> todosEstudantes = estudanteDAO.getEstudantes();
        List<Docente> todosDocentes = docenteDAO.getDocentes();

        for (Curso curso : cursos) {
            boolean iniciado = curso.getAnosIniciados() != null && !curso.getAnosIniciados().isEmpty();
            String statusCurso = iniciado
                    ? "INICIADO (anos: " + curso.getAnosIniciados() + ")"
                    : "NÃO INICIADO";

            linhas.add("══ CURSO: " + curso.getNome() + " [" + statusCurso + "] ══");

            List<Estudante> alunos = todosEstudantes.stream()
                    .filter(e -> curso.getNome().equalsIgnoreCase(e.getNomeCurso()))
                    .toList();

            linhas.add("  Alunos inscritos: " + alunos.size() + " (" + alunos.stream().filter(Estudante::isAtivo).count() + " ativos)");
            for (Estudante e : alunos) {
                String status = e.isAtivo() ? "Ativo" : "Inativo";
                linhas.add("    • " + e.getNome() + " (Nº " + e.getNumeroMec() + ") — "
                        + e.getAnoLetivo() + "º Ano — " + status);
            }

            if (curso.getUnidadeCurriculars() != null && !curso.getUnidadeCurriculars().isEmpty()) {
                linhas.add("  Unidades Curriculares:");
                for (UnidadeCurricular uc : curso.getUnidadeCurriculars()) {
                    String docenteStr = (uc.getDocente() != null)
                            ? uc.getDocente().getNome() + " (" + uc.getDocente().getSigla() + ")"
                            : "Sem docente";
                    String momentos = (uc.getMomentosAvaliacao() != null && !uc.getMomentosAvaliacao().isEmpty())
                            ? String.join(", ", uc.getMomentosAvaliacao())
                            : "Sem momentos definidos";
                    linhas.add("    [" + uc.getAnoCurricular() + "º Ano] " + uc.getNome()
                            + " | " + docenteStr + " | Momentos: " + momentos);
                }
            } else {
                linhas.add("  Sem Unidades Curriculares associadas.");
            }
            linhas.add("");
        }

        linhas.add("── Docentes no sistema: " + todosDocentes.size() + " ──");
        for (Docente d : todosDocentes) {
            linhas.add("  • " + d.getNome() + " (" + d.getSigla() + ") — " + (d.isAtivo() ? "Ativo" : "Inativo"));
        }

        return linhas;
    }

    /**
     * Para anos concluídos: lê o snapshot guardado.
     * Para o ano atual: lê dados em tempo real.
     */
    public List<String> gerarRelatorioAnoPorCalendario(int anoCalendario) {
        List<String> linhas = new ArrayList<>();
        AnoLetivo ano = anoLetivoDAO.obterPorAnoCalendario(anoCalendario);

        if (ano == null) {
            linhas.add("Ano letivo " + anoCalendario + "/" + (anoCalendario + 1) + " não encontrado.");
            return linhas;
        }

        // Se for o ano atual, usa dados em tempo real
        if (ano.isAtivo()) return gerarRelatorioAnoAtual();

        // Lê snapshot histórico
        List<AnoLetivoCursoSnapshot> cursosSnap = anoLetivoDAO.obterCursosSnapshot(ano.getId());
        if (cursosSnap.isEmpty()) {
            linhas.add("Sem dados de snapshot para este ano letivo.");
            linhas.add("(Este ano foi criado antes de a funcionalidade de snapshot estar disponível)");
            return linhas;
        }

        for (AnoLetivoCursoSnapshot cursoSnap : cursosSnap) {
            linhas.add("══ CURSO: " + cursoSnap.getNomeSnapshot() + " [" + cursoSnap.getEstadoCurso() + "] ══");

            List<AnoLetivoUCSnapshot> ucs = anoLetivoDAO.obterUCsSnapshot(cursoSnap.getId());
            if (!ucs.isEmpty()) {
                linhas.add("  Unidades Curriculares:");
                for (AnoLetivoUCSnapshot uc : ucs) {
                    String docStr = uc.getDocenteNome() != null
                            ? uc.getDocenteNome() + " (" + uc.getDocenteSigla() + ")"
                            : "Sem docente";
                    String momentos = uc.getMomentos() != null ? uc.getMomentos() : "Sem momentos";
                    linhas.add("    [" + uc.getAnoCurricular() + "º Ano] " + uc.getNomeUC()
                            + " | " + docStr + " | Momentos: " + momentos);
                }
            }

            List<AnoLetivoEstudanteSnapshot> estudantes = anoLetivoDAO.obterEstudantesSnapshot(cursoSnap.getId());
            linhas.add("  Alunos: " + estudantes.size());
            for (AnoLetivoEstudanteSnapshot est : estudantes) {
                String propStr = est.isPropinaPaga()
                        ? "Propina Paga"
                        : "Dívida: " + String.format("%.2f€", est.getValorEmDivida());
                String res = switch (est.getResultado()) {
                    case "TRANSICAO" -> "→ Transitou para " + est.getAnoCurricularFim() + "º Ano";
                    case "CONCLUIDO_CURSO" -> "✓ Concluiu o curso";
                    default -> "✗ Retido no " + est.getAnoCurricularInicio() + "º Ano";
                };
                linhas.add("    • " + est.getNomeSnapshot() + " (Nº " + est.getNumeroMec() + ")"
                        + " | " + propStr + " | " + res);

                List<AnoLetivoNotaSnapshot> notas = anoLetivoDAO.obterNotasSnapshot(est.getId());
                if (!notas.isEmpty()) {
                    for (AnoLetivoNotaSnapshot nota : notas) {
                        String notaStr = nota.getNota() != null
                                ? String.format("%.1f", nota.getNota())
                                : "Sem nota";
                        linhas.add("        » " + nota.getNomeUC() + " [" + nota.getMomento() + "]: " + notaStr);
                    }
                }
            }
            linhas.add("");
        }

        return linhas;
    }
}
