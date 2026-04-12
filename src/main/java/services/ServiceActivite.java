package services;

import models.Activite;
import utils.MyDataBase;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ServiceActivite {
    private Connection connection;

    public ServiceActivite() {
        this.connection = MyDataBase.getInstance().getConnection();
    }

    public void ajouterActivite(Activite activite) throws SQLException {
        String query = "INSERT INTO activite (titre, description, type, adresse, date_debut, date_fin, latitude, longitude) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, activite.getTitre());
            stmt.setString(2, activite.getDescription());
            stmt.setString(3, activite.getType());
            stmt.setString(4, activite.getAdresse());
            stmt.setDate(5, Date.valueOf(activite.getDateDebut()));
            stmt.setDate(6, Date.valueOf(activite.getDateFin()));
            stmt.setDouble(7, activite.getLatitude());
            stmt.setDouble(8, activite.getLongitude());

            stmt.executeUpdate();
            System.out.println("Activité ajoutée avec succès!");
        }
    }

    public void modifierActivite(Activite activite) throws SQLException {
        String query = "UPDATE activite SET titre=?, description=?, type=?, adresse=?, date_debut=?, date_fin=?, latitude=?, longitude=? WHERE id_activite=?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, activite.getTitre());
            stmt.setString(2, activite.getDescription());
            stmt.setString(3, activite.getType());
            stmt.setString(4, activite.getAdresse());
            stmt.setDate(5, Date.valueOf(activite.getDateDebut()));
            stmt.setDate(6, Date.valueOf(activite.getDateFin()));
            stmt.setDouble(7, activite.getLatitude());
            stmt.setDouble(8, activite.getLongitude());
            stmt.setInt(9, activite.getIdActivite());

            stmt.executeUpdate();
            System.out.println("Activité modifiée avec succès!");
        }
    }

    public void supprimerActivite(int idActivite) throws SQLException {
        // Supprimer d'abord les réservations liées
        String deleteRes = "DELETE FROM reservation WHERE id_activite = ?";
        PreparedStatement psRes = connection.prepareStatement(deleteRes);
        psRes.setInt(1, idActivite);
        psRes.executeUpdate();

        // Ensuite supprimer l'activité
        String query = "DELETE FROM activite WHERE id_activite=?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, idActivite);
            stmt.executeUpdate();
        }
    }

    public List<Activite> getAllActivites() throws SQLException {
        List<Activite> activites = new ArrayList<>();
        String query = "SELECT * FROM activite";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Activite activite = new Activite();
                activite.setIdActivite(rs.getInt("id_activite"));
                activite.setTitre(rs.getString("titre"));
                activite.setDescription(rs.getString("description"));
                activite.setType(rs.getString("type"));
                activite.setAdresse(rs.getString("adresse"));

                Date dateDebut = rs.getDate("date_debut");
                if (dateDebut != null) {
                    activite.setDateDebut(dateDebut.toLocalDate());
                }

                Date dateFin = rs.getDate("date_fin");
                if (dateFin != null) {
                    activite.setDateFin(dateFin.toLocalDate());
                }

                activite.setLatitude(rs.getDouble("latitude"));
                activite.setLongitude(rs.getDouble("longitude"));

                activites.add(activite);
            }
        }

        return activites;
    }

    public Activite getActiviteById(int idActivite) throws SQLException {
        String query = "SELECT * FROM activite WHERE id_activite=?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, idActivite);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Activite activite = new Activite();
                    activite.setIdActivite(rs.getInt("id_activite"));
                    activite.setTitre(rs.getString("titre"));
                    activite.setDescription(rs.getString("description"));
                    activite.setType(rs.getString("type"));
                    activite.setAdresse(rs.getString("adresse"));

                    Date dateDebut = rs.getDate("date_debut");
                    if (dateDebut != null) {
                        activite.setDateDebut(dateDebut.toLocalDate());
                    }

                    Date dateFin = rs.getDate("date_fin");
                    if (dateFin != null) {
                        activite.setDateFin(dateFin.toLocalDate());
                    }

                    activite.setLatitude(rs.getDouble("latitude"));
                    activite.setLongitude(rs.getDouble("longitude"));

                    return activite;
                }
            }
        }

        return null;
    }
}
