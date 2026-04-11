package Services;

import interfaces.IService;
import Models.RendezVous;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public abstract class ServiceRendezVous implements IService<RendezVous> {

    private Connection cnx;

    public ServiceRendezVous() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    // ✅ CREATE
    @Override
    public void add(RendezVous r) {

        String req = "INSERT INTO rendez_vous " +
                "(date, heure_debut, heure_fin, type_rdv, statut, lien_meet, lieu, telephone, psychologue_id, etudiant_id) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?)";

        try {
            PreparedStatement ps = cnx.prepareStatement(req);

            ps.setDate(1, r.getDate());
            ps.setTime(2, r.getHeureDebut());
            ps.setTime(3, r.getHeureFin());
            ps.setString(4, r.getTypeRdv());
            ps.setString(5, r.getStatut());
            ps.setString(6, r.getLienMeet());
            ps.setString(7, r.getLieu());
            ps.setString(8, r.getTelephone());
            ps.setInt(9, r.getPsychologueId());

            if (r.getEtudiantId() != null)
                ps.setInt(10, r.getEtudiantId());
            else
                ps.setNull(10, Types.INTEGER);

            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // ✅ READ
    @Override
    public List<RendezVous> getAll() {

        List<RendezVous> list = new ArrayList<>();
        String req = "SELECT * FROM rendez_vous";

        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(req);

            while (rs.next()) {
                RendezVous r = new RendezVous();

                r.setId(rs.getInt("id"));
                r.setDate(rs.getDate("date"));
                r.setHeureDebut(rs.getTime("heure_debut"));
                r.setHeureFin(rs.getTime("heure_fin"));
                r.setTypeRdv(rs.getString("type_rdv"));
                r.setStatut(rs.getString("statut"));
                r.setLienMeet(rs.getString("lien_meet"));
                r.setLieu(rs.getString("lieu"));
                r.setTelephone(rs.getString("telephone"));
                r.setPsychologueId(rs.getInt("psychologue_id"));
                r.setEtudiantId((Integer) rs.getObject("etudiant_id"));

                list.add(r);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return list;
    }

    // ✅ UPDATE
    @Override
    public void update(RendezVous r) {

        String req = "UPDATE rendez_vous SET " +
                "date=?, heure_debut=?, heure_fin=?, type_rdv=?, statut=?, lien_meet=?, lieu=?, telephone=?, psychologue_id=?, etudiant_id=? " +
                "WHERE id=?";

        try {
            PreparedStatement ps = cnx.prepareStatement(req);

            ps.setDate(1, r.getDate());
            ps.setTime(2, r.getHeureDebut());
            ps.setTime(3, r.getHeureFin());
            ps.setString(4, r.getTypeRdv());
            ps.setString(5, r.getStatut());
            ps.setString(6, r.getLienMeet());
            ps.setString(7, r.getLieu());
            ps.setString(8, r.getTelephone());
            ps.setInt(9, r.getPsychologueId());

            if (r.getEtudiantId() != null)
                ps.setInt(10, r.getEtudiantId());
            else
                ps.setNull(10, Types.INTEGER);

            ps.setInt(11, r.getId());

            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // ✅ DELETE
    @Override
    public void delete(RendezVous r) {

        String req = "DELETE FROM rendez_vous WHERE id=?";

        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, r.getId());
            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}