package Controllor;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.event.ActionEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.scene.chart.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import services.ServiceRendezVous;
import Services.ServiceDossier;
import services.PDFService;
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
    @FXML private Label welcomeLabel, sidebarUserName, sidebarUserRole;

    // ===== IMAGE =====
    @FXML private ImageView logoImage;

    // ===== CHARTS =====
    @FXML private PieChart rdvPieChart;
    @FXML private BarChart<String, Number> rdvBarChart;

    // ===== ÉTATS =====
    private boolean rdvOpen = false;
    private boolean dossiersOpen = false;

    // ===== COMPOSANTS PRINCIPAUX =====
    @FXML private BorderPane rootPane;
    @FXML
    private StackPane contentArea;

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

        // Valeurs dynamiques pour les RDV (Session)
        int psyId = 6;
        if (utils.SessionManager.getInstance().isLoggedIn()) {
            models.User user = utils.SessionManager.getInstance().getCurrentUser();
            psyId = user.getId();
            
            // Mettre à jour les noms d'utilisateur
            if (welcomeLabel != null) welcomeLabel.setText("Bonjour, " + user.getPrenom() + " 👋");
            if (sidebarUserName != null) sidebarUserName.setText(user.getPrenom() + " " + user.getNom());
            if (sidebarUserRole != null) sidebarUserRole.setText(user.getRole());
        }
        var allRdvs = serviceRdv.getByPsychologueId(psyId);

        rdvCount.setText(String.valueOf(allRdvs.stream().filter(r -> "en attente".equalsIgnoreCase(r.getStatut()) || "réservé".equalsIgnoreCase(r.getStatut())).count()));
        int dossiersCount = serviceDossier.getByPsychologueId(psyId).size();
        dossierCount.setText(String.valueOf(dossiersCount));
        activiteCount.setText("8");

        sidebarPatientsCount.setText(String.valueOf(dossiersCount));
        sidebarRdvToday.setText(String.valueOf(allRdvs.stream().filter(r -> r.getDate() != null && r.getDate().toLocalDate().equals(LocalDate.now())).count()));
        sidebarDossiersTotal.setText(String.valueOf(dossiersCount));

        // Sous-menus cachés
        submenuRdv.setVisible(false);
        submenuRdv.setManaged(false);

        submenuDossiers.setVisible(false);
        submenuDossiers.setManaged(false);

        arrowRdv.setText("▶");
        arrowDossiers.setText("▶");

        // Initialiser les graphiques
        setupCharts(allRdvs);

        // Charger logo
        var stream = getClass().getResourceAsStream("/Images/logo.png");
        if (stream != null) {
            logoImage.setImage(new Image(stream));
        }

        // Actif par défaut
        setActive(navHome);
        
        setupResponsiveBehavior();
    }

    private void setupResponsiveBehavior() {
        javafx.application.Platform.runLater(() -> {
            if (navHome != null && navHome.getScene() != null) {
                navHome.getScene().widthProperty().addListener((obs, oldVal, newVal) -> {
                    adjustLayoutForWidth(newVal.doubleValue());
                });
                adjustLayoutForWidth(navHome.getScene().getWidth());
            }
        });
    }

    private void adjustLayoutForWidth(double width) {
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

    @FXML void goHome(javafx.event.Event event) { loadPage(event, "/DashboardPsyVue.fxml"); }
    @FXML void goActivites(javafx.event.Event event) { setActive(navActivites); }
    @FXML void goRessources(javafx.event.Event event) { setActive(navRessources); loadPage(event, "/StudentRessources.fxml"); }
    @FXML void goStats(javafx.event.Event event) { setActive(navStats); loadPage(event, "/StatsAdmin.fxml"); }

    private void loadPage(javafx.event.Event event, String path) {
        try {
            java.net.URL url = getClass().getResource(path);
            if (url == null) return;
            Parent root = FXMLLoader.load(url);
            Stage stage = null;
            if (event != null && event.getSource() instanceof Node) {
                stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            } else if (navHome != null && navHome.getScene() != null) {
                stage = (Stage) navHome.getScene().getWindow();
            }
            if (stage != null) {
                stage.setScene(new Scene(root));
                stage.show();
                stage.centerOnScreen();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML void goVoirRendezVous(javafx.event.Event event) { loadPage(event, "/VoirRendezVous.fxml"); }
    @FXML void goCalendrier(javafx.event.Event event) { loadPage(event, "/Calendrier.fxml"); }
    @FXML void goNouveauDossier(javafx.event.Event event) { loadPage(event, "/NouveauDossier.fxml"); }
    @FXML void goConsulterDossiers(javafx.event.Event event) { loadPage(event, "/ConsulterDossiers.fxml"); }

    @FXML
    void logout(javafx.event.Event event) {
        utils.SessionManager.getInstance().logout();
        loadPage(event, "/Login.fxml");
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
        dialog.showAndWait().ifPresent(d -> {
            try {
                List<RendezVous> history = serviceRdv.getByEtudiantId(d.getPatientId());
                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String fileName = "Dossier_" + d.getPatientNom().replace(" ", "_") + "_" + timestamp + ".pdf";
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
        dialog.showAndWait().ifPresent(period -> {
            try {
                int psyId = 6;
                if (utils.SessionManager.getInstance().isLoggedIn()) psyId = utils.SessionManager.getInstance().getCurrentUser().getId();
                List<RendezVous> all = serviceRdv.getByPsychologueId(psyId);
                List<RendezVous> filtered;
                LocalDate now = LocalDate.now();
                if ("Aujourd'hui".equals(period)) {
                    filtered = all.stream().filter(r -> r.getDate() != null && r.getDate().toLocalDate().equals(now)).collect(Collectors.toList());
                } else if ("Cette Semaine".equals(period)) {
                    LocalDate weekEnd = now.plusDays(7);
                    filtered = all.stream().filter(r -> r.getDate() != null && !r.getDate().toLocalDate().isBefore(now) && r.getDate().toLocalDate().isBefore(weekEnd)).collect(Collectors.toList());
                } else {
                    LocalDate monthEnd = now.plusMonths(1);
                    filtered = all.stream().filter(r -> r.getDate() != null && !r.getDate().toLocalDate().isBefore(now) && r.getDate().toLocalDate().isBefore(monthEnd)).collect(Collectors.toList());
                }
                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String fileName = "Planning_" + period.replace(" ", "_") + "_" + timestamp + ".pdf";
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

    private void setActive(HBox active) {
        HBox[] all = {navHome, navRdv, navDossiers, navActivites, navRessources, navStats};
        for (HBox h : all) {
            h.setStyle("-fx-padding: 11 14; -fx-background-radius: 10; -fx-cursor: hand; -fx-background-color: transparent;");
        }
        active.setStyle("-fx-padding: 11 14; -fx-background-radius: 10; -fx-cursor: hand; -fx-background-color: rgba(255,255,255,0.15);");
    }

    private void setupCharts(List<RendezVous> allRdvs) {
        if (rdvPieChart != null) {
            long confirmed = allRdvs.stream().filter(r -> "confirmé".equalsIgnoreCase(r.getStatut())).count();
            long pending = allRdvs.stream().filter(r -> "en attente".equalsIgnoreCase(r.getStatut()) || "réservé".equalsIgnoreCase(r.getStatut())).count();
            long free = allRdvs.stream().filter(r -> "libre".equalsIgnoreCase(r.getStatut()) || "disponible".equalsIgnoreCase(r.getStatut())).count();
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                new PieChart.Data("Confirmés", confirmed),
                new PieChart.Data("En attente", pending),
                new PieChart.Data("Libres", free)
            );
            rdvPieChart.setData(pieData);
        }
        if (rdvBarChart != null) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Rendez-vous");
            String[] jours = {"Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"};
            for (String j : jours) {
                series.getData().add(new XYChart.Data<>(j, (int)(Math.random() * 10)));
            }
            rdvBarChart.getData().add(series);
        }
    }
}
