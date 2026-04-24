package utils;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;
import java.util.Random;

public class EmailService {

    private static final String FROM_EMAIL    = "sirine.klidi@esprit.tn";
    private static final String FROM_PASSWORD = "tbvc dlth sfmg amih";

    public static String generateCode() {
        int code = 100000 + new Random().nextInt(900000);
        return String.valueOf(code);
    }

    public static boolean sendCode(String toEmail, String code) {
        String subject = "🔐 MentalUp — Code de vérification";
        String html =
                "<div style='font-family:Arial; max-width:500px; margin:auto;'>" +
                        "<div style='background:#2C5F8A; padding:30px; text-align:center;'>" +
                        "<h1 style='color:white;'>MentalUp</h1>" +
                        "<p style='color:rgba(255,255,255,0.8);'>Vérification en deux étapes</p></div>" +
                        "<div style='background:#F0F4F8; padding:30px; text-align:center;'>" +
                        "<p style='color:#2C3E50; font-size:16px;'>Votre code :</p>" +
                        "<div style='background:white; border:2px solid #2C5F8A; border-radius:10px; padding:20px; margin:20px auto;'>" +
                        "<span style='font-size:40px; font-weight:bold; color:#2C5F8A; letter-spacing:10px;'>" + code + "</span></div>" +
                        "<p style='color:#E74C3C;'>⏱️ Expire dans <strong>5 minutes</strong></p></div></div>";
        return sendEmail(toEmail, subject, html);
    }

    public static boolean sendResetCode(String toEmail, String prenom, String code) {
        String subject = "🔑 MentalUp — Réinitialisation mot de passe";
        String html =
                "<div style='font-family:Arial; max-width:500px; margin:auto;'>" +
                        "<div style='background:#2C5F8A; padding:30px; text-align:center;'>" +
                        "<h1 style='color:white;'>MentalUp</h1></div>" +
                        "<div style='background:#F0F4F8; padding:30px;'>" +
                        "<p style='color:#2C3E50;'>Bonjour <strong>" + prenom + "</strong>,</p>" +
                        "<p>Votre code de réinitialisation :</p>" +
                        "<div style='background:white; border:2px solid #2C5F8A; border-radius:10px; padding:20px; text-align:center;'>" +
                        "<span style='font-size:40px; font-weight:bold; color:#2C5F8A; letter-spacing:10px;'>" + code + "</span></div>" +
                        "<p style='color:#E74C3C;'>⏱️ Expire dans <strong>10 minutes</strong></p></div></div>";
        return sendEmail(toEmail, subject, html);
    }

    private static boolean sendEmail(String toEmail, String subject, String htmlContent) {
        // ✅ Essai port 587 (TLS)
        if (trySend(toEmail, subject, htmlContent, "587", false)) return true;

        // ✅ Essai port 465 (SSL) si 587 échoue
        System.out.println("⚠️ Port 587 échoué, essai port 465...");
        if (trySend(toEmail, subject, htmlContent, "465", true)) return true;

        System.err.println("❌ Tous les ports ont échoué. Réseau bloqué ?");
        return false;
    }

    private static boolean trySend(String toEmail, String subject,
                                   String html, String port, boolean ssl) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", port);
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.connectiontimeout", "10000");
            props.put("mail.smtp.timeout", "10000");

            if (ssl) {
                props.put("mail.smtp.ssl.enable", "true");
                props.put("mail.smtp.socketFactory.port", port);
                props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            } else {
                props.put("mail.smtp.starttls.enable", "true");
            }

            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(FROM_EMAIL, FROM_PASSWORD);
                }
            });

            // 🔥 AJOUTEZ CES 2 LIGNES ICI (juste après la création de session)
            session.setDebug(true);  // ← Affiche les logs SMTP détaillés
            System.out.println("🔍 Tentative d'envoi sur port " + port + "...");

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setContent(html, "text/html; charset=UTF-8");
            Transport.send(message);

            System.out.println("✅ Email envoyé via port " + port + " à : " + toEmail);
            return true;

        } catch (Exception e) {
            System.err.println("❌ Port " + port + " : " + e.getMessage());
            // 🔥 AJOUTEZ CETTE LIGNE ICI (dans le catch)
            e.printStackTrace();  // ← Affiche l'erreur complète avec la cause
            return false;
        }
    }
}