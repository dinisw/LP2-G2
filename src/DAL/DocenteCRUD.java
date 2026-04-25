package DAL;

import model.Docente;
import model.Resultado;
import model.UnidadeCurricular;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DocenteCRUD {
    private static final String CAMINHO_FICHEIRO = "docentes.csv";
    private List<Docente> docentes;

    // VÁRIAVEL DE SEGURANÇA: Impede o ciclo infinito (StackOverflowError)
    private static boolean carregandoRelacoes = false;

    public DocenteCRUD() {
        this.docentes = new ArrayList<>();
        carregarFicheiro();
    }

    private void carregarFicheiro() {
        File ficheiro = new File(CAMINHO_FICHEIRO);
        if (!ficheiro.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(CAMINHO_FICHEIRO))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                String[] dados = linha.split(";");

                // O formato base tem pelo menos 7 campos fixos (índices 0 a 6)
                if (dados.length >= 7) {
                    Docente docente = new Docente(
                            dados[0], // nome
                            dados[1], // morada
                            Integer.parseInt(dados[2]), // nif
                            LocalDate.parse(dados[3]), // dataNascimento
                            dados[4], // email
                            dados[5], // hash
                            dados[6], // sigla
                            new ArrayList<>(), // listaAvaliacao
                            new ArrayList<>()  // unidadesCurriculares
                    );
                    docentes.add(docente);
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.out.println("Erro ao carregar docentes: " + e.getMessage());
        }

        // ------------------------------------------------------------------
        // QUEBRA DO CICLO INFINITO (LAZY LOADING PROTEGIDO)
        // Em vez de instanciar a classe no topo, chamamos apenas uma vez!
        // ------------------------------------------------------------------
        if (!carregandoRelacoes) {
            carregandoRelacoes = true; // Tranca a porta

            UnidadeCurricularCRUD ucCRUD = new UnidadeCurricularCRUD();
            List<UnidadeCurricular> todasUCs = ucCRUD.getUnidadeCurriculars();

            // Sincronização Perfeita: Vamos buscar as UCs à fonte (UnidadeCurricularCRUD)
            // e associá-las aos respetivos docentes.
            for (Docente docente : docentes) {
                for (UnidadeCurricular unidadeCurricular : todasUCs) {
                    if (unidadeCurricular.getDocente() != null && unidadeCurricular.getDocente().getSigla().equalsIgnoreCase(docente.getSigla())) {
                        // Se a UC pertence a este docente, adicionamo-la à lista dele
                        docente.adicionarUnidadeCurricular(unidadeCurricular);
                    }
                }
            }

            carregandoRelacoes = false; // Destranca a porta
        }
    }

    private void guardarTodosNoFicheiro() {
        try (PrintWriter print = new PrintWriter(new FileWriter(CAMINHO_FICHEIRO))) {
            for (Docente docente : docentes) {

                // CORREÇÃO CRÍTICA: Juntar com VÍRGULA (",") e não com PONTO E VÍRGULA (";")
                // Se usarmos ";", o CSV fica corrompido na leitura!
                String ucNames = docente.getUnidadesCurriculares().stream()
                        .map(UnidadeCurricular::getNome)
                        .collect(Collectors.joining(","));

                String linha = String.format("%s;%s;%s;%s;%s;%s;%s;%s",
                        safe(docente.getNome()),
                        safe(docente.getMorada()),
                        safe(docente.getNif()),
                        safe(docente.getDataNascimento()),
                        safe(docente.getEmail()),
                        safe(docente.getHash()),
                        safe(docente.getSigla()),
                        safe(ucNames.isEmpty() ? null : ucNames));
                print.println(linha);
            }
        } catch (IOException e) {
            System.out.println("Erro ao guardar docentes: " + e.getMessage());
        }
    }

    // CREATE
    public boolean registarDocente(Docente docente) {
        if (docente != null && procurarPorNif(docente.getNif()) == null) {
            docentes.add(docente);
            guardarTodosNoFicheiro();
            return true;
        }
        return false;
    }

    // READ
    public List<Docente> getDocentes() {
        return new ArrayList<>(docentes);
    }

    public Docente procurarPorNif(int nif) {
        for (Docente docente : docentes) {
            if (docente.getNif() == nif) {
                return docente;
            }
        }
        return null;
    }

    public Docente procurarPorSigla(String sigla) {
        if (sigla == null || sigla.trim().isEmpty()) return null;
        for (Docente docente : docentes) {
            if (docente.getSigla().equalsIgnoreCase(sigla)) {
                return docente;
            }
        }
        return null;
    }

    // UPDATE SENHA
    public Resultado atualizarSenha(Docente docente){
        Resultado resultado = new Resultado();
        if(docente != null){
            for (int i = 0; i < docentes.size(); i++) {
                if (docentes.get(i).getNif() == docente.getNif()) {
                    docentes.set(i, docente);
                    guardarTodosNoFicheiro();
                    resultado.success = true;
                    return resultado;
                }
            }
        }
        resultado.success = false;
        resultado.errorMessage = "Erro ao atualizar o ficheiro do docente";
        return resultado;
    }

    // UPDATE
    public boolean atualizarDocente(Docente docenteAtualizado) {
        for (int i = 0; i < docentes.size(); i++) {
            if (docentes.get(i).getNif() == docenteAtualizado.getNif()) {
                docentes.set(i, docenteAtualizado);
                guardarTodosNoFicheiro();
                return true;
            }
        }
        return false;
    }

    // DELETE
    public boolean eliminarDocente(int nif) {
        for (int i = 0; i < docentes.size(); i++) {
            if (docentes.get(i).getNif() == nif) {
                docentes.remove(i);
                guardarTodosNoFicheiro();
                return true;
            }
        }
        return false;
    }

    // MÉTODO AUXILIAR PARA O CONTROLLER (Verificação de Dependências no Curso)
    public int contarDocentesNoCurso(String nomeCurso) {
        int count = 0;
        for (Docente docente : docentes) {
            for (UnidadeCurricular unidadeCurricular : docente.getUnidadesCurriculares()) {
                // Se a lógica do seu projeto ligar UCs ao Curso, poderá validar aqui
                // (Por agora retorna 0, pode ser expandido conforme a sua estrutura)
            }
        }
        return count;
    }

    private String safe(Object o){
        return (o == null || o.toString().trim().isEmpty()) ? "SEM REGISTO" : o.toString();
    }
}