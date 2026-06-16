package common.utils;

import DAL.DAOFactory;
import model.*;

import java.io.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Exporta todos os dados da base de dados SQL para os ficheiros CSV,
 * respeitando exactamente o formato de cada CRUD.
 *
 * Uso: chamar DbToCsvExporter.exportarTudo() a partir do menu ou de Main.
 */
public class DbToCsvExporter {

    private static final String CSV_DIR = "src/main/java/CSVs/";

    public static void exportarTudo() {
        System.out.println("A exportar dados da BD para CSV...");
        exportarDepartamentos();
        exportarDocentes();
        exportarUCs();
        exportarCursos();
        exportarGestores();
        exportarEstudantes();
        exportarAvaliacoes();
        exportarPropinas();
        System.out.println("Exportação concluída. Ficheiros actualizados em: " + CSV_DIR);
    }

    // ── departamentos.csv: sigla;nome ─────────────────────────────────────────
    private static void exportarDepartamentos() {
        List<Departamento> lista = DAOFactory.getDepartamentoDAO().getDepartamentos();
        escrever("departamentos.csv", lista.stream()
                .map(d -> d.getSigla() + ";" + d.getNome())
                .collect(Collectors.toList()));
        System.out.println("  departamentos.csv — " + lista.size() + " registos");
    }

    // ── docentes.csv: nome;morada;nif;dataNascimento;email;hash;sigla ─────────
    private static void exportarDocentes() {
        List<Docente> lista = DAOFactory.getDocenteDAO().getDocentes();
        escrever("docentes.csv", lista.stream()
                .map(d -> String.join(";",
                        san(d.getNome()), san(d.getMorada()),
                        String.valueOf(d.getNif()),
                        d.getDataNascimento().toString(),
                        san(d.getEmail()), san(d.getHash()), san(d.getSigla())))
                .collect(Collectors.toList()));
        System.out.println("  docentes.csv — " + lista.size() + " registos");
    }

    // ── ucs.csv: id;nome;anoCurricular;semestre;siglaDocente;momentos ─────────
    private static void exportarUCs() {
        List<UnidadeCurricular> lista = DAOFactory.getUnidadeCurricularDAO().getUnidadeCurriculars();
        escrever("ucs.csv", lista.stream()
                .map(uc -> {
                    String siglaDoc = uc.getDocente() != null ? uc.getDocente().getSigla() : "N/A";
                    String momentos = uc.getMomentosAvaliacao() != null
                            ? String.join(",", uc.getMomentosAvaliacao()) : "";
                    return String.join(";",
                            String.valueOf(uc.getId()), san(uc.getNome()),
                            String.valueOf(uc.getAnoCurricular()),
                            String.valueOf(uc.getSemestre()),
                            siglaDoc, momentos);
                })
                .collect(Collectors.toList()));
        System.out.println("  ucs.csv — " + lista.size() + " registos");
    }

    // ── cursos.csv: nome;duracao;siglaDep;precoAnual;anosIniciados;ucs ────────
    private static void exportarCursos() {
        List<Curso> lista = DAOFactory.getCursoDAO().getCursos();
        escrever("cursos.csv", lista.stream()
                .map(c -> {
                    String siglaDep = c.getDepartamento() != null ? c.getDepartamento().getSigla() : "N/A";
                    String anos = (c.getAnosIniciados() == null || c.getAnosIniciados().isEmpty())
                            ? "Nenhum Curso Iniciado"
                            : c.getAnosIniciados().stream().map(String::valueOf).collect(Collectors.joining(","));
                    String ucs = (c.getUnidadeCurriculars() == null || c.getUnidadeCurriculars().isEmpty())
                            ? ""
                            : c.getUnidadeCurriculars().stream().map(UnidadeCurricular::getNome).collect(Collectors.joining(","));
                    String preco = c.getPrecoAnual() != null ? String.format("%.2f", c.getPrecoAnual()) : "";
                    return String.format("%s;%d;%s;%s;%s;%s",
                            san(c.getNome()), c.getDuracao(), siglaDep, preco, anos, ucs);
                })
                .collect(Collectors.toList()));
        System.out.println("  cursos.csv — " + lista.size() + " registos");
    }

    // ── gestores.csv: id;nome;morada;nif;dataNascimento;email;hash;cargo ──────
    private static void exportarGestores() {
        List<Gestor> lista = DAOFactory.getGestorDAO().getGestores();
        escrever("gestores.csv", lista.stream()
                .map(g -> String.join(";",
                        String.valueOf(g.getId()), san(g.getNome()), san(g.getMorada()),
                        String.valueOf(g.getNif()),
                        g.getDataNascimento().toString(),
                        san(g.getEmail()), san(g.getHash()), san(g.getCargo())))
                .collect(Collectors.toList()));
        System.out.println("  gestores.csv — " + lista.size() + " registos");
    }

    // ── estudantes.csv: numeroMec;nome;morada;nif;dataNascimento;email;hash;nomeCurso;ativo;anoLetivo
    private static void exportarEstudantes() {
        List<Estudante> lista = DAOFactory.getEstudanteDAO().getEstudantes();
        escrever("estudantes.csv", lista.stream()
                .map(e -> String.format("%d;%s;%s;%d;%s;%s;%s;%s;%b;%d",
                        e.getNumeroMec(), san(e.getNome()), san(e.getMorada()), e.getNif(),
                        e.getDataNascimento().toString(),
                        san(e.getEmail()), san(e.getHash()),
                        san(e.getNomeCurso()), e.isAtivo(), e.getAnoLetivo()))
                .collect(Collectors.toList()));
        System.out.println("  estudantes.csv — " + lista.size() + " registos");
    }

    // ── avaliacoes.csv: momento;nota;nomeUC;numeroMec ─────────────────────────
    private static void exportarAvaliacoes() {
        List<Avaliacao> todas = new java.util.ArrayList<>();
        for (Estudante e : DAOFactory.getEstudanteDAO().getEstudantes()) {
            List<Avaliacao> av = DAOFactory.getAvaliacaoDAO().listarPorEstudante(e.getNumeroMec());
            if (av != null) todas.addAll(av);
        }
        List<Avaliacao> final_ = todas;
        escrever("avaliacoes.csv", final_.stream()
                .filter(a -> a != null && a.getUnidadeCurricular() != null && a.getEstudante() != null)
                .map(a -> {
                    String notaStr = a.getNota() == null ? "null" : String.valueOf(a.getNota());
                    return String.join(";",
                            san(a.getMomento()), notaStr,
                            san(a.getUnidadeCurricular().getNome()),
                            String.valueOf(a.getEstudante().getNumeroMec()));
                })
                .collect(Collectors.toList()));
        System.out.println("  avaliacoes.csv — " + final_.size() + " registos");
    }

    // ── propinas.csv: numeroMec;anoLetivo;valorTotal;valorPago;historico ──────
    private static void exportarPropinas() {
        List<Propina> lista = DAOFactory.getPropinaDAO().getTodasPropinas();
        escrever("propinas.csv", lista.stream()
                .map(p -> {
                    String historico = (p.getHistoricoPagamentos() != null && !p.getHistoricoPagamentos().isEmpty())
                            ? String.join("|", p.getHistoricoPagamentos()) : "";
                    return String.format("%d;%d;%.2f;%.2f;%s",
                            p.getNumeroMecEstudante(), p.getAnoLetivo(),
                            p.getValorTotal(), p.getValorPago(), historico);
                })
                .collect(Collectors.toList()));
        System.out.println("  propinas.csv — " + lista.size() + " registos");
    }

    // ── utilitários ───────────────────────────────────────────────────────────

    private static void escrever(String nomeFile, List<String> linhas) {
        File ficheiro = new File(CSV_DIR + nomeFile);
        ficheiro.getParentFile().mkdirs();
        try (PrintWriter pw = new PrintWriter(new FileWriter(ficheiro))) {
            for (String linha : linhas) pw.println(linha);
        } catch (IOException e) {
            System.err.println("Erro ao escrever " + nomeFile + ": " + e.getMessage());
        }
    }

    private static String san(String s) {
        return s == null ? "" : s.replace(";", ",");
    }
}
