package services;

import interfaces.IService;
import models.Sujet;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceSujet implements IService<Sujet> {

    private Connection cnx;
    private static final int CURRENT_USER_ID = 2; // Utilisateur statique pour le moment

    public ServiceSujet() {
        this.cnx = MyDataBase.getInstance().getCnx();
    }

    // 🔹 AJOUT
    @Override
    public void add(Sujet s) {
        String req = "INSERT INTO sujet (titre, contenu, date_creation, is_anonyme, nb_likes, nb_dislikes, nb_vues, score_toxicite, est_toxique, id_user_id) " +
                "VALUES (?, ?, NOW(), ?, 0, 0, 0, ?, ?, ?)";

        try {
            PreparedStatement pstm = cnx.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);

            pstm.setString(1, s.getTitre());
            pstm.setString(2, s.getContenu());
            pstm.setBoolean(3, s.isAnonyme());
            pstm.setDouble(4, s.getScoreToxicite());
            pstm.setBoolean(5, s.isEstToxique());
            pstm.setInt(6, CURRENT_USER_ID); // Utilisateur ID 2

            pstm.executeUpdate();

            // Récupérer l'ID généré
            ResultSet rs = pstm.getGeneratedKeys();
            if (rs.next()) {
                s.setId(rs.getInt(1));
            }

            System.out.println("Sujet ajouté par l'utilisateur ID: " + CURRENT_USER_ID);
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'ajout: " + e.getMessage());
        }
    }

    // 🔹 AFFICHAGE
    // 🔹 AFFICHAGE
    @Override
    public List<Sujet> getAll() {
        List<Sujet> sujets = new ArrayList<>();

        String req = "SELECT s.*, u.nom, u.prenom " +
                "FROM sujet s " +
                "JOIN user u ON s.id_user_id = u.id " +
                "ORDER BY s.date_creation DESC";

        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(req);

            while (rs.next()) {
                Sujet s = new Sujet();

                s.setId(rs.getInt("id"));
                s.setTitre(rs.getString("titre"));
                s.setContenu(rs.getString("contenu"));
                s.setNbLikes(rs.getInt("nb_likes"));
                s.setNbDislikes(rs.getInt("nb_dislikes"));
                s.setNbVues(rs.getInt("nb_vues"));  // 🔥 IMPORTANT : récupérer les vues
                s.setDateCreation(rs.getDate("date_creation"));
                s.setAnonyme(rs.getBoolean("is_anonyme"));
                s.setScoreToxicite(rs.getDouble("score_toxicite"));
                s.setEstToxique(rs.getBoolean("est_toxique"));

                s.setIdUser(rs.getInt("id_user_id"));

                String fullName = rs.getString("nom") + " " + rs.getString("prenom");
                s.setUserName(fullName);

                sujets.add(s);
            }

        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération: " + e.getMessage());
        }

        return sujets;
    }

    // Récupérer les sujets d'un utilisateur spécifique
    public List<Sujet> getSujetsByUser(int userId) {
        List<Sujet> sujets = new ArrayList<>();
        String req = "SELECT * FROM sujet WHERE id_user_id = ? ORDER BY date_creation DESC";

        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setInt(1, userId);
            ResultSet rs = pstm.executeQuery();

            while (rs.next()) {
                Sujet s = new Sujet();
                s.setId(rs.getInt("id"));
                s.setTitre(rs.getString("titre"));
                s.setContenu(rs.getString("contenu"));
                s.setNbLikes(rs.getInt("nb_likes"));
                s.setNbDislikes(rs.getInt("nb_dislikes"));
                s.setDateCreation(rs.getDate("date_creation"));
                sujets.add(s);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return sujets;
    }

    // 🔹 UPDATE
    @Override
    public void update(Sujet s) {
        // Vérifier que l'utilisateur est le propriétaire
        if (!isUserOwner(s.getId(), CURRENT_USER_ID)) {
            System.out.println("Vous ne pouvez modifier que vos propres sujets !");
            return;
        }

        String req = "UPDATE sujet SET titre=?, contenu=?, is_anonyme=? WHERE id=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setString(1, s.getTitre());
            pstm.setString(2, s.getContenu());
            pstm.setBoolean(3, s.isAnonyme());
            pstm.setInt(4, s.getId());
            pstm.executeUpdate();
            System.out.println("Sujet modifié par l'utilisateur ID: " + CURRENT_USER_ID);
        } catch (SQLException e) {
            System.out.println("Erreur lors de la modification: " + e.getMessage());
        }
    }

    // 🔹 DELETE
    @Override
    public void delete(Sujet s) {
        // Vérifier que l'utilisateur est le propriétaire
        if (!isUserOwner(s.getId(), CURRENT_USER_ID)) {
            System.out.println("Vous ne pouvez supprimer que vos propres sujets !");
            return;
        }

        try {
            // 1. Supprimer les votes liés au sujet
            String reqVotes = "DELETE FROM vote WHERE sujet_id=?";
            PreparedStatement psVotes = cnx.prepareStatement(reqVotes);
            psVotes.setInt(1, s.getId());
            psVotes.executeUpdate();

            // 2. Supprimer les commentaires liés
            String reqComments = "DELETE FROM commentaire WHERE sujet_id=?";
            PreparedStatement psComments = cnx.prepareStatement(reqComments);
            psComments.setInt(1, s.getId());
            psComments.executeUpdate();

            // 3. Supprimer le sujet
            String reqSujet = "DELETE FROM sujet WHERE id=?";
            PreparedStatement psSujet = cnx.prepareStatement(reqSujet);
            psSujet.setInt(1, s.getId());
            psSujet.executeUpdate();

            System.out.println("Sujet + commentaires + votes supprimés par l'utilisateur ID: " + CURRENT_USER_ID);
        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression: " + e.getMessage());
        }
    }

    // Vérifier si l'utilisateur est le propriétaire du sujet
    private boolean isUserOwner(int sujetId, int userId) {
        String req = "SELECT id_user_id FROM sujet WHERE id = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setInt(1, sujetId);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                return rs.getInt("id_user_id") == userId;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    // Incrémenter le nombre de vues
    public void incrementVues(int sujetId) {
        String req = "UPDATE sujet SET nb_vues = nb_vues + 1 WHERE id = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setInt(1, sujetId);
            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    // 🔹 RECHERCHER UN SUJET PAR ID
    public Sujet getById(int id) {
        String req = "SELECT s.*, u.nom, u.prenom " +
                "FROM sujet s " +
                "JOIN user u ON s.id_user_id = u.id " +
                "WHERE s.id = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setInt(1, id);
            ResultSet rs = pstm.executeQuery();

            if (rs.next()) {
                Sujet s = new Sujet();
                s.setId(rs.getInt("id"));
                s.setTitre(rs.getString("titre"));
                s.setContenu(rs.getString("contenu"));
                s.setNbLikes(rs.getInt("nb_likes"));
                s.setNbDislikes(rs.getInt("nb_dislikes"));
                s.setNbVues(rs.getInt("nb_vues"));
                s.setDateCreation(rs.getDate("date_creation"));
                s.setAnonyme(rs.getBoolean("is_anonyme"));
                s.setScoreToxicite(rs.getDouble("score_toxicite"));
                s.setEstToxique(rs.getBoolean("est_toxique"));
                s.setIdUser(rs.getInt("id_user_id"));

                String fullName = rs.getString("nom") + " " + rs.getString("prenom");
                s.setUserName(fullName);

                return s;
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération du sujet: " + e.getMessage());
        }
        return null;
    }
}