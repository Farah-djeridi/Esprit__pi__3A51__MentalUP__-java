package Controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Activite;
import models.Reservation;
import services.ServiceActivite;
import services.ServiceNotation;
import services.ServiceReservation;
import utils.CaptchaVerification;
import utils.CaptchaAvance;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class EtudiantActivitesController implements Initializable {

    @FXML private GridPane activitesGrid;
    @FXML private TextField searchField;

    private ServiceActivite serviceActivite;
    private ServiceReservation serviceReservation;
    private ServiceNotation serviceNotation;
    private List<Activite> toutesActivites = new java.util.ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        serviceActivite    = new ServiceActivite();
        serviceReservation = new ServiceReservation();
        serviceNotation    = new ServiceNotation();
        corrigerCoordonnees();
        chargerActivites();

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                afficherActivites(toutesActivites);
            } else {
                String lower = newVal.toLowerCase();
                List<Activite> filtrees = toutesActivites.stream()
                        .filter(a -> a.getTitre().toLowerCase().contains(lower)
                                  || a.getType().toLowerCase().contains(lower)
                                  || a.getAdresse().toLowerCase().contains(lower))
                        .toList();
                afficherActivites(filtrees);
            }
        });
    }

    // Assigner des coordonnées Tunis aux activités sans coordonnées
    private void corrigerCoordonnees() {
        // Avenue Habib Bourguiba, Tunis — zone urbaine dense avec données OSM complètes
        try {
            java.sql.Connection conn = utils.MyDataBase.getInstance().getConnection();
            // Réinitialiser les coordonnées incorrectes (hors zone urbaine)
            conn.createStatement().executeUpdate(
                "UPDATE activite SET latitude=0, longitude=0 " +
                "WHERE (latitude < 36.7 OR latitude > 37.0 OR longitude < 9.8 OR longitude > 10.6) " +
                "AND latitude != 0");
            // Assigner des coordonnées valides
            java.sql.ResultSet rs = conn.createStatement()
                    .executeQuery("SELECT id_activite FROM activite WHERE latitude=0 AND longitude=0");
            while (rs.next()) {
                int id = rs.getInt("id_activite");
                // Autour de l'Avenue Bourguiba — toutes dans la même zone urbaine
                double lat = 36.8190 + (id % 3) * 0.002;
                double lon = 10.1658 + (id % 3) * 0.002;
                java.sql.PreparedStatement ps = conn.prepareStatement(
                        "UPDATE activite SET latitude=?, longitude=? WHERE id_activite=?");
                ps.setDouble(1, lat);
                ps.setDouble(2, lon);
                ps.setInt(3, id);
                ps.executeUpdate();
            }
        } catch (Exception e) {
            System.err.println("corrigerCoordonnees: " + e.getMessage());
        }
    }

    // ─── Assistant ────────────────────────────────────────────────────────────

    @FXML
    private void ouvrirAssistant() {
        Stage owner = (Stage) activitesGrid.getScene().getWindow();
        new AssistantController().ouvrirAssistant(owner);
    }

    @FXML
    private void ouvrirAssistantVocal() {
        Stage owner = (Stage) activitesGrid.getScene().getWindow();
        new AssistantVocalController().demarrerEcoute(owner, texteReconnu -> {
            if (texteReconnu == null || texteReconnu.isEmpty()) {
                afficherToast("Aucune voix détectée. Réessayez.", "#ed8936", "⚠️");
                return;
            }

            // Chercher l'activité correspondante
            Activite trouvee = AssistantVocalController.rechercherActivite(texteReconnu, toutesActivites);

            if (trouvee == null) {
                afficherToast("Activité \"" + texteReconnu + "\" introuvable.", "#e53e3e", "❌");
                return;
            }

            afficherToast("Activité trouvée : " + trouvee.getTitre(), "#27ae60", "✅");

            // Trouver la carte correspondante dans le GridPane et la faire clignoter
            final Activite activiteTrouvee = trouvee;
            javafx.application.Platform.runLater(() -> {
                activitesGrid.getChildren().stream()
                    .filter(node -> node instanceof VBox)
                    .map(node -> (VBox) node)
                    .filter(card -> activiteTrouvee.equals(card.getUserData()))
                    .findFirst()
                    .ifPresent(card -> {
                        card.requestFocus();
                        AssistantVocalController.clignoterCarte(card);
                    });
            });
        });
    }

    // ─── Chargement ──────────────────────────────────────────────────────────

    private void chargerActivites() {
        try {
            toutesActivites = serviceActivite.getAllActivites();
            afficherActivites(toutesActivites);
        } catch (SQLException e) {
            System.err.println("Erreur chargement activités: " + e.getMessage());
        }
    }

    private void afficherActivites(List<Activite> activites) {
        activitesGrid.getChildren().clear();
        int column = 0, row = 0, index = 0;
        for (Activite activite : activites) {
            VBox card = creerCarteActivite(activite, index++);
            GridPane.setHgrow(card, Priority.ALWAYS);
            activitesGrid.add(card, column, row);
            if (++column == 2) { column = 0; row++; }
        }
    }

    // ─── Carte activité ──────────────────────────────────────────────────────

    private VBox creerCarteActivite(Activite activite, int index) {
        VBox card = new VBox(0);
        card.setAlignment(Pos.TOP_LEFT);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setUserData(activite); // stocker l'activité pour la recherche vocale
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; " +
                      "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);");

        // ── Carte OpenStreetMap ───────────────────────────────────────────────
        double lat = activite.getLatitude();
        double lon = activite.getLongitude();
        if (lat == 0.0 && lon == 0.0) { lat = 36.8065; lon = 10.1815; }
        javafx.scene.layout.StackPane mapView = creerCarteOSM(lat, lon);

        // ── Contenu texte ─────────────────────────────────────────────────────
        VBox content = new VBox(10);
        content.setPadding(new Insets(16));

        Label typeLabel = new Label(getIconeType(activite.getType()) + "  " + activite.getType());
        typeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b9fb5; -fx-font-weight: bold; " +
                           "-fx-background-color: #e8f4f8; -fx-padding: 5 10; -fx-background-radius: 15;");

        Label titreLabel = new Label(activite.getTitre());
        titreLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2d3748; -fx-wrap-text: true;");
        titreLabel.setMaxWidth(Double.MAX_VALUE);

        Label descLabel = new Label(activite.getDescription());
        descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #718096; -fx-wrap-text: true;");
        descLabel.setMaxWidth(Double.MAX_VALUE);
        descLabel.setMaxHeight(50);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        Label datesLabel = new Label("📅 Du " + activite.getDateDebut().format(fmt)
                + " au " + activite.getDateFin().format(fmt));
        datesLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #4a5568;");

        long duree = java.time.temporal.ChronoUnit.DAYS.between(
                activite.getDateDebut(), activite.getDateFin());
        Label dureeLabel = new Label("⏱ Durée : " + duree + " jours");
        dureeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #4a5568;");

        int nbRes = 0;
        try { nbRes = serviceReservation.getNombreReservations(activite.getIdActivite()); }
        catch (SQLException ignored) {}
        Label resLabel = new Label("👥 " + nbRes + " réservation(s)");
        resLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #718096;");

        boolean disponible = LocalDate.now().isBefore(activite.getDateFin());
        Label statutLabel = new Label(disponible ? "✅ Disponible !" : "❌ Terminée");
        statutLabel.setStyle(disponible
                ? "-fx-background-color: #e6f9f0; -fx-text-fill: #27ae60; -fx-padding: 6 14; -fx-background-radius: 8; -fx-font-weight: bold; -fx-font-size: 12px;"
                : "-fx-background-color: #f0f0f0; -fx-text-fill: #999; -fx-padding: 6 14; -fx-background-radius: 8; -fx-font-weight: bold; -fx-font-size: 12px;");

        Button btnReserver = new Button("🎟 Réserver");
        btnReserver.setDisable(!disponible);
        btnReserver.setStyle(disponible
                ? "-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 10 25; -fx-cursor: hand; -fx-background-radius: 8;"
                : "-fx-background-color: #ccc; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 10 25; -fx-background-radius: 8;");
        btnReserver.setOnAction(e -> {
            System.out.println("🎯 Bouton réserver cliqué pour: " + activite.getTitre());
            Stage parentStage = (Stage) activitesGrid.getScene().getWindow();
            
            // Choisir le type de CAPTCHA aléatoirement
            int typeCaptcha = new java.util.Random().nextInt(2);
            System.out.println("🎲 Type CAPTCHA choisi: " + typeCaptcha + " (0=Avancé, 1=Verification)");
            
            if (typeCaptcha == 0) {
                CaptchaAvance.afficherCaptchaAvance(
                    parentStage, 
                    "réserver cette activité", 
                    success -> {
                        System.out.println("🔐 Résultat CAPTCHA avancé: " + (success ? "SUCCÈS" : "ÉCHEC"));
                        if (success) {
                            javafx.application.Platform.runLater(() -> {
                                System.out.println("🎟️ Ouverture sélection de place...");
                                try {
                                    ouvrirSelectionPlace(activite);
                                    System.out.println("✅ Sélection de place ouverte avec succès");
                                } catch (Exception ex) {
                                    System.err.println("❌ Erreur ouverture sélection: " + ex.getMessage());
                                    ex.printStackTrace();
                                }
                            });
                        }
                    }
                );
            } else {
                CaptchaVerification.afficherVerification(
                    parentStage, 
                    "réserver cette activité", 
                    success -> {
                        System.out.println("🔐 Résultat CAPTCHA verification: " + (success ? "SUCCÈS" : "ÉCHEC"));
                        if (success) {
                            javafx.application.Platform.runLater(() -> {
                                System.out.println("🎟️ Ouverture sélection de place...");
                                try {
                                    ouvrirSelectionPlace(activite);
                                    System.out.println("✅ Sélection de place ouverte avec succès");
                                } catch (Exception ex) {
                                    System.err.println("❌ Erreur ouverture sélection: " + ex.getMessage());
                                    ex.printStackTrace();
                                }
                            });
                        }
                    }
                );
            }
        });

        Button btnTicket = new Button("🎫");
        btnTicket.setStyle("-fx-background-color: #5a67d8; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 14; -fx-cursor: hand; -fx-background-radius: 8;");
        btnTicket.setOnMouseEntered(e -> btnTicket.setStyle("-fx-background-color: #434190; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 14; -fx-cursor: hand; -fx-background-radius: 8;"));
        btnTicket.setOnMouseExited(e  -> btnTicket.setStyle("-fx-background-color: #5a67d8; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 14; -fx-cursor: hand; -fx-background-radius: 8;"));
        btnTicket.setOnAction(e -> afficherTicket(activite));

        // Bouton Jouer pour les activités de type "Jeux"
        HBox footer;
        if (activite.getType() != null && activite.getType().toLowerCase().contains("jeu")) {
            Button btnJouer = new Button("🎮");
            btnJouer.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 14; -fx-cursor: hand; -fx-background-radius: 8;");
            btnJouer.setOnMouseEntered(e -> btnJouer.setStyle("-fx-background-color: #8e44ad; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 14; -fx-cursor: hand; -fx-background-radius: 8;"));
            btnJouer.setOnMouseExited(e  -> btnJouer.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 14; -fx-cursor: hand; -fx-background-radius: 8;"));
            btnJouer.setOnAction(e -> {
                Stage parentStage = (Stage) activitesGrid.getScene().getWindow();
                afficherPopupChoixJeu(parentStage, activite.getTitre());
            });
            
            footer = new HBox(10, statutLabel, btnReserver, btnTicket, btnJouer);
        } else {
            footer = new HBox(10, statutLabel, btnReserver, btnTicket);
        }
        footer.setAlignment(Pos.CENTER_LEFT);
        VBox.setMargin(footer, new Insets(8, 0, 0, 0));

        // ── Notation étoiles ──────────────────────────────────────────────────
        HBox starsRow = creerLigneEtoiles(activite);

        content.getChildren().addAll(typeLabel, titreLabel, descLabel, datesLabel, dureeLabel, resLabel, starsRow, footer);
        card.getChildren().addAll(mapView, content);
        return card;
    }

    // ─── Notation étoiles ────────────────────────────────────────────────────

    private HBox creerLigneEtoiles(Activite activite) {
        HBox row = new HBox(6);
        row.setAlignment(Pos.CENTER_LEFT);

        // Récupérer note actuelle de l'étudiant et moyenne
        int[] maNote = {0};
        double[] moyenne = {0.0};
        int[] nbNotes = {0};
        try {
            maNote[0]  = serviceNotation.getNoteEtudiant(activite.getIdActivite(), "Sophie Am.");
            moyenne[0] = serviceNotation.getMoyenne(activite.getIdActivite());
            nbNotes[0] = serviceNotation.getNombreNotes(activite.getIdActivite());
        } catch (Exception ignored) {}

        // 5 étoiles interactives
        Label[] etoiles = new Label[5];
        for (int i = 0; i < 5; i++) {
            etoiles[i] = new Label(i < maNote[0] ? "★" : "☆");
            etoiles[i].setStyle("-fx-font-size: 16px; -fx-text-fill: " +
                                (i < maNote[0] ? "#f6ad55" : "#cbd5e0") +
                                "; -fx-cursor: hand;");
            final int note = i + 1;
            // Hover
            etoiles[i].setOnMouseEntered(e -> {
                for (int j = 0; j < 5; j++)
                    etoiles[j].setStyle("-fx-font-size: 16px; -fx-text-fill: " +
                                        (j < note ? "#f6ad55" : "#cbd5e0") + "; -fx-cursor: hand;");
            });
            etoiles[i].setOnMouseExited(e -> {
                for (int j = 0; j < 5; j++)
                    etoiles[j].setStyle("-fx-font-size: 16px; -fx-text-fill: " +
                                        (j < maNote[0] ? "#f6ad55" : "#cbd5e0") + "; -fx-cursor: hand;");
            });
            // Clic → vérification CAPTCHA puis notation
            etoiles[i].setOnMouseClicked(e -> {
                Stage parentStage = (Stage) activitesGrid.getScene().getWindow();
                
                // Alterner entre CAPTCHA simple et avancé
                boolean utiliserCaptchaAvance = new java.util.Random().nextBoolean();
                
                if (utiliserCaptchaAvance) {
                    CaptchaAvance.afficherCaptchaAvance(
                        parentStage, 
                        "noter cette activité", 
                        success -> {
                            if (success) {
                                javafx.application.Platform.runLater(() -> {
                                    ouvrirPopupNotation(activite, note, etoiles, maNote);
                                });
                            }
                        }
                    );
                } else {
                    CaptchaVerification.afficherVerification(
                        parentStage, 
                        "noter cette activité", 
                        success -> {
                            if (success) {
                                javafx.application.Platform.runLater(() -> {
                                    ouvrirPopupNotation(activite, note, etoiles, maNote);
                                });
                            }
                        }
                    );
                }
            });
            row.getChildren().add(etoiles[i]);
        }

        // Moyenne affichée
        Label lblMoy = new Label(nbNotes[0] > 0
                ? String.format("%.1f (%d avis)", moyenne[0], nbNotes[0])
                : "Pas encore noté");
        lblMoy.setStyle("-fx-font-size: 10px; -fx-text-fill: #718096;");
        row.getChildren().add(lblMoy);

        return row;
    }

    private void ouvrirPopupNotation(Activite activite, int noteChoisie,
                                     Label[] etoiles, int[] maNote) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.initStyle(javafx.stage.StageStyle.UNDECORATED);

        VBox root = new VBox(16);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(28));
        root.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                      "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 20, 0, 0, 5);");
        root.setPrefWidth(360);

        Label titre = new Label("⭐ Noter l'activité");
        titre.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");

        Label nomAct = new Label(activite.getTitre());
        nomAct.setStyle("-fx-font-size: 13px; -fx-text-fill: #718096;");

        // Étoiles dans le popup
        HBox starsPopup = new HBox(8);
        starsPopup.setAlignment(Pos.CENTER);
        Label[] popupStars = new Label[5];
        int[] selected = {noteChoisie};

        for (int i = 0; i < 5; i++) {
            popupStars[i] = new Label(i < noteChoisie ? "★" : "☆");
            popupStars[i].setStyle("-fx-font-size: 28px; -fx-text-fill: " +
                                   (i < noteChoisie ? "#f6ad55" : "#cbd5e0") + "; -fx-cursor: hand;");
            final int n = i + 1;
            popupStars[i].setOnMouseEntered(e -> {
                for (int j = 0; j < 5; j++)
                    popupStars[j].setStyle("-fx-font-size: 28px; -fx-text-fill: " +
                                           (j < n ? "#f6ad55" : "#cbd5e0") + "; -fx-cursor: hand;");
            });
            popupStars[i].setOnMouseExited(e -> {
                for (int j = 0; j < 5; j++)
                    popupStars[j].setStyle("-fx-font-size: 28px; -fx-text-fill: " +
                                           (j < selected[0] ? "#f6ad55" : "#cbd5e0") + "; -fx-cursor: hand;");
            });
            popupStars[i].setOnMouseClicked(e -> {
                selected[0] = n;
                for (int j = 0; j < 5; j++)
                    popupStars[j].setStyle("-fx-font-size: 28px; -fx-text-fill: " +
                                           (j < n ? "#f6ad55" : "#cbd5e0") + "; -fx-cursor: hand;");
            });
            starsPopup.getChildren().add(popupStars[i]);
        }

        // Commentaire
        TextArea tfComment = new TextArea();
        tfComment.setPromptText("Votre commentaire (optionnel)...");
        tfComment.setPrefRowCount(3);
        tfComment.setWrapText(true);
        tfComment.setStyle("-fx-font-size: 12px; -fx-border-color: #e2e8f0; -fx-border-width: 1; " +
                           "-fx-border-radius: 8; -fx-background-radius: 8;");

        Button btnValider = new Button("✅ Valider ma note");
        btnValider.setMaxWidth(Double.MAX_VALUE);
        btnValider.setStyle("-fx-background-color: #f6ad55; -fx-text-fill: white; -fx-font-size: 13px; " +
                            "-fx-font-weight: bold; -fx-padding: 11; -fx-background-radius: 10; -fx-cursor: hand;");
        btnValider.setOnAction(e -> {
            try {
                serviceNotation.noterActivite(activite.getIdActivite(), "Sophie Am.",
                        selected[0], tfComment.getText().trim());
                maNote[0] = selected[0];
                // Mettre à jour les étoiles dans la carte
                for (int j = 0; j < 5; j++)
                    etoiles[j].setStyle("-fx-font-size: 16px; -fx-text-fill: " +
                                        (j < selected[0] ? "#f6ad55" : "#cbd5e0") + "; -fx-cursor: hand;");
                popup.close();
                afficherToast("Note " + selected[0] + "★ enregistrée !", "#f6ad55", "⭐");
            } catch (Exception ex) {
                afficherToast("Erreur: " + ex.getMessage(), "#e53e3e", "❌");
            }
        });

        Button btnAnnuler = new Button("Annuler");
        btnAnnuler.setMaxWidth(Double.MAX_VALUE);
        btnAnnuler.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #4a5568; -fx-font-size: 13px; " +
                            "-fx-padding: 11; -fx-background-radius: 10; -fx-cursor: hand;");
        btnAnnuler.setOnAction(e -> popup.close());

        root.getChildren().addAll(titre, nomAct, starsPopup, tfComment, btnValider, btnAnnuler);

        javafx.scene.Scene scene = new javafx.scene.Scene(root);
        scene.setFill(Color.TRANSPARENT);
        popup.setScene(scene);
        popup.show();

        javafx.geometry.Rectangle2D screen = javafx.stage.Screen.getPrimary().getVisualBounds();
        popup.setX(screen.getWidth() / 2 - 180);
        popup.setY(screen.getHeight() / 2 - 180);
    }

    // ─── Bannière jeu (pas de carte OSM pour les activités Jeux) ─────────────

    private javafx.scene.layout.StackPane creerBanniereJeu(String nomActivite) {
        final int H = 160;

        // Fond violet dégradé via Canvas
        javafx.scene.canvas.Canvas canvas = new javafx.scene.canvas.Canvas(600, H);
        javafx.scene.canvas.GraphicsContext gc = canvas.getGraphicsContext2D();

        javafx.scene.paint.LinearGradient grad = new javafx.scene.paint.LinearGradient(
                0, 0, 1, 1, true, javafx.scene.paint.CycleMethod.NO_CYCLE,
                new javafx.scene.paint.Stop(0, javafx.scene.paint.Color.web("#7c3aed")),
                new javafx.scene.paint.Stop(1, javafx.scene.paint.Color.web("#4c1d95")));
        gc.setFill(grad);
        gc.fillRect(0, 0, 600, H);

        // Cercles décoratifs
        gc.setFill(javafx.scene.paint.Color.web("#ffffff", 0.06));
        gc.fillOval(-30, -30, 160, 160);
        gc.fillOval(460, 60, 120, 120);
        gc.fillOval(200, -20, 80, 80);

        // Icône manette dessinée en formes
        double cx = 300, cy = 80;
        gc.setFill(javafx.scene.paint.Color.web("#ffffff", 0.9));
        // Corps manette
        gc.fillRoundRect(cx - 40, cy - 18, 80, 36, 18, 18);
        // Poignées
        gc.fillRoundRect(cx - 44, cy + 8, 22, 22, 10, 10);
        gc.fillRoundRect(cx + 22, cy + 8, 22, 22, 10, 10);
        // Croix directionnelle (gauche)
        gc.setFill(javafx.scene.paint.Color.web("#7c3aed"));
        gc.fillRect(cx - 32, cy - 6, 14, 5);
        gc.fillRect(cx - 27, cy - 10, 5, 14);
        // Boutons (droite)
        gc.setFill(javafx.scene.paint.Color.web("#f87171"));
        gc.fillOval(cx + 18, cy - 10, 8, 8);
        gc.setFill(javafx.scene.paint.Color.web("#34d399"));
        gc.fillOval(cx + 28, cy - 2, 8, 8);

        // Titre
        gc.setFill(javafx.scene.paint.Color.WHITE);
        gc.setFont(javafx.scene.text.Font.font("Arial",
                javafx.scene.text.FontWeight.BOLD, 15));
        double tw = nomActivite.length() * 8.5;
        gc.fillText(nomActivite, cx - tw / 2, cy + 38);

        // Sous-titre
        gc.setFill(javafx.scene.paint.Color.web("#ffffff", 0.65));
        gc.setFont(javafx.scene.text.Font.font("Arial", 11));
        String sub = "Activite de jeu interactive";
        gc.fillText(sub, cx - sub.length() * 3.0, cy + 54);

        // Le canvas doit s'étirer en largeur
        javafx.scene.layout.StackPane stack = new javafx.scene.layout.StackPane(canvas);
        stack.setPrefHeight(H);
        stack.setMinHeight(H);
        stack.setMaxHeight(H);
        stack.setMaxWidth(Double.MAX_VALUE);

        // Redessiner quand la largeur change
        stack.widthProperty().addListener((obs, ov, nv) -> {
            double w = nv.doubleValue();
            if (w <= 0) return;
            canvas.setWidth(w);
            gc.setFill(grad);
            gc.fillRect(0, 0, w, H);
            gc.setFill(javafx.scene.paint.Color.web("#ffffff", 0.06));
            gc.fillOval(-30, -30, 160, 160);
            gc.fillOval(w - 140, 60, 120, 120);
            gc.fillOval(w / 2 - 40, -20, 80, 80);
            double cx2 = w / 2, cy2 = 80.0;
            gc.setFill(javafx.scene.paint.Color.web("#ffffff", 0.9));
            gc.fillRoundRect(cx2 - 40, cy2 - 18, 80, 36, 18, 18);
            gc.fillRoundRect(cx2 - 44, cy2 + 8, 22, 22, 10, 10);
            gc.fillRoundRect(cx2 + 22, cy2 + 8, 22, 22, 10, 10);
            gc.setFill(javafx.scene.paint.Color.web("#7c3aed"));
            gc.fillRect(cx2 - 32, cy2 - 6, 14, 5);
            gc.fillRect(cx2 - 27, cy2 - 10, 5, 14);
            gc.setFill(javafx.scene.paint.Color.web("#f87171"));
            gc.fillOval(cx2 + 18, cy2 - 10, 8, 8);
            gc.setFill(javafx.scene.paint.Color.web("#34d399"));
            gc.fillOval(cx2 + 28, cy2 - 2, 8, 8);
            gc.setFill(javafx.scene.paint.Color.WHITE);
            gc.setFont(javafx.scene.text.Font.font("Arial",
                    javafx.scene.text.FontWeight.BOLD, 15));
            gc.fillText(nomActivite, cx2 - tw / 2, cy2 + 38);
            gc.setFill(javafx.scene.paint.Color.web("#ffffff", 0.65));
            gc.setFont(javafx.scene.text.Font.font("Arial", 11));
            gc.fillText(sub, cx2 - sub.length() * 3.0, cy2 + 54);
        });

        return stack;
    }

    // ─── Carte assemblée depuis tuiles OSM ───────────────────────────────────

    private javafx.scene.layout.StackPane creerCarteOSM(double actLat, double actLon) {
        final int ZOOM      = 14;
        final int TILE_SIZE = 256;
        final int MAP_H     = 160;
        final int COLS      = 5;
        final int ROWS      = 3;

        final int centerTileX = lon2tileX(actLon, ZOOM);
        final int centerTileY = lat2tileY(actLat, ZOOM);

        double fracX = lon2tileXFrac(actLon, ZOOM) - centerTileX;
        double fracY = lat2tileYFrac(actLat, ZOOM) - centerTileY;

        int midCol = COLS / 2;
        int midRow = ROWS / 2;
        final double markerAbsX = (midCol + fracX) * TILE_SIZE;
        final double markerAbsY = (midRow + fracY) * TILE_SIZE;

        // ── Grille de tuiles ─────────────────────────────────────────────────
        javafx.scene.layout.GridPane tileGrid = new javafx.scene.layout.GridPane();
        tileGrid.setSnapToPixel(true);

        for (int dc = 0; dc < COLS; dc++) {
            for (int dr = 0; dr < ROWS; dr++) {
                ImageView iv = new ImageView();
                iv.setFitWidth(TILE_SIZE);
                iv.setFitHeight(TILE_SIZE);
                iv.setPreserveRatio(false);
                iv.setSmooth(true);
                tileGrid.add(iv, dc, dr);
                chargerTuile(iv, centerTileX + dc - midCol, centerTileY + dr - midRow, ZOOM);
            }
        }

        // ── Marqueur pin ─────────────────────────────────────────────────────
        javafx.scene.layout.VBox markerNode = new javafx.scene.layout.VBox(0);
        markerNode.setAlignment(javafx.geometry.Pos.CENTER);
        markerNode.setMouseTransparent(true);

        javafx.scene.shape.Circle circle = new javafx.scene.shape.Circle(8,
                javafx.scene.paint.Color.web("#e53e3e"));
        circle.setStroke(javafx.scene.paint.Color.WHITE);
        circle.setStrokeWidth(2);
        circle.setEffect(new DropShadow(4, javafx.scene.paint.Color.rgb(0,0,0,0.5)));

        javafx.scene.shape.Polygon pin = new javafx.scene.shape.Polygon(
                0.0, 10.0, 7.0, -2.0, -7.0, -2.0);
        pin.setFill(javafx.scene.paint.Color.web("#e53e3e"));
        markerNode.getChildren().addAll(circle, pin);

        // ── Pane principal ────────────────────────────────────────────────────
        javafx.scene.layout.Pane mapPane = new javafx.scene.layout.Pane();
        mapPane.setPrefHeight(MAP_H);
        mapPane.setMinHeight(MAP_H);
        mapPane.setMaxHeight(MAP_H);
        mapPane.setMaxWidth(Double.MAX_VALUE);
        mapPane.setStyle("-fx-background-color: #f2efe9; -fx-cursor: grab;");

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(mapPane.widthProperty());
        clip.setHeight(MAP_H);
        mapPane.setClip(clip);

        final double[] offset = {0.0, 0.0};
        tileGrid.setLayoutX(offset[0]);
        tileGrid.setLayoutY(offset[1]);
        markerNode.setLayoutX(markerAbsX + offset[0] - 8);
        markerNode.setLayoutY(markerAbsY + offset[1] - 26);

        // Centrer quand la largeur est disponible
        mapPane.widthProperty().addListener((obs, ov, nv) -> {
            if (nv.doubleValue() > 0 && offset[0] == 0.0) {
                offset[0] = nv.doubleValue() / 2.0 - markerAbsX;
                offset[1] = MAP_H / 2.0 - markerAbsY;
                tileGrid.setLayoutX(offset[0]);
                tileGrid.setLayoutY(offset[1]);
                markerNode.setLayoutX(markerAbsX + offset[0] - 8);
                markerNode.setLayoutY(markerAbsY + offset[1] - 26);
            }
        });

        // ── Boutons +/- ───────────────────────────────────────────────────────
        Button btnPlus  = creerBtnZoom("+");
        Button btnMoins = creerBtnZoom("−");
        btnPlus.setLayoutX(8);  btnPlus.setLayoutY(8);
        btnMoins.setLayoutX(8); btnMoins.setLayoutY(36);

        javafx.scene.control.Label attrib = new javafx.scene.control.Label("© OpenStreetMap");
        attrib.setStyle("-fx-font-size: 9px; -fx-text-fill: #555; -fx-background-color: rgba(255,255,255,0.7); -fx-padding: 1 4;");
        attrib.setMouseTransparent(true);
        attrib.layoutXProperty().bind(mapPane.widthProperty().subtract(attrib.widthProperty()).subtract(2));
        attrib.setLayoutY(MAP_H - 14);

        mapPane.getChildren().addAll(tileGrid, markerNode, btnPlus, btnMoins, attrib);

        // ── Drag ─────────────────────────────────────────────────────────────
        final double[] dragStart    = {0, 0};
        final double[] offsetAtDrag = {0, 0};

        mapPane.setOnMousePressed(e -> {
            dragStart[0]    = e.getX();
            dragStart[1]    = e.getY();
            offsetAtDrag[0] = offset[0];
            offsetAtDrag[1] = offset[1];
            mapPane.setStyle("-fx-background-color: #f2efe9; -fx-cursor: grabbing;");
        });

        mapPane.setOnMouseDragged(e -> {
            double dx = e.getX() - dragStart[0];
            double dy = e.getY() - dragStart[1];
            offset[0] = offsetAtDrag[0] + dx;
            offset[1] = offsetAtDrag[1] + dy;
            tileGrid.setLayoutX(offset[0]);
            tileGrid.setLayoutY(offset[1]);
            markerNode.setLayoutX(markerAbsX + offset[0] - 8);
            markerNode.setLayoutY(markerAbsY + offset[1] - 26);
        });

        mapPane.setOnMouseReleased(e ->
            mapPane.setStyle("-fx-background-color: #f2efe9; -fx-cursor: grab;"));

        btnPlus.setOnAction(e  -> scaleMap(tileGrid, markerNode, 1.2,   offset, markerAbsX, markerAbsY));
        btnMoins.setOnAction(e -> scaleMap(tileGrid, markerNode, 1/1.2, offset, markerAbsX, markerAbsY));

        return new javafx.scene.layout.StackPane(mapPane);
    }

    private Button creerBtnZoom(String label) {
        Button btn = new Button(label);
        btn.setStyle("-fx-background-color: white; -fx-text-fill: #333; -fx-font-size: 14px; " +
                     "-fx-font-weight: bold; -fx-pref-width: 26; -fx-pref-height: 26; " +
                     "-fx-background-radius: 4; -fx-cursor: hand; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 3, 0, 0, 1);");
        btn.setOnMouseEntered(ev -> btn.setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: #333; -fx-font-size: 14px; " +
                     "-fx-font-weight: bold; -fx-pref-width: 26; -fx-pref-height: 26; " +
                     "-fx-background-radius: 4; -fx-cursor: hand; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 3, 0, 0, 1);"));
        btn.setOnMouseExited(ev -> btn.setStyle("-fx-background-color: white; -fx-text-fill: #333; -fx-font-size: 14px; " +
                     "-fx-font-weight: bold; -fx-pref-width: 26; -fx-pref-height: 26; " +
                     "-fx-background-radius: 4; -fx-cursor: hand; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 3, 0, 0, 1);"));
        return btn;
    }

    private void scaleMap(javafx.scene.layout.GridPane grid,
                          javafx.scene.layout.VBox marker,
                          double factor,
                          double[] offset,
                          double markerAbsX, double markerAbsY) {
        double newScale = Math.max(0.5, Math.min(3.0, grid.getScaleX() * factor));
        grid.setScaleX(newScale);
        grid.setScaleY(newScale);
        marker.setScaleX(newScale);
        marker.setScaleY(newScale);
    }

    private void chargerTuile(javafx.scene.image.ImageView iv, int x, int y, int zoom) {
        String[] subs = {"a", "b", "c"};
        String url = "https://" + subs[Math.abs(x + y) % 3] +
                ".tile.openstreetmap.org/" + zoom + "/" + x + "/" + y + ".png";
        Thread t = new Thread(() -> {
            try {
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection)
                        new java.net.URL(url).openConnection();
                conn.setRequestProperty("User-Agent", "MentalUpApp/1.0 JavaFX");
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);
                if (conn.getResponseCode() == 200) {
                    javafx.scene.image.Image img = new javafx.scene.image.Image(
                            conn.getInputStream(), 256, 256, false, true);
                    javafx.application.Platform.runLater(() -> iv.setImage(img));
                }
            } catch (Exception e) {
                System.err.println("Tuile échouée: " + url);
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private int lon2tileX(double lon, int zoom) {
        return (int) Math.floor((lon + 180.0) / 360.0 * (1 << zoom));
    }
    private int lat2tileY(double lat, int zoom) {
        double latRad = Math.toRadians(lat);
        return (int) Math.floor((1 - Math.log(Math.tan(latRad) + 1 / Math.cos(latRad)) / Math.PI) / 2 * (1 << zoom));
    }
    private double lon2tileXFrac(double lon, int zoom) {
        return (lon + 180.0) / 360.0 * (1 << zoom);
    }
    private double lat2tileYFrac(double lat, int zoom) {
        double latRad = Math.toRadians(lat);
        return (1 - Math.log(Math.tan(latRad) + 1 / Math.cos(latRad)) / Math.PI) / 2 * (1 << zoom);
    }


    // ─── Sélection de place ───────────────────────────────────────────────────

    private void ouvrirSelectionPlace(Activite activite) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Sélection de place - " + activite.getTitre());

        VBox root = new VBox(20);
        root.setStyle("-fx-background-color: white; -fx-padding: 30;");
        root.setAlignment(Pos.TOP_CENTER);

        Label titre = new Label("Sélection de place - " + activite.getTitre());
        titre.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");

        Label ecran = new Label("ÉCRAN / SCÈNE");
        ecran.setMaxWidth(Double.MAX_VALUE);
        ecran.setAlignment(Pos.CENTER);
        ecran.setStyle("-fx-background-color: #2d3748; -fx-text-fill: white; -fx-font-size: 14px; " +
                       "-fx-font-weight: bold; -fx-padding: 12; -fx-background-radius: 8;");

        GridPane grille = new GridPane();
        grille.setHgap(8); grille.setVgap(8); grille.setAlignment(Pos.CENTER);

        String[] rangees = {"A","B","C","D","E","F","G","H","I","J"};
        int nbColonnes = 12;

        List<String> placesReservees;
        try { placesReservees = serviceReservation.getPlacesReservees(activite.getIdActivite()); }
        catch (SQLException e) { placesReservees = List.of(); }

        final String[] placeSelectionnee = {null};
        Label lblSelection = new Label("Place sélectionnée: Aucune");
        lblSelection.setStyle("-fx-font-size: 13px; -fx-text-fill: #4a5568; -fx-font-weight: bold;");

        final List<String> placesReserveesFinal = placesReservees;

        // Vérifier si l'étudiant a déjà une réservation — AVANT la grille
        Reservation reservationExistante;
        try { reservationExistante = serviceReservation.getReservationEtudiant(activite.getIdActivite(), "Sophie Am."); }
        catch (SQLException e) { reservationExistante = null; }
        final Reservation resExistante = reservationExistante;

        // Place de l'étudiant actuel (si elle existe)
        final String maPlace = resExistante != null ? resExistante.getPlace() : null;

        for (int r = 0; r < rangees.length; r++) {
            Label lblRangee = new Label(rangees[r]);
            lblRangee.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #4a5568; -fx-pref-width: 20;");
            grille.add(lblRangee, 0, r);
            for (int c = 1; c <= nbColonnes; c++) {
                String codePlace = rangees[r] + c;
                Button btnPlace = new Button(String.valueOf(c));
                btnPlace.setPrefSize(38, 38);
                boolean reservee = placesReserveesFinal.contains(codePlace);
                if (reservee) {
                    // Ma place = vert, autres réservées = gris
                    if (codePlace.equals(maPlace)) {
                        btnPlace.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-size: 11px;");
                        btnPlace.setDisable(true);
                    } else {
                        btnPlace.setStyle("-fx-background-color: #bdc3c7; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-size: 11px;");
                        btnPlace.setDisable(true);
                    }
                } else {
                    btnPlace.setStyle("-fx-background-color: #6b9fb5; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-size: 11px; -fx-cursor: hand;");
                    btnPlace.setOnAction(ev -> {
                        grille.getChildren().forEach(node -> {
                            if (node instanceof Button b && !b.isDisabled())
                                b.setStyle("-fx-background-color: #6b9fb5; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-size: 11px; -fx-cursor: hand;");
                        });
                        btnPlace.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-size: 11px; -fx-cursor: hand;");
                        placeSelectionnee[0] = codePlace;
                        lblSelection.setText("Place sélectionnée: " + codePlace);
                    });
                }
                grille.add(btnPlace, c, r);
            }
        }

        HBox legende = new HBox(20);
        legende.setAlignment(Pos.CENTER);
        Label lDispo   = new Label("  Disponible");   lDispo.setStyle("-fx-background-color: #6b9fb5; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 6;");
        Label lSelect  = new Label("  Sélectionné");  lSelect.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 6;");
        Label lIndispo = new Label("  Indisponible"); lIndispo.setStyle("-fx-background-color: #bdc3c7; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 6;");
        legende.getChildren().addAll(lDispo, lSelect, lIndispo);
        if (maPlace != null) {
            Label lMaPlace = new Label("  Ma place (" + maPlace + ")");
            lMaPlace.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 6;");
            legende.getChildren().add(lMaPlace);
        }

        Button btnAnnuler = new Button(resExistante != null ? "🗑 Annuler ma réservation" : "Annuler");
        btnAnnuler.setStyle(resExistante != null
                ? "-fx-background-color: #e53e3e; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 10 25; -fx-cursor: hand; -fx-background-radius: 8;"
                : "-fx-background-color: #e2e8f0; -fx-text-fill: #4a5568; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 10 25; -fx-cursor: hand; -fx-background-radius: 8;");

        btnAnnuler.setOnAction(e -> {
            if (resExistante != null) {
                // Confirmer l'annulation
                javafx.scene.control.Alert confirm = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Annuler la réservation");
                confirm.setHeaderText("Annuler la réservation - Place " + resExistante.getPlace());
                confirm.setContentText("Voulez-vous vraiment annuler votre réservation pour \"" + activite.getTitre() + "\" ?");
                confirm.showAndWait().ifPresent(response -> {
                    if (response == javafx.scene.control.ButtonType.OK) {
                        try {
                            serviceReservation.supprimerReservation(resExistante.getIdReservation());
                            popup.close();
                            afficherToast("Réservation annulée !", "#e53e3e", "🗑");
                            chargerActivites();
                        } catch (SQLException ex) {
                            afficherToast("Erreur: " + ex.getMessage(), "#e53e3e", "❌");
                        }
                    }
                });
            } else {
                popup.close();
            }
        });

        Button btnConfirmer = new Button("✅ Confirmer la réservation");
        btnConfirmer.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 10 25; -fx-cursor: hand; -fx-background-radius: 8;");
        btnConfirmer.setOnAction(e -> {
            if (placeSelectionnee[0] == null) { afficherToast("Veuillez sélectionner une place!", "#ed8936", "⚠️"); return; }
            // Ouvrir le paiement avant de confirmer
            popup.close();
            ouvrirPaiement(activite, placeSelectionnee[0]);
        });

        HBox footerBtns = new HBox(15);
        footerBtns.setAlignment(Pos.CENTER_RIGHT);
        footerBtns.getChildren().addAll(lblSelection, new Region() {{ HBox.setHgrow(this, Priority.ALWAYS); }}, btnAnnuler, btnConfirmer);

        root.getChildren().addAll(titre, ecran, grille, legende, footerBtns);
        popup.setScene(new Scene(root, 750, 620));
        popup.showAndWait();
    }

    // ─── Paiement ─────────────────────────────────────────────────────────────

    private void ouvrirPaiement(Activite activite, String place) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.initStyle(javafx.stage.StageStyle.UNDECORATED);

        VBox root = new VBox(0);
        root.setAlignment(Pos.CENTER);
        root.setPrefWidth(480);
        root.setStyle("-fx-background-color: #1a1a2e; -fx-background-radius: 20; " +
                      "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 30, 0, 0, 10);");

        // ── Header ────────────────────────────────────────────────────────────
        VBox header = new VBox(6);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(28, 20, 22, 20));
        header.setStyle("-fx-background-color: #27ae60; -fx-background-radius: 20 20 0 0;");
        Label lblIcon  = new Label("💳");
        lblIcon.setStyle("-fx-font-size: 38px;");
        Label lblTitre = new Label("PAIEMENT");
        lblTitre.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label lblSous  = new Label(activite.getTitre() + "  •  Place " + place);
        lblSous.setStyle("-fx-font-size: 12px; -fx-text-fill: rgba(255,255,255,0.85);");
        header.getChildren().addAll(lblIcon, lblTitre, lblSous);

        // ── Formulaire ────────────────────────────────────────────────────────
        VBox form = new VBox(14);
        form.setPadding(new Insets(24, 28, 20, 28));
        form.setStyle("-fx-background-color: #16213e;");

        // Montant
        HBox montantRow = new HBox(10);
        montantRow.setAlignment(Pos.CENTER_LEFT);
        montantRow.setPadding(new Insets(12, 16, 12, 16));
        montantRow.setStyle("-fx-background-color: #0f3460; -fx-background-radius: 10;");
        Label lblMontantTxt = new Label("Montant à payer :");
        lblMontantTxt.setStyle("-fx-font-size: 13px; -fx-text-fill: #8892b0;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Label lblMontant = new Label("25.00 TND");
        lblMontant.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");
        montantRow.getChildren().addAll(lblMontantTxt, sp, lblMontant);

        // Méthode de paiement
        Label lblMethode = new Label("Méthode de paiement");
        lblMethode.setStyle("-fx-font-size: 12px; -fx-text-fill: #8892b0; -fx-font-weight: bold;");

        javafx.scene.control.ToggleGroup tg = new javafx.scene.control.ToggleGroup();
        HBox methodesRow = new HBox(10);
        methodesRow.setAlignment(Pos.CENTER_LEFT);

        for (String[] m : new String[][]{
                {"💳", "Carte bancaire"},
                {"📱", "Paiement mobile"},
                {"🏦", "Virement"}}) {
            javafx.scene.control.ToggleButton btn = new javafx.scene.control.ToggleButton(m[0] + " " + m[1]);
            btn.setToggleGroup(tg);
            btn.setStyle("-fx-background-color: #0f3460; -fx-text-fill: #8892b0; " +
                         "-fx-font-size: 11px; -fx-padding: 8 12; -fx-background-radius: 8; -fx-cursor: hand;");
            btn.selectedProperty().addListener((obs, ov, nv) ->
                btn.setStyle(nv
                    ? "-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 8 12; -fx-background-radius: 8; -fx-cursor: hand;"
                    : "-fx-background-color: #0f3460; -fx-text-fill: #8892b0; -fx-font-size: 11px; -fx-padding: 8 12; -fx-background-radius: 8; -fx-cursor: hand;"));
            methodesRow.getChildren().add(btn);
        }
        ((javafx.scene.control.ToggleButton) methodesRow.getChildren().get(0)).setSelected(true);

        // Numéro de carte
        Label lblCarte = new Label("Numéro de carte");
        lblCarte.setStyle("-fx-font-size: 12px; -fx-text-fill: #8892b0; -fx-font-weight: bold;");
        TextField tfCarte = new TextField();
        tfCarte.setPromptText("1234  5678  9012  3456");
        tfCarte.setStyle("-fx-background-color: #0f3460; -fx-text-fill: white; -fx-prompt-text-fill: #4a5568; " +
                         "-fx-font-size: 15px; -fx-padding: 12; -fx-background-radius: 8; -fx-border-color: transparent;");
        // Formatage automatique
        tfCarte.textProperty().addListener((obs, ov, nv) -> {
            String digits = nv.replaceAll("[^0-9]", "").substring(0, Math.min(nv.replaceAll("[^0-9]", "").length(), 16));
            StringBuilder formatted = new StringBuilder();
            for (int i = 0; i < digits.length(); i++) {
                if (i > 0 && i % 4 == 0) formatted.append("  ");
                formatted.append(digits.charAt(i));
            }
            if (!formatted.toString().equals(nv)) {
                tfCarte.setText(formatted.toString());
                tfCarte.positionCaret(formatted.length());
            }
        });

        // Expiry + CVV
        HBox row2 = new HBox(12);
        VBox boxExp = new VBox(6);
        Label lblExp = new Label("Date d'expiration");
        lblExp.setStyle("-fx-font-size: 12px; -fx-text-fill: #8892b0; -fx-font-weight: bold;");
        TextField tfExp = new TextField();
        tfExp.setPromptText("MM/AA");
        tfExp.setStyle("-fx-background-color: #0f3460; -fx-text-fill: white; -fx-prompt-text-fill: #4a5568; " +
                       "-fx-font-size: 14px; -fx-padding: 12; -fx-background-radius: 8; -fx-border-color: transparent;");
        boxExp.getChildren().addAll(lblExp, tfExp);
        HBox.setHgrow(boxExp, Priority.ALWAYS);

        VBox boxCvv = new VBox(6);
        Label lblCvv = new Label("CVV");
        lblCvv.setStyle("-fx-font-size: 12px; -fx-text-fill: #8892b0; -fx-font-weight: bold;");
        TextField tfCvv = new TextField();
        tfCvv.setPromptText("•••");
        tfCvv.setStyle("-fx-background-color: #0f3460; -fx-text-fill: white; -fx-prompt-text-fill: #4a5568; " +
                       "-fx-font-size: 14px; -fx-padding: 12; -fx-background-radius: 8; -fx-border-color: transparent;");
        tfCvv.setPrefWidth(90);
        boxCvv.getChildren().addAll(lblCvv, tfCvv);
        row2.getChildren().addAll(boxExp, boxCvv);

        form.getChildren().addAll(montantRow, lblMethode, methodesRow, lblCarte, tfCarte, row2);

        // ── Boutons ───────────────────────────────────────────────────────────
        HBox btns = new HBox(12);
        btns.setAlignment(Pos.CENTER);
        btns.setPadding(new Insets(0, 28, 24, 28));
        btns.setStyle("-fx-background-color: #16213e; -fx-background-radius: 0 0 20 20;");

        Button btnAnnuler = new Button("✕  Annuler");
        btnAnnuler.setPrefWidth(160); btnAnnuler.setPrefHeight(44);
        btnAnnuler.setStyle("-fx-background-color: #2a3f5f; -fx-text-fill: #8892b0; " +
                            "-fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand;");
        btnAnnuler.setOnAction(ev -> popup.close());

        Button btnPayer = new Button("💳  Payer 25.00 TND");
        btnPayer.setPrefWidth(200); btnPayer.setPrefHeight(44);
        btnPayer.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; " +
                          "-fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand;");
        btnPayer.setOnMouseEntered(ev -> btnPayer.setOpacity(0.85));
        btnPayer.setOnMouseExited(ev  -> btnPayer.setOpacity(1.0));
        btnPayer.setOnAction(ev -> {
            // Validation basique
            String digits = tfCarte.getText().replaceAll("[^0-9]", "");
            if (digits.length() < 16) {
                afficherToast("Numéro de carte invalide", "#e53e3e", "❌"); return;
            }
            if (tfExp.getText().trim().isEmpty()) {
                afficherToast("Date d'expiration requise", "#e53e3e", "❌"); return;
            }
            if (tfCvv.getText().trim().length() < 3) {
                afficherToast("CVV invalide", "#e53e3e", "❌"); return;
            }
            // Récupérer la méthode sélectionnée
            String methode = "Carte bancaire";
            if (tg.getSelectedToggle() != null) {
                methode = ((javafx.scene.control.ToggleButton) tg.getSelectedToggle())
                        .getText().replaceAll("^[^ ]+ ", "");
            }
            final String methodeFinal = methode;
            popup.close();
            afficherAnimationPaiement(activite, place, methodeFinal);
        });

        btns.getChildren().addAll(btnAnnuler, btnPayer);
        root.getChildren().addAll(header, form, btns);

        // Animation d'entrée
        root.setScaleX(0.8); root.setScaleY(0.8); root.setOpacity(0);
        javafx.animation.ScaleTransition st = new javafx.animation.ScaleTransition(
                javafx.util.Duration.millis(250), root);
        st.setToX(1); st.setToY(1);
        st.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
        javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(
                javafx.util.Duration.millis(200), root);
        ft.setToValue(1);
        new javafx.animation.ParallelTransition(st, ft).play();

        javafx.scene.Scene scene = new javafx.scene.Scene(root);
        scene.setFill(Color.TRANSPARENT);
        popup.setScene(scene);
        popup.show();

        javafx.geometry.Rectangle2D screen = javafx.stage.Screen.getPrimary().getVisualBounds();
        popup.setX(screen.getWidth() / 2 - 240);
        popup.setY(screen.getHeight() / 2 - 280);
    }

    private void afficherAnimationPaiement(Activite activite, String place, String methode) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.initStyle(javafx.stage.StageStyle.UNDECORATED);

        VBox root = new VBox(16);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setPrefWidth(360);
        root.setStyle("-fx-background-color: #1a1a2e; -fx-background-radius: 20; " +
                      "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 30, 0, 0, 10);");

        Label lblIcon = new Label("⏳");
        lblIcon.setStyle("-fx-font-size: 48px;");
        Label lblMsg = new Label("Traitement du paiement...");
        lblMsg.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: white;");

        javafx.scene.control.ProgressBar progress = new javafx.scene.control.ProgressBar(0);
        progress.setPrefWidth(280);
        progress.setStyle("-fx-accent: #27ae60;");

        root.getChildren().addAll(lblIcon, lblMsg, progress);

        javafx.scene.Scene scene = new javafx.scene.Scene(root);
        scene.setFill(Color.TRANSPARENT);
        popup.setScene(scene);
        popup.show();

        javafx.geometry.Rectangle2D screen = javafx.stage.Screen.getPrimary().getVisualBounds();
        popup.setX(screen.getWidth() / 2 - 180);
        popup.setY(screen.getHeight() / 2 - 100);

        // Animation de progression
        javafx.animation.Timeline tl = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.millis(0),
                new javafx.animation.KeyValue(progress.progressProperty(), 0)),
            new javafx.animation.KeyFrame(javafx.util.Duration.millis(1800),
                new javafx.animation.KeyValue(progress.progressProperty(), 1))
        );
        tl.setOnFinished(e -> {
            popup.close();
            // Enregistrer la réservation avec infos paiement
            try {
                Reservation r = new Reservation(activite.getIdActivite(), "Sophie Am.", place, LocalDate.now());
                r.setMontant(25.00);
                r.setStatutPaiement("PAYE");
                r.setMethodePaiement(methode);
                serviceReservation.ajouterReservation(r);
                chargerActivites();
                afficherToast("✅ Paiement réussi ! Place " + place + " réservée.", "#27ae60", "💳");
            } catch (SQLException ex) {
                afficherToast("Erreur réservation: " + ex.getMessage(), "#e53e3e", "❌");
            }
        });
        tl.play();
    }

    // ─── Ticket ───────────────────────────────────────────────────────────────

    private void afficherTicket(Activite activite) {
        Reservation res = null;
        try { res = serviceReservation.getReservationEtudiant(activite.getIdActivite(), "Sophie Am."); }
        catch (SQLException e) { e.printStackTrace(); }

        if (res == null) {
            afficherToast("Vous n'avez pas de réservation pour cette activité.", "#ed8936", "⚠️");
            return;
        }

        final Reservation reservation = res;
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Mon Ticket de Réservation");

        VBox outer = new VBox(15);
        outer.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 20;");
        outer.setAlignment(Pos.TOP_CENTER);

        Label titrePopup = new Label("Mon Ticket de Réservation");
        titrePopup.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");

        VBox ticket = new VBox(12);
        ticket.setAlignment(Pos.TOP_CENTER);
        ticket.setStyle("-fx-background-color: linear-gradient(to bottom, #667eea, #764ba2); " +
                        "-fx-padding: 20; -fx-background-radius: 18; -fx-pref-width: 380; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0, 0, 6);");

        Label ticketHeader  = new Label("🎫  TICKET DE RÉSERVATION");
        ticketHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label ticketActivite = new Label(activite.getTitre());
        ticketActivite.setStyle("-fx-font-size: 13px; -fx-text-fill: rgba(255,255,255,0.9);");
        Label sep1 = new Label("- - - - - - - - - - - - - - - - - - - - - - -");
        sep1.setStyle("-fx-text-fill: rgba(255,255,255,0.4); -fx-font-size: 11px;");

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(12); infoGrid.setVgap(8); infoGrid.setAlignment(Pos.CENTER);
        infoGrid.add(creerInfoBox("Date de début",   activite.getDateDebut().format(fmt)),          0, 0);
        infoGrid.add(creerInfoBox("Date de fin",     activite.getDateFin().format(fmt)),             1, 0);
        infoGrid.add(creerInfoBox("Réservé le",      reservation.getDateReservation().format(fmt)), 0, 1);
        infoGrid.add(creerInfoBox("Réservation N°",  "#" + reservation.getIdReservation()),         1, 1);

        VBox placeBox = new VBox(3);
        placeBox.setAlignment(Pos.CENTER);
        placeBox.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-padding: 12; -fx-background-radius: 10; -fx-pref-width: 340;");
        Label lblPlaceTitre = new Label("PLACE");
        lblPlaceTitre.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(255,255,255,0.7); -fx-font-weight: bold;");
        Label lblPlaceVal = new Label(reservation.getPlace());
        lblPlaceVal.setStyle("-fx-font-size: 38px; -fx-font-weight: bold; -fx-text-fill: white;");
        placeBox.getChildren().addAll(lblPlaceTitre, lblPlaceVal);

        Label sep2    = new Label("- - - - - - - - - - - - - - - - - - - - - - -");
        sep2.setStyle("-fx-text-fill: rgba(255,255,255,0.4); -fx-font-size: 11px;");

        // ── QR Code ───────────────────────────────────────────────────────────
        String qrData = String.format(
                "MENTALUP - TICKET DE RESERVATION\n" +
                "Reservation N : %d\n" +
                "Activite      : %s\n" +
                "Place         : %s\n" +
                "Reserve le    : %s\n" +
                "Du            : %s\n" +
                "Au            : %s",
                reservation.getIdReservation(),
                activite.getTitre(),
                reservation.getPlace(),
                reservation.getDateReservation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                activite.getDateDebut().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                activite.getDateFin().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        );

        javafx.scene.image.ImageView qrView = new javafx.scene.image.ImageView();
        qrView.setFitWidth(100);
        qrView.setFitHeight(100);
        qrView.setPreserveRatio(true);
        qrView.setSmooth(true);

        // Générer QR via API gratuite (sans librairie)
        Thread qrThread = new  Thread(() -> {
            try {
                String encoded = java.net.URLEncoder.encode(qrData, java.nio.charset.StandardCharsets.UTF_8);
                String url = "https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=" + encoded;
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection)
                        new java.net.URL(url).openConnection();
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                if (conn.getResponseCode() == 200) {
                    javafx.scene.image.Image img = new javafx.scene.image.Image(conn.getInputStream());
                    javafx.application.Platform.runLater(() -> qrView.setImage(img));
                }
            } catch (Exception ex) {
                System.err.println("QR API: " + ex.getMessage());
            }
        });
        qrThread.setDaemon(true);
        qrThread.start();

        VBox qrBox = new VBox(5);
        qrBox.setAlignment(Pos.CENTER);
        qrBox.setStyle("-fx-background-color: white; -fx-padding: 8; -fx-background-radius: 8;");
        Label qrLabel = new Label("Scanner pour vérifier");
        qrLabel.setStyle("-fx-font-size: 9px; -fx-text-fill: #555;");
        qrBox.getChildren().addAll(qrView, qrLabel);
        Label footer1 = new Label("Mental Up – Système de réservation");
        footer1.setStyle("-fx-font-size: 10px; -fx-text-fill: rgba(255,255,255,0.6);");
        Label footer2 = new Label("Présentez ce ticket le jour de l'activité");
        footer2.setStyle("-fx-font-size: 10px; -fx-text-fill: rgba(255,255,255,0.6);");

        ticket.getChildren().addAll(ticketHeader, ticketActivite, sep1, infoGrid, placeBox, sep2, qrBox, footer1, footer2);

        Button btnFermer = new Button("Fermer");
        btnFermer.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #4a5568; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 10 25; -fx-cursor: hand; -fx-background-radius: 8;");
        btnFermer.setOnAction(e -> popup.close());

        Button btnImprimer = new Button("🖨 Imprimer");
        btnImprimer.setStyle("-fx-background-color: #5a67d8; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 10 25; -fx-cursor: hand; -fx-background-radius: 8;");
        btnImprimer.setOnAction(e -> {
            javafx.print.PrinterJob job = javafx.print.PrinterJob.createPrinterJob();
            if (job != null && job.showPrintDialog(popup)) {
                javafx.scene.transform.Scale scale = new javafx.scene.transform.Scale(0.75, 0.75);
                ticket.getTransforms().add(scale);
                if (job.printPage(ticket)) job.endJob();
                ticket.getTransforms().remove(scale);
            }
        });

        HBox btnBox = new HBox(15, btnFermer, btnImprimer);
        btnBox.setAlignment(Pos.CENTER_RIGHT);
        outer.getChildren().addAll(titrePopup, ticket, btnBox);
        popup.setScene(new Scene(outer, 460, 680));
        popup.showAndWait();
    }

    // ─── Utilitaires ─────────────────────────────────────────────────────────

    private String getIconeType(String type) {
        if (type == null) return "⚡";
        String t = type.toLowerCase();
        if (t.contains("sport"))                          return "⚽";
        if (t.contains("culturel"))                       return "🎭";
        if (t.contains("créatif") || t.contains("creatif")) return "🎨";
        if (t.contains("musique"))                        return "🎵";
        if (t.contains("nature"))                         return "🌿";
        if (t.contains("social"))                         return "👥";
        if (t.contains("jeux") || t.contains("jeu"))      return "🎮";
        return "⚡";
    }

    private VBox creerInfoBox(String label, String valeur) {
        VBox box = new VBox(3);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-padding: 10 15; -fx-background-radius: 8; -fx-pref-width: 155;");
        Label lbl = new Label(label);  lbl.setStyle("-fx-font-size: 10px; -fx-text-fill: rgba(255,255,255,0.7);");
        Label val = new Label(valeur); val.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");
        box.getChildren().addAll(lbl, val);
        return box;
    }

    // ─── Popup choix du jeu ───────────────────────────────────────────────────

    private void afficherPopupChoixJeu(Stage parentStage, String nomActivite) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.initOwner(parentStage);
        popup.initStyle(javafx.stage.StageStyle.UNDECORATED);

        VBox root = new VBox(0);
        root.setAlignment(Pos.CENTER);
        root.setPrefWidth(480);
        root.setStyle("-fx-background-color: #1a1a2e; -fx-background-radius: 20; " +
                      "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 30, 0, 0, 10);");

        // ── Bandeau titre ─────────────────────────────────────────────────────
        VBox header = new VBox(6);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(28, 20, 22, 20));
        header.setStyle("-fx-background-color: #9b59b6; -fx-background-radius: 20 20 0 0;");

        Label lblEmoji = new Label("🎮");
        lblEmoji.setStyle("-fx-font-size: 42px;");
        Label lblTitre = new Label("ARCADE DES JEUX");
        lblTitre.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white; " +
                          "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 4, 0, 0, 2);");
        Label lblSous = new Label("Choisissez votre défi !");
        lblSous.setStyle("-fx-font-size: 13px; -fx-text-fill: rgba(255,255,255,0.85);");
        header.getChildren().addAll(lblEmoji, lblTitre, lblSous);

        // ── Grille de jeux ────────────────────────────────────────────────────
        VBox jeux = new VBox(10);
        jeux.setPadding(new Insets(20, 24, 10, 24));
        jeux.setStyle("-fx-background-color: #16213e;");

        String[][] configs = {
            {"🎯", "Réaction Pro",  "Testez vos réflexes avec des combos !",  "#e67e22"},
            {"🧠", "Mémoire",       "Entraînez votre cerveau avec des cartes !", "#3498db"},
            {"🌀", "Labyrinthe",    "Trouvez la sortie du labyrinthe !",        "#27ae60"},
            {"🐍", "Snake",         "Mangez sans vous mordre la queue !",       "#e74c3c"},
            {"🎲", "Surprise",      "Jeu aléatoire pour les aventuriers !",     "#f39c12"},
        };

        for (String[] cfg : configs) {
            HBox row = new HBox(14);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(12, 16, 12, 16));
            row.setStyle("-fx-background-color: #1e2d4a; -fx-background-radius: 12; -fx-cursor: hand;");

            Label ico = new Label(cfg[0]);
            ico.setStyle("-fx-font-size: 26px; -fx-min-width: 36;");

            VBox txt = new VBox(2);
            Label nom = new Label(cfg[1]);
            nom.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");
            Label desc = new Label(cfg[2]);
            desc.setStyle("-fx-font-size: 11px; -fx-text-fill: #8892b0;");
            txt.getChildren().addAll(nom, desc);
            HBox.setHgrow(txt, Priority.ALWAYS);

            Rectangle accent = new Rectangle(4, 40);
            accent.setFill(javafx.scene.paint.Color.web(cfg[3]));
            accent.setArcWidth(4); accent.setArcHeight(4);

            row.getChildren().addAll(accent, ico, txt);

            // Hover
            row.setOnMouseEntered(ev -> row.setStyle(
                    "-fx-background-color: #2a3f5f; -fx-background-radius: 12; -fx-cursor: hand;"));
            row.setOnMouseExited(ev -> row.setStyle(
                    "-fx-background-color: #1e2d4a; -fx-background-radius: 12; -fx-cursor: hand;"));

            // Clic
            final String nomJeu = cfg[1];
            row.setOnMouseClicked(ev -> {
                popup.close();
                switch (nomJeu) {
                    case "Réaction Pro" -> utils.JeuAmeliore.lancerJeuReactionAmeliore(parentStage, nomActivite);
                    case "Mémoire"      -> utils.JeuMemoire.lancerJeuMemoire(parentStage, nomActivite);
                    case "Labyrinthe"   -> utils.JeuLabyrinthe.lancerJeuLabyrinthe(parentStage, nomActivite);
                    case "Snake"        -> utils.JeuSnake.lancerJeuSnake(parentStage, nomActivite);
                    case "Surprise"     -> {
                        int choix = new java.util.Random().nextInt(4);
                        switch (choix) {
                            case 0 -> utils.JeuAmeliore.lancerJeuReactionAmeliore(parentStage, nomActivite);
                            case 1 -> utils.JeuMemoire.lancerJeuMemoire(parentStage, nomActivite);
                            case 2 -> utils.JeuLabyrinthe.lancerJeuLabyrinthe(parentStage, nomActivite);
                            case 3 -> utils.JeuSnake.lancerJeuSnake(parentStage, nomActivite);
                        }
                    }
                }
            });

            jeux.getChildren().add(row);
        }

        // ── Bouton annuler ────────────────────────────────────────────────────
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(14, 24, 20, 24));
        footer.setStyle("-fx-background-color: #16213e; -fx-background-radius: 0 0 20 20;");

        Button btnAnnuler = new Button("✕  Annuler");
        btnAnnuler.setPrefWidth(160); btnAnnuler.setPrefHeight(40);
        btnAnnuler.setStyle("-fx-background-color: #2a3f5f; -fx-text-fill: #8892b0; " +
                            "-fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand;");
        btnAnnuler.setOnMouseEntered(ev -> btnAnnuler.setStyle(
                "-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                "-fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand;"));
        btnAnnuler.setOnMouseExited(ev -> btnAnnuler.setStyle(
                "-fx-background-color: #2a3f5f; -fx-text-fill: #8892b0; " +
                "-fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand;"));
        btnAnnuler.setOnAction(ev -> popup.close());
        footer.getChildren().add(btnAnnuler);

        root.getChildren().addAll(header, jeux, footer);

        // Animation d'entrée
        root.setScaleX(0.5); root.setScaleY(0.5); root.setOpacity(0);
        javafx.animation.ScaleTransition st = new javafx.animation.ScaleTransition(
                javafx.util.Duration.millis(300), root);
        st.setToX(1); st.setToY(1);
        st.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
        javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(
                javafx.util.Duration.millis(200), root);
        ft.setToValue(1);
        new javafx.animation.ParallelTransition(st, ft).play();

        javafx.scene.Scene scene = new javafx.scene.Scene(root);
        scene.setFill(Color.TRANSPARENT);
        popup.setScene(scene);
        popup.show();

        // Centrer sur le parent
        popup.setX(parentStage.getX() + (parentStage.getWidth()  - 480) / 2);
        popup.setY(parentStage.getY() + (parentStage.getHeight() - 500) / 2);
    }

    private void afficherToast(String message, String couleur, String icone) {
        Stage toast = new Stage();
        toast.initStyle(javafx.stage.StageStyle.TRANSPARENT);
        toast.setAlwaysOnTop(true);

        HBox box = new HBox(12);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(16, 20, 16, 20));
        box.setStyle("-fx-background-color: " + couleur + "; -fx-background-radius: 12; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0, 0, 5);");
        Label ico = new Label(icone); ico.setStyle("-fx-font-size: 18px;");
        Label lbl = new Label(message); lbl.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");
        lbl.setMaxWidth(300); lbl.setWrapText(true);
        box.getChildren().addAll(ico, lbl);

        javafx.scene.Scene scene = new javafx.scene.Scene(box);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        toast.setScene(scene);

        javafx.geometry.Rectangle2D screen = javafx.stage.Screen.getPrimary().getVisualBounds();
        toast.setX(screen.getMaxX() - 380);
        toast.setY(screen.getMaxY() - 100);
        toast.show();

        javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(
                javafx.util.Duration.millis(500), box);
        fade.setDelay(javafx.util.Duration.millis(2000));
        fade.setFromValue(1.0); fade.setToValue(0.0);
        fade.setOnFinished(e -> toast.close());
        fade.play();
    }
}
