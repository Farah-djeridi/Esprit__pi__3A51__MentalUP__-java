package controllers;

import Models.RendezVous;
import Services.ServiceRendezVous;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;

public class ControllerRdvCalendrier {

    @FXML private Label     labelPsyNom;
    @FXML private Label     labelSemaine;
    @FXML private Label     labelUserName, avatarInitials;
    @FXML private HBox      headerJours;
    @FXML private HBox      grilleCalendrier;
    @FXML private VBox      colonneHeures;
    @FXML private Button    btnVueSemaine, btnVueJour;
    @FXML private ImageView logoImage;

    private final ServiceRendezVous serviceRdv = new ServiceRendezVous();
    private int    psyId;
    private String psyNom;
    private int    etudiantId;
    private LocalDate        debutSemaine;
    private List<RendezVous> creneaux = new ArrayList<>();

    private static final int HEURE_DEBUT   = 8;
    private static final int HEURE_FIN     = 20;
    private static final int HAUTEUR_HEURE = 52;

    // ══════════════════════════════════════════════════════
    //  INIT appelé depuis ControllerRdvEtudiant
    // ══════════════════════════════════════════════════════
    public void initData(int psyId, String psyNom, int etudiantId) {
        this.psyId       = psyId;
        this.psyNom      = psyNom;
        this.etudiantId  = etudiantId;
        this.debutSemaine = lundiDeLaSemaine(LocalDate.now());
        labelPsyNom.setText(psyNom);
        chargerEtAfficher();
    }

    @FXML
    public void initialize() {
        logoImage.setImage(new Image(getClass().getResourceAsStream("/Images/logo.png")));
    }

    // ══════════════════════════════════════════════════════
    //  NAVIGATION SEMAINE
    // ══════════════════════════════════════════════════════
    @FXML private void onPrevWeek(ActionEvent e)  { debutSemaine = debutSemaine.minusWeeks(1); chargerEtAfficher(); }
    @FXML private void onNextWeek(ActionEvent e)  { debutSemaine = debutSemaine.plusWeeks(1);  chargerEtAfficher(); }
    @FXML private void onToday(ActionEvent e)     { debutSemaine = lundiDeLaSemaine(LocalDate.now()); chargerEtAfficher(); }
    @FXML private void onVueSemaine(ActionEvent e){ /* déjà en vue semaine */ }
    @FXML private void onVueJour(ActionEvent e)   { debutSemaine = LocalDate.now(); chargerEtAfficher(); }

    // ══════════════════════════════════════════════════════
    //  CHARGEMENT + RENDU
    // ══════════════════════════════════════════════════════
    private void chargerEtAfficher() {
        creneaux = serviceRdv.getByPsychologueId(psyId);

        LocalDate finSemaine = debutSemaine.plusDays(6);
        String[] mois = {"jan.","fév.","mar.","avr.","mai","juin",
                "juil.","août","sep.","oct.","nov.","déc."};
        labelSemaine.setText(
                debutSemaine.getDayOfMonth() + " – " +
                        finSemaine.getDayOfMonth() + " " +
                        mois[finSemaine.getMonthValue()-1] + " " + finSemaine.getYear()
        );
        construireGrille();
    }

    private void construireGrille() {
        // ── En-têtes jours ──
        while (headerJours.getChildren().size() > 1)
            headerJours.getChildren().remove(1);

        for (int i = 0; i < 7; i++) {
            LocalDate jour    = debutSemaine.plusDays(i);
            boolean   isToday = jour.equals(LocalDate.now());

            VBox header = new VBox(2);
            header.setAlignment(javafx.geometry.Pos.CENTER);
            header.setPrefWidth(100);
            header.setStyle(
                    "-fx-padding: 10 4;" +
                            "-fx-border-color: transparent rgba(151,187,228,0.2) transparent transparent;" +
                            "-fx-border-width: 0 1 0 0;" +
                            (isToday ? "-fx-background-color: rgba(44,95,138,0.06);" : "")
            );

            String jourNom = jour.getDayOfWeek()
                    .getDisplayName(TextStyle.SHORT, Locale.FRENCH).toUpperCase();
            Label lNom = new Label(jourNom);
            lNom.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " +
                    (isToday ? "#2C5F8A;" : "#5A6C7D;"));

            Label lNum = new Label(String.valueOf(jour.getDayOfMonth()));
            lNum.setStyle(isToday
                    ? "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;" +
                      "-fx-background-color: #2C5F8A; -fx-background-radius: 50;" +
                      "-fx-min-width: 32; -fx-min-height: 32; -fx-alignment: center;"
                    : "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;"
            );

            header.getChildren().addAll(lNom, lNum);
            headerJours.getChildren().add(header);
        }

        // ── Colonne heures ──
        colonneHeures.getChildren().clear();
        for (int h = HEURE_DEBUT; h <= HEURE_FIN; h++) {
            Label lh = new Label(String.format("%02dh", h));
            lh.setStyle("-fx-font-size: 11px; -fx-text-fill: #94A3B8; -fx-font-weight: 600;");
            lh.setPrefHeight(HAUTEUR_HEURE);
            lh.setAlignment(javafx.geometry.Pos.TOP_CENTER);
            lh.setPrefWidth(60);
            colonneHeures.getChildren().add(lh);
        }

        // ── Colonnes jours ──
        while (grilleCalendrier.getChildren().size() > 1)
            grilleCalendrier.getChildren().remove(1);

        for (int i = 0; i < 7; i++) {
            LocalDate jour    = debutSemaine.plusDays(i);
            boolean   isToday = jour.equals(LocalDate.now());

            VBox colonne = new VBox(0);
            colonne.setPrefWidth(100);
            HBox.setHgrow(colonne, Priority.ALWAYS);
            colonne.setStyle(
                    "-fx-border-color: transparent rgba(151,187,228,0.15) transparent transparent;" +
                            "-fx-border-width: 0 1 0 0;" +
                            (isToday ? "-fx-background-color: rgba(44,95,138,0.03);" : "-fx-background-color: white;")
            );

            List<RendezVous> creneauxJour = creneaux.stream()
                    .filter(r -> r.getDate() != null && r.getDate().toLocalDate().equals(jour))
                    .toList();

            for (int h = HEURE_DEBUT; h <= HEURE_FIN; h++) {
                final int heure = h;
                VBox cellule = new VBox();
                cellule.setPrefHeight(HAUTEUR_HEURE);
                cellule.setAlignment(javafx.geometry.Pos.TOP_LEFT);
                cellule.setStyle(
                        "-fx-border-color: transparent transparent rgba(151,187,228,0.12) transparent;" +
                                "-fx-border-width: 0 0 1 0;"
                );

                Optional<RendezVous> rdvOpt = creneauxJour.stream()
                        .filter(r -> r.getHeureDebut() != null &&
                                r.getHeureDebut().toLocalTime().getHour() == heure)
                        .findFirst();

                if (rdvOpt.isPresent()) {
                    cellule.getChildren().add(createCreneauBlock(rdvOpt.get()));
                }
                colonne.getChildren().add(cellule);
            }
            grilleCalendrier.getChildren().add(colonne);
        }

        FadeTransition ft = new FadeTransition(Duration.millis(250), grilleCalendrier);
        ft.setFromValue(0.7); ft.setToValue(1); ft.play();
    }

    // ══════════════════════════════════════════════════════
    //  BLOC CRÉNEAU — "libre" est cliquable (= statut du psy)
    // ══════════════════════════════════════════════════════
    private VBox createCreneauBlock(RendezVous rdv) {
        String statut  = rdv.getStatut() != null ? rdv.getStatut().toLowerCase() : "";
        // "libre" = le psy a créé le créneau, il est disponible pour l'étudiant
        boolean isLibre = statut.equals("libre") || statut.equals("disponible");

        String bg, textColor, border, iconeLabel;
        switch (statut) {
            case "libre", "disponible" -> {
                bg = "rgba(39,174,96,0.15)"; textColor = "#1E8449"; border = "#27AE60";
                iconeLabel = "🟢 Libre";
            }
            case "confirmé" -> {
                bg = "rgba(41,128,185,0.15)"; textColor = "#1A5276"; border = "#2980B9";
                iconeLabel = "🔵 Confirmé";
            }
            case "réservé" -> {
                bg = "rgba(230,126,34,0.15)"; textColor = "#935116"; border = "#E67E22";
                iconeLabel = "🟠 Réservé";
            }
            default -> {
                bg = "rgba(149,165,166,0.15)"; textColor = "#616A6B"; border = "#95A5A6";
                iconeLabel = "⚫ " + statut;
            }
        }

        VBox bloc = new VBox(2);
        bloc.setPrefWidth(90);
        bloc.setPrefHeight(HAUTEUR_HEURE - 4);
        bloc.setStyle(
                "-fx-background-color: " + bg + "; -fx-background-radius: 6; -fx-padding: 5 7;" +
                        "-fx-border-color: " + border + "; -fx-border-radius: 6; -fx-border-width: 1;" +
                        (isLibre ? "-fx-cursor: hand;" : "-fx-cursor: default;")
        );

        String hD = rdv.getHeureDebut() != null ? rdv.getHeureDebut().toString().substring(0,5) : "--:--";
        String hF = rdv.getHeureFin()   != null ? rdv.getHeureFin().toString().substring(0,5)   : "--:--";

        Label lHeure = new Label(hD + " – " + hF);
        lHeure.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: " + textColor + ";");

        Label lStatut = new Label(iconeLabel);
        lStatut.setStyle("-fx-font-size: 10px; -fx-text-fill: " + textColor + ";");

        bloc.getChildren().addAll(lHeure, lStatut);

        if (isLibre) {
            bloc.setOnMouseEntered(e -> {
                ScaleTransition st = new ScaleTransition(Duration.millis(120), bloc);
                st.setToX(1.05); st.setToY(1.05); st.play();
            });
            bloc.setOnMouseExited(e -> {
                ScaleTransition st = new ScaleTransition(Duration.millis(120), bloc);
                st.setToX(1); st.setToY(1); st.play();
            });
            bloc.setOnMouseClicked(e -> ouvrirConfirmation(rdv));
        }

        return bloc;
    }

    // ══════════════════════════════════════════════════════
    //  POPUP CONFIRMATION
    // ══════════════════════════════════════════════════════
    private void ouvrirConfirmation(RendezVous rdv) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/gui/RendezVous_Confirmation.fxml")
            );
            Parent root = loader.load();

            ControllerRdvConfirmation ctrl = loader.getController();
            ctrl.initData(rdv, psyNom, etudiantId, this::chargerEtAfficher);

            Stage dialog = new Stage();
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.initOwner(labelPsyNom.getScene().getWindow());
            dialog.initStyle(StageStyle.TRANSPARENT);
            dialog.setTitle("Confirmer la réservation");

            Scene scene = new Scene(root);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            dialog.setScene(scene);
            dialog.show();

            ScaleTransition st = new ScaleTransition(Duration.millis(200), root);
            st.setFromX(0.85); st.setFromY(0.85);
            st.setToX(1); st.setToY(1); st.play();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ══════════════════════════════════════════════════════
    //  NAVIGATION
    // ══════════════════════════════════════════════════════
    @FXML
    private void onRetour(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/gui/RendezVous_Etudiant.fxml"));
            Stage stage = (Stage) labelPsyNom.getScene().getWindow();
            stage.setScene(new Scene(root));
            FadeTransition ft = new FadeTransition(Duration.millis(300), root);
            ft.setFromValue(0); ft.setToValue(1); ft.play();
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void onNavHomeClicked(MouseEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Home.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root)); stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void onNavRdvClicked(MouseEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/gui/RendezVous_Etudiant.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root)); stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

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
    @FXML private void onLogout(ActionEvent e) {}

    private LocalDate lundiDeLaSemaine(LocalDate date) {
        return date.with(DayOfWeek.MONDAY);
    }
}