package controller;

import BLL.EstudanteCalculo;
import DAL.EstudanteCRUD;
import Common.SenhaUtils;
import model.Avaliacao;
import model.Estudante;
import view.EstudanteView;

import java.util.List;

public class EstudanteController {
    private Estudante model;
    private EstudanteView view;
    private EstudanteCalculo bll;

    public EstudanteController(Estudante model, EstudanteView view) {
        this.model = model;
        this.view = view;
    }

    public EstudanteController() {
        // Constructor for CRUD operations
    }

    public String exibirFichaEstudante() {
        String dataNascimentoStr = (model.getDataNascimento() != null) ? model.getDataNascimento().toString() : "Não definida";
        String cursoStr = (model.getNomeCurso() != null && !model.getNomeCurso().isEmpty()) ? model.getNomeCurso() : "Sem curso";

        var fichaDeEstudante = imprimirFichaEstudante(
                model.getNome(),
                model.getNumeroMec(),
                model.getEmail(),
                model.getNif(),
                dataNascimentoStr,
                model.getMorada(),
                cursoStr,
                model.getAnoLetivo()
        );
        return fichaDeEstudante;
    }
    public void exibirNotas() {
        imprimirNotas(model.getListaAvaliacoes());
    }
    public void tentarPassarDeAno(int totalUCsInscritas) {
        boolean passou = bll.verificarProgressao(totalUCsInscritas);
        if (passou) {
            mostrarMensagem("Sucesso: O estudante transitou para o " + model.getAnoLetivo() + "º ano letivo.");
        } else {
            mostrarMensagem("Falhou: O estudante falhou em cumprir os 60% de aproveitamento e manter-se-á no " + model.getAnoLetivo() + "º ano.");
        }
    }
    public String imprimirFichaEstudante (String nome, int numMec, String email, int nif, String dataNascimento, String morada, String curso, int anoLetivo) {
        return """
        FICHA DE ESTUDANTE
        Nome: %s
        Nº Mecanográfico: %s
        Email: %s
        NIF: %d
        Data Nascimento: %s
        Morada: %s
        Curso (Inscrição): %s
        Ano Letivo Atual: %dº Ano
        """.formatted(nome, numMec, email, nif, dataNascimento, morada, curso, anoLetivo);
    }

    public void imprimirNotas (List<Avaliacao> notas) {
        System.out.println("NOTAS DE AVALIAÇÃO");
        if (notas.isEmpty()) {
            System.out.println("O estudante ainda não possui notas registadas.");
        } else {
            for (Avaliacao avaliacao : notas) {
                String nomeUC = (avaliacao.getUnidadeCurricular() != null) ? avaliacao.getUnidadeCurricular().getNome() : "Desconhecida";
                System.out.println("UC: " + nomeUC + " | Momento: " + avaliacao.getMomento() + " | Nota: " + avaliacao.getNota());
            }
        }
    }
    public void mostrarMensagem(String mensagem) {
        System.out.println(mensagem);
    }


    public void inscreverEmCurso(){
//        Curso c = new Curso();
//        c.pegarCursos();


    }

    // CRUD methods
    private EstudanteCRUD estudanteCRUD = new EstudanteCRUD();

    public boolean registarEstudante(String nome, String morada, int nif, java.time.LocalDate dataNascimento, String curso,String hash, String salt) {
        int numeroMec = estudanteCRUD.gerarNumeroMecanografico();
        String email = numeroMec + "@isep.ipp.pt";
        Estudante estudante = new Estudante(nome, morada, nif, dataNascimento, email, numeroMec, hash, salt, curso);
        return estudanteCRUD.registarEstudante(estudante);
    }

    public List<Estudante> listarEstudantes() {
        return estudanteCRUD.getEstudantes();
    }

    public Estudante procurarEstudantePorNumeroMec(int numeroMec) {
        return estudanteCRUD.lerEstudante(numeroMec);
    }

    public boolean atualizarEstudante(int numeroMec, String nome, String morada, String email, String curso) {
        Estudante estudante = estudanteCRUD.lerEstudante(numeroMec);
        if (estudante != null) {
            if (nome != null) estudante.setNome(nome);
            if (morada != null) estudante.setMorada(morada);
            if (email != null) estudante.setEmail(email);
            if (curso != null) estudante.setNomeCurso(curso);
            return estudanteCRUD.atualizarEstudante(estudante);
        }
        return false;
    }

    public boolean eliminarEstudante(int numeroMec) {
        return estudanteCRUD.eliminarEstudante(numeroMec);
    }

    public int gerarNumeroMecanografico() {
        return estudanteCRUD.gerarNumeroMecanografico();
    }
}
