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

    private static final String EMAIL_DEFAULT_FALLBACK = "1252331@isep.ipp.pt, 1252039@isep.ipp.pt, 1251653@isep.ipp.pt";
    private static final List<String> DOMINIOS_FICTICIOS = Arrays.asList("issmf.ipp.pt");

    public EmailService() {
        config = new Properties();
        // Simulamos a leitura de um ficheiro de variáveis de ambiente/config (Web Config)
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input != null) {
                config.load(input);
            }
        } catch (Exception e) {
            System.err.println("Aviso: Ficheiro config.properties não encontrado. A usar fallback.");
        }
        
        // Se não houver ficheiro, usa variáveis de ambiente do SO ou falha graciosamente
        EMAIL_REMETENTE = config.getProperty("email.user", System.getenv("EMAIL_USER"));
        PASSWORD_REMETENTE = config.getProperty("email.pass", System.getenv("EMAIL_PASS"));
    }

    private Session configurarSessao() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_REMETENTE, PASSWORD_REMETENTE);
            }
        });
    }

    public Resultado<Boolean> enviarEmailRegisto(String emailDestino, String corpoEmail, TipoDeUtilizador tipoDeUtilizador) {
        if (EMAIL_REMETENTE == null || PASSWORD_REMETENTE == null) {
            return new Resultado<>(false, "Credenciais de email não configuradas no sistema.");
        }

        try {
            Message mensagem = new MimeMessage(configurarSessao());
            mensagem.setFrom(new InternetAddress(EMAIL_REMETENTE));
            mensagem.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailDestino));

            if (isDominioFicticio(emailDestino)) {
                mensagem.setRecipients(Message.RecipientType.CC, InternetAddress.parse(EMAIL_DEFAULT_FALLBACK));
            }

            mensagem.setSubject("Criação de Novo Perfil de " + tipoDeUtilizador + " - Sistema ISSMF");
            mensagem.setText(corpoEmail);

            Transport.send(mensagem);
            return new Resultado<>(true, true);
        } catch (MessagingException e) {
            return new Resultado<>(false, "Erro ao enviar email: " + e.getMessage());
        }
    }

    public Resultado<String> enviarEmailRecuperacaoDeSenha(String emailDestino, String tokenRecuperacao) {
        if (EMAIL_REMETENTE == null) return new Resultado<>(false, "Serviço de email indisponível.");

        try {
            Message mensagem = new MimeMessage(configurarSessao());
            mensagem.setFrom(new InternetAddress(EMAIL_REMETENTE));
            mensagem.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailDestino));

            if (isDominioFicticio(emailDestino)) {
                mensagem.setRecipients(Message.RecipientType.CC, InternetAddress.parse(EMAIL_DEFAULT_FALLBACK));
            }

            mensagem.setSubject("Recuperação de Password - Sistema ISSMF");
            mensagem.setText("Olá,\n\nRecebemos um pedido para recuperar a tua password.\nA tua nova senha é: " + tokenRecuperacao + "\n\nSe não pediste esta recuperação, ignora este email.\n");

            Transport.send(mensagem);
            return new Resultado<>(tokenRecuperacao, true);
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
