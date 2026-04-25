package Controllors;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.event.ActionEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class ControllerHomeAdmin {

    @FXML private HBox navAccueil, navSuivi, navForum, navRdv, navActivites, navContenus, navDossiers, navUtilisateurs;
    @FXML private HBox navSuiviStats, navObjectifs, navSujets, navCommentaires;
    @FXML private VBox submenuSuivi, submenuForum;
    @FXML private Label arrowSuivi, arrowForum;

    @FXML private Label statUsers, statRdvs, statTopics, statMood;
    @FXML private Label labelDate, labelUserName, avatarInitials;
    @FXML private Button notifButton, logoutButton;

    @FXML private ImageView logoImage;

    private boolean suiviOpen = false;
    private boolean forumOpen = false;

    @FXML
    public void initialize() {
        labelDate.setText(new java.text.SimpleDateFormat("EEEE d MMMM yyyy").format(new java.util.Date()));

        submenuSuivi.setVisible(false);
        submenuSuivi.setManaged(false);
        submenuForum.setVisible(false);
        submenuForum.setManaged(false);

        arrowSuivi.setText("▶");
        arrowForum.setText("▶");

        logoImage.setImage(new Image(getClass().getResourceAsStream("/Images/logo.png")));
    }

    @FXML
    void toggleSuiviMenu(MouseEvent event) {
        suiviOpen = !suiviOpen;
        submenuSuivi.setVisible(suiviOpen);
        submenuSuivi.setManaged(suiviOpen);
        arrowSuivi.setText(suiviOpen ? "▼" : "▶");
    }

    @FXML
    void toggleForumMenu(MouseEvent event) {
        forumOpen = !forumOpen;
        submenuForum.setVisible(forumOpen);
        submenuForum.setManaged(forumOpen);
        arrowForum.setText(forumOpen ? "▼" : "▶");
    }

    @FXML
    private void onNavHomeClicked(MouseEvent event) {
        setActiveNav(navAccueil);
        System.out.println("Accueil Admin");
    }

    @FXML
    private void onNavSuiviStatsClicked(MouseEvent event) {
        System.out.println("Statistiques de suivi");
    }

    @FXML
    private void onNavObjectifsClicked(MouseEvent event) {
        System.out.println("Objectifs des utilisateurs");
    }

    @FXML
    private void onNavSujetsClicked(MouseEvent event) {
        navigateTo("/AdminSujet.fxml");
    }

    @FXML
    private void onNavCommentairesClicked(MouseEvent event) {
        navigateTo("/AdminCommentaire.fxml");
    }
    @FXML private void onNavBansClicked() { navigateTo("/AdminBan.fxml"); }

    @FXML
    private void onNavRdvClicked(MouseEvent event) {
        setActiveNav(navRdv);
        System.out.println("Rendez-vous");
    }

    @FXML
    private void onNavUtilisateursClicked(MouseEvent event) {
        setActiveNav(navUtilisateurs);
        System.out.println("Utilisateurs");
    }

    @FXML
    private void onNavDossiersClicked(MouseEvent event) {
        setActiveNav(navDossiers);
        System.out.println("Dossiers médicaux");
    }

    @FXML
    private void onNavContenusClicked(MouseEvent event) {
        setActiveNav(navContenus);
        System.out.println("Contenus");
    }

    @FXML
    private void onNavActivitesClicked(MouseEvent event) {
        setActiveNav(navActivites);
        System.out.println("Activités");
    }

    @FXML
    private void onNavHoverEnter(MouseEvent event) {
        HBox source = (HBox) event.getSource();
        if (!source.getStyle().contains("#34495E")) {
            source.setStyle("-fx-background-color: rgba(52,73,94,0.5); -fx-background-radius: 8; -fx-padding: 10 14; -fx-cursor: hand;");
        }
    }

    @FXML
    private void onNavHoverExit(MouseEvent event) {
        HBox source = (HBox) event.getSource();
        if (!source.getStyle().contains("#34495E")) {
            source.setStyle("-fx-background-color: transparent; -fx-background-radius: 8; -fx-padding: 10 14; -fx-cursor: hand;");
        }
    }

    @FXML
    private void onSubmenuHoverEnter(MouseEvent event) {
        HBox source = (HBox) event.getSource();
        source.setStyle("-fx-background-color: #34495E; -fx-background-radius: 6; -fx-padding: 8 12; -fx-cursor: hand;");
        for (javafx.scene.Node node : source.getChildren()) {
            if (node instanceof Label) node.setStyle("-fx-text-fill: white;");
        }
    }

    @FXML
    private void onSubmenuHoverExit(MouseEvent event) {
        HBox source = (HBox) event.getSource();
        source.setStyle("-fx-background-radius: 6; -fx-padding: 8 12; -fx-cursor: hand;");
        for (javafx.scene.Node node : source.getChildren()) {
            if (node instanceof Label) node.setStyle("-fx-text-fill: rgba(255,255,255,0.6);");
        }
    }

    @FXML
    private void onNotifications(ActionEvent event) {
        showAlert("Notifications", "Fonctionnalité à venir", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void onLogout(ActionEvent event) {
        System.out.println("Déconnexion");
    }

    @FXML
    private void onAddUser(ActionEvent event) {
        System.out.println("Ajouter un utilisateur");
    }

    @FXML
    private void onCreateRdv(ActionEvent event) {
        System.out.println("Créer un rendez-vous");
    }

    @FXML
    private void onModerateForum(ActionEvent event) {
        navigateTo("/AdminSujets.fxml");
    }

    @FXML
    private void onExportData(ActionEvent event) {
        System.out.println("Exporter les données");
    }

    private void setActiveNav(HBox activeNav) {
        HBox[] allNavs = {navAccueil, navSuivi, navForum, navRdv, navActivites, navContenus, navDossiers, navUtilisateurs};
        for (HBox nav : allNavs) {
            if (nav != null) {
                nav.setStyle("-fx-background-color: transparent; -fx-background-radius: 8; -fx-padding: 10 14; -fx-cursor: hand;");
            }
        }
        if (activeNav != null) {
            activeNav.setStyle("-fx-background-color: #34495E; -fx-background-radius: 8; -fx-padding: 10 14; -fx-cursor: hand;");
        }
    }

    private void navigateTo(String fxmlPath) {
        try {
            Stage stage = (Stage) navAccueil.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la page: " + fxmlPath, Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}