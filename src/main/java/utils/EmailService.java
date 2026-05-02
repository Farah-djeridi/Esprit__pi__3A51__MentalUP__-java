package utils;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;
import java.util.Random;

public class EmailService {

    private static final String FROM_EMAIL    = AppConfig.get("email.from",     "");
    private static final String FROM_PASSWORD = AppConfig.get("email.password", "");

    /** Genere un code a 6 chiffres */
    public static String generateCode() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    /** Envoie le code 2FA */
    public static boolean sendCode(String toEmail, String code) {
        String subject = "MentalUp - Code de verification";
        String body = buildCodeHtml(code);
        return sendEmail(toEmail, subject, body);
    }


    /** Envoie un code de reinitialisation avec le prenom de l'utilisateur */
    public static boolean sendResetCode(String toEmail, String prenom, String code) {
        String subject = "MentalUp - Reinitialisation mot de passe";
        String body = "<html><body style='font-family:sans-serif;background:#F4F8FF;padding:30px;'>"
                + "<div style='max-width:480px;margin:auto;background:white;border-radius:16px;"
                + "padding:32px;box-shadow:0 4px 20px rgba(0,0,0,0.08);'>"
                + "<h2 style='color:#2C5F8A;margin-top:0'>Reinitialisation de mot de passe</h2>"
                + "<p style='color:#5A6C7D'>Bonjour <b>" + prenom + "</b>,</p>"
                + "<p style='color:#5A6C7D'>Votre code de reinitialisation est :</p>"
                + "<div style='background:#F0F6FF;border-radius:12px;padding:20px;text-align:center;"
                + "font-size:36px;font-weight:bold;color:#2C3E50;letter-spacing:12px;'>"
                + code + "</div>"
                + "<p style='color:#999;font-size:12px;margin-top:20px'>Ce code expire dans 15 minutes.</p>"
                + "</div></body></html>";
        return sendEmail(toEmail, subject, body);
    }

    /** Envoie un lien de reinitialisation de mot de passe */
    public static boolean sendResetLink(String toEmail, String token) {
        String subject = "MentalUp - Reinitialisation mot de passe";
        String link = "http://localhost:8989/reset?token=" + token;
        String body = "<html><body style='font-family:sans-serif;'>"
                + "<h2 style='color:#2C5F8A'>Reinitialisation de votre mot de passe</h2>"
                + "<p>Cliquez sur le lien ci-dessous pour reinitialiser votre mot de passe :</p>"
                + "<a href='" + link + "' style='background:#2C5F8A;color:white;padding:10px 20px;"
                + "border-radius:8px;text-decoration:none;'>Reinitialiser</a>"
                + "<p style='color:#999;font-size:12px;margin-top:20px'>Lien valide 15 minutes.</p>"
                + "</body></html>";
        return sendEmail(toEmail, subject, body);
    }

    private static boolean sendEmail(String to, String subject, String htmlBody) {
        if (FROM_EMAIL.isEmpty() || FROM_PASSWORD.isEmpty()) {
            System.err.println("Email non configure dans config.properties");
            return false;
        }
        try {
            boolean useSSL  = false;
            String  port    = "587";

            Properties props = new Properties();
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", port);
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.connectiontimeout", "10000");
            props.put("mail.smtp.timeout", "10000");

            if (useSSL) {
                props.put("mail.smtp.ssl.enable", "true");
                props.put("mail.smtp.socketFactory.port", port);
                props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            } else {
                props.put("mail.smtp.starttls.enable", "true");
            }

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(FROM_EMAIL, FROM_PASSWORD);
                }
            });

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject(subject, "UTF-8");
            message.setContent(htmlBody, "text/html; charset=UTF-8");
            Transport.send(message);
            System.out.println("Email envoye a : " + to);
            return true;

        } catch (Exception e) {
            System.err.println("Erreur envoi email : " + e.getMessage());
            return false;
        }
    }

    private static String buildCodeHtml(String code) {
        return "<html><body style='font-family:sans-serif;background:#F4F8FF;padding:30px;'>"
                + "<div style='max-width:480px;margin:auto;background:white;border-radius:16px;"
                + "padding:32px;box-shadow:0 4px 20px rgba(0,0,0,0.08);'>"
                + "<h2 style='color:#2C5F8A;margin-top:0'>MentalUp - Verification</h2>"
                + "<p style='color:#5A6C7D'>Votre code de verification est :</p>"
                + "<div style='background:#F0F6FF;border-radius:12px;padding:20px;text-align:center;"
                + "font-size:36px;font-weight:bold;color:#2C3E50;letter-spacing:12px;'>"
                + code + "</div>"
                + "<p style='color:#999;font-size:12px;margin-top:20px'>Ce code expire dans 10 minutes.</p>"
                + "</div></body></html>";
    }
}
