package controller;

import DAL.DAOFactory;
import DAL.IHorarioDAO;
import DAL.IPresencaDAO;
import model.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controla o fluxo de 2 passos para marcação de presenças (Enunciado v1.3):
 *   1. Docente marca a sua presença (abre o registo)
 *   2. Estudante confirma a sua presença (só possível após o docente)
 */
public class PresencaController {
    private final IPresencaDAO presencaDAO;
    private final IHorarioDAO horarioDAO;

    public PresencaController() {
        this.presencaDAO = DAOFactory.getPresencaDAO();
        this.horarioDAO  = DAOFactory.getHorarioDAO();
    }

    public Resultado<Presenca> marcarPresencaDocente(int horarioId, int numeroMec, LocalDate data) {
        Horario horario = horarioDAO.procurarPorId(horarioId);
        if (horario == null) return new Resultado<>(false, "Horário não encontrado.");

        Presenca existente = encontrarPresenca(horarioId, numeroMec, data);
        if (existente != null) {
            if (existente.isPresencaDocente()) return new Resultado<>(false, "A presença do docente já foi registada para esta aula.");
            existente.setPresencaDocente(true);
            presencaDAO.atualizarPresenca(existente);
            return new Resultado<>(existente, true);
        }

        EstudanteController estCtrl = new EstudanteController();
        Estudante est = estCtrl.procurarEstudantePorNumeroMec(numeroMec);
        if (est == null) return new Resultado<>(false, "Estudante não encontrado.");
        if (!est.isAtivo()) return new Resultado<>(false, "O estudante está inativo.");

        Presenca nova = new Presenca(est, horario, data);
        nova.setPresencaDocente(true);
        presencaDAO.registarPresenca(nova);
        return new Resultado<>(nova, true);
    }

    public Resultado<Presenca> marcarPresencaEstudante(int horarioId, int numeroMec, LocalDate data) {
        Presenca existente = encontrarPresenca(horarioId, numeroMec, data);
        if (existente == null)
            return new Resultado<>(false, "O docente ainda não marcou presença para esta aula. Aguarde.");
        if (!existente.isPresencaDocente())
            return new Resultado<>(false, "O docente ainda não marcou presença para esta aula. Aguarde.");
        if (existente.isPresencaEstudante())
            return new Resultado<>(false, "A sua presença já foi registada.");

        existente.setPresencaEstudante(true);
        presencaDAO.atualizarPresenca(existente);
        return new Resultado<>(existente, true);
    }

    private Presenca encontrarPresenca(int horarioId, int numeroMec, LocalDate data) {
        return presencaDAO.listarPorEstudante(numeroMec).stream()
                .filter(p -> p.getHorario().getId() == horarioId && p.getData().equals(data))
                .findFirst().orElse(null);
    }

    public List<Presenca> listarFaltasPorUC(int ucId) {
        return presencaDAO.listarFaltasPorUC(ucId);
    }

    public List<Presenca> listarPresencasPorEstudante(int numeroMec) {
        return presencaDAO.listarPorEstudante(numeroMec);
    }

    public String obterResumoFaltasEstudante(int numeroMec) {
        List<Presenca> presencas = presencaDAO.listarPorEstudante(numeroMec);
        long total = presencas.stream().filter(Presenca::isPresencaDocente).count();
        long faltas = presencas.stream().filter(Presenca::isFalta).count();
        long presentes = total - faltas;
        return String.format("Total de aulas: %d | Presente: %d | Falta: %d", total, presentes, faltas);
    }

    public List<Presenca> listarTodasPresencas() {
        return presencaDAO.listarTodas();
    }
}
