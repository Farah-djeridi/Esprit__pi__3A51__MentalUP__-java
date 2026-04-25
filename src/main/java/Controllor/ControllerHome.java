package Controllor;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import models.User;
import utils.SceneManager;
import utils.SessionManager;

public class ControllerHome {

    @FXML private VBox card1, card2, card3;
    @FXML private VBox adv1, adv2, adv3, adv4;
    @FXML private HBox bannerBox;
    @FXML private Button startButton, notifButton, menuButton;
    @FXML private HBox navAccueil, navSuivi, navRdv, navForum, navActivites, navRessources;
    @FXML private Label badgeRdv, labelUserName, labelDate, avatarInitials;
    @FXML private ImageView logoImage;

    // Menu contextuel (3 points)
    private ContextMenu contextMenu;

    @FXML
    public void initialize() {
        // Remplir les infos de l'utilisateur connecté
        User user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            labelUserName.setText(user.getPrenom() + " " + user.getNom());
            String initials = "";
            if (user.getPrenom() != null && !user.getPrenom().isEmpty())
                initials += user.getPrenom().charAt(0);
            if (user.getNom() != null && !user.getNom().isEmpty())
                initials += user.getNom().charAt(0);
            avatarInitials.setText(initials.toUpperCase());
        }

        // Date du jour
        labelDate.setText(java.time.LocalDate.now()
                .format(java.time.format.DateTimeFormatter
                        .ofPattern("EEEE d MMMM yyyy", java.util.Locale.FRENCH)));

        // Logo
        try {
            logoImage.setImage(new Image(getClass().getResourceAsStream("/Images/logo.png")));
        } catch (Exception ignored) {}

        // ✅ Créer le menu contextuel des 3 points
        contextMenu = new ContextMenu();
        contextMenu.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 3);");

        MenuItem modifierProfil = new MenuItem("✏️  Modifier mon profil");
        modifierProfil.setStyle("-fx-font-size: 13px; -fx-padding: 8 16;");
        modifierProfil.setOnAction(e -> SceneManager.switchTo("Profile.fxml", "Mon Profil"));

        MenuItem separator = new MenuItem();
        separator.setDisable(true);

        MenuItem deconnexion = new MenuItem("🚪  Déconnexion");
        deconnexion.setStyle("-fx-font-size: 13px; -fx-padding: 8 16; -fx-text-fill: #e74c3c;");
        deconnexion.setOnAction(e -> {
            SessionManager.getInstance().logout();
            SceneManager.goToLogin();
        });

        contextMenu.getItems().addAll(modifierProfil, deconnexion);

        // Animations d'apparition
        animateFade(bannerBox, 500);
        animateFade(card1, 400);
        animateFade(card2, 500);
        animateFade(card3, 600);
    }

    // ✅ Ouvre le menu 3 points
    @FXML
    public void onMenuClicked(ActionEvent event) {
        Button btn = (Button) event.getSource();
        contextMenu.show(btn, javafx.geometry.Side.BOTTOM, 0, 5);
    }

    // Navigation
    private void loadPage(String fxml, MouseEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            FadeTransition ft = new FadeTransition(Duration.millis(300), root);
            ft.setFromValue(0); ft.setToValue(1); ft.play();
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML void onNavHomeClicked(MouseEvent event)      { loadPage("/Home.fxml", event); }
    @FXML void onNavSuiviClicked(MouseEvent event)     { System.out.println("Suivi"); }
    @FXML void onNavRdvClicked(MouseEvent event)       { System.out.println("RDV"); }
    @FXML void onNavBlogClicked(MouseEvent event)      { loadPage("/forum.fxml", event); }
    @FXML void onNavActivitesClicked(MouseEvent event) { System.out.println("Activités"); }
    @FXML void onNavRessourcesClicked(MouseEvent event){ System.out.println("Ressources"); }

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

    // Boutons top
    @FXML
    private void onNotifications(ActionEvent event) {
        Button btn = (Button) event.getSource();
        ScaleTransition st = new ScaleTransition(Duration.millis(150), btn);
        st.setToX(1.1); st.setToY(1.1);
        st.setAutoReverse(true); st.setCycleCount(2); st.play();
    }

    @FXML
    private void onStartSuivi(ActionEvent event) {
        Button btn = (Button) event.getSource();
        ScaleTransition st = new ScaleTransition(Duration.millis(150), btn);
        st.setToX(1.05); st.setToY(1.05);
        st.setAutoReverse(true); st.setCycleCount(2); st.play();
    }

    // Helpers
    private void animateFade(Node node, int ms) {
        if (node == null) return;
        FadeTransition ft = new FadeTransition(Duration.millis(ms), node);
        ft.setFromValue(0); ft.setToValue(1); ft.play();
    }

    private void scale(Node node, double factor, int ms) {
        ScaleTransition st = new ScaleTransition(Duration.millis(ms), node);
        st.setToX(factor); st.setToY(factor); st.play();
    }
}