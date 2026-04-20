package controllers;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.event.ActionEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import models.Notification;
import services.NotificationService;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class ControllerHome {

    @FXML private VBox card1, card2, card3;
    @FXML private VBox adv1, adv2, adv3, adv4;
    @FXML private HBox bannerBox;
    @FXML private Button startButton, notifButton, logoutButton;
    @FXML private HBox navAccueil, navSuivi, navObjectifs, navRdv, navForum, navActivites, navRessources;
    @FXML private Label badgeRdv, labelUserName, labelDate, avatarInitials;
    @FXML private ImageView logoImage;

    @FXML private StackPane contentArea;
    @FXML private VBox homeContent;

    @FXML private VBox notifPanel;
    @FXML private ListView<Notification> notifListView;
    @FXML private Label notifBadgeLabel;

    @FXML private ComboBox<String> notifFilterCombo;
    @FXML private Button clearAllNotifButton;

    private final NotificationService notificationService = new NotificationService();
    private final int currentUserId = 1;

    // évite d’afficher plusieurs fois le popup durant la même session
    private boolean popupRappelDejaAffiche = false;

    @FXML
    public void initialize() {
        if (bannerBox != null) {
            FadeTransition ftBanner = new FadeTransition(Duration.millis(500), bannerBox);
            ftBanner.setFromValue(0);
            ftBanner.setToValue(1);
            ftBanner.play();
        }

        if (card1 != null) {
            FadeTransition ftCard1 = new FadeTransition(Duration.millis(400), card1);
            ftCard1.setFromValue(0);
            ftCard1.setToValue(1);
            ftCard1.play();
        }

        if (card2 != null) {
            FadeTransition ftCard2 = new FadeTransition(Duration.millis(500), card2);
            ftCard2.setFromValue(0);
            ftCard2.setToValue(1);
            ftCard2.play();
        }

        if (card3 != null) {
            FadeTransition ftCard3 = new FadeTransition(Duration.millis(600), card3);
            ftCard3.setFromValue(0);
            ftCard3.setToValue(1);
            ftCard3.play();
        }

        try {
            if (logoImage != null) {
                logoImage.setImage(new Image(getClass().getResourceAsStream("/Images/logo.png")));
            }
        } catch (Exception e) {
            System.out.println("Logo introuvable : /Images/logo.png");
        }

        if (notifPanel != null) {
            notifPanel.setVisible(false);
            notifPanel.setManaged(false);
            notifPanel.setOpacity(0);
        }

        configurerFiltreNotifications();
        configurerListViewNotifications();

        // 1) Vérification immédiate du rappel du jour au lancement
        verifierEtDeclencherRappelImmediat();

        // 2) Chargement normal des notifications ensuite
        chargerNotifications();
        mettreAJourBadgeNotif();
        setActiveNav(navAccueil);
    }

    /**
     * Vérifie immédiatement s'il faut créer une notification de rappel du jour.
     * Cette méthode suppose que NotificationService contient une méthode
     * qui vérifie s'il n'existe pas déjà un suivi aujourd'hui
     * et crée une notification "rappel_suivi" si nécessaire.
     *
     * Si chez toi le nom de la méthode est différent,
     * remplace seulement la ligne concernée.
     */
    private void verifierEtDeclencherRappelImmediat() {
        try {
            // Cette méthode doit :
            // - vérifier si l'utilisateur n'a pas encore ajouté de suivi aujourd'hui
            // - créer une notification de type "rappel_suivi" si besoin
            // - éviter les doublons pour la même date
            boolean rappelCree = notificationService.checkAndCreateDailyReminder(currentUserId);

            if (rappelCree && !popupRappelDejaAffiche) {
                popupRappelDejaAffiche = true;
                afficherPopupRappel(
                        "Vous n'avez pas encore ajouté votre suivi mental aujourd'hui 🧠"
                );
            }

        } catch (Exception e) {
            System.out.println("Erreur lors de la vérification immédiate du rappel : " + e.getMessage());
            e.printStackTrace();
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
        if (notifFilterCombo == null) {
            return;
        }

        notifFilterCombo.setItems(FXCollections.observableArrayList(
                "Tous",
                "progression_hausse",
                "progression_baisse",
                "progression_stable",
                "rappel_suivi"
        ));
        notifFilterCombo.setValue("Tous");
    }

    private void configurerListViewNotifications() {
        if (notifListView == null) {
            return;
        }

        notifListView.setCellFactory(listView -> new ListCell<Notification>() {
            @Override
            protected void updateItem(Notification notif, boolean empty) {
                super.updateItem(notif, empty);

                if (empty || notif == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                    return;
                }

                String iconText = getIconForType(notif.getType());

                Label iconLabel = new Label(iconText);
                iconLabel.setStyle(
                        "-fx-font-size: 16px;" +
                                "-fx-font-weight: bold;"
                );

                StackPane iconContainer = new StackPane(iconLabel);
                iconContainer.setMinSize(34, 34);
                iconContainer.setPrefSize(34, 34);
                iconContainer.setMaxSize(34, 34);
                iconContainer.setStyle(getIconContainerStyle(notif.getType()));

                Label titleLabel = new Label(
                        notif.getTitle() == null ? "Notification" : notif.getTitle()
                );
                titleLabel.setStyle(
                        "-fx-font-size: 13px;" +
                                "-fx-font-weight: bold;" +
                                "-fx-text-fill: #1E3A5F;"
                );

                Label messageLabel = new Label(
                        notif.getMessage() == null ? "" : notif.getMessage()
                );
                messageLabel.setWrapText(true);
                messageLabel.setStyle(
                        "-fx-font-size: 12px;" +
                                "-fx-text-fill: #5B6B7A;" +
                                "-fx-font-weight: 500;"
                );

                Label dateLabel = new Label(getRelativeTime(notif.getCreatedAt()));
                dateLabel.setStyle(
                        "-fx-font-size: 11px;" +
                                "-fx-text-fill: #8A9AAA;"
                );

                Circle unreadDot = new Circle(5);
                unreadDot.setFill(notif.isRead() ? Color.TRANSPARENT : Color.web("#E74C3C"));

                Button deleteBtn = new Button("✕");
                deleteBtn.setStyle(
                        "-fx-background-color: transparent;" +
                                "-fx-text-fill: #8A9AAA;" +
                                "-fx-font-size: 12px;" +
                                "-fx-font-weight: bold;" +
                                "-fx-cursor: hand;"
                );
                deleteBtn.setOnAction(e -> {
                    notificationService.deleteNotificationById(notif.getId());
                    chargerNotifications();
                    mettreAJourBadgeNotif();
                });

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                HBox topRow = new HBox(8, unreadDot, iconContainer, titleLabel, spacer, dateLabel, deleteBtn);
                topRow.setAlignment(Pos.CENTER_LEFT);

                VBox content = new VBox(6, topRow, messageLabel);
                content.setPadding(new Insets(12));
                content.setStyle(
                        "-fx-background-color: white;" +
                                "-fx-background-radius: 14;" +
                                "-fx-border-color: #E6EEF8;" +
                                "-fx-border-radius: 14;" +
                                "-fx-effect: dropshadow(gaussian, rgba(30,58,95,0.06), 8, 0, 0, 2);"
                );

                setGraphic(content);
                setText(null);
                setPadding(new Insets(4, 2, 4, 2));
                setStyle("-fx-background-color: transparent;");
            }
        });
    }

    private String getIconForType(String type) {
        if (type == null) {
            return "🔔";
        }

        switch (type) {
            case "progression_hausse":
                return "📈";
            case "progression_baisse":
                return "📉";
            case "progression_stable":
                return "➖";
            case "rappel_suivi":
                return "⏰";
            default:
                return "🔔";
        }
    }

    private String getIconContainerStyle(String type) {
        if (type == null) {
            return "-fx-background-color: #EEF4FB;" +
                    "-fx-background-radius: 999;" +
                    "-fx-border-color: #D8E5F2;" +
                    "-fx-border-radius: 999;";
        }

        switch (type) {
            case "progression_hausse":
                return "-fx-background-color: #E8F8F0;" +
                        "-fx-background-radius: 999;" +
                        "-fx-border-color: #BFE8D1;" +
                        "-fx-border-radius: 999;";

            case "progression_baisse":
                return "-fx-background-color: #FFF1F1;" +
                        "-fx-background-radius: 999;" +
                        "-fx-border-color: #F5C2C0;" +
                        "-fx-border-radius: 999;";

            case "progression_stable":
                return "-fx-background-color: #FFF8E8;" +
                        "-fx-background-radius: 999;" +
                        "-fx-border-color: #F1DFB3;" +
                        "-fx-border-radius: 999;";

            case "rappel_suivi":
                return "-fx-background-color: #EEF4FF;" +
                        "-fx-background-radius: 999;" +
                        "-fx-border-color: #C9D9F7;" +
                        "-fx-border-radius: 999;";

            default:
                return "-fx-background-color: #EEF4FB;" +
                        "-fx-background-radius: 999;" +
                        "-fx-border-color: #D8E5F2;" +
                        "-fx-border-radius: 999;";
        }
    }

    private String getRelativeTime(Timestamp timestamp) {
        if (timestamp == null) {
            return "";
        }

        LocalDateTime dateTime = timestamp.toLocalDateTime();
        LocalDateTime now = LocalDateTime.now();
        java.time.Duration duration = java.time.Duration.between(dateTime, now);

        long minutes = duration.toMinutes();
        long hours = duration.toHours();
        long days = duration.toDays();

        if (minutes < 1) {
            return "à l'instant";
        } else if (minutes < 60) {
            return "il y a " + minutes + " min";
        } else if (hours < 24) {
            return "il y a " + hours + " h";
        } else if (days < 7) {
            return "il y a " + days + " j";
        } else {
            return dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        }
    }

    private void chargerNotifications() {
        String filtre = notifFilterCombo != null ? notifFilterCombo.getValue() : "Tous";
        List<Notification> notifications = notificationService.getNotificationsByUserAndType(currentUserId, filtre);

        if (notifListView != null) {
            notifListView.setItems(FXCollections.observableArrayList(notifications));
        }
    }

    private void mettreAJourBadgeNotif() {
        if (notifBadgeLabel == null) {
            return;
        }

        int unreadCount = notificationService.countUnreadNotifications(currentUserId);
        notifBadgeLabel.setText(String.valueOf(unreadCount));

        if (unreadCount > 0) {
            notifBadgeLabel.setVisible(true);
            notifBadgeLabel.setManaged(true);
        } else {
            notifBadgeLabel.setVisible(false);
            notifBadgeLabel.setManaged(false);
        }
    }

    @FXML
    private void onNotifFilterChanged(ActionEvent event) {
        chargerNotifications();
    }

    @FXML
    private void onClearAllNotifications(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer toutes les notifications ?");
        alert.setContentText("Cette action supprimera toutes vos notifications.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            notificationService.deleteAllNotifications(currentUserId);
            chargerNotifications();
            mettreAJourBadgeNotif();
        }
    }

    private void loadContent(String fxmlPath) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentArea.getChildren().setAll(view);

            FadeTransition ft = new FadeTransition(Duration.millis(250), view);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showHomeContent() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(homeContent);

        if (notifPanel != null && !contentArea.getChildren().contains(notifPanel)) {
            contentArea.getChildren().add(notifPanel);
        }

        FadeTransition ft = new FadeTransition(Duration.millis(250), homeContent);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();

        // Revérification quand on revient à l'accueil
        verifierEtDeclencherRappelImmediat();
        chargerNotifications();
        mettreAJourBadgeNotif();
    }

    private void setActiveNav(HBox activeItem) {
        resetNavStyle(navAccueil);
        resetNavStyle(navSuivi);
        resetNavStyle(navObjectifs);
        resetNavStyle(navRdv);
        resetNavStyle(navForum);
        resetNavStyle(navActivites);
        resetNavStyle(navRessources);

        if (activeItem != null) {
            activeItem.setStyle(
                    "-fx-background-color: #1E4568;" +
                            "-fx-background-radius: 8;" +
                            "-fx-padding: 10 14;" +
                            "-fx-cursor: hand;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);"
            );
        }
    }

    private void resetNavStyle(HBox item) {
        if (item != null) {
            item.setStyle(
                    "-fx-background-color: transparent;" +
                            "-fx-background-radius: 8;" +
                            "-fx-padding: 10 14;" +
                            "-fx-cursor: hand;"
            );
        }
    }

    @FXML
    void onNavHomeClicked(MouseEvent event) {
        showHomeContent();
        setActiveNav(navAccueil);
    }

    @FXML
    private void onNavSuiviClicked(MouseEvent event) {
        loadContent("/suivi_mentale.fxml");
        setActiveNav(navSuivi);
    }

    @FXML
    private void onNavObjectifsClicked(MouseEvent event) {
        loadContent("/objvue.fxml");
        setActiveNav(navObjectifs);
    }

    @FXML
    private void onNavRdvClicked(MouseEvent event) {
        setActiveNav(navRdv);
    }

    @FXML
    private void onNavBlogClicked(MouseEvent event) {
        loadContent("/forum.fxml");
        setActiveNav(navForum);
    }

    @FXML
    private void onNavActivitesClicked(MouseEvent event) {
        setActiveNav(navActivites);
    }

    @FXML
    private void onNavRessourcesClicked(MouseEvent event) {
        setActiveNav(navRessources);
    }

    @FXML
    private void onCardSuiviClicked(MouseEvent event) {
        loadContent("/suivi_mentale.fxml");
        setActiveNav(navSuivi);
    }

    @FXML
    private void onNavHoverEnter(MouseEvent event) {
        HBox source = (HBox) event.getSource();

        ScaleTransition st = new ScaleTransition(Duration.millis(150), source);
        st.setToX(1.02);
        st.setToY(1.02);
        st.play();

        if (!source.getStyle().contains("#1E4568")) {
            source.setStyle(
                    "-fx-background-color: rgba(255,255,255,0.15);" +
                            "-fx-background-radius: 8;" +
                            "-fx-padding: 10 14;" +
                            "-fx-cursor: hand;"
            );
        }
    }

    @FXML
    private void onNavHoverExit(MouseEvent event) {
        HBox source = (HBox) event.getSource();

        ScaleTransition st = new ScaleTransition(Duration.millis(150), source);
        st.setToX(1);
        st.setToY(1);
        st.play();

        if (!source.getStyle().contains("#1E4568")) {
            resetNavStyle(source);
        }
    }

    @FXML
    private void onCardHoverEnter(MouseEvent event) {
        VBox card = (VBox) event.getSource();
        ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
        st.setToX(1.02);
        st.setToY(1.02);
        st.play();

        card.setStyle(
                "-fx-background-color: #F8FBFF;" +
                        "-fx-background-radius: 16;" +
                        "-fx-padding: 22;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 15, 0, 0, 4);" +
                        "-fx-cursor: hand;"
        );
    }

    @FXML
    private void onCardHoverExit(MouseEvent event) {
        VBox card = (VBox) event.getSource();
        ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
        st.setToX(1);
        st.setToY(1);
        st.play();

        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 16;" +
                        "-fx-padding: 22;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 2);" +
                        "-fx-cursor: hand;"
        );
    }

    @FXML
    private void onAdvantageHoverEnter(MouseEvent event) {
        VBox adv = (VBox) event.getSource();
        ScaleTransition st = new ScaleTransition(Duration.millis(150), adv);
        st.setToX(1.05);
        st.setToY(1.05);
        st.play();

        adv.setStyle(
                "-fx-padding: 10;" +
                        "-fx-background-radius: 12;" +
                        "-fx-background-color: rgba(151,187,228,0.15);" +
                        "-fx-cursor: hand;"
        );
    }

    @FXML
    private void onAdvantageHoverExit(MouseEvent event) {
        VBox adv = (VBox) event.getSource();
        ScaleTransition st = new ScaleTransition(Duration.millis(150), adv);
        st.setToX(1);
        st.setToY(1);
        st.play();

        adv.setStyle(
                "-fx-padding: 10;" +
                        "-fx-background-radius: 12;" +
                        "-fx-background-color: transparent;" +
                        "-fx-cursor: hand;"
        );
    }

    @FXML
    private void onNotifications(ActionEvent event) {
        if (notifPanel == null) {
            return;
        }

        boolean ouverture = !notifPanel.isVisible();
        notifPanel.setVisible(ouverture);
        notifPanel.setManaged(ouverture);

        if (ouverture) {
            chargerNotifications();
            notificationService.markAllAsRead(currentUserId);
            mettreAJourBadgeNotif();

            FadeTransition ft = new FadeTransition(Duration.millis(220), notifPanel);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();
        }
    }

    @FXML
    private void onLogout(ActionEvent event) {
        System.out.println("Logout");
    }

    @FXML
    private void onStartSuivi(ActionEvent event) {
        loadContent("/suivi_mentale.fxml");
        setActiveNav(navSuivi);
    }
}