package Controllor;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import models.User;
import utils.SceneManager;
import utils.SessionManager;
import Services.ServiceRating;
import utils.MyDataBase;
import Models.RendezVous;
import services.ServiceRendezVous;
import models.Notification;
import services.NotificationService;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class ControllerHome {

    @FXML private VBox card1, card2, card3;
    @FXML private VBox adv1, adv2, adv3, adv4;
    @FXML private HBox bannerBox;
    @FXML private Button startButton, notifButton, menuButton, logoutButton;
    @FXML private HBox navAccueil, navSuivi, navObjectifs, navRdv, navForum, navActivites, navRessources;
    @FXML private Label badgeRdv, labelUserName, labelDate, avatarInitials, labelWelcome, labelUserRole;
    @FXML private HBox psyListContainer;
    @FXML private ImageView logoImage;
    @FXML private VBox rdvHomeContainer;
    
    // Notifications
    @FXML private VBox notifPanel;
    @FXML private ListView<Notification> notifListView;
    @FXML private Label notifBadgeLabel;
    @FXML private ComboBox<String> notifFilterCombo;
    @FXML private Button clearAllNotifButton;

    private ContextMenu contextMenu;
    private final ServiceRating serviceRating = new ServiceRating();
    private final ServiceRendezVous serviceRdv = new ServiceRendezVous();
    private final NotificationService notificationService = new NotificationService();
    private int etudiantId;
    private boolean popupRappelDejaAffiche = false;

    @FXML
    public void initialize() {
        // 1. Remplir les infos de l'utilisateur connecté
        User user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            etudiantId = user.getId();
            labelUserName.setText(user.getPrenom() + " " + user.getNom());
            if (labelWelcome != null) labelWelcome.setText("Bonjour, " + user.getPrenom() + " 👋");
            if (labelUserRole != null) labelUserRole.setText(user.getRole() != null ? user.getRole() : "Étudiant");
            
            String initials = "";
            if (user.getPrenom() != null && !user.getPrenom().isEmpty())
                initials += user.getPrenom().charAt(0);
            if (user.getNom() != null && !user.getNom().isEmpty())
                initials += user.getNom().charAt(0);
            avatarInitials.setText(initials.toUpperCase());
        }

        // 2. Date du jour
        labelDate.setText(java.time.LocalDate.now()
                .format(java.time.format.DateTimeFormatter
                        .ofPattern("EEEE d MMMM yyyy", java.util.Locale.FRENCH)));

        // 3. Logo
        try {
            logoImage.setImage(new Image(getClass().getResourceAsStream("/Images/logo.png")));
        } catch (Exception ignored) {}

        // 4. Créer le menu contextuel (3 points)
        setupContextMenu();

        // 5. Charger les données dynamiques
        chargerPsychologues();
        chargerRendezVous();

        // 6. Animations d'apparition
        animateFade(bannerBox, 500);
        animateFade(card1, 400);
        animateFade(card2, 500);
        animateFade(card3, 600);

        // 7. Notifications
        if (notifPanel != null) {
            notifPanel.setVisible(false);
            notifPanel.setManaged(false);
            notifPanel.setOpacity(0);
        }
        configurerFiltreNotifications();
        configurerListViewNotifications();
        verifierEtDeclencherRappelImmediat();
        chargerNotifications();
        mettreAJourBadgeNotif();
    }

    private void setupContextMenu() {
        contextMenu = new ContextMenu();
        contextMenu.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 3);");

        MenuItem modifierProfil = new MenuItem("✏️  Modifier mon profil");
        modifierProfil.setStyle("-fx-font-size: 13px; -fx-padding: 8 16;");
        modifierProfil.setOnAction(e -> SceneManager.switchTo("Profile.fxml", "Mon Profil"));

        MenuItem deconnexion = new MenuItem("🚪  Déconnexion");
        deconnexion.setStyle("-fx-font-size: 13px; -fx-padding: 8 16; -fx-text-fill: #e74c3c;");
        deconnexion.setOnAction(e -> {
            SessionManager.getInstance().logout();
            SceneManager.goToLogin();
        });

        contextMenu.getItems().addAll(modifierProfil, deconnexion);
    }

    @FXML
    public void onMenuClicked(ActionEvent event) {
        Button btn = (Button) event.getSource();
        contextMenu.show(btn, javafx.geometry.Side.BOTTOM, 0, 5);
    }

    private void loadPage(String fxml, Event event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage;
            if (event != null && event.getSource() instanceof Node) {
                stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            } else {
                stage = (Stage) bannerBox.getScene().getWindow();
            }
            Scene scene = new Scene(root);
            stage.setScene(scene);
            FadeTransition ft = new FadeTransition(Duration.millis(300), root);
            ft.setFromValue(0); ft.setToValue(1); ft.play();
            stage.show();
            stage.centerOnScreen();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // Navigation
    @FXML void onNavHomeClicked(MouseEvent event)      { loadPage("/Home.fxml", event); }
    @FXML void onNavSuiviClicked(MouseEvent event)     { loadPage("/suivi_mentale.fxml", event); }
    @FXML void onNavObjectifsClicked(MouseEvent event) { loadPage("/objvue.fxml", event); }
    @FXML void onNavRdvClicked(MouseEvent event)       { loadPage("/RendezVous_Etudiant.fxml", event); }
    @FXML void onNavBlogClicked(MouseEvent event)      { loadPage("/forum.fxml", event); }
    @FXML void onNavActivitesClicked(MouseEvent event) { System.out.println("Activités"); }
    @FXML void onNavRessourcesClicked(MouseEvent event){ loadPage("/StudentRessources.fxml", event); }
    @FXML void onCardSuiviClicked(MouseEvent event)    { loadPage("/suivi_mentale.fxml", event); }
    @FXML void onNavRdvClicked_Btn(ActionEvent event)  { loadPage("/RendezVous_Etudiant.fxml", event); }

    // Hover sidebar
    @FXML
    private void onNavHoverEnter(MouseEvent event) {
        HBox src = (HBox) event.getSource();
        scale(src, 1.02, 150);
        src.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-background-radius: 8; -fx-padding: 10 14; -fx-cursor: hand;");
    }

    @FXML
    private void onNavHoverExit(MouseEvent event) {
        HBox src = (HBox) event.getSource();
        scale(src, 1.0, 150);
        src.setStyle("-fx-background-color: transparent; -fx-background-radius: 8; -fx-padding: 10 14; -fx-cursor: hand;");
    }

    // Hover cartes
    @FXML
    private void onCardHoverEnter(MouseEvent event) {
        VBox card = (VBox) event.getSource();
        scale(card, 1.02, 200);
        card.setStyle("-fx-background-color: #F8FBFF; -fx-background-radius: 16; -fx-padding: 22; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 15, 0, 0, 4);");
    }

    @FXML
    private void onCardHoverExit(MouseEvent event) {
        VBox card = (VBox) event.getSource();
        scale(card, 1.0, 200);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-padding: 22; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 2);");
    }

    // Hover avantages
    @FXML
    private void onAdvantageHoverEnter(MouseEvent event) {
        VBox adv = (VBox) event.getSource();
        scale(adv, 1.05, 150);
        adv.setStyle("-fx-padding: 10; -fx-background-radius: 12; -fx-background-color: rgba(151,187,228,0.15);");
    }

    @FXML
    private void onAdvantageHoverExit(MouseEvent event) {
        VBox adv = (VBox) event.getSource();
        scale(adv, 1.0, 150);
        adv.setStyle("-fx-padding: 10; -fx-background-radius: 12; -fx-background-color: transparent;");
    }

    // Boutons actions

    @FXML
    private void onLogout(ActionEvent event) {
        SessionManager.getInstance().logout();
        SceneManager.goToLogin();
    }

    @FXML
    private void onStartSuivi(ActionEvent event) {
        Button btn = (Button) event.getSource();
        scale(btn, 1.05, 150);
        System.out.println("Start suivi");
    }

    // Données dynamiques
    private void chargerPsychologues() {
        if (psyListContainer == null) return;
        psyListContainer.getChildren().clear();
        Connection conn = MyDataBase.getInstance().getCnx();
        if (conn == null) return;
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT id, prenom, nom FROM user WHERE role = 'psychologue' LIMIT 3")) {
            while (rs.next()) {
                String nomComplet = "Dr. " + rs.getString("prenom") + " " + rs.getString("nom");
                double avg = serviceRating.getAverageForPsy(rs.getInt("id"));
                psyListContainer.getChildren().add(createMiniPsyCard(nomComplet, avg));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private VBox createMiniPsyCard(String nom, double avg) {
        VBox card = new VBox(8);
        card.setAlignment(javafx.geometry.Pos.CENTER);
        card.setStyle("-fx-background-color: #F8FBFF; -fx-background-radius: 12; -fx-padding: 15; -fx-border-color: #E2E8F0; -fx-border-radius: 12;");
        card.setPrefWidth(160);
        Label lblNom = new Label(nom);
        lblNom.setStyle("-fx-font-weight: bold; -fx-text-fill: #2C3E50; -fx-font-size: 13px;");
        HBox stars = new HBox(2);
        stars.setAlignment(javafx.geometry.Pos.CENTER);
        int fullStars = (int) Math.round(avg);
        for (int i = 1; i <= 5; i++) {
            Label star = new Label(i <= fullStars ? "★" : "☆");
            star.setStyle("-fx-text-fill: #F1C40F; -fx-font-size: 14px;");
            stars.getChildren().add(star);
        }
        Label lblAvg = new Label(String.format("%.1f/5", avg));
        lblAvg.setStyle("-fx-text-fill: #64748B; -fx-font-size: 11px;");
        card.getChildren().addAll(lblNom, stars, lblAvg);
        return card;
    }

    private void chargerRendezVous() {
        if (rdvHomeContainer == null) return;
        rdvHomeContainer.getChildren().clear();
        List<RendezVous> aujourdhui = serviceRdv.getRdvAujourdhui(etudiantId);
        List<RendezVous> avenir = serviceRdv.getRdvAvenir(etudiantId);
        if (aujourdhui.isEmpty() && avenir.isEmpty()) {
            Label noRdv = new Label("Vous n'avez aucun rendez-vous de prévu.");
            noRdv.setStyle("-fx-text-fill: #64748B; -fx-font-size: 13px; -fx-font-style: italic;");
            rdvHomeContainer.getChildren().add(noRdv);
            return;
        }
        int count = 0;
        for (RendezVous r : aujourdhui) {
            if (count >= 3) break;
            rdvHomeContainer.getChildren().add(createRdvRow(r, "Aujourd'hui"));
            count++;
        }
        for (RendezVous r : avenir) {
            if (count >= 3) break;
            rdvHomeContainer.getChildren().add(createRdvRow(r, r.getDate().toString()));
            count++;
        }
    }

    private HBox createRdvRow(RendezVous r, String dateStr) {
        HBox row = new HBox(15);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #F8FBFF; -fx-background-radius: 10; -fx-padding: 12; -fx-border-color: #E2E8F0; -fx-border-radius: 10;");
        Label icon = new Label("📅");
        icon.setStyle("-fx-font-size: 18px;");
        VBox info = new VBox(2);
        Label lblDate = new Label(dateStr + " • " + r.getHeureDebut());
        lblDate.setStyle("-fx-text-fill: #2C5F8A; -fx-font-weight: bold; -fx-font-size: 12px;");
        Label lblType = new Label(r.getTypeRdv() + " (" + r.getLieu() + ")");
        lblType.setStyle("-fx-text-fill: #2C3E50; -fx-font-weight: bold; -fx-font-size: 13px;");
        info.getChildren().addAll(lblDate, lblType);
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        Label statusBadge = new Label(r.getStatut().toUpperCase());
        String badgeStyle = "-fx-padding: 4 8; -fx-background-radius: 12; -fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: white;";
        if ("confirmé".equalsIgnoreCase(r.getStatut())) badgeStyle += "-fx-background-color: #2ECC71;";
        else if ("en attente".equalsIgnoreCase(r.getStatut()) || "réservé".equalsIgnoreCase(r.getStatut())) badgeStyle += "-fx-background-color: #F1C40F;";
        else badgeStyle += "-fx-background-color: #94A3B8;";
        statusBadge.setStyle(badgeStyle);
        row.getChildren().addAll(icon, info, spacer, statusBadge);
        return row;
    }



    // =========================================================================
    // NOTIFICATIONS
    // =========================================================================

    private void verifierEtDeclencherRappelImmediat() {
        try {
            boolean rappelCree = notificationService.checkAndCreateDailyReminder(etudiantId);
            if (rappelCree && !popupRappelDejaAffiche) {
                popupRappelDejaAffiche = true;
                afficherPopupRappel("Vous n'avez pas encore ajouté votre suivi mental aujourd'hui 🧠");
            }
        } catch (Exception e) {
            System.err.println("Erreur rappel : " + e.getMessage());
        }
    }

    private void afficherPopupRappel(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Rappel automatique");
        alert.setHeaderText("Suivi mental");
        alert.setContentText(message);
        alert.show();
    }

    private void configurerFiltreNotifications() {
        if (notifFilterCombo == null) return;
        notifFilterCombo.setItems(FXCollections.observableArrayList(
                "Tous", "progression_hausse", "progression_baisse", "progression_stable", "rappel_suivi"
        ));
        notifFilterCombo.setValue("Tous");
    }

    private void configurerListViewNotifications() {
        if (notifListView == null) return;
        notifListView.setCellFactory(listView -> new ListCell<Notification>() {
            @Override
            protected void updateItem(Notification notif, boolean empty) {
                super.updateItem(notif, empty);
                if (empty || notif == null) {
                    setText(null); setGraphic(null); setStyle("-fx-background-color: transparent;");
                    return;
                }
                String iconText = getIconForType(notif.getType());
                Label iconLabel = new Label(iconText);
                iconLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
                StackPane iconContainer = new StackPane(iconLabel);
                iconContainer.setMinSize(34, 34);
                iconContainer.setStyle(getIconContainerStyle(notif.getType()));

                Label titleLabel = new Label(notif.getTitle() == null ? "Notification" : notif.getTitle());
                titleLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1E3A5F;");
                Label msgLabel = new Label(notif.getMessage() == null ? "" : notif.getMessage());
                msgLabel.setWrapText(true);
                msgLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #5B6B7A; -fx-font-weight: 500;");
                Label dateLabel = new Label(getRelativeTime(notif.getCreatedAt()));
                dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #8A9AAA;");

                Circle unreadDot = new Circle(5);
                unreadDot.setFill(notif.isRead() ? Color.TRANSPARENT : Color.web("#E74C3C"));

                Button deleteBtn = new Button("✕");
                deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #8A9AAA; -fx-cursor: hand;");
                deleteBtn.setOnAction(e -> {
                    notificationService.deleteNotificationById(notif.getId());
                    chargerNotifications();
                    mettreAJourBadgeNotif();
                });

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                HBox topRow = new HBox(8, unreadDot, iconContainer, titleLabel, spacer, dateLabel, deleteBtn);
                topRow.setAlignment(Pos.CENTER_LEFT);
                VBox content = new VBox(6, topRow, msgLabel);
                content.setPadding(new Insets(12));
                content.setStyle("-fx-background-color: white; -fx-background-radius: 14; -fx-border-color: #E6EEF8; -fx-border-radius: 14;");
                setGraphic(content);
                setText(null);
                setStyle("-fx-background-color: transparent;");
            }
        });
    }

    private String getIconForType(String type) {
        if (type == null) return "🔔";
        switch (type) {
            case "progression_hausse": return "📈";
            case "progression_baisse": return "📉";
            case "progression_stable": return "➖";
            case "rappel_suivi": return "⏰";
            default: return "🔔";
        }
    }

    private String getIconContainerStyle(String type) {
        String base = "-fx-background-radius: 999; -fx-border-radius: 999; -fx-border-width: 1; ";
        if (type == null) return base + "-fx-background-color: #EEF4FB; -fx-border-color: #D8E5F2;";
        switch (type) {
            case "progression_hausse": return base + "-fx-background-color: #E8F8F0; -fx-border-color: #BFE8D1;";
            case "progression_baisse": return base + "-fx-background-color: #FFF1F1; -fx-border-color: #F5C2C0;";
            case "progression_stable": return base + "-fx-background-color: #FFF8E8; -fx-border-color: #F1DFB3;";
            case "rappel_suivi": return base + "-fx-background-color: #EEF4FF; -fx-border-color: #C9D9F7;";
            default: return base + "-fx-background-color: #EEF4FB; -fx-border-color: #D8E5F2;";
        }
    }

    private String getRelativeTime(Timestamp ts) {
        if (ts == null) return "";
        LocalDateTime dt = ts.toLocalDateTime();
        java.time.Duration d = java.time.Duration.between(dt, LocalDateTime.now());
        long m = d.toMinutes();
        if (m < 1) return "à l'instant";
        if (m < 60) return "il y a " + m + " min";
        long h = d.toHours();
        if (h < 24) return "il y a " + h + " h";
        long days = d.toDays();
        if (days < 7) return "il y a " + days + " j";
        return dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    private void chargerNotifications() {
        String filter = notifFilterCombo != null ? notifFilterCombo.getValue() : "Tous";
        List<Notification> list = notificationService.getNotificationsByUserAndType(etudiantId, filter);
        if (notifListView != null) notifListView.setItems(FXCollections.observableArrayList(list));
    }

    private void mettreAJourBadgeNotif() {
        if (notifBadgeLabel == null) return;
        int count = notificationService.countUnreadNotifications(etudiantId);
        notifBadgeLabel.setText(String.valueOf(count));
        notifBadgeLabel.setVisible(count > 0);
        notifBadgeLabel.setManaged(count > 0);
    }

    @FXML
    private void onNotifFilterChanged(ActionEvent event) { chargerNotifications(); }

    @FXML
    private void onClearAllNotifications(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer toutes les notifications ?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            notificationService.deleteAllNotifications(etudiantId);
            chargerNotifications();
            mettreAJourBadgeNotif();
        }
    }

    @FXML
    private void onNotifications(ActionEvent event) {
        if (notifPanel == null) return;
        boolean open = !notifPanel.isVisible();
        notifPanel.setVisible(open);
        notifPanel.setManaged(open);
        if (open) {
            chargerNotifications();
            notificationService.markAllAsRead(etudiantId);
            mettreAJourBadgeNotif();
            FadeTransition ft = new FadeTransition(Duration.millis(220), notifPanel);
            ft.setFromValue(0); ft.setToValue(1); ft.play();
        }
    }

    // Helpers
    private void animateFade(Node node, int ms) {
        if (node == null) return;
        FadeTransition ft = new FadeTransition(Duration.millis(ms), node);
        ft.setFromValue(0); ft.setToValue(1); ft.play();
    }

    private void scale(Node node, double factor, int ms) {
        if (node == null) return;
        ScaleTransition st = new ScaleTransition(Duration.millis(ms), node);
        st.setToX(factor); st.setToY(factor);
        if (factor > 1.0) { st.setAutoReverse(true); st.setCycleCount(2); }
        st.play();
    }
}