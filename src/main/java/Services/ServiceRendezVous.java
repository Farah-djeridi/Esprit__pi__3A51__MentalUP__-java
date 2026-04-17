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
        String sql = "SELECT * FROM rendez_vous WHERE date IS NOT NULL AND date != '0000-00-00' ORDER BY date, heure_debut";
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

    // ── Créneaux d'un psychologue (pour le calendrier étudiant) ──
    public List<RendezVous> getByPsychologueId(int psychologueId) {
        List<RendezVous> list = new ArrayList<>();
        String sql = "SELECT * FROM rendez_vous WHERE psychologue_id = ? AND date IS NOT NULL AND date != '0000-00-00' ORDER BY date, heure_debut";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, psychologueId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("[getByPsychologueId] " + e.getMessage());
        }
        System.out.println("[getByPsychologueId] psyId=" + psychologueId + " → " + list.size() + " créneaux trouvés");
        return list;
    }

    public boolean reserverCreneau(int rdvId, int etudiantId) {
        // Accepte "libre" ET "disponible" comme statuts réservables
        String sql = "UPDATE rendez_vous SET etudiant_id = ?, statut = 'en attente' " +
                "WHERE id = ? AND (statut = 'libre' OR statut = 'disponible')";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, etudiantId);
            ps.setInt(2, rdvId);
            int rows = ps.executeUpdate();
            System.out.println("[reserverCreneau] rows updated=" + rows);
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("[reserverCreneau] " + e.getMessage());
            return false;
        }
    }

    // ── Annuler une réservation (remet à "libre") ──
    public boolean annulerReservation(int rdvId, int etudiantId) {
        String sql = "UPDATE rendez_vous SET etudiant_id = NULL, statut = 'libre' " +
                "WHERE id = ? AND etudiant_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, rdvId);
            ps.setInt(2, etudiantId);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("[annulerReservation] " + e.getMessage());
            return false;
        }
    }
    // ── Tous les RDV d'un étudiant ──
    public List<RendezVous> getByEtudiantId(int etudiantId) {
        List<RendezVous> list = new ArrayList<>();
        String sql = "SELECT * FROM rendez_vous WHERE etudiant_id = ? AND date IS NOT NULL AND date != '0000-00-00' ORDER BY date DESC, heure_debut DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, etudiantId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("[getByEtudiantId] " + e.getMessage());
        }
        return list;
    }

    // ── RDV d'aujourd'hui pour un étudiant ──
    public List<RendezVous> getRdvAujourdhui(int etudiantId) {
        List<RendezVous> list = new ArrayList<>();
        String sql = "SELECT * FROM rendez_vous WHERE etudiant_id = ? AND date IS NOT NULL AND date != '0000-00-00' AND date = CURDATE() ORDER BY heure_debut";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, etudiantId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("[getRdvAujourdhui] " + e.getMessage());
        }
        return list;
    }

    // ── RDV à venir (après aujourd'hui) ──
    public List<RendezVous> getRdvAvenir(int etudiantId) {
        List<RendezVous> list = new ArrayList<>();
        String sql = "SELECT * FROM rendez_vous WHERE etudiant_id = ? AND date IS NOT NULL AND date != '0000-00-00' AND date > CURDATE() ORDER BY date, heure_debut";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, etudiantId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("[getRdvAvenir] " + e.getMessage());
        }
        return list;
    }

    // ── RDV anciens (avant aujourd'hui) ──
    public List<RendezVous> getRdvAnciens(int etudiantId) {
        List<RendezVous> list = new ArrayList<>();
        String sql = "SELECT * FROM rendez_vous WHERE etudiant_id = ? AND date IS NOT NULL AND date != '0000-00-00' AND date < CURDATE() ORDER BY date DESC, heure_debut DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, etudiantId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("[getRdvAnciens] " + e.getMessage());
        }
        return list;
    }

    // ── Récupérer les psychologues distincts ──
    public List<Integer> getPsychologuesIds() {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT DISTINCT psychologue_id FROM rendez_vous ORDER BY psychologue_id";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                ids.add(rs.getInt("psychologue_id"));
            }
        } catch (SQLException e) {
            System.err.println("[getPsychologuesIds] " + e.getMessage());
        }
        return ids;
    }

    // ── Helper privé : mapping ResultSet → RendezVous ──
    private RendezVous mapResultSet(ResultSet rs) throws SQLException {
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
        return r;
    }

    public List<RendezVous> getRdvEnAttente(int psyId) {
        List<RendezVous> list = new ArrayList<>();
        String sql = "SELECT * FROM rendez_vous WHERE psychologue_id = ? AND (statut = 'réservé' OR statut = 'en attente')";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, psyId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return list;
    }

    // Confirmer RDV
    public void confirmerRdv(int id) {
        String sql = "UPDATE rendez_vous SET statut='confirmé' WHERE id=?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // Refuser RDV
    public void refuserRdv(int id) {
        String sql = "UPDATE rendez_vous SET statut='libre', etudiant_id=NULL WHERE id=?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
}