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

    @FXML private HBox navAccueil, navSuivi, navForum, navRdv, navUtilisateurs, navDossiers, navContenus, navActivites;
    @FXML private HBox navSuiviStats, navObjectifs, navSujets, navCommentaires;
    @FXML private VBox submenuSuivi, submenuForum;
    @FXML private Label arrowSuivi, arrowForum;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterCombo;
    @FXML private Label totalSujetsLabel;

    @FXML private VBox cardsContainer;

    private ServiceSujet serviceSujet;
    private ServiceCommentaire serviceCommentaire;
    private List<Sujet> allSujets;
    private List<Sujet> filteredSujets;

    private boolean suiviOpen = false;
    private boolean forumOpen = true;

    private static final String COLOR_PRIMARY = "#2C5F8A";
    private static final String COLOR_DANGER = "#EF4444";
    private static final String COLOR_WARNING = "#F59E0B";
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

        serviceSujet = new ServiceSujet();
        serviceCommentaire = new ServiceCommentaire();
        allSujets = new ArrayList<>();
        filteredSujets = new ArrayList<>();

        setupFilters();
        loadSujets();
        setupNavigationHoverEffects();

        submenuSuivi.setVisible(false);
        submenuSuivi.setManaged(false);
        submenuForum.setVisible(true);
        submenuForum.setManaged(true);
        arrowSuivi.setText("▶");
        arrowForum.setText("▼");
    }

    private void setupNavigationHoverEffects() {
        addHoverEffect(navAccueil);
        addHoverEffect(navSuivi);
        addHoverEffect(navRdv);
        addHoverEffect(navUtilisateurs);
        addHoverEffect(navDossiers);
        addHoverEffect(navContenus);
        addHoverEffect(navActivites);
        addSubmenuHoverEffect(navSuiviStats);
        addSubmenuHoverEffect(navCommentaires);
    }

    private void addHoverEffect(HBox navItem) {
        navItem.setOnMouseEntered(e -> {
            if (!navItem.equals(navForum)) {
                navItem.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; -fx-padding: 10 14; -fx-cursor: hand;");
            }
        });
        navItem.setOnMouseExited(e -> {
            if (!navItem.equals(navForum)) {
                navItem.setStyle("-fx-background-color: transparent; -fx-background-radius: 10; -fx-padding: 10 14; -fx-cursor: hand;");
            }
        });
    }

    private void addSubmenuHoverEffect(HBox navItem) {
        navItem.setOnMouseEntered(e -> {
            navItem.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 8; -fx-padding: 8 12; -fx-cursor: hand;");
        });
        navItem.setOnMouseExited(e -> {
            navItem.setStyle("-fx-background-radius: 8; -fx-padding: 8 12; -fx-cursor: hand;");
        });
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
        row.setStyle("-fx-background-color: " + (isEven ? "rgba(255,255,255,0.9)" : "rgba(248,250,252,0.9)") +
                "; -fx-padding: 12 16; -fx-border-color: #E2E8F0; -fx-border-width: 0 0 1 0;");



        Label titreLabel = new Label(sujet.getTitre());
        titreLabel.setPrefWidth(200);
        titreLabel.setStyle("-fx-text-fill: #1A2B3C; -fx-font-weight: 600; -fx-font-size: 12px;");
        titreLabel.setWrapText(true);

        Label auteurLabel = new Label(sujet.isAnonyme() ? "Anonyme" : sujet.getUserName());
        auteurLabel.setPrefWidth(130);
        auteurLabel.setStyle("-fx-text-fill: #6B7C8D; -fx-font-size: 12px;");

        String contenuText = sujet.getContenu();
        if (contenuText.length() > 55) {
            contenuText = contenuText.substring(0, 55) + "...";
        }
        Label contenuLabel = new Label(contenuText);
        contenuLabel.setPrefWidth(220);
        contenuLabel.setStyle("-fx-text-fill: #4A5A6A; -fx-font-size: 12px;");
        contenuLabel.setWrapText(true);

        Label dateLabel = new Label(formatDate(sujet.getDateCreation()));
        dateLabel.setPrefWidth(100);
        dateLabel.setStyle("-fx-text-fill: #6B7C8D; -fx-font-size: 12px;");

        Label likesLabel = new Label("👍 " + sujet.getNbLikes());
        likesLabel.setPrefWidth(50);
        likesLabel.setStyle("-fx-text-fill: " + COLOR_SUCCESS + "; -fx-font-weight: bold; -fx-font-size: 12px;");
        likesLabel.setAlignment(Pos.CENTER);

        Label dislikesLabel = new Label("👎 " + sujet.getNbDislikes());
        dislikesLabel.setPrefWidth(50);
        dislikesLabel.setStyle("-fx-text-fill: " + COLOR_DANGER + "; -fx-font-weight: bold; -fx-font-size: 12px;");
        dislikesLabel.setAlignment(Pos.CENTER);

        Label vuesLabel = new Label("👁 " + sujet.getNbVues());
        vuesLabel.setPrefWidth(50);
        vuesLabel.setStyle("-fx-text-fill: #3498DB; -fx-font-weight: bold; -fx-font-size: 12px;");
        vuesLabel.setAlignment(Pos.CENTER);

        HBox actionsBox = new HBox(10);
        actionsBox.setAlignment(Pos.CENTER);
        actionsBox.setPrefWidth(180);

        Button editBtn = new Button("Modifier");
        editBtn.setStyle("-fx-background-color: #F39C12; -fx-text-fill: white; -fx-cursor: hand; " +
                "-fx-background-radius: 5; -fx-padding: 6 16; -fx-font-size: 10px; -fx-font-weight: bold;");
        editBtn.setMinWidth(80);
        editBtn.setOnAction(e -> editSujet(sujet));
        editBtn.setOnMouseEntered(e -> editBtn.setStyle("-fx-background-color: #E67E22; -fx-text-fill: white; -fx-cursor: hand; " +
                "-fx-background-radius: 5; -fx-padding: 6 16; -fx-font-size: 10px; -fx-font-weight: bold;"));
        editBtn.setOnMouseExited(e -> editBtn.setStyle("-fx-background-color: #F39C12; -fx-text-fill: white; -fx-cursor: hand; " +
                "-fx-background-radius: 5; -fx-padding: 6 16; -fx-font-size: 10px; -fx-font-weight: bold;"));

        Button deleteBtn = new Button("Supprimer");
        deleteBtn.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-cursor: hand; " +
                "-fx-background-radius: 5; -fx-padding: 6 16; -fx-font-size: 9px; -fx-font-weight: bold;");
        deleteBtn.setMinWidth(80);
        deleteBtn.setOnAction(e -> deleteSujet(sujet));
        deleteBtn.setOnMouseEntered(e -> deleteBtn.setStyle("-fx-background-color: #C0392B; -fx-text-fill: white; -fx-cursor: hand; " +
                "-fx-background-radius: 5; -fx-padding: 6 16; -fx-font-size: 9px; -fx-font-weight: bold;"));
        deleteBtn.setOnMouseExited(e -> deleteBtn.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-cursor: hand; " +
                "-fx-background-radius: 5; -fx-padding: 6 16; -fx-font-size: 9px; -fx-font-weight: bold;"));

        actionsBox.getChildren().addAll(editBtn, deleteBtn);

        row.getChildren().addAll( titreLabel, auteurLabel, contenuLabel, dateLabel,
                likesLabel, dislikesLabel, vuesLabel, actionsBox);
        return row;
    }

    private void editSujet(Sujet sujet) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Modifier le sujet");
        dialog.setHeaderText("Modifier le sujet #" + sujet.getId());

        VBox content = new VBox(12);
        content.setPadding(new Insets(24));
        content.setStyle("-fx-background-color: " + COLOR_BG + "; -fx-background-radius: 16;");

        Label titreLabel = new Label("📌 Titre");
        titreLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #1A2B3C;");

        TextField titreField = new TextField(sujet.getTitre());
        titreField.setPromptText("Titre");
        titreField.setStyle("-fx-padding: 10; -fx-background-radius: 12; -fx-border-color: #E2E8F0; -fx-border-radius: 12; -fx-background-color: rgba(255,255,255,0.9);");

        Label contenuLabel = new Label("💬 Contenu");
        contenuLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #1A2B3C;");

        TextArea contenuArea = new TextArea(sujet.getContenu());
        contenuArea.setPromptText("Contenu");
        contenuArea.setPrefHeight(200);
        contenuArea.setStyle("-fx-padding: 10; -fx-background-radius: 12; -fx-border-color: #E2E8F0; -fx-border-radius: 12; -fx-background-color: rgba(255,255,255,0.9);");

        CheckBox anonymeCheck = new CheckBox("📝 Publier anonymement");
        anonymeCheck.setSelected(sujet.isAnonyme());
        anonymeCheck.setStyle("-fx-text-fill: #4A5A6A; -fx-font-size: 13px; -fx-font-weight: 500;");

        content.getChildren().addAll(titreLabel, titreField, contenuLabel, contenuArea, anonymeCheck);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setStyle("-fx-background-color: " + COLOR_BG + "; -fx-background-radius: 16;");

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setStyle("-fx-background-color: " + COLOR_PRIMARY + "; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 8; -fx-padding: 8 24; -fx-font-weight: bold;");

        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #6B7C8D; -fx-cursor: hand; -fx-padding: 8 24; -fx-font-weight: 600;");

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String newTitre = titreField.getText().trim();
            String newContenu = contenuArea.getText().trim();

            if (newTitre.isEmpty() || newTitre.length() < 3) {
                showAlert("Erreur", "Titre invalide (min 3 caractères)", Alert.AlertType.ERROR);
                return;
            }
            if (newTitre.length() > 100) {
                showAlert("Erreur", "Titre trop long (max 100 caractères)", Alert.AlertType.ERROR);
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
            showAlert("Succès", "✓ Sujet modifié avec succès !", Alert.AlertType.INFORMATION);
        }
    }

    private void deleteSujet(Sujet sujet) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Supprimer le sujet");
        confirm.setHeaderText(null);
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer ce sujet ?\nTous les commentaires seront également supprimés.");

        DialogPane dialogPane = confirm.getDialogPane();
        dialogPane.setStyle("-fx-background-color: " + COLOR_BG + "; -fx-background-radius: 12;");

        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        okButton.setStyle("-fx-background-color: " + COLOR_DANGER + "; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 8; -fx-padding: 8 24; -fx-font-weight: bold;");

        Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
        cancelButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #6B7C8D; -fx-cursor: hand; -fx-padding: 8 24; -fx-font-weight: 600;");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                serviceSujet.deleteByAdmin(sujet);
                loadSujets();
                showAlert("Succès", "✓ Sujet supprimé avec succès !", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("Erreur", "Impossible de supprimer le sujet: " + e.getMessage(), Alert.AlertType.ERROR);
            }
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

    @FXML private void onNotifications() {
        showAlert("Notifications", "🔔 Aucune nouvelle notification", Alert.AlertType.INFORMATION);
    }

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
            showAlert("Erreur", "Impossible de naviguer vers: " + fxmlPath, Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: " + COLOR_BG + "; -fx-background-radius: 12;");

        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        okButton.setStyle("-fx-background-color: " + COLOR_PRIMARY + "; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 8; -fx-padding: 8 24; -fx-font-weight: bold;");

        alert.showAndWait();
    }
}