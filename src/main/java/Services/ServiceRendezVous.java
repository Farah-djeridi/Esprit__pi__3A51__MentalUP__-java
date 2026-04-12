package Services;

import Models.RendezVous;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceRendezVous {

    private final Connection conn;

    public ServiceRendezVous() {
        this.conn = MyDataBase.getInstance().getCnx();
    }

    // CREATE
    public void add(RendezVous r) {
        String sql = "INSERT INTO rendez_vous " +
                "(date, heure_debut, heure_fin, type_rdv, statut, psychologue_id) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, r.getDate());
            ps.setTime(2, r.getHeureDebut());
            ps.setTime(3, r.getHeureFin());
            ps.setString(4, r.getTypeRdv());
            ps.setString(5, r.getStatut());
            ps.setInt(6, r.getPsychologueId());
            ps.executeUpdate();
            System.out.println("Créneau ajouté : " + r);
        } catch (SQLException e) {
            System.err.println("[add] " + e.getMessage());
        }
    }

    // READ ALL
    public List<RendezVous> getAll() {
        List<RendezVous> list = new ArrayList<>();
        String sql = "SELECT * FROM rendez_vous ORDER BY date, heure_debut";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                RendezVous r = new RendezVous();
                r.setId(rs.getInt("id"));
                r.setDate(rs.getDate("date"));
                r.setHeureDebut(rs.getTime("heure_debut"));
                r.setHeureFin(rs.getTime("heure_fin"));
                r.setTypeRdv(rs.getString("type_rdv"));
                r.setStatut(rs.getString("statut"));
                r.setPsychologueId(rs.getInt("psychologue_id"));
                r.setLienMeet(rs.getString("lien_meet"));
                r.setLieu(rs.getString("lieu"));
                r.setTelephone(rs.getString("telephone"));
                int etudiantId = rs.getInt("etudiant_id");
                r.setEtudiantId(rs.wasNull() ? null : etudiantId);
                list.add(r);
            }
        } catch (SQLException e) {
            System.err.println("[getAll] " + e.getMessage());
        }
        return list;
    }

    // UPDATE
    public void update(RendezVous r) {
        String sql = "UPDATE rendez_vous SET " +
                "date=?, heure_debut=?, heure_fin=?, type_rdv=?, statut=? " +
                "WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, r.getDate());
            ps.setTime(2, r.getHeureDebut());
            ps.setTime(3, r.getHeureFin());
            ps.setString(4, r.getTypeRdv());
            ps.setString(5, r.getStatut());
            ps.setInt(6, r.getId());
            ps.executeUpdate();
            System.out.println("Créneau mis à jour : id=" + r.getId());
        } catch (SQLException e) {
            System.err.println("[update] " + e.getMessage());
        }
    }

    // DELETE
    public void delete(int id) {
        String sql = "DELETE FROM rendez_vous WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Créneau supprimé : id=" + id);
        } catch (SQLException e) {
            System.err.println("[delete] " + e.getMessage());
        }
    }
}