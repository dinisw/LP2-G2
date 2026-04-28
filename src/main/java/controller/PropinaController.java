package controller;

import DAL.PropinaCRUD;
import DAL.EstudanteCRUD;
import model.Estudante;
import model.Propina;
import model.Resultado;

import java.util.ArrayList;
import java.util.List;

public class PropinaController {
    private PropinaCRUD propinaCRUD;
    // O valor da propina pode ser configurado por curso, mas para simplificar aplicamos uma constante
    private final double VALOR_PROPINA_PADRAO = 1000.0;

    public PropinaController() {
        this.propinaCRUD = new PropinaCRUD();
    }

    // 1. Gerar a Propina Anual (Chamado automaticamente quando o aluno progride ou se inscreve)
    public Resultado gerarPropinaAnual(int numeroMec, int anoLetivo) {
        Resultado resultado = new Resultado();

        // Verifica se a propina já existe para não duplicar dívidas
        if (propinaCRUD.procurarPropina(numeroMec, anoLetivo) != null) {
            resultado.success = false;
            resultado.errorMessage = "A propina para o " + anoLetivo + "º ano já foi gerada.";
            return resultado;
        }

        Propina novaPropina = new Propina(numeroMec, anoLetivo, VALOR_PROPINA_PADRAO, 0.0);
        if (propinaCRUD.registarPropina(novaPropina)) {
            resultado.success = true;
        } else {
            resultado.success = false;
            resultado.errorMessage = "Erro ao gerar a propina na base de dados.";
        }
        return resultado;
    }

    // 2. Realizar Pagamento (Total ou Parcial)
    public Resultado pagarPropina(int numeroMec, int anoLetivo, double valorPagamento) {
        Resultado resultado = new Resultado();

        if (valorPagamento <= 0) {
            resultado.success = false;
            resultado.errorMessage = "O valor do pagamento deve ser superior a zero.";
            return resultado;
        }

        Propina propina = propinaCRUD.procurarPropina(numeroMec, anoLetivo);
        if (propina == null) {
            resultado.success = false;
            resultado.errorMessage = "Propina não encontrada para o " + anoLetivo + "º ano.";
            return resultado;
        }

        if (propina.isTotalmentePaga()) {
            resultado.success = false;
            resultado.errorMessage = "Esta propina já se encontra totalmente paga. Obrigado!";
            return resultado;
        }

        if (valorPagamento > propina.getValorEmDivida()) {
            resultado.success = false;
            resultado.errorMessage = "O valor inserido excede o valor em dívida (" + String.format("%.2f", propina.getValorEmDivida()) + "€).";
            return resultado;
        }

        propina.registarPagamento(valorPagamento);

        if (propinaCRUD.atualizarPropina(propina)) {
            resultado.success = true;
            resultado.object = propina; // Retorna a propina atualizada para a View
        } else {
            resultado.success = false;
            resultado.errorMessage = "Erro ao guardar o pagamento no sistema.";
        }
        return resultado;
    }

    // 3. Listar Propinas de um Estudante (Para a EstudanteView)
    public List<Propina> consultarPropinasEstudante(int numeroMec) {
        return propinaCRUD.listarPropinasPorEstudante(numeroMec);
    }

    // 4. Listar Alunos em Dívida (Para a GestorView)
    public List<Estudante> obterAlunosEmDivida() {
        List<Propina> todas = propinaCRUD.getTodasPropinas();
        EstudanteCRUD estudanteCRUD = new EstudanteCRUD();
        List<Estudante> devedores = new ArrayList<>();
        List<Integer> mecsAdicionados = new ArrayList<>(); // Evita duplicar o mesmo aluno se dever 2 anos diferentes

        for (Propina p : todas) {
            if (!p.isTotalmentePaga()) {
                if (!mecsAdicionados.contains(p.getNumeroMecEstudante())) {
                    Estudante est = estudanteCRUD.lerEstudante(p.getNumeroMecEstudante());
                    if (est != null) {
                        devedores.add(est);
                        mecsAdicionados.add(p.getNumeroMecEstudante());
                    }
                }
            }
        }
        return devedores;
    }

    // 5. Método auxiliar: Verificar se tem dívidas num determinado ano
    public boolean isPropinaPaga(int numeroMec, int anoLetivo) {
        Propina p = propinaCRUD.procurarPropina(numeroMec, anoLetivo);
        if (p == null) return false;
        return p.isTotalmentePaga();
    }
}