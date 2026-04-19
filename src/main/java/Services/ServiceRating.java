package Services;

import Models.Rating;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceRating {
    private Connection cnx;

    public ServiceRating() {
        this.cnx = MyDataBase.getInstance().getCnx();
    }

    public void add(Rating r) {
        String query = "INSERT INTO rating (etudiant_id, psychologue_id, note, commentaires) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, r.getEtudiantId());
            ps.setInt(2, r.getPsychologueId());
            ps.setInt(3, r.getNote());
            ps.setString(4, r.getCommentaires());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public double getAverageForPsy(int psyId) {
        String query = "SELECT AVG(note) as average FROM rating WHERE psychologue_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, psyId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble("average");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public int getCountForPsy(int psyId) {
        String query = "SELECT COUNT(*) as count FROM rating WHERE psychologue_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, psyId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
