package services;

import javafx.scene.control.Alert;
import models.Notification;
import utils.MyDataBase;
import models.TipData;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationService {

    private static final String TABLE_NAME = "notification";
    private final Connection cnx;

    public NotificationService() {
        cnx = MyDataBase.getInstance().getCnx();
        System.out.println("NotificationService initialisé. Connexion = " + cnx);
    }

    public void ajouterNotification(String type, String title, String message,
                                    int userId, Integer objectifId, Integer suiviId) {

        String sql = "INSERT INTO " + TABLE_NAME + " (type, title, message, is_read, created_at, user_id, objectif_id, suivi_id) " +
                "VALUES (?, ?, ?, ?, NOW(), ?, ?, ?)";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, type);
            ps.setString(2, title);
            ps.setString(3, message);
            ps.setInt(4, 0);
            ps.setInt(5, userId);

            if (objectifId != null) {
                ps.setInt(6, objectifId);
            } else {
                ps.setNull(6, Types.INTEGER);
            }

            if (suiviId != null) {
                ps.setInt(7, suiviId);
            } else {
                ps.setNull(7, Types.INTEGER);
            }

            int rows = ps.executeUpdate();
            System.out.println("Notification ajoutée. Lignes insérées = " + rows);

        } catch (SQLException e) {
            System.out.println("Erreur SQL ajout notification : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<Notification> getNotificationsByUser(int userId) {
        List<Notification> list = new ArrayList<>();

        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE user_id = ? ORDER BY created_at DESC";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Notification n = mapNotification(rs);
                    list.add(n);
                }
            }

        } catch (SQLException e) {
            System.out.println("Erreur récupération notifications : " + e.getMessage());
            e.printStackTrace();
        }

        return list;
    }
    public void notifierMicroExercice(TipData tip, int userId, Integer objectifId, Integer suiviId) {
        if (tip == null) {
            return;
        }

        String title = tip.getTitle() != null && !tip.getTitle().isBlank()
                ? tip.getTitle()
                : "Conseil bien-être";

        String text = tip.getText() != null ? tip.getText().trim() : "";
        String exercise = tip.getExercise() != null ? tip.getExercise().trim() : "";
        String source = tip.getSource() != null ? tip.getSource().trim() : "";
        String url = tip.getUrl() != null ? tip.getUrl().trim() : "";

        StringBuilder messageBuilder = new StringBuilder();

        if (!text.isBlank()) {
            messageBuilder.append(text);
        }

        if (!exercise.isBlank()) {
            if (messageBuilder.length() > 0) {
                messageBuilder.append("\n\n");
            }
            messageBuilder.append("Micro-exercice : ").append(exercise);
        }

        if (!source.isBlank()) {
            if (messageBuilder.length() > 0) {
                messageBuilder.append("\n\n");
            }
            messageBuilder.append("Source : ").append(source);
        }

        if (!url.isBlank()) {
            messageBuilder.append("\n").append(url);
        }

        String message = messageBuilder.toString().trim();

        if (message.length() > 700) {
            message = message.substring(0, 700);
        }

        ajouterNotification(
                "micro_exercice",
                "🧠 " + title,
                message,
                userId,
                objectifId,
                suiviId
        );

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Conseil bien-être");
        alert.setHeaderText("🧠 " + title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public List<Notification> getNotificationsByUserAndType(int userId, String type) {
        List<Notification> list = new ArrayList<>();

        String sql;
        boolean allTypes = type == null || type.trim().isEmpty() || "Tous".equalsIgnoreCase(type);

        if (allTypes) {
            sql = "SELECT * FROM " + TABLE_NAME + " WHERE user_id = ? ORDER BY created_at DESC";
        } else {
            sql = "SELECT * FROM " + TABLE_NAME + " WHERE user_id = ? AND type = ? ORDER BY created_at DESC";
        }

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);

            if (!allTypes) {
                ps.setString(2, type);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Notification n = mapNotification(rs);
                    list.add(n);
                }
            }

        } catch (SQLException e) {
            System.out.println("Erreur récupération notifications filtrées : " + e.getMessage());
            e.printStackTrace();
        }

        return list;
    }

    public int countUnreadNotifications(int userId) {
        String sql = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE user_id = ? AND is_read = 0";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.out.println("Erreur count notifications non lues : " + e.getMessage());
            e.printStackTrace();
        }

        return 0;
    }

    public void markAllAsRead(int userId) {
        String sql = "UPDATE " + TABLE_NAME + " SET is_read = 1 WHERE user_id = ? AND is_read = 0";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erreur markAllAsRead : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void deleteAllNotifications(int userId) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE user_id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erreur suppression totale notifications : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void deleteNotificationById(int notificationId) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, notificationId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erreur suppression notification : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean hasReminderNotificationToday(int userId) {
        String sql = "SELECT COUNT(*) FROM " + TABLE_NAME +
                " WHERE user_id = ? AND type = 'rappel_suivi' AND DATE(created_at) = CURDATE()";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.out.println("Erreur vérification rappel du jour : " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Vérifie s'il existe déjà un suivi aujourd'hui pour cet utilisateur.
     * Correction : la vraie colonne est date_de_suivi.
     */
    public boolean hasSuiviToday(int userId) {
        String sql = "SELECT COUNT(*) FROM suivi_mentale WHERE user_id = ? AND date_de_suivi = CURDATE()";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.out.println("Erreur vérification suivi du jour : " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    public void creerNotificationRappelSuivi(int userId) {
        String type = "rappel_suivi";
        String title = "Rappel quotidien";
        String message = "Vous n'avez pas encore ajouté votre suivi mental aujourd'hui.";

        ajouterNotification(type, title, message, userId, null, null);
    }

    public void afficherPopupRappelSuivi() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Rappel de suivi");
        alert.setHeaderText("Suivi mental du jour");
        alert.setContentText("Vous n'avez pas encore ajouté votre suivi mental aujourd'hui.");
        alert.showAndWait();
    }

    /**
     * Méthode principale à appeler depuis ControllerHome au démarrage.
     * Retourne true si une nouvelle notification de rappel a été créée.
     */
    public boolean checkAndCreateDailyReminder(int userId) {
        try {
            boolean suiviExiste = hasSuiviToday(userId);
            if (suiviExiste) {
                System.out.println("Aucun rappel créé : suivi du jour déjà existant.");
                return false;
            }

            boolean rappelExiste = hasReminderNotificationToday(userId);
            if (rappelExiste) {
                System.out.println("Aucun rappel créé : notification de rappel déjà existante aujourd'hui.");
                return false;
            }

            creerNotificationRappelSuivi(userId);
            System.out.println("Rappel quotidien créé avec succès.");
            return true;

        } catch (Exception e) {
            System.out.println("Erreur checkAndCreateDailyReminder : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public void notifierProgression(int ancienneProgression, int nouvelleProgression,
                                    int userId, Integer objectifId, Integer suiviId) {
        int difference = nouvelleProgression - ancienneProgression;
        String message;
        String titre = "Évolution de votre objectif";
        String type;
        Alert.AlertType alertType;

        if (difference > 0) {
            type = "progression_hausse";
            alertType = Alert.AlertType.INFORMATION;
            message = "Votre progression est maintenant de " + nouvelleProgression + "%.\n"
                    + "Elle est en hausse de " + difference + "%.\n"
                    + "Continuez vos efforts !";
        } else if (difference < 0) {
            type = "progression_baisse";
            alertType = Alert.AlertType.WARNING;
            message = "Votre progression est maintenant de " + nouvelleProgression + "%.\n"
                    + "Elle est en baisse de " + Math.abs(difference) + "%.\n"
                    + "Essayez de reprendre votre rythme.";
        } else {
            type = "progression_stable";
            alertType = Alert.AlertType.INFORMATION;
            message = "Votre progression est maintenant de " + nouvelleProgression + "%.\n"
                    + "Elle est stable.";
        }

        ajouterNotification(type, titre, message, userId, objectifId, suiviId);

        Alert alert = new Alert(alertType);
        alert.setTitle("Notification de progression");
        alert.setHeaderText("Évolution de votre objectif");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private Notification mapNotification(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setId(rs.getInt("id"));
        n.setType(rs.getString("type"));
        n.setTitle(rs.getString("title"));
        n.setMessage(rs.getString("message"));
        n.setRead(rs.getInt("is_read") == 1);
        n.setCreatedAt(rs.getTimestamp("created_at"));
        n.setUserId(rs.getInt("user_id"));

        int objectifId = rs.getInt("objectif_id");
        if (!rs.wasNull()) {
            n.setObjectifId(objectifId);
        }

        int suiviId = rs.getInt("suivi_id");
        if (!rs.wasNull()) {
            n.setSuiviId(suiviId);
        }

        return n;
    }
}