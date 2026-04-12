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
    @FXML private Label totalCommentairesLabel;

    @FXML private VBox cardsContainer;

    @FXML private HBox paginationBox;

    private ServiceCommentaire serviceCommentaire;
    private ServiceSujet serviceSujet;
    private List<Commentaire> allCommentaires;
    private List<Commentaire> filteredCommentaires;

    private int currentPage = 1;
    private int totalPages = 1;
    private final int itemsPerPage = 5;

    private boolean suiviOpen = false;
    private boolean forumOpen = true;

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
        searchField.textProperty().addListener((obs, old, newVal) -> filterAndDisplay());
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

        filteredCommentaires = new ArrayList<>(allCommentaires);

        if (searchText != null && !searchText.isEmpty()) {
            filteredCommentaires.removeIf(c ->
                    !c.getContenu().toLowerCase().contains(searchText) &&
                            (c.getUserName() == null || !c.getUserName().toLowerCase().contains(searchText))
            );
        }

        updatePagination();
        displayCurrentPage();
    }

    private void updatePagination() {
        totalPages = (int) Math.ceil((double) filteredCommentaires.size() / itemsPerPage);
        if (totalPages < 1) totalPages = 1;
        if (currentPage > totalPages) currentPage = totalPages;
    }

    private void displayCurrentPage() {
        cardsContainer.getChildren().clear();

        int start = (currentPage - 1) * itemsPerPage;
        int end = Math.min(start + itemsPerPage, filteredCommentaires.size());

        for (int i = start; i < end; i++) {
            Commentaire commentaire = filteredCommentaires.get(i);
            cardsContainer.getChildren().add(createCommentaireRow(commentaire, i % 2 == 0));
        }

    }

    private HBox createCommentaireRow(Commentaire commentaire, boolean isEven) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: " + (isEven ? "white" : "#F8FAFE") +
                "; -fx-padding: 12 15; -fx-border-color: #E8EEF4; -fx-border-width: 0 0 1 0;");

        Label idLabel = new Label(String.valueOf(commentaire.getId()));
        idLabel.setPrefWidth(50);
        idLabel.setStyle("-fx-text-fill: #2C3E50; -fx-font-weight: bold;");

        String contenuText = commentaire.getContenu();
        if (contenuText.length() > 55) {
            contenuText = contenuText.substring(0, 55) + "...";
        }
        Label contenuLabel = new Label(contenuText);
        contenuLabel.setPrefWidth(280);
        contenuLabel.setStyle("-fx-text-fill: #2C3E50;");
        contenuLabel.setWrapText(true);

        Label auteurLabel = new Label(commentaire.isAnonyme() ? "Anonyme" : commentaire.getUserName());
        auteurLabel.setPrefWidth(130);
        auteurLabel.setStyle("-fx-text-fill: #7F8C8D;");

        String sujetTitre = "Chargement...";
        try {
            Sujet sujet = serviceSujet.getById(commentaire.getSujetId());
            if (sujet != null) {
                sujetTitre = sujet.getTitre();
                if (sujetTitre.length() > 25) {
                    sujetTitre = sujetTitre.substring(0, 25) + "...";
                }
            } else {
                sujetTitre = "Sujet inconnu";
            }
        } catch (Exception e) {
            sujetTitre = "Sujet inconnu";
        }
        Label sujetLabel = new Label(sujetTitre);
        sujetLabel.setPrefWidth(180);
        sujetLabel.setStyle("-fx-text-fill: #7F8C8D;");

        Label dateLabel = new Label(formatDate(commentaire.getDateCommentaire()));
        dateLabel.setPrefWidth(100);
        dateLabel.setStyle("-fx-text-fill: #7F8C8D;");

        Label likesLabel = new Label(String.valueOf(commentaire.getNbLikes()));
        likesLabel.setPrefWidth(50);
        likesLabel.setStyle("-fx-text-fill: #27AE60; -fx-font-weight: bold;");
        likesLabel.setAlignment(Pos.CENTER);

        Label dislikesLabel = new Label(String.valueOf(commentaire.getNbDislikes()));
        dislikesLabel.setPrefWidth(50);
        dislikesLabel.setStyle("-fx-text-fill: #E74C3C; -fx-font-weight: bold;");
        dislikesLabel.setAlignment(Pos.CENTER);

        HBox actionsBox = new HBox(10);
        actionsBox.setPrefWidth(140);
        actionsBox.setAlignment(Pos.CENTER);

        Button editBtn = new Button("Modifier");
        editBtn.setStyle("-fx-background-color: #F39C12; -fx-text-fill: white; -fx-cursor: hand; " +
                "-fx-background-radius: 5; -fx-padding: 6 16; -fx-font-size: 10px; -fx-font-weight: bold;");
        editBtn.setMinWidth(80);
        editBtn.setOnAction(e -> editCommentaire(commentaire));
        editBtn.setOnMouseEntered(e -> editBtn.setStyle("-fx-background-color: #E67E22; -fx-text-fill: white; -fx-cursor: hand; " +
                "-fx-background-radius: 5; -fx-padding: 6 16; -fx-font-size: 10px; -fx-font-weight: bold;"));
        editBtn.setOnMouseExited(e -> editBtn.setStyle("-fx-background-color: #F39C12; -fx-text-fill: white; -fx-cursor: hand; " +
                "-fx-background-radius: 5; -fx-padding: 6 16; -fx-font-size: 10px; -fx-font-weight: bold;"));

        Button deleteBtn = new Button("Supprimer");
        deleteBtn.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-cursor: hand; " +
                "-fx-background-radius: 5; -fx-padding: 6 16; -fx-font-size: 9px; -fx-font-weight: bold;");
        deleteBtn.setMinWidth(80);
        deleteBtn.setOnAction(e -> deleteCommentaire(commentaire));
        deleteBtn.setOnMouseEntered(e -> deleteBtn.setStyle("-fx-background-color: #C0392B; -fx-text-fill: white; -fx-cursor: hand; " +
                "-fx-background-radius: 5; -fx-padding: 6 16; -fx-font-size: 9px; -fx-font-weight: bold;"));
        deleteBtn.setOnMouseExited(e -> deleteBtn.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-cursor: hand; " +
                "-fx-background-radius: 5; -fx-padding: 6 16; -fx-font-size: 9px; -fx-font-weight: bold;"));

        actionsBox.getChildren().addAll(editBtn, deleteBtn);

        row.getChildren().addAll(idLabel, contenuLabel, auteurLabel, sujetLabel, dateLabel,
                likesLabel, dislikesLabel, actionsBox);
        return row;
    }

    private void editCommentaire(Commentaire commentaire) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Modifier le commentaire");
        dialog.setHeaderText("Modifier le commentaire #" + commentaire.getId());

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        TextArea contenuArea = new TextArea(commentaire.getContenu());
        contenuArea.setPromptText("Contenu");
        contenuArea.setPrefHeight(150);
        contenuArea.setStyle("-fx-padding: 10; -fx-background-radius: 8; -fx-border-color: #E8EEF4; -fx-border-radius: 8;");

        CheckBox anonymeCheck = new CheckBox("Anonyme");
        anonymeCheck.setSelected(commentaire.isAnonyme());
        anonymeCheck.setStyle("-fx-text-fill: #7F8C8D;");

        content.getChildren().addAll(
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
            String newContenu = contenuArea.getText().trim();

            if (newContenu.isEmpty() || newContenu.length() < 3) {
                showAlert("Erreur", "Commentaire invalide (min 3 caractères)", Alert.AlertType.ERROR);
                return;
            }
            if (newContenu.length() > 1000) {
                showAlert("Erreur", "Commentaire trop long (max 1000 caractères)", Alert.AlertType.ERROR);
                return;
            }

            commentaire.setContenu(newContenu);
            commentaire.setAnonyme(anonymeCheck.isSelected());
            serviceCommentaire.update(commentaire);
            loadCommentaires();
            showAlert("Succès", "Commentaire modifié !", Alert.AlertType.INFORMATION);
        }
    }

    private void deleteCommentaire(Commentaire commentaire) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Supprimer le commentaire");
        confirm.setHeaderText(null);
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer ce commentaire ?");

        DialogPane dialogPane = confirm.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #F8FAFE;");

        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        okButton.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 5; -fx-padding: 8 20;");

        Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
        cancelButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #7F8C8D; -fx-cursor: hand; -fx-padding: 8 20;");

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