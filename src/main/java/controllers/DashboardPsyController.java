package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.event.ActionEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import Services.ServiceRendezVous;
import Services.ServiceDossier;
import Services.PDFService;
import Models.Dossier;
import Models.RendezVous;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    @FXML private Label currentDate;
    @FXML private Label rdvCount, dossierCount, activiteCount;
    @FXML private Label sidebarPatientsCount, sidebarRdvToday, sidebarDossiersTotal;

    // ===== IMAGE =====
    @FXML private ImageView logoImage;

    // ===== ÉTATS =====
    private boolean rdvOpen = false;
    private boolean dossiersOpen = false;

    // ===== COMPOSANTS PRINCIPAUX =====
    @FXML private BorderPane rootPane;

    @FXML private ScrollPane mainScrollPane;
    private ServiceRendezVous serviceRdv = new ServiceRendezVous();
    private ServiceDossier serviceDossier = new ServiceDossier();
    private PDFService pdfService = new PDFService();

    // ===== INITIALIZE =====
    @FXML
    public void initialize() {

        // Date actuelle
        currentDate.setText(
                new SimpleDateFormat("EEEE d MMMM yyyy").format(new Date())
        );

        // Valeurs dynamiques pour les RDV (ID statique 6)
        int psyId = 6;
        var allRdvs = serviceRdv.getByPsychologueId(psyId);

        rdvCount.setText(String.valueOf(allRdvs.stream().filter(r -> "en attente".equalsIgnoreCase(r.getStatut()) || "réservé".equalsIgnoreCase(r.getStatut())).count()));
        dossierCount.setText("24"); // À dynamiser plus tard
        activiteCount.setText("8");

        sidebarPatientsCount.setText("24");
        sidebarRdvToday.setText(String.valueOf(allRdvs.stream().filter(r -> r.getDate() != null && r.getDate().toLocalDate().equals(LocalDate.now())).count()));
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

        // Ajouter un listener pour le redimensionnement de la scène
        setupResponsiveBehavior();
    }

    // ===== GESTION RESPONSIVE =====
    private void setupResponsiveBehavior() {
        // Attendre que la scène soit disponible
        if (navHome != null && navHome.getScene() != null) {
            navHome.getScene().widthProperty().addListener((obs, oldVal, newVal) -> {
                adjustLayoutForWidth(newVal.doubleValue());
            });
        } else {
            // Si la scène n'est pas encore disponible, utiliser un listener sur la fenêtre
            javafx.application.Platform.runLater(() -> {
                if (navHome != null && navHome.getScene() != null) {
                    navHome.getScene().widthProperty().addListener((obs, oldVal, newVal) -> {
                        adjustLayoutForWidth(newVal.doubleValue());
                    });
                    // Appliquer immédiatement
                    adjustLayoutForWidth(navHome.getScene().getWidth());
                }
            });
        }
    }

    private void adjustLayoutForWidth(double width) {

        // Ajuster le padding du contenu principal
        if (mainScrollPane != null && mainScrollPane.getContent() != null) {
            VBox content = (VBox) mainScrollPane.getContent();
            if (width < 1000) {
                content.setStyle("-fx-padding: 20 24 24 24;");
            } else if (width < 1300) {
                content.setStyle("-fx-padding: 24 28 28 28;");
            } else {
                content.setStyle("-fx-padding: 28 32 32 32;");
            }
        }
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

        try {
            Parent root = FXMLLoader.load(getClass().getResource("/gui/DashboardPsyVue.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void goActivites(MouseEvent event) {
        setActive(navActivites);

        System.out.println("Activités");
    }

    @FXML
    void goRessources(MouseEvent event) {
        setActive(navRessources);

        System.out.println("Ressources");
    }

    @FXML
    void goStats(MouseEvent event) {
        setActive(navStats);

        System.out.println("Statistiques");
    }

    // ===== SOUS-MENU ACTIONS =====

    @FXML
    void goVoirRendezVous(MouseEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/gui/VoirRendezVous.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void goCalendrier(MouseEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/gui/Calendrier.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
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

    @FXML
    void handleExportPatientPDF(ActionEvent event) {
        List<Dossier> dossiers = serviceDossier.getAll();
        if (dossiers.isEmpty()) {
            showAlert("Info", "Aucun dossier patient trouvé.");
            return;
        }

        ChoiceDialog<Dossier> dialog = new ChoiceDialog<>(dossiers.get(0), dossiers);
        dialog.setTitle("Exporter Dossier Patient");
        dialog.setHeaderText("Sélectionnez un patient à exporter");
        dialog.setContentText("Patient :");

        Optional<Dossier> result = dialog.showAndWait();
        result.ifPresent(d -> {
            try {
                List<RendezVous> history = serviceRdv.getByEtudiantId(d.getPatientId());
                String fileName = "Dossier_" + d.getPatientNom().replace(" ", "_") + ".pdf";
                String path = System.getProperty("user.home") + "\\Desktop\\" + fileName;
                pdfService.generatePatientDossierPDF(d, history, path);
                showAlert("Succès", "Dossier exporté sur le bureau : " + fileName);
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Erreur", "Erreur lors de l'export : " + e.getMessage());
            }
        });
    }

    @FXML
    void handleExportPlanningPDF(ActionEvent event) {
        List<String> choices = List.of("Aujourd'hui", "Cette Semaine", "Ce Mois");
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Aujourd'hui", choices);
        dialog.setTitle("Exporter Planning");
        dialog.setHeaderText("Choisissez la période d'export");
        dialog.setContentText("Période :");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(period -> {
            try {
                List<RendezVous> all = serviceRdv.getByPsychologueId(6); // ID fixe pour démo
                List<RendezVous> filtered;
                LocalDate now = LocalDate.now();

                if ("Aujourd'hui".equals(period)) {
                    filtered = all.stream()
                        .filter(r -> r.getDate() != null && r.getDate().toLocalDate().equals(now))
                        .collect(Collectors.toList());
                } else if ("Cette Semaine".equals(period)) {
                    LocalDate weekEnd = now.plusDays(7);
                    filtered = all.stream()
                        .filter(r -> r.getDate() != null && !r.getDate().toLocalDate().isBefore(now) && r.getDate().toLocalDate().isBefore(weekEnd))
                        .collect(Collectors.toList());
                } else {
                    LocalDate monthEnd = now.plusMonths(1);
                    filtered = all.stream()
                        .filter(r -> r.getDate() != null && !r.getDate().toLocalDate().isBefore(now) && r.getDate().toLocalDate().isBefore(monthEnd))
                        .collect(Collectors.toList());
                }

                String fileName = "Planning_" + period.replace(" ", "_") + ".pdf";
                String path = System.getProperty("user.home") + "\\Desktop\\" + fileName;
                pdfService.generatePlanningPDF(filtered, period, path);
                showAlert("Succès", "Planning exporté sur le bureau : " + fileName);
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Erreur", "Erreur lors de l'export : " + e.getMessage());
            }
        });
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
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