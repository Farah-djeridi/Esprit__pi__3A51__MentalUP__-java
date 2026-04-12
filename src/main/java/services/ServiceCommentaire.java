package services;

import interfaces.IService;
import models.Commentaire;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceCommentaire implements IService<Commentaire> {

    private Connection cnx;

    public ServiceCommentaire() {
        this.cnx = MyDataBase.getInstance().getCnx();
    }

    // 🔹 AJOUT
    @Override
    public void add(Commentaire c) {

        String req = "INSERT INTO commentaire (contenu, date_commentaire, is_anonyme, nb_likes, nb_dislikes, score_toxicite, est_toxique, user_id, sujet_id) " +
                "VALUES (?, NOW(), ?, 0, 0, ?, ?, ?, ?)";

        try {
            PreparedStatement pstm = cnx.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);

            pstm.setString(1, c.getContenu());
            pstm.setBoolean(2, c.isAnonyme());
            pstm.setDouble(3, c.getScoreToxicite());
            pstm.setBoolean(4, c.isEstToxique());
            pstm.setInt(5, c.getUserId());
            pstm.setInt(6, c.getSujetId());

            pstm.executeUpdate();

            // 🔥 IMPORTANT : récupérer ID généré
            ResultSet rs = pstm.getGeneratedKeys();
            if (rs.next()) {
                c.setId(rs.getInt(1));
            }

            System.out.println("Commentaire ajouté !");
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'ajout du commentaire: " + e.getMessage());
        }
    }

    // 🔹 AFFICHAGE TOUS LES COMMENTAIRES AVEC NOM UTILISATEUR
    @Override
    public List<Commentaire> getAll() {

        List<Commentaire> commentaires = new ArrayList<>();

        String req = "SELECT c.*, CONCAT(u.prenom, ' ', u.nom) as user_name FROM commentaire c " +
                "LEFT JOIN user u ON c.user_id = u.id " +
                "ORDER BY c.date_commentaire DESC";

        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(req);

            while (rs.next()) {
                Commentaire c = extractCommentaireFromResultSet(rs);
                commentaires.add(c);
            }

        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des commentaires: " + e.getMessage());
        }

        return commentaires;
    }

    // 🔹 RECHERCHER COMMENTAIRES PAR SUJET ID AVEC NOM UTILISATEUR
    public List<Commentaire> getBySujetId(int sujetId) {
        List<Commentaire> commentaires = new ArrayList<>();
        String req = "SELECT c.*, CONCAT(u.prenom, ' ', u.nom) as user_name FROM commentaire c " +
                "LEFT JOIN user u ON c.user_id = u.id " +
                "WHERE c.sujet_id = ? ORDER BY c.date_commentaire ASC";

        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setInt(1, sujetId);
            ResultSet rs = pstm.executeQuery();

            while (rs.next()) {
                Commentaire c = extractCommentaireFromResultSet(rs);
                commentaires.add(c);
            }

        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des commentaires par sujet: " + e.getMessage());
        }

        return commentaires;
    }

    // 🔹 RECHERCHER COMMENTAIRES PAR UTILISATEUR ID AVEC NOM UTILISATEUR
    public List<Commentaire> getByUserId(int userId) {
        List<Commentaire> commentaires = new ArrayList<>();
        String req = "SELECT c.*, CONCAT(u.prenom, ' ', u.nom) as user_name FROM commentaire c " +
                "LEFT JOIN user u ON c.user_id = u.id " +
                "WHERE c.user_id = ? ORDER BY c.date_commentaire DESC";

        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setInt(1, userId);
            ResultSet rs = pstm.executeQuery();

            while (rs.next()) {
                Commentaire c = extractCommentaireFromResultSet(rs);
                commentaires.add(c);
            }

        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des commentaires par utilisateur: " + e.getMessage());
        }

        return commentaires;
    }

    // 🔹 RECHERCHER UN COMMENTAIRE PAR ID AVEC NOM UTILISATEUR
    public Commentaire getById(int id) {
        Commentaire commentaire = null;
        String req = "SELECT c.*, CONCAT(u.prenom, ' ', u.nom) as user_name FROM commentaire c " +
                "LEFT JOIN user u ON c.user_id = u.id " +
                "WHERE c.id = ?";

        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setInt(1, id);
            ResultSet rs = pstm.executeQuery();

            if (rs.next()) {
                commentaire = extractCommentaireFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération du commentaire par ID: " + e.getMessage());
        }

        return commentaire;
    }

    // 🔹 RECHERCHER LE NOM D'UN UTILISATEUR PAR SON ID
    public String getUserNameById(int userId) {
        String req = "SELECT CONCAT(prenom, ' ', nom) as user_name FROM user WHERE id = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setInt(1, userId);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                return rs.getString("user_name");
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération du nom utilisateur: " + e.getMessage());
        }
        return "Utilisateur";
    }

    // 🔹 RECHERCHER LES INITIALES D'UN UTILISATEUR PAR SON ID
    public String getUserInitialsById(int userId) {
        String req = "SELECT CONCAT(UPPER(SUBSTRING(prenom, 1, 1)), UPPER(SUBSTRING(nom, 1, 1))) as initials FROM user WHERE id = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setInt(1, userId);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                String initials = rs.getString("initials");
                if (initials != null && !initials.isEmpty()) {
                    return initials.toUpperCase();
                }
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des initiales: " + e.getMessage());
        }
        return "U";
    }

    // 🔹 UPDATE COMPLET
    @Override
    public void update(Commentaire c) {
        String req = "UPDATE commentaire SET contenu=?, is_anonyme=?, nb_likes=?, nb_dislikes=?, score_toxicite=?, est_toxique=? WHERE id=?";

        try {
            PreparedStatement pstm = cnx.prepareStatement(req);

            pstm.setString(1, c.getContenu());
            pstm.setBoolean(2, c.isAnonyme());
            pstm.setInt(3, c.getNbLikes());
            pstm.setInt(4, c.getNbDislikes());
            pstm.setDouble(5, c.getScoreToxicite());
            pstm.setBoolean(6, c.isEstToxique());
            pstm.setInt(7, c.getId());

            pstm.executeUpdate();

            System.out.println("Commentaire modifié !");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la modification du commentaire: " + e.getMessage());
        }
    }

    // 🔹 UPDATE LIKES SEULEMENT
    public void updateLikes(int commentaireId, int nbLikes) {
        String req = "UPDATE commentaire SET nb_likes=? WHERE id=?";

        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setInt(1, nbLikes);
            pstm.setInt(2, commentaireId);
            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erreur lors de la mise à jour des likes: " + e.getMessage());
        }
    }

    // 🔹 UPDATE DISLIKES SEULEMENT
    public void updateDislikes(int commentaireId, int nbDislikes) {
        String req = "UPDATE commentaire SET nb_dislikes=? WHERE id=?";

        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setInt(1, nbDislikes);
            pstm.setInt(2, commentaireId);
            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erreur lors de la mise à jour des dislikes: " + e.getMessage());
        }
    }

    // 🔹 DELETE
    @Override
    public void delete(Commentaire c) {
        String req = "DELETE FROM commentaire WHERE id=?";

        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setInt(1, c.getId());
            pstm.executeUpdate();

            System.out.println("Commentaire supprimé !");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression du commentaire: " + e.getMessage());
        }
    }


    // 🔹 COMPTER LES COMMENTAIRES PAR SUJET
    public int countBySujetId(int sujetId) {
        String req = "SELECT COUNT(*) FROM commentaire WHERE sujet_id = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setInt(1, sujetId);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors du comptage des commentaires: " + e.getMessage());
        }
        return 0;
    }

    // 🔹 MÉTHODE UTILITAIRE : Extraire un Commentaire d'un ResultSet
    private Commentaire extractCommentaireFromResultSet(ResultSet rs) throws SQLException {
        Commentaire c = new Commentaire();
        c.setId(rs.getInt("id"));
        c.setContenu(rs.getString("contenu"));
        c.setDateCommentaire(rs.getDate("date_commentaire"));
        c.setAnonyme(rs.getBoolean("is_anonyme"));
        c.setNbLikes(rs.getInt("nb_likes"));
        c.setNbDislikes(rs.getInt("nb_dislikes"));
        c.setScoreToxicite(rs.getDouble("score_toxicite"));
        c.setEstToxique(rs.getBoolean("est_toxique"));
        c.setUserId(rs.getInt("user_id"));
        c.setSujetId(rs.getInt("sujet_id"));

        // Récupérer le nom de l'utilisateur
        try {
            String userName = rs.getString("user_name");
            c.setUserName(userName);
        } catch (SQLException e) {
            c.setUserName(null);
        }

        return c;
    }
}