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
    @FXML private HBox filterGreen;
    @FXML private HBox filterRed;

    private boolean showLibre = true;
    private boolean showReserved = true;

    private final ServiceRendezVous serviceRdv = new ServiceRendezVous();
    private int    psyId;
    private String psyNom;
    private int    etudiantId;
    private LocalDate        debutSemaine;
    private List<RendezVous> creneaux = new ArrayList<>();

    private static final int HEURE_DEBUT   = 8;
    private static final int HEURE_FIN     = 20;
    private static final int HAUTEUR_HEURE = 52;


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

    @FXML private void onPrevWeek(ActionEvent e)  { debutSemaine = debutSemaine.minusWeeks(1); chargerEtAfficher(); }
    @FXML private void onNextWeek(ActionEvent e)  { debutSemaine = debutSemaine.plusWeeks(1);  chargerEtAfficher(); }
    @FXML private void onToday(ActionEvent e)     { debutSemaine = lundiDeLaSemaine(LocalDate.now()); chargerEtAfficher(); }
    @FXML private void onVueSemaine(ActionEvent e){ /* déjà en vue semaine */ }
    @FXML private void onVueJour(ActionEvent e)   { debutSemaine = LocalDate.now(); chargerEtAfficher(); }


    private void chargerEtAfficher() {
        creneaux = serviceRdv.getSlotsWithVirtuals(psyId, debutSemaine, debutSemaine.plusDays(6));
        System.out.println("[ControllerRdvCalendrier] psyId=" + psyId + " → " + creneaux.size() + " créneaux chargés");

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


        colonneHeures.getChildren().clear();
        for (int h = 0; h <= (16 - 9) * 2 + 1; h++) {
            int totalM = h * 30;
            int hh = 9 + (totalM / 60);
            int mm = totalM % 60;
            if (hh > 16 || (hh == 16 && mm > 0)) break;
            
            Label lh = new Label(String.format("%02d:%02d", hh, mm));
            lh.setStyle("-fx-font-size: 11px; -fx-text-fill: #94A3B8; -fx-font-weight: 600;");
            lh.setPrefHeight(HAUTEUR_HEURE);
            lh.setAlignment(javafx.geometry.Pos.TOP_CENTER);
            lh.setPrefWidth(60);
            colonneHeures.getChildren().add(lh);
        }

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

            for (int h = 0; h <= (16 - 9) * 2 + 1; h++) {
                int totalM = h * 30;
                final int heure = 9 + (totalM / 60);
                final int minute = totalM % 60;
                if (heure > 16 || (heure == 16 && minute > 0)) break;
                VBox cellule = new VBox();
                cellule.setPrefHeight(HAUTEUR_HEURE);
                cellule.setAlignment(javafx.geometry.Pos.TOP_LEFT);
                cellule.setStyle(
                        "-fx-border-color: transparent transparent rgba(151,187,228,0.12) transparent;" +
                                "-fx-border-width: 0 0 1 0;"
                );

                Optional<RendezVous> rdvOpt = creneauxJour.stream()
                        .filter(r -> r.getHeureDebut() != null &&
                                r.getHeureDebut().toLocalTime().getHour() == heure &&
                                r.getHeureDebut().toLocalTime().getMinute() == minute)
                        .findFirst();

                if (rdvOpt.isPresent() && !"annulé".equalsIgnoreCase(rdvOpt.get().getStatut())) {
                    RendezVous r = rdvOpt.get();
                    String status = r.getStatut();
                    boolean isLibre = (r.getId() == -1 || "libre".equalsIgnoreCase(status));
                    boolean isReserved = "réservé".equalsIgnoreCase(status) || "confirmé".equalsIgnoreCase(status) || "en attente".equalsIgnoreCase(status);

                    if ((isLibre && showLibre) || (isReserved && showReserved)) {
                        cellule.getChildren().add(createCreneauBlock(r));
                    }
                }
                colonne.getChildren().add(cellule);
            }
            grilleCalendrier.getChildren().add(colonne);
        }

        FadeTransition ft = new FadeTransition(Duration.millis(250), grilleCalendrier);
        ft.setFromValue(0.7); ft.setToValue(1); ft.play();
    }

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
            case "confirmé", "réservé", "en attente" -> {
                bg = "rgba(231,76,60,0.15)"; textColor = "#C0392B"; border = "#E74C3C";
                iconeLabel = "🔴 Réservé";
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
            Parent root = FXMLLoader.load(getClass().getResource("/gui/Home.fxml"));
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

    @FXML public void toggleFilterGreen() {
        if (showLibre && !showReserved) {
            showLibre = true;
            showReserved = true;
        } else {
            showLibre = true;
            showReserved = false;
        }
        updateLegendOpacity();
        construireGrille();
    }

    @FXML public void toggleFilterRed() {
        if (showReserved && !showLibre) {
            showLibre = true;
            showReserved = true;
        } else {
            showLibre = false;
            showReserved = true;
        }
        updateLegendOpacity();
        construireGrille();
    }

    private void updateLegendOpacity() {
        filterGreen.setOpacity(showLibre ? 1.0 : 0.4);
        filterRed.setOpacity(showReserved ? 1.0 : 0.4);
    }

    private LocalDate lundiDeLaSemaine(LocalDate date) {
        return date.with(DayOfWeek.MONDAY);
    }
}