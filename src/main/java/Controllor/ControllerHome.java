package Controllor;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import models.Notification;
import models.User;
import services.NotificationService;
import utils.SceneManager;
import utils.SessionManager;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.Priority;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class ControllerHome {

    // Sidebar fields
    @FXML private HBox navAccueil, navSuivi, navObjectifs, navRdv, navForum, navActivites, navRessources;
    @FXML private Label badgeRdv, labelUserName, labelDate, avatarInitials, labelWelcome, labelUserRole;
    @FXML private ImageView logoImage;
    @FXML private Button logoutButton, notifButton, menuButton;
    @FXML private Circle avatarCircle;
    @FXML private ImageView avatarImage;

    // Content area
    @FXML private StackPane contentPane;

    // Notifications
    @FXML private VBox notifPanel;
    @FXML private ListView<Notification> notifListView;
    @FXML private Label notifBadgeLabel;
    @FXML private ComboBox<String> notifFilterCombo;
    @FXML private Button clearAllNotifButton;

    private ContextMenu contextMenu;
    private final NotificationService notificationService = new NotificationService();
    private int etudiantId;
    private HBox activeNav;

    @FXML
    public void initialize() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            etudiantId = user.getId();
            labelUserName.setText(user.getPrenom() + " " + user.getNom());
            if (labelWelcome != null) labelWelcome.setText("Bonjour, " + user.getPrenom() + " 👋");
            if (labelUserRole != null) labelUserRole.setText(user.getRole() != null ? user.getRole() : "étudiant");
            String initials = "";
            if (user.getPrenom() != null && !user.getPrenom().isEmpty()) initials += user.getPrenom().charAt(0);
            if (user.getNom()    != null && !user.getNom().isEmpty())    initials += user.getNom().charAt(0);
            avatarInitials.setText(initials.toUpperCase());

            // Load profile photo if available
            loadAvatarPhoto(user.getAvatarFilename());
        }

        labelDate.setText(java.time.LocalDate.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", java.util.Locale.FRENCH)));

        try { logoImage.setImage(new Image(getClass().getResourceAsStream("/Images/logo.png"))); }
        catch (Exception ignored) {}

        setupContextMenu();

        if (notifPanel != null) { notifPanel.setVisible(false); notifPanel.setManaged(false); }
        configurerFiltreNotifications();
        configurerListViewNotifications();
        chargerNotifications();
        mettreAJourBadgeNotif();

        // Load home content by default
        setActiveNav(navAccueil);
        loadContent("/HomeContent.fxml");
    }

    // ─── Content loading ─────────────────────────────────────────────────────

    private void loadContent(String fxml) {
        try {
            Parent content = FXMLLoader.load(getClass().getResource(fxml));
            contentPane.getChildren().setAll(content);
            FadeTransition ft = new FadeTransition(Duration.millis(250), content);
            ft.setFromValue(0); ft.setToValue(1); ft.play();
        } catch (Exception e) {
            e.printStackTrace();
            // Show error label in content pane
            Label err = new Label("Erreur chargement : " + fxml + "\n" + e.getMessage());
            err.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 13px; -fx-padding: 20;");
            contentPane.getChildren().setAll(err);
        }
    }

    private void setActiveNav(HBox nav) {
        // Reset previous active
        if (activeNav != null) {
            activeNav.setStyle("-fx-background-color: transparent; -fx-background-radius: 8; -fx-padding: 10 12; -fx-cursor: hand;");
        }
        activeNav = nav;
        if (nav != null) {
            nav.setStyle("-fx-background-color: rgba(255,255,255,0.18); -fx-background-radius: 8; -fx-padding: 10 12; -fx-cursor: hand;");
        }
    }

    // ─── Navigation ──────────────────────────────────────────────────────────

    @FXML void onNavHomeClicked(MouseEvent e)      { setActiveNav(navAccueil);   loadContent("/HomeContent.fxml"); }
    @FXML void onNavSuiviClicked(MouseEvent e)     { setActiveNav(navSuivi);     loadContent("/suivi_mentale.fxml"); }
    @FXML void onNavObjectifsClicked(MouseEvent e) { setActiveNav(navObjectifs); loadContent("/objvue.fxml"); }
    @FXML void onNavRdvClicked(MouseEvent e)       { setActiveNav(navRdv);       loadContent("/RendezVous_Etudiant.fxml"); }
    @FXML void onNavForumClicked(MouseEvent e)     { setActiveNav(navForum);     loadContent("/forum.fxml"); }
    @FXML void onNavActivitesClicked(MouseEvent e) { setActiveNav(navActivites); loadContent("/EtudiantActivites.fxml"); }
    @FXML void onNavRessourcesClicked(MouseEvent e){ setActiveNav(navRessources);loadContent("/StudentRessources.fxml"); }

    // ─── Hover ───────────────────────────────────────────────────────────────

    @FXML
    private void onNavHoverEnter(MouseEvent event) {
        HBox src = (HBox) event.getSource();
        if (src != activeNav) {
            src.setStyle("-fx-background-color: rgba(255,255,255,0.10); -fx-background-radius: 8; -fx-padding: 10 12; -fx-cursor: hand;");
        }
    }

    @FXML
    private void onNavHoverExit(MouseEvent event) {
        HBox src = (HBox) event.getSource();
        if (src != activeNav) {
            src.setStyle("-fx-background-color: transparent; -fx-background-radius: 8; -fx-padding: 10 12; -fx-cursor: hand;");
        }
    }

    // ─── Top bar ─────────────────────────────────────────────────────────────

    @FXML
    public void onMenuClicked(ActionEvent event) {
        Button btn = (Button) event.getSource();
        contextMenu.show(btn, javafx.geometry.Side.BOTTOM, 0, 5);
    }

    @FXML
    private void onLogout(ActionEvent event) {
        SessionManager.getInstance().logout();
        SceneManager.goToLogin();
    }

    // ─── Avatar photo ────────────────────────────────────────────────────────

    private void loadAvatarPhoto(String path) {
        if (path == null || path.isEmpty()) return;
        try {
            java.io.File f = new java.io.File(path);
            if (!f.exists()) return;
            javafx.scene.image.Image img = new javafx.scene.image.Image(f.toURI().toString());
            avatarImage.setImage(img);
            // Circular clip
            javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(20, 20, 20);
            avatarImage.setClip(clip);
            avatarImage.setVisible(true);
            avatarInitials.setVisible(false);
            if (avatarCircle != null) avatarCircle.setVisible(false);
        } catch (Exception e) {
            System.err.println("Avatar load error: " + e.getMessage());
        }
    }

    private void setupContextMenu() {
        contextMenu = new ContextMenu();
        contextMenu.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 3);");
        MenuItem modifierProfil = new MenuItem("✏️  Modifier mon profil");
        modifierProfil.setStyle("-fx-font-size: 13px; -fx-padding: 8 16;");
        modifierProfil.setOnAction(e -> SceneManager.switchTo("Profile.fxml", "Mon Profil"));
        MenuItem deconnexion = new MenuItem("🚪  Déconnexion");
        deconnexion.setStyle("-fx-font-size: 13px; -fx-padding: 8 16; -fx-text-fill: #e74c3c;");
        deconnexion.setOnAction(e -> { SessionManager.getInstance().logout(); SceneManager.goToLogin(); });
        contextMenu.getItems().addAll(modifierProfil, deconnexion);
    }

    // ─── Notifications ───────────────────────────────────────────────────────

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
            FadeTransition ft = new FadeTransition(Duration.millis(200), notifPanel);
            ft.setFromValue(0); ft.setToValue(1); ft.play();
        }
    }

    @FXML private void onNotifFilterChanged(ActionEvent event) { chargerNotifications(); }

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

    private void configurerFiltreNotifications() {
        if (notifFilterCombo == null) return;
        notifFilterCombo.setItems(FXCollections.observableArrayList(
                "Tous", "progression_hausse", "progression_baisse", "progression_stable", "rappel_suivi"));
        notifFilterCombo.setValue("Tous");
    }

    private void configurerListViewNotifications() {
        if (notifListView == null) return;
        notifListView.setCellFactory(listView -> new ListCell<Notification>() {
            @Override
            protected void updateItem(Notification notif, boolean empty) {
                super.updateItem(notif, empty);
                if (empty || notif == null) { setText(null); setGraphic(null); setStyle("-fx-background-color: transparent;"); return; }
                Label iconLabel = new Label(getIconForType(notif.getType()));
                iconLabel.setStyle("-fx-font-size: 16px;");
                StackPane iconContainer = new StackPane(iconLabel);
                iconContainer.setMinSize(32, 32);
                iconContainer.setStyle(getIconContainerStyle(notif.getType()));
                Label titleLabel = new Label(notif.getTitle() == null ? "Notification" : notif.getTitle());
                titleLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1E3A5F;");
                Label msgLabel = new Label(notif.getMessage() == null ? "" : notif.getMessage());
                msgLabel.setWrapText(true);
                msgLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #5B6B7A;");
                Label dateLabel = new Label(getRelativeTime(notif.getCreatedAt()));
                dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #8A9AAA;");
                Circle unreadDot = new Circle(5);
                unreadDot.setFill(notif.isRead() ? Color.TRANSPARENT : Color.web("#E74C3C"));
                Button deleteBtn = new Button("✕");
                deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #8A9AAA; -fx-cursor: hand;");
                deleteBtn.setOnAction(e -> { notificationService.deleteNotificationById(notif.getId()); chargerNotifications(); mettreAJourBadgeNotif(); });
                Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
                HBox topRow = new HBox(8, unreadDot, iconContainer, titleLabel, spacer, dateLabel, deleteBtn);
                topRow.setAlignment(Pos.CENTER_LEFT);
                VBox content = new VBox(6, topRow, msgLabel);
                content.setPadding(new Insets(10));
                content.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: #E6EEF8; -fx-border-radius: 12;");
                setGraphic(content); setText(null); setStyle("-fx-background-color: transparent;");
            }
        });
    }

    private String getIconForType(String type) {
        if (type == null) return "🔔";
        return switch (type) {
            case "progression_hausse" -> "📈";
            case "progression_baisse" -> "📉";
            case "progression_stable" -> "➖";
            case "rappel_suivi"       -> "⏰";
            default                   -> "🔔";
        };
    }

    private String getIconContainerStyle(String type) {
        String base = "-fx-background-radius: 999; -fx-border-radius: 999; -fx-border-width: 1; ";
        if (type == null) return base + "-fx-background-color: #EEF4FB; -fx-border-color: #D8E5F2;";
        return switch (type) {
            case "progression_hausse" -> base + "-fx-background-color: #E8F8F0; -fx-border-color: #BFE8D1;";
            case "progression_baisse" -> base + "-fx-background-color: #FFF1F1; -fx-border-color: #F5C2C0;";
            case "progression_stable" -> base + "-fx-background-color: #FFF8E8; -fx-border-color: #F1DFB3;";
            case "rappel_suivi"       -> base + "-fx-background-color: #EEF4FF; -fx-border-color: #C9D9F7;";
            default                   -> base + "-fx-background-color: #EEF4FB; -fx-border-color: #D8E5F2;";
        };
    }

    private String getRelativeTime(Timestamp ts) {
        if (ts == null) return "";
        LocalDateTime dt = ts.toLocalDateTime();
        java.time.Duration d = java.time.Duration.between(dt, LocalDateTime.now());
        long m = d.toMinutes();
        if (m < 1)  return "à l'instant";
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
}
