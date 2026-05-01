package services;

import models.Reservation;
import utils.MyDataBase;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ServiceReservation {

    private Connection connection;

    public ServiceReservation() {
        connection = MyDataBase.getInstance().getConnection();
        creerTableSiNecessaire();
    }

    private void creerTableSiNecessaire() {
        try (Statement st = connection.createStatement()) {
            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS reservation (" +
                "id_reservation INT AUTO_INCREMENT PRIMARY KEY, " +
                "id_activite INT NOT NULL, " +
                "nom_etudiant VARCHAR(100) NOT NULL, " +
                "place VARCHAR(10) NOT NULL, " +
                "date_reservation DATE NOT NULL, " +
                "statut VARCHAR(20) DEFAULT 'EN_ATTENTE', " +
                "montant DECIMAL(10,2) DEFAULT 0.00, " +
                "statut_paiement VARCHAR(30) DEFAULT 'EN_ATTENTE_PAIEMENT', " +
                "methode_paiement VARCHAR(50) DEFAULT NULL)");
            // Ajouter les colonnes si elles n'existent pas encore
            for (String col : new String[]{
                "ALTER TABLE reservation ADD COLUMN statut VARCHAR(20) DEFAULT 'EN_ATTENTE'",
                "ALTER TABLE reservation ADD COLUMN montant DECIMAL(10,2) DEFAULT 0.00",
                "ALTER TABLE reservation ADD COLUMN statut_paiement VARCHAR(30) DEFAULT 'EN_ATTENTE_PAIEMENT'",
                "ALTER TABLE reservation ADD COLUMN methode_paiement VARCHAR(50) DEFAULT NULL"
            }) {
                try { st.executeUpdate(col); } catch (SQLException ignored) {}
            }
        } catch (SQLException e) {
            System.err.println("Erreur création table reservation: " + e.getMessage());
        }
    }

    public void mettreAJourStatut(int idReservation, String statut) throws SQLException {
        String sql = "UPDATE reservation SET statut = ? WHERE id_reservation = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, statut);
        ps.setInt(2, idReservation);
        ps.executeUpdate();
    }

    public void ajouterReservation(Reservation r) throws SQLException {
        String sql = "INSERT INTO reservation (id_activite, nom_etudiant, place, date_reservation, montant, statut_paiement, methode_paiement) VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, r.getIdActivite());
        ps.setString(2, r.getNomEtudiant());
        ps.setString(3, r.getPlace());
        ps.setDate(4, Date.valueOf(r.getDateReservation()));
        ps.setDouble(5, r.getMontant());
        ps.setString(6, r.getStatutPaiement());
        ps.setString(7, r.getMethodePaiement());
        ps.executeUpdate();
    }

    public void supprimerReservation(int idReservation) throws SQLException {
        String sql = "DELETE FROM reservation WHERE id_reservation = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, idReservation);
        ps.executeUpdate();
    }

    public void modifierReservation(Reservation r) throws SQLException {
        String sql = "UPDATE reservation SET nom_etudiant = ?, place = ?, date_reservation = ? WHERE id_reservation = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, r.getNomEtudiant());
        ps.setString(2, r.getPlace());
        ps.setDate(3, Date.valueOf(r.getDateReservation()));
        ps.setInt(4, r.getIdReservation());
        ps.executeUpdate();
    }

    public List<Reservation> getAllReservations() throws SQLException {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT r.*, a.titre as titre_activite FROM reservation r " +
                     "LEFT JOIN activite a ON r.id_activite = a.id_activite " +
                     "ORDER BY r.id_reservation DESC";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            Reservation r = new Reservation();
            r.setIdReservation(rs.getInt("id_reservation"));
            r.setIdActivite(rs.getInt("id_activite"));
            r.setNomEtudiant(rs.getString("nom_etudiant"));
            r.setPlace(rs.getString("place"));
            r.setDateReservation(rs.getDate("date_reservation").toLocalDate());
            r.setTitreActivite(rs.getString("titre_activite"));
            try { r.setStatut(rs.getString("statut")); } catch (Exception ignored) {}
            try { r.setMontant(rs.getDouble("montant")); } catch (Exception ignored) {}
            try { r.setStatutPaiement(rs.getString("statut_paiement")); } catch (Exception ignored) {}
            try { r.setMethodePaiement(rs.getString("methode_paiement")); } catch (Exception ignored) {}
            list.add(r);
        }
        return list;
    }

    public List<String> getPlacesReservees(int idActivite) throws SQLException {
        List<String> places = new ArrayList<>();
        String sql = "SELECT place FROM reservation WHERE id_activite = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, idActivite);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) places.add(rs.getString("place"));
        return places;
    }

    public int getNombreReservations(int idActivite) throws SQLException {
        String sql = "SELECT COUNT(*) FROM reservation WHERE id_activite = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, idActivite);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) return rs.getInt(1);
        return 0;
    }

    public Reservation getReservationEtudiant(int idActivite, String nomEtudiant) throws SQLException {
        String sql = "SELECT * FROM reservation WHERE id_activite = ? AND nom_etudiant = ? " +
                     "ORDER BY id_reservation DESC LIMIT 1";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, idActivite);
        ps.setString(2, nomEtudiant);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            Reservation r = new Reservation();
            r.setIdReservation(rs.getInt("id_reservation"));
            r.setIdActivite(rs.getInt("id_activite"));
            r.setNomEtudiant(rs.getString("nom_etudiant"));
            r.setPlace(rs.getString("place"));
            r.setDateReservation(rs.getDate("date_reservation").toLocalDate());
            return r;
        }
        return null;
    }
}
