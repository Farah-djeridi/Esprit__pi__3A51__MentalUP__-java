package Controllors;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Locale;

import models.Commentaire;
import models.Sujet;
import models.Vote;
import services.*;

public class ControllerDetailDiscussion {

    @FXML private Label labelDate;
    @FXML private Label badgeRdv;
    @FXML private Label labelUserName;
    @FXML private Label avatarInitials;
    @FXML private Label userRoleLabel;

    @FXML private Label sujetTitre;
    @FXML private Label sujetAuteur;
    @FXML private Label sujetDate;
    @FXML private Label sujetContenu;
    @FXML private Label sujetAvatar;
    @FXML private Label nbVuesLabel;

    @FXML private Button likeButton;
    @FXML private Button dislikeButton;
    @FXML private Button translateSujetBtn;
    @FXML private Button cancelTranslateSujetBtn;

    @FXML private MenuButton topicMenuButton;

    @FXML private VBox commentsContainer;
    @FXML private VBox emptyCommentsState;
    @FXML private ScrollPane commentsScrollPane;
    @FXML private HBox commentPaginationBox;
    @FXML private Label totalCommentsLabel;

    @FXML private CheckBox anonymeCheckBox;
    @FXML private TextArea commentTextArea;
    @FXML private Button submitCommentButton;

    @FXML private Button logoutButton;
    @FXML private Button notifButton;

    // Navigation
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
    private ServiceTraduction serviceTraduction;
    private ToxicityAnalysisService toxicityService;

    private Sujet currentSujet;
    private List<Commentaire> allCommentaires;

    private int currentUserId = 2;
    private String currentUserName = "";
    private String currentUserInitials = "";

    private int currentCommentPage = 1;
    private int totalCommentPages = 1;
    private final int commentsPerPage = 3;

    // Variables pour la traduction
    private boolean isSujetTranslated = false;
    private String originalSujetTitre;
    private String originalSujetContenu;
    private Map<Integer, String> originalCommentContents = new HashMap<>();
    private Map<Integer, Boolean> isCommentTranslated = new HashMap<>();

    private ProfanityFilterService profanityFilter;
    private ServiceBan serviceBan;

    private static final String COLOR_PRIMARY = "#2C5F8A";
    private static final String COLOR_PRIMARY_DARK = "#1E4D7B";
    private static final String COLOR_PRIMARY_LIGHT = "#3A6FA8";
    private static final String COLOR_BG = "#F0F4FA";
    private static final String COLOR_CARD = "rgba(255,255,255,0.85)";
    private static final String COLOR_TEXT_PRIMARY = "#1A2B3C";
    private static final String COLOR_TEXT_SECONDARY = "#6B7C8D";
    private static final String COLOR_TEXT_MUTED = "#94A3B8";
    private static final String COLOR_BORDER = "rgba(226,232,240,0.6)";
    private static final String COLOR_SUCCESS = "#22C55E";
    private static final String COLOR_DANGER = "#EF4444";

    private static final String COLOR_WARNING_BG = "rgba(239,68,68,0.1)";
    private static final String COLOR_WARNING = "#EF4444";

    @FXML
    public void initialize() {
        updateDate();

        serviceCommentaire = new ServiceCommentaire();
        serviceTraduction = new ServiceTraduction();
        toxicityService = new ToxicityAnalysisService();
        serviceBan = new ServiceBan();

        loadCurrentUserInfo();

        try {
            Image logo = new Image(getClass().getResourceAsStream("/images/logo.png"));
            if (logo != null) {
                logoImage.setImage(logo);
            }
        } catch (Exception e) {
            System.err.println("Logo non trouvé, utilisation du texte par défaut");
        }

        labelUserName.setText(currentUserName);
        avatarInitials.setText(currentUserInitials);

        serviceSujet = new ServiceSujet();
        serviceVote = new ServiceVote();
        allCommentaires = new ArrayList<>();

        setupNavigationHoverEffects();

        submitCommentButton.setDisable(true);
        commentTextArea.textProperty().addListener((obs, old, newVal) ->
                submitCommentButton.setDisable(newVal == null || newVal.trim().isEmpty()));

        badgeRdv.setText("3");

        cancelTranslateSujetBtn = new Button("↩ Annuler");
        cancelTranslateSujetBtn.setStyle(
                "-fx-background-color: #F1F5F9;" +
                        "-fx-text-fill: #64748B;" +
                        "-fx-background-radius: 20;" +
                        "-fx-padding: 5 14;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-cursor: hand;"
        );
        cancelTranslateSujetBtn.setOnAction(e -> annulerTraductionSujet());
        cancelTranslateSujetBtn.setVisible(false);
    }

    public void setProfanityFilter(ProfanityFilterService filter) {
        this.profanityFilter = filter;
    }

    private void loadCurrentUserInfo() {
        currentUserName = serviceCommentaire.getUserNameById(currentUserId);
        currentUserInitials = serviceCommentaire.getUserInitialsById(currentUserId);
    }

    private void updateDate() {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRENCH);
        String formattedDate = today.format(formatter);
        formattedDate = formattedDate.substring(0, 1).toUpperCase() + formattedDate.substring(1);
        labelDate.setText(formattedDate);
    }

    private String getInitials(String fullName) {
        if (fullName == null || fullName.isEmpty()) return "U";
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

    public void setSujet(Sujet sujet) {
        this.currentSujet = sujet;
        loadSujetDetails();
        loadCommentaires();
    }

    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
        loadCurrentUserInfo();
        labelUserName.setText(currentUserName);
        avatarInitials.setText(currentUserInitials);
    }

    private void loadSujetDetails() {
        originalSujetTitre = currentSujet.getTitre();
        originalSujetContenu = currentSujet.getContenu();
        isSujetTranslated = false;

        sujetTitre.setText(currentSujet.getTitre());
        sujetContenu.setText(currentSujet.getContenu());
        sujetContenu.getParent().setStyle(
                "-fx-background-color: white;" +
                        "-fx-padding: 20;" +
                        "-fx-background-radius: 12;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10,0,0,2);"
        );

        boolean isOwner = currentSujet.getIdUser() == currentUserId;

        String authorText;
        String avatarText;
        String avatarColor;

        if (currentSujet.isAnonyme()) {
            authorText = "Anonyme";
            avatarText = "A";
            avatarColor = "#8E9EAB";
        } else if (isOwner) {
            authorText = "Vous";
            avatarText = currentUserInitials;
            avatarColor = "#2C5F8A";
        } else {
            String userName = currentSujet.getUserName();
            if (userName == null || userName.isEmpty()) {
                authorText = "Utilisateur";
                avatarText = "U";
            } else {
                authorText = userName;
                avatarText = getInitials(userName);
            }
            avatarColor = "#2C5F8A";
        }

        sujetAuteur.setText("Par " + authorText);
        sujetAvatar.setText(avatarText);
        sujetAvatar.setStyle("-fx-background-color: " + avatarColor + "; -fx-text-fill: white; -fx-font-weight: bold;");

        sujetDate.setText(formatDate(currentSujet.getDateCreation()));
        nbVuesLabel.setText("👁 " + currentSujet.getNbVues());
        nbVuesLabel.setStyle(
                "-fx-background-color: #27AE6022;" +
                        "-fx-text-fill: #27AE60;" +
                        "-fx-padding: 5 12;" +
                        "-fx-background-radius: 20;" +
                        "-fx-font-weight: bold;"
        );
        updateLikeDislikeButtons();

        topicMenuButton.setVisible(isOwner);
        topicMenuButton.setManaged(isOwner);
        topicMenuButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 15px; -fx-text-fill: " + COLOR_TEXT_MUTED + ";");

        if (isOwner) {
            topicMenuButton.getItems().clear();

            MenuItem editItem = new MenuItem("✏ Modifier");
            editItem.setStyle("-fx-padding: 8 16;");
            editItem.setOnAction(e -> onEditSujet());

            MenuItem deleteItem = new MenuItem("🗑 Supprimer");
            deleteItem.setStyle("-fx-text-fill: " + COLOR_DANGER + "; -fx-padding: 8 16;");
            deleteItem.setOnAction(e -> onDeleteSujet());

            topicMenuButton.getItems().addAll(editItem, new SeparatorMenuItem(), deleteItem);
        }

        translateSujetBtn.setOnAction(e -> traduireSujet());
    }

    private void traduireSujet() {
        try {
            String titreTraduit = serviceTraduction.traduire(originalSujetTitre, "fr", "auto");
            String contenuTraduit = serviceTraduction.traduire(originalSujetContenu, "fr", "auto");

            if (titreTraduit != null && contenuTraduit != null) {
                sujetTitre.setText(titreTraduit);
                sujetContenu.setText(contenuTraduit);
                isSujetTranslated = true;

                translateSujetBtn.setVisible(false);
                cancelTranslateSujetBtn.setVisible(true);

                javafx.scene.Parent parent = translateSujetBtn.getParent();
                if (parent instanceof HBox) {
                    HBox hboxParent = (HBox) parent;
                    if (!hboxParent.getChildren().contains(cancelTranslateSujetBtn)) {
                        hboxParent.getChildren().add(cancelTranslateSujetBtn);
                    }
                } else if (parent instanceof VBox) {
                    VBox vboxParent = (VBox) parent;
                    if (!vboxParent.getChildren().contains(cancelTranslateSujetBtn)) {
                        vboxParent.getChildren().add(cancelTranslateSujetBtn);
                    }
                }

            }} catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la traduction: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void annulerTraductionSujet() {
        if (originalSujetTitre != null && originalSujetContenu != null) {
            sujetTitre.setText(originalSujetTitre);
            sujetContenu.setText(originalSujetContenu);
            isSujetTranslated = false;

            translateSujetBtn.setVisible(true);
            cancelTranslateSujetBtn.setVisible(false);

            showAlert("Information", "Retour au texte original", Alert.AlertType.INFORMATION);
        }
    }

    private void traduireCommentaire(Commentaire commentaire, Label contentLabel, Button translateBtn, Button cancelBtn) {
        try {
            if (!originalCommentContents.containsKey(commentaire.getId())) {
                originalCommentContents.put(commentaire.getId(), commentaire.getContenu());
            }

            String contenuTraduit = serviceTraduction.traduire(commentaire.getContenu(), "fr", "auto");

            if (contenuTraduit != null) {
                contentLabel.setText(contenuTraduit);
                isCommentTranslated.put(commentaire.getId(), true);

                translateBtn.setVisible(false);
                cancelBtn.setVisible(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la traduction du commentaire", Alert.AlertType.ERROR);
        }
    }

    private void annulerTraductionCommentaire(Commentaire commentaire, Label contentLabel, Button translateBtn, Button cancelBtn) {
        String original = originalCommentContents.get(commentaire.getId());
        if (original != null) {
            contentLabel.setText(original);
            isCommentTranslated.put(commentaire.getId(), false);

            translateBtn.setVisible(true);
            cancelBtn.setVisible(false);
        }
    }

    private void updateLikeDislikeButtons() {
        Vote userVote = serviceVote.getUserVoteOnSujet(currentSujet.getId());
        boolean hasLiked = userVote != null && "like".equals(userVote.getType());
        boolean hasDisliked = userVote != null && "dislike".equals(userVote.getType());

        likeButton.setText("👍 " + currentSujet.getNbLikes());
        likeButton.setStyle(
                "-fx-background-color: " + (hasLiked ? "#27AE60" : "transparent") + ";" +
                        "-fx-text-fill: " + (hasLiked ? "white" : "#7F8C8D") + ";" +
                        "-fx-padding: 6 14;" +
                        "-fx-background-radius: 20;" +
                        "-fx-cursor: hand;"
        );

        dislikeButton.setText("👎 " + currentSujet.getNbDislikes());
        dislikeButton.setStyle(
                "-fx-background-color: " + (hasDisliked ? "#E74C3C" : "transparent") + ";" +
                        "-fx-text-fill: " + (hasDisliked ? "white" : "#7F8C8D") + ";" +
                        "-fx-padding: 6 14;" +
                        "-fx-background-radius: 20;" +
                        "-fx-cursor: hand;"
        );

        likeButton.setOnMouseEntered(e -> {
            if (!hasLiked)
                likeButton.setStyle("-fx-background-color: #E8F8F5; -fx-text-fill: #27AE60; -fx-padding: 6 14; -fx-background-radius: 20;");
        });

        likeButton.setOnMouseExited(e -> updateLikeDislikeButtons());

        dislikeButton.setOnMouseEntered(e -> {
            if (!hasDisliked)
                dislikeButton.setStyle("-fx-background-color: #FDEDEC; -fx-text-fill: #E74C3C; -fx-padding: 6 14; -fx-background-radius: 20;");
        });

        dislikeButton.setOnMouseExited(e -> updateLikeDislikeButtons());

        likeButton.setOnAction(e -> handleLike());
        dislikeButton.setOnAction(e -> handleDislike());
    }

    private void handleLike() {
        Vote currentVote = serviceVote.getUserVoteOnSujet(currentSujet.getId());

        if (currentVote != null && "like".equals(currentVote.getType())) {
            serviceVote.removeVoteFromSujet(currentSujet.getId());
            currentSujet.setNbLikes(currentSujet.getNbLikes() - 1);
        } else {
            serviceVote.voteForSujet(currentSujet.getId(), "like");
            if (currentVote != null && "dislike".equals(currentVote.getType())) {
                currentSujet.setNbDislikes(currentSujet.getNbDislikes() - 1);
                currentSujet.setNbLikes(currentSujet.getNbLikes() + 1);
            } else {
                currentSujet.setNbLikes(currentSujet.getNbLikes() + 1);
            }
        }
        updateLikeDislikeButtons();
    }

    private void handleDislike() {
        Vote currentVote = serviceVote.getUserVoteOnSujet(currentSujet.getId());

        if (currentVote != null && "dislike".equals(currentVote.getType())) {
            serviceVote.removeVoteFromSujet(currentSujet.getId());
            currentSujet.setNbDislikes(currentSujet.getNbDislikes() - 1);
        } else {
            serviceVote.voteForSujet(currentSujet.getId(), "dislike");
            if (currentVote != null && "like".equals(currentVote.getType())) {
                currentSujet.setNbLikes(currentSujet.getNbLikes() - 1);
                currentSujet.setNbDislikes(currentSujet.getNbDislikes() + 1);
            } else {
                currentSujet.setNbDislikes(currentSujet.getNbDislikes() + 1);
            }
        }
        updateLikeDislikeButtons();
    }

    private void loadCommentaires() {
        allCommentaires = serviceCommentaire.getBySujetId(currentSujet.getId());
        currentCommentPage = 1;
        displayCommentaires();
    }

    private void displayCommentaires() {
        commentsContainer.getChildren().clear();

        if (allCommentaires == null || allCommentaires.isEmpty()) {
            emptyCommentsState.setVisible(true);
            emptyCommentsState.setManaged(true);
            commentPaginationBox.setVisible(false);
            commentPaginationBox.setManaged(false);
            if (totalCommentsLabel != null) {
                totalCommentsLabel.setText("0 commentaire");
            }
            return;
        }

        emptyCommentsState.setVisible(false);
        emptyCommentsState.setManaged(false);

        if (totalCommentsLabel != null) {
            totalCommentsLabel.setText(allCommentaires.size() + " commentaire" + (allCommentaires.size() > 1 ? "s" : ""));
        }

        totalCommentPages = (int) Math.ceil((double) allCommentaires.size() / commentsPerPage);
        if (totalCommentPages < 1) totalCommentPages = 1;
        if (currentCommentPage > totalCommentPages) currentCommentPage = totalCommentPages;

        int start = (currentCommentPage - 1) * commentsPerPage;
        int end = Math.min(start + commentsPerPage, allCommentaires.size());

        for (int i = start; i < end; i++) {
            Commentaire commentaire = allCommentaires.get(i);
            commentsContainer.getChildren().add(createCommentCard(commentaire));
        }

        updateCommentPaginationControls();

        if (commentsScrollPane != null) {
            commentsScrollPane.setVvalue(0);
        }
    }

    private void updateCommentPaginationControls() {
        if (commentPaginationBox == null) return;

        commentPaginationBox.getChildren().clear();

        if (totalCommentPages <= 1) {
            commentPaginationBox.setVisible(false);
            commentPaginationBox.setManaged(false);
            return;
        }

        commentPaginationBox.setVisible(true);
        commentPaginationBox.setManaged(true);

        Button prevBtn = new Button("◀");
        prevBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #2C5F8A; -fx-cursor: hand; -fx-padding: 5 10; -fx-font-size: 13px;");
        prevBtn.setDisable(currentCommentPage == 1);
        prevBtn.setOnAction(e -> {
            if (currentCommentPage > 1) {
                currentCommentPage--;
                displayCommentaires();
            }
        });

        HBox pagesBox = new HBox(5);
        pagesBox.setAlignment(Pos.CENTER);

        int startPage = Math.max(1, currentCommentPage - 2);
        int endPage = Math.min(totalCommentPages, startPage + 4);

        if (startPage > 1) {
            Button firstBtn = createCommentPageButton(1);
            pagesBox.getChildren().add(firstBtn);
            if (startPage > 2) {
                Label dots = new Label("...");
                dots.setStyle("-fx-text-fill: #7F8C8D; -fx-padding: 5; -fx-font-size: 12px;");
                pagesBox.getChildren().add(dots);
            }
        }

        for (int i = startPage; i <= endPage; i++) {
            Button pageBtn = createCommentPageButton(i);
            if (i == currentCommentPage) {
                pageBtn.setStyle("-fx-background-color: #2C5F8A; -fx-text-fill: white; " +
                        "-fx-background-radius: 8; -fx-padding: 5 12; -fx-font-weight: bold; -fx-font-size: 13px;");
            }
            pagesBox.getChildren().add(pageBtn);
        }

        if (endPage < totalCommentPages) {
            if (endPage < totalCommentPages - 1) {
                Label dots = new Label("...");
                dots.setStyle("-fx-text-fill: #7F8C8D; -fx-padding: 5; -fx-font-size: 12px;");
                pagesBox.getChildren().add(dots);
            }
            Button lastBtn = createCommentPageButton(totalCommentPages);
            pagesBox.getChildren().add(lastBtn);
        }

        Button nextBtn = new Button("▶");
        nextBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #2C5F8A; -fx-cursor: hand; -fx-padding: 5 10; -fx-font-size: 13px;");
        nextBtn.setDisable(currentCommentPage == totalCommentPages);
        nextBtn.setOnAction(e -> {
            if (currentCommentPage < totalCommentPages) {
                currentCommentPage++;
                displayCommentaires();
            }
        });

        commentPaginationBox.getChildren().addAll(prevBtn, pagesBox, nextBtn);
    }

    private Button createCommentPageButton(int pageNum) {
        Button btn = new Button(String.valueOf(pageNum));
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #2C5F8A; " +
                "-fx-background-radius: 8; -fx-padding: 5 12; -fx-cursor: hand; -fx-font-size: 13px;");
        btn.setOnAction(e -> {
            currentCommentPage = pageNum;
            displayCommentaires();
        });
        return btn;
    }

    private double analyzeCommentToxicity(String content) {
        if (content == null || content.isEmpty()) return 0.0;
        double score = toxicityService.analyze(content);
        System.out.println("📊 Analyse commentaire - Score: " + score + " - Texte: " + content);
        return score;
    }

    private VBox createCommentCard(Commentaire commentaire) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: " + COLOR_CARD + "; -fx-padding: 16; -fx-background-radius: 18; " +
                "-fx-border-color: " + COLOR_BORDER + "; -fx-border-radius: 18; -fx-border-width: 1;");
        card.setCursor(javafx.scene.Cursor.HAND);

        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-background-color: rgba(255,255,255,0.95); -fx-padding: 16; -fx-background-radius: 18; " +
                    "-fx-border-color: rgba(44,95,138,0.2); -fx-border-radius: 18; -fx-border-width: 1; " +
                    "-fx-effect: dropshadow(gaussian, rgba(44,95,138,0.12), 15, 0, 0, 4);");
        });
        card.setOnMouseExited(e -> {
            card.setStyle("-fx-background-color: " + COLOR_CARD + "; -fx-padding: 16; -fx-background-radius: 18; " +
                    "-fx-border-color: " + COLOR_BORDER + "; -fx-border-radius: 18; -fx-border-width: 1;");
        });

        HBox header = new HBox(12);

        boolean isOwner = commentaire.getUserId() == currentUserId;
        String displayName = commentaire.isAnonyme() ? "A" :
                (commentaire.getUserName() != null ? commentaire.getUserName() : "Utilisateur");

        Label avatar = new Label(commentaire.isAnonyme() ? "?" : getInitials(displayName));
        String avatarColor = commentaire.isAnonyme() ? COLOR_TEXT_MUTED : COLOR_PRIMARY;
        avatar.setStyle("-fx-background-color: " + avatarColor + "; -fx-text-fill: white; -fx-padding: 0; " +
                "-fx-background-radius: 50; -fx-font-weight: bold; -fx-font-size: 13px; -fx-alignment: center;");
        avatar.setPrefWidth(40);
        avatar.setPrefHeight(40);
        avatar.setAlignment(Pos.CENTER);

        VBox contentBox = new VBox(5);
        HBox.setHgrow(contentBox, Priority.ALWAYS);

        String authorText;
        if (commentaire.isAnonyme()) authorText = "Anonyme";
        else if (isOwner) authorText = "Vous";
        else authorText = commentaire.getUserName() != null ? commentaire.getUserName() : "Utilisateur";

        HBox metaData = new HBox(8);
        Label authorLabel = new Label("Par " + authorText);
        authorLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + COLOR_TEXT_SECONDARY + "; -fx-font-weight: 600;");
        Label dot = new Label("•");
        dot.setStyle("-fx-text-fill: " + COLOR_TEXT_MUTED + ";");
        Label dateValue = new Label(formatDate(commentaire.getDateCommentaire()));
        dateValue.setStyle("-fx-font-size: 12px; -fx-text-fill: " + COLOR_TEXT_SECONDARY + ";");
        metaData.getChildren().addAll(authorLabel, dot, dateValue);

        if (commentaire.isEstToxique() && commentaire.getScoreToxicite() >= 0.7) {
            Label toxicBadge = new Label("⚠ Toxique " + String.format("(%.0f%%)", commentaire.getScoreToxicite() * 100));
            toxicBadge.setStyle(
                    "-fx-background-color: " + COLOR_WARNING_BG + ";" +
                            "-fx-text-fill: " + COLOR_WARNING + ";" +
                            "-fx-font-size: 10px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-padding: 2 8;" +
                            "-fx-background-radius: 12;"
            );
            Label dot2 = new Label("•");
            dot2.setStyle("-fx-text-fill: " + COLOR_TEXT_MUTED + ";");
            metaData.getChildren().addAll(dot2, toxicBadge);
        }

        Label content = new Label(commentaire.getContenu());
        content.setWrapText(true);
        content.setStyle("-fx-font-size: 13px; -fx-text-fill: #4A5A6A; -fx-line-spacing: 2;");

        contentBox.getChildren().addAll(metaData, content);
        header.getChildren().addAll(avatar, contentBox);

        HBox actionsBox = new HBox(8);
        actionsBox.setAlignment(Pos.CENTER_RIGHT);
        actionsBox.setStyle("-fx-padding: 8 0 0 0;");

        Vote userVote = serviceVote.getUserVoteOnCommentaire(commentaire.getId());
        boolean hasLiked = userVote != null && "like".equals(userVote.getType());
        boolean hasDisliked = userVote != null && "dislike".equals(userVote.getType());

        Button likeBtn = new Button("👍 " + commentaire.getNbLikes());
        likeBtn.setStyle("-fx-background-color: " + (hasLiked ? COLOR_SUCCESS : "transparent") +
                "; -fx-text-fill: " + (hasLiked ? "white" : COLOR_TEXT_MUTED) +
                "; -fx-background-radius: 20; -fx-padding: 5 12; -fx-font-size: 12px; -fx-font-weight: bold; -fx-cursor: hand;");

        likeBtn.setOnAction(e -> {
            e.consume();
            Vote currentVote = serviceVote.getUserVoteOnCommentaire(commentaire.getId());
            if (currentVote != null && "like".equals(currentVote.getType())) {
                serviceVote.removeVoteFromCommentaire(commentaire.getId());
                commentaire.setNbLikes(commentaire.getNbLikes() - 1);
            } else {
                serviceVote.voteForCommentaire(commentaire.getId(), "like");
                if (currentVote != null && "dislike".equals(currentVote.getType())) {
                    commentaire.setNbDislikes(commentaire.getNbDislikes() - 1);
                    commentaire.setNbLikes(commentaire.getNbLikes() + 1);
                } else {
                    commentaire.setNbLikes(commentaire.getNbLikes() + 1);
                }
            }
            loadCommentaires();
        });

        Button dislikeBtn = new Button("👎 " + commentaire.getNbDislikes());
        dislikeBtn.setStyle("-fx-background-color: " + (hasDisliked ? COLOR_DANGER : "transparent") +
                "; -fx-text-fill: " + (hasDisliked ? "white" : COLOR_TEXT_MUTED) +
                "; -fx-background-radius: 20; -fx-padding: 5 12; -fx-font-size: 12px; -fx-font-weight: bold; -fx-cursor: hand;");

        dislikeBtn.setOnAction(e -> {
            e.consume();
            Vote currentVote = serviceVote.getUserVoteOnCommentaire(commentaire.getId());
            if (currentVote != null && "dislike".equals(currentVote.getType())) {
                serviceVote.removeVoteFromCommentaire(commentaire.getId());
                commentaire.setNbDislikes(commentaire.getNbDislikes() - 1);
            } else {
                serviceVote.voteForCommentaire(commentaire.getId(), "dislike");
                if (currentVote != null && "like".equals(currentVote.getType())) {
                    commentaire.setNbLikes(commentaire.getNbLikes() - 1);
                    commentaire.setNbDislikes(commentaire.getNbDislikes() + 1);
                } else {
                    commentaire.setNbDislikes(commentaire.getNbDislikes() + 1);
                }
            }
            loadCommentaires();
        });

        Button translateCommentBtn = new Button("🌍 Traduire");
        translateCommentBtn.setStyle(
                "-fx-background-color: #E0F2FE;" +
                        "-fx-text-fill: #0369A1;" +
                        "-fx-background-radius: 20;" +
                        "-fx-padding: 5 12;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-cursor: hand;"
        );

        Button cancelTranslateCommentBtn = new Button("↩ Annuler");
        cancelTranslateCommentBtn.setStyle(
                "-fx-background-color: #F1F5F9;" +
                        "-fx-text-fill: #64748B;" +
                        "-fx-background-radius: 20;" +
                        "-fx-padding: 5 12;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-cursor: hand;"
        );
        cancelTranslateCommentBtn.setVisible(false);

        translateCommentBtn.setOnAction(e -> {
            e.consume();
            traduireCommentaire(commentaire, content, translateCommentBtn, cancelTranslateCommentBtn);
        });

        cancelTranslateCommentBtn.setOnAction(e -> {
            e.consume();
            annulerTraductionCommentaire(commentaire, content, translateCommentBtn, cancelTranslateCommentBtn);
        });

        actionsBox.getChildren().addAll(likeBtn, dislikeBtn, translateCommentBtn, cancelTranslateCommentBtn);

        if (isOwner) {
            MenuButton actionsMenu = new MenuButton("⋮");
            actionsMenu.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 13px; -fx-text-fill: " + COLOR_TEXT_MUTED + ";");
            MenuItem editItem = new MenuItem("✏ Modifier");
            editItem.setOnAction(e -> editComment(commentaire, content, actionsMenu));
            MenuItem deleteItem = new MenuItem("🗑 Supprimer");
            deleteItem.setStyle("-fx-text-fill: " + COLOR_DANGER + ";");
            deleteItem.setOnAction(e -> deleteComment(commentaire));
            actionsMenu.getItems().addAll(editItem, new SeparatorMenuItem(), deleteItem);
            actionsBox.getChildren().add(actionsMenu);
        }

        card.getChildren().addAll(header, actionsBox);
        return card;
    }

    private boolean validateCommentContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            showAlert("Erreur", "Le commentaire ne peut pas être vide", Alert.AlertType.WARNING);
            return false;
        }
        if (content.length() < 3) {
            showAlert("Erreur", "Le commentaire doit contenir au moins 3 caractères", Alert.AlertType.WARNING);
            return false;
        }
        if (content.length() > 1000) {
            showAlert("Erreur", "Le commentaire ne peut pas dépasser 1000 caractères", Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }



    private void editComment(Commentaire commentaire, Label contentLabel, MenuButton menuButton) {
        TextArea editArea = new TextArea(commentaire.getContenu());
        editArea.setWrapText(true);
        editArea.setStyle("-fx-padding: 8; -fx-background-radius: 8; -fx-border-color: #E8EEF4; -fx-border-radius: 8;");
        editArea.setPrefHeight(100);

        editArea.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.length() > 1000) {
                editArea.setText(oldVal);
                showAlert("Attention", "Le commentaire ne peut pas dépasser 1000 caractères", Alert.AlertType.WARNING);
            }
        });

        VBox parent = (VBox) contentLabel.getParent();
        int index = parent.getChildren().indexOf(contentLabel);
        parent.getChildren().set(index, editArea);

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setStyle("-fx-padding: 10 0 0 0;");

        Button cancelButton = new Button("Annuler");
        cancelButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #7F8C8D; -fx-cursor: hand; -fx-padding: 6 15; -fx-background-radius: 20;");
        cancelButton.setOnAction(e -> {
            parent.getChildren().set(index, contentLabel);
            buttonBox.setVisible(false);
            recreateCommentMenu(commentaire, contentLabel, menuButton);
            menuButton.setVisible(true);
        });

        Button saveButton = new Button("✓ Sauvegarder");
        saveButton.setStyle("-fx-background-color: #2C5F8A; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 6 20; -fx-background-radius: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 1);");
        saveButton.setOnAction(e -> {
            String newText = editArea.getText().trim();

            if (newText.isEmpty()) {
                showAlert("Erreur", "Le commentaire ne peut pas être vide", Alert.AlertType.WARNING);
                return;
            }
            if (newText.length() < 3) {
                showAlert("Erreur", "Le commentaire doit contenir au moins 3 caractères", Alert.AlertType.WARNING);
                return;
            }
            if (newText.length() > 1000) {
                showAlert("Erreur", "Le commentaire ne peut pas dépasser 1000 caractères", Alert.AlertType.WARNING);
                return;
            }

            if (profanityFilter != null) {
                try {
                    profanityFilter.validateText(newText, "commentaire");
                } catch (IllegalArgumentException ex) {
                    showAlert("Contenu inapproprié", ex.getMessage(), Alert.AlertType.WARNING);
                    return;
                }
            }

            if (newText.equals(commentaire.getContenu())) {
                showAlert("Information", "Aucune modification détectée", Alert.AlertType.INFORMATION);
                parent.getChildren().set(index, contentLabel);
                buttonBox.setVisible(false);
                recreateCommentMenu(commentaire, contentLabel, menuButton);
                menuButton.setVisible(true);
                return;
            }

            double scoreToxicite = analyzeCommentToxicity(newText);
            boolean estToxique = scoreToxicite >= 0.7;

            commentaire.setContenu(newText);
            commentaire.setScoreToxicite(scoreToxicite);
            commentaire.setEstToxique(estToxique);

            serviceCommentaire.update(commentaire);

            contentLabel.setText(newText);
            parent.getChildren().set(index, contentLabel);
            buttonBox.setVisible(false);
            recreateCommentMenu(commentaire, contentLabel, menuButton);
            menuButton.setVisible(true);

            showAlert("Succès", "Commentaire modifié avec succès !", Alert.AlertType.INFORMATION);


            loadCommentaires();
        });

        buttonBox.getChildren().addAll(cancelButton, saveButton);

        VBox parentOfParent = (VBox) editArea.getParent();
        int buttonIndex = parentOfParent.getChildren().indexOf(editArea) + 1;
        parentOfParent.getChildren().add(buttonIndex, buttonBox);

        menuButton.setVisible(false);
    }

    private void recreateCommentMenu(Commentaire commentaire, Label contentLabel, MenuButton menuButton) {
        menuButton.getItems().clear();

        MenuItem editItem = new MenuItem("✏ Modifier");
        editItem.setOnAction(e -> {
            editComment(commentaire, contentLabel, menuButton);
        });

        MenuItem deleteItem = new MenuItem("🗑 Supprimer");
        deleteItem.setStyle("-fx-text-fill: #E74C3C;");
        deleteItem.setOnAction(e -> deleteComment(commentaire));

        menuButton.getItems().addAll(editItem, new SeparatorMenuItem(), deleteItem);
        menuButton.setVisible(true);
    }

    private void deleteComment(Commentaire commentaire) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Supprimer le commentaire");
        confirm.setHeaderText(null);
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer ce commentaire ?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            serviceCommentaire.delete(commentaire);
            loadCommentaires();
            showAlert("Succès", "Commentaire supprimé !", Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    private void onSubmitComment() {
        String contenu = commentTextArea.getText().trim();

        // Vérifier si l'utilisateur est banni
        if (serviceBan.isUserBanned(currentUserId)) {
            String banMessage = serviceBan.getBanMessage(currentUserId);
            Alert banAlert = new Alert(Alert.AlertType.ERROR);
            banAlert.setTitle("Accès refusé");
            banAlert.setHeaderText("❌ Vous êtes banni du forum");
            banAlert.setContentText(banMessage);
            banAlert.showAndWait();
            return;
        }

        if (!validateCommentContent(contenu)) {
            return;
        }

        if (profanityFilter != null) {
            try {
                profanityFilter.validateText(contenu, "commentaire");
            } catch (IllegalArgumentException e) {
                showAlert("Contenu inapproprié", e.getMessage(), Alert.AlertType.WARNING);
                return;
            }
        }

        double scoreToxicite = analyzeCommentToxicity(contenu);
        boolean estToxique = scoreToxicite >= 0.7;

        Commentaire commentaire = new Commentaire();
        commentaire.setContenu(contenu);
        commentaire.setAnonyme(anonymeCheckBox.isSelected());
        commentaire.setSujetId(currentSujet.getId());
        commentaire.setUserId(currentUserId);
        commentaire.setScoreToxicite(scoreToxicite);
        commentaire.setEstToxique(estToxique);

        try {
            serviceCommentaire.add(commentaire);
            commentTextArea.clear();
            anonymeCheckBox.setSelected(false);
            loadCommentaires();


            showAlert("Succès", "Commentaire ajouté avec succès !", Alert.AlertType.INFORMATION);

        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'ajouter le commentaire: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void onCancelComment() {
        commentTextArea.clear();
        anonymeCheckBox.setSelected(false);
    }

    @FXML
    private void onEditSujet() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Modifier la discussion");
        dialog.setHeaderText("Modifier votre sujet");

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        TextField titreField = new TextField(currentSujet.getTitre());
        titreField.setPromptText("Titre (3-100 caractères)");

        titreField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.length() > 100) {
                titreField.setText(oldVal);
                showAlert("Attention", "Le titre ne peut pas dépasser 100 caractères", Alert.AlertType.WARNING);
            }
        });

        TextArea contenuArea = new TextArea(currentSujet.getContenu());
        contenuArea.setPromptText("Contenu (10-5000 caractères)");
        contenuArea.setPrefHeight(200);

        contenuArea.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.length() > 5000) {
                contenuArea.setText(oldVal);
                showAlert("Attention", "Le contenu ne peut pas dépasser 5000 caractères", Alert.AlertType.WARNING);
            }
        });

        CheckBox anonymeCheck = new CheckBox("Publier anonymement");
        anonymeCheck.setSelected(currentSujet.isAnonyme());

        Label titreLabel = new Label("Titre:");
        titreLabel.setStyle("-fx-font-weight: bold;");
        Label contenuLabel = new Label("Contenu:");
        contenuLabel.setStyle("-fx-font-weight: bold;");

        content.getChildren().addAll(
                titreLabel, titreField,
                contenuLabel, contenuArea,
                anonymeCheck
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setStyle("-fx-background-color: #2C5F8A; -fx-text-fill: white; -fx-cursor: hand;");

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String newTitre = titreField.getText().trim();
            String newContenu = contenuArea.getText().trim();

            if (!validateSujetLength(newTitre, newContenu)) {
                return;
            }

            if (profanityFilter != null) {
                try {
                    profanityFilter.validateText(newTitre, "titre");
                    profanityFilter.validateText(newContenu, "contenu");
                } catch (IllegalArgumentException ex) {
                    showAlert("Contenu inapproprié", ex.getMessage(), Alert.AlertType.WARNING);
                    return;
                }
            }

            currentSujet.setTitre(newTitre);
            currentSujet.setContenu(newContenu);
            currentSujet.setAnonyme(anonymeCheck.isSelected());
            serviceSujet.update(currentSujet);
            loadSujetDetails();
            showAlert("Succès", "Sujet modifié !", Alert.AlertType.INFORMATION);
        }
    }

    private boolean validateSujetLength(String titre, String contenu) {
        if (titre == null || titre.trim().isEmpty()) {
            showAlert("Erreur", "Le titre ne peut pas être vide", Alert.AlertType.WARNING);
            return false;
        }
        if (titre.length() < 3) {
            showAlert("Erreur", "Le titre doit contenir au moins 3 caractères", Alert.AlertType.WARNING);
            return false;
        }
        if (titre.length() > 100) {
            showAlert("Erreur", "Le titre ne peut pas dépasser 100 caractères", Alert.AlertType.WARNING);
            return false;
        }
        if (contenu == null || contenu.trim().isEmpty()) {
            showAlert("Erreur", "Le contenu ne peut pas être vide", Alert.AlertType.WARNING);
            return false;
        }
        if (contenu.length() < 10) {
            showAlert("Erreur", "Le contenu doit contenir au moins 10 caractères", Alert.AlertType.WARNING);
            return false;
        }
        if (contenu.length() > 5000) {
            showAlert("Erreur", "Le contenu ne peut pas dépasser 5000 caractères", Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    @FXML
    private void onDeleteSujet() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Supprimer la discussion");
        confirm.setHeaderText(null);
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer cette discussion ? Cette action est irréversible et supprimera tous les commentaires.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            serviceSujet.delete(currentSujet);
            onBackToForum();
        }
    }

    @FXML
    private void onBackToForum() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Forum.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) navForum.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String formatDate(Date date) {
        if (date == null) return "Date inconnue";
        LocalDate localDate = date.toLocalDate();
        LocalDate today = LocalDate.now();

        if (localDate.equals(today)) {
            return "Aujourd'hui";
        } else if (localDate.equals(today.minusDays(1))) {
            return "Hier";
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return localDate.format(formatter);
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void onNavHomeClicked() {
        navigateTo("/Home.fxml");
    }

    @FXML
    private void onNavSuiviClicked() {
        navigateTo("/Suivi.fxml");
    }

    @FXML
    private void onNavObjectifClicked() {
        navigateTo("/Objectif.fxml");
    }

    @FXML
    private void onNavForumClicked() {
        navigateTo("/Forum.fxml");
    }

    @FXML
    private void onNavRendezVousClicked() {
        navigateTo("/RendezVous.fxml");
    }

    @FXML
    private void onNavActivitesClicked() {
        navigateTo("/Activites.fxml");
    }

    @FXML
    private void onNavContenuClicked() {
        navigateTo("/Contenu.fxml");
    }

    @FXML
    private void onNotifications() {
        showAlert("Notifications", "Fonctionnalité à venir", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void onLogout() {
        try {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Déconnexion");
            confirm.setHeaderText(null);
            confirm.setContentText("Voulez-vous vraiment vous déconnecter ?");

            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
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
}