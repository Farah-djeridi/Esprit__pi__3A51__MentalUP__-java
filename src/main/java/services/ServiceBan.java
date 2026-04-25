package services;

import models.Ban;
import utils.MyDataBase;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ServiceBan {
    private Connection cnx;

    public ServiceBan() {
        this.cnx = MyDataBase.getInstance().getCnx();
    }

    // Bannir un utilisateur
    public void banUser(int userId, String reason, int daysDuration, int bannedBy) {
        String req = "INSERT INTO ban (user_id, ban_reason, ban_date, ban_expiry_date, is_active, banned_by) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setInt(1, userId);
            pstm.setString(2, reason);
            pstm.setDate(3, Date.valueOf(LocalDate.now()));
            pstm.setDate(4, Date.valueOf(LocalDate.now().plusDays(daysDuration)));
            pstm.setBoolean(5, true);
            pstm.setInt(6, bannedBy);
            pstm.executeUpdate();
            System.out.println("✅ Utilisateur " + userId + " banni pour " + daysDuration + " jours");
        } catch (SQLException e) {
            System.out.println("Erreur lors du bannissement: " + e.getMessage());
        }
    }
    // Dans ServiceBan.java - Ajouter cette méthode
    public void updateBan(int banId, String reason, int daysDuration) {
        String req = "UPDATE ban SET ban_reason = ?, ban_expiry_date = ? WHERE id = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setString(1, reason);
            pstm.setDate(2, Date.valueOf(LocalDate.now().plusDays(daysDuration)));
            pstm.setInt(3, banId);
            pstm.executeUpdate();
            System.out.println("✅ Bannissement " + banId + " mis à jour - Nouvelle durée: " + daysDuration + " jours");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la mise à jour: " + e.getMessage());
            e.printStackTrace();
        }
    }
    // Vérifier si un utilisateur est actuellement banni
    // Dans ServiceBan.java
    public boolean isUserBanned(int userId) {
        String req = "SELECT * FROM ban WHERE user_id = ? AND is_active = true AND ban_expiry_date >= ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setInt(1, userId);
            pstm.setDate(2, Date.valueOf(LocalDate.now()));
            ResultSet rs = pstm.executeQuery();

            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la vérification: " + e.getMessage());
        }
        return false;
    }

    // Obtenir le bannissement actif d'un utilisateur
    public Ban getActiveBan(int userId) {
        String req = "SELECT b.*, u.nom as user_name, a.nom as admin_name " +
                "FROM ban b " +
                "LEFT JOIN user u ON b.user_id = u.id " +
                "LEFT JOIN user a ON b.banned_by = a.id " +
                "WHERE b.user_id = ? AND b.is_active = true AND b.ban_expiry_date >= ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setInt(1, userId);
            pstm.setDate(2, Date.valueOf(LocalDate.now()));
            ResultSet rs = pstm.executeQuery();

            if (rs.next()) {
                Ban ban = new Ban();
                ban.setId(rs.getInt("id"));
                ban.setUserId(rs.getInt("user_id"));
                ban.setUserName(rs.getString("user_name"));
                ban.setBanReason(rs.getString("ban_reason"));
                ban.setBanDate(rs.getDate("ban_date"));
                ban.setBanExpiryDate(rs.getDate("ban_expiry_date"));
                ban.setActive(rs.getBoolean("is_active"));
                ban.setBannedBy(rs.getInt("banned_by"));
                ban.setBannedByName(rs.getString("admin_name"));
                return ban;
            }
        } catch (SQLException e) {
            System.out.println("Erreur: " + e.getMessage());
        }
        return null;
    }

    // Désactiver un bannissement
    private void deactivateBan(int banId) {
        String req = "UPDATE ban SET is_active = false WHERE id = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setInt(1, banId);
            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erreur: " + e.getMessage());
        }
    }

    // Débannir manuellement un utilisateur
    // Dans ServiceBan.java
    public void unbanUser(int userId) {
        // Désactiver tous les bannissements actifs de cet utilisateur
        String req = "UPDATE ban SET is_active = false WHERE user_id = ? AND is_active = true";
        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setInt(1, userId);
            int rowsAffected = pstm.executeUpdate();
            System.out.println("✅ Utilisateur " + userId + " débanni - " + rowsAffected + " bannissement(s) désactivé(s)");
        } catch (SQLException e) {
            System.out.println("Erreur lors du débannissement: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Obtenir tous les utilisateurs bannis
    public List<Ban> getAllActiveBans() {
        List<Ban> bans = new ArrayList<>();
        String req = "SELECT b.*, u.nom as user_name, a.nom as admin_name " +
                "FROM ban b " +
                "LEFT JOIN user u ON b.user_id = u.id " +
                "LEFT JOIN user a ON b.banned_by = a.id " +
                "WHERE b.is_active = true AND b.ban_expiry_date >= ? " +
                "ORDER BY b.ban_date DESC";
        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setDate(1, Date.valueOf(LocalDate.now()));
            ResultSet rs = pstm.executeQuery();

            while (rs.next()) {
                Ban ban = new Ban();
                ban.setId(rs.getInt("id"));
                ban.setUserId(rs.getInt("user_id"));
                ban.setUserName(rs.getString("user_name"));
                ban.setBanReason(rs.getString("ban_reason"));
                ban.setBanDate(rs.getDate("ban_date"));
                ban.setBanExpiryDate(rs.getDate("ban_expiry_date"));
                ban.setActive(rs.getBoolean("is_active"));
                ban.setBannedBy(rs.getInt("banned_by"));
                ban.setBannedByName(rs.getString("admin_name"));
                bans.add(ban);
            }
        } catch (SQLException e) {
            System.out.println("Erreur: " + e.getMessage());
        }
        return bans;
    }

    // Obtenir les informations de bannissement (message pour l'utilisateur)
    public String getBanMessage(int userId) {
        Ban ban = getActiveBan(userId);
        if (ban != null) {
            return "❌ VOUS ÊTES BANNI DU FORUM ❌\n\n" +
                    "Raison: " + ban.getBanReason() + "\n" +
                    "Date du bannissement: " + ban.getBanDate() + "\n" +
                    "Expire le: " + ban.getBanExpiryDate() + "\n" +
                    "Banni par: " + (ban.getBannedByName() != null ? ban.getBannedByName() : "Administrateur");
        }
        return null;
    }
    // Ajouter cette méthode pour récupérer tous les bannissements avec les noms d'utilisateurs
// Dans ServiceBan.java
    public List<Ban> getAllBansWithDetails() {
        List<Ban> bans = new ArrayList<>();
        // Récupérer TOUS les bannissements (actifs ET inactifs) pour l'affichage
        String req = "SELECT b.*, u.nom as user_name, a.nom as admin_name " +
                "FROM ban b " +
                "LEFT JOIN user u ON b.user_id = u.id " +
                "LEFT JOIN user a ON b.banned_by = a.id " +
                "ORDER BY b.ban_date DESC";
        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            ResultSet rs = pstm.executeQuery();

            while (rs.next()) {
                Ban ban = new Ban();
                ban.setId(rs.getInt("id"));
                ban.setUserId(rs.getInt("user_id"));
                ban.setUserName(rs.getString("user_name"));
                ban.setBanReason(rs.getString("ban_reason"));
                ban.setBanDate(rs.getDate("ban_date"));
                ban.setBanExpiryDate(rs.getDate("ban_expiry_date"));
                ban.setActive(rs.getBoolean("is_active"));
                ban.setBannedBy(rs.getInt("banned_by"));
                ban.setBannedByName(rs.getString("admin_name"));
                bans.add(ban);
            }
        } catch (SQLException e) {
            System.out.println("Erreur: " + e.getMessage());
            e.printStackTrace();
        }
        return bans;
    }
    /**
     * Supprimer définitivement un bannissement de la base de données
     */
    public void deleteBan(int banId) {
        String req = "DELETE FROM ban WHERE id = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setInt(1, banId);
            int rowsAffected = pstm.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✅ Bannissement " + banId + " supprimé définitivement");
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression: " + e.getMessage());
            e.printStackTrace();
        }
    }
}