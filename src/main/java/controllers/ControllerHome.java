package controllers;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class ControllerHome {

    @FXML private VBox card1, card2, card3;
    @FXML private VBox adv1, adv2, adv3, adv4;
    @FXML private HBox bannerBox;
    @FXML private Button startButton, notifButton, logoutButton;
    @FXML private HBox navAccueil, navSuivi, navObjectifs, navRdv, navForum, navActivites, navRessources;
    @FXML private Label badgeRdv, labelUserName, labelDate, avatarInitials;
    @FXML private ImageView logoImage;

    @FXML private StackPane contentArea;
    @FXML private VBox homeContent;

    @FXML
    public void initialize() {
        if (bannerBox != null) {
            FadeTransition ftBanner = new FadeTransition(Duration.millis(500), bannerBox);
            ftBanner.setFromValue(0);
            ftBanner.setToValue(1);
            ftBanner.play();
        }

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

        try {
            if (logoImage != null) {
                logoImage.setImage(new Image(getClass().getResourceAsStream("/Images/logo.png")));
            }
        } catch (Exception e) {
            System.out.println("Logo introuvable : /Images/logo.jpg");
        }

        setActiveNav(navAccueil);
    }

    private void loadContent(String fxmlPath) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentArea.getChildren().setAll(view);

            FadeTransition ft = new FadeTransition(Duration.millis(250), view);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showHomeContent() {
        contentArea.getChildren().setAll(homeContent);

        FadeTransition ft = new FadeTransition(Duration.millis(250), homeContent);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    private void setActiveNav(HBox activeItem) {
        resetNavStyle(navAccueil);
        resetNavStyle(navSuivi);
        resetNavStyle(navObjectifs);
        resetNavStyle(navRdv);
        resetNavStyle(navForum);
        resetNavStyle(navActivites);
        resetNavStyle(navRessources);

        if (activeItem != null) {
            activeItem.setStyle(
                    "-fx-background-color: #1E4568;" +
                            "-fx-background-radius: 8;" +
                            "-fx-padding: 10 14;" +
                            "-fx-cursor: hand;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);"
            );
        }
    }

    private void resetNavStyle(HBox item) {
        if (item != null) {
            item.setStyle(
                    "-fx-background-color: transparent;" +
                            "-fx-background-radius: 8;" +
                            "-fx-padding: 10 14;" +
                            "-fx-cursor: hand;"
            );
        }
    }

    @FXML
    void onNavHomeClicked(MouseEvent event) {
        showHomeContent();
        setActiveNav(navAccueil);
    }

    @FXML
    private void onNavSuiviClicked(MouseEvent event) {
        loadContent("/suivi_mentale.fxml");
        setActiveNav(navSuivi);
    }

    @FXML
    private void onNavObjectifsClicked(MouseEvent event) {
        loadContent("/objvue.fxml");
        setActiveNav(navObjectifs);
    }

    @FXML
    private void onNavRdvClicked(MouseEvent event) {
        System.out.println("Rendez-vous");
        setActiveNav(navRdv);
    }

    @FXML
    private void onNavBlogClicked(MouseEvent event) {
        loadContent("/forum.fxml");
        setActiveNav(navForum);
    }

    @FXML
    private void onNavActivitesClicked(MouseEvent event) {
        System.out.println("Activités");
        setActiveNav(navActivites);
    }

    @FXML
    private void onNavRessourcesClicked(MouseEvent event) {
        System.out.println("Ressources");
        setActiveNav(navRessources);
    }

    @FXML
    private void onCardSuiviClicked(MouseEvent event) {
        loadContent("/suivi_mentale.fxml");
        setActiveNav(navSuivi);
    }

    @FXML
    private void onNavHoverEnter(MouseEvent event) {
        HBox source = (HBox) event.getSource();

        ScaleTransition st = new ScaleTransition(Duration.millis(150), source);
        st.setToX(1.02);
        st.setToY(1.02);
        st.play();

        if (!source.getStyle().contains("#1E4568")) {
            source.setStyle(
                    "-fx-background-color: rgba(255,255,255,0.15);" +
                            "-fx-background-radius: 8;" +
                            "-fx-padding: 10 14;" +
                            "-fx-cursor: hand;"
            );
        }
    }

    @FXML
    private void onNavHoverExit(MouseEvent event) {
        HBox source = (HBox) event.getSource();

        ScaleTransition st = new ScaleTransition(Duration.millis(150), source);
        st.setToX(1);
        st.setToY(1);
        st.play();

        if (!source.getStyle().contains("#1E4568")) {
            resetNavStyle(source);
        }
    }

    @FXML
    private void onCardHoverEnter(MouseEvent event) {
        VBox card = (VBox) event.getSource();
        ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
        st.setToX(1.02);
        st.setToY(1.02);
        st.play();
        card.setStyle(
                "-fx-background-color: #F8FBFF;" +
                        "-fx-background-radius: 16;" +
                        "-fx-padding: 22;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 15, 0, 0, 4);" +
                        "-fx-cursor: hand;"
        );
    }

    @FXML
    private void onCardHoverExit(MouseEvent event) {
        VBox card = (VBox) event.getSource();
        ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
        st.setToX(1);
        st.setToY(1);
        st.play();
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 16;" +
                        "-fx-padding: 22;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 2);" +
                        "-fx-cursor: hand;"
        );
    }

    @FXML
    private void onAdvantageHoverEnter(MouseEvent event) {
        VBox adv = (VBox) event.getSource();
        ScaleTransition st = new ScaleTransition(Duration.millis(150), adv);
        st.setToX(1.05);
        st.setToY(1.05);
        st.play();
        adv.setStyle(
                "-fx-padding: 10;" +
                        "-fx-background-radius: 12;" +
                        "-fx-background-color: rgba(151,187,228,0.15);"
        );
    }

    @FXML
    private void onAdvantageHoverExit(MouseEvent event) {
        VBox adv = (VBox) event.getSource();
        ScaleTransition st = new ScaleTransition(Duration.millis(150), adv);
        st.setToX(1);
        st.setToY(1);
        st.play();
        adv.setStyle(
                "-fx-padding: 10;" +
                        "-fx-background-radius: 12;" +
                        "-fx-background-color: transparent;"
        );
    }

    @FXML
    private void onNotifications(ActionEvent event) {
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
        Button btn = (Button) event.getSource();
        ScaleTransition st = new ScaleTransition(Duration.millis(150), btn);
        st.setToX(1.05);
        st.setToY(1.05);
        st.setAutoReverse(true);
        st.setCycleCount(2);
        st.play();

        loadContent("/suivi_mentale.fxml");
        setActiveNav(navSuivi);
    }
}