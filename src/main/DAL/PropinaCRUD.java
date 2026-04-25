package main.DAL;

import main.model.Propina;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class PropinaCRUD {
    private static final String CAMINHO_FICHEIRO = "propinas.csv";
    private List<Propina> propinas;

    public PropinaCRUD() {
        this.propinas = new ArrayList<>();
        carregarFicheiro();
    }

    private void carregarFicheiro() {
        File ficheiro = new File(CAMINHO_FICHEIRO);
        if (!ficheiro.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(CAMINHO_FICHEIRO))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                String[] dados = linha.split(";");
                if (dados.length >= 4) {
                    Propina propina = new Propina(
                            Integer.parseInt(dados[0]), // Nº Mec
                            Integer.parseInt(dados[1]), // Ano Letivo
                            Double.parseDouble(dados[2]), // Valor Total
                            Double.parseDouble(dados[3])  // Valor Pago
                    );
                    propinas.add(propina);
                }
            }
        } catch (IOException | NumberFormatException e) {
            throw new RuntimeException("Erro interno ao carregar o ficheiro de propinas.", e);
        }
    }

    private void guardarTodosNoFicheiro() {
        try (PrintWriter print = new PrintWriter(new FileWriter(CAMINHO_FICHEIRO))) {
            for (Propina propina : propinas) {
                String linha = String.format("%d;%d;%.2f;%.2f",
                        propina.getNumeroMecEstudante(),
                        propina.getAnoLetivo(),
                        propina.getValorTotal(),
                        propina.getValorPago()).replace(",", "."); // Garantir ponto nas decimais
                print.println(linha);
            }
        } catch (IOException e) {
            throw new RuntimeException("Erro interno ao guardar o ficheiro de propinas.", e);
        }
    }

    // CREATE
    public boolean registarPropina(Propina propina) {
        if (propina != null) {
            propinas.add(propina);
            guardarTodosNoFicheiro();
            return true;
        }
        return false;
    }

    // READ: Devolve todas as propinas de um estudante específico
    public List<Propina> listarPropinasPorEstudante(int numeroMec) {
        List<Propina> resultado = new ArrayList<>();
        for (Propina p : propinas) {
            if (p.getNumeroMecEstudante() == numeroMec) {
                resultado.add(p);
            }
        }
        return resultado;
    }

    // READ: Procura a propina exata de um aluno num determinado ano
    public Propina procurarPropina(int numeroMec, int anoLetivo) {
        for (Propina p : propinas) {
            if (p.getNumeroMecEstudante() == numeroMec && p.getAnoLetivo() == anoLetivo) {
                return p;
            }
        }
        return null;
    }

    // READ: Lista global (útil para o Gestor ver os devedores)
    public List<Propina> getTodasPropinas() {
        return new ArrayList<>(propinas);
    }

    // UPDATE: Atualiza os valores (útil para quando o aluno faz um pagamento)
    public boolean atualizarPropina(Propina propinaAtualizada) {
        for (int i = 0; i < propinas.size(); i++) {
            Propina p = propinas.get(i);
            if (p.getNumeroMecEstudante() == propinaAtualizada.getNumeroMecEstudante() &&
                    p.getAnoLetivo() == propinaAtualizada.getAnoLetivo()) {

                propinas.set(i, propinaAtualizada);
                guardarTodosNoFicheiro();
                return true;
            }
        }
        return false;
    }
}