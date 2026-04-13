package Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import models.Reservation;
import services.ServiceReservation;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class ReservationAdminController implements Initializable {

    @FXML private VBox reservationsVBox;
    @FXML private Label lblTotal;

    private ServiceReservation serviceReservation;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        serviceReservation = new ServiceReservation();
        chargerReservations();
    }

    // ─── Chargement ──────────────────────────────────────────────────────────

    private void chargerReservations() {
        try {
            List<Reservation> list = serviceReservation.getAllReservations();
            reservationsVBox.getChildren().clear();
            lblTotal.setText(String.valueOf(list.size()));

            if (list.isEmpty()) {
                Label vide = new Label("Aucune réservation trouvée");
                vide.setStyle("-fx-font-size: 14px; -fx-text-fill: #a0aec0; -fx-padding: 40;");
                vide.setMaxWidth(Double.MAX_VALUE);
                vide.setAlignment(Pos.CENTER);
                reservationsVBox.getChildren().add(vide);
            } else {
                for (Reservation r : list) {
                    reservationsVBox.getChildren().add(creerCarte(r));
                }
            }
        } catch (SQLException e) {
            afficherToast("Erreur chargement: " + e.getMessage(), "#e53e3e", "❌");
        }
    }

    // ─── Carte réservation ───────────────────────────────────────────────────

    private HBox creerCarte(Reservation r) {
        HBox card = new HBox(20);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(18, 22, 18, 22));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 14; " +
                      "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.07), 8, 0, 0, 2);");

        // Badge ID
        Label idLbl = new Label("#" + r.getIdReservation());
        idLbl.setMinWidth(50);
        idLbl.setAlignment(Pos.CENTER);
        idLbl.setStyle("-fx-background-color: #2d3748; -fx-text-fill: white; " +
                       "-fx-font-size: 13px; -fx-font-weight: bold; " +
                       "-fx-padding: 6 12; -fx-background-radius: 20;");

        // Activité
        VBox vbActivite = new VBox(3);
        vbActivite.setMinWidth(180);
        Label lblActiviteTitre = new Label(r.getTitreActivite() != null ? r.getTitreActivite() : "—");
        lblActiviteTitre.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");
        Label lblActiviteSub = new Label("Activité");
        lblActiviteSub.setStyle("-fx-font-size: 11px; -fx-text-fill: #a0aec0;");
        vbActivite.getChildren().addAll(lblActiviteSub, lblActiviteTitre);

        // Étudiant
        VBox vbEtudiant = new VBox(3);
        vbEtudiant.setMinWidth(150);
        Label lblEtudiantSub = new Label("Étudiant");
        lblEtudiantSub.setStyle("-fx-font-size: 11px; -fx-text-fill: #a0aec0;");
        Label lblEtudiantVal = new Label("👤 " + (r.getNomEtudiant() != null ? r.getNomEtudiant() : "—"));
        lblEtudiantVal.setStyle("-fx-font-size: 13px; -fx-text-fill: #2d3748;");
        vbEtudiant.getChildren().addAll(lblEtudiantSub, lblEtudiantVal);

        // Place
        VBox vbPlace = new VBox(3);
        vbPlace.setMinWidth(80);
        Label lblPlaceSub = new Label("Place");
        lblPlaceSub.setStyle("-fx-font-size: 11px; -fx-text-fill: #a0aec0;");
        Label lblPlaceVal = new Label(r.getPlace() != null ? r.getPlace() : "—");
        lblPlaceVal.setStyle("-fx-background-color: #e8f4f8; -fx-text-fill: #2d3748; " +
                             "-fx-font-size: 14px; -fx-font-weight: bold; " +
                             "-fx-padding: 4 12; -fx-background-radius: 8;");
        vbPlace.getChildren().addAll(lblPlaceSub, lblPlaceVal);

        // Date
        VBox vbDate = new VBox(3);
        vbDate.setMinWidth(130);
        Label lblDateSub = new Label("Date réservation");
        lblDateSub.setStyle("-fx-font-size: 11px; -fx-text-fill: #a0aec0;");
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        Label lblDateVal = new Label(r.getDateReservation() != null ? "📅 " + r.getDateReservation().format(fmt) : "—");
        lblDateVal.setStyle("-fx-font-size: 13px; -fx-text-fill: #4a5568;");
        vbDate.getChildren().addAll(lblDateSub, lblDateVal);

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Boutons
        Button btnEdit = new Button("✏ Modifier");
        btnEdit.setStyle("-fx-background-color: #2d3748; -fx-text-fill: white; -fx-font-size: 12px; " +
                         "-fx-font-weight: bold; -fx-padding: 9 18; -fx-background-radius: 20; -fx-cursor: hand;");
        btnEdit.setOnMouseEntered(e -> btnEdit.setStyle("-fx-background-color: #4a5568; -fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 9 18; -fx-background-radius: 20; -fx-cursor: hand;"));
        btnEdit.setOnMouseExited(e  -> btnEdit.setStyle("-fx-background-color: #2d3748; -fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 9 18; -fx-background-radius: 20; -fx-cursor: hand;"));
        btnEdit.setOnAction(e -> ouvrirModification(r));
        btnEdit.setMinWidth(110);

        Button btnDel = new Button("🗑 Supprimer");
        btnDel.setStyle("-fx-background-color: #e53e3e; -fx-text-fill: white; -fx-font-size: 12px; " +
                        "-fx-font-weight: bold; -fx-padding: 9 18; -fx-background-radius: 20; -fx-cursor: hand;");
        btnDel.setOnMouseEntered(e -> btnDel.setStyle("-fx-background-color: #c53030; -fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 9 18; -fx-background-radius: 20; -fx-cursor: hand;"));
        btnDel.setOnMouseExited(e  -> btnDel.setStyle("-fx-background-color: #e53e3e; -fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 9 18; -fx-background-radius: 20; -fx-cursor: hand;"));
        btnDel.setOnAction(e -> afficherConfirmationSuppression(r));
        btnDel.setMinWidth(110);

        HBox actions = new HBox(10, btnEdit, btnDel);
        actions.setAlignment(Pos.CENTER_RIGHT);

        card.getChildren().addAll(idLbl, vbActivite, vbEtudiant, vbPlace, vbDate, spacer, actions);

        // Hover
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 14; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.13), 12, 0, 0, 4);"));
        card.setOnMouseExited(e  -> card.setStyle("-fx-background-color: white; -fx-background-radius: 14; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.07), 8, 0, 0, 2);"));

        return card;
    }

    // ─── Navigation ──────────────────────────────────────────────────────────

    @FXML
    private void ouvrirActivites() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/GestionActivite.fxml"));
            Stage stage = (Stage) reservationsVBox.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 800));
            stage.setTitle("Gestion des Activités - MentalUp");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ─── Popup Modification ──────────────────────────────────────────────────

    private void ouvrirModification(Reservation r) {
        Stage popup = new Stage();
        popup.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        popup.setTitle("Modifier la réservation #" + r.getIdReservation());
        popup.setResizable(false);

        final String nomInitial   = r.getNomEtudiant();
        final String placeInitial = r.getPlace();
        final java.time.LocalDate dateInitiale = r.getDateReservation();

        VBox root = new VBox(18);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #f5f7fa;");

        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        Label titreLabel = new Label("✏  Modifier la réservation  #" + r.getIdReservation());
        titreLabel.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");
        root.getChildren().add(titreLabel);

        // Carte blanche
        VBox card = new VBox(16);
        card.setPadding(new Insets(25));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 14; " +
                      "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 3);");

        VBox boxNom   = fieldBox("👤  Nom de l'étudiant", r.getNomEtudiant());
        VBox boxPlace = fieldBox("🪑  Place (ex: A1, H8)", r.getPlace());
        TextField tfNom   = (TextField) boxNom.getChildren().get(1);
        TextField tfPlace = (TextField) boxPlace.getChildren().get(1);

        VBox boxDate = new VBox(6);
        Label lblDate = new Label("📅  Date de réservation");
        lblDate.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #4a5568;");
        DatePicker dpDate = new DatePicker(r.getDateReservation());
        dpDate.setMaxWidth(Double.MAX_VALUE);
        dpDate.setStyle("-fx-font-size: 13px; -fx-background-color: white;");
        boxDate.getChildren().addAll(lblDate, dpDate);

        card.getChildren().addAll(boxNom, boxPlace, boxDate);
        root.getChildren().add(card);

        // Boutons
        Button btnAnnuler = new Button("Annuler");
        btnAnnuler.setMaxWidth(Double.MAX_VALUE);
        btnAnnuler.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #4a5568; -fx-font-size: 13px; " +
                            "-fx-font-weight: bold; -fx-padding: 13; -fx-cursor: hand; -fx-background-radius: 10;");
        btnAnnuler.setOnAction(e -> popup.close());

        Button btnModifier = new Button("✅  Modifier");
        btnModifier.setMaxWidth(Double.MAX_VALUE);
        btnModifier.setStyle("-fx-background-color: #2d3748; -fx-text-fill: white; -fx-font-size: 13px; " +
                             "-fx-font-weight: bold; -fx-padding: 13; -fx-cursor: hand; -fx-background-radius: 10;");
        btnModifier.setOnAction(e -> {
            if (tfNom.getText().trim().isEmpty() || tfPlace.getText().trim().isEmpty() || dpDate.getValue() == null) {
                afficherToast("Tous les champs sont obligatoires!", "#ed8936", "⚠️"); return;
            }
            boolean aucun = tfNom.getText().trim().equals(nomInitial)
                    && tfPlace.getText().trim().toUpperCase().equals(placeInitial)
                    && dpDate.getValue().equals(dateInitiale);
            if (aucun) { afficherToast("Aucune modification détectée!", "#ed8936", "⚠️"); return; }
            try {
                r.setNomEtudiant(tfNom.getText().trim());
                r.setPlace(tfPlace.getText().trim().toUpperCase());
                r.setDateReservation(dpDate.getValue());
                serviceReservation.modifierReservation(r);
                popup.close();
                chargerReservations();
                afficherToast("Réservation modifiée avec succès!", "#27ae60", "✅");
            } catch (SQLException ex) {
                afficherToast("Erreur: " + ex.getMessage(), "#e53e3e", "❌");
            }
        });

        HBox btns = new HBox(12, btnAnnuler, btnModifier);
        HBox.setHgrow(btnAnnuler, Priority.ALWAYS);
        HBox.setHgrow(btnModifier, Priority.ALWAYS);
        root.getChildren().add(btns);

        popup.setScene(new Scene(root, 440, 420));
        popup.showAndWait();
    }

    // ─── Popup Suppression ───────────────────────────────────────────────────

    private void afficherConfirmationSuppression(Reservation r) {
        Stage popup = new Stage();
        popup.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        popup.setTitle("Confirmer la suppression");
        popup.setResizable(false);

        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(35));
        root.setStyle("-fx-background-color: #f5f7fa;");

        VBox card = new VBox(18);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(30));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 14; " +
                      "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 3);");

        Label icone = new Label("🗑");
        icone.setStyle("-fx-font-size: 42px;");
        Label titre = new Label("Supprimer la réservation");
        titre.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");
        Label msg = new Label("Voulez-vous vraiment supprimer\nla réservation #" + r.getIdReservation() + " ?");
        msg.setStyle("-fx-font-size: 13px; -fx-text-fill: #718096; -fx-text-alignment: center;");
        msg.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        Button btnAnnuler = new Button("Annuler");
        btnAnnuler.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #4a5568; -fx-font-size: 13px; " +
                            "-fx-font-weight: bold; -fx-padding: 11 35; -fx-cursor: hand; -fx-background-radius: 10;");
        btnAnnuler.setOnAction(e -> popup.close());

        Button btnSupp = new Button("🗑  Supprimer");
        btnSupp.setStyle("-fx-background-color: #e53e3e; -fx-text-fill: white; -fx-font-size: 13px; " +
                         "-fx-font-weight: bold; -fx-padding: 11 35; -fx-cursor: hand; -fx-background-radius: 10;");
        btnSupp.setOnAction(e -> {
            try {
                serviceReservation.supprimerReservation(r.getIdReservation());
                popup.close();
                chargerReservations();
                afficherToast("Réservation #" + r.getIdReservation() + " supprimée!", "#e53e3e", "🗑");
            } catch (SQLException ex) {
                popup.close();
                afficherToast("Erreur: " + ex.getMessage(), "#e53e3e", "❌");
            }
        });

        HBox btns = new HBox(15, btnAnnuler, btnSupp);
        btns.setAlignment(Pos.CENTER);
        card.getChildren().addAll(icone, titre, msg, btns);
        root.getChildren().add(card);

        popup.setScene(new Scene(root, 400, 300));
        popup.showAndWait();
    }

    // ─── Toast ───────────────────────────────────────────────────────────────

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
        Label lbl = new Label(message);
        lbl.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");
        lbl.setMaxWidth(300); lbl.setWrapText(true);
        box.getChildren().addAll(ico, lbl);

        Scene scene = new Scene(box);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        toast.setScene(scene);
        javafx.geometry.Rectangle2D screen = javafx.stage.Screen.getPrimary().getVisualBounds();
        toast.setX(screen.getMaxX() - 380);
        toast.setY(screen.getMaxY() - 100);
        toast.show();

        javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(javafx.util.Duration.millis(500), box);
        fade.setDelay(javafx.util.Duration.millis(2000));
        fade.setFromValue(1.0); fade.setToValue(0.0);
        fade.setOnFinished(e -> toast.close());
        fade.play();
    }

    // ─── Utilitaires ─────────────────────────────────────────────────────────

    private VBox fieldBox(String labelText, String value) {
        VBox box = new VBox(6);
        Label lbl = new Label(labelText);
        lbl.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #4a5568;");
        TextField tf = new TextField(value == null ? "" : value);
        tf.setStyle("-fx-padding: 11; -fx-font-size: 13px; -fx-border-color: #e2e8f0; " +
                    "-fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-background-color: white;");
        tf.setMaxWidth(Double.MAX_VALUE);
        box.getChildren().addAll(lbl, tf);
        return box;
    }
}
