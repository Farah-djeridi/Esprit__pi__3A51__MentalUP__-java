package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ControllerHomeAdmin {

    @FXML private HBox navAccueil, navSuivi, navForum, navRdv, navUtilisateurs;
    @FXML private HBox navSuiviStats, navObjectifs;
    @FXML private VBox submenuSuivi;
    @FXML private Label arrowSuivi;

    @FXML private Label statUsers, statRdvs, statTopics, statMood;
    @FXML private Label labelDate, labelUserName, avatarInitials;
    @FXML private Button notifButton, logoutButton;
    @FXML private ImageView logoImage;

    @FXML private VBox dashboardContent;
    @FXML private StackPane contentArea;

    private boolean suiviOpen = false;

    @FXML
    public void initialize() {
        labelDate.setText(
                new SimpleDateFormat("EEEE d MMMM yyyy", Locale.FRENCH).format(new Date())
        );

        submenuSuivi.setVisible(false);
        submenuSuivi.setManaged(false);
        arrowSuivi.setText("▶");

        try {
            Image image = new Image(getClass().getResourceAsStream("/Images/logo.png"));
            logoImage.setImage(image);
        } catch (Exception e) {
            System.out.println("Logo introuvable dans /Images/logo.png");
        }

        showDashboard();
        setActiveNav(navAccueil);
    }

    @FXML
    void toggleSuiviMenu(MouseEvent event) {
        suiviOpen = !suiviOpen;
        submenuSuivi.setVisible(suiviOpen);
        submenuSuivi.setManaged(suiviOpen);
        arrowSuivi.setText(suiviOpen ? "▼" : "▶");
    }

    @FXML
    void onNavHomeClicked(MouseEvent event) {
        showDashboard();
        setActiveNav(navAccueil);
    }

    @FXML
    void onNavSuiviStatsClicked(MouseEvent event) {
        loadPage("/AdminSuiviMental.fxml");
        setActiveNav(navSuivi);
    }

    @FXML
    void onNavObjectifsClicked(MouseEvent event) {
        loadPage("/AdminObjectif.fxml");
        setActiveNav(navSuivi);
    }

    @FXML
    void onNavRdvClicked(MouseEvent event) {
        setActiveNav(navRdv);
        showPageError("Page Rendez-vous non encore branchée.");
    }

    @FXML
    void onNavUtilisateursClicked(MouseEvent event) {
        setActiveNav(navUtilisateurs);
        showPageError("Page Utilisateurs non encore branchée.");
    }

    @FXML
    private void onNavHoverEnter(MouseEvent event) {
        HBox source = (HBox) event.getSource();
        if (!source.equals(navAccueil) && !source.getStyle().contains("#34495E")) {
            source.setStyle("-fx-background-color: rgba(52,73,94,0.5); -fx-background-radius: 8; -fx-padding: 10 14; -fx-cursor: hand;");
        }
    }

    @FXML
    private void onNavHoverExit(MouseEvent event) {
        HBox source = (HBox) event.getSource();
        if (!source.equals(navAccueil) && !source.equals(navSuivi) && !source.equals(navRdv) && !source.equals(navUtilisateurs)) {
            source.setStyle("-fx-background-color: transparent; -fx-background-radius: 8; -fx-padding: 10 14; -fx-cursor: hand;");
        }
    }

    @FXML
    private void onSubmenuHoverEnter(MouseEvent event) {
        HBox source = (HBox) event.getSource();
        source.setStyle("-fx-background-color: #34495E; -fx-background-radius: 6; -fx-padding: 8 12; -fx-cursor: hand;");
        for (Node node : source.getChildren()) {
            if (node instanceof Label label) {
                label.setStyle("-fx-text-fill: white;");
            }
        }
    }

    @FXML
    private void onSubmenuHoverExit(MouseEvent event) {
        HBox source = (HBox) event.getSource();
        source.setStyle("-fx-background-radius: 6; -fx-padding: 8 12; -fx-cursor: hand;");
        for (Node node : source.getChildren()) {
            if (node instanceof Label label) {
                label.setStyle("-fx-text-fill: rgba(255,255,255,0.6);");
            }
        }
    }

    @FXML
    private void onNotifications() {
        System.out.println("Notifications");
    }

    @FXML
    private void onLogout() {
        System.out.println("Déconnexion");
    }

    @FXML
    private void onAddUser() {
        System.out.println("Ajouter un utilisateur");
    }

    @FXML
    private void onCreateRdv() {
        System.out.println("Créer un rendez-vous");
    }

    @FXML
    private void onModerateForum() {
        System.out.println("Modérer le forum");
    }

    @FXML
    private void onExportData() {
        System.out.println("Exporter les données");
    }

    private void showDashboard() {
        dashboardContent.setVisible(true);
        dashboardContent.setManaged(true);

        contentArea.getChildren().clear();
        contentArea.setVisible(false);
        contentArea.setManaged(false);
    }

    private void loadPage(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node page = loader.load();

            dashboardContent.setVisible(false);
            dashboardContent.setManaged(false);

            contentArea.getChildren().setAll(page);
            contentArea.setVisible(true);
            contentArea.setManaged(true);

        } catch (IOException e) {
            e.printStackTrace();
            showPageError("Erreur chargement page : " + fxmlPath + "\n" + e.getMessage());
        }
    }

    private void showPageError(String message) {
        contentArea.getChildren().clear();

        Label label = new Label(message);
        label.setWrapText(true);
        label.setStyle("-fx-text-fill: #DC2626; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 20;");

        contentArea.getChildren().add(label);
        contentArea.setVisible(true);
        contentArea.setManaged(true);

        dashboardContent.setVisible(false);
        dashboardContent.setManaged(false);
    }

    private void setActiveNav(HBox activeNav) {
        HBox[] allNavs = {navAccueil, navSuivi, navForum, navRdv, navUtilisateurs};

        for (HBox nav : allNavs) {
            if (nav != null) {
                nav.setStyle("-fx-background-color: transparent; -fx-background-radius: 8; -fx-padding: 10 14; -fx-cursor: hand;");
            }
        }

        if (activeNav != null) {
            activeNav.setStyle("-fx-background-color: #34495E; -fx-background-radius: 8; -fx-padding: 10 14; -fx-cursor: hand;");
        }
    }
}