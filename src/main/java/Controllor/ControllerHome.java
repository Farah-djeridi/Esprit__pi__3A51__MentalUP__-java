package Controllor;

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
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import models.User;
import utils.SceneManager;
import utils.SessionManager;
import Services.ServiceRating;
import utils.MyDataBase;
import Models.RendezVous;
import services.ServiceRendezVous;
import java.sql.*;
import java.util.List;

public class ControllerHome {

    @FXML private VBox card1, card2, card3;
    @FXML private VBox adv1, adv2, adv3, adv4;
    @FXML private HBox bannerBox;
    @FXML private Button startButton, notifButton, menuButton, logoutButton;
    @FXML private HBox navAccueil, navSuivi, navRdv, navForum, navActivites, navRessources;
    @FXML private Label badgeRdv, labelUserName, labelDate, avatarInitials, labelWelcome, labelUserRole;
    @FXML private HBox psyListContainer;
    @FXML private ImageView logoImage;
    @FXML private VBox rdvHomeContainer;

    private ContextMenu contextMenu;
    private final ServiceRating serviceRating = new ServiceRating();
    private final ServiceRendezVous serviceRdv = new ServiceRendezVous();
    private int etudiantId;

    @FXML
    public void initialize() {
        // 1. Remplir les infos de l'utilisateur connecté
        User user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            etudiantId = user.getId();
            labelUserName.setText(user.getPrenom() + " " + user.getNom());
            if (labelWelcome != null) labelWelcome.setText("Bonjour, " + user.getPrenom() + " 👋");
            if (labelUserRole != null) labelUserRole.setText(user.getRole() != null ? user.getRole() : "Étudiant");
            
            String initials = "";
            if (user.getPrenom() != null && !user.getPrenom().isEmpty())
                initials += user.getPrenom().charAt(0);
            if (user.getNom() != null && !user.getNom().isEmpty())
                initials += user.getNom().charAt(0);
            avatarInitials.setText(initials.toUpperCase());
        }

        // 2. Date du jour
        labelDate.setText(java.time.LocalDate.now()
                .format(java.time.format.DateTimeFormatter
                        .ofPattern("EEEE d MMMM yyyy", java.util.Locale.FRENCH)));

        // 3. Logo
        try {
            logoImage.setImage(new Image(getClass().getResourceAsStream("/Images/logo.png")));
        } catch (Exception ignored) {}

        // 4. Créer le menu contextuel (3 points)
        setupContextMenu();

        // 5. Charger les données dynamiques
        chargerPsychologues();
        chargerRendezVous();

        // 6. Animations d'apparition
        animateFade(bannerBox, 500);
        animateFade(card1, 400);
        animateFade(card2, 500);
        animateFade(card3, 600);
    }

    private void setupContextMenu() {
        contextMenu = new ContextMenu();
        contextMenu.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 3);");

        MenuItem modifierProfil = new MenuItem("✏️  Modifier mon profil");
        modifierProfil.setStyle("-fx-font-size: 13px; -fx-padding: 8 16;");
        modifierProfil.setOnAction(e -> SceneManager.switchTo("Profile.fxml", "Mon Profil"));

        MenuItem deconnexion = new MenuItem("🚪  Déconnexion");
        deconnexion.setStyle("-fx-font-size: 13px; -fx-padding: 8 16; -fx-text-fill: #e74c3c;");
        deconnexion.setOnAction(e -> {
            SessionManager.getInstance().logout();
            SceneManager.goToLogin();
        });

        contextMenu.getItems().addAll(modifierProfil, deconnexion);
    }

    @FXML
    public void onMenuClicked(ActionEvent event) {
        Button btn = (Button) event.getSource();
        contextMenu.show(btn, javafx.geometry.Side.BOTTOM, 0, 5);
    }

    private void loadPage(String fxml, Event event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage;
            if (event != null && event.getSource() instanceof Node) {
                stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            } else {
                stage = (Stage) bannerBox.getScene().getWindow();
            }
            Scene scene = new Scene(root);
            stage.setScene(scene);
            FadeTransition ft = new FadeTransition(Duration.millis(300), root);
            ft.setFromValue(0); ft.setToValue(1); ft.play();
            stage.show();
            stage.centerOnScreen();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // Navigation
    @FXML void onNavHomeClicked(MouseEvent event)      { loadPage("/Home.fxml", event); }
    @FXML void onNavSuiviClicked(MouseEvent event)     { System.out.println("Suivi"); }
    @FXML void onNavRdvClicked(MouseEvent event)       { loadPage("/RendezVous_Etudiant.fxml", event); }
    @FXML void onNavBlogClicked(MouseEvent event)      { loadPage("/forum.fxml", event); }
    @FXML void onNavActivitesClicked(MouseEvent event) { System.out.println("Activités"); }
    @FXML void onNavRessourcesClicked(MouseEvent event){ loadPage("/StudentRessources.fxml", event); }
    @FXML void onNavRdvClicked_Btn(ActionEvent event)  { loadPage("/RendezVous_Etudiant.fxml", event); }

    // Hover sidebar
    @FXML
    private void onNavHoverEnter(MouseEvent event) {
        HBox src = (HBox) event.getSource();
        scale(src, 1.02, 150);
        src.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-background-radius: 8; -fx-padding: 10 14; -fx-cursor: hand;");
    }

    @FXML
    private void onNavHoverExit(MouseEvent event) {
        HBox src = (HBox) event.getSource();
        scale(src, 1.0, 150);
        src.setStyle("-fx-background-color: transparent; -fx-background-radius: 8; -fx-padding: 10 14; -fx-cursor: hand;");
    }

    // Hover cartes
    @FXML
    private void onCardHoverEnter(MouseEvent event) {
        VBox card = (VBox) event.getSource();
        scale(card, 1.02, 200);
        card.setStyle("-fx-background-color: #F8FBFF; -fx-background-radius: 16; -fx-padding: 22; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 15, 0, 0, 4);");
    }

    @FXML
    private void onCardHoverExit(MouseEvent event) {
        VBox card = (VBox) event.getSource();
        scale(card, 1.0, 200);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-padding: 22; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 2);");
    }

    // Hover avantages
    @FXML
    private void onAdvantageHoverEnter(MouseEvent event) {
        VBox adv = (VBox) event.getSource();
        scale(adv, 1.05, 150);
        adv.setStyle("-fx-padding: 10; -fx-background-radius: 12; -fx-background-color: rgba(151,187,228,0.15);");
    }

    @FXML
    private void onAdvantageHoverExit(MouseEvent event) {
        VBox adv = (VBox) event.getSource();
        scale(adv, 1.0, 150);
        adv.setStyle("-fx-padding: 10; -fx-background-radius: 12; -fx-background-color: transparent;");
    }

    // Boutons actions
    @FXML
    private void onNotifications(ActionEvent event) {
        Button btn = (Button) event.getSource();
        scale(btn, 1.1, 150);
        System.out.println("Notifications");
    }

    @FXML
    private void onLogout(ActionEvent event) {
        SessionManager.getInstance().logout();
        SceneManager.goToLogin();
    }

    @FXML
    private void onStartSuivi(ActionEvent event) {
        Button btn = (Button) event.getSource();
        scale(btn, 1.05, 150);
        System.out.println("Start suivi");
    }

    // Données dynamiques
    private void chargerPsychologues() {
        if (psyListContainer == null) return;
        psyListContainer.getChildren().clear();
        Connection conn = MyDataBase.getInstance().getCnx();
        if (conn == null) return;
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT id, prenom, nom FROM user WHERE role = 'psychologue' LIMIT 3")) {
            while (rs.next()) {
                String nomComplet = "Dr. " + rs.getString("prenom") + " " + rs.getString("nom");
                double avg = serviceRating.getAverageForPsy(rs.getInt("id"));
                psyListContainer.getChildren().add(createMiniPsyCard(nomComplet, avg));
            }
        } catch (SQLException e) { e.printStackTrace(); }
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
        List<RendezVous> aujourdhui = serviceRdv.getRdvAujourdhui(etudiantId);
        List<RendezVous> avenir = serviceRdv.getRdvAvenir(etudiantId);
        if (aujourdhui.isEmpty() && avenir.isEmpty()) {
            Label noRdv = new Label("Vous n'avez aucun rendez-vous de prévu.");
            noRdv.setStyle("-fx-text-fill: #64748B; -fx-font-size: 13px; -fx-font-style: italic;");
            rdvHomeContainer.getChildren().add(noRdv);
            return;
        }
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
        if ("confirmé".equalsIgnoreCase(r.getStatut())) badgeStyle += "-fx-background-color: #2ECC71;";
        else if ("en attente".equalsIgnoreCase(r.getStatut()) || "réservé".equalsIgnoreCase(r.getStatut())) badgeStyle += "-fx-background-color: #F1C40F;";
        else badgeStyle += "-fx-background-color: #94A3B8;";
        statusBadge.setStyle(badgeStyle);
        row.getChildren().addAll(icon, info, spacer, statusBadge);
        return row;
    }

    // Helpers
    private void animateFade(Node node, int ms) {
        if (node == null) return;
        FadeTransition ft = new FadeTransition(Duration.millis(ms), node);
        ft.setFromValue(0); ft.setToValue(1); ft.play();
    }

    private void scale(Node node, double factor, int ms) {
        if (node == null) return;
        ScaleTransition st = new ScaleTransition(Duration.millis(ms), node);
        st.setToX(factor); st.setToY(factor);
        if (factor > 1.0) { st.setAutoReverse(true); st.setCycleCount(2); }
        st.play();
    }
}