package model;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class EmailService {
    private final Properties config;
    private final String EMAIL_REMETENTE;
    private final String PASSWORD_REMETENTE;
    private final String FALLBACK_EMAILS;
    private final List<String> DOMINIOS_FICTICIOS;

    public EmailService() {
        config = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input != null) config.load(input);
        } catch (Exception e) {
            System.err.println("Aviso: config.properties não encontrado. A usar valores hardcoded de fallback.");
        }

        EMAIL_REMETENTE = config.getProperty("email.user", System.getenv("EMAIL_USER"));
        PASSWORD_REMETENTE = config.getProperty("email.pass", System.getenv("EMAIL_PASS"));

        FALLBACK_EMAILS = config.getProperty("email.fallback", "1252331@isep.ipp.pt, 1252039@isep.ipp.pt, 1251653@isep.ipp.pt");

        String dominios = config.getProperty("email.dominios.ficticios", "issmf.ipp.pt");
        DOMINIOS_FICTICIOS = Arrays.asList(dominios.split(",\\s*"));
    }

    private Session configurarSessao() {
        Properties props = new Properties();

        props.put("mail.smtp.auth", config.getProperty("email.smtp.auth", "true"));
        props.put("mail.smtp.starttls.enable", config.getProperty("email.smtp.starttls.enable", "true"));
        props.put("mail.smtp.host", config.getProperty("email.smtp.host", "smtp.gmail.com"));
        props.put("mail.smtp.port", config.getProperty("email.smtp.port", "587"));
        props.put("mail.smtp.ssl.protocols", config.getProperty("email.smtp.ssl.protocols", "TLSv1.2"));

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_REMETENTE, PASSWORD_REMETENTE);
            }
        });
    }

    public Resultado<Boolean> enviarEmailRegisto(String emailDestino, String corpoEmail, TipoDeUtilizador tipoDeUtilizador) {
        String assunto = "Criação de Novo Perfil de " + tipoDeUtilizador + " - Sistema ISSMF";
        return enviarEmailGenerico(emailDestino, assunto, corpoEmail);
    }

    public Resultado<String> enviarEmailRecuperacaoDeSenha(String emailDestino, String tokenRecuperacao) {
        String assunto = "Recuperação de Password - Sistema ISSMF";
        String corpo = "Olá,\n\nRecebemos um pedido para recuperar a tua password.\nA tua nova senha é: " + tokenRecuperacao + "\n\nSe não pediste esta recuperação, ignora este email.\n";

        Resultado<Boolean> envio = enviarEmailGenerico(emailDestino, assunto, corpo);

        if (envio.sucesso) {
            return new Resultado<>(tokenRecuperacao, true);
        } else {
            return new Resultado<>(false, envio.mensagemErro);
        }
    }

    private Resultado<Boolean> enviarEmailGenerico(String emailDestino, String assunto, String corpo) {
        if (EMAIL_REMETENTE == null || PASSWORD_REMETENTE == null) {
            return new Resultado<>(false, "Credenciais de email não configuradas no sistema.");
        }
        try {
            Message mensagem = new MimeMessage(configurarSessao());
            mensagem.setFrom(new InternetAddress(EMAIL_REMETENTE));
            mensagem.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailDestino));

            if (isDominioFicticio(emailDestino)) {
                mensagem.setRecipients(Message.RecipientType.CC, InternetAddress.parse(FALLBACK_EMAILS));
            }

            mensagem.setSubject(assunto);
            mensagem.setText(corpo);
            Transport.send(mensagem);

            return new Resultado<>(true, true);
        } catch (MessagingException e) {
            return new Resultado<>(false, "Erro ao enviar email: " + e.getMessage());
        }
    }

    private boolean isDominioFicticio(String email) {
        if (email == null || !email.contains("@")) return false;
        String dominioDoEmail = email.substring(email.indexOf("@") + 1).toLowerCase();
        return DOMINIOS_FICTICIOS.stream().anyMatch(d -> dominioDoEmail.equals(d) || dominioDoEmail.endsWith("." + d));
    }
}