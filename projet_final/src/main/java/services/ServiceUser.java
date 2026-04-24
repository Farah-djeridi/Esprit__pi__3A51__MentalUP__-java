package services;

import interfaces.IService;
import models.User;
import utils.MyDataBase;
import utils.PasswordUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ServiceUser implements IService<User> {

    private Connection cnx;

    public ServiceUser() {
        this.cnx = MyDataBase.getInstance().getCnx();
    }

    // ========== AUTHENTIFICATION ==========

    public User login(String email, String plainPassword) {
        User user = getUserByEmail(email);
        if (user == null) return null;

        // Check if account is locked
        if (user.getLockedUntil() != null && user.getLockedUntil().after(new Date())) {
            return null; // Account is locked
        }

        boolean passwordValid = PasswordUtils.verifyPassword(plainPassword, user.getMotDePasse());
        if (passwordValid) {
            // Successful login, reset failed attempts
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
            update(user);
            return user;
        } else {
            // Failed login, increment attempts
            int attempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(attempts);
            if (attempts >= 3) {
                // Lock account for 2 minutes
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.MINUTE, 2);
                user.setLockedUntil(cal.getTime());
            }
            update(user);
            return null;
        }
    }

    public boolean isAccountLocked(String email) {
        User user = getUserByEmail(email);
        return user != null && user.getLockedUntil() != null && user.getLockedUntil().after(new Date());
    }

    public boolean register(User user) {
        if (getUserByEmail(user.getEmail()) != null) return false;
        user.setMotDePasse(PasswordUtils.hashPassword(user.getMotDePasse()));
        add(user);
        return true;
    }

    public boolean emailExistsForOther(String email, int excludeId) {
        try {
            PreparedStatement pstm = cnx.prepareStatement(
                "SELECT id FROM `user` WHERE `email`=? AND `id`!=?");
            pstm.setString(1, email);
            pstm.setInt(2, excludeId);
            return pstm.executeQuery().next();
        } catch (SQLException e) { return false; }
    }

    // ========== CRUD ==========

    @Override
    public void add(User u) {
        String req = "INSERT INTO `user`(`prenom`,`nom`,`email`,`mot_de_passe`,`role`,`roles`," +
                "`reset_token`,`reset_token_expires_at`,`avatar_filename`,`updated_at`," +
                "`created_at`,`github_username`,`telephone`,`deleted_at`,`failed_login_attempts`,`locked_until`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setString(1, u.getPrenom());
            pstm.setString(2, u.getNom());
            pstm.setString(3, u.getEmail());
            pstm.setString(4, u.getMotDePasse());
            pstm.setString(5, u.getRole());
            pstm.setString(6, u.getRoles());
            pstm.setString(7, u.getResetToken());
            pstm.setTimestamp(8, u.getResetTokenExpiresAt() != null ? new Timestamp(u.getResetTokenExpiresAt().getTime()) : null);
            pstm.setString(9, u.getAvatarFilename());
            pstm.setTimestamp(10, u.getUpdatedAt() != null ? new Timestamp(u.getUpdatedAt().getTime()) : null);
            pstm.setTimestamp(11, u.getCreatedAt() != null ? new Timestamp(u.getCreatedAt().getTime()) : null);
            pstm.setString(12, u.getGithubUsername());
            pstm.setString(13, u.getTelephone());
            pstm.setTimestamp(14, u.getDeletedAt() != null ? new Timestamp(u.getDeletedAt().getTime()) : null);
            pstm.setInt(15, u.getFailedLoginAttempts());
            pstm.setTimestamp(16, u.getLockedUntil() != null ? new Timestamp(u.getLockedUntil().getTime()) : null);
            pstm.executeUpdate();
        } catch (SQLException e) { System.out.println("Erreur add: " + e.getMessage()); }
    }

    @Override
    public List<User> getAll() {
        List<User> users = new ArrayList<>();
        try {
            // Check if deleted_at column exists
            ResultSet rsCheck = cnx.getMetaData().getColumns(null, null, "user", "deleted_at");
            String query = "SELECT * FROM `user`";
            if (rsCheck.next()) {
                query += " WHERE `deleted_at` IS NULL";
            }
            ResultSet rs = cnx.createStatement().executeQuery(query);
            while (rs.next()) users.add(map(rs));
        } catch (SQLException e) { System.out.println("Erreur getAll: " + e.getMessage()); }
        return users;
    }

    @Override
    public void update(User u) {
        String req = "UPDATE `user` SET `prenom`=?,`nom`=?,`email`=?,`mot_de_passe`=?," +
                "`role`=?,`roles`=?,`reset_token`=?,`reset_token_expires_at`=?," +
                "`avatar_filename`=?,`updated_at`=?,`created_at`=?,`github_username`=?," +
                "`telephone`=?,`deleted_at`=?,`failed_login_attempts`=?,`locked_until`=? WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setString(1, u.getPrenom());
            pstm.setString(2, u.getNom());
            pstm.setString(3, u.getEmail());
            pstm.setString(4, u.getMotDePasse());
            pstm.setString(5, u.getRole());
            pstm.setString(6, u.getRoles());
            pstm.setString(7, u.getResetToken());
            pstm.setTimestamp(8, u.getResetTokenExpiresAt() != null ? new Timestamp(u.getResetTokenExpiresAt().getTime()) : null);
            pstm.setString(9, u.getAvatarFilename());
            pstm.setTimestamp(10, u.getUpdatedAt() != null ? new Timestamp(u.getUpdatedAt().getTime()) : null);
            pstm.setTimestamp(11, u.getCreatedAt() != null ? new Timestamp(u.getCreatedAt().getTime()) : null);
            pstm.setString(12, u.getGithubUsername());
            pstm.setString(13, u.getTelephone());
            pstm.setTimestamp(14, u.getDeletedAt() != null ? new Timestamp(u.getDeletedAt().getTime()) : null);
            pstm.setInt(15, u.getFailedLoginAttempts());
            pstm.setTimestamp(16, u.getLockedUntil() != null ? new Timestamp(u.getLockedUntil().getTime()) : null);
            pstm.setInt(17, u.getId());
            pstm.executeUpdate();
        } catch (SQLException e) { System.out.println("Erreur update: " + e.getMessage()); }
    }

    @Override
    public void delete(User u) {
        try {
            PreparedStatement pstm = cnx.prepareStatement("UPDATE `user` SET `deleted_at` = NOW() WHERE `id`=?");
            pstm.setInt(1, u.getId());
            pstm.executeUpdate();
        } catch (SQLException e) { System.out.println("Erreur delete: " + e.getMessage()); }
    }

    public List<User> getDeletedUsers() {
        List<User> users = new ArrayList<>();
        try {
            // Check if deleted_at column exists
            ResultSet rsCheck = cnx.getMetaData().getColumns(null, null, "user", "deleted_at");
            if (rsCheck.next()) {
                ResultSet rs = cnx.createStatement().executeQuery("SELECT * FROM `user` WHERE `deleted_at` IS NOT NULL");
                while (rs.next()) users.add(map(rs));
            }
        } catch (SQLException e) { System.out.println("Erreur getDeletedUsers: " + e.getMessage()); }
        return users;
    }

    public boolean updateProfile(User u, String newPlainPassword) {
        try {
            PreparedStatement pstm;
            if (newPlainPassword != null && !newPlainPassword.isEmpty()) {
                String hashed = PasswordUtils.hashPassword(newPlainPassword);
                pstm = cnx.prepareStatement(
                    "UPDATE `user` SET `prenom`=?,`nom`=?,`email`=?,`mot_de_passe`=? WHERE `id`=?");
                pstm.setString(1, u.getPrenom());
                pstm.setString(2, u.getNom());
                pstm.setString(3, u.getEmail());
                pstm.setString(4, hashed);
                pstm.setInt(5, u.getId());
                u.setMotDePasse(hashed);
            } else {
                pstm = cnx.prepareStatement(
                    "UPDATE `user` SET `prenom`=?,`nom`=?,`email`=? WHERE `id`=?");
                pstm.setString(1, u.getPrenom());
                pstm.setString(2, u.getNom());
                pstm.setString(3, u.getEmail());
                pstm.setInt(4, u.getId());
            }
            pstm.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Erreur updateProfile: " + e.getMessage());
            return false;
        }
    }

    public User getUserByEmail(String email) {
        try {
            String query = "SELECT * FROM `user` WHERE `email`=?";
            PreparedStatement pstm = cnx.prepareStatement(query);
            pstm.setString(1, email);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                return map(rs);
            }
        } catch (SQLException e) {
            System.err.println("Erreur getUserByEmail: " + e.getMessage());
        }
        return null;
    }
    private User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setPrenom(rs.getString("prenom"));
        u.setNom(rs.getString("nom"));
        u.setEmail(rs.getString("email"));
        u.setMotDePasse(rs.getString("mot_de_passe"));
        u.setRole(rs.getString("role"));
        u.setRoles(rs.getString("roles"));
        u.setResetToken(rs.getString("reset_token"));
        u.setResetTokenExpiresAt(rs.getTimestamp("reset_token_expires_at"));
        u.setAvatarFilename(rs.getString("avatar_filename"));
        u.setUpdatedAt(rs.getTimestamp("updated_at"));
        u.setCreatedAt(rs.getTimestamp("created_at"));
        u.setGithubUsername(rs.getString("github_username"));
        u.setTelephone(rs.getString("telephone"));
        try {
            u.setDeletedAt(rs.getTimestamp("deleted_at"));
        } catch (SQLException e) {
            u.setDeletedAt(null);
        }
        try {
            u.setFailedLoginAttempts(rs.getInt("failed_login_attempts"));
        } catch (SQLException e) {
            u.setFailedLoginAttempts(0);
        }
        try {
            u.setLockedUntil(rs.getTimestamp("locked_until"));
        } catch (SQLException e) {
            u.setLockedUntil(null);
        }
        return u;
    }
    public void restore(User u) {
        try {
            PreparedStatement pstm = cnx.prepareStatement(
                    "UPDATE `user` SET `deleted_at` = NULL WHERE `id`=?");
            pstm.setInt(1, u.getId());
            pstm.executeUpdate();
        } catch (SQLException e) { System.out.println("Erreur restore: " + e.getMessage()); }
    }
    public User getUserByEmailAdmin(String email) {
        try {
            PreparedStatement pstm = cnx.prepareStatement(
                    "SELECT * FROM `user` WHERE `email`=?");
            pstm.setString(1, email);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) return map(rs);
        } catch (SQLException e) { System.out.println(e.getMessage()); }
        return null;
    }


    // Methode ajoutee pour l'inscription Google OAuth
    // (pas de hachage du mot de passe - deja hache dans GoogleAuthService)
    public boolean registerGoogleUser(User user) {
        if (getUserByEmail(user.getEmail()) != null) return false;
        // Le mot de passe est deja hache (genere aleatoirement dans GoogleAuthService)
        add(user);
        return true;
    }

}