package model;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;
import java.util.List;
import java.util.Arrays;

public class EmailService {

    private static final String SMTP_HOST = "smtp.office365.com";
    private static final String SMTP_PORT = "587"; // Porta para TLS
    private static final String EMAIL_REMETENTE = ""; //por defenir;
    private static final String PASSWORD_REMETENTE = "dZ3T7Qkbye";

    private static final String EMAIL_DEFAULT_FALLBACK = "1252331@isep.ipp.pt, 1252039@isep.ipp.pt, 1251653@isep.ipp.pt";

    private static final List<String> DOMINIOS_FICTICIOS = Arrays.asList(
            "issmf.ipp.pt"
    );

    public Resultado enviarEmailRegisto(String emailDestino, String corpoEmail, TipoDeUtilizador tipoDeUtilizador) {
        Resultado res = new Resultado();
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_REMETENTE, PASSWORD_REMETENTE);
            }
        });

        try {
            Message mensagem = new MimeMessage(session);
            mensagem.setFrom(new InternetAddress(EMAIL_REMETENTE));

            mensagem.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailDestino));

            if (isDominioFicticio(emailDestino)) {
                mensagem.setRecipients(Message.RecipientType.CC, InternetAddress.parse(EMAIL_DEFAULT_FALLBACK));
            }

            mensagem.setSubject("Criação de Novo Perfil de " + tipoDeUtilizador + " - Sistema ISSMF");

            mensagem.setText(corpoEmail);

            Transport.send(mensagem);
            res.success = true;

        } catch (MessagingException e) {
            res.success = false;
            res.errorMessage ="Erro ao enviar email: " + e.getMessage();
            e.printStackTrace();
        }
        return res;
    }
    public Resultado enviarEmailRecuperacaoDeSenha(String emailDestino, String tokenRecuperacao) {
        Resultado res = new Resultado();
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_REMETENTE, PASSWORD_REMETENTE);
            }
        });

        try {
            Message mensagem = new MimeMessage(session);
            mensagem.setFrom(new InternetAddress(EMAIL_REMETENTE));

            mensagem.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailDestino));

            if (isDominioFicticio(emailDestino)) {
                mensagem.setRecipients(Message.RecipientType.CC, InternetAddress.parse(EMAIL_DEFAULT_FALLBACK));
            }

            mensagem.setSubject("Recuperação de Password - Sistema ISSMF");

            String corpoEmail = "Olá,\n\n"
                    + "Recebemos um pedido para recuperar a tua password.\n"
                    + "Sua nova senha é: " + tokenRecuperacao + "\n\n"
                    + "Se não pediste esta recuperação, ignora este email.\n";

            mensagem.setText(corpoEmail);

            Transport.send(mensagem);
            res.success = true;
            res.object = tokenRecuperacao;

        } catch (MessagingException e) {
            res.success = false;
            res.errorMessage ="Erro ao enviar email: " + e.getMessage();
            e.printStackTrace();
        }
        return res;
    }

    private boolean isDominioFicticio(String email) {
        if (email == null || !email.contains("@")) {
            return false;
        }

        String dominioDoEmail = email.substring(email.indexOf("@") + 1).toLowerCase();

        for (String dominioFicticio : DOMINIOS_FICTICIOS) {
            if (dominioDoEmail.equals(dominioFicticio) || dominioDoEmail.endsWith("." + dominioFicticio)) {
                return true;
            }
        }
        return false;
    }
}
