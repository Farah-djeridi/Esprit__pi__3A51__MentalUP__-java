package Controllors;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import models.Commentaire;
import models.Sujet;
import services.ServiceCommentaire;
import services.ServiceSujet;

public class ControllerAdminCommentaire {

    @FXML private Label labelDate;
    @FXML private Label labelUserName;
    @FXML private Label avatarInitials;
    @FXML private Button logoutButton;
    @FXML private Button notifButton;
    @FXML private ImageView logoImage;

    @FXML private HBox navAccueil, navSuivi, navForum, navRdv, navUtilisateurs, navDossiers, navContenus, navActivites;
    @FXML private HBox navSuiviStats, navObjectifs, navSujets, navCommentaires;
    @FXML private VBox submenuSuivi, submenuForum;
    @FXML private Label arrowSuivi, arrowForum;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterCombo;
    @FXML private Label totalCommentairesLabel;

    @FXML private VBox cardsContainer;

    private ServiceCommentaire serviceCommentaire;
    private ServiceSujet serviceSujet;
    private List<Commentaire> allCommentaires;
    private List<Commentaire> filteredCommentaires;

    private boolean suiviOpen = false;
    private boolean forumOpen = true;

    private static final String COLOR_PRIMARY = "#2C5F8A";
    private static final String COLOR_DANGER = "#EF4444";
    private static final String COLOR_SUCCESS = "#22C55E";
    private static final String COLOR_BG = "#F0F4FA";

    @FXML
    public void initialize() {
        updateDate();

        labelUserName.setText("Admin MentalUp");
        avatarInitials.setText("AD");

        try {
            Image logo = new Image(getClass().getResourceAsStream("/images/logo.png"));
            if (logo != null) {
                logoImage.setImage(logo);
            }
        } catch (Exception e) {
            System.err.println("Logo non trouvé, utilisation du texte par défaut");
        }

        serviceCommentaire = new ServiceCommentaire();
        serviceSujet = new ServiceSujet();
        allCommentaires = new ArrayList<>();
        filteredCommentaires = new ArrayList<>();

        setupFilters();
        loadCommentaires();

        submenuSuivi.setVisible(false);
        submenuSuivi.setManaged(false);
        submenuForum.setVisible(true);
        submenuForum.setManaged(true);
        arrowSuivi.setText("▶");
        arrowForum.setText("▼");
    }

    private void updateDate() {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRENCH);
        String formattedDate = today.format(formatter);
        formattedDate = formattedDate.substring(0, 1).toUpperCase() + formattedDate.substring(1);
        labelDate.setText(formattedDate);
    }

    private void setupFilters() {
        // Initialisation du ComboBox
        filterCombo.getItems().clear();
        filterCombo.getItems().addAll("Tous", "Toxiques", "Non toxiques");
        filterCombo.setValue("Tous");

        // Listeners pour le filtrage
        searchField.textProperty().addListener((obs, old, newVal) -> filterAndDisplay());
        filterCombo.valueProperty().addListener((obs, old, newVal) -> filterAndDisplay());
    }

    private void loadCommentaires() {
        try {
            allCommentaires = serviceCommentaire.getAll();
            totalCommentairesLabel.setText(String.valueOf(allCommentaires.size()));
            filterAndDisplay();
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de charger les commentaires: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void filterAndDisplay() {
        String searchText = searchField.getText().toLowerCase();
        String filter = filterCombo.getValue();

        filteredCommentaires = new ArrayList<>(allCommentaires);

        // Filtre par recherche
        if (searchText != null && !searchText.isEmpty()) {
            filteredCommentaires.removeIf(c ->
                    !c.getContenu().toLowerCase().contains(searchText) &&
                            (c.getUserName() == null || !c.getUserName().toLowerCase().contains(searchText))
            );
        }

        // Filtre par toxicité
        if ("Toxiques".equals(filter)) {
            filteredCommentaires.removeIf(c -> !c.isEstToxique());
        } else if ("Non toxiques".equals(filter)) {
            filteredCommentaires.removeIf(c -> c.isEstToxique());
        }

        // Affichage
        displayAllCommentaires();
    }

    private void displayAllCommentaires() {
        cardsContainer.getChildren().clear();

        for (int i = 0; i < filteredCommentaires.size(); i++) {
            Commentaire commentaire = filteredCommentaires.get(i);
            cardsContainer.getChildren().add(createCommentaireRow(commentaire, i % 2 == 0));
        }
    }

    /**
     * Crée une barre de progression stylisée pour le score de toxicité
     */
    private VBox createToxicityProgressBar(double score) {
        VBox container = new VBox(4);
        container.setAlignment(Pos.CENTER_LEFT);
        container.setPrefWidth(100);

        int percentage = (int) Math.round(score * 100);

        String barColor;
        String textColor;

        if (score < 0.3) {
            barColor = "#22C55E";
            textColor = "#22C55E";
        } else if (score < 0.7) {
            barColor = "#F59E0B";
            textColor = "#F59E0B";
        } else {
            barColor = "#EF4444";
            textColor = "#EF4444";
        }

        Label percentLabel = new Label(String.format("%d%%", percentage));
        percentLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + textColor + ";");

        StackPane progressBar = new StackPane();
        progressBar.setPrefWidth(90);
        progressBar.setPrefHeight(6);

        Rectangle background = new Rectangle(90, 6);
        background.setFill(Color.web("#E2E8F0"));
        background.setArcWidth(3);
        background.setArcHeight(3);

        Rectangle progress = new Rectangle(90 * score, 6);
        progress.setFill(Color.web(barColor));
        progress.setArcWidth(3);
        progress.setArcHeight(3);

        progressBar.getChildren().addAll(background, progress);
        container.getChildren().addAll(percentLabel, progressBar);

        return container;
    }

    private HBox createCommentaireRow(Commentaire commentaire, boolean isEven) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: " + (isEven ? "white" : "#F8FAFE") +
                "; -fx-padding: 12 15; -fx-border-color: #E8EEF4; -fx-border-width: 0 0 1 0;");

        String contenuText = commentaire.getContenu();
        if (contenuText.length() > 55) {
            contenuText = contenuText.substring(0, 55) + "...";
        }
        Label contenuLabel = new Label(contenuText);
        contenuLabel.setPrefWidth(250);
        contenuLabel.setStyle("-fx-text-fill: #2C3E50; -fx-font-size: 12px;");
        contenuLabel.setWrapText(true);

        Label auteurLabel = new Label(commentaire.isAnonyme() ? "Anonyme" : commentaire.getUserName());
        auteurLabel.setPrefWidth(120);
        auteurLabel.setStyle("-fx-text-fill: #6B7C8D; -fx-font-size: 12px;");

        String sujetTitre = "Chargement...";
        try {
            Sujet sujet = serviceSujet.getById(commentaire.getSujetId());
            if (sujet != null) {
                sujetTitre = sujet.getTitre();
                if (sujetTitre.length() > 20) {
                    sujetTitre = sujetTitre.substring(0, 20) + "...";
                }
            } else {
                sujetTitre = "Sujet inconnu";
            }
        } catch (Exception e) {
            sujetTitre = "Sujet inconnu";
        }
        Label sujetLabel = new Label(sujetTitre);
        sujetLabel.setPrefWidth(150);
        sujetLabel.setStyle("-fx-text-fill: #6B7C8D; -fx-font-size: 12px;");

        Label dateLabel = new Label(formatDate(commentaire.getDateCommentaire()));
        dateLabel.setPrefWidth(90);
        dateLabel.setStyle("-fx-text-fill: #6B7C8D; -fx-font-size: 12px;");

        Label likesLabel = new Label("👍 " + commentaire.getNbLikes());
        likesLabel.setPrefWidth(50);
        likesLabel.setStyle("-fx-text-fill: " + COLOR_SUCCESS + "; -fx-font-weight: bold; -fx-font-size: 12px;");
        likesLabel.setAlignment(Pos.CENTER);

        Label dislikesLabel = new Label("👎 " + commentaire.getNbDislikes());
        dislikesLabel.setPrefWidth(50);
        dislikesLabel.setStyle("-fx-text-fill: " + COLOR_DANGER + "; -fx-font-weight: bold; -fx-font-size: 12px;");
        dislikesLabel.setAlignment(Pos.CENTER);

        // Barre de progression toxicité
        VBox toxicityContainer = createToxicityProgressBar(commentaire.getScoreToxicite());
        toxicityContainer.setPrefWidth(110);
        toxicityContainer.setAlignment(Pos.CENTER_LEFT);

        HBox actionsBox = new HBox(8);
        actionsBox.setPrefWidth(120);
        actionsBox.setAlignment(Pos.CENTER);

        // Bouton conditionnel basé sur isEstToxique()
        if (commentaire.isEstToxique()) {
            // Bouton Bannir pour les commentaires toxiques
            Button banBtn = new Button("🚫 Bannir");
            banBtn.setStyle("-fx-background-color: #DC2626; -fx-text-fill: white; -fx-cursor: hand; " +
                    "-fx-background-radius: 5; -fx-padding: 5 12; -fx-font-size: 10px; -fx-font-weight: bold;");
            banBtn.setMinWidth(80);
            banBtn.setOnAction(e -> banCommentaire(commentaire));
            banBtn.setOnMouseEntered(ev -> banBtn.setStyle("-fx-background-color: #B91C1C; -fx-text-fill: white; -fx-cursor: hand; " +
                    "-fx-background-radius: 5; -fx-padding: 5 12; -fx-font-size: 10px; -fx-font-weight: bold;"));
            banBtn.setOnMouseExited(ev -> banBtn.setStyle("-fx-background-color: #DC2626; -fx-text-fill: white; -fx-cursor: hand; " +
                    "-fx-background-radius: 5; -fx-padding: 5 12; -fx-font-size: 10px; -fx-font-weight: bold;"));
            actionsBox.getChildren().add(banBtn);
        } else {
            // Bouton Supprimer pour les commentaires non toxiques
            Button deleteBtn = new Button("🗑 Supprimer");
            deleteBtn.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-cursor: hand; " +
                    "-fx-background-radius: 5; -fx-padding: 5 12; -fx-font-size: 10px; -fx-font-weight: bold;");
            deleteBtn.setMinWidth(80);
            deleteBtn.setOnAction(e -> deleteCommentaire(commentaire));
            deleteBtn.setOnMouseEntered(ev -> deleteBtn.setStyle("-fx-background-color: #C0392B; -fx-text-fill: white; -fx-cursor: hand; " +
                    "-fx-background-radius: 5; -fx-padding: 5 12; -fx-font-size: 10px; -fx-font-weight: bold;"));
            deleteBtn.setOnMouseExited(ev -> deleteBtn.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-cursor: hand; " +
                    "-fx-background-radius: 5; -fx-padding: 5 12; -fx-font-size: 10px; -fx-font-weight: bold;"));
            actionsBox.getChildren().add(deleteBtn);
        }

        row.getChildren().addAll(contenuLabel, auteurLabel, sujetLabel, dateLabel,
                likesLabel, dislikesLabel, toxicityContainer, actionsBox);
        return row;
    }

    /**
     * Méthode pour bannir un commentaire toxique
     */
    private void banCommentaire(Commentaire commentaire) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Bannir le commentaire");
        confirm.setHeaderText("⚠️ Commentaire toxique détecté");
        confirm.setContentText("Score de toxicité: " + String.format("%.0f", commentaire.getScoreToxicite() * 100) + "%\n\n" +
                "Êtes-vous sûr de vouloir bannir ce commentaire ?\n" +
                "Le commentaire sera supprimé et l'utilisateur recevra un avertissement.");

        DialogPane dialogPane = confirm.getDialogPane();
        dialogPane.setStyle("-fx-background-color: " + COLOR_BG + "; -fx-background-radius: 12;");

        Button banButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        banButton.setText("Bannir");
        banButton.setStyle("-fx-background-color: #DC2626; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 8; -fx-padding: 8 24; -fx-font-weight: bold;");

        Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
        cancelButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #6B7C8D; -fx-cursor: hand; -fx-padding: 8 24; -fx-font-weight: 600;");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                serviceCommentaire.delete(commentaire);
                loadCommentaires();
                showAlert("Succès", "✓ Commentaire banni avec succès !\nUn avertissement a été envoyé à l'utilisateur.", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("Erreur", "Impossible de bannir le commentaire: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void deleteCommentaire(Commentaire commentaire) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Supprimer le commentaire");
        confirm.setHeaderText(null);
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer ce commentaire ?");

        DialogPane dialogPane = confirm.getDialogPane();
        dialogPane.setStyle("-fx-background-color: " + COLOR_BG + "; -fx-background-radius: 12;");

        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        okButton.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 8; -fx-padding: 8 24; -fx-font-weight: bold;");

        Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
        cancelButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #6B7C8D; -fx-cursor: hand; -fx-padding: 8 24; -fx-font-weight: 600;");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            serviceCommentaire.delete(commentaire);
            loadCommentaires();
            showAlert("Succès", "Commentaire supprimé !", Alert.AlertType.INFORMATION);
        }
    }

    private String formatDate(Date date) {
        if (date == null) return "Date inconnue";
        LocalDate localDate = date.toLocalDate();
        LocalDate today = LocalDate.now();
        if (localDate.equals(today)) return "Aujourd'hui";
        if (localDate.equals(today.minusDays(1))) return "Hier";
        return localDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    @FXML private void onFilter() { filterAndDisplay(); }

    @FXML private void onNavHomeClicked() { navigateTo("/HomeAdmin.fxml"); }
    @FXML private void onNavSuiviStatsClicked() { navigateTo("/StatistiquesAdmin.fxml"); }
    @FXML private void onNavObjectifsClicked() { navigateTo("/ObjectifsAdmin.fxml"); }
    @FXML private void onNavSujetsClicked() { navigateTo("/AdminSujet.fxml"); }
    @FXML private void onNavCommentairesClicked() { navigateTo("/AdminCommentaire.fxml"); }
    @FXML private void onNavRdvClicked() { navigateTo("/RendezVousAdmin.fxml"); }
    @FXML private void onNavUtilisateursClicked() { navigateTo("/UtilisateursAdmin.fxml"); }
    @FXML private void onNavDossiersClicked() { navigateTo("/DossiersMedicaux.fxml"); }
    @FXML private void onNavContenusClicked() { navigateTo("/ContenusAdmin.fxml"); }
    @FXML private void onNavActivitesClicked() { navigateTo("/ActivitesAdmin.fxml"); }

    @FXML private void toggleSuiviMenu() {
        suiviOpen = !suiviOpen;
        submenuSuivi.setVisible(suiviOpen);
        submenuSuivi.setManaged(suiviOpen);
        arrowSuivi.setText(suiviOpen ? "▼" : "▶");
    }

    @FXML private void toggleForumMenu() {
        forumOpen = !forumOpen;
        submenuForum.setVisible(forumOpen);
        submenuForum.setManaged(forumOpen);
        arrowForum.setText(forumOpen ? "▼" : "▶");
    }

    @FXML private void onNotifications() { showAlert("Notifications", "Fonctionnalité à venir", Alert.AlertType.INFORMATION); }

    @FXML private void onLogout() {
        try {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Déconnexion");
            confirm.setHeaderText(null);
            confirm.setContentText("Voulez-vous vraiment vous déconnecter ?");

            DialogPane dialogPane = confirm.getDialogPane();
            dialogPane.setStyle("-fx-background-color: " + COLOR_BG + "; -fx-background-radius: 12;");

            Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
            okButton.setStyle("-fx-background-color: " + COLOR_PRIMARY + "; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 8; -fx-padding: 8 24;");

            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                navigateTo("/views/Login.fxml");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void navigateTo(String fxmlPath) {
        try {
            Stage stage = (Stage) navAccueil.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de naviguer", Alert.AlertType.ERROR);
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