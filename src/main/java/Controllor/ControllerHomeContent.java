package Controllor;

import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import models.RendezVous;
import models.User;
import services.ServiceRating;
import services.ServiceRendezVous;
import utils.MyDataBase;
import utils.SessionManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class ControllerHomeContent {

    @FXML private VBox rdvHomeContainer;
    @FXML private HBox psyListContainer;

    private final ServiceRating serviceRating = new ServiceRating();
    private final ServiceRendezVous serviceRdv = new ServiceRendezVous();

    @FXML
    public void initialize() {
        User user = SessionManager.getInstance().getCurrentUser();
        int etudiantId = user != null ? user.getId() : 0;
        chargerPsychologues();
        chargerRendezVous(etudiantId);
    }

    private void chargerPsychologues() {
        if (psyListContainer == null) return;
        psyListContainer.getChildren().clear();
        Connection conn = MyDataBase.getInstance().getCnx();
        if (conn == null) return;
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT id, prenom, nom FROM user WHERE role = 'psychologue' LIMIT 3")) {
            while (rs.next()) {
                String nom = "Dr. " + rs.getString("prenom") + " " + rs.getString("nom");
                double avg = 0.0;
                try { avg = serviceRating.getAverageForPsy(rs.getInt("id")); }
                catch (Exception ignored) {}
                psyListContainer.getChildren().add(createPsyCard(nom, avg));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private VBox createPsyCard(String nom, double avg) {
        VBox card = new VBox(6);
        card.setAlignment(javafx.geometry.Pos.CENTER);
        card.setStyle("-fx-background-color: #f8fbff; -fx-background-radius: 10; -fx-padding: 12; -fx-border-color: #e2e8f0; -fx-border-radius: 10;");
        card.setPrefWidth(140);
        Label lblNom = new Label(nom);
        lblNom.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-font-size: 12px; -fx-wrap-text: true;");
        lblNom.setMaxWidth(130);
        HBox stars = new HBox(2);
        stars.setAlignment(javafx.geometry.Pos.CENTER);
        int full = (int) Math.round(avg);
        for (int i = 1; i <= 5; i++) {
            Label s = new Label(i <= full ? "★" : "☆");
            s.setStyle("-fx-text-fill: #f1c40f; -fx-font-size: 13px;");
            stars.getChildren().add(s);
        }
        Label lblAvg = new Label(String.format("%.1f/5", avg));
        lblAvg.setStyle("-fx-text-fill: #64748b; -fx-font-size: 10px;");
        card.getChildren().addAll(lblNom, stars, lblAvg);
        return card;
    }

    private void chargerRendezVous(int etudiantId) {
        if (rdvHomeContainer == null) return;
        rdvHomeContainer.getChildren().clear();
        // Keep the title label
        Label title = new Label("📅  Prochains rendez-vous");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1a3a5c;");
        rdvHomeContainer.getChildren().add(title);

        List<RendezVous> aujourdhui = serviceRdv.getRdvAujourdhui(etudiantId);
        List<RendezVous> avenir     = serviceRdv.getRdvAvenir(etudiantId);

        if (aujourdhui.isEmpty() && avenir.isEmpty()) {
            Label noRdv = new Label("Vous n'avez aucun rendez-vous de prévu.");
            noRdv.setStyle("-fx-text-fill: #7a9cb8; -fx-font-size: 13px; -fx-font-style: italic;");
            rdvHomeContainer.getChildren().add(noRdv);
            return;
        }
        int count = 0;
        for (RendezVous r : aujourdhui) { if (count++ >= 3) break; rdvHomeContainer.getChildren().add(createRdvRow(r, "Aujourd'hui")); }
        for (RendezVous r : avenir)     { if (count++ >= 3) break; rdvHomeContainer.getChildren().add(createRdvRow(r, r.getDate().toString())); }
    }

    private HBox createRdvRow(RendezVous r, String dateStr) {
        HBox row = new HBox(12);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #f8fbff; -fx-background-radius: 8; -fx-padding: 10; -fx-border-color: #e2e8f0; -fx-border-radius: 8;");
        Label icon = new Label("📅"); icon.setStyle("-fx-font-size: 16px;");
        VBox info = new VBox(2);
        Label lblDate = new Label(dateStr + " • " + r.getHeureDebut());
        lblDate.setStyle("-fx-text-fill: #2c5f8a; -fx-font-weight: bold; -fx-font-size: 11px;");
        Label lblType = new Label(r.getTypeRdv() != null ? r.getTypeRdv() : "");
        lblType.setStyle("-fx-text-fill: #2c3e50; -fx-font-size: 12px;");
        info.getChildren().addAll(lblDate, lblType);
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        Label badge = new Label(r.getStatut() != null ? r.getStatut().toUpperCase() : "");
        badge.setStyle("-fx-padding: 3 8; -fx-background-radius: 10; -fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: white; -fx-background-color: #2ecc71;");
        row.getChildren().addAll(icon, info, spacer, badge);
        return row;
    }
}
