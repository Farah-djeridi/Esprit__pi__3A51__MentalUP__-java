package Controllor;

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
import javafx.scene.control.Alert;
import Models.RendezVous;
import Models.Dossier;
import Services.PDFService;
import services.ServiceRendezVous;
import Services.ServiceDossier;
import services.ServiceUser;
import models.User;
import utils.SceneManager;
import utils.SessionManager;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Locale;
import java.time.format.DateTimeFormatter;

public class ControllerHomeAdmin {

    @FXML private HBox navAccueil, navSuivi, navForum, navRdv, navActivites, navContenus, navDossiers, navUtilisateurs, navRessources;
    @FXML private HBox navSuiviStats, navObjectifs, navSujets, navCommentaires;
    @FXML private VBox submenuSuivi, submenuForum;
    @FXML private Label arrowSuivi, arrowForum;
    @FXML private Label statPatients, statPsychologues, statDossiers, statRdvs, statUsers, statTopics, statMood;
    @FXML private Label labelDate, labelUserName, avatarInitials;
    @FXML private Button notifButton, logoutButton;
    @FXML private ImageView logoImage;
    @FXML private PieChart pieChartRdvs;
    @FXML private PieChart pieChartDossiers;

    private boolean suiviOpen = false;
    private boolean forumOpen = false;
    private final ServiceUser serviceUser = new ServiceUser();
    private final ServiceRendezVous serviceRdv = new ServiceRendezVous();
    private final ServiceDossier serviceDossier = new ServiceDossier();

    @FXML
    public void initialize() {
        // 1. Date du jour
        labelDate.setText(java.time.LocalDate.now()
                .format(java.time.format.DateTimeFormatter
                        .ofPattern("EEEE d MMMM yyyy", java.util.Locale.FRENCH)));

        // 2. Menus
        submenuSuivi.setVisible(false); submenuSuivi.setManaged(false);
        submenuForum.setVisible(false); submenuForum.setManaged(false);
        arrowSuivi.setText("▶"); arrowForum.setText("▶");

        // 3. Logo
        try { logoImage.setImage(new Image(getClass().getResourceAsStream("/Images/logo.png"))); }
        catch (Exception ignored) {}

        // 4. Infos admin connecté
        User user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            labelUserName.setText(user.getPrenom() + " " + user.getNom());
            String init = "";
            if (user.getPrenom() != null && !user.getPrenom().isEmpty()) init += user.getPrenom().charAt(0);
            if (user.getNom() != null && !user.getNom().isEmpty()) init += user.getNom().charAt(0);
            avatarInitials.setText(init.toUpperCase());
        }

        // 5. Statistiques
        chargerDonneesStatistiques();
    }

    private void chargerDonneesStatistiques() {
        try {
            // Dossiers
            List<Dossier> dossiers = serviceDossier.getAll();
            if (statDossiers != null) statDossiers.setText(String.valueOf(dossiers.size()));

            long eleve = dossiers.stream().filter(d -> "élevé".equalsIgnoreCase(d.getNiveauRisque())).count();
            long moyen = dossiers.stream().filter(d -> "moyen".equalsIgnoreCase(d.getNiveauRisque())).count();
            long faible = dossiers.stream().filter(d -> "faible".equalsIgnoreCase(d.getNiveauRisque())).count();

            if (pieChartDossiers != null) {
                ObservableList<PieChart.Data> dossierData = FXCollections.observableArrayList(
                    new PieChart.Data("Élevé (" + eleve + ")", eleve),
                    new PieChart.Data("Moyen (" + moyen + ")", moyen),
                    new PieChart.Data("Faible (" + faible + ")", faible)
                );
                pieChartDossiers.setData(dossierData);
            }

            // Rendez-vous
            List<RendezVous> rdvs = serviceRdv.getAll();
            if (statRdvs != null) statRdvs.setText(String.valueOf(rdvs.size()));

            long libres = rdvs.stream().filter(r -> "libre".equalsIgnoreCase(r.getStatut())).count();
            long reserves = rdvs.stream().filter(r -> "réservé".equalsIgnoreCase(r.getStatut()) || "en attente".equalsIgnoreCase(r.getStatut())).count();
            long confirmes = rdvs.stream().filter(r -> "confirmé".equalsIgnoreCase(r.getStatut())).count();

            if (pieChartRdvs != null) {
                ObservableList<PieChart.Data> rdvData = FXCollections.observableArrayList(
                    new PieChart.Data("Libres (" + libres + ")", libres),
                    new PieChart.Data("Réservés (" + reserves + ")", reserves),
                    new PieChart.Data("Confirmés (" + confirmes + ")", confirmes)
                );
                pieChartRdvs.setData(rdvData);
            }

            // Utilisateurs
            int totalUsers = serviceUser.getAll().size();
            if (statUsers != null) statUsers.setText(String.valueOf(totalUsers));
            if (statPatients != null) statPatients.setText("1,234"); // Mock or real if available
            if (statPsychologues != null) statPsychologues.setText("45"); // Mock

        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    void handleDownloadGlobalPDF(ActionEvent event) {
        try {
            PDFService pdfService = new PDFService();
            Map<String, Object> stats = new HashMap<>();
            List<RendezVous> allRdvs = serviceRdv.getAll();
            stats.put("totalPatients", 1234);
            Map<String, Integer> rdvPerMonth = new TreeMap<>();
            DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH);
            for (RendezVous r : allRdvs) {
                if (r.getDate() != null) {
                    String month = r.getDate().toLocalDate().format(monthFormatter);
                    rdvPerMonth.put(month, rdvPerMonth.getOrDefault(month, 0) + 1);
                }
            }
            stats.put("rdvPerMonth", rdvPerMonth);
            long annulés = allRdvs.stream().filter(r -> "annulé".equalsIgnoreCase(r.getStatut())).count();
            double rate = allRdvs.isEmpty() ? 0 : (annulés * 100.0 / allRdvs.size());
            stats.put("cancellationRate", String.format("%.1f", rate));
            List<String> topPsys = java.util.Arrays.asList("Dr. Ahmed Mansour", "Dr. Sarah Ben Salem", "Dr. Yassine Trabelsi");
            stats.put("topPsys", topPsys);

            String desktopPath = System.getProperty("user.home") + "\\Desktop\\Rapport_Statistique_Global.pdf";
            pdfService.generateGlobalStatsPDF(stats, desktopPath);
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setContentText("Rapport généré sur le bureau :\n" + desktopPath);
            alert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Échec de la génération : " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML void toggleSuiviMenu(MouseEvent event) {
        suiviOpen = !suiviOpen;
        submenuSuivi.setVisible(suiviOpen); submenuSuivi.setManaged(suiviOpen);
        arrowSuivi.setText(suiviOpen ? "▼" : "▶");
    }

    @FXML void toggleForumMenu(MouseEvent event) {
        forumOpen = !forumOpen;
        submenuForum.setVisible(forumOpen); submenuForum.setManaged(forumOpen);
        arrowForum.setText(forumOpen ? "▼" : "▶");
    }

    // Navigation
    @FXML void onNavHomeClicked(MouseEvent event)     { setActiveNav(navAccueil); }
    @FXML void onNavRdvClicked(MouseEvent event)      { loadPage(event, "/AdminRdv.fxml"); }
    @FXML void onNavActivitesClicked(MouseEvent event){ setActiveNav(navActivites); }
    @FXML void onNavContenusClicked(MouseEvent event) { SceneManager.switchTo("/AdminRessources.fxml", "Gestion des Ressources"); }
    @FXML void onNavDossiersClicked(MouseEvent event) { loadPage(event, "/AdminDossiers.fxml"); }
    @FXML void onNavUtilisateursClicked(MouseEvent event) { SceneManager.switchTo("/AdminUsers.fxml", "Gestion des Utilisateurs"); }
    @FXML void onNavRessourcesClicked(MouseEvent event) { SceneManager.switchTo("/AdminRessources.fxml", "Gestion des Ressources"); }
    
    @FXML void onNavSuiviStatsClicked(MouseEvent event)  { System.out.println("Stats suivi"); }
    @FXML void onNavObjectifsClicked(MouseEvent event)   { System.out.println("Objectifs"); }
    @FXML void onNavSujetsClicked(MouseEvent event)      { System.out.println("Sujets"); }
    @FXML void onNavCommentairesClicked(MouseEvent event){ System.out.println("Commentaires"); }

    @FXML private void onNavHoverEnter(MouseEvent event) {
        HBox src = (HBox) event.getSource();
        if (!src.getStyle().contains("#34495E"))
            src.setStyle("-fx-background-color: rgba(52,73,94,0.5); -fx-background-radius: 8; -fx-padding: 10 14; -fx-cursor: hand;");
    }
    @FXML private void onNavHoverExit(MouseEvent event) {
        HBox src = (HBox) event.getSource();
        if (!src.getStyle().contains("#34495E"))
            src.setStyle("-fx-background-color: transparent; -fx-background-radius: 8; -fx-padding: 10 14; -fx-cursor: hand;");
    }
    @FXML private void onSubmenuHoverEnter(MouseEvent event) {
        HBox src = (HBox) event.getSource();
        src.setStyle("-fx-background-color: #34495E; -fx-background-radius: 6; -fx-padding: 8 12; -fx-cursor: hand;");
    }
    @FXML private void onSubmenuHoverExit(MouseEvent event) {
        HBox src = (HBox) event.getSource();
        src.setStyle("-fx-background-radius: 6; -fx-padding: 8 12; -fx-cursor: hand;");
    }

    @FXML private void onNotifications(ActionEvent event) { System.out.println("Notifications"); }
    
    @FXML private void onLogout(ActionEvent event) {
        SessionManager.getInstance().logout();
        SceneManager.goToLogin();
    }

    @FXML private void onAddUser(ActionEvent event) { SceneManager.switchTo("AdminUsers.fxml", "Gestion des Utilisateurs"); }
    @FXML private void onViewStats(ActionEvent event) { SceneManager.switchTo("StatsAdmin.fxml", "Statistiques"); }

    @FXML private void onCreateRdv(ActionEvent event) { System.out.println("Créer RDV"); }
    @FXML private void onModerateForum(ActionEvent event) { System.out.println("Modérer forum"); }
    @FXML private void onExportData(ActionEvent event) { System.out.println("Exporter données"); }

    private void setActiveNav(HBox active) {
        HBox[] all = {navAccueil, navSuivi, navForum, navRdv, navActivites, navContenus, navDossiers, navUtilisateurs, navRessources};
        for (HBox n : all)
            if (n != null) n.setStyle("-fx-background-color: transparent; -fx-background-radius: 8; -fx-padding: 10 14; -fx-cursor: hand;");
        if (active != null)
            active.setStyle("-fx-background-color: #34495E; -fx-background-radius: 8; -fx-padding: 10 14; -fx-cursor: hand;");
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
