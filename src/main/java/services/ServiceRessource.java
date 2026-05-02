package services;

import interfaces.IService;
import models.Ressource;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceRessource implements IService<Ressource> {

    private Connection cnx;

    public ServiceRessource() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void add(Ressource ressource) {
        String query = "INSERT INTO ressource (titre, description, type, lien, image, date_publication, nb_vues, categorie_id, moderation_status, moderation_score) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement pst = cnx.prepareStatement(query);
            pst.setString(1, ressource.getTitre());
            pst.setString(2, ressource.getDescription());
            pst.setString(3, ressource.getType());
            pst.setString(4, ressource.getLien());
            pst.setString(5, ressource.getImage());
            pst.setTimestamp(6, ressource.getDatePublication());
            pst.setInt(7, ressource.getNbVues());
            
            if (ressource.getCategorieId() > 0) {
                pst.setInt(8, ressource.getCategorieId());
            } else {
                pst.setNull(8, java.sql.Types.INTEGER);
            }
            
            pst.setString(9, ressource.getModerationStatus());
            pst.setDouble(10, ressource.getModerationScore());
            
            pst.executeUpdate();
            System.out.println("Ressource ajoutee avec succes");
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'ajout de la ressource: " + e.getMessage());
        }
    }

    @Override
    public List<Ressource> getAll() {
        List<Ressource> ressources = new ArrayList<>();
        // Jointure pour recuperer le nom de la categorie
        String query = "SELECT r.*, c.nom as categorie_nom FROM ressource r LEFT JOIN categorie c ON r.categorie_id = c.id";
        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                Ressource r = new Ressource();
                r.setId(rs.getInt("id"));
                r.setTitre(rs.getString("titre"));
                r.setDescription(rs.getString("description"));
                r.setType(rs.getString("type"));
                r.setLien(rs.getString("lien"));
                r.setImage(rs.getString("image"));
                r.setDatePublication(rs.getTimestamp("date_publication"));
                r.setNbVues(rs.getInt("nb_vues"));
                r.setCategorieId(rs.getInt("categorie_id"));
                r.setCategorieNom(rs.getString("categorie_nom"));
                // Fallback to "SAFE" and 0.0 if column doesn't exist or is null
                try {
                    String modStatus = rs.getString("moderation_status");
                    r.setModerationStatus(modStatus != null ? modStatus : "SAFE");
                    r.setModerationScore(rs.getDouble("moderation_score"));
                } catch (SQLException ex) {
                    r.setModerationStatus("SAFE");
                    r.setModerationScore(0.0);
                }
                ressources.add(r);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la recuperation des ressources: " + e.getMessage());
        }
        return ressources;
    }

    @Override
    public void update(Ressource ressource) {
        String query = "UPDATE ressource SET titre = ?, description = ?, type = ?, lien = ?, image = ?, categorie_id = ?, moderation_status = ?, moderation_score = ? WHERE id = ?";
        try {
            PreparedStatement pst = cnx.prepareStatement(query);
            pst.setString(1, ressource.getTitre());
            pst.setString(2, ressource.getDescription());
            pst.setString(3, ressource.getType());
            pst.setString(4, ressource.getLien());
            pst.setString(5, ressource.getImage());
            
            if (ressource.getCategorieId() > 0) {
                pst.setInt(6, ressource.getCategorieId());
            } else {
                pst.setNull(6, java.sql.Types.INTEGER);
            }
            
            pst.setString(7, ressource.getModerationStatus());
            pst.setDouble(8, ressource.getModerationScore());
            pst.setInt(9, ressource.getId());
            
            pst.executeUpdate();
            System.out.println("Ressource modifiee avec succes");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la modification de la ressource: " + e.getMessage());
        }
    }

    @Override
    public void delete(Ressource ressource) {
        String query = "DELETE FROM ressource WHERE id = ?";
        try {
            PreparedStatement pst = cnx.prepareStatement(query);
            pst.setInt(1, ressource.getId());
            pst.executeUpdate();
            System.out.println("Ressource supprimee avec succes");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression de la ressource: " + e.getMessage());
        }
    }

    public void incrementViews(int id) {
        String query = "UPDATE ressource SET nb_vues = nb_vues + 1 WHERE id = ?";
        try {
            PreparedStatement pst = cnx.prepareStatement(query);
            pst.setInt(1, id);
            pst.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'incrementation des vues: " + e.getMessage());
        }
    }

    public List<Ressource> getByCategorie(int categorieId) {
        List<Ressource> ressources = new ArrayList<>();
        String query = "SELECT r.*, c.nom as categorie_nom FROM ressource r LEFT JOIN categorie c ON r.categorie_id = c.id WHERE r.categorie_id = ?";
        try {
            PreparedStatement pst = cnx.prepareStatement(query);
            pst.setInt(1, categorieId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                Ressource r = new Ressource();
                r.setId(rs.getInt("id"));
                r.setTitre(rs.getString("titre"));
                r.setDescription(rs.getString("description"));
                r.setType(rs.getString("type"));
                r.setLien(rs.getString("lien"));
                r.setImage(rs.getString("image"));
                r.setDatePublication(rs.getTimestamp("date_publication"));
                r.setNbVues(rs.getInt("nb_vues"));
                r.setCategorieId(rs.getInt("categorie_id"));
                r.setCategorieNom(rs.getString("categorie_nom"));
                try {
                    String modStatus = rs.getString("moderation_status");
                    r.setModerationStatus(modStatus != null ? modStatus : "SAFE");
                    r.setModerationScore(rs.getDouble("moderation_score"));
                } catch (SQLException ex) {
                    r.setModerationStatus("SAFE");
                    r.setModerationScore(0.0);
                }
                ressources.add(r);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la recuperation des ressources par categorie: " + e.getMessage());
        }
        return ressources;
    }
    @Override
    public Ressource find(int id) {
        String query = "SELECT r.*, c.nom as categorie_nom FROM ressource r LEFT JOIN categorie c ON r.categorie_id = c.id WHERE r.id = ?";

        try {
            PreparedStatement pst = cnx.prepareStatement(query);
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                Ressource r = new Ressource();
                r.setId(rs.getInt("id"));
                r.setTitre(rs.getString("titre"));
                r.setDescription(rs.getString("description"));
                r.setType(rs.getString("type"));
                r.setLien(rs.getString("lien"));
                r.setImage(rs.getString("image"));
                r.setDatePublication(rs.getTimestamp("date_publication"));
                r.setNbVues(rs.getInt("nb_vues"));
                r.setCategorieId(rs.getInt("categorie_id"));
                r.setCategorieNom(rs.getString("categorie_nom"));

                try {
                    String modStatus = rs.getString("moderation_status");
                    r.setModerationStatus(modStatus != null ? modStatus : "SAFE");
                    r.setModerationScore(rs.getDouble("moderation_score"));
                } catch (SQLException ex) {
                    r.setModerationStatus("SAFE");
                    r.setModerationScore(0.0);
                }

                return r;
            }

        } catch (SQLException e) {
            System.out.println("Erreur find: " + e.getMessage());
        }

        return null;
    }
}
