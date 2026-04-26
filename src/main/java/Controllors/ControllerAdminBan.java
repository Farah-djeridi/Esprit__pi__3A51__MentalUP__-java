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
import javafx.scene.chart.PieChart;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import models.Ban;
import services.ServiceBan;

public class ControllerAdminBan {

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
    @FXML private Label totalBansLabel;
    @FXML private Label activeBansLabel;
    @FXML private Label expiredBansLabel;
    @FXML private PieChart banStatsChart;

    @FXML private VBox cardsContainer;

    private ServiceBan serviceBan;
    private List<Ban> allBans;
    private List<Ban> filteredBans;

    private boolean suiviOpen = false;
    private boolean forumOpen = true;

    private static final String COLOR_PRIMARY = "#2C5F8A";
    private static final String COLOR_DANGER = "#EF4444";
    private static final String COLOR_SUCCESS = "#22C55E";
    private static final String COLOR_WARNING = "#F59E0B";
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

        serviceBan = new ServiceBan();
        allBans = new ArrayList<>();
        filteredBans = new ArrayList<>();

        setupFilters();
        loadBans();

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
        filterCombo.getItems().clear();
        filterCombo.getItems().addAll("Tous", "Actifs", "Expirés");
        filterCombo.setValue("Actifs");

        searchField.textProperty().addListener((obs, old, newVal) -> filterAndDisplay());
        filterCombo.valueProperty().addListener((obs, old, newVal) -> filterAndDisplay());
    }

    private void loadBans() {
        try {
            allBans = serviceBan.getAllBansWithDetails();
            updateStatistics();
            filterAndDisplay();
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de charger les bannissements: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void updateStatistics() {
        totalBansLabel.setText(String.valueOf(allBans.size()));

        LocalDate today = LocalDate.now();
        long active = allBans.stream().filter(b -> b.isActive() && !b.getBanExpiryDate().toLocalDate().isBefore(today)).count();
        long expired = allBans.size() - active;

        activeBansLabel.setText(String.valueOf(active));
        expiredBansLabel.setText(String.valueOf(expired));

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
            new PieChart.Data("Actifs", active),
            new PieChart.Data("Expirés", expired)
        );
        banStatsChart.setData(pieChartData);
        banStatsChart.setTitle("Statut des Bans");
    }

    private void filterAndDisplay() {
        String searchText = searchField.getText().toLowerCase();
        String filter = filterCombo.getValue();

        filteredBans = new ArrayList<>(allBans);

        if (searchText != null && !searchText.isEmpty()) {
            filteredBans.removeIf(b ->
                    (b.getUserName() == null || !b.getUserName().toLowerCase().contains(searchText))
            );
        }

        LocalDate today = LocalDate.now();
        if ("Actifs".equals(filter)) {
            filteredBans.removeIf(b ->
                    !b.isActive() || b.getBanExpiryDate().toLocalDate().isBefore(today)
            );
        } else if ("Expirés".equals(filter)) {
            filteredBans.removeIf(b ->
                    b.isActive() && !b.getBanExpiryDate().toLocalDate().isBefore(today)
            );
        }

        displayAllBans();
    }

    private void displayAllBans() {
        cardsContainer.getChildren().clear();

        for (int i = 0; i < filteredBans.size(); i++) {
            Ban ban = filteredBans.get(i);
            cardsContainer.getChildren().add(createBanRow(ban, i % 2 == 0));
        }
    }

    private HBox createBanRow(Ban ban, boolean isEven) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: " + (isEven ? "white" : "#F8FAFE") +
                "; -fx-padding: 12 15; -fx-border-color: #E8EEF4; -fx-border-width: 0 0 1 0;");

        Label userLabel = new Label(ban.getUserName() != null ? ban.getUserName() : "Utilisateur #" + ban.getUserId());
        userLabel.setPrefWidth(180);
        userLabel.setStyle("-fx-text-fill: #2C3E50; -fx-font-weight: 600; -fx-font-size: 13px;");

        Label banDateLabel = new Label(formatDate(ban.getBanDate()));
        banDateLabel.setPrefWidth(120);
        banDateLabel.setStyle("-fx-text-fill: #6B7C8D; -fx-font-size: 12px;");

        Label expiryDateLabel = new Label(formatDate(ban.getBanExpiryDate()));
        expiryDateLabel.setPrefWidth(120);

        LocalDate today = LocalDate.now();
        boolean isExpired = ban.getBanExpiryDate().toLocalDate().isBefore(today);
        boolean isActive = ban.isActive() && !isExpired;

        if (isExpired || !ban.isActive()) {
            expiryDateLabel.setStyle("-fx-text-fill: #6B7C8D; -fx-font-size: 12px;");
        } else {
            expiryDateLabel.setStyle("-fx-text-fill: #DC2626; -fx-font-weight: bold; -fx-font-size: 12px;");
        }

        long days = ChronoUnit.DAYS.between(ban.getBanDate().toLocalDate(), ban.getBanExpiryDate().toLocalDate());
        Label durationLabel = new Label(days + " jours");
        durationLabel.setPrefWidth(80);
        durationLabel.setStyle("-fx-text-fill: #6B7C8D; -fx-font-size: 12px;");
        durationLabel.setAlignment(Pos.CENTER);

        String reasonText = ban.getBanReason();
        if (reasonText.length() > 35) {
            reasonText = reasonText.substring(0, 35) + "...";
        }
        Label reasonLabel = new Label(reasonText);
        reasonLabel.setPrefWidth(250);
        reasonLabel.setStyle("-fx-text-fill: #4A5A6A; -fx-font-size: 12px;");
        reasonLabel.setWrapText(true);

        Label bannedByLabel = new Label(ban.getBannedByName() != null ? ban.getBannedByName() : "Admin");
        bannedByLabel.setPrefWidth(120);
        bannedByLabel.setStyle("-fx-text-fill: #6B7C8D; -fx-font-size: 12px;");

        HBox actionsBox = new HBox(8);
        actionsBox.setPrefWidth(180);
        actionsBox.setAlignment(Pos.CENTER);

        if (isActive) {
            // Bouton Modifier
            Button modifyBtn = new Button("✏ Modifier");
            modifyBtn.setStyle("-fx-background-color: #F59E0B; -fx-text-fill: white; -fx-cursor: hand; " +
                    "-fx-background-radius: 5; -fx-padding: 5 8; -fx-font-size: 10px; -fx-font-weight: bold;");
            modifyBtn.setOnAction(e -> modifyBan(ban));
            modifyBtn.setOnMouseEntered(ev -> modifyBtn.setStyle("-fx-background-color: #D97706; -fx-text-fill: white; -fx-cursor: hand; " +
                    "-fx-background-radius: 5; -fx-padding: 5 8; -fx-font-size: 10px; -fx-font-weight: bold;"));
            modifyBtn.setOnMouseExited(ev -> modifyBtn.setStyle("-fx-background-color: #F59E0B; -fx-text-fill: white; -fx-cursor: hand; " +
                    "-fx-background-radius: 5; -fx-padding: 5 8; -fx-font-size: 10px; -fx-font-weight: bold;"));
            actionsBox.getChildren().add(modifyBtn);

            Button unbanBtn = new Button("🔓 Débannir");
            unbanBtn.setStyle("-fx-background-color: #22C55E; -fx-text-fill: white; -fx-cursor: hand; " +
                    "-fx-background-radius: 5; -fx-padding: 5 8; -fx-font-size: 10px; -fx-font-weight: bold;");
            unbanBtn.setOnAction(e -> unbanUser(ban));
            unbanBtn.setOnMouseEntered(ev -> unbanBtn.setStyle("-fx-background-color: #16A34A; -fx-text-fill: white; -fx-cursor: hand; " +
                    "-fx-background-radius: 5; -fx-padding: 5 8; -fx-font-size: 10px; -fx-font-weight: bold;"));
            unbanBtn.setOnMouseExited(ev -> unbanBtn.setStyle("-fx-background-color: #22C55E; -fx-text-fill: white; -fx-cursor: hand; " +
                    "-fx-background-radius: 5; -fx-padding: 5 8; -fx-font-size: 10px; -fx-font-weight: bold;"));
            actionsBox.getChildren().add(unbanBtn);
        } else {

            Button deleteBtn = new Button("🗑 Supprimer");
            deleteBtn.setStyle("-fx-background-color: #6B7C8D; -fx-text-fill: white; -fx-cursor: hand; " +
                    "-fx-background-radius: 5; -fx-padding: 5 8; -fx-font-size: 10px; -fx-font-weight: bold;");
            deleteBtn.setOnAction(e -> deleteBan(ban));
            deleteBtn.setOnMouseEntered(ev -> deleteBtn.setStyle("-fx-background-color: #DC2626; -fx-text-fill: white; -fx-cursor: hand; " +
                    "-fx-background-radius: 5; -fx-padding: 5 8; -fx-font-size: 10px; -fx-font-weight: bold;"));
            deleteBtn.setOnMouseExited(ev -> deleteBtn.setStyle("-fx-background-color: #6B7C8D; -fx-text-fill: white; -fx-cursor: hand; " +
                    "-fx-background-radius: 5; -fx-padding: 5 8; -fx-font-size: 10px; -fx-font-weight: bold;"));
            actionsBox.getChildren().add(deleteBtn);

            Label statusLabel = new Label(isExpired ? "(Expiré)" : "(Désactivé)");
            statusLabel.setStyle("-fx-text-fill: #6B7C8D; -fx-font-size: 10px; -fx-font-style: italic;");
            actionsBox.getChildren().add(statusLabel);
        }

        row.getChildren().addAll(userLabel, banDateLabel, expiryDateLabel, durationLabel,
                reasonLabel, bannedByLabel, actionsBox);
        return row;
    }


    private void deleteBan(Ban ban) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Supprimer le bannissement");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: " + COLOR_BG + "; -fx-background-radius: 12;");

        Label iconLabel = new Label("⚠");
        iconLabel.setStyle("-fx-font-size: 42px; -fx-text-fill: #DC2626;");
        iconLabel.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Suppression définitive");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #DC2626;");
        titleLabel.setAlignment(Pos.CENTER);

        Label messageLabel = new Label("Êtes-vous sûr de vouloir supprimer ce bannissement ?");
        messageLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #4A5A6A;");
        messageLabel.setAlignment(Pos.CENTER);

        Label infoLabel = new Label("👤 " + ban.getUserName() + " | 📅 " + ban.getBanDate());
        infoLabel.setStyle("-fx-background-color: #F1F5F9; -fx-padding: 8 12; -fx-background-radius: 8; -fx-font-size: 14px; -fx-text-fill: #475569;");
        infoLabel.setAlignment(Pos.CENTER);

        Label warningLabel = new Label("⚠ Cette action est irréversible et supprimera l'historique.");
        warningLabel.setStyle("-fx-background-color: #FEE2E2; -fx-padding: 10; -fx-background-radius: 8; -fx-font-size: 13px; -fx-text-fill: #DC2626;");
        warningLabel.setWrapText(true);

        content.getChildren().addAll(iconLabel, titleLabel, messageLabel, infoLabel, warningLabel);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setStyle("-fx-background-color: " + COLOR_BG + "; -fx-background-radius: 16;");
        dialog.getDialogPane().setPrefWidth(350);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setText("🗑 Supprimer");
        okButton.setStyle("-fx-background-color: #DC2626; -fx-text-fill: white; -fx-cursor: hand; " +
                "-fx-background-radius: 8; -fx-padding: 8 20; -fx-font-weight: bold;");

        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelButton.setText("❌ Annuler");
        cancelButton.setStyle("-fx-background-color: #F1F5F9; -fx-text-fill: #475569; -fx-cursor: hand; " +
                "-fx-background-radius: 8; -fx-padding: 8 20; -fx-font-weight: 600;");

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                serviceBan.deleteBan(ban.getId());
                loadBans();

                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Succès");
                successAlert.setHeaderText(null);
                successAlert.setContentText("✓ Bannissement supprimé avec succès !");
                successAlert.showAndWait();
            } catch (Exception e) {
                showAlert("Erreur", "Impossible de supprimer le bannissement: " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }

    private void modifyBan(Ban existingBan) {
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

        Label headerSubtitle = new Label("Utilisateur: " + existingBan.getUserName());
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

        HBox expiryRow = new HBox(10);
        expiryRow.setAlignment(Pos.CENTER_LEFT);
        Label expiryIcon = new Label("⏰");
        expiryIcon.setStyle("-fx-font-size: 12px;");

        long currentDays = ChronoUnit.DAYS.between(existingBan.getBanDate().toLocalDate(),
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
        reasonArea.setPromptText("Entrez la raison du bannissement...");

        reasonBox.getChildren().addAll(newReasonLabel, reasonArea);

        modifySection.getChildren().addAll(modifyTitle, durationBox, reasonBox);

        content.getChildren().addAll(headerBox, infoCard, separator, modifySection);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setStyle("-fx-background-color: " + COLOR_BG + "; -fx-background-radius: 16;");
        dialog.getDialogPane().setPrefWidth(500);

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
                loadBans();

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

    private void unbanUser(Ban ban) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Débannir l'utilisateur");
        confirm.setHeaderText(null);
        confirm.setContentText("Êtes-vous sûr de vouloir débannir " + ban.getUserName() + " ?\n\n" +
                "L'utilisateur pourra à nouveau publier sur le forum.");

        DialogPane dialogPane = confirm.getDialogPane();
        dialogPane.setStyle("-fx-background-color: " + COLOR_BG + "; -fx-background-radius: 12;");

        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        okButton.setStyle("-fx-background-color: " + COLOR_SUCCESS + "; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 8; -fx-padding: 8 24; -fx-font-weight: bold;");

        Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
        cancelButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #6B7C8D; -fx-cursor: hand; -fx-padding: 8 24; -fx-font-weight: 600;");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                serviceBan.unbanUser(ban.getUserId());

                allBans = serviceBan.getAllBansWithDetails();

                filterAndDisplay();

                totalBansLabel.setText(String.valueOf(allBans.size()));

                showAlert("Succès", "✓ " + ban.getUserName() + " a été débanni avec succès !", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("Erreur", "Impossible de débannir l'utilisateur: " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
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
    @FXML private void onRefresh() { loadBans(); }

    @FXML
    private void onExportPDF() {
        if (filteredBans == null || filteredBans.isEmpty()) {
            showAlert("Export PDF", "Aucun bannissement à exporter.", Alert.AlertType.WARNING);
            return;
        }
        services.PDFService.exportBansToPDF(filteredBans, (Stage) navAccueil.getScene().getWindow());
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
}