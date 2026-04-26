package controllers;

import Models.RendezVous;
import Services.ServiceRendezVous;
import Models.Rating;
import Services.ServiceRating;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import utils.MyDataBase;

import java.sql.*;
import java.util.List;
import java.awt.Desktop;
import java.net.URI;

public class ControllerRdvEtudiant {

    @FXML private HBox psychologuesContainer;
    @FXML private VBox rdvAujContainer;
    @FXML private VBox rdvAvenirContainer;
    @FXML private VBox rdvAnciensContainer;
    @FXML private Label labelUserName, avatarInitials, badgeRdv;
    @FXML private Button notifButton, logoutButton;
    @FXML private ImageView logoImage;

    private final ServiceRendezVous serviceRdv = new ServiceRendezVous();
    private final ServiceRating serviceRating = new ServiceRating();


    private final int etudiantId  = 2;
    private final int psyIdDefaut = 6;


    @FXML
    public void initialize() {
        logoImage.setImage(new Image(getClass().getResourceAsStream("/Images/logo.png")));
        chargerPsychologues();
        chargerRdvAujourdhui();
        chargerRdvAvenir();
        chargerRdvAnciens();
        FadeTransition ft = new FadeTransition(Duration.millis(400), psychologuesContainer);
        ft.setFromValue(0); ft.setToValue(1); ft.play();
    }

    private void chargerPsychologues() {
        psychologuesContainer.getChildren().clear();
        boolean found = false;

        Connection conn = MyDataBase.getInstance().getCnx();

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT id, prenom, nom FROM user WHERE role = 'psychologue'"
             )) {

            while (rs.next()) {
                found = true;
                int id = rs.getInt("id");
                String prenom = rs.getString("prenom");
                String nom = rs.getString("nom");
                System.out.println("[chargerPsychologues] Psy trouvé: id=" + id + ", nom=Dr. " + prenom + " " + nom);

                psychologuesContainer.getChildren().add(
                        createPsyCard(id, "Dr. " + prenom + " " + nom, "Psychologue")
                );
            }

        } catch (SQLException e) {
            System.err.println("[chargerPsychologues] " + e.getMessage());
        }
        if (!found) {
            System.out.println("[chargerPsychologues] Aucun psychologue trouvé dans la table user !");
        }
    }




    private VBox createPsyCard(int psyId, String nom, String specialite) {
        VBox card = new VBox(10);
        card.setAlignment(javafx.geometry.Pos.CENTER);
        card.setPrefWidth(180);
        card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 16; -fx-padding: 22 18;" +
                        "-fx-border-color: rgba(44,95,138,0.12); -fx-border-radius: 16; -fx-border-width: 1;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 2); -fx-cursor: hand;"
        );

        StackPane avatar = new StackPane();
        Circle cercle = new Circle(30);
        cercle.setStyle("-fx-fill: linear-gradient(to bottom, #7BA6C9, #2C5F8A);");
        // Initiales sécurisées
        String initiales = nom.length() > 4
                ? nom.substring(4, Math.min(nom.length(), 6)).toUpperCase()
                : nom.substring(0, Math.min(nom.length(), 2)).toUpperCase();
        Label lblInit = new Label(initiales);
        lblInit.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
        avatar.getChildren().addAll(cercle, lblInit);

        Label labelNom = new Label(nom);
        labelNom.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");
        labelNom.setAlignment(javafx.geometry.Pos.CENTER);
        labelNom.setWrapText(true);

        Label labelSpe = new Label(specialite);
        labelSpe.setStyle("-fx-font-size: 12px; -fx-text-fill: #5A6C7D;");
        labelSpe.setAlignment(javafx.geometry.Pos.CENTER);

        Button btnChoisir = new Button("Choisir");
        btnChoisir.setStyle(
                "-fx-background-color: linear-gradient(to right, #7BA6C9, #2C5F8A);" +
                        "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;" +
                        "-fx-background-radius: 8; -fx-padding: 8 24; -fx-cursor: hand;"
        );
        btnChoisir.setPrefWidth(130);
        btnChoisir.setOnAction(e -> ouvrirCalendrier(psyId, nom));

        card.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(150), card);
            st.setToX(1.03); st.setToY(1.03); st.play();
        });
        card.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(150), card);
            st.setToX(1); st.setToY(1); st.play();
        });

        // Rating Stars
        double avg = serviceRating.getAverageForPsy(psyId);
        HBox stars = createStarsDisplay(avg);
        
        Button btnRate = new Button("Noter ★");
        btnRate.setStyle("-fx-background-color: transparent; -fx-text-fill: #2C5F8A; -fx-font-size: 11px; -fx-underline: true; -fx-cursor: hand;");
        btnRate.setOnAction(e -> showRatingDialog(psyId, nom));

        card.getChildren().addAll(avatar, labelNom, labelSpe, stars, btnRate, btnChoisir);
        return card;
    }

    private HBox createStarsDisplay(double avg) {
        HBox container = new HBox(2);
        container.setAlignment(javafx.geometry.Pos.CENTER);
        int fullStars = (int) Math.round(avg);
        for (int i = 1; i <= 5; i++) {
            Label star = new Label(i <= fullStars ? "★" : "☆");
            star.setStyle("-fx-text-fill: #F1C40F; -fx-font-size: 14px;");
            container.getChildren().add(star);
        }
        Label lblAvg = new Label(String.format(" (%.1f)", avg));
        lblAvg.setStyle("-fx-font-size: 10px; -fx-text-fill: #94A3B8;");
        container.getChildren().add(lblAvg);
        return container;
    }

    private void showRatingDialog(int psyId, String psyNom) {
        // Vérifier si l'étudiant a eu au moins une consultation terminée avec ce psy
        if (!serviceRdv.hasHadConsultation(etudiantId, psyId)) {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
            alert.setTitle("Accès restreint");
            alert.setHeaderText("Vote impossible");
            alert.setContentText("Vous ne pouvez noter que les psychologues avec qui vous avez déjà eu une consultation terminée.");
            alert.showAndWait();
            return;
        }

        javafx.scene.control.ChoiceDialog<Integer> dialog = new javafx.scene.control.ChoiceDialog<>(5, 1, 2, 3, 4, 5);
        dialog.setTitle("Noter le praticien");
        dialog.setHeaderText("Quelle note donnez-vous au " + psyNom + " ?");
        dialog.setContentText("Note (sur 5) :");
        
        dialog.showAndWait().ifPresent(note -> {
            Rating r = new Rating(etudiantId, psyId, note, "Note via dashboard");
            serviceRating.add(r);
            chargerPsychologues(); // Refresh
        });
    }

    // ══════════════════════════════════════════════════════
    //  RDV ÉTUDIANT
    // ══════════════════════════════════════════════════════
    private void chargerRdvAujourdhui() {
        rdvAujContainer.getChildren().clear();
        List<RendezVous> list = serviceRdv.getRdvAujourdhui(etudiantId);
        if (list.isEmpty())
            rdvAujContainer.getChildren().add(emptyLabel("Rien de prévu pour le moment."));
        else
            list.forEach(r -> rdvAujContainer.getChildren().add(createRdvCard(r, false)));
    }

    private void chargerRdvAvenir() {
        rdvAvenirContainer.getChildren().clear();
        List<RendezVous> list = serviceRdv.getRdvAvenir(etudiantId);
        if (list.isEmpty())
            rdvAvenirContainer.getChildren().add(emptyLabel("Aucun rendez-vous à venir."));
        else
            list.forEach(r -> rdvAvenirContainer.getChildren().add(createRdvCard(r, true)));
    }

    private void chargerRdvAnciens() {
        rdvAnciensContainer.getChildren().clear();
        List<RendezVous> list = serviceRdv.getRdvAnciens(etudiantId);
        if (list.isEmpty())
            rdvAnciensContainer.getChildren().add(emptyLabel("Aucun historique disponible."));
        else
            list.forEach(r -> rdvAnciensContainer.getChildren().add(createRdvCard(r, false)));
    }

    private HBox createRdvCard(RendezVous r, boolean canCancel) {
        HBox card = new HBox(14);
        card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        card.setStyle(
                "-fx-background-color: #F8FBFF; -fx-background-radius: 12; -fx-padding: 14 18;" +
                        "-fx-border-color: rgba(44,95,138,0.12); -fx-border-radius: 12; -fx-border-width: 1;"
        );

        String couleur = getCouleurStatut(r.getStatut());

        VBox barre = new VBox();
        barre.setPrefWidth(5); barre.setPrefHeight(50);
        barre.setStyle("-fx-background-color: " + couleur + "; -fx-background-radius: 3;");

        Label icone = new Label(getIconeType(r.getTypeRdv()));
        icone.setStyle("-fx-font-size: 22px;");

        VBox infos = new VBox(4);
        Label lblDateHeure = new Label(
                formatDate(r.getDate()) + "  •  " +
                        formatTime(r.getHeureDebut()) + " – " + formatTime(r.getHeureFin())
        );
        lblDateHeure.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");
        Label lblType = new Label(r.getTypeRdv() != null ? r.getTypeRdv() : "Consultation");
        lblType.setStyle("-fx-font-size: 12px; -fx-text-fill: #5A6C7D;");
        infos.getChildren().addAll(lblDateHeure, lblType);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label badge = new Label(r.getStatut() != null ? r.getStatut().toUpperCase() : "");
        badge.setStyle(
                "-fx-background-color: " + couleur + "22; -fx-text-fill: " + couleur + ";" +
                        "-fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 3 10;"
        );

        card.getChildren().addAll(barre, icone, infos, spacer);



        card.getChildren().add(badge);
        
        if ("confirmé".equalsIgnoreCase(r.getStatut()) && r.getLienMeet() != null && !r.getLienMeet().isEmpty()) {
            Button btnMeet = new Button("🎥 Meet");
            btnMeet.setStyle(
                    "-fx-background-color: #E3F2FD; -fx-text-fill: #1565C0; -fx-font-weight: bold;" +
                    "-fx-font-size: 12px; -fx-background-radius: 7; -fx-padding: 6 14; -fx-cursor: hand;" +
                    "-fx-border-color: rgba(21,101,192,0.3); -fx-border-radius: 7; -fx-border-width: 1;"
            );
            btnMeet.setOnAction(e -> openBrowser(r.getLienMeet()));
            card.getChildren().add(btnMeet);
        }

        if (canCancel && ("réservé".equalsIgnoreCase(r.getStatut()) || "en attente".equalsIgnoreCase(r.getStatut()))) {
            Button btnAnnuler = new Button("Annuler");
            btnAnnuler.setStyle(
                    "-fx-background-color: #FFF0F0; -fx-text-fill: #E74C3C; -fx-font-weight: bold;" +
                            "-fx-font-size: 12px; -fx-background-radius: 7; -fx-padding: 6 14; -fx-cursor: hand;" +
                            "-fx-border-color: rgba(231,76,60,0.3); -fx-border-radius: 7; -fx-border-width: 1;"
            );
            btnAnnuler.setOnAction(e -> {
                serviceRdv.annulerReservation(r.getId(), etudiantId);
                chargerRdvAvenir();
                chargerRdvAnciens();
            });
            card.getChildren().add(btnAnnuler);
        }
        return card;
    }



    // ══════════════════════════════════════════════════════
    //  OUVRIR CALENDRIER
    // ══════════════════════════════════════════════════════
    private void ouvrirCalendrier(int psyId, String psyNom) {
        try {
            System.out.println("[ouvrirCalendrier] psyId=" + psyId + ", psyNom=" + psyNom + ", etudiantId=" + etudiantId);
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/gui/RendezVous_Calendrier.fxml")
            );
            Parent root = loader.load();
            ControllerRdvCalendrier ctrl = loader.getController();
            ctrl.initData(psyId, psyNom, etudiantId);

            Stage stage = (Stage) psychologuesContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            FadeTransition ft = new FadeTransition(Duration.millis(300), root);
            ft.setFromValue(0); ft.setToValue(1); ft.play();
            stage.show();
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ══════════════════════════════════════════════════════
    //  NAVIGATION
    // ══════════════════════════════════════════════════════
    private void loadPage(String fxml, MouseEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            FadeTransition ft = new FadeTransition(Duration.millis(300), root);
            ft.setFromValue(0); ft.setToValue(1); ft.play();
            stage.show();
            stage.centerOnScreen();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void onNavHomeClicked(MouseEvent e)  { loadPage("/gui/Home.fxml", e); }
    @FXML private void onNavSuiviClicked(MouseEvent e) {}
    @FXML private void onNavForumClicked(MouseEvent e) { loadPage("/forum.fxml", e); }

    @FXML private void onNavHoverEnter(MouseEvent event) {
        ((HBox) event.getSource()).setStyle(
                "-fx-background-color: rgba(255,255,255,0.15); -fx-background-radius: 8; -fx-padding: 10 14; -fx-cursor: hand;"
        );
    }
    @FXML private void onNavHoverExit(MouseEvent event) {
        ((HBox) event.getSource()).setStyle(
                "-fx-background-color: transparent; -fx-background-radius: 8; -fx-padding: 10 14; -fx-cursor: hand;"
        );
    }
    @FXML private void onNotifications(ActionEvent e) {}
    @FXML private void onLogout(ActionEvent e) {}

    // ══════════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════════
    private Label emptyLabel(String msg) {
        Label l = new Label(msg);
        l.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 13px; -fx-padding: 8 0;");
        return l;
    }

    private String getCouleurStatut(String statut) {
        if (statut == null) return "#95A5A6";
        return switch (statut.toLowerCase()) {
            case "libre", "disponible" -> "#27AE60";
            case "confirmé"            -> "#2980B9";
            case "en attente", "réservé" -> "#E67E22";
            default                    -> "#95A5A6";
        };
    }

    private String getIconeType(String type) {
        if (type == null) return "📋";
        String t = type.toLowerCase();
        if (t.contains("vidéo") || t.contains("meet")) return "🎥";
        if (t.contains("téléphone"))                   return "📞";
        if (t.contains("urgence"))                     return "🚨";
        return "🏥";
    }

    private String formatDate(java.sql.Date date) {
        if (date == null) return "—";
        java.time.LocalDate ld = date.toLocalDate();
        String[] mois = {"jan.","fév.","mar.","avr.","mai","juin",
                "juil.","août","sep.","oct.","nov.","déc."};
        return ld.getDayOfMonth() + " " + mois[ld.getMonthValue()-1] + " " + ld.getYear();
    }

    private String formatTime(java.sql.Time time) {
        if (time == null) return "--:--";
        return time.toString().substring(0, 5);
    }

    private void openBrowser(String url) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}