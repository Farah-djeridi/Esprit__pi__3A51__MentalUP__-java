package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.event.ActionEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.scene.chart.PieChart;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import Models.RendezVous;
import Models.Dossier;
import Services.PDFService;
import Services.ServiceRendezVous;
import Services.ServiceDossier;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Locale;
import java.time.format.DateTimeFormatter;
import javafx.scene.control.Alert;

public class ControllerHomeAdmin {


    @FXML private HBox navAccueil, navSuivi, navForum, navRdv, navActivites, navContenus, navDossiers, navUtilisateurs;
    @FXML private HBox navSuiviStats, navObjectifs, navSujets, navCommentaires;
    @FXML private VBox submenuSuivi, submenuForum;
    @FXML private Label arrowSuivi, arrowForum;


    @FXML private Label statPatients, statPsychologues, statDossiers, statRdvs;
    @FXML private Label labelDate, labelUserName, avatarInitials;
    @FXML private Button notifButton, logoutButton;

    @FXML private ImageView logoImage;
    @FXML private PieChart pieChartRdvs;
    @FXML private PieChart pieChartDossiers;

    private ServiceRendezVous serviceRdv = new ServiceRendezVous();
    private ServiceDossier serviceDossier = new ServiceDossier();
    private boolean suiviOpen = false;
    private boolean forumOpen = false;

    @FXML
    public void initialize() {

        labelDate.setText(new java.text.SimpleDateFormat("EEEE d MMMM yyyy").format(new java.util.Date()));


        submenuSuivi.setVisible(false);
        submenuSuivi.setManaged(false);
        submenuForum.setVisible(false);
        submenuForum.setManaged(false);

        arrowSuivi.setText("▶");
        arrowForum.setText("▶");

        logoImage.setImage(new Image(getClass().getResourceAsStream("/Images/logo.png")));

        chargerDonneesStatistiques();
    }

    private void chargerDonneesStatistiques() {
        // Load Dossiers
        List<Dossier> dossiers = serviceDossier.getAll();
        statDossiers.setText(String.valueOf(dossiers.size()));

        long eleve = dossiers.stream().filter(d -> "élevé".equalsIgnoreCase(d.getNiveauRisque())).count();
        long moyen = dossiers.stream().filter(d -> "moyen".equalsIgnoreCase(d.getNiveauRisque())).count();
        long faible = dossiers.stream().filter(d -> "faible".equalsIgnoreCase(d.getNiveauRisque())).count();

        ObservableList<PieChart.Data> dossierData = FXCollections.observableArrayList(
            new PieChart.Data("Élevé (" + eleve + ")", eleve),
            new PieChart.Data("Moyen (" + moyen + ")", moyen),
            new PieChart.Data("Faible (" + faible + ")", faible)
        );
        pieChartDossiers.setData(dossierData);

        // Load RDVs
        List<RendezVous> rdvs = serviceRdv.getAll();
        statRdvs.setText(String.valueOf(rdvs.size()));

        long libres = rdvs.stream().filter(r -> "libre".equalsIgnoreCase(r.getStatut())).count();
        long reserves = rdvs.stream().filter(r -> "réservé".equalsIgnoreCase(r.getStatut()) || "en attente".equalsIgnoreCase(r.getStatut())).count();
        long confirmes = rdvs.stream().filter(r -> "confirmé".equalsIgnoreCase(r.getStatut())).count();

        ObservableList<PieChart.Data> rdvData = FXCollections.observableArrayList(
            new PieChart.Data("Libres (" + libres + ")", libres),
            new PieChart.Data("Réservés (" + reserves + ")", reserves),
            new PieChart.Data("Confirmés (" + confirmes + ")", confirmes)
        );
        pieChartRdvs.setData(rdvData);

        // Mock for missing services (Patients / Psychologues)
        statPatients.setText("1,234");
        statPsychologues.setText("45");
    }

    @FXML
    void handleDownloadGlobalPDF(ActionEvent event) {
        try {
            PDFService pdfService = new PDFService();
            Map<String, Object> stats = new HashMap<>();
            
            List<RendezVous> allRdvs = serviceRdv.getAll();
            List<Dossier> allDossiers = serviceDossier.getAll();

            stats.put("totalPatients", 1234); // Mock
            
            // RDV par mois
            Map<String, Integer> rdvPerMonth = new TreeMap<>();
            DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH);
            for (RendezVous r : allRdvs) {
                if (r.getDate() != null) {
                    String month = r.getDate().toLocalDate().format(monthFormatter);
                    rdvPerMonth.put(month, rdvPerMonth.getOrDefault(month, 0) + 1);
                }
            }
            stats.put("rdvPerMonth", rdvPerMonth);
            
            // Taux d'annulation
            long annulés = allRdvs.stream().filter(r -> "annulé".equalsIgnoreCase(r.getStatut())).count();
            double rate = allRdvs.isEmpty() ? 0 : (annulés * 100.0 / allRdvs.size());
            stats.put("cancellationRate", String.format("%.1f", rate));
            
            // Psys les plus sollicités (Mock)
            List<String> topPsys = java.util.Arrays.asList("Dr. Ahmed Mansour", "Dr. Sarah Ben Salem", "Dr. Yassine Trabelsi");
            stats.put("topPsys", topPsys);

            String fileName = "Rapport_Statistique_Global.pdf";
            String desktopPath = System.getProperty("user.home") + "\\Desktop\\" + fileName;

            pdfService.generateGlobalStatsPDF(stats, desktopPath);
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText(null);
            alert.setContentText("Le rapport statistique global a été généré sur votre bureau :\n" + desktopPath);
            alert.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Impossible de générer le PDF : " + e.getMessage());
            alert.showAndWait();
        }
    }



    @FXML
    void toggleSuiviMenu(MouseEvent event) {
        suiviOpen = !suiviOpen;
        submenuSuivi.setVisible(suiviOpen);
        submenuSuivi.setManaged(suiviOpen);
        arrowSuivi.setText(suiviOpen ? "▼" : "▶");
    }

    @FXML
    void toggleForumMenu(MouseEvent event) {
        forumOpen = !forumOpen;
        submenuForum.setVisible(forumOpen);
        submenuForum.setManaged(forumOpen);
        arrowForum.setText(forumOpen ? "▼" : "▶");
    }

    // ========== NAVIGATION PRINCIPALE ==========

    @FXML
    void onNavHomeClicked(MouseEvent event) {
        setActiveNav(navAccueil);
        System.out.println("Accueil Admin");
    }

    @FXML
    void onNavRdvClicked(MouseEvent event) {
        loadPage(event, "/gui/AdminRdv.fxml");
    }

    @FXML
    void onNavActivitesClicked(MouseEvent event) {
        setActiveNav(navActivites);
        System.out.println("Activités");
    }

    @FXML
    void onNavContenusClicked(MouseEvent event) {
        setActiveNav(navContenus);
        System.out.println("Contenus");
    }

    @FXML
    void onNavDossiersClicked(MouseEvent event) {
        loadPage(event, "/gui/AdminDossiers.fxml");
    }

    @FXML
    void onNavUtilisateursClicked(MouseEvent event) {
        setActiveNav(navUtilisateurs);
        System.out.println("Utilisateurs");
    }

    // ========== SOUS-MENUS ==========

    @FXML
    void onNavSuiviStatsClicked(MouseEvent event) {
        System.out.println("Statistiques de suivi");
    }

    @FXML
    void onNavObjectifsClicked(MouseEvent event) {
        System.out.println("Objectifs des utilisateurs");
    }

    @FXML
    void onNavSujetsClicked(MouseEvent event) {
        System.out.println("Gestion des sujets");
    }

    @FXML
    void onNavCommentairesClicked(MouseEvent event) {
        System.out.println("Gestion des commentaires");
    }

    // ========== HOVER EFFECTS ==========

    @FXML
    private void onNavHoverEnter(MouseEvent event) {
        HBox source = (HBox) event.getSource();
        if (!source.getStyle().contains("#34495E")) {
            source.setStyle("-fx-background-color: rgba(52,73,94,0.5); -fx-background-radius: 8; -fx-padding: 10 14; -fx-cursor: hand;");
        }
    }

    @FXML
    private void onNavHoverExit(MouseEvent event) {
        HBox source = (HBox) event.getSource();
        if (!source.getStyle().contains("#34495E")) {
            source.setStyle("-fx-background-color: transparent; -fx-background-radius: 8; -fx-padding: 10 14; -fx-cursor: hand;");
        }
    }

    @FXML
    private void onSubmenuHoverEnter(MouseEvent event) {
        HBox source = (HBox) event.getSource();
        source.setStyle("-fx-background-color: #34495E; -fx-background-radius: 6; -fx-padding: 8 12; -fx-cursor: hand;");
        for (javafx.scene.Node node : source.getChildren()) {
            if (node instanceof Label) node.setStyle("-fx-text-fill: white;");
        }
    }

    @FXML
    private void onSubmenuHoverExit(MouseEvent event) {
        HBox source = (HBox) event.getSource();
        source.setStyle("-fx-background-radius: 6; -fx-padding: 8 12; -fx-cursor: hand;");
        for (javafx.scene.Node node : source.getChildren()) {
            if (node instanceof Label) node.setStyle("-fx-text-fill: rgba(255,255,255,0.6);");
        }
    }


    @FXML
    private void onNotifications(ActionEvent event) { System.out.println("Notifications"); }

    @FXML
    private void onLogout(ActionEvent event) { System.out.println("Déconnexion"); }





    private void setActiveNav(HBox activeNav) {
        HBox[] allNavs = {navAccueil, navSuivi, navForum, navRdv, navActivites, navContenus, navDossiers, navUtilisateurs};
        for (HBox nav : allNavs) {
            if (nav != null) nav.setStyle("-fx-background-color: transparent; -fx-background-radius: 8; -fx-padding: 10 14; -fx-cursor: hand;");
        }
        if (activeNav != null) activeNav.setStyle("-fx-background-color: #34495E; -fx-background-radius: 8; -fx-padding: 10 14; -fx-cursor: hand;");
    }


    private void loadPage(MouseEvent event, String path) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(path));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }





}