package view;

import DAL.CursoCRUD;
import common.utils.BackendUtils;
import common.utils.MenuUtils;
import controller.TurmaController;
import controller.UnidadeCurricularController;
import model.Curso;
import model.Turma;
import model.UnidadeCurricular;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static common.utils.DesignUtils.*;
import static common.utils.DesignUtils.GetRed;
import static common.utils.DesignUtils.GetReset;

public class TurmaView{

    private TurmaController turmaController = null;
    private final Scanner scanner;


    public TurmaView() {
        this.turmaController = turmaController;
        this.scanner = new Scanner(System.in);
    }

    public void exiberMenuTurma(){
        String opcao;
        ArrayList<String> opcoes = new ArrayList<>();
        opcoes.add("1. Criar Turma");
        opcoes.add("2. Consultar Turma");
        opcoes.add("3. Listar Turmas");
        opcoes.add("4. Adicionar UC a uma Turma");
        opcoes.add("0. Voltar ao Menu de Gestão");

        do{
            try{
                MenuUtils.exibirSubTitulo("GESTÃO DE TURMAS", opcoes);
                System.out.println("\n" + GetWhiteBold() + "Selecione uma opção: " + GetReset());
                opcao = scanner.nextLine();

                switch (opcao) {
                    case "1":
                        criarTurma();
                        break;
                    case "2":
                        consultarTurma();
                        break;
                    case "3":
                        listarTurmas();
                        break;
                    case "4":
                        adicionarUC();
                        break;
                    case "0":
                        System.out.println(GetYellow() + "\nA voltar ao menu de gestão..." + GetReset());
                        return;
                    default:
                        System.out.println(GetRed() + "Opção inválida. Por favor, escolha uma opção da lista." + GetReset());
                        MenuUtils.pressionarEnter(scanner);
                }
            }catch (Exception e){
                System.out.println(GetRed() + "Ocorreu um erro na navegação: " + e.getMessage() + GetReset());
                MenuUtils.pressionarEnter(scanner);
            }
        } while (true);
    }

    private Curso obterCursoInterativo() {
        CursoCRUD cursoCRUD = new CursoCRUD();
        List<Curso> cursos = cursoCRUD.getCursos();

        if (cursos.isEmpty()) {
            System.out.println(GetYellow() + "Não existem cursos registados." + GetReset());
            //perguntar se a pessoa quer criar um curso e redirecionar
            return null;
        }

        System.out.println("\nCursos Disponíveis:");
        for (int i = 0; i < cursos.size(); i++) {
            System.out.printf("%d - %s\n", i + 1, cursos.get(i).getNome());
        }

        int cursoID = Integer.parseInt(BackendUtils.lerInputString(scanner, "Selecione o ID do Curso: "));
        if (cursoID >= 1 && cursoID <= cursos.size()) {
            return cursos.get(cursoID - 1);
        }
        System.out.println(GetRed() + "Curso inválido." + GetReset());
        return null;
    }

    private void criarTurma() {
        System.out.println(GetBlue() + "\n--- CRIAR TURMA ---" + GetReset());
        Curso curso = obterCursoInterativo();
        if (curso == null) return;

        //faltam validações
        int ano = Integer.parseInt(BackendUtils.lerInputString(scanner, "Ano Curricular (1 a 3): "));
        String anoLetivo = BackendUtils.lerInputString(scanner, "Ano Letivo (ex: 2025/2026): ");

        try {
            //da erro aqui
            turmaController.criarTurma(curso, ano, anoLetivo);
            System.out.println(GetGreen() + "Turma criada com sucesso!" + GetReset());
        } catch (Exception e) {
            System.out.println(GetRed() + "Erro: " + e.getMessage() + GetReset());
        }
        MenuUtils.pressionarEnter(scanner);
    }

    private void consultarTurma() {
        System.out.println(GetBlue() + "\n--- CONSULTAR TURMA ---" + GetReset());
        Curso curso = obterCursoInterativo();
        if (curso == null) return;

        int ano = Integer.parseInt(BackendUtils.lerInputString(scanner, "Ano Curricular: "));
        String anoLetivo = BackendUtils.lerInputString(scanner, "Ano Letivo (ex: 2025/2026): ");

        Turma turma = turmaController.obterTurma(curso, ano, anoLetivo);
        if (turma != null) {
            System.out.println(GetGreen() + "\nDetalhes da Turma:" + GetReset());
            System.out.println(turma);
            System.out.println("\nAlunos Inscritos: " + turma.getEstudantes().size());
            turma.getEstudantes().forEach(e -> System.out.println(" - " + e.getNome() + " (" + e.getNumeroMec() + ")"));

            System.out.println("\nUnidades Curriculares: " + turma.getUnidadesCurriculares().size());
            turma.getUnidadesCurriculares().forEach(uc -> System.out.println(" - " + uc.getNome()));
        } else {
            System.out.println(GetYellow() + "Turma não encontrada." + GetReset());
        }
        MenuUtils.pressionarEnter(scanner);
    }

    private void listarTurmas() {
        System.out.println(GetBlue() + "\n--- LISTA DE TURMAS ---" + GetReset());
        List<Turma> turmas = turmaController.listarTodas();
        if (turmas.isEmpty()) {
            System.out.println(GetYellow() + "Nenhuma turma registada." + GetReset());
        } else {
            turmas.forEach(System.out::println);
        }
        MenuUtils.pressionarEnter(scanner);
    }

    private void adicionarUC() {
        System.out.println(GetBlue() + "\n--- ADICIONAR UC À TURMA ---" + GetReset());
        Curso curso = obterCursoInterativo();
        if (curso == null) return;

        int ano = Integer.parseInt(BackendUtils.lerInputString(scanner, "Ano Curricular: "));
        String anoLetivo = BackendUtils.lerInputString(scanner, "Ano Letivo (ex: 2025/2026): ");

        if(!turmaController.existeTurma(curso, ano, anoLetivo)){
            System.out.println(GetRed() + "Turma não encontrada." + GetReset());
            MenuUtils.pressionarEnter(scanner);
            return;
        }

        String nomeUC = BackendUtils.lerInputString(scanner, "Nome da UC a adicionar: ");
        UnidadeCurricularController ucCtrl = new UnidadeCurricularController();
        UnidadeCurricular uc = ucCtrl.procurarUCPorNome(nomeUC);

        if (uc == null) {
            System.out.println(GetRed() + "UC não encontrada no sistema." + GetReset());
        } else {
            try {
                turmaController.adicionarUC(curso, ano, anoLetivo, uc);
                System.out.println(GetGreen() + "UC adicionada à turma com sucesso!" + GetReset());
            } catch (Exception e) {
                System.out.println(GetRed() + "Erro: " + e.getMessage() + GetReset());
            }
        }
        MenuUtils.pressionarEnter(scanner);
    }
}