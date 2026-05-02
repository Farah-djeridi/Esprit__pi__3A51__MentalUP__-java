package Controllor;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Locale;

import java.util.HashMap;
import java.util.Map;

import models.Sujet;
import services.ServiceSujet;
import services.ServiceCommentaire;
import services.ServiceTraduction;
import services.ServiceVote;
import services.ProfanityFilterService;
import models.User;
import models.Vote;
import utils.SessionManager;

public class ControllerForum {

    @FXML private Label labelDate;
    @FXML private Label badgeRdv;
    @FXML private Label labelUserName;
    @FXML private Label avatarInitials;
    @FXML private Label totalDiscussions;

    @FXML private Button logoutButton;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> orderCombo;

    @FXML private VBox discussionsContainer;
    @FXML private VBox emptyState;
    @FXML private HBox paginationBox;

    @FXML private HBox navAccueil;
    @FXML private HBox navSuivi;
    @FXML private HBox navObjectif;
    @FXML private HBox navForum;
    @FXML private HBox navRendezVous;
    @FXML private HBox navActivites;
    @FXML private HBox navContenu;

    @FXML private javafx.scene.image.ImageView logoImage;

    private ServiceSujet serviceSujet;
    private ServiceCommentaire serviceCommentaire;
    private ServiceVote serviceVote;
    private List<Sujet> allSujets;
    private List<Sujet> filteredSujets;

    private int currentPage = 1;
    private int totalPages = 1;
    private final int itemsPerPage = 5;

    private int currentUserId = 2;

    private ServiceTraduction serviceTraduction;
    private ProfanityFilterService profanityFilter;
    private Map<Integer, String> originalTitres = new HashMap<>();
    private Map<Integer, String> originalContenus = new HashMap<>();
    private Map<Integer, Boolean> isTranslated = new HashMap<>();

    private static final String COLOR_PRIMARY = "#2C5F8A";
    private static final String COLOR_PRIMARY_DARK = "#1E4D7B";
    private static final String COLOR_PRIMARY_LIGHT = "#3A6FA8";
    private static final String COLOR_BG = "#F0F4FA";
    private static final String COLOR_CARD = "rgba(255,255,255,0.85)";
    private static final String COLOR_CARD_HOVER = "rgba(255,255,255,0.95)";
    private static final String COLOR_TEXT_PRIMARY = "#1A2B3C";
    private static final String COLOR_TEXT_SECONDARY = "#6B7C8D";
    private static final String COLOR_TEXT_MUTED = "#94A3B8";
    private static final String COLOR_BORDER = "rgba(226,232,240,0.6)";
    private static final String COLOR_SUCCESS = "#22C55E";
    private static final String COLOR_DANGER = "#EF4444";
    private static final String COLOR_SUCCESS_BG = "rgba(34,197,94,0.1)";
    private static final String COLOR_DANGER_BG = "rgba(239,68,68,0.1)";
    private static final String COLOR_PRIMARY_BG = "rgba(44,95,138,0.08)";

    @FXML
    public void initialize() {
        updateDate();

        // Load user from session
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            this.currentUserId = currentUser.getId();
            labelUserName.setText(currentUser.getPrenom() + " " + currentUser.getNom());
            avatarInitials.setText(getInitials(currentUser.getPrenom() + " " + currentUser.getNom()));
        }

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
        serviceVote = new ServiceVote();
        allSujets = new ArrayList<>();
        filteredSujets = new ArrayList<>();

        profanityFilter = new ProfanityFilterService();

        setupNavigationHoverEffects();
        loadSujetsFromDatabase();
        setupSearchAndFilter();

        badgeRdv.setText(String.valueOf(getRendezVousCount()));

        orderCombo.getItems().addAll("Plus récents", "Plus populaires");
        orderCombo.setValue("Plus récents");

        serviceTraduction = new ServiceTraduction();

        if (!profanityFilter.isApiConfigured()) {
            System.err.println("⚠️ Filtre de mots inappropriés: API non configurée");
        }
    }

    private void updateDate() {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRENCH);
        String formattedDate = today.format(formatter);
        formattedDate = formattedDate.substring(0, 1).toUpperCase() + formattedDate.substring(1);
        labelDate.setText(formattedDate);
    }

    private String getInitials(String fullName) {
        String[] parts = fullName.split(" ");
        if (parts.length >= 2) {
            return parts[0].substring(0, 1) + parts[1].substring(0, 1);
        }
        return fullName.substring(0, Math.min(2, fullName.length())).toUpperCase();
    }

    private void setupNavigationHoverEffects() {
        addHoverEffect(navAccueil);
        addHoverEffect(navSuivi);
        addHoverEffect(navObjectif);
        addHoverEffect(navRendezVous);
        addHoverEffect(navActivites);
        addHoverEffect(navContenu);
    }

    private void addHoverEffect(HBox navItem) {
        if (navItem != null && navItem.getId() != null && !"navForum".equals(navItem.getId())) {
            navItem.setOnMouseEntered(event -> {
                navItem.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-background-radius: 8; -fx-padding: 10 14; -fx-cursor: hand;");
            });
            navItem.setOnMouseExited(event -> {
                navItem.setStyle("-fx-background-color: transparent; -fx-background-radius: 8; -fx-padding: 10 14; -fx-cursor: hand;");
            });
        }
    }

    private void loadSujetsFromDatabase() {
        try {
            allSujets = serviceSujet.getAll();
            totalDiscussions.setText(String.valueOf(allSujets.size()));
            refreshDisplay();
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de charger les discussions: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void setupSearchAndFilter() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            currentPage = 1;
            refreshDisplay();
        });
        orderCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            currentPage = 1;
            refreshDisplay();
        });
    }

    private void refreshDisplay() {
        filterSujets();
        sortSujets();
        updatePagination();
        displayCurrentPage();
    }

    private void filterSujets() {
        String searchText = searchField.getText();
        if (searchText == null || searchText.trim().isEmpty()) {
            filteredSujets = new ArrayList<>(allSujets);
        } else {
            filteredSujets = new ArrayList<>();
            String lowerSearch = searchText.toLowerCase();
            for (Sujet s : allSujets) {
                if (s.getTitre().toLowerCase().contains(lowerSearch) ||
                        s.getContenu().toLowerCase().contains(lowerSearch)) {
                    filteredSujets.add(s);
                }
            }
        }
    }

    private void sortSujets() {
        String order = orderCombo.getValue();
        if (order == null) order = "Plus récents";

        if ("Plus populaires".equals(order)) {
            filteredSujets.sort((a, b) -> Integer.compare(b.getNbLikes(), a.getNbLikes()));
        } else {
            filteredSujets.sort((a, b) -> b.getId() - a.getId());
        }
    }

    private void updatePagination() {
        totalPages = (int) Math.ceil((double) filteredSujets.size() / itemsPerPage);
        if (totalPages < 1) totalPages = 1;
        if (currentPage > totalPages) currentPage = totalPages;
    }

    private void displayCurrentPage() {
        discussionsContainer.getChildren().clear();

        if (filteredSujets.isEmpty()) {
            emptyState.setVisible(true);
            emptyState.setManaged(true);
            paginationBox.setVisible(false);
            paginationBox.setManaged(false);
            return;
        }

        emptyState.setVisible(false);
        emptyState.setManaged(false);

        int start = (currentPage - 1) * itemsPerPage;
        int end = Math.min(start + itemsPerPage, filteredSujets.size());

        for (int i = start; i < end; i++) {
            Sujet sujet = filteredSujets.get(i);
            VBox card = createSujetCard(sujet);

            card.setOpacity(0);
            card.setTranslateY(15);
            FadeTransition ft = new FadeTransition(Duration.millis(350), card);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.setDelay(Duration.millis((i - start) * 80));
            TranslateTransition tt = new TranslateTransition(Duration.millis(350), card);
            tt.setFromY(15);
            tt.setToY(0);
            tt.setDelay(Duration.millis((i - start) * 80));

            discussionsContainer.getChildren().add(card);
            ft.play();
            tt.play();
        }

        updatePaginationControls();
    }

    private VBox createSujetCard(Sujet sujet) {
        VBox card = new VBox(12);
        card.setStyle(
                "-fx-background-color: " + COLOR_CARD + ";" +
                        "-fx-padding: 18 20;" +
                        "-fx-background-radius: 18;" +
                        "-fx-border-color: " + COLOR_BORDER + ";" +
                        "-fx-border-radius: 18;" +
                        "-fx-border-width: 1;"
        );
        card.setCursor(javafx.scene.Cursor.HAND);

        card.setOnMouseEntered(e -> {
            card.setStyle(
                    "-fx-background-color: " + COLOR_CARD_HOVER + ";" +
                            "-fx-padding: 18 20;" +
                            "-fx-background-radius: 18;" +
                            "-fx-border-color: rgba(44,95,138,0.2);" +
                            "-fx-border-radius: 18;" +
                            "-fx-border-width: 1;" +
                            "-fx-effect: dropshadow(gaussian, rgba(44,95,138,0.12), 15, 0, 0, 4);"
            );
        });
        card.setOnMouseExited(e -> {
            card.setStyle(
                    "-fx-background-color: " + COLOR_CARD + ";" +
                            "-fx-padding: 18 20;" +
                            "-fx-background-radius: 18;" +
                            "-fx-border-color: " + COLOR_BORDER + ";" +
                            "-fx-border-radius: 18;" +
                            "-fx-border-width: 1;"
            );
        });
        card.setOnMouseClicked(e -> onSujetClicked(sujet, true));

        HBox header = new HBox(14);
        header.setAlignment(Pos.TOP_LEFT);

        boolean isOwner = sujet.getIdUser() == currentUserId;
        String displayName = sujet.isAnonyme() ? "A" :
                (sujet.getUserName() != null ? sujet.getUserName() : "Utilisateur");

        Label avatar = new Label(sujet.isAnonyme() ? "?" : getInitials(displayName));
        String avatarColor = sujet.isAnonyme() ? "#94A3B8" : COLOR_PRIMARY;
        avatar.setStyle(
                "-fx-background-color: " + avatarColor + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-padding: 0;" +
                        "-fx-background-radius: 50;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 13px;" +
                        "-fx-alignment: center;"
        );
        avatar.setPrefWidth(44);
        avatar.setPrefHeight(44);
        avatar.setMinWidth(44);
        avatar.setMinHeight(44);
        avatar.setAlignment(Pos.CENTER);

        VBox contentBox = new VBox(6);
        HBox.setHgrow(contentBox, Priority.ALWAYS);

        Label title = new Label(sujet.getTitre());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: " + COLOR_TEXT_PRIMARY + ";");
        title.setWrapText(true);
        String authorText;
        if (sujet.isAnonyme()) authorText = "Anonyme";
        else if (isOwner) authorText = "Vous";
        else authorText = sujet.getUserName() != null ? sujet.getUserName() : "Utilisateur";

        HBox metaData = new HBox(8);
        metaData.setAlignment(Pos.CENTER_LEFT);

        Label authorLabel = new Label("Par " + authorText);
        authorLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + COLOR_TEXT_SECONDARY + "; -fx-font-weight: 600;");

        Label dot = new Label("•");
        dot.setStyle("-fx-text-fill: " + COLOR_TEXT_MUTED + ";");

        Label dateValue = new Label(formatDate(sujet.getDateCreation()));
        dateValue.setStyle("-fx-font-size: 12px; -fx-text-fill: " + COLOR_TEXT_SECONDARY + ";");

        metaData.getChildren().addAll(authorLabel, dot, dateValue);

        if (sujet.isEstToxique() && sujet.getScoreToxicite() >= 0.5) {
            Label toxicBadge = new Label("⚠ Toxique " + String.format("(%.0f%%)", sujet.getScoreToxicite() * 100));
            toxicBadge.setStyle(
                    "-fx-background-color: " + COLOR_DANGER_BG + ";" +
                            "-fx-text-fill: " + COLOR_DANGER + ";" +
                            "-fx-font-size: 11px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-padding: 3 10;" +
                            "-fx-background-radius: 20;"
            );
            Label dot2 = new Label("•");
            dot2.setStyle("-fx-text-fill: " + COLOR_TEXT_MUTED + ";");
            metaData.getChildren().addAll(dot2, toxicBadge);
        }

        String contentText = sujet.getContenu();
        if (contentText != null && contentText.length() > 120) {
            contentText = contentText.substring(0, 120) + "...";
        }
        Label content = new Label(contentText);
        content.setWrapText(true);
        content.setStyle("-fx-font-size: 13px; -fx-text-fill: " + COLOR_TEXT_SECONDARY + "; -fx-line-spacing: 2;");

        int commentCount = getCommentCountForSujet(sujet.getId());

        HBox statsAndActions = new HBox();
        statsAndActions.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(statsAndActions, Priority.ALWAYS);

        HBox statsRow = new HBox(8);
        statsRow.setAlignment(Pos.CENTER_LEFT);
        statsRow.setPadding(new Insets(4, 0, 0, 0));

        Label commentsLabel = new Label("💬 " + commentCount);
        commentsLabel.setStyle(
                "-fx-background-color: " + COLOR_PRIMARY_BG + ";" +
                        "-fx-text-fill: " + COLOR_PRIMARY + ";" +
                        "-fx-font-size: 11px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 5 12;" +
                        "-fx-background-radius: 20;"
        );

        Label vuesLabel = new Label("👁 " + sujet.getNbVues());
        vuesLabel.setStyle(
                "-fx-background-color: " + COLOR_SUCCESS_BG + ";" +
                        "-fx-text-fill: " + COLOR_SUCCESS + ";" +
                        "-fx-font-size: 11px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 5 12;" +
                        "-fx-background-radius: 20;"
        );

        statsRow.getChildren().addAll(commentsLabel, vuesLabel);

        HBox actionsBox = new HBox(6);
        Button translateBtn = new Button("🌍 Traduire");
        Button cancelBtn = new Button("↩ Annuler");

        translateBtn.setStyle(
                "-fx-background-color: #E0F2FE;" +
                        "-fx-text-fill: #0369A1;" +
                        "-fx-background-radius: 20;" +
                        "-fx-padding: 5 14;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;"
        );

        cancelBtn.setStyle(
                "-fx-background-color: #F1F5F9;" +
                        "-fx-text-fill: #64748B;" +
                        "-fx-background-radius: 20;" +
                        "-fx-padding: 5 14;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;"
        );

        translateBtn.setOnAction(e -> {
            e.consume();

            try {
                if (!isTranslated.getOrDefault(sujet.getId(), false)) {
                    originalTitres.put(sujet.getId(), sujet.getTitre());
                    originalContenus.put(sujet.getId(), sujet.getContenu());
                }

                String titreTraduit = serviceTraduction.traduire(
                        sujet.getTitre(), "fr", "auto");

                String contenuTraduit = serviceTraduction.traduire(
                        sujet.getContenu(), "fr", "auto");

                if (titreTraduit != null) sujet.setTitre(titreTraduit);
                if (contenuTraduit != null) sujet.setContenu(contenuTraduit);

                isTranslated.put(sujet.getId(), true);

                refreshDisplay();

            } catch (Exception ex) {
                showAlert("Erreur", "Traduction échouée", Alert.AlertType.ERROR);
            }
        });
        cancelBtn.setOnAction(e -> {
            e.consume();

            if (isTranslated.getOrDefault(sujet.getId(), false)) {

                sujet.setTitre(originalTitres.get(sujet.getId()));
                sujet.setContenu(originalContenus.get(sujet.getId()));

                isTranslated.put(sujet.getId(), false);

                refreshDisplay();
            }
        });
        actionsBox.setAlignment(Pos.CENTER_RIGHT);

        Vote userVote = serviceVote.getUserVoteOnSujet(sujet.getId());
        boolean hasLiked = userVote != null && "like".equals(userVote.getType());
        boolean hasDisliked = userVote != null && "dislike".equals(userVote.getType());

        Button likeBtn = new Button("👍 " + sujet.getNbLikes());
        String likeStyle = hasLiked
                ? "-fx-background-color: " + COLOR_SUCCESS + "; -fx-text-fill: white;"
                : "-fx-background-color: transparent; -fx-text-fill: " + COLOR_TEXT_MUTED + ";";
        likeBtn.setStyle(likeStyle +
                "-fx-background-radius: 20; -fx-padding: 5 14; -fx-font-size: 12px; -fx-font-weight: bold; -fx-cursor: hand;");

        likeBtn.setOnMouseEntered(e -> {
            if (!hasLiked)
                likeBtn.setStyle("-fx-background-color: " + COLOR_SUCCESS_BG + "; -fx-text-fill: " + COLOR_SUCCESS +
                        "; -fx-background-radius: 20; -fx-padding: 5 14; -fx-font-size: 12px; -fx-font-weight: bold; -fx-cursor: hand;");
        });
        likeBtn.setOnMouseExited(e -> {
            String resetStyle = hasLiked
                    ? "-fx-background-color: " + COLOR_SUCCESS + "; -fx-text-fill: white;"
                    : "-fx-background-color: transparent; -fx-text-fill: " + COLOR_TEXT_MUTED + ";";
            likeBtn.setStyle(resetStyle +
                    "-fx-background-radius: 20; -fx-padding: 5 14; -fx-font-size: 12px; -fx-font-weight: bold; -fx-cursor: hand;");
        });
        likeBtn.setOnAction(e -> {
            e.consume();
            Vote currentVote = serviceVote.getUserVoteOnSujet(sujet.getId());
            if (currentVote != null && "like".equals(currentVote.getType())) {
                serviceVote.removeVoteFromSujet(sujet.getId());
                sujet.setNbLikes(sujet.getNbLikes() - 1);
            } else {
                serviceVote.voteForSujet(sujet.getId(), "like");
                if (currentVote != null && "dislike".equals(currentVote.getType())) {
                    sujet.setNbDislikes(sujet.getNbDislikes() - 1);
                }
                sujet.setNbLikes(sujet.getNbLikes() + 1);
            }
            refreshDisplay();
        });

        Button dislikeBtn = new Button("👎 " + sujet.getNbDislikes());
        String dislikeStyle = hasDisliked
                ? "-fx-background-color: " + COLOR_DANGER + "; -fx-text-fill: white;"
                : "-fx-background-color: transparent; -fx-text-fill: " + COLOR_TEXT_MUTED + ";";
        dislikeBtn.setStyle(dislikeStyle +
                "-fx-background-radius: 20; -fx-padding: 5 14; -fx-font-size: 12px; -fx-font-weight: bold; -fx-cursor: hand;");

        dislikeBtn.setOnMouseEntered(e -> {
            if (!hasDisliked)
                dislikeBtn.setStyle("-fx-background-color: " + COLOR_DANGER_BG + "; -fx-text-fill: " + COLOR_DANGER +
                        "; -fx-background-radius: 20; -fx-padding: 5 14; -fx-font-size: 12px; -fx-font-weight: bold; -fx-cursor: hand;");
        });
        dislikeBtn.setOnMouseExited(e -> {
            String resetStyle = hasDisliked
                    ? "-fx-background-color: " + COLOR_DANGER + "; -fx-text-fill: white;"
                    : "-fx-background-color: transparent; -fx-text-fill: " + COLOR_TEXT_MUTED + ";";
            dislikeBtn.setStyle(resetStyle +
                    "-fx-background-radius: 20; -fx-padding: 5 14; -fx-font-size: 12px; -fx-font-weight: bold; -fx-cursor: hand;");
        });
        dislikeBtn.setOnAction(e -> {
            e.consume();
            Vote currentVote = serviceVote.getUserVoteOnSujet(sujet.getId());
            if (currentVote != null && "dislike".equals(currentVote.getType())) {
                serviceVote.removeVoteFromSujet(sujet.getId());
                sujet.setNbDislikes(sujet.getNbDislikes() - 1);
            } else {
                serviceVote.voteForSujet(sujet.getId(), "dislike");
                if (currentVote != null && "like".equals(currentVote.getType())) {
                    sujet.setNbLikes(sujet.getNbLikes() - 1);
                }
                sujet.setNbDislikes(sujet.getNbDislikes() + 1);
            }
            refreshDisplay();
        });

        Button commentBtn = new Button("💬 Répondre");
        commentBtn.setStyle(
                "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, " + COLOR_PRIMARY_LIGHT + ", " + COLOR_PRIMARY + ");" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 20;" +
                        "-fx-padding: 6 18;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(44,95,138,0.2), 4, 0, 0, 2);"
        );
        commentBtn.setOnAction(e -> {
            e.consume();
            onSujetClicked(sujet, true);
        });

        if (isTranslated.getOrDefault(sujet.getId(), false)) {
            actionsBox.getChildren().addAll(
                    likeBtn, dislikeBtn, commentBtn, translateBtn, cancelBtn
            );
        } else {
            actionsBox.getChildren().addAll(
                    likeBtn, dislikeBtn, commentBtn, translateBtn
            );
        }

        if (isOwner) {
            MenuButton actionsMenu = new MenuButton("⋮");
            actionsMenu.setStyle(
                    "-fx-background-color: transparent;" +
                            "-fx-text-fill: " + COLOR_TEXT_MUTED + ";" +
                            "-fx-font-size: 16px;" +
                            "-fx-cursor: hand;" +
                            "-fx-padding: 2 6;"
            );

            MenuItem editItem = new MenuItem("✏ Modifier");
            editItem.setOnAction(e -> { e.consume(); editSujet(sujet); });

            MenuItem deleteItem = new MenuItem("🗑 Supprimer");
            deleteItem.setOnAction(e -> { e.consume(); deleteSujet(sujet); });

            actionsMenu.getItems().addAll(editItem, new SeparatorMenuItem(), deleteItem);
            actionsBox.getChildren().add(actionsMenu);
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox bottomRow = new HBox();
        bottomRow.setAlignment(Pos.CENTER);
        bottomRow.getChildren().addAll(statsRow, spacer, actionsBox);

        contentBox.getChildren().addAll(title, metaData, content, bottomRow);
        header.getChildren().addAll(avatar, contentBox);
        card.getChildren().add(header);

        return card;
    }

    private void editSujet(Sujet sujet) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EditSujet.fxml"));
            Parent root = loader.load();
            ControllerEditSujet controller = loader.getController();
            controller.setSujet(sujet, serviceSujet);
            controller.setProfanityFilter(profanityFilter);
            Stage stage = new Stage();
            stage.setTitle("Modifier la discussion");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();
            loadSujetsFromDatabase();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la fenêtre de modification: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void deleteSujet(Sujet sujet) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Supprimer la discussion");
        confirm.setHeaderText(null);
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer cette discussion ? Cette action est irréversible.");

        DialogPane dialogPane = confirm.getDialogPane();
        dialogPane.setStyle("-fx-background-color: " + COLOR_BG + "; -fx-background-radius: 12;");

        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        okButton.setStyle("-fx-background-color: " + COLOR_DANGER + "; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 8; -fx-padding: 8 22; -fx-font-weight: bold;");

        Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
        cancelButton.setStyle("-fx-background-color: transparent; -fx-text-fill: " + COLOR_TEXT_SECONDARY + "; -fx-cursor: hand; -fx-padding: 8 22;");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            serviceSujet.delete(sujet);
            loadSujetsFromDatabase();
            showAlert("Succès", "Discussion supprimée avec succès !", Alert.AlertType.INFORMATION);
        }
    }

    private int getCommentCountForSujet(int sujetId) {
        try {
            return serviceCommentaire.countBySujetId(sujetId);
        } catch (Exception e) {
            return 0;
        }
    }


    private void updatePaginationControls() {
        paginationBox.getChildren().clear();
        paginationBox.setVisible(true);
        paginationBox.setManaged(true);

        if (totalPages <= 1) {
            paginationBox.setVisible(false);
            paginationBox.setManaged(false);
            return;
        }

        Button prevBtn = new Button("← Précédent");
        prevBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + COLOR_PRIMARY +
                "; -fx-cursor: hand; -fx-padding: 6 14; -fx-font-weight: 600; -fx-font-size: 12px;");
        prevBtn.setDisable(currentPage == 1);
        prevBtn.setOnAction(e -> { if (currentPage > 1) { currentPage--; displayCurrentPage(); } });

        HBox pagesBox = new HBox(6);
        pagesBox.setAlignment(Pos.CENTER);

        int startPage = Math.max(1, currentPage - 2);
        int endPage = Math.min(totalPages, startPage + 4);

        if (startPage > 1) {
            pagesBox.getChildren().add(createPageButton(1));
            if (startPage > 2) {
                Label dots = new Label("···");
                dots.setStyle("-fx-text-fill: " + COLOR_TEXT_MUTED + "; -fx-padding: 5;");
                pagesBox.getChildren().add(dots);
            }
        }

        for (int i = startPage; i <= endPage; i++) {
            Button pageBtn = createPageButton(i);
            if (i == currentPage) {
                pageBtn.setStyle(
                        "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, " + COLOR_PRIMARY_LIGHT + ", " + COLOR_PRIMARY + ");" +
                                "-fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 6 14; -fx-font-weight: bold; -fx-font-size: 12px;" +
                                "-fx-effect: dropshadow(gaussian, rgba(44,95,138,0.25), 4, 0, 0, 2);"
                );
            }
            pagesBox.getChildren().add(pageBtn);
        }

        if (endPage < totalPages) {
            if (endPage < totalPages - 1) {
                Label dots = new Label("···");
                dots.setStyle("-fx-text-fill: " + COLOR_TEXT_MUTED + "; -fx-padding: 5;");
                pagesBox.getChildren().add(dots);
            }
            pagesBox.getChildren().add(createPageButton(totalPages));
        }

        Button nextBtn = new Button("Suivant →");
        nextBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + COLOR_PRIMARY +
                "; -fx-cursor: hand; -fx-padding: 6 14; -fx-font-weight: 600; -fx-font-size: 12px;");
        nextBtn.setDisable(currentPage == totalPages);
        nextBtn.setOnAction(e -> { if (currentPage < totalPages) { currentPage++; displayCurrentPage(); } });

        paginationBox.getChildren().addAll(prevBtn, pagesBox, nextBtn);
    }

    private Button createPageButton(int pageNum) {
        Button btn = new Button(String.valueOf(pageNum));
        btn.setStyle("-fx-background-color: " + COLOR_PRIMARY_BG + "; -fx-text-fill: " + COLOR_PRIMARY +
                "; -fx-background-radius: 10; -fx-padding: 6 14; -fx-cursor: hand; -fx-font-size: 12px; -fx-font-weight: 600;");
        btn.setOnAction(e -> { currentPage = pageNum; displayCurrentPage(); });
        return btn;
    }

    private String formatDate(Date date) {
        if (date == null) return "Date inconnue";
        LocalDate localDate = date.toLocalDate();
        LocalDate today = LocalDate.now();

        if (localDate.equals(today)) return "Aujourd'hui";
        else if (localDate.equals(today.minusDays(1))) return "Hier";
        else {
            long daysAgo = java.time.temporal.ChronoUnit.DAYS.between(localDate, today);
            if (daysAgo <= 7) return "Il y a " + daysAgo + " jours";
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return localDate.format(formatter);
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: " + COLOR_BG + ";");

        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        okButton.setStyle("-fx-background-color: " + COLOR_PRIMARY + "; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 8; -fx-padding: 8 22; -fx-font-weight: bold;");

        alert.showAndWait();
    }

    private int getRendezVousCount() {
        return 3;
    }


    @FXML private void onNavHomeClicked() { navigateTo("/Home.fxml"); }
    @FXML private void onNavSuiviClicked() { navigateTo("/Suivi.fxml"); }
    @FXML private void onNavObjectifClicked() { navigateTo("/Objectif.fxml"); }
    @FXML private void onNavForumClicked() { loadSujetsFromDatabase(); }
    @FXML private void onNavRendezVousClicked() { navigateTo("/RendezVous.fxml"); }
    @FXML private void onNavActivitesClicked() { navigateTo("/Activites.fxml"); }
    @FXML private void onNavContenuClicked() { navigateTo("/Contenu.fxml"); }

    @FXML
    private void onNavHoverEnter(javafx.scene.input.MouseEvent event) {
        HBox source = (HBox) event.getSource();
        if (source != null && source.getId() != null && !"navForum".equals(source.getId())) {
            source.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-background-radius: 8; -fx-padding: 10 14; -fx-cursor: hand;");
        }
    }

    @FXML
    private void onNavHoverExit(javafx.scene.input.MouseEvent event) {
        HBox source = (HBox) event.getSource();
        if (source != null && source.getId() != null && !"navForum".equals(source.getId())) {
            source.setStyle("-fx-background-color: transparent; -fx-background-radius: 8; -fx-padding: 10 14; -fx-cursor: hand;");
        }
    }

    @FXML
    private void onNewDiscussion() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/NouvelleDiscussion.fxml"));
            Parent root = loader.load();
            ControllerNouvelleDiscussion controller = loader.getController();
            controller.setServiceSujet(serviceSujet);
            controller.setUserId(currentUserId);
            controller.setProfanityFilter(profanityFilter);
            Stage stage = new Stage();
            stage.setTitle("Nouvelle discussion");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();
            loadSujetsFromDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onFilter() {
        currentPage = 1;
        refreshDisplay();
    }

    private void onSujetClicked(Sujet sujet, boolean incrementVues) {
        try {
            if (incrementVues) {
                serviceSujet.incrementVues(sujet.getId());
                loadSujetsFromDatabase();
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DetailDiscussion.fxml"));
            Parent root = loader.load();
            ControllerDetailDiscussion controller = loader.getController();
            controller.setSujet(sujet);
            controller.setCurrentUserId(currentUserId);
            controller.setProfanityFilter(profanityFilter);
            Stage stage = (Stage) navForum.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la discussion", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void onNotifications() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Notifications.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Notifications");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onLogout() {
        try {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Déconnexion");
            confirm.setHeaderText(null);
            confirm.setContentText("Voulez-vous vraiment vous déconnecter ?");

            DialogPane dialogPane = confirm.getDialogPane();
            dialogPane.setStyle("-fx-background-color: " + COLOR_BG + ";");

            Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
            okButton.setStyle("-fx-background-color: " + COLOR_PRIMARY + "; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 8; -fx-font-weight: bold;");

            Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
            cancelButton.setStyle("-fx-background-color: transparent; -fx-text-fill: " + COLOR_TEXT_SECONDARY + "; -fx-cursor: hand;");

            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                SessionManager.getInstance().logout();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Login.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) logoutButton.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("MentalUp - Connexion");
                stage.show();
            }
        } catch (IOException e) {
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
        }
    }
}
