package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.event.ActionEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DashboardPsyController {

    // ===== NAV PRINCIPAL =====
    @FXML private HBox navHome, navRdv, navDossiers, navActivites, navRessources, navStats;

    // ===== SOUS-MENUS =====
    @FXML private VBox submenuRdv, submenuDossiers;
    @FXML private Label arrowRdv, arrowDossiers;

    // ===== SOUS-MENU ITEMS =====
    @FXML private HBox navVoirRdv, navCalendrier;
    @FXML private HBox navConsulterDossiers, navNouveauDossier;

    // ===== LABELS =====
    @FXML private Label pageTitle, currentDate;
    @FXML private Label rdvCount, dossierCount, activiteCount;
    @FXML private Label sidebarPatientsCount, sidebarRdvToday, sidebarDossiersTotal;

    // ===== IMAGE =====
    @FXML private ImageView logoImage;

    // ===== ÉTATS =====
    private boolean rdvOpen = false;
    private boolean dossiersOpen = false;

    // ===== INITIALIZE =====
    @FXML
    public void initialize() {

        // Date actuelle
        currentDate.setText(
                new SimpleDateFormat("EEEE d MMMM yyyy").format(new Date())
        );

        // Valeurs par défaut
        rdvCount.setText("5");
        dossierCount.setText("20");
        activiteCount.setText("8");

        sidebarPatientsCount.setText("24");
        sidebarRdvToday.setText("5");
        sidebarDossiersTotal.setText("42");

        // Sous-menus cachés
        submenuRdv.setVisible(false);
        submenuRdv.setManaged(false);

        submenuDossiers.setVisible(false);
        submenuDossiers.setManaged(false);

        arrowRdv.setText("▶");
        arrowDossiers.setText("▶");

        // Charger logo
        var stream = getClass().getResourceAsStream("/Images/logo.png");
        if (stream != null) {
            logoImage.setImage(new Image(stream));
        } else {
            System.out.println("Logo introuvable !");
        }

        // Actif par défaut
        setActive(navHome);
    }

    // ===== TOGGLE MENUS =====

    @FXML
    void toggleRdvMenu(MouseEvent event) {
        rdvOpen = !rdvOpen;

        submenuRdv.setVisible(rdvOpen);
        submenuRdv.setManaged(rdvOpen);

        arrowRdv.setText(rdvOpen ? "▼" : "▶");
    }

    @FXML
    void toggleDossiersMenu(MouseEvent event) {
        dossiersOpen = !dossiersOpen;

        submenuDossiers.setVisible(dossiersOpen);
        submenuDossiers.setManaged(dossiersOpen);

        arrowDossiers.setText(dossiersOpen ? "▼" : "▶");
    }

    // ===== NAVIGATION =====

    @FXML
    void goHome(MouseEvent event) {
        setActive(navHome);
        pageTitle.setText("Tableau de bord");
        System.out.println("Accueil");
    }

    @FXML
    void goActivites(MouseEvent event) {
        setActive(navActivites);
        pageTitle.setText("Activités");
        System.out.println("Activités");
    }

    @FXML
    void goRessources(MouseEvent event) {
        setActive(navRessources);
        pageTitle.setText("Ressources");
        System.out.println("Ressources");
    }

    @FXML
    void goStats(MouseEvent event) {
        setActive(navStats);
        pageTitle.setText("Statistiques");
        System.out.println("Statistiques");
    }

    // ===== SOUS-MENU ACTIONS =====

    @FXML
    void goVoirRendezVous(MouseEvent event) {
        pageTitle.setText("Liste des rendez-vous");
        System.out.println("Voir RDV");
    }

    @FXML
    void goCalendrier(MouseEvent event) {
        pageTitle.setText("Calendrier");
        System.out.println("Calendrier");
    }




    @FXML
    void goNouveauDossier(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/NouveauDossier.fxml"));
            Parent root = loader.load();

            // récupérer la scène actuelle
            Stage stage = (Stage) navHome.getScene().getWindow();
            stage.setScene(new Scene(root));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void goConsulterDossiers(MouseEvent event) {

        try {
            Parent root = FXMLLoader.load(getClass().getResource("/gui/ConsulterDossiers.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===== ACTIONS =====

    @FXML
    void logout(ActionEvent event) {
        System.out.println("Déconnexion...");
    }


    // ===== STYLE ACTIF =====

    private void setActive(HBox active) {

        HBox[] all = {navHome, navRdv, navDossiers, navActivites, navRessources, navStats};

        for (HBox h : all) {
            h.setStyle(
                    "-fx-padding: 11 14;" +
                            "-fx-background-radius: 10;" +
                            "-fx-cursor: hand;" +
                            "-fx-background-color: transparent;"
            );
        }

        active.setStyle(
                "-fx-padding: 11 14;" +
                        "-fx-background-radius: 10;" +
                        "-fx-cursor: hand;" +
                        "-fx-background-color: rgba(255,255,255,0.15);"
        );
    }
}