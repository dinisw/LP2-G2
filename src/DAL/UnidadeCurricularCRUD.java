package DAL;
import model.UnidadeCurricular;
import model.Docente;
import model.Curso;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class UnidadeCurricularCRUD {
    private static final String CAMINHO_FICHEIRO = "ucs.csv";
    private List<UnidadeCurricular> ucs;

    public UnidadeCurricularCRUD() {
        this.ucs = new ArrayList<>();
        carregarFicheiro();
    }

    private void carregarFicheiro() {
        File ficheiro = new File(CAMINHO_FICHEIRO);
        if (!ficheiro.exists()) return;

        DocenteCRUD docCRUD = new DocenteCRUD();

        try (BufferedReader reader = new BufferedReader(new FileReader(CAMINHO_FICHEIRO))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                String[] dados = linha.split(";");
                if (dados.length >= 3) {
                    String nome = dados[0];
                    int ano = Integer.parseInt(dados[1]);
                    String siglaDocente = dados[2];

                    Docente docente = docCRUD.procurarPorSigla(siglaDocente);
                    UnidadeCurricular uc = new UnidadeCurricular(nome, ano, docente);
                    ucs.add(uc);

                    // Re-associar UC aos cursos (isso deve ser feito no carregamento dos cursos ou numa fase posterior)
                    // Mas para o CRUD de UC, apenas mantemos a lista de UCs existentes.
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.out.println("Erro ao carregar UCs: " + e.getMessage());
        }
    }

    public void guardarTodosNoFicheiro() {
        try (PrintWriter print = new PrintWriter(new FileWriter(CAMINHO_FICHEIRO))) {
            for (UnidadeCurricular uc : ucs) {
                String siglaDocente = (uc.getDocente() != null) ? uc.getDocente().getSigla() : "SEM_DOCENTE";
                print.println(uc.getNome() + ";" + uc.getAnoCurricular() + ";" + siglaDocente);
            }
        } catch (IOException e) {
            System.out.println("Erro ao guardar UCs: " + e.getMessage());
        }
    }

    public boolean registarUC(UnidadeCurricular uc) {
        if (uc != null) {
            // Garantir que apenas um docente é responsável por uma determinada UC (nesta lista global)
            // Se a UC já existe (pelo nome), não registamos de novo.
            if (procurarPorNome(uc.getNome()) != null) {
                return false; 
            }
            
            // Validar se o docente já é responsável por outra UC (opcional, dependendo da interpretação de "apenas um docente é responsável por UMA determinada UC")
            // A frase "apenas um docente é responsável por uma determinada unidade curricular" significa que a UC 'Matemática' só pode ter UM docente.
            // E não necessariamente que o docente só possa ter uma UC.
            
            ucs.add(uc);
            guardarTodosNoFicheiro();
            return true;
        }
        return false;
    }

    public List<UnidadeCurricular> getUcs() {
        return new ArrayList<>(ucs);
    }

    public UnidadeCurricular procurarPorNome(String nome) {
        for (UnidadeCurricular uc : ucs) {
            if (uc.getNome().equalsIgnoreCase(nome)) {
                return uc;
            }
        }
        return null;
    }
}
