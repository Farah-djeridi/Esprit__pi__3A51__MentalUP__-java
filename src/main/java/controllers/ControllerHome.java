package controllers;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import Services.ServiceRating;
import utils.MyDataBase;
import Models.RendezVous;
import Services.ServiceRendezVous;
import javafx.scene.control.Hyperlink;
import java.sql.*;
import java.util.List;

public class ControllerHome {


    @FXML private VBox card1, card2, card3;
    @FXML private VBox adv1, adv2, adv3, adv4;
    @FXML private HBox bannerBox;
    @FXML private Button startButton, notifButton, logoutButton;
    @FXML private HBox navAccueil, navSuivi, navRdv, navForum, navActivites, navRessources;
    @FXML private Label badgeRdv, labelUserName, labelDate, avatarInitials;
    @FXML private HBox psyListContainer;
    @FXML private ImageView logoImage;
    @FXML private VBox rdvHomeContainer;
    private final ServiceRating serviceRating = new ServiceRating();
    private final ServiceRendezVous serviceRdv = new ServiceRendezVous();
    private final int etudiantId = 2; // ID de l'étudiant connecté (simulé)

    private void loadPage(String fxml, Event event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage;
            if (event != null && event.getSource() instanceof Node) {
                stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            } else {
                stage = (Stage) bannerBox.getScene().getWindow();
            }

            // Animation de transition entre les pages
            Scene scene = new Scene(root);
            stage.setScene(scene);

            // Animation fade-in pour la nouvelle page
            FadeTransition ft = new FadeTransition(Duration.millis(300), root);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();

            stage.show();
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Animation au chargement de la page
    public void initialize() {

        // Animation d'apparition pour la bannière
        FadeTransition ftBanner = new FadeTransition(Duration.millis(500), bannerBox);
        ftBanner.setFromValue(0);
        ftBanner.setToValue(1);
        ftBanner.play();

        // Animation d'apparition pour les cartes (décalées)
        if (card1 != null) {
            FadeTransition ftCard1 = new FadeTransition(Duration.millis(400), card1);
            ftCard1.setFromValue(0);
            ftCard1.setToValue(1);
            ftCard1.play();
        }

        if (card2 != null) {
            FadeTransition ftCard2 = new FadeTransition(Duration.millis(500), card2);
            ftCard2.setFromValue(0);
            ftCard2.setToValue(1);
            ftCard2.play();
        }

        if (card3 != null) {
            FadeTransition ftCard3 = new FadeTransition(Duration.millis(600), card3);
            ftCard3.setFromValue(0);
            ftCard3.setToValue(1);
            ftCard3.play();
        }
        logoImage.setImage(new Image(getClass().getResourceAsStream("/Images/logo.png")));
        chargerPsychologues();
        chargerRendezVous();
    }

    private void chargerPsychologues() {
        if (psyListContainer == null) return;
        psyListContainer.getChildren().clear();
        
        Connection conn = MyDataBase.getInstance().getCnx();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT id, prenom, nom FROM user WHERE role = 'psychologue' LIMIT 3")) {
            
            while (rs.next()) {
                int id = rs.getInt("id");
                String nomComplet = "Dr. " + rs.getString("prenom") + " " + rs.getString("nom");
                double avg = serviceRating.getAverageForPsy(id);
                
                VBox card = createMiniPsyCard(nomComplet, avg);
                psyListContainer.getChildren().add(card);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private VBox createMiniPsyCard(String nom, double avg) {
        VBox card = new VBox(8);
        card.setAlignment(javafx.geometry.Pos.CENTER);
        card.setStyle("-fx-background-color: #F8FBFF; -fx-background-radius: 12; -fx-padding: 15; -fx-border-color: #E2E8F0; -fx-border-radius: 12;");
        card.setPrefWidth(160);

        Label lblNom = new Label(nom);
        lblNom.setStyle("-fx-font-weight: bold; -fx-text-fill: #2C3E50; -fx-font-size: 13px;");
        
        HBox stars = new HBox(2);
        stars.setAlignment(javafx.geometry.Pos.CENTER);
        int fullStars = (int) Math.round(avg);
        for (int i = 1; i <= 5; i++) {
            Label star = new Label(i <= fullStars ? "★" : "☆");
            star.setStyle("-fx-text-fill: #F1C40F; -fx-font-size: 14px;");
            stars.getChildren().add(star);
        }
        
        Label lblAvg = new Label(String.format("%.1f/5", avg));
        lblAvg.setStyle("-fx-text-fill: #64748B; -fx-font-size: 11px;");

        card.getChildren().addAll(lblNom, stars, lblAvg);
        return card;
    }

    private void chargerRendezVous() {
        if (rdvHomeContainer == null) return;
        rdvHomeContainer.getChildren().clear();

        // Récupérer les RDV aujourd'hui et à venir
        List<RendezVous> aujourdhui = serviceRdv.getRdvAujourdhui(etudiantId);
        List<RendezVous> avenir = serviceRdv.getRdvAvenir(etudiantId);

        if (aujourdhui.isEmpty() && avenir.isEmpty()) {
            Label noRdv = new Label("Vous n'avez aucun rendez-vous de prévu.");
            noRdv.setStyle("-fx-text-fill: #64748B; -fx-font-size: 13px; -fx-font-style: italic;");
            rdvHomeContainer.getChildren().add(noRdv);
            return;
        }

        // Afficher max 3 rendez-vous
        int count = 0;
        for (RendezVous r : aujourdhui) {
            if (count >= 3) break;
            rdvHomeContainer.getChildren().add(createRdvRow(r, "Aujourd'hui"));
            count++;
        }
        for (RendezVous r : avenir) {
            if (count >= 3) break;
            rdvHomeContainer.getChildren().add(createRdvRow(r, r.getDate().toString()));
            count++;
        }
    }

    private HBox createRdvRow(RendezVous r, String dateStr) {
        HBox row = new HBox(15);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #F8FBFF; -fx-background-radius: 10; -fx-padding: 12; -fx-border-color: #E2E8F0; -fx-border-radius: 10;");

        Label icon = new Label("📅");
        icon.setStyle("-fx-font-size: 18px;");

        VBox info = new VBox(2);
        Label lblDate = new Label(dateStr + " • " + r.getHeureDebut());
        lblDate.setStyle("-fx-text-fill: #2C5F8A; -fx-font-weight: bold; -fx-font-size: 12px;");
        
        Label lblType = new Label(r.getTypeRdv() + " (" + r.getLieu() + ")");
        lblType.setStyle("-fx-text-fill: #2C3E50; -fx-font-weight: bold; -fx-font-size: 13px;");
        
        info.getChildren().addAll(lblDate, lblType);

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Label statusBadge = new Label(r.getStatut().toUpperCase());
        String badgeStyle = "-fx-padding: 4 8; -fx-background-radius: 12; -fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: white;";
        if ("confirmé".equalsIgnoreCase(r.getStatut())) {
            badgeStyle += "-fx-background-color: #2ECC71;";
        } else if ("en attente".equalsIgnoreCase(r.getStatut()) || "réservé".equalsIgnoreCase(r.getStatut())) {
            badgeStyle += "-fx-background-color: #F1C40F;";
        } else {
            badgeStyle += "-fx-background-color: #94A3B8;";
        }
        statusBadge.setStyle(badgeStyle);

        row.getChildren().addAll(icon, info, spacer, statusBadge);
        return row;
    }

    @FXML
    void onNavRdvClicked_Btn(ActionEvent event) {
        loadPage("/gui/RendezVous_Etudiant.fxml", event);
    }

    // 🔹 Navigation
    @FXML
    void onNavHomeClicked(MouseEvent event) {
        loadPage("/gui/Home.fxml", event);
    }

    @FXML
    private void onNavSuiviClicked(MouseEvent event) {
        System.out.println("Suivi Mental");
    }

    @FXML
    private void onNavRdvClicked(MouseEvent event) {
        loadPage("/gui/RendezVous_Etudiant.fxml", event);
    }
    @FXML
    private void onNavBlogClicked(MouseEvent event) {
        loadPage("/forum.fxml", event);
    }

    @FXML
    private void onNavActivitesClicked(MouseEvent event) {
        System.out.println("Activités");
    }

    @FXML
    private void onNavRessourcesClicked(MouseEvent event) {
        System.out.println("Ressources");
    }

    // 🔹 Hover pour la sidebar
    @FXML
    private void onNavHoverEnter(MouseEvent event) {
        HBox source = (HBox) event.getSource();
        ScaleTransition st = new ScaleTransition(Duration.millis(150), source);
        st.setToX(1.02);
        st.setToY(1.02);
        st.play();
        source.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-background-radius: 8; -fx-padding: 10 14; -fx-cursor: hand;");
    }

    @FXML
    private void onNavHoverExit(MouseEvent event) {
        HBox source = (HBox) event.getSource();
        ScaleTransition st = new ScaleTransition(Duration.millis(150), source);
        st.setToX(1);
        st.setToY(1);
        st.play();
        source.setStyle("-fx-background-color: transparent; -fx-background-radius: 8; -fx-padding: 10 14; -fx-cursor: hand;");
    }

    // 🔹 Animations pour les cartes
    @FXML
    private void onCardHoverEnter(MouseEvent event) {
        VBox card = (VBox) event.getSource();
        ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
        st.setToX(1.02);
        st.setToY(1.02);
        st.play();
        card.setStyle("-fx-background-color: #F8FBFF; -fx-background-radius: 16; -fx-padding: 22; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 15, 0, 0, 4);");
    }

    @FXML
    private void onCardHoverExit(MouseEvent event) {
        VBox card = (VBox) event.getSource();
        ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
        st.setToX(1);
        st.setToY(1);
        st.play();
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-padding: 22; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 2);");
    }

    // 🔹 Animations pour les avantages
    @FXML
    private void onAdvantageHoverEnter(MouseEvent event) {
        VBox adv = (VBox) event.getSource();
        ScaleTransition st = new ScaleTransition(Duration.millis(150), adv);
        st.setToX(1.05);
        st.setToY(1.05);
        st.play();
        adv.setStyle("-fx-padding: 10; -fx-background-radius: 12; -fx-background-color: rgba(151,187,228,0.15);");
    }

    @FXML
    private void onAdvantageHoverExit(MouseEvent event) {
        VBox adv = (VBox) event.getSource();
        ScaleTransition st = new ScaleTransition(Duration.millis(150), adv);
        st.setToX(1);
        st.setToY(1);
        st.play();
        adv.setStyle("-fx-padding: 10; -fx-background-radius: 12; -fx-background-color: transparent;");
    }

    // 🔹 Top actions
    @FXML
    private void onNotifications(ActionEvent event) {
        // Animation du bouton notification
        Button btn = (Button) event.getSource();
        ScaleTransition st = new ScaleTransition(Duration.millis(150), btn);
        st.setToX(1.1);
        st.setToY(1.1);
        st.setAutoReverse(true);
        st.setCycleCount(2);
        st.play();
        System.out.println("Notifications");
    }

    @FXML
    private void onLogout(ActionEvent event) {
        // Animation du bouton logout
        Button btn = (Button) event.getSource();
        ScaleTransition st = new ScaleTransition(Duration.millis(150), btn);
        st.setToX(0.95);
        st.setToY(0.95);
        st.setAutoReverse(true);
        st.setCycleCount(2);
        st.play();
        System.out.println("Logout");
    }

    @FXML
    private void onStartSuivi(ActionEvent event) {
        // Animation du bouton start
        Button btn = (Button) event.getSource();
        ScaleTransition st = new ScaleTransition(Duration.millis(150), btn);
        st.setToX(1.05);
        st.setToY(1.05);
        st.setAutoReverse(true);
        st.setCycleCount(2);
        st.play();
        System.out.println("Start suivi");
    }
}