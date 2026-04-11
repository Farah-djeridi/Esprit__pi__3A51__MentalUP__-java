package services;

import interfaces.IService;
import models.User;
import utils.MyDataBase;
import utils.PasswordUtils;

import java.sql.*;
import java.util.ArrayList;
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
        return PasswordUtils.verifyPassword(plainPassword, user.getMotDePasse()) ? user : null;
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
                "`created_at`,`github_username`,`telephone`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
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
            pstm.executeUpdate();
        } catch (SQLException e) { System.out.println("Erreur add: " + e.getMessage()); }
    }

    @Override
    public List<User> getAll() {
        List<User> users = new ArrayList<>();
        try {
            ResultSet rs = cnx.createStatement().executeQuery("SELECT * FROM `user`");
            while (rs.next()) users.add(map(rs));
        } catch (SQLException e) { System.out.println("Erreur getAll: " + e.getMessage()); }
        return users;
    }

    @Override
    public void update(User u) {
        String req = "UPDATE `user` SET `prenom`=?,`nom`=?,`email`=?,`mot_de_passe`=?," +
                "`role`=?,`roles`=?,`reset_token`=?,`reset_token_expires_at`=?," +
                "`avatar_filename`=?,`updated_at`=?,`created_at`=?,`github_username`=?," +
                "`telephone`=? WHERE `id`=?";
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
            pstm.setInt(14, u.getId());
            pstm.executeUpdate();
        } catch (SQLException e) { System.out.println("Erreur update: " + e.getMessage()); }
    }

    @Override
    public void delete(User u) {
        try {
            PreparedStatement pstm = cnx.prepareStatement("DELETE FROM `user` WHERE `id`=?");
            pstm.setInt(1, u.getId());
            pstm.executeUpdate();
        } catch (SQLException e) { System.out.println("Erreur delete: " + e.getMessage()); }
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
            PreparedStatement pstm = cnx.prepareStatement("SELECT * FROM `user` WHERE `email`=?");
            pstm.setString(1, email);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) return map(rs);
        } catch (SQLException e) { System.out.println("Erreur getUserByEmail: " + e.getMessage()); }
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
        return u;
    }
}
