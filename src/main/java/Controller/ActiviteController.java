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
    @FXML private Button btnVuePublique;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortCombo;

    private ServiceActivite serviceActivite;
    private ObservableList<Activite> activitesList;
    private FilteredList<Activite> filteredList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        serviceActivite = new ServiceActivite();
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

        card.getChildren().addAll(header, idLbl, titreLbl, descLbl, sep, adrLbl, dateLbl, actions, barre);

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

        // Champs
        TextField tfTitre = champ("Titre de l'activité");
        TextArea tfDesc   = new TextArea();
        tfDesc.setPromptText("Description de l'activité");
        tfDesc.setPrefRowCount(3); tfDesc.setWrapText(true);
        tfDesc.setStyle("-fx-font-size: 13px; -fx-border-color: #e2e8f0; -fx-border-width: 2; " +
                        "-fx-border-radius: 8; -fx-background-radius: 8; -fx-background-color: white;");
        TextField tfType    = champ("Type (sport, culturel, créatif...)");
        TextField tfAdresse = champ("Adresse / Lieu");
        DatePicker dpDebut  = new DatePicker();
        DatePicker dpFin    = new DatePicker();
        dpDebut.setMaxWidth(Double.MAX_VALUE);
        dpFin.setMaxWidth(Double.MAX_VALUE);
        dpDebut.setStyle("-fx-font-size: 13px; -fx-background-color: white;");
        dpFin.setStyle("-fx-font-size: 13px; -fx-background-color: white;");

        if (isModif) {
            tfTitre.setText(activite.getTitre());
            tfDesc.setText(activite.getDescription());
            tfType.setText(activite.getType());
            tfAdresse.setText(activite.getAdresse());
            dpDebut.setValue(activite.getDateDebut());
            dpFin.setValue(activite.getDateFin());
        }

        GridPane grid = new GridPane();
        grid.setHgap(15); grid.setVgap(12);
        grid.add(label("Titre:"),        0, 0); grid.add(tfTitre,   1, 0);
        grid.add(label("Description:"),  0, 1); grid.add(tfDesc,    1, 1);
        grid.add(label("Type:"),         0, 2); grid.add(tfType,    1, 2);
        grid.add(label("Adresse:"),      0, 3); grid.add(tfAdresse, 1, 3);
        grid.add(label("Date Début:"),   0, 4); grid.add(dpDebut,   1, 4);
        grid.add(label("Date Fin:"),     0, 5); grid.add(dpFin,     1, 5);
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
            // Validation
            if (tfTitre.getText().trim().length() < 3) { alerte("Validation", "Titre: min 3 caractères.", Alert.AlertType.ERROR); return; }
            if (tfDesc.getText().trim().length() < 40)  { alerte("Validation", "Description: min 40 caractères.", Alert.AlertType.ERROR); return; }
            if (tfType.getText().trim().isEmpty())       { alerte("Validation", "Type obligatoire.", Alert.AlertType.ERROR); return; }
            if (tfAdresse.getText().trim().isEmpty())    { alerte("Validation", "Adresse obligatoire.", Alert.AlertType.ERROR); return; }
            if (dpDebut.getValue() == null)              { alerte("Validation", "Date début obligatoire.", Alert.AlertType.ERROR); return; }
            if (dpFin.getValue() == null)                { alerte("Validation", "Date fin obligatoire.", Alert.AlertType.ERROR); return; }
            if (dpDebut.getValue().isAfter(dpFin.getValue())) { alerte("Validation", "Date début doit être avant date fin.", Alert.AlertType.ERROR); return; }

            // Détection de changements (seulement en mode modification)
            if (isModif) {
                boolean aucunChangement =
                        tfTitre.getText().trim().equals(activite.getTitre()) &&
                        tfDesc.getText().trim().equals(activite.getDescription()) &&
                        tfType.getText().trim().equals(activite.getType()) &&
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
                    activite.setType(tfType.getText().trim());
                    activite.setAdresse(tfAdresse.getText().trim());
                    activite.setDateDebut(dpDebut.getValue());
                    activite.setDateFin(dpFin.getValue());
                    serviceActivite.modifierActivite(activite);
                    alerte("Succès", "Activité modifiée avec succès!", Alert.AlertType.INFORMATION);
                } else {
                    serviceActivite.ajouterActivite(new Activite(
                            tfTitre.getText().trim(), tfDesc.getText().trim(),
                            tfType.getText().trim(), tfAdresse.getText().trim(),
                            dpDebut.getValue(), dpFin.getValue()));
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
        popup.setScene(new Scene(root, 500, 460));
        popup.showAndWait();
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

    // ─── Navigation ──────────────────────────────────────────────────────────

    @FXML
    private void ouvrirVuePublique() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/EtudiantActivites.fxml"));
            Stage stage = new Stage();
            stage.setTitle("MentalUp - Espace Étudiant");
            stage.setScene(new Scene(root, 1280, 850));
            stage.show();
        } catch (IOException e) {
            alerte("Erreur", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

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

    private String getIconeType(String type) {
        if (type == null) return "⚡";
        String t = type.toLowerCase();
        if (t.contains("sport")) return "⚽";
        if (t.contains("culturel")) return "🎭";
        if (t.contains("créatif") || t.contains("creatif")) return "🎨";
        if (t.contains("musique")) return "🎵";
        if (t.contains("nature")) return "🌿";
        if (t.contains("social")) return "👥";
        return "⚡";
    }
}
