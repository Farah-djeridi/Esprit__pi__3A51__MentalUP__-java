package services;

import models.Objectif;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceObjectifAdmin {

    private final Connection cnx;

    public ServiceObjectifAdmin() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    public List<Objectif> afficherTous() {
        List<Objectif> liste = new ArrayList<>();
        String sql = "SELECT * FROM objectif ORDER BY id DESC";

        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                liste.add(mapResultSetToObjectif(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur afficherTous objectifs : " + e.getMessage(), e);
        }

        return liste;
    }

    public List<Objectif> filtrer(String titre, String statut) {
        List<Objectif> liste = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM objectif WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (titre != null && !titre.isEmpty()) {
            sql.append(" AND LOWER(titre) LIKE ?");
            params.add("%" + titre.toLowerCase() + "%");
        }

        if (statut != null && !statut.isEmpty()) {
            sql.append(" AND LOWER(statut_objectif) = ?");
            params.add(statut.toLowerCase());
        }

        sql.append(" ORDER BY id DESC");

        try (PreparedStatement ps = cnx.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setString(i + 1, params.get(i).toString());
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    liste.add(mapResultSetToObjectif(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur filtrer objectifs : " + e.getMessage(), e);
        }

        return liste;
    }

    public void modifier(Objectif o) throws SQLException {
        String sql = "UPDATE objectif SET " +
                "date_debut = ?, " +
                "date_fin = ?, " +
                "titre = ?, " +
                "description = ?, " +
                "date_creation = ?, " +
                "statut_objectif = ?, " +
                "progression = ?, " +
                "type_objectif = ?, " +
                "valeur_cible = ?, " +
                "user_id = ? " +
                "WHERE id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setDate(1, o.getDateDebut());
            ps.setDate(2, o.getDateFin());
            ps.setString(3, o.getTitre());
            ps.setString(4, o.getDescription());
            ps.setDate(5, o.getDateCreation());
            ps.setString(6, o.getStatutObjectif());
            ps.setInt(7, o.getProgression());
            ps.setString(8, o.getTypeObjectif());
            ps.setDouble(9, o.getValeurCible());
            ps.setInt(10, o.getUserId());
            ps.setInt(11, o.getId());

            ps.executeUpdate();
        }
    }

    public void supprimer(Objectif o) throws SQLException {
        String sql = "DELETE FROM objectif WHERE id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, o.getId());
            ps.executeUpdate();
        }
    }

    public Map<String, Integer> getStatistiques() {
        Map<String, Integer> stats = new HashMap<>();

        String sql = """
                SELECT
                    COUNT(*) AS total,
                    SUM(CASE WHEN LOWER(statut_objectif) = 'en cours' THEN 1 ELSE 0 END) AS en_cours,
                    SUM(CASE WHEN LOWER(statut_objectif) LIKE 'atteint%' THEN 1 ELSE 0 END) AS atteints,
                    SUM(CASE WHEN LOWER(statut_objectif) LIKE 'annul%' THEN 1 ELSE 0 END) AS annules
                FROM objectif
                """;

        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                stats.put("total", rs.getInt("total"));
                stats.put("en_cours", rs.getInt("en_cours"));
                stats.put("atteints", rs.getInt("atteints"));
                stats.put("annules", rs.getInt("annules"));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur statistiques objectifs : " + e.getMessage(), e);
        }

        return stats;
    }

    public String getNomUtilisateurParId(int userId) {
        String sql = "SELECT nom, prenom FROM user WHERE id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String nom = rs.getString("nom");
                    String prenom = rs.getString("prenom");
                    String fullName = ((prenom != null ? prenom : "") + " " + (nom != null ? nom : "")).trim();

                    if (!fullName.isEmpty()) {
                        return fullName;
                    }
                }
            }

        } catch (SQLException e) {
            System.out.println("Erreur récupération nom user : " + e.getMessage());
        }

        return "Utilisateur #" + userId;
    }

    private Objectif mapResultSetToObjectif(ResultSet rs) throws SQLException {
        Objectif o = new Objectif();

        o.setId(rs.getInt("id"));
        o.setDateDebut(rs.getDate("date_debut"));
        o.setDateFin(rs.getDate("date_fin"));
        o.setTitre(rs.getString("titre"));
        o.setDescription(rs.getString("description"));
        o.setDateCreation(rs.getDate("date_creation"));
        o.setStatutObjectif(rs.getString("statut_objectif"));
        o.setProgression(rs.getInt("progression"));
        o.setTypeObjectif(rs.getString("type_objectif"));
        o.setValeurCible(rs.getDouble("valeur_cible"));
        o.setUserId(rs.getInt("user_id"));

        // On ignore id_activite ici pour éviter l'erreur
        o.setIdActivite(null);

        return o;
    }
}