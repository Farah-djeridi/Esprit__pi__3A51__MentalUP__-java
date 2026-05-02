package services;

import interfaces.IService;
import models.SuiviMentale;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceSuiviMentale implements IService<SuiviMentale> {

    private final Connection cnx;

    public ServiceSuiviMentale() {
        this.cnx = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void add(SuiviMentale s) {
        String req = "INSERT INTO suivi_mentale " +
                "(score_mentale, taux_de_stress, taux_de_stress_globale, date_de_suivi, qualite_du_sommeil, journal_emotionnelle, heuredesommeil, hummeur, niveaudenergie, user_id, objectif_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement ps = cnx.prepareStatement(req);
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

            ps.executeUpdate();
            System.out.println("Suivi ajouté avec succès");

        } catch (SQLException e) {
            System.out.println("Erreur add suivi : " + e.getMessage());
        }
    }

    @Override
    public List<SuiviMentale> getAll() {
        List<SuiviMentale> liste = new ArrayList<>();
        String req = "SELECT * FROM suivi_mentale ORDER BY date_de_suivi DESC, id DESC";

        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(req);

            while (rs.next()) {
                liste.add(mapResultSet(rs));
            }

        } catch (SQLException e) {
            System.out.println("Erreur getAll suivi : " + e.getMessage());
        }

        return liste;
    }

    public List<SuiviMentale> getByUser(int userId) {
        List<SuiviMentale> liste = new ArrayList<>();
        String req = "SELECT * FROM suivi_mentale WHERE user_id = ? ORDER BY date_de_suivi DESC, id DESC";

        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                liste.add(mapResultSet(rs));
            }

        } catch (SQLException e) {
            System.out.println("Erreur getByUser suivi : " + e.getMessage());
        }

        return liste;
    }

    public List<SuiviMentale> getByObjectif(int objectifId) {
        List<SuiviMentale> liste = new ArrayList<>();
        String req = "SELECT * FROM suivi_mentale WHERE objectif_id = ? ORDER BY date_de_suivi DESC, id DESC";

        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, objectifId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                liste.add(mapResultSet(rs));
            }

        } catch (SQLException e) {
            System.out.println("Erreur getByObjectif suivi : " + e.getMessage());
        }

        return liste;
    }

    public SuiviMentale getById(int id) {
        String req = "SELECT * FROM suivi_mentale WHERE id = ?";

        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapResultSet(rs);
            }

        } catch (SQLException e) {
            System.out.println("Erreur getById suivi : " + e.getMessage());
        }

        return null;
    }

    public boolean hasSuiviToday(int userId) {
        String req = "SELECT COUNT(*) FROM suivi_mentale WHERE user_id = ? AND date_de_suivi = CURDATE()";

        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.out.println("Erreur vérification suivi du jour : " + e.getMessage());
        }

        return false;
    }

    public SuiviMentale getDernierSuiviParUser(int userId) {
        List<SuiviMentale> suivis = getByUser(userId);

        if (suivis == null || suivis.isEmpty()) {
            return null;
        }

        SuiviMentale dernier = suivis.get(0);

        for (SuiviMentale s : suivis) {
            if (s.getDateDeSuivi() != null && dernier.getDateDeSuivi() != null) {
                if (s.getDateDeSuivi().after(dernier.getDateDeSuivi())) {
                    dernier = s;
                }
            }
        }

        return dernier;
    }

    @Override
    public void update(SuiviMentale s) {
        String req = "UPDATE suivi_mentale SET " +
                "score_mentale=?, taux_de_stress=?, taux_de_stress_globale=?, date_de_suivi=?, qualite_du_sommeil=?, journal_emotionnelle=?, heuredesommeil=?, hummeur=?, niveaudenergie=?, user_id=?, objectif_id=? " +
                "WHERE id=?";

        try {
            PreparedStatement ps = cnx.prepareStatement(req);
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
            System.out.println("Suivi modifié avec succès");

        } catch (SQLException e) {
            System.out.println("Erreur update suivi : " + e.getMessage());
        }
    }

    @Override
    public void delete(SuiviMentale s) {
        String req = "DELETE FROM suivi_mentale WHERE id=?";

        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, s.getId());
            ps.executeUpdate();
            System.out.println("Suivi supprimé avec succès");

        } catch (SQLException e) {
            System.out.println("Erreur delete suivi : " + e.getMessage());
        }
    }

    private SuiviMentale mapResultSet(ResultSet rs) throws SQLException {
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
    @Override
    public SuiviMentale find(int id) {
        return getById(id);
    }
}