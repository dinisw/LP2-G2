package controller;

import DAL.CursoCRUD;
import DAL.DocenteCRUD;
import DAL.UnidadeCurricularCRUD;
import model.Curso;
import model.Docente;
import model.UnidadeCurricular;
import view.UnidadeCurricularView;

import java.util.List;
import java.util.Scanner;

public class UnidadeCurricularController {
    private UnidadeCurricularCRUD ucCRUD;
    private UnidadeCurricularView view;
    private DocenteCRUD docenteCRUD;
    private CursoCRUD cursoCRUD;

    public UnidadeCurricularController() {
        this.ucCRUD = new UnidadeCurricularCRUD();
        this.view = new UnidadeCurricularView();
        this.docenteCRUD = new DocenteCRUD();
        this.cursoCRUD = new CursoCRUD();
    }

    public void registarNovaUC() {
        String nome = view.solicitarNome();
        if (ucCRUD.procurarPorNome(nome) != null) {
            view.mostrarMensagem("Erro: Já existe uma UC com esse nome!");
            return;
        }

        int ano = view.solicitarAno();
        if (ano <= 0) {
            view.mostrarMensagem("Erro: Ano curricular inválido!");
            return;
        }

        // Listar e selecionar docente
        List<Docente> docentes = docenteCRUD.getDocentes();
        if (docentes.isEmpty()) {
            view.mostrarMensagem("Erro: Não existem docentes registados no sistema.");
            return;
        }
        view.listarDocentesDisponiveis(docentes);
        
        String siglaDocente = view.solicitarSiglaDocente();
        Docente docente = docenteCRUD.procurarPorSigla(siglaDocente);
        
        if (docente == null) {
            view.mostrarMensagem("Erro: Docente não encontrado!");
            return;
        }

        // Criar a UC
        UnidadeCurricular novaUC = new UnidadeCurricular(nome, ano, docente);
        
        // Tentar registar no DAL (unicidade de docente responsável por ESTA UC é garantida pelo modelo de 1 para 1 no objeto)
        if (ucCRUD.registarUC(novaUC)) {
            view.mostrarMensagem("UC registada com sucesso!");
            
            // Associar a cursos
            boolean continuarAssociando = true;
            while (continuarAssociando) {
                String nomeCurso = view.solicitarNomeCurso();
                if (nomeCurso.isEmpty()) {
                    continuarAssociando = false;
                } else {
                    Curso curso = cursoCRUD.procurarPorNome(nomeCurso);
                    if (curso != null) {
                        if (curso.adicionarUnidadeCurricular(novaUC)) {
                            // Salvar alteração no curso
                            cursoCRUD.atualizarCurso(curso.getNome(), curso);
                            view.mostrarMensagem("UC associada ao curso " + curso.getNome() + " com sucesso!");
                        } else {
                            view.mostrarMensagem("Bloqueado: O curso " + curso.getNome() + " já atingiu o limite de 5 UCs para o ano " + ano + ".");
                        }
                    } else {
                        view.mostrarMensagem("Erro: Curso não encontrado!");
                    }
                }
            }
            
            // Mostrar resumo
            view.imprimirDadosUnidadeCurricular(novaUC);
        } else {
            view.mostrarMensagem("Erro ao registar a UC no sistema.");
        }
    }
    
    public void listarTodasUCs() {
        List<UnidadeCurricular> ucs = ucCRUD.getUcs();
        if (ucs.isEmpty()) {
            view.mostrarMensagem("Não há UCs registadas.");
        } else {
            for (UnidadeCurricular uc : ucs) {
                view.imprimirDadosUnidadeCurricular(uc);
            }
        }
    }

    public void listarUCsPorDocente(String siglaDocente) {
        List<UnidadeCurricular> ucs = ucCRUD.getUcs();
        boolean encontrou = false;
        for (UnidadeCurricular uc : ucs) {
            if (uc.getDocente() != null && uc.getDocente().getSigla().equalsIgnoreCase(siglaDocente)) {
                view.imprimirDadosUnidadeCurricular(uc);
                encontrou = true;
            }
        }
        if (!encontrou) {
            view.mostrarMensagem("Não é responsável por nenhuma Unidade Curricular.");
        }
    }

    public void exibirMenuGestaoUCs() {
        Scanner scanner = new Scanner(System.in);
        String opcao;
        do {
            System.out.println("\n--- GESTÃO DE UNIDADES CURRICULARES ---");
            System.out.println("1. Registar Nova UC");
            System.out.println("2. Listar Todas as UCs");
            System.out.println("0. Voltar");
            System.out.print("Selecione uma opção: ");
            opcao = scanner.nextLine().trim();

            switch (opcao) {
                case "1": registarNovaUC(); break;
                case "2": listarTodasUCs(); break;
                case "0": break;
                default: System.out.println("Opção inválida!");
            }
        } while (!opcao.equals("0"));
    }
}
