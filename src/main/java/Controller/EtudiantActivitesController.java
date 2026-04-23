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
import services.ServiceReservation;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class EtudiantActivitesController implements Initializable {

    @FXML private GridPane activitesGrid;
    @FXML private TextField searchField;

    private ServiceActivite serviceActivite;
    private ServiceReservation serviceReservation;
    private List<Activite> toutesActivites = new java.util.ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        serviceActivite  = new ServiceActivite();
        serviceReservation = new ServiceReservation();
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

        // ── Carte OSM assemblée depuis tuiles ─────────────────────────────────
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
        btnReserver.setOnAction(e -> ouvrirSelectionPlace(activite));

        Button btnTicket = new Button("🎫");
        btnTicket.setStyle("-fx-background-color: #5a67d8; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 14; -fx-cursor: hand; -fx-background-radius: 8;");
        btnTicket.setOnMouseEntered(e -> btnTicket.setStyle("-fx-background-color: #434190; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 14; -fx-cursor: hand; -fx-background-radius: 8;"));
        btnTicket.setOnMouseExited(e  -> btnTicket.setStyle("-fx-background-color: #5a67d8; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 14; -fx-cursor: hand; -fx-background-radius: 8;"));
        btnTicket.setOnAction(e -> afficherTicket(activite));

        HBox footer = new HBox(10, statutLabel, btnReserver, btnTicket);
        footer.setAlignment(Pos.CENTER_LEFT);
        VBox.setMargin(footer, new Insets(8, 0, 0, 0));

        content.getChildren().addAll(typeLabel, titreLabel, descLabel, datesLabel, dureeLabel, resLabel, footer);
        card.getChildren().addAll(mapView, content);
        return card;
    }

    // ─── Carte assemblée depuis tuiles OSM ───────────────────────────────────

    private javafx.scene.layout.StackPane creerCarteOSM(double actLat, double actLon) {
        final int ZOOM      = 14;
        final int TILE_SIZE = 256;
        final int MAP_H     = 160;
        final int COLS      = 5;
        final int ROWS      = 3;

        // Tuile centrale = tuile de l'activité
        final int centerTileX = lon2tileX(actLon, ZOOM);
        final int centerTileY = lat2tileY(actLat, ZOOM);

        // Position fractionnaire dans la tuile centrale (0..1)
        double fracX = lon2tileXFrac(actLon, ZOOM) - centerTileX;
        double fracY = lat2tileYFrac(actLat, ZOOM) - centerTileY;

        // Position absolue du marqueur dans la grille (tuile centrale = colonne COLS/2)
        int midCol = COLS / 2; // = 2
        int midRow = 0;        // tuiles commencent à la ligne 0
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
                chargerTuile(iv, centerTileX + dc - midCol, centerTileY + dr, ZOOM);
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

        // Offset initial : centrer la carte sur le marqueur
        final double[] offset = {0.0, 0.0};

        // Centrer la grille : on veut que markerAbsX soit au centre de la carte
        // On calcule l'offset initial après que la largeur soit connue
        tileGrid.setLayoutX(offset[0]);
        tileGrid.setLayoutY(offset[1]);
        markerNode.setLayoutX(markerAbsX + offset[0] - 8);
        markerNode.setLayoutY(markerAbsY + offset[1] - 26);

        // Centrer automatiquement quand la largeur est disponible
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

        // Attribution
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
            // Marqueur suit la grille → reste sur la localisation
            markerNode.setLayoutX(markerAbsX + offset[0] - 8);
            markerNode.setLayoutY(markerAbsY + offset[1] - 26);
        });

        mapPane.setOnMouseReleased(e ->
            mapPane.setStyle("-fx-background-color: #f2efe9; -fx-cursor: grab;"));

        // ── Zoom +/- ──────────────────────────────────────────────────────────
        // (recharge les tuiles — simple rechargement à zoom fixe +1/-1)
        btnPlus.setOnAction(e -> { /* zoom in visuel : scale */ scaleMap(tileGrid, markerNode, 1.2, offset, markerAbsX, markerAbsY); });
        btnMoins.setOnAction(e -> { scaleMap(tileGrid, markerNode, 1/1.2, offset, markerAbsX, markerAbsY); });

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
        String url = "https://tile.openstreetmap.org/" + zoom + "/" + x + "/" + y + ".png";
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
            try {
                serviceReservation.ajouterReservation(
                        new Reservation(activite.getIdActivite(), "Sophie Am.", placeSelectionnee[0], LocalDate.now()));
                popup.close();
                afficherToast("Place " + placeSelectionnee[0] + " réservée !", "#27ae60", "✅");
                chargerActivites();
            } catch (SQLException ex) { afficherToast("Erreur: " + ex.getMessage(), "#e53e3e", "❌"); }
        });

        HBox footerBtns = new HBox(15);
        footerBtns.setAlignment(Pos.CENTER_RIGHT);
        footerBtns.getChildren().addAll(lblSelection, new Region() {{ HBox.setHgrow(this, Priority.ALWAYS); }}, btnAnnuler, btnConfirmer);

        root.getChildren().addAll(titre, ecran, grille, legende, footerBtns);
        popup.setScene(new Scene(root, 750, 620));
        popup.showAndWait();
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
        Thread qrThread = new Thread(() -> {
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
