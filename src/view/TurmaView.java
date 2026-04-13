package view;

import common.utils.MenuUtils;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import static common.utils.DesignUtils.*;

public class TurmaView {
    private final Scanner scanner;

    public TurmaView() {
        this.scanner = new Scanner(System.in);
    }

    public void exiberMenuTurma(){
        String opcao;
        ArrayList<String> opcoes = new ArrayList<>();
        opcoes.add("1. Criar Turma");
        opcoes.add("2. Listar Turmas");
        opcoes.add("3. Procurar Turma");
        opcoes.add("4. Atualizar Turma");
        opcoes.add("5. Excluir Turma");
        opcoes.add("0. Voltar ao Menu de Gestão");

        do{
            try{
                MenuUtils.exibirSubTitulo("GESTÃO DE TURMAS", opcoes);
                System.out.println("\n" + GetWhiteBold() + "Selecione uma opção: " + GetReset());
                opcao = scanner.nextLine();

                switch (opcao) {
                    case "1":
                        registarTurmas();
                        break;
                    case "2":
                        listarTurmas();
                        break;
                    case "3":
                        procurarTurmas();
                        break;
                    case "4":
                        atualizarTurmas();
                        break;
                    case "5":
                        excluirTurmas();
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
    private void registarTurmas(){
    }

    private void listarTurmas(){
    }

    private void procurarTurmas(){}

    private void atualizarTurmas(){}

    private void excluirTurmas(){
    }
}