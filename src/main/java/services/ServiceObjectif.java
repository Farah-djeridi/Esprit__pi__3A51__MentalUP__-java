package services;

import interfaces.IService;
import models.Activite;
import models.Objectif;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceObjectif implements IService<Objectif> {

    private final Connection cnx;

    public ServiceObjectif() {
        this.cnx = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void add(Objectif o) {
        String req = "INSERT INTO objectif " +
                "(date_debut, date_fin, titre, description, date_creation, statut_objectif, progression, type_objectif, valeur_cible, id_activite_id, user_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement ps = cnx.prepareStatement(req);

            ps.setDate(1, o.getDateDebut());
            ps.setDate(2, o.getDateFin());
            ps.setString(3, o.getTitre());
            ps.setString(4, o.getDescription());
            ps.setDate(5, o.getDateCreation());
            ps.setString(6, o.getStatutObjectif());
            ps.setInt(7, o.getProgression());
            ps.setString(8, o.getTypeObjectif());
            ps.setDouble(9, o.getValeurCible());

            if (o.getIdActivite() != null) {
                ps.setInt(10, o.getIdActivite());
            } else {
                ps.setNull(10, Types.INTEGER);
            }

            ps.setInt(11, o.getUserId());

            ps.executeUpdate();
            System.out.println("Objectif ajouté avec succès");

        } catch (SQLException e) {
            System.out.println("Erreur add objectif : " + e.getMessage());
        }
    }

    @Override
    public List<Objectif> getAll() {
        List<Objectif> objectifs = new ArrayList<>();
        String req = "SELECT * FROM objectif ORDER BY date_creation DESC, id DESC";

        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(req);

            while (rs.next()) {
                objectifs.add(mapResultSetToObjectif(rs));
            }

        } catch (SQLException e) {
            System.out.println("Erreur getAll objectif : " + e.getMessage());
        }

        return objectifs;
    }

    public List<Objectif> getByUser(int userId) {
        List<Objectif> objectifs = new ArrayList<>();
        String req = "SELECT * FROM objectif WHERE user_id = ? ORDER BY date_creation DESC, id DESC";

        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                objectifs.add(mapResultSetToObjectif(rs));
            }

        } catch (SQLException e) {
            System.out.println("Erreur getByUser objectif : " + e.getMessage());
        }

        return objectifs;
    }

    public Objectif getById(int id) {
        String req = "SELECT * FROM objectif WHERE id = ?";

        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapResultSetToObjectif(rs);
            }

        } catch (SQLException e) {
            System.out.println("Erreur getById objectif : " + e.getMessage());
        }

        return null;
    }

    public Objectif getObjectifEnCoursByUser(int userId) {
        String req = "SELECT * FROM objectif WHERE user_id = ? AND LOWER(statut_objectif) = 'en cours' ORDER BY id DESC LIMIT 1";

        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapResultSetToObjectif(rs);
            }

        } catch (SQLException e) {
            System.out.println("Erreur getObjectifEnCoursByUser : " + e.getMessage());
        }

        return null;
    }

    public boolean hasObjectifEnCours(int userId) {
        return getObjectifEnCoursByUser(userId) != null;
    }

    public int countByStatusForUser(int userId, String statut) {
        String req = "SELECT COUNT(*) FROM objectif WHERE user_id = ? AND LOWER(statut_objectif) = LOWER(?)";

        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, userId);
            ps.setString(2, statut);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.out.println("Erreur countByStatusForUser : " + e.getMessage());
        }

        return 0;
    }

    public void updateProgression(int objectifId, int progression) {
        String req = "UPDATE objectif SET progression = ? WHERE id = ?";

        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, progression);
            ps.setInt(2, objectifId);
            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Erreur updateProgression objectif : " + e.getMessage());
        }
    }

    @Override
    public void update(Objectif o) {
        String req = "UPDATE objectif SET " +
                "date_debut=?, date_fin=?, titre=?, description=?, date_creation=?, statut_objectif=?, progression=?, type_objectif=?, valeur_cible=?, id_activite_id=?, user_id=? " +
                "WHERE id=?";

        try {
            PreparedStatement ps = cnx.prepareStatement(req);

            ps.setDate(1, o.getDateDebut());
            ps.setDate(2, o.getDateFin());
            ps.setString(3, o.getTitre());
            ps.setString(4, o.getDescription());
            ps.setDate(5, o.getDateCreation());
            ps.setString(6, o.getStatutObjectif());
            ps.setInt(7, o.getProgression());
            ps.setString(8, o.getTypeObjectif());
            ps.setDouble(9, o.getValeurCible());

            if (o.getIdActivite() != null) {
                ps.setInt(10, o.getIdActivite());
            } else {
                ps.setNull(10, Types.INTEGER);
            }

            ps.setInt(11, o.getUserId());
            ps.setInt(12, o.getId());

            ps.executeUpdate();
            System.out.println("Objectif modifié avec succès");

        } catch (SQLException e) {
            System.out.println("Erreur update objectif : " + e.getMessage());
        }
    }

    @Override
    public void delete(Objectif o) {
        String req = "DELETE FROM objectif WHERE id=?";

        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, o.getId());
            ps.executeUpdate();
            System.out.println("Objectif supprimé avec succès");

        } catch (SQLException e) {
            System.out.println("Erreur delete objectif : " + e.getMessage());
        }
    }
    public int terminerObjectifsExpiresByUser(int userId) {
        String req = "UPDATE objectif " +
                "SET statut_objectif = 'terminé', progression = 100 " +
                "WHERE user_id = ? " +
                "AND LOWER(statut_objectif) = 'en cours' " +
                "AND date_fin < CURDATE()";

        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, userId);

            int rows = ps.executeUpdate();
            System.out.println("Objectifs expirés terminés automatiquement : " + rows);
            return rows;

        } catch (SQLException e) {
            System.out.println("Erreur terminerObjectifsExpiresByUser : " + e.getMessage());
            return 0;
        }
    }

    public List<Activite> getActivites() {
        List<Activite> activites = new ArrayList<>();
        String req = "SELECT id_activite, titre FROM activite";

        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(req);

            while (rs.next()) {
                Activite a = new Activite();
                a.setIdActivite(rs.getInt("id_activite"));
                a.setTitre(rs.getString("titre"));
                activites.add(a);
            }
        } catch (SQLException e) {
            System.out.println("Erreur chargement activités : " + e.getMessage());
        }

        return activites;
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

        int idAct = rs.getInt("id_activite_id");
        if (rs.wasNull()) {
            o.setIdActivite(null);
        } else {
            o.setIdActivite(idAct);
        }

        o.setUserId(rs.getInt("user_id"));

        return o;
    }
    @Override
    public Objectif find(int id) {
        return getById(id);
    }
}