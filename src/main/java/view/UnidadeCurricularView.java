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
import java.util.Arrays;
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
        ArrayList<String> opcoes = new ArrayList<>();
        opcoes.add("1. Registar Unidade Curricular");
        opcoes.add("2. Listar Unidades Curriculares");
        opcoes.add("3. Procurar Unidade Curricular");
        opcoes.add("4. Atualizar Unidade Curricular");
        opcoes.add("5. Eliminar Unidade Curricular");
        opcoes.add("6. Listar Alunos da UC");
        opcoes.add("0. Voltar ao Menu de Gest o");

        do {
            try {
                MenuUtils.limparTela();
                MenuUtils.exibirSubTitulo("PORTAL GESTOR > MENU PRINCIPAL > UNIDADES CURRICULARES", opcoes);
                System.out.print("\n" + GetWhiteBold() + "Selecione uma op o: " + GetReset());
                opcao = scanner.nextLine().trim();

                switch (opcao) {
                    case "1":
                        registarUnidadeCurricular();
                        break;
                    case "2":
                        listarUnidadesCurriculares();
                        break;
                    case "3":
                        procurarUnidadeCurricular();
                        break;
                    case "4":
                        atualizarUnidadeCurricular();
                        break;
                    case "5":
                        eliminarUnidadeCurricular();
                        break;
                    case "6":
                        listarAlunosDaUC();
                        break;
                    case "0":
                        System.out.println(GetYellow() + "\nA voltar ao menu de gest o..." + GetReset());
                        return;
                    default:
                        mostrarErroOpcao();
                }
            } catch (Exception e) {
                System.out.println("\n" + GetRed() + "Ocorreu um erro na navega o: " + e.getMessage() + GetReset());
                MenuUtils.pressionarEnter(scanner);
            }
        } while (true);
    }

    private void mostrarErroOpcao() {
        System.out.println(GetRed() + "Op o inv lida! Por favor, escolha uma op o vis vel na lista." + GetReset());
        MenuUtils.pressionarEnter(scanner);
    }

    private void registarUnidadeCurricular() {
        try {
            System.out.println(GetBlue() + "\n--- REGISTO DE UNIDADE CURRICULAR ---" + GetReset());

            DocenteController verificacaoDocentes = new DocenteController();
            if (verificacaoDocentes.listarDocentes().isEmpty()) {
                System.out.println(GetRed() + "Erro: Não existem docentes registados no sistema." + GetReset());
                System.out.println(GetYellow() + "Por favor, vá ao menu de Docentes e crie um antes de registar uma Unidade Curricular." + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }

            System.out.println(GetYellow() + "[Digite '0' a qualquer momento para cancelar a opera o!]" + GetReset());

            String nome = "";
            while (nome.isEmpty()) {
                nome = BackendUtils.lerInputString(scanner, "Nome da UC: ");

                if (!nome.isEmpty()) {
                    String [] palavras = nome.toLowerCase().split("\\s+");
                    StringBuilder nomeFormatado = new StringBuilder();
                    List<String> excecoes = Arrays.asList("de", "do", "da", "dos", "das", "e", "em", "no", "na", "nos", "nas", "por", "para", "com", "a", "o", "as", "os");                    nome = nome.substring(0, 1).toUpperCase() + nome.substring(1);

                    for (int i = 0; i < palavras.length; i++) {
                        String palavra = palavras[i];
                        if (palavra.isEmpty())
                            continue;
                        if (i == 0 || !excecoes.contains(palavra)) {
                            palavra = palavra.substring(0,1).toUpperCase() + palavra.substring(1);
                        }
                        nomeFormatado.append(palavra).append(" ");
                    }
                    nome = nomeFormatado.toString().trim();
                } else {
                    System.out.println(GetRed() + "O campo Nome da UC não pode estar vazio. Tente novamente." + GetReset());
                }
            }

            int ano = 0;
            boolean anoValido = false;
            while (!anoValido) {
                try {
                    String anoStr = BackendUtils.lerInputString(scanner, "Ano Curricular (1, 2 ou 3): ");
                    if (anoStr.equals("0")) throw new CancelarRegistoException("Opera o cancelada pelo utilizador.");
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
                    if (semStr.equals("0")) throw new CancelarRegistoException("Opera o cancelada pelo utilizador.");
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
                siglaDocente = BackendUtils.lerInputString(scanner, "\nSigla do Docente Respons vel (Obrigat rio): ").toUpperCase();
                if (siglaDocente.equals("0")) throw new CancelarRegistoException("Opera o cancelada pelo utilizador.");
                DocenteController dc = new DocenteController();
                if (dc.procurarDocentePorSigla(siglaDocente) != null) {
                    docenteValido = true;
                } else {
                    System.out.println(GetRed() + "Erro: Docente com essa sigla n o encontrado. Tente novamente." + GetReset());
                }
            }

            UnidadeCurricularController unidadeCurricularControllerAtualizado = new UnidadeCurricularController();
            Resultado<UnidadeCurricular> resultado = unidadeCurricularControllerAtualizado.registarUC(nome, ano, semestre, siglaDocente);

            if (resultado.sucesso) {
                System.out.println(GetGreen() + "\nUC registada com sucesso!" + GetReset());
                UnidadeCurricular novaUC = unidadeCurricularControllerAtualizado.procurarUCPorNome(nome);
                if (novaUC != null) {
                    imprimirDadosUnidadeCurricular(novaUC);
                }
            } else {
                System.out.println(GetRed() + "\nErro ao registar: " + resultado.mensagemErro + GetReset());
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
                System.out.println(GetYellow() + "Nenhuma UC registada at  ao momento!" + GetReset());
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
            System.out.println(GetYellow() + "[Digite '0' a qualquer momento para cancelar a opera o!]" + GetReset());
            listarUCsSemPausa();

            String idStr = BackendUtils.lerInputString(scanner, "Digite o ID da UC a procurar: ");
            int id;
            try {
                id = Integer.parseInt(idStr);
            } catch (NumberFormatException e) {
                System.out.println(GetRed() + "ID inv lido. Por favor introduza um n mero." + GetReset());
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
                System.out.println(GetYellow() + "\nUnidade Curricular n o encontrada com o ID informado." + GetReset());
            }
            MenuUtils.pressionarEnter(scanner);
        } catch (CancelarRegistoException e) {
            System.out.println("\n" + GetYellow() + "Aviso: " + e.getMessage() + GetReset());
            System.out.println(GetRed() + "Opera o interrompida!" + GetReset());
            MenuUtils.pressionarEnter(scanner);
        } catch (Exception e) {
            System.out.println(GetRed() + "Ocorreu um erro inesperado: " + e.getMessage() + GetReset());
            MenuUtils.pressionarEnter(scanner);
        }
    }

    private void atualizarUnidadeCurricular() {
        try {
            System.out.println(GetBlue() + "\n--- ATUALIZAR UNIDADE CURRICULAR ---" + GetReset());
            System.out.println(GetYellow() + "[Digite '0' a qualquer momento para cancelar a opera o!]" + GetReset());
            listarUCsSemPausa();

            String idStr = BackendUtils.lerInputString(scanner, "\nDigite o ID da UC a atualizar: ");
            int id;
            try {
                id = Integer.parseInt(idStr);
            } catch (NumberFormatException e) {
                System.out.println(GetRed() + "ID inv lido. Por favor introduza um n mero." + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }

            UnidadeCurricularController unidadeCurricularControllerAtualizado = new UnidadeCurricularController();
            UnidadeCurricular ucExistente = unidadeCurricularControllerAtualizado.procurarUCPorId(id);
            String nomeAtual;

            while (ucExistente == null) {
                nomeAtual = BackendUtils.lerInputString(scanner, "\nDigite o nome da UC a atualizar: ");
                if (nomeAtual.equals("0")) throw new CancelarRegistoException("Opera o cancelada pelo utilizador.");
                ucExistente = unidadeCurricularControllerAtualizado.procurarUCPorNome(nomeAtual);
                if (ucExistente == null) {
                    System.out.println(GetRed() + "Erro: UC n o encontrada. Verifique a lista e tente novamente." + GetReset());
                }
            }

            System.out.println(GetGreen() + "\nDados atuais: [ID: " + ucExistente.getId() + "] " + ucExistente.getNome() + GetReset());
            System.out.println(GetYellow() + "[Pressione ENTER nos campos que deseja manter iguais]" + GetReset());

            String novoNome = BackendUtils.lerInputString(scanner, "Novo nome: ");
            String nomeFinal = null;

            if (!novoNome.isEmpty()) {
                String[] palavras = novoNome.toLowerCase().split("\\s+");
                StringBuilder nomeFormatado = new StringBuilder();
                java.util.List<String> excecoes = java.util.Arrays.asList("de", "do", "da", "dos", "das", "e", "em", "no", "na", "nos", "nas", "por", "para", "com", "a", "o", "as", "os");

                for (int i = 0; i < palavras.length; i++) {
                    String palavra = palavras[i];
                    if (palavra.isEmpty()) continue;

                    if (i == 0 || !excecoes.contains(palavra)) {
                        palavra = palavra.substring(0, 1).toUpperCase() + palavra.substring(1);
                    }
                    nomeFormatado.append(palavra).append(" ");
                }
                nomeFinal = nomeFormatado.toString().trim();
            }

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
                    System.out.println(GetRed() + "Erro: Valor inv lido. Introduza um n mero entre 1 e 3, ou pressione Enter para manter." + GetReset());
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
                            System.out.println(GetYellow() + "Aviso: Semestre inv lido. Mantendo o semestre original." + GetReset());
                    } catch (NumberFormatException e) {
                        System.out.println(GetYellow() + "Aviso: Formato inv lido. Mantendo o semestre original." + GetReset());
                    }
                } else {
                    break;
                }
            }

            listarDocentesDisponiveis();
            String siglaFinal = null;
            DocenteController dc = new DocenteController();

            while (true) {
                String novaSiglaDocente = BackendUtils.lerInputString(scanner, "\nSigla do novo Docente (ou Enter para manter o mesmo): ").toUpperCase();
                if (novaSiglaDocente.isEmpty()) {
                    break;
                }
                if (dc.procurarDocentePorSigla(novaSiglaDocente) != null) {
                    siglaFinal = novaSiglaDocente;
                    break;
                }
                System.out.println(GetRed() + "Erro: Docente com essa sigla não encontrado no sistema. Tente novamente ou pressione ENTER para manter o atual." + GetReset());
            }

            System.out.println(GetYellow() + "\n AVISO DE IMPACTO GLOBAL ");
            System.out.println("A Unidade Curricular que está a editar pode estar a ser partilhada por múltiplos cursos.");
            System.out.println("Qualquer alteração que faça agora irá refletir-se automaticamente no plano de estudos de TODOS os cursos que a incluem." + GetReset());
            String confirmacao = BackendUtils.lerInputString(scanner, GetWhiteBold() + "\nTem a certeza que deseja guardar estas altera es? (S/N): " + GetReset());
            if (!confirmacao.equalsIgnoreCase("S")) {
                System.out.println(GetBlue() + "\nOpera o de atualiza o cancelada pelo utilizador." + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }

            Resultado<UnidadeCurricular> resultado = unidadeCurricularControllerAtualizado.atualizarUC(id, nomeFinal, novoAno, novoSemestre, siglaFinal);

            if (resultado.sucesso) {
                System.out.println(GetGreen() + "\nUC atualizada com sucesso!" + GetReset());
                UnidadeCurricular ucAtualizada = unidadeCurricularControllerAtualizado.procurarUCPorId(id);
                if (ucAtualizada != null) {
                    imprimirDadosUnidadeCurricular(ucAtualizada);
                }
            } else {
                System.out.println(GetRed() + "\nErro ao atualizar: " + resultado.mensagemErro + GetReset());
            }
            MenuUtils.pressionarEnter(scanner);
        } catch (CancelarRegistoException e) {
            System.out.println("\n" + GetYellow() + "Aviso: " + e.getMessage() + GetReset());
            System.out.println(GetRed() + "Opera o de atualiza o interrompida!" + GetReset());
            MenuUtils.pressionarEnter(scanner);
        } catch (Exception e) {
            System.out.println(GetRed() + "Ocorreu um erro inesperado: " + e.getMessage() + GetReset());
            MenuUtils.pressionarEnter(scanner);
        }
    }

    private void eliminarUnidadeCurricular() {
        try {
            System.out.println(GetBlue() + "\n--- ELIMINAR UNIDADE CURRICULAR ---" + GetReset());
            System.out.println(GetYellow() + "[Digite '0' a qualquer momento para cancelar a opera o!]" + GetReset());
            listarUCsSemPausa();

            String idStr = BackendUtils.lerInputString(scanner, "\nDigite o ID da UC a eliminar: ");
            int id;
            try {
                id = Integer.parseInt(idStr);
            } catch (NumberFormatException e) {
                System.out.println(GetRed() + "ID inv lido. Por favor introduza um n mero." + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }

            UnidadeCurricularController unidadeCurricularControllerAtualizado = new UnidadeCurricularController();
            UnidadeCurricular unidadeCurricular = unidadeCurricularControllerAtualizado.procurarUCPorId(id);

            if (unidadeCurricular == null) {
                System.out.println(GetRed() + "Erro: UC n o encontrada com o ID informado." + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }

            String confirmacao = BackendUtils.lerInputString(scanner, GetYellow() + "Tem a certeza que deseja eliminar a UC '" + unidadeCurricular.getNome() + "' (ID: " + id + ")? (S/N): " + GetReset()).toUpperCase();

            if (confirmacao.equals("S")) {
                Resultado<UnidadeCurricular> resultado = unidadeCurricularControllerAtualizado.eliminarUCPorId(id);
                if (resultado.sucesso) {
                    System.out.println(GetGreen() + "\nUC eliminada com sucesso!" + GetReset());
                } else {
                    System.out.println(GetRed() + "\nErro ao eliminar: " + resultado.mensagemErro + GetReset());
                }
            } else {
                System.out.println(GetYellow() + "Opera o cancelada." + GetReset());
            }
            MenuUtils.pressionarEnter(scanner);
        } catch (CancelarRegistoException e) {
            System.out.println("\n" + GetYellow() + "Aviso: " + e.getMessage() + GetReset());
            System.out.println(GetRed() + "Opera o interrompida!" + GetReset());
            MenuUtils.pressionarEnter(scanner);
        } catch (Exception e) {
            System.out.println(GetRed() + "Ocorreu um erro inesperado: " + e.getMessage() + GetReset());
            MenuUtils.pressionarEnter(scanner);
        }
    }

    private void listarAlunosDaUC() {
        try {
            System.out.println(GetBlue() + "\n--- LISTAR ALUNOS POR UC ---" + GetReset());
            System.out.println(GetYellow() + "[Digite '0' a qualquer momento para cancelar a opera o!]" + GetReset());
            listarUCsSemPausa();

            String idStr = BackendUtils.lerInputString(scanner, "\nDigite o ID da UC: ");
            int id = Integer.parseInt(idStr);

            UnidadeCurricularController ucController = new UnidadeCurricularController();
            UnidadeCurricular uc = ucController.procurarUCPorId(id);
            if (uc == null) {
                System.out.println(GetRed() + "Unidade Curricular n o encontrada." + GetReset());
                MenuUtils.pressionarEnter(scanner);
                return;
            }

            DocenteController docenteController = new DocenteController();
            List<model.Estudante> alunos = docenteController.listarAlunosPorUC(uc.getNome());

            if (alunos.isEmpty()) {
                System.out.println(GetYellow() + "Ainda n o existem estudantes com avalia es registadas nesta UC." + GetReset());
            } else {
                System.out.println(GetGreen() + "\nEstudantes inscritos em " + uc.getNome() + ":" + GetReset());
                for (model.Estudante e : alunos) {
                    System.out.printf("- N  Mec: %d | Nome: %s\n", e.getNumeroMec(), e.getNome());
                }
            }
            MenuUtils.pressionarEnter(scanner);
        } catch (NumberFormatException e) {
            System.out.println(GetRed() + "ID inv lido." + GetReset());
            MenuUtils.pressionarEnter(scanner);
        } catch (CancelarRegistoException e) {
            System.out.println(GetYellow() + "Opera o cancelada." + GetReset());
            MenuUtils.pressionarEnter(scanner);
        }
    }

    private void listarUCsSemPausa() {
        UnidadeCurricularController ctrl = new UnidadeCurricularController();
        List<UnidadeCurricular> ucs = ctrl.listarTodasUCs();
        if (ucs.isEmpty()) {
            System.out.println(GetYellow() + "Nenhuma UC registada at  ao momento!" + GetReset());
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
        System.out.println("\n" + GetBlue() + "--- Docentes Dispon veis ---" + GetReset());
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
        System.out.println("ID: " + unidadeCurricular.getId());
        System.out.println("Nome: " + unidadeCurricular.getNome());
        System.out.println("Ano Curricular: " + unidadeCurricular.getAnoCurricular());
        System.out.println("Semestre: " + unidadeCurricular.getSemestre());
        System.out.println("ECTS: " + unidadeCurricular.getEcts());
        if (unidadeCurricular.getDocente() != null) {
            System.out.println("Docente Respons vel: " + unidadeCurricular.getDocente().getNome() + " (" + unidadeCurricular.getDocente().getSigla() + ")");
        } else {
            System.out.println("Docente Respons vel: " + GetYellow() + "N O ATRIBU DO" + GetReset());
        }
        System.out.println(GetWhiteBold() + "-----------------------------------" + GetReset());
    }
}