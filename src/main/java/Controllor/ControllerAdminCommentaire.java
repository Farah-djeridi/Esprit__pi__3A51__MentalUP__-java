package Controllor;
import javafx.scene.input.MouseEvent;
import javafx.event.ActionEvent;
import Controllor.AdminSidebarHelper;

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
import javafx.scene.chart.PieChart;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import models.User;
import utils.SessionManager;

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
import models.Ban;
import services.ServiceBan;
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
    @FXML private HBox navSuiviStats, navObjectifs, navSujets, navCommentaires, navBans;
    @FXML private VBox submenuSuivi, submenuForum;
    @FXML private Label arrowSuivi, arrowForum;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterCombo;
    @FXML private Label totalCommentairesLabel;
    @FXML private Label toxicCommentsLabel;
    @FXML private Label safeCommentsLabel;
    @FXML private PieChart commentStatsChart;

    @FXML private VBox cardsContainer;

    private ServiceCommentaire serviceCommentaire;
    private ServiceSujet serviceSujet;
    private List<Commentaire> allCommentaires;
    private List<Commentaire> filteredCommentaires;
    private ServiceBan serviceBan;
    private int adminId = 1; // L'ID de l'administrateur connecté

    private boolean suiviOpen = false;
    private boolean forumOpen = true;

    private static final String COLOR_PRIMARY = "#2C5F8A";
    private static final String COLOR_DANGER = "#EF4444";
    private static final String COLOR_SUCCESS = "#22C55E";
    private static final String COLOR_BG = "#F0F4FA";

    @FXML
    public void initialize() {
        updateDate();

        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            this.adminId = currentUser.getId();
            labelUserName.setText(currentUser.getPrenom() + " " + currentUser.getNom());
            avatarInitials.setText(getInitials(currentUser.getPrenom() + " " + currentUser.getNom()));
        } else {
            labelUserName.setText("Admin MentalUp");
            avatarInitials.setText("AD");
        }

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
        serviceBan = new ServiceBan();
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

    private String getInitials(String fullName) {
        if (fullName == null || fullName.isEmpty()) return "AD";
        String[] parts = fullName.split(" ");
        if (parts.length >= 2) {
            return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
        }
        return fullName.substring(0, Math.min(2, fullName.length())).toUpperCase();
    }

    private void updateDate() {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRENCH);
        String formattedDate = today.format(formatter);
        formattedDate = formattedDate.substring(0, 1).toUpperCase() + formattedDate.substring(1);
        labelDate.setText(formattedDate);
    }

    private void setupFilters() {
        filterCombo.getItems().clear();
        filterCombo.getItems().addAll("Tous", "Toxiques", "Non toxiques");
        filterCombo.setValue("Tous");

        searchField.textProperty().addListener((obs, old, newVal) -> filterAndDisplay());
        filterCombo.valueProperty().addListener((obs, old, newVal) -> filterAndDisplay());
    }

    private void loadCommentaires() {
        try {
            allCommentaires = serviceCommentaire.getAll();
            updateStatistics();
            filterAndDisplay();
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de charger les commentaires: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void updateStatistics() {
        totalCommentairesLabel.setText(String.valueOf(allCommentaires.size()));

        long toxic = allCommentaires.stream().filter(Commentaire::isEstToxique).count();
        long safe = allCommentaires.size() - toxic;

        toxicCommentsLabel.setText(String.valueOf(toxic));
        safeCommentsLabel.setText(String.valueOf(safe));

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
            new PieChart.Data("Toxiques", toxic),
            new PieChart.Data("Sains", safe)
        );
        commentStatsChart.setData(pieChartData);
        commentStatsChart.setTitle("Analyse de Toxicité");
    }

    private void filterAndDisplay() {
        String searchText = searchField.getText().toLowerCase();
        String filter = filterCombo.getValue();

        filteredCommentaires = new ArrayList<>(allCommentaires);

        if (searchText != null && !searchText.isEmpty()) {
            filteredCommentaires.removeIf(c ->
                    !c.getContenu().toLowerCase().contains(searchText) &&
                            (c.getUserName() == null || !c.getUserName().toLowerCase().contains(searchText))
            );
        }

        if ("Toxiques".equals(filter)) {
            filteredCommentaires.removeIf(c -> !c.isEstToxique());
        } else if ("Non toxiques".equals(filter)) {
            filteredCommentaires.removeIf(c -> c.isEstToxique());
        }

        displayAllCommentaires();
    }

    private void displayAllCommentaires() {
        cardsContainer.getChildren().clear();

        for (int i = 0; i < filteredCommentaires.size(); i++) {
            Commentaire commentaire = filteredCommentaires.get(i);
            cardsContainer.getChildren().add(createCommentaireRow(commentaire, i % 2 == 0));
        }
    }

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

        VBox toxicityContainer = createToxicityProgressBar(commentaire.getScoreToxicite());
        toxicityContainer.setPrefWidth(110);
        toxicityContainer.setAlignment(Pos.CENTER_LEFT);

        HBox actionsBox = new HBox(8);
        actionsBox.setPrefWidth(120);
        actionsBox.setAlignment(Pos.CENTER);

        if (commentaire.isEstToxique()) {
            Button banBtn = new Button("🚫 Bannir");
            banBtn.setStyle("-fx-background-color: #DC2626; -fx-text-fill: white; -fx-cursor: hand; " +
                    "-fx-background-radius: 5; -fx-padding: 5 12; -fx-font-size: 10px; -fx-font-weight: bold;");
            banBtn.setMinWidth(80);
            banBtn.setOnAction(e -> openBanDialog(commentaire));
            banBtn.setOnMouseEntered(ev -> banBtn.setStyle("-fx-background-color: #B91C1C; -fx-text-fill: white; -fx-cursor: hand; " +
                    "-fx-background-radius: 5; -fx-padding: 5 12; -fx-font-size: 10px; -fx-font-weight: bold;"));
            banBtn.setOnMouseExited(ev -> banBtn.setStyle("-fx-background-color: #DC2626; -fx-text-fill: white; -fx-cursor: hand; " +
                    "-fx-background-radius: 5; -fx-padding: 5 12; -fx-font-size: 10px; -fx-font-weight: bold;"));
            actionsBox.getChildren().add(banBtn);
        } else {
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

    private void openBanDialog(Commentaire commentaire) {
        boolean isAlreadyBanned = serviceBan.isUserBanned(commentaire.getUserId());
        Ban existingBan = serviceBan.getActiveBan(commentaire.getUserId());

        if (isAlreadyBanned && existingBan != null) {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Utilisateur déjà banni");

            VBox content = new VBox(15);
            content.setPadding(new Insets(20));
            content.setStyle("-fx-background-color: " + COLOR_BG + "; -fx-background-radius: 12;");

            Label iconLabel = new Label("⚠");
            iconLabel.setStyle("-fx-font-size: 40px; -fx-text-fill: #F59E0B;");
            iconLabel.setAlignment(Pos.CENTER);

            Label titleLabel = new Label("Utilisateur déjà banni");
            titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #F59E0B;");
            titleLabel.setAlignment(Pos.CENTER);

            VBox infoCard = new VBox(8);
            infoCard.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 12; " +
                    "-fx-border-color: #E2E8F0; -fx-border-radius: 10; -fx-border-width: 1;");

            Label userName = new Label("👤 " + (commentaire.isAnonyme() ? "Anonyme" : commentaire.getUserName()));
            userName.setStyle("-fx-font-weight: bold; -fx-text-fill: #2C3E50; -fx-font-size: 13px;");

            Label expiryInfo = new Label("⏰ Banni jusqu'au: " + existingBan.getBanExpiryDate());
            expiryInfo.setStyle("-fx-text-fill: #DC2626; -fx-font-size: 12px; -fx-font-weight: 500;");

            Label reasonInfo = new Label("📝 Raison: " + existingBan.getBanReason());
            reasonInfo.setStyle("-fx-text-fill: #6B7C8D; -fx-font-size: 12px;");
            reasonInfo.setWrapText(true);

            infoCard.getChildren().addAll(userName, expiryInfo, reasonInfo);

            Label questionLabel = new Label("Que souhaitez-vous faire ?");
            questionLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #4A5A6A;");
            questionLabel.setAlignment(Pos.CENTER);

            content.getChildren().addAll(iconLabel, titleLabel, infoCard, questionLabel);

            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().setStyle("-fx-background-color: " + COLOR_BG + "; -fx-background-radius: 16;");
            dialog.getDialogPane().setPrefWidth(400);

            ButtonType modifyBanBtn = new ButtonType("✏ Modifier le bannissement");
            ButtonType deleteOnlyBtn = new ButtonType("🗑 Supprimer uniquement");
            ButtonType cancelBtn = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);

            dialog.getDialogPane().getButtonTypes().addAll(modifyBanBtn, deleteOnlyBtn, cancelBtn);

            Button modifyButton = (Button) dialog.getDialogPane().lookupButton(modifyBanBtn);
            modifyButton.setStyle("-fx-background-color: #F59E0B; -fx-text-fill: white; -fx-cursor: hand; " +
                    "-fx-background-radius: 8; -fx-padding: 8 16; -fx-font-weight: bold;");

            Button deleteButton = (Button) dialog.getDialogPane().lookupButton(deleteOnlyBtn);
            deleteButton.setStyle("-fx-background-color: #DC2626; -fx-text-fill: white; -fx-cursor: hand; " +
                    "-fx-background-radius: 8; -fx-padding: 8 16; -fx-font-weight: bold;");

            Button cancelButton = (Button) dialog.getDialogPane().lookupButton(cancelBtn);
            cancelButton.setStyle("-fx-background-color: #F1F5F9; -fx-text-fill: #475569; -fx-cursor: hand; " +
                    "-fx-background-radius: 8; -fx-padding: 8 16; -fx-font-weight: 600;");

            Optional<ButtonType> result = dialog.showAndWait();

            if (result.isPresent() && result.get() == modifyBanBtn) {
                modifyBanCommentaire(existingBan, commentaire);
            } else if (result.isPresent() && result.get() == deleteOnlyBtn) {
                deleteCommentaire(commentaire);
            }
            return;
        }

        showBanCommentaireDialog(commentaire, null);
    }

    private void modifyBanCommentaire(Ban existingBan, Commentaire commentaire) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Modifier le bannissement");
        dialog.setHeaderText(null);

        VBox content = new VBox(15);
        content.setPadding(new Insets(25));
        content.setStyle("-fx-background-color: " + COLOR_BG + "; -fx-background-radius: 16;");

        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setStyle("-fx-padding: 0 0 10 0;");

        Label headerIcon = new Label("✏");
        headerIcon.setStyle("-fx-font-size: 28px;");

        VBox headerText = new VBox(3);
        Label headerTitle = new Label("Modification du bannissement");
        headerTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");

        String userName = commentaire.isAnonyme() ? "Anonyme" : commentaire.getUserName();
        Label headerSubtitle = new Label("Utilisateur: " + userName);
        headerSubtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #6B7C8D;");

        headerText.getChildren().addAll(headerTitle, headerSubtitle);
        headerBox.getChildren().addAll(headerIcon, headerText);

        VBox infoCard = new VBox(8);
        infoCard.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 15; " +
                "-fx-border-color: #E2E8F0; -fx-border-radius: 12; -fx-border-width: 1;");

        Label infoTitle = new Label("📋 Bannissement actuel");
        infoTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");

        VBox infoDetails = new VBox(5);

        HBox dateRow = new HBox(10);
        dateRow.setAlignment(Pos.CENTER_LEFT);
        Label dateIcon = new Label("📅");
        dateIcon.setStyle("-fx-font-size: 12px;");
        Label dateLabel = new Label("Date: " + existingBan.getBanDate());
        dateLabel.setStyle("-fx-text-fill: #6B7C8D; -fx-font-size: 12px;");
        dateRow.getChildren().addAll(dateIcon, dateLabel);

        // Ligne Expiration
        HBox expiryRow = new HBox(10);
        expiryRow.setAlignment(Pos.CENTER_LEFT);
        Label expiryIcon = new Label("⏰");
        expiryIcon.setStyle("-fx-font-size: 12px;");

        long currentDays = java.time.temporal.ChronoUnit.DAYS.between(
                existingBan.getBanDate().toLocalDate(),
                existingBan.getBanExpiryDate().toLocalDate());

        Label expiryLabel = new Label("Expire le: " + existingBan.getBanExpiryDate() + " (" + currentDays + " jours)");
        expiryLabel.setStyle("-fx-text-fill: #DC2626; -fx-font-size: 12px; -fx-font-weight: bold;");
        expiryRow.getChildren().addAll(expiryIcon, expiryLabel);

        HBox reasonRow = new HBox(10);
        reasonRow.setAlignment(Pos.CENTER_LEFT);
        Label reasonIcon = new Label("📝");
        reasonIcon.setStyle("-fx-font-size: 12px;");
        Label reasonLabelText = new Label("Raison: " + existingBan.getBanReason());
        reasonLabelText.setStyle("-fx-text-fill: #6B7C8D; -fx-font-size: 12px;");
        reasonLabelText.setWrapText(true);
        reasonRow.getChildren().addAll(reasonIcon, reasonLabelText);

        infoDetails.getChildren().addAll(dateRow, expiryRow, reasonRow);
        infoCard.getChildren().addAll(infoTitle, infoDetails);

        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #E2E8F0;");

        VBox modifySection = new VBox(12);
        modifySection.setStyle("-fx-padding: 5 0 0 0;");

        Label modifyTitle = new Label("✏ Modifier le bannissement");
        modifyTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");

        VBox durationBox = new VBox(6);
        Label durationLabel = new Label("📅 Nouvelle durée");
        durationLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #4A5A6A; -fx-font-size: 13px;");

        HBox durationSelectionBox = new HBox(10);
        durationSelectionBox.setAlignment(Pos.CENTER_LEFT);

        ChoiceBox<Integer> durationChoice = new ChoiceBox<>();
        durationChoice.getItems().addAll(1, 3, 7, 15, 30, 90, 365);
        durationChoice.setValue((int) currentDays);
        durationChoice.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-font-size: 13px;");
        durationChoice.setPrefWidth(100);

        Label daysLabel = new Label("jours");
        daysLabel.setStyle("-fx-text-fill: #6B7C8D; -fx-font-size: 13px;");

        durationSelectionBox.getChildren().addAll(durationChoice, daysLabel);
        durationBox.getChildren().addAll(durationLabel, durationSelectionBox);

        VBox reasonBox = new VBox(6);
        Label newReasonLabel = new Label("📝 Nouvelle raison");
        newReasonLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #4A5A6A; -fx-font-size: 13px;");

        TextArea reasonArea = new TextArea();
        reasonArea.setText(existingBan.getBanReason());
        reasonArea.setPrefHeight(100);
        reasonArea.setWrapText(true);
        reasonArea.setStyle("-fx-background-color: white; -fx-background-radius: 8; " +
                "-fx-border-color: #E2E8F0; -fx-border-radius: 8; -fx-border-width: 1; " +
                "-fx-font-size: 12px; -fx-padding: 10;");

        reasonBox.getChildren().addAll(newReasonLabel, reasonArea);

        modifySection.getChildren().addAll(modifyTitle, durationBox, reasonBox);

        content.getChildren().addAll(headerBox, infoCard, separator, modifySection);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setStyle("-fx-background-color: " + COLOR_BG + "; -fx-background-radius: 16;");
        dialog.getDialogPane().setPrefWidth(550);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setText("💾 Enregistrer");
        okButton.setStyle("-fx-background-color: #F59E0B; -fx-text-fill: white; -fx-cursor: hand; " +
                "-fx-background-radius: 10; -fx-padding: 10 30; -fx-font-weight: bold; -fx-font-size: 13px;");

        okButton.setOnMouseEntered(e -> okButton.setStyle("-fx-background-color: #D97706; -fx-text-fill: white; -fx-cursor: hand; " +
                "-fx-background-radius: 10; -fx-padding: 10 30; -fx-font-weight: bold; -fx-font-size: 13px;"));
        okButton.setOnMouseExited(e -> okButton.setStyle("-fx-background-color: #F59E0B; -fx-text-fill: white; -fx-cursor: hand; " +
                "-fx-background-radius: 10; -fx-padding: 10 30; -fx-font-weight: bold; -fx-font-size: 13px;"));

        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelButton.setText("❌ Annuler");
        cancelButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #6B7C8D; -fx-cursor: hand; " +
                "-fx-padding: 10 30; -fx-font-weight: 600; -fx-font-size: 13px;");

        cancelButton.setOnMouseEntered(e -> cancelButton.setStyle("-fx-background-color: #F1F5F9; -fx-text-fill: #475569; -fx-cursor: hand; " +
                "-fx-padding: 10 30; -fx-font-weight: 600; -fx-font-size: 13px; -fx-background-radius: 10;"));
        cancelButton.setOnMouseExited(e -> cancelButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #6B7C8D; -fx-cursor: hand; " +
                "-fx-padding: 10 30; -fx-font-weight: 600; -fx-font-size: 13px;"));

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            int newDuration = durationChoice.getValue();
            String newReason = reasonArea.getText().trim();

            if (newReason.isEmpty()) {
                newReason = existingBan.getBanReason();
            }

            if (newDuration == currentDays && newReason.equals(existingBan.getBanReason())) {
                showAlert("Information", "Aucune modification détectée.", Alert.AlertType.INFORMATION);
                return;
            }

            try {

                serviceBan.updateBan(existingBan.getId(), newReason, newDuration);
                serviceCommentaire.delete(commentaire);
                loadCommentaires();

                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Succès");
                successAlert.setHeaderText(null);
                successAlert.setContentText("✓ Bannissement modifié avec succès !\n\n" +
                        "🆔 ID: " + existingBan.getId() + "\n" +
                        "📅 Nouvelle durée: " + newDuration + " jours\n" +
                        "📝 Nouvelle raison: " + newReason);

                DialogPane successPane = successAlert.getDialogPane();
                successPane.setStyle("-fx-background-color: " + COLOR_BG + "; -fx-background-radius: 12;");

                Button successOk = (Button) successPane.lookupButton(ButtonType.OK);
                successOk.setStyle("-fx-background-color: " + COLOR_SUCCESS + "; -fx-text-fill: white; -fx-cursor: hand; " +
                        "-fx-background-radius: 8; -fx-padding: 8 24; -fx-font-weight: bold;");

                successAlert.showAndWait();
            } catch (Exception e) {
                showAlert("Erreur", "Impossible de modifier le bannissement: " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }

    private void showBanCommentaireDialog(Commentaire commentaire, Ban existingBan) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Bannir l'utilisateur");
        dialog.setHeaderText(null);

        VBox content = new VBox(15);
        content.setPadding(new Insets(25));
        content.setStyle("-fx-background-color: " + COLOR_BG + "; -fx-background-radius: 16;");

        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label headerIcon = new Label("🚫");
        headerIcon.setStyle("-fx-font-size: 28px;");

        VBox headerText = new VBox(3);
        Label headerTitle = new Label("Bannissement utilisateur");
        headerTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #DC2626;");

        Label headerSubtitle = new Label("Commentaire toxique détecté - Score: " +
                String.format("%.0f", commentaire.getScoreToxicite() * 100) + "%");
        headerSubtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7C8D;");

        headerText.getChildren().addAll(headerTitle, headerSubtitle);
        headerBox.getChildren().addAll(headerIcon, headerText);

        VBox infoCard = new VBox(8);
        infoCard.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 15; " +
                "-fx-border-color: #E2E8F0; -fx-border-radius: 12; -fx-border-width: 1;");

        Label infoTitle = new Label("👤 Informations");
        infoTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");

        Label userName = new Label("Utilisateur: " + (commentaire.isAnonyme() ? "Anonyme" : commentaire.getUserName()));
        userName.setStyle("-fx-text-fill: #2C3E50; -fx-font-size: 13px; -fx-font-weight: 600;");

        Label commentInfo = new Label("💬 Commentaire #" + commentaire.getId());
        commentInfo.setStyle("-fx-text-fill: #6B7C8D; -fx-font-size: 12px;");

        infoCard.getChildren().addAll(infoTitle, userName, commentInfo);

        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #E2E8F0;");

        VBox banSection = new VBox(12);
        banSection.setStyle("-fx-padding: 5 0 0 0;");

        Label sectionTitle = new Label("⚙️ Configuration du bannissement");
        sectionTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");

        VBox durationBox = new VBox(6);
        Label durationLabel = new Label("📅 Durée");
        durationLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #4A5A6A; -fx-font-size: 13px;");

        HBox durationSelectionBox = new HBox(10);
        durationSelectionBox.setAlignment(Pos.CENTER_LEFT);

        ChoiceBox<Integer> durationChoice = new ChoiceBox<>();
        durationChoice.getItems().addAll(1, 3, 7, 15, 30, 90, 365);
        durationChoice.setValue(7);
        durationChoice.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-font-size: 13px;");
        durationChoice.setPrefWidth(100);

        Label daysLabel = new Label("jours");
        daysLabel.setStyle("-fx-text-fill: #6B7C8D; -fx-font-size: 13px;");

        durationSelectionBox.getChildren().addAll(durationChoice, daysLabel);
        durationBox.getChildren().addAll(durationLabel, durationSelectionBox);

        VBox reasonBox = new VBox(6);
        Label reasonLabel = new Label("📝 Raison");
        reasonLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #4A5A6A; -fx-font-size: 13px;");

        TextArea reasonArea = new TextArea();
        reasonArea.setPromptText("Ex: Commentaire toxique (score: " + String.format("%.0f", commentaire.getScoreToxicite() * 100) + "%)");
        reasonArea.setPrefHeight(80);
        reasonArea.setWrapText(true);
        reasonArea.setStyle("-fx-background-color: white; -fx-background-radius: 8; " +
                "-fx-border-color: #E2E8F0; -fx-border-radius: 8; -fx-border-width: 1; " +
                "-fx-font-size: 12px; -fx-padding: 10;");

        reasonBox.getChildren().addAll(reasonLabel, reasonArea);

        banSection.getChildren().addAll(sectionTitle, durationBox, reasonBox);

        content.getChildren().addAll(headerBox, infoCard, separator, banSection);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setStyle("-fx-background-color: " + COLOR_BG + "; -fx-background-radius: 16;");
        dialog.getDialogPane().setPrefWidth(450);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setText("🚫 Bannir");
        okButton.setStyle("-fx-background-color: #DC2626; -fx-text-fill: white; -fx-cursor: hand; " +
                "-fx-background-radius: 10; -fx-padding: 10 24; -fx-font-weight: bold; -fx-font-size: 13px;");

        okButton.setOnMouseEntered(e -> okButton.setStyle("-fx-background-color: #B91C1C; -fx-text-fill: white; -fx-cursor: hand; " +
                "-fx-background-radius: 10; -fx-padding: 10 24; -fx-font-weight: bold; -fx-font-size: 13px;"));
        okButton.setOnMouseExited(e -> okButton.setStyle("-fx-background-color: #DC2626; -fx-text-fill: white; -fx-cursor: hand; " +
                "-fx-background-radius: 10; -fx-padding: 10 24; -fx-font-weight: bold; -fx-font-size: 13px;"));

        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelButton.setText("❌ Annuler");
        cancelButton.setStyle("-fx-background-color: #F1F5F9; -fx-text-fill: #475569; -fx-cursor: hand; " +
                "-fx-background-radius: 10; -fx-padding: 10 24; -fx-font-weight: 600; -fx-font-size: 13px;");

        cancelButton.setOnMouseEntered(e -> cancelButton.setStyle("-fx-background-color: #E2E8F0; -fx-text-fill: #1E293B; -fx-cursor: hand; " +
                "-fx-background-radius: 10; -fx-padding: 10 24; -fx-font-weight: 600; -fx-font-size: 13px;"));
        cancelButton.setOnMouseExited(e -> cancelButton.setStyle("-fx-background-color: #F1F5F9; -fx-text-fill: #475569; -fx-cursor: hand; " +
                "-fx-background-radius: 10; -fx-padding: 10 24; -fx-font-weight: 600; -fx-font-size: 13px;"));

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            int duration = durationChoice.getValue();
            String reason = reasonArea.getText().trim();
            if (reason.isEmpty()) {
                reason = "Commentaire toxique (score: " + String.format("%.0f", commentaire.getScoreToxicite() * 100) + "%)";
            }

            try {
                serviceBan.banUser(commentaire.getUserId(), reason, duration, adminId);
                serviceCommentaire.delete(commentaire);
                loadCommentaires();

                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Succès");
                successAlert.setHeaderText(null);
                successAlert.setContentText("✓ Utilisateur banni pour " + duration + " jours !\n\n" +
                        "👤 " + (commentaire.isAnonyme() ? "Anonyme" : commentaire.getUserName()) + "\n" +
                        "📝 " + reason);

                DialogPane successPane = successAlert.getDialogPane();
                successPane.setStyle("-fx-background-color: " + COLOR_BG + "; -fx-background-radius: 12;");

                Button successOk = (Button) successPane.lookupButton(ButtonType.OK);
                successOk.setStyle("-fx-background-color: " + COLOR_SUCCESS + "; -fx-text-fill: white; -fx-cursor: hand; " +
                        "-fx-background-radius: 8; -fx-padding: 8 24; -fx-font-weight: bold;");

                successAlert.showAndWait();
            } catch (Exception e) {
                showAlert("Erreur", "Impossible de bannir l'utilisateur: " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }

    private void executeBan(Commentaire commentaire, int duration, String reason) {
        try {
            serviceBan.banUser(commentaire.getUserId(), reason, duration, adminId);

            serviceCommentaire.delete(commentaire);

            loadCommentaires();

            String message = "✓ Utilisateur banni avec succès !\n\n" +
                    "📝 Détails du bannissement:\n" +
                    "• Utilisateur: " + (commentaire.isAnonyme() ? "Anonyme" : commentaire.getUserName()) + "\n" +
                    "• Durée: " + duration + " jours\n" +
                    "• Raison: " + reason + "\n\n" +
                    "Le commentaire a été supprimé.";

            showAlert("Succès - Bannissement effectué", message, Alert.AlertType.INFORMATION);

        } catch (Exception e) {
            showAlert("Erreur", "Impossible de bannir l'utilisateur: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
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
    @FXML private void onRefresh() { loadCommentaires(); }

    @FXML
    private void onExportPDF() {
        if (filteredCommentaires == null || filteredCommentaires.isEmpty()) {
            showAlert("Export PDF", "Aucun commentaire à exporter.", Alert.AlertType.WARNING);
            return;
        }
        services.PDFService.exportCommentairesToPDF(filteredCommentaires, (Stage) navAccueil.getScene().getWindow());
    }

    @FXML private void onNavHomeClicked() { navigateTo("/HomeAdmin.fxml"); }
    @FXML private void onNavSuiviStatsClicked() { navigateTo("/StatistiquesAdmin.fxml"); }
    @FXML private void onNavObjectifsClicked() { navigateTo("/ObjectifsAdmin.fxml"); }
    @FXML private void onNavSujetsClicked() { navigateTo("/AdminSujet.fxml"); }
    @FXML private void onNavCommentairesClicked() { navigateTo("/AdminCommentaire.fxml"); }
    @FXML private void onNavBansClicked() { navigateTo("/AdminBan.fxml"); }
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
                SessionManager.getInstance().logout();
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

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: " + COLOR_BG + "; -fx-background-radius: 12;");

        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        okButton.setStyle("-fx-background-color: " + COLOR_PRIMARY + "; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 8; -fx-padding: 8 24; -fx-font-weight: bold;");

        alert.showAndWait();
    }

    @FXML public void onNavHomeClicked(MouseEvent e)         { AdminSidebarHelper.goToAccueil(); }
    @FXML public void onNavSuiviClicked(MouseEvent e)        { AdminSidebarHelper.goToSuiviMental(); }
    @FXML public void onNavForumClicked(MouseEvent e)        { AdminSidebarHelper.goToForum(); }
    @FXML public void onNavRdvClicked(MouseEvent e)          { AdminSidebarHelper.goToRendezVous(); }
    @FXML public void onNavDossiersClicked(MouseEvent e)     { AdminSidebarHelper.goToDossiers(); }
    @FXML public void onNavUtilisateursClicked(MouseEvent e) { AdminSidebarHelper.goToUtilisateurs(); }
    @FXML public void onNavContenusClicked(MouseEvent e)     { AdminSidebarHelper.goToContenus(); }
    @FXML public void onNavActivitesClicked(MouseEvent e)    { AdminSidebarHelper.goToActivites(); }
    @FXML public void onNavReservationsClicked(MouseEvent e) { AdminSidebarHelper.goToReservations(); }
    @FXML public void onNavHoverEnter(MouseEvent e)          { }
    @FXML public void onNavHoverExit(MouseEvent e)           { }
    @FXML public void onLogout(ActionEvent e)                { AdminSidebarHelper.logout(); }
    @FXML public void onSubmenuHoverEnter(MouseEvent e)       { }
    @FXML public void onSubmenuHoverExit(MouseEvent e)        { }
}