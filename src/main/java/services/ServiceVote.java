package services;

import models.Vote;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceVote {

    private Connection cnx;
    private static final int CURRENT_USER_ID = 2; // Utilisateur statique pour le moment

    public ServiceVote() {
        this.cnx = MyDataBase.getInstance().getCnx();
    }

    // Vérifier si l'utilisateur a déjà voté pour un sujet
    public Vote getUserVoteOnSujet(int sujetId) {
        String req = "SELECT * FROM vote WHERE user_id = ? AND sujet_id = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setInt(1, CURRENT_USER_ID);
            pstm.setInt(2, sujetId);
            ResultSet rs = pstm.executeQuery();

            if (rs.next()) {
                Vote vote = new Vote();
                vote.setId(rs.getInt("id"));
                vote.setType(rs.getString("type"));
                vote.setUser_id(rs.getInt("user_id"));
                vote.setSujet_id(rs.getInt("sujet_id"));
                vote.setCreated_at(rs.getTimestamp("created_at"));
                return vote;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    // Ajouter ou modifier un vote pour un sujet
    public void voteForSujet(int sujetId, String voteType) {
        Vote existingVote = getUserVoteOnSujet(sujetId);

        try {
            if (existingVote == null) {
                // Nouveau vote
                String req = "INSERT INTO vote (type, user_id, sujet_id, created_at) VALUES (?, ?, ?, NOW())";
                PreparedStatement pstm = cnx.prepareStatement(req);
                pstm.setString(1, voteType);
                pstm.setInt(2, CURRENT_USER_ID);
                pstm.setInt(3, sujetId);
                pstm.executeUpdate();

                // Mettre à jour les compteurs du sujet
                updateSujetCounters(sujetId, voteType, "add");

            } else if (!existingVote.getType().equals(voteType)) {
                // Changement de vote (like -> dislike ou inverse)
                String req = "UPDATE vote SET type = ?, created_at = NOW() WHERE id = ?";
                PreparedStatement pstm = cnx.prepareStatement(req);
                pstm.setString(1, voteType);
                pstm.setInt(2, existingVote.getId());
                pstm.executeUpdate();

                // Mettre à jour les compteurs (annuler l'ancien, ajouter le nouveau)
                updateSujetCounters(sujetId, existingVote.getType(), "remove");
                updateSujetCounters(sujetId, voteType, "add");
            }

            System.out.println("Vote enregistré avec succès !");

        } catch (SQLException e) {
            System.out.println("Erreur lors du vote : " + e.getMessage());
        }
    }

    // Supprimer un vote
    public void removeVoteFromSujet(int sujetId) {
        Vote existingVote = getUserVoteOnSujet(sujetId);

        if (existingVote != null) {
            try {
                String req = "DELETE FROM vote WHERE id = ?";
                PreparedStatement pstm = cnx.prepareStatement(req);
                pstm.setInt(1, existingVote.getId());
                pstm.executeUpdate();

                // Décrémenter le compteur correspondant
                updateSujetCounters(sujetId, existingVote.getType(), "remove");

                System.out.println("Vote supprimé !");

            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    // Mettre à jour les compteurs de likes/dislikes d'un sujet
    private void updateSujetCounters(int sujetId, String voteType, String action) {
        String column = voteType.equals("like") ? "nb_likes" : "nb_dislikes";
        String operation = action.equals("add") ? "+" : "-";

        String req = "UPDATE sujet SET " + column + " = " + column + " " + operation + " 1 WHERE id = ?";

        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setInt(1, sujetId);
            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erreur mise à jour compteurs : " + e.getMessage());
        }
    }

    // Obtenir le nombre de likes d'un sujet
    public int getLikesCount(int sujetId) {
        String req = "SELECT nb_likes FROM sujet WHERE id = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setInt(1, sujetId);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                return rs.getInt("nb_likes");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return 0;
    }

    // Obtenir le nombre de dislikes d'un sujet
    public int getDislikesCount(int sujetId) {
        String req = "SELECT nb_dislikes FROM sujet WHERE id = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setInt(1, sujetId);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                return rs.getInt("nb_dislikes");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return 0;
    }

    // Vérifier si l'utilisateur a déjà voté pour un commentaire
    public Vote getUserVoteOnCommentaire(int commentaireId) {
        String req = "SELECT * FROM vote WHERE user_id = ? AND commentaire_id = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setInt(1, CURRENT_USER_ID);
            pstm.setInt(2, commentaireId);
            ResultSet rs = pstm.executeQuery();

            if (rs.next()) {
                Vote vote = new Vote();
                vote.setId(rs.getInt("id"));
                vote.setType(rs.getString("type"));
                vote.setUser_id(rs.getInt("user_id"));
                vote.setCommentaire_id(rs.getInt("commentaire_id"));
                vote.setCreated_at(rs.getTimestamp("created_at"));
                return vote;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    // Ajouter ou modifier un vote pour un commentaire
    public void voteForCommentaire(int commentaireId, String voteType) {
        Vote existingVote = getUserVoteOnCommentaire(commentaireId);

        try {
            if (existingVote == null) {
                String req = "INSERT INTO vote (type, user_id, commentaire_id, created_at) VALUES (?, ?, ?, NOW())";
                PreparedStatement pstm = cnx.prepareStatement(req);
                pstm.setString(1, voteType);
                pstm.setInt(2, CURRENT_USER_ID);
                pstm.setInt(3, commentaireId);
                pstm.executeUpdate();
                updateCommentaireCounters(commentaireId, voteType, "add");
            } else if (!existingVote.getType().equals(voteType)) {
                String req = "UPDATE vote SET type = ?, created_at = NOW() WHERE id = ?";
                PreparedStatement pstm = cnx.prepareStatement(req);
                pstm.setString(1, voteType);
                pstm.setInt(2, existingVote.getId());
                pstm.executeUpdate();
                updateCommentaireCounters(commentaireId, existingVote.getType(), "remove");
                updateCommentaireCounters(commentaireId, voteType, "add");
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors du vote: " + e.getMessage());
        }
    }

    // Supprimer un vote d'un commentaire
    public void removeVoteFromCommentaire(int commentaireId) {
        Vote existingVote = getUserVoteOnCommentaire(commentaireId);

        if (existingVote != null) {
            try {
                String req = "DELETE FROM vote WHERE id = ?";
                PreparedStatement pstm = cnx.prepareStatement(req);
                pstm.setInt(1, existingVote.getId());
                pstm.executeUpdate();
                updateCommentaireCounters(commentaireId, existingVote.getType(), "remove");
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    // Mettre à jour les compteurs de likes/dislikes d'un commentaire
    private void updateCommentaireCounters(int commentaireId, String voteType, String action) {
        String column = voteType.equals("like") ? "nb_likes" : "nb_dislikes";
        String operation = action.equals("add") ? "+" : "-";

        String req = "UPDATE commentaire SET " + column + " = " + column + " " + operation + " 1 WHERE id = ?";

        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setInt(1, commentaireId);
            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erreur mise à jour compteurs commentaire: " + e.getMessage());
        }
    }
}