package services;

import utils.MyDataBase;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class ServiceNotation {

    private final Connection connection;

    public ServiceNotation() {
        this.connection = MyDataBase.getInstance().getConnection();
        creerTableSiNecessaire();
    }

    private void creerTableSiNecessaire() {
        try (Statement st = connection.createStatement()) {
            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS notation (" +
                "id_notation INT AUTO_INCREMENT PRIMARY KEY, " +
                "id_activite INT NOT NULL, " +
                "nom_etudiant VARCHAR(100) NOT NULL, " +
                "note INT NOT NULL CHECK (note BETWEEN 1 AND 5), " +
                "commentaire TEXT, " +
                "date_notation TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "UNIQUE KEY unique_note (id_activite, nom_etudiant))");
        } catch (SQLException e) {
            System.err.println("Erreur création table notation: " + e.getMessage());
        }
    }

    /** Ajouter ou mettre à jour une note */
    public void noterActivite(int idActivite, String nomEtudiant, int note, String commentaire) throws SQLException {
        String sql = "INSERT INTO notation (id_activite, nom_etudiant, note, commentaire) " +
                     "VALUES (?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE note=VALUES(note), commentaire=VALUES(commentaire)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idActivite);
            ps.setString(2, nomEtudiant);
            ps.setInt(3, note);
            ps.setString(4, commentaire);
            ps.executeUpdate();
        }
    }

    /** Moyenne des notes d'une activité */
    public double getMoyenne(int idActivite) throws SQLException {
        String sql = "SELECT AVG(note) FROM notation WHERE id_activite = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idActivite);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        }
        return 0.0;
    }

    /** Nombre de notes d'une activité */
    public int getNombreNotes(int idActivite) throws SQLException {
        String sql = "SELECT COUNT(*) FROM notation WHERE id_activite = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idActivite);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    /** Note d'un étudiant pour une activité (0 si pas encore noté) */
    public int getNoteEtudiant(int idActivite, String nomEtudiant) throws SQLException {
        String sql = "SELECT note FROM notation WHERE id_activite = ? AND nom_etudiant = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idActivite);
            ps.setString(2, nomEtudiant);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    /** Top activités par note moyenne (pour recommandations) */
    public Map<Integer, Double> getTopActivites(int limit) throws SQLException {
        Map<Integer, Double> result = new LinkedHashMap<>();
        String sql = "SELECT id_activite, AVG(note) as moy FROM notation " +
                     "GROUP BY id_activite ORDER BY moy DESC LIMIT ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) result.put(rs.getInt("id_activite"), rs.getDouble("moy"));
        }
        return result;
    }

    /** Distribution des notes (1→5) pour une activité */
    public int[] getDistribution(int idActivite) throws SQLException {
        int[] dist = new int[5];
        String sql = "SELECT note, COUNT(*) as cnt FROM notation WHERE id_activite = ? GROUP BY note";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idActivite);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) dist[rs.getInt("note") - 1] = rs.getInt("cnt");
        }
        return dist;
    }
}
