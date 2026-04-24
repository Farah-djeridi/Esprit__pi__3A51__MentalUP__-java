package Services;

import Models.RendezVous;
import utils.MyDataBase;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
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

    public List<RendezVous> getSlotsWithVirtuals(int psyId, LocalDate start, LocalDate end) {
        List<RendezVous> realRdvs = getByPsychologueId(psyId);
        List<RendezVous> result = new ArrayList<>();

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            // Lundi (1) à Vendredi (5)
            int dayOfWeek = date.getDayOfWeek().getValue();
            if (dayOfWeek >= 1 && dayOfWeek <= 5) {
                // Créneaux du matin
                addVirtualIfMissing(result, realRdvs, psyId, date, "09:00", "10:00");
                addVirtualIfMissing(result, realRdvs, psyId, date, "10:00", "11:00");
                addVirtualIfMissing(result, realRdvs, psyId, date, "11:00", "12:00");
                addVirtualIfMissing(result, realRdvs, psyId, date, "12:00", "12:30");
                
                // Pause 12:30 - 13:30
                
                // Créneaux de l'après-midi
                addVirtualIfMissing(result, realRdvs, psyId, date, "13:30", "14:30");
                addVirtualIfMissing(result, realRdvs, psyId, date, "14:30", "15:30");
                addVirtualIfMissing(result, realRdvs, psyId, date, "15:30", "16:00");
            }
        }
        
        // Ajouter aussi les RDV qui ne sont pas dans les horaires standards (si existants)
        for (RendezVous r : realRdvs) {
            if (!result.stream().anyMatch(v -> v.getId() == r.getId())) {
                result.add(r);
            }
        }

        return result;
    }

    private void addVirtualIfMissing(List<RendezVous> result, List<RendezVous> realRdvs, int psyId, LocalDate date, String startStr, String endStr) {
        Time start = Time.valueOf(startStr + ":00");
        Time end = Time.valueOf(endStr + ":00");
        
        // Vérifier si un RDV réel existe déjà à cette heure
        RendezVous existing = realRdvs.stream()
                .filter(r -> r.getDate().toLocalDate().equals(date) && r.getHeureDebut().equals(start))
                .findFirst()
                .orElse(null);
        
        if (existing != null) {
            result.add(existing);
        } else {
            // Créer un créneau virtuel
            RendezVous v = new RendezVous();
            v.setId(-1); // Indique un créneau virtuel
            v.setDate(Date.valueOf(date));
            v.setHeureDebut(start);
            v.setHeureFin(end);
            v.setStatut("libre");
            v.setPsychologueId(psyId);
            v.setTypeRdv("consultation");
            result.add(v);
        }
    }

    public boolean reserverCreneau(RendezVous r, int etudiantId, String mode, String telephone) {
        if (r.getId() != -1) {
            // Créneau réel existant
            String sql = "UPDATE rendez_vous SET etudiant_id = ?, statut = 'en attente', lieu = ?, telephone = ? " +
                    "WHERE id = ? AND (statut = 'libre' OR statut = 'disponible')";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, etudiantId);
                ps.setString(2, mode);
                ps.setString(3, telephone);
                ps.setInt(4, r.getId());
                int rows = ps.executeUpdate();
                return rows > 0;
            } catch (SQLException e) {
                System.err.println("[reserverCreneau UPDATE] " + e.getMessage());
                return false;
            }
        } else {
            // Créneau virtuel -> INSERT
            String sql = "INSERT INTO rendez_vous (date, heure_debut, heure_fin, type_rdv, statut, psychologue_id, etudiant_id, lieu, telephone) " +
                    "VALUES (?, ?, ?, ?, 'en attente', ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setDate(1, r.getDate());
                ps.setTime(2, r.getHeureDebut());
                ps.setTime(3, r.getHeureFin());
                ps.setString(4, r.getTypeRdv() != null ? r.getTypeRdv() : "consultation");
                ps.setInt(5, r.getPsychologueId());
                ps.setInt(6, etudiantId);
                ps.setString(7, mode);
                ps.setString(8, telephone);
                ps.executeUpdate();
                
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        r.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            } catch (SQLException e) {
                System.err.println("[reserverCreneau INSERT] " + e.getMessage());
                return false;
            }
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
        RendezVous r = getById(id);
        String sql = "UPDATE rendez_vous SET statut='libre', etudiant_id=NULL, lieu=NULL, lien_meet=NULL WHERE id=?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            
            // Envoyer SMS auto
            if (r != null && r.getTelephone() != null && !r.getTelephone().isEmpty()) {
                SmsService.notifyRdvAnnulation(r.getTelephone(), r.getDate() != null ? r.getDate().toString() : "");
            }
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
        RendezVous r = getById(id);
        String lienMeet = null;
        if ("En ligne".equalsIgnoreCase(mode)) {
            // Générer un lien Jitsi Meet unique
            String roomName = "MentalUP-" + UUID.randomUUID().toString().substring(0, 8);
            lienMeet = "https://meet.jit.si/" + roomName;
        }

        String sql = "UPDATE rendez_vous SET statut='confirmé', lieu=?, lien_meet=? WHERE id=?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, mode);
            ps.setString(2, lienMeet);
            ps.setInt(3, id);
            ps.executeUpdate();
            
            // Envoyer SMS de confirmation
            if (r != null && r.getTelephone() != null && !r.getTelephone().isEmpty()) {
                SmsService.notifyRdvConfirmation(r.getTelephone(), 
                    r.getDate() != null ? r.getDate().toString() : "", 
                    r.getHeureDebut() != null ? r.getHeureDebut().toString() : "");
            }
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