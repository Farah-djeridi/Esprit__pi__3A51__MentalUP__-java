package services;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import models.Activite;
import models.Reservation;

import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class ServiceEmail {

    // ── Configuration SMTP ────────────────────────────────────────────────────
    // Option 1: Gmail (nécessite App Password)
    // Option 2: Mailtrap (pour tests - gratuit sur mailtrap.io)
    private static String SMTP_HOST     = "smtp-relay.brevo.com";
    private static int    SMTP_PORT     = 587;
    private static String EMAIL_SENDER  = ""; // Ton email Brevo
    private static String SMTP_USERNAME = ""; // Ton email Brevo
    private static String EMAIL_PASSWORD = ""; // Clé SMTP Brevo

    /** Mettre à jour les credentials depuis l'interface admin */
    public static void setCredentials(String email, String username, String password) {
        EMAIL_SENDER   = email;
        SMTP_USERNAME  = username;
        EMAIL_PASSWORD = password;
    }

    public static String getEmailSender()  { return EMAIL_SENDER;  }
    public static String getSmtpUsername() { return SMTP_USERNAME; }

    /** Tester la connexion SMTP */
    public static boolean testerConnexion() {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth",            "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host",            SMTP_HOST);
            props.put("mail.smtp.port",            String.valueOf(SMTP_PORT));
            props.put("mail.smtp.ssl.trust",       "*");
            props.put("mail.smtp.connectiontimeout", "5000");
            props.put("mail.smtp.timeout",           "5000");

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(
                        SMTP_USERNAME.isEmpty() ? EMAIL_SENDER : SMTP_USERNAME,
                        EMAIL_PASSWORD);
                }
            });
            Transport transport = session.getTransport("smtp");
            transport.connect(SMTP_HOST, SMTP_PORT,
                SMTP_USERNAME.isEmpty() ? EMAIL_SENDER : SMTP_USERNAME,
                EMAIL_PASSWORD);
            transport.close();
            return true;
        } catch (Exception e) {
            System.err.println("Test SMTP échoué: " + e.getMessage());
            return false;
        }
    }

    /**
     * Envoie un email de notification de statut de réservation.
     */
    public void envoyerNotificationStatut(Reservation reservation, Activite activite, String statut) {
        Thread thread = new Thread(() -> {
            try {
                String destinataire = getEmailEtudiant(reservation.getNomEtudiant());
                if (destinataire == null || destinataire.isEmpty()) {
                    System.out.println("⚠ Pas d'email pour : " + reservation.getNomEtudiant());
                    return;
                }

                String sujet  = construireSujet(statut, activite);
                String corps  = construireCorps(statut, reservation, activite);

                envoyerEmail(destinataire, sujet, corps);
                System.out.println("✅ Email envoyé à " + destinataire + " [" + statut + "]");

            } catch (MessagingException | java.io.UnsupportedEncodingException e) {
                System.err.println("❌ Erreur envoi email: " + e.getMessage());
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void envoyerEmail(String destinataire, String sujet, String corps)
            throws MessagingException, java.io.UnsupportedEncodingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth",            "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host",            SMTP_HOST);
        props.put("mail.smtp.port",            String.valueOf(SMTP_PORT));
        props.put("mail.smtp.ssl.trust",       "*");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                    SMTP_USERNAME.isEmpty() ? EMAIL_SENDER : SMTP_USERNAME,
                    EMAIL_PASSWORD);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(EMAIL_SENDER, "MentalUp"));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinataire));
        message.setSubject(sujet);
        message.setContent(corps, "text/html; charset=UTF-8");

        Transport.send(message);
    }

    private String construireSujet(String statut, Activite activite) {
        return switch (statut) {
            case "ACCEPTEE" -> "✅ Réservation confirmée - " + activite.getTitre();
            case "REFUSEE"  -> "❌ Réservation refusée - " + activite.getTitre();
            default         -> "⏳ Mise à jour de votre réservation - " + activite.getTitre();
        };
    }

    private String construireCorps(String statut, Reservation reservation, Activite activite) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        String couleurStatut = switch (statut) {
            case "ACCEPTEE" -> "#38a169";
            case "REFUSEE"  -> "#e53e3e";
            default         -> "#ed8936";
        };
        String iconeStatut = switch (statut) {
            case "ACCEPTEE" -> "✅";
            case "REFUSEE"  -> "❌";
            default         -> "⏳";
        };
        String messageStatut = switch (statut) {
            case "ACCEPTEE" -> "Votre réservation a été <strong>acceptée</strong> ! Présentez votre ticket le jour de l'activité.";
            case "REFUSEE"  -> "Nous sommes désolés, votre réservation a été <strong>refusée</strong>. Vous pouvez réserver une autre activité.";
            default         -> "Le statut de votre réservation a été mis à jour.";
        };

        return """
            <!DOCTYPE html>
            <html>
            <head>
              <meta charset="UTF-8"/>
              <style>
                body { font-family: Arial, sans-serif; background: #f5f7fa; margin: 0; padding: 20px; }
                .container { max-width: 560px; margin: 0 auto; background: white;
                             border-radius: 16px; overflow: hidden;
                             box-shadow: 0 4px 20px rgba(0,0,0,0.1); }
                .header { background: #2d3748; padding: 30px; text-align: center; }
                .header h1 { color: white; margin: 0; font-size: 24px; }
                .header p  { color: #a0aec0; margin: 5px 0 0; font-size: 13px; }
                .status-banner { background: %s; padding: 20px; text-align: center; }
                .status-banner .icon { font-size: 40px; }
                .status-banner p { color: white; font-size: 16px; font-weight: bold; margin: 8px 0 0; }
                .body { padding: 28px; }
                .info-grid { background: #f7fafc; border-radius: 10px; padding: 18px; margin: 16px 0; }
                .info-row { display: flex; justify-content: space-between; padding: 6px 0;
                            border-bottom: 1px solid #e2e8f0; font-size: 13px; }
                .info-row:last-child { border-bottom: none; }
                .info-label { color: #718096; }
                .info-value { color: #2d3748; font-weight: bold; }
                .footer { background: #f7fafc; padding: 16px; text-align: center;
                          font-size: 11px; color: #a0aec0; }
                .btn { display: inline-block; background: %s; color: white;
                       padding: 12px 28px; border-radius: 8px; text-decoration: none;
                       font-weight: bold; font-size: 14px; margin: 16px 0; }
              </style>
            </head>
            <body>
              <div class="container">
                <div class="header">
                  <h1>🧠 MentalUp</h1>
                  <p>Système de gestion des activités</p>
                </div>
                <div class="status-banner" style="background:%s">
                  <div class="icon">%s</div>
                  <p>%s</p>
                </div>
                <div class="body">
                  <p style="color:#2d3748;font-size:15px;">Bonjour <strong>%s</strong>,</p>
                  <p style="color:#718096;font-size:13px;">%s</p>
                  <div class="info-grid">
                    <div class="info-row">
                      <span class="info-label">🎭 Activité</span>
                      <span class="info-value">%s</span>
                    </div>
                    <div class="info-row">
                      <span class="info-label">🪑 Place</span>
                      <span class="info-value">%s</span>
                    </div>
                    <div class="info-row">
                      <span class="info-label">📅 Date réservation</span>
                      <span class="info-value">%s</span>
                    </div>
                    <div class="info-row">
                      <span class="info-label">📍 Lieu</span>
                      <span class="info-value">%s</span>
                    </div>
                    <div class="info-row">
                      <span class="info-label">🗓 Période</span>
                      <span class="info-value">%s → %s</span>
                    </div>
                    <div class="info-row">
                      <span class="info-label">📌 Statut</span>
                      <span class="info-value" style="color:%s">%s %s</span>
                    </div>
                  </div>
                  <p style="color:#718096;font-size:12px;">
                    Réservation N° <strong>#%d</strong>
                  </p>
                </div>
                <div class="footer">
                  MentalUp © %d — Esprit School of Engineering<br/>
                  Cet email a été envoyé automatiquement, merci de ne pas y répondre.
                </div>
              </div>
            </body>
            </html>
            """.formatted(
                couleurStatut, couleurStatut, couleurStatut,
                iconeStatut,
                switch (statut) {
                    case "ACCEPTEE" -> "Réservation Confirmée";
                    case "REFUSEE"  -> "Réservation Refusée";
                    default         -> "Mise à jour";
                },
                reservation.getNomEtudiant(),
                messageStatut,
                activite.getTitre(),
                reservation.getPlace(),
                reservation.getDateReservation() != null ? reservation.getDateReservation().format(fmt) : "-",
                activite.getAdresse() != null ? activite.getAdresse() : "-",
                activite.getDateDebut() != null ? activite.getDateDebut().format(fmt) : "-",
                activite.getDateFin()   != null ? activite.getDateFin().format(fmt)   : "-",
                couleurStatut, iconeStatut, statut,
                reservation.getIdReservation(),
                java.time.LocalDate.now().getYear()
        );
    }

    /**
     * Récupère l'email d'un étudiant depuis son nom.
     * En production, faire une vraie requête BDD sur la table utilisateurs.
     */
    private String getEmailEtudiant(String nomEtudiant) {
        // Mapping simple — à remplacer par une vraie requête BDD
        if (nomEtudiant == null) return null;
        return switch (nomEtudiant.trim()) {
            case "Sophie Am." -> "sophie.am@esprit.tn";
            default -> nomEtudiant.toLowerCase()
                        .replace(" ", ".")
                        .replace("..", ".")
                        + "@esprit.tn";
        };
    }
}
