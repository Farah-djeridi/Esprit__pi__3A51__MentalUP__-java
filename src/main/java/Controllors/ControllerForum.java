package Controllors;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.geometry.Insets;

import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Locale;


import models.Sujet;
import services.ServiceSujet;
import services.ServiceCommentaire;
import services.ServiceVote;
import models.Vote;

public class ControllerForum {

    @FXML private Label labelDate;
    @FXML private Label badgeRdv;
    @FXML private Label labelUserName;
    @FXML private Label avatarInitials;
    @FXML private Label totalDiscussions;

    @FXML private Button logoutButton;
    @FXML private Button notifButton;
    @FXML private Button newDiscussionButton;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> orderCombo;

    @FXML private VBox discussionsContainer;
    @FXML private VBox emptyState;
    @FXML private HBox paginationBox;

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
    private List<Sujet> allSujets;
    private List<Sujet> filteredSujets;

    private int currentPage = 1;
    private int totalPages = 1;
    private final int itemsPerPage = 5;

    // Session utilisateur (ID 2 comme demandé)
    private int currentUserId = 2;
    private String currentUserName = "Sophie Amara";

    @FXML
    public void initialize() {
        updateDate();

        labelUserName.setText(currentUserName);
        avatarInitials.setText(getInitials(currentUserName));

        serviceSujet = new ServiceSujet();
        serviceCommentaire = new ServiceCommentaire();
        serviceVote = new ServiceVote();
        allSujets = new ArrayList<>();
        filteredSujets = new ArrayList<>();

        setupNavigationHoverEffects();
        loadSujetsFromDatabase();
        setupSearchAndFilter();

        badgeRdv.setText(String.valueOf(getRendezVousCount()));

        orderCombo.getItems().addAll("Plus récents", "Plus populaires");
        orderCombo.setValue("Plus récents");
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
            discussionsContainer.getChildren().add(createSujetCard(sujet));
        }

        updatePaginationControls();
    }

    private VBox createSujetCard(Sujet sujet) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: #F8FAFE; -fx-padding: 15; -fx-background-radius: 12; " +
                "-fx-border-color: #E8EEF4; -fx-border-radius: 12; -fx-border-width: 1;");
        card.setCursor(javafx.scene.Cursor.HAND);
        card.setOnMouseClicked(e -> onSujetClicked(sujet));

        HBox header = new HBox(12);

        boolean isOwner = sujet.getIdUser() == currentUserId;

        String displayName = sujet.isAnonyme() ? "A" : (sujet.getUserName() != null ? sujet.getUserName() : "Utilisateur");
        Label avatar = new Label(sujet.isAnonyme() ? "A" : getInitials(displayName));

        avatar.setStyle("-fx-background-color: " + (sujet.isAnonyme() ? "#8E9EAB" : "#2C5F8A") +
                "; -fx-text-fill: white; -fx-padding: 10; -fx-background-radius: 50; " +
                "-fx-font-weight: bold; -fx-font-size: 12px;");
        avatar.setPrefWidth(40);
        avatar.setPrefHeight(40);
        avatar.setAlignment(Pos.CENTER);

        VBox contentBox = new VBox(5);
        HBox.setHgrow(contentBox, Priority.ALWAYS);

        Label title = new Label(sujet.getTitre());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #2C3E50;");
        title.setWrapText(true);

        String authorText;
        if (sujet.isAnonyme()) {
            authorText = "Anonyme";
        } else if (isOwner) {
            authorText = "Vous";
        } else {
            authorText = sujet.getUserName() != null ? sujet.getUserName() : "Utilisateur";
        }

        HBox metaData = new HBox(10);

        Label authorLabel = new Label("Par " + authorText);
        authorLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7F8C8D;");

        Label dot = new Label("•");
        dot.setStyle("-fx-text-fill: #7F8C8D;");

        Label dateValue = new Label(formatDate(sujet.getDateCreation()));
        dateValue.setStyle("-fx-font-size: 12px; -fx-text-fill: #7F8C8D;");

        metaData.getChildren().addAll(authorLabel, dot, dateValue);

        String contentText = sujet.getContenu();
        if (contentText != null && contentText.length() > 100) {
            contentText = contentText.substring(0, 100) + "...";
        }

        Label content = new Label(contentText);
        content.setWrapText(true);
        content.setStyle("-fx-font-size: 13px; -fx-text-fill: #5A6C7D; -fx-font-weight: 500;");

        contentBox.getChildren().addAll(title, metaData, content);

        VBox statsBox = new VBox(5);
        statsBox.setAlignment(Pos.CENTER_RIGHT);

        int commentCount = getCommentCountForSujet(sujet.getId());

        Label commentsLabel = new Label("💬 " + commentCount);
        commentsLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #2C5F8A; -fx-font-weight: bold;");

        Label likesLabel = new Label("👍 " + sujet.getNbLikes());
        likesLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7F8C8D;");

        statsBox.getChildren().addAll(commentsLabel, likesLabel);

        header.getChildren().addAll(avatar, contentBox, statsBox);

        HBox actionsBox = new HBox(10);
        actionsBox.setAlignment(Pos.CENTER_RIGHT);

        Vote userVote = serviceVote.getUserVoteOnSujet(sujet.getId());
        boolean hasLiked = userVote != null && "like".equals(userVote.getType());
        boolean hasDisliked = userVote != null && "dislike".equals(userVote.getType());

        Button likeBtn = new Button("👍 " + sujet.getNbLikes());
        likeBtn.setStyle("-fx-background-color: " + (hasLiked ? "#2C5F8A" : "transparent") +
                "; -fx-text-fill: " + (hasLiked ? "white" : "#7F8C8D") +
                "; -fx-cursor: hand; -fx-background-radius: 20; -fx-padding: 5 10;");

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
        dislikeBtn.setStyle("-fx-background-color: " + (hasDisliked ? "#E74C3C" : "transparent") +
                "; -fx-text-fill: " + (hasDisliked ? "white" : "#7F8C8D") +
                "; -fx-cursor: hand; -fx-background-radius: 20; -fx-padding: 5 10;");

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
        commentBtn.setStyle("-fx-background-color: #2C5F8A; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 20; -fx-padding: 5 15;");
        commentBtn.setOnAction(e -> {
            e.consume();
            onSujetClicked(sujet);
        });

        actionsBox.getChildren().addAll(likeBtn, dislikeBtn, commentBtn);

        if (isOwner) {
            MenuButton actionsMenu = new MenuButton("⋮");
            actionsMenu.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 5 10;");

            MenuItem editItem = new MenuItem("✏️ Modifier");
            editItem.setOnAction(e -> {
                e.consume();
                editSujet(sujet);
            });

            MenuItem deleteItem = new MenuItem("🗑️ Supprimer");
            deleteItem.setStyle("-fx-text-fill: #E74C3C;");
            deleteItem.setOnAction(e -> {
                e.consume();
                deleteSujet(sujet);
            });

            actionsMenu.getItems().addAll(editItem, new SeparatorMenuItem(), deleteItem);
            actionsBox.getChildren().add(actionsMenu);
        }

        card.getChildren().addAll(header, actionsBox);

        return card;
    }

    // Méthode pour éditer un sujet (utilise la fenêtre FXML existante)
    private void editSujet(Sujet sujet) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EditSujet.fxml"));
            Parent root = loader.load();

            ControllerEditSujet controller = loader.getController();
            controller.setSujet(sujet, serviceSujet);

            Stage stage = new Stage();
            stage.setTitle("Modifier la discussion");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            // Pas de initModality - la fenêtre n'est pas modale
            stage.showAndWait();

            // Recharger la liste après modification
            loadSujetsFromDatabase();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la fenêtre de modification: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // Méthode pour supprimer un sujet
    private void deleteSujet(Sujet sujet) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Supprimer la discussion");
        confirm.setHeaderText(null);
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer cette discussion ? Cette action est irréversible et supprimera tous les commentaires.");

        // Styliser le dialogue de confirmation
        DialogPane dialogPane = confirm.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #F8FAFE;");

        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        okButton.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 5; -fx-padding: 8 20;");

        Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
        cancelButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #7F8C8D; -fx-cursor: hand; -fx-padding: 8 20;");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            serviceSujet.delete(sujet);
            loadSujetsFromDatabase();
            showAlert("Succès", "Sujet supprimé !", Alert.AlertType.INFORMATION);
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

        Button prevBtn = new Button("◀ Précédent");
        prevBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #2C5F8A; -fx-cursor: hand; -fx-padding: 5 10;");
        prevBtn.setDisable(currentPage == 1);
        prevBtn.setOnAction(e -> {
            if (currentPage > 1) {
                currentPage--;
                displayCurrentPage();
            }
        });

        HBox pagesBox = new HBox(5);
        pagesBox.setAlignment(Pos.CENTER);

        int startPage = Math.max(1, currentPage - 2);
        int endPage = Math.min(totalPages, startPage + 4);

        if (startPage > 1) {
            Button page1 = createPageButton(1);
            pagesBox.getChildren().add(page1);
            if (startPage > 2) {
                Label dots = new Label("...");
                dots.setStyle("-fx-text-fill: #7F8C8D; -fx-padding: 5;");
                pagesBox.getChildren().add(dots);
            }
        }

        for (int i = startPage; i <= endPage; i++) {
            Button pageBtn = createPageButton(i);
            if (i == currentPage) {
                pageBtn.setStyle("-fx-background-color: #2C5F8A; -fx-text-fill: white; " +
                        "-fx-background-radius: 8; -fx-padding: 5 12; -fx-font-weight: bold;");
            }
            pagesBox.getChildren().add(pageBtn);
        }

        if (endPage < totalPages) {
            if (endPage < totalPages - 1) {
                Label dots = new Label("...");
                dots.setStyle("-fx-text-fill: #7F8C8D; -fx-padding: 5;");
                pagesBox.getChildren().add(dots);
            }
            Button lastPage = createPageButton(totalPages);
            pagesBox.getChildren().add(lastPage);
        }

        Button nextBtn = new Button("Suivant ▶");
        nextBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #2C5F8A; -fx-cursor: hand; -fx-padding: 5 10;");
        nextBtn.setDisable(currentPage == totalPages);
        nextBtn.setOnAction(e -> {
            if (currentPage < totalPages) {
                currentPage++;
                displayCurrentPage();
            }
        });

        paginationBox.getChildren().addAll(prevBtn, pagesBox, nextBtn);
    }

    private Button createPageButton(int pageNum) {
        Button btn = new Button(String.valueOf(pageNum));
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #2C5F8A; " +
                "-fx-background-radius: 8; -fx-padding: 5 12; -fx-cursor: hand;");
        btn.setOnAction(e -> {
            currentPage = pageNum;
            displayCurrentPage();
        });
        return btn;
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

        // Styliser l'alerte
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #F8FAFE;");

        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        okButton.setStyle("-fx-background-color: #2C5F8A; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 5; -fx-padding: 8 20;");

        alert.showAndWait();
    }

    private int getRendezVousCount() {
        return 3;
    }

    // ========== NAVIGATION ==========

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
        loadSujetsFromDatabase();
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

    // ========== ACTIONS ==========

    @FXML
    private void onNewDiscussion() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/NouvelleDiscussion.fxml"));
            Parent root = loader.load();

            ControllerNouvelleDiscussion controller = loader.getController();
            controller.setServiceSujet(serviceSujet);
            controller.setUserId(currentUserId);


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

    private void onSujetClicked(Sujet sujet) {
        try {
            serviceSujet.incrementVues(sujet.getId());

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DetailDiscussion.fxml"));
            Parent root = loader.load();

            ControllerDetailDiscussion controller = loader.getController();
            controller.setSujet(sujet);
            controller.setCurrentUserId(currentUserId);

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
            dialogPane.setStyle("-fx-background-color: #F8FAFE;");

            Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
            okButton.setStyle("-fx-background-color: #2C5F8A; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 5;");

            Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
            cancelButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #7F8C8D; -fx-cursor: hand;");

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
}