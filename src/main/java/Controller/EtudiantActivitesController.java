package Controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
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

    @FXML
    private GridPane activitesGrid;
    @FXML
    private TextField searchField;

    private ServiceActivite serviceActivite;
    private ServiceReservation serviceReservation;
    private List<Activite> toutesActivites = new java.util.ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        serviceActivite = new ServiceActivite();
        serviceReservation = new ServiceReservation();
        chargerActivites();

        // Recherche en temps réel
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
        int column = 0, row = 0;
        for (Activite activite : activites) {
            VBox card = creerCarteActivite(activite);
            activitesGrid.add(card, column, row);
            column++;
            if (column == 2) { column = 0; row++; }
        }
    }

    private VBox creerCarteActivite(Activite activite) {
        VBox card = new VBox(12);
        card.setAlignment(Pos.TOP_LEFT);
        card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 15; " +
                      "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3); -fx-pref-width: 380;");

        // Badge type
        Label typeLabel = new Label(getIconeType(activite.getType()) + "  " + activite.getType());
        typeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b9fb5; -fx-font-weight: bold; " +
                           "-fx-background-color: #e8f4f8; -fx-padding: 5 10; -fx-background-radius: 15;");

        // Titre
        Label titreLabel = new Label(activite.getTitre());
        titreLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2d3748; -fx-wrap-text: true;");
        titreLabel.setMaxWidth(340);

        // Description
        Label descLabel = new Label(activite.getDescription());
        descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #718096; -fx-wrap-text: true;");
        descLabel.setMaxWidth(340);
        descLabel.setMaxHeight(50);

        // Adresse
        Label adresseLabel = new Label("📍 " + activite.getAdresse());
        adresseLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #4a5568;");

        // Dates
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        Label datesLabel = new Label("📅 Du " + activite.getDateDebut().format(fmt) + " au " + activite.getDateFin().format(fmt));
        datesLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #4a5568;");

        // Nombre de réservations
        int nbRes = 0;
        try { nbRes = serviceReservation.getNombreReservations(activite.getIdActivite()); } catch (SQLException ignored) {}
        Label resLabel = new Label("👥 " + nbRes + " réservation(s)");
        resLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #718096;");

        // Statut disponible/terminé
        boolean disponible = LocalDate.now().isBefore(activite.getDateFin());
        Label statutLabel = new Label(disponible ? "✅ Disponible !" : "❌ Terminée");
        statutLabel.setStyle(disponible
                ? "-fx-background-color: #e6f9f0; -fx-text-fill: #27ae60; -fx-padding: 6 14; -fx-background-radius: 8; -fx-font-weight: bold; -fx-font-size: 12px;"
                : "-fx-background-color: #f0f0f0; -fx-text-fill: #999; -fx-padding: 6 14; -fx-background-radius: 8; -fx-font-weight: bold; -fx-font-size: 12px;");

        // Bouton Réserver
        Button btnReserver = new Button("🎟 Réserver");
        btnReserver.setDisable(!disponible);
        btnReserver.setStyle(disponible
                ? "-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 10 25; -fx-cursor: hand; -fx-background-radius: 8;"
                : "-fx-background-color: #ccc; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 10 25; -fx-background-radius: 8;");
        btnReserver.setOnAction(e -> ouvrirSelectionPlace(activite));

        // Bouton Ticket
        Button btnTicket = new Button("🎫");
        btnTicket.setStyle("-fx-background-color: #5a67d8; -fx-text-fill: white; -fx-font-size: 14px; " +
                           "-fx-padding: 10 14; -fx-cursor: hand; -fx-background-radius: 8;");
        btnTicket.setOnMouseEntered(e -> btnTicket.setStyle(
                "-fx-background-color: #434190; -fx-text-fill: white; -fx-font-size: 14px; " +
                "-fx-padding: 10 14; -fx-cursor: hand; -fx-background-radius: 8;"));
        btnTicket.setOnMouseExited(e -> btnTicket.setStyle(
                "-fx-background-color: #5a67d8; -fx-text-fill: white; -fx-font-size: 14px; " +
                "-fx-padding: 10 14; -fx-cursor: hand; -fx-background-radius: 8;"));
        btnTicket.setOnAction(e -> afficherTicket(activite));

        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.getChildren().addAll(statutLabel, btnReserver, btnTicket);
        VBox.setMargin(footer, new Insets(8, 0, 0, 0));

        card.getChildren().addAll(typeLabel, titreLabel, descLabel, adresseLabel, datesLabel, resLabel, footer);
        return card;
    }

    private void ouvrirSelectionPlace(Activite activite) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Sélection de place - " + activite.getTitre());

        VBox root = new VBox(20);
        root.setStyle("-fx-background-color: white; -fx-padding: 30;");
        root.setAlignment(Pos.TOP_CENTER);

        // Titre popup
        Label titre = new Label("Sélection de place - " + activite.getTitre());
        titre.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");

        // Écran / Scène
        Label ecran = new Label("ÉCRAN / SCÈNE");
        ecran.setMaxWidth(Double.MAX_VALUE);
        ecran.setAlignment(Pos.CENTER);
        ecran.setStyle("-fx-background-color: #2d3748; -fx-text-fill: white; -fx-font-size: 14px; " +
                       "-fx-font-weight: bold; -fx-padding: 12; -fx-background-radius: 8;");

        // Grille des places
        GridPane grille = new GridPane();
        grille.setHgap(8);
        grille.setVgap(8);
        grille.setAlignment(Pos.CENTER);

        String[] rangees = {"A","B","C","D","E","F","G","H","I","J"};
        int nbColonnes = 12;

        List<String> placesReservees;
        try {
            placesReservees = serviceReservation.getPlacesReservees(activite.getIdActivite());
        } catch (SQLException e) {
            placesReservees = List.of();
        }

        final String[] placeSelectionnee = {null};

        // Label place sélectionnée
        Label lblSelection = new Label("Place sélectionnée: Aucune");
        lblSelection.setStyle("-fx-font-size: 13px; -fx-text-fill: #4a5568; -fx-font-weight: bold;");

        final List<String> placesReserveesFinal = placesReservees;

        for (int r = 0; r < rangees.length; r++) {
            // Label rangée
            Label lblRangee = new Label(rangees[r]);
            lblRangee.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #4a5568; -fx-pref-width: 20;");
            grille.add(lblRangee, 0, r);

            for (int c = 1; c <= nbColonnes; c++) {
                String codePlace = rangees[r] + c;
                Button btnPlace = new Button(String.valueOf(c));
                btnPlace.setPrefSize(38, 38);

                boolean reservee = placesReserveesFinal.contains(codePlace);

                if (reservee) {
                    btnPlace.setStyle("-fx-background-color: #bdc3c7; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-size: 11px;");
                    btnPlace.setDisable(true);
                } else {
                    btnPlace.setStyle("-fx-background-color: #6b9fb5; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-size: 11px; -fx-cursor: hand;");
                    btnPlace.setOnAction(ev -> {
                        // Reset toutes les places disponibles
                        grille.getChildren().forEach(node -> {
                            if (node instanceof Button b && !b.isDisabled()) {
                                b.setStyle("-fx-background-color: #6b9fb5; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-size: 11px; -fx-cursor: hand;");
                            }
                        });
                        // Sélectionner cette place
                        btnPlace.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-size: 11px; -fx-cursor: hand;");
                        placeSelectionnee[0] = codePlace;
                        lblSelection.setText("Place sélectionnée: " + codePlace);
                    });
                }
                grille.add(btnPlace, c, r);
            }
        }

        // Légende
        HBox legende = new HBox(20);
        legende.setAlignment(Pos.CENTER);
        Label lDispo = new Label("  Disponible");
        lDispo.setStyle("-fx-background-color: #6b9fb5; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 6;");
        Label lSelect = new Label("  Sélectionné");
        lSelect.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 6;");
        Label lIndispo = new Label("  Indisponible");
        lIndispo.setStyle("-fx-background-color: #bdc3c7; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 6;");
        legende.getChildren().addAll(lDispo, lSelect, lIndispo);

        // Boutons footer
        Button btnAnnuler = new Button("Annuler");
        btnAnnuler.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #4a5568; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 10 25; -fx-cursor: hand; -fx-background-radius: 8;");
        btnAnnuler.setOnAction(e -> popup.close());

        Button btnConfirmer = new Button("✅ Confirmer la réservation");
        btnConfirmer.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 10 25; -fx-cursor: hand; -fx-background-radius: 8;");
        btnConfirmer.setOnAction(e -> {
            if (placeSelectionnee[0] == null) {
                afficherToast("Veuillez sélectionner une place!", "#ed8936", "⚠️");
                return;
            }
            try {
                Reservation res = new Reservation(activite.getIdActivite(), "Sophie Am.", placeSelectionnee[0], LocalDate.now());
                serviceReservation.ajouterReservation(res);
                popup.close();
                afficherToast("Place " + placeSelectionnee[0] + " réservée pour \"" + activite.getTitre() + "\" !", "#27ae60", "✅");
                chargerActivites();
            } catch (SQLException ex) {
                afficherToast("Erreur: " + ex.getMessage(), "#e53e3e", "❌");
            }
        });

        HBox footerBtns = new HBox(15);
        footerBtns.setAlignment(Pos.CENTER_RIGHT);
        footerBtns.getChildren().addAll(lblSelection, new Region() {{ HBox.setHgrow(this, Priority.ALWAYS); }}, btnAnnuler, btnConfirmer);

        root.getChildren().addAll(titre, ecran, grille, legende, footerBtns);

        Scene scene = new Scene(root, 750, 620);
        popup.setScene(scene);
        popup.showAndWait();
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

    private void afficherTicket(Activite activite) {
        Reservation res = null;
        try {
            res = serviceReservation.getReservationEtudiant(activite.getIdActivite(), "Sophie Am.");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (res == null) {
            afficherToast("Vous n'avez pas de réservation pour cette activité.", "#ed8936", "⚠️");
            return;
        }

        final Reservation reservation = res;
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Mon Ticket de Réservation");

        // Fond gris clair
        VBox outer = new VBox(15);
        outer.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 20;");
        outer.setAlignment(Pos.TOP_CENTER);

        Label titrePopup = new Label("Mon Ticket de Réservation");
        titrePopup.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");

        // Carte ticket avec dégradé violet
        VBox ticket = new VBox(12);
        ticket.setAlignment(Pos.TOP_CENTER);
        ticket.setStyle("-fx-background-color: linear-gradient(to bottom, #667eea, #764ba2); " +
                        "-fx-padding: 20; -fx-background-radius: 18; -fx-pref-width: 380; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0, 0, 6);");

        // Header ticket
        Label ticketHeader = new Label("🎫  TICKET DE RÉSERVATION");
        ticketHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label ticketActivite = new Label(activite.getTitre());
        ticketActivite.setStyle("-fx-font-size: 13px; -fx-text-fill: rgba(255,255,255,0.9);");

        // Séparateur pointillé
        Label sep1 = new Label("- - - - - - - - - - - - - - - - - - - - - - -");
        sep1.setStyle("-fx-text-fill: rgba(255,255,255,0.4); -fx-font-size: 11px;");

        // Grille infos
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(12);
        infoGrid.setVgap(8);
        infoGrid.setAlignment(Pos.CENTER);

        infoGrid.add(creerInfoBox("Date de début", activite.getDateDebut().format(fmt)), 0, 0);
        infoGrid.add(creerInfoBox("Date de fin", activite.getDateFin().format(fmt)), 1, 0);
        infoGrid.add(creerInfoBox("Réservé le", reservation.getDateReservation().format(fmt)), 0, 1);
        infoGrid.add(creerInfoBox("Réservation N°", "#" + reservation.getIdReservation()), 1, 1);

        // Place (grande)
        VBox placeBox = new VBox(3);
        placeBox.setAlignment(Pos.CENTER);
        placeBox.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-padding: 12; " +
                          "-fx-background-radius: 10; -fx-pref-width: 340;");
        Label lblPlaceTitre = new Label("PLACE");
        lblPlaceTitre.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(255,255,255,0.7); -fx-font-weight: bold;");
        Label lblPlaceVal = new Label(reservation.getPlace());
        lblPlaceVal.setStyle("-fx-font-size: 38px; -fx-font-weight: bold; -fx-text-fill: white;");
        placeBox.getChildren().addAll(lblPlaceTitre, lblPlaceVal);

        // Séparateur pointillé bas
        Label sep2 = new Label("- - - - - - - - - - - - - - - - - - - - - - -");
        sep2.setStyle("-fx-text-fill: rgba(255,255,255,0.4); -fx-font-size: 11px;");

        Label footer1 = new Label("Mental Up – Système de réservation");
        footer1.setStyle("-fx-font-size: 10px; -fx-text-fill: rgba(255,255,255,0.6);");
        Label footer2 = new Label("Présentez ce ticket le jour de l'activité");
        footer2.setStyle("-fx-font-size: 10px; -fx-text-fill: rgba(255,255,255,0.6);");

        ticket.getChildren().addAll(ticketHeader, ticketActivite, sep1, infoGrid, placeBox, sep2, footer1, footer2);

        // Boutons bas
        Button btnFermer = new Button("Fermer");
        btnFermer.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #4a5568; -fx-font-size: 13px; " +
                           "-fx-font-weight: bold; -fx-padding: 10 25; -fx-cursor: hand; -fx-background-radius: 8;");
        btnFermer.setOnAction(e -> popup.close());

        Button btnImprimer = new Button("🖨 Imprimer");
        btnImprimer.setStyle("-fx-background-color: #5a67d8; -fx-text-fill: white; -fx-font-size: 13px; " +
                             "-fx-font-weight: bold; -fx-padding: 10 25; -fx-cursor: hand; -fx-background-radius: 8;");
        btnImprimer.setOnAction(e -> {
            javafx.print.PrinterJob job = javafx.print.PrinterJob.createPrinterJob();
            if (job != null) {
                boolean ok = job.showPrintDialog(popup);
                if (ok) {
                    // Appliquer une mise à l'échelle pour que le ticket rentre dans la page
                    javafx.scene.transform.Scale scale = new javafx.scene.transform.Scale(0.75, 0.75);
                    ticket.getTransforms().add(scale);
                    boolean printed = job.printPage(ticket);
                    ticket.getTransforms().remove(scale);
                    if (printed) {
                        job.endJob();
                        afficherToast("Ticket imprimé avec succès!", "#27ae60", "✅");
                    } else {
                        afficherToast("Échec de l'impression.", "#e53e3e", "❌");
                    }
                }
            } else {
                afficherToast("Aucune imprimante disponible.", "#e53e3e", "❌");
            }
        });

        HBox btnBox = new HBox(15, btnFermer, btnImprimer);
        btnBox.setAlignment(Pos.CENTER_RIGHT);

        outer.getChildren().addAll(titrePopup, ticket, btnBox);

        popup.setScene(new Scene(outer, 460, 580));
        popup.showAndWait();
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

        Label ico = new Label(icone);
        ico.setStyle("-fx-font-size: 18px;");
        Label lbl = new Label(message);
        lbl.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");
        lbl.setMaxWidth(300);
        lbl.setWrapText(true);
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
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.setOnFinished(e -> toast.close());
        fade.play();
    }

    private VBox creerInfoBox(String label, String valeur) {
        VBox box = new VBox(3);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-padding: 10 15; -fx-background-radius: 8; -fx-pref-width: 155;");
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 10px; -fx-text-fill: rgba(255,255,255,0.7);");
        Label val = new Label(valeur);
        val.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");
        box.getChildren().addAll(lbl, val);
        return box;
    }
}
