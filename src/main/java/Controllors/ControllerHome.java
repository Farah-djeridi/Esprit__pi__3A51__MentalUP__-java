package Controllors;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ControllerHome {

    // Éléments FXML pour les animations
    @FXML private VBox card1, card2, card3;
    @FXML private VBox adv1, adv2, adv3, adv4;
    @FXML private HBox bannerBox;
    @FXML private Button startButton, notifButton, logoutButton;
    @FXML private HBox navAccueil, navSuivi, navRdv, navForum, navActivites, navRessources;
    @FXML private Label badgeRdv, labelUserName, labelDate, avatarInitials;
    @FXML private ImageView logoImage;

    private void loadPage(String fxml, MouseEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Animation de transition entre les pages
            Scene scene = new Scene(root);
            stage.setScene(scene);

            // Animation fade-in pour la nouvelle page
            FadeTransition ft = new FadeTransition(Duration.millis(300), root);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();

            stage.show();
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
    }

    // 🔹 Navigation
    @FXML
    void onNavHomeClicked(MouseEvent event) {
        loadPage("/Home.fxml", event);
    }

    @FXML
    private void onNavSuiviClicked(MouseEvent event) {
        System.out.println("Suivi Mental");
    }

    @FXML
    private void onNavRdvClicked(MouseEvent event) {
        System.out.println("Rendez-vous");
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