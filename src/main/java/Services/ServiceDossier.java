package Services;

import interfaces.IService;
import Models.Dossier;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceDossier implements IService<Dossier> {

    private Connection cnx;

    public ServiceDossier() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void add(Dossier d) {
        String query = "INSERT INTO dossier_patient (date_creation, notes_generales, niveau_risque, patient_id, psychologue_id) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setDate(1, d.getDateCreation());
            ps.setString(2, d.getNotesGenerales());
            ps.setString(3, d.getNiveauRisque());
            ps.setInt(4, d.getPatientId());
            ps.setInt(5, d.getPsychologueId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(Dossier d) {
        String query = "UPDATE dossier_patient SET date_creation=?, notes_generales=?, niveau_risque=?, patient_id=?, psychologue_id=?, ai_summary=?, ai_key_points=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setDate(1, d.getDateCreation());
            ps.setString(2, d.getNotesGenerales());
            ps.setString(3, d.getNiveauRisque());
            ps.setInt(4, d.getPatientId());
            ps.setInt(5, d.getPsychologueId());
            ps.setString(6, d.getAiSummary());
            ps.setString(7, d.getAiKeyPoints());
            ps.setInt(8, d.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(Dossier d) {
        String query = "DELETE FROM dossier_patient WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, d.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public Dossier find(int id) {
        String query ="SELECT d.*, u.nom, u.prenom " +
                "FROM dossier_patient d " +
                "JOIN user u ON d.patient_id = u.id " +
                "WHERE d.id=?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Dossier(
                        rs.getInt("id"),
                        rs.getDate("date_creation"),
                        rs.getString("notes_generales"),
                        rs.getString("niveau_risque"),
                        rs.getInt("patient_id"),
                        rs.getInt("psychologue_id"),
                        rs.getString("ai_summary"),
                        rs.getString("ai_key_points")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Dossier> getAll() {
        List<Dossier> dossiers = new ArrayList<>();
        String query =
                "SELECT d.*, u.nom, u.prenom " +
                        "FROM dossier_patient d " +
                        "JOIN user u ON d.patient_id = u.id";

        try (Statement st = cnx.createStatement()) {
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                dossiers.add(new Dossier(
                        rs.getInt("id"),
                        rs.getDate("date_creation"),
                        rs.getString("notes_generales"),
                        rs.getString("niveau_risque"),
                        rs.getInt("patient_id"),
                        rs.getInt("psychologue_id"),
                        rs.getString("ai_summary"),
                        rs.getString("ai_key_points")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dossiers;


    }

    // Recherche par patientId ou mot clé
    public List<Dossier> search(String keyword) {
        List<Dossier> results = new ArrayList<>();
        String query = "SELECT * FROM dossier_patient WHERE notes_generales LIKE ? OR niveau_risque LIKE ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setString(1, "%" + keyword + "%");
            ps.setString(2, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                results.add(new Dossier(
                        rs.getInt("id"),
                        rs.getDate("date_creation"),
                        rs.getString("notes_generales"),
                        rs.getString("niveau_risque"),
                        rs.getInt("patient_id"),
                        rs.getInt("psychologue_id"),
                        rs.getString("ai_summary"),
                        rs.getString("ai_key_points")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    // Tri par date (asc ou desc)
    public List<Dossier> sortByDate(boolean ascending) {
        List<Dossier> results = new ArrayList<>();
        String query = "SELECT * FROM dossier_patient ORDER BY date_creation " + (ascending ? "ASC" : "DESC");
        try (Statement st = cnx.createStatement()) {
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                results.add(new Dossier(
                        rs.getInt("id"),
                        rs.getDate("date_creation"),
                        rs.getString("notes_generales"),
                        rs.getString("niveau_risque"),
                        rs.getInt("patient_id"),
                        rs.getInt("psychologue_id"),
                        rs.getString("ai_summary"),
                        rs.getString("ai_key_points")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }


}