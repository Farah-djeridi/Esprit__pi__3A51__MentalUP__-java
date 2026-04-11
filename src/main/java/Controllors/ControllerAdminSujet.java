package Controllors;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.image.ImageView;

import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import models.Sujet;
import services.ServiceSujet;
import services.ServiceCommentaire;

public class ControllerAdminSujet {

    @FXML private Label labelDate;
    @FXML private Label labelUserName;
    @FXML private Label avatarInitials;
    @FXML private Button logoutButton;
    @FXML private Button notifButton;
    @FXML private ImageView logoImage;

    // Sidebar
    @FXML private HBox navAccueil, navSuivi, navForum, navRdv, navUtilisateurs, navDossiers, navContenus, navActivites;
    @FXML private HBox navSuiviStats, navObjectifs, navSujets, navCommentaires;
    @FXML private VBox submenuSuivi, submenuForum;
    @FXML private Label arrowSuivi, arrowForum;

    // Filtres
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterCombo;
    @FXML private Label totalSujetsLabel;

    // Container pour les lignes
    @FXML private VBox cardsContainer;

    private ServiceSujet serviceSujet;
    private ServiceCommentaire serviceCommentaire;
    private List<Sujet> allSujets;
    private List<Sujet> filteredSujets;

    private boolean suiviOpen = false;
    private boolean forumOpen = true;

    @FXML
    public void initialize() {
        updateDate();
        labelUserName.setText("Admin MentalUp");
        avatarInitials.setText("AD");

        serviceSujet = new ServiceSujet();
        serviceCommentaire = new ServiceCommentaire();
        allSujets = new ArrayList<>();
        filteredSujets = new ArrayList<>();

        setupFilters();
        loadSujets();

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
        filterCombo.getItems().addAll("Tous", "Plus populaires", "Plus récents");
        filterCombo.setValue("Tous");
        searchField.textProperty().addListener((obs, old, newVal) -> filterAndDisplay());
        filterCombo.valueProperty().addListener((obs, old, newVal) -> filterAndDisplay());
    }

    private void loadSujets() {
        try {
            allSujets = serviceSujet.getAll();
            totalSujetsLabel.setText(String.valueOf(allSujets.size()));
            filterAndDisplay();
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de charger les sujets: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void filterAndDisplay() {
        String searchText = searchField.getText().toLowerCase();
        String filter = filterCombo.getValue();

        filteredSujets = new ArrayList<>(allSujets);

        if (searchText != null && !searchText.isEmpty()) {
            filteredSujets.removeIf(s -> !s.getTitre().toLowerCase().contains(searchText) &&
                    !s.getContenu().toLowerCase().contains(searchText));
        }

        if ("Plus populaires".equals(filter)) {
            filteredSujets.sort((a, b) -> Integer.compare(b.getNbLikes(), a.getNbLikes()));
        } else if ("Plus récents".equals(filter)) {
            filteredSujets.sort((a, b) -> b.getDateCreation().compareTo(a.getDateCreation()));
        }

        displayAllSujets();
    }

    private void displayAllSujets() {
        cardsContainer.getChildren().clear();

        for (int i = 0; i < filteredSujets.size(); i++) {
            Sujet sujet = filteredSujets.get(i);
            cardsContainer.getChildren().add(createSujetRow(sujet, i % 2 == 0));
        }
    }

    private HBox createSujetRow(Sujet sujet, boolean isEven) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: " + (isEven ? "white" : "#F8FAFE") +
                "; -fx-padding: 12 15; -fx-border-color: #E8EEF4; -fx-border-width: 0 0 1 0;");

        // ID
        Label idLabel = new Label(String.valueOf(sujet.getId()));
        idLabel.setPrefWidth(50);
        idLabel.setStyle("-fx-text-fill: #2C3E50; -fx-font-weight: bold;");

        // Titre
        Label titreLabel = new Label(sujet.getTitre());
        titreLabel.setPrefWidth(200);
        titreLabel.setStyle("-fx-text-fill: #2C3E50; -fx-font-weight: 500;");
        titreLabel.setWrapText(true);

        // Auteur
        Label auteurLabel = new Label(sujet.isAnonyme() ? "Anonyme" : sujet.getUserName());
        auteurLabel.setPrefWidth(130);
        auteurLabel.setStyle("-fx-text-fill: #7F8C8D;");

        // Contenu
        String contenuText = sujet.getContenu();
        if (contenuText.length() > 55) {
            contenuText = contenuText.substring(0, 55) + "...";
        }
        Label contenuLabel = new Label(contenuText);
        contenuLabel.setPrefWidth(220);
        contenuLabel.setStyle("-fx-text-fill: #5A6C7D;");
        contenuLabel.setWrapText(true);

        // Date
        Label dateLabel = new Label(formatDate(sujet.getDateCreation()));
        dateLabel.setPrefWidth(100);
        dateLabel.setStyle("-fx-text-fill: #7F8C8D;");

        // Likes
        Label likesLabel = new Label(String.valueOf(sujet.getNbLikes()));
        likesLabel.setPrefWidth(50);
        likesLabel.setStyle("-fx-text-fill: #27AE60; -fx-font-weight: bold;");
        likesLabel.setAlignment(Pos.CENTER);

        // Dislikes
        Label dislikesLabel = new Label(String.valueOf(sujet.getNbDislikes()));
        dislikesLabel.setPrefWidth(50);
        dislikesLabel.setStyle("-fx-text-fill: #E74C3C; -fx-font-weight: bold;");
        dislikesLabel.setAlignment(Pos.CENTER);

        // Vues
        Label vuesLabel = new Label(String.valueOf(sujet.getNbVues()));
        vuesLabel.setPrefWidth(50);
        vuesLabel.setStyle("-fx-text-fill: #3498DB; -fx-font-weight: bold;");
        vuesLabel.setAlignment(Pos.CENTER);

        // Actions
        HBox actionsBox = new HBox(10);
        actionsBox.setAlignment(Pos.CENTER);
        actionsBox.setPrefWidth(180);

        Button editBtn = new Button("Modifier");
        editBtn.setStyle("-fx-background-color: #F39C12; -fx-text-fill: white; -fx-cursor: hand; " +
                "-fx-background-radius: 5; -fx-padding: 6 16; -fx-font-size: 11px; -fx-font-weight: bold;");
        editBtn.setMinWidth(80);
        editBtn.setOnAction(e -> editSujet(sujet));
        editBtn.setOnMouseEntered(e -> editBtn.setStyle("-fx-background-color: #E67E22; -fx-text-fill: white; -fx-cursor: hand; " +
                "-fx-background-radius: 5; -fx-padding: 6 16; -fx-font-size: 11px; -fx-font-weight: bold;"));
        editBtn.setOnMouseExited(e -> editBtn.setStyle("-fx-background-color: #F39C12; -fx-text-fill: white; -fx-cursor: hand; " +
                "-fx-background-radius: 5; -fx-padding: 6 16; -fx-font-size: 11px; -fx-font-weight: bold;"));

        Button deleteBtn = new Button("Supprimer");
        deleteBtn.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-cursor: hand; " +
                "-fx-background-radius: 5; -fx-padding: 6 16; -fx-font-size: 11px; -fx-font-weight: bold;");
        deleteBtn.setMinWidth(80);
        deleteBtn.setOnAction(e -> deleteSujet(sujet));
        deleteBtn.setOnMouseEntered(e -> deleteBtn.setStyle("-fx-background-color: #C0392B; -fx-text-fill: white; -fx-cursor: hand; " +
                "-fx-background-radius: 5; -fx-padding: 6 16; -fx-font-size: 11px; -fx-font-weight: bold;"));
        deleteBtn.setOnMouseExited(e -> deleteBtn.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-cursor: hand; " +
                "-fx-background-radius: 5; -fx-padding: 6 16; -fx-font-size: 11px; -fx-font-weight: bold;"));

        actionsBox.getChildren().addAll(editBtn, deleteBtn);

        row.getChildren().addAll(idLabel, titreLabel, auteurLabel, contenuLabel, dateLabel,
                likesLabel, dislikesLabel, vuesLabel, actionsBox);
        return row;
    }

    private void editSujet(Sujet sujet) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Modifier le sujet");
        dialog.setHeaderText("Modifier le sujet #" + sujet.getId());

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        TextField titreField = new TextField(sujet.getTitre());
        titreField.setPromptText("Titre");
        titreField.setStyle("-fx-padding: 10; -fx-background-radius: 8; -fx-border-color: #E8EEF4; -fx-border-radius: 8;");

        TextArea contenuArea = new TextArea(sujet.getContenu());
        contenuArea.setPromptText("Contenu");
        contenuArea.setPrefHeight(200);
        contenuArea.setStyle("-fx-padding: 10; -fx-background-radius: 8; -fx-border-color: #E8EEF4; -fx-border-radius: 8;");

        CheckBox anonymeCheck = new CheckBox("Publier anonymement");
        anonymeCheck.setSelected(sujet.isAnonyme());
        anonymeCheck.setStyle("-fx-text-fill: #7F8C8D;");

        content.getChildren().addAll(
                new Label("Titre:"), titreField,
                new Label("Contenu:"), contenuArea,
                anonymeCheck
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setStyle("-fx-background-color: #2C5F8A; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 5; -fx-padding: 8 20;");
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #7F8C8D; -fx-cursor: hand; -fx-padding: 8 20;");

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String newTitre = titreField.getText().trim();
            String newContenu = contenuArea.getText().trim();

            if (newTitre.isEmpty() || newTitre.length() < 3) {
                showAlert("Erreur", "Titre invalide (min 3 caractères)", Alert.AlertType.ERROR);
                return;
            }
            if (newContenu.isEmpty() || newContenu.length() < 10) {
                showAlert("Erreur", "Contenu invalide (min 10 caractères)", Alert.AlertType.ERROR);
                return;
            }
            if (newContenu.length() > 5000) {
                showAlert("Erreur", "Contenu trop long (max 5000 caractères)", Alert.AlertType.ERROR);
                return;
            }

            sujet.setTitre(newTitre);
            sujet.setContenu(newContenu);
            sujet.setAnonyme(anonymeCheck.isSelected());
            serviceSujet.update(sujet);
            loadSujets();
            showAlert("Succès", "Sujet modifié !", Alert.AlertType.INFORMATION);
        }
    }

    private void deleteSujet(Sujet sujet) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Supprimer le sujet");
        confirm.setHeaderText(null);
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer ce sujet ?\nTous les commentaires seront également supprimés.");

        DialogPane dialogPane = confirm.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #F8FAFE;");

        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        okButton.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 5; -fx-padding: 8 20;");

        Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
        cancelButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #7F8C8D; -fx-cursor: hand; -fx-padding: 8 20;");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            serviceSujet.delete(sujet);
            loadSujets();
            showAlert("Succès", "Sujet supprimé !", Alert.AlertType.INFORMATION);
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

    // Navigation
    @FXML private void onNavHomeClicked() { navigateTo("/HomeAdmin.fxml"); }
    @FXML private void onNavSuiviStatsClicked() { System.out.println("Statistiques"); }
    @FXML private void onNavObjectifsClicked() { System.out.println("Objectifs"); }
    @FXML private void onNavSujetsClicked() { navigateTo("/AdminSujet.fxml"); }
    @FXML private void onNavCommentairesClicked() { navigateTo("/AdminCommentaire.fxml"); }
    @FXML private void onNavRdvClicked() { System.out.println("Rendez-vous"); }
    @FXML private void onNavUtilisateursClicked() { System.out.println("Utilisateurs"); }
    @FXML private void onNavDossiersClicked() { System.out.println("Dossiers médicaux"); }
    @FXML private void onNavContenusClicked() { System.out.println("Contenus"); }
    @FXML private void onNavActivitesClicked() { System.out.println("Activités"); }

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
    @FXML private void onLogout() { System.out.println("Déconnexion"); }

    private void navigateTo(String fxmlPath) {
        try {
            Stage stage = (Stage) navAccueil.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            stage.getScene().setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}