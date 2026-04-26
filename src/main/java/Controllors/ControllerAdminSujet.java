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

import models.Sujet;
import models.Ban;
import services.ServiceBan;
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
    @FXML private Label toxicSujetsLabel;
    @FXML private Label popularSujetsLabel;
    @FXML private PieChart sujetStatsChart;


    @FXML private VBox cardsContainer;

    private ServiceSujet serviceSujet;
    private ServiceCommentaire serviceCommentaire;
    private List<Sujet> allSujets;
    private List<Sujet> filteredSujets;

    private ServiceBan serviceBan;
    private int adminId = 1;

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

        serviceBan = new ServiceBan();

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
        filterCombo.getItems().addAll("Tous", "Plus populaires", "Plus récents", "Plus toxiques");
        filterCombo.setValue("Tous");
        searchField.textProperty().addListener((obs, old, newVal) -> filterAndDisplay());
        filterCombo.valueProperty().addListener((obs, old, newVal) -> filterAndDisplay());
    }

    private void loadSujets() {
        try {
            allSujets = serviceSujet.getAll();
            updateStatistics();
            filterAndDisplay();
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de charger les sujets: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void updateStatistics() {
        totalSujetsLabel.setText(String.valueOf(allSujets.size()));

        long toxic = allSujets.stream().filter(Sujet::isEstToxique).count();
        long popular = allSujets.stream().filter(s -> s.getNbLikes() > 10).count();
        long normal = allSujets.size() - toxic - popular;
        if (normal < 0) normal = 0;

        toxicSujetsLabel.setText(String.valueOf(toxic));
        popularSujetsLabel.setText(String.valueOf(popular));

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
            new PieChart.Data("Toxiques", toxic),
            new PieChart.Data("Populaires", popular),
            new PieChart.Data("Normaux", normal)
        );
        sujetStatsChart.setData(pieChartData);
        sujetStatsChart.setTitle("Répartition des Sujets");
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
        } else if ("Plus toxiques".equals(filter)) {
            filteredSujets.sort((a, b) -> Double.compare(b.getScoreToxicite(), a.getScoreToxicite()));
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


    private VBox createToxicityProgressBar(double score) {
        VBox container = new VBox(4);
        container.setAlignment(Pos.CENTER_LEFT);
        container.setPrefWidth(120);

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
        progressBar.setPrefWidth(100);
        progressBar.setPrefHeight(6);

        Rectangle background = new Rectangle(100, 6);
        background.setFill(Color.web("#E2E8F0"));
        background.setArcWidth(3);
        background.setArcHeight(3);

        Rectangle progress = new Rectangle(100 * score, 6);
        progress.setFill(Color.web(barColor));
        progress.setArcWidth(3);
        progress.setArcHeight(3);

        progressBar.getChildren().addAll(background, progress);
        container.getChildren().addAll(percentLabel, progressBar);

        return container;
    }

    private HBox createSujetRow(Sujet sujet, boolean isEven) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: " + (isEven ? "rgba(255,255,255,0.9)" : "rgba(248,250,252,0.9)") +
                "; -fx-padding: 12 15; -fx-border-color: #E2E8F0; -fx-border-width: 0 0 1 0;");

        Label titreLabel = new Label(sujet.getTitre());
        titreLabel.setPrefWidth(180);
        titreLabel.setStyle("-fx-text-fill: #1A2B3C; -fx-font-weight: 600; -fx-font-size: 12px;");
        titreLabel.setWrapText(true);

        Label auteurLabel = new Label(sujet.isAnonyme() ? "Anonyme" : sujet.getUserName());
        auteurLabel.setPrefWidth(120);
        auteurLabel.setStyle("-fx-text-fill: #6B7C8D; -fx-font-size: 12px;");

        String contenuText = sujet.getContenu();
        if (contenuText.length() > 50) {
            contenuText = contenuText.substring(0, 50) + "...";
        }
        Label contenuLabel = new Label(contenuText);
        contenuLabel.setPrefWidth(200);
        contenuLabel.setStyle("-fx-text-fill: #4A5A6A; -fx-font-size: 12px;");
        contenuLabel.setWrapText(true);

        Label dateLabel = new Label(formatDate(sujet.getDateCreation()));
        dateLabel.setPrefWidth(90);
        dateLabel.setStyle("-fx-text-fill: #6B7C8D; -fx-font-size: 12px;");

        Label likesLabel = new Label("👍 " + sujet.getNbLikes());
        likesLabel.setPrefWidth(45);
        likesLabel.setStyle("-fx-text-fill: " + COLOR_SUCCESS + "; -fx-font-weight: bold; -fx-font-size: 12px;");
        likesLabel.setAlignment(Pos.CENTER);

        Label dislikesLabel = new Label("👎 " + sujet.getNbDislikes());
        dislikesLabel.setPrefWidth(45);
        dislikesLabel.setStyle("-fx-text-fill: " + COLOR_DANGER + "; -fx-font-weight: bold; -fx-font-size: 12px;");
        dislikesLabel.setAlignment(Pos.CENTER);

        Label vuesLabel = new Label("👁 " + sujet.getNbVues());
        vuesLabel.setPrefWidth(45);
        vuesLabel.setStyle("-fx-text-fill: #3498DB; -fx-font-weight: bold; -fx-font-size: 12px;");
        vuesLabel.setAlignment(Pos.CENTER);

        VBox toxicityContainer = createToxicityProgressBar(sujet.getScoreToxicite());
        toxicityContainer.setPrefWidth(120);
        toxicityContainer.setAlignment(Pos.CENTER_LEFT);

        HBox actionsBox = new HBox(8);
        actionsBox.setAlignment(Pos.CENTER);
        actionsBox.setPrefWidth(160);

        if (sujet.isEstToxique()) {
            Button banBtn = new Button("🚫 Bannir");
            banBtn.setStyle("-fx-background-color: #DC2626; -fx-text-fill: white; -fx-cursor: hand; " +
                    "-fx-background-radius: 5; -fx-padding: 5 12; -fx-font-size: 10px; -fx-font-weight: bold;");
            banBtn.setOnAction(e -> banSujet(sujet));
            banBtn.setOnMouseEntered(ev -> banBtn.setStyle("-fx-background-color: #B91C1C; -fx-text-fill: white; -fx-cursor: hand; " +
                    "-fx-background-radius: 5; -fx-padding: 5 12; -fx-font-size: 10px; -fx-font-weight: bold;"));
            banBtn.setOnMouseExited(ev -> banBtn.setStyle("-fx-background-color: #DC2626; -fx-text-fill: white; -fx-cursor: hand; " +
                    "-fx-background-radius: 5; -fx-padding: 5 12; -fx-font-size: 10px; -fx-font-weight: bold;"));
            actionsBox.getChildren().add(banBtn);
        } else {
            Button deleteBtn = new Button("🗑 Supprimer");
            deleteBtn.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-cursor: hand; " +
                    "-fx-background-radius: 5; -fx-padding: 5 12; -fx-font-size: 10px; -fx-font-weight: bold;");
            deleteBtn.setOnAction(e -> deleteSujet(sujet));
            deleteBtn.setOnMouseEntered(ev -> deleteBtn.setStyle("-fx-background-color: #C0392B; -fx-text-fill: white; -fx-cursor: hand; " +
                    "-fx-background-radius: 5; -fx-padding: 5 12; -fx-font-size: 10px; -fx-font-weight: bold;"));
            deleteBtn.setOnMouseExited(ev -> deleteBtn.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-cursor: hand; " +
                    "-fx-background-radius: 5; -fx-padding: 5 12; -fx-font-size: 10px; -fx-font-weight: bold;"));
            actionsBox.getChildren().add(deleteBtn);
        }

        row.getChildren().addAll(
                titreLabel, auteurLabel, contenuLabel, dateLabel,
                likesLabel, dislikesLabel, vuesLabel,
                toxicityContainer, actionsBox
        );

        return row;
    }

    private void banSujet(Sujet sujet) {
        boolean isAlreadyBanned = serviceBan.isUserBanned(sujet.getIdUser());
        Ban existingBan = serviceBan.getActiveBan(sujet.getIdUser());

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

            Label userName = new Label("👤 " + (sujet.isAnonyme() ? "Anonyme" : sujet.getUserName()));
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

            ButtonType modifyBanBtn = new ButtonType("✏ Modifier");
            ButtonType deleteOnlyBtn = new ButtonType("🗑 Supprimer");
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
                modifyBan(existingBan, sujet);
            } else if (result.isPresent() && result.get() == deleteOnlyBtn) {
                deleteSujet(sujet);
            }
            return;
        }

        showBanDialog(sujet, null);
    }

    private void modifyBan(Ban existingBan, Sujet sujet) {
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

        String userName = sujet.isAnonyme() ? "Anonyme" : sujet.getUserName();
        Label headerSubtitle = new Label("Utilisateur: " + userName + " | Sujet #" + sujet.getId());
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

        long currentDays = java.time.temporal.ChronoUnit.DAYS.between(
                LocalDate.now(),
                existingBan.getBanExpiryDate().toLocalDate());

        String expiryText;
        String expiryStyle;
        if (currentDays <= 0) {
            expiryText = "Expiré (était le " + existingBan.getBanExpiryDate() + ")";
            expiryStyle = "-fx-text-fill: #6B7C8D; -fx-font-size: 12px;";
        } else {
            expiryText = existingBan.getBanExpiryDate() + " (" + currentDays + " jours restants)";
            expiryStyle = "-fx-text-fill: #DC2626; -fx-font-size: 12px; -fx-font-weight: bold;";
        }

        Label expiryLabelField = new Label(expiryText);
        expiryLabelField.setStyle(expiryStyle);
        expiryRow.getChildren().addAll(expiryIcon, expiryLabelField);

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
        if (currentDays > 0) {
            durationChoice.setValue((int) currentDays);
        } else {
            durationChoice.setValue(7);
        }
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

                serviceSujet.deleteByAdmin(sujet);

                loadSujets();

                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Succès");
                successAlert.setHeaderText(null);
                successAlert.setContentText("✓ Bannissement modifié avec succès !\n\n" +
                        "🆔 ID du bannissement: " + existingBan.getId() + "\n" +
                        "📅 Nouvelle durée: " + newDuration + " jours\n" +
                        "📝 Nouvelle raison: " + newReason + "\n\n" +
                        "🗑 Le sujet a été supprimé.");

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
    private void showBanDialog(Sujet sujet, Ban existingBan) {
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

        Label headerSubtitle = new Label("Sujet toxique détecté - Score: " +
                String.format("%.0f", sujet.getScoreToxicite() * 100) + "%");
        headerSubtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7C8D;");

        headerText.getChildren().addAll(headerTitle, headerSubtitle);
        headerBox.getChildren().addAll(headerIcon, headerText);

        VBox infoCard = new VBox(8);
        infoCard.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 15; " +
                "-fx-border-color: #E2E8F0; -fx-border-radius: 12; -fx-border-width: 1;");

        Label infoTitle = new Label("👤 Informations");
        infoTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");

        Label userName = new Label("Utilisateur: " + (sujet.isAnonyme() ? "Anonyme" : sujet.getUserName()));
        userName.setStyle("-fx-text-fill: #2C3E50; -fx-font-size: 13px; -fx-font-weight: 600;");

        Label sujetInfo = new Label("📝 Sujet #" + sujet.getId() + " - " + sujet.getTitre());
        if (sujetInfo.getText().length() > 40) {
            sujetInfo.setText(sujetInfo.getText().substring(0, 40) + "...");
        }
        sujetInfo.setStyle("-fx-text-fill: #6B7C8D; -fx-font-size: 12px;");

        infoCard.getChildren().addAll(infoTitle, userName, sujetInfo);

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
        reasonArea.setPromptText("Ex: Publication de contenu toxique (score: " + String.format("%.0f", sujet.getScoreToxicite() * 100) + "%)");
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
                reason = "Publication de contenu toxique (score: " + String.format("%.0f", sujet.getScoreToxicite() * 100) + "%)";
            }

            try {
                serviceBan.banUser(sujet.getIdUser(), reason, duration, adminId);
                serviceSujet.deleteByAdmin(sujet);
                loadSujets();

                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Succès");
                successAlert.setHeaderText(null);
                successAlert.setContentText("✓ Utilisateur banni pour " + duration + " jours !\n\n" +
                        "👤 " + (sujet.isAnonyme() ? "Anonyme" : sujet.getUserName()) + "\n" +
                        "📝 " + reason + "\n\n" +
                        "🗑 Le sujet a été supprimé.");

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

    @FXML
    private void onExportPDF() {
        if (filteredSujets == null || filteredSujets.isEmpty()) {
            showAlert("Export PDF", "Aucun sujet à exporter.", Alert.AlertType.WARNING);
            return;
        }
        services.PDFService.exportSujetsToPDF(filteredSujets, (Stage) navAccueil.getScene().getWindow());
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