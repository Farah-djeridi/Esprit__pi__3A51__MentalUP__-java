package services;

import models.SuiviMentale;
import utils.MyDataBase;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class SuiviMentaleAdminService {

    private final Connection cnx;

    public SuiviMentaleAdminService() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    public List<SuiviMentale> afficherTous() {
        List<SuiviMentale> liste = new ArrayList<>();
        String sql = "SELECT * FROM suivi_mentale ORDER BY date_de_suivi DESC";

        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                liste.add(mapResultSetToSuivi(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur afficherTous : " + e.getMessage(), e);
        }

        return liste;
    }

    public List<SuiviMentale> afficherParUserId(int userId) {
        List<SuiviMentale> liste = new ArrayList<>();
        String sql = "SELECT * FROM suivi_mentale WHERE user_id = ? ORDER BY date_de_suivi DESC";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    liste.add(mapResultSetToSuivi(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur afficherParUserId : " + e.getMessage(), e);
        }

        return liste;
    }

    public List<SuiviMentale> rechercher(Integer userId, String dateTxt, String humeurTxt) {
        List<SuiviMentale> liste = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM suivi_mentale WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (userId != null) {
            sql.append(" AND user_id = ?");
            params.add(userId);
        }

        if (dateTxt != null && !dateTxt.isEmpty()) {
            sql.append(" AND date_de_suivi = ?");
            params.add(Date.valueOf(dateTxt));
        }

        if (humeurTxt != null && !humeurTxt.isEmpty()) {
            sql.append(" AND LOWER(hummeur) LIKE ?");
            params.add("%" + humeurTxt.toLowerCase() + "%");
        }

        sql.append(" ORDER BY date_de_suivi DESC");

        try (PreparedStatement ps = cnx.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                Object param = params.get(i);

                if (param instanceof Integer) {
                    ps.setInt(i + 1, (Integer) param);
                } else if (param instanceof Date) {
                    ps.setDate(i + 1, (Date) param);
                } else {
                    ps.setString(i + 1, param.toString());
                }
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    liste.add(mapResultSetToSuivi(rs));
                }
            }

        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Date invalide. Utilise le format yyyy-mm-dd.");
        } catch (SQLException e) {
            throw new RuntimeException("Erreur rechercher : " + e.getMessage(), e);
        }

        return liste;
    }

    public void modifier(SuiviMentale s) throws SQLException {
        String sql = "UPDATE suivi_mentale SET " +
                "score_mentale = ?, " +
                "taux_de_stress = ?, " +
                "taux_de_stress_globale = ?, " +
                "date_de_suivi = ?, " +
                "qualite_du_sommeil = ?, " +
                "journal_emotionnelle = ?, " +
                "heuredesommeil = ?, " +
                "hummeur = ?, " +
                "niveaudenergie = ?, " +
                "user_id = ?, " +
                "objectif_id = ? " +
                "WHERE id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, s.getScoreMentale());
            ps.setInt(2, s.getTauxDeStress());
            ps.setInt(3, s.getTauxDeStressGlobale());
            ps.setDate(4, s.getDateDeSuivi());
            ps.setString(5, s.getQualiteDuSommeil());
            ps.setString(6, s.getJournalEmotionnelle());
            ps.setDouble(7, s.getHeureDeSommeil());
            ps.setString(8, s.getHumeur());
            ps.setInt(9, s.getNiveauDenergie());
            ps.setInt(10, s.getUserId());
            ps.setInt(11, s.getObjectifId());
            ps.setInt(12, s.getId());

            ps.executeUpdate();
        }
    }

    public void supprimer(SuiviMentale s) throws SQLException {
        String sql = "DELETE FROM suivi_mentale WHERE id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, s.getId());
            ps.executeUpdate();
        }
    }

    public Map<String, Double> getStatistiquesGlobales() {
        Map<String, Double> stats = new HashMap<>();

        String sql = """
                SELECT 
                    COUNT(*) AS total_suivis,
                    AVG(score_mentale) AS score_moyen,
                    AVG(taux_de_stress) AS stress_moyen,
                    AVG(niveaudenergie) AS energie_moyenne
                FROM suivi_mentale
                """;

        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                stats.put("total_suivis", rs.getDouble("total_suivis"));
                stats.put("score_moyen", rs.getDouble("score_moyen"));
                stats.put("stress_moyen", rs.getDouble("stress_moyen"));
                stats.put("energie_moyenne", rs.getDouble("energie_moyenne"));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur statistiques globales : " + e.getMessage(), e);
        }

        return stats;
    }

    public List<Map<String, Object>> getStatistiquesParUser() {
        List<Map<String, Object>> liste = new ArrayList<>();

        String sql = """
                SELECT 
                    s.user_id,
                    COUNT(*) AS total_suivis,
                    AVG(s.score_mentale) AS score_moyen,
                    AVG(s.taux_de_stress) AS stress_moyen,
                    AVG(s.niveaudenergie) AS energie_moyenne
                FROM suivi_mentale s
                GROUP BY s.user_id
                ORDER BY s.user_id ASC
                """;

        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int userId = rs.getInt("user_id");

                Map<String, Object> row = new HashMap<>();
                row.put("user_id", userId);
                row.put("user_name", getNomUtilisateurParId(userId));
                row.put("total_suivis", rs.getInt("total_suivis"));
                row.put("score_moyen", rs.getDouble("score_moyen"));
                row.put("stress_moyen", rs.getDouble("stress_moyen"));
                row.put("energie_moyenne", rs.getDouble("energie_moyenne"));
                liste.add(row);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur statistiques par user : " + e.getMessage(), e);
        }

        return liste;
    }

    public String getNomUtilisateurParId(int userId) {
        // Adapte cette requête si ta table utilisateur a un autre nom
        String sql = """
                SELECT 
                    id,
                    nom,
                    prenom
                FROM user
                WHERE id = ?
                """;

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
            System.out.println("Impossible de récupérer le nom utilisateur pour user_id=" + userId + " : " + e.getMessage());
        }

        return "Utilisateur #" + userId;
    }

    private SuiviMentale mapResultSetToSuivi(ResultSet rs) throws SQLException {
        SuiviMentale s = new SuiviMentale();
        s.setId(rs.getInt("id"));
        s.setScoreMentale(rs.getInt("score_mentale"));
        s.setTauxDeStress(rs.getInt("taux_de_stress"));
        s.setTauxDeStressGlobale(rs.getInt("taux_de_stress_globale"));
        s.setDateDeSuivi(rs.getDate("date_de_suivi"));
        s.setQualiteDuSommeil(rs.getString("qualite_du_sommeil"));
        s.setJournalEmotionnelle(rs.getString("journal_emotionnelle"));
        s.setHeureDeSommeil(rs.getDouble("heuredesommeil"));
        s.setHumeur(rs.getString("hummeur"));
        s.setNiveauDenergie(rs.getInt("niveaudenergie"));
        s.setUserId(rs.getInt("user_id"));
        s.setObjectifId(rs.getInt("objectif_id"));
        return s;
    }
}