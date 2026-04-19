package Services;

import Models.RendezVous;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ServiceRendezVous {

    private final Connection conn;

    public ServiceRendezVous() {
        this.conn = MyDataBase.getInstance().getCnx();
    }

    // CREATE
    public void add(RendezVous r) {
        String sql = "INSERT INTO rendez_vous " +
                "(date, heure_debut, heure_fin, type_rdv, statut, psychologue_id, lieu, telephone) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, r.getDate());
            ps.setTime(2, r.getHeureDebut());
            ps.setTime(3, r.getHeureFin());
            ps.setString(4, r.getTypeRdv());
            ps.setString(5, r.getStatut());
            ps.setInt(6, r.getPsychologueId());
            ps.setString(7, r.getLieu());
            ps.setString(8, r.getTelephone());
            ps.executeUpdate();
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
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("[getAll] " + e.getMessage());
        }
        return list;
    }

    // UPDATE
    public void update(RendezVous r) {
        // Automatically generate Jitsi link if confirmed and online (mode is in 'lieu')
        if ("confirmé".equalsIgnoreCase(r.getStatut()) && "En ligne".equalsIgnoreCase(r.getLieu()) && (r.getLienMeet() == null || r.getLienMeet().isEmpty())) {
            String roomName = "MentalUp-" + UUID.randomUUID().toString().substring(0, 8);
            r.setLienMeet("https://meet.jit.si/" + roomName);
        }

        String sql = "UPDATE rendez_vous SET " +
                "date=?, heure_debut=?, heure_fin=?, type_rdv=?, statut=?, lieu=?, telephone=?, lien_meet=?, etudiant_id=? " +
                "WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, r.getDate());
            ps.setTime(2, r.getHeureDebut());
            ps.setTime(3, r.getHeureFin());
            ps.setString(4, r.getTypeRdv());
            ps.setString(5, r.getStatut());
            ps.setString(6, r.getLieu());
            ps.setString(7, r.getTelephone());
            ps.setString(8, r.getLienMeet());
            if (r.getEtudiantId() != null) ps.setInt(9, r.getEtudiantId()); else ps.setNull(9, Types.INTEGER);
            ps.setInt(10, r.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[update] " + e.getMessage());
        }
    }

    public void setPsyJoined(int rdvId, boolean joined) {
        // Map psyJoined to statut='en cours'
        String newStatut = joined ? "en cours" : "confirmé";
        String sql = "UPDATE rendez_vous SET statut = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatut);
            ps.setInt(2, rdvId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[setPsyJoined] " + e.getMessage());
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM rendez_vous WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[delete] " + e.getMessage());
        }
    }

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
        return list;
    }

    public boolean reserverCreneau(int rdvId, int etudiantId, String mode) {
        // Map mode to lieu
        String sql = "UPDATE rendez_vous SET etudiant_id = ?, statut = 'en attente', lieu = ? " +
                "WHERE id = ? AND (statut = 'libre' OR statut = 'disponible')";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, etudiantId);
            ps.setString(2, mode);
            ps.setInt(3, rdvId);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("[reserverCreneau] " + e.getMessage());
            return false;
        }
    }

    public boolean annulerReservation(int rdvId, int etudiantId) {
        String sql = "UPDATE rendez_vous SET etudiant_id = NULL, statut = 'libre', lieu = NULL, lien_meet = NULL " +
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

    public void refuserRdv(int id) {
        String sql = "UPDATE rendez_vous SET statut='libre', etudiant_id=NULL, lieu=NULL, lien_meet=NULL WHERE id=?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
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

    public void terminerRdv(int id) {
        String sql = "UPDATE rendez_vous SET statut='terminé' WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

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

    public void confirmerRdv(int id, String mode) {
        // Map mode to lieu
        String meetLink = null;
        if ("En ligne".equalsIgnoreCase(mode)) {
            meetLink = "https://meet.jit.si/MentalUp-" + UUID.randomUUID().toString().substring(0, 8);
        }
        String sql = "UPDATE rendez_vous SET statut='confirmé', lieu=?, lien_meet=? WHERE id=?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, mode);
            ps.setString(2, meetLink);
            ps.setInt(3, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public RendezVous getById(int id) {
        String sql = "SELECT * FROM rendez_vous WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }
}