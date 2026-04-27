package main.view;

import main.common.exceptions.CancelarRegistoException;
import main.common.utils.BackendUtils;
import main.common.utils.MenuUtils;
import main.controller.DocenteController;
import main.controller.UnidadeCurricularController;
import main.model.Docente;
import main.model.Resultado;
import main.model.UnidadeCurricular;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static main.common.utils.DesignUtils.*;

public class UnidadeCurricularView {
    private final UnidadeCurricularController ucController;
    private final DocenteController docenteController;
    private final Scanner scanner;

    public UnidadeCurricularView() {
        this.ucController = new UnidadeCurricularController();
        this.docenteController = new DocenteController();
        this.scanner = new Scanner(System.in);
    }

    public void exibirMenuUnidadesCurriculares() {
        String opcao;
        ArrayList<String> opcoes = new ArrayList<>();
        opcoes.add("1. Registar Unidade Curricular");
        opcoes.add("2. Listar Unidades Curriculares");
        opcoes.add("3. Procurar Unidade Curricular");
        opcoes.add("4. Atualizar Unidade Curricular");
        opcoes.add("5. Eliminar Unidade Curricular");
        opcoes.add("0. Voltar ao Menu de Gestão");

        do {
            try {
                MenuUtils.exibirSubTitulo("PORTAL GESTOR > MENU PRINCIPAL > UNIDADES CURRICULARES", opcoes);
                System.out.print("\n" + GetWhiteBold() + "Selecione uma opção: " + GetReset());
                opcao = scanner.nextLine().trim();

                switch (opcao) {
                    case "1": registarUnidadeCurricular(); break;
                    case "2": listarUnidadesCurriculares(); break;
                    case "3": procurarUnidadeCurricular(); break;
                    case "4": atualizarUnidadeCurricular(); break;
                    case "5": eliminarUnidadeCurricular(); break;
                    case "0":
                        System.out.println(GetYellow() + "\nA voltar ao menu de gestão..." + GetReset());
                        return;
                    default:
                        System.out.println(GetRed() + "Opção inválida! Por favor, escolha uma opção da lista." + GetReset());
                        MenuUtils.pressionarEnter(scanner);
                }
            } catch (Exception e) {
                System.out.println("\n" + GetRed() + "Ocorreu um erro na navegação: " + e.getMessage() + GetReset());
                MenuUtils.pressionarEnter(scanner);
            }
        } while (true);
    }

    private void registarUnidadeCurricular() {
        try {
            System.out.println(GetBlue() + "\n--- REGISTO DE UNIDADE CURRICULAR ---" + GetReset());
            System.out.println(GetYellow() + "[Digite '0' a qualquer momento para cancelar a operação!]" + GetReset());

            String nome = BackendUtils.lerInputString(scanner, "Nome da UC: ");

            int ano = 0;
            boolean anoValido = false;
            while (!anoValido) {
                try {
                    String anoStr = BackendUtils.lerInputString(scanner, "Ano Curricular (1, 2 ou 3): ");
                    ano = Integer.parseInt(anoStr);
                    if (ano >= 1 && ano <= 3) {
                        anoValido = true;
                    } else {
                        System.out.println(GetRed() + "Aviso: O Ano Curricular deve ser 1, 2 ou 3." + GetReset());
                    }
                } catch (NumberFormatException e) {
                    System.out.println(GetRed() + "Aviso: O valor introduzido não é um número válido." + GetReset());
                }
            }

            int semestre = 0;
            boolean semestreValido = false;
            while (!semestreValido) {
                try {
                    String semStr = BackendUtils.lerInputString(scanner, "Semestre (1 ou 2): ");
                    semestre = Integer.parseInt(semStr);
                    if (semestre == 1 || semestre == 2) {
                        semestreValido = true;
                    } else {
                        System.out.println(GetRed() + "Aviso: O Semestre deve ser 1 ou 2." + GetReset());
                    }
                } catch (NumberFormatException e) {
                System.out.println(GetRed() + "Aviso: O valor introduzido não é um número válido." + GetReset());
                }
            }

            listarDocentesDisponiveis();
            String siglaDocente = "";
            boolean docenteValido = false;

            while (!docenteValido) {
                siglaDocente = BackendUtils.lerInputString(scanner, "\nSigla do Docente Responsável (Obrigatório): ").toUpperCase();

                if (siglaDocente.trim().isEmpty()) {
                    System.out.println(GetRed() + "Erro: É obrigatório indicar a sigla de um Docente para criar a UC." + GetReset());
                } else {
                    docenteValido = true;
                }
            }

            UnidadeCurricularController unidadeCurricularControllerAtualizado = new UnidadeCurricularController();
            Resultado resultado = unidadeCurricularControllerAtualizado.registarUC(nome, ano,semestre, siglaDocente.isEmpty() ? null : siglaDocente);

            if (resultado.success) {
                System.out.println(GetGreen() + "\nUC registada com sucesso!" + GetReset());
                UnidadeCurricular novaUC = unidadeCurricularControllerAtualizado.procurarUCPorNome(nome);
                if (novaUC != null) {
                    imprimirDadosUnidadeCurricular(novaUC);
                }
            } else {
                System.out.println(GetRed() + "\nErro ao registar: " + resultado.errorMessage + GetReset());
            }

            MenuUtils.pressionarEnter(scanner);

        } catch (CancelarRegistoException e) {
            System.out.println("\n" + GetYellow() + "Aviso: " + e.getMessage() + GetReset());
            System.out.println(GetRed() + "Registo interrompido!" + GetReset());
            MenuUtils.pressionarEnter(scanner);
        } catch (Exception e) {
            System.out.println(GetRed() + "Ocorreu um erro inesperado: " + e.getMessage() + GetReset());
            MenuUtils.pressionarEnter(scanner);
        }
    }

    private void listarUnidadesCurriculares() {
        try {
            System.out.println(GetBlue() + "\n--- LISTA DE UNIDADES CURRICULARES ---" + GetReset());
            UnidadeCurricularController unidadeCurricularControllerAtualizado = new UnidadeCurricularController();
            List<UnidadeCurricular> unidadeCurriculars = unidadeCurricularControllerAtualizado.listarTodasUCs();

            if (unidadeCurriculars.isEmpty()) {
                System.out.println(GetYellow() + "Nenhuma UC registada até ao momento!" + GetReset());
            } else {
                System.out.println(GetCyanBold() + "-----------------------------------------------------------------" + GetReset());
                System.out.printf(GetWhiteBold() + " %-25s | %-5s | %-3s | %-15s | %-4s \n" + GetReset(), "NOME DA UC", "ANO", "SEM", "DOCENTE", "ECTS");
                System.out.println(GetCyanBold() + "-----------------------------------------------------------------" + GetReset());

                for (UnidadeCurricular unidadeCurricular : unidadeCurriculars) {
                    String docenteNome = (unidadeCurricular.getDocente() != null) ? unidadeCurricular.getDocente().getSigla() : GetYellow() + "N/A" + GetReset();
                    System.out.printf(" %-25s | %-5d |%-3d | %-15s | %-4d \n",
                            unidadeCurricular.getNome(), unidadeCurricular.getAnoCurricular(),unidadeCurricular.getSemestre(), docenteNome, unidadeCurricular.getEcts());
                }
                System.out.println(GetCyanBold() + "-----------------------------------------------------------------" + GetReset());
            }

            MenuUtils.pressionarEnter(scanner);

        } catch (Exception e) {
            System.out.println(GetRed() + "Ocorreu um erro ao listar as UCs: " + e.getMessage() + GetReset());
            MenuUtils.pressionarEnter(scanner);
        }
    }

    private void procurarUnidadeCurricular() {
        try {
            System.out.println(GetBlue() + "\n--- PROCURAR UNIDADE CURRICULAR ---" + GetReset());
            System.out.println(GetYellow() + "[Digite '0' a qualquer momento para cancelar a operação!]" + GetReset());

            String nome = BackendUtils.lerInputString(scanner, "Digite o nome da UC a procurar: ");

            UnidadeCurricularController unidadeCurricularControllerAtualizado = new UnidadeCurricularController();
            UnidadeCurricular unidadeCurricular = unidadeCurricularControllerAtualizado.procurarUCPorNome(nome);

            if (unidadeCurricular != null) {
                System.out.println(GetGreen() + "\nUC Encontrada:" + GetReset());
                imprimirDadosUnidadeCurricular(unidadeCurricular);

                main.DAL.CursoCRUD cursoCRUD = new main.DAL.CursoCRUD();
                List<String> cursosAssociados = new ArrayList<>();

                for (main.model.Curso curso : cursoCRUD.getCursos()) {
                    if (curso.getUnidadeCurriculars() != null) {
                        for (main.model.UnidadeCurricular unidadeCurricularDoCurso : curso.getUnidadeCurriculars()) {
                            if (unidadeCurricularDoCurso.getNome().equalsIgnoreCase(unidadeCurricular.getNome())) {
                                cursosAssociados.add(curso.getNome());
                                break;
                            }
                        }
                    }
                }
                System.out.println(GetWhiteBold() + "Cursos a que pertence: " + GetReset() +
                        (cursosAssociados.isEmpty() ? GetYellow() + "Nenhum curso associado" + GetReset() : String.join(" | ", cursosAssociados)));
            } else {
                System.out.println(GetYellow() + "\nUnidade Curricular não encontrada com o nome informado." + GetReset());
            }

            MenuUtils.pressionarEnter(scanner);

        } catch (CancelarRegistoException e) {
            System.out.println("\n" + GetYellow() + "Aviso: " + e.getMessage() + GetReset());
            System.out.println(GetRed() + "Operação interrompida!" + GetReset());
            MenuUtils.pressionarEnter(scanner);
        } catch (Exception e) {
            System.out.println(GetRed() + "Ocorreu um erro inesperado: " + e.getMessage() + GetReset());
            MenuUtils.pressionarEnter(scanner);
        }
    }

    private void atualizarUnidadeCurricular() {
        try {
            System.out.println(GetBlue() + "\n--- ATUALIZAR UNIDADE CURRICULAR ---" + GetReset());
            System.out.println(GetYellow() + "[Digite '0' a qualquer momento para cancelar a operação!]" + GetReset());

            listarUnidadesCurriculares();

            String nomeAtual = BackendUtils.lerInputString(scanner, "\nDigite o nome da UC a atualizar: ");

            UnidadeCurricularController unidadeCurricularControllerAtualizado = new UnidadeCurricularController();
            UnidadeCurricular ucExistente = unidadeCurricularControllerAtualizado.procurarUCPorNome(nomeAtual);

            if (ucExistente == null) {
                System.out.println(GetYellow() + "\nUnidade Curricular não encontrada." + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }

            System.out.println(GetGreen() + "\nDados atuais: " + ucExistente.getNome() + GetReset());
            System.out.println(GetYellow() + "[Pressione ENTER nos campos que deseja manter iguais]" + GetReset());

            String novoNome = BackendUtils.lerInputString(scanner, "Novo nome: ");
            String nomeFinal = novoNome.isEmpty() ? null : novoNome;

            int novoAno = 0;
            String anoInput = BackendUtils.lerInputString(scanner, "Novo ano curricular (1, 2 ou 3): ");
            if (!anoInput.isEmpty()) {
                try {
                    novoAno = Integer.parseInt(anoInput);
                    if (novoAno < 1 || novoAno > 3) {
                        System.out.println(GetYellow() + "Aviso: Ano inválido. Mantendo o original." + GetReset());
                        novoAno = 0;
                    }
                } catch (NumberFormatException e) {
                    System.out.println(GetYellow() + "Aviso: Formato inválido. Mantendo o original." + GetReset());
                    novoAno = 0;
                }
            }

            int novoSemestre = 0;
            String semInput = BackendUtils.lerInputString(scanner, "Novo semestre (1 ou 2): ");
            if (!semInput.isEmpty()) {
                try {
                    novoSemestre = Integer.parseInt(semInput);
                    if (novoSemestre < 1 || novoSemestre > 2) {
                        System.out.println(GetYellow() + "Aviso: Semestre inválido. Mantendo o semestre original." + GetReset());
                        novoSemestre = 0;
                    }
                } catch (NumberFormatException e) {
                    System.out.println(GetYellow() + "Aviso: Formato inválido. Mantendo o semestre original." + GetReset());
                }
            }

            listarDocentesDisponiveis();
            String novaSiglaDocente = BackendUtils.lerInputString(scanner, "\nSigla do novo Docente (ou Enter para manter o mesmo): ").toUpperCase();
            String siglaFinal = novaSiglaDocente.isEmpty() ? null : novaSiglaDocente;

            System.out.println(GetYellow() + "\n AVISO DE IMPACTO GLOBAL ");
            System.out.println("A Unidade Curricular que está a editar pode estar a ser partilhada por múltiplos cursos.");
            System.out.println("Qualquer alteração que faça agora irá refletir-se automaticamente no plano de estudos de TODOS os cursos que a incluem." + GetReset());
            System.out.print(GetWhiteBold() + "\nTem a certeza que deseja guardar estas alterações? (S/N): " + GetReset());

            String confirmacao = BackendUtils.lerInputString(scanner, GetWhiteBold() + "\nTem a certeza que deseja guardar estas alterações? (S/N): " + GetReset());
            if (!confirmacao.equalsIgnoreCase("S")) {
                System.out.println(GetBlue() + "\nOperação de atualização cancelada pelo utilizador." + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }

            Resultado resultado = unidadeCurricularControllerAtualizado.atualizarUC(nomeAtual, nomeFinal, novoAno,novoSemestre, siglaFinal);

            if (resultado.success) {
                System.out.println(GetGreen() + "\nUC atualizada com sucesso!" + GetReset());
                UnidadeCurricular ucAtualizada = unidadeCurricularControllerAtualizado.procurarUCPorNome(nomeFinal != null ? nomeFinal : nomeAtual);
                if (ucAtualizada != null) {
                    imprimirDadosUnidadeCurricular(ucAtualizada);
                }
            } else {
                System.out.println(GetRed() + "\nErro ao atualizar: " + resultado.errorMessage + GetReset());
            }

            MenuUtils.pressionarEnter(scanner);

        } catch (CancelarRegistoException e) {
            System.out.println("\n" + GetYellow() + "Aviso: " + e.getMessage() + GetReset());
            System.out.println(GetRed() + "Operação de atualização interrompida!" + GetReset());
            MenuUtils.pressionarEnter(scanner);
        } catch (Exception e) {
            System.out.println(GetRed() + "Ocorreu um erro inesperado: " + e.getMessage() + GetReset());
            MenuUtils.pressionarEnter(scanner);
        }
    }

    private void eliminarUnidadeCurricular() {
        try {
            System.out.println(GetBlue() + "\n--- ELIMINAR UNIDADE CURRICULAR ---" + GetReset());
            System.out.println(GetYellow() + "[Digite '0' a qualquer momento para cancelar a operação!]" + GetReset());

            listarUnidadesCurriculares();

            String nome = BackendUtils.lerInputString(scanner, "\nDigite o nome da UC a eliminar: ");

            UnidadeCurricularController unidadeCurricularControllerAtualizado = new UnidadeCurricularController();
            UnidadeCurricular unidadeCurricular = unidadeCurricularControllerAtualizado.procurarUCPorNome(nome);

            if (unidadeCurricular == null) {
                System.out.println(GetYellow() + "\nUnidade Curricular não encontrada." + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }

            String confirmacao = BackendUtils.lerInputString(scanner, GetYellow() + "Tem a certeza que deseja eliminar a UC '" + nome + "'? (S/N): " + GetReset()).toUpperCase();

            if (confirmacao.equals("S")) {
                Resultado resultado = unidadeCurricularControllerAtualizado.eliminarUC(nome);

                if (resultado.success) {
                    System.out.println(GetGreen() + "\nUC eliminada com sucesso!" + GetReset());
                } else {
                    System.out.println(GetRed() + "\nErro ao eliminar: " + resultado.errorMessage + GetReset());
                }
            } else {
                System.out.println(GetYellow() + "\nEliminação cancelada." + GetReset());
            }

            MenuUtils.pressionarEnter(scanner);

        } catch (CancelarRegistoException e) {
            System.out.println("\n" + GetYellow() + "Aviso: " + e.getMessage() + GetReset());
            System.out.println(GetRed() + "Operação interrompida!" + GetReset());
            MenuUtils.pressionarEnter(scanner);
        } catch (Exception e) {
            System.out.println(GetRed() + "Ocorreu um erro inesperado: " + e.getMessage() + GetReset());
            MenuUtils.pressionarEnter(scanner);
        }
    }

    private void listarDocentesDisponiveis() {
        System.out.println("\n" + GetBlue() + "--- Docentes Disponíveis ---" + GetReset());
        DocenteController docenteControllerAtualizado = new DocenteController();
        List<Docente> docentes = docenteControllerAtualizado.listarDocentes();
        if (docentes == null || docentes.isEmpty()) {
            System.out.println(GetYellow() + "Nenhum docente registado no sistema." + GetReset());
        } else {
            for (Docente docente : docentes) {
                System.out.println(docente.getSigla() + " - " + docente.getNome());
            }
        }
    }

    private void imprimirDadosUnidadeCurricular(UnidadeCurricular unidadeCurricular) {
        System.out.println("\n" + GetWhiteBold() + "--- Dados da Unidade Curricular ---" + GetReset());
        System.out.println("Nome: " + unidadeCurricular.getNome());
        System.out.println("Ano Curricular: " + unidadeCurricular.getAnoCurricular());
        System.out.println("Semestre: " + unidadeCurricular.getSemestre());
        System.out.println("ECTS: " + unidadeCurricular.getEcts());

        if (unidadeCurricular.getDocente() != null) {
            System.out.println("Docente Responsável: " + unidadeCurricular.getDocente().getNome() + " (" + unidadeCurricular.getDocente().getSigla() + ")");
        } else {
            System.out.println("Docente Responsável: " + GetYellow() + "NÃO ATRIBUÍDO" + GetReset());
        }
        System.out.println(GetWhiteBold() + "-----------------------------------" + GetReset());
    }
}