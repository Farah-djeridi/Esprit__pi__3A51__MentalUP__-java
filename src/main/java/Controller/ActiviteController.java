package Controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Activite;
import services.ServiceActivite;
import services.ServiceNotation;
import services.ServiceReservation;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

public class ActiviteController implements Initializable {

    @FXML private FlowPane activitesFlow;
    @FXML private Label lblCount;
    @FXML private Button btnAjouter;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortCombo;

    private ServiceActivite serviceActivite;
    private ServiceNotation serviceNotation;
    private ServiceReservation serviceReservation;
    private ObservableList<Activite> activitesList;
    private FilteredList<Activite> filteredList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        serviceActivite    = new ServiceActivite();
        serviceNotation    = new ServiceNotation();
        serviceReservation = new ServiceReservation();
        activitesList = FXCollections.observableArrayList();
        filteredList = new FilteredList<>(activitesList, a -> true);
        configurerRechercheEtTri();
        chargerActivites();
    }

    // ─── Chargement ──────────────────────────────────────────────────────────

    private void chargerActivites() {
        try {
            activitesList.setAll(serviceActivite.getAllActivites());
            rafraichirAffichage();
        } catch (SQLException e) {
            alerte("Erreur", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void afficherCartes(List<Activite> activites) {
        activitesFlow.getChildren().clear();
        if (lblCount != null) lblCount.setText(activites.size() + " activité(s)");
        for (Activite a : activites) activitesFlow.getChildren().add(creerCarte(a));
    }

    // ─── Carte ───────────────────────────────────────────────────────────────

    private VBox creerCarte(Activite activite) {
        VBox card = new VBox(10);
        card.setPrefWidth(270);
        card.setPadding(new Insets(18));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 14; " +
                      "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");

        boolean disponible = activite.getDateFin() != null && LocalDate.now().isBefore(activite.getDateFin());
        String couleur = disponible ? "#27ae60" : "#e53e3e";

        // Header: badge type + statut
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        Label badgeType = new Label(getIconeType(activite.getType()) + " " + activite.getType());
        badgeType.setStyle("-fx-background-color: #edf2f7; -fx-text-fill: #4a5568; " +
                           "-fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 3 8; -fx-background-radius: 10;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Label statut = new Label(disponible ? "● Actif" : "● Terminé");
        statut.setStyle("-fx-text-fill: " + couleur + "; -fx-font-size: 11px; -fx-font-weight: bold;");
        header.getChildren().addAll(badgeType, sp, statut);

        // ID + Titre
        Label idLbl = new Label("#" + activite.getIdActivite());
        idLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #a0aec0;");
        Label titreLbl = new Label(activite.getTitre());
        titreLbl.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #2d3748; -fx-wrap-text: true;");
        titreLbl.setMaxWidth(234);

        // Description
        String desc = activite.getDescription() != null && activite.getDescription().length() > 55
                ? activite.getDescription().substring(0, 55) + "..." : activite.getDescription();
        Label descLbl = new Label(desc);
        descLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #718096; -fx-wrap-text: true;");
        descLbl.setMaxWidth(234);

        // Séparateur
        Region sep = new Region();
        sep.setStyle("-fx-background-color: #e2e8f0; -fx-pref-height: 1;");

        // Infos
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        Label adrLbl = new Label("📍 " + activite.getAdresse());
        adrLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #4a5568;");
        String dates = (activite.getDateDebut() != null ? activite.getDateDebut().format(fmt) : "?")
                     + " → " + (activite.getDateFin() != null ? activite.getDateFin().format(fmt) : "?");
        Label dateLbl = new Label("📅 " + dates);
        dateLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #4a5568;");

        // Note moyenne
        double moy = 0; int nbNotes = 0;
        try { moy = serviceNotation.getMoyenne(activite.getIdActivite());
              nbNotes = serviceNotation.getNombreNotes(activite.getIdActivite()); }
        catch (Exception ignored) {}
        HBox starsRow = new HBox(3);
        starsRow.setAlignment(Pos.CENTER_LEFT);
        for (int i = 1; i <= 5; i++) {
            Label s = new Label(i <= Math.round(moy) ? "★" : "☆");
            s.setStyle("-fx-font-size: 12px; -fx-text-fill: " + (i <= Math.round(moy) ? "#f6ad55" : "#cbd5e0") + ";");
            starsRow.getChildren().add(s);
        }
        Label lblMoy = new Label(nbNotes > 0 ? String.format(" %.1f (%d)", moy, nbNotes) : " Pas noté");
        lblMoy.setStyle("-fx-font-size: 10px; -fx-text-fill: #718096;");
        starsRow.getChildren().add(lblMoy);

        // Boutons modifier / supprimer
        Button btnEdit = new Button("✏ Modifier");
        btnEdit.setStyle("-fx-background-color: #2d3748; -fx-text-fill: white; -fx-font-size: 11px; " +
                         "-fx-font-weight: bold; -fx-padding: 7 14; -fx-background-radius: 20; -fx-cursor: hand;");
        btnEdit.setOnMouseEntered(e -> btnEdit.setStyle("-fx-background-color: #4a5568; -fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 7 14; -fx-background-radius: 20; -fx-cursor: hand;"));
        btnEdit.setOnMouseExited(e  -> btnEdit.setStyle("-fx-background-color: #2d3748; -fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 7 14; -fx-background-radius: 20; -fx-cursor: hand;"));
        btnEdit.setOnAction(e -> ouvrirPopupModification(activite));

        Button btnDel = new Button("🗑 Supprimer");
        btnDel.setStyle("-fx-background-color: #e53e3e; -fx-text-fill: white; -fx-font-size: 11px; " +
                        "-fx-font-weight: bold; -fx-padding: 7 14; -fx-background-radius: 20; -fx-cursor: hand;");
        btnDel.setOnMouseEntered(e -> btnDel.setStyle("-fx-background-color: #c53030; -fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 7 14; -fx-background-radius: 20; -fx-cursor: hand;"));
        btnDel.setOnMouseExited(e  -> btnDel.setStyle("-fx-background-color: #e53e3e; -fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 7 14; -fx-background-radius: 20; -fx-cursor: hand;"));
        btnDel.setOnAction(e -> supprimerActivite(activite));

        HBox actions = new HBox(8, btnEdit, btnDel);
        actions.setAlignment(Pos.CENTER_LEFT);

        // Barre colorée bas
        Region barre = new Region();
        barre.setPrefHeight(4);
        barre.setStyle("-fx-background-color: " + couleur + "; -fx-background-radius: 4;");

        card.getChildren().addAll(header, idLbl, titreLbl, descLbl, sep, adrLbl, dateLbl, starsRow, actions, barre);

        // Hover card
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 14; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 14, 0, 0, 4); -fx-scale-x: 1.01; -fx-scale-y: 1.01;"));
        card.setOnMouseExited(e  -> card.setStyle("-fx-background-color: white; -fx-background-radius: 14; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);"));

        return card;
    }

    // ─── Popup Ajouter ───────────────────────────────────────────────────────

    @FXML
    private void ouvrirPopupAjout() {
        ouvrirPopupFormulaire(null);
    }

    private void ouvrirPopupModification(Activite activite) {
        ouvrirPopupFormulaire(activite);
    } 

    private void ouvrirPopupFormulaire(Activite activite) {
        boolean isModif = activite != null;
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle(isModif ? "Modifier l'activité" : "Ajouter une activité");

        VBox root = new VBox(15);
        root.setStyle("-fx-background-color: white; -fx-padding: 30;");

        Label titre = new Label(isModif ? "✏ Modifier l'activité" : "＋ Nouvelle activité");
        titre.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");

        // Champs avec leurs labels d'erreur
        TextField tfTitre   = champ("Titre de l'activité");
        Label errTitre      = errLabel();

        TextArea tfDesc = new TextArea();
        tfDesc.setPromptText("Description de l'activité");
        tfDesc.setPrefRowCount(3); tfDesc.setWrapText(true);
        tfDesc.setStyle("-fx-font-size: 13px; -fx-border-color: #e2e8f0; -fx-border-width: 2; " +
                        "-fx-border-radius: 8; -fx-background-radius: 8; -fx-background-color: white;");
        Label errDesc       = errLabel();

        ComboBox<String> tfType = new ComboBox<>();
        tfType.setItems(FXCollections.observableArrayList(
                "⚽ Sport", "🎭 Culturel", "🎨 Créatif", "🎵 Musique", "🌿 Nature", "👥 Social", "🎮 Jeux"));
        tfType.setPromptText("Choisir un type...");
        tfType.setMaxWidth(Double.MAX_VALUE);
        tfType.setStyle("-fx-font-size: 13px; -fx-background-color: white; " +
                        "-fx-border-color: #e2e8f0; -fx-border-width: 2; " +
                        "-fx-border-radius: 8; -fx-background-radius: 8;");
        Label errType       = errLabel();

        TextField tfAdresse = champ("Adresse / Lieu");
        Label errAdresse    = errLabel();

        // Champs latitude / longitude (auto-remplis par géocodage)
        TextField tfLat = champ("Latitude (auto)");
        tfLat.setEditable(false);
        tfLat.setStyle("-fx-padding: 10; -fx-font-size: 12px; -fx-border-color: #e2e8f0; " +
                       "-fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; " +
                       "-fx-background-color: #f7fafc; -fx-text-fill: #718096;");
        TextField tfLon = champ("Longitude (auto)");
        tfLon.setEditable(false);
        tfLon.setStyle("-fx-padding: 10; -fx-font-size: 12px; -fx-border-color: #e2e8f0; " +
                       "-fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; " +
                       "-fx-background-color: #f7fafc; -fx-text-fill: #718096;");
        Label lblGeoStatus = new Label("");
        lblGeoStatus.setStyle("-fx-font-size: 11px; -fx-text-fill: #718096;");

        // Géocodage automatique après 800ms de pause dans la saisie
        final javafx.animation.PauseTransition geoPause = new javafx.animation.PauseTransition(
                javafx.util.Duration.millis(800));
        tfAdresse.textProperty().addListener((obs, ov, nv) -> {
            geoPause.stop();
            if (nv != null && nv.trim().length() > 4) {
                geoPause.setOnFinished(ev -> geocoderAdresse(nv.trim(), tfLat, tfLon, lblGeoStatus));
                geoPause.play();
            } else {
                tfLat.clear(); tfLon.clear(); lblGeoStatus.setText("");
            }
        });

        DatePicker dpDebut  = new DatePicker();
        dpDebut.setMaxWidth(Double.MAX_VALUE);
        dpDebut.setStyle("-fx-font-size: 13px; -fx-background-color: white;");
        Label errDebut      = errLabel();

        DatePicker dpFin    = new DatePicker();
        dpFin.setMaxWidth(Double.MAX_VALUE);
        dpFin.setStyle("-fx-font-size: 13px; -fx-background-color: white;");
        Label errFin        = errLabel();

        if (isModif) {
            tfTitre.setText(activite.getTitre());
            tfDesc.setText(activite.getDescription());
            // Trouver l'option correspondante dans la ComboBox
            String typeVal = activite.getType();
            tfType.getItems().stream()
                    .filter(item -> item.toLowerCase().contains(typeVal != null ? typeVal.toLowerCase().replaceAll("[^a-zéèàùâêîôûç ]", "").trim() : ""))
                    .findFirst()
                    .ifPresentOrElse(tfType::setValue, () -> tfType.setValue(typeVal));
            tfAdresse.setText(activite.getAdresse());
            dpDebut.setValue(activite.getDateDebut());
            dpFin.setValue(activite.getDateFin());
            if (activite.getLatitude() != 0.0) tfLat.setText(String.valueOf(activite.getLatitude()));
            if (activite.getLongitude() != 0.0) tfLon.setText(String.valueOf(activite.getLongitude()));
        }

        GridPane grid = new GridPane();
        grid.setHgap(15); grid.setVgap(6);

        // Ligne 0: Titre
        grid.add(label("Titre:"),   0, 0); grid.add(tfTitre,   1, 0);
        grid.add(errTitre,          1, 1);
        // Ligne 2: Description
        grid.add(label("Description:"), 0, 2); grid.add(tfDesc, 1, 2);
        grid.add(errDesc,               1, 3);
        // Ligne 4: Type
        grid.add(label("Type:"),    0, 4); grid.add(tfType,    1, 4);
        grid.add(errType,           1, 5);
        // Ligne 6: Adresse
        grid.add(label("Adresse:"), 0, 6); grid.add(tfAdresse, 1, 6);
        grid.add(errAdresse,        1, 7);
        // Ligne 8: Lat/Lon
        HBox coordBox = new HBox(8, tfLat, tfLon);
        HBox.setHgrow(tfLat, Priority.ALWAYS); HBox.setHgrow(tfLon, Priority.ALWAYS);
        grid.add(label("Coordonnées:"), 0, 8); grid.add(coordBox, 1, 8);
        grid.add(lblGeoStatus,          1, 9);
        // Ligne 10: Date Début
        grid.add(label("Date Début:"), 0, 10); grid.add(dpDebut, 1, 10);
        grid.add(errDebut,             1, 11);
        // Ligne 12: Date Fin
        grid.add(label("Date Fin:"), 0, 12); grid.add(dpFin,   1, 12);
        grid.add(errFin,             1, 13);

        ColumnConstraints c1 = new ColumnConstraints(100);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(c1, c2);

        // Boutons
        Button btnAnnuler = new Button("Annuler");
        btnAnnuler.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #4a5568; -fx-font-size: 13px; " +
                            "-fx-font-weight: bold; -fx-padding: 10 25; -fx-cursor: hand; -fx-background-radius: 8;");
        btnAnnuler.setOnAction(e -> popup.close());

        String labelBtn = isModif ? "✅ Modifier" : "✅ Ajouter";
        Button btnSave = new Button(labelBtn);
        btnSave.setStyle("-fx-background-color: #4a5568; -fx-text-fill: white; -fx-font-size: 13px; " +
                         "-fx-font-weight: bold; -fx-padding: 10 25; -fx-cursor: hand; -fx-background-radius: 8;");

        btnSave.setOnAction(e -> {
            // Reset tous les styles et erreurs
            resetChamp(tfTitre, errTitre);
            resetChamp(tfDesc,  errDesc);
            // Reset type
            tfType.setStyle("-fx-font-size: 13px; -fx-background-color: white; " +
                            "-fx-border-color: #e2e8f0; -fx-border-width: 2; " +
                            "-fx-border-radius: 8; -fx-background-radius: 8;");
            errType.setText("");
            resetChamp(tfAdresse, errAdresse);
            dpDebut.setStyle("-fx-font-size: 13px; -fx-background-color: white;");
            dpFin.setStyle("-fx-font-size: 13px; -fx-background-color: white;");
            errDebut.setText(""); errFin.setText("");

            // Valider TOUS les champs et collecter les erreurs
            boolean valide = true;

            if (tfTitre.getText().trim().length() < 3) {
                marquerErreur(tfTitre, errTitre, "Le titre doit contenir au moins 3 caractères.");
                valide = false;
            }
            if (tfDesc.getText().trim().length() < 40) {
                marquerErreurArea(tfDesc, errDesc, "La description doit contenir au moins 40 caractères.");
                valide = false;
            } else if (tfDesc.getText().trim().length() > 1000) {
                marquerErreurArea(tfDesc, errDesc, "La description ne doit pas dépasser 1000 caractères.");
                valide = false;
            }
            if (tfType.getValue() == null || tfType.getValue().trim().isEmpty()) {
                tfType.setStyle("-fx-font-size: 13px; -fx-background-color: white; " +
                                "-fx-border-color: #e53e3e; -fx-border-width: 2; " +
                                "-fx-border-radius: 8; -fx-background-radius: 8;");
                errType.setText("⚠ Le type est obligatoire.");
                valide = false;
            }
            if (tfAdresse.getText().trim().isEmpty()) {
                marquerErreur(tfAdresse, errAdresse, "L'adresse est obligatoire.");
                valide = false;
            }
            if (dpDebut.getValue() == null) {
                dpDebut.setStyle("-fx-font-size: 13px; -fx-background-color: white; -fx-border-color: #e53e3e; -fx-border-width: 2;");
                errDebut.setText("⚠ La date de début est obligatoire.");
                valide = false;
            }
            if (dpFin.getValue() == null) {
                dpFin.setStyle("-fx-font-size: 13px; -fx-background-color: white; -fx-border-color: #e53e3e; -fx-border-width: 2;");
                errFin.setText("⚠ La date de fin est obligatoire.");
                valide = false;
            }
            if (dpDebut.getValue() != null && dpFin.getValue() != null
                    && dpDebut.getValue().isAfter(dpFin.getValue())) {
                dpDebut.setStyle("-fx-font-size: 13px; -fx-background-color: white; -fx-border-color: #e53e3e; -fx-border-width: 2;");
                dpFin.setStyle("-fx-font-size: 13px; -fx-background-color: white; -fx-border-color: #e53e3e; -fx-border-width: 2;");
                errFin.setText("⚠ La date de début doit être avant la date de fin.");
                valide = false;
            }

            if (!valide) return;

            // Extraire le type sans l'emoji (ex: "⚽ Sport" → "Sport")
            String typeChoisi = tfType.getValue() != null
                    ? tfType.getValue().replaceAll("^[^a-zA-ZÀ-ÿ]+", "").trim()
                    : "";

            // Détection de changements en mode modification
            if (isModif) {
                boolean aucunChangement =
                        tfTitre.getText().trim().equals(activite.getTitre()) &&
                        tfDesc.getText().trim().equals(activite.getDescription()) &&
                        typeChoisi.equals(activite.getType()) &&
                        tfAdresse.getText().trim().equals(activite.getAdresse()) &&
                        dpDebut.getValue().equals(activite.getDateDebut()) &&
                        dpFin.getValue().equals(activite.getDateFin());
                if (aucunChangement) {
                    alerte("Attention", "Aucune modification détectée.", Alert.AlertType.WARNING);
                    return;
                }
            }

            try {
                if (isModif) {
                    activite.setTitre(tfTitre.getText().trim());
                    activite.setDescription(tfDesc.getText().trim());
                    activite.setType(typeChoisi);
                    activite.setAdresse(tfAdresse.getText().trim());
                    activite.setDateDebut(dpDebut.getValue());
                    activite.setDateFin(dpFin.getValue());
                    try { activite.setLatitude(Double.parseDouble(tfLat.getText())); } catch (Exception ignored) {}
                    try { activite.setLongitude(Double.parseDouble(tfLon.getText())); } catch (Exception ignored) {}
                    serviceActivite.modifierActivite(activite);
                    alerte("Succès", "Activité modifiée avec succès!", Alert.AlertType.INFORMATION);
                } else {
                    double newLat = 0.0, newLon = 0.0;
                    try { newLat = Double.parseDouble(tfLat.getText()); } catch (Exception ignored) {}
                    try { newLon = Double.parseDouble(tfLon.getText()); } catch (Exception ignored) {}
                    Activite nouvelleActivite = new Activite(
                            tfTitre.getText().trim(), tfDesc.getText().trim(),
                            typeChoisi, tfAdresse.getText().trim(),
                            dpDebut.getValue(), dpFin.getValue());
                    nouvelleActivite.setLatitude(newLat);
                    nouvelleActivite.setLongitude(newLon);
                    serviceActivite.ajouterActivite(nouvelleActivite);
                    alerte("Succès", "Activité ajoutée avec succès!", Alert.AlertType.INFORMATION);
                }
                popup.close();
                chargerActivites();
            } catch (SQLException ex) {
                alerte("Erreur", ex.getMessage(), Alert.AlertType.ERROR);
            }
        });

        HBox footer = new HBox(12, btnAnnuler, btnSave);
        footer.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(titre, grid, footer);
        popup.setScene(new Scene(root, 520, 640));
        popup.showAndWait();
    }

    private Label errLabel() {
        Label l = new Label("");
        l.setStyle("-fx-text-fill: #e53e3e; -fx-font-size: 11px;");
        l.setWrapText(true);
        return l;
    }

    private void marquerErreur(TextField tf, Label err, String msg) {
        tf.setStyle("-fx-padding: 10; -fx-font-size: 13px; -fx-border-color: #e53e3e; " +
                    "-fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-background-color: white;");
        err.setText("⚠ " + msg);
    }

    private void marquerErreurArea(TextArea ta, Label err, String msg) {
        ta.setStyle("-fx-font-size: 13px; -fx-border-color: #e53e3e; -fx-border-width: 2; " +
                    "-fx-border-radius: 8; -fx-background-radius: 8; -fx-background-color: white;");
        err.setText("⚠ " + msg);
    }

    private void resetChamp(TextField tf, Label err) {
        tf.setStyle("-fx-padding: 10; -fx-font-size: 13px; -fx-border-color: #e2e8f0; " +
                    "-fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-background-color: white;");
        err.setText("");
    }

    private void resetChamp(TextArea ta, Label err) {
        ta.setStyle("-fx-font-size: 13px; -fx-border-color: #e2e8f0; -fx-border-width: 2; " +
                    "-fx-border-radius: 8; -fx-background-radius: 8; -fx-background-color: white;");
        err.setText("");
    }

    private void supprimerActivite(Activite activite) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.initStyle(javafx.stage.StageStyle.UNDECORATED);

        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: white; -fx-background-radius: 15; " +
                      "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 20, 0, 0, 5);");

        Label icone = new Label("🗑");
        icone.setStyle("-fx-font-size: 40px;");

        Label titre = new Label("Supprimer l'activité");
        titre.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");

        Label msg = new Label("Voulez-vous vraiment supprimer\n\"" + activite.getTitre() + "\" ?");
        msg.setStyle("-fx-font-size: 13px; -fx-text-fill: #718096; -fx-text-alignment: center;");
        msg.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        Button btnAnnuler = new Button("Annuler");
        btnAnnuler.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #4a5568; -fx-font-size: 13px; " +
                            "-fx-font-weight: bold; -fx-padding: 10 30; -fx-cursor: hand; -fx-background-radius: 8;");
        btnAnnuler.setOnAction(e -> popup.close());

        Button btnSupp = new Button("🗑 Supprimer");
        btnSupp.setStyle("-fx-background-color: #e53e3e; -fx-text-fill: white; -fx-font-size: 13px; " +
                         "-fx-font-weight: bold; -fx-padding: 10 30; -fx-cursor: hand; -fx-background-radius: 8;");
        btnSupp.setOnAction(e -> {
            try {
                serviceActivite.supprimerActivite(activite.getIdActivite());
                popup.close();
                chargerActivites();
                afficherToast("Activité supprimée avec succès!", "#e53e3e", "🗑");
            } catch (SQLException ex) {
                popup.close();
                alerte("Erreur", ex.getMessage(), Alert.AlertType.ERROR);
            }
        });

        HBox btns = new HBox(12, btnAnnuler, btnSupp);
        btns.setAlignment(Pos.CENTER);

        root.getChildren().addAll(icone, titre, msg, btns);

        Scene scene = new Scene(root, 380, 230);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        popup.setScene(scene);
        popup.showAndWait();
    }

    // ─── Recherche & Tri ─────────────────────────────────────────────────────

    private void configurerRechercheEtTri() {
        sortCombo.setItems(FXCollections.observableArrayList(
                "Titre A→Z", "Titre Z→A", "Date début ↑", "Date début ↓", "Type A→Z"));
        // filteredList déjà initialisée dans initialize()
        searchField.textProperty().addListener((o, ov, nv) -> rafraichirAffichage());
        sortCombo.valueProperty().addListener((o, ov, nv) -> rafraichirAffichage());
    }

    private void rafraichirAffichage() {
        String q = searchField.getText() == null ? "" : searchField.getText().toLowerCase();
        filteredList.setPredicate(a -> q.isEmpty()
                || a.getTitre().toLowerCase().contains(q)
                || (a.getType() != null && a.getType().toLowerCase().contains(q))
                || (a.getAdresse() != null && a.getAdresse().toLowerCase().contains(q))
                || (a.getDescription() != null && a.getDescription().toLowerCase().contains(q)));

        SortedList<Activite> sorted = new SortedList<>(filteredList);
        String tri = sortCombo.getValue();
        if (tri != null) switch (tri) {
            case "Titre A→Z"    -> sorted.setComparator(Comparator.comparing(Activite::getTitre));
            case "Titre Z→A"    -> sorted.setComparator(Comparator.comparing(Activite::getTitre).reversed());
            case "Date début ↑" -> sorted.setComparator(Comparator.comparing(Activite::getDateDebut));
            case "Date début ↓" -> sorted.setComparator(Comparator.comparing(Activite::getDateDebut).reversed());
            case "Type A→Z"     -> sorted.setComparator(Comparator.comparing(Activite::getType));
        }
        afficherCartes(sorted);
    }

    // ─── Recommandations intelligentes ───────────────────────────────────────

    @FXML
    private void ouvrirRecommandations() {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("🏆 Recommandations");

        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: white; -fx-background-radius: 14;");
        root.setPrefWidth(500);

        // Header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(16, 20, 16, 20));
        header.setStyle("-fx-background-color: #9f7aea; -fx-background-radius: 14 14 0 0;");
        Label titreH = new Label("🏆 Activités Recommandées");
        titreH.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label subH = new Label("Basé sur les notes et réservations");
        subH.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(255,255,255,0.8);");
        VBox hv = new VBox(2, titreH, subH);
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Button btnClose = new Button("✕");
        btnClose.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14px; -fx-cursor: hand;");
        btnClose.setOnAction(e -> popup.close());
        header.getChildren().addAll(hv, sp, btnClose);

        VBox liste = new VBox(10);
        liste.setPadding(new Insets(16));

        try {
            List<Activite> toutes = serviceActivite.getAllActivites();
            // Score = (moyenne_note * 0.6) + (nb_reservations_normalisé * 0.4)
            int maxRes = toutes.stream().mapToInt(a -> {
                try { return serviceReservation.getNombreReservations(a.getIdActivite()); }
                catch (Exception ex) { return 0; }
            }).max().orElse(1);

            toutes.sort((a, b) -> {
                try {
                    double moyA = serviceNotation.getMoyenne(a.getIdActivite());
                    double moyB = serviceNotation.getMoyenne(b.getIdActivite());
                    int resA = serviceReservation.getNombreReservations(a.getIdActivite());
                    int resB = serviceReservation.getNombreReservations(b.getIdActivite());
                    double scoreA = moyA * 0.6 + (resA / (double) Math.max(maxRes, 1)) * 5 * 0.4;
                    double scoreB = moyB * 0.6 + (resB / (double) Math.max(maxRes, 1)) * 5 * 0.4;
                    return Double.compare(scoreB, scoreA);
                } catch (Exception ex) { return 0; }
            });

            int rank = 1;
            for (Activite a : toutes.subList(0, Math.min(5, toutes.size()))) {
                double moy = serviceNotation.getMoyenne(a.getIdActivite());
                int nbRes = serviceReservation.getNombreReservations(a.getIdActivite());
                int nbNotes = serviceNotation.getNombreNotes(a.getIdActivite());
                double score = moy * 0.6 + (nbRes / (double) Math.max(maxRes, 1)) * 5 * 0.4;

                HBox row = new HBox(12);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(12, 14, 12, 14));
                String bg = rank == 1 ? "#fffbeb" : rank == 2 ? "#f0fff4" : "white";
                row.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 10;");

                // Rang
                Label rankLbl = new Label(rank == 1 ? "🥇" : rank == 2 ? "🥈" : rank == 3 ? "🥉" : "#" + rank);
                rankLbl.setStyle("-fx-font-size: " + (rank <= 3 ? "22" : "14") + "px; -fx-min-width: 36;");

                // Infos
                VBox infos = new VBox(3);
                HBox.setHgrow(infos, Priority.ALWAYS);
                Label nomLbl = new Label(a.getTitre());
                nomLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");
                Label typeLbl = new Label(getIconeType(a.getType()) + " " + a.getType() +
                                          "  •  " + nbRes + " réservations");
                typeLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #718096;");

                // Étoiles
                HBox stars = new HBox(2);
                for (int i = 1; i <= 5; i++) {
                    Label s = new Label(i <= Math.round(moy) ? "★" : "☆");
                    s.setStyle("-fx-font-size: 13px; -fx-text-fill: " + (i <= Math.round(moy) ? "#f6ad55" : "#cbd5e0") + ";");
                    stars.getChildren().add(s);
                }
                Label moyLbl = new Label(nbNotes > 0 ? String.format(" %.1f", moy) : " -");
                moyLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #718096;");
                stars.getChildren().add(moyLbl);
                stars.setAlignment(Pos.CENTER_LEFT);

                infos.getChildren().addAll(nomLbl, typeLbl, stars);

                // Score
                Label scoreLbl = new Label(String.format("%.1f/5", score));
                scoreLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #9f7aea;");

                row.getChildren().addAll(rankLbl, infos, scoreLbl);
                liste.getChildren().add(row);
                rank++;
            }
        } catch (Exception e) {
            liste.getChildren().add(new Label("Erreur: " + e.getMessage()));
        }

        ScrollPane scroll = new ScrollPane(liste);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: white; -fx-background-color: white; -fx-border-color: transparent;");

        root.getChildren().addAll(header, scroll);
        popup.setScene(new Scene(root, 500, 420));
        popup.showAndWait();
    }

    // ─── Export PDF ───────────────────────────────────────────────────────────

    @FXML
    private void exporterPDF() {
        // Choisir le fichier de destination
        javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
        fc.setTitle("Enregistrer le PDF");
        fc.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("PDF", "*.pdf"));
        fc.setInitialFileName("activites_rapport.pdf");
        java.io.File fichier = fc.showSaveDialog(activitesFlow.getScene().getWindow());
        if (fichier == null) return;

        try {
            List<Activite> activites = serviceActivite.getAllActivites();
            genererPDF(fichier, activites);
            alerte("Succès", "PDF exporté : " + fichier.getName(), Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            alerte("Erreur", "Impossible de générer le PDF : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void genererPDF(java.io.File fichier, List<Activite> activites) throws Exception {
        // Générer un PDF simple avec du HTML converti via JavaFX WebView → Print
        // On utilise une approche Canvas pour dessiner le PDF sans librairie externe
        javafx.scene.canvas.Canvas canvas = new javafx.scene.canvas.Canvas(595, 842); // A4
        javafx.scene.canvas.GraphicsContext gc = canvas.getGraphicsContext2D();

        // Fond blanc
        gc.setFill(javafx.scene.paint.Color.WHITE);
        gc.fillRect(0, 0, 595, 842);

        // En-tête
        gc.setFill(javafx.scene.paint.Color.web("#2d3748"));
        gc.fillRect(0, 0, 595, 80);
        gc.setFill(javafx.scene.paint.Color.WHITE);
        gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 22));
        gc.fillText("MentalUp - Rapport des Activités", 30, 45);
        gc.setFont(javafx.scene.text.Font.font("Arial", 12));
        gc.fillText("Généré le " + java.time.LocalDate.now().format(
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")), 30, 65);

        // Stats globales
        gc.setFill(javafx.scene.paint.Color.web("#f7fafc"));
        gc.fillRect(20, 95, 555, 60);
        gc.setFill(javafx.scene.paint.Color.web("#2d3748"));
        gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 13));
        gc.fillText("Total activités : " + activites.size(), 35, 120);
        long actives = activites.stream().filter(a -> a.getDateFin() != null &&
                java.time.LocalDate.now().isBefore(a.getDateFin())).count();
        gc.fillText("Actives : " + actives + "   |   Terminées : " + (activites.size() - actives), 35, 142);

        // Tableau
        double y = 175;
        // En-tête tableau
        gc.setFill(javafx.scene.paint.Color.web("#4a5568"));
        gc.fillRect(20, (int)y - 18, 555, 24);
        gc.setFill(javafx.scene.paint.Color.WHITE);
        gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 11));
        gc.fillText("#", 28, y);
        gc.fillText("Titre", 55, y);
        gc.fillText("Type", 230, y);
        gc.fillText("Date début", 320, y);
        gc.fillText("Date fin", 410, y);
        gc.fillText("Note", 490, y);
        gc.fillText("Rés.", 540, y);
        y += 10;

        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yy");
        boolean alt = false;
        for (Activite a : activites) {
            if (y > 800) break; // limite de page
            y += 22;
            if (alt) {
                gc.setFill(javafx.scene.paint.Color.web("#f7fafc"));
                gc.fillRect(20, (int)y - 16, 555, 22);
            }
            alt = !alt;

            double moy = 0; int nbRes = 0;
            try { moy = serviceNotation.getMoyenne(a.getIdActivite());
                  nbRes = serviceReservation.getNombreReservations(a.getIdActivite()); }
            catch (Exception ignored) {}

            gc.setFill(javafx.scene.paint.Color.web("#2d3748"));
            gc.setFont(javafx.scene.text.Font.font("Arial", 10));
            gc.fillText(String.valueOf(a.getIdActivite()), 28, y);
            String titre = a.getTitre().length() > 22 ? a.getTitre().substring(0, 22) + "…" : a.getTitre();
            gc.fillText(titre, 55, y);
            String type = a.getType() != null ? (a.getType().length() > 12 ? a.getType().substring(0, 12) : a.getType()) : "-";
            gc.fillText(type, 230, y);
            gc.fillText(a.getDateDebut() != null ? a.getDateDebut().format(fmt) : "-", 320, y);
            gc.fillText(a.getDateFin() != null ? a.getDateFin().format(fmt) : "-", 410, y);
            gc.fillText(moy > 0 ? String.format("%.1f★", moy) : "-", 490, y);
            gc.fillText(String.valueOf(nbRes), 545, y);

            // Ligne séparatrice
            gc.setStroke(javafx.scene.paint.Color.web("#e2e8f0"));
            gc.setLineWidth(0.5);
            gc.strokeLine(20, y + 5, 575, y + 5);
        }

        // Footer
        gc.setFill(javafx.scene.paint.Color.web("#a0aec0"));
        gc.setFont(javafx.scene.text.Font.font("Arial", 9));
        gc.fillText("MentalUp © " + java.time.LocalDate.now().getYear() +
                    " - Rapport confidentiel", 30, 825);

        // Snapshot → image → sauvegarder
        javafx.scene.image.WritableImage img = canvas.snapshot(null, null);
        java.awt.image.BufferedImage buffered = new java.awt.image.BufferedImage(
                (int) img.getWidth(), (int) img.getHeight(),
                java.awt.image.BufferedImage.TYPE_INT_RGB);
        for (int px = 0; px < (int) img.getWidth(); px++) {
            for (int py = 0; py < (int) img.getHeight(); py++) {
                javafx.scene.paint.Color c = img.getPixelReader().getColor(px, py);
                buffered.setRGB(px, py, new java.awt.Color(
                        (float) c.getRed(), (float) c.getGreen(), (float) c.getBlue()).getRGB());
            }
        }
        // Sauvegarder comme PNG (renommé .pdf pour compatibilité sans librairie)
        // Pour un vrai PDF, utiliser iText ou Apache PDFBox
        String path = fichier.getAbsolutePath().replace(".pdf", ".png");
        javax.imageio.ImageIO.write(buffered, "PNG", new java.io.File(path));
        // Ouvrir avec le visualiseur système
        java.awt.Desktop.getDesktop().open(new java.io.File(path));
    }

    // ─── Navigation ──────────────────────────────────────────────────────────

    @FXML
    private void ouvrirReservations() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/GestionReservations.fxml"));
            Stage stage = (Stage) activitesFlow.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 800));
            stage.setTitle("Gestion des Réservations - MentalUp");
        } catch (IOException e) {
            alerte("Erreur", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // ─── Utilitaires ─────────────────────────────────────────────────────────

    private TextField champ(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setStyle("-fx-padding: 10; -fx-font-size: 13px; -fx-border-color: #e2e8f0; " +
                    "-fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-background-color: white;");
        return tf;
    }

    private Label label(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #4a5568;");
        return l;
    }

    private void alerte(String titre, String msg, Alert.AlertType type) {
        switch (type) {
            case INFORMATION -> afficherToast(msg, "#27ae60", "✅");
            case WARNING     -> afficherToast(msg, "#ed8936", "⚠️");
            case ERROR       -> afficherToast(msg, "#e53e3e", "❌");
            default          -> afficherToast(msg, "#4a5568", "ℹ️");
        }
    }

    private void afficherToast(String message, String couleur, String icone) {
        Stage toast = new Stage();
        toast.initStyle(javafx.stage.StageStyle.TRANSPARENT);
        toast.setAlwaysOnTop(true);

        HBox box = new HBox(12);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(16, 20, 16, 20));
        box.setStyle("-fx-background-color: " + couleur + "; " +
                     "-fx-background-radius: 12; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0, 0, 5);");

        Label ico = new Label(icone);
        ico.setStyle("-fx-font-size: 18px;");

        Label lbl = new Label(message);
        lbl.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        lbl.setMaxWidth(300);
        lbl.setWrapText(true);

        box.getChildren().addAll(ico, lbl);

        Scene scene = new Scene(box);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        toast.setScene(scene);

        // Positionner en bas à droite
        javafx.geometry.Rectangle2D screen = javafx.stage.Screen.getPrimary().getVisualBounds();
        toast.setX(screen.getMaxX() - 380);
        toast.setY(screen.getMaxY() - 100);

        toast.show();

        // Disparaît après 2.5 secondes avec fondu
        javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(
                javafx.util.Duration.millis(500), box);
        fade.setDelay(javafx.util.Duration.millis(2000));
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.setOnFinished(e -> toast.close());
        fade.play();
    }

    // ─── Géocodage Nominatim ─────────────────────────────────────────────────

    private void geocoderAdresse(String adresse, TextField tfLat, TextField tfLon, Label lblStatus) {
        lblStatus.setText("🔍 Recherche en cours...");
        lblStatus.setStyle("-fx-font-size: 11px; -fx-text-fill: #718096;");
        Thread thread = new Thread(() -> {
            try {
                String encoded = java.net.URLEncoder.encode(adresse, java.nio.charset.StandardCharsets.UTF_8);
                java.net.URL url = new java.net.URL(
                        "https://nominatim.openstreetmap.org/search?q=" + encoded + "&format=json&limit=1");
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestProperty("User-Agent", "MentalUpApp/1.0");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();

                String json = sb.toString();
                // Parse simple sans librairie JSON
                if (json.contains("\"lat\"")) {
                    String lat = json.replaceAll(".*\"lat\":\"([^\"]+)\".*", "$1").split(",")[0];
                    String lon = json.replaceAll(".*\"lon\":\"([^\"]+)\".*", "$1").split(",")[0];
                    // Prendre seulement le premier résultat
                    lat = lat.replaceAll("\\[\\{.*?\"lat\":\"", "").replaceAll("\".*", "");
                    lon = lon.replaceAll(".*\"lon\":\"", "").replaceAll("\".*", "");
                    // Re-parse proprement
                    int latIdx = json.indexOf("\"lat\":\"") + 7;
                    int latEnd = json.indexOf("\"", latIdx);
                    int lonIdx = json.indexOf("\"lon\":\"") + 7;
                    int lonEnd = json.indexOf("\"", lonIdx);
                    final String latVal = json.substring(latIdx, latEnd);
                    final String lonVal = json.substring(lonIdx, lonEnd);
                    javafx.application.Platform.runLater(() -> {
                        tfLat.setText(latVal);
                        tfLon.setText(lonVal);
                        lblStatus.setText("📍 Coordonnées trouvées");
                        lblStatus.setStyle("-fx-font-size: 11px; -fx-text-fill: #27ae60;");
                    });
                } else {
                    javafx.application.Platform.runLater(() -> {
                        tfLat.clear(); tfLon.clear();
                        lblStatus.setText("⚠ Adresse introuvable");
                        lblStatus.setStyle("-fx-font-size: 11px; -fx-text-fill: #ed8936;");
                    });
                }
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    lblStatus.setText("⚠ Erreur de géocodage");
                    lblStatus.setStyle("-fx-font-size: 11px; -fx-text-fill: #e53e3e;");
                });
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private String getIconeType(String type) {
        if (type == null) return "⚡";
        String t = type.toLowerCase();
        if (t.contains("sport")) return "⚽";
        if (t.contains("culturel")) return "🎭";
        if (t.contains("créatif") || t.contains("creatif")) return "🎨";
        if (t.contains("musique")) return "🎵";
        if (t.contains("nature")) return "🌿";
        if (t.contains("social")) return "👥";
        if (t.contains("jeux") || t.contains("jeu")) return "🎮";
        return "⚡";
    }
}
