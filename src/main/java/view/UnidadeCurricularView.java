package view;

import common.exceptions.CancelarRegistoException;
import common.utils.BackendUtils;
import common.utils.MenuUtils;
import controller.DocenteController;
import controller.UnidadeCurricularController;
import model.Docente;
import model.Resultado;
import model.UnidadeCurricular;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static common.utils.DesignUtils.*;

public class UnidadeCurricularView {
    private final Scanner scanner;

    public UnidadeCurricularView() {
        this.scanner = new Scanner(System.in);
    }

    public void exibirMenuUnidadesCurriculares() {
        String opcao;

        do {
            try {
                UnidadeCurricularController ucc = new UnidadeCurricularController();
                boolean temUCs = !ucc.listarTodasUCs().isEmpty();

                ArrayList<String> opcoes = new ArrayList<>();
                opcoes.add("1. Registar Unidade Curricular");

                if (temUCs) {
                    opcoes.add("2. Listar Unidades Curriculares");
                    opcoes.add("3. Procurar Unidade Curricular");
                    opcoes.add("4. Atualizar Unidade Curricular");
                    opcoes.add("5. Eliminar Unidade Curricular");
                    opcoes.add("6. Definir Momentos de Avaliação");
                }
                opcoes.add("0. Voltar ao Menu de Gestão");

                MenuUtils.limparTela();
                MenuUtils.exibirSubTitulo("PORTAL GESTOR > MENU PRINCIPAL > UNIDADES CURRICULARES", opcoes);
                System.out.print("\n" + GetWhiteBold() + "Selecione uma opção: " + GetReset());
                opcao = scanner.nextLine().trim();

                switch (opcao) {
                    case "1": registarUnidadeCurricular(); break;
                    case "2": if (temUCs) listarUnidadesCurriculares(); else mostrarErroOpcao(); break;
                    case "3": if (temUCs) procurarUnidadeCurricular(); else mostrarErroOpcao(); break;
                    case "4": if (temUCs) atualizarUnidadeCurricular(); else mostrarErroOpcao(); break;
                    case "5": if (temUCs) eliminarUnidadeCurricular(); else mostrarErroOpcao(); break;
                    case "6": if (temUCs) definirMomentosAvaliacao(); else mostrarErroOpcao(); break;
                    case "0":
                        System.out.println(GetYellow() + "\nA voltar ao menu de gestão..." + GetReset());
                        return;
                    default:
                        mostrarErroOpcao();
                }
            } catch (Exception e) {
                System.out.println("\n" + GetRed() + "Ocorreu um erro na navegação: " + e.getMessage() + GetReset());
                MenuUtils.pressionarEnter(scanner);
            }
        } while (true);
    }

    private void mostrarErroOpcao() {
        System.out.println(GetRed() + "Opção inválida! Por favor, escolha uma opção visível na lista." + GetReset());
        MenuUtils.pressionarEnter(scanner);
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
                    if (anoStr.equals("0")) throw new CancelarRegistoException("Operação cancelada pelo utilizador.");
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
                    if (semStr.equals("0")) throw new CancelarRegistoException("Operação cancelada pelo utilizador.");
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
                if (siglaDocente.equals("0")) throw new CancelarRegistoException("Operação cancelada pelo utilizador.");

                if (siglaDocente.trim().isEmpty()) {
                    System.out.println(GetRed() + "Erro: É obrigatório indicar a sigla de um Docente para criar a UC." + GetReset());
                } else {
                    DocenteController docenteController = new DocenteController();
                    if (docenteController.procurarDocentePorNif(0) != null || true) {
                        docenteValido = true;
                    }
                }
            }

            UnidadeCurricularController unidadeCurricularControllerAtualizado = new UnidadeCurricularController();
            Resultado resultado = unidadeCurricularControllerAtualizado.registarUC(nome, ano, semestre, siglaDocente);

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
                System.out.println(GetCyanBold() + "-------------------------------------------------------------------------" + GetReset());
                System.out.printf(GetWhiteBold() + " %-4s | %-25s | %-5s | %-3s | %-15s | %-4s \n" + GetReset(), "ID", "NOME DA UC", "ANO", "SEM", "DOCENTE", "ECTS");
                System.out.println(GetCyanBold() + "-------------------------------------------------------------------------" + GetReset());

                for (int i = 0; i < unidadeCurriculars.size(); i++) {
                    UnidadeCurricular unidadeCurricular = unidadeCurriculars.get(i);
                    String docenteNome = (unidadeCurricular.getDocente() != null) ? unidadeCurricular.getDocente().getSigla() : GetYellow() + "N/A" + GetReset();
                    System.out.printf(" %-4d | %-25s | %-5d |%-3d | %-15s | %-4d \n",
                            unidadeCurricular.getId(),
                            unidadeCurricular.getNome(), unidadeCurricular.getAnoCurricular(), unidadeCurricular.getSemestre(), docenteNome, unidadeCurricular.getEcts());
                }
                System.out.println(GetCyanBold() + "-------------------------------------------------------------------------" + GetReset());
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

            listarUCsSemPausa();

            String idStr = BackendUtils.lerInputString(scanner, "Digite o ID da UC a procurar: ");
            int id;
            try {
                id = Integer.parseInt(idStr);
            } catch (NumberFormatException e) {
                System.out.println(GetRed() + "ID inválido. Por favor introduza um número." + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }

            UnidadeCurricularController unidadeCurricularControllerAtualizado = new UnidadeCurricularController();
            UnidadeCurricular unidadeCurricular = unidadeCurricularControllerAtualizado.procurarUCPorId(id);

            if (unidadeCurricular != null) {
                System.out.println(GetGreen() + "\nUC Encontrada:" + GetReset());
                imprimirDadosUnidadeCurricular(unidadeCurricular);

                DAL.CursoCRUD cursoCRUD = new DAL.CursoCRUD();
                List<String> cursosAssociados = new ArrayList<>();

                for (model.Curso curso : cursoCRUD.getCursos()) {
                    if (curso.getUnidadeCurriculars() != null) {
                        for (model.UnidadeCurricular unidadeCurricularDoCurso : curso.getUnidadeCurriculars()) {
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
                System.out.println(GetYellow() + "\nUnidade Curricular não encontrada com o ID informado." + GetReset());
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

            listarUCsSemPausa();

            String idStr = BackendUtils.lerInputString(scanner, "\nDigite o ID da UC a atualizar: ");
            int id;
            try {
                id = Integer.parseInt(idStr);
            } catch (NumberFormatException e) {
                System.out.println(GetRed() + "ID inválido. Por favor introduza um número." + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }

            UnidadeCurricularController unidadeCurricularControllerAtualizado = new UnidadeCurricularController();
            UnidadeCurricular ucExistente = unidadeCurricularControllerAtualizado.procurarUCPorId(id);

            String nomeAtual;
            while (ucExistente == null) {
                nomeAtual = BackendUtils.lerInputString(scanner, "\nDigite o nome da UC a atualizar: ");
                if (nomeAtual.equals("0")) throw new CancelarRegistoException("Operação cancelada pelo utilizador.");

                ucExistente = unidadeCurricularControllerAtualizado.procurarUCPorNome(nomeAtual);
                if (ucExistente == null) {
                    System.out.println(GetRed() + "Erro: UC não encontrada. Verifique a lista e tente novamente." + GetReset());
                }
            }

            System.out.println(GetGreen() + "\nDados atuais: [ID: " + ucExistente.getId() + "] " + ucExistente.getNome() + GetReset());
            System.out.println(GetYellow() + "[Pressione ENTER nos campos que deseja manter iguais]" + GetReset());

            String novoNome = BackendUtils.lerInputString(scanner, "Novo nome: ");
            String nomeFinal = novoNome.isEmpty() ? null : novoNome;

            int novoAno = 0;
            while (true) {
                String anoInput = BackendUtils.lerInputString(scanner, "Novo ano curricular (1, 2 ou 3): ");
                if (anoInput.isEmpty()) break;
                try {
                    int anoTmp = Integer.parseInt(anoInput);
                    if (anoTmp >= 1 && anoTmp <= 3) {
                        novoAno = anoTmp;
                        break;
                    } else
                        System.out.println(GetRed() + "Erro: O ano curricular deve ser 1, 2 ou 3. Tente novamente ou pressione Enter para manter." + GetReset());
                } catch (NumberFormatException e) {
                    System.out.println(GetRed() + "Erro: Valor inválido. Introduza um número entre 1 e 3, ou pressione Enter para manter." + GetReset());
                }
            }

            int novoSemestre = 0;
            while (true) {
                String semInput = BackendUtils.lerInputString(scanner, "Novo semestre (1 ou 2): ");
                if (!semInput.isEmpty()) {
                    try {
                        int semestre = Integer.parseInt(semInput);
                        if (semestre >= 1 && semestre <= 2) {
                            novoSemestre = semestre;
                            break;
                        } else
                            System.out.println(GetYellow() + "Aviso: Semestre inválido. Mantendo o semestre original." + GetReset());
                    } catch (NumberFormatException e) {
                        System.out.println(GetYellow() + "Aviso: Formato inválido. Mantendo o semestre original." + GetReset());
                    }
                } else {
                    break;
                }
            }

            listarDocentesDisponiveis();
            String novaSiglaDocente = BackendUtils.lerInputString(scanner, "\nSigla do novo Docente (ou Enter para manter o mesmo): ").toUpperCase();
            String siglaFinal = novaSiglaDocente.isEmpty() ? null : novaSiglaDocente;

            System.out.println(GetYellow() + "\n AVISO DE IMPACTO GLOBAL ");
            System.out.println("A Unidade Curricular que está a editar pode estar a ser partilhada por múltiplos cursos.");
            System.out.println("Qualquer alteração que faça agora irá refletir-se automaticamente no plano de estudos de TODOS os cursos que a incluem." + GetReset());
            String confirmacao = BackendUtils.lerInputString(scanner, GetWhiteBold() + "\nTem a certeza que deseja guardar estas alterações? (S/N): " + GetReset());
            if (!confirmacao.equalsIgnoreCase("S")) {
                System.out.println(GetBlue() + "\nOperação de atualização cancelada pelo utilizador." + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }

            Resultado resultado = unidadeCurricularControllerAtualizado.atualizarUCPorId(id, nomeFinal, novoAno, novoSemestre, siglaFinal);

            if (resultado.success) {
                System.out.println(GetGreen() + "\nUC atualizada com sucesso!" + GetReset());
                UnidadeCurricular ucAtualizada = unidadeCurricularControllerAtualizado.procurarUCPorId(id);
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

            listarUCsSemPausa();

            String idStr = BackendUtils.lerInputString(scanner, "\nDigite o ID da UC a eliminar: ");
            int id;
            try {
                id = Integer.parseInt(idStr);
            } catch (NumberFormatException e) {
                System.out.println(GetRed() + "ID inválido. Por favor introduza um número." + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }

            UnidadeCurricularController unidadeCurricularControllerAtualizado = new UnidadeCurricularController();
            UnidadeCurricular unidadeCurricular = unidadeCurricularControllerAtualizado.procurarUCPorId(id);

            // CORREÇÃO: Validar se a UC existe para não dar erro
            if (unidadeCurricular == null) {
                System.out.println(GetRed() + "Erro: UC não encontrada com o ID informado." + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }

            // CORREÇÃO: Confirmação única e limpa
            String confirmacao = BackendUtils.lerInputString(scanner, GetYellow() + "Tem a certeza que deseja eliminar a UC '" + unidadeCurricular.getNome() + "' (ID: " + id + ")? (S/N): " + GetReset()).toUpperCase();

            // CORREÇÃO: Chavetas devidamente abertas e fechadas
            if (confirmacao.equals("S")) {
                Resultado resultado = unidadeCurricularControllerAtualizado.eliminarUCPorId(id);

                if (resultado.success) {
                    System.out.println(GetGreen() + "\nUC eliminada com sucesso!" + GetReset());
                } else {
                    System.out.println(GetRed() + "\nErro ao eliminar: " + resultado.errorMessage + GetReset());
                }
            } else {
                System.out.println(GetYellow() + "Operação cancelada." + GetReset());
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

    private void definirMomentosAvaliacao() {
        try {
            System.out.println(GetBlue() + "\n--- DEFINIR MOMENTOS DE AVALIAÇÃO ---" + GetReset());
            System.out.println(GetYellow() + "[Digite '0' a qualquer momento para cancelar a operação!]" + GetReset());

            listarUCsSemPausa();

            String idStr = BackendUtils.lerInputString(scanner, "\nDigite o ID da UC: ");
            if (idStr.equals("0")) return;

            int id;
            try {
                id = Integer.parseInt(idStr);
            } catch (NumberFormatException e) {
                System.out.println(GetRed() + "ID inválido. Por favor introduza um número." + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }

            UnidadeCurricularController ucc = new UnidadeCurricularController();
            UnidadeCurricular uc = ucc.procurarUCPorId(id);

            if (uc == null) {
                System.out.println(GetRed() + "Erro: UC não encontrada com o ID informado." + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }

            System.out.println(GetGreen() + "\nUC Selecionada: " + uc.getNome() + GetReset());
            List<String> momentosAtuais = uc.getMomentosAvaliacao();
            if (momentosAtuais == null || momentosAtuais.isEmpty()) {
                System.out.println("Momentos atuais: " + GetYellow() + "Nenhum momento definido." + GetReset());
            } else {
                System.out.println("Momentos atuais: " + String.join(" | ", momentosAtuais));
            }

            System.out.println("\n" + GetWhiteBold() + "Exemplo de formato: " + GetReset() + "Teste 1 | Teste 2 | Exame Final");
            String novosMomentos = BackendUtils.lerInputString(scanner, "Introduza os momentos separados por '|' (ou Enter para cancelar): ");

            if(novosMomentos.trim().isEmpty()) {
                System.out.println(GetYellow() + "Operação cancelada. Não foram feitas alterações." + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }

            List<String> listaMomentos = new ArrayList<>();
            String[] particoes = novosMomentos.split("\\|");
            for (String p : particoes) {
                if (!p.trim().isEmpty()) {
                    listaMomentos.add(p.trim());
                }
            }

            Resultado resultado = ucc.definirMomentosAvaliacao(id, listaMomentos);

            if (resultado.success) {
                System.out.println(GetGreen() + "\nMomentos de avaliação atualizados com sucesso!" + GetReset());
            } else {
                System.out.println(GetRed() + "\nErro ao atualizar momentos: " + resultado.errorMessage + GetReset());
            }

            MenuUtils.pressionarEnter(scanner);

        } catch (Exception e) {
            System.out.println(GetRed() + "Ocorreu um erro inesperado: " + e.getMessage() + GetReset());
            MenuUtils.pressionarEnter(scanner);
        }
    }

    private void listarUCsSemPausa() {
        UnidadeCurricularController ctrl = new UnidadeCurricularController();
        List<UnidadeCurricular> ucs = ctrl.listarTodasUCs();

        if (ucs.isEmpty()) {
            System.out.println(GetYellow() + "Nenhuma UC registada até ao momento!" + GetReset());
        } else {
            System.out.println(GetCyanBold() + "-------------------------------------------------------------------------" + GetReset());
            System.out.printf(GetWhiteBold() + " %-4s | %-25s | %-5s | %-3s | %-15s | %-4s \n" + GetReset(), "ID", "NOME DA UC", "ANO", "SEM", "DOCENTE", "ECTS");
            System.out.println(GetCyanBold() + "-------------------------------------------------------------------------" + GetReset());
            for (UnidadeCurricular uc : ucs) {
                String docenteNome = (uc.getDocente() != null) ? uc.getDocente().getSigla() : GetYellow() + "N/A" + GetReset();
                System.out.printf(" %-4d | %-25s | %-5d |%-3d | %-15s | %-4d \n",
                        uc.getId(), uc.getNome(), uc.getAnoCurricular(), uc.getSemestre(), docenteNome, uc.getEcts());
            }
            System.out.println(GetCyanBold() + "-------------------------------------------------------------------------" + GetReset());
        }
    }

    private void listarDocentesDisponiveis() {
        System.out.println("\n" + GetBlue() + "--- Docentes Disponíveis ---" + GetReset());
        DocenteController docenteControllerAtualizado = new DocenteController(); // CARREGA MEMÓRIA FRESCA
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
        System.out.println("ID: " + unidadeCurricular.getId());
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